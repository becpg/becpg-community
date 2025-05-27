package fr.becpg.repo.supplier.security;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
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
import fr.becpg.repo.security.data.PermissionModel;
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
	private EntityDictionaryService entityDictionaryService;

	@Override
	public boolean checkIsInSecurityGroup(NodeRef entityNodeRef, PermissionModel permissionModel) {
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
			if (supplierNodeRef != null) {
				if (supplierPortalService.isCurrentUserInSupplierGroup(supplierNodeRef)) {
					String supplierAccount = AuthenticationUtil.getFullyAuthenticatedUser();
					
					for (WorkflowInstance workflow : workflowService.getWorkflowsForContent(entityNodeRef, true)) {
						for (WorkflowTask task : workflowService.getAssignedTasks(supplierAccount,
								WorkflowTaskState.IN_PROGRESS)) {
							if (task.getPath().getInstance().getId().equals(workflow.getId())) {
								return Math.min(accesMode, SecurityService.WRITE_ACCESS);
							}
						}
						
						for (WorkflowTask task : workflowService.getPooledTasks(supplierAccount)) {
							if (task.getPath().getInstance().getId().equals(workflow.getId())) {
								return Math.min(accesMode, SecurityService.WRITE_ACCESS);
							}
						}

					}
					return Math.min(accesMode, SecurityService.READ_ACCESS);
				}
				
			}
		}

		return SecurityService.NONE_ACCESS;

	}

}
