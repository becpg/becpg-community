/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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

import fr.becpg.common.BeCPGException;

/**
 * Entity service
 * 
 * @author querephi
 * 
 */
public interface EntityService {

	NodeRef getImage(NodeRef entityNodeRef, String imgName) throws BeCPGException;

	List<NodeRef> getImages(NodeRef entityNodeRef) throws BeCPGException;

	NodeRef getEntityDefaultImage(NodeRef entityNodeRef) throws BeCPGException;

	void writeImages(NodeRef entityNodeRef, Map<String, byte[]> images) throws BeCPGException;
	
	NodeRef getImageFolder(NodeRef entityNodeRef) throws BeCPGException;

	byte[] getImage(NodeRef imgNodeRef);

	String getDefaultImageName(QName entityTypeQName);

	@Deprecated
	boolean hasAssociatedImages(QName type);

	NodeRef createOrCopyFrom(NodeRef sourceNodeRef, NodeRef parentNodeRef, QName entityType, String entityName);

	void copyFiles(NodeRef sourceNodeRef, NodeRef targetNodeRef);

	void moveFiles(NodeRef sourceNodeRef, NodeRef targetNodeRef);

	void deleteFiles(NodeRef entityNodeRef, boolean deleteArchivedNodes);

	void deleteDataLists(NodeRef entityNodeRef, boolean deleteArchivedNodes);

	NodeRef getOrCreateDocumentsFolder(NodeRef entityNodeRef);

	NodeRef createDefaultImage(NodeRef nodeRef) throws BeCPGException;

	

}
