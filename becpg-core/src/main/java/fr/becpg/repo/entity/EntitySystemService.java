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

/**
 * <p>EntitySystemService interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface EntitySystemService {

	/**
	 * <p>createSystemEntity.</p>
	 *
	 * @param parentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param path a {@link java.lang.String} object.
	 * @param entitySystemDataLists a {@link java.util.Map} object.
	 * @return create systeme entity
	 */
	NodeRef createSystemEntity(NodeRef parentNodeRef, String path, Map<String, QName> entitySystemDataLists);

	/**
	 * <p>getSystemEntity.</p>
	 *
	 * @param parentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param systemEntityPath a {@link java.lang.String} object.
	 * @return system entity for given systemEntityPath
	 */
	NodeRef getSystemEntity(NodeRef parentNodeRef, String systemEntityPath);

	/**
	 * <p>getSystemEntityDataList.</p>
	 *
	 * @param systemEntityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param dataListPath a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} system entity datalist.
	 */
	NodeRef getSystemEntityDataList(NodeRef systemEntityNodeRef, String dataListPath);

	/**
	 * <p>getSystemEntityDataList.</p>
	 *
	 * @param parentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param systemEntityPath a {@link java.lang.String} object.
	 * @param dataListPath a {@link java.lang.String} object.
	 * @return system entity datalist
	 */
	NodeRef getSystemEntityDataList(NodeRef parentNodeRef, String systemEntityPath, String dataListPath);

	/**
	 * <p>getSystemEntities.</p>
	 *
	 * @return entities of type TYPE_SYSTEM_ENTITY
	 */
	List<NodeRef> getSystemEntities();

	/**
	 * <p>getSystemFolders.</p>
	 *
	 * @return folders with aspect SYSTEM_FOLDER
	 */
	List<NodeRef> getSystemFolders();

}
