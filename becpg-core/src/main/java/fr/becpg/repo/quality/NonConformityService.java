package fr.becpg.repo.quality;

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

	public void classifyNC(NodeRef ncNodeRef, NodeRef productNodeRef);
}
