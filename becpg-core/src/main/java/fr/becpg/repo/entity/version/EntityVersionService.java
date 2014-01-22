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
		
	/** The Constant VERSION_DELIMITER. */
	public static final String VERSION_DELIMITER = ".";
	
	/**
	 * Creates the entity version with datalists and checkin
	 *
	 */
	public NodeRef createVersionAndCheckin(NodeRef origNodeRef, NodeRef workingCopyNodeRef, Map<String,Serializable> versionProperties);
	
	/**
	 * Get the entity version with datalists
	 * @param version
	 * @return
	 */
	public NodeRef getEntityVersion(Version version);
	
	/**
	 * @param entityNodeRef
	 * delete all version history
	 */
	public void deleteVersionHistory(NodeRef entityNodeRef);
	
	/**
	 * Get the entitiesHistoryFolder	
	 * @return
	 */
	public NodeRef getEntitiesHistoryFolder();
	
	/**
	 * Gets the version history with properties.
	 *
	 * @param entityNodeRef the entity node ref
	 * @return the version history with properties
	 */
	public List<EntityVersion> getAllVersions(NodeRef entityNodeRef);
	
	public NodeRef getVersionHistoryNodeRef(NodeRef entityNodeRef);
	
	public NodeRef createVersion(NodeRef nodeRef, Map<String,Serializable> versionProperties);
	
	public List<NodeRef> buildVersionHistory(NodeRef versionHistoryRef, NodeRef nodeRef);

	public NodeRef checkOutDataListAndFiles(NodeRef origNodeRef, NodeRef workingCopyNodeRef);

	public void cancelCheckOut(NodeRef origNodeRef, NodeRef workingCopyNodeRef);
	
}