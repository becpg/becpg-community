package fr.becpg.repo.project;

import java.util.List;

import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;

/**
 * Class used to manage workflow
 * @author quere
 *
 */
public interface ProjectWorkflowService {

	public boolean isWorkflowActive(TaskListDataItem task);
	public void cancelWorkflow(TaskListDataItem task);
	public void startWorkflow(ProjectData projectData, TaskListDataItem taskListDataItem,
			List<DeliverableListDataItem> nextDeliverables);
}
