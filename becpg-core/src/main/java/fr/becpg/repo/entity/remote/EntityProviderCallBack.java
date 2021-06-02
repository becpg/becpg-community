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
package fr.becpg.repo.entity.remote;

import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>EntityProviderCallBack interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface EntityProviderCallBack {

	/**
	 * <p>provideNode.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param destNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param cache a {@link java.util.Map} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef provideNode(NodeRef nodeRef, NodeRef destNodeRef, Map<NodeRef, NodeRef> cache);
	
	/**
	 * <p>provideNode.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param cache a {@link java.util.Map} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef provideNode(NodeRef nodeRef,Map<NodeRef, NodeRef> cache);

	/**
	 * <p>provideContent.</p>
	 *
	 * @param origNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param destNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void provideContent(NodeRef origNodeRef, NodeRef destNodeRef);

}
