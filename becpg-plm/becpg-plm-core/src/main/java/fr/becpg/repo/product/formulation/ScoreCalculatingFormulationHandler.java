package fr.becpg.repo.product.formulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.cache.BeCPGCacheDataProviderCallBack;
import fr.becpg.repo.cache.BeCPGCacheService;
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
import fr.becpg.repo.search.BeCPGQueryBuilder;

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
	public static final String MESSAGE_OR = "message.formulate.or";
	public static final String MESSAGE_NON_UNIQUE_FIELD = "message.formulate.non-unique-field";
	
	public static final String CATALOGS_PATH = "/app:company_home/cm:System/cm:PropertyCatalogs";
	public static final String CATALOG_DEFS = "CATALOG_DEFS";

	private AlfrescoRepository<ProductData> alfrescoRepository;

	private NodeService nodeService;

	private DictionaryService dictionaryService;

	private NamespaceService namespaceService;

	@Autowired
	private BeCPGCacheService beCPGCacheService;
	
	private AssociationService associationService;

	private NodeService mlNodeService;
	
	private FormulaService formulaService;
	
	private Repository repository;
	
	private FileFolderService fileFolderService;

	
	@Autowired
	private ContentService contentService;

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

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
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

			cleanOldRclSources(product);

			// visits all refs and adds rclDataItem to them if required
			for (AbstractProductDataView view : product.getViews()) {
				if (view.getMainDataList() != null) {
					for (CompositionDataItem dataItem : view.getMainDataList()) {
						if (dataItem.getComponent() != null) {
							if (!checkProductValidity(dataItem.getComponent())) {

								final ReqCtrlListDataItem nonValidatedRclDataItem = createValidationRclDataItem(dataItem.getComponent());

								List<ReqCtrlListDataItem> productNonValidatedRcl = product
										.getReqCtrlList()
										.stream()
										.filter(rcl -> nonValidatedRclDataItem.getReqMessage().equals(rcl.getReqMessage())
												&& nonValidatedRclDataItem.getReqDataType().equals(rcl.getReqDataType())
												&& nonValidatedRclDataItem.getReqType().equals(rcl.getReqType())).collect(Collectors.toList());

								// add new rcl or add sources to existing
								// validation rcl
								if (productNonValidatedRcl.size() > 0) {

									for (NodeRef tmpNodeRef : nonValidatedRclDataItem.getSources()) {
										if (!productNonValidatedRcl.get(0).getSources().contains(tmpNodeRef)) {
											productNonValidatedRcl.get(0).getSources().add(tmpNodeRef);
										
										}
									}
								} else {
									product.getReqCtrlList().add(nonValidatedRclDataItem);
								}
							} else {
								childScore += 1;
							}
							childrenSize += 1;
						}
					}
				}
			}

			// pre merge, ctrlDataItems might be duplicated, visit them only
			// once
			List<String> visitedCtrlDataItems = new ArrayList<>();
			if (product.getReqCtrlList() != null) {
				for (ReqCtrlListDataItem ctrl : product.getReqCtrlList()) {

					if (specificationScore > 10) {
						if ((ctrl.getReqDataType() == RequirementDataType.Specification) && (ctrl.getReqType() == RequirementType.Forbidden)
								&& !visitedCtrlDataItems.contains(ctrl.getKey())) {
							if (logger.isDebugEnabled()) {
								logger.debug("Visiting specification rclDataItem: " + ctrl.getReqMessage() + ", s=" + ctrl.getSources());
							}
							specificationScore -= 10;
							visitedCtrlDataItems.add(ctrl.getKey());
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
			Map<String, Map<String, Integer>> ctrlCount = getCtrlCount(product);

			List<JSONObject> ctrlArray = new ArrayList<>();

			for (String ctrlKey : ctrlCount.keySet()) {
				JSONObject currentJSO = new JSONObject();
				currentJSO.put(ctrlKey, ctrlCount.get(ctrlKey));

				ctrlArray.add(currentJSO);
			}

			ctrlArray.sort((o1, o2) -> {
				// sort on keys (fbd > all)

					try {
						JSONObject o1Values = o1.getJSONObject((String) o1.keys().next());
						JSONObject o2Values = o2.getJSONObject((String) o2.keys().next());

						if ((o1Values.has("Forbidden") && !(o2Values.has("Forbidden")))
								|| (o1Values.has("Forbidden") && o2Values.has("Forbidden") && (o1Values.getInt("Forbidden") >= o2Values
										.getInt("Forbidden")))) {
							return -1;
						} else {
							return 1;
						}
					} catch (JSONException e) {
						if (logger.isDebugEnabled()) {
							logger.debug("JSONException, returning equals");
						}
						return 0;
					}
				});

			logger.debug("Ctrl sorted array: " + ctrlArray);

			scores.put("ctrlCount", ctrlArray);
			scores.put("totalForbidden", getForbiddenCtrlAmount(ctrlCount));

			scores.put(JsonScoreHelper.PROP_CATALOGS, mandatoryFields);
			logger.debug("Done calculating score of product " + product.getName() + ", children: " + componentsValidationScore + "%, mandatory: "
					+ mandatoryFieldsScore + "% , specifications: " + specificationScore + "%, global=" + completionPercent + "%");

			logger.debug("Score json: " + scores);

		} catch (JSONException e) {
			logger.error("Cannot create Json Score", e);
		}

		product.setEntityScore(scores.toString());

		return true;
	}

	private void cleanOldRclSources(ProductData product) {
		List<ReqCtrlListDataItem> matchingRcl = product.getReqCtrlList().stream()
				.filter(rcl -> RequirementDataType.Completion.equals(rcl.getReqDataType()) || RequirementDataType.Validation.equals(rcl.getReqDataType()))
				.collect(Collectors.toList());
		
		for(ReqCtrlListDataItem tmp : matchingRcl){
			tmp.getSources().clear();
		}
	}

	/**
	 * Creates a new ReqCtrlListDataItem for node for validation issues
	 *
	 * @param node
	 * @return
	 */
	public ReqCtrlListDataItem createValidationRclDataItem(NodeRef node) {
		logger.debug("Creating new non validated rclDataItem, src=" + node);
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
		
		JSONArray catalogs = getCatalogDef();
		
		if ((catalogs != null) && (productData.getNodeRef() != null) && nodeService.exists(productData.getNodeRef())) {
			// Break rules !!!!
			Map<QName, Serializable> properties = nodeService.getProperties(productData.getNodeRef());
			String defaultLocale = Locale.getDefault().getLanguage();
			QName productType = nodeService.getType(productData.getNodeRef());

			for (int i = 0; i < catalogs.length(); i++) {
				JSONObject catalog = catalogs.getJSONObject(i);
				
				//if( apply(catalog, productData)){

				// if( apply(catalog, productData)){

				// check if product matches various criteria, such as family,
				// subfamily, etc.
				boolean productPassesFilter = productMatches(catalog, productData, productType);

				if (logger.isDebugEnabled()) {
					logger.debug("\n\t\t== Catalog \"" + catalog.getString(JsonScoreHelper.PROP_LABEL) + "\" ==");
					logger.debug("Type of product: " + productType);
					logger.debug("Catalog json: " + catalog);
					logger.debug("ProductPassesFilter: " + productPassesFilter);
				}

				// if this catalog applies to this type, or this catalog has no
				// type defined (it applies to every entity type)
				if (productPassesFilter) {
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

					String color = getCatalogColor(catalog, i);

					JSONArray reqFields = catalog.getJSONArray(JsonScoreHelper.PROP_FIELDS);
					JSONArray uniqueFields = catalog.getJSONArray(JsonScoreHelper.PROP_UNIQUE_FIELDS);

					JSONArray nonUniqueFields = extractNonUniqueFields(productData, catalog.getString(JsonScoreHelper.PROP_LABEL), properties,
							uniqueFields);

					for (String lang : langs) {

						JSONArray missingFields = extractMissingFields(productData, catalog.getString(JsonScoreHelper.PROP_LABEL), properties,
								reqFields, defaultLocale.equals(lang) ? null : lang);
						if (missingFields.length() > 0 || nonUniqueFields.length() > 0) {
							JSONObject catalogDesc = new JSONObject();
							catalogDesc.put(JsonScoreHelper.PROP_MISSING_FIELDS, missingFields.length() > 0 ? missingFields : null);
							catalogDesc.put(JsonScoreHelper.PROP_NON_UNIQUE_FIELDS, nonUniqueFields.length() > 0 ? nonUniqueFields : null);
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

	private JSONArray getCatalogDef() {

		logger.debug("getCatalogDef, cacheService: " + beCPGCacheService);

		return beCPGCacheService.getFromCache(ScoreCalculatingFormulationHandler.class.getName(), CATALOG_DEFS,
				new BeCPGCacheDataProviderCallBack<JSONArray>() {

					@Override
					public JSONArray getData() {

						// get JSON from file in system
						NodeRef folder = getCatalogFolderNodeRef();
						logger.debug("Catalogs folder: " + folder);

						List<FileInfo> files = fileFolderService.list(folder);
						logger.debug("Number of catalogs: " + files.size());

						if (!files.isEmpty()) {
							FileInfo file = files.get(0);

							logger.debug("File in catalog folder nr: " + file.getNodeRef());
							ContentReader reader = contentService.getReader(file.getNodeRef(), ContentModel.PROP_CONTENT);
							String content = reader.getContentString();
							logger.debug("Content: " + content);
							// Serializable content =
							// nodeService.getProperty(file.getNodeRef(),
							// ContentModel.PROP_CONTENT);
							JSONArray res = new JSONArray();

							try {
								res = new JSONArray(content);
							} catch (JSONException e) {
								logger.error("Unable to parse content to catalog, content: " + content, e);
							}
							
							return res;
						} else {
							// no file in catalog folder
							return new JSONArray();
						}
					}
				});
	}

	private String getCatalogColor(JSONObject catalog, int i) throws JSONException {
		String color = catalog.has(JsonScoreHelper.PROP_COLOR) ? catalog.getString(JsonScoreHelper.PROP_COLOR) : "hsl(" + (i * (360 / 7))
				+ ", 60%, 50%)";
		if (logger.isDebugEnabled()) {
			logger.debug("Color of catalog is: " + color + (catalog.has(JsonScoreHelper.PROP_COLOR) ? " (fetched from catalog)" : " (generated)"));
		}
		
		return color;
	}

	private JSONArray extractNonUniqueFields(ProductData productData, String catalogName, Map<QName, Serializable> properties, JSONArray uniqueFields)
			throws JSONException {
		JSONArray res = new JSONArray();

		for (int i = 0; i < uniqueFields.length(); i++) {
			
			String field = uniqueFields.getString(i);

			QName propQName = QName.createQName(field, namespaceService);
			Serializable propValue = nodeService.getProperty(productData.getNodeRef(), propQName);
			List<NodeRef> propDuplicates = getPropertyDuplicates(propQName, (String) propValue);

			if (!(propDuplicates.size() <= 1)) {

				ClassAttributeDefinition classDef = formatQnameString(field);
				String propTitle = classDef.getTitle(dictionaryService);

				res.put(propTitle);

				String message = I18NUtil.getMessage(MESSAGE_NON_UNIQUE_FIELD, propTitle) + " (" + propValue + ")";
				ReqCtrlListDataItem rclDataItem = new ReqCtrlListDataItem(null, RequirementType.Forbidden, message, null, new ArrayList<NodeRef>(),
						RequirementDataType.Completion);
				rclDataItem.getSources().add(productData.getNodeRef());
				productData.getReqCtrlList().add(rclDataItem);
			}
		}

		return res;
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

		if (field.contains("_")) {
			String fieldSpecificLang = field.split("_")[1];
			if ((mlText == null) || (mlText.getValue(new Locale(fieldSpecificLang)) == null)
					|| mlText.getValue(new Locale(fieldSpecificLang)).isEmpty()) {
				res = false;
			}
		} else if ((lang != null) && ((mlText == null) || (mlText.getValue(new Locale(lang)) == null) || mlText.getValue(new Locale(lang)).isEmpty())) {
			res = false;
		} else {
			res = (properties.get(fieldQname) != null) && !properties.get(fieldQname).toString().isEmpty();
		}

		return res;
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

		final String message = (lang != null ? I18NUtil.getMessage(MESSAGE_MANDATORY_FIELD_MISSING_LOCALIZED, displayName, catalogName, "(" + lang
				+ ")") : I18NUtil.getMessage(MESSAGE_MANDATORY_FIELD_MISSING, displayName, catalogName));

		if (lang != null) {
			field.put(JsonScoreHelper.PROP_LOCALE, lang);
		}

		field.put(JsonScoreHelper.PROP_ID, id);
		field.put(JsonScoreHelper.PROP_DISPLAY_NAME, displayName);

		List<ReqCtrlListDataItem> matchingRclDataItems = productData
				.getReqCtrlList()
				.stream()
				.filter(rcl -> rcl.getReqMessage().equals(message) && RequirementType.Forbidden.equals(rcl.getReqType())
						&& RequirementDataType.Completion.equals(rcl.getReqDataType())).collect(Collectors.toList());

		if (matchingRclDataItems.size() > 0) {
			// add sources
			if (!matchingRclDataItems.get(0).getSources().contains(productData.getNodeRef())) {
				matchingRclDataItems.get(0).getSources().add(productData.getNodeRef());
			}
		} else {
			// rcl not found in visited list, add it to the product
			ReqCtrlListDataItem rclDataItem = new ReqCtrlListDataItem(null, RequirementType.Forbidden, message, null, new ArrayList<NodeRef>(),
					RequirementDataType.Completion);
			rclDataItem.getSources().add(productData.getNodeRef());

			productData.getReqCtrlList().add(rclDataItem);
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

	private Map<String, Map<String, Integer>> getCtrlCount(ProductData product) {

		Map<String, Map<String, Integer>> counts = new HashMap<>();
		List<ReqCtrlListDataItem> visitedRclItems = new ArrayList<>();

		for (ReqCtrlListDataItem rclDataItem : product.getReqCtrlList()) {
			RequirementDataType key = rclDataItem.getReqDataType();

			// make sure we don't put duplicates rclDataItems
			if (counts.containsKey(key.toString()) && !visitedRclItems.contains(rclDataItem)) {
				Map<String, Integer> currentCount = counts.get(key.toString());
				if (currentCount == null) {
					currentCount = new HashMap<String, Integer>();
				}

				if (currentCount.containsKey(rclDataItem.getReqType().toString())) {
					currentCount.put(rclDataItem.getReqType().toString(), currentCount.get(rclDataItem.getReqType().toString()) + 1);
				} else {
					currentCount.put(rclDataItem.getReqType().toString(), 1);
				}
				visitedRclItems.add(rclDataItem);

			} else if (!counts.containsKey(key.toString())) {
				// this dataType was not found before, adding it
				Map<String, Integer> newMap = new HashMap<String, Integer>();
				newMap.put(rclDataItem.getReqType().toString(), 1);
				counts.put(key.toString(), newMap);

				visitedRclItems.add(rclDataItem);
			}
		}

		return counts;
	}

	private int getForbiddenCtrlAmount(Map<String, Map<String, Integer>> reqCtrlList) {
		int res = 0;

		for (Map<String, Integer> map : reqCtrlList.values()) {
			if (map.containsKey("Forbidden")) {
				res += map.get("Forbidden");
			}
		}

		return res;
	}

	private List<NodeRef> getPropertyDuplicates(QName propQName, String value) {

		List<NodeRef> queryResults = new ArrayList<>();
		if (value != null && !value.isEmpty()) {

			BeCPGQueryBuilder query = BeCPGQueryBuilder.createQuery().andPropEquals(propQName, value);
			query = query.excludeAspect(BeCPGModel.ASPECT_COMPOSITE_VERSION);
			// query = query.excludeAspect(BeCPGModel.ASPECT_ENTITY_BRANCH);
			query = query.excludeAspect(ContentModel.ASPECT_WORKING_COPY);
			logger.debug("Query: " + query.toString());
			queryResults = query.list();
			List<NodeRef> falsePositives = new ArrayList<>();

			// Lucene equals is actually contains, remove results that contain
			// but do not equal value
			for (NodeRef result : queryResults) {
				Serializable resultProp = nodeService.getProperty(result, propQName);
				logger.debug("result: " + result + " prop value: " + resultProp);

				if (resultProp != null && !resultProp.equals(value)) {
					logger.debug("Result " + result + " does not match value " + value + " (its value: " + resultProp + "), removing from res");
					falsePositives.add(result);
				}
			}

			for (NodeRef falsePositive : falsePositives) {
				queryResults.remove(falsePositive);
			}
		}

		logger.debug("Number of properties of name: " + propQName + " and value: " + value + " = " + queryResults.size() + ", res=" + queryResults
				+ ", is unique ? " + (queryResults.size() <= 1));

		return queryResults;
	}

	private boolean productMatches(JSONObject catalog, ProductData product, QName productType) throws JSONException {

		boolean matchesOnType = productMatchesOnType(product, catalog, productType);
		if (catalog.has(JsonScoreHelper.PROP_ENTITY_FILTER)) {
			String filterFormula = catalog.getString(JsonScoreHelper.PROP_ENTITY_FILTER);
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
				ReqCtrlListDataItem rclDataItem = new ReqCtrlListDataItem(null, RequirementType.Tolerated, "Unable to parse formula " + formula,
						null, new ArrayList<NodeRef>(), RequirementDataType.Completion);
				product.getReqCtrlList().add(rclDataItem);
				res = false;
			}
		}

		return res;
	}
	
	private boolean productMatchesOnType(ProductData product, JSONObject catalog, QName productType) throws JSONException {
		JSONArray catalogEntityTypes = (catalog.has(JsonScoreHelper.PROP_ENTITY_TYPE)) ? catalog.getJSONArray(JsonScoreHelper.PROP_ENTITY_TYPE)
				: new JSONArray();
		List<QName> qnameCatalogEntityTypeList = new ArrayList<QName>();

		logger.debug("catalog has " + catalogEntityTypes.length() + " entities: " + catalogEntityTypes);

		for (int catalogEntityTypeIndex = 0; catalogEntityTypeIndex < catalogEntityTypes.length(); ++catalogEntityTypeIndex) {
			QName qname = QName.createQName(catalogEntityTypes.getString(catalogEntityTypeIndex), namespaceService);

			qnameCatalogEntityTypeList.add(qname);
		}
		logger.debug("CatalogEntityTypeList: " + qnameCatalogEntityTypeList + ", productType: " + productType);

		return qnameCatalogEntityTypeList.contains(productType);
	}

	private NodeRef getCatalogFolderNodeRef() {
		logger.debug("CatalogFileNR: " + BeCPGQueryBuilder.createQuery().selectNodeByPath(repository.getCompanyHome(), CATALOGS_PATH));

		return BeCPGQueryBuilder.createQuery().selectNodeByPath(repository.getCompanyHome(), CATALOGS_PATH);
	}

}