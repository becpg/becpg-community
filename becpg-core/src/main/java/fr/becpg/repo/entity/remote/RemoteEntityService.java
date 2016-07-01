/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG. 
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
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.common.BeCPGException;

public interface RemoteEntityService {

	/* Constants */
	String CHILD_ASSOC_TYPE = "childAssoc";
	String ASSOC_TYPE = "assoc";
	String NODEREF_TYPE = "d:noderef";
	String CATEGORY_TYPE = "d:category";
	String NODE_TYPE = "node";
	String ATTR_TYPE = "type";
	String ATTR_PATH = "path";
	String ATTR_NAME = "name";
	String ATTR_NODEREF = "nodeRef";
	String ATTR_CODE = "code";
	String ELEM_ENTITIES = "entities";
	String ELEM_DATA = "data";
	String ELEM_LIST = "values";
	String ELEM_LIST_VALUE = "value";

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
	NodeRef createOrUpdateEntity(NodeRef entityNodeRef, NodeRef destNodeRef, Map<QName, Serializable> properties, InputStream in,
			RemoteEntityFormat format, EntityProviderCallBack callback) throws BeCPGException;

	/**
	 * List entities at format
	 * 
	 * @param entities
	 * @param out
	 * @param format
	 */
	void listEntities(List<NodeRef> entities, OutputStream result, RemoteEntityFormat format) throws BeCPGException;

	/**
	 * Return entity data
	 * 
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

}
