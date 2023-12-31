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
package fr.becpg.repo.olap;

import java.io.IOException;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;

import fr.becpg.repo.olap.data.OlapChart;
import fr.becpg.repo.olap.data.OlapChartData;


/**
 * Act as a proxy to OLAP Engine
 *
 * @author "Matthieu Laborie"
 * @version $Id: $Id
 */
public interface OlapService {

	/**
	 * <p>retrieveOlapCharts.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	List<OlapChart> retrieveOlapCharts();
	
	/**
	 * <p>retrieveOlapChartsFromSaiku.</p>
	 *
	 * @return a {@link java.util.List} object.
	 * @throws java.io.IOException if any.
	 * @throws org.json.JSONException if any.
	 */
	List<OlapChart> retrieveOlapChartsFromSaiku() throws IOException, JSONException;
	
	/**
	 * <p>retrieveChartData.</p>
	 *
	 * @param olapQueryId a {@link java.lang.String} object.
	 * @return a {@link fr.becpg.repo.olap.data.OlapChartData} object.
	 * @throws java.io.IOException if any.
	 * @throws org.json.JSONException if any.
	 */
	OlapChartData retrieveChartData(String olapQueryId) throws IOException, JSONException;

	/**
	 * <p>getOlapQueriesFolder.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getOlapQueriesFolder();

	/**
	 * <p>getSSOUrl.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	String getSSOUrl();
	
}
