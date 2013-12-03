package fr.becpg.repo.entity;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.common.BeCPGException;


/**
 * Entity service
 * @author querephi
 *
 */
public interface EntityService {

	    
	/**
	 * Gets the image defined in the image folder of the entity
	 *
	 * @param nodeRef the node ref
	 * @param imgName the img name
	 * @return the  image
	 * @throws BeCPGException 
	 */
	 NodeRef getImage(NodeRef nodeRef, String imgName) throws BeCPGException;
	
	
	/**
	 * Gets the images defined in the image folder of the entity
	 * @param nodeRef
	 * @return
	 */
	 List<NodeRef> getImages(NodeRef nodeRef) throws BeCPGException;
	
	
	/**
	 * Get default image for entity
	 * @param sourceNodeRef
	 * @return
	 * @throws BeCPGException
	 */
	 NodeRef getEntityDefaultImage(NodeRef sourceNodeRef) throws BeCPGException;

	
	/**
	 * 
	 * @param imgNodeRef
	 * @return
	 */
	 byte[] getImage(NodeRef imgNodeRef);
	 
	/**
	 * Create or copy an entity
	 * @param sourceNodeRef
	 * @param parentNodeRef
	 * @param entityType
	 * @param entityName
	 * @return
	 */
	NodeRef createOrCopyFrom(NodeRef sourceNodeRef, NodeRef parentNodeRef, QName entityType, String entityName);

	/**
	 * Write image in the image folder of the entity 
	 * @param images
	 * @throws BeCPGException 
	 */
	 void writeImages(NodeRef nodeRef, Map<String, byte[]> images) throws BeCPGException;

	 /**
	  * @param type
	  * @return true if entity type can have image
	  */
	 boolean hasAssociatedImages(QName type);

	 void copyFiles(NodeRef sourceNodeRef, NodeRef targetNodeRef); 
    
	 void moveFiles(NodeRef sourceNodeRef, NodeRef targetNodeRef);
	 
	 /**
	 * delete files
	 * @param nodeRef
	 * @param deleteArchivedNode (delete from trash)
	 */
	 void deleteFiles(NodeRef entityNodeRef, boolean deleteArchivedNodes);
	 
	 /**
	 * delete dataLists
	 * @param nodeRef
	 * @param deleteArchivedNode (delete from trash)
	 */
	 void deleteDataLists(NodeRef entityNodeRef, boolean deleteArchivedNodes);
	 

	    /**
	     * Classify by hierarchy.
	     *
	     * @param containerNodeRef the container node ref
	     * @param entityNodeRef the entity node ref
	     */
	 void classifyByHierarchy(NodeRef containerNodeRef, NodeRef entityNodeRef);
}
