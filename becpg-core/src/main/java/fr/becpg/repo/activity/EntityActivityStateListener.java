package fr.becpg.repo.activity;

import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.activity.data.ActivityListDataItem;
import fr.becpg.repo.activity.data.ActivityType;
import fr.becpg.repo.helper.AttributeExtractorService;

@Service
public class EntityActivityStateListener implements EntityActivityListener {

	public static final String ENTITY_STATE_ACTIVITY = "fr.becpg.entity.state-changed";

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
	
		boolean isMatch = false;
		for(EntityActivityPlugin entityActivityPlugin : entityActivityPlugins){	
			if (entityActivityPlugin.isMatchingEntityType(nodeService.getType(entityNodeRef))){
				isMatch = true;
				break;
			}
		}
	
		if (activityListDataItem.getActivityType().equals(ActivityType.State) && isMatch) {
			activityService.postActivity(ENTITY_STATE_ACTIVITY, attributeExtractorService.extractSiteId(entityNodeRef), "entity",
					activityListDataItem.getActivityData());
		}


	}

}
