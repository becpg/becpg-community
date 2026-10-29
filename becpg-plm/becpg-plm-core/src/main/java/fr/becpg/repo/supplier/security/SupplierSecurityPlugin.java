package fr.becpg.repo.supplier.security;

import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowPackageComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.helper.AuthorityHelper;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.security.SecurityService;
import fr.becpg.repo.security.plugins.SecurityServicePlugin;
import fr.becpg.repo.supplier.SupplierPortalService;

/**
 * <p>SupplierSecurityPlugin class.</p>
 *
 * @author matthieu
 */
@Service
public class SupplierSecurityPlugin implements SecurityServicePlugin {

	private static final Log logger = LogFactory.getLog(SupplierSecurityPlugin.class);

	private static final String SUPPLIER_WIZARD_PREFIX = "/share/page/wizard?id=supplier-";

	@Autowired
	@Lazy
	private SupplierPortalService supplierPortalService;

	@Autowired
	@Qualifier("WorkflowService")
	private WorkflowService workflowService;
	
	@Autowired
	@Qualifier("workflowPackageImpl")
	private WorkflowPackageComponent workflowPackageComponent;

	@Autowired
	private AlfrescoRepository<ProjectData> alfrescoRepository;

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	/** {@inheritDoc} */
	@Override
	public boolean checkIsInSecurityGroup(NodeRef entityNodeRef, List<NodeRef> groups) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accept(QName nodeType) {
		return AuthorityHelper.isCurrentUserExternal() && entityDictionaryService.isSubClass(nodeType, BeCPGModel.TYPE_ENTITY_V2);
	}


	/** {@inheritDoc} */
	@Override
	public int getMaxAccessMode(NodeRef entityNodeRef) {
	    if (entityNodeRef != null) {
	        NodeRef supplierNodeRef = supplierPortalService.getSupplierNodeRef(entityNodeRef);
	        if ((supplierNodeRef != null) && supplierPortalService.isCurrentUserInSupplierGroup(supplierNodeRef)) {
	            String supplierAccount = AuthenticationUtil.getFullyAuthenticatedUser();
	            
	            List<String> contentWorkflowIds =  workflowPackageComponent.getWorkflowIdsForContent(entityNodeRef);
	            if (contentWorkflowIds.isEmpty()) {
	                return SecurityService.READ_ACCESS;
	            }
	            
	            List<WorkflowTask> assignedTasks = workflowService.getAssignedTasks(
	                supplierAccount, WorkflowTaskState.IN_PROGRESS);
	            
	            boolean hasMatchingTask = assignedTasks.stream()
	                .anyMatch(task -> contentWorkflowIds.contains(task.getPath().getInstance().getId())
	                    && hasSupplierWizardDeliverable(task));
	                
	            if (hasMatchingTask) {
	                return SecurityService.WRITE_ACCESS;
	            }
	            
	            // Check pooled tasks
	            List<WorkflowTask> pooledTasks = workflowService.getPooledTasks(supplierAccount);
	            hasMatchingTask = pooledTasks.stream()
	                .anyMatch(task -> contentWorkflowIds.contains(task.getPath().getInstance().getId())
	                    && hasSupplierWizardDeliverable(task));
	                
	            if (hasMatchingTask) {
	                return SecurityService.WRITE_ACCESS;
	            }
	            
	            return SecurityService.READ_ACCESS;
	        }
	    }
	    
	    return SecurityService.WRITE_ACCESS;
	}

	/**
	 * Tells whether a task opens the supplier wizard: only such a task lets the supplier edit the entity,
	 * not a signature or a validation task of the same project.
	 *
	 * @param task the in-progress workflow task of the supplier
	 * @return true if one of the project deliverables linked to the task is a supplier wizard
	 */
	private boolean hasSupplierWizardDeliverable(WorkflowTask task) {
		NodeRef projectNodeRef = (NodeRef) task.getProperties().get(BeCPGModel.ASSOC_WORKFLOW_ENTITY);
		if (projectNodeRef == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("No project node found on workflow task " + task.getId());
			}
			return false;
		}

		ProjectData projectData = alfrescoRepository.findOne(projectNodeRef);
		if ((projectData == null) || (projectData.getDeliverableList() == null)) {
			if (logger.isDebugEnabled()) {
				logger.debug("No deliverables for project " + projectNodeRef + " on task " + task.getId());
			}
			return false;
		}

		NodeRef workflowTaskNodeRef = (NodeRef) task.getProperties().get(ProjectModel.ASSOC_WORKFLOW_TASK);

		for (DeliverableListDataItem deliverable : projectData.getDeliverableList()) {
			String url = deliverable.getUrl();
			if ((url != null) && url.startsWith(SUPPLIER_WIZARD_PREFIX)) {
				List<NodeRef> deliverableTasks = deliverable.getTasks();
				if ((deliverableTasks != null) && deliverableTasks.contains(workflowTaskNodeRef)) {
					return true;
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("No supplier wizard deliverable found for task " + task.getId() + " in project " + projectNodeRef);
		}
		return false;
	}

}
