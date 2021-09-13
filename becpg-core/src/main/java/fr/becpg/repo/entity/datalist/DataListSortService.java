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
package fr.becpg.repo.entity.datalist;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>DataListSortService interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface DataListSortService {

	/**
	 * <p>getLastChild.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getLastChild(NodeRef nodeRef);

	/**
	 * <p>insertAfter.</p>
	 *
	 * @param selectedNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void insertAfter(NodeRef selectedNodeRef, NodeRef nodeRef);

	/**
	 * <p>computeDepthAndSort.</p>
	 *
	 * @param nodeRefs a {@link java.util.Set} object.
	 */
	void computeDepthAndSort(Set<NodeRef> nodeRefs);

	/**
	 * <p>move.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param moveUp a boolean.
	 */
	void move(NodeRef nodeRef, boolean moveUp);

	/**
	 * <p>deleteChildrens.</p>
	 *
	 * @param parentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void deleteChildrens(NodeRef parentNodeRef, NodeRef nodeRef);
	
}
