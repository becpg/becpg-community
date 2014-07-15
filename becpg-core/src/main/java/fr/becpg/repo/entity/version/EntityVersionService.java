/*
 * 
 */
package fr.becpg.repo.entity.version;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;

/**
 * The Interface EntityVersionService.
 *
 * @author querephi
 */
public interface EntityVersionService {			
	
	/*
	 * Private method 
	 */
	NodeRef createVersionAndCheckin(NodeRef origNodeRef, NodeRef workingCopyNodeRef, Map<String,Serializable> versionProperties);
	NodeRef doCheckOut(NodeRef origNodeRef, NodeRef workingCopyNodeRef);
	void cancelCheckOut(NodeRef origNodeRef, NodeRef workingCopyNodeRef);
	
	public NodeRef getEntityVersion(Version version);
	
	public void deleteVersionHistory(NodeRef entityNodeRef);

	public NodeRef getEntitiesHistoryFolder();
	
	public List<EntityVersion> getAllVersions(NodeRef entityNodeRef);
	
	public NodeRef getVersionHistoryNodeRef(NodeRef entityNodeRef);
	
	public List<NodeRef> buildVersionHistory(NodeRef versionHistoryRef, NodeRef nodeRef);
	
	public List<EntityVersion> getAllVersionAndBranches(NodeRef entityNodeRef);
	
	public List<NodeRef> getAllVersionBranches(NodeRef entityNodeRef);
	
	public NodeRef createVersion(NodeRef entityNodeRef, Map<String, Serializable> versionProperties);


	
}