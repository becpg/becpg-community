/*
 * 
 */
package fr.becpg.repo.project.policy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.project.ProjectService;

/**
 * The Class ProjectPolicy.
 * 
 * @author querephi
 */
@Service
public class ProjectPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnCreateAssociationPolicy {

	/** The logger. */
	private static Log logger = LogFactory.getLog(ProjectPolicy.class);

	private EntityListDAO entityListDAO;
	private ProjectService projectService;
	private AssociationService associationService;
	private CopyService copyService;

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

	/**
	 * Inits the.
	 */
	public void doInit() {
		logger.debug("Init ProjectPolicy...");
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME,
				ProjectModel.TYPE_PROJECT, ProjectModel.ASSOC_PROJECT_TPL, new JavaBehaviour(this,
						"onCreateAssociation"));
		
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME,
				ProjectModel.TYPE_PROJECT, ProjectModel.ASSOC_PROJECT_ENTITY, new JavaBehaviour(this,
						"onCreateAssociation"));

	}

	@Override
	public void onCreateAssociation(AssociationRef assocRef) {
		
		if(assocRef.getTypeQName().equals(ProjectModel.ASSOC_PROJECT_TPL)){
			// copy datalist from Tpl to project
			logger.debug("copy datalists");
			Collection<QName> dataLists = new ArrayList<QName>();
			dataLists.add(ProjectModel.TYPE_TASK_LIST);
			dataLists.add(ProjectModel.TYPE_DELIVERABLE_LIST);
			entityListDAO.copyDataLists(assocRef.getTargetRef(), assocRef.getSourceRef(), dataLists, true);
			
			//refresh reference to prevTasks
			//TODO : do it in a generic way		
			NodeRef listContainerNodeRef = entityListDAO.getListContainer(assocRef.getSourceRef());
			if(listContainerNodeRef != null){
				NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, ProjectModel.TYPE_TASK_LIST);
				if(listNodeRef != null){
					List<NodeRef> listItems = entityListDAO.getListItems(listNodeRef, ProjectModel.TYPE_TASK_LIST);
					Map<NodeRef, NodeRef> originalMaps = new HashMap<NodeRef, NodeRef>(listItems.size());
					for(NodeRef listItem : listItems){
						originalMaps.put(copyService.getOriginal(listItem), listItem);
					}				
					
					//updateOriginalNodes(originalMaps, listItems, ProjectModel.ASSOC_TL_PREV_TASKS);
					
					listNodeRef = entityListDAO.getList(listContainerNodeRef, ProjectModel.TYPE_DELIVERABLE_LIST);
					listItems = entityListDAO.getListItems(listNodeRef, ProjectModel.TYPE_DELIVERABLE_LIST);
					updateOriginalNodes(originalMaps, listItems, ProjectModel.ASSOC_DL_TASK);
					
				}
			}

			projectService.start(assocRef.getSourceRef());
		}
		else if(assocRef.getTypeQName().equals(ProjectModel.ASSOC_PROJECT_ENTITY)){
			//add project aspect on entity
			nodeService.addAspect(assocRef.getTargetRef(), ProjectModel.ASPECT_PROJECT_ASPECT, null);
			associationService.update(assocRef.getTargetRef(), ProjectModel.ASSOC_PROJECT, assocRef.getSourceRef());
		}		
	}
	
	private void updateOriginalNodes(Map<NodeRef, NodeRef> originalMaps, List<NodeRef> listItems, QName propertyQName){
		
		for(NodeRef listItem : listItems){
			List<NodeRef> originalTasks = associationService.getTargetAssocs(listItem, propertyQName);
			List<NodeRef> tasks = new ArrayList<NodeRef>(originalTasks.size());
			
			logger.debug("originalPrevTasks: " + originalTasks);
			
			for(NodeRef originalTask : originalTasks){
				logger.debug("###originalMaps.get(originalTask): " + originalMaps.get(originalTask));
				if(originalMaps.containsKey(originalTask)){
					tasks.add(originalMaps.get(originalTask));
				}						
			}
			logger.debug("tasks: " + tasks);
			associationService.update(listItem, propertyQName, tasks);
		}
	}
}
