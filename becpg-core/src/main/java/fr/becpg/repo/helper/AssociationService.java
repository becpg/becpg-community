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
package fr.becpg.repo.helper;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.alfresco.query.PagingRequest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.helper.impl.AssociationCriteriaFilter;
import fr.becpg.repo.helper.impl.ChildAssocCacheEntry;
import fr.becpg.repo.helper.impl.EntitySourceAssoc;

/**
 * <p>AssociationService interface provides methods for managing associations between nodes in the repository.</p>
 * <p>This service handles both child and regular associations, with support for caching and filtering.</p>
 *
 * @author matthieu
 * @version 1.0
 */
public interface AssociationService {

	/**
	 * Updates the associations for a given node with the specified association type.
	 * 
	 * @param nodeRef the node reference to update associations for
	 * @param qName the association type QName
	 * @param assocNodeRefs the list of node references to associate with the node
	 */
	void update(NodeRef nodeRef, QName qName, List<NodeRef> assocNodeRefs);
	
	/**
	 * Updates the associations for a given node with the specified association type.
	 * 
	 * @param nodeRef the node reference to update associations for
	 * @param qName the association type QName
	 * @param assocNodeRef the node reference to associate with the node
	 */
	void update(NodeRef nodeRef, QName qName, NodeRef assocNodeRef);
	
	/**
	 * Retrieves the target association for a given node with the specified association type.
	 * 
	 * @param nodeRef the node reference to retrieve the target association for
	 * @param qName the association type QName
	 * @return the target association node reference
	 */
	NodeRef getTargetAssoc(NodeRef nodeRef, QName qName);
	
	/**
	 * Retrieves the target associations for a given node with the specified association type.
	 * 
	 * @param nodeRef the node reference to retrieve the target associations for
	 * @param qName the association type QName
	 * @return the list of target association node references
	 */
	List<NodeRef> getTargetAssocs(NodeRef nodeRef, QName qName);
	
	/**
	 * Retrieves the child association for a given node with the specified association type.
	 * 
	 * @param nodeRef the node reference to retrieve the child association for
	 * @param qName the association type QName
	 * @return the child association node reference
	 */
	NodeRef getChildAssoc(NodeRef nodeRef, QName qName);
	
	/**
	 * Retrieves the child associations for a given node with the specified association type.
	 * 
	 * @param nodeRef the node reference to retrieve the child associations for
	 * @param qName the association type QName
	 * @return the list of child association node references
	 */
	List<NodeRef> getChildAssocs(NodeRef nodeRef, QName qName);
	
	/**
	 * Retrieves the child associations for a given node with the specified association type and child type.
	 * 
	 * @param nodeRef the node reference to retrieve the child associations for
	 * @param qName the association type QName
	 * @param childTypeQName the child type QName
	 * @return the list of child association node references
	 */
	List<NodeRef> getChildAssocs(NodeRef nodeRef, QName qName, QName childTypeQName);
	
	/**
	 * Retrieves the child associations for a given list node with the specified association type and list type.
	 * 
	 * @param listNodeRef the list node reference to retrieve the child associations for
	 * @param assocContains the association type QName
	 * @param listQNameFilter the list type QName filter
	 * @param sortMap the sort map
	 * @return the list of child association node references
	 */
	List<NodeRef> getChildAssocs(NodeRef listNodeRef, QName assocContains, QName listQNameFilter, @Nullable Map<String, Boolean> sortMap);
	
	/**
	 * Retrieves the source associations for a given node.
	 * 
	 * @param nodeRef the node reference to retrieve the source associations for
	 * @return the list of source association node references
	 */
	List<NodeRef> getSourcesAssocs(NodeRef nodeRef);
	
	/**
	 * Retrieves the source associations for a given node with the specified association type.
	 * 
	 * @param nodeRef the node reference to retrieve the source associations for
	 * @param qNamePattern the association type QName pattern
	 * @return the list of source association node references
	 */
	List<NodeRef> getSourcesAssocs(NodeRef nodeRef, QName qNamePattern);
	
	/**
	 * Retrieves the source associations for a given node with the specified association type and versioning options.
	 * 
	 * @param nodeRef the node reference to retrieve the source associations for
	 * @param qNamePattern the association type QName pattern
	 * @param includeVersions whether to include versioned associations
	 * @return the list of source association node references
	 */
	List<NodeRef> getSourcesAssocs(NodeRef nodeRef, QName qNamePattern, Boolean includeVersions);
	
	/**
	 * Retrieves the source associations for a given node with the specified association type, versioning options, and pagination.
	 * 
	 * @param nodeRef the node reference to retrieve the source associations for
	 * @param qNamePattern the association type QName pattern
	 * @param includeVersions whether to include versioned associations
	 * @param maxResults the maximum number of results to return
	 * @param offset the offset for pagination
	 * @return the list of source association node references
	 */
	List<NodeRef> getSourcesAssocs(NodeRef nodeRef, QName qNamePattern, Boolean includeVersions, Integer maxResults, Integer offset);
	
	/**
	 * Retrieves the source associations for a given node with the specified association type, versioning options, pagination, and permission checking.
	 * 
	 * @param nodeRef the node reference to retrieve the source associations for
	 * @param qName the association type QName
	 * @param includeVersions whether to include versioned associations
	 * @param maxResults the maximum number of results to return
	 * @param offset the offset for pagination
	 * @param checkPermissions whether to check permissions
	 * @return the list of source association node references
	 */
	List<NodeRef> getSourcesAssocs(NodeRef nodeRef, QName qName, Boolean includeVersions, Integer maxResults, Integer offset, boolean checkPermissions);
	
	/**
	 * Retrieves entity source associations with filtering and pagination.
	 * 
	 * @param nodeRefs the node references to check
	 * @param assocQName the association type QName
	 * @param listTypeQname the list type QName
	 * @param isOrOperator whether to use OR operator for filtering
	 * @param criteriaFilters the criteria filters to apply
	 * @return the list of entity source associations
	 */
	List<EntitySourceAssoc> getEntitySourceAssocs(List<NodeRef> nodeRefs, QName assocQName, QName listTypeQname, boolean isOrOperator, List<AssociationCriteriaFilter> criteriaFilters);
	
	/**
	 * Retrieves entity source associations with filtering, pagination, and permission checking.
	 * 
	 * @param nodeRefs the node references to check
	 * @param assocQName the association type QName
	 * @param listTypeQname the list type QName
	 * @param isOrOperator whether to use OR operator for filtering
	 * @param criteriaFilters the criteria filters to apply
	 * @param pagingRequest the pagination request
	 * @return the list of entity source associations
	 */
	List<EntitySourceAssoc> getEntitySourceAssocs(List<NodeRef> nodeRefs, QName assocQName, QName listTypeQname, boolean isOrOperator, List<AssociationCriteriaFilter> criteriaFilters, PagingRequest pagingRequest);
	
	/**
	 * Retrieves entity source associations with filtering, pagination, and permission checking.
	 * 
	 * @param nodeRefs the node references to check
	 * @param assocQName the association type QName
	 * @param listTypeQname the list type QName
	 * @param isOrOperator whether to use OR operator for filtering
	 * @param criteriaFilters the criteria filters to apply
	 * @param pagingRequest the pagination request
	 * @param checkPermissions whether to check permissions
	 * @return the list of entity source associations
	 */
	List<EntitySourceAssoc> getEntitySourceAssocs(List<NodeRef> nodeRefs, QName assocQName, QName listTypeQname, boolean isOrOperator, List<AssociationCriteriaFilter> criteriaFilters, PagingRequest pagingRequest, boolean checkPermissions);
	
	/**
	 * Removes the child cached association for a given node with the specified association type.
	 * 
	 * @param nodeRef the node reference to remove the child cached association for
	 * @param qName the association type QName
	 */
	void removeChildCachedAssoc(NodeRef nodeRef, QName qName);
	
	/**
	 * Removes all cached associations for a given node.
	 * 
	 * @param nodeRef the node reference to remove all cached associations for
	 */
	void removeAllCacheAssocs(NodeRef nodeRef);
	
	/**
	 * Retrieves the child associations by type for a given node with the specified association type.
	 * 
	 * @param nodeRef the node reference to retrieve the child associations by type for
	 * @param qName the association type QName
	 * @return the child association cache entry
	 */
	ChildAssocCacheEntry getChildAssocsByType(NodeRef nodeRef, QName qName);
	
}
