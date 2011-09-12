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
    
}
