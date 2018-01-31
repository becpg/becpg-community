package fr.becpg.repo.web.scripts.discussion;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.web.scripts.discussion.ForumPostDelete;
import org.alfresco.service.cmr.discussion.PostInfo;
import org.alfresco.service.cmr.discussion.TopicInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.AssociationService;

/**
 * This class is the controller for the forum post deleting forum-post.delete
 * webscript.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class BeCPGForumPostDelete extends ForumPostDelete {
	private static final String MSG_NODE_MARKED_REMOVED = "forum-post.msg.marked.removed";
	private static final String MSG_NODE_DELETED = "forum-post.msg.deleted";
	private static final String DELETED_POST_TEXT = "[[deleted]]";

	AssociationService associationService;

	AuthorityService authorityService;

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	@Override
	protected Map<String, Object> executeImpl(SiteInfo site, NodeRef nodeRef, TopicInfo topic, PostInfo post, WebScriptRequest req, JSONObject json,
			Status status, Cache cache) {
		final ResourceBundle rb = getResources();

		// Build the common model parts
		Map<String, Object> model = buildCommonModel(site, topic, post, req);

		// Are we deleting a topic, or a post in it?
		String message = null;
		if (post != null) {
			message = doDeletePost(topic, post, rb);
		} else if (topic != null) {
			message = doDeleteTopic(topic, site, req, json, rb);
		} else {
			String error = "Node was of the wrong type, only Topic and Post are supported";
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, error);
		}

		// Finish the model and return
		model.put("message", message);
		return model;
	}

	private String doDeleteTopic(TopicInfo topic, SiteInfo site, WebScriptRequest req, JSONObject json, ResourceBundle rb) {

		// Remove supplier form site
		NodeRef userNodeRef = null;
		NodeRef discussion = nodeService.getPrimaryParent(topic.getNodeRef()).getParentRef();
		String siteShortName = siteService.getSiteShortName(nodeService.getPrimaryParent(discussion).getParentRef());
		
		if (nodeService.hasAspect(topic.getNodeRef(), PLMModel.ASPECT_SUPPLIERS_ACCOUNTREF)) {
			List<AssociationRef> associations = nodeService.getTargetAssocs(topic.getNodeRef(), PLMModel.ASSOC_SUPPLIER_ACCOUNT);
			userNodeRef = associations.get(0).getTargetRef();
		}

		// Delete the topic, which removes all its posts too
		discussionService.deleteTopic(topic);

		if (userNodeRef != null) {

			String userName = nodeService.getProperty(userNodeRef, ContentModel.PROP_USERNAME).toString();
			if (userName != null) {
				if (!hasTopic(userName)) {
					siteService.removeMembership(siteShortName, userName);
				}
			}
		}

		// Add an activity entry for this if it's site based
		if (site != null) {
			addActivityEntry("post", "deleted", topic, null, site, req, json);
		}

		// All done
		String message = rb.getString(MSG_NODE_DELETED);

		return MessageFormat.format(message, topic.getNodeRef());
	}

	/**
	 * We can't just delete posts with replies attached to them, as that breaks
	 * the reply threading. For that reason, we mark deleted posts with a
	 * special text contents. TODO If a post has no replies, then delete it
	 * fully
	 */
	private String doDeletePost(TopicInfo topic, PostInfo post, ResourceBundle rb) {
		// Set the marker text and save
		post.setTitle(DELETED_POST_TEXT);
		post.setContents(DELETED_POST_TEXT);
		discussionService.updatePost(post);

		// Note - we don't add activity feed entries for deleted posts
		// Only deleted whole topic qualify for that at the moment

		String message = rb.getString(MSG_NODE_MARKED_REMOVED);
		return MessageFormat.format(message, post.getNodeRef());
	}

	private boolean hasTopic(String oldUserName) {
		for (NodeRef nodeRef : associationService.getSourcesAssocs(personService.getPerson(oldUserName), PLMModel.ASSOC_SUPPLIER_ACCOUNT)) {
			if (nodeService.getType(nodeRef).equals(ForumModel.TYPE_TOPIC)) {
				return true;
			}
		}
		return false;
	}

}
