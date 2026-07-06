/*
 *
 */
package fr.becpg.test.project.formulation;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.project.data.PlanningMode;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.system.SystemConfigurationService;
import fr.becpg.test.project.AbstractProjectTestCase;

/**
 * Tests the automatic derivation of the project state from its task or sub-project states
 * when project.formulation.autoProjectState is enabled (#28147).
 *
 * @author matthieu
 */
public class ProjectAutoStateIT extends AbstractProjectTestCase {

	private static final Log logger = LogFactory.getLog(ProjectAutoStateIT.class);

	@Autowired
	private SystemConfigurationService systemConfigurationService;

	/**
	 * Tests that the parent project state follows the sub-project states by priority:
	 * all cancelled leads to Cancelled, one in progress leads to InProgress, one on hold
	 * leads to OnHold, only completed or cancelled leads to Completed, otherwise Planned.
	 */
	@Test
	public void testProjectStateDerivedFromSubProjects() {

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			systemConfigurationService.updateConfValue("project.formulation.autoProjectState", "true");
			return null;
		}, false, true);

		try {
			final NodeRef subProject1 = createSubProject("Sub Pjt 1.1");
			final NodeRef subProject2 = createSubProject("Sub Pjt 1.2");
			final NodeRef parentNodeRef = createParentProject("Parent Pjt 1", subProject1, subProject2);

			formulateProject(parentNodeRef);
			assertProjectState(parentNodeRef, ProjectState.Planned);

			setProjectState(subProject1, ProjectState.InProgress);
			formulateProject(parentNodeRef);
			assertProjectState(parentNodeRef, ProjectState.InProgress);

			setProjectState(subProject1, ProjectState.OnHold);
			formulateProject(parentNodeRef);
			assertProjectState(parentNodeRef, ProjectState.OnHold);

			setProjectState(subProject1, ProjectState.Completed);
			setProjectState(subProject2, ProjectState.Cancelled);
			formulateProject(parentNodeRef);
			assertProjectState(parentNodeRef, ProjectState.Completed);

			setProjectState(subProject1, ProjectState.Cancelled);
			formulateProject(parentNodeRef);
			assertProjectState(parentNodeRef, ProjectState.Cancelled);

		} finally {
			transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
				systemConfigurationService.resetConfValue("project.formulation.autoProjectState");
				return null;
			}, false, true);
		}
	}

	/**
	 * Tests that manually cancelling the parent project cascades the cancellation
	 * to its planned sub-projects so that the derived state remains Cancelled.
	 */
	@Test
	public void testCancelParentProjectCascadesToSubProjects() {

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			systemConfigurationService.updateConfValue("project.formulation.autoProjectState", "true");
			return null;
		}, false, true);

		try {
			final NodeRef subProject1 = createSubProject("Sub Pjt 2.1");
			final NodeRef subProject2 = createSubProject("Sub Pjt 2.2");
			final NodeRef parentNodeRef = createParentProject("Parent Pjt 2", subProject1, subProject2);

			formulateProject(parentNodeRef);
			assertProjectState(parentNodeRef, ProjectState.Planned);

			setProjectState(parentNodeRef, ProjectState.Cancelled);

			assertProjectState(subProject1, ProjectState.Cancelled);
			assertProjectState(subProject2, ProjectState.Cancelled);

			formulateProject(parentNodeRef);
			assertProjectState(parentNodeRef, ProjectState.Cancelled);

		} finally {
			transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
				systemConfigurationService.resetConfValue("project.formulation.autoProjectState");
				return null;
			}, false, true);
		}
	}

	private NodeRef createSubProject(final String name) {

		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_ENTITY_TPL_REF);

			ProjectData projectData = new ProjectData(null, name, PROJECT_HIERARCHY1_SEA_FOOD_REF, PROJECT_HIERARCHY2_CRUSTACEAN_REF, null, null, null,
					PlanningMode.Planning, null, ProjectState.Planned, null, 0, null);
			projectData.setParentNodeRef(getTestFolderNodeRef());

			projectData = (ProjectData) alfrescoRepository.save(projectData);

			return projectData.getNodeRef();
		}, false, true);
	}

	private NodeRef createParentProject(final String name, final NodeRef subProject1, final NodeRef subProject2) {

		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_ENTITY_TPL_REF);

			ProjectData projectData = new ProjectData(null, name, PROJECT_HIERARCHY1_SEA_FOOD_REF, PROJECT_HIERARCHY2_CRUSTACEAN_REF, null,
					null, null, PlanningMode.Planning, null, ProjectState.Planned, null, 0, null);
			projectData.setParentNodeRef(getTestFolderNodeRef());

			List<TaskListDataItem> taskList = new LinkedList<>();
			TaskListDataItem task1 = TaskListDataItem.build().withTaskName("sub project task 1").withIsMilestone(false).withDuration(1);
			task1.setSubProject(subProject1);
			taskList.add(task1);
			TaskListDataItem task2 = TaskListDataItem.build().withTaskName("sub project task 2").withIsMilestone(false).withDuration(1);
			task2.setSubProject(subProject2);
			taskList.add(task2);
			projectData.setTaskList(taskList);

			projectData = (ProjectData) alfrescoRepository.save(projectData);

			return projectData.getNodeRef();
		}, false, true);
	}

	private void setProjectState(final NodeRef projectNodeRef, final ProjectState projectState) {
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			logger.debug("Set project state " + projectState + " on " + projectNodeRef);
			nodeService.setProperty(projectNodeRef, ProjectModel.PROP_PROJECT_STATE, projectState.toString());
			return null;
		}, false, true);
	}

	private void formulateProject(final NodeRef projectNodeRef) {
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			projectService.formulate(projectNodeRef);
			return null;
		}, false, true);
	}

	private void assertProjectState(final NodeRef projectNodeRef, final ProjectState expectedState) {
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
			StringBuilder states = new StringBuilder();
			for (TaskListDataItem task : projectData.getTaskList()) {
				states.append(task.getTaskName()).append("=").append(task.getTaskState());
				if (task.getSubProject() != null) {
					states.append(" (sub=").append(nodeService.getProperty(task.getSubProject(), ProjectModel.PROP_PROJECT_STATE)).append(")");
				}
				states.append("; ");
			}
			assertEquals("Task states: " + states, expectedState, projectData.getProjectState());
			return null;
		}, false, true);
	}
}
