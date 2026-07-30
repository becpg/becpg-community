/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.batch.WorkProviderFactory;
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

	/** Constant <code>logger</code> */
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
	 *
	 * Projects that could not be formulated because of database contention are retried once at the
	 * end of the run, so that they are not left stale until the next scheduled execution.
	 */
	public void executeFormulation() {

		List<NodeRef> contendedProjectNodeRefs = formulateAll(queryProjectsToFormulate());

		if (!contendedProjectNodeRefs.isEmpty()) {
			logger.warn("Reformulation delayed by database contention for " + contendedProjectNodeRefs.size() + " project(s), retrying once");

			List<NodeRef> stillContendedProjectNodeRefs = formulateAll(contendedProjectNodeRefs);

			if (!stillContendedProjectNodeRefs.isEmpty()) {
				logger.error("Reformulation still blocked by database contention for " + stillContendedProjectNodeRefs.size() + " project(s): "
						+ stillContendedProjectNodeRefs);
			}
		}

	}

	/**
	 * Formulate the given projects.
	 *
	 * @param projectNodeRefs a {@link java.util.List} object
	 * @return the projects that could not be formulated because of database contention
	 */
	private List<NodeRef> formulateAll(List<NodeRef> projectNodeRefs) {
		List<NodeRef> contendedProjectNodeRefs = new ArrayList<>();

		for (NodeRef projectNodeRef : projectNodeRefs) {
			if (!formulateProject(projectNodeRef)) {
				contendedProjectNodeRefs.add(projectNodeRef);
			}
		}

		return contendedProjectNodeRefs;
	}

	/**
	 * Formulate a single project in its own transaction.
	 *
	 * @param projectNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return false when the formulation was abandoned because of database contention, true otherwise
	 */
	private boolean formulateProject(NodeRef projectNodeRef) {
		try {
			transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
				if (logger.isDebugEnabled()) {
					logger.debug("Reformulating project: " + projectNodeRef);
				}
				projectService.formulate(projectNodeRef);

				return true;

			}, false, true);

			return true;

		} catch (Exception e) {
			if (RetryingTransactionHelper.extractRetryCause(e) != null) {
				logger.warn("Database contention while reformulating project: " + projectNodeRef + " " + TenantUtil.getCurrentDomain() + " - "
						+ e.getMessage());
				return false;
			}

			logger.error("Cannot reformulate project:" + projectNodeRef + " " + TenantUtil.getCurrentDomain(), e);
			return true;
		}
	}

	/**
	 * Query the projects that have to be reformulated: in progress or on hold projects not formulated
	 * today, and planned projects whose start date has been reached.
	 *
	 * @return a {@link java.util.List} object
	 */
	private List<NodeRef> queryProjectsToFormulate() {
		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			Date startOfDay = getStartOfDay();

			List<NodeRef> projectNodeRefs = new ArrayList<>(queryProjects(ProjectState.InProgress, BeCPGModel.PROP_FORMULATED_DATE, startOfDay));
			projectNodeRefs.addAll(queryProjects(ProjectState.OnHold, BeCPGModel.PROP_FORMULATED_DATE, startOfDay));
			projectNodeRefs.addAll(queryProjects(ProjectState.Planned, ProjectModel.PROP_PROJECT_START_DATE, Calendar.getInstance().getTime()));

			return projectNodeRefs;

		}, false, true);
	}

	/**
	 * Query the projects of a given state whose date property is before the given date.
	 *
	 * @param projectState a {@link fr.becpg.repo.project.data.ProjectState} object
	 * @param dateProperty a {@link org.alfresco.service.namespace.QName} object
	 * @param maxDate a {@link java.util.Date} object
	 * @return a {@link java.util.List} object
	 */
	private List<NodeRef> queryProjects(ProjectState projectState, QName dateProperty, Date maxDate) {
		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().ofType(ProjectModel.TYPE_PROJECT)
				.excludeVersions()
				.excludeArchivedEntities()
				.andPropEquals(ProjectModel.PROP_PROJECT_STATE, projectState.toString())
				.andBetween(dateProperty, "MIN", ISO8601DateFormat.format(maxDate));

		return WorkProviderFactory.fromQueryBuilder(queryBuilder.inDB().ftsLanguage()).collect();
	}

	private Date getStartOfDay() {
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return cal.getTime();
	}
}
