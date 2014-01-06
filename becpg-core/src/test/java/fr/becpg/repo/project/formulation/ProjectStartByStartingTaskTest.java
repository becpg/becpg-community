/*
 * 
 */
package fr.becpg.repo.project.formulation;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.repo.project.AbstractProjectTestCase;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.TaskState;

/**
 * The Class ProjectCalculatePlanningDatesTest.
 * 
 * @author quere
 */
public class ProjectStartByStartingTaskTest extends AbstractProjectTestCase {	

	private static Log logger = LogFactory.getLog(ProjectStartByStartingTaskTest.class);		

	/**
	 * Test the project is InProgress if we set a task InProgress
	 */
	@Test
	public void testStartProjectByStartingTask() {

		logger.debug("testStartProjectByStartingTask");
		initTest();
		createProject(ProjectState.Planned, null, null);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

				assertNotNull(projectData);
				assertNotNull(projectData.getTaskList());
				assertEquals(6, projectData.getTaskList().size());
				assertEquals(TaskState.Planned, projectData.getTaskList().get(0).getState());
				assertEquals(TaskState.Planned, projectData.getTaskList().get(1).getState());
				
				projectData.getTaskList().get(0).setState(TaskState.InProgress);
				alfrescoRepository.save(projectData);

				return null;
			}
		}, false, true);
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

				assertNotNull(projectData);
				assertNotNull(projectData.getTaskList());
				assertEquals(6, projectData.getTaskList().size());
				assertEquals(TaskState.InProgress, projectData.getTaskList().get(0).getState());
				assertEquals(TaskState.Planned, projectData.getTaskList().get(1).getState());				
				assertEquals(ProjectState.InProgress, projectData.getProjectState());				

				return null;
			}
		}, false, true);
	}
}
