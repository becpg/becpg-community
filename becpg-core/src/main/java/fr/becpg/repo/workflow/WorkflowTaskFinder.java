package fr.becpg.repo.workflow;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Answers whether a user holds an in-progress task on a given set of workflows.
 *
 * <p>Assigned tasks are asked of Activiti workflow by workflow: {@link WorkflowTaskQuery} accepts
 * both {@code processId} and {@code actorId}, so the returned tasks belong to the right workflow by
 * construction and nothing has to be filtered in memory. Scanning every task of the account and
 * reading {@code task.getPath().getInstance().getId()} on each would lazily load the path and the
 * workflow instance once per task, for a cost driven by the size of the user's task list rather
 * than by the one or two workflows actually asked about.</p>
 *
 * <p>Pooled tasks cannot be expressed that way — {@link WorkflowTaskQuery} has no setting for
 * candidate groups — so they are scanned, but only as a fallback once the bounded query came back
 * empty.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("workflowTaskFinder")
public class WorkflowTaskFinder {

	@Autowired
	@Qualifier("WorkflowService")
	private WorkflowService workflowService;

	/**
	 * Whether the user holds an in-progress task on one of these workflows.
	 *
	 * <p>Assigned tasks are queried first; pooled tasks are only scanned when the query found
	 * nothing.</p>
	 *
	 * @param actorId the user, as Activiti knows it
	 * @param workflowIds the workflow instances carried by the content
	 * @param accept an extra filter on the task, or {@code null} to accept them all. It is only
	 *        evaluated on the tasks already retained, so a costly predicate stays cheap.
	 * @return true on the first matching task
	 */
	public boolean hasTaskOn(String actorId, Collection<String> workflowIds, Predicate<WorkflowTask> accept) {
		if ((actorId == null) || (workflowIds == null) || workflowIds.isEmpty()) {
			return false;
		}

		Predicate<WorkflowTask> filter = (accept != null) ? accept : task -> true;

		for (String workflowId : workflowIds) {
			WorkflowTaskQuery query = new WorkflowTaskQuery();
			query.setProcessId(workflowId);
			query.setActorId(actorId);
			query.setTaskState(WorkflowTaskState.IN_PROGRESS);
			query.setActive(Boolean.TRUE);

			for (WorkflowTask task : workflowService.queryTasks(query)) {
				if (filter.test(task)) {
					return true;
				}
			}
		}

		return hasPooledTaskOn(actorId, workflowIds, filter);
	}

	/**
	 * The fallback: pooled tasks, for want of an API to query them by workflow.
	 *
	 * <p>Filtering on the instance is still needed here, with its lazy loading, but it is only paid
	 * by the accounts holding no assigned task on these workflows.</p>
	 *
	 * @param actorId the user
	 * @param workflowIds the workflow instances looked for
	 * @param filter an extra filter, never {@code null}
	 * @return true on the first matching task
	 */
	private boolean hasPooledTaskOn(String actorId, Collection<String> workflowIds, Predicate<WorkflowTask> filter) {
		List<WorkflowTask> pooledTasks = workflowService.getPooledTasks(actorId);
		for (WorkflowTask task : pooledTasks) {
			if (workflowIds.contains(task.getPath().getInstance().getId()) && filter.test(task)) {
				return true;
			}
		}
		return false;
	}

}
