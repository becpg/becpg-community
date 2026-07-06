/*
 *
 */
package fr.becpg.test.project.formulation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.project.data.PlanningMode;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.project.formulation.TaskWrapper;
import fr.becpg.test.project.AbstractProjectTestCase;

/**
 * Tests the planning of nested parent tasks (#34909) : the dates and duration of a parent task
 * must follow its child tasks, including when a parent task is nested inside another parent task.
 *
 * @author matthieu
 */
public class ProjectNestedParentPlanningIT extends AbstractProjectTestCase {

	private static final Log logger = LogFactory.getLog(ProjectNestedParentPlanningIT.class);

	/**
	 * Builds the following hierarchy and checks parent durations follow the children:
	 *
	 * task0 (2d)
	 * parent1
	 *   parent2 (prev: task0)
	 *     taskA (3d)
	 *     taskB (5d, prev: taskA)
	 *     milestone (prev: taskB)
	 *     taskC (4d, prev: milestone)
	 *   parent3 (prev: parent2)
	 *     taskD (6d)
	 */
	@Test
	public void testNestedParentDurations() {

		final NodeRef projectNodeRef = createNestedParentProject("Nested Pjt 1");

		inWriteTx(() -> {
			projectService.formulate(projectNodeRef);
			return null;
		});

		inReadTx(() -> {
			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			if (logger.isInfoEnabled()) {
				logger.info("Nested parent project:" + TaskWrapper.print(projectData));
			}

			TaskListDataItem parent1 = findTask(projectData, "parent1");
			TaskListDataItem parent2 = findTask(projectData, "parent2");
			TaskListDataItem parent3 = findTask(projectData, "parent3");
			TaskListDataItem taskA = findTask(projectData, "taskA");
			TaskListDataItem taskC = findTask(projectData, "taskC");
			TaskListDataItem taskD = findTask(projectData, "taskD");

			// parent2 spans from taskA start to taskC end
			assertNotNull("parent2 duration is null", parent2.getDuration());
			assertEquals(taskA.getStart(), parent2.getStart());
			assertEquals(taskC.getEnd(), parent2.getEnd());
			assertTrue("parent2 duration must cover its children chain (3+5+4 days at least), actual: " + parent2.getDuration(),
					parent2.getDuration() >= 12);

			// parent3 spans over taskD, starting after taskC
			assertNotNull("parent3 duration is null", parent3.getDuration());
			assertEquals(Integer.valueOf(6), parent3.getDuration());
			assertEquals(taskD.getStart(), parent3.getStart());
			assertEquals(taskD.getEnd(), parent3.getEnd());
			assertTrue("taskD must start after taskC end", taskD.getStart().after(taskC.getEnd()));

			// parent1 spans from taskA start to taskD end
			assertNotNull("parent1 duration is null", parent1.getDuration());
			assertEquals(parent2.getStart(), parent1.getStart());
			assertEquals(parent3.getEnd(), parent1.getEnd());
			assertTrue("parent1 duration must cover both nested parents, actual: " + parent1.getDuration(),
					parent1.getDuration() >= (parent2.getDuration() + parent3.getDuration()));

			return null;
		});
	}

	/**
	 * Same hierarchy with mixed task states like the reported case (#34909) : first child completed,
	 * second child in progress, remaining children planned. Parent durations must still follow children.
	 */
	@Test
	public void testNestedParentDurationsWithMixedStates() {

		final NodeRef projectNodeRef = createNestedParentProject("Nested Pjt 2");

		inWriteTx(() -> {
			projectService.formulate(projectNodeRef);
			return null;
		});

		inWriteTx(() -> {
			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
			findTask(projectData, "taskA").setTaskState(TaskState.InProgress);
			alfrescoRepository.save(projectData);
			return null;
		});

		inWriteTx(() -> {
			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
			findTask(projectData, "taskA").setTaskState(TaskState.Completed);
			alfrescoRepository.save(projectData);
			return null;
		});

		inWriteTx(() -> {
			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
			findTask(projectData, "taskB").setTaskState(TaskState.InProgress);
			alfrescoRepository.save(projectData);
			return null;
		});

		inWriteTx(() -> {
			projectService.formulate(projectNodeRef);
			return null;
		});

		inReadTx(() -> {
			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			if (logger.isInfoEnabled()) {
				logger.info("Nested parent project with mixed states:" + TaskWrapper.print(projectData));
			}

			TaskListDataItem parent1 = findTask(projectData, "parent1");
			TaskListDataItem parent2 = findTask(projectData, "parent2");
			TaskListDataItem parent3 = findTask(projectData, "parent3");
			TaskListDataItem taskA = findTask(projectData, "taskA");
			TaskListDataItem taskC = findTask(projectData, "taskC");
			TaskListDataItem taskD = findTask(projectData, "taskD");

			assertEquals(TaskState.Completed, taskA.getTaskState());
			assertEquals(TaskState.InProgress, parent2.getTaskState());

			// parent2 must still span all its children, not only the completed one
			assertNotNull("parent2 duration is null", parent2.getDuration());
			assertEquals(taskC.getEnd(), parent2.getEnd());
			assertTrue("parent2 duration must cover its children chain, actual: " + parent2.getDuration(), parent2.getDuration() >= 12);

			// parent1 must span both nested parents
			assertNotNull("parent1 duration is null", parent1.getDuration());
			assertEquals(parent3.getEnd(), parent1.getEnd());
			assertEquals(taskD.getEnd(), parent1.getEnd());
			assertTrue("parent1 duration must cover both nested parents, actual: " + parent1.getDuration(),
					parent1.getDuration() >= (parent2.getDuration() + parent3.getDuration()));

			return null;
		});
	}

	private NodeRef createNestedParentProject(final String name) {

		return inWriteTx(() -> {

			policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_ENTITY_TPL_REF);

			ProjectData projectData = new ProjectData(null, name, PROJECT_HIERARCHY1_SEA_FOOD_REF, PROJECT_HIERARCHY2_CRUSTACEAN_REF, null,
					null, null, PlanningMode.Planning, null, ProjectState.Planned, null, 0, null);
			projectData.setParentNodeRef(getTestFolderNodeRef());

			List<TaskListDataItem> taskList = new LinkedList<>();

			TaskListDataItem task0 = TaskListDataItem.build().withTaskName("task0").withIsMilestone(false).withDuration(2);
			taskList.add(task0);

			TaskListDataItem parent1 = TaskListDataItem.build().withTaskName("parent1").withIsMilestone(false);
			taskList.add(parent1);

			TaskListDataItem parent2 = TaskListDataItem.build().withTaskName("parent2").withIsMilestone(false);
			parent2.setParent(parent1);
			taskList.add(parent2);

			TaskListDataItem taskA = TaskListDataItem.build().withTaskName("taskA").withIsMilestone(false).withDuration(3);
			taskA.setParent(parent2);
			taskList.add(taskA);

			TaskListDataItem taskB = TaskListDataItem.build().withTaskName("taskB").withIsMilestone(false).withDuration(5);
			taskB.setParent(parent2);
			taskList.add(taskB);

			TaskListDataItem milestone = TaskListDataItem.build().withTaskName("milestone").withIsMilestone(true);
			milestone.setParent(parent2);
			taskList.add(milestone);

			TaskListDataItem taskC = TaskListDataItem.build().withTaskName("taskC").withIsMilestone(false).withDuration(4);
			taskC.setParent(parent2);
			taskList.add(taskC);

			TaskListDataItem parent3 = TaskListDataItem.build().withTaskName("parent3").withIsMilestone(false);
			parent3.setParent(parent1);
			taskList.add(parent3);

			TaskListDataItem taskD = TaskListDataItem.build().withTaskName("taskD").withIsMilestone(false).withDuration(6);
			taskD.setParent(parent3);
			taskList.add(taskD);

			projectData.setTaskList(taskList);

			projectData = (ProjectData) alfrescoRepository.save(projectData);

			// prev tasks require saved nodeRefs
			List<TaskListDataItem> savedTasks = projectData.getTaskList();
			setPrevTasks(savedTasks, "parent2", "task0");
			setPrevTasks(savedTasks, "taskB", "taskA");
			setPrevTasks(savedTasks, "milestone", "taskB");
			setPrevTasks(savedTasks, "taskC", "milestone");
			setPrevTasks(savedTasks, "parent3", "parent2");

			alfrescoRepository.save(projectData);

			return projectData.getNodeRef();
		});
	}

	private void setPrevTasks(List<TaskListDataItem> tasks, String taskName, String prevTaskName) {
		TaskListDataItem task = null;
		TaskListDataItem prevTask = null;
		for (TaskListDataItem tmp : tasks) {
			if (taskName.equals(tmp.getTaskName())) {
				task = tmp;
			} else if (prevTaskName.equals(tmp.getTaskName())) {
				prevTask = tmp;
			}
		}
		assertNotNull(task);
		assertNotNull(prevTask);
		List<NodeRef> prevTasks = new ArrayList<>(1);
		prevTasks.add(prevTask.getNodeRef());
		task.setPrevTasks(prevTasks);
	}

	private TaskListDataItem findTask(ProjectData projectData, String taskName) {
		for (TaskListDataItem task : projectData.getTaskList()) {
			if (taskName.equals(task.getTaskName())) {
				return task;
			}
		}
		fail("Task not found: " + taskName);
		return null;
	}
}
