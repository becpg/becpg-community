package fr.becpg.repo.olap.data;

import java.util.Iterator;
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
	
	List<OlapChartMetadata> metadatas = new LinkedList<OlapChartMetadata>();
	List<List<Object>> resultsets = new LinkedList<List<Object>>();
	

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
		for (Iterator<OlapChartMetadata> iterator = metadatas.iterator(); iterator.hasNext();) {
			jsonArray.put(iterator.next().toJSONObject());	
		}
		obj.put("metadatas", jsonArray);
		obj.put("resultsets", resultsets);
		
		return  obj;
	}

	
	
}
