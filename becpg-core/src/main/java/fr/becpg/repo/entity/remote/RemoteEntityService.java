/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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

import org.alfresco.query.PagingResults;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;

/**
 * <p>RemoteEntityService interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface RemoteEntityService {

	/** Constant <code>CHILD_ASSOC_TYPE="childAssoc"</code> */
	String CHILD_ASSOC_TYPE = "childAssoc";
	/** Constant <code>ASSOC_TYPE="assoc"</code> */
	String ASSOC_TYPE = "assoc";
	/** Constant <code>NODEREF_TYPE="d:noderef"</code> */
	String NODEREF_TYPE = "d:noderef";
	/** Constant <code>CATEGORY_TYPE="d:category"</code> */
	String CATEGORY_TYPE = "d:category";
	/** Constant <code>NODE_TYPE="node"</code> */
	String NODE_TYPE = "node";
	/** Constant <code>MLTEXT_TYPE="d:mltext"</code> */
	String MLTEXT_TYPE = "d:mltext";
	/** Constant <code>ATTR_TYPE="type"</code> */
	String ATTR_TYPE = "type";
	/** Constant <code>ATTR_PATH="path"</code> */
	String ATTR_PATH = "path";
	/** Constant <code>ATTR_NAME="name"</code> */
	String ATTR_NAME = "name";
	/** Constant <code>ATTR_SITE="site"</code> */
	String ATTR_SITE = "site";
	/** Constant <code>ATTR_NODEREF="nodeRef"</code> */
	String ATTR_NODEREF = "nodeRef";
	/** Constant <code>ATTR_CODE="code"</code> */
	String ATTR_CODE = "code";
	/** Constant <code>ATTR_ID="id"</code> */
	String ATTR_ID = "id";

	String ATTR_CREATE_IN_PATH = "createInPath";

	/** Constant <code>CHARACT_ATTR_PATH="charactPath"</code> */
	String CHARACT_ATTR_PATH = "charactPath";
	/** Constant <code>CHARACT_ATTR_NAME="charactName"</code> */
	String CHARACT_ATTR_NAME = "charactName";
	/** Constant <code>CHARACT_ATTR_NODEREF="charactNodeRef"</code> */
	String CHARACT_ATTR_NODEREF = "charactNodeRef";
	/** Constant <code>CHARACT_ATTR_CODE="charactCode"</code> */
	String CHARACT_ATTR_CODE = "charactCode";
	/** Constant <code>CHARACT_ATTR_ERP_CODE="charactErpCode"</code> */
	String CHARACT_ATTR_ERP_CODE = "charactErpCode";

	/** Constant <code>ATTR_ERP_CODE="erpCode"</code> */
	String ATTR_ERP_CODE = "erpCode";
	/** Constant <code>ELEM_ENTITIES="entities"</code> */
	String ELEM_ENTITIES = "entities";
	/** Constant <code>ELEM_DATA="data"</code> */
	String ELEM_DATA = "data";
	/** Constant <code>ELEM_LIST="values"</code> */
	String ELEM_LIST = "values";
	/** Constant <code>ELEM_LIST_VALUE="value"</code> */
	String ELEM_LIST_VALUE = "value";
	/** Constant <code>ELEM_DATALISTS="datalists"</code> */
	String ELEM_DATALISTS = "datalists";
	/** Constant <code>ELEM_PROPERTIES="properties"</code> */
	String ELEM_PROPERTIES = "properties";
	/** Constant <code>ELEM_ASSOCIATIONS="associations"</code> */
	String ELEM_ASSOCIATIONS = "associations";
	/** Constant <code>ELEM_ENTITY="entity"</code> */
	String ELEM_ENTITY = "entity";
	/** Constant <code>ELEM_ATTRIBUTES="attributes"</code> */
	String ELEM_ATTRIBUTES = "attributes";
	/** Constant <code>ELEM_PARAMS="params"</code> */
	String ELEM_PARAMS = "params";
	/** Constant <code>FULL_PATH_IMPORT_TO_DO="/app:company_home/cm:Exchange/cm:Import"{trunked}</code> */
	String FULL_PATH_IMPORT_TO_DO = "/app:company_home/cm:Exchange/cm:Import/cm:ImportToDo";
	/** Constant <code>EMPTY_NAME_PREFIX="REMOTE-"</code> */
	String EMPTY_NAME_PREFIX = "REMOTE-";

	/** Constant <code>ATTR_PARENT_ID="parent"</code> */
	String ATTR_PARENT_ID = "parent";
	
	String ATTR_VERSION = "version";
	
	String ELEM_CONTENT = "content";

	/**
	 * Get entity at provided format
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param result a {@link java.io.OutputStream} object.
	 * @param format a {@link fr.becpg.repo.entity.remote.RemoteEntityFormat} object.
	 * @throws fr.becpg.common.BeCPGException if any.
	 */
	void getEntity(NodeRef entityNodeRef, OutputStream result, RemoteParams params);

	/**
	 * create or update entity form corresponding format
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param in a {@link java.io.InputStream} object.
	 * @param format a {@link fr.becpg.repo.entity.remote.RemoteEntityFormat} object.
	 * @param callback a {@link fr.becpg.repo.entity.remote.EntityProviderCallBack} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @throws fr.becpg.common.BeCPGException if any.
	 */
	NodeRef createOrUpdateEntity(NodeRef entityNodeRef, InputStream in, RemoteParams params, EntityProviderCallBack callback);

	/**
	 * create or update entity form corresponding format override properties and
	 * set destination
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param destNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param in a {@link java.io.InputStream} object.
	 * @param format a {@link fr.becpg.repo.entity.remote.RemoteEntityFormat} object.
	 * @param callback a {@link fr.becpg.repo.entity.remote.EntityProviderCallBack} object.
	 * @param cache a {@link java.util.Map} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @throws fr.becpg.common.BeCPGException if any.
	 */
	NodeRef internalCreateOrUpdateEntity(NodeRef entityNodeRef, NodeRef destNodeRef, InputStream in, RemoteParams params,
			EntityProviderCallBack callback, Map<NodeRef, NodeRef> cache);

	/**
	 * List entities at format with specific assoc/props
	 *
	 * @param entities a {@link java.util.List} object.
	 * @param result a {@link java.io.OutputStream} object.
	 * @param format a {@link fr.becpg.repo.entity.remote.RemoteEntityFormat} object.
	 * @param fields a {@link java.util.List} object.
	 * @throws fr.becpg.common.BeCPGException if any.
	 */
	void listEntities(PagingResults<NodeRef> entities, OutputStream result, RemoteParams params);

	/**
	 * Return entity data
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param outputStream a {@link java.io.OutputStream} object.
	 * @param format a {@link fr.becpg.repo.entity.remote.RemoteEntityFormat} object.
	 * @param fields a {@link java.util.List} object.
	 * @throws fr.becpg.common.BeCPGException if any.
	 */
	void getEntityData(NodeRef entityNodeRef, OutputStream outputStream, RemoteParams params);

	/**
	 * <p>addOrUpdateEntityData.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param inputStream a {@link java.io.InputStream} object.
	 * @param format a {@link fr.becpg.repo.entity.remote.RemoteEntityFormat} object.
	 * @throws fr.becpg.common.BeCPGException if any.
	 */
	void addOrUpdateEntityData(NodeRef entityNodeRef, InputStream inputStream, RemoteParams params);
	
	
	/**
	 * Return schema for type
	 * @param type
	 * @param out
	 * @param params
	 */
	void getEntitySchema(QName type, OutputStream out, RemoteParams params);

	/**
	 * Accessors
	 *
	 * @return a {@link org.alfresco.service.transaction.TransactionService} object.
	 */
	@Deprecated
	TransactionService getTransactionService();

	/**
	 * <p>getPolicyBehaviourFilter.</p>
	 *
	 * @return a {@link org.alfresco.repo.policy.BehaviourFilter} object.
	 */
	@Deprecated
	BehaviourFilter getPolicyBehaviourFilter();


}
