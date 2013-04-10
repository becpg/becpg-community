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
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
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
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.project.ProjectActivityService;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.repo.project.data.ProjectData;
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
	NodeServicePolicies.OnDeleteAssociationPolicy,
	CopyServicePolicies.OnCopyNodePolicy{

	/** The logger. */
	private static Log logger = LogFactory.getLog(ProjectListPolicy.class);

	private ProjectService projectService;

	private PermissionService permissionService;
	
	private AlfrescoRepository<ProjectData> alfrescoRepository;
	
	private EntityListDAO entityListDAO;
	
	private ProjectActivityService projectActivityService;
	
	public void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<ProjectData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}
	
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setProjectActivityService(ProjectActivityService projectActivityService) {
		this.projectActivityService = projectActivityService;
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
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
				ProjectModel.TYPE_SCORE_LIST, new JavaBehaviour(this, "onUpdateProperties"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME,
				ProjectModel.TYPE_TASK_LIST, ProjectModel.ASSOC_TL_RESOURCES, new JavaBehaviour(this,
						"onCreateAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME,
				ProjectModel.TYPE_TASK_LIST, ProjectModel.ASSOC_TL_RESOURCES, new JavaBehaviour(this,
						"onDeleteAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME,
				ProjectModel.TYPE_TASK_LIST, ProjectModel.ASSOC_TL_PREV_TASKS, new JavaBehaviour(this,
						"onCreateAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME,
				ProjectModel.TYPE_TASK_LIST, ProjectModel.ASSOC_TL_PREV_TASKS, new JavaBehaviour(this,
						"onDeleteAssociation"));
		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyNodePolicy.QNAME, 
				ProjectModel.TYPE_DELIVERABLE_LIST, new JavaBehaviour(this, "getCopyCallback"));		
		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyNodePolicy.QNAME, 
				ProjectModel.TYPE_TASK_LIST, new JavaBehaviour(this, "getCopyCallback"));
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
		} else if (nodeService.getType(nodeRef).equals(ProjectModel.TYPE_SCORE_LIST)) {
				onUpdatePropertiesScoreList(nodeRef, before, after);
			}
	}

	public void onUpdatePropertiesTaskList(NodeRef nodeRef, Map<QName, Serializable> before,
			Map<QName, Serializable> after) {
		
		boolean formulateProject = false;
		String beforeState = (String) before.get(ProjectModel.PROP_TL_STATE);
		String afterState = (String) after.get(ProjectModel.PROP_TL_STATE);

		if (beforeState != null && afterState != null && !beforeState.equals(afterState)) {
			
			projectActivityService.postTaskStateChangeActivity(nodeRef, beforeState, afterState);
			formulateProject = true;
			
			if (afterState.equals(TaskState.Completed.toString())) {
				logger.debug("update task list: " + nodeRef + " - afterState: " + afterState);
				// we want to keep the planned duration to calculate overdue				
				nodeService.setProperty(nodeRef, ProjectModel.PROP_TL_END, ProjectHelper.removeTime(new Date()));
				//milestone duration is maximum 1 day
				Boolean isMileStone = (Boolean)nodeService.getProperty(nodeRef, ProjectModel.PROP_TL_IS_MILESTONE);
				if(isMileStone != null && isMileStone.booleanValue()){
					nodeService.setProperty(nodeRef, ProjectModel.PROP_TL_START, ProjectHelper.removeTime(new Date()));
				}
			} 
			else if (beforeState.equals(DeliverableState.Completed.toString())
					&& afterState.equals(DeliverableState.InProgress.toString())) {

				// re-open task
				logger.debug("re-open task: " + nodeRef);				
				projectService.openTask(nodeRef);
			}
		}
		
		if (isPropChanged(before, after, ProjectModel.PROP_TL_DURATION)
				|| isPropChanged(before, after, ProjectModel.PROP_TL_START)
				|| isPropChanged(before, after, ProjectModel.PROP_TL_END)){
			
			logger.debug("update task list start, duration or end: " + nodeRef);
			formulateProject = true;
		}
				
		if(formulateProject){
			queueListItem(nodeRef);
		}
	}

	public void onUpdatePropertiesDeliverableList(NodeRef nodeRef, Map<QName, Serializable> before,
			Map<QName, Serializable> after) {

		String beforeState = (String) before.get(ProjectModel.PROP_DL_STATE);
		String afterState = (String) after.get(ProjectModel.PROP_DL_STATE);

		if (beforeState != null && afterState != null && !afterState.equals(beforeState)) {
			
			projectActivityService.postDeliverableStateChangeActivity(nodeRef, beforeState, afterState);
			
			if (beforeState.equals(DeliverableState.InProgress.toString())
					&& afterState.equals(DeliverableState.Completed.toString())) {
				logger.debug("submit deliverable: " + nodeRef);
				queueListItem(nodeRef);
			} else if (beforeState.equals(DeliverableState.Completed.toString())
					&& afterState.equals(DeliverableState.InProgress.toString())) {

				// re-open deliverable and disable policy to avoid every dl are re-opened
				logger.debug("re-open deliverable: " + nodeRef);
				try{
					policyBehaviourFilter.disableBehaviour(ProjectModel.TYPE_TASK_LIST);
					projectService.openDeliverable(nodeRef);
				}
				finally{
					policyBehaviourFilter.enableBehaviour(ProjectModel.TYPE_TASK_LIST);
				}
				
				queueListItem(nodeRef);
			}
		}
	}
	
	public void onUpdatePropertiesScoreList(NodeRef nodeRef, Map<QName, Serializable> before,
			Map<QName, Serializable> after) {

		if (isPropChanged(before, after, ProjectModel.PROP_SL_SCORE)
				|| isPropChanged(before, after, ProjectModel.PROP_SL_WEIGHT)){
			
			logger.debug("update score list : " + nodeRef);
			queueListItem(nodeRef);
		}
	}
	
	private void queueListItem(NodeRef listItemNodeRef){
		NodeRef projectNodeRef =  entityListDAO.getEntity(listItemNodeRef);
		queueNode(projectNodeRef);
	}

	@Override
	public void onDeleteAssociation(AssociationRef assocRef) {
		setPermission(assocRef, false);
	}

	@Override
	public void onCreateAssociation(AssociationRef assocRef) {
		if(assocRef.getTypeQName().equals(ProjectModel.ASSOC_TL_RESOURCES)){
			setPermission(assocRef, true);
		}
		else if(assocRef.getTypeQName().equals(ProjectModel.ASSOC_TL_PREV_TASKS)){
			queueListItem(assocRef.getSourceRef());
		}								
	}
	
	private void setPermission(AssociationRef assocRef, boolean allow){
		
		NodeRef taskListNodeRef = assocRef.getSourceRef();
		NodeRef resourceNodeRef = assocRef.getTargetRef();	
		
		List<NodeRef> nodeRefs = new ArrayList<NodeRef>(1);
		nodeRefs.add(taskListNodeRef);
		
		NodeRef projectNodeRef =  entityListDAO.getEntity(taskListNodeRef);
		
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
	
	@Override
	public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails) {
		return new ProjectListCopyBehaviourCallback();
	}
	
	private class ProjectListCopyBehaviourCallback extends DefaultCopyBehaviourCallback {
		
        private ProjectListCopyBehaviourCallback(){        
        }
        
		@Override
		public boolean getMustCopy(QName classQName, CopyDetails copyDetails) {				
			return true;
		}

		@Override
		public Map<QName, Serializable> getCopyProperties(QName classQName, CopyDetails copyDetails,
				Map<QName, Serializable> properties) {		
			
			if(ProjectModel.TYPE_TASK_LIST.equals(classQName)){
				properties.remove(ProjectModel.PROP_TL_WORKFLOW_INSTANCE);
				properties.remove(ProjectModel.PROP_COMPLETION_PERCENT);
				if(properties.containsKey(ProjectModel.PROP_TL_STATE)){
					properties.put(ProjectModel.PROP_TL_STATE, TaskState.Planned);
				}
			}
			else if(ProjectModel.TYPE_DELIVERABLE_LIST.equals(classQName)){
				if(properties.containsKey(ProjectModel.PROP_DL_STATE)){
					properties.put(ProjectModel.PROP_DL_STATE, DeliverableState.Planned);
				}
			}
			
			return properties;
		}
	}
}
