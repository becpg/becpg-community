package fr.becpg.repo.glop.model;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;

/**
 * <p>GlopData class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class GlopData extends JSONObject {

	/** Constant <code>CONSTRAINTS="constraints"</code> */
	public static final String CONSTRAINTS = "constraints";
	/** Constant <code>VARIABLES="variables"</code> */
	public static final String VARIABLES = "variables";
	/** Constant <code>OBJECTIVE="objective"</code> */
	public static final String OBJECTIVE = "objective";
	/** Constant <code>OPTIMAL="optimal"</code> */
	public static final String OPTIMAL = "optimal";
	/** Constant <code>SUBOPTIMAL="suboptimal"</code> */
	public static final String SUBOPTIMAL = "suboptimal";
	/** Constant <code>COEFFICIENTS="coefficients"</code> */
	public static final String COEFFICIENTS = "coefficients";
	/** Constant <code>STATUS="status"</code> */
	public static final String STATUS = "status";
	/** Constant <code>COMPONENTS="components"</code> */
	public static final String COMPONENTS = "components";
	/** Constant <code>VALUE="value"</code> */
	public static final String VALUE = "value";
	/** Constant <code>NAME="name"</code> */
	public static final String NAME = "name";
	/** Constant <code>ID="id"</code> */
	public static final String ID = "id";

	/**
	 * <p>getComponentValue.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a double
	 * @throws org.json.JSONException if any.
	 */
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
	
	/**
	 * <p>getComponentValue.</p>
	 *
	 * @param name a {@link java.lang.String} object
	 * @return a double
	 * @throws org.json.JSONException if any.
	 */
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
	
	/**
	 * <p>getStatus.</p>
	 *
	 * @return a {@link java.lang.String} object
	 * @throws org.json.JSONException if any.
	 */
	public String getStatus() throws JSONException {
		return getString(STATUS);
	}
	
	/**
	 * <p>applyValues.</p>
	 *
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param apply a boolean
	 * @throws org.json.JSONException if any.
	 */
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
