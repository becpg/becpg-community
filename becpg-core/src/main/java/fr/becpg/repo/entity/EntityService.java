package fr.becpg.repo.entity;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;


/**
 * Entity service
 * @author querephi
 *
 */
public interface EntityService {

	/**
	 * Check if the datalists have been modified after the modification of the entity
	 * @return
	 */
	public boolean hasDataListModified(NodeRef nodeRef);
	
	/**
     * Initialize entity folder.
     *
     * @param entityNodeRef the entity node ref
     */
    public void initializeEntityFolder(NodeRef entityNodeRef);
    
    
	/**
	 * Gets the image defined in the image folder of the entity
	 *
	 * @param nodeRef the node ref
	 * @param imgName the img name
	 * @return the  image
	 */
	public NodeRef getImage(NodeRef nodeRef, String imgName);

	
	/**
	 * Initialyze default param on 
	 * 
	 * @param entityNodeRef
	 */
	public void initializeEntity(NodeRef entityNodeRef);

	/**
	 * Delete all version of the entity
	 * @param entityNodeRef
	 */
	public void deleteEntity(NodeRef entityNodeRef);

	/**
	 * Create or copy an entity
	 * @param parentNodeRef
	 * @param sourceNodeRef
	 * @param entityType
	 * @param entityName
	 * @return
	 */
	public NodeRef createOrCopyFrom(NodeRef parentNodeRef, NodeRef sourceNodeRef, QName entityType, String entityName);
	
    
}
