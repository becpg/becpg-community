package fr.becpg.repo.regulatory.plugins;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.entity.remote.RemoteParams;
import fr.becpg.repo.entity.remote.RemoteServiceRegisty;
import fr.becpg.repo.entity.remote.extractor.JsonEntityVisitor;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.RestTemplateHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ing.IngItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.IngRegulatoryListDataItem;
import fr.becpg.repo.regulatory.CountryBatch;
import fr.becpg.repo.regulatory.RegulatoryBatch;
import fr.becpg.repo.regulatory.RegulatoryContext;
import fr.becpg.repo.regulatory.RegulatoryPlugin;
import fr.becpg.repo.regulatory.RegulatoryService;
import fr.becpg.repo.regulatory.RequirementDataType;
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.repo.regulatory.RequirementType;
import fr.becpg.repo.regulatory.UsageBatch;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * BeCPG Regulatory Plugin that uses the BeCPG Regulatory API instead of Decernis.
 *
 * @author beCPG
 */
@Service
public class BeCPGRegulatoryPlugin implements RegulatoryPlugin {
	public static final String BECPG_CHAIN_ID = "becpg-regulatory";

	private static final String MESSAGE_BECPG_ERROR = "message.becpg.regulatory.error";
	private static final String MESSAGE_PROHIBITED_ING = "message.becpg.regulatory.ingredient.prohibited";
	private static final String MESSAGE_TOLERATED_ING = "message.becpg.regulatory.ingredient.tolerated";

	private static final Log logger = LogFactory.getLog(BeCPGRegulatoryPlugin.class);

	private SystemConfigurationService systemConfigurationService;
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	private RemoteServiceRegisty remoteServiceRegisty;

	/**
	 * Constructor for BeCPGRegulatoryPlugin.
	 * 
	 * @param systemConfigurationService the system configuration service
	 * @param nodeService the node service
	 * @param alfrescoRepository the alfresco repository
	 * @param remoteServiceRegisty the remote service registry
	 */
	public BeCPGRegulatoryPlugin(SystemConfigurationService systemConfigurationService, @Qualifier("nodeService") NodeService nodeService,
			AlfrescoRepository<RepositoryEntity> alfrescoRepository, RemoteServiceRegisty remoteServiceRegisty) {
		this.systemConfigurationService = systemConfigurationService;
		this.alfrescoRepository = alfrescoRepository;
		this.remoteServiceRegisty = remoteServiceRegisty;
	}

	private String regulatoryApiUrl() {
		return systemConfigurationService.confValue("beCPG.regulatory.apiUrl");
	}

	@Override
	public void checkRecipe(RegulatoryContext context, RegulatoryBatch regulatoryBatch) {
		if (logger.isDebugEnabled()) {
			logger.debug("Launch BeCPG regulatory check for recipe");
		}

		try {
			JSONObject requestPayload = createComplianceCheckPayload(context, regulatoryBatch);
			if (requestPayload != null) {
				JSONObject response = postComplianceCheck(requestPayload);
				if (response != null) {
					List<RequirementListDataItem> requirements = parseComplianceResponse(context, regulatoryBatch, response);
					context.getRequirements().addAll(requirements);
				}
			}
		} catch (HttpStatusCodeException e) {
			logger.error("Error during BeCPG regulatory check: " + e.getMessage(), e);
			for (String country : regulatoryBatch.countryBatches().countries()) {
				for (String usage : regulatoryBatch.usageBatches().usages()) {
					RequirementListDataItem req = RequirementListDataItem.forbidden()
							.withMessage(MLTextHelper.getI18NMessage(MESSAGE_BECPG_ERROR, "Error during BeCPG regulatory check: " + e.getMessage()))
							.ofDataType(RequirementDataType.Formulation).withFormulationChainId(RegulatoryService.REGULATORY_KEY)
							.withRegulatoryCode(country + (!usage.isEmpty() ? " - " + usage : ""));
					context.getRequirements().add(req);
				}
			}
		}
	}

	@Override
	public void checkIngredients(RegulatoryContext context, RegulatoryBatch checkContext) {
		if (logger.isDebugEnabled()) {
			logger.debug("Launch BeCPG regulatory check for ingredients");
		}

		try {
			JSONObject requestPayload = createComplianceCheckPayload(context, checkContext);
			if (requestPayload != null) {
				JSONObject response = postComplianceCheck(requestPayload);
				if (response != null) {
					List<IngRegulatoryListDataItem> ingRegulatoryItems = parseIngRegulatoryResponse(context, response);
					context.getIngRegulatoryListDataItems().addAll(ingRegulatoryItems);
				}
			}
		} catch (HttpStatusCodeException e) {
			logger.error("Error during BeCPG ingredients regulatory check: " + e.getMessage(), e);
			RequirementListDataItem req = RequirementListDataItem.forbidden()
					.withMessage(
							MLTextHelper.getI18NMessage(MESSAGE_BECPG_ERROR, "Error during BeCPG ingredients regulatory check: " + e.getMessage()))
					.ofDataType(RequirementDataType.Formulation).withFormulationChainId(RegulatoryService.REGULATORY_KEY);
			context.getRequirements().add(req);
		}
	}

	@Override
	public String fetchIngredientId(IngListDataItem ingListDataItem) {
		if (logger.isDebugEnabled()) {
			logger.debug("Fetching ingredient ID from BeCPG regulatory API");
		}

		IngItem ingItem = (IngItem) alfrescoRepository.findOne(ingListDataItem.getIng());
		if (ingItem == null) {
			return null;
		}

		String regulatoryCode = ingItem.getRegulatoryCode();
		if ((regulatoryCode != null) && !regulatoryCode.isEmpty()) {
			return regulatoryCode;
		}

		return null;
	}

	@Override
	public List<CountryBatch> splitCountries(RegulatoryContext context, List<String> countries) {
		List<CountryBatch> countryBatches = new ArrayList<>();
		countryBatches.add(new CountryBatch(countries));
		return countryBatches;
	}

	@Override
	public List<UsageBatch> splitUsages(RegulatoryContext context, List<String> usages) {
		List<UsageBatch> usageBatches = new ArrayList<>();
		usageBatches.add(new UsageBatch("", usages));
		return usageBatches;
	}

	private JSONObject createComplianceCheckPayload(RegulatoryContext context, RegulatoryBatch regulatoryBatch) {
		try {
			ProductData product = context.getProduct();
			NodeRef productNodeRef = product.getNodeRef();

			RemoteParams params = new RemoteParams(RemoteEntityFormat.json);

			String fieldsParam = """
					bcpg:ingListIng|bcpg:ingTypeV2,bcpg:ingListQtyPerc,bcpg:ingListIsProcessingAid,bcpg:ingListIsGMO,\
					bcpg:ingListIng,bcpg:ingListIng|bcpg:regulatoryCode,bcpg:regulatoryCode,bcpg:regulatoryUsageRef,bcpg:regulatoryCountries,\
					bcpg:ingListIng|bcpg:casNumber,bcpg:ingListIng|bcpg:ceNumber,bcpg:regulatoryState,bcpg:regulatoryMode,\
					bcpg:pclValue,bcpg:pclPhysicoChem,bcpg:pclPhysicoChem|bcpg:regulatoryCode,bcpg:regulatoryFormulatedDate,\
					bcpg:legalName,bcpg:regulatoryCountries|bcpg:geoOriginISOCode,bcpg:regulatoryCountries|bcpg:regulatoryCode,\
					bcpg:regulatoryCountries|bcpg:linkedSearchAssociation,bcpg:regulatoryUsageRef|bcpg:regulatoryCode,\
					bcpg:pclPhysicoChem|bcpg:legalName,bcpg:ingListIng|bcpg:legalName,bcpg:ingTypeItem|bcpg:regulatoryCode,\
					bcpg:ingTypeItem|bcpg:legalName""";

			Set<String> fields = new HashSet<>(Arrays.asList(fieldsParam.split(",")));
			params.setFilteredFields(fields, remoteServiceRegisty.namespaceService());

			Set<String> lists = new HashSet<>(Arrays.asList("ingList", "physicoChemList", "regulatoryList"));
			params.setFilteredLists(lists);

			JSONObject paramsJson = new JSONObject();
			paramsJson.put("appendMlText", false);
			params.setJsonParams(paramsJson);

			JsonEntityVisitor visitor = new JsonEntityVisitor(remoteServiceRegisty);
			visitor.setParams(params);

			java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
			visitor.visit(productNodeRef, outputStream);

			String jsonString = outputStream.toString(StandardCharsets.UTF_8.name());
			return new JSONObject(jsonString);
		} catch (Exception e) {
			logger.error("Error creating compliance check payload", e);
			return null;
		}
	}

	private JSONObject postComplianceCheck(JSONObject payload) {
		String url = regulatoryApiUrl() + "/api/v1/regulatory/check";

		HttpEntity<String> entity = createEntity(payload.toString());

		if (logger.isTraceEnabled()) {
			logger.trace("POST url: " + url + " body: " + payload);
		}

		ResponseEntity<String> response = RestTemplateHelper.getRestTemplateLongTimeout().exchange(url, HttpMethod.POST, entity, String.class,
				new HashMap<>());

		if (HttpStatus.OK.equals(response.getStatusCode()) && (response.getBody() != null)) {
			return new JSONObject(response.getBody());
		}

		return null;
	}

	private HttpEntity<String> createEntity(String body) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		return new HttpEntity<>(body, headers);
	}

	private List<RequirementListDataItem> parseComplianceResponse(RegulatoryContext context, RegulatoryBatch regulatoryBatch, JSONObject response) {
		List<RequirementListDataItem> requirements = new ArrayList<>();

		if (!response.has(RemoteEntityService.ELEM_DATALISTS)) {
			return requirements;
		}

		JSONObject datalists = response.getJSONObject(RemoteEntityService.ELEM_DATALISTS);
		String forbiddenIngListType = PLMModel.TYPE_FORBIDDENINGLIST.toPrefixString(remoteServiceRegisty.namespaceService());
		if (!datalists.has(forbiddenIngListType)) {
			return requirements;
		}

		JSONArray forbiddenIngList = datalists.getJSONArray(forbiddenIngListType);

		for (int i = 0; i < forbiddenIngList.length(); i++) {
			JSONObject forbiddenItem = forbiddenIngList.getJSONObject(i);
			RequirementListDataItem requirement = parseForbiddenIngItem(context, forbiddenItem);
			if (requirement != null) {
				requirements.add(requirement);
			}
		}

		return requirements;
	}

	private RequirementListDataItem parseForbiddenIngItem(RegulatoryContext context, JSONObject forbiddenItem) {
		if (!forbiddenItem.has(RemoteEntityService.ELEM_ATTRIBUTES)) {
			return null;
		}

		JSONObject attributes = forbiddenItem.getJSONObject(RemoteEntityService.ELEM_ATTRIBUTES);

		String reqType = attributes.optString(PLMModel.PROP_FIL_REQ_TYPE.toPrefixString(remoteServiceRegisty.namespaceService()), "Forbidden");
		Double maxQty = attributes.optDouble(PLMModel.PROP_FIL_QTY_PERC_MAXI.toPrefixString(remoteServiceRegisty.namespaceService()), 0.0);
		String regulatoryCode = attributes.optString(PLMModel.PROP_REGULATORY_CODE.toPrefixString(remoteServiceRegisty.namespaceService()), "");
		String message = attributes.optString(PLMModel.PROP_FIL_REQ_MESSAGE.toPrefixString(remoteServiceRegisty.namespaceService()), "");

		RequirementType requirementType = "Tolerated".equals(reqType) ? RequirementType.Tolerated : RequirementType.Forbidden;

		MLText reqMessage = message.isEmpty()
				? MLTextHelper.getI18NMessage(requirementType == RequirementType.Tolerated ? MESSAGE_TOLERATED_ING : MESSAGE_PROHIBITED_ING)
				: new MLText(message);

		RequirementListDataItem requirement = RequirementListDataItem.forbidden().withMessage(reqMessage)
				.ofDataType(RequirementDataType.Specification).withFormulationChainId(RegulatoryService.REGULATORY_KEY)
				.withRegulatoryCode(regulatoryCode);

		requirement.setReqType(requirementType);
		requirement.setReqMaxQty(maxQty);

		if (forbiddenItem.has(RemoteEntityService.ELEM_ASSOCIATIONS)) {
			JSONObject associations = forbiddenItem.getJSONObject(RemoteEntityService.ELEM_ASSOCIATIONS);
			String filIngsAssoc = PLMModel.ASSOC_FIL_INGS.toPrefixString(remoteServiceRegisty.namespaceService());
			if (associations.has(filIngsAssoc)) {
				Object ingsObj = associations.get(filIngsAssoc);
				if (ingsObj instanceof JSONArray ings) {
					if (ings.length() > 0) {
						JSONObject ingRef = ings.getJSONObject(0);
						NodeRef ingNodeRef = findIngredientNodeRef(context, ingRef);
						if (ingNodeRef != null) {
							requirement.setCharact(ingNodeRef);
							requirement.addSource(ingNodeRef);
						}
					}
				}
			}
		}

		return requirement;
	}

	private List<IngRegulatoryListDataItem> parseIngRegulatoryResponse(RegulatoryContext context, JSONObject response) {
		List<IngRegulatoryListDataItem> ingRegulatoryItems = new ArrayList<>();

		if (!response.has(RemoteEntityService.ELEM_DATALISTS)) {
			return ingRegulatoryItems;
		}

		JSONObject datalists = response.getJSONObject(RemoteEntityService.ELEM_DATALISTS);
		String ingRegulatoryListType = PLMModel.TYPE_ING_REGULATORY_LIST.toPrefixString(remoteServiceRegisty.namespaceService());
		if (!datalists.has(ingRegulatoryListType)) {
			return ingRegulatoryItems;
		}

		JSONArray ingRegulatoryList = datalists.getJSONArray(ingRegulatoryListType);

		for (int i = 0; i < ingRegulatoryList.length(); i++) {
			JSONObject ingRegItem = ingRegulatoryList.getJSONObject(i);
			IngRegulatoryListDataItem item = parseIngRegulatoryItem(context, ingRegItem);
			if (item != null) {
				ingRegulatoryItems.add(item);
			}
		}

		return ingRegulatoryItems;
	}

	private IngRegulatoryListDataItem parseIngRegulatoryItem(RegulatoryContext context, JSONObject ingRegItem) {
		if (!ingRegItem.has(RemoteEntityService.ELEM_ASSOCIATIONS)) {
			return null;
		}

		JSONObject associations = ingRegItem.getJSONObject(RemoteEntityService.ELEM_ASSOCIATIONS);

		NodeRef ingNodeRef = null;
		String irlIngAssoc = PLMModel.ASSOC_IRL_ING.toPrefixString(remoteServiceRegisty.namespaceService());
		if (associations.has(irlIngAssoc)) {
			Object ingObj = associations.get(irlIngAssoc);
			if ((ingObj instanceof JSONArray ings) && (ings.length() > 0)) {
				JSONObject ingRef = ings.getJSONObject(0);
				ingNodeRef = findIngredientNodeRef(context, ingRef);
			}
		}

		if (ingNodeRef == null) {
			return null;
		}

		IngRegulatoryListDataItem item = new IngRegulatoryListDataItem();
		item.setIng(ingNodeRef);

		String regulatoryCountriesAssoc = PLMModel.ASSOC_REGULATORY_COUNTRIES.toPrefixString(remoteServiceRegisty.namespaceService());
		if (associations.has(regulatoryCountriesAssoc)) {
			Object countriesObj = associations.get(regulatoryCountriesAssoc);
			if (countriesObj instanceof JSONArray countries) {
				List<NodeRef> countryRefs = new ArrayList<>();
				for (int i = 0; i < countries.length(); i++) {
					JSONObject countryRef = countries.getJSONObject(i);
					if (countryRef.has(RemoteEntityService.ELEM_ATTRIBUTES)) {
						JSONObject countryAttributes = countryRef.getJSONObject(RemoteEntityService.ELEM_ATTRIBUTES);
						String countryCode = countryAttributes
								.optString(PLMModel.PROP_REGULATORY_CODE.toPrefixString(remoteServiceRegisty.namespaceService()), "");
						NodeRef countryNodeRef = context.getCountryNodeRef(countryCode);
						if (countryNodeRef != null) {
							countryRefs.add(countryNodeRef);
						}
					}
				}
				item.setRegulatoryCountries(countryRefs);
			}
		}

		if (ingRegItem.has(RemoteEntityService.ELEM_ATTRIBUTES)) {
			JSONObject attributes = ingRegItem.getJSONObject(RemoteEntityService.ELEM_ATTRIBUTES);
			String irlUsagesProp = PLMModel.PROP_IRL_USAGES.toPrefixString(remoteServiceRegisty.namespaceService());
			if (attributes.has(irlUsagesProp)) {
				item.setUsages(new MLText(attributes.getString(irlUsagesProp)));
			}
			String irlCitationProp = PLMModel.PROP_IRL_CITATION.toPrefixString(remoteServiceRegisty.namespaceService());
			if (attributes.has(irlCitationProp)) {
				item.setCitation(new MLText(attributes.getString(irlCitationProp)));
			}
			String irlRestrictionLevelsProp = PLMModel.PROP_IRL_RESTRICTION_LEVELS.toPrefixString(remoteServiceRegisty.namespaceService());
			if (attributes.has(irlRestrictionLevelsProp)) {
				item.setRestrictionLevels(new MLText(attributes.getString(irlRestrictionLevelsProp)));
			}
			String irlPrecautionsProp = PLMModel.PROP_IRL_PRECAUTIONS.toPrefixString(remoteServiceRegisty.namespaceService());
			if (attributes.has(irlPrecautionsProp)) {
				item.setPrecautions(new MLText(attributes.getString(irlPrecautionsProp)));
			}
			String irlResultIndicatorProp = PLMModel.PROP_IRL_RESULT_INDICATOR.toPrefixString(remoteServiceRegisty.namespaceService());
			if (attributes.has(irlResultIndicatorProp)) {
				item.setResultIndicator(new MLText(attributes.getString(irlResultIndicatorProp)));
			}
		}

		return item;
	}

	private NodeRef findIngredientNodeRef(RegulatoryContext context, JSONObject ingRef) {
		if (!ingRef.has(RemoteEntityService.ELEM_ATTRIBUTES)) {
			return null;
		}

		JSONObject attributes = ingRef.getJSONObject(RemoteEntityService.ELEM_ATTRIBUTES);
		String regulatoryCode = attributes.optString(PLMModel.PROP_REGULATORY_CODE.toPrefixString(remoteServiceRegisty.namespaceService()), "");
		String casNumber = attributes.optString(PLMModel.PROP_CAS_NUMBER.toPrefixString(remoteServiceRegisty.namespaceService()), "");

		for (IngListDataItem ingListItem : context.getProduct().getIngList()) {
			if (ingListItem.getIng() != null) {
				IngItem ingItem = (IngItem) alfrescoRepository.findOne(ingListItem.getIng());
				if (ingItem != null) {
					if ((!regulatoryCode.isEmpty() && regulatoryCode.equals(ingItem.getRegulatoryCode())) || (!casNumber.isEmpty() && casNumber.equals(ingItem.getIngCASCode()))) {
						return ingListItem.getIng();
					}
				}
			}
		}

		return null;
	}
}
