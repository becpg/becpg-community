package fr.becpg.repo.glop.model;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;

public class GlopData extends JSONObject {

	public static final String CONSTRAINTS = "constraints";
	public static final String VARIABLES = "variables";
	public static final String OBJECTIVE = "objective";
	public static final String OPTIMAL = "optimal";
	public static final String SUBOPTIMAL = "suboptimal";
	public static final String COEFFICIENTS = "coefficients";
	public static final String STATUS = "status";
	public static final String COMPONENTS = "components";
	public static final String VALUE = "value";
	public static final String NAME = "name";
	public static final String ID = "id";

	public double getComponentValue(NodeRef nodeRef) throws JSONException {
		JSONArray comps = getJSONArray(COMPONENTS);
		
		for (int index = 0; index < comps.length(); index++) {
			JSONObject comp = (JSONObject) comps.get(index);
			if (comp.has(ID) && comp.getString(ID).equals(nodeRef.toString())) {
				return comp.getDouble(VALUE);
			}
		}
		
		return 0;
	}
	
	public double getComponentValue(String name) throws JSONException {
		JSONArray comps = getJSONArray(COMPONENTS);
		
		for (int index = 0; index < comps.length(); index++) {
			JSONObject comp = (JSONObject) comps.get(index);
			if (comp.has("name") && comp.getString("name").equals(name)) {
				return comp.getDouble(VALUE);
			}
		}
		
		return 0;
	}
	
	public String getStatus() throws JSONException {
		return getString(STATUS);
	}
	
	public void applyValues(ProductData product, boolean apply) throws JSONException {
		if (apply) {
			JSONArray comps = getJSONArray(COMPONENTS);
			
			for (int index = 0; index < comps.length(); index++) {
				JSONObject comp = (JSONObject) comps.get(index);
				if (comp.has(ID)) {
					for (CompoListDataItem compoListItem : product.getCompoList()) {
						if (comp.get(ID).equals(compoListItem.getProduct().toString())) {
							compoListItem.setQtySubFormula(comp.getDouble(VALUE));
							break;
						}
					}
				}
			}
		}
	}
	
}
