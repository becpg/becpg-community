package fr.becpg.repo.entity.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.BeCPGModel.EntityFormat;
import fr.becpg.model.DataListModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityFormatService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteEntityService;

@Service("entityFormatService")
public class EntityFormatServiceImpl implements EntityFormatService {

	@Autowired
	protected NodeService nodeService;

	@Autowired
	protected EntityListDAO entityListDAO;

	@Autowired
	private RemoteEntityService remoteEntityService;

	@Autowired
	protected ContentService contentService;

	@Autowired
	protected NamespaceService namespaceService;

	private static final Log logger = LogFactory.getLog(EntityFormatServiceImpl.class);

	@Override
	public void setDatalistFormat(NodeRef entityNodeRef, QName dataListType, EntityFormat format) {

		NodeRef entityListContainer = entityListDAO.getListContainer(entityNodeRef);

		if (entityListContainer != null) {
			NodeRef listNodeRef = entityListDAO.getList(entityListContainer, dataListType);
			setDatalistFormat(listNodeRef, format);
		}
	}

	@Override
	public void setDatalistFormat(NodeRef listNodeRef, EntityFormat format) {
		nodeService.setProperty(listNodeRef, BeCPGModel.PROP_ENTITY_FORMAT, format);
	}

	@Override
	public String getDatalistFormat(NodeRef entityNodeRef, QName dataListType) {

		NodeRef entityListContainer = entityListDAO.getListContainer(entityNodeRef);

		if (entityListContainer != null) {
			NodeRef listNodeRef = entityListDAO.getList(entityListContainer, dataListType);
			if (listNodeRef != null) {
				return getDatalistFormat(listNodeRef);
			}
		}

		return null;
	}

	@Override
	public String getDatalistFormat(NodeRef listNodeRef) {
		return nodeService.exists(listNodeRef) ? (String) nodeService.getProperty(listNodeRef, BeCPGModel.PROP_ENTITY_FORMAT) : null;
	}

	@Override
	public void setDataListData(NodeRef entityNodeRef, QName dataListType, String data) {

		NodeRef entityListContainer = entityListDAO.getListContainer(entityNodeRef);

		if (entityListContainer != null) {
			NodeRef listNodeRef = entityListDAO.getList(entityListContainer, dataListType);
			if (listNodeRef != null) {
				setDataListData(listNodeRef, data);
			}
		}
	}

	@Override
	public void setDataListData(NodeRef listNodeRef, String data) {
		ContentWriter contentWriter = this.contentService.getWriter(listNodeRef, BeCPGModel.PROP_ENTITY_DATA, true);
		contentWriter.setEncoding("UTF-8");
		contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
		contentWriter.putContent(data);
	}

	@Override
	public String getDataListData(NodeRef listNodeRef) {

		if (this.nodeService.hasAspect(listNodeRef, BeCPGModel.ASPECT_ENTITY_FORMAT)) {
			ContentReader reader = this.contentService.getReader(listNodeRef, BeCPGModel.PROP_ENTITY_DATA);
			if (reader != null) {
				return reader.getContentString();
			}
		}
		return null;

	}

	@Override
	public String getDataListData(NodeRef entityNodeRef, QName dataListType) {

		NodeRef entityListContainer = entityListDAO.getListContainer(entityNodeRef);

		if (entityListContainer != null) {
			NodeRef listNodeRef = entityListDAO.getList(entityListContainer, dataListType);
			return getDataListData(listNodeRef);
		}

		return null;
	}

	private String extractDatalistData(NodeRef entityNodeRef, QName dataListType, RemoteEntityFormat format) {

		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			remoteEntityService.getEntity(entityNodeRef, out, format, null, Arrays.asList(dataListType.getLocalName()));

			return out.toString();
		} catch (IOException e) {
			logger.error("Failed to extract datalist", e);
		}

		return null;
	}

	@Override
	public void convertDataListFormat(NodeRef entityNodeRef, QName dataListType, EntityFormat toFormat) {

		NodeRef entityListContainer = entityListDAO.getListContainer(entityNodeRef);

		if (entityListContainer != null) {
			NodeRef listNodeRef = entityListDAO.getList(entityListContainer, dataListType);

			if (listNodeRef != null) {
				convertDataListFormat(listNodeRef, toFormat);
			}
		}
	}

	@Override
	public void convertDataListFormat(NodeRef listNodeRef, EntityFormat toFormat) {

		String fromFormat = getDatalistFormat(listNodeRef);

		if (!EntityFormat.JSON.toString().equals(fromFormat) && EntityFormat.JSON.equals(toFormat)) {

			String name = (String) nodeService.getProperty(listNodeRef, ContentModel.PROP_NAME);

			if (name.startsWith(RepoConsts.WUSED_PREFIX) || name.startsWith(RepoConsts.CUSTOM_VIEW_PREFIX)
					|| name.startsWith(RepoConsts.SMART_CONTENT_PREFIX) || name.contains("@")) {
				setDatalistFormat(listNodeRef, EntityFormat.NODE);
				return;
			}

			NodeRef entityNodeRef = nodeService.getPrimaryParent(nodeService.getPrimaryParent(listNodeRef).getParentRef()).getParentRef();

			QName dataListType = QName.createQName((String) nodeService.getProperty(listNodeRef, DataListModel.PROP_DATALISTITEMTYPE),
					namespaceService);

			String json = extractDatalistData(entityNodeRef, dataListType, RemoteEntityFormat.json);

			setDataListData(listNodeRef, json);

			List<NodeRef> listItems = entityListDAO.getListItems(listNodeRef, dataListType);

			for (NodeRef listItem : listItems) {
				nodeService.removeChild(listNodeRef, listItem);
			}

			setDatalistFormat(listNodeRef, toFormat);

		} else if (EntityFormat.JSON.toString().equals(fromFormat) && EntityFormat.NODE.equals(toFormat)) {
			// TODO
		}

	}

	@Override
	public void convert(NodeRef entityNodeRef, EntityFormat toFormat) {

		String fromFormat = getEntityFormat(entityNodeRef);

		if (!EntityFormat.JSON.toString().equals(fromFormat) && EntityFormat.JSON.equals(toFormat)) {

			String entityJson = null;

			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				remoteEntityService.getEntity(entityNodeRef, out, RemoteEntityFormat.json_all);
				entityJson = out.toString();
			} catch (IOException e) {
				logger.error("Failed to convert entity to JSON format", e);
			}

			setEntityData(entityNodeRef, entityJson);

			setEntityFormat(entityNodeRef, EntityFormat.JSON);
			
			List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(entityNodeRef);
			
			for (ChildAssociationRef childAssoc : childAssocs) {
				nodeService.removeChild(entityNodeRef, childAssoc.getChildRef());
			}
			
		} else if (EntityFormat.JSON.toString().equals(fromFormat) && EntityFormat.NODE.equals(toFormat)) {

			String entityJson = getEntityData(entityNodeRef);

			if (entityJson != null) {
				ByteArrayInputStream in = new ByteArrayInputStream(entityJson.getBytes());
				remoteEntityService.createOrUpdateEntity(entityNodeRef, in, RemoteEntityFormat.json, null);
			}
			
			setEntityFormat(entityNodeRef, EntityFormat.NODE);
		}
	}

	@Override
	public void setEntityData(NodeRef entityNodeRef, String data) {
		ContentWriter contentWriter = this.contentService.getWriter(entityNodeRef, BeCPGModel.PROP_ENTITY_DATA, true);
		contentWriter.setEncoding("UTF-8");
		contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
		contentWriter.putContent(data);
	}

	@Override
	public String getEntityData(NodeRef entityNodeRef) {
		if (nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_FORMAT)) {
			ContentReader reader = this.contentService.getReader(entityNodeRef, BeCPGModel.PROP_ENTITY_DATA);
			if (reader != null) {
				return reader.getContentString();
			}
		}

		return null;
	}

	@Override
	public void setEntityFormat(NodeRef entityNodeRef, EntityFormat format) {
		nodeService.setProperty(entityNodeRef, BeCPGModel.PROP_ENTITY_FORMAT, format);
	}

	@Override
	public String getEntityFormat(NodeRef entityNodeRef) {
		if(entityNodeRef!=null && nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_FORMAT)) {
			Serializable prop = nodeService.getProperty(entityNodeRef, BeCPGModel.PROP_ENTITY_FORMAT);
			return prop == null ? null : prop.toString();
		}
		return null;
	}

}
