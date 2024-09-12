/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package fr.becpg.repo.bootstrap;

import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

public class MultiTenantWorkflowBootstrap extends AbstractLifecycleBean {

	private TenantAdminService tenantAdminService;
	private WorkflowService workflowService;
	private TransactionService transactionService;

	private static Log logger = LogFactory.getLog(MultiTenantWorkflowBootstrap.class);

	public void setTenantAdminService(TenantAdminService tenantAdminService) {
		this.tenantAdminService = tenantAdminService;
	}

	public void setWorkflowService(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	@Override
	protected void onBootstrap(ApplicationEvent event)

	{
		if (tenantAdminService.isEnabled()) {
			@SuppressWarnings("deprecation")
			List<Tenant> tenants = tenantAdminService.getAllTenants();
			if (tenants.size() > 1) {
				for (Tenant tenant : tenants) {
					if (tenant.isEnabled()) {
						String tenantDomain = tenant.getTenantDomain();
						List<WorkflowDefinition> workflowDefinitions = AuthenticationUtil.runAs(() -> {

							return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
								return workflowService.getAllDefinitions();
							}, false, true);

						}, tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain));

						if (logger.isInfoEnabled()) {
							logger.info("Loading " + workflowDefinitions.size() + " workflow definitions for tenant " + tenantDomain);
						}

					}
				}
			}
		}

	}

	@Override
	protected void onShutdown(ApplicationEvent event) {
		//Do Nothing
		
	}

}
