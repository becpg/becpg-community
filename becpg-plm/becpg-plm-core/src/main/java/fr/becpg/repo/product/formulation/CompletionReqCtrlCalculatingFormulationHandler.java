package fr.becpg.repo.product.formulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.SystemState;
import fr.becpg.repo.formulation.EntityCatalogService;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.EntityCatalogHelper;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.AbstractProductDataView;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.CompositionDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.search.BeCPGQueryBuilder;

public class CompletionReqCtrlCalculatingFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(CompletionReqCtrlCalculatingFormulationHandler.class);

	public static final String MESSAGE_NON_VALIDATED_STATE = "message.formulate.nonValidatedState";
	public static final String MESSAGE_MANDATORY_FIELD_MISSING = "message.formulate.mandatory_property";
	public static final String MESSAGE_MANDATORY_FIELD_MISSING_LOCALIZED = "message.formulate.mandatory_property_localized";
	public static final String MESSAGE_OR = "message.formulate.or";
	
	public static final String MESSAGE_NON_UNIQUE_FIELD = "message.formulate.non-unique-field";

	private AlfrescoRepository<ProductData> alfrescoRepository;
	private NodeService nodeService;
	private DictionaryService dictionaryService;
	private NamespaceService namespaceService;
	private AssociationService associationService;
	private NodeService mlNodeService;
	private FormulaService formulaService;
	private EntityCatalogService catalogService;

	public void setCatalogService(EntityCatalogService catalogService) {
		this.catalogService = catalogService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
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

	public void setFormulaService(FormulaService formulaService) {
		this.formulaService = formulaService;
	}



	@Override
	public boolean process(ProductData product) throws FormulateException {
		logger.debug("===== Calculating score of " + product.getName() + " (" + product.getState() + ") =====");
		if (logger.isDebugEnabled()) {
			logger.debug("===== Calculating score of product " + product.getName() + " =====");
		}

		JSONObject scores = new JSONObject();

		try {

			// checks if mandatory fields are present
			JSONArray mandatoryFields = calculateMandatoryFieldsScore(product);

			ReqCtrlListDataItem rclDataItem = new ReqCtrlListDataItem(null, RequirementType.Tolerated,
					I18NUtil.getMessage(MESSAGE_NON_VALIDATED_STATE), null, new ArrayList<NodeRef>(), RequirementDataType.Validation);

			boolean shouldAdd = false;

			// visits all refs and adds rclDataItem to them if required
			for (AbstractProductDataView view : product.getViews()) {
				if (view.getMainDataList() != null) {
					for (CompositionDataItem dataItem : view.getMainDataList()) {
						if (dataItem.getComponent() != null) {
							if (!checkProductValidity(dataItem.getComponent())) {
								rclDataItem.getSources().add(dataItem.getComponent());
								shouldAdd = true;
							}
						}
					}
				}
			}

			if (shouldAdd) {
				product.getReqCtrlList().add(rclDataItem);
			}

			scores.put(EntityCatalogHelper.PROP_CATALOGS, mandatoryFields);

		} catch (JSONException e) {
			logger.error("Cannot create Json Score", e);
		}

		product.setEntityScore(scores.toString());
		return true;
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

	private JSONObject createMissingFields(ProductData productData, String catalogName, List<String> fields) throws JSONException {

		JSONObject field = new JSONObject();

		String id = "";
		String displayName = "";
		String lang = null;

		for (int i = 0; i < fields.size(); ++i) {
			String currentField = fields.get(i);
			ClassAttributeDefinition classDef = formatQnameString(currentField);

			if (currentField.contains("_")) {
				lang = currentField.split("_")[1];
			} else {
				lang = null;
			}

			if (classDef == null) {
				logger.debug("classDef for field " + currentField + " returned null");
				break;
			}

			id += classDef.getName().toPrefixString(namespaceService) + (i == (fields.size() - 1) ? "" : "|");
			displayName += classDef.getTitle(dictionaryService) + (i == (fields.size() - 1) ? "" : " " + I18NUtil.getMessage(MESSAGE_OR) + " ");
		}

		final String message = (lang != null
				? I18NUtil.getMessage(MESSAGE_MANDATORY_FIELD_MISSING_LOCALIZED, displayName, catalogName, "(" + lang + ")")
				: I18NUtil.getMessage(MESSAGE_MANDATORY_FIELD_MISSING, displayName, catalogName));

		if (lang != null) {
			field.put(EntityCatalogHelper.PROP_LOCALE, lang);
		}

		field.put(EntityCatalogHelper.PROP_ID, id);
		field.put(EntityCatalogHelper.PROP_DISPLAY_NAME, displayName);

		ReqCtrlListDataItem rclDataItem = new ReqCtrlListDataItem(null, RequirementType.Forbidden, message, null, new ArrayList<NodeRef>(),
				RequirementDataType.Completion);
		rclDataItem.getSources().add(productData.getNodeRef());

		productData.getReqCtrlList().add(rclDataItem);

		return field;

	}

	private ClassAttributeDefinition formatQnameString(String qNameString) {
		ClassAttributeDefinition res = null;

		qNameString = qNameString.trim();
		PropertyDefinition propDef = dictionaryService.getProperty(QName.createQName(qNameString.split("_")[0], namespaceService));

		if (propDef != null) {
			res = propDef;
		} else {
			AssociationDefinition assocDef = dictionaryService.getAssociation(QName.createQName(qNameString, namespaceService));
			res = assocDef;
		}

		return res;
	}

	public JSONArray calculateMandatoryFieldsScore(ProductData productData) throws JSONException {
		JSONArray ret = new JSONArray();

		List<JSONArray> catalogs = catalogService.getCatalogsDef();

		if ((!catalogs.isEmpty()) && (productData.getNodeRef() != null) && nodeService.exists(productData.getNodeRef())) {
			// Break rules !!!!
			Map<QName, Serializable> properties = nodeService.getProperties(productData.getNodeRef());
			String defaultLocale = Locale.getDefault().getLanguage();
			QName productType = nodeService.getType(productData.getNodeRef());

			for (JSONArray catalogDef : catalogs) {
				for (int i = 0; i < catalogDef.length(); i++) {
					JSONObject catalog = catalogDef.getJSONObject(i);

					// if( apply(catalog, productData)){

					// if( apply(catalog, productData)){

					// check if product matches various criteria, such as
					// family,
					// subfamily, etc.
					boolean productPassesFilter = productMatches(catalog, productData, productType);

					if (logger.isDebugEnabled()) {
						logger.debug("\n\t\t== Catalog \"" + catalog.getString(EntityCatalogHelper.PROP_LABEL) + "\" ==");
						logger.debug("Type of product: " + productType);
						logger.debug("Catalog json: " + catalog);
						logger.debug("ProductPassesFilter: " + productPassesFilter);
					}

					// if this catalog applies to this type, or this catalog has
					// no
					// type defined (it applies to every entity type)
					if (productPassesFilter) {
						if (logger.isDebugEnabled()) {
							logger.debug("Formulating for catalog \"" + catalog.getString(EntityCatalogHelper.PROP_LABEL) + "\"");
						}
						List<String> langs = new LinkedList<>(EntityCatalogHelper.getLocales(productData.getReportLocales(), catalog));

						langs.sort((o1, o2) -> {
							if (o1.equals(defaultLocale)) {
								return -1;
							}
							if (o2.equals(defaultLocale)) {
								return 1;
							}
							return 0;
						});

						String color = getCatalogColor(catalog, i);

						JSONArray reqFields = catalog.has(EntityCatalogHelper.PROP_FIELDS) ? catalog.getJSONArray(EntityCatalogHelper.PROP_FIELDS)
								: new JSONArray();
						JSONArray uniqueFields = catalog.has(EntityCatalogHelper.PROP_UNIQUE_FIELDS)
								? catalog.getJSONArray(EntityCatalogHelper.PROP_UNIQUE_FIELDS)
								: new JSONArray();

						JSONArray nonUniqueFields = extractNonUniqueFields(productData, catalog.getString(EntityCatalogHelper.PROP_LABEL), properties,
								uniqueFields);

						for (String lang : langs) {

							JSONArray missingFields = extractMissingFields(productData, catalog.getString(EntityCatalogHelper.PROP_LABEL), properties,
									reqFields, defaultLocale.equals(lang) ? null : lang);
							if ((missingFields.length() > 0) || (nonUniqueFields.length() > 0)) {
								JSONObject catalogDesc = new JSONObject();
								catalogDesc.put(EntityCatalogHelper.PROP_MISSING_FIELDS, missingFields.length() > 0 ? missingFields : null);
								catalogDesc.put(EntityCatalogHelper.PROP_NON_UNIQUE_FIELDS, nonUniqueFields.length() > 0 ? nonUniqueFields : null);
								catalogDesc.put(EntityCatalogHelper.PROP_LOCALE, defaultLocale.equals(lang) ? null : lang);
								catalogDesc.put(EntityCatalogHelper.PROP_SCORE,
										((reqFields.length() - missingFields.length()) * 100d) / (reqFields.length() > 0 ? reqFields.length() : 1d));
								catalogDesc.put(EntityCatalogHelper.PROP_LABEL, catalog.getString(EntityCatalogHelper.PROP_LABEL));
								catalogDesc.put(EntityCatalogHelper.PROP_ID, catalog.getString(EntityCatalogHelper.PROP_ID));
								catalogDesc.put(EntityCatalogHelper.PROP_COLOR, color);
								ret.put(catalogDesc);
							}

						}
					}
				}
			}
		}

		return ret;
	}


	private boolean productMatches(JSONObject catalog, ProductData product, QName productType) throws JSONException {

		boolean matchesOnType = EntityCatalogHelper.isMatcheEntityType(catalog, productType, namespaceService);
		if (catalog.has(EntityCatalogHelper.PROP_ENTITY_FILTER)) {
			String filterFormula = catalog.getString(EntityCatalogHelper.PROP_ENTITY_FILTER);
			return matchesOnType && productMatchesOnFormula(filterFormula, product);
		} else {
			// no entity filter in catalog
			return matchesOnType;
		}
	}

	private boolean productMatchesOnFormula(String formula, ProductData product) throws JSONException {

		boolean res = true;

		StandardEvaluationContext context = formulaService.createEvaluationContext(product);

		if (context != null) {
			ExpressionParser parser = new SpelExpressionParser();
			Expression expression = parser.parseExpression(formula);

			try {
				Boolean result = (Boolean) (expression.getValue(context));
				logger.debug("Expression " + expression + " returned " + result);
				res = result.booleanValue();
			} catch (Exception e) {
				logger.error("Unable to parse expression " + expression, e);
				logger.debug("Creating new CtrlListDataItem (method productMatchesOnFormula...)");
				ReqCtrlListDataItem rclDataItem = new ReqCtrlListDataItem(null, RequirementType.Tolerated, "Unable to parse formula " + formula, null,
						new ArrayList<NodeRef>(), RequirementDataType.Completion);
				product.getReqCtrlList().add(rclDataItem);
				res = false;
			}
		}

		return res;
	}

	

	private String getCatalogColor(JSONObject catalog, int i) throws JSONException {
		String color = catalog.has(EntityCatalogHelper.PROP_COLOR) ? catalog.getString(EntityCatalogHelper.PROP_COLOR)
				: "hsl(" + (i * (360 / 7)) + ", 60%, 50%)";
		if (logger.isDebugEnabled()) {
			logger.debug("Color of catalog is: " + color + (catalog.has(EntityCatalogHelper.PROP_COLOR) ? " (fetched from catalog)" : " (generated)"));
		}

		return color;
	}

	private JSONArray extractNonUniqueFields(ProductData productData, String catalogName, Map<QName, Serializable> properties, JSONArray uniqueFields)
			throws JSONException {
		JSONArray res = new JSONArray();

		if (productData.getNodeRef() != null) {
			for (int i = 0; i < uniqueFields.length(); i++) {

				String field = uniqueFields.getString(i);

				QName propQName = QName.createQName(field, namespaceService);
				Serializable propValue = nodeService.getProperty(productData.getNodeRef(), propQName);

				if (propValue != null) {
					List<NodeRef> propDuplicates = getPropertyDuplicates(productData.getNodeRef(), propQName, propValue.toString());

					if (!(propDuplicates.isEmpty())) {

						ClassAttributeDefinition classDef = formatQnameString(field);
						String propTitle = classDef.getTitle(dictionaryService);
						res.put(propTitle);

						String message = I18NUtil.getMessage(MESSAGE_NON_UNIQUE_FIELD, propTitle, propValue);

						ReqCtrlListDataItem rclDataItem = new ReqCtrlListDataItem(null, RequirementType.Forbidden, message, null, propDuplicates,
								RequirementDataType.Completion);
						productData.getReqCtrlList().add(rclDataItem);

					}
				}
			}
		}

		return res;
	}

	private List<NodeRef> getPropertyDuplicates(NodeRef productNodeRef, QName propQName, String value) {

		List<NodeRef> queryResults = new ArrayList<>();
		if ((value != null) && !value.isEmpty()) {

			queryResults = BeCPGQueryBuilder.createQuery().ofType(nodeService.getType(productNodeRef)).andNotID(productNodeRef).excludeDefaults()
					.andPropEquals(propQName, value).inDBIfPossible().list();

			List<NodeRef> falsePositives = new ArrayList<>();

			// Lucene equals is actually contains, remove results that contain
			// but do not equal value
			for (NodeRef result : queryResults) {
				Serializable resultProp = nodeService.getProperty(result, propQName);

				if ((resultProp != null) && !resultProp.equals(value)) {

					falsePositives.add(result);
				}
			}

			for (NodeRef falsePositive : falsePositives) {
				queryResults.remove(falsePositive);
			}
		}

		return queryResults;
	}

	

	private JSONArray extractMissingFields(ProductData productData, String catalogName, Map<QName, Serializable> properties, JSONArray reqFields,
			String lang) throws JSONException {
		JSONArray ret = new JSONArray();

		logger.debug("=== Catalog name: " + catalogName + ", lang: " + lang);
		for (int i = 0; i < reqFields.length(); i++) {
			String field = reqFields.getString(i);

			List<String> splitFields = Arrays.asList(field.split(Pattern.quote("|")));
			boolean present = false;

			// if this field can be ignored (do not raise ctrl if absent)
			boolean ignore = false;

			for (String currentField : splitFields) {
				QName fieldQname = null;

				try {
					fieldQname = QName.createQName(currentField.split("_")[0], namespaceService);
				} catch (NamespaceException e) {
					// happens if namespace does not exist
					ignore = true;
					break;
				}

				if (logger.isDebugEnabled()) {
					logger.debug("Test missing field qname: " + fieldQname + ", lang: " + lang);
				}

				PropertyDefinition propDef = dictionaryService.getProperty(fieldQname);

				if ((propDef != null) && (DataTypeDefinition.MLTEXT.equals(propDef.getDataType().getName()))) {
					// prop is present
					if (mlTextIsPresent(currentField, productData, lang, properties)) {
						logger.debug("mlProp is present");
						present = true;
						break;
					}

				} else if ((propDef != null) && (lang == null)) {
					// non ML field case
					if ((properties.get(fieldQname) != null) && !properties.get(fieldQname).toString().isEmpty()) {
						logger.debug("regular prop is present");
						present = true;
						break;
					}
				} else if ((propDef != null) && !DataTypeDefinition.MLTEXT.equals(propDef.getDataType().getName()) && (lang != null)) {
					logger.debug("Non ml prop with non null lang, skipping");
					// case non ml prop with not null lang, we don't care
					ignore = true;
					break;

				} else if ((propDef == null) && (lang == null)) {
					// only check assoc when lang is null
					logger.debug("Checking if assoc is found");
					if (associationService.getTargetAssoc(productData.getNodeRef(), fieldQname) != null) {
						present = true;
						break;
					}

				} else {
					// lang is not null and it's not a prop
					logger.debug("Skipping associations on localized catalogs");
					ignore = true;
					break;
				}
			}

			if (!present && !ignore) {
				logger.debug("\tfield " + field + " is absent...");
				ret.put(createMissingFields(productData, catalogName, splitFields));
			}

		}

		return ret;
	}

	

	private boolean mlTextIsPresent(String field, ProductData productData, String lang, Map<QName, Serializable> properties) {
		boolean res = true;
		QName fieldQname = QName.createQName(field.split("_")[0], namespaceService);
		MLText mlText = (MLText) mlNodeService.getProperty(productData.getNodeRef(), fieldQname);
		Locale loc = null;
		if (lang != null) {
			loc = MLTextHelper.parseLocale(lang);
		}
		if (field.contains("_")) {
			String fieldSpecificLang = field.split("_")[1];
			if ((mlText == null) || ((loc != null) || (mlText.getValue(loc) == null)) || mlText.getValue(new Locale(fieldSpecificLang)).isEmpty()) {
				res = false;
			}
		} else if ((loc != null) && ((mlText == null) || (mlText.getValue(loc) == null) || mlText.getValue(loc).isEmpty())) {
			res = false;
		} else {

			res = ((properties.get(fieldQname) != null) && !properties.get(fieldQname).toString().isEmpty())
					|| ((mlText != null) && (mlText.get(loc) != null) && !mlText.get(loc).isEmpty());

		}

		return res;
	}

}
