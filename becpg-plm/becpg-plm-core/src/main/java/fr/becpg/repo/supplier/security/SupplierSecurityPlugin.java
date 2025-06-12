package fr.becpg.repo.supplier.security;

import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowPackageComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.helper.AuthorityHelper;
import fr.becpg.repo.security.SecurityService;
import fr.becpg.repo.security.plugins.SecurityServicePlugin;
import fr.becpg.repo.supplier.SupplierPortalService;

@Service
public class SupplierSecurityPlugin implements SecurityServicePlugin {

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
	private EntityDictionaryService entityDictionaryService;

	@Override
	public boolean checkIsInSecurityGroup(NodeRef entityNodeRef, List<NodeRef> groups) {
		return false;
	}

	@Override
	public boolean accept(QName nodeType) {
		return AuthorityHelper.isCurrentUserExternal() && entityDictionaryService.isSubClass(nodeType, BeCPGModel.TYPE_ENTITY_V2);
	}

	@Override
	public int computeAccessMode(NodeRef entityNodeRef, int accesMode) {
	    if (entityNodeRef != null) {
	        NodeRef supplierNodeRef = supplierPortalService.getSupplierNodeRef(entityNodeRef);
	        if ((supplierNodeRef != null) && supplierPortalService.isCurrentUserInSupplierGroup(supplierNodeRef)) {
	            String supplierAccount = AuthenticationUtil.getFullyAuthenticatedUser();
	            
	            List<String> contentWorkflowIds =  workflowPackageComponent.getWorkflowIdsForContent(entityNodeRef);
	            if (contentWorkflowIds.isEmpty()) {
	                return Math.min(accesMode, SecurityService.READ_ACCESS);
	            }
	            
	            List<WorkflowTask> assignedTasks = workflowService.getAssignedTasks(
	                supplierAccount, WorkflowTaskState.IN_PROGRESS);
	            
	            boolean hasMatchingTask = assignedTasks.stream()
	                .anyMatch(task -> contentWorkflowIds.contains(task.getPath().getInstance().getId()));
	                
	            if (hasMatchingTask) {
	                return Math.min(accesMode, SecurityService.WRITE_ACCESS);
	            }
	            
	            // Check pooled tasks
	            List<WorkflowTask> pooledTasks = workflowService.getPooledTasks(supplierAccount);
	            hasMatchingTask = pooledTasks.stream()
	                .anyMatch(task -> contentWorkflowIds.contains(task.getPath().getInstance().getId()));
	                
	            if (hasMatchingTask) {
	                return Math.min(accesMode, SecurityService.WRITE_ACCESS);
	            }
	            
	            return Math.min(accesMode, SecurityService.READ_ACCESS);
	        }
	    }
	    
	    return accesMode;
	}


}
