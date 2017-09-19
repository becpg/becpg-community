package fr.becpg.repo.web.scripts.discussion;

import java.util.List;
import java.util.Map;

import org.alfresco.repo.web.scripts.discussion.ForumTopicPost;
import org.alfresco.service.cmr.discussion.PostInfo;
import org.alfresco.service.cmr.discussion.TopicInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteRole;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import fr.becpg.model.PLMModel;

public class BeCPGForumTopicPost extends ForumTopicPost {

	@Override
	protected Map<String, Object> executeImpl(SiteInfo site, NodeRef nodeRef, TopicInfo topic, PostInfo post, WebScriptRequest req, JSONObject json,
			Status status, Cache cache) {

		// They shouldn't be adding to an existing Post or Topic
		if (topic != null || post != null) {
			String error = "Can't create a new Topic inside an existing Topic or Post";
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, error);
		}

		// Grab the details of the new Topic and Post
		String title = "";
		String contents = "";
		String supplierUserName = "";
		if (json.containsKey("title")) {
			title = (String) json.get("title");
		}
		if (json.containsKey("content")) {
			contents = (String) json.get("content");
		}

		if (json.containsKey("supplierUserName")) {
			supplierUserName = (String) json.get("supplierUserName");
		}

		List<String> tags = getTags(json);

		// Have the topic created
		if (site != null) {
			topic = discussionService.createTopic(site.getShortName(), title);
		} else {
			topic = discussionService.createTopic(nodeRef, title);
		}
		if (tags != null && tags.size() > 0) {
			topic.getTags().clear();
			topic.getTags().addAll(tags);
			discussionService.updateTopic(topic);
		}

		// Save supplierAccountRef association
		if (!nodeService.hasAspect(topic.getNodeRef(), PLMModel.ASPECT_SUPPLIERS_ACCOUNTREF) && supplierUserName != null
				&& !supplierUserName.isEmpty()) {

			NodeRef supplierAccountNodeRef = personService.getPersonOrNull(supplierUserName);
			nodeService.addAspect(topic.getNodeRef(), PLMModel.ASPECT_SUPPLIERS_ACCOUNTREF, null);
			nodeService.createAssociation(topic.getNodeRef(), supplierAccountNodeRef, PLMModel.ASSOC_SUPPLIER_ACCOUNT);

			updateTopicPermission(topic.getNodeRef(), supplierUserName);

		}

		// Have the primary post created
		post = discussionService.createPost(topic, contents);

		// Record the activity
		addActivityEntry("post", "created", topic, post, site, req, json);

		// Build the common model parts
		Map<String, Object> model = buildCommonModel(site, topic, post, req);

		// Build the JSON for the whole topic
		model.put(KEY_POSTDATA, renderTopic(topic, site));

		// All done
		return model;
	}

	private void updateTopicPermission(NodeRef topicNodeRef, String userName) {
		NodeRef discussion = nodeService.getPrimaryParent(topicNodeRef).getParentRef();
		String siteShortName = siteService.getSiteShortName(nodeService.getPrimaryParent(discussion).getParentRef());
		if (!siteService.isMember(siteShortName, userName)) {
			siteService.setMembership(siteShortName, userName, SiteRole.SiteConsumer.toString());
		}
		permissionService.setPermission(discussion, userName, PermissionService.READ, true);
		permissionService.setPermission(topicNodeRef, userName, PermissionService.CREATE_CHILDREN, true);
		permissionService.setPermission(topicNodeRef, userName, PermissionService.READ, true);
	}

}
