package fr.becpg.repo.activity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.json.JSONObject;


import fr.becpg.repo.activity.data.ActivityEvent;
import fr.becpg.repo.activity.data.ActivityListDataItem;
import fr.becpg.repo.activity.data.ActivityType;
import fr.becpg.repo.batch.BatchInfo;

/**
 * <p>EntityActivityService interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface EntityActivityService {

	/** Constant <code>PROP_COMMENT_NODEREF="commentNodeRef"</code> */
	static final String PROP_COMMENT_NODEREF = "commentNodeRef";
	/** Constant <code>PROP_CONTENT_NODEREF="contentNodeRef"</code> */
	static final String PROP_CONTENT_NODEREF = "contentNodeRef";
	/** Constant <code>PROP_DATALIST_NODEREF="datalistNodeRef"</code> */
	static final String PROP_DATALIST_NODEREF = "datalistNodeRef";
	/** Constant <code>PROP_DATALIST_TYPE="datalistType"</code> */
	static final String PROP_DATALIST_TYPE= "datalistType";
	/** Constant <code>PROP_ENTITY_NODEREF="entityNodeRef"</code> */
	static final String PROP_ENTITY_NODEREF = "entityNodeRef";
	
	static final String PROP_PARENT_NAME = "parentName";
	/** Constant <code>PROP_ENTITY_TYPE="entityType"</code> */
	static final String PROP_ENTITY_TYPE= "entityType";
	/** Constant <code>PROP_CHARACT_NODEREF="charactNodeRef"</code> */
	static final String PROP_CHARACT_NODEREF = "charactNodeRef";
	/** Constant <code>PROP_CHARACT_TYPE="charactType"</code> */
	static final String PROP_CHARACT_TYPE = "charactType";
	/** Constant <code>PROP_ACTIVITY_EVENT="activityEvent"</code> */
	static final String PROP_ACTIVITY_EVENT = "activityEvent";
	/** Constant <code>PROP_CLASSNAME="className"</code> */
	static final String PROP_CLASSNAME = "className";
	/** Constant <code>PROP_TITLE="title"</code> */
	static final String PROP_TITLE = "title";
	/** Constant <code>PROP_BRANCH_TITLE="branchTitle"</code> */
	static final String PROP_BRANCH_TITLE = "branchTitle";
	/** Constant <code>PROP_VERSION_NODEREF="versionNodeRef"</code> */
	static final String PROP_VERSION_NODEREF="versionNodeRef";
	/** Constant <code>PROP_VERSION_LABEL="versionLabel"</code> */
	static final String PROP_VERSION_LABEL="versionLabel";
	/** Constant <code>PROP_BEFORE_STATE="beforeState"</code> */
	static final String PROP_BEFORE_STATE = "beforeState";
	/** Constant <code>PROP_AFTER_STATE="afterState"</code> */
	static final String PROP_AFTER_STATE = "afterState";
	
	/** Constant <code>PROP_PROPERTIES="properties"</code> */
	static final String PROP_PROPERTIES= "properties";
	/** Constant <code>BEFORE="before"</code> */
	static final String BEFORE = "before";
	/** Constant <code>AFTER="after"</code> */
	static final String AFTER = "after";
	

	

	/**
	 * <p>postCommentActivity.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param actionedUponNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param activityEvent a {@link fr.becpg.repo.activity.data.ActivityEvent} object.
	 * @return a boolean.
	 */
	boolean postCommentActivity(NodeRef entityNodeRef, NodeRef actionedUponNodeRef, ActivityEvent activityEvent);
	
	/**
	 * <p>postCommentActivity.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param commentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param activityEvent a {@link fr.becpg.repo.activity.data.ActivityEvent} object.
	 * @param notifyObservers a boolean.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	ActivityListDataItem postCommentActivity(NodeRef entityNodeRef, NodeRef commentNodeRef, ActivityEvent activityEvent, boolean notifyObservers);

	/**
	 * <p>postContentActivity.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param contentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param activityEvent a {@link fr.becpg.repo.activity.data.ActivityEvent} object.
	 * @return a boolean.
	 */
	boolean postContentActivity(NodeRef entityNodeRef, NodeRef contentNodeRef, ActivityEvent activityEvent);

	/**
	 * <p>postDatalistActivity.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param datalistItemNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param activityEvent a {@link fr.becpg.repo.activity.data.ActivityEvent} object.
	 * @param updatedProperties a {@link java.util.Map} object.
	 * @return a boolean.
	 */
	boolean postDatalistActivity(NodeRef entityNodeRef, NodeRef datalistItemNodeRef, ActivityEvent activityEvent,Map<QName,Pair<Serializable,Serializable>> updatedProperties);
	
	/**
	 * <p>postVersionActivity.</p>
	 *
	 * @param origNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param versionNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param versionLabel a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	boolean postVersionActivity(NodeRef origNodeRef, NodeRef versionNodeRef, String versionLabel);
	
	/**
	 * <p>postMergeBranchActivity.</p>
	 *
	 * @param branchNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param branchToNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param versionType a {@link org.alfresco.service.cmr.version.VersionType} object.
	 * @param description a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	boolean postMergeBranchActivity(NodeRef branchNodeRef, NodeRef branchToNodeRef, VersionType versionType, String description);
	
	/**
	 * <p>postEntityActivity.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param activityType a {@link fr.becpg.repo.activity.data.ActivityType} object.
	 * @param activityEvent a {@link fr.becpg.repo.activity.data.ActivityEvent} object.
	 * @param updatedProperties a {@link java.util.Map} object.
	 * @return a boolean.
	 */
	boolean postEntityActivity(NodeRef entityNodeRef, ActivityType activityType, ActivityEvent activityEvent, Map<QName,Pair<List<Serializable>,List<Serializable>>> updatedProperties);

	/**
	 * <p>postStateChangeActivity.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param datalistItemNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param beforeState a {@link java.lang.String} object.
	 * @param afterState a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	boolean postStateChangeActivity(NodeRef entityNodeRef, NodeRef datalistItemNodeRef, String beforeState, String afterState);

	/**
	 * <p>postActivityLookUp.</p>
	 *
	 * @param activityType a {@link fr.becpg.repo.activity.data.ActivityType} object.
	 * @param value a {@link java.lang.String} object.
	 * @return a {@link org.json.JSONObject} object.
	 */
	JSONObject postActivityLookUp(ActivityType activityType, String value);

	/**
	 * <p>getEntityNodeRefForActivity.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param itemType a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getEntityNodeRefForActivity(NodeRef nodeRef, QName itemType);

	/**
	 * <p>cleanActivities.</p>
	 */
	BatchInfo cleanActivities();

	/**
	 * <p>mergeActivities.</p>
	 *
	 * @param fromNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param toNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void mergeActivities(NodeRef fromNodeRef, NodeRef toNodeRef);
	
	/**
	 * <p>clearAllActivities.</p>
	 *
	 * @param entityTplNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void clearAllActivities(NodeRef entityTplNodeRef);

	/**
	 * <p>isMatchingStateProperty.</p>
	 *
	 * @param propName a {@link org.alfresco.service.namespace.QName} object.
	 * @return a boolean.
	 */
	boolean isMatchingStateProperty(QName propName);

	/**
	 * <p>isIgnoreStateProperty.</p>
	 *
	 * @param propName a {@link org.alfresco.service.namespace.QName} object.
	 * @return a boolean.
	 */
	boolean isIgnoreStateProperty(QName propName);

	void postExportActivity(NodeRef entityNodeRef, QName dataType, String fileName);

	boolean postChangeOrderActivity(NodeRef entityNodeRef, NodeRef changeOrderNodeRef);

	void postDataListCopyActivity(NodeRef entityNodeRef, NodeRef sourceEntityNodeRef, NodeRef sourceListNodeRef, String action);

}
