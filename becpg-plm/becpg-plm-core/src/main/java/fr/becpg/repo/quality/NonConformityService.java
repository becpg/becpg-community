package fr.becpg.repo.quality;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

public interface NonConformityService {

	/**
	 * Calculate the storage folder (product NC or default)
	 * 
	 * @param productNodeRef
	 *            may be null
	 * @return
	 */
	public NodeRef getStorageFolder(NodeRef productNodeRef);

	@Deprecated
	public void classifyNC(NodeRef ncNodeRef, NodeRef productNodeRef);

	public List<String> getAssociatedWorkflow(NodeRef ncNodeRef);
	
	public void deleteWorkflows(List<String> instanceIds);
}
