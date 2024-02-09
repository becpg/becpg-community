package fr.becpg.repo.entity.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.view.ExporterService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.BeCPGModel.EntityFormat;
import fr.becpg.model.DataListModel;
import fr.becpg.repo.entity.EntityFormatService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.entity.remote.RemoteParams;

@Service("entityFormatService")
public class EntityFormatServiceImpl implements EntityFormatService {

	@Autowired
	@Qualifier("mtAwareNodeService")
	private NodeService dbNodeService;

	@Autowired
	protected EntityListDAO entityListDAO;

	@Autowired
	private RemoteEntityService remoteEntityService;

	@Autowired
	protected ContentService contentService;

	@Autowired
	protected NamespaceService namespaceService;
	
	@Autowired
	@Qualifier("exporterComponent")
	private ExporterService exporterService;
	
	@Autowired
	protected Repository repositoryHelper;

	@Autowired
	protected RuleService ruleService;
	
	private static final Log logger = LogFactory.getLog(EntityFormatServiceImpl.class);

	@Override
	public String generateEntityData(NodeRef entityNodeRef, EntityFormat toFormat) {
		return generateEntityData(entityNodeRef, toFormat, null);
	}
	
	@Override
	public String generateEntityData(NodeRef entityNodeRef, EntityFormat toFormat, Map<String, Object> extraParams) {
		
		if (EntityFormat.JSON.equals(toFormat)) {
			
			boolean isMLAware = MLPropertyInterceptor.setMLAware(false);
			
			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
		
				RemoteParams remoteParams = new RemoteParams(RemoteEntityFormat.json_all);
				
				remoteParams.setFilteredLists(List.of("!" + BeCPGModel.TYPE_ACTIVITY_LIST.getLocalName()));
				
				JSONObject jsonParams = new JSONObject();
				jsonParams.put(RemoteParams.PARAM_APPEND_MLTEXT_CONSTRAINT, false);
				jsonParams.put(RemoteParams.PARAM_UPDATE_ENTITY_NODEREFS, true);
				jsonParams.put(RemoteParams.PARAM_REPLACE_HISTORY_NODEREFS, true);
				
				if (extraParams != null) {
					for (Entry<String, Object> entry : extraParams.entrySet()) {
						jsonParams.put(entry.getKey(), entry.getValue());
					}
				}
				
				remoteParams.setJsonParams(jsonParams);
				
				remoteEntityService.getEntity(entityNodeRef, out, remoteParams);
				return out.toString();
			} catch (IOException | JSONException e) {
				logger.error("Failed to convert entity to JSON format", e);
			} finally {
				MLPropertyInterceptor.setMLAware(isMLAware);
			}
		}
		
		return null;
	}
	
	@Override
	public void convertToFormat(NodeRef entityNodeRef, EntityFormat targetFormat) {
		if (EntityFormat.JSON.equals(targetFormat)) {
			convertEntityToJsonFormat(entityNodeRef);
		} else if (EntityFormat.NODE.equals(targetFormat)) {
			convertEntityToNodeFormat(entityNodeRef);
		}
	}

	private void convertEntityToJsonFormat(NodeRef entityNodeRef) {
		String currentFormat = getEntityFormat(entityNodeRef);
		
		if (EntityFormat.JSON.toString().equals(currentFormat)) {
			logger.warn("Entity is already in JSON format: " + entityNodeRef);
			return;
		}
				
		String jsonEntityData = generateEntityData(entityNodeRef, EntityFormat.JSON);
		updateEntityFormat(entityNodeRef, EntityFormat.JSON, jsonEntityData);
		removeEntityDatalists(entityNodeRef);
	}

	private void removeEntityDatalists(NodeRef entityNodeRef) {
		NodeRef listContainer = entityListDAO.getListContainer(entityNodeRef);
	
		if (listContainer != null) {
			List<ChildAssociationRef> childAssocs = dbNodeService.getChildAssocs(listContainer);
			for (ChildAssociationRef childAssoc : childAssocs) {
				if (dbNodeService.getType(childAssoc.getChildRef()).equals(DataListModel.TYPE_DATALIST)) {
					List<ChildAssociationRef> listItems = dbNodeService.getChildAssocs(childAssoc.getChildRef());
					for (ChildAssociationRef listItem : listItems) {
						dbNodeService.removeChild(childAssoc.getChildRef(), listItem.getChildRef());
					}
				}
			}
		}
	}

	private void convertEntityToNodeFormat(NodeRef entityNodeRef) {
		String currentFormat = getEntityFormat(entityNodeRef);
		if (currentFormat == null || EntityFormat.NODE.toString().equals(currentFormat)) {
			logger.warn("Entity is already in NODE format: " + entityNodeRef);
			return;
		}
		
		String entityJson = getEntityData(entityNodeRef);
		
		if (entityJson != null) {
			ByteArrayInputStream in = new ByteArrayInputStream(entityJson.getBytes());
			if (EntityFormat.JSON.toString().equals(currentFormat)) {
				remoteEntityService.createOrUpdateEntity(entityNodeRef, in,  new RemoteParams(RemoteEntityFormat.json), null);
			} else if (EntityFormat.XML.toString().equals(currentFormat)) {
				remoteEntityService.createOrUpdateEntity(entityNodeRef, in,  new RemoteParams(RemoteEntityFormat.xml), null);
			}
		}
		
		dbNodeService.setProperty(entityNodeRef, BeCPGModel.PROP_ENTITY_FORMAT, EntityFormat.NODE);
		dbNodeService.removeProperty(entityNodeRef, BeCPGModel.PROP_ENTITY_DATA);
	}

	@Override
	public void createOrUpdateEntityFromJson(NodeRef entityNodeRef, String entityJson) {
		
		try {
			JSONObject root = new JSONObject(entityJson);
			
			if (root.has(RemoteEntityService.ELEM_ENTITY)) {
				JSONObject entity = root.getJSONObject(RemoteEntityService.ELEM_ENTITY);
				
				if (!entity.has(RemoteEntityService.ELEM_PARAMS)) {
					JSONObject params = new JSONObject();
					entity.put(RemoteEntityService.ELEM_PARAMS, params);
				}
				
				JSONObject params = entity.getJSONObject(RemoteEntityService.ELEM_PARAMS);
				params.put(RemoteParams.PARAM_FAIL_ON_ASSOC_NOT_FOUND, false);
			}

			ByteArrayInputStream in = new ByteArrayInputStream(root.toString().getBytes());
			remoteEntityService.createOrUpdateEntity(entityNodeRef, in, new RemoteParams(RemoteEntityFormat.json), null);
		} catch (JSONException e) {
			logger.error("Failed to parse JSON", e);
		}
		
	}
	
	@Override
	public void updateEntityFormat(NodeRef entityNodeRef, EntityFormat format, String data) {
		dbNodeService.setProperty(entityNodeRef, BeCPGModel.PROP_ENTITY_FORMAT, format);
		ContentWriter contentWriter = this.contentService.getWriter(entityNodeRef, BeCPGModel.PROP_ENTITY_DATA, true);
		contentWriter.setEncoding("UTF-8");
		contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
		contentWriter.putContent(data);
	}

	@Override
	public String getEntityData(NodeRef entityNodeRef) {
		if (dbNodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_FORMAT)) {
			ContentReader reader = this.contentService.getReader(entityNodeRef, BeCPGModel.PROP_ENTITY_DATA);
			if (reader != null) {
				return reader.getContentString();
			}
		}

		return null;
	}

	@Override
	public String getEntityFormat(NodeRef entityNodeRef) {
		if (entityNodeRef != null && dbNodeService.exists(entityNodeRef) && dbNodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_FORMAT)) {
			Serializable prop = dbNodeService.getProperty(entityNodeRef, BeCPGModel.PROP_ENTITY_FORMAT);
			return prop == null ? null : prop.toString();
		}
		return null;
	}
	
}
