package fr.becpg.repo.web.scripts.discussion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.discussion.AbstractDiscussionWebScript;
import org.alfresco.service.cmr.discussion.PostInfo;
import org.alfresco.service.cmr.discussion.TopicInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemGroup;

public class BeCPGForumPostGet extends AbstractDiscussionWebScript {

	AuthorityService authorityService;

	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	@Override
	protected Map<String, Object> executeImpl(SiteInfo site, NodeRef nodeRef, TopicInfo topic, PostInfo post, WebScriptRequest req, JSONObject json,
			Status status, Cache cache) {

		// Build the common model parts
		Map<String, Object> model = buildCommonModel(site, topic, post, req);

		// Did they want just one post, or the whole of the topic?
		if (post != null) {
			model.put(KEY_POSTDATA, renderPost(post, site));
		} else if (topic != null) {
			// Include supplierAccountRef
			Map<String, Object> tmp = renderTopic(topic, site);
			Map<String, String> supplierAccountRef = getSupplierAccount(topic);
			
			if (supplierAccountRef.containsKey("userName")) {
				tmp.put("supplierAccountRef", supplierAccountRef);
			}
			model.put(KEY_POSTDATA, tmp);
			
			String supplierUserName = supplierAccountRef.get("userName") != null ? supplierAccountRef.get("userName") : "";
			if(isCurrentUserExternal() && !AuthenticationUtil.getFullyAuthenticatedUser().equals(supplierUserName)) {
				String error = "Not allawed for this user";
				throw new WebScriptException(Status.STATUS_BAD_REQUEST, error);
			}
			
		} else {
			String error = "Node was of the wrong type, only Topic and Post are supported";
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, error);
		}

		// All done
		return model;
	}

	private Map<String, String> getSupplierAccount(TopicInfo topic) {

		Map<String, String> ret = new HashMap<>();

		if (nodeService.hasAspect(topic.getNodeRef(), PLMModel.ASPECT_SUPPLIERS_ACCOUNTREF)) {
			List<AssociationRef> associations = nodeService.getTargetAssocs(topic.getNodeRef(), PLMModel.ASSOC_SUPPLIER_ACCOUNT);
			NodeRef userNodeRef = associations.get(0).getTargetRef();
			ret.put("userName", (String) nodeService.getProperty(userNodeRef, ContentModel.PROP_USERNAME));
			ret.put("firstName", (String) nodeService.getProperty(userNodeRef, ContentModel.PROP_FIRSTNAME));
			ret.put("lastName", (String) nodeService.getProperty(userNodeRef, ContentModel.PROP_LASTNAME));
		}

		return ret;
	}

	private boolean isCurrentUserExternal() {
		for (String currAuth : authorityService.getAuthorities()) {
			if ((PermissionService.GROUP_PREFIX + SystemGroup.ExternalUser.toString()).equals(currAuth)) {
				return true;
			}
		}
		return false;
	}
	
}
