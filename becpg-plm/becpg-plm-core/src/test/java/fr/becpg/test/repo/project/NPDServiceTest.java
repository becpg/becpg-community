/*
 * 
 */
package fr.becpg.test.repo.project;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemState;
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

	@Resource
	private PersonService personService;

	private static final String NPDWF_URI = "http://www.bcpg.fr/model/npd-workflow/1.0";
	
	@Override
	protected boolean shouldInit() {
		//Force reinit
		return true;
	}

	@Test
	public void testNPDProjectTask() {

		final NodeRef projectNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				// create project Tpl
				ProjectData projectData = new ProjectData(null, "Pjt", PROJECT_HIERARCHY1_SEA_FOOD_REF, PROJECT_HIERARCHY2_CRUSTACEAN_REF, null,
						null, null, PlanningMode.Planning, null, null, null, 0, null);

				// create datalists
				List<TaskListDataItem> taskList = new LinkedList<TaskListDataItem>();
				taskList.add(new TaskListDataItem(null, "task1", false, 2, null, assigneesOne, taskLegends.get(0), "activiti$projectNewProduct"));
				projectData.setTaskList(taskList);

				projectData.setParentNodeRef(testFolderNodeRef);
				projectData = (ProjectData) alfrescoRepository.save(projectData);
				

				// start
				projectData.setProjectState(ProjectState.InProgress);
				projectData = (ProjectData) alfrescoRepository.save(projectData);

				return projectData.getNodeRef();
			}
		}, false, true);

		String workflowInstance = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<String>() {
			@Override
			public String execute() throws Throwable {

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
				assertEquals(ProjectState.InProgress, projectData.getProjectState());

				logger.info("workflow instance " + projectData.getTaskList().get(0).getWorkflowInstance());
				assertNotNull(projectData.getTaskList().get(0).getWorkflowInstance());

				return projectData.getTaskList().get(0).getWorkflowInstance();
			}
		}, false, true);

		testNPDWorkflow(workflowInstance);

	}

	private void testNPDWorkflow(final String workflowInstanceId) {
		assertNotNull("The workflow instance is null!", workflowInstanceId);

		List<WorkflowPath> paths = workflowService.getWorkflowPaths(workflowInstanceId);
		assertEquals(1, paths.size());
		WorkflowPath path = paths.get(0);

		List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path.getId());
		assertEquals(1, tasks.size());

		final WorkflowTask task1 = tasks.get(0);

		assertEquals("npdwf:newProductTask", task1.getName());

		final WorkflowTask task2 = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<WorkflowTask>() {
			public WorkflowTask execute() throws Throwable {

				logger.info("Set npd task information " + task1.getName());
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
				properties.put(QName.createQName(NPDWF_URI, "npdProductName"), "Test NPD");
				properties.put(QName.createQName(NPDWF_URI, "npdAction"), "createNewProduct");
				java.util.Map<QName, List<NodeRef>> assocs = new HashMap<QName, List<NodeRef>>();

				workflowService.updateTask(task1.getId(), properties, assocs, new HashMap<QName, List<NodeRef>>());

				workflowService.endTask(task1.getId(), null);

				List<WorkflowPath> paths = workflowService.getWorkflowPaths(workflowInstanceId);
				assertEquals(1, paths.size());
				WorkflowPath path = paths.get(0);

				List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path.getId());
				assertEquals(1, tasks.size());

				WorkflowTask task = tasks.get(0);

				workflowService.updateTask(task.getId(), properties, assocs, new HashMap<QName, List<NodeRef>>());
				workflowService.endTask(task.getId(), null);

				paths = workflowService.getWorkflowPaths(workflowInstanceId);
				assertEquals(1, paths.size());
				path = paths.get(0);

				tasks = workflowService.getTasksForWorkflowPath(path.getId());
				assertEquals(1, tasks.size());

				return tasks.get(0);
			}
		}, false, true);

		final NodeRef productNoderef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {
				int count = 0;
				NodeRef ret = null;
				NodeRef pkgNodeRef = workflowService.getWorkflowById(workflowInstanceId).getWorkflowPackage();
				List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(pkgNodeRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS,
						RegexQNamePattern.MATCH_ALL);
				for (ChildAssociationRef childAssoc : childAssocs) {
					if (PLMModel.TYPE_FINISHEDPRODUCT.equals(nodeService.getType(childAssoc.getChildRef()))) {
						count++;
						ret = childAssoc.getChildRef();
						logger.info("NPD created product" + nodeService.getProperty(ret, ContentModel.PROP_NAME) + " "
								+ nodeService.getProperty(ret, PLMModel.PROP_PRODUCT_STATE));
						assertEquals(SystemState.Simulation.toString(), nodeService.getProperty(ret, PLMModel.PROP_PRODUCT_STATE));
					}
				}
				assertEquals(2, count);
				return ret;
			}
		}, false, true);

		final WorkflowTask task3 = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<WorkflowTask>() {
			public WorkflowTask execute() throws Throwable {

				logger.info("Set npd task information " + task2.getName());
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
				properties.put(QName.createQName(NPDWF_URI, "npdAction"), "submitTask");
				java.util.Map<QName, List<NodeRef>> assocs = new HashMap<QName, List<NodeRef>>();
				assocs.put(QName.createQName(NPDWF_URI, "npdSelectedProducts"), Arrays.asList(productNoderef));

				workflowService.updateTask(task2.getId(), properties, assocs, new HashMap<QName, List<NodeRef>>());
				return workflowService.endTask(task2.getId(), null);
			}
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {
				int count = 0;
				NodeRef ret = null;
				NodeRef pkgNodeRef = workflowService.getWorkflowById(workflowInstanceId).getWorkflowPackage();
				List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(pkgNodeRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS,
						RegexQNamePattern.MATCH_ALL);
				for (ChildAssociationRef childAssoc : childAssocs) {
					if (PLMModel.TYPE_FINISHEDPRODUCT.equals(nodeService.getType(childAssoc.getChildRef()))) {
						count++;
						ret = childAssoc.getChildRef();
						logger.info("NPD updated product" + nodeService.getProperty(ret, ContentModel.PROP_NAME) + " "
								+ nodeService.getProperty(ret, PLMModel.PROP_PRODUCT_STATE));
						assertEquals(SystemState.ToValidate.toString(), nodeService.getProperty(ret, PLMModel.PROP_PRODUCT_STATE));
					}
				}
				assertEquals(1, count);
				return ret;
			}
		}, false, true);
	}
}
