/*
Copyright (C) 2010-2014 beCPG. 
 
This file is part of beCPG 
 
beCPG is free software: you can redistribute it and/or modify 
it under the terms of the GNU Lesser General Public License as published by 
the Free Software Foundation, either version 3 of the License, or 
(at your option) any later version. 
 
beCPG is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 

MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
GNU Lesser General Public License for more details. 
 
You should have received a copy of the GNU Lesser General Public License 
along with beCPG. If not, see <http://www.gnu.org/licenses/>.
*/
package fr.becpg.repo.helper;

import org.activiti.engine.impl.util.json.JSONArray;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * @author matthieu
 *
 */
public class CompareHelper {

	public static final String JSON_COMP_ITEMS = "comp";
	public static final String JSON_COMP_VALUE = "value";
	public static final String JSON_COMP_ITEM_NODEREF = "nodeRef";
	
	
	private static Log logger = LogFactory.getLog(CompareHelper.class);
	
	/**
	 * @param strValue1
	 * @return
	 */
	public static String cleanCompareJSON(String value) {
		if(value!=null && value.contains(JSON_COMP_ITEMS)) {
			try {
				JSONParser parser=new JSONParser();
				JSONObject jsonObject = (JSONObject) parser.parse(value);
				JSONArray array = (JSONArray) jsonObject.get(JSON_COMP_ITEMS);
				return (String) ((JSONObject)array.get(0)).get(JSON_COMP_VALUE);
			} catch (Exception e) {
				logger.warn("Cannot parse "+value,e);
			}
		}
		return value;
	}

}
