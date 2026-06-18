package fr.becpg.repo.regulatory.decernis;

import com.google.common.collect.Lists;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.activity.EntityActivityService;
import fr.becpg.repo.batch.BatchQueueService;
import fr.becpg.repo.batch.BatchStep;
import fr.becpg.repo.batch.BatchStepAdapter;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.RestTemplateHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ing.IngItem;
import fr.becpg.repo.product.data.ing.IngTypeItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.IngRegulatoryListDataItem;
import fr.becpg.repo.product.data.productList.RegulatoryListDataItem;
import fr.becpg.repo.regulatory.*;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.system.SystemConfigurationService;
import fr.becpg.util.MutexFactory;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static fr.becpg.repo.regulatory.decernis.RegulatoryHelper.extractIngName;

/**
 * <p>RegulatoryService class.</p>
 *
 * @author Valentin
 */
@Service
public class DecernisRegulatoryService extends AbstractRegulatoryService {
	private static final Log logger = LogFactory.getLog(DecernisRegulatoryService.class);

	private static final String MESSAGE_NO_CODE_CHARACT = "message.regulatory.charact.noCode";
	/** Constant <code>MESSAGE_DECERNIS_ERROR="message.decernis.error"</code> */
	private static final String MESSAGE_DECERNIS_ERROR = "message.decernis.error";

	/** Constant <code>PARAM_QUERY="query"</code> */
	private static final String PARAM_QUERY = "query";
	/** Constant <code>PARAM_PHRASE="phrase"</code> */
	private static final String PARAM_PHRASE = "phrase";
	/** Constant <code>PARAM_SCOPE_ID="scope_id"</code> */
	private static final String PARAM_SCOPE_ID = "scope_id";
	/** Constant <code>PARAM_RESULTS="results"</code> */
	private static final String PARAM_RESULTS = "results";
	/** Constant <code>PARAM_COMPANY="company"</code> */
	private static final String PARAM_COMPANY = "company";


	/** Constant <code>EUROPEAN_UNION="European Union"</code> */
	private static final String EUROPEAN_UNION = "European Union";
	/** Constant <code>FORMULATION_CHECK="FORMULATION_CHECK"</code> */
	private static final String FORMULATION_CHECK = "FORMULATION_CHECK";
	/** Constant <code>COSMETICS="COSMETICS"</code> */
	private static final String COSMETICS = "COSMETICS";
	/** Constant <code>STANDARDS_OF_IDENTITY_FOOD="STANDARDS_OF_IDENTITY_FOOD"</code> */
	private static final String STANDARDS_OF_IDENTITY_FOOD = "STANDARDS_OF_IDENTITY_FOOD";
	/** Constant <code>FOOD_ADDITIVES="FOOD_ADDITIVES"</code> */
	private static final String FOOD_ADDITIVES = "FOOD_ADDITIVES";

	/** Constant <code>GET_URL="GET url: "</code> */
	private static final String GET_URL = "GET url: ";

	/** Constant <code>moduleToCodeMap</code> */
	private static final Map<String, String> moduleToCodeMap = new HashMap<>();
	/** Constant <code>moduleToIDMap</code> */
	private static final Map<String, Integer> moduleToIDMap = new HashMap<>();

	static {
		moduleToCodeMap.put(FOOD_ADDITIVES, "ADD");
		moduleToCodeMap.put(STANDARDS_OF_IDENTITY_FOOD, "SOI");
		moduleToCodeMap.put(COSMETICS, "COS");
		moduleToCodeMap.put(FORMULATION_CHECK, "PC");
		moduleToIDMap.put(FOOD_ADDITIVES, 1);
		moduleToIDMap.put(STANDARDS_OF_IDENTITY_FOOD, 2);
		moduleToIDMap.put(COSMETICS, 9);
		moduleToIDMap.put(FORMULATION_CHECK, 100);
	}

	private ProductDataDecernisJsonService productDataDecernisJsonService;
	private Map<String, List<String>> functionsMap = new ConcurrentHashMap<>();
	private Map<String, String> functionsIdMap = new ConcurrentHashMap<>();


	/**
	 * <p>Constructor for RegulatoryService.</p>
	 *
	 * @param nodeService           a {@link NodeService} object
	 * @param alfrescoRepository    a {@link AlfrescoRepository} object
	 * @param formulationService    a {@link FormulationService} object
	 * @param batchQueueService     a {@link BatchQueueService} object
	 * @param policyBehaviourFilter a {@link BehaviourFilter} object
	 * @param entityActivityService a {@link EntityActivityService} object
	 * @param mutexFactory          a {@link MutexFactory} object
	 */
	public DecernisRegulatoryService(@Qualifier("nodeService") NodeService nodeService,
                                        AlfrescoRepository<RepositoryEntity> alfrescoRepository,
                                        FormulationService<FormulatedEntity> formulationService,
                                        BatchQueueService batchQueueService,
                                        @Qualifier("policyBehaviourFilter") BehaviourFilter policyBehaviourFilter,
                                        EntityActivityService entityActivityService,
                                        SystemConfigurationService systemConfigurationService,
                                        MutexFactory mutexFactory,
										ProductDataDecernisJsonService productDataDecernisJsonService) {
		super(nodeService, alfrescoRepository, formulationService, batchQueueService,
				policyBehaviourFilter, entityActivityService, mutexFactory, systemConfigurationService);
        this.productDataDecernisJsonService = productDataDecernisJsonService;
	}

	@Override
	protected String serverUrl() {
		return systemConfigurationService.confValue("beCPG.regulatory.decernis.serverUrl");
	}

	protected Boolean ingredientAnalysisEnabled() {
		return Boolean.parseBoolean(systemConfigurationService.confValue("beCPG.regulatory.decernis.ingredient.analysis.enabled"));
	}

	protected Integer maxCountriesPerRequest() {
		String confValue = systemConfigurationService.confValue("beCPG.regulatory.decernis.maxCountriesPerRequest");
		if (confValue != null && !confValue.isBlank()) {
			return Integer.parseInt(confValue);
		}
		return null;
	}

	protected Integer maxUsagesPerRequest() {
		String confValue = systemConfigurationService.confValue("beCPG.regulatory.decernis.maxUsagesPerRequest");
		if (confValue != null && !confValue.isBlank()) {
			return Integer.parseInt(confValue);
		}
		return null;
	}

	protected String analysisUrl() {
		return systemConfigurationService.confValue("beCPG.regulatory.decernis.analysisUrl");
	}

	protected String companyName() {
		return systemConfigurationService.confValue("beCPG.regulatory.decernis.companyName");
	}

	@Override
	protected String getToken() {
		return DecernisHelper.getToken().trim();
	}

	@Override
	protected RegulatoryContext createContext(ProductData product) {
		RegulatoryContext context = super.createContext(product);
		List<RegulatoryBatch> regulatoryBatches = new ArrayList<>();
		regulatoryBatches.addAll(createRegulatoryBatches(context, product));
		for (RegulatoryListDataItem regulatoryListItem : product.getRegulatoryList()) {
			regulatoryBatches.addAll(createRegulatoryBatches(context, regulatoryListItem));
		}
		context.setRegulatoryBatches(regulatoryBatches);
		return context;
	}

	private List<RegulatoryBatch> createRegulatoryBatches(RegulatoryContext context, RegulatoryEntity regulatoryEntity) {
		List<RegulatoryBatch> regulatoryBatches = new ArrayList<>();
		List<CountryBatch> countryBatches = createCountryBatches(context, regulatoryEntity);
		List<UsageBatch> usageBatches = createUsageBatches(context, regulatoryEntity);
		for (CountryBatch countryBatch : countryBatches) {
			for (UsageBatch usageBatch : usageBatches) {
				regulatoryBatches.add(new RegulatoryBatch(countryBatch, usageBatch));
			}
		}
		return regulatoryBatches;
	}

	private List<CountryBatch> createCountryBatches(RegulatoryContext context, RegulatoryEntity regulatoryEntity) {
		List<String> countries = new ArrayList<>();
		for (NodeRef countryRef : regulatoryEntity.getRegulatoryCountriesRef()) {
			addCountry(context, countries, countryRef);
		}
		return splitCountries(context, countries);
	}

	private List<CountryBatch> splitCountries(RegulatoryContext context, List<String> countries) {
		List<CountryBatch> countryBatches = new ArrayList<>();
		if (countries.contains(EUROPEAN_UNION)) {
			countries.remove(EUROPEAN_UNION);
			countryBatches.add(new CountryBatch(List.of(EUROPEAN_UNION)));
		}
		int ingListSize = context.getIngList().size();
		int maxCountries = 20;
		if (ingListSize >= 130) {
			maxCountries = 3;
		} else if (ingListSize >= 100) {
			maxCountries = 5;
		} else if (ingListSize >= 50) {
			maxCountries = 9;
		} else if (ingListSize >= 25) {
			maxCountries = 11;
		}
		Integer maxCountriesPerRequest = maxCountriesPerRequest();
		if (maxCountriesPerRequest != null) {
			maxCountries = Math.min(maxCountries, maxCountriesPerRequest);
		}
		List<List<String>> countriesBatches = Lists.partition(countries, maxCountries);
		for (List<String> countriesBatch : countriesBatches) {
			countryBatches.add(new CountryBatch(countriesBatch));
		}
		return countryBatches;
	}

	private void addCountry(RegulatoryContext context, List<String> countries, NodeRef countryRef) {
		String code = extractCode(countryRef);
		if (code != null && !code.isBlank()) {
			countries.add(code);
			context.addCountry(code, countryRef);
		} else {
			RequirementListDataItem noCodeRequirement = createReqCtrl(null,
					MLTextHelper.getI18NMessage(MESSAGE_NO_CODE_CHARACT), RequirementType.Tolerated);
			context.getRequirements().add(noCodeRequirement);
		}
	}

	private List<UsageBatch> createUsageBatches(RegulatoryContext context, RegulatoryEntity regulatoryEntity) {
		List<String> usages = new ArrayList<>();
		for (NodeRef usageRef : regulatoryEntity.getRegulatoryUsagesRef()) {
			addUsage(context, usages, usageRef);
		}
		return splitUsages(context, usages);
	}

	private List<UsageBatch> splitUsages(RegulatoryContext context, List<String> usages) {
		List<UsageBatch> usageBatches = new ArrayList<>();
		Map<String, List<String>> moduleToUsages = new HashMap<>();
		for (String usage : usages) {
			String usageModule = context.getUsageModule(usage);
			List<String> moduleUsages = moduleToUsages.computeIfAbsent(usageModule, k -> new ArrayList<>());
			moduleUsages.add(usage);
		}
		int maxUsages = 20;
		Integer maxUsagesPerRequest = maxUsagesPerRequest();
		if (maxUsagesPerRequest != null) {
			maxUsages = Math.min(maxUsages, maxUsagesPerRequest);
		}
		for (Map.Entry<String, List<String>> entry : moduleToUsages.entrySet()) {
			List<String> allUsages = entry.getValue();
			List<List<String>> usagesBatches = Lists.partition(new ArrayList<>(allUsages), maxUsages);
			for (List<String> usagesBatch : usagesBatches) {
				usageBatches.add(new UsageBatch(entry.getKey(), usagesBatch));
			}
		}
		return usageBatches;
	}

	private void addUsage(RegulatoryContext context, List<String> usages, NodeRef usageRef) {
		String code = extractCode(usageRef);
		if (code != null && !code.isBlank()) {
			usages.add(code);
			String moduleName = (String) nodeService.getProperty(usageRef, PLMModel.PROP_REGULATORY_MODULE);
			context.addUsage(code, moduleName);
		} else {
			RequirementListDataItem noCodeRequirement = createReqCtrl(null,
					MLTextHelper.getI18NMessage(MESSAGE_NO_CODE_CHARACT), RequirementType.Tolerated);
			context.getRequirements().add(noCodeRequirement);
		}
	}

	@Override
	protected void delegateSyncComplianceCheck(RegulatoryContext context) {
		fetchIngredients(context);
		for (RegulatoryBatch regulatoryCheckContext : context.getRegulatoryBatches()) {
			checkRecipe(context, regulatoryCheckContext);
		}
		finalizeRecipeCheck(context, context.getProduct());
		if (isIngRegulatoryListEnabled(context.getProduct())) {
			List<IngRegulatoryListDataItem> ingRegulatoryListDataItems = new ArrayList<>();
			for (RegulatoryBatch regulatoryCheckContext : context.getRegulatoryBatches()) {
				checkIngredients(context, regulatoryCheckContext);
			}
			processRegulatoryList(context.getProduct(), ingRegulatoryListDataItems);
		}
	}

	private void checkIngredients(RegulatoryContext context, RegulatoryBatch checkContext) {
		JSONObject ingredientAnalysisResults = null;
		try {
			ingredientAnalysisResults = ingredientAnalysis(context, checkContext);
		} catch (RestClientException e) {
			logger.error("Error during Decernis ingredients analysis: " + cleanError(e.getMessage()), e);
			RequirementListDataItem req = RequirementListDataItem.forbidden()
					.withMessage(MLTextHelper.getI18NMessage(MESSAGE_DECERNIS_ERROR, generateError(e)))
					.ofDataType(RequirementDataType.Formulation)
					.withFormulationChainId(REGULATORY_KEY);
			context.getRequirements().add(req);
		}
		if (ingredientAnalysisResults != null) {
			List<IngRegulatoryListDataItem> parseIngredientAnalysisResults = productDataDecernisJsonService.ingredientAnalysisParseResults(context, checkContext,
					ingredientAnalysisResults);
			context.getIngRegulatoryListDataItems().addAll(parseIngredientAnalysisResults);
		}
	}

	private JSONObject ingredientAnalysis(RegulatoryContext context, RegulatoryBatch regulatoryBatch) {
		JSONObject ingredientAnalysisResults = null;
		int retries = 2;
		while (ingredientAnalysisResults == null && retries >= 0) {
			try {
				retries--;
				JSONObject payload = productDataDecernisJsonService.ingredientAnalysisPreparePayload(context, regulatoryBatch);
				String url = serverUrl() + "/ingredient-analysis/transaction?report=tabular";
				HttpEntity<String> entity = createEntity(payload.toString());

				tracePostRequest(payload, url);
				String responseStr = RestTemplateHelper.getRestTemplateLongTimeout().postForObject(url, entity, String.class, new HashMap<>());
				ingredientAnalysisResults = new JSONObject(responseStr);
			} catch (RestClientException e) {
				if (retries <= 0) {
					throw e;
				}
				logger.error("Error during Decernis ingredient analysis: " + cleanError(e.getMessage())
						+ ", try restarting request...");
				ingredientAnalysisResults = null;
			}
		}
		return ingredientAnalysisResults;
	}

	private void checkRecipe(RegulatoryContext context, RegulatoryBatch regulatoryBatch) {

		if (logger.isDebugEnabled()) {
			logger.debug("Launch decernis in mode :" + context.getRegulatoryMode());
		}

		if (RegulatoryMode.BOTH.equals(context.getRegulatoryMode()) || RegulatoryMode.DECERNIS_ONLY.equals(context.getRegulatoryMode())) {
			createRecipe(context);
		}

		if (RegulatoryMode.BOTH.equals(context.getRegulatoryMode()) || RegulatoryMode.BECPG_ONLY.equals(context.getRegulatoryMode())) {
			JSONObject recipeAnalysisResults = null;
			try {
				recipeAnalysisResults = recipeAnalysis(context, regulatoryBatch);
			} catch (RestClientException e) {
				logger.error("Error during Decernis recipe analysis: " + cleanError(e.getMessage()), e);
				for (String country : regulatoryBatch.countryBatches().countries()) {
					for (String usage : regulatoryBatch.usageBatches().usages()) {
						RequirementListDataItem req = RequirementListDataItem.forbidden()
								.withMessage(MLTextHelper.getI18NMessage(MESSAGE_DECERNIS_ERROR, generateError(e)))
								.ofDataType(RequirementDataType.Formulation)
								.withFormulationChainId(REGULATORY_KEY)
								.withRegulatoryCode(country + (!usage.isEmpty() ? " - " + usage : ""));

						context.getRequirements().add(req);
					}
				}
			}
			if (recipeAnalysisResults != null) {
				List<RequirementListDataItem> parseRecipeAnalysisResults = productDataDecernisJsonService.recipeAnalysisParseResults(
						context, regulatoryBatch, recipeAnalysisResults, addInfoReqCtrl());
				context.getRequirements().addAll(parseRecipeAnalysisResults);
			}
			checkUsagesID(context);
		}
	}

	/**
	 * <p>checkUsagesID.</p>
	 *
	 * @param context a {@link RegulatoryContext} object
	 */
	private void checkUsagesID(RegulatoryContext context) {
		for (NodeRef usageRef : context.getProduct().getRegulatoryUsagesRef()) {
			String usageCode = (String) nodeService.getProperty(usageRef, PLMModel.PROP_REGULATORY_CODE);
			Integer moduleId = moduleToIDMap.get(context.getUsageModule(usageCode));
			updateUsageID(usageRef, usageCode, moduleId, serverUrl());
		}
		for (RegulatoryListDataItem regulatoryListItem : context.getProduct().getRegulatoryList()) {
			for (NodeRef usageRef : regulatoryListItem.getRegulatoryUsagesRef()) {
				String usageCode = (String) nodeService.getProperty(usageRef, PLMModel.PROP_REGULATORY_CODE);
				Integer moduleId = moduleToIDMap.get(context.getUsageModule(usageCode));
				updateUsageID(usageRef, usageCode, moduleId, serverUrl());
			}
		}
	}

	/**
	 * <p>updateUsageID.</p>
	 *
	 * @param usageRef  a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param usageCode a {@link java.lang.String} object
	 * @param moduleId  a {@link java.lang.Integer} object
	 * @param serverUrl
	 */
	public void updateUsageID(NodeRef usageRef, String usageCode, Integer moduleId, String serverUrl) {
		if (nodeService.getProperty(usageRef, PLMModel.PROP_REGULATORY_ID) instanceof String) {
			return;
		}
		String url = serverUrl + "/usages/structurized" + "?module_id=" + moduleId + "&phrase=" + usageCode;
		traceGetRequest(url);
		ResponseEntity<String> response = RestTemplateHelper.getRestTemplateLongTimeout().exchange(url, HttpMethod.GET, createEntity(null),
				String.class, new HashMap<>());
		if (HttpStatus.OK.equals(response.getStatusCode()) && response.getBody() != null) {
			productDataDecernisJsonService.getUsageId(usageCode, response).ifPresent(usageId ->
					nodeService.setProperty(usageRef, PLMModel.PROP_REGULATORY_ID, usageId));
		}
	}

	private JSONObject recipeAnalysis(RegulatoryContext context, RegulatoryBatch regulatoryBatch) {
		JSONObject recipeAnalysisResults = null;
		int retries = 2;
		while (recipeAnalysisResults == null && retries >= 0) {
			try {
				retries--;

				recipeAnalysisResults = postV5RecipeAnalysis(context, regulatoryBatch);
			} catch (RestClientException e) {
				if (retries <= 0) {
					throw e;
				}
				logger.error("Error during Decernis recipe analysis: " + cleanError(e.getMessage())
						+ ", try restarting request...");
				recipeAnalysisResults = null;
			}
		}
		return recipeAnalysisResults;
	}

	private JSONObject postV5RecipeAnalysis(RegulatoryContext context, RegulatoryBatch regulatoryBatch) {
		JSONArray ingredients = new JSONArray();

		String moduleCode = moduleToCodeMap.get(regulatoryBatch.usageBatches().module());

		for (IngListDataItem ingListDataItem : context.getIngList()) {

			IngItem ingItem = (IngItem) alfrescoRepository.findOne(ingListDataItem.getIng());

			for (IngTypeItem ingType : RegulatoryHelper.extractIngTypes(ingListDataItem, alfrescoRepository)) {
				productDataDecernisJsonService.buildIngredientJsonById(ingListDataItem, ingItem, getFunction(ingType, moduleCode))
						.ifPresent(ingredients::put);
			}
		}
		String code = (String) nodeService.getProperty(context.getProduct().getNodeRef(), BeCPGModel.PROP_CODE);
		JSONObject payload = productDataDecernisJsonService.recipeAnalysisPreparePayload(context, regulatoryBatch, code, ingredients, moduleCode);
		String url = analysisUrl() + "/recipe-analysis/transaction?report=tabular";
		HttpEntity<String> entity = createEntity(payload.toString());

		tracePostRequest(payload, url);
		String responseStr = RestTemplateHelper.getRestTemplateLongTimeout().postForObject(url, entity, String.class, new HashMap<>());
		return new JSONObject(responseStr);
	}

	private String getFunction(IngTypeItem ingType, String moduleCode) {
		String function = null;
		if (ingType != null) {
			String functionValue = ingType.getRegulatoryCode();
			if (functionValue != null) {
				function = findFunction(moduleCode, functionValue);
			}
			if (function == null) {
				functionValue = ingType.getLvCode();
				if (functionValue != null) {
					function = findFunction(moduleCode, functionValue);
				}
			}
			if (function == null) {
				functionValue = ingType.getLvValue();
				if (functionValue != null) {
					function = findFunction(moduleCode, functionValue);
				}
			}
		}
		return function;
	}

	/**
	 * <p>createRecipe.</p>
	 *
	 * @param context a {@link RegulatoryContext} object
	 */
	private void createRecipe(RegulatoryContext context) {
		try {
			JSONObject recipePayload = createRecipePayload(context);
			if (recipePayload != null) {
				String recipeId = context.getProduct().getRegulatoryRecipeId();
				HttpEntity<String> request = createEntity(recipePayload.toString());
				String url = serverUrl() + "/formulas";
				if (recipeId != null && !recipeId.isBlank()) {
					url += "/" + recipeId;
					if (logger.isTraceEnabled()) {
						logger.trace("PUT url: " + url + " body: " + recipePayload);
					}
					logger.debug("Update decernis recipe : " + recipeId);
					ResponseEntity<String> responseEntity = RestTemplateHelper.getRestTemplateLongTimeout()
							.exchange(url, HttpMethod.PUT, request, String.class);

					if (!responseEntity.getStatusCode().is2xxSuccessful()) {
						logger.debug("Error while updating recipe : " + recipeId + ", response is: " + responseEntity);
						recipeId = postRecipe(recipePayload, request, url);
					}
				} else {
					recipeId = postRecipe(recipePayload, request, url);
				}
				context.setRegulatoryRecipeId(recipeId);
			}
		} catch (RestClientException e) {
			logger.error(generateError(e), e);
			RequirementListDataItem req = RequirementListDataItem.forbidden()
					.withMessage(MLTextHelper.getI18NMessage(MESSAGE_DECERNIS_ERROR, generateError(e)))
					.ofDataType(RequirementDataType.Specification)
					.withFormulationChainId(REGULATORY_KEY);
			context.getRequirements().add(req);
		}
	}

	private JSONObject createRecipePayload(RegulatoryContext context) {
		JSONArray ingredients = new JSONArray();
		JSONObject ret = productDataDecernisJsonService.wrapIngredients(context, companyName(), ingredients);

		for (IngListDataItem ingListDataItem : context.getIngList()) {

			if (ingListDataItem.getIng() != null) {

				IngItem ingItem = (IngItem) alfrescoRepository.findOne(ingListDataItem.getIng());
				String ingName = extractIngName(ingItem);
				String rid = ingItem.getRegulatoryCode();

				Double ingQtyPerc = DecernisHelper.truncateDoubleValue(ingListDataItem.getQtyPerc());

				for (IngTypeItem ingType : RegulatoryHelper.extractIngTypes(ingListDataItem, alfrescoRepository)) {
					String function = null;
					if (ingType != null) {
						function = fetchFunctionId(ingType.getRegulatoryCode(), companyName(), serverUrl());
					}
					try {
						productDataDecernisJsonService.buildIngredientJsonByDid(rid, ingName, ingQtyPerc, function).ifPresent(ingredients::put);
					} catch (RestClientException e) {
						logger.warn("Cannot retrieve ingredient " + ingName + " error:" + e.getMessage());
					} catch (Exception e) {
						logger.error(e, e);
						throw new FormulateException("Unexpected decernis error: " + cleanError(e.getMessage()), e);
					}
				}
			}
		}
		if (ingredients.length() > 0) {
			return ret;
		}
		return null;
	}

	/**
	 * <p>postRecipe.</p>
	 *
	 * @param recipePayload a {@link org.json.JSONObject} object
	 * @param request a {@link org.springframework.http.HttpEntity} object
	 * @param url a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	private String postRecipe(JSONObject recipePayload, HttpEntity<String> request, String url) {
		tracePostRequest(recipePayload, url);
		String payload = RestTemplateHelper.getRestTemplateLongTimeout().postForObject(url, request, String.class);
		JSONObject jsonObject = new JSONObject(payload);
		String recipeId = null;
		if (jsonObject.has("id")) {
			recipeId = jsonObject.get("id").toString();
		}
		logger.debug("Create decernis recipe : " + recipeId);
		return recipeId;
	}

	private boolean isIngRegulatoryListEnabled(ProductData productData) {
		return alfrescoRepository.hasDataList(productData, PLMModel.TYPE_ING_REGULATORY_LIST) && productData.getIngRegulatoryList() != null
				&& ingredientAnalysisEnabled();
	}

	@Override
	protected List<BatchStep<RegulatoryBatch>> delegatePrepareAsyncSteps(RegulatoryContext context, NodeRef entityNodeRef) {
		List<BatchStep<RegulatoryBatch>> steps = new ArrayList<>();

		BatchStep<RegulatoryBatch> fetchIngredientsStep = new BatchStep<>();
		fetchIngredientsStep.setStepDescId("becpg.batch.regulatory.fetchIng");
		fetchIngredientsStep.setWorkProvider(regulatoryWorkProvider(List.of(new RegulatoryBatch(null, null))));
		fetchIngredientsStep.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<>() {
			public void process(RegulatoryBatch regulatoryCheckContext) throws Throwable {
				fetchIngredients(context);
			}
		});
		steps.add(fetchIngredientsStep);

		BatchStep<RegulatoryBatch> recipeStep = new BatchStep<>();
		recipeStep.setStepDescId("becpg.batch.regulatory.recipe");
		recipeStep.setWorkProvider(regulatoryWorkProvider(context.getRegulatoryBatches()));
		recipeStep.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<>() {
			public void process(RegulatoryBatch regulatoryCheckContext) throws Throwable {
				checkRecipe(context, regulatoryCheckContext);
			}
		});
		recipeStep.setBatchStepListener(new BatchStepAdapter() {
			@Override
			public void afterStep() {
				policyBehaviourFilter.disableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
				policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
				policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
				ProductData finalProductData = (ProductData) alfrescoRepository.findOne(entityNodeRef);
				finalizeRecipeCheck(context, finalProductData);
				alfrescoRepository.save(finalProductData);
			}
		});
		steps.add(recipeStep);

		if (isIngRegulatoryListEnabled(context.getProduct())) {
			BatchStep<RegulatoryBatch> ingredientsStep = new BatchStep<>();
			ingredientsStep.setWorkProvider(regulatoryWorkProvider(context.getRegulatoryBatches()));
			ingredientsStep.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<>() {
				public void process(RegulatoryBatch regulatoryCheckContext) throws Throwable {
					checkIngredients(context, regulatoryCheckContext);
				}
			});
			ingredientsStep.setBatchStepListener(new BatchStepAdapter() {
				@Override
				public void afterStep() {
					policyBehaviourFilter.disableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
					policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
					policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
					ProductData finalProductData = (ProductData) alfrescoRepository.findOne(entityNodeRef);
					processRegulatoryList(finalProductData, context.getIngRegulatoryListDataItems());
					alfrescoRepository.save(finalProductData);
				}
			});
			ingredientsStep.setStepDescId("becpg.batch.regulatory.ingredients");
			steps.add(ingredientsStep);
		}
		return steps;
	}

	@Override
	protected boolean isEnabled() {
		return Boolean.parseBoolean(systemConfigurationService.confValue("beCPG.regulatory.decernis.enabled")) &&
				serverUrl() != null && !serverUrl().isBlank() && analysisUrl() != null && !analysisUrl().isBlank();
	}

	private void fetchIngredients(RegulatoryContext context) {
		for (IngListDataItem ingListDataItem : context.getIngList()) {
			if (ingListDataItem.getIng() != null) {
				IngItem ingItem = (IngItem) alfrescoRepository.findOne(ingListDataItem.getIng());
				String rid = ingItem.getRegulatoryCode();
				if (rid == null || rid.isEmpty()) {
					rid = fetchIngredientId(ingListDataItem, companyName());
					if (logger.isDebugEnabled()) {
						logger.debug("Try to fetch ingredient ID: " + ingItem.getCharactName());
					}
					if (rid != null) {
						if (logger.isDebugEnabled()) {
							logger.debug("Found ingredient ID: " + ingItem.getCharactName() + ", ID: " + rid);
						}
					} else {
						if (logger.isDebugEnabled()) {
							logger.debug("Could not find ingredient ID: " + ingItem.getCharactName());
						}
						rid = UNKNOWN;
					}
					ingItem.setRegulatoryCode(rid);
					alfrescoRepository.save(ingItem);
				}
				if (UNKNOWN.equals(rid)) {
					RequirementListDataItem noCodeRequirement = createReqCtrl(ingListDataItem,
							MLTextHelper.getI18NMessage(MESSAGE_NO_CODE_CHARACT), RequirementType.Tolerated);
					context.getRequirements().add(noCodeRequirement);
				}
			}
		}
	}

	public String fetchIngredientId(IngListDataItem ingListDataItem, String companyName) {

		IngItem ingItem = (IngItem) alfrescoRepository.findOne(ingListDataItem.getIng());
		String ingName = RegulatoryHelper.extractIngName(ingItem);

		String ingredientId = null;

		String url = serverUrl() + "/ingredients?current_company={company}&q={query}&identifier_type={type}&limit=25";

		Map<String, String> params = new HashMap<>();
		params.put(PARAM_COMPANY, companyName);

		if (!productDataDecernisJsonService.buildQuery(ingListDataItem, params)) {
			params.put(PARAM_QUERY, ingName);
			params.put("type", "Name");
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Look for ingredients in decernis by " + params.get("type") + ": " + params.get(PARAM_QUERY));
		}

		if (logger.isTraceEnabled()) {
			logger.trace(GET_URL + url + " params: " + params);
		}

		ResponseEntity<String> response = RestTemplateHelper.getRestTemplateLongTimeout()
				.exchange(url, HttpMethod.GET, createEntity(null), String.class, params);

		if (HttpStatus.OK.equals(response.getStatusCode()) && (response.getBody() != null)) {
			ingredientId = productDataDecernisJsonService.parseIngredients(ingListDataItem, response, ingName, params, ingredientId);
		}
		return ingredientId;
	}

	private RequirementListDataItem createReqCtrl(IngListDataItem ingListDataItem, MLText reqCtrlMessage, RequirementType reqType) {
		RequirementListDataItem reqCtrlItem = new RequirementListDataItem();
		reqCtrlItem.setReqType(reqType);
		if (ingListDataItem != null) {
			reqCtrlItem.setCharact(ingListDataItem.getNodeRef() != null ? ingListDataItem.getNodeRef() : ingListDataItem.getIng());
			reqCtrlItem.addSource(ingListDataItem.getIng());
		}
		reqCtrlItem.setReqDataType(RequirementDataType.Specification);
		reqCtrlItem.setReqMlMessage(reqCtrlMessage);
		reqCtrlItem.setFormulationChainId(REGULATORY_KEY);
		return reqCtrlItem;
	}

	@Override
	protected String generateError(Exception e) {
		return "Error while creating Decernis recipe: " + cleanError(e.getMessage());
	}

	/**
	 * <p>traceGetRequest.</p>
	 *
	 * @param url a {@link java.lang.String} object
	 */
	private void traceGetRequest(String url) {
		if (logger.isTraceEnabled()) {
			logger.trace(GET_URL + url);
		}
	}

	/**
	 * <p>fetchFunctionId.</p>
	 *
	 * @param regulatoryCode a {@link java.lang.String} object
	 * @param companyName
	 * @param serverUrl
	 * @return a {@link java.lang.String} object
	 */
	private String fetchFunctionId(String regulatoryCode, String companyName, String serverUrl) {
		if (functionsIdMap.containsKey(regulatoryCode)) {
			return functionsIdMap.get(regulatoryCode);
		}
		String url = serverUrl + "/functions?current_company={company}&phrase={phrase}&module_id=1&limit=1";
		Map<String, String> params = new HashMap<>();
		params.put(PARAM_COMPANY, companyName);
		params.put(PARAM_PHRASE, regulatoryCode);
		if (logger.isTraceEnabled()) {
			logger.trace(GET_URL + url + " params: " + params);
		}
		ResponseEntity<String> response = RestTemplateHelper.getRestTemplateLongTimeout()
				.exchange(url, HttpMethod.GET, createEntity(null), String.class, params);
		if (HttpStatus.OK.equals(response.getStatusCode()) && (response.getBody() != null)) {
			JSONObject jsonObject = new JSONObject(response.getBody());
			if (jsonObject.has(PARAM_RESULTS)) {
				JSONArray results = jsonObject.getJSONArray(PARAM_RESULTS);
				if (results.length() > 0) {
					JSONObject result = results.getJSONObject(0);
					if (result.has(PARAM_SCOPE_ID)) {
						String functionId = result.get(PARAM_SCOPE_ID).toString();
						functionsIdMap.put(regulatoryCode, functionId);
						return functionId;
					}
				}
			}
		}
		return null;
	}

	/**
	 * <p>findFunction.</p>
	 *
	 * @param moduleCode   a {@link java.lang.String} object
	 * @param ingTypeValue a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	private String findFunction(String moduleCode, String ingTypeValue) {
		if (!functionsMap.containsKey(moduleCode)) {
			List<String> functions = fetchFunctions(moduleCode, analysisUrl());
			functionsMap.put(moduleCode, functions);
		}
		if (logger.isTraceEnabled()) {
			logger.trace("functionsMap=" + functionsMap);
		}
		for (String function : functionsMap.get(moduleCode)) {
			if (function.trim().equalsIgnoreCase(ingTypeValue.trim())) {
				return function;
			}
		}
		logger.warn("Ingredient function is not recognized by Decernis v5 API: " + ingTypeValue + ", available functions are: "
				+ functionsMap.get(moduleCode));
		return null;
	}

	/**
	 * <p>fetchFunctions.</p>
	 *
	 * @param moduleCode  a {@link java.lang.String} object
	 * @param analysisUrl
	 * @return a {@link java.util.List} object
	 */
	private List<String> fetchFunctions(String moduleCode, String analysisUrl) {

		List<String> functions = new ArrayList<>();

		String url = analysisUrl + "/scope/function?topic=" + moduleCode;

		HttpHeaders headers = new HttpHeaders();
		String token = getToken();
		if (token != null && !token.isBlank())
			headers.setBearerAuth(token);

		traceGetRequest(url);
		ResponseEntity<String> response = RestTemplateHelper.getRestTemplateLongTimeout().exchange(url, HttpMethod.GET, new HttpEntity<>(headers),
				String.class, new HashMap<>());

		if (HttpStatus.OK.equals(response.getStatusCode()) && (response.getBody() != null)) {
			JSONObject responseBody = new JSONObject(response.getBody());

			if (responseBody.has("functions")) {
				JSONArray functionArray = responseBody.getJSONArray("functions");
				for (int i = 0; i < functionArray.length(); i++) {
					functions.add(functionArray.getString(i));
				}
			}
		}
		return functions;
	}
}
