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
package fr.becpg.repo.search;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.search.impl.SearchConfig;

/**
 * do an advanced search query
 *
 * @author querephi
 * @version $Id: $Id
 */
public interface AdvSearchService {

	
	/**
	 * <p>createSearchQuery.</p>
	 *
	 * @param datatype a {@link org.alfresco.service.namespace.QName} object.
	 * @param term a {@link java.lang.String} object.
	 * @param tag a {@link java.lang.String} object.
	 * @param isRepo a boolean.
	 * @param siteId a {@link java.lang.String} object.
	 * @param containerId a {@link java.lang.String} object.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	BeCPGQueryBuilder createSearchQuery(QName datatype, String term, String tag, boolean isRepo, String siteId, String containerId);


	/**
	 * <p>queryAdvSearch.</p>
	 *
	 * @param datatype a {@link org.alfresco.service.namespace.QName} object.
	 * @param beCPGQueryBuilder a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 * @param criteria a {@link java.util.Map} object.
	 * @param maxResults a int.
	 * @return a {@link java.util.List} object.
	 */
	List<NodeRef> queryAdvSearch(QName datatype, BeCPGQueryBuilder beCPGQueryBuilder, Map<String, String> criteria, int maxResults);


	/**
	 * <p>getSearchConfig.</p>
	 *
	 * @return a {@link fr.becpg.repo.search.impl.SearchConfig} object.
	 */
	SearchConfig getSearchConfig();

}
