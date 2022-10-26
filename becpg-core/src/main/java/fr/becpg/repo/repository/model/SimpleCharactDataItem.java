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
package fr.becpg.repo.repository.model;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.RepositoryEntity;

/**
 * <p>SimpleCharactDataItem interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface SimpleCharactDataItem extends RepositoryEntity, CopiableDataItem {

	/**
	 * <p>setCharactNodeRef.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void setCharactNodeRef(NodeRef nodeRef);

	/**
	 * <p>setValue.</p>
	 *
	 * @param value a {@link java.lang.Double} object.
	 */
	void setValue(Double value);

	/**
	 * <p>getCharactNodeRef.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getCharactNodeRef();

	/**
	 * <p>getValue.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	Double getValue();
	
	/**
	 * <p>shouldDetailIfZero.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	default Boolean shouldDetailIfZero(){
		return false;
	}
}
