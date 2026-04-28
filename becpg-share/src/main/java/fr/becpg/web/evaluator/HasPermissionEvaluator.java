package fr.becpg.web.evaluator;

import org.alfresco.web.evaluator.BaseEvaluator;
import org.json.simple.JSONObject;

/**
 * <p>HasPermissionEvaluator class.</p>
 *
 * @author matthieu
 */
public class HasPermissionEvaluator extends BaseEvaluator {

	private String permission;
	
	/**
	 * <p>Setter for the field <code>permission</code>.</p>
	 *
	 * @param permission a {@link java.lang.String} object
	 */
	public void setPermission(String permission) {
		this.permission = permission;
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean evaluate(JSONObject jsonObject) {
        JSONObject node = (JSONObject) jsonObject.get("node");
        if (node != null) {
        	JSONObject permissions = (JSONObject) node.get("permissions");
        	if (permissions != null) {
        		JSONObject userPermissions = (JSONObject) permissions.get("user");
        		if (userPermissions != null) {
        			Boolean userPermission = (Boolean) userPermissions.get(permission);
        			return Boolean.TRUE.equals(userPermission);
        		}
        	}
        }
		return false;
	}

}
