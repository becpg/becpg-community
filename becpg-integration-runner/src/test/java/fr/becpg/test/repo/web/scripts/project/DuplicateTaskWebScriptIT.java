/*
 *
 */
package fr.becpg.test.repo.web.scripts.project;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.project.data.PlanningMode;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.test.project.AbstractProjectTestCase;
import fr.becpg.test.utils.TestWebscriptExecuters;
import fr.becpg.test.utils.TestWebscriptExecuters.PostRequest;
import fr.becpg.test.utils.TestWebscriptExecuters.Response;

/**
 * Tests the datalist duplicate action on hierarchical task lists (#33202) :
 * duplicating a task under a parent task must create the copy under the same parent,
 * with a consistent sort and depth level.
 *
 * @author matthieu
 */
public class DuplicateTaskWebScriptIT extends AbstractProjectTestCase {

	/**
	 * Tests that duplicating a child task keeps the link with its parent task.
	 */
	@Test
	public void testDuplicateTaskUnderParentTask() throws Exception {

		final NodeRef projectNodeRef = createMultiLevelProject(ProjectState.Planned, null, null, PlanningMode.Planning);

		List<NodeRef> originalTasks = inReadTx(() -> {
			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
			List<NodeRef> taskNodeRefs = new ArrayList<>();
			for (TaskListDataItem task : projectData.getTaskList()) {
				taskNodeRefs.add(task.getNodeRef());
			}
			return taskNodeRefs;
		});

		assertEquals(6, originalTasks.size());

		// task2 (index 1) is a child of task1 (index 0)
		NodeRef parentTaskNodeRef = originalTasks.get(0);
		NodeRef childTaskNodeRef = originalTasks.get(1);

		duplicateTasks(projectNodeRef, List.of(childTaskNodeRef));

		inReadTx(() -> {
			NodeRef duplicatedTask = findDuplicatedTask(projectNodeRef, originalTasks, 7);

			assertEquals(parentTaskNodeRef, nodeService.getProperty(duplicatedTask, BeCPGModel.PROP_PARENT_LEVEL));
			assertEquals(2, nodeService.getProperty(duplicatedTask, BeCPGModel.PROP_DEPTH_LEVEL));
			assertNotNull(nodeService.getProperty(duplicatedTask, BeCPGModel.PROP_SORT));
			return null;
		});
	}

	/**
	 * Tests that duplicating a parent task with its children remaps the children copies
	 * to the duplicated parent.
	 */
	@Test
	public void testDuplicateParentTaskWithChildren() throws Exception {

		final NodeRef projectNodeRef = createMultiLevelProject(ProjectState.Planned, null, null, PlanningMode.Planning);

		List<NodeRef> originalTasks = inReadTx(() -> {
			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
			List<NodeRef> taskNodeRefs = new ArrayList<>();
			for (TaskListDataItem task : projectData.getTaskList()) {
				taskNodeRefs.add(task.getNodeRef());
			}
			return taskNodeRefs;
		});

		// task1 (index 0) is the parent of task2 (index 1) and task3 (index 2)
		duplicateTasks(projectNodeRef, List.of(originalTasks.get(0), originalTasks.get(1), originalTasks.get(2)));

		inReadTx(() -> {
			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
			assertEquals(9, projectData.getTaskList().size());

			NodeRef duplicatedParent = null;
			List<NodeRef> duplicatedChildParents = new ArrayList<>();
			for (TaskListDataItem task : projectData.getTaskList()) {
				if (!originalTasks.contains(task.getNodeRef())) {
					NodeRef parentLevel = (NodeRef) nodeService.getProperty(task.getNodeRef(), BeCPGModel.PROP_PARENT_LEVEL);
					if (parentLevel == null) {
						duplicatedParent = task.getNodeRef();
					} else {
						duplicatedChildParents.add(parentLevel);
					}
				}
			}

			assertNotNull(duplicatedParent);
			assertEquals(2, duplicatedChildParents.size());
			for (NodeRef childParent : duplicatedChildParents) {
				assertEquals(duplicatedParent, childParent);
			}
			return null;
		});
	}

	private void duplicateTasks(NodeRef projectNodeRef, List<NodeRef> taskNodeRefs) throws Exception {

		NodeRef taskListNodeRef = inReadTx(() -> {
			NodeRef listContainer = entityListDAO.getListContainer(projectNodeRef);
			return entityListDAO.getList(listContainer, ProjectModel.TYPE_TASK_LIST);
		});

		JSONObject data = new JSONObject();
		JSONArray nodeRefs = new JSONArray();
		for (NodeRef taskNodeRef : taskNodeRefs) {
			nodeRefs.put(taskNodeRef.toString());
		}
		data.put("nodeRefs", nodeRefs);

		String url = "/slingshot/datalists/action/duplicate/node/" + taskListNodeRef.toString().replace("://", "/");
		Response response = TestWebscriptExecuters.sendRequest(new PostRequest(url, data.toString(), "application/json"), 200, "admin");
		assertNotNull(response);

		JSONObject result = new JSONObject(response.getContentAsString());
		JSONArray results = result.getJSONArray("results");
		for (int i = 0; i < results.length(); i++) {
			assertTrue(results.getJSONObject(i).getBoolean("success"));
		}
	}

	private NodeRef findDuplicatedTask(NodeRef projectNodeRef, List<NodeRef> originalTasks, int expectedSize) {
		ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
		assertEquals(expectedSize, projectData.getTaskList().size());
		NodeRef duplicatedTask = null;
		for (TaskListDataItem task : projectData.getTaskList()) {
			if (!originalTasks.contains(task.getNodeRef())) {
				duplicatedTask = task.getNodeRef();
			}
		}
		assertNotNull(duplicatedTask);
		return duplicatedTask;
	}
}
