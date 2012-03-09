/*
 * 
 */
package fr.becpg.repo.entity.version;

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
	 * Creates the entity version with datalists
	 *
	 * @param entityNodeRef the entity node ref
	 * @param properties the properties
	 * @return the node ref
	 */
	public NodeRef createEntityVersion(NodeRef entityNodeRef, Version version);
	
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
	 * Get the entitysHistoryFolder	
	 * @return
	 */
	public NodeRef getEntitysHistoryFolder();
}