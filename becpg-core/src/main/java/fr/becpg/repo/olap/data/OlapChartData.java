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

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Use to store Olap Chart Data
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
public class OlapChartData {
	
	List<OlapChartMetadata> metadatas = new LinkedList<>();
	List<List<Object>> resultsets = new LinkedList<>();
	

	public void addMetadata(OlapChartMetadata olapChartMetadata) {
		metadatas.add(olapChartMetadata);
		
	}

	public void shiftMetadata() {
		if(!metadatas.isEmpty()){
			metadatas.remove(0);
		}
	}

	public List<OlapChartMetadata> getMetadatas() {
		return metadatas;
	}

	public void setMetadatas(List<OlapChartMetadata> metadatas) {
		this.metadatas = metadatas;
	}

	public List<List<Object>> getResultsets() {
		return resultsets;
	}

	public void setResultsets(List<List<Object>> resultsets) {
		this.resultsets = resultsets;
	}

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
