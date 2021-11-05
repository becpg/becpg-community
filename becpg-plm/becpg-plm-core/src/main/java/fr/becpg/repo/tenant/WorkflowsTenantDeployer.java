package fr.becpg.repo.tenant;

import javax.annotation.PostConstruct;

import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantDeployer;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class WorkflowsTenantDeployer implements TenantDeployer {

	private static Log logger = LogFactory.getLog(WorkflowsTenantDeployer.class);
	
	@Autowired
	@Qualifier("WorkflowService")
	private WorkflowService workflowService;
	@Autowired
	private TenantAdminService tenantAdminService;
	
	@Override
	public void onEnableTenant() {
		//#11496 Fix a bug in multitenancy cache is not set-up correctly;
		logger.info("beCPG - Refresh workflow definitions for: "+ TenantUtil.getCurrentDomain());
		workflowService.getAllDefinitions();	
	}

	@Override
	public void onDisableTenant() {
		// Do nothing
		
	}

	@Override
	@PostConstruct
	public void init() {
	  tenantAdminService.register(this);
	}

	@Override
	public void destroy() {
		// Do nothing
	}

}
