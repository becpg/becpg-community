/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.project.impl;

import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
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

	private static final Log logger = LogFactory.getLog(ProjectFormulationJob.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		logger.info("Start of Project Formulation Job.");

		JobDataMap jobData = context.getJobDetail().getJobDataMap();

		final ProjectFormulationWorker projectFormulationWorker = (ProjectFormulationWorker) jobData.get(KEY_PROJECT_FORMULATION_WORKER);
		final TenantAdminService tenantAdminService = (TenantAdminService) jobData.get(KEY_TENANT_ADMIN_SERVICE);

		AuthenticationUtil.runAsSystem(() -> {
			projectFormulationWorker.executeFormulation();
			return null;
		});

		if ((tenantAdminService != null) && tenantAdminService.isEnabled()) {
			List<Tenant> tenants = tenantAdminService.getAllTenants();
			for (Tenant tenant : tenants) {
				String tenantDomain = tenant.getTenantDomain();
				AuthenticationUtil.runAs(() -> {
					projectFormulationWorker.executeFormulation();
					return null;
				} , tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain));
			}
		}

		logger.info("End of Project Formulation Job.");
	}
}
