/*
 * 
 */
package fr.becpg.repo.entity.version;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.springframework.stereotype.Service;

/**
 * The Interface EntityVersionService.
 *
 * @author querephi
 */
@Service
public interface EntityVersionService {			
		
	/** The Constant VERSION_DELIMITER. */
	public static final String VERSION_DELIMITER = ".";
	
	/**
	 * Creates the entity version with datalists and checkin
	 *
	 */
	public NodeRef createVersionAndCheckin(NodeRef origNodeRef, NodeRef workingCopyNodeRef);
	
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
	
}