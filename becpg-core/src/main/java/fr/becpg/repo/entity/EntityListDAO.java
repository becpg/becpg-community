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
package fr.becpg.repo.entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public interface EntityListDAO {

	/**
	 * Return entity containing this dataListItem
	 */
	NodeRef getEntity(NodeRef listItemNodeRef);

	NodeRef getEntityFromList(NodeRef listNodeRef);

	void registerHiddenList(QName listTypeQname);

	NodeRef getListContainer(NodeRef nodeRef);

	NodeRef createListContainer(NodeRef dataNodeRef);

	/**
	 * Get dataList with specified name and type
	 */
	NodeRef getList(NodeRef listContainerNodeRef, String name);

	/**
	 * Create dataList with specified name and type
	 */
	NodeRef createList(NodeRef listContainerNodeRef, String name, QName type);

	/**
	 * Get the data list NodeRef.
	 */
	NodeRef getList(NodeRef listContainerNodeRef, QName dataListQName);

	/**
	 * Create the data list NodeRef.
	 */
	NodeRef createList(NodeRef listContainerNodeRef, QName dataListQName);

	/**
	 * Get the link node of a data list that has the nodeRef stored in the
	 */
	NodeRef getListItem(NodeRef listNodeRef, QName propertyQName, NodeRef nodeRef);

	/**
	 * Create the link node of a data list that has the nodeRef stored in the
	 * propertyQName.
	 */
	NodeRef createListItem(NodeRef listNodeRef, QName listType, Map<QName, Serializable> properties, Map<QName, List<NodeRef>> associations);

	List<NodeRef> getExistingListsNodeRef(NodeRef listContainerNodeRef);

	/**
	 * Copy all data lists.
	 */
	void copyDataLists(NodeRef sourceNodeRef, NodeRef targetNodeRef, boolean override);

	/**
	 * Copy one datalist to another entity
	 */
	void copyDataList(NodeRef dataListNodeRef, NodeRef entityNodeRef, boolean override);

	/**
	 * Copy all data lists
	 */
	void copyDataLists(NodeRef sourceNodeRef, NodeRef targetNodeRef, Collection<QName> listQNames, boolean override);

	/**
	 * Get list items
	 *
	 */
	List<NodeRef> getListItems(NodeRef dataListNodeRef, QName dataType, Map<String, Boolean> sortMap);

	/**
	 * Get list items
	 *
	 */
	List<NodeRef> getListItems(NodeRef listNodeRef, QName listQName);

	/**
	 * Move datalists
	 */
	void moveDataLists(NodeRef sourceNodeRef, NodeRef targetNodeRef);

	/**
	 * Test datalist is empty
	 */
	boolean isEmpty(NodeRef listNodeRef, QName listQNameFilter);

}
