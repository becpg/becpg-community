package fr.becpg.repo.activity;

import java.util.Date;

import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.activity.data.ActivityListDataItem;
import fr.becpg.repo.activity.data.ActivityType;
import fr.becpg.repo.helper.AttributeExtractorService;

@Service
public class EntityActivityStateListener implements EntityActivityListener {

	public static final String ENTITY_STATE_ACTIVITY = "fr.becpg.entity.state-changed";

	private static Log logger = LogFactory.getLog(EntityActivityStateListener.class);

	@Autowired
	private ActivityService activityService;

	@Autowired
	private AttributeExtractorService attributeExtractorService;

	@Autowired
	private EntityActivityPlugin[] entityActivityPlugins;

	@Autowired
	private NodeService nodeService;

	@Override
	public void notify(NodeRef entityNodeRef, ActivityListDataItem activityListDataItem) {
		if (ActivityType.State.equals(activityListDataItem.getActivityType())) {
			boolean isMatch = false;
			for (EntityActivityPlugin entityActivityPlugin : entityActivityPlugins) {
				if (entityActivityPlugin.isMatchingEntityType(nodeService.getType(entityNodeRef))) {
					isMatch = true;
					break;
				}
			}

			if (isMatch) {

				nodeService.setProperty(entityNodeRef, BeCPGModel.PROP_STATE_ACTIVITY_MODIFIED, new Date());
				nodeService.setProperty(entityNodeRef, BeCPGModel.PROP_STATE_ACTIVITY_MODIFIER, activityListDataItem.getUserId());

				try {
					JSONObject data = activityListDataItem.getJSONData();

					if (data.has(EntityActivityService.PROP_BEFORE_STATE)) {
						nodeService.setProperty(entityNodeRef, BeCPGModel.PROP_STATE_ACTIVITY_PREVIOUSSTATE, (String) data.get("beforeState"));
					}
				} catch (JSONException e) {
					logger.error(e, e);
				}

				activityService.postActivity(ENTITY_STATE_ACTIVITY, attributeExtractorService.extractSiteId(entityNodeRef), "entity",
						activityListDataItem.getActivityData());
			}
		}
	}

}
