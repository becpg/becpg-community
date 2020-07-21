package fr.becpg.repo.product.requirement;

import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import fr.becpg.repo.product.formulation.ProductSpecificationsFormulationHandler;

// Un cron régulier qui formule et recalcul tous les cahiers des charges

/*
 *  TODO 
 *  Ajouter une assoc respectedProductSpecifications
 *  Ajouter une liste cas d'emploi sur les spécifications avec MP - Famille /SS Famille - Date de formulation
 *  Pour toutes les MP et PFs pour chaque cahier des charges tester
 *   si les dates de modifs et de formulation si pas à jour rechecker le cahier des charges et mettre à jour l'assoc
 *   
 */


//
//ReqCtrlRefScanner.java:
//Construit un index cahier des charges, produit non respecté oui/non, date de création
//- Régulièrement
//- On cherche les produits dont la date de créa est > à la date de l'index et on enrichi l'index
//- On cherche les produits dont la date de modif est > à la date de l'index et on mets à jour l'index
//- On cherche les cahiers des charges dont la date de modif est > à la date de l'index et on mets à jour l'i


/**
 * <p>ProductSpecificationFormulationJob class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ProductSpecificationFormulationJob implements Job {

	/** {@inheritDoc} */
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap jobData = context.getJobDetail().getJobDataMap();

		final ProductSpecificationsFormulationHandler productSpecificationsFormulationHandler = (ProductSpecificationsFormulationHandler) jobData.get("productSpecificationsFormulationHandler");
		final TenantAdminService tenantAdminService = (TenantAdminService) jobData.get("tenantAdminService");

		AuthenticationUtil.runAsSystem(new RunAsWork<Object>() {
			public Object doWork() throws Exception {
				productSpecificationsFormulationHandler.run();
				return null;
			}
		});
		
		if ((tenantAdminService != null) && tenantAdminService.isEnabled()) {
			List<Tenant> tenants = tenantAdminService.getAllTenants();
			for (Tenant tenant : tenants) {
				String tenantDomain = tenant.getTenantDomain();
				AuthenticationUtil.runAs(new RunAsWork<Object>() {
					public Object doWork() throws Exception {
						productSpecificationsFormulationHandler.run();
						return null;
					}
				}, tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain));
			}
		} 

	}

}
