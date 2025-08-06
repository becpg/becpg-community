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

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Use to store Olap Chart Data
 *
 * @author "Matthieu Laborie"
 * @version $Id: $Id
 */
public class OlapChartData {
	
	List<OlapChartMetadata> metadatas = new ArrayList<>();
	List<List<Object>> resultsets = new ArrayList<>();
	

	/**
	 * <p>addMetadata.</p>
	 *
	 * @param olapChartMetadata a {@link fr.becpg.repo.olap.data.OlapChartMetadata} object.
	 */
	public void addMetadata(OlapChartMetadata olapChartMetadata) {
		metadatas.add(olapChartMetadata);
		
	}

	/**
	 * <p>shiftMetadata.</p>
	 */
	public void shiftMetadata() {
		if(!metadatas.isEmpty()){
			metadatas.remove(0);
		}
	}

	/**
	 * <p>Getter for the field <code>metadatas</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<OlapChartMetadata> getMetadatas() {
		return metadatas;
	}

	/**
	 * <p>Setter for the field <code>metadatas</code>.</p>
	 *
	 * @param metadatas a {@link java.util.List} object.
	 */
	public void setMetadatas(List<OlapChartMetadata> metadatas) {
		this.metadatas = metadatas;
	}

	/**
	 * <p>Getter for the field <code>resultsets</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<List<Object>> getResultsets() {
		return resultsets;
	}

	/**
	 * <p>Setter for the field <code>resultsets</code>.</p>
	 *
	 * @param resultsets a {@link java.util.List} object.
	 */
	public void setResultsets(List<List<Object>> resultsets) {
		this.resultsets = resultsets;
	}

	/**
	 * <p>toJSONObject.</p>
	 *
	 * @return a {@link org.json.JSONObject} object.
	 * @throws org.json.JSONException if any.
	 */
	public JSONObject toJSONObject() throws JSONException {
		JSONObject obj = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		for (OlapChartMetadata metadata : metadatas) {
			jsonArray.put(metadata.toJSONObject());
		}
		obj.put("metadatas", jsonArray);
		obj.put("resultsets", resultsets);
		
		return  obj;
	}

	
	
}
