package fr.becpg.repo.web.scripts.discussion;

import java.util.List;
import java.util.Map;

import org.alfresco.model.ForumModel;
import org.alfresco.repo.web.scripts.discussion.ForumPostPut;
import org.alfresco.service.cmr.discussion.PostInfo;
import org.alfresco.service.cmr.discussion.TopicInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteRole;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.AssociationService;

public class BeCPGForumPostPut extends ForumPostPut {

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

		// Build the common model parts
		Map<String, Object> model = buildCommonModel(site, topic, post, req);

		// Did they want to change a reply or the whole topic?
		if (post != null) {
			// Update the specified post
			doUpdatePost(post, post.getTopic(), req, json);

			// Add the activity entry for the reply change
			addActivityEntry("reply", "updated", post.getTopic(), post, site, req, json);

			// Build the JSON for just this post
			model.put(KEY_POSTDATA, renderPost(post, site));
		} else if (topic != null) {
			// Update the primary post of the topic
			post = discussionService.getPrimaryPost(topic);
			if (post == null) {
				throw new WebScriptException(Status.STATUS_PRECONDITION_FAILED, "First (primary) post was missing from the topic, can't fetch");
			}
			doUpdatePost(post, topic, req, json);

			// Add the activity entry for the topic change
			addActivityEntry("post", "updated", topic, null, site, req, json);

			// Build the JSON for the whole topic
			model.put(KEY_POSTDATA, renderTopic(topic, site));
		} else {
			String error = "Node was of the wrong type, only Topic and Post are supported";
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, error);
		}

		// All done
		return model;
	}

	private void doUpdatePost(PostInfo post, TopicInfo topic, WebScriptRequest req, JSONObject json) {
		boolean updateTopic = false;
		// Fetch the details from the JSON

		// Update the titles on the post and it's topic
		if (json.containsKey("title")) {
			String title = (String) json.get("title");
			post.setTitle(title);
			if (title.length() > 0) {
				updateTopic = true;
				topic.setTitle(title);
			}
		}

		// Contents is on the post
		if (json.containsKey("content")) {
			post.setContents((String) json.get("content"));
		}

		// SupplierAccountRef is on the topic

		if (json.containsKey("supplierUserName")) {
			NodeRef newUserNodeRef = personService.getPersonOrNull((String) json.get("supplierUserName"));
			NodeRef oldUserNodeRef = null;

			if (nodeService.hasAspect(topic.getNodeRef(), PLMModel.ASPECT_SUPPLIERS_ACCOUNTREF)) {
				List<AssociationRef> associations = nodeService.getTargetAssocs(topic.getNodeRef(), PLMModel.ASSOC_SUPPLIER_ACCOUNT);
				oldUserNodeRef = associations.get(0).getTargetRef();
				nodeService.removeAssociation(topic.getNodeRef(), oldUserNodeRef, PLMModel.ASSOC_SUPPLIER_ACCOUNT);
			} else {
				nodeService.addAspect(topic.getNodeRef(), PLMModel.ASPECT_SUPPLIERS_ACCOUNTREF, null);
			}

			nodeService.createAssociation(topic.getNodeRef(), newUserNodeRef, PLMModel.ASSOC_SUPPLIER_ACCOUNT);

			updateTopicPermission(topic.getNodeRef(), newUserNodeRef, oldUserNodeRef);

		}

		// Tags are on the topic
		if (json.containsKey("tags")) {
			topic.getTags().clear();

			List<String> tags = getTags(json);
			if (tags != null) {
				topic.getTags().addAll(tags);
			}
			updateTopic = true;
		}

		// Save the topic and the post
		if (updateTopic == true) {
			discussionService.updateTopic(topic);
		}
		discussionService.updatePost(post);
	}

	public void updateTopicPermission(NodeRef topicNodeRef, NodeRef newUserNodeRef, NodeRef oldUserNodeRef) {
		NodeRef discussion = nodeService.getPrimaryParent(topicNodeRef).getParentRef();
		String siteShortName = siteService.getSiteShortName(nodeService.getPrimaryParent(discussion).getParentRef());

		if (oldUserNodeRef != null) {
			String oldUserName = personService.getPerson(oldUserNodeRef).getUserName();
			permissionService.deletePermission(topicNodeRef, oldUserName, PermissionService.CREATE_CHILDREN);
			permissionService.deletePermission(topicNodeRef, oldUserName, PermissionService.READ);
			if(!hasTopic(oldUserName)){
				siteService.removeMembership(siteShortName, oldUserName);
			}
			
		}

		String newUserName = personService.getPerson(newUserNodeRef).getUserName();

		permissionService.setPermission(discussion, newUserName, PermissionService.READ, true);
		permissionService.setPermission(topicNodeRef, newUserName, PermissionService.CREATE_CHILDREN, true);
		permissionService.setPermission(topicNodeRef, newUserName, PermissionService.READ, true);

		// Check External if is a site member otherwise add him
		if (!siteService.isMember(siteShortName, newUserName)) {
			siteService.setMembership(siteShortName, newUserName, SiteRole.SiteConsumer.toString());
		}
	}
	
	private boolean hasTopic(String oldUserName){
		for (NodeRef nodeRef : associationService.getSourcesAssocs(personService.getPerson(oldUserName),
				PLMModel.ASSOC_SUPPLIER_ACCOUNT)) {
			if (nodeService.getType(nodeRef).equals(ForumModel.TYPE_TOPIC)) {
				return true;
			}
		}
		return false;
	}

}
