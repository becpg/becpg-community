/*
 * 
 */
package fr.becpg.repo.project.policy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.entity.datalist.WUsedListService;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.DeliverableState;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.project.impl.ProjectHelper;
import fr.becpg.repo.repository.AlfrescoRepository;

/**
 * The Class SubmitTaskPolicy.
 * 
 * @author querephi
 */
@Service
public class ProjectListPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnUpdatePropertiesPolicy,
	NodeServicePolicies.OnCreateAssociationPolicy,
	NodeServicePolicies.OnDeleteAssociationPolicy{

	/** The logger. */
	private static Log logger = LogFactory.getLog(ProjectListPolicy.class);

	private ProjectService projectService;

	private WUsedListService wUsedListService;
	
	private PermissionService permissionService;
	
	private AlfrescoRepository<ProjectData> alfrescoRepository;
	
	public void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}

	public void setwUsedListService(WUsedListService wUsedListService) {
		this.wUsedListService = wUsedListService;
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<ProjectData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/**
	 * Inits the.
	 */
	public void doInit() {
		logger.debug("Init SubmitTaskPolicy...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
				ProjectModel.TYPE_TASK_LIST, new JavaBehaviour(this, "onUpdateProperties"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
				ProjectModel.TYPE_DELIVERABLE_LIST, new JavaBehaviour(this, "onUpdateProperties"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME,
				ProjectModel.TYPE_TASK_LIST, ProjectModel.ASSOC_TL_RESOURCES, new JavaBehaviour(this,
						"onCreateAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME,
				ProjectModel.TYPE_TASK_LIST, ProjectModel.ASSOC_TL_RESOURCES, new JavaBehaviour(this,
						"onDeleteAssociation"));
	}

	@Override
	protected void doBeforeCommit(String key, Set<NodeRef> pendingNodes) {

		for (NodeRef projectNodeRef : pendingNodes) {
			if(nodeService.exists(projectNodeRef)){
				try {
					policyBehaviourFilter.disableBehaviour(ProjectModel.TYPE_TASK_LIST);
					policyBehaviourFilter.disableBehaviour(ProjectModel.TYPE_DELIVERABLE_LIST);
					projectService.formulate(projectNodeRef);
				} catch (FormulateException e) {
					logger.error(e,e);
				} finally {
					policyBehaviourFilter.enableBehaviour(ProjectModel.TYPE_TASK_LIST);
					policyBehaviourFilter.enableBehaviour(ProjectModel.TYPE_DELIVERABLE_LIST);
				}
			}			
		}
	}

	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

		if (nodeService.getType(nodeRef).equals(ProjectModel.TYPE_TASK_LIST)) {
			onUpdatePropertiesTaskList(nodeRef, before, after);
		} else if (nodeService.getType(nodeRef).equals(ProjectModel.TYPE_DELIVERABLE_LIST)) {
			onUpdatePropertiesDeliverableList(nodeRef, before, after);
		}
	}

	public void onUpdatePropertiesTaskList(NodeRef nodeRef, Map<QName, Serializable> before,
			Map<QName, Serializable> after) {
		
		boolean formulateProject = false;
		String beforeState = (String) before.get(ProjectModel.PROP_TL_STATE);
		String afterState = (String) after.get(ProjectModel.PROP_TL_STATE);

		if (beforeState != null && afterState != null) {
			if (beforeState.equals(TaskState.InProgress.toString())
					&& afterState.equals(TaskState.Completed.toString())) {
				logger.debug("update task list: " + nodeRef + " - afterState: " + afterState);
				Date startDate = (Date)nodeService.getProperty(nodeRef, ProjectModel.PROP_TL_START);
				Date endDate = ProjectHelper.removeTime(new Date());
				Integer duration = ProjectHelper.calculateTaskDuration(startDate, endDate);
				nodeService.setProperty(nodeRef, ProjectModel.PROP_TL_END, endDate);
				nodeService.setProperty(nodeRef, ProjectModel.PROP_TL_DURATION, duration);
				formulateProject = true;
			}
			
			//start project by task list if project has state InProgress -> InProgress
			if(!afterState.equals(beforeState) && afterState.equals(TaskState.InProgress)){
				NodeRef projectNodeRef = wUsedListService.getRoot(nodeRef);
				ProjectData projectData = (ProjectData)alfrescoRepository.findOne(projectNodeRef);
				if(ProjectState.Planned.equals(projectData.getProjectState())){
					projectData.setProjectState(ProjectState.InProgress);
					formulateProject = true;
				}
			}
		}
		
		if (isPropChanged(nodeRef, before, after, ProjectModel.PROP_TL_DURATION)
				|| isPropChanged(nodeRef, before, after, ProjectModel.PROP_TL_START)
				|| isPropChanged(nodeRef, before, after, ProjectModel.PROP_TL_END)){
			
			logger.debug("update task list start, duration or end: " + nodeRef);
			formulateProject = true;
		}
				
		if(formulateProject){
			NodeRef projectNodeRef = wUsedListService.getRoot(nodeRef);
			queueNode(projectNodeRef);
		}
	}

	private boolean isPropChanged(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after,
			QName propertyQName) {
		Serializable beforeProp = before.get(propertyQName);
		Serializable afterProp = after.get(propertyQName);

		if (afterProp != null && !afterProp.equals(beforeProp)) {
			return true;
		}
		return false;
	}

	public void onUpdatePropertiesDeliverableList(NodeRef nodeRef, Map<QName, Serializable> before,
			Map<QName, Serializable> after) {

		String beforeState = (String) before.get(ProjectModel.PROP_DL_STATE);
		String afterState = (String) after.get(ProjectModel.PROP_DL_STATE);

		if (beforeState != null && afterState != null) {
			if (beforeState.equals(DeliverableState.InProgress.toString())
					&& afterState.equals(DeliverableState.Completed.toString())) {
				logger.debug("submit deliverable: " + nodeRef);
				NodeRef projectNodeRef = wUsedListService.getRoot(nodeRef);
				queueNode(projectNodeRef);
			} else if (beforeState.equals(DeliverableState.Completed.toString())
					&& afterState.equals(DeliverableState.InProgress.toString())) {

				// re-open deliverable
				logger.debug("re-open deliverable: " + nodeRef);
				projectService.openDeliverable(nodeRef);

				NodeRef projectNodeRef = wUsedListService.getRoot(nodeRef);
				queueNode(projectNodeRef);
			}
		}
	}

	@Override
	public void onDeleteAssociation(AssociationRef assocRef) {
		setPermission(assocRef, false);
	}

	@Override
	public void onCreateAssociation(AssociationRef assocRef) {
		setPermission(assocRef, true);						
	}
	
	private void setPermission(AssociationRef assocRef, boolean allow){
		
		NodeRef taskListNodeRef = assocRef.getSourceRef();
		NodeRef resourceNodeRef = assocRef.getTargetRef();	
		
		List<NodeRef> nodeRefs = new ArrayList<NodeRef>(1);
		nodeRefs.add(taskListNodeRef);
		
		NodeRef projectNodeRef = wUsedListService.getRoot(taskListNodeRef);
		
		if(ProjectModel.TYPE_PROJECT.equals(nodeService.getType(projectNodeRef))){
			String userName = (String)nodeService.getProperty(resourceNodeRef, ContentModel.PROP_USERNAME);
					
			ProjectData projectData = alfrescoRepository.findOne(projectNodeRef);
			List<DeliverableListDataItem> deliverableList = ProjectHelper.getDeliverables(projectData, taskListNodeRef);
			for(DeliverableListDataItem dl : deliverableList){
				nodeRefs.add(dl.getNodeRef());				
			}
			
			for(NodeRef n : nodeRefs){
				if(allow){
					permissionService.setPermission(n, userName, PermissionService.EDITOR, allow);
				}
				else{					
					//permissionService.deletePermission(n, userName, PermissionService.EDITOR);
					permissionService.clearPermission(n, userName);
				}
				
			}
		}		
	}
}
