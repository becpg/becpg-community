/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG. 
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

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;

import fr.becpg.repo.helper.impl.AssociationCriteriaFilter;
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
	/**
	 * <p>getSourcesAssocs.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param qNamePattern a {@link org.alfresco.service.namespace.QNamePattern} object.
	 * @return a {@link java.util.List} object.
	 */
	List<NodeRef> getSourcesAssocs(NodeRef nodeRef, QNamePattern qNamePattern);
	/**
	 * <p>getEntitySourceAssocs.</p>
	 *
	 * @param nodeRefs a {@link java.util.List} object.
	 * @param assocQName a {@link org.alfresco.service.namespace.QName} object.
	 * @param isOrOperator a boolean.
	 * @return a {@link java.util.List} object.
	 */
	List<EntitySourceAssoc> getEntitySourceAssocs(List<NodeRef> nodeRefs, QName assocQName, boolean isOrOperator, List<AssociationCriteriaFilter> criteriaFilters);
	
	
	void removeChildCachedAssoc(NodeRef nodeRef, QName qName);
	
	void removeAllCacheAssocs(NodeRef nodeRef);
	
}
