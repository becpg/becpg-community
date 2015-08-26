/*
Copyright (C) 2010-2015 beCPG. 
 
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import fr.becpg.config.format.PropertyFormats;

/**
 * @author matthieu
 *
 */
public class JsonFormulaHelper {

	
	private static final Log logger = LogFactory.getLog(JsonFormulaHelper.class);
	
	public static final String JSON_COMP_ITEMS = "comp";
	public static final String JSON_VALUE = "value";
	public static final String JSON_NODEREF = "nodeRef";
	public static final String JSON_PATH = "path";
	public static final String JSON_SUB_VALUES = "sub";
    public static final String JSON_DISPLAY_VALUE = "displayValue";
	
	
	
	/**
	 * @param strValue1
	 * @return
	 */
	public static Object cleanCompareJSON(String value) {
		if(value!=null && value.contains(JSON_COMP_ITEMS)) {
			try {
				JSONTokener tokener = new JSONTokener(value);
				JSONObject jsonObject = new JSONObject(tokener);
				JSONArray array = (JSONArray) jsonObject.get(JSON_COMP_ITEMS);
				return((JSONObject)array.get(0)).get(JSON_VALUE);
			} catch (Exception e) {
				logger.warn("Cannot parse "+value,e);
			}
		}
		
		if(value!=null && value.contains(JSON_SUB_VALUES)) {
			try {
				JSONObject jsonObject = new JSONObject(value);
				if(jsonObject.has(JSON_VALUE)){
					return jsonObject.get(JSON_VALUE);
				}
				return "";
			} catch (Exception e) {
				logger.warn("Cannot parse "+value,e);
			}
		}
		
		return value;
	}
	
	public static Object formatValue(Object v) {
		PropertyFormats propertyFormats = new PropertyFormats(true);
		
		if (v != null && (v instanceof Double || v instanceof Float)) {
			return propertyFormats.formatDecimal(v);
		}
		return v;
	}

}
