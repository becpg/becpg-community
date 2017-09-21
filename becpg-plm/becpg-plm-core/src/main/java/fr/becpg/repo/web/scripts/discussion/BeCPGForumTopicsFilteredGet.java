package fr.becpg.repo.web.scripts.discussion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ForumModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.discussion.TopicInfoImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.discussion.ForumTopicsFilteredGet;
import org.alfresco.service.cmr.discussion.TopicInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.util.Pair;

import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemGroup;
import fr.becpg.repo.helper.AssociationService;

public class BeCPGForumTopicsFilteredGet extends ForumTopicsFilteredGet {


	
	AssociationService associationService;
	
	AuthorityService authorityService;
	
	
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}


	@Override
	protected PagingResults<TopicInfo> doSearch(Pair<String, String> searchQuery, boolean sortAscending, PagingRequest paging) {
		
		if (isCurrentUserExternal()) {
			List<TopicInfo> topics = new ArrayList<>();
			
			for(NodeRef nodeRef : associationService.getSourcesAssocs(personService.getPerson(AuthenticationUtil.getFullyAuthenticatedUser()),
					PLMModel.ASSOC_SUPPLIER_ACCOUNT)){
				if(nodeService.getType(nodeRef).equals(ForumModel.TYPE_TOPIC)){
				   ChildAssociationRef ref = nodeService.getPrimaryParent(nodeRef);
		           String topicName = ref.getQName().getLocalName();
		           TopicInfo  topic = discussionService.getTopic(ref.getParentRef(), topicName);
		           String path = nodeService.getPath(topic.getNodeRef()).toDisplayPath(nodeService, permissionService);
		           String site = path.split("/")[3];
		           TopicInfoImpl tii = (TopicInfoImpl)topic;
		           tii.setShortSiteName(site);
		           topics.add(tii);
					
				}
			}
			
			return new PagingResults<TopicInfo>() {

				@Override
				public List<TopicInfo> getPage() {
					return topics;
				}

				@Override
				public boolean hasMoreItems() {
					return false;
				}

				@Override
				public Pair<Integer, Integer> getTotalResultCount() {
				 return new Pair<Integer, Integer>(topics.size(), topics.size());
				}

				@Override
				public String getQueryExecutionId() {
					// TODO Auto-generated method stub
					return null;
				}
				
			};
			
		} else {
		
			return super.doSearch(searchQuery, sortAscending, paging);
		}
	}
	

	/*
	 * Renders out the list of topics TODO Fetch the post data in one go, rather
	 * than one at a time
	 */
	@Override
	protected Map<String, Object> renderTopics(List<TopicInfo> topics, Pair<Integer, Integer> size, PagingRequest paging, SiteInfo site) {
		Map<String, Object> model = new HashMap<String, Object>();

		// Paging info
		model.put("total", size.getFirst());
		model.put("pageSize", paging.getMaxItems());
		model.put("startIndex", paging.getSkipCount());
		model.put("itemCount", topics.size());

		// Data
		List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
		for (TopicInfo topic : topics) {
			// ACE-772 fix of incorrect display of topics into "My Discussions"
			// dashlet.
			// Into "My Discussions" dashlet forum topic will be displayed only
			// if user is a member of that site.
			if (null == site && null != topic.getShortSiteName() && !isCurrentUserExternal()) {
				String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
				String siteShortName = topic.getShortSiteName();
				boolean isSiteMember = siteService.isMember(siteShortName, currentUser);

				if (isSiteMember) {
					items.add(renderTopic(topic, site));
				}
			}
			// Display all topics on the forum of the site.
			else {
				items.add(renderTopic(topic, site));
			}
		}
		model.put("items", items);

		// All done
		return model;
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
