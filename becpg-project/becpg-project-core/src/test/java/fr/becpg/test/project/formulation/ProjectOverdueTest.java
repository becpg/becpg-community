/*
 * 
 */
package fr.becpg.test.project.formulation;

import java.util.Date;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;

import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.project.formulation.PlanningFormulationHandler;
import fr.becpg.repo.project.impl.ProjectHelper;
import fr.becpg.test.project.AbstractProjectTestCase;

/**
 * The Class ProjectCalculatePlanningDatesTest.
 * 
 * @author quere
 */
public class ProjectOverdueTest extends AbstractProjectTestCase {	

	/**
	 * Test the calculation of the project overdue
	 */
	@Test
	public void testProjectOverdue() {

		createProject(ProjectState.Planned, new Date(), null);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

				assertNotNull(projectData);
				assertEquals(0, projectData.getOverdue().intValue());
				
				PlanningFormulationHandler planningFormulationHandler = new PlanningFormulationHandler();					
				
				projectData.getTaskList().get(0).setTaskState(TaskState.InProgress);				
				planningFormulationHandler.process(projectData);				
				assertEquals(0, projectData.getOverdue().intValue());
				
				// set start date to simulate 1 day after start (task in progress)				
				Date startDate = ProjectHelper.calculateNextDate(new Date(), 1, false);
				projectData.setStartDate(startDate);
				planningFormulationHandler.process(projectData);				
				assertEquals(0, projectData.getOverdue().intValue());
				
				// set start date to simulate 3 days after start (task in progress)
				startDate = ProjectHelper.calculateNextDate(new Date(), 3, false);
				projectData.getTaskList().get(0).setStart(startDate);				
				planningFormulationHandler.process(projectData);	
				assertEquals(1, projectData.getOverdue().intValue());
				
				// set start date to simulate 3 days after start (task completed in time)
				startDate = ProjectHelper.calculateNextDate(new Date(), 3, false);			
				Date endDate = ProjectHelper.calculateEndDate(startDate, 2);
				projectData.getTaskList().get(0).setTaskState(TaskState.Completed);
				projectData.getTaskList().get(0).setStart(startDate);	
				projectData.getTaskList().get(0).setEnd(endDate);
				projectData.getTaskList().get(1).setTaskState(TaskState.InProgress);
				planningFormulationHandler.process(projectData);				
				assertEquals(0, projectData.getOverdue().intValue());
				
				// set start date to simulate 6 days after start (task4 and task5 in progress)
				startDate = ProjectHelper.calculateNextDate(new Date(), 6, false);			
				projectData.setStartDate(startDate);
				for(TaskListDataItem t : projectData.getTaskList()){
					t.setTaskState(TaskState.Planned);
				}				
				planningFormulationHandler.process(projectData);	
				projectData.getTaskList().get(0).setTaskState(TaskState.Completed);
				projectData.getTaskList().get(1).setTaskState(TaskState.Completed);
				projectData.getTaskList().get(2).setTaskState(TaskState.Completed);
				projectData.getTaskList().get(3).setTaskState(TaskState.InProgress);
				projectData.getTaskList().get(4).setTaskState(TaskState.InProgress);	
				planningFormulationHandler.process(projectData);
				assertEquals(0, projectData.getOverdue().intValue());
				
				// set start date to simulate 9 days after start (task4 and task5 in progress)
				startDate = ProjectHelper.calculateNextDate(new Date(), 9, false);			
				projectData.getTaskList().get(0).setStart(startDate);
				for(TaskListDataItem t : projectData.getTaskList()){
					t.setTaskState(TaskState.Planned);
				}
				planningFormulationHandler.process(projectData);	
				projectData.getTaskList().get(0).setTaskState(TaskState.Completed);
				projectData.getTaskList().get(1).setTaskState(TaskState.Completed);
				projectData.getTaskList().get(2).setTaskState(TaskState.Completed);
				projectData.getTaskList().get(3).setTaskState(TaskState.InProgress);
				projectData.getTaskList().get(4).setTaskState(TaskState.InProgress);
				planningFormulationHandler.process(projectData);
				assertEquals(1, projectData.getOverdue().intValue());

				return null;
			}
		}, false, true);		
	}
}
