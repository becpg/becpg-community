package fr.becpg.repo.ecm;

import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 
 * @author matthieu
 * 
 *         Case 1: - Automatic applied changed on wused (Formulation and
 *         Reports) - Changed with Version Case 2: - Create OM
 */
// Can be made with a job ?
// Maybe a policy on entity
// Avec quoi on remplit les OM automatic ?
// - Product , entity
// On fait un wused à chaud ?
// On les mets tous ?

public class AutomaticECOJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap jobData = context.getJobDetail().getJobDataMap();

		final AutomaticECOService automaticECOService = (AutomaticECOService) jobData.get("automaticECOService");
		final TenantAdminService tenantAdminService = (TenantAdminService) jobData.get("tenantAdminService");

		AuthenticationUtil.runAsSystem(new RunAsWork<Object>() {
			public Object doWork() throws Exception {
				automaticECOService.applyAutomaticEco();
				return null;
			}
		});
		
		if ((tenantAdminService != null) && tenantAdminService.isEnabled()) {
			List<Tenant> tenants = tenantAdminService.getAllTenants();
			for (Tenant tenant : tenants) {
				String tenantDomain = tenant.getTenantDomain();
				AuthenticationUtil.runAs(new RunAsWork<Object>() {
					public Object doWork() throws Exception {
						automaticECOService.applyAutomaticEco();
						return null;
					}
				}, tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain));
			}
		} 

	}

}
