package fr.becpg.repo.entity.remote;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.common.BeCPGException;

public interface RemoteEntityService {

	

	/**
	 * Get entity at provided format
	 * @param entityNodeRef
	 * @param result
	 * @param format
	 * @throws BeCPGException
	 */
	public void getEntity(NodeRef entityNodeRef, OutputStream result, RemoteEntityFormat format) throws BeCPGException;


	/**
	 * create or update entity form corresponding format
	 * @param entityNodeRef
	 * @param in
	 * @param format
	 * @param callback
	 * @return Create entity nodeRef
	 * @throws BeCPGException
	 */
	public NodeRef createOrUpdateEntity(NodeRef entityNodeRef, InputStream in, RemoteEntityFormat format, EntityProviderCallBack callback) throws BeCPGException;
	
	
	/**
	 * List entities at format
	 * @param entities
	 * @param out
	 * @param format
	 */
	public void listEntities(List<NodeRef> entities, OutputStream result, RemoteEntityFormat format) throws BeCPGException;

	/**
	 * Return entity data
	 * @param entityNodeRef
	 * @param outputStream
	 * @param format
	 */
	public void getEntityData(NodeRef entityNodeRef, OutputStream outputStream, RemoteEntityFormat format) throws BeCPGException;

	/**
	 * 
	 * @param entityNodeRef
	 * @param inputStream
	 * @param format
	 */
	public void addOrUpdateEntityData(NodeRef entityNodeRef, InputStream inputStream, RemoteEntityFormat format) throws BeCPGException;
	
	
}
