/*
 * 
 */
package fr.becpg.repo.project.policy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.project.ProjectActivityService;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.impl.ProjectHelper;
import fr.becpg.repo.repository.AlfrescoRepository;

/**
 * The Class ProjectPolicy.
 * 
 * @author querephi
 */
public class ProjectPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnCreateAssociationPolicy,
		NodeServicePolicies.OnUpdatePropertiesPolicy,
		CopyServicePolicies.OnCopyNodePolicy{

	private static String KEY_INIT_DL_CONTENT = "ProjectPolicy.InitDLContent";
	
	private static Log logger = LogFactory.getLog(ProjectPolicy.class);

	private EntityListDAO entityListDAO;
	private ProjectService projectService;
	private AssociationService associationService;
	private ProjectActivityService projectActivityService;
	private AlfrescoRepository<ProjectData> alfrescoRepository;

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}
	
	public void setProjectActivityService(ProjectActivityService projectActivityService) {
		this.projectActivityService = projectActivityService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<ProjectData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/**
	 * Inits the.
	 */
	public void doInit() {
		logger.debug("Init ProjectPolicy...");
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME,
				ProjectModel.TYPE_PROJECT, BeCPGModel.ASSOC_ENTITY_TPL_REF, new JavaBehaviour(this,
						"onCreateAssociation"));


		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
				ProjectModel.TYPE_PROJECT, new JavaBehaviour(this, "onUpdateProperties"));		
		
		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyNodePolicy.QNAME, 
				ProjectModel.TYPE_PROJECT, new JavaBehaviour(this, "getCopyCallback"));
		
		// disable otherwise, impossible to copy project that has a template
		super.disableOnCopyBehaviour(ProjectModel.TYPE_PROJECT);		
	}

	@Override
	public void onCreateAssociation(AssociationRef assocRef) {
		
		NodeRef projectNodeRef = assocRef.getSourceRef();
		
		if (assocRef.getTypeQName().equals(BeCPGModel.ASSOC_ENTITY_TPL_REF)) {
			
			NodeRef projectTplNodeRef = assocRef.getTargetRef();
			
			// copy folders
			// already done by entity policy
			
			// copy datalist from Tpl to project			
			Collection<QName> dataLists = new ArrayList<QName>();
			dataLists.add(ProjectModel.TYPE_TASK_LIST);
			dataLists.add(ProjectModel.TYPE_DELIVERABLE_LIST);			
			entityListDAO.copyDataLists(projectTplNodeRef, projectNodeRef, dataLists, false);
			
			// we wait files are copied by entity policy
			queueNode(KEY_INIT_DL_CONTENT, assocRef.getSourceRef());
			
			// initialize
			queueNode(projectNodeRef);
							
		} 
	}	
	
	// TODO : do it in a generic way
	public void initializeNodeRefsAfterCopy(NodeRef projectNodeRef){
					
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(projectNodeRef);
		if (listContainerNodeRef != null) {			
			
			//Deliverables
			NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, ProjectModel.TYPE_DELIVERABLE_LIST);
			if (listNodeRef != null) {
				List<NodeRef> listItems = entityListDAO.getListItems(listNodeRef, ProjectModel.TYPE_DELIVERABLE_LIST);
				for (NodeRef listItem : listItems) {
					updateDelieverableDocument(projectNodeRef, listItem);
				}
			}
		}
	}
	
	private void updateDelieverableDocument(NodeRef projectNodeRef, NodeRef listItem){
		
		Stack<String> stack = new Stack<String>();
		NodeRef documentNodeRef = associationService.getTargetAssoc(listItem, ProjectModel.ASSOC_DL_CONTENT);
		
		if(documentNodeRef != null){
			NodeRef folderNodeRef = nodeService.getPrimaryParent(documentNodeRef).getParentRef();
			
			while(folderNodeRef!=null && !nodeService.hasAspect(folderNodeRef, BeCPGModel.ASPECT_ENTITYLISTS)){
				String name = (String)nodeService.getProperty(folderNodeRef, ContentModel.PROP_NAME);
				logger.debug("folderNodeRef: " + folderNodeRef + " name: " + name);
				stack.push(name);
				folderNodeRef = nodeService.getPrimaryParent(folderNodeRef).getParentRef();
			}
			
			logger.debug("stack: " + stack);
			
			folderNodeRef = projectNodeRef;
			Iterator<String>iterator = stack.iterator();
			while(iterator.hasNext() && folderNodeRef !=null){
				folderNodeRef = nodeService.getChildByName(folderNodeRef, ContentModel.ASSOC_CONTAINS, iterator.next());				
			}
			 
			if(folderNodeRef != null){
				NodeRef newDocumentNodeRef = nodeService.getChildByName(folderNodeRef, ContentModel.ASSOC_CONTAINS, 
						(String)nodeService.getProperty(documentNodeRef, ContentModel.PROP_NAME));
				logger.debug("Update dlContent with doc " + newDocumentNodeRef);
				associationService.update(listItem, ProjectModel.ASSOC_DL_CONTENT, newDocumentNodeRef);
			}
			
		}
	}

	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

		boolean formulateProject = false;
		String beforeState = (String) before.get(ProjectModel.PROP_PROJECT_STATE);
		String afterState = (String) after.get(ProjectModel.PROP_PROJECT_STATE);		

		// change state
		if (afterState != null && !afterState.equals(beforeState)) {
			
			projectActivityService.postProjectStateChangeActivity(nodeRef, beforeState, afterState);
			
			if (afterState.equals(ProjectState.InProgress.toString())) {
				logger.debug("onUpdateProperties:start project");
				Date startDate = ProjectHelper.removeTime(new Date());
				nodeService.setProperty(nodeRef, ProjectModel.PROP_PROJECT_START_DATE, startDate);
				ProjectData projectData = alfrescoRepository.findOne(nodeRef);
				for(TaskListDataItem taskListDataItem : ProjectHelper.getNextTasks(projectData, null)){
					nodeService.setProperty(taskListDataItem.getNodeRef(), ProjectModel.PROP_TL_START, startDate);
				}
				formulateProject = true;
			} else if (afterState.equals(ProjectState.Cancelled.toString())) {
				logger.debug("onUpdateProperties:cancel project");
				projectService.cancel(nodeRef);
			}
		}
		
		// change startdate, duedate
		if(isPropChanged(before, after, ProjectModel.PROP_PROJECT_START_DATE) ||
				isPropChanged(before, after, ProjectModel.PROP_PROJECT_DUE_DATE)){
			formulateProject = true;
		}
				
		if(formulateProject){
			queueNode(nodeRef);
		}
	}

	@Override
	protected void doBeforeCommit(String key, Set<NodeRef> pendingNodes) {

		if(KEY_INIT_DL_CONTENT.equals(key)){
			for (NodeRef nodeRef : pendingNodes) {
				initializeNodeRefsAfterCopy(nodeRef);	
			}
		}
		else{
			for (NodeRef nodeRef : pendingNodes) {
				try {
					if(nodeService.exists(nodeRef) && isNotLocked(nodeRef)){
						logger.debug("Project policy formulate");
						projectService.formulate(nodeRef);
					}				
				} catch (FormulateException e) {
					logger.error(e,e);
				}
			}
		}
		
	}
	
	@Override
	public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails) {
		super.getCopyCallback(classRef, copyDetails);
		return new ProjectCopyBehaviourCallback();
	}
		
	private class ProjectCopyBehaviourCallback extends DefaultCopyBehaviourCallback {
		
        private ProjectCopyBehaviourCallback(){        
        }
        
		@Override
		public boolean getMustCopy(QName classQName, CopyDetails copyDetails) {				
			return true;
		}

		@Override
		public Map<QName, Serializable> getCopyProperties(QName classQName, CopyDetails copyDetails,
				Map<QName, Serializable> properties) {		
			
			if(ProjectModel.TYPE_PROJECT.equals(classQName)){
				if(properties.containsKey(ProjectModel.PROP_PROJECT_STATE)){
					properties.put(ProjectModel.PROP_PROJECT_STATE, ProjectState.Planned);
				}
				if(properties.containsKey(ProjectModel.PROP_PROJECT_START_DATE)){					
					properties.remove(ProjectModel.PROP_PROJECT_START_DATE);					
				}
				if(properties.containsKey(ProjectModel.PROP_PROJECT_DUE_DATE)){					
					properties.remove(ProjectModel.PROP_PROJECT_DUE_DATE);					
				}
				if(properties.containsKey(ProjectModel.PROP_PROJECT_COMPLETION_DATE)){					
					properties.remove(ProjectModel.PROP_PROJECT_COMPLETION_DATE);					
				}
			}
			
			return properties;
		}
	}

}
