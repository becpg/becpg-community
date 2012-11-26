package fr.becpg.repo.project;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Class used to manage a project
 * 
 * @author quere
 * 
 */
public interface ProjectService {

	/**
	 * Open deliverable in progress
	 * 
	 * @param deliverableNodeRef
	 */
	public void openDeliverable(NodeRef deliverableNodeRef);

	/**
	 * Get the task legend list
	 * 
	 * @return
	 */
	public List<NodeRef> getTaskLegendList();

	/**
	 * Get the projects container
	 * 
	 * @param siteId
	 * @return
	 */
	public NodeRef getProjectsContainer(String siteId);

	/**
	 * Cancel a project
	 * 
	 * @param projectNodeRef
	 */
	public void cancel(NodeRef projectNodeRef);

	/**
	 * Formulate a project
	 * 
	 * @param projectNodeRef
	 * @throws ProjectException
	 */
	public void formulate(NodeRef projectNodeRef) throws ProjectException;
}
