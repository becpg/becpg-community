/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG. 
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
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
public interface OlapService {

	List<OlapChart> retrieveOlapCharts();
	
	List<OlapChart> retrieveOlapChartsFromSaiku() throws IOException, JSONException;
	
	OlapChartData retrieveChartData(String olapQueryId) throws IOException, JSONException;

	NodeRef getOlapQueriesFolder();

	String getSSOUrl();
	
}
