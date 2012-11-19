package fr.becpg.repo.project;

import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Class used to manage a project
 * @author quere
 *
 */
public interface ProjectService {

	/**
	 * Submit the task as done, create next task and next deliverables if ready
	 * @param projectNodeRef
	 * @param taskNodeRef
	 */
	public void submitTask(NodeRef taskNodeRef);
	
	/**
	 * Start a project
	 * @param projectNodeRef
	 */
	public void start(NodeRef projectNodeRef);
	
	/**
	 * Submit a deliverable when it's completed
	 * @param deliverableNodeRef
	 */
	public void submitDeliverable(NodeRef deliverableNodeRef);
	
	/**
	 * Open deliverable in progress
	 * @param deliverableNodeRef
	 */
	public void openDeliverable(NodeRef deliverableNodeRef);
	
	/**
	 * Get the task legend list
	 * @return
	 */
	public List<NodeRef> getTaskLegendList();
	
	/**
	 * Get the projects container
	 * @param siteId
	 * @return
	 */
	public NodeRef getProjectsContainer(String siteId);
	
	/**
	 * Re-calculate the dates of the tasks
	 * @param taskNodeRef
	 */
	public void calculateTaskDates(NodeRef taskListNodeRef);
	
	/**
	 * Initialize project dates
	 * @param projectNodeRef
	 */
	public void initializeProjectDates(NodeRef projectNodeRef);
	
	/**
	 * Calculate the end date
	 * @param startDate
	 * @param duration
	 * @return
	 */
	public Date calculateEndDate(Date startDate, Integer duration);
	
	/**
	 * Calculate the start date of the next task
	 * @param startDate
	 * @param duration
	 * @return
	 */
	public Date calculateNextStartDate(Date endDate);
	
	/**
	 * Calculate the duration of a task
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public int calculateTaskDuration(Date startDate, Date endDate);
}
