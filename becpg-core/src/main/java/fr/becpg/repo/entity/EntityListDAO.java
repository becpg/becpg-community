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
package fr.becpg.repo.entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * <p>EntityListDAO interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface EntityListDAO {

	/**
	 * Return entity containing this dataListItem
	 *
	 * @param listItemNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getEntity(NodeRef listItemNodeRef);

	/**
	 * <p>getEntityFromList.</p>
	 *
	 * @param listNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getEntityFromList(NodeRef listNodeRef);

	/**
	 * <p>registerHiddenList.</p>
	 *
	 * @param listTypeQname a {@link org.alfresco.service.namespace.QName} object.
	 */
	void registerHiddenList(QName listTypeQname);

	/**
	 * <p>getListContainer.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getListContainer(NodeRef nodeRef);

	/**
	 * <p>createListContainer.</p>
	 *
	 * @param dataNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef createListContainer(NodeRef dataNodeRef);

	/**
	 * Get dataList with specified name and type
	 *
	 * @param listContainerNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param name a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getList(NodeRef listContainerNodeRef, String name);
	

	/**
	 * Get the data list NodeRef.
	 *
	 * @param listContainerNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param dataListQName a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getList(NodeRef listContainerNodeRef, QName dataListQName);


	/**
	 * Create dataList with specified name and type
	 *
	 * @param listContainerNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param name a {@link java.lang.String} object.
	 * @param type a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef createList(NodeRef listContainerNodeRef, String name, QName type);

	/**
	 * Create the data list NodeRef.
	 *
	 * @param listContainerNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param dataListQName a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef createList(NodeRef listContainerNodeRef, QName dataListQName);

	/**
	 * Get the link node of a data list that has the nodeRef stored in the
	 *
	 * @param listNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param propertyQName a {@link org.alfresco.service.namespace.QName} object.
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getListItem(NodeRef listNodeRef, QName propertyQName, NodeRef nodeRef);

	/**
	 * Create the link node of a data list that has the nodeRef stored in the
	 * propertyQName.
	 *
	 * @param listNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param listType a {@link org.alfresco.service.namespace.QName} object.
	 * @param properties a {@link java.util.Map} object.
	 * @param associations a {@link java.util.Map} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef createListItem(NodeRef listNodeRef, QName listType, Map<QName, Serializable> properties, Map<QName, List<NodeRef>> associations);

	/**
	 * <p>getExistingListsNodeRef.</p>
	 *
	 * @param listContainerNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link java.util.List} object.
	 */
	List<NodeRef> getExistingListsNodeRef(NodeRef listContainerNodeRef);

	/**
	 * Copy all data lists.
	 *
	 * @param sourceNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param targetNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param override a boolean.
	 */
	void copyDataLists(NodeRef sourceNodeRef, NodeRef targetNodeRef, boolean override);

	/**
	 * Copy one datalist to another entity
	 *
	 * @param dataListNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param override a boolean.
	 */
	void copyDataList(NodeRef dataListNodeRef, NodeRef entityNodeRef, boolean override);
	
	/**
	 *
	 * Merge one dataList into another
	 *
	 * @param dataListNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param appendOnly a boolean
	 */
	void mergeDataList(NodeRef dataListNodeRef,NodeRef entityNodeRef, boolean appendOnly );

	/**
	 * Copy all data lists
	 *
	 * @param sourceNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param targetNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param listQNames a {@link java.util.Collection} object.
	 * @param override a boolean.
	 */
	void copyDataLists(NodeRef sourceNodeRef, NodeRef targetNodeRef, Collection<QName> listQNames, boolean override);

	/**
	 * Get list items
	 *
	 * @param dataListNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param dataType a {@link org.alfresco.service.namespace.QName} object.
	 * @param sortMap a {@link java.util.Map} object.
	 * @return a {@link java.util.List} object.
	 */
	List<NodeRef> getListItems(NodeRef dataListNodeRef, QName dataType, Map<String, Boolean> sortMap);

	/**
	 * Get list items
	 *
	 * @param listNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param listQName a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link java.util.List} object.
	 */
	List<NodeRef> getListItems(NodeRef listNodeRef, QName listQName);

	/**
	 * Move datalists
	 *
	 * @param sourceNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param targetNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void moveDataLists(NodeRef sourceNodeRef, NodeRef targetNodeRef);

	/**
	 * Test datalist is empty
	 *
	 * @param listNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param listQNameFilter a {@link org.alfresco.service.namespace.QName} object.
	 * @return a boolean.
	 */
	boolean isEmpty(NodeRef listNodeRef, QName listQNameFilter);

	/**
	 * Find list with the same list in other entityListContainer
	 *
	 * @param dataListNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param targetListContainerNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a nodeRef
	 */
	NodeRef findMatchingList(NodeRef dataListNodeRef, NodeRef targetListContainerNodeRef);

	/**
	 * <p>getListItemsByType.</p>
	 *
	 * @param dataListNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link java.util.Map} object
	 */
	Map<QName, List<NodeRef>> getListItemsByType(NodeRef dataListNodeRef);

}
