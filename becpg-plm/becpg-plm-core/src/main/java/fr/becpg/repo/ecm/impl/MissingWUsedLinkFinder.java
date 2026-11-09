/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
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
package fr.becpg.repo.ecm.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.ecm.data.dataList.WUsedListDataItem;

/**
 * Finds the impacted where used lines of a product whose list item no longer
 * exists in the product, typically because a merge recreated the composition
 * lines after the where used list was calculated.
 *
 * @author matthieu
 */
final class MissingWUsedLinkFinder {

	private static final Integer DIRECT_USE_DEPTH_LEVEL = 2;

	private MissingWUsedLinkFinder() {
	}

	/**
	 * Lists the impacted where used lines raised on the product whose link is not
	 * one of the product list items.
	 *
	 * @param productNodeRef the product the change order is applied to
	 * @param wUsedList the where used list of the change order
	 * @param listItemNodeRefs the composition, packaging and process items of the product
	 * @return the where used lines whose list item was not found, empty when all are found
	 */
	static List<WUsedListDataItem> findMissingLinks(NodeRef productNodeRef, List<WUsedListDataItem> wUsedList, Set<NodeRef> listItemNodeRefs) {
		List<WUsedListDataItem> missingLinks = new ArrayList<>();
		for (WUsedListDataItem wUsedItem : wUsedList) {
			if (isImpactedDirectUseOf(wUsedItem, productNodeRef) && !isLinkFound(wUsedItem.getLink(), listItemNodeRefs)) {
				missingLinks.add(wUsedItem);
			}
		}
		return missingLinks;
	}

	private static boolean isLinkFound(NodeRef link, Set<NodeRef> listItemNodeRefs) {
		return (link != null) && listItemNodeRefs.contains(link);
	}

	private static boolean isImpactedDirectUseOf(WUsedListDataItem wUsedItem, NodeRef productNodeRef) {
		List<NodeRef> sourceItems = wUsedItem.getSourceItems();
		return Boolean.TRUE.equals(wUsedItem.getIsWUsedImpacted()) && DIRECT_USE_DEPTH_LEVEL.equals(wUsedItem.getDepthLevel())
				&& (sourceItems != null) && !sourceItems.isEmpty() && productNodeRef.equals(sourceItems.get(0));
	}
}
