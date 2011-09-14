package fr.becpg.repo.entity;

import org.alfresco.service.cmr.repository.NodeRef;

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
	
    
}
