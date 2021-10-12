package fr.becpg.repo.glop.model;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GlopData extends JSONObject {

	public double getComponentValue(NodeRef nodeRef) throws JSONException {
		JSONArray comps = getJSONArray("components");
		
		for (int index = 0; index < comps.length(); index++) {
			JSONObject comp = (JSONObject) comps.get(index);
			if (comp.has("id") && comp.getString("id").equals(nodeRef.toString())) {
				return comp.getDouble("value");
			}
		}
		
		return 0;
	}
	
	public String getStatus() throws JSONException {
		return getString("status");
	}
	
}
