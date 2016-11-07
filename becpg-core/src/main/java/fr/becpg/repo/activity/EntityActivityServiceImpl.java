package fr.becpg.repo.activity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.forum.CommentService;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
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
import fr.becpg.repo.activity.data.ActivityEvent;
import fr.becpg.repo.activity.data.ActivityListDataItem;
import fr.becpg.repo.activity.data.ActivityType;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.repository.AlfrescoRepository;

@Service("entityActivityService")
public class EntityActivityServiceImpl implements EntityActivityService {

	private static Log logger = LogFactory.getLog(EntityActivityServiceImpl.class);

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
	NamespaceService namespaceService;
	

	@Autowired
	EntityDictionaryService entityDictionaryService;

	private static final String PROP_COMMENT_NODEREF = "commentNodeRef";
	private static final String PROP_CONTENT_NODEREF = "contentNodeRef";
	private static final String PROP_DATALIST_NODEREF = "datalistNodeRef";
	private static final String PROP_ACTIVITY_EVENT = "activityEvent";
	private static final String PROP_TITLE = "title";

	private static Integer MAX_DEPTH_LEVEL = 6;

	private static final Set<QName> IGNORE_PARENT_ASSOC_TYPES = new HashSet<QName>(7);
	static {
		IGNORE_PARENT_ASSOC_TYPES.add(ContentModel.ASSOC_MEMBER);
		IGNORE_PARENT_ASSOC_TYPES.add(ContentModel.ASSOC_IN_ZONE);
	}

	@Override
	public void postCommentActivity(NodeRef entityNodeRef, NodeRef commentNodeRef, ActivityEvent activityEvent) {
		if (commentNodeRef != null) {
			try {

				NodeRef activityListNodeRef = getActivityList(entityNodeRef);

				// No list no activity
				if (activityListNodeRef != null) {

					if (nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_TPL)
							|| nodeService.hasAspect(activityListNodeRef, ContentModel.ASPECT_PENDING_DELETE)) {
						logger.debug("No activity on entity template or pending delete node");
						return;
					}

					ActivityListDataItem activityListDataItem = new ActivityListDataItem();
					JSONObject data = new JSONObject();
					data.put(PROP_COMMENT_NODEREF, commentNodeRef);
					data.put(PROP_ACTIVITY_EVENT, activityEvent.toString());

					NodeRef itemNodeRef = commentService.getDiscussableAncestor(commentNodeRef);

					data.put(PROP_TITLE, attributeExtractorService.extractPropName(itemNodeRef));

					activityListDataItem.setActivityType(ActivityType.Comment);
					activityListDataItem.setActivityData(data.toString());
					activityListDataItem.setParentNodeRef(activityListNodeRef);

					if (logger.isDebugEnabled()) {
						logger.debug("Post Activity :" + activityListDataItem.toString());
					}

					alfrescoRepository.save(activityListDataItem);
				}

			} catch (JSONException e) {
				logger.error(e, e);
			}
		}

	}

	@Override
	public void postContentActivity(NodeRef entityNodeRef, NodeRef contentNodeRef, ActivityEvent activityEvent) {
		if ((contentNodeRef != null) && !nodeService.hasAspect(contentNodeRef, ContentModel.ASPECT_WORKING_COPY)) {
			try {

				NodeRef activityListNodeRef = getActivityList(entityNodeRef);

				// No list no activity
				if (activityListNodeRef != null) {

					if (nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_TPL)
							|| nodeService.hasAspect(activityListNodeRef, ContentModel.ASPECT_PENDING_DELETE)) {
						logger.debug("No activity on entity template or pending delete node");
						return;
					}

					// Project activity
					ActivityListDataItem activityListDataItem = new ActivityListDataItem();
					JSONObject data = new JSONObject();
					data.put(PROP_CONTENT_NODEREF, contentNodeRef);
					data.put(PROP_ACTIVITY_EVENT, activityEvent.toString());

					data.put(PROP_TITLE, nodeService.getProperty(contentNodeRef, ContentModel.PROP_NAME));

					activityListDataItem.setActivityType(ActivityType.Content);
					activityListDataItem.setActivityData(data.toString());
					activityListDataItem.setParentNodeRef(activityListNodeRef);

					if (logger.isDebugEnabled()) {
						logger.debug("Post Activity :" + activityListDataItem.toString());
					}

					alfrescoRepository.save(activityListDataItem);
				}
			} catch (JSONException e) {
				logger.error(e, e);
			}
		}

	}

	@Override
	public void postDatalistActivity(NodeRef entityNodeRef, NodeRef datalistNodeRef, ActivityEvent activityEvent) {
		if ((datalistNodeRef != null) ) {
			try {

				NodeRef activityListNodeRef = getActivityList(entityNodeRef);

				// No list no activity
				if (activityListNodeRef != null) {

					if (nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_TPL)
							|| nodeService.hasAspect(activityListNodeRef, ContentModel.ASPECT_PENDING_DELETE)) {
						logger.debug("No activity on entity template or pending delete node");
						return;
					}

					// Project activity
					ActivityListDataItem activityListDataItem = new ActivityListDataItem();
					JSONObject data = new JSONObject();
					data.put(PROP_DATALIST_NODEREF, datalistNodeRef);
					data.put(PROP_ACTIVITY_EVENT, activityEvent.toString());

					data.put(PROP_TITLE, nodeService.getType(datalistNodeRef).toPrefixString(namespaceService));

					activityListDataItem.setActivityType(ActivityType.Datalist);
					activityListDataItem.setActivityData(data.toString());
					activityListDataItem.setParentNodeRef(activityListNodeRef);

					if (logger.isDebugEnabled()) {
						logger.debug("Post Activity :" + activityListDataItem.toString());
					}

					alfrescoRepository.save(activityListDataItem);
				}
			} catch (JSONException e) {
				logger.error(e, e);
			}
		}

	}

	@Override
	public void postEntityActivity(NodeRef entityNodeRef, ActivityEvent activityEvent) {
		try {

			NodeRef activityListNodeRef = getActivityList(entityNodeRef);

			// No list no activity
			if (activityListNodeRef != null) {

				if (nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_TPL)
						|| nodeService.hasAspect(activityListNodeRef, ContentModel.ASPECT_PENDING_DELETE)) {
					logger.debug("No activity on entity template or pending delete node");
					return;
				}

				// Project activity
				ActivityListDataItem activityListDataItem = new ActivityListDataItem();
				JSONObject data = new JSONObject();
				data.put(PROP_ACTIVITY_EVENT, activityEvent.toString());

				data.put(PROP_TITLE, nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME));

				activityListDataItem.setActivityType(ActivityType.Entity);
				activityListDataItem.setActivityData(data.toString());
				activityListDataItem.setParentNodeRef(activityListNodeRef);

				if (logger.isDebugEnabled()) {
					logger.debug("Post Activity :" + activityListDataItem.toString());
				}

				alfrescoRepository.save(activityListDataItem);
			}
		} catch (JSONException e) {
			logger.error(e, e);
		}

	}

	@Override
	public NodeRef getEntityNodeRef(NodeRef nodeRef, QName itemType) {
		return getEntityNodeRef(nodeRef, itemType, new HashSet<NodeRef>());
	}

	private NodeRef getEntityNodeRef(NodeRef nodeRef, QName itemType, Set<NodeRef> visitedNodeRefs) {
		if (nodeService.exists(nodeRef) && !nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)
				&& !nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)) {

			if (entityDictionaryService.isSubClass(itemType, BeCPGModel.TYPE_ENTITY_V2)) {
				return nodeRef;
			}

			// Create the visited nodes set if it has not already been created
			if (visitedNodeRefs == null) {
				visitedNodeRefs = new HashSet<NodeRef>();
			}

			// This check prevents stack over flow when we have a cyclic node
			// graph
			if ((visitedNodeRefs.contains(nodeRef) == false) && (visitedNodeRefs.size() < MAX_DEPTH_LEVEL)) {
				visitedNodeRefs.add(nodeRef);

				List<ChildAssociationRef> parents = nodeService.getParentAssocs(nodeRef);
				for (ChildAssociationRef parent : parents) {
					// We are not interested in following potentially massive
					// person group membership trees!
					if (IGNORE_PARENT_ASSOC_TYPES.contains(parent.getTypeQName())) {
						continue;
					}

					NodeRef entityNodeRef = getEntityNodeRef(parent.getParentRef(), parent.getTypeQName(), visitedNodeRefs);
					if (entityNodeRef != null) {
						return entityNodeRef;
					}

				}

			}
		}
		return null;
	}

	private NodeRef getActivityList(NodeRef projectNodeRef) {
		NodeRef listNodeRef = null;
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(projectNodeRef);
		if (listContainerNodeRef != null) {
			listNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST);
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
					} else if (!ActivityEvent.Delete.equals(activityEvent)) {

						jsonObject.put("content", I18NUtil.getMessage("entity.activity.comment.deleted"));
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
