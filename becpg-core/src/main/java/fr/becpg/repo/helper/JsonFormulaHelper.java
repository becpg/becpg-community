/*
Copyright (C) 2010-2020 beCPG. 
 
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

import fr.becpg.config.format.FormatMode;
import fr.becpg.config.format.PropertyFormatService;

/**
 * <p>JsonFormulaHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class JsonFormulaHelper {

	
	private static final Log logger = LogFactory.getLog(JsonFormulaHelper.class);
	
	/** Constant <code>JSON_COMP_ITEMS="comp"</code> */
	public static final String JSON_COMP_ITEMS = "comp";
	/** Constant <code>JSON_VALUE="value"</code> */
	public static final String JSON_VALUE = "value";
	/** Constant <code>JSON_NODEREF="nodeRef"</code> */
	public static final String JSON_NODEREF = "nodeRef";
	/** Constant <code>JSON_PATH="path"</code> */
	public static final String JSON_PATH = "path";
	/** Constant <code>JSON_SUB_VALUES="sub"</code> */
	public static final String JSON_SUB_VALUES = "sub";
    /** Constant <code>JSON_DISPLAY_VALUE="displayValue"</code> */
    public static final String JSON_DISPLAY_VALUE = "displayValue";
	
	
	
	/**
	 * <p>cleanCompareJSON.</p>
	 *
	 * @param value a {@link java.lang.String} object.
	 * @return a {@link java.lang.Object} object.
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
	
	/**
	 * <p>formatValue.</p>
	 *
	 * @param v a {@link java.lang.Object} object.
	 * @return a {@link java.lang.Object} object.
	 */
	public static Object formatValue(Object v) {
		
		if (v != null && (v instanceof Double || v instanceof Float)) {
			return PropertyFormatService.getInstance().getPropertyFormats(FormatMode.JSON,true).formatDecimal(v);
		}
		return v;
	}

}
