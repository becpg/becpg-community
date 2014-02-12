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
		
	public NodeRef createVersionAndCheckin(NodeRef origNodeRef, NodeRef workingCopyNodeRef, Map<String,Serializable> versionProperties);
	
	public NodeRef getEntityVersion(Version version);
	
	public void deleteVersionHistory(NodeRef entityNodeRef);

	public NodeRef getEntitiesHistoryFolder();
	
	public List<EntityVersion> getAllVersions(NodeRef entityNodeRef);
	
	public NodeRef getVersionHistoryNodeRef(NodeRef entityNodeRef);
	
	public NodeRef createVersion(NodeRef nodeRef, Map<String,Serializable> versionProperties);
	
	public List<NodeRef> buildVersionHistory(NodeRef versionHistoryRef, NodeRef nodeRef);

	public NodeRef checkOutDataListAndFiles(NodeRef origNodeRef, NodeRef workingCopyNodeRef);

	public void cancelCheckOut(NodeRef origNodeRef, NodeRef workingCopyNodeRef);
	
	public List<EntityVersion> getAllVersionAndBranches(NodeRef entityNodeRef);
	
	public List<NodeRef> getAllVersionBranches(NodeRef entityNodeRef);

	
}