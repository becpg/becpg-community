package fr.becpg.repo.activity.helper;

import org.alfresco.util.ISO8601DateFormat;
import org.json.JSONObject;

import fr.becpg.repo.activity.data.ActivityListDataItem;
import fr.becpg.repo.activity.data.ActivityType;

public class AuditActivityHelper {
	
	private AuditActivityHelper() {
		
	}

	public static ActivityListDataItem parseActivity(JSONObject result) {
		
		ActivityListDataItem activityListDataItem = new ActivityListDataItem();
		
		activityListDataItem.setId(result.getLong("id"));
		activityListDataItem.setUserId(result.getString("prop_bcpg_alUserId"));
		activityListDataItem.setActivityType(ActivityType.valueOf(result.getString("prop_bcpg_alType")));
		activityListDataItem.setActivityData(result.getString("prop_bcpg_alData"));
		activityListDataItem.setCreated(ISO8601DateFormat.parse(result.getString("prop_cm_created")));

		return activityListDataItem;
	}
	
	public static String serializeActivity(ActivityListDataItem activityListDataItem) {

		JSONObject result = new JSONObject();
		
		result.put("prop_bcpg_alUserId", activityListDataItem.getUserId());
		result.put("prop_bcpg_alType", activityListDataItem.getActivityType());
		result.put("prop_bcpg_alData", activityListDataItem.getActivityData());
		result.put("prop_cm_created", activityListDataItem.getCreatedDate());

		return result.toString();
	}
	
}
