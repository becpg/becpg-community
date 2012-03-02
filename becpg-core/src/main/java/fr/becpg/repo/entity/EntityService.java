package fr.becpg.repo.entity;

import java.io.InputStream;
import java.io.OutputStream;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.common.BeCPGException;
import fr.becpg.model.ExportFormat;
import fr.becpg.repo.entity.remote.EntityProviderCallBack;


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
	 * 
	 * @param imgNodeRef
	 * @return
	 */
	public byte[] getImage(NodeRef imgNodeRef);
	
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
	 * @param sourceNodeRef
	 * @param parentNodeRef
	 * @param entityType
	 * @param entityName
	 * @return
	 */
	public NodeRef createOrCopyFrom(NodeRef sourceNodeRef, NodeRef parentNodeRef, QName entityType, String entityName);

	
	
	/**
	 * Export entity to specified format
	 * @param entityNodeRef
	 * @param outputStream
	 * @throws BeCPGException 
	 */
	public void exportEntity(NodeRef entityNodeRef, OutputStream out, ExportFormat format) throws BeCPGException;


	/**
	 * 
	 * @param entityNodeRef
	 * @param in
	 * @param format
	 * @param callback
	 * @return
	 * @throws BeCPGException
	 */
	public NodeRef createOrUpdateEntity(NodeRef entityNodeRef, InputStream in, ExportFormat format, EntityProviderCallBack callback) throws BeCPGException;
    
}
