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
package fr.becpg.repo.entity;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.formulation.FormulateException;


/**
 * <p>EntityTplService interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface EntityTplService {

	/**
	 * <p>createEntityTpl.</p>
	 *
	 * @param parentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param entityType a {@link org.alfresco.service.namespace.QName} object.
	 * @param entityTplName a {@link java.lang.String} object.
	 * @param enabled a boolean.
	 * @param isDefault a boolean.
	 * @param entityLists a {@link java.util.Set} object.
	 * @param subFolders a {@link java.util.Set} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef createEntityTpl(NodeRef parentNodeRef, QName entityType, String entityTplName,  boolean enabled, boolean isDefault, Set<QName> entityLists, Set<String> subFolders);
	
	/**
	 * <p>getEntityTpl.</p>
	 *
	 * @param nodeType a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getEntityTpl(QName nodeType);
	
	/**
	 * <p>createWUsedList.</p>
	 *
	 * @param entityTplNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param typeQName a {@link org.alfresco.service.namespace.QName} object.
	 * @param assocQName a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef createWUsedList(NodeRef entityTplNodeRef, QName typeQName, QName assocQName);
	
	/**
	 * <p>synchronizeEntities.</p>
	 *
	 * @param tplNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link fr.becpg.repo.batch.BatchInfo} object
	 */
	BatchInfo synchronizeEntities(NodeRef tplNodeRef);
	
	/**
	 * <p>formulateEntities.</p>
	 *
	 * @param tplNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @throws fr.becpg.repo.formulation.FormulateException if any.
	 * @return a {@link fr.becpg.repo.batch.BatchInfo} object
	 */
	BatchInfo formulateEntities(NodeRef tplNodeRef) throws FormulateException;

	/**
	 * <p>synchronizeEntity.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param entityTplNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void synchronizeEntity(NodeRef entityNodeRef, NodeRef entityTplNodeRef);

	/**
	 * <p>createView.</p>
	 *
	 * @param entityTplNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param typeQName a {@link org.alfresco.service.namespace.QName} object.
	 * @param name a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef createView(NodeRef entityTplNodeRef, QName typeQName, String name);
	
	/**
	 * <p>removeDataListOnEntities.</p>
	 *
	 * @param entityTplNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param entityListName a {@link java.lang.String} object.
	 */
	void removeDataListOnEntities(NodeRef entityTplNodeRef, String entityListName);

	/**
	 * <p>createOrUpdateList.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param type a {@link org.alfresco.service.namespace.QName} object
	 */
	NodeRef createOrUpdateList(NodeRef entityNodeRef, QName type);
	
}
