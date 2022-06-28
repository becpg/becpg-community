package fr.becpg.repo.glop.model;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;

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
	
	public double getComponentValue(String name) throws JSONException {
		JSONArray comps = getJSONArray("components");
		
		for (int index = 0; index < comps.length(); index++) {
			JSONObject comp = (JSONObject) comps.get(index);
			if (comp.has("name") && comp.getString("name").equals(name)) {
				return comp.getDouble("value");
			}
		}
		
		return 0;
	}
	
	public String getStatus() throws JSONException {
		return getString("status");
	}
	
	public void applyValues(ProductData product, boolean apply) throws JSONException {
		if (apply) {
			JSONArray comps = getJSONArray("components");
			
			for (int index = 0; index < comps.length(); index++) {
				JSONObject comp = (JSONObject) comps.get(index);
				if (comp.has("id")) {
					for (CompoListDataItem compoListItem : product.getCompoList()) {
						if (comp.get("id").equals(compoListItem.getProduct().toString())) {
							compoListItem.setQtySubFormula(comp.getDouble("value"));
							break;
						}
					}
				}
			}
		}
	}
	
}
