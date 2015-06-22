/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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

/**
 * 
 * @author matthieu
 *
 */
public interface EntitySystemService {

	/**
	 * 
	 * @param parentNodeRef
	 * @param path
	 * @param entitySystemDataLists
	 * @return create systeme entity
	 */
	NodeRef createSystemEntity(NodeRef parentNodeRef, String path, Map<String, QName> entitySystemDataLists);

	/**
	 * 
	 * @param parentNodeRef
	 * @param systemEntityPath
	 * @return system entity for given systemEntityPath
	 */
	NodeRef getSystemEntity(NodeRef parentNodeRef, String systemEntityPath);

	/**
	 * 
	 * @param systemEntityNodeRef
	 * @param dataListPath
	 * @returns system entity datalist
	 */
	NodeRef getSystemEntityDataList(NodeRef systemEntityNodeRef, String dataListPath);

	/**
	 * 
	 * @param parentNodeRef
	 * @param systemEntityPath
	 * @param dataListPath
	 * @return system entity datalist
	 */
	NodeRef getSystemEntityDataList(NodeRef parentNodeRef, String systemEntityPath, String dataListPath);

	/**
	 * @return entities of type TYPE_SYSTEM_ENTITY
	 */
	List<NodeRef> getSystemEntities();
	
	
	/**
	 * @return folders with aspect SYSTEM_FOLDER
	 */
	List<NodeRef> getSystemFolders();

}
