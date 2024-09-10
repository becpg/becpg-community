package fr.becpg.repo.decernis.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.decernis.DecernisAnalysisPlugin;
import fr.becpg.repo.decernis.DecernisMode;
import fr.becpg.repo.decernis.DecernisService;
import fr.becpg.repo.decernis.helper.DecernisHelper;
import fr.becpg.repo.decernis.model.RegulatoryContext;
import fr.becpg.repo.decernis.model.RegulatoryContextItem;
import fr.becpg.repo.decernis.model.UsageContext;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationChainPlugin;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.RestTemplateHelper;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RegulatoryEntity;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.IngRegulatoryListDataItem;
import fr.becpg.repo.product.data.productList.RegulatoryListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.system.SystemConfigurationService;
import fr.becpg.repo.variant.filters.VariantFilters;

/**
 * <p>DecernisServiceImpl class.</p>
 *
 * @author matthieu,valentin
 * @version $Id: $Id
 */
@Service("decernisService")
public class DecernisServiceImpl  extends AbstractLifecycleBean implements DecernisService, FormulationChainPlugin{

	private static final String FORMULATION_CHECK = "FORMULATION_CHECK";
	private static final String COSMETICS = "COSMETICS";
	private static final String STANDARDS_OF_IDENTITY_FOOD = "STANDARDS_OF_IDENTITY_FOOD";
	private static final String FOOD_ADDITIVES = "FOOD_ADDITIVES";

	private static final Log logger = LogFactory.getLog(DecernisServiceImpl.class);

	private static final String MESSAGE_NO_RID_ING = "message.decernis.ingredient.noRid";
	private static final String MESSAGE_NO_CODE_CHARACT = "message.decernis.charact.noCode";
	private static final String MESSAGE_SEVERAL_RID_ING = "message.decernis.ingredient.severalRid";

	private static final String PARAM_COMPANY = "company";
	private static final String PARAM_FORMULA = "formula";
	private static final String PARAM_COUNT = "count";
	private static final String PARAM_RESULTS = "results";
	private static final String PARAM_QUERY = "query";

	private static final String MISSING_VALUE = "NA";
	
	private final NodeService nodeService;

	private final DecernisAnalysisPlugin[] decernisPlugins;

	private final SystemConfigurationService systemConfigurationService;
	
	private final AlfrescoRepository<ProductData> alfrescoRepository;

	private static final Map<String, Integer> moduleIdMap = new HashMap<>();

	private static final Map<QName, String> ingNumbers = new HashMap<>();

	static {
		ingNumbers.put(PLMModel.PROP_CAS_NUMBER, "CAS");
		ingNumbers.put(PLMModel.PROP_EC_NUMBER, "EC No.");
		ingNumbers.put(PLMModel.PROP_CE_NUMBER, "EINECS");
		ingNumbers.put(PLMModel.PROP_FEMA_NUMBER, "FEMA No.");
		ingNumbers.put(PLMModel.PROP_FL_NUMBER, "FL No.");
		ingNumbers.put(PLMModel.PROP_FDA_NUMBER, "FDA Cat.");

		moduleIdMap.put(FOOD_ADDITIVES, 1);
		moduleIdMap.put(STANDARDS_OF_IDENTITY_FOOD, 2);
		moduleIdMap.put(COSMETICS, 9);
		moduleIdMap.put(FORMULATION_CHECK, 100);
	}

	/**
	 * <p>Constructor for DecernisServiceImpl.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 * @param decernisPlugins an array of {@link fr.becpg.repo.decernis.DecernisAnalysisPlugin} objects
	 * @param systemConfigurationService a {@link fr.becpg.repo.system.SystemConfigurationService} object
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object
	 */
	public DecernisServiceImpl(@Qualifier("nodeService") NodeService nodeService,
			DecernisAnalysisPlugin[] decernisPlugins, SystemConfigurationService systemConfigurationService, AlfrescoRepository<ProductData> alfrescoRepository) {
		super();
		this.nodeService = nodeService;
		this.decernisPlugins = decernisPlugins;
		this.systemConfigurationService = systemConfigurationService;
		this.alfrescoRepository = alfrescoRepository;
	}

	private String serverUrl() {
		return systemConfigurationService.confValue("beCPG.decernis.serverUrl");
	}

	private String companyName() {
		return systemConfigurationService.confValue("beCPG.decernis.companyName");
	}

	private String token() {
		return systemConfigurationService.confValue("beCPG.decernis.token");
	}
	
	private Boolean ingredientAnalysisEnabled() {
		return Boolean.parseBoolean(systemConfigurationService.confValue("beCPG.decernis.ingredient.analysis.enabled"));
	}

	/** {@inheritDoc} */
	@Override
	public String getChainId() {
		return DECERNIS_CHAIN_ID;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isChainActiveOnEntity(NodeRef entityNodeRef) {
		return isEnabled();
	}

	/** {@inheritDoc} */
	@Override
	public boolean isEnabled() {
		return serverUrl() != null && !serverUrl().isBlank() && token() != null && !token().isBlank();
	}
	
	@Override
	protected void onBootstrap(ApplicationEvent event) {
		if(isEnabled()) {
			String ttl = java.security.Security.getProperty("networkaddress.cache.ttl");
			
			if(ttl == null)
			{
				throw new IllegalStateException("To use decernis please set -Djava.security.properties=/usr/local/tomcat/decernis.security");
			}
			
			logger.info("Starting Decernis Module, DNS Cache is set to "+ttl+"s");
		}
	}

	/** {@inheritDoc} */
	@Override
	public List<ReqCtrlListDataItem> extractRequirements(ProductData product) {

		try {

			RegulatoryContext context = createContext(product);

			if (context.isTreatable()) {

				checkIngredients(context);

				boolean recipeCreated = false;
				
				logger.debug("Launch decernis in mode :"+ context.getRegulatoryMode());

				if (DecernisMode.BOTH.equals(context.getRegulatoryMode()) || DecernisMode.DECERNIS_ONLY.equals(context.getRegulatoryMode())) {
					createRecipe(context);
					updateContextItemsRecipeId(context);
					recipeCreated = true;
				}

				if (DecernisMode.BOTH.equals(context.getRegulatoryMode()) || DecernisMode.BECPG_ONLY.equals(context.getRegulatoryMode())) {
					if (getAnalysisPlugin().needsRecipeId() && !recipeCreated) {
						createRecipe(context);
					}
					analyzeRecipe(context);
					checkUsagesID(context);
				}

				if (DecernisMode.BECPG_ONLY.equals(context.getRegulatoryMode()) && context.getRegulatoryRecipeId() != null) {
					deleteRecipe(context.getRegulatoryRecipeId());
					context.getProduct().setRegulatoryRecipeId(null);
				}
			}

			if( alfrescoRepository.hasDataList(product, PLMModel.TYPE_ING_REGULATORY_LIST) 
					&& product.getIngRegulatoryList()!=null) {
				processRegulatoryList(context);
			}

			return context.getRequirements();

		} catch (Exception e) {
			throw new FormulateException("Unexpected decernis error: " + e.getMessage(), e);
		}
	}

	/**
	 * <p>processRegulatoryList.</p>
	 *
	 * @param context a {@link fr.becpg.repo.decernis.model.RegulatoryContext} object
	 */
	public void processRegulatoryList(RegulatoryContext context) {
		Map<NodeRef, Map<NodeRef, List<IngRegulatoryListDataItem>>> groupedByIngAndCountry = context.getIngRegulatoryList().stream().collect(
				Collectors.groupingBy(IngRegulatoryListDataItem::getIng, Collectors.groupingBy(item -> item.getRegulatoryCountries().get(0))));

		for (Map<NodeRef, List<IngRegulatoryListDataItem>> countryGroup : groupedByIngAndCountry.values()) {
			for (List<IngRegulatoryListDataItem> items : countryGroup.values()) {
				mergeItems(context, items);
			}
		}
		
		List<IngRegulatoryListDataItem> filteredList = context.getProduct().getIngRegulatoryList().stream()
		        .filter(item -> context.getIngRegulatoryList().stream()
		                .anyMatch(ingRegulatoryListDataItem ->
		                        Objects.equals(item.getIng(), ingRegulatoryListDataItem.getIng()) &&
		                                Objects.equals(item.getRegulatoryCountries(), ingRegulatoryListDataItem.getRegulatoryCountries())))
		        .collect(Collectors.toList());

		context.getProduct().getIngRegulatoryList().retainAll(filteredList);

	}

	private IngRegulatoryListDataItem mergeItems(RegulatoryContext context, List<IngRegulatoryListDataItem> items) {

		// Assuming all items have the same ing and country
		IngRegulatoryListDataItem sampleItem = items.get(0);

		IngRegulatoryListDataItem mergedItem = context.getProduct().getIngRegulatoryList().stream()
	        .filter(item ->
	                Objects.equals(item.getIng(), sampleItem.getIng()) &&
	                Objects.equals(item.getRegulatoryCountries(), sampleItem.getRegulatoryCountries()))
	        .findFirst()
	        .orElseGet(() -> {
	            IngRegulatoryListDataItem newItem = new IngRegulatoryListDataItem();
	            newItem.setIng(sampleItem.getIng());
	            newItem.setRegulatoryCountries(sampleItem.getRegulatoryCountries());
	            context.getProduct().getIngRegulatoryList().add(newItem);
	            return newItem;
	        });

		String citation = items.stream().map(item -> item.getCitation().getDefaultValue()).distinct().sorted().collect(Collectors.joining(";;"));
		String usages = items.stream().map(item -> item.getUsages().getDefaultValue()).distinct().sorted().collect(Collectors.joining(";;"));
		String restrictionLevels = items.stream().map(item -> item.getRestrictionLevels().getDefaultValue()).filter(r -> r != null && !r.isBlank() && !r.equals("-")).distinct().sorted().collect(Collectors.joining(";;"));
		String resultIndicators = items.stream().map(item -> item.getResultIndicator().getDefaultValue()).distinct().sorted().collect(Collectors.joining(";;"));
		String precautions = items.stream().map(item -> item.getPrecautions().getDefaultValue()).distinct().sorted().collect(Collectors.joining(";;"));

		mergedItem.setResultIndicator(new MLText(resultIndicators));
		mergedItem.setCitation(new MLText(citation));
		mergedItem.setUsages(new MLText(usages));
		mergedItem.setRestrictionLevels(new MLText(restrictionLevels));
		mergedItem.setPrecautions(new MLText(precautions));
		boolean mlAware = MLPropertyInterceptor.setMLAware(true);
		try {
			MLText comment =	(MLText) nodeService.getProperty(mergedItem.getIng(), PLMModel.PROP_REGULATORY_COMMENT);
			mergedItem.setComment(comment);
		} finally {
			MLPropertyInterceptor.setMLAware(mlAware);
		}

		mergedItem.setSources(extractSources(mergedItem.getIng(), context.getProduct()));

		return mergedItem;
	}

	private List<NodeRef> extractSources(NodeRef ing, ProductData formulatedProduct) {

		Set<NodeRef> sources = new HashSet<>();
		
		List<CompoListDataItem> compoList = formulatedProduct
				.getCompoList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()));

		if (compoList != null) {
			for (CompoListDataItem compoItem : compoList) {

				if ((compoItem.getQtySubFormula() != null) && (compoItem.getQtySubFormula() > 0)) {
					ProductData componentProductData = (ProductData) alfrescoRepository.findOne(compoItem.getProduct());
					if(componentProductData.getIngList()!=null) {
					
						for (IngListDataItem ingListDataItem : componentProductData.getIngList()) {
							if(ingListDataItem.getIng().equals(ing)) {
								sources.add(compoItem.getProduct());
								break;
							}
						}
					}
				}
			}
		}
		return new ArrayList<>(sources);
	}

	private DecernisAnalysisPlugin getAnalysisPlugin() {
		for (DecernisAnalysisPlugin plugin : decernisPlugins) {
			if (plugin.isEnabled()) {
				return plugin;
			}
		}
		throw new IllegalStateException("At least one plugin should be provided");
	}

	private void analyzeRecipe(RegulatoryContext productContext) {
		for (RegulatoryContextItem contextItem : productContext.getContextItems()) {
			if (!contextItem.isEmpty()) {
				getAnalysisPlugin().extractRequirements(productContext, contextItem);
				if (Boolean.TRUE.equals(ingredientAnalysisEnabled())) {
					getAnalysisPlugin().ingredientAnalysis(productContext, contextItem);
				}
			}
		}
	}

	private void checkUsagesID(RegulatoryContext context) {
		for (RegulatoryContextItem contextItem : context.getContextItems()) {
			for (NodeRef usageRef : contextItem.getItem().getRegulatoryUsagesRef()) {
				updateUsageID(usageRef);
			}
		}
	}

	private void updateContextItemsRecipeId(RegulatoryContext context) {
		for (RegulatoryContextItem contextItem : context.getContextItems()) {
			contextItem.getItem().setRegulatoryRecipeId(context.getRegulatoryRecipeId());
		}
	}

	private void createRecipe(RegulatoryContext context) {

		try {
			JSONObject recipePayload = createRecipePayload(context);
			
			String recipeId = null;
			
			if (recipePayload != null) {
				String url = serverUrl() + "/formulas";
				HttpEntity<String> request = createEntity(recipePayload.toString());
				if (logger.isTraceEnabled()) {
					logger.trace("POST url: " + url + " body: " + recipePayload);
				}
				JSONObject jsonObject = new JSONObject(RestTemplateHelper.getRestTemplate().postForObject(url, request, String.class));
				if (jsonObject.has("id")) {
					recipeId = jsonObject.get("id").toString();
				}
			}
			
			logger.debug("Create decernis recipe : "+recipeId);
			
			context.getProduct().setRegulatoryRecipeId(recipeId);
			
			for (RegulatoryContextItem contextItem : context.getContextItems()) {
				contextItem.getItem().setRegulatoryRecipeId(recipeId);
			}
		} catch (HttpStatusCodeException e) {
			logger.error("Error while creating Decernis recipe: " + e.getMessage(), e);
			ReqCtrlListDataItem req = ReqCtrlListDataItem.forbidden()
					.withMessage(MLTextHelper.getI18NMessage("message.decernis.error", "Error while creating Decernis recipe: " + e.getMessage()))
					.ofDataType(RequirementDataType.Specification).withFormulationChainId(DecernisService.DECERNIS_CHAIN_ID);
			context.getRequirements().add(req);
		}
	}

	private JSONObject createRecipePayload(RegulatoryContext context) throws JSONException {

		JSONObject ret = new JSONObject();

		String code = (String) nodeService.getProperty(context.getProduct().getNodeRef(), BeCPGModel.PROP_CODE);
		code += Calendar.getInstance().getTimeInMillis();

		ret.put("spec", code);
		ret.put("name", code + " " + context.getProduct().getName());
		ret.put(PARAM_COMPANY, cleanToken(companyName()));

		JSONArray ingredients = new JSONArray();
		ret.put("ingredients", ingredients);

		for (IngListDataItem ingListDataItem : context.getProduct().getIngList()) {

			if (ingListDataItem.getIng() != null) {

				String legalName = (String) nodeService.getProperty(ingListDataItem.getIng(), BeCPGModel.PROP_LEGAL_NAME);

				String ingName = (legalName != null) && !legalName.isEmpty() ? legalName
						: (String) nodeService.getProperty(ingListDataItem.getIng(), BeCPGModel.PROP_CHARACT_NAME);
				String rid = (String) nodeService.getProperty(ingListDataItem.getIng(), PLMModel.PROP_REGULATORY_CODE);

				Double ingQtyPerc = DecernisHelper.truncateDoubleValue(ingListDataItem.getQtyPerc());

				NodeRef ingType = (NodeRef) nodeService.getProperty(ingListDataItem.getIng(), PLMModel.PROP_ING_TYPE_V2);
				String function = null;
				if (ingType != null) {
					function = (String) nodeService.getProperty(ingType, PLMModel.PROP_REGULATORY_CODE);
				}
				try {
					if ((rid != null) && !rid.isEmpty() && !rid.equals(MISSING_VALUE)
							&& ((ingName != null) && !ingName.isEmpty())) {
						JSONObject ingredient = new JSONObject();
						ingredient.put("name", ingName);
						ingredient.put("percentage", ingQtyPerc == null ? 0d : ingQtyPerc);
						ingredient.put("ingredient_did", rid);
						if (function != null) {
							ingredient.put("function", function);
						}
						ingredient.put("spec_parameters", JSONObject.NULL);
						ingredient.put("upper_limit", JSONObject.NULL);
						ingredients.put(ingredient);
					}

				} catch (HttpStatusCodeException e) {
					logger.warn("Cannot retrieve ingredient " + ingName + " error:" + e.getStatusText());
				} catch (Exception e) {
					logger.error(e, e);
					throw new FormulateException("Unexpected decernis error: " + e.getMessage(), e);
				}
			}
		}

		if (ingredients.length() > 0) {
			return ret;
		}
		return null;
	}

	private void checkIngredients(RegulatoryContext context) {
		for (IngListDataItem ingListDataItem : context.getProduct().getIngList()) {
			if (ingListDataItem.getIng() != null) {
				String rid = (String) nodeService.getProperty(ingListDataItem.getIng(), PLMModel.PROP_REGULATORY_CODE);
				if (rid == null || rid.isEmpty()) {
					rid = fetchIngredientId(ingListDataItem, context.getRequirements());
					nodeService.setProperty(ingListDataItem.getIng(), PLMModel.PROP_REGULATORY_CODE, rid);
				}
			}
		}
	}

	private HttpEntity<String> createEntity(String param) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setBearerAuth(cleanToken(token()));
		headers.setContentType(MediaType.APPLICATION_JSON);

		return new HttpEntity<>(param, headers);
	}

	private String cleanToken(String token) {
		return token != null ? token.replace("Bearer ", "").strip() : "";
	}

	private boolean buildQuery(IngListDataItem ingListDataItem, Map<String, String> params) {
		Iterator<Map.Entry<QName, String>> iterator = ingNumbers.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<QName, String> ingNumber = iterator.next();
			String number = (String) nodeService.getProperty(ingListDataItem.getIng(), ingNumber.getKey());
			if ((number != null) && !number.isEmpty() && !number.equals(MISSING_VALUE) && !number.contains(",")) {
				params.put(PARAM_QUERY, number);
				params.put("type", ingNumber.getValue());
				return true;
			}
		}

		return false;
	}

	private JSONObject getRidByIngName(JSONArray results, String ingName) throws JSONException {
		for (int i = 0; i < results.length(); i++) {
			JSONObject result = results.getJSONObject(i);
			if (result.has("synonyms")) {
				JSONArray synonyms = result.getJSONArray("synonyms");
				int j = 0;
				while (j < synonyms.length()) {

					String[] split = synonyms.getString(j).split(",");

					for (String syn : split) {
						if (syn.toLowerCase().trim().equals(ingName.toLowerCase().replace(",", "").trim())) {
							return result;
						}
					}

					j++;
				}
			}
		}
		return null;
	}

	private String fetchIngredientId(IngListDataItem ingListDataItem, List<ReqCtrlListDataItem> requirements) {

		String legalName = (String) nodeService.getProperty(ingListDataItem.getIng(), BeCPGModel.PROP_LEGAL_NAME);

		String ingName = (legalName != null) && !legalName.isEmpty() ? legalName
				: (String) nodeService.getProperty(ingListDataItem.getIng(), BeCPGModel.PROP_CHARACT_NAME);

		String ingredientId = "";

		String url = serverUrl() + "/ingredients?current_company={company}&q={query}&identifier_type={type}&limit=25";

		Map<String, String> params = new HashMap<>();
		params.put(PARAM_COMPANY, companyName());

		if (!buildQuery(ingListDataItem, params)) {
			params.put(PARAM_QUERY, ingName);
			params.put("type", "Name");
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Look for ingredients in decernis by " + params.get("type") + ": " + params.get(PARAM_QUERY));
		}

		if (logger.isTraceEnabled()) {
			logger.trace("GET url: " + url + " params: " + params);
		}
		ResponseEntity<String> response = RestTemplateHelper.getRestTemplate().exchange(url, HttpMethod.GET, createEntity(null), String.class, params);

		if ((response != null) && HttpStatus.OK.equals(response.getStatusCode()) && (response.getBody() != null)) {

			JSONObject jsonObject = new JSONObject(response.getBody());

			if (jsonObject.has(PARAM_COUNT) && (jsonObject.getInt(PARAM_COUNT) >= 1) && jsonObject.has(PARAM_RESULTS)) {
				JSONArray results = jsonObject.getJSONArray(PARAM_RESULTS);
				JSONObject result = null;
				if (jsonObject.getInt(PARAM_COUNT) == 1) {
					result = results.getJSONObject(0);
				} else {
					result = getRidByIngName(results, ingName);
				}
				if (result == null && results.toList().stream().map(o -> ((Map<?, ?>) o).get("did")).distinct().count() == 1) {
					result = results.getJSONObject(0);
				}
				if (result != null) {
					ingredientId = result.get("did").toString();
					if (logger.isDebugEnabled()) {
						logger.debug("RID of ingredient " + params.get(PARAM_QUERY) + ": " + ingredientId);
					}
					nodeService.setProperty(ingListDataItem.getIng(), PLMModel.PROP_REGULATORY_CODE, ingredientId);
					// Get ingredient numbers (CAS, FEMA, CE)
					if (result.has("libidents")) {
						JSONObject libidents = result.getJSONObject("libidents");
						for (Map.Entry<QName, String> entry : ingNumbers.entrySet()) {
							QName numberPropName = entry.getKey();
							String numberPropValue = entry.getValue();
							String ingNumberToFill = (String) nodeService.getProperty(ingListDataItem.getIng(), numberPropName);

							if ((ingNumberToFill == null || ingNumberToFill.isEmpty()) && libidents.has(numberPropValue)) {
								JSONArray numbers = libidents.getJSONArray(numberPropValue);
								String number = null;
								if (numbers.length() > 0) {
									StringBuilder sb = new StringBuilder();
									for (int i = 0; i < numbers.length(); i++) {
										sb.append(numbers.getString(i)).append(",");
									}
									number = sb.deleteCharAt(sb.length() - 1).toString();
								}
								if ((number != null) && !number.isEmpty()) {
									if (logger.isDebugEnabled()) {
										logger.debug("Set ingredient RID: " + params.get(PARAM_QUERY) + " " + ingListDataItem.getIng() + " "
												+ numberPropName + " " + number);
									}

									nodeService.setProperty(ingListDataItem.getIng(), numberPropName, number);
								}
							}
						}
					}

				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("Several RIDs for ingredient " + params.get(PARAM_QUERY)+ " "+results.toString());
					}
					requirements.add(
							createReqCtrl(ingListDataItem.getIng(), MLTextHelper.getI18NMessage(MESSAGE_SEVERAL_RID_ING), RequirementType.Tolerated));
				}
			} else if (jsonObject.has(PARAM_COUNT) && (jsonObject.getInt(PARAM_COUNT) == 0)) {
				requirements.add(createReqCtrl(ingListDataItem.getIng(), MLTextHelper.getI18NMessage(MESSAGE_NO_RID_ING), RequirementType.Tolerated));
			}
		} else {
			requirements.add(createReqCtrl(ingListDataItem.getIng(), MLTextHelper.getI18NMessage(MESSAGE_NO_RID_ING), RequirementType.Forbidden));
		}
		return ingredientId;
	}

	private ReqCtrlListDataItem createReqCtrl(NodeRef ing, MLText reqCtrlMessage, RequirementType reqType) {
		ReqCtrlListDataItem reqCtrlItem = new ReqCtrlListDataItem();
		reqCtrlItem.setReqType(reqType);
		reqCtrlItem.setCharact(ing);
		reqCtrlItem.addSource(ing);
		reqCtrlItem.setReqDataType(RequirementDataType.Specification);
		reqCtrlItem.setReqMlMessage(reqCtrlMessage);
		reqCtrlItem.setFormulationChainId(DecernisService.DECERNIS_CHAIN_ID);
		return reqCtrlItem;
	}

	private void deleteRecipe(String recipeId) {
		try {
			Map<String, String> params = new HashMap<>();
			params.put(PARAM_COMPANY, companyName());
			params.put(PARAM_FORMULA, recipeId);

			String url = serverUrl() + "/formulas/" + recipeId + "?current_company={company}";
			if (logger.isTraceEnabled()) {
				logger.trace("DELETE url: " + url);
			}
			
			RestTemplateHelper.getRestTemplate().exchange(url, HttpMethod.DELETE, createEntity(null),
					String.class, params);
		} catch (Exception e) {
			logger.error("failed to delete recipe: " + recipeId, e);
		}
	}

	private RegulatoryContext createContext(ProductData product) {
		RegulatoryContext context = new RegulatoryContext();
		context.setProduct(product);

		context.getContextItems().add(createProductContextItem(product, context));

		for (RegulatoryListDataItem item : product.getRegulatoryList()) {
			context.getContextItems().add(createContextItem(item, context));
		}

		return context;
	}
	
	private RegulatoryContextItem createProductContextItem(ProductData product, RegulatoryContext context) {
		if (!product.getRegulatoryCountries().isEmpty() && !product.getRegulatoryUsages().isEmpty()) {
			RegulatoryContextItem contextItem = new RegulatoryContextItem();
			contextItem.setItem(product);
			Map<String, NodeRef> countries = new HashMap<>();
			List<UsageContext> usages = new ArrayList<>();
			for (String country : product.getRegulatoryCountries()) {
				countries.put(country, null);
			}
			for (String usage : product.getRegulatoryUsages()) {
				UsageContext usageContext = new UsageContext();
				usageContext.setModuleId(moduleIdMap.get(FOOD_ADDITIVES));
				usageContext.setName(usage);
				usages.add(usageContext);
			}
			contextItem.setCountries(countries);
			contextItem.setUsages(usages);
			return contextItem;
		}
		return createContextItem(product, context);
	}

	private RegulatoryContextItem createContextItem(RegulatoryEntity item, RegulatoryContext context) {

		RegulatoryContextItem contextItem = new RegulatoryContextItem();
		contextItem.setItem(item);

		Map<String, NodeRef> countries = new HashMap<>();
		List<UsageContext> usages = new ArrayList<>();

		for (NodeRef nodeRef : item.getRegulatoryCountriesRef()) {
			extractCodes(context, countries, nodeRef);
		}
		for (NodeRef nodeRef : item.getRegulatoryUsagesRef()) {
			UsageContext usageContext = createUsage(context, nodeRef);
			if (usageContext != null) {
				usages.add(usageContext);
			}
		}

		contextItem.setCountries(countries);
		contextItem.setUsages(usages);

		return contextItem;
	}

	private UsageContext createUsage(RegulatoryContext context, NodeRef nodeRef) {
		String code = extractCode(nodeRef);
		if (code == null || code.isBlank()) {
			String name = (String) nodeService.getProperty(nodeRef, BeCPGModel.PROP_CHARACT_NAME);
			logger.warn("charact " + name + " has no regulatoryCode");
			context.getRequirements().add(createReqCtrl(null, MLTextHelper.getI18NMessage(MESSAGE_NO_CODE_CHARACT, name), RequirementType.Tolerated));
			return null;
		}
		UsageContext usage = new UsageContext();
		usage.setName(code);
		usage.setNodeRef(nodeRef);
		Integer moduleId = moduleIdMap.get(nodeService.getProperty(nodeRef, PLMModel.PROP_REGULATORY_MODULE));
		usage.setModuleId(moduleId);
		return usage;
	}
	
	private void extractCodes(RegulatoryContext context, Map<String, NodeRef> codes, NodeRef nodeRef) {
		String code = extractCode(nodeRef);
		if (code == null || code.isBlank()) {
			String name = (String) nodeService.getProperty(nodeRef, BeCPGModel.PROP_CHARACT_NAME);
			logger.warn("charact " + name + " has no regulatoryCode");
			context.getRequirements().add(createReqCtrl(null, MLTextHelper.getI18NMessage(MESSAGE_NO_CODE_CHARACT, name), RequirementType.Tolerated));
		} else {
			codes.put(code, nodeRef);
		}
	}

	private String extractCode(NodeRef node) {
		return (String) nodeService.getProperty(node, PLMModel.PROP_REGULATORY_CODE);
	}

	private void updateUsageID(NodeRef usageRef) {
		
		if (nodeService.getProperty(usageRef, PLMModel.PROP_REGULATORY_ID) instanceof String) {
			return;
		}
		
		Integer moduleId = moduleIdMap.get(nodeService.getProperty(usageRef, PLMModel.PROP_REGULATORY_MODULE));
		String usageCode = extractCode(usageRef);
		
		String url = serverUrl() + "/usages/structurized" + "?module_id=" + moduleId + "&phrase=" + usageCode;
		if (logger.isTraceEnabled()) {
			logger.trace("GET url: " + url);
		}
		ResponseEntity<String> response = RestTemplateHelper.getRestTemplate().exchange(url, HttpMethod.GET, createEntity(null),
				String.class, new HashMap<>());

		if (HttpStatus.OK.equals(response.getStatusCode()) && response.getBody() != null) {
			JSONObject responseBody = new JSONObject(response.getBody());
			if (responseBody.has(PARAM_RESULTS)) {
				JSONObject results = responseBody.getJSONObject(PARAM_RESULTS);
				for (String key : results.keySet()) {
					JSONArray resultArray = results.getJSONArray(key);
					for (int i = 0; i < resultArray.length(); i++) {
						JSONObject result = resultArray.getJSONObject(i);

						if (result.has("phrase") && result.getString("phrase").equals(usageCode)) {
							String usageId = Integer.toString(result.getInt("scope_id"));
							nodeService.setProperty(usageRef, PLMModel.PROP_REGULATORY_ID, usageId);
							return;
						}
					}
				}
			}
		}
	}

	@Override
	protected void onShutdown(ApplicationEvent event) {
	//DO Nothing
		
	}



}
