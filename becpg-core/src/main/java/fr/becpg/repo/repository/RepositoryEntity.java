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
package fr.becpg.repo.repository;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;


//TODO merge with BaseObject and BeCPGDataObject
public interface RepositoryEntity {

	NodeRef getNodeRef();
	void setNodeRef(NodeRef nodeRef);
	
	NodeRef getParentNodeRef();
	void setParentNodeRef(NodeRef parentNodeRef);
	String getName();
	void setName(String name);
	
	
	
	/**
	 * Optional Map to put extra props
	 * @return
	 */
	Map<QName, Serializable> getExtraProperties();
	
	
	void setExtraProperties(Map<QName, Serializable> extraProperties);

	/**
	 * Test if the entity is transiant
	 */
	boolean isTransient();
	
	/**
	 * Use to determine if changes has applied
	 */
	void setDbHashCode(int hashCode);
	int getDbHashCode();
}
