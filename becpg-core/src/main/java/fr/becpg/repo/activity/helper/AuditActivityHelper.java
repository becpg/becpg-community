package fr.becpg.repo.activity.helper;

import org.alfresco.util.ISO8601DateFormat;
import org.json.JSONObject;

import fr.becpg.repo.activity.data.ActivityListDataItem;
import fr.becpg.repo.activity.data.ActivityType;
import fr.becpg.repo.audit.plugin.AuditPlugin;
import fr.becpg.repo.audit.plugin.impl.ActivityAuditPlugin;

/**
 * <p>AuditActivityHelper class.</p>
 *
 * @author matthieu
 */
public class AuditActivityHelper {
	
	private AuditActivityHelper() {
		
	}

	/**
	 * <p>parseActivity.</p>
	 *
	 * @param result a {@link org.json.JSONObject} object
	 * @return a {@link fr.becpg.repo.activity.data.ActivityListDataItem} object
	 */
	public static ActivityListDataItem parseActivity(JSONObject result) {
		
		ActivityListDataItem activityListDataItem = new ActivityListDataItem();
		
		activityListDataItem.setId(result.getLong(AuditPlugin.ID));
		activityListDataItem.setUserId(result.getString(ActivityAuditPlugin.PROP_BCPG_AL_USER_ID));
		activityListDataItem.setActivityType(ActivityType.valueOf(result.getString(ActivityAuditPlugin.PROP_BCPG_AL_TYPE)));
		activityListDataItem.setActivityData(result.getString(ActivityAuditPlugin.PROP_BCPG_AL_DATA));
		activityListDataItem.setCreated(ISO8601DateFormat.parse(result.getString(ActivityAuditPlugin.PROP_CM_CREATED)));

		return activityListDataItem;
	}
	
	/**
	 * <p>serializeActivity.</p>
	 *
	 * @param activityListDataItem a {@link fr.becpg.repo.activity.data.ActivityListDataItem} object
	 * @return a {@link java.lang.String} object
	 */
	public static String serializeActivity(ActivityListDataItem activityListDataItem) {

		JSONObject result = new JSONObject();
		
		result.put(ActivityAuditPlugin.PROP_BCPG_AL_USER_ID, activityListDataItem.getUserId());
		result.put(ActivityAuditPlugin.PROP_BCPG_AL_TYPE, activityListDataItem.getActivityType());
		result.put(ActivityAuditPlugin.PROP_BCPG_AL_DATA, activityListDataItem.getActivityData());
		result.put(ActivityAuditPlugin.PROP_CM_CREATED, activityListDataItem.getCreatedDate());

		return result.toString();
	}
	
}
