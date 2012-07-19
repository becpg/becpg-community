package fr.becpg.repo.entity.remote;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.stereotype.Service;

import fr.becpg.common.BeCPGException;

@Service
public interface RemoteEntityService {

	/* Constants */
	static final String CHILD_ASSOC_TYPE = "childAssoc";
	static final String ASSOC_TYPE = "assoc";
	static final String NODEREF_TYPE = "d:noderef";
	static final String NODE_TYPE = "node";
	static final String ATTR_TYPE = "type";
	static final String ATTR_PATH = "path";
	static final String ATTR_NAME = "name";
	static final String ATTR_NODEREF = "nodeRef";
	static final String ATTR_CODE = "code";
	static final String ELEM_ENTITIES = "entities";
	static final String ELEM_DATA = "data";
	static final String ELEM_IMAGE = "image";

	/**
	 * Get entity at provided format
	 * @param entityNodeRef
	 * @param result
	 * @param format
	 * @throws BeCPGException
	 */
	void getEntity(NodeRef entityNodeRef, OutputStream result, RemoteEntityFormat format) throws BeCPGException;


	/**
	 * create or update entity form corresponding format
	 * @param entityNodeRef
	 * @param in
	 * @param format
	 * @param callback
	 * @return Create entity nodeRef
	 * @throws BeCPGException
	 */
	NodeRef createOrUpdateEntity(NodeRef entityNodeRef, InputStream in, RemoteEntityFormat format, EntityProviderCallBack callback) throws BeCPGException;
	
	
	/**
	 * List entities at format
	 * @param entities
	 * @param out
	 * @param format
	 */
	void listEntities(List<NodeRef> entities, OutputStream result, RemoteEntityFormat format) throws BeCPGException;

	/**
	 * Return entity data
	 * @param entityNodeRef
	 * @param outputStream
	 * @param format
	 */
	void getEntityData(NodeRef entityNodeRef, OutputStream outputStream, RemoteEntityFormat format) throws BeCPGException;

	/**
	 * 
	 * @param entityNodeRef
	 * @param inputStream
	 * @param format
	 */
	void addOrUpdateEntityData(NodeRef entityNodeRef, InputStream inputStream, RemoteEntityFormat format) throws BeCPGException;


	/**
	 * Is entity has templateFolder
	 * @param entityNodeRef
	 * @return
	 */
	boolean containsData(NodeRef entityNodeRef);
	
	
}
