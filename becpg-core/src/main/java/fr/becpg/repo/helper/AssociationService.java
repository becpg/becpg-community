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
 * <p>AssociationService interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface AssociationService {


	/**
	 * <p>update.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param qName a {@link org.alfresco.service.namespace.QName} object.
	 * @param assocNodeRefs a {@link java.util.List} object.
	 */
	void update(NodeRef nodeRef, QName qName, List<NodeRef> assocNodeRefs);
	/**
	 * <p>update.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param qName a {@link org.alfresco.service.namespace.QName} object.
	 * @param assocNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void update(NodeRef nodeRef, QName qName, NodeRef assocNodeRef);
	/**
	 * <p>getTargetAssoc.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param qName a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getTargetAssoc(NodeRef nodeRef, QName qName);
	/**
	 * <p>getTargetAssocs.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param qName a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link java.util.List} object.
	 */
	List<NodeRef> getTargetAssocs(NodeRef nodeRef, QName qName);
	/**
	 * <p>getChildAssoc.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param qName a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getChildAssoc(NodeRef nodeRef, QName qName);
	/**
	 * <p>getChildAssocs.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param qName a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link java.util.List} object.
	 */
	List<NodeRef> getChildAssocs(NodeRef nodeRef, QName qName);
	/**
	 * <p>getChildAssocs.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param qName a {@link org.alfresco.service.namespace.QName} object.
	 * @param childTypeQName a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link java.util.List} object.
	 */
	List<NodeRef> getChildAssocs(NodeRef nodeRef, QName qName, QName childTypeQName);
	/**
	 * <p>getChildAssocs.</p>
	 *
	 * @param listNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param assocContains a {@link org.alfresco.service.namespace.QName} object.
	 * @param listQNameFilter a {@link org.alfresco.service.namespace.QName} object.
	 * @param sortMap a {@link java.util.Map} object.
	 * @return a {@link java.util.List} object.
	 */
	List<NodeRef> getChildAssocs(NodeRef listNodeRef, QName assocContains, QName listQNameFilter,@Nullable Map<String, Boolean> sortMap);
	
	List<NodeRef> getSourcesAssocs(NodeRef nodeRef);
	
	/**
	 * <p>getSourcesAssocs.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param qNamePattern a {@link org.alfresco.service.namespace.QNamePattern} object.
	 * @return a {@link java.util.List} object.
	 */
	List<NodeRef> getSourcesAssocs(NodeRef nodeRef, QName qNamePattern);
	
	/**
	 * <p>getSourcesAssocs.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param qNamePattern a {@link org.alfresco.service.namespace.QNamePattern} object
	 * @param includeVersions a boolean
	 * @return a {@link java.util.List} object
	 */
	List<NodeRef> getSourcesAssocs(NodeRef nodeRef, QName qNamePattern, Boolean includeVersions);
	
	List<NodeRef> getSourcesAssocs(NodeRef nodeRef, QName qNamePattern, Boolean includeVersions, Integer maxResults, Integer offset);
	
	List<NodeRef> getSourcesAssocs(NodeRef nodeRef, QName qName, Boolean includeVersions, Integer maxResults, Integer offset,
			boolean checkPermissions);
	
	/**
	 * <p>getEntitySourceAssocs.</p>
	 *
	 * @param nodeRefs a {@link java.util.List} object.
	 * @param assocQName a {@link org.alfresco.service.namespace.QName} object.
	 * @param isOrOperator a boolean.
	 * @return a {@link java.util.List} object.
	 * @param listTypeQname a {@link org.alfresco.service.namespace.QName} object
	 * @param criteriaFilters a {@link java.util.List} object
	 */
	List<EntitySourceAssoc> getEntitySourceAssocs(List<NodeRef> nodeRefs, QName assocQName, QName listTypeQname, boolean isOrOperator, List<AssociationCriteriaFilter> criteriaFilters);
	
	/**
	 * <p>getEntitySourceAssocs.</p>
	 *
	 * @param nodeRefs a {@link java.util.List} object
	 * @param assocQName a {@link org.alfresco.service.namespace.QName} object
	 * @param listTypeQname a {@link org.alfresco.service.namespace.QName} object
	 * @param isOrOperator a boolean
	 * @param criteriaFilters a {@link java.util.List} object
	 * @param pagingRequest a {@link org.alfresco.query.PagingRequest} object
	 * @return a {@link java.util.List} object
	 */
	List<EntitySourceAssoc> getEntitySourceAssocs(List<NodeRef> nodeRefs, QName assocQName, QName listTypeQname, boolean isOrOperator, List<AssociationCriteriaFilter> criteriaFilters, PagingRequest pagingRequest);
	
	
	/**
	 * <p>removeChildCachedAssoc.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param qName a {@link org.alfresco.service.namespace.QName} object
	 */
	void removeChildCachedAssoc(NodeRef nodeRef, QName qName);
	
	/**
	 * <p>removeAllCacheAssocs.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	void removeAllCacheAssocs(NodeRef nodeRef);
	
	/**
	 * <p>getChildAssocsByType.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param qName a {@link org.alfresco.service.namespace.QName} object
	 * @return a {@link fr.becpg.repo.helper.impl.ChildAssocCacheEntry} object
	 */
	ChildAssocCacheEntry getChildAssocsByType(NodeRef nodeRef, QName qName);
	
	
}
