package fr.becpg.web.evaluator;

import org.alfresco.web.evaluator.BaseEvaluator;
import org.json.simple.JSONObject;

public class HasPermissionEvaluator extends BaseEvaluator {

	private String permission;
	
	public void setPermission(String permission) {
		this.permission = permission;
	}
	
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
