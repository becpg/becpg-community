package fr.becpg.test.project.formulation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.junit.Test;

import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.test.BeCPGTestHelper;
import fr.becpg.test.project.AbstractProjectTestCase;

public class ProjectTaskAssignationIT extends AbstractProjectTestCase {

	private static final String AUTHORITY_GROUP = "AUTHORITY_GROUP_1";

	@Test
	public void testTaskAssignation() {
		
		NodeRef authorityGroup = inWriteTx(() -> BeCPGTestHelper.createGroup(AUTHORITY_GROUP, BeCPGTestHelper.USER_ONE, BeCPGTestHelper.USER_TWO));
		
		NodeRef projectNodeRef = inWriteTx(() -> {

			ProjectData projectData = new ProjectData(null, "Assignation Project Test", null, null, null, null, null, null, null, null, null, 0, null);

			projectData.setProjectState(ProjectState.InProgress);
			
			projectData.setParentNodeRef(getTestFolderNodeRef());

			projectData = (ProjectData) alfrescoRepository.save(projectData);
			
			return projectData.getNodeRef();
			
		});
		
		inWriteTx(() -> {
			
			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
			

			List<NodeRef> resources1 = new ArrayList<>();
			resources1.add(userOne);

			List<NodeRef> resources2 = new ArrayList<>();
			resources2.add(userOne);
			resources2.add(userTwo);
			
			List<NodeRef> resources3 = new ArrayList<>();
			resources3.add(authorityGroup);

			List<TaskListDataItem> taskList = new LinkedList<>();
			taskList.add(TaskListDataItem.build()
    .withTaskName("task1")
    .withIsMilestone(false)
    .withDuration(2)
    .withPrevTasks(null)
    .withResources(resources1)
    .withTaskLegend(taskLegends.get(1))
    .withWorkflowName("activiti$projectAdhoc"));
			taskList.add(TaskListDataItem.build()
    .withTaskName("task2")
    .withIsMilestone(false)
    .withDuration(2)
    .withPrevTasks(null)
    .withResources(resources2)
    .withTaskLegend(taskLegends.get(1))
    .withWorkflowName("activiti$projectAdhoc"));
			taskList.add(TaskListDataItem.build()
    .withTaskName("task3")
    .withIsMilestone(false)
    .withDuration(2)
    .withPrevTasks(null)
    .withResources(resources3)
    .withTaskLegend(taskLegends.get(1))
    .withWorkflowName("activiti$projectAdhoc"));
			projectData.setTaskList(taskList);

			projectData = (ProjectData) alfrescoRepository.save(projectData);
			
			projectService.formulate(projectNodeRef);
			
			return null;
		});
		
		inWriteTx(() -> {
			
			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
			
			int checks = 0;
			
			for (TaskListDataItem task : projectData.getTaskList()) {
				WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
				taskQuery.setProcessId(task.getWorkflowInstance());
				taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);
				
				List<WorkflowTask> workflowTasks = workflowService.queryTasks(taskQuery, false);
				
				for (WorkflowTask workflowTask : workflowTasks) {
					if ("task1".equals(task.getTaskName())) {
						checks++;
						assertEquals(BeCPGTestHelper.USER_ONE, workflowTask.getProperties().get(ContentModel.PROP_OWNER));
						assertTrue(((List<?>) workflowTask.getProperties().get(WorkflowModel.ASSOC_POOLED_ACTORS)).isEmpty());
					} else if ("task2".equals(task.getTaskName())) {
						checks++;
						assertNull(workflowTask.getProperties().get(ContentModel.PROP_OWNER));
						assertEquals(2, ((List<?>) workflowTask.getProperties().get(WorkflowModel.ASSOC_POOLED_ACTORS)).size());
						assertTrue(((List<?>) workflowTask.getProperties().get(WorkflowModel.ASSOC_POOLED_ACTORS)).contains(userOne));
						assertTrue(((List<?>) workflowTask.getProperties().get(WorkflowModel.ASSOC_POOLED_ACTORS)).contains(userTwo));
					} else if ("task3".equals(task.getTaskName())) {
						checks++;
						assertNull(workflowTask.getProperties().get(ContentModel.PROP_OWNER));
						assertEquals(1, ((List<?>) workflowTask.getProperties().get(WorkflowModel.ASSOC_POOLED_ACTORS)).size());
						assertTrue(((List<?>) workflowTask.getProperties().get(WorkflowModel.ASSOC_POOLED_ACTORS)).contains(authorityGroup));
					}
				}
			}
			
			assertEquals(3, checks);

			return null;
		});
		
		inWriteTx(() -> {
			
			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
			
			for (TaskListDataItem task : projectData.getTaskList()) {
				if ("task1".equals(task.getTaskName())) {
					task.getResources().add(userTwo);
				} else if ("task2".equals(task.getTaskName())) {
					task.getResources().remove(userTwo);
				}
			}
			
			alfrescoRepository.save(projectData);
			
			projectService.formulate(projectNodeRef);
			
			return null;
		});
		
		inWriteTx(() -> {
			
			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
			
			int checks = 0;
			
			for (TaskListDataItem task : projectData.getTaskList()) {
				WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
				taskQuery.setProcessId(task.getWorkflowInstance());
				taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);
				
				List<WorkflowTask> workflowTasks = workflowService.queryTasks(taskQuery, false);
				
				for (WorkflowTask workflowTask : workflowTasks) {
					if ("task2".equals(task.getTaskName())) {
						checks++;
						assertEquals(BeCPGTestHelper.USER_ONE, workflowTask.getProperties().get(ContentModel.PROP_OWNER));
						assertTrue(((List<?>) workflowTask.getProperties().get(WorkflowModel.ASSOC_POOLED_ACTORS)).isEmpty());
					} else if ("task1".equals(task.getTaskName())) {
						checks++;
						assertNull(workflowTask.getProperties().get(ContentModel.PROP_OWNER));
						assertEquals(2, ((List<?>) workflowTask.getProperties().get(WorkflowModel.ASSOC_POOLED_ACTORS)).size());
						assertTrue(((List<?>) workflowTask.getProperties().get(WorkflowModel.ASSOC_POOLED_ACTORS)).contains(userOne));
						assertTrue(((List<?>) workflowTask.getProperties().get(WorkflowModel.ASSOC_POOLED_ACTORS)).contains(userTwo));
					} else if ("task3".equals(task.getTaskName())) {
						checks++;
						assertNull(workflowTask.getProperties().get(ContentModel.PROP_OWNER));
						assertEquals(1, ((List<?>) workflowTask.getProperties().get(WorkflowModel.ASSOC_POOLED_ACTORS)).size());
						assertTrue(((List<?>) workflowTask.getProperties().get(WorkflowModel.ASSOC_POOLED_ACTORS)).contains(authorityGroup));
					}
				}
			}
			
			assertEquals(3, checks);

			return null;
		});
		
	}

}