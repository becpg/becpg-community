package fr.becpg.repo.activity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.json.JSONObject;

import com.google.gdata.util.common.base.Pair;

import fr.becpg.repo.activity.data.ActivityEvent;
import fr.becpg.repo.activity.data.ActivityType;

/**
 * 
 * @author matthieu
 *
 */
public interface EntityActivityService {

	static final String PROP_COMMENT_NODEREF = "commentNodeRef";
	static final String PROP_CONTENT_NODEREF = "contentNodeRef";
	static final String PROP_DATALIST_NODEREF = "datalistNodeRef";
	static final String PROP_ENTITY_NODEREF = "entityNodeRef";
	static final String PROP_ACTIVITY_EVENT = "activityEvent";
	static final String PROP_CLASSNAME = "className";
	static final String PROP_TITLE = "title";
	static final String PROP_BRANCH_TITLE = "branchTitle";
	static final String PROP_VERSION_NODEREF="versionNodeRef";
	static final String PROP_VERSION_LABEL="versionLabel";
	static final String PROP_BEFORE_STATE = "beforeState";
	static final String PROP_AFTER_STATE = "afterState";
	
	static final String PROP_PROPERTIES= "properties";
	static final String BEFORE = "before";
	static final String AFTER = "after";
	

	

	boolean postCommentActivity(NodeRef entityNodeRef, NodeRef actionedUponNodeRef, ActivityEvent activityEvent);
	
	NodeRef postCommentActivity(NodeRef entityNodeRef, NodeRef commentNodeRef, ActivityEvent activityEvent, boolean notifyObservers);

	boolean postContentActivity(NodeRef entityNodeRef, NodeRef contentNodeRef, ActivityEvent activityEvent);

	boolean postDatalistActivity(NodeRef entityNodeRef, NodeRef datalistItemNodeRef, ActivityEvent activityEvent,Map<QName,Pair<Serializable,Serializable>> updatedProperties);
	
	boolean postVersionActivity(NodeRef origNodeRef, NodeRef versionNodeRef, String versionLabel);
	
	boolean postMergeBranchActivity(NodeRef branchNodeRef, NodeRef branchToNodeRef, VersionType versionType, String description);
	
	boolean postEntityActivity(NodeRef entityNodeRef, ActivityType activityType, ActivityEvent activityEvent, Map<QName,Pair<List<Serializable>,List<Serializable>>> updatedProperties);

	boolean postStateChangeActivity(NodeRef entityNodeRef, NodeRef datalistItemNodeRef, String beforeState, String afterState);

	JSONObject postActivityLookUp(ActivityType activityType, String value);

	NodeRef getEntityNodeRefForActivity(NodeRef nodeRef, QName itemType);

	void cleanActivities();

	void mergeActivities(NodeRef fromNodeRef, NodeRef toNodeRef);

	boolean isMatchingStateProperty(QName propName);

	boolean isIgnoreStateProperty(QName propName);





	
}
