package fr.becpg.repo.project.impl;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.activities.post.lookup.PostLookup;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.project.ProjectActivityService;

public class ProjectActivityServiceImpl implements ProjectActivityService {

	private static String PROJECT_ACTIVITY_TYPE = "fr.becpg.project";
	private static String PAGE_PROJECT = "entity-details?nodeRef=%s";

	public static String PROJECT_STATE_ACTIVITY = PROJECT_ACTIVITY_TYPE + ".project-state";
	public static String TASK_STATE_ACTIVITY = PROJECT_ACTIVITY_TYPE + ".task-state";
	public static String DELIVERABLE_STATE_ACTIVITY = PROJECT_ACTIVITY_TYPE + ".deliverable-state";
	public static String COMMENT_CREATED_ACTIVITY = "org.alfresco.comments.comment-created";
	
	private static Log logger = LogFactory.getLog(ProjectActivityServiceImpl.class);

	ActivityService activityService;

	EntityListDAO entityListDAO;

	NodeService nodeService;
	
	AttributeExtractorService attributeExtractorService;
	

	public void setAttributeExtractorService(AttributeExtractorService attributeExtractorService) {
		this.attributeExtractorService = attributeExtractorService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setActivityService(ActivityService activityService) {
		this.activityService = activityService;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	@Override
	public void postTaskStateChangeActivity(NodeRef taskNodeRef, String beforeState, String afterState) {		
		postStateChangeActivity(TASK_STATE_ACTIVITY,(String)nodeService.getProperty(taskNodeRef, ProjectModel.PROP_TL_TASK_NAME) , taskNodeRef, beforeState, afterState, true);
	}

	@Override
	public void postProjectStateChangeActivity(NodeRef projectNodeRef, String beforeState, String afterState) {
		postStateChangeActivity(PROJECT_STATE_ACTIVITY,(String)nodeService.getProperty(projectNodeRef,ContentModel.PROP_NAME), projectNodeRef, beforeState, afterState, false);

	}
	
	@Override
	public void postDeliverableStateChangeActivity(NodeRef deliverableNodeRef, String beforeState, String afterState) {
		postStateChangeActivity(DELIVERABLE_STATE_ACTIVITY,(String)nodeService.getProperty(deliverableNodeRef,ProjectModel.PROP_DL_DESCRIPTION), deliverableNodeRef, beforeState, afterState, true);

	}

	private void postStateChangeActivity(String activityType,String title, NodeRef itemNodeRef, String beforeState, String afterState, boolean isItem) {
		if (itemNodeRef != null && beforeState!=null && afterState!=null) {
			try {
				JSONObject data = new JSONObject();
				if (isItem) {
					NodeRef projectNodeRef = getProjectNodeRef(itemNodeRef);
					data.put("entityNodeRef", projectNodeRef);
					data.put("entityTitle", nodeService.getProperty(projectNodeRef, ContentModel.PROP_NAME));
				}
				data.put(PostLookup.JSON_NODEREF, itemNodeRef);
				data.put("title",title);
				data.put("beforeState", beforeState);
				data.put("afterState", afterState);
				
				activityService.postActivity(activityType, attributeExtractorService.extractSiteId(itemNodeRef), "project", data.toString());
			} catch (JSONException e) {
				logger.error(e,e);
			}
		}

	}

	private NodeRef getProjectNodeRef(NodeRef listItemNodeRef) {
		return entityListDAO.getEntity(listItemNodeRef);
	}

	@Override
	public void postProjectCommentCreatedActivity(NodeRef projectNodeRef, String comment) {

		if (projectNodeRef != null && comment != null) {
			try {
				JSONObject data = new JSONObject();
				data.put(PostLookup.JSON_PAGE, String.format(PAGE_PROJECT, projectNodeRef));
				data.put(PostLookup.JSON_NODEREF, projectNodeRef);
				data.put("title", nodeService.getProperty(projectNodeRef, ContentModel.PROP_NAME));

				activityService.postActivity(COMMENT_CREATED_ACTIVITY,
						attributeExtractorService.extractSiteId(projectNodeRef), "comments", data.toString());

			} catch (JSONException e) {
				logger.error(e, e);
			}
		}
	}

}
