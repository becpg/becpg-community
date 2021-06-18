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
package fr.becpg.repo.olap.data;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Provide Metadata rapport
 *
 * @author "Matthieu Laborie"
 * @version $Id: $Id
 */
public class OlapChartMetadata {

	private final int colIndex;
	private final String colType;
	private final String colName;
	

	
	/**
	 * <p>Constructor for OlapChartMetadata.</p>
	 *
	 * @param colIndex a int.
	 * @param colType a {@link java.lang.String} object.
	 * @param colName a {@link java.lang.String} object.
	 */
	public OlapChartMetadata(int colIndex, String colType, String colName) {
		super();
		this.colIndex = colIndex;
		this.colType = colType;
		this.colName = colName;
	}
	/**
	 * <p>Getter for the field <code>colIndex</code>.</p>
	 *
	 * @return a int.
	 */
	public int getColIndex() {
		return colIndex;
	}
	/**
	 * <p>Getter for the field <code>colType</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getColType() {
		return colType;
	}
	/**
	 * <p>Getter for the field <code>colName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getColName() {
		return colName;
	}
	
	/**
	 * <p>toJSONObject.</p>
	 *
	 * @return a {@link org.json.JSONObject} object.
	 * @throws org.json.JSONException if any.
	 */
	public JSONObject toJSONObject() throws JSONException {
		JSONObject obj = new JSONObject();
		
		obj.put("colIndex", colIndex);
		obj.put("colType", colType);
		obj.put("colName", colName);
		
		return  obj;
	}
	
	

}
