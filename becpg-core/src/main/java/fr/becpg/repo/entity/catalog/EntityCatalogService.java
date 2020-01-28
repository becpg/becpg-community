package fr.becpg.repo.entity.catalog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

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
import org.alfresco.service.cmr.repository.ContentService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.surf.util.ISO8601DateFormat;
import org.springframework.stereotype.Service;

import fr.becpg.model.DataListModel;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.search.BeCPGQueryBuilder;

@Service("entityCatalogService")
public class EntityCatalogService {

	public static final Log logger = LogFactory.getLog(EntityCatalogService.class);

	public static final String PROP_CATALOGS = "catalogs";
	public static final String PROP_MISSING_FIELDS = "missingFields";
	public static final String PROP_UNIQUE_FIELDS = "uniqueFields";
	public static final String PROP_NON_UNIQUE_FIELDS = "nonUniqueFields";
	public static final String PROP_I18N_MESSAGES = "i18nMessages";
	public static final String PROP_DISPLAY_NAME = "displayName";
	public static final String PROP_FIELDS = "fields";
	public static final String PROP_LABEL = "label";
	public static final String PROP_ID = "id";
	public static final String PROP_LOCALES = "locales";
	public static final String PROP_SCORE = "score";
	public static final String PROP_LOCALE = "locale";
	public static final String PROP_ENTITY_TYPE = "entityType";
	public static final String PROP_COLOR = "color";
	public static final String PROP_ENTITY_FILTER = "entityFilter";
	public static final String PROP_OPERATOR = "operator";
	public static final String PROP_CATALOG_MODIFIED_DATE = "modifiedDate";
	public static final String PROP_FORMULA = "formula";
	public static final String PROP_CATALOG_MODIFIED_FIELD = "modifiedField";
	public static final String PROP_AUDITED_FIELDS = "auditedFields";
	public static final String PROP_VALUE = "value";
	public static final String PROP_ENTITIES = "entities";

	public static final String CATALOGS_PATH = "/app:company_home/cm:System/cm:PropertyCatalogs";
	public static final String CATALOG_DEFS = "CATALOG_DEFS";

	public static final String MESSAGE_OR = "message.formulate.or";

	@Autowired
	private NamespaceService namespaceService;
	@Autowired
	private NodeService nodeService;
	@Autowired
	private BeCPGCacheService beCPGCacheService;
	@Autowired
	private FileFolderService fileFolderService;
	@Autowired
	private ContentService contentService;
	@Autowired
	private Repository repository;
	@Autowired
	private DictionaryService dictionaryService;

	@Autowired
	@Qualifier("mlAwareNodeService")
	private NodeService mlNodeService;

	@Autowired
	private AssociationService associationService;

	public List<JSONArray> getCatalogsDef() {

		return beCPGCacheService.getFromCache(EntityCatalogService.class.getName(), CATALOG_DEFS, () -> {

			List<JSONArray> res = new ArrayList<>();

			// get JSON from file in system
			NodeRef folder = getCatalogFolderNodeRef();
			logger.debug("Catalogs folder: " + folder);

			List<FileInfo> files = null;
			if (folder != null) {
				files = fileFolderService.list(folder);
			}

			if ((files != null) && !files.isEmpty()) {

				for (FileInfo file : files) {
					logger.debug("File in catalog folder nr: " + file.getNodeRef());
					ContentReader reader = contentService.getReader(file.getNodeRef(), ContentModel.PROP_CONTENT);
					String content = reader.getContentString();
					logger.debug("Content: " + content);
					try {
						res.add(new JSONArray(content));
					} catch (JSONException e) {
						logger.error("Unable to parse content to catalog, content: " + content, e);
					}
				}

				return res;
			} else {
				// no file in catalog folder
				return new ArrayList<>();
			}
		});
	}

	private NodeRef getCatalogFolderNodeRef() {
		return BeCPGQueryBuilder.createQuery().selectNodeByPath(repository.getCompanyHome(), CATALOGS_PATH);
	}

	public void updateAuditedField(NodeRef entityNodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after,
			Set<NodeRef> listNodeRefs) {
		try {
			if (((before != null) && (after != null)) || (listNodeRefs != null)) {

				for (JSONArray catalogDef : getCatalogsDef()) {

					for (int i = 0; i < catalogDef.length(); i++) {
						JSONObject catalog = catalogDef.getJSONObject(i);
						if (catalog.has(PROP_CATALOG_MODIFIED_FIELD)) {
							Set<QName> auditedFields = getAuditedFields(catalog, namespaceService);
							if ((auditedFields != null) && !auditedFields.isEmpty()) {
								QName catalogModifiedDate = QName.createQName(catalog.getString(PROP_CATALOG_MODIFIED_FIELD), namespaceService);

								if (isMatchEntityType(catalog, nodeService.getType(entityNodeRef), namespaceService)) {

									if (listNodeRefs != null) {
										for (NodeRef listNodeRef : listNodeRefs) {
											QName listType = QName.createQName(
													(String) nodeService.getProperty(listNodeRef, DataListModel.PROP_DATALISTITEMTYPE),
													namespaceService);
											if (auditedFields.contains(listType)) {
												if (logger.isDebugEnabled()) {
													logger.debug("Catalog list changed update date: " + catalogModifiedDate);
												}
												nodeService.setProperty(entityNodeRef, catalogModifiedDate, new Date());
												break;
											}
										}

									} else {
										for (QName beforeType : before.keySet()) {
											Serializable beforeValue = before.get(beforeType);
											if (auditedFields.contains(beforeType) && (((beforeValue == null) && (after.get(beforeType) != null))
													|| ((beforeValue != null) && !beforeValue.equals(after.get(beforeType))))) {
												if (logger.isDebugEnabled()) {
													logger.debug("Catalog properties changed update date: " + catalogModifiedDate);
												}
												nodeService.setProperty(entityNodeRef, catalogModifiedDate, new Date());
												break;
											}
										}
									}
								}
							}
						}
					}
				}
			}

		} catch (JSONException e) {
			logger.error("Unable to update catalog's properties!!", e);
		}

	}

	public boolean isMatchEntityType(JSONObject catalog, QName productType, NamespaceService namespaceService) throws JSONException {
		JSONArray catalogEntityTypes = (catalog.has(PROP_ENTITY_TYPE)) ? catalog.getJSONArray(PROP_ENTITY_TYPE) : new JSONArray();

		for (int i = 0; i < catalogEntityTypes.length(); ++i) {
			QName qname = QName.createQName(catalogEntityTypes.getString(i), namespaceService);
			if (qname.isMatch(productType)) {
				return true;
			}
		}

		return false;
	}

	public Set<String> getLocales(List<String> reportLocales, JSONObject catalog) throws JSONException {
		Set<String> langs = new HashSet<>();

		if (catalog.has(PROP_LOCALES)) {
			JSONArray catalogLocales = catalog.getJSONArray(PROP_LOCALES);
			for (int j = 0; j < catalogLocales.length(); j++) {
				langs.add(catalogLocales.getString(j));
			}
		} else if (reportLocales != null) {
			langs.addAll(reportLocales);
		}

		if (langs.isEmpty()) {
			// put system locale
			langs.add(Locale.getDefault().getLanguage());
		}

		return langs;
	}

	public Set<QName> getAuditedFields(JSONObject catalog, NamespaceService namespaceService) throws JSONException {
		Set<QName> ret = new HashSet<>();

		if (catalog.has(PROP_AUDITED_FIELDS)) {
			JSONArray monitoredFields = catalog.getJSONArray(PROP_AUDITED_FIELDS);
			for (int i = 0; i < monitoredFields.length(); i++) {
				String field = monitoredFields.getString(i);
				QName propQName = QName.createQName(field, namespaceService);
				ret.add(propQName);
			}
		}

		return ret;
	}

	public interface EntityCatalogMatcher {

		public boolean matchFormula(String formula);
	}

	public JSONArray formulateCatalogs(NodeRef entityNodeRef, List<String> locales, EntityCatalogMatcher entityCatalogMatcher) throws JSONException {
		return formulateCatalog(null, entityNodeRef, locales, entityCatalogMatcher);
	}

	public JSONArray formulateCatalog(String catalogId, NodeRef entityNodeRef, List<String> locales, EntityCatalogMatcher entityCatalogMatcher)
			throws JSONException {
		JSONArray ret = new JSONArray();

		List<JSONArray> catalogs = getCatalogsDef();

		if ((!catalogs.isEmpty()) && (entityNodeRef != null) && nodeService.exists(entityNodeRef)) {
			// Break rules !!!!
			Map<QName, Serializable> properties = nodeService.getProperties(entityNodeRef);
			String defaultLocale = Locale.getDefault().getLanguage();
			QName productType = nodeService.getType(entityNodeRef);

			for (JSONArray catalogDef : catalogs) {
				for (int i = 0; i < catalogDef.length(); i++) {
					JSONObject catalog = catalogDef.getJSONObject(i);

					if ((catalogId == null) || catalog.getString(PROP_ID).equals(catalogId)) {

						boolean matchesOnType = isMatchEntityType(catalog, productType, namespaceService);
						if (catalog.has(EntityCatalogService.PROP_ENTITY_FILTER)) {
							String filterFormula = catalog.getString(EntityCatalogService.PROP_ENTITY_FILTER);
							matchesOnType = matchesOnType && ((entityCatalogMatcher == null) || entityCatalogMatcher.matchFormula(filterFormula));
						}

						if (logger.isDebugEnabled()) {
							logger.debug("\n\t\t== Catalog \"" + catalog.getString(EntityCatalogService.PROP_LABEL) + "\" ==");
							logger.debug("Type of product: " + productType);
							logger.debug("Catalog json: " + catalog);
							logger.debug("ProductPassesFilter: " + matchesOnType);
						}

						// if this catalog applies to this type, or this catalog
						// has
						// no
						// type defined (it applies to every entity type)
						if (matchesOnType) {

							if (logger.isDebugEnabled()) {
								logger.debug("Formulating for catalog \"" + catalog.getString(EntityCatalogService.PROP_LABEL) + "\"");
							}
							List<String> langs = new LinkedList<>(getLocales(locales, catalog));

							langs.sort((o1, o2) -> {
								if (o1.equals(defaultLocale)) {
									return -1;
								}
								if (o2.equals(defaultLocale)) {
									return 1;
								}
								return 0;
							});

							String color = catalog.has(EntityCatalogService.PROP_COLOR) ? catalog.getString(EntityCatalogService.PROP_COLOR)
									: "hsl(" + (i * (360 / 7)) + ", 60%, 50%)";

							JSONArray reqFields = catalog.has(EntityCatalogService.PROP_FIELDS)
									? catalog.getJSONArray(EntityCatalogService.PROP_FIELDS)
									: new JSONArray();
							JSONObject i18nMessages = catalog.has(EntityCatalogService.PROP_I18N_MESSAGES)
									? catalog.getJSONObject(EntityCatalogService.PROP_I18N_MESSAGES)
									: new JSONObject();
							JSONArray uniqueFields = catalog.has(EntityCatalogService.PROP_UNIQUE_FIELDS)
									? catalog.getJSONArray(EntityCatalogService.PROP_UNIQUE_FIELDS)
									: new JSONArray();

							JSONArray nonUniqueFields = extractNonUniqueFields(entityNodeRef, catalog.getString(EntityCatalogService.PROP_LABEL),
									properties, uniqueFields, i18nMessages);

							for (String lang : langs) {

								JSONObject catalogDesc = new JSONObject();

								logger.debug("=== Catalog name: " + catalog.getString(EntityCatalogService.PROP_LABEL) + ", lang: " + lang);

								JSONArray missingFields = extractMissingFields(entityNodeRef, properties, reqFields, i18nMessages,
										defaultLocale.equals(lang) ? null : lang);
								if ((missingFields.length() > 0) || (nonUniqueFields.length() > 0)) {

									catalogDesc.put(EntityCatalogService.PROP_MISSING_FIELDS, missingFields.length() > 0 ? missingFields : null);
									catalogDesc.put(EntityCatalogService.PROP_NON_UNIQUE_FIELDS,
											nonUniqueFields.length() > 0 ? nonUniqueFields : null);
								}

								catalogDesc.put(EntityCatalogService.PROP_LOCALE, defaultLocale.equals(lang) ? null : lang);
								catalogDesc.put(EntityCatalogService.PROP_SCORE,
										((reqFields.length() - missingFields.length()) * 100d) / (reqFields.length() > 0 ? reqFields.length() : 1d));
								catalogDesc.put(EntityCatalogService.PROP_LABEL, catalog.getString(EntityCatalogService.PROP_LABEL));
								catalogDesc.put(EntityCatalogService.PROP_ID, catalog.getString(EntityCatalogService.PROP_ID));
								catalogDesc.put(EntityCatalogService.PROP_COLOR, color);

								if (catalog.has(EntityCatalogService.PROP_CATALOG_MODIFIED_FIELD)) {
									QName catalogModifiedDate = QName.createQName(catalog.getString(EntityCatalogService.PROP_CATALOG_MODIFIED_FIELD),
											namespaceService);
									Date modifiedDate = (Date) properties.get(catalogModifiedDate);
									if (modifiedDate != null) {
										catalogDesc.put(EntityCatalogService.PROP_CATALOG_MODIFIED_DATE, ISO8601DateFormat.format(modifiedDate));
									}
								}
								ret.put(catalogDesc);

							}
						}
					}
				}
			}
		}

		return ret;
	}

	private JSONArray extractNonUniqueFields(NodeRef productNodeRef, String catalogName, Map<QName, Serializable> properties, JSONArray uniqueFields,
			JSONObject i18nMessages) throws JSONException {
		JSONArray res = new JSONArray();

		if (productNodeRef != null) {
			for (int i = 0; i < uniqueFields.length(); i++) {

				String fieldDef = uniqueFields.getString(i);
				String field;
				QName typeQName = nodeService.getType(productNodeRef);
				QName propQName = null;

				if (fieldDef.contains("|")) {
					propQName = QName.createQName(fieldDef.split("\\|")[0], namespaceService);
					typeQName = QName.createQName(fieldDef.split("\\|")[1], namespaceService);
					field = fieldDef.split("\\|")[0];

				} else {
					propQName = QName.createQName(fieldDef, namespaceService);
					field = fieldDef;
				}

				Serializable propValue = nodeService.getProperty(productNodeRef, propQName);

				if (propValue != null) {

					List<NodeRef> propDuplicates = getPropertyDuplicates(productNodeRef, typeQName, propQName, propValue.toString());

					if (!(propDuplicates.isEmpty())) {

						ClassAttributeDefinition classDef = formatQnameString(field);
						String propTitle = getFieldDisplayName(classDef, i18nMessages.has(field) ? i18nMessages.getString(field) : null);

						JSONObject nonUniqueField = new JSONObject();
						nonUniqueField.put(EntityCatalogService.PROP_ID, field);
						nonUniqueField.put(EntityCatalogService.PROP_DISPLAY_NAME, propTitle);
						nonUniqueField.put(EntityCatalogService.PROP_VALUE, propValue);
						nonUniqueField.put(EntityCatalogService.PROP_ENTITIES, toJsonArray(propDuplicates));

						MLText displayMLName = MLTextHelper.createMLTextI18N(loc -> {
							Locale old = I18NUtil.getLocale();
							String ret = "";
							try {
								I18NUtil.setLocale(loc);
								return getFieldDisplayName(classDef, i18nMessages.has(field) ? i18nMessages.getString(field) : null);

							} catch (JSONException e) {
								logger.error(e, e);
							} finally {
								I18NUtil.setLocale(old);
							}

							return ret;

						});

						if (displayMLName != null) {
							for (Map.Entry<Locale, String> mlEntry : displayMLName.entrySet()) {
								String code = MLTextHelper.localeKey(mlEntry.getKey());
								if ((code != null) && !code.isEmpty() && (mlEntry.getValue() != null)) {
									nonUniqueField.put(EntityCatalogService.PROP_DISPLAY_NAME + "_" + code, mlEntry.getValue());

								}
							}
						}

						res.put(nonUniqueField);

					}
				}
			}
		}

		return res;
	}

	private JSONArray toJsonArray(List<NodeRef> propDuplicates) {
		JSONArray ret = new JSONArray();
		for (NodeRef tmp : propDuplicates) {
			ret.put(tmp.toString());
		}
		return ret;
	}

	private List<NodeRef> getPropertyDuplicates(NodeRef productNodeRef, QName typeQName, QName propQName, String value) {

		List<NodeRef> queryResults = new ArrayList<>();
		if ((value != null) && !value.isEmpty()) {

			queryResults = BeCPGQueryBuilder.createQuery().ofType(typeQName).andNotID(productNodeRef).excludeDefaults()
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

	private JSONArray extractMissingFields(NodeRef entityNodeRef, Map<QName, Serializable> properties, JSONArray reqFields, JSONObject i18nMessages,
			String lang) throws JSONException {
		JSONArray ret = new JSONArray();

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
					if (mlTextIsPresent(currentField, entityNodeRef, lang, properties)) {
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
					if (associationService.getTargetAssoc(entityNodeRef, fieldQname) != null) {
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
				ret.put(createMissingFields(entityNodeRef, splitFields, i18nMessages));
			}

		}

		return ret;
	}

	private boolean mlTextIsPresent(String field, NodeRef entityNodeRef, String lang, Map<QName, Serializable> properties) {
		boolean res = true;
		QName fieldQname = QName.createQName(field.split("_")[0], namespaceService);
		MLText mlText = (MLText) mlNodeService.getProperty(entityNodeRef, fieldQname);
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

	private JSONObject createMissingFields(NodeRef enrityNodeRef, List<String> fields, JSONObject i18nMessages) throws JSONException {

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

			String i18nKey = i18nMessages.has(currentField) ? i18nMessages.getString(currentField) : null;

			id += classDef.getName().toPrefixString(namespaceService) + (i == (fields.size() - 1) ? "" : "|");
			displayName += getFieldDisplayName(classDef, i18nKey) + (i == (fields.size() - 1) ? "" : " " + I18NUtil.getMessage(MESSAGE_OR) + " ");

		}

		if (lang != null) {
			field.put(EntityCatalogService.PROP_LOCALE, lang);
		}

		field.put(EntityCatalogService.PROP_ID, id);
		field.put(EntityCatalogService.PROP_DISPLAY_NAME, displayName);

		MLText displayMLName = MLTextHelper.createMLTextI18N(loc -> {
			Locale old = I18NUtil.getLocale();
			String ret = "";
			try {
				I18NUtil.setLocale(loc);

				for (int i = 0; i < fields.size(); ++i) {
					String currentField = fields.get(i);
					ClassAttributeDefinition classDef = formatQnameString(currentField);

					String i18nKey = i18nMessages.has(currentField) ? i18nMessages.getString(currentField) : null;

					ret = ret + getFieldDisplayName(classDef, i18nKey)
							+ (i == (fields.size() - 1) ? "" : " " + I18NUtil.getMessage(MESSAGE_OR) + " ");
				}
			} catch (JSONException e) {
				logger.error(e, e);
			} finally {
				I18NUtil.setLocale(old);
			}

			return ret;

		});

		if (displayMLName != null) {
			for (Map.Entry<Locale, String> mlEntry : displayMLName.entrySet()) {
				String code = MLTextHelper.localeKey(mlEntry.getKey());
				if ((code != null) && !code.isEmpty() && (mlEntry.getValue() != null)) {
					field.put(EntityCatalogService.PROP_DISPLAY_NAME + "_" + code, mlEntry.getValue());

				}
			}
		}

		return field;

	}

	public String getFieldDisplayName(ClassAttributeDefinition classDef, String messageKey) {
		String displayName = messageKey != null ? I18NUtil.getMessage(messageKey) : classDef.getTitle(dictionaryService);
		return displayName != null ? displayName : messageKey;
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

}
