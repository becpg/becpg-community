/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.entity.remote;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.common.BeCPGException;

public interface RemoteEntityService {

	/* Constants */
	String CHILD_ASSOC_TYPE = "childAssoc";
	String ASSOC_TYPE = "assoc";
	String NODEREF_TYPE = "d:noderef";
	String CATEGORY_TYPE = "d:category";
	String NODE_TYPE = "node";
	String MLTEXT_TYPE = "d:mltext";
	String ATTR_TYPE = "type";
	String ATTR_PATH = "path";
	String ATTR_NAME = "name";
	String ATTR_SITE = "site";
	String ATTR_NODEREF = "nodeRef";
	String ATTR_CODE = "code";
	String ATTR_ID = "id";
	
	
	String CHARACT_ATTR_PATH = "charactPath";
	String CHARACT_ATTR_NAME = "charactName";
	String CHARACT_ATTR_NODEREF = "charactNodeRef";
	String CHARACT_ATTR_CODE = "charactCode";
	String CHARACT_ATTR_ERP_CODE = "charactErpCode";
	
	String ATTR_ERP_CODE = "erpCode";
	String ELEM_ENTITIES = "entities";
	String ELEM_DATA = "data";
	String ELEM_LIST = "values";
	String ELEM_LIST_VALUE = "value";
	String ELEM_DATALISTS = "datalists";
	String ELEM_PROPERTIES = "properties";
	String ELEM_ASSOCIATIONS = "associations";
	String ELEM_ENTITY = "entity";
	String ELEM_ATTRIBUTES = "attributes";
	String FULL_PATH_IMPORT_TO_DO = "/app:company_home/cm:Exchange/cm:Import/cm:ImportToDo";
	String EMPTY_NAME_PREFIX = "REMOTE-";
	
	String ATTR_PARENT_ID = "parent";

	/**
	 * Get entity at provided format
	 * 
	 * @param entityNodeRef
	 * @param result
	 * @param format
	 * @throws BeCPGException
	 */
	void getEntity(NodeRef entityNodeRef, OutputStream result, RemoteEntityFormat format) throws BeCPGException;

	/**
	 * create or update entity form corresponding format
	 * 
	 * @param entityNodeRef
	 * @param in
	 * @param format
	 * @param callback
	 * @return Create entity nodeRef
	 * @throws BeCPGException
	 */
	NodeRef createOrUpdateEntity(NodeRef entityNodeRef, InputStream in, RemoteEntityFormat format, EntityProviderCallBack callback)
			throws BeCPGException;

	/**
	 * create or update entity form corresponding format
	 * override properties and set destination
	 * @param entityNodeRef
	 * @param destNodeRef
	 * @param properties
	 * @param in
	 * @param format
	 * @param callback
	 * @return
	 * @throws BeCPGException
	 */
	NodeRef internalCreateOrUpdateEntity(NodeRef entityNodeRef, NodeRef destNodeRef, InputStream in,
			RemoteEntityFormat format, EntityProviderCallBack callback, Map<NodeRef, NodeRef> cache) throws BeCPGException;

	/**
	 * List entities at format
	 * 
	 * @param entities
	 * @param out
	 * @param format
	 */
	void listEntities(List<NodeRef> entities, OutputStream result, RemoteEntityFormat format) throws BeCPGException;

	/**
	 * List entities at format with specific assoc/props
	 * 
	 * @param entities
	 * @param out
	 * @param format
	 * @param fields
	 */
	void listEntities(List<NodeRef> entities, OutputStream result, RemoteEntityFormat format, List<String> fields) throws BeCPGException;
	
	/**
	 * Return entity data
	 * 
	 * @param entityNodeRef
	 * @param outputStream
	 * @param format
	 */
	void getEntityData(NodeRef entityNodeRef, OutputStream outputStream, RemoteEntityFormat format) throws BeCPGException;
	
	/**
	 * Return entity data with specific assoc/prop and lists
	 * 
	 * @param entityNodeRef
	 * @param outputStream
	 * @param format
	 */
	void getEntity(NodeRef entityNodeRef, OutputStream out, RemoteEntityFormat format, List<String> fields, List<String> lists) throws BeCPGException;
	
	/**
	 * 
	 * @param entityNodeRef
	 * @param inputStream
	 * @param format
	 */
	void addOrUpdateEntityData(NodeRef entityNodeRef, InputStream inputStream, RemoteEntityFormat format) throws BeCPGException;

	

}
