package fr.becpg.repo.admin.patch;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * 
 * @author matthieu
 *
 */
public class RemoveProductValidationWorkflowPatch extends AbstractPatch {
	
	private static Log logger = LogFactory.getLog(RemoveProductValidationWorkflowPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.removeProductValidationWorkflowPatch.result";

	private WorkflowService workflowService;
	
	private AuthorityService authorityService;
	
	
	public void setWorkflowService(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}
	

	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}


	@Override
	protected String applyInternal() throws Exception {
		
		logger.info("Apply RemoveProductValidationWorkflowPatch");
		
		WorkflowDefinition wfDef = workflowService.getDefinitionByName("jbpm$bcpgwf:productValidationWF");
		
		if(wfDef!=null){
			logger.info("Undeploy WF definition: "+wfDef.getName());
			workflowService.undeployDefinition(wfDef.getId());
		}
		String group = PermissionService.GROUP_PREFIX + "ProductReviewer";
		if(authorityService.authorityExists(group)){
		
			logger.info("Deleting group: "+group);
			authorityService.deleteAuthority(group);
		}
		
		return I18NUtil.getMessage(MSG_SUCCESS);
	}

}
