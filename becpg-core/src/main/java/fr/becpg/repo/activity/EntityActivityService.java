package fr.becpg.repo.activity;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.JSONObject;

import fr.becpg.repo.activity.data.ActivityEvent;
import fr.becpg.repo.activity.data.ActivityType;

public interface EntityActivityService {

	static final String PROP_COMMENT_NODEREF = "commentNodeRef";
	static final String PROP_CONTENT_NODEREF = "contentNodeRef";
	static final String PROP_DATALIST_NODEREF = "datalistNodeRef";
	static final String PROP_ENTITY_NODEREF = "entityNodeRef";
	static final String PROP_ACTIVITY_EVENT = "activityEvent";
	static final String PROP_CLASSNAME = "className";
	static final String PROP_TITLE = "title";

	boolean postCommentActivity(NodeRef entityNodeRef, NodeRef commentNodeRef, ActivityEvent activityEvent);

	boolean postContentActivity(NodeRef entityNodeRef, NodeRef contentNodeRef, ActivityEvent activityEvent);

	boolean postDatalistActivity(NodeRef entityNodeRef, NodeRef datalistItemNodeRef, ActivityEvent activityEvent);

	boolean postEntityActivity(NodeRef entityNodeRef, ActivityEvent activityEvent);

	boolean postStateChangeActivity(NodeRef entityNodeRef, NodeRef datalistItemNodeRef, String beforeState, String afterState);

	JSONObject postActivityLookUp(ActivityType activityType, String value);

	NodeRef getEntityNodeRef(NodeRef nodeRef, QName itemType);

}
