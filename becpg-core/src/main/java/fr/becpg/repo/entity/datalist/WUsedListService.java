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

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.entity.datalist.data.MultiLevelListData;

/**
 * <p>WUsedListService interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface WUsedListService {
	
	public enum WUsedOperator {
		AND,OR
	}


	/**
	 * Calculate the WUsed entities of the item
	 *
	 * @param entityNodeRef item associated to datalists
	 * @param associationName a {@link org.alfresco.service.namespace.QName} object.
	 * @param maxDepthLevel a int.
	 * @return a {@link fr.becpg.repo.entity.datalist.data.MultiLevelListData} object.
	 */
	MultiLevelListData getWUsedEntity(NodeRef entityNodeRef, QName associationName, int maxDepthLevel);
    
	/**
	 * Calculate the WUsed entities of the items
	 *
	 * @param entityNodeRefs item associated to datalists
	 * @param associationName a {@link org.alfresco.service.namespace.QName} object.
	 * @param filter a {@link fr.becpg.repo.entity.datalist.WUsedFilter} object.
	 * @param maxDepthLevel a int.
	 * @param operator a {@link fr.becpg.repo.entity.datalist.WUsedListService.WUsedOperator} object.
	 * @return a {@link fr.becpg.repo.entity.datalist.data.MultiLevelListData} object.
	 */
	MultiLevelListData getWUsedEntity(List<NodeRef> entityNodeRefs, WUsedOperator operator, WUsedFilter filter, QName associationName, int maxDepthLevel);
	/**
	 * Calculate the WUsed entities of the items
	 *
	 * @param entityNodeRefs item associated to datalists
	 * @param associationName a {@link org.alfresco.service.namespace.QName} object.
	 * @param maxDepthLevel a int.
	 * @param operator a {@link fr.becpg.repo.entity.datalist.WUsedListService.WUsedOperator} object.
	 * @return a {@link fr.becpg.repo.entity.datalist.data.MultiLevelListData} object.
	 */
	MultiLevelListData getWUsedEntity(List<NodeRef> entityNodeRefs, WUsedOperator operator, QName associationName, int maxDepthLevel);

    
}
