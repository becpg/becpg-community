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
package fr.becpg.repo.repository;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;


//TODO merge with BaseObject and BeCPGDataObject
public interface RepositoryEntity {

	public NodeRef getNodeRef();
	public void setNodeRef(NodeRef nodeRef);
	
	public NodeRef getParentNodeRef();
	public void setParentNodeRef(NodeRef parentNodeRef);
	public String getName();
	
	
	
	/**
	 * Optional Map to put extra props
	 * @return
	 */
	public Map<QName, Serializable> getExtraProperties();
	public void setExtraProperties(Map<QName, Serializable> extraProperties);

	/**
	 * Test if the entity is transiant
	 */
	public boolean isTransient();
	
	/**
	 * Use to determine if changes has applied
	 */
	public void setDbHashCode(int hashCode);
	public int getDbHashCode();
}
