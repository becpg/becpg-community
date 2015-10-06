package fr.becpg.test.project.formulation;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
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
				
				Calendar now = Calendar.getInstance();
				
				
				
				//simple delegation
				userThree = BeCPGTestHelper.createUser(USER_THREE);	
				userFour = BeCPGTestHelper.createUser(USER_FOUR);	
				
				nodeService.setProperty(userThree, ProjectModel.PROP_QNAME_DELEGATION_STATE,true);
				nodeService.setProperty(userThree, ProjectModel.PROP_QNAME_DELEGATION_START, now.getTime());
				
				nodeService.setProperty(userThree, ProjectModel.PROP_QNAME_REASSIGN_TASK,true);
				associationService.update(userThree,ProjectModel.PROP_QNAME_REASSIGN_RESOURCE,userOne);		
				
				
				//multiple delegation
				nodeService.setProperty(userFour, ProjectModel.PROP_QNAME_DELEGATION_STATE,true);
				nodeService.setProperty(userFour, ProjectModel.PROP_QNAME_DELEGATION_START,now.getTime());
				nodeService.setProperty(userFour, ProjectModel.PROP_QNAME_REASSIGN_TASK,true);
				associationService.update(userFour,ProjectModel.PROP_QNAME_REASSIGN_RESOURCE,userThree);		
				
				
				now.add(Calendar.MONTH, 1);
				
				nodeService.setProperty(userThree, ProjectModel.PROP_QNAME_DELEGATION_END, now.getTime());
				nodeService.setProperty(userFour, ProjectModel.PROP_QNAME_DELEGATION_END, now.getTime());
			
				
				assigneesThree = new ArrayList<>();
				assigneesThree.add(userThree);	
				
				assigneesFour = new ArrayList<>();
				assigneesFour.add(userFour);
				
				List<TaskListDataItem> taskList = new LinkedList<>();				
				taskList.add(new TaskListDataItem(null, "task5", false, 2, null, assigneesThree, taskLegends.get(1), "activiti$projectAdhoc"));
				taskList.add(new TaskListDataItem(null, "task6", false, 2, null, assigneesFour, taskLegends.get(1), "activiti$projectAdhoc"));
				projectData.setTaskList(taskList);
				
				now = Calendar.getInstance();
				now.add(Calendar.DAY_OF_MONTH, -1);
				
				projectData.getTaskList().get(0).setStart(now.getTime());
				projectData.getTaskList().get(1).setStart(now.getTime());
				
				now.add(Calendar.MONTH, 6);
				
				projectData.getTaskList().get(0).setEnd(now.getTime());
				projectData.getTaskList().get(1).setEnd(now.getTime());
				
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