/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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
import org.alfresco.rest.api.model.Comment;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.project.ProjectActivityService;
import fr.becpg.repo.project.data.projectList.ActivityListDataItem;
import fr.becpg.repo.project.data.projectList.ActivityType;
import fr.becpg.repo.repository.AlfrescoRepository;

@Service("projectActivityService")
public class ProjectActivityServiceImpl implements ProjectActivityService {

	private static String PROJECT_ACTIVITY_TYPE = "fr.becpg.project";

	public static String PROJECT_STATE_ACTIVITY = PROJECT_ACTIVITY_TYPE + ".project-state";
	public static String TASK_STATE_ACTIVITY = PROJECT_ACTIVITY_TYPE + ".task-state";
	public static String DELIVERABLE_STATE_ACTIVITY = PROJECT_ACTIVITY_TYPE + ".deliverable-state";
	public static String COMMENT_CREATED_ACTIVITY = "org.alfresco.comments.comment-created";

	private static String PROP_COMMENT_NODEREF= "commentNodeRef";
	
	private static Log logger = LogFactory.getLog(ProjectActivityServiceImpl.class);

	@Autowired
	ActivityService activityService;

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

	@Override
	public void postTaskStateChangeActivity(NodeRef taskNodeRef, String beforeState, String afterState) {
		postStateChangeActivity(TASK_STATE_ACTIVITY, (String) nodeService.getProperty(taskNodeRef, ProjectModel.PROP_TL_TASK_NAME), taskNodeRef,
				beforeState, afterState, true);

	}

	@Override
	public void postProjectStateChangeActivity(NodeRef projectNodeRef, String beforeState, String afterState) {
		postStateChangeActivity(PROJECT_STATE_ACTIVITY, (String) nodeService.getProperty(projectNodeRef, ContentModel.PROP_NAME), projectNodeRef,
				beforeState, afterState, false);

	}

	@Override
	public void postDeliverableStateChangeActivity(NodeRef deliverableNodeRef, String beforeState, String afterState) {
		postStateChangeActivity(DELIVERABLE_STATE_ACTIVITY, (String) nodeService.getProperty(deliverableNodeRef, ProjectModel.PROP_DL_DESCRIPTION),
				deliverableNodeRef, beforeState, afterState, true);

	}

	private void postStateChangeActivity(String activityType, String title, NodeRef itemNodeRef, String beforeState, String afterState, boolean isItem) {
		if (itemNodeRef != null && beforeState != null && afterState != null) {
			try {
				JSONObject data = new JSONObject();
				NodeRef projectNodeRef = itemNodeRef;
				if (isItem) {
					projectNodeRef = getProjectNodeRef(itemNodeRef);
					data.put("entityNodeRef", projectNodeRef);
					data.put("entityTitle", nodeService.getProperty(projectNodeRef, ContentModel.PROP_NAME));
				}
				data.put(PostLookup.JSON_NODEREF, itemNodeRef);
				data.put("title", title);
				data.put("beforeState", beforeState);
				data.put("afterState", afterState);

				// Alfresco activity
				activityService.postActivity(activityType, attributeExtractorService.extractSiteId(itemNodeRef), "project", data.toString());

				// Project activity
				data = new JSONObject();
				data.put("title", title);
				data.put("beforeState", beforeState);
				data.put("afterState", afterState);
				
				ActivityListDataItem activityListDataItem = new ActivityListDataItem();

				activityListDataItem.setActivityData(data.toString());
				activityListDataItem.setParentNodeRef(getActivityList(projectNodeRef));
				activityListDataItem.setActivityType(ActivityType.State);
				if (DELIVERABLE_STATE_ACTIVITY.equals(activityType)) {
					activityListDataItem.setDeliverable(itemNodeRef);
					activityListDataItem.setTask(getTaskNodeRef(itemNodeRef));
				}
				if (TASK_STATE_ACTIVITY.equals(activityType)) {
					activityListDataItem.setTask(itemNodeRef);
				}

				if (logger.isDebugEnabled()) {
					logger.debug("Post Activity :" + activityListDataItem.toString());
				}

				alfrescoRepository.save(activityListDataItem);

			} catch (JSONException e) {
				logger.error(e, e);
			}
		}

	}

	private NodeRef getTaskNodeRef(NodeRef deliverableNodeRef) {
		return associationService.getTargetAssoc(deliverableNodeRef, ProjectModel.ASSOC_DL_TASK);
	}

	private NodeRef getProjectNodeRef(NodeRef listItemNodeRef) {
		return entityListDAO.getEntity(listItemNodeRef);
	}

	@Override
	public void postCommentActivity(NodeRef commentNodeRef) {

		if (commentNodeRef != null) {
			try {
				// Project activity
				ActivityListDataItem activityListDataItem = new ActivityListDataItem();
				JSONObject data = new JSONObject();
				data.put(PROP_COMMENT_NODEREF, commentNodeRef);
				
				if(nodeService.hasAspect(commentNodeRef, ContentModel.ASPECT_AUDITABLE)
						&& nodeService.getProperty(commentNodeRef, ContentModel.PROP_MODIFIED)!=null
						&& !nodeService.getProperty(commentNodeRef, ContentModel.PROP_MODIFIED)
						.equals(nodeService.getProperty(commentNodeRef, ContentModel.PROP_CREATED)) ){
					data.put("isUpdate",true);
				}
				
				NodeRef itemNodeRef = commentService.getDiscussableAncestor(commentNodeRef);
				NodeRef projectNodeRef = itemNodeRef;
				QName itemType = nodeService.getType(itemNodeRef);
				if (ProjectModel.TYPE_PROJECT.equals(itemType)) {
					data.put("title", (String) nodeService.getProperty(itemNodeRef, ContentModel.PROP_NAME));

				} else if (ProjectModel.TYPE_TASK_LIST.equals(itemType)) {
					projectNodeRef = getProjectNodeRef(itemNodeRef);
					data.put("title", (String) nodeService.getProperty(itemNodeRef, ProjectModel.PROP_TL_TASK_NAME));
					activityListDataItem.setTask(itemNodeRef);

				} else if (ProjectModel.TYPE_DELIVERABLE_LIST.equals(itemType)) {
					projectNodeRef = getProjectNodeRef(itemNodeRef);
					data.put("title", (String) nodeService.getProperty(itemNodeRef, ProjectModel.PROP_DL_DESCRIPTION));
					activityListDataItem.setDeliverable(itemNodeRef);
					activityListDataItem.setTask(getTaskNodeRef(itemNodeRef));
				}
				activityListDataItem.setActivityType(ActivityType.Comment);
				activityListDataItem.setActivityData(data.toString());
				activityListDataItem.setParentNodeRef(getActivityList(projectNodeRef));

				if (logger.isDebugEnabled()) {
					logger.debug("Post Activity :" + activityListDataItem.toString());
				}

				alfrescoRepository.save(activityListDataItem);

			} catch (JSONException e) {
				logger.error(e, e);
			}
		}

	}

	private NodeRef getActivityList(NodeRef projectNodeRef) {
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(projectNodeRef);
		if (listContainerNodeRef == null) {
			listContainerNodeRef = entityListDAO.createListContainer(projectNodeRef);
		}

				NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef,ProjectModel.TYPE_ACTIVITY_LIST);
				if (listNodeRef == null) {
					listNodeRef = entityListDAO.createList(listContainerNodeRef,ProjectModel.TYPE_ACTIVITY_LIST);
				}
				
		return listNodeRef;
		
	}
	
	@Override
	public JSONObject postActivityLookUp(ActivityType activityType, String value){
			if(value!=null ) {
				try {
					JSONTokener tokener = new JSONTokener(value);
					JSONObject jsonObject = new JSONObject(tokener);
					if(activityType.equals(ActivityType.Comment)){
						NodeRef commentNodeRef = new NodeRef((String) jsonObject.get(PROP_COMMENT_NODEREF));
						if(nodeService.exists(commentNodeRef)){
						ContentReader reader = contentService.getReader(commentNodeRef, ContentModel.PROP_CONTENT);
					        if(reader != null)
					        {
						        String content = reader.getContentString();
						        jsonObject.put("content", content);
					        }
						}
						
					}
					
					
					return jsonObject;
				} catch (Exception e) {
					logger.warn("Cannot parse "+value,e);
				}
			}
			return null;	
	}

}
