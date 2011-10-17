/*
 * 
 */
package fr.becpg.repo.entity.version;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

// TODO: Auto-generated Javadoc
/**
 * The Interface ProductVersionService.
 *
 * @author querephi
 */
public interface EntityVersionService {			
		
	/** The Constant VERSION_DELIMITER. */
	public static final String VERSION_DELIMITER = ".";
	
	/**
	 * Creates the version.
	 *
	 * @param productNodeRef the product node ref
	 * @param properties the properties
	 * @return the node ref
	 */
	public NodeRef createVersion(NodeRef productNodeRef, Map<String, Serializable> properties);	
	
	/**
	 * Gets the version history.
	 *
	 * @param entityNodeRef the entity node ref
	 * @return the version history
	 */
	public List<NodeRef> getVersionHistory(NodeRef entityNodeRef);
	
	/**
	 * Gets the version history with properties.
	 *
	 * @param entityNodeRef the entity node ref
	 * @return the version history with properties
	 */
	public List<VersionData> getVersionHistoryWithProperties(NodeRef entityNodeRef);

	/**
	 * @param entityNodeRef
	 * delete all version history
	 */
	public void deleteVersionHistory(NodeRef entityNodeRef);
}
