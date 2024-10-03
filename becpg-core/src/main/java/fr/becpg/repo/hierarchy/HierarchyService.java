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
package fr.becpg.repo.hierarchy;

import java.util.List;
import java.util.Locale;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * <p>HierarchyService interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface HierarchyService {
	
	/**
	 * <p>getHierarchyByPath.</p>
	 *
	 * @param path a {@link java.lang.String} object
	 * @param parentHierachyNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param key a {@link org.alfresco.service.namespace.QName} object
	 * @param value a {@link java.lang.String} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	NodeRef getHierarchyByPath(String path, NodeRef parentHierachyNodeRef, QName key, String value);

	/**
	 * <p>getHierarchyByPath.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 * @param parentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param value a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getHierarchyByPath(String path, NodeRef parentNodeRef, String value);	
	/**
	 * <p>getHierarchiesByPath.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 * @param parentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param value a {@link java.lang.String} object.
	 * @return a {@link java.util.List} object.
	 * @param includeDeleted a boolean
	 */
	List<NodeRef> getHierarchiesByPath(String path, NodeRef parentNodeRef, String value, boolean includeDeleted);
	/**
	 * <p>getAllHierarchiesByPath.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 * @param query a {@link java.lang.String} object.
	 * @return a {@link java.util.List} object.
	 */
	List<NodeRef> getAllHierarchiesByPath(String path, String query);
	/**
	 * <p>createRootHierarchy.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param path a {@link java.lang.String} object
	 * @param query a {@link java.lang.String} object
	 * @param depthLevel a {@link java.lang.String} object
	 */
	List<NodeRef> getAllHierarchiesByDepthLevel(String path, String query, String depthLevel);
	
	
	/**
	 * <p>createRootHierarchy.</p>
	 *
	 * @param dataListNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param value a {@link java.lang.String} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	NodeRef createRootHierarchy(NodeRef dataListNodeRef, String value);
	/**
	 * <p>createHierarchy.</p>
	 *
	 * @param dataListNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param hierarchyParent a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param value a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef createHierarchy(NodeRef dataListNodeRef, NodeRef hierarchyParent, String value);
	/**
	 * <p>classifyByHierarchy.</p>
	 *
	 * @param containerNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void classifyByHierarchy(NodeRef containerNodeRef, NodeRef entityNodeRef);
	/**
	 * <p>classifyByHierarchy.</p>
	 *
	 * @param containerNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param hierarchyQname a {@link org.alfresco.service.namespace.QName} object.
	 * @param locale a {@link java.util.Locale} object.
	 * @return a boolean
	 */
	boolean classifyByHierarchy(NodeRef containerNodeRef, NodeRef entityNodeRef, QName hierarchyQname, Locale locale);
	/**
	 * <p>getOrCreateHierachyFolder.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param hierarchyQname a {@link org.alfresco.service.namespace.QName} object.
	 * @param destinationNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getOrCreateHierachyFolder(NodeRef entityNodeRef, QName hierarchyQname, NodeRef destinationNodeRef);

	
}
