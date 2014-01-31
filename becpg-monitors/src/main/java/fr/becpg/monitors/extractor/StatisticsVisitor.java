/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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
package fr.becpg.monitors.extractor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import fr.becpg.tools.InstanceManager.Instance;
import fr.becpg.tools.jdbc.JdbcUtils;

/**
 * 
 * @author matthieu
 * 
 */
public class StatisticsVisitor {

	private Connection connection;

	private Instance instance;

	public StatisticsVisitor(Connection connection, Instance instance) {
		super();
		this.instance = instance;
		this.connection = connection;
	}


	private static Log logger = LogFactory.getLog(StatisticsVisitor.class);

	public void visit(InputStream in) throws SQLException, IOException, ParseException  {

	     JSONParser jsonParser = new JSONParser();
	    JSONObject jsonObject =  (JSONObject) jsonParser.parse(new InputStreamReader(in));
	    if(jsonObject.get("status").equals("200")) {

	    	JSONObject systemInfo = (JSONObject) jsonObject.get("systemInfo");
	    	
			Double totalMemory =  (Double) systemInfo.get("totalMemory");
			Double freeMemory= (Double) systemInfo.get("freeMemory");
			Double maxMemory= (Double) systemInfo.get("maxMemory");
			Double nonHeapMemoryUsage=  (Double) systemInfo.get("nonHeapMemoryUsage");
			Long connectedUsers =  (Long) systemInfo.get("connectedUsers");
			
			Object[] valueObjects  =  new Object[] { totalMemory, freeMemory, maxMemory, nonHeapMemoryUsage, connectedUsers, instance.getId() };
			
			if(logger.isDebugEnabled()) {
				logger.debug("Insert statistics for :"+instance.getInstanceUrl());
				logger.debug("Values:(`total_memory`,`free_memory`,`max_memory`,`non_heap_memory_usage`,`connected_users`,`instance_id`)");
				logger.debug(" - "+Arrays.asList(valueObjects).toString());
			}
			
			JdbcUtils.update(connection, "insert into `becpg_statistics` " + "(`total_memory`,`free_memory`,`max_memory`,`non_heap_memory_usage`,`connected_users`,`instance_id`) "
					+ " values (?,?,?,?,?,?)", valueObjects);

	    } else {
	    	throw new IllegalStateException("Incorrect return status ("+jsonObject.get("status")+") for instance: "+instance.getInstanceUrl());
	    }
	    

	}





}
