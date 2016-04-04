package fr.becpg.repo.product.formulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.SystemState;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.JsonScoreHelper;
import fr.becpg.repo.product.data.AbstractProductDataView;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.CompositionDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;

/**
 * Computes product completion score
 *
 * @author steven
 *
 */
public class ScoreCalculatingFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(ScoreCalculatingFormulationHandler.class);

	public static final String MESSAGE_MANDATORY_FIELD_MISSING = "message.formulate.mandatory_property";
	public static final String MESSAGE_MANDATORY_FIELD_MISSING_LOCALIZED = "message.formulate.mandatory_property_localized";
	public static final String MESSAGE_NON_VALIDATED_STATE = "message.formulate.nonValidatedState";

	private AlfrescoRepository<ProductData> alfrescoRepository;

	private NodeService nodeService;

	private DictionaryService dictionaryService;

	private NamespaceService namespaceService;

	private AssociationService associationService;

	private NodeService mlNodeService;

	/**
	 * Product can't be completed unless these fields are present
	 */
	private String mandatoryFields;

	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setMandatoryFields(String mandatoryFields) {
		this.mandatoryFields = mandatoryFields;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public void setMlNodeService(NodeService mlNodeService) {
		this.mlNodeService = mlNodeService;
	}

	@Override
	public boolean process(ProductData product) {

		if (logger.isDebugEnabled()) {
			logger.debug("===== Calculating score of product " + product.getName() + " =====");
		}

		JSONObject scores = new JSONObject();

		try {

			Integer childScore = 0;
			Integer childrenSize = 0;
			Double specificationScore = 100d;
			Double mandatoryFieldsScore = 100d;

			// visits all refs and adds rclDataItem to them if required
			for (AbstractProductDataView view : product.getViews()) {
				for (CompositionDataItem dataItem : view.getMainDataList()) {
					if (dataItem.getComponent() != null) {
						if (!checkProductValidity(dataItem.getComponent())) {
							view.getReqCtrlList().add(createValidationRclDataItem(dataItem.getComponent()));
						} else {
							childScore += 1;
						}
						childrenSize += 1;
					}
				}

				// pre merge, ctrlDataItems might be duplicated, visit them only
				// once
				List<String> visitedCtrlDataItems = new ArrayList<>();
				for (ReqCtrlListDataItem ctrl : view.getReqCtrlList()) {

					if (specificationScore > 10) {
						if ((ctrl.getReqDataType() == RequirementDataType.Specification) && (ctrl.getReqType() == RequirementType.Forbidden)
								&& !visitedCtrlDataItems.contains(ctrl.getReqMessage())) {
							if (logger.isDebugEnabled()) {
								logger.debug("Visiting specification rclDataItem: " + ctrl.getReqMessage() + ", s=" + ctrl.getSources());
							}
							specificationScore -= 10;
							visitedCtrlDataItems.add(ctrl.getReqMessage());
						}
					} else {
						break;
					}

				}
			}

			// checks if mandatory fields are present
			JSONArray mandatoryFields = calculateMandatoryFieldsScore(product);

			if (mandatoryFields.length() > 0) {

				mandatoryFieldsScore = 0d;

				for (int j = 0; j < mandatoryFields.length(); j++) {
					JSONObject catalog = (JSONObject) mandatoryFields.get(j);
					mandatoryFieldsScore += catalog.getDouble(JsonScoreHelper.PROP_SCORE);
				}

				mandatoryFieldsScore /= mandatoryFields.length();
			}

			Double componentsValidationScore = (childrenSize > 0 ? (childScore * 100d) / childrenSize : 100d);
			Double completionPercent = (componentsValidationScore + mandatoryFieldsScore + specificationScore) / 3d;

			JSONObject details = new JSONObject();

			details.put("mandatoryFields", mandatoryFieldsScore);
			details.put("specifications", specificationScore);
			details.put("componentsValidation", componentsValidationScore);

			scores.put("global", completionPercent);
			scores.put("details", details);
			scores.put(JsonScoreHelper.PROP_CATALOGS, mandatoryFields);
			logger.debug("Done calculating score of product " + product.getName() + ", children: " + componentsValidationScore + "%, mandatory: "
					+ mandatoryFieldsScore + "% , specifications: " + specificationScore + "%, global=" + completionPercent + "%");

		} catch (JSONException e) {
			logger.error("Cannot create Json Score", e);
		}

		product.setEntityScore(scores.toString());

		return true;
	}

	/**
	 * Creates a new ReqCtrlListDataItem for node for validation issues
	 *
	 * @param node
	 * @return
	 */
	public ReqCtrlListDataItem createValidationRclDataItem(NodeRef node) {
		String message = I18NUtil.getMessage(MESSAGE_NON_VALIDATED_STATE);

		ReqCtrlListDataItem rclDataItem = new ReqCtrlListDataItem(null, RequirementType.Tolerated, message, null, new ArrayList<NodeRef>(),
				RequirementDataType.Validation);

		rclDataItem.getSources().add(node);
		return rclDataItem;
	}

	/**
	 * Returns if node exists and is in valid state
	 *
	 * @param node
	 * @return
	 */
	public boolean checkProductValidity(NodeRef node) {
		ProductData found = alfrescoRepository.findOne(node);
		if (found != null) {
			return SystemState.Valid.equals(found.getState());
		}
		return false;
	}

	public JSONArray calculateMandatoryFieldsScore(ProductData productData) throws JSONException {
		JSONArray ret = new JSONArray();
		if ((mandatoryFields != null) && nodeService.exists(productData.getNodeRef())) {
			// Break rules !!!!
			Map<QName, Serializable> properties = nodeService.getProperties(productData.getNodeRef());
			String defaultLocale = Locale.getDefault().getLanguage();
			JSONArray catalogs = new JSONArray(mandatoryFields);
			QName productType = nodeService.getType(productData.getNodeRef());

			for (int i = 0; i < catalogs.length(); i++) {
				JSONObject catalog = catalogs.getJSONObject(i);
				JSONArray catalogEntityTypes = (catalog.has(JsonScoreHelper.PROP_ENTITY_TYPE))
						? catalog.getJSONArray(JsonScoreHelper.PROP_ENTITY_TYPE) : new JSONArray();
				List<QName> qnameCatalogEntityTypeList = new ArrayList<QName>();

				for (int catalogEntityTypeIndex = 0; catalogEntityTypeIndex < catalogEntityTypes.length(); catalogEntityTypeIndex++) {
					qnameCatalogEntityTypeList.add(QName.createQName(catalogEntityTypes.getString(catalogEntityTypeIndex), namespaceService));
				}

				if (logger.isDebugEnabled()) {
					logger.debug("== Catalog \"" + catalog.getString(JsonScoreHelper.PROP_LABEL) + "\" ==");
					logger.debug("Types of catalog: " + qnameCatalogEntityTypeList);
					logger.debug("Type of product: " + productType);
				}

				// if this catalog applies to this type, or this catalog has no
				// type defined (it applies to every entity type)
				if (qnameCatalogEntityTypeList.isEmpty() || qnameCatalogEntityTypeList.contains(productType)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Formulating for catalog \"" + catalog.getString(JsonScoreHelper.PROP_LABEL) + "\"");
					}
					List<String> langs = new LinkedList<>(getLocales(productData.getReportLocales(), catalog));

					langs.sort((o1, o2) -> {
						if (o1.equals(defaultLocale)) {
							return -1;
						}
						if (o2.equals(defaultLocale)) {
							return 1;
						}
						return 0;
					});

					String color = catalog.has(JsonScoreHelper.PROP_COLOR) ? catalog.getString(JsonScoreHelper.PROP_COLOR)
							: "hsl(" + (i * (360 / 7)) + ", 60%, 50%)";
					if (logger.isDebugEnabled()) {
						logger.debug("Color of catalog is: " + color
								+ (catalog.has(JsonScoreHelper.PROP_COLOR) ? " (fetched from catalog)" : " (generated)"));
					}

					JSONArray reqFields = catalog.getJSONArray(JsonScoreHelper.PROP_FIELDS);
					for (String lang : langs) {

						JSONArray missingFields = extractMissingFields(productData, catalog.getString(JsonScoreHelper.PROP_LABEL), properties,
								reqFields, defaultLocale.equals(lang) ? null : lang);
						if (missingFields.length() > 0) {
							JSONObject catalogDesc = new JSONObject();
							catalogDesc.put(JsonScoreHelper.PROP_MISSING_FIELDS, missingFields);
							catalogDesc.put(JsonScoreHelper.PROP_LOCALE, defaultLocale.equals(lang) ? null : lang);
							catalogDesc.put(JsonScoreHelper.PROP_SCORE, ((reqFields.length() - missingFields.length()) * 100d) / reqFields.length());
							catalogDesc.put(JsonScoreHelper.PROP_LABEL, catalog.getString(JsonScoreHelper.PROP_LABEL));
							catalogDesc.put(JsonScoreHelper.PROP_ID, catalog.getString(JsonScoreHelper.PROP_ID));
							catalogDesc.put(JsonScoreHelper.PROP_COLOR, color);
							ret.put(catalogDesc);
						}

					}
				}
			}
		}

		return ret;
	}

	private JSONArray extractMissingFields(ProductData productData, String catalogName, Map<QName, Serializable> properties, JSONArray reqFields,
			String lang) throws JSONException {
		JSONArray ret = new JSONArray();

		for (int i = 0; i < reqFields.length(); i++) {
			String field = reqFields.getString(i);
			QName fieldQname = QName.createQName(field.split("_")[0], namespaceService);

			if (logger.isDebugEnabled()) {
				logger.debug("Test missing field: " + fieldQname);
			}

			PropertyDefinition propDef = dictionaryService.getProperty(fieldQname);
			if (propDef != null) {

				if (DataTypeDefinition.MLTEXT.equals(propDef.getDataType().getName()) && (lang != null)) {
					MLText mlText = (MLText) mlNodeService.getProperty(productData.getNodeRef(), fieldQname);

					if (field.contains("_")) {
						String fieldSpecificLang = field.split("_")[1];

						if ((mlText == null) || (mlText.getValue(new Locale(fieldSpecificLang)) == null)
								|| mlText.getValue(new Locale(fieldSpecificLang)).isEmpty()) {
							ret.put(createMissingFields(productData, catalogName, propDef, fieldSpecificLang));
						}
					} else {
						if ((mlText == null) || (mlText.getValue(new Locale(lang)) == null) || mlText.getValue(new Locale(lang)).isEmpty()) {
							ret.put(createMissingFields(productData, catalogName, propDef, lang));
						}
					}

				} else if (lang == null) {
					if ((properties.get(fieldQname) == null) || properties.get(fieldQname).toString().isEmpty()) {
						ret.put(createMissingFields(productData, catalogName, propDef, null));
					}
				}
			} else if (lang == null) {

				// Break rules !!!!
				if (associationService.getTargetAssoc(productData.getNodeRef(), fieldQname) == null) {
					ret.put(createMissingFields(productData, catalogName, dictionaryService.getAssociation(fieldQname), null));
				}

			}

		}

		return ret;
	}

	private JSONObject createMissingFields(ProductData productData, String catalogName, ClassAttributeDefinition classDef, String lang)
			throws JSONException {

		JSONObject field = new JSONObject();

		field.put(JsonScoreHelper.PROP_ID, classDef.getName().toPrefixString(namespaceService));
		field.put(JsonScoreHelper.PROP_DISPLAY_NAME, classDef.getTitle(dictionaryService));

		String message = null;

		if (lang != null) {
			message = I18NUtil.getMessage(MESSAGE_MANDATORY_FIELD_MISSING_LOCALIZED, field.getString(JsonScoreHelper.PROP_DISPLAY_NAME), catalogName,
					"(" + lang + ")");

			field.put(JsonScoreHelper.PROP_LOCALE, lang);
		} else {
			message = I18NUtil.getMessage(MESSAGE_MANDATORY_FIELD_MISSING, field.getString(JsonScoreHelper.PROP_DISPLAY_NAME), catalogName);
		}

		logger.debug("Creating new rclDataItem: " + message);
		ReqCtrlListDataItem rclDataItem = new ReqCtrlListDataItem(null, RequirementType.Forbidden, message, null, new ArrayList<NodeRef>(),
				RequirementDataType.Completion);
		rclDataItem.getSources().add(productData.getNodeRef());

		// put rclDataItem in proper view

		if (!productData.getCompoListView().getCompoList().isEmpty()) {
			productData.getCompoListView().getReqCtrlList().add(rclDataItem);
		} else if (!productData.getProcessListView().getProcessList().isEmpty()) {
			productData.getProcessListView().getReqCtrlList().add(rclDataItem);
		} else if (!productData.getPackagingListView().getPackagingList().isEmpty()) {
			productData.getPackagingListView().getReqCtrlList().add(rclDataItem);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Create missing fields: " + field.toString());
		}

		return field;

	}

	private Set<String> getLocales(List<String> reportLocales, JSONObject catalog) throws JSONException {
		Set<String> langs = new HashSet<>();

		if (catalog.has(JsonScoreHelper.PROP_LOCALES)) {
			JSONArray catalogLocales = catalog.getJSONArray(JsonScoreHelper.PROP_LOCALES);
			for (int j = 0; j < catalogLocales.length(); j++) {
				langs.add(catalogLocales.getString(j));
			}
		}

		if (reportLocales != null) {
			langs.addAll(reportLocales);
		}

		if (langs.isEmpty()) {
			// put system locale
			langs.add(Locale.getDefault().getLanguage());
		}

		return langs;
	}

}