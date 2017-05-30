/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG. 
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
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
public class OlapChartMetadata {

	private final int colIndex;
	private final String colType;
	private final String colName;
	

	
	public OlapChartMetadata(int colIndex, String colType, String colName) {
		super();
		this.colIndex = colIndex;
		this.colType = colType;
		this.colName = colName;
	}
	public int getColIndex() {
		return colIndex;
	}
	public String getColType() {
		return colType;
	}
	public String getColName() {
		return colName;
	}
	
	public JSONObject toJSONObject() throws JSONException {
		JSONObject obj = new JSONObject();
		
		obj.put("colIndex", colIndex);
		obj.put("colType", colType);
		obj.put("colName", colName);
		
		return  obj;
	}
	
	

}
