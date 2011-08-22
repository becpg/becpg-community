/*
 * 
 */
package fr.becpg.repo.product.version;

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
public interface ProductVersionService {			
		
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
	 * @param productNodeRef the product node ref
	 * @return the version history
	 */
	public List<NodeRef> getVersionHistory(NodeRef productNodeRef);
	
	/**
	 * Gets the version history with properties.
	 *
	 * @param productNodeRef the product node ref
	 * @return the version history with properties
	 */
	public List<VersionData> getVersionHistoryWithProperties(NodeRef productNodeRef);
}
