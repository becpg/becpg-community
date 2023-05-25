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

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.expressions.ExpressionService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>
 * EntityCatalogService class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("entityCatalogService")
public class EntityCatalogServiceImpl implements EntityCatalogService {

	/** Constant <code>logger</code> */
	public static final Log logger = LogFactory.getLog(EntityCatalogServiceImpl.class);

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

	@Autowired
	private ExpressionService expressionService;

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	@Autowired
	private EntityCatalogObserver[] observers;

	/**
	 * <p>
	 * getCatalogsDef.
	 * </p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	private List<JSONArray> getCatalogsDef() {

		return beCPGCacheService.getFromCache(EntityCatalogService.class.getName(), CATALOG_DEFS, () -> {

			List<JSONArray> res = new ArrayList<>();

			// get JSON from file in system
			NodeRef folder = getCatalogFolderNodeRef();

			List<FileInfo> files = null;
			if (folder != null) {
				files = fileFolderService.list(folder);
			}

			if ((files != null) && !files.isEmpty()) {

				for (FileInfo file : files) {
					logger.debug("File in catalog folder nr: " + file.getNodeRef());
					ContentReader reader = contentService.getReader(file.getNodeRef(), ContentModel.PROP_CONTENT);
					String content = reader.getContentString();
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
		return BeCPGQueryBuilder.createQuery().selectNodeByPath(repository.getCompanyHome(), RepoConsts.CATALOGS_PATH);
	}

	/**
	 * <p>
	 * updateAuditedField.
	 * </p>
	 *
	 * @param entityNodeRef
	 *            a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param before
	 *            a {@link java.util.Map} object.
	 * @param after
	 *            a {@link java.util.Map} object.
	 * @param listNodeRefs
	 *            a {@link java.util.Set} object.
	 */
	@Override
	public void updateAuditedField(NodeRef entityNodeRef, Set<QName> diffQnames, Set<NodeRef> listNodeRefs) {
		try {
			if ((diffQnames != null || listNodeRefs != null) && nodeService.exists(entityNodeRef)) {

				for (JSONArray catalogDef : getCatalogsDef()) {

					for (int i = 0; i < catalogDef.length(); i++) {
						JSONObject catalog = catalogDef.getJSONObject(i);
						QName type = nodeService.getType(entityNodeRef);
						if (isMatchEntityType(catalog, type, namespaceService) && catalog.has(PROP_AUDITED_FIELDS)
								&& isMatchFilter(catalog, entityNodeRef)) {
							Set<QName> auditedFields = getAuditedFields(catalog, namespaceService);
							QName changedField = checkHasChange(auditedFields, diffQnames, listNodeRefs);

							if (changedField != null) {
								if (catalog.has(PROP_CATALOG_MODIFIED_FIELD)) {
									QName catalogModifiedDate = QName.createQName(catalog.getString(PROP_CATALOG_MODIFIED_FIELD), namespaceService);
									if (logger.isDebugEnabled()) {
										logger.debug("Audited field " + changedField + " has changed, update date: " + catalogModifiedDate);
									}

									nodeService.setProperty(entityNodeRef, catalogModifiedDate, new Date());
								}
								for (EntityCatalogObserver observer : observers) {
									if (observer.acceptCatalogEvents(type, entityNodeRef)) {
										observer.notifyAuditedFieldChange(catalog.getString(PROP_ID), entityNodeRef);
									}
								}
							}
						}
					}
				}

				if (checkHasChange(
						new HashSet<>(Arrays.asList(ContentModel.PROP_MODIFIED, ContentModel.PROP_CREATED, BeCPGModel.PROP_FORMULATED_DATE)),
						diffQnames, null) != null) {
					for (EntityCatalogObserver observer : observers) {
						if (observer.acceptCatalogEvents(ContentModel.PROP_MODIFIED, entityNodeRef)) {
							observer.notifyAuditedFieldChange(null, entityNodeRef);
						}
					}
				}

			}

		} catch (JSONException e) {
			logger.error("Unable to update catalog's properties!!", e);
		}

	}

	private QName checkHasChange(Set<QName> auditedFields, Set<QName> diffQnames, Set<NodeRef> listNodeRefs) {
		QName changedField = null;
		if ((auditedFields != null) && !auditedFields.isEmpty()) {

			if ((listNodeRefs != null) && !listNodeRefs.isEmpty()) {
				for (NodeRef listNodeRef : listNodeRefs) {
					QName listType = QName.createQName((String) nodeService.getProperty(listNodeRef, DataListModel.PROP_DATALISTITEMTYPE),
							namespaceService);
					if (auditedFields.contains(listType)) {
						changedField = listType;
						break;
					}
				}
			} else if ((diffQnames != null) && !diffQnames.isEmpty()) {
				for (QName diffQName : diffQnames) {
					if (auditedFields.contains(diffQName)) {
						changedField = diffQName;
						break;
					}
				}

			}
		}
		return changedField;
	}

	private boolean isMatchFilter(JSONObject catalog, NodeRef entityNodeRef) throws JSONException {
		if (catalog.has(EntityCatalogService.PROP_ENTITY_FILTER)) {
			return isMatchFilter(catalog, alfrescoRepository.findOne(entityNodeRef));
		}
		return true;

	}

	private boolean isMatchFilter(JSONObject catalog, RepositoryEntity entity) throws JSONException {

		if (catalog.has(EntityCatalogService.PROP_ENTITY_FILTER)) {

			return testCondition(catalog.getString(EntityCatalogService.PROP_ENTITY_FILTER), entity);

		}
		return true;
	}

	private boolean testCondition(String condition, RepositoryEntity entity) {

		if ((condition != null) && !(condition.startsWith("spel") || condition.startsWith("js"))) {
			condition = "spel(" + condition + ")";
		}
		Object filter = expressionService.eval(condition, entity);
		if ((filter == null) || !Boolean.parseBoolean(filter.toString())) {
			logger.debug("Skipping condition doesn't match : [" + condition + "]");
			return false;
		}

		return true;
	}

	/**
	 * <p>
	 * isMatchEntityType.
	 * </p>
	 *
	 * @param catalog
	 *            a {@link org.json.JSONObject} object.
	 * @param productType
	 *            a {@link org.alfresco.service.namespace.QName} object.
	 * @param namespaceService
	 *            a {@link org.alfresco.service.namespace.NamespaceService}
	 *            object.
	 * @return a boolean.
	 * @throws org.json.JSONException
	 *             if any.
	 */
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

	/**
	 * <p>
	 * getLocales.
	 * </p>
	 *
	 * @param reportLocales
	 *            a {@link java.util.List} object.
	 * @param catalog
	 *            a {@link org.json.JSONObject} object.
	 * @return a {@link java.util.Set} object.
	 * @throws org.json.JSONException
	 *             if any.
	 */
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

	/**
	 * <p>
	 * getAuditedFields.
	 * </p>
	 *
	 * @param catalog
	 *            a {@link org.json.JSONObject} object.
	 * @param namespaceService
	 *            a {@link org.alfresco.service.namespace.NamespaceService}
	 *            object.
	 * @return a {@link java.util.Set} object.
	 * @throws org.json.JSONException
	 *             if any.
	 */
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

	/**
	 * <p>
	 * formulateCatalogs.
	 * </p>
	 *
	 * @param entityNodeRef
	 *            a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param locales
	 *            a {@link java.util.List} object.
	 * @param entityCatalogMatcher
	 *            a
	 *            {@link fr.becpg.repo.entity.catalog.EntityCatalogService.EntityCatalogMatcher}
	 *            object.
	 * @return a {@link org.json.JSONArray} object.
	 * @throws org.json.JSONException
	 *             if any.
	 */
	@Override
	public JSONArray formulateCatalogs(RepositoryEntity formulatedEntity, List<String> locales) throws JSONException {
		return formulateCatalog(null, formulatedEntity, locales);
	}

	@Override
	public JSONArray formulateCatalog(String catalogId, NodeRef entityNodeRef, List<String> locales) throws JSONException {
		return formulateCatalog(catalogId, alfrescoRepository.findOne(entityNodeRef), locales);
	}

	/**
	 * <p>
	 * formulateCatalog.
	 * </p>
	 *
	 * @param catalogId
	 *            a {@link java.lang.String} object.
	 * @param entityNodeRef
	 *            a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param locales
	 *            a {@link java.util.List} object.
	 * @param entityCatalogMatcher
	 *            a
	 *            {@link fr.becpg.repo.entity.catalog.EntityCatalogService.EntityCatalogMatcher}
	 *            object.
	 * @return a {@link org.json.JSONArray} object.
	 * @throws org.json.JSONException
	 *             if any.
	 */
	public JSONArray formulateCatalog(String catalogId, RepositoryEntity formulatedEntity, List<String> locales) throws JSONException {
		JSONArray ret = new JSONArray();

		List<JSONArray> catalogs = getCatalogsDef();

		if ((!catalogs.isEmpty()) && (formulatedEntity.getNodeRef() != null) && nodeService.exists(formulatedEntity.getNodeRef())) {
			NodeRef entityNodeRef = formulatedEntity.getNodeRef();
			QName entityType = nodeService.getType(entityNodeRef);
			Map<QName, Serializable> properties = null;
			String defaultLocale = Locale.getDefault().getLanguage();

			for (JSONArray catalogDef : catalogs) {
				for (int i = 0; i < catalogDef.length(); i++) {
					JSONObject catalog = catalogDef.getJSONObject(i);

					if (((catalogId == null) || catalog.getString(PROP_ID).equals(catalogId))
							&& (isMatchEntityType(catalog, entityType, namespaceService)) && isMatchFilter(catalog, formulatedEntity)) {

						if (logger.isDebugEnabled()) {
							logger.debug("Formulating catalog \"" + catalog.getString(EntityCatalogService.PROP_LABEL) + "\"");
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

						JSONArray reqFields = catalog.has(EntityCatalogService.PROP_FIELDS) ? catalog.getJSONArray(EntityCatalogService.PROP_FIELDS)
								: new JSONArray();
						JSONObject i18nMessages = catalog.has(EntityCatalogService.PROP_I18N_MESSAGES)
								? catalog.getJSONObject(EntityCatalogService.PROP_I18N_MESSAGES)
								: new JSONObject();
						JSONArray uniqueFields = catalog.has(EntityCatalogService.PROP_UNIQUE_FIELDS)
								? catalog.getJSONArray(EntityCatalogService.PROP_UNIQUE_FIELDS)
								: new JSONArray();

						JSONArray nonUniqueFields = extractNonUniqueFields(entityType, entityNodeRef, uniqueFields, i18nMessages);

						for (String lang : langs) {

							JSONObject catalogDesc = new JSONObject();

							if (properties == null) {
								properties = nodeService.getProperties(entityNodeRef);
							}

							JSONArray missingFields = extractMissingFields(formulatedEntity, properties, reqFields, i18nMessages,
									defaultLocale.equals(lang) ? null : lang);
							if ((missingFields.length() > 0) || (nonUniqueFields.length() > 0)) {

								catalogDesc.put(EntityCatalogService.PROP_MISSING_FIELDS, missingFields.length() > 0 ? missingFields : null);
								catalogDesc.put(EntityCatalogService.PROP_NON_UNIQUE_FIELDS, nonUniqueFields.length() > 0 ? nonUniqueFields : null);
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

		return ret;
	}

	private JSONArray extractNonUniqueFields(QName entityType, NodeRef entityNodeRef, JSONArray uniqueFields, JSONObject i18nMessages)
			throws JSONException {
		JSONArray res = new JSONArray();

		if (entityNodeRef != null) {
			for (int i = 0; i < uniqueFields.length(); i++) {

				String fieldDef = uniqueFields.getString(i);
				String field;
				QName propQName = null;

				if (fieldDef.contains("|")) {
					propQName = QName.createQName(fieldDef.split("\\|")[0], namespaceService);
					entityType = QName.createQName(fieldDef.split("\\|")[1], namespaceService);
					field = fieldDef.split("\\|")[0];

				} else {
					propQName = QName.createQName(fieldDef, namespaceService);
					field = fieldDef;
				}

				Serializable propValue = nodeService.getProperty(entityNodeRef, propQName);

				if (propValue != null) {

					List<NodeRef> propDuplicates = getPropertyDuplicates(entityNodeRef, entityType, propQName, propValue.toString());

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

			queryResults = BeCPGQueryBuilder.createQuery().ofType(typeQName).andNotID(productNodeRef).andPropEquals(propQName, value).inDB()
					.maxResults(10).list();

			List<NodeRef> falsePositives = new ArrayList<>();

			// Remove version
			for (NodeRef result : queryResults) {
				if (nodeService.hasAspect(result, BeCPGModel.ASPECT_COMPOSITE_VERSION)) {

					falsePositives.add(result);
				}
			}

			for (NodeRef falsePositive : falsePositives) {
				queryResults.remove(falsePositive);
			}
		}

		return queryResults;
	}

	private JSONArray extractMissingFields(RepositoryEntity formulatedEntity, Map<QName, Serializable> properties, JSONArray reqFields,
			JSONObject i18nMessages, String lang) throws JSONException {
		JSONArray ret = new JSONArray();

		for (int i = 0; i < reqFields.length(); i++) {
			String field = reqFields.getString(i);

			List<String> splitFields = Arrays.asList(field.split(Pattern.quote("|")));
			boolean present = false;

			// if this field can be ignored (do not raise ctrl if absent)
			boolean ignore = false;

			String currLang = null;
			StringBuilder id = new StringBuilder();
			MLText displayName = new MLText();

			if ((splitFields.size() == 2) && splitFields.get(0).startsWith("formula")) {
				present = testCondition(splitFields.get(1), formulatedEntity);
				id.append(splitFields.get(0));
				String i18nkey = i18nMessages.has(splitFields.get(0)) ? i18nMessages.getString(splitFields.get(0)) : splitFields.get(0);

				for (String key : RepoConsts.SUPPORTED_UI_LOCALES.split(",")) {
					if (MLTextHelper.getSupportedLocalesList().contains(key)) {
						Locale loc = MLTextHelper.parseLocale(key);
						String label = I18NUtil.getMessage(i18nkey, loc);
						if ((label != null) && !label.isBlank()) {
							displayName.addValue(loc, label);
						} else {
							displayName.addValue(loc, i18nkey);
						}
					}
				}
			} else {

				for (String currentField : splitFields) {

					if (currentField.contains("_")) {
						currLang = currentField.split("_")[1];
						currentField = currentField.split("_")[0];
					}

					QName fieldQname = null;

					try {
						fieldQname = QName.createQName(currentField, namespaceService);
						String i18nKey = i18nMessages.has(currentField) ? i18nMessages.getString(currentField) : null;

						if (logger.isDebugEnabled()) {
							logger.debug("Test missing field qname: " + fieldQname + ", lang: " + lang);
						}

						ClassAttributeDefinition propDef = dictionaryService.getProperty(fieldQname);

						if ((propDef instanceof PropertyDefinition)
								&& (DataTypeDefinition.MLTEXT.equals(((PropertyDefinition) propDef).getDataType().getName()))) {
							// prop is present
							if (mlTextIsPresent(fieldQname, formulatedEntity.getNodeRef(), lang, currLang, properties)) {
								logger.debug("mlProp is present");
								present = true;
							}

						} else if ((propDef instanceof PropertyDefinition) && (lang == null)) {
							// non ML field case
							if ((properties.get(fieldQname) != null) && !properties.get(fieldQname).toString().isEmpty()) {
								logger.debug("regular prop is present");
								present = true;
							}
						} else if ((propDef instanceof PropertyDefinition)
								&& !DataTypeDefinition.MLTEXT.equals(((PropertyDefinition) propDef).getDataType().getName()) && (lang != null)) {
							logger.debug("Non ml prop with non null lang, skipping");
							// case non ml prop with not null lang, we don't care
							ignore = true;

						} else if ((propDef == null) && (lang == null)) {

							propDef = dictionaryService.getAssociation(fieldQname);

							if (propDef == null) {
								ignore = true;
							} else {
								// only check assoc when lang is null
								logger.debug("Checking if assoc is found");
								if (associationService.getTargetAssoc(formulatedEntity.getNodeRef(), fieldQname) != null) {
									present = true;
								}
							}

						} else {
							// lang is not null and it's not a prop
							logger.debug("Skipping associations on localized catalogs");
							ignore = true;
						}

						if (!ignore) {
							if (!id.toString().isBlank()) {
								id.append("|");
							}
							id.append(currentField);

							for (String key : RepoConsts.SUPPORTED_UI_LOCALES.split(",")) {
								if (MLTextHelper.getSupportedLocalesList().contains(key)) {
									Locale loc = MLTextHelper.parseLocale(key);
									String label = displayName.get(loc) != null ? displayName.get(loc) : "";
									Locale old = I18NUtil.getLocale();
									try {
										I18NUtil.setLocale(loc);
										if (!label.isBlank()) {
											label += " " + I18NUtil.getMessage(MESSAGE_OR) + " ";
										}
										label += getFieldDisplayName(propDef, i18nKey);

										displayName.addValue(loc, label);
									} finally {
										I18NUtil.setLocale(old);
									}

								}
							}

						}

					} catch (NamespaceException e) {
						// happens if namespace does not exist
						ignore = true;
					}
				}

				if (ignore) {
					break;
				}

			}

			if (!present && !ignore) {
				if (logger.isDebugEnabled()) {
					logger.debug("\tfield " + field + " is absent...");
				}

				JSONObject missingField = new JSONObject();

				missingField.put(EntityCatalogService.PROP_ID, id.toString());

				if (currLang != null) {
					missingField.put(EntityCatalogService.PROP_LOCALE, currLang);
				}

				for (Map.Entry<Locale, String> mlEntry : displayName.entrySet()) {
					String code = MLTextHelper.localeKey(mlEntry.getKey());
					if ((code != null) && !code.isEmpty() && (mlEntry.getValue() != null)) {
						missingField.put(EntityCatalogService.PROP_DISPLAY_NAME + "_" + code, mlEntry.getValue());
						if (MLTextHelper.isDefaultLocale(mlEntry.getKey())) {
							missingField.put(EntityCatalogService.PROP_DISPLAY_NAME, mlEntry.getValue());
						}
					}
				}

				ret.put(missingField);
			}

		}

		return ret;
	}

	private boolean mlTextIsPresent(QName fieldQname, NodeRef entityNodeRef, String lang, String curLang, Map<QName, Serializable> properties) {
		boolean res = true;
		MLText mlText = (MLText) mlNodeService.getProperty(entityNodeRef, fieldQname);
		Locale loc = null;
		if (lang != null) {
			loc = MLTextHelper.parseLocale(lang);
		}
		if (curLang != null) {
			if ((mlText == null) || ((loc != null) || (mlText.getValue(loc) == null))
					|| mlText.getValue(MLTextHelper.parseLocale(curLang)).isEmpty()) {
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

	/**
	 * <p>
	 * getFieldDisplayName.
	 * </p>
	 *
	 * @param classDef
	 *            a
	 *            {@link org.alfresco.service.cmr.dictionary.ClassAttributeDefinition}
	 *            object.
	 * @param messageKey
	 *            a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getFieldDisplayName(ClassAttributeDefinition classDef, String messageKey) {
		String displayName = messageKey != null ? I18NUtil.getMessage(messageKey) : classDef.getTitle(dictionaryService);
		return displayName != null ? displayName : messageKey;
	}

	private ClassAttributeDefinition formatQnameString(String qNameString) {
		ClassAttributeDefinition res = null;

		qNameString = qNameString.trim();
		PropertyDefinition propDef = dictionaryService.getProperty(QName.createQName(qNameString, namespaceService));

		if (propDef != null) {
			res = propDef;
		} else {
			AssociationDefinition assocDef = dictionaryService.getAssociation(QName.createQName(qNameString, namespaceService));
			res = assocDef;
		}

		return res;
	}

}
