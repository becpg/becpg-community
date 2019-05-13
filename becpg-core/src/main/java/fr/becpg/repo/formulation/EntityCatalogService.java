package fr.becpg.repo.formulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.helper.EntityCatalogHelper;
import fr.becpg.repo.search.BeCPGQueryBuilder;

@Service("catalogService")
public class EntityCatalogService {

	public static final Log logger = LogFactory.getLog(EntityCatalogService.class);

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

		return beCPGCacheService.getFromCache(EntityCatalogService.class.getName(), EntityCatalogHelper.CATALOG_DEFS, () -> {

			List<JSONArray> res = new ArrayList<>();

			// get JSON from file in system
			NodeRef folder = getCatalogFolderNodeRef();
			logger.info("Catalogs folder: " + folder);

			List<FileInfo> files = null;
			if(folder != null) {
				files = fileFolderService.list(folder);
			}
			
			if (files != null && !files.isEmpty()) {

				for (FileInfo file : files) {
					// FileInfo file = files.get(0);

					logger.info("File in catalog folder nr: " + file.getNodeRef());
					ContentReader reader = contentService.getReader(file.getNodeRef(), ContentModel.PROP_CONTENT);
					String content = reader.getContentString();
					logger.debug("Content: " + content);
					// JSONArray res = new JSONArray();

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
		return BeCPGQueryBuilder.createQuery().selectNodeByPath(repository.getCompanyHome(),
				EntityCatalogHelper.CATALOGS_PATH);
	}


	public void updateAuditedField(NodeRef entityNodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after, Set<NodeRef> listNodeRefs) {
		try {
			if ((before != null && after != null) || (listNodeRefs != null)) {
				if(!nodeService.getAspects(entityNodeRef).contains(BeCPGModel.ASPECT_ENTITY_CATALOG)) {
					nodeService.addAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_CATALOG, null);
				}
				for (JSONArray catalogDef : getCatalogsDef()) {
					for (int i = 0; i < catalogDef.length(); i++) {
						JSONObject catalog = catalogDef.getJSONObject(i);
						if(EntityCatalogHelper.isMatcheEntityType(catalog, nodeService.getType(entityNodeRef), namespaceService)) {
							Set<QName> auditedFields = EntityCatalogHelper.getAuditedFields(catalog, namespaceService);
							if(listNodeRefs != null) {
								for(NodeRef listNodeRef : listNodeRefs) {
									QName listType = QName.createQName((String) nodeService.getProperty(listNodeRef, DataListModel.PROP_DATALISTITEMTYPE), namespaceService);
									if(auditedFields.contains(listType)) {
										nodeService.setProperty(entityNodeRef, 
												QName.createQName(catalog.getString(EntityCatalogHelper.PROP_CATALOG_MODIFIED_DATE_FIELD), namespaceService),
												new Date());
										break;
									}
								}
								
							} else {
								for(QName beforeType : before.keySet()) {
									Serializable beforeValue = before.get(beforeType);
									if (auditedFields.contains(beforeType) && !beforeValue.equals(after.get(beforeType))) {
										logger.info("add date: " +catalog.getString(EntityCatalogHelper.PROP_CATALOG_MODIFIED_DATE_FIELD) );
										nodeService.setProperty(entityNodeRef, 
												QName.createQName(catalog.getString(EntityCatalogHelper.PROP_CATALOG_MODIFIED_DATE_FIELD), namespaceService),
												new Date());
										break;
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
	
	



	
	
}
