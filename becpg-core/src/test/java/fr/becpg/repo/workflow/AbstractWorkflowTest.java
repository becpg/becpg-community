package fr.becpg.repo.workflow;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.subethamail.wiser.Wiser;

import fr.becpg.test.RepoBaseTestCase;

public abstract class AbstractWorkflowTest extends RepoBaseTestCase {

	private static Log logger = LogFactory.getLog(AbstractWorkflowTest.class);

	protected Wiser wiser = new Wiser(2500);

	@Resource(name = "WorkflowService")
	protected WorkflowService workflowService;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		// First start wiser
		try {
			wiser.start();
		} catch (Exception e) {
			logger.warn("cannot open wiser!");
		}

	}

	@Override
	@After
	public void tearDown() throws Exception {

		super.tearDown();
		try {
			wiser.stop();
		} catch (Exception e) {
			logger.warn("cannot stop wiser!");
		}

	}

	protected WorkflowTask getNextTaskForWorkflow(String workflowInstanceId) {
		WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
		taskQuery.setProcessId(workflowInstanceId);
		taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);

		List<WorkflowTask> workflowTasks = workflowService.queryTasks(taskQuery, false);
		assertEquals(1, workflowTasks.size());
		return workflowTasks.get(0);
	}

	protected WorkflowTask submitTask(final String workflowInstanceId,final String taskName,final String transitionName,final Map<QName, Serializable> properties) {

		return submitTask(workflowInstanceId, taskName, transitionName, properties,new HashMap<QName, List<NodeRef>>());

	}
	
	protected WorkflowTask submitTask(final String workflowInstanceId,final String taskName,final String transitionName,final Map<QName, Serializable> properties,final Map<QName, List<NodeRef>> assocs) {

		return transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<WorkflowTask>() {
			public WorkflowTask execute() throws Throwable {

				WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
				taskQuery.setProcessId(workflowInstanceId);
				taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);

				
				List<WorkflowTask> workflowTasks = workflowService.queryTasks(taskQuery, false);

				for (WorkflowTask task : workflowTasks) {
					logger.info("Active task Name " + task.getName());
					if (taskName.equals(task.getName())) {
						logger.info(" --- submit task " + task.getName());
						workflowService.updateTask(task.getId(), properties, assocs, new HashMap<QName, List<NodeRef>>());
						task = workflowService.endTask(task.getId(), transitionName);

						return task;
					}
				}
				return null;

			}
		}, false, true);

	}
	

	protected void printInProgressTasks(String workflowInstanceId) {
		WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
		taskQuery.setProcessId(workflowInstanceId);
		taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);

		List<WorkflowTask> workflowTasks = workflowService.queryTasks(taskQuery, false);

		for (WorkflowTask task : workflowTasks) {

			logger.info("iter task " + task.getName() + " - " + task.getState());
		}
	}
	
	protected void assertNotTask(String workflowInstanceId, String taskName) {
		WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
		taskQuery.setProcessId(workflowInstanceId);
		taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);

		List<WorkflowTask> workflowTasks = workflowService.queryTasks(taskQuery, false);

		for (WorkflowTask task : workflowTasks) {

			logger.info("iter task " + task.getName() + " - " + task.getState());
			if (taskName.equals(task.getName())) {
				assertFalse(true);
			}
		}
	}

}
