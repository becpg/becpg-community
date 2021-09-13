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

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;

/**
 * <p>MultiLevelDataListService interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface MultiLevelDataListService {
	/**
	 * <p>getMultiLevelListData.</p>
	 *
	 * @param dataListFilter a {@link fr.becpg.repo.entity.datalist.data.DataListFilter} object.
	 * @return a {@link fr.becpg.repo.entity.datalist.data.MultiLevelListData} object.
	 */
	MultiLevelListData getMultiLevelListData(DataListFilter dataListFilter);
	/**
	 * <p>getMultiLevelListData.</p>
	 *
	 * @param dataListFilter a {@link fr.becpg.repo.entity.datalist.data.DataListFilter} object.
	 * @param useExpandedCache a boolean.
	 * @param resetTree a boolean.
	 * @return a {@link fr.becpg.repo.entity.datalist.data.MultiLevelListData} object.
	 */
	MultiLevelListData getMultiLevelListData(DataListFilter dataListFilter, boolean useExpandedCache, boolean resetTree);
	/**
	 * <p>expandOrColapseNode.</p>
	 *
	 * @param nodeToExpand a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param expand a boolean.
	 */
	void expandOrColapseNode(NodeRef nodeToExpand, boolean expand);
	/**
	 * <p>isExpandedNode.</p>
	 *
	 * @param entityFolder a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param condition a boolean.
	 * @param resetTree a boolean.
	 * @return a boolean.
	 */
	boolean isExpandedNode(NodeRef entityFolder, boolean condition, boolean resetTree);
	
	
}
