package fr.becpg.repo.activity;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.JSONObject;

import fr.becpg.repo.activity.data.ActivityEvent;
import fr.becpg.repo.activity.data.ActivityType;

public interface EntityActivityService {

	void postCommentActivity(NodeRef entityNodeRef, NodeRef commentNodeRef, ActivityEvent activityEvent);

	void postContentActivity(NodeRef entityNodeRef,NodeRef contentNodeRef, ActivityEvent activityEvent);

	void postDatalistActivity(NodeRef entityNodeRef, NodeRef datalistItemNodeRef, ActivityEvent activityEvent);

	void postEntityActivity(NodeRef entityNodeRef, ActivityEvent activityEvent);

	JSONObject postActivityLookUp(ActivityType activityType, String value);

	NodeRef getEntityNodeRef(NodeRef nodeRef, QName itemType);

	
}
