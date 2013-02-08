/*
 * 
 */
package fr.becpg.repo.project.policy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.project.impl.ProjectHelper;

/**
 * The Class ProjectPolicy.
 * 
 * @author querephi
 */
@Service
//Fusionner avec EntityPolicy
@Deprecated 
public class ProjectPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnCreateAssociationPolicy,
		NodeServicePolicies.OnUpdatePropertiesPolicy,
		NodeServicePolicies.OnDeleteNodePolicy{

	/** The logger. */
	private static Log logger = LogFactory.getLog(ProjectPolicy.class);

	private EntityListDAO entityListDAO;
	private ProjectService projectService;
	private AssociationService associationService;
	private CopyService copyService;
	private EntityService entityService;

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public void setCopyService(CopyService copyService) {
		this.copyService = copyService;
	}

	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

	/**
	 * Inits the.
	 */
	public void doInit() {
		logger.debug("Init ProjectPolicy...");
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME,
				ProjectModel.TYPE_PROJECT, BeCPGModel.ASSOC_ENTITY_TPL_REF, new JavaBehaviour(this,
						"onCreateAssociation"));

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME,
				ProjectModel.TYPE_PROJECT, ProjectModel.ASSOC_PROJECT_ENTITY, new JavaBehaviour(this,
						"onCreateAssociation"));

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
				ProjectModel.TYPE_PROJECT, new JavaBehaviour(this, "onUpdateProperties"));
		
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnDeleteNodePolicy.QNAME,
				ProjectModel.TYPE_PROJECT, new JavaBehaviour(this, "onDeleteNode"));
		
		// disable otherwise, impossible to copy project that has a template
		super.disableOnCopyBehaviour(ProjectModel.TYPE_PROJECT);		
	}

	@Override
	public void onCreateAssociation(AssociationRef assocRef) {
		
		NodeRef projectNodeRef = assocRef.getSourceRef();
		
		if (assocRef.getTypeQName().equals(BeCPGModel.ASSOC_ENTITY_TPL_REF)) {
			
			NodeRef projectTplNodeRef = assocRef.getTargetRef();
			
			// copy folders
			entityService.copyEntityFolders(projectTplNodeRef, projectNodeRef);
			
			// copy datalist from Tpl to project
			logger.debug("copy datalists");
			Collection<QName> dataLists = new ArrayList<QName>();
			dataLists.add(ProjectModel.TYPE_TASK_LIST);
			dataLists.add(ProjectModel.TYPE_DELIVERABLE_LIST);			
			entityListDAO.copyDataLists(projectTplNodeRef, projectNodeRef, dataLists, false);

			initializeNodeRefsAfterCopy(projectNodeRef);

			// initialize
			queueNode(projectNodeRef);

		} else if (assocRef.getTypeQName().equals(ProjectModel.ASSOC_PROJECT_ENTITY)) {
			
			NodeRef entityNodeRef = assocRef.getTargetRef();
			
			// add project aspect on entity
			nodeService.addAspect(entityNodeRef, ProjectModel.ASPECT_PROJECT_ASPECT, null);
			associationService.update(entityNodeRef, ProjectModel.ASSOC_PROJECT, projectNodeRef);
		}
	}
	
	// TODO : do it in a generic way
	private void initializeNodeRefsAfterCopy(NodeRef projectNodeRef){
					
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(projectNodeRef);
		if (listContainerNodeRef != null) {
			
			// refresh reference to prevTasks
			NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, ProjectModel.TYPE_TASK_LIST);
			if (listNodeRef != null) {
				List<NodeRef> listItems = entityListDAO.getListItems(listNodeRef, ProjectModel.TYPE_TASK_LIST);
				Map<NodeRef, NodeRef> originalMaps = new HashMap<NodeRef, NodeRef>(listItems.size());
				for (NodeRef listItem : listItems) {
					originalMaps.put(copyService.getOriginal(listItem), listItem);
				}

				listNodeRef = entityListDAO.getList(listContainerNodeRef, ProjectModel.TYPE_DELIVERABLE_LIST);
				listItems = entityListDAO.getListItems(listNodeRef, ProjectModel.TYPE_DELIVERABLE_LIST);
				updateOriginalNodes(originalMaps, listItems, ProjectModel.ASSOC_DL_TASK);

			}
			
			//Deliverables
			listNodeRef = entityListDAO.getList(listContainerNodeRef, ProjectModel.TYPE_DELIVERABLE_LIST);
			if (listNodeRef != null) {
				List<NodeRef> listItems = entityListDAO.getListItems(listNodeRef, ProjectModel.TYPE_DELIVERABLE_LIST);
				for (NodeRef listItem : listItems) {
					updateDelieverableDocument(projectNodeRef, listItem);
				}
			}
		}
	}

	private void updateOriginalNodes(Map<NodeRef, NodeRef> originalMaps, List<NodeRef> listItems, QName propertyQName) {

		for (NodeRef listItem : listItems) {
			List<NodeRef> originalTasks = associationService.getTargetAssocs(listItem, propertyQName);
			List<NodeRef> tasks = new ArrayList<NodeRef>(originalTasks.size());

			for (NodeRef originalTask : originalTasks) {
				if (originalMaps.containsKey(originalTask)) {
					tasks.add(originalMaps.get(originalTask));
				}
			}
			associationService.update(listItem, propertyQName, tasks);
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
				associationService.update(listItem, ProjectModel.ASSOC_DL_CONTENT, newDocumentNodeRef);
			}
			
		}
	}

	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

		String beforeState = (String) before.get(ProjectModel.PROP_PROJECT_STATE);
		String afterState = (String) after.get(ProjectModel.PROP_PROJECT_STATE);

		// change state
		if (afterState != null && !afterState.equals(beforeState)) {
			if (afterState.equals(TaskState.InProgress.toString())) {
				logger.debug("onUpdateProperties:start project");
				nodeService.setProperty(nodeRef, ProjectModel.PROP_PROJECT_START_DATE,
						ProjectHelper.removeTime(new Date()));
				queueNode(nodeRef);
			} else if (afterState.equals(TaskState.Cancelled.toString())) {
				logger.debug("onUpdateProperties:cancel project");
				projectService.cancel(nodeRef);
			}
		}
		
		// change startdate
		Date beforeStartDate = (Date) before.get(ProjectModel.PROP_PROJECT_START_DATE);
		Date afterStartDate = (Date) after.get(ProjectModel.PROP_PROJECT_START_DATE);
		if(afterStartDate != null && afterStartDate.equals(beforeStartDate)){
			queueNode(nodeRef);
		}
		
		// change duedate
		Date beforeDueDate = (Date) before.get(ProjectModel.PROP_PROJECT_DUE_DATE);
		Date afterDueDate = (Date) after.get(ProjectModel.PROP_PROJECT_DUE_DATE);
		if(afterDueDate != null && afterDueDate.equals(beforeDueDate)){
			queueNode(nodeRef);
		}		
	}

	@Override
	protected void doBeforeCommit(String key, Set<NodeRef> pendingNodes) {

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

	@Override
	public void onDeleteNode(ChildAssociationRef childAssoc, boolean isNodeArchived) {
		
		if(!isNodeArchived){
			logger.debug("Project policy delete");
			projectService.cancel(childAssoc.getChildRef());
		}		
	}

}
