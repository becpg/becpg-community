/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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

import java.util.Calendar;
import java.util.List;

import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>ProjectFormulationWorker class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ProjectFormulationWorker {

	private static final Log logger = LogFactory.getLog(ProjectFormulationWorker.class);

	private ProjectService projectService;
	private TransactionService transactionService;

	/**
	 * <p>Setter for the field <code>projectService</code>.</p>
	 *
	 * @param projectService a {@link fr.becpg.repo.project.ProjectService} object.
	 */
	public void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}

	/**
	 * <p>Setter for the field <code>transactionService</code>.</p>
	 *
	 * @param transactionService a {@link org.alfresco.service.transaction.TransactionService} object.
	 */
	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	/**
	 * <p>executeFormulation.</p>
	 */
	public void executeFormulation() {

		List<NodeRef> projectNodeRefs = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			Calendar cal = Calendar.getInstance();
			
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			
			BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().ofType(ProjectModel.TYPE_PROJECT)
					.excludeVersions()
					.andPropEquals(ProjectModel.PROP_PROJECT_STATE, ProjectState.InProgress.toString())
					.andBetween(BeCPGModel.PROP_FORMULATED_DATE, "MIN", ISO8601DateFormat.format(cal.getTime()));

			List<NodeRef> ret = queryBuilder.inDB().ftsLanguage().maxResults(RepoConsts.MAX_RESULTS_UNLIMITED).list();

			queryBuilder = BeCPGQueryBuilder.createQuery().ofType(ProjectModel.TYPE_PROJECT)
					.excludeVersions()
					.andPropEquals(ProjectModel.PROP_PROJECT_STATE, ProjectState.OnHold.toString())
					.andBetween(BeCPGModel.PROP_FORMULATED_DATE, "MIN", ISO8601DateFormat.format(cal.getTime()));
			
			ret.addAll(queryBuilder.inDB().ftsLanguage().maxResults(RepoConsts.MAX_RESULTS_UNLIMITED).list());
			
			// query
			queryBuilder = BeCPGQueryBuilder.createQuery().ofType(ProjectModel.TYPE_PROJECT)
					.excludeVersions()
					.andPropEquals(ProjectModel.PROP_PROJECT_STATE, ProjectState.Planned.toString())
					.andBetween(ProjectModel.PROP_PROJECT_START_DATE, "MIN", ISO8601DateFormat.format(Calendar.getInstance().getTime()));

			ret.addAll(queryBuilder.inDB().ftsLanguage().maxResults(RepoConsts.MAX_RESULTS_UNLIMITED).list());

			return ret;

		}, false, true);

		for (NodeRef projectNodeRef : projectNodeRefs) {
			try {
				transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
					if (logger.isDebugEnabled()) {
						logger.debug("Reformulating project: " + projectNodeRef);
					}
					projectService.formulate(projectNodeRef);

					return true;

				}, false, true);

			} catch (Exception e) {
				logger.error("Cannot reformulate project:" + projectNodeRef+ " "+ TenantUtil.getCurrentDomain(), e);
			}

		}

	}
}
