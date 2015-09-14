package fr.becpg.test.project.formulation;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.test.BeCPGTestHelper;
import fr.becpg.test.project.AbstractProjectTestCase;

public class ProjectTaskDelegationTest extends AbstractProjectTestCase {
	
	protected static final String USER_THREE = "userThree";
	protected static final String USER_FOUR = "userFour";
	private NodeRef userThree;
	private NodeRef userFour;
	private List<NodeRef> assigneesThree;
	private List<NodeRef> assigneesFour;
	
	@Test
	public void testDelegation() {	

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {
				
				AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
				ProjectData projectData = new ProjectData(null, "Deleg", null, null,
						null, null, null, null, null, null, null, 0, null);
				
				projectData.setParentNodeRef(getTestFolderNodeRef());			
				
				//simple delegation
				userThree = BeCPGTestHelper.createUser(USER_THREE);	
				nodeService.setProperty(userThree, ProjectModel.PROP_QNAME_DELEGATION_STATE,true);
				nodeService.setProperty(userThree, ProjectModel.PROP_QNAME_DELEGATION_START, new SimpleDateFormat("dd/MM/yyyy").parse("09/09/2015"));
				nodeService.setProperty(userThree, ProjectModel.PROP_QNAME_DELEGATION_END, new SimpleDateFormat("dd/MM/yyyy").parse("30/09/2015"));
				nodeService.setProperty(userThree, ProjectModel.PROP_QNAME_REASSIGN_TASK,true);
				associationService.update(userThree,ProjectModel.PROP_QNAME_REASSIGN_RESOURCE,userOne);		
				
				assigneesThree = new ArrayList<>();
				assigneesThree.add(userThree);	
				
				//multiple delegation
				userFour = BeCPGTestHelper.createUser(USER_FOUR);	

				nodeService.setProperty(userFour, ProjectModel.PROP_QNAME_DELEGATION_STATE,true);
				nodeService.setProperty(userFour, ProjectModel.PROP_QNAME_DELEGATION_START, new SimpleDateFormat("dd/MM/yyyy").parse("09/09/2015"));
				nodeService.setProperty(userFour, ProjectModel.PROP_QNAME_DELEGATION_END, new SimpleDateFormat("dd/MM/yyyy").parse("30/09/2015"));
				nodeService.setProperty(userFour, ProjectModel.PROP_QNAME_REASSIGN_TASK,true);
				associationService.update(userFour,ProjectModel.PROP_QNAME_REASSIGN_RESOURCE,userThree);		
				
				assigneesFour = new ArrayList<>();
				assigneesFour.add(userFour);
				
				List<TaskListDataItem> taskList = new LinkedList<>();				
				taskList.add(new TaskListDataItem(null, "task5", false, 2, null, assigneesThree, taskLegends.get(1), "activiti$projectAdhoc"));
				//taskList.add(new TaskListDataItem(null, "task5", false, 2, new SimpleDateFormat("dd/MM/yyyy").parse("08/09/2015"), new SimpleDateFormat("dd/MM/yyyy").parse("09/12/2015"), TaskState.Planned, 0, prevTasks, resources, taskLegend, workflowName, workflowInstance, plannedExpense, expense));
				taskList.add(new TaskListDataItem(null, "task6", false, 2, null, assigneesFour, taskLegends.get(1), "activiti$projectAdhoc"));
				projectData.setTaskList(taskList);
				
				projectData.getTaskList().get(0).setStart(new SimpleDateFormat("dd/MM/yyyy").parse("08/09/2015"));
				projectData.getTaskList().get(0).setEnd(new SimpleDateFormat("dd/MM/yyyy").parse("09/12/2015"));
				projectData.getTaskList().get(1).setStart(new SimpleDateFormat("dd/MM/yyyy").parse("08/09/2015"));
				projectData.getTaskList().get(1).setEnd(new SimpleDateFormat("dd/MM/yyyy").parse("09/12/2015"));
				
				projectData = (ProjectData) alfrescoRepository.save(projectData);
				NodeRef projectNodeRef = projectData.getNodeRef();
				projectService.formulate(projectNodeRef);				
				
				assertNotNull(projectNodeRef);	
				
				projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);	
				 		
				assertEquals(projectData.getTaskList().get(0).getResources().get(0),userOne);
				assertEquals(projectData.getTaskList().get(1).getResources().get(0),userOne);
				
				return null;
			}
		}, false, true);
	}

}