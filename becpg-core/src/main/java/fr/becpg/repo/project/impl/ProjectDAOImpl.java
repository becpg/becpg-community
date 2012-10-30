package fr.becpg.repo.project.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.BeCPGListDao;
import fr.becpg.repo.data.DataItem;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.project.data.AbstractProjectData;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectTplData;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.TaskHistoryListDataItem;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;

/**
 * Class used to manage CRUD operation against projects
 * 
 * @author quere
 * 
 */
public class ProjectDAOImpl implements BeCPGListDao<AbstractProjectData> {

	private static Log logger = LogFactory.getLog(ProjectDAOImpl.class);

	private NodeService nodeService;
	private EntityListDAO entityListDAO;
	private AssociationService associationService;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	@Override
	public NodeRef create(NodeRef parentNodeRef, AbstractProjectData projectData, Collection<QName> dataLists) {

		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();

		properties.put(ContentModel.PROP_NAME, projectData.getName());

		QName projectType = ProjectModel.TYPE_PROJECT;

		if (projectData instanceof ProjectTplData) {
			projectType = ProjectModel.TYPE_PROJECT_TPL;
		}

		NodeRef projectNodeRef = nodeService.createNode(
				parentNodeRef,
				ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
						QName.createValidLocalName(projectData.getName())), projectType, properties).getChildRef();

		associationService.update(projectNodeRef, ProjectModel.ASSOC_PROJECT_TPL, projectData.getProjectTpl());
		createDataLists(projectNodeRef, projectData, dataLists);

		return projectNodeRef;
	}

	@Override
	public void update(NodeRef projectNodeRef, AbstractProjectData projectData, Collection<QName> dataLists) {

		nodeService.setProperty(projectNodeRef, ContentModel.PROP_NAME, projectData.getName());
		associationService.update(projectNodeRef, ProjectModel.ASSOC_PROJECT_TPL, projectData.getProjectTpl());
		createDataLists(projectNodeRef, projectData, dataLists);
	}

	@Override
	public AbstractProjectData find(NodeRef projectNodeRef, Collection<QName> dataLists) {

		AbstractProjectData projectData = null;
		QName projectType = nodeService.getType(projectNodeRef);
		String name = (String) nodeService.getProperty(projectNodeRef, ContentModel.PROP_NAME);

		if (projectType.isMatch(ProjectModel.TYPE_PROJECT)) {
			NodeRef projectTplNodeRef = associationService.getTargetAssoc(projectNodeRef,
					ProjectModel.ASSOC_PROJECT_TPL);
			projectData = new ProjectData(projectNodeRef, name, projectTplNodeRef);
		} else {
			projectData = new ProjectTplData(projectNodeRef, name);
		}

		// load datalists
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(projectNodeRef);

		if (dataLists != null) {
			for (QName dataList : dataLists) {

				if (dataList.equals(ProjectModel.TYPE_TASK_LIST)) {
					projectData.setTaskList(loadTaskList(listContainerNodeRef));
				} else if (dataList.equals(ProjectModel.TYPE_DELIVERABLE_LIST)) {
					projectData.setDeliverableList(loadDeliverableList(listContainerNodeRef));
				} else if (dataList.equals(ProjectModel.TYPE_TASK_HISTORY_LIST)) {
					projectData.setTaskHistoryList(loadTaskHistoryList(listContainerNodeRef));
				} else {
					logger.debug(String.format("DataList '%s' is not loaded since it is not implemented.", dataList));
				}
			}
		}

		return projectData;
	}

	@Override
	public void delete(NodeRef projectNodeRef) {

		nodeService.deleteNode(projectNodeRef);
	}

	private void createDataLists(NodeRef projectNodeRef, AbstractProjectData projectData, Collection<QName> dataLists) {
		// Container
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(projectNodeRef);
		if (listContainerNodeRef == null) {
			listContainerNodeRef = entityListDAO.createListContainer(projectNodeRef);
		}

		// Lists
		if (dataLists != null) {
			for (QName dataList : dataLists) {

				if (dataList.equals(ProjectModel.TYPE_TASK_LIST)) {
					createList(listContainerNodeRef, projectData.getTaskList(), dataList);
				} else if (dataList.equals(ProjectModel.TYPE_DELIVERABLE_LIST)) {
					createList(listContainerNodeRef, projectData.getDeliverableList(), dataList);
				} else if (dataList.equals(ProjectModel.TYPE_TASK_HISTORY_LIST)) {
					createList(listContainerNodeRef, projectData.getTaskHistoryList(), dataList);
				} else {
					logger.debug(String.format("DataList '%s' is not created since it is not implemented.", dataList));
				}
			}
		}
	}

	private List<TaskListDataItem> loadTaskList(NodeRef listContainerNodeRef) {
		List<TaskListDataItem> taskList = null;

		if (listContainerNodeRef != null) {
			NodeRef taskListNodeRef = entityListDAO.getList(listContainerNodeRef, ProjectModel.TYPE_TASK_LIST);

			if (taskListNodeRef != null) {
				taskList = new ArrayList<TaskListDataItem>();
				List<NodeRef> listItemNodeRefs = entityListDAO.getListItems(taskListNodeRef,
						ProjectModel.TYPE_TASK_LIST);

				for (NodeRef listItemNodeRef : listItemNodeRefs) {

					taskList.add(new TaskListDataItem(listItemNodeRef, (Boolean) nodeService.getProperty(
							listItemNodeRef, ProjectModel.PROP_TL_IS_MILESTONE), (Integer) nodeService.getProperty(
							listItemNodeRef, ProjectModel.PROP_TL_DURATION), (String) nodeService.getProperty(
							listItemNodeRef, ProjectModel.PROP_TL_WORKFLOW_NAME), associationService.getTargetAssoc(
							listItemNodeRef, ProjectModel.ASSOC_TL_TASKSET), associationService.getTargetAssoc(
							listItemNodeRef, ProjectModel.ASSOC_TL_TASK), associationService.getTargetAssocs(
							listItemNodeRef, ProjectModel.ASSOC_TL_PREV_TASKS), associationService.getTargetAssocs(
							listItemNodeRef, ProjectModel.ASSOC_TL_ASSIGNEES)));
				}
			}
		}

		return taskList;
	}

	private List<TaskHistoryListDataItem> loadTaskHistoryList(NodeRef listContainerNodeRef) {
		List<TaskHistoryListDataItem> taskHistoryList = null;

		if (listContainerNodeRef != null) {
			NodeRef taskListNodeRef = entityListDAO.getList(listContainerNodeRef, ProjectModel.TYPE_TASK_HISTORY_LIST);

			if (taskListNodeRef != null) {
				taskHistoryList = new ArrayList<TaskHistoryListDataItem>();
				List<NodeRef> listItemNodeRefs = entityListDAO.getListItems(taskListNodeRef,
						ProjectModel.TYPE_TASK_HISTORY_LIST);

				for (NodeRef listItemNodeRef : listItemNodeRefs) {

					String s = (String) nodeService.getProperty(listItemNodeRef, ProjectModel.PROP_THL_STATE);
					TaskState taskState = s != null ? TaskState.valueOf(s) : TaskState.InProgress;

					taskHistoryList.add(new TaskHistoryListDataItem(listItemNodeRef, (Date) nodeService.getProperty(
							listItemNodeRef, ProjectModel.PROP_THL_START), (Date) nodeService.getProperty(
							listItemNodeRef, ProjectModel.PROP_THL_END), (Integer) nodeService.getProperty(
							listItemNodeRef, ProjectModel.PROP_THL_DURATION), (String) nodeService.getProperty(
							listItemNodeRef, ProjectModel.PROP_THL_COMMENT), taskState, associationService
							.getTargetAssoc(listItemNodeRef, ProjectModel.ASSOC_THL_TASKSET), associationService
							.getTargetAssoc(listItemNodeRef, ProjectModel.ASSOC_THL_TASK), associationService
							.getTargetAssocs(listItemNodeRef, WorkflowModel.ASSOC_ASSIGNEES)));
				}
			}
		}

		return taskHistoryList;
	}

	private List<DeliverableListDataItem> loadDeliverableList(NodeRef listContainerNodeRef) {
		List<DeliverableListDataItem> deliverableList = null;

		if (listContainerNodeRef != null) {
			NodeRef taskListNodeRef = entityListDAO.getList(listContainerNodeRef, ProjectModel.TYPE_DELIVERABLE_LIST);

			if (taskListNodeRef != null) {
				deliverableList = new ArrayList<DeliverableListDataItem>();
				List<NodeRef> listItemNodeRefs = entityListDAO.getListItems(taskListNodeRef,
						ProjectModel.TYPE_DELIVERABLE_LIST);

				for (NodeRef listItemNodeRef : listItemNodeRefs) {

					String s = (String) nodeService.getProperty(listItemNodeRef, ProjectModel.PROP_DL_STATE);
					TaskState taskState = s != null ? TaskState.valueOf(s) : TaskState.InProgress;

					deliverableList.add(new DeliverableListDataItem(listItemNodeRef, associationService.getTargetAssoc(
							listItemNodeRef, ProjectModel.ASSOC_DL_TASK), taskState, (String) nodeService.getProperty(
							listItemNodeRef, ProjectModel.PROP_DL_DESCRIPTION)));
				}
			}
		}

		return deliverableList;
	}

	// private List<TaskListDataItem> loadList(NodeRef listContainerNodeRef,
	// QName dataListType) {
	// List<DataItem> dataList = null;
	//
	// if (listContainerNodeRef != null) {
	// NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef,
	// dataListType);
	//
	// if (listNodeRef != null) {
	// dataList = new ArrayList<DataItem>();
	// List<NodeRef> listItemNodeRefs = entityListDAO.getListItems(listNodeRef,
	// dataListType);
	//
	// for (NodeRef listItemNodeRef : listItemNodeRefs) {
	//
	// taskList.add(new TaskListDataItem(listItemNodeRef,
	// (Boolean)nodeService.getProperty(listItemNodeRef,
	// ProjectModel.PROP_TL_IS_MILESTONE),
	// (Integer)nodeService.getProperty(listItemNodeRef,
	// ProjectModel.PROP_TL_DURATION),
	// (String)nodeService.getProperty(listItemNodeRef,
	// ProjectModel.PROP_TL_WORKFLOW_NAME),
	// associationService.getTargetAssoc(listItemNodeRef,
	// ProjectModel.ASSOC_TL_TASKSET),
	// associationService.getTargetAssoc(listItemNodeRef,
	// ProjectModel.ASSOC_TL_TASK),
	// associationService.getTargetAssocs(listItemNodeRef,
	// ProjectModel.ASSOC_TL_PREV_TASKS)));
	// }
	// }
	// }
	//
	// return taskList;
	// }

	private void createList(NodeRef listContainerNodeRef, List<? extends DataItem> dataList, QName dataListType) {

		if (listContainerNodeRef != null) {
			NodeRef dataListNodeRef = entityListDAO.getList(listContainerNodeRef, dataListType);

			if (dataList == null) {
				// delete existing list
				if (dataListNodeRef != null)
					nodeService.deleteNode(dataListNodeRef);
			} else {
				// price list, create if needed
				if (dataListNodeRef == null) {
					dataListNodeRef = entityListDAO.createList(listContainerNodeRef, dataListType);
				}

				List<NodeRef> listItemNodeRefs = entityListDAO.getListItems(dataListNodeRef, dataListType);

				// create temp list
				List<NodeRef> listToTreat = new ArrayList<NodeRef>();
				for (DataItem dataListItem : dataList) {
					listToTreat.add(dataListItem.getNodeRef());
				}

				// remove deleted nodes
				List<NodeRef> filesToUpdate = new ArrayList<NodeRef>();
				for (NodeRef listItemNodeRef : listItemNodeRefs) {

					if (!listToTreat.contains(listItemNodeRef)) {
						// delete
						nodeService.deleteNode(listItemNodeRef);
					} else {
						filesToUpdate.add(listItemNodeRef);
					}
				}

				// update or create nodes
				for (DataItem dataListItem : dataList) {

					Map<QName, Serializable> properties = dataListItem.getProperties();
					Map<QName, NodeRef> singleAssociations = dataListItem.getSingleAssociations();
					Map<QName, List<NodeRef>> multipleAssociations = dataListItem.getMultipleAssociations();

					if (filesToUpdate.contains(dataListItem.getNodeRef())) {
						// update
						nodeService.addProperties(dataListItem.getNodeRef(), properties);
					} else {
						// create
						ChildAssociationRef childAssocRef = nodeService.createNode(dataListNodeRef,
								ContentModel.ASSOC_CONTAINS,
								QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, GUID.generate()),
								dataListType, properties);
						dataListItem.setNodeRef(childAssocRef.getChildRef());
					}

					for (Map.Entry<QName, NodeRef> association : singleAssociations.entrySet()) {
						// logger.debug("update assoc: " + association.getKey()
						// + " - " + association.getValue());
						associationService.update(dataListItem.getNodeRef(), association.getKey(),
								association.getValue());
					}
					for (Map.Entry<QName, List<NodeRef>> association : multipleAssociations.entrySet()) {
						// logger.debug("update assoc: " + association.getKey()
						// + " - " + association.getValue());
						associationService.update(dataListItem.getNodeRef(), association.getKey(),
								association.getValue());
					}
				}
			}
		}
	}
}
