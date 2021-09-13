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
package fr.becpg.repo.quality;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>NonConformityService interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface NonConformityService {

	/**
	 * Calculate the storage folder (product NC or default)
	 *
	 * @param productNodeRef
	 *            may be null
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getStorageFolder(NodeRef productNodeRef);

	/**
	 * <p>classifyNC.</p>
	 *
	 * @param ncNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param productNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@Deprecated
	void classifyNC(NodeRef ncNodeRef, NodeRef productNodeRef);

	/**
	 * <p>getAssociatedWorkflow.</p>
	 *
	 * @param ncNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link java.util.List} object.
	 */
	List<String> getAssociatedWorkflow(NodeRef ncNodeRef);
	
	/**
	 * <p>deleteWorkflows.</p>
	 *
	 * @param instanceIds a {@link java.util.List} object.
	 */
	void deleteWorkflows(List<String> instanceIds);
}
