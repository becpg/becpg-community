/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.activities.post.lookup.PostLookup;
import org.alfresco.repo.forum.CommentService;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.project.ProjectActivityService;
import fr.becpg.repo.project.ProjectNotificationService;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.ActivityEvent;
import fr.becpg.repo.project.data.projectList.ActivityListDataItem;
import fr.becpg.repo.project.data.projectList.ActivityType;
import fr.becpg.repo.repository.AlfrescoRepository;

@Service("projectActivityService")
public class ProjectActivityServiceImpl implements ProjectActivityService {

	
	private static final String PROJECT_ACTIVITY_TYPE = "fr.becpg.project";

	public static final String PROJECT_STATE_ACTIVITY = PROJECT_ACTIVITY_TYPE + ".project-state";
	public static final String TASK_STATE_ACTIVITY = PROJECT_ACTIVITY_TYPE + ".task-state";
	public static final String DELIVERABLE_STATE_ACTIVITY = PROJECT_ACTIVITY_TYPE + ".deliverable-state";
	public static String COMMENT_CREATED_ACTIVITY = "org.alfresco.comments.comment-created";

	private static final String PROP_COMMENT_NODEREF = "commentNodeRef";
	private static final String PROP_CONTENT_NODEREF = "contentNodeRef";
	private static final String PROP_ACTIVITY_EVENT = "activityEvent";
	private static final String PROP_TITLE = "title";
	

	private static Integer MAX_DEPTH_LEVEL = 6;
	
	private static final Set<QName> IGNORE_PARENT_ASSOC_TYPES = new HashSet<QName>(7);
	static {
		IGNORE_PARENT_ASSOC_TYPES.add(ContentModel.ASSOC_MEMBER);
		IGNORE_PARENT_ASSOC_TYPES.add(ContentModel.ASSOC_IN_ZONE);
	}


	private static final Log logger = LogFactory.getLog(ProjectActivityServiceImpl.class);

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
	
	@Autowired
	ProjectNotificationService projectNotificationService;
	
	@Autowired
	EntityDictionaryService entityDictionaryService;

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
					projectNodeRef = getProjectNodeRefFromList(itemNodeRef);
					data.put("entityNodeRef", projectNodeRef);
					data.put("entityTitle", nodeService.getProperty(projectNodeRef, ContentModel.PROP_NAME));
				}
			
				NodeRef activityListNodeRef = getActivityList(projectNodeRef);
				
				if(nodeService.hasAspect(projectNodeRef, BeCPGModel.ASPECT_ENTITY_TPL) || 
						nodeService.hasAspect(activityListNodeRef, ContentModel.ASPECT_PENDING_DELETE)){
					logger.debug("No activity on project template or pending node");
					return ; 
				}
				
				data.put(PostLookup.JSON_NODEREF, itemNodeRef);
				data.put(PROP_TITLE, title);
				data.put("beforeState", beforeState);
				data.put("afterState", afterState);

				// Alfresco activity
				activityService.postActivity(activityType, attributeExtractorService.extractSiteId(itemNodeRef), "project", data.toString());

				// Project activity
				data = new JSONObject();
				data.put(PROP_TITLE, title);
				data.put("beforeState", beforeState);
				data.put("afterState", afterState);

				ActivityListDataItem activityListDataItem = new ActivityListDataItem();

				activityListDataItem.setActivityData(data.toString());
				activityListDataItem.setParentNodeRef(activityListNodeRef);
				activityListDataItem.setActivityType(ActivityType.State);
				if (DELIVERABLE_STATE_ACTIVITY.equals(activityType)) {
					activityListDataItem.setDeliverable(itemNodeRef);
					activityListDataItem.setTask(getTaskNodeRef(itemNodeRef));
				}
				if (TASK_STATE_ACTIVITY.equals(activityType)) {
					activityListDataItem.setTask(itemNodeRef);
					projectNotificationService.notifyTaskStateChanged(projectNodeRef, itemNodeRef, beforeState, afterState);
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

	private NodeRef getProjectNodeRefFromList(NodeRef listItemNodeRef) {
		return entityListDAO.getEntity(listItemNodeRef);
	}

	@Override
	public void postCommentActivity(NodeRef commentNodeRef, ActivityEvent activityEvent) {

		if (commentNodeRef != null) {
			try {
				// Project activity
				ActivityListDataItem activityListDataItem = new ActivityListDataItem();
				JSONObject data = new JSONObject();
				data.put(PROP_COMMENT_NODEREF, commentNodeRef);
				data.put(PROP_ACTIVITY_EVENT, activityEvent.toString());

				NodeRef itemNodeRef = commentService.getDiscussableAncestor(commentNodeRef);
				NodeRef projectNodeRef = itemNodeRef;
				QName itemType = nodeService.getType(itemNodeRef);
				if (ProjectModel.TYPE_PROJECT.equals(itemType)) {
					data.put(PROP_TITLE, nodeService.getProperty(itemNodeRef, ContentModel.PROP_NAME));
					projectNotificationService.notifyComment(commentNodeRef, activityEvent, projectNodeRef, null, null);
				} else if (ProjectModel.TYPE_TASK_LIST.equals(itemType)) {
					projectNodeRef = getProjectNodeRefFromList(itemNodeRef);
					data.put(PROP_TITLE, nodeService.getProperty(itemNodeRef, ProjectModel.PROP_TL_TASK_NAME));
					activityListDataItem.setTask(itemNodeRef);
					projectNotificationService.notifyComment(commentNodeRef, activityEvent, projectNodeRef, itemNodeRef, null);

				} else if (ProjectModel.TYPE_DELIVERABLE_LIST.equals(itemType)) {
					projectNodeRef = getProjectNodeRefFromList(itemNodeRef);
					data.put(PROP_TITLE, nodeService.getProperty(itemNodeRef, ProjectModel.PROP_DL_DESCRIPTION));
					activityListDataItem.setDeliverable(itemNodeRef);
					NodeRef taskNodeRef = getTaskNodeRef(itemNodeRef);
					activityListDataItem.setTask(taskNodeRef);
					projectNotificationService.notifyComment(commentNodeRef, activityEvent, projectNodeRef, taskNodeRef, itemNodeRef);
				} else if ( entityDictionaryService.isSubClass(itemType, BeCPGModel.TYPE_ENTITY_V2) ) {
				   projectNodeRef = getProjectNodeRef(itemNodeRef,null);
				   data.put(PROP_TITLE, nodeService.getProperty(itemNodeRef, ContentModel.PROP_NAME));
				   projectNotificationService.notifyComment(commentNodeRef, activityEvent, projectNodeRef, null, null);
				  
			    } else {
					return;					
				}
				
				NodeRef activityListNodeRef = getActivityList(projectNodeRef);
				
				if(nodeService.hasAspect(projectNodeRef, BeCPGModel.ASPECT_ENTITY_TPL) || 
						nodeService.hasAspect(activityListNodeRef, ContentModel.ASPECT_PENDING_DELETE)){
					logger.debug("No activity on project template or pending node");
					return ; 
				}
				
				activityListDataItem.setActivityType(ActivityType.Comment);
				activityListDataItem.setActivityData(data.toString());
				activityListDataItem.setParentNodeRef(activityListNodeRef);

				if (logger.isDebugEnabled()) {
					logger.debug("Post Activity :" + activityListDataItem.toString());
				}

				alfrescoRepository.save(activityListDataItem);
				
				//Update curr comments assoc
				associationService.update(projectNodeRef, ProjectModel.ASSOC_PROJECT_CUR_COMMENTS, Collections.singletonList(activityListDataItem.getNodeRef()));
				
			} catch (JSONException e) {
				logger.error(e, e);
			}
		}

	}

	@Override
	public void postContentActivity(NodeRef contentNodeRef, ActivityEvent activityEvent) {

		if (contentNodeRef != null && !nodeService.hasAspect(contentNodeRef, ContentModel.ASPECT_WORKING_COPY)) {
			try {
				// Project activity
				ActivityListDataItem activityListDataItem = new ActivityListDataItem();
				JSONObject data = new JSONObject();
				data.put(PROP_CONTENT_NODEREF, contentNodeRef);
				data.put(PROP_ACTIVITY_EVENT, activityEvent.toString());
				
				NodeRef projectNodeRef = getProjectNodeRef(contentNodeRef, null);
				
				
				NodeRef activityListNodeRef = getActivityList(projectNodeRef);
				
				if(nodeService.hasAspect(projectNodeRef, BeCPGModel.ASPECT_ENTITY_TPL) || 
						nodeService.hasAspect(activityListNodeRef, ContentModel.ASPECT_PENDING_DELETE)){
					logger.debug("No activity on project template or pending node");
					return ; 
				}	
				
				data.put(PROP_TITLE, nodeService.getProperty(contentNodeRef, ContentModel.PROP_NAME));

				activityListDataItem.setActivityType(ActivityType.Content);
				activityListDataItem.setActivityData(data.toString());
				activityListDataItem.setParentNodeRef(activityListNodeRef);

				if (logger.isDebugEnabled()) {
					logger.debug("Post Activity :" + activityListDataItem.toString());
				}

				alfrescoRepository.save(activityListDataItem);

			} catch (JSONException e) {
				logger.error(e, e);
			}
		}

		
	}
	
	
	public boolean isInProject(NodeRef nodeRef) {
		return getProjectNodeRef(nodeRef,null)!=null;
	}
	

	private NodeRef getProjectNodeRef(NodeRef nodeRef, Set<NodeRef> visitedNodeRefs) {
		if (nodeService.exists(nodeRef) && !nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY) && !nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)) {

			QName itemType = nodeService.getType(nodeRef);
			
			if (ProjectModel.TYPE_PROJECT.equals(itemType)) {
				return nodeRef;
			} else if(entityDictionaryService.isSubClass(itemType, BeCPGModel.TYPE_ENTITY_V2) ) {
				for(NodeRef projectNodeRef : associationService.getSourcesAssocs(nodeRef, ProjectModel.ASSOC_PROJECT_ENTITY)){
					if(projectNodeRef!=null && ProjectState.InProgress.toString().equals(nodeService.getProperty(projectNodeRef, ProjectModel.PROP_PROJECT_STATE))){
						return projectNodeRef;
					}
				}
				return null;  
			}

			// Create the visited nodes set if it has not already been created
			if (visitedNodeRefs == null) {
				visitedNodeRefs = new HashSet<NodeRef>();
			}

			// This check prevents stack over flow when we have a cyclic node
			// graph
			if (visitedNodeRefs.contains(nodeRef) == false && visitedNodeRefs.size() < MAX_DEPTH_LEVEL) {
				visitedNodeRefs.add(nodeRef);

				List<ChildAssociationRef> parents = nodeService.getParentAssocs(nodeRef);
				for (ChildAssociationRef parent : parents) {
					// We are not interested in following potentially massive
					// person group membership trees!
					if (IGNORE_PARENT_ASSOC_TYPES.contains(parent.getTypeQName())) {
						continue;
					}

					NodeRef projectNodeRef = getProjectNodeRef(parent.getParentRef(), visitedNodeRefs);
					if (projectNodeRef!=null) {
						return projectNodeRef;
					}

				}

			}
		}
		return null;
	}
	
	
	private NodeRef getActivityList(NodeRef projectNodeRef) {
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(projectNodeRef);
		if (listContainerNodeRef == null) {
			listContainerNodeRef = entityListDAO.createListContainer(projectNodeRef);
		}

		NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, ProjectModel.TYPE_ACTIVITY_LIST);
		if (listNodeRef == null) {
			listNodeRef = entityListDAO.createList(listContainerNodeRef, ProjectModel.TYPE_ACTIVITY_LIST);
		}

		return listNodeRef;

	}

	@Override
	public JSONObject postActivityLookUp(ActivityType activityType, String value) {
		if (value != null) {
			try {
				JSONTokener tokener = new JSONTokener(value);
				JSONObject jsonObject = new JSONObject(tokener);
				if (activityType.equals(ActivityType.Comment)) {
					NodeRef commentNodeRef = new NodeRef((String) jsonObject.get(PROP_COMMENT_NODEREF));
					ActivityEvent activityEvent = ActivityEvent.valueOf((String) jsonObject.get(PROP_ACTIVITY_EVENT));
					if (nodeService.exists(commentNodeRef)) {
						ContentReader reader = contentService.getReader(commentNodeRef, ContentModel.PROP_CONTENT);
						if (reader != null) {
							String content = reader.getContentString();
							jsonObject.put("content", content);
						}
					} else if(!ActivityEvent.Delete.equals(activityEvent))  {
						
						jsonObject.put("content", I18NUtil.getMessage("project.activity.comment.deleted"));
					}

				}

				return jsonObject;
			} catch (Exception e) {
				logger.warn("Cannot parse " + value, e);
			}
		}
		return null;
	}

}
