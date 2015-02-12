/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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
package fr.becpg.repo.olap.webscripts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.olap.OlapService;
import fr.becpg.repo.olap.data.OlapChart;
import fr.becpg.repo.olap.data.OlapChartData;
/**
 * Act as a proxy for OLAP queries
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
public class OlapChartWebScript  extends AbstractWebScript
{
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(OlapChartWebScript.class);
	
	// request parameter names
	/** The Constant PARAM_ACTION. */
	private static final String PARAM_QUERY_ID = "olapQueryId";
	
	private String olapServerUrl;
	
	
	private OlapService olapService;
	
	
    public void setOlapService(OlapService olapService) {
		this.olapService = olapService;
	}

    

	public void setOlapServerUrl(String olapServerUrl) {
		this.olapServerUrl = olapServerUrl;
	}



	/* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.WebScript#execute(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse)
     */
    @Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws WebScriptException
    {
    	logger.debug("Call OlapChartWebScript");
    	
    	String olapQueryId = req.getParameter(PARAM_QUERY_ID);

    	try {
	    
	    	
	    	if(olapQueryId!=null){
	    		OlapChartData data = new OlapChartData();
	    		if(!olapQueryId.isEmpty()){
	    			try {
	    				data  = olapService.retrieveChartData(olapQueryId);
	    			} catch (Exception e) {
	    	    		logger.error(e, e);
	    			}
		   
	    		}
	    		String jsonString = data.toJSONObject().toString();
		    	res.getWriter().write(jsonString);
		
	    	    		
	    	} else {
	    		List<OlapChart> charts = new ArrayList<OlapChart>(); 
	    		try {
	    			charts=  olapService.retrieveOlapCharts();
	    		} catch (Exception e) {
    	    		logger.error(e, e);
    			}
	    		
	    		JSONObject ret = new JSONObject();
	    		JSONArray obj = new JSONArray();
		    	for (Iterator<OlapChart> iterator = charts.iterator(); iterator.hasNext();) {
					OlapChart olapChart = iterator.next();
					obj.put(olapChart.toJSONObject());
				}
		    	ret.put("queries",obj);
		    	JSONObject metadata = new JSONObject();
		    	metadata.put("currentUserName", olapService.getCurrentOlapUserName());
		    	metadata.put("olapServerUrl", olapServerUrl);
		    	metadata.put("olapQueriesFolder", olapService.getOlapQueriesFolder());
		    	ret.put("metadata",metadata);
		    	
		    	String jsonString = ret.toString();
		    	res.getWriter().write(jsonString);
		    
	    	}
	    	res.setContentEncoding("UTF-8");
	    	res.setContentType("application/json");
	    	
    		
    	} catch (Exception e) {
    		logger.error(e, e);
    		throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, e.getMessage());
		}
    	
    	
    }
}
