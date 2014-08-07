/*
 * 
 */
package fr.becpg.test.repo.project;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.repo.project.ProjectWorkflowService;
import fr.becpg.repo.project.data.PlanningMode;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.policy.ProjectPolicy;
import fr.becpg.test.project.AbstractProjectTestCase;

/**
 * The Class ProjectServiceTest.
 * 
 * @author quere
 */
public class NPDServiceTest extends AbstractProjectTestCase {	

	private static Log logger = LogFactory.getLog(NPDServiceTest.class);
	
	@Resource
	private ProjectPolicy projectPolicy;
	
	@Resource
	private ProjectWorkflowService projectWorkflowService;
	
	@Resource
	private CopyService copyService;
	
	@Resource private PersonService personService;
	
	@Test
	public void testNPDProjectTask() {

		
		final NodeRef projectNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<NodeRef>() {
					@Override
					public NodeRef execute() throws Throwable {
						
						// create project Tpl
						ProjectData projectData = new ProjectData(null, "Pjt", PROJECT_HIERARCHY1_SEA_FOOD_REF, PROJECT_HIERARCHY2_CRUSTACEAN_REF, null,
										null, null, PlanningMode.Planning, null, null, null, 0, null);
						
						// create datalists
						List<TaskListDataItem> taskList = new LinkedList<TaskListDataItem>();
						taskList.add(new TaskListDataItem(null, "task1", false, 2, null, assigneesOne, taskLegends.get(0),
								"activiti$projectNewProduct"));
						projectData.setTaskList(taskList);
						
						projectData.setParentNodeRef(testFolderNodeRef);
						projectData = (ProjectData) alfrescoRepository.save(projectData);
						
						// start
						projectData.setProjectState(ProjectState.InProgress);
						projectData = (ProjectData) alfrescoRepository.save(projectData);
						
						return projectData.getNodeRef();
					}
				}, false, true);
		
		transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<NodeRef>() {
					@Override
					public NodeRef execute() throws Throwable {
						
						ProjectData projectData = (ProjectData)alfrescoRepository.findOne(projectNodeRef);
						assertEquals(ProjectState.InProgress, projectData.getProjectState());
						
						logger.info("workflow instance " + projectData.getTaskList().get(0).getWorkflowInstance());
						assertNotNull(projectData.getTaskList().get(0).getWorkflowInstance());
						
						return null;
					}
				}, false, true);

	}
}
