package fr.becpg.repo.project;

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
	public void start(NodeRef projectNodeRef);
	public void submitDeliverable(NodeRef deliverableNodeRef);
	public List<NodeRef> getTaskLegendList();
	public NodeRef getProjectsContainer(String siteId);
}
