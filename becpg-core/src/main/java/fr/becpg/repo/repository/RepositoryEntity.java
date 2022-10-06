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
package fr.becpg.repo.repository;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * <p>RepositoryEntity interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface RepositoryEntity extends Serializable {

	/**
	 * <p>getNodeRef.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getNodeRef();
	/**
	 * <p>setNodeRef.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void setNodeRef(NodeRef nodeRef);
	
	/**
	 * <p>getParentNodeRef.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getParentNodeRef();
	/**
	 * <p>setParentNodeRef.</p>
	 *
	 * @param parentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void setParentNodeRef(NodeRef parentNodeRef);
	/**
	 * <p>getName.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	String getName();
	/**
	 * <p>setName.</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 */
	void setName(String name);
	
	
	/**
	 * Optional Map to put extra props
	 *
	 * @return a {@link java.util.Map} object.
	 */
	Map<QName, Serializable> getExtraProperties();
	
	
	/**
	 * <p>setExtraProperties.</p>
	 *
	 * @param extraProperties a {@link java.util.Map} object.
	 */
	void setExtraProperties(Map<QName, Serializable> extraProperties);

	/**
	 * Test if the entity is transiant
	 *
	 * @return a boolean.
	 */
	boolean isTransient();
	
	/**
	 * Use to determine if changes has applied
	 *
	 * @param hashCode a long.
	 */
	void setDbHashCode(long hashCode);
	/**
	 * <p>getDbHashCode.</p>
	 *
	 * @return a long.
	 */
	long getDbHashCode();
}
