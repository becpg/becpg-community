package fr.becpg.repo.entity.catalog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.DataListModel;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.search.BeCPGQueryBuilder;

@Service("entityCatalogService")
public class EntityCatalogService {

	public static final Log logger = LogFactory.getLog(EntityCatalogService.class);

	public static final String PROP_CATALOGS = "catalogs";
	public static final String PROP_MISSING_FIELDS = "missingFields";
	public static final String PROP_UNIQUE_FIELDS = "uniqueFields";
	public static final String PROP_NON_UNIQUE_FIELDS = "nonUniqueFields";
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
	public static final String CATALOGS_PATH = "/app:company_home/cm:System/cm:PropertyCatalogs";
	public static final String CATALOG_DEFS = "CATALOG_DEFS";



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
											if (auditedFields.contains(beforeType)
													&& (((beforeValue == null) && (after != null)) || !beforeValue.equals(after.get(beforeType)))) {
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

}
