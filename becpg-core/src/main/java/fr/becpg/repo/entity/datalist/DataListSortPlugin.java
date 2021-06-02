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
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>DataListSortPlugin interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface DataListSortPlugin {


	/** Constant <code>BEFORE=-1</code> */
	int BEFORE = -1;
	/** Constant <code>EQUAL=0</code> */
	int EQUAL = 0;
	/** Constant <code>AFTER=1</code> */
	int AFTER = 1;
	
	/**
	 * <p>getPluginId.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	String getPluginId();

	/**
	 * <p>sort.</p>
	 *
	 * @param projectList a {@link java.util.List} object.
	 * @param sortMap a {@link java.util.Map} object.
	 * @return a {@link java.util.List} object.
	 */
	List<NodeRef> sort(List<NodeRef> projectList, Map<String, Boolean> sortMap);
}
