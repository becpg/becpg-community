package fr.becpg.repo.quality.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;

import fr.becpg.model.QualityModel;
import fr.becpg.repo.BeCPGDao;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.quality.data.NonConformityData;
import fr.becpg.repo.quality.data.dataList.WorkLogDataItem;

public class NonConformityDAOImpl implements BeCPGDao<NonConformityData> {

	private NodeService nodeService;
	private FileFolderService fileFolderService;
	private EntityListDAO entityListDAO;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	@Override
	public NodeRef create(NodeRef parentNodeRef, NonConformityData ncData) {

		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();

		properties.put(ContentModel.PROP_NAME, ncData.getName());
		properties.put(QualityModel.PROP_NC_STATE, ncData.getState());
		properties.put(QualityModel.PROP_NC_COMMENT, ncData.getComment());

		NodeRef cpNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, ncData.getName()), QualityModel.TYPE_NC,
				properties).getChildRef();

		// work log
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(cpNodeRef);
		if (listContainerNodeRef == null) {
			listContainerNodeRef = entityListDAO.createListContainer(cpNodeRef);
		}
		createWorkLog(listContainerNodeRef, ncData.getWorkLog());

		return cpNodeRef;
	}

	@Override
	public void update(NodeRef ncNodeRef, NonConformityData ncData) {

		nodeService.setProperty(ncNodeRef, ContentModel.PROP_NAME, ncData.getName());
		nodeService.setProperty(ncNodeRef, QualityModel.PROP_NC_STATE, ncData.getState());
		nodeService.setProperty(ncNodeRef, QualityModel.PROP_NC_COMMENT, ncData.getComment());

		// work log
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(ncNodeRef);
		if (listContainerNodeRef == null) {
			listContainerNodeRef = entityListDAO.createListContainer(ncNodeRef);
		}
		createWorkLog(listContainerNodeRef, ncData.getWorkLog());
	}

	@Override
	public NonConformityData find(NodeRef ncNodeRef) {

		NonConformityData ncData = new NonConformityData();
		ncData.setName((String) nodeService.getProperty(ncNodeRef, ContentModel.PROP_NAME));
		ncData.setState((String) nodeService.getProperty(ncNodeRef, QualityModel.PROP_NC_STATE));
		ncData.setComment((String) nodeService.getProperty(ncNodeRef, QualityModel.PROP_NC_COMMENT));
		ncData.setNodeRef(ncNodeRef);

		NodeRef listContainerNodeRef = entityListDAO.getListContainer(ncNodeRef);
		if (listContainerNodeRef != null) {
			ncData.setWorkLog(loadWorkLog(listContainerNodeRef));
		}
		return ncData;
	}

	@Override
	public void delete(NodeRef wiaNodeRef) {

		nodeService.deleteNode(wiaNodeRef);

	}

	private List<WorkLogDataItem> loadWorkLog(NodeRef listContainerNodeRef) {

		List<WorkLogDataItem> workLog = null;

		if (listContainerNodeRef != null) {
			NodeRef workLogNodeRef = entityListDAO.getList(listContainerNodeRef, QualityModel.TYPE_WORK_LOG);

			if (workLogNodeRef != null) {
				workLog = new ArrayList<WorkLogDataItem>();
				List<FileInfo> nodes = fileFolderService.listFiles(workLogNodeRef);

				for (int z_idx = 0; z_idx < nodes.size(); z_idx++) {
					FileInfo node = nodes.get(z_idx);
					NodeRef nodeRef = node.getNodeRef();

					WorkLogDataItem workLogDataItem = new WorkLogDataItem(nodeRef, (String) nodeService.getProperty(
							nodeRef, QualityModel.PROP_WL_STATE), (String) nodeService.getProperty(nodeRef,
							QualityModel.PROP_WL_COMMENT), (String) nodeService.getProperty(nodeRef,
							ContentModel.PROP_CREATOR), (Date) nodeService.getProperty(nodeRef,
							ContentModel.PROP_CREATED));

					workLog.add(workLogDataItem);
				}
			}
		}

		return workLog;
	}

	private void createWorkLog(NodeRef listContainerNodeRef, List<WorkLogDataItem> workLog) {

		if (listContainerNodeRef != null) {
			NodeRef workLogNodeRef = entityListDAO.getList(listContainerNodeRef, QualityModel.TYPE_WORK_LOG);

			if (workLog == null) {
				// delete existing list
				if (workLogNodeRef != null)
					nodeService.deleteNode(workLogNodeRef);
			} else {
				// work log, create if needed
				if (workLogNodeRef == null) {
					workLogNodeRef = entityListDAO.createList(listContainerNodeRef, QualityModel.TYPE_WORK_LOG);
				}

				List<FileInfo> files = fileFolderService.listFiles(workLogNodeRef);

				// create temp list
				List<NodeRef> workLogToTreat = new ArrayList<NodeRef>();
				for (WorkLogDataItem workLogDataItem : workLog) {
					workLogToTreat.add(workLogDataItem.getNodeRef());
				}

				// remove deleted nodes
				Map<NodeRef, NodeRef> filesToUpdate = new HashMap<NodeRef, NodeRef>();
				for (FileInfo file : files) {

					if (!workLogToTreat.contains(file.getNodeRef())) {
						// delete
						nodeService.deleteNode(file.getNodeRef());
					} else {
						filesToUpdate.put(file.getNodeRef(), file.getNodeRef());
					}
				}

				// update or create nodes
				for (WorkLogDataItem workLogDataItem : workLog) {
					NodeRef controlNodeRef = workLogDataItem.getNodeRef();
					Map<QName, Serializable> properties = new HashMap<QName, Serializable>();

					properties.put(QualityModel.PROP_WL_STATE, workLogDataItem.getState());
					properties.put(QualityModel.PROP_WL_COMMENT, workLogDataItem.getComment());

					if (filesToUpdate.containsKey(controlNodeRef)) {
						// update
						nodeService.setProperties(filesToUpdate.get(controlNodeRef), properties);
					} else {
						// create
						ChildAssociationRef childAssocRef = nodeService.createNode(workLogNodeRef,
								ContentModel.ASSOC_CONTAINS,
								QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, GUID.generate()),
								QualityModel.TYPE_WORK_LOG, properties);
						controlNodeRef = childAssocRef.getChildRef();
					}
				}
			}
		}
	}

}
