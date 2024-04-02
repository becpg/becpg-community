/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.project.impl;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.activities.post.lookup.PostLookup;
import org.alfresco.repo.forum.CommentService;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.activity.EntityActivityListener;
import fr.becpg.repo.activity.EntityActivityPlugin;
import fr.becpg.repo.activity.EntityActivityService;
import fr.becpg.repo.activity.data.ActivityEvent;
import fr.becpg.repo.activity.data.ActivityListDataItem;
import fr.becpg.repo.activity.data.ActivityType;
import fr.becpg.repo.activity.helper.AuditActivityHelper;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.project.ProjectActivityService;
import fr.becpg.repo.project.ProjectNotificationService;
import fr.becpg.repo.repository.AlfrescoRepository;

/**
 * <p>ProjectActivityServiceImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("projectActivityService")
public class ProjectActivityServiceImpl implements ProjectActivityService, EntityActivityListener, EntityActivityPlugin {

	private static final String PROJECT_ACTIVITY_TYPE = "fr.becpg.project";

	/** Constant <code>PROJECT_STATE_ACTIVITY="PROJECT_ACTIVITY_TYPE + .project-state"</code> */
	public static final String PROJECT_STATE_ACTIVITY = PROJECT_ACTIVITY_TYPE + ".project-state";
	/** Constant <code>TASK_STATE_ACTIVITY="PROJECT_ACTIVITY_TYPE + .task-state"</code> */
	public static final String TASK_STATE_ACTIVITY = PROJECT_ACTIVITY_TYPE + ".task-state";
	/** Constant <code>DELIVERABLE_STATE_ACTIVITY="PROJECT_ACTIVITY_TYPE + .deliverable-st"{trunked}</code> */
	public static final String DELIVERABLE_STATE_ACTIVITY = PROJECT_ACTIVITY_TYPE + ".deliverable-state";
	/** Constant <code>COMMENT_CREATED_ACTIVITY="org.alfresco.comments.comment-created"</code> */
	public static final String COMMENT_CREATED_ACTIVITY = "org.alfresco.comments.comment-created";

	private static final Log logger = LogFactory.getLog(ProjectActivityServiceImpl.class);

	@Autowired
	ActivityService activityService;

	@Autowired
	EntityActivityService entityActivityService;

	@Autowired
	EntityListDAO entityListDAO;

	@Autowired
	AssociationService associationService;

	@Autowired
	NodeService nodeService;

	@Autowired
	AttributeExtractorService attributeExtractorService;

	@Autowired
	AlfrescoRepository<ActivityListDataItem> alfrescoRepository;

	@Autowired
	CommentService commentService;

	@Autowired
	ContentService contentService;

	@Autowired
	ProjectNotificationService projectNotificationService;

	@Autowired
	EntityDictionaryService entityDictionaryService;

	/** {@inheritDoc} */
	@Override
	public boolean isMatchingStateProperty(QName propName){
		return ProjectModel.PROP_PROJECT_STATE.isMatch(propName) ||  ProjectModel.PROP_TL_STATE.isMatch(propName) || ProjectModel.PROP_DL_STATE.isMatch(propName);
	}

	

	/** {@inheritDoc} */
	@Override
	public boolean isIgnoreStateProperty(QName propName) {
		return ProjectModel.PROP_TL_STATE.isMatch(propName) || ProjectModel.PROP_DL_STATE.isMatch(propName);
	}

	
	
	/** {@inheritDoc} */
	@Override
	public boolean isMatchingEntityType(QName entityType) {
		return entityDictionaryService.isSubClass(entityType, ProjectModel.TYPE_PROJECT);
	}

	
	/** {@inheritDoc} */
	@Override
	public void postTaskStateChangeActivity(NodeRef taskNodeRef,NodeRef commentNodeRef, String beforeState, String afterState, boolean notifyOnly) {

		QName itemType = nodeService.getType(taskNodeRef);

		NodeRef entityNodeRef = entityActivityService.getEntityNodeRefForActivity(taskNodeRef, itemType);

		if (entityNodeRef != null) {

			logger.debug("Task state change with comment: "+(commentNodeRef!=null)+", notify only : "+notifyOnly);
			
			if (notifyOnly || entityActivityService.postStateChangeActivity(entityNodeRef, taskNodeRef, beforeState, afterState)) {
				
				projectNotificationService.notifyTaskStateChanged(entityNodeRef, taskNodeRef, beforeState, afterState);
				
				if(commentNodeRef!=null) {
					 ActivityListDataItem activityItem = entityActivityService.postCommentActivity(entityNodeRef, commentNodeRef, ActivityEvent.Create, false);
					
					nodeService.setProperty(entityNodeRef, ProjectModel.PROP_PROJECT_CUR_COMMENT, AuditActivityHelper.serializeActivity(activityItem));

				}
				
				postStateChangeActivity(TASK_STATE_ACTIVITY, (String) nodeService.getProperty(taskNodeRef, ProjectModel.PROP_TL_TASK_NAME),
						taskNodeRef, beforeState, afterState, true);

			}
		}

	}
	


	/** {@inheritDoc} */
	@Override
	public void postDeliverableStateChangeActivity(NodeRef deliverableNodeRef, String beforeState, String afterState, boolean notifyOnly) {

		QName itemType = nodeService.getType(deliverableNodeRef);

		NodeRef entityNodeRef = entityActivityService.getEntityNodeRefForActivity(deliverableNodeRef, itemType);

		if (entityNodeRef != null) {
			if (notifyOnly || entityActivityService.postStateChangeActivity(entityNodeRef, deliverableNodeRef, beforeState, afterState)) {

				postStateChangeActivity(DELIVERABLE_STATE_ACTIVITY,
						(String) nodeService.getProperty(deliverableNodeRef, ProjectModel.PROP_DL_DESCRIPTION), deliverableNodeRef, beforeState,
						afterState, true);

			}
		}

	}

	private void postStateChangeActivity(String activityType, String title, NodeRef itemNodeRef, String beforeState, String afterState,
			boolean isItem) {
		if ((itemNodeRef != null) && (beforeState != null) && (afterState != null)) {
			try {
				JSONObject data = new JSONObject();
				NodeRef projectNodeRef = itemNodeRef;
				if (isItem) {
					projectNodeRef = getProjectNodeRefFromList(itemNodeRef);
					data.put("entityNodeRef", projectNodeRef);
					data.put("entityTitle", nodeService.getProperty(projectNodeRef, ContentModel.PROP_NAME));
				}

				data.put(PostLookup.JSON_NODEREF, itemNodeRef);
				data.put("title", title);
				data.put("beforeState", beforeState);
				data.put("afterState", afterState);

				// Alfresco activity
				activityService.postActivity(activityType, attributeExtractorService.extractSiteId(itemNodeRef), "project", data.toString());

			} catch (JSONException e) {
				logger.error(e, e);
			}
		}

	}

	private NodeRef getTaskNodeRef(NodeRef deliverableNodeRef) {
		return associationService.getTargetAssoc(deliverableNodeRef, ProjectModel.ASSOC_DL_TASK);
	}

	private NodeRef getProjectNodeRefFromList(NodeRef listItemNodeRef) {
		return entityListDAO.getEntity(listItemNodeRef);
	}

	/** {@inheritDoc} */
	@Override
	public void notify(NodeRef entityNodeRef, ActivityListDataItem activityListDataItem) {
		if (ActivityType.Comment.equals(activityListDataItem.getActivityType())) {

			if (ProjectModel.TYPE_PROJECT.equals(nodeService.getType(entityNodeRef))) {
				try {

					JSONObject data = new JSONObject(activityListDataItem.getActivityData());
					NodeRef commentNodeRef = null, itemNodeRef = null;
					ActivityEvent activityEvent = ActivityEvent.Create;

					if (data.has(EntityActivityService.PROP_COMMENT_NODEREF)) {
						commentNodeRef = new NodeRef((String) data.get(EntityActivityService.PROP_COMMENT_NODEREF));
					}
					if (data.has(EntityActivityService.PROP_DATALIST_NODEREF)) {
						itemNodeRef = new NodeRef((String) data.get(EntityActivityService.PROP_DATALIST_NODEREF));
					}

					if (data.has(EntityActivityService.PROP_ACTIVITY_EVENT)) {
						activityEvent = ActivityEvent.valueOf((String) data.get(EntityActivityService.PROP_ACTIVITY_EVENT));

					}

					if (commentNodeRef != null) {

						if (itemNodeRef != null) {
							QName itemType = nodeService.getType(itemNodeRef);

							if (ProjectModel.TYPE_DELIVERABLE_LIST.equals(itemType)) {
								NodeRef taskNodeRef = getTaskNodeRef(itemNodeRef);
								projectNotificationService.notifyComment(commentNodeRef, activityEvent, entityNodeRef, taskNodeRef, itemNodeRef);
							} else {
								projectNotificationService.notifyComment(commentNodeRef, activityEvent, entityNodeRef, itemNodeRef, null);
							}
						} else {
							projectNotificationService.notifyComment(commentNodeRef, activityEvent, entityNodeRef, null, null);
						}

					}

					nodeService.setProperty(entityNodeRef, ProjectModel.PROP_PROJECT_CUR_COMMENT, AuditActivityHelper.serializeActivity(activityListDataItem));

				} catch (JSONException e) {
					logger.error(e, e);
				}
			}
		}
	}
	
}
