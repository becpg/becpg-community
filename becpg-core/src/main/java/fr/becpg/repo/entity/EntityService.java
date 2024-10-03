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

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.EntityListState;

/**
 * Entity service
 *
 * @author querephi
 * @version $Id: $Id
 */
public interface EntityService {

	/**
	 * <p>getImage.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param imgName a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getImage(NodeRef entityNodeRef, String imgName) ;
	
	/**
	 * <p>getImages.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link java.util.List} object.
	 */
	List<NodeRef> getImages(NodeRef entityNodeRef) ;

	/**
	 * <p>getEntityDefaultImage.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getEntityDefaultImage(NodeRef entityNodeRef) ;

	/**
	 * <p>writeImages.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param images a {@link java.util.Map} object.
	 */
	void writeImages(NodeRef entityNodeRef, Map<String, byte[]> images) ;
	
	/**
	 * <p>getImageFolder.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getImageFolder(NodeRef entityNodeRef) ;
	
	/**
	 * <p>getOrCreateImageFolder.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	NodeRef getOrCreateImageFolder(NodeRef entityNodeRef) ;

	/**
	 * <p>getImage.</p>
	 *
	 * @param imgNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return an array of {@link byte} objects.
	 */
	byte[] getImage(NodeRef imgNodeRef);

	/**
	 * <p>getDefaultImageName.</p>
	 *
	 * @param entityTypeQName a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link java.lang.String} object.
	 */
	String getDefaultImageName(QName entityTypeQName);

	/**
	 * <p>hasAssociatedImages.</p>
	 *
	 * @param type a {@link org.alfresco.service.namespace.QName} object.
	 * @return a boolean.
	 */
	boolean hasAssociatedImages(QName type);

	/**
	 * <p>createOrCopyFrom.</p>
	 *
	 * @param sourceNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param parentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param entityType a {@link org.alfresco.service.namespace.QName} object.
	 * @param entityName a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef createOrCopyFrom(NodeRef sourceNodeRef, NodeRef parentNodeRef, QName entityType, String entityName);

	/**
	 * <p>copyFiles.</p>
	 *
	 * @param sourceNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param targetNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void copyFiles(NodeRef sourceNodeRef, NodeRef targetNodeRef);

	/**
	 * <p>moveFiles.</p>
	 *
	 * @param sourceNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param targetNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void moveFiles(NodeRef sourceNodeRef, NodeRef targetNodeRef);

	/**
	 * <p>deleteFiles.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param deleteArchivedNodes a boolean.
	 */
	void deleteFiles(NodeRef entityNodeRef, boolean deleteArchivedNodes);

	/**
	 * <p>deleteDataLists.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param deleteArchivedNodes a boolean.
	 */
	void deleteDataLists(NodeRef entityNodeRef, boolean deleteArchivedNodes);

	/**
	 * <p>getOrCreateDocumentsFolder.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getOrCreateDocumentsFolder(NodeRef entityNodeRef);

	/**
	 * <p>createDefaultImage.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef createDefaultImage(NodeRef nodeRef);

	/**
	 * <p>getEntityNodeRef.</p>
	 *
	 * @param childNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param itemType a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getEntityNodeRef(NodeRef childNodeRef, QName itemType);

	/**
	 * <p>changeEntityListStates.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param state a {@link fr.becpg.model.EntityListState} object.
	 * @return a boolean.
	 */
	boolean changeEntityListStates(NodeRef entityNodeRef, EntityListState state);

	/**
	 * <p>getDocumentsFolder.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param create a boolean.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getDocumentsFolder(NodeRef entityNodeRef, boolean create);

	/**
	 * <p>getEntityDefaultIcon.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param resolution a {@link java.lang.String} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	NodeRef getEntityDefaultIcon(NodeRef nodeRef, String resolution);

	/**
	 * <p>getEntityIcons.</p>
	 *
	 * @return a {@link java.util.Map} object
	 */
	Map<String, NodeRef> getEntityIcons();

}
