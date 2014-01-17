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

import fr.becpg.test.PLMBaseTestCase;

public abstract class AbstractWorkflowTest extends PLMBaseTestCase {

	private static Log logger = LogFactory.getLog(AbstractWorkflowTest.class);


	@Resource(name = "WorkflowService")
	protected WorkflowService workflowService;



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
				logger.error("No task "+taskName+" found");
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
