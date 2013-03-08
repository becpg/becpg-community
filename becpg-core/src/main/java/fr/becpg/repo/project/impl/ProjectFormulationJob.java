package fr.becpg.repo.project.impl;

import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class ProjectFormulationJob implements Job {
	
	private static final String KEY_PROJECT_FORMULATION_WORKER = "projectFormulationWorker";
	private static final String KEY_TENANT_ADMIN_SERVICE = "tenantAdminService";
	
	private static Log logger = LogFactory.getLog(ProjectFormulationJob.class);
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		logger.info("Start of Project Formulation Job.");
		
		JobDataMap jobData = context.getJobDetail().getJobDataMap();
		
		final ProjectFormulationWorker projectFormulationWorker = (ProjectFormulationWorker)jobData.get(KEY_PROJECT_FORMULATION_WORKER);
		final TenantAdminService tenantAdminService = (TenantAdminService)jobData.get(KEY_TENANT_ADMIN_SERVICE);
		
		projectFormulationWorker.executeFormulation();
        
        if ((tenantAdminService != null) && tenantAdminService.isEnabled())
        {
            List<Tenant> tenants = tenantAdminService.getAllTenants();
            for (Tenant tenant : tenants)
            {
                String tenantDomain = tenant.getTenantDomain();
                AuthenticationUtil.runAs(new RunAsWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                    	projectFormulationWorker.executeFormulation();
                        return null;
                    }
                }, tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain));
            }
        }
		
		logger.info("End of Project Formulation Job.");
	}
}
