package fr.becpg.repo.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.forum.CommentService;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
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
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.repo.search.BeCPGQueryBuilder;

@Service("entityActivityService")
public class EntityActivityServiceImpl implements EntityActivityService {

	private static Log logger = LogFactory.getLog(EntityActivityServiceImpl.class);
	private static final int MAX_PAGE = 50;

	@Autowired
	private EntityListDAO entityListDAO;

	@Autowired
	private AssociationService associationService;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private AttributeExtractorService attributeExtractorService;

	@Autowired
	private AlfrescoRepository<ActivityListDataItem> alfrescoRepository;

	@Autowired
	private CommentService commentService;

	@Autowired
	private ContentService contentService;

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	@Autowired
	private EntityActivityListener[] entityActivityListeners;

	@Autowired
	private EntityActivityPlugin[] entityActivityPlugins;

	@Autowired
	private BehaviourFilter policyBehaviourFilter;

	@Autowired
	private TransactionService transactionService;

	private static Integer MAX_DEPTH_LEVEL = 6;

	private static final Set<QName> IGNORE_PARENT_ASSOC_TYPES = new HashSet<>(7);
	static {
		IGNORE_PARENT_ASSOC_TYPES.add(ContentModel.ASSOC_MEMBER);
		IGNORE_PARENT_ASSOC_TYPES.add(ContentModel.ASSOC_IN_ZONE);
	}

	@Override
	public boolean isMatchingStateProperty(QName propName) {

		for (EntityActivityPlugin entityActivityPlugin : entityActivityPlugins) {
			if (entityActivityPlugin.isMatchingStateProperty(propName)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean postCommentActivity(NodeRef entityNodeRef, NodeRef commentNodeRef, ActivityEvent activityEvent) {
		if (commentNodeRef != null) {
			try {

				NodeRef activityListNodeRef = getActivityList(entityNodeRef);

				// No list no activity
				if (activityListNodeRef != null) {

					if (nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_TPL)
							|| nodeService.hasAspect(activityListNodeRef, ContentModel.ASPECT_PENDING_DELETE)) {
						logger.debug("No activity on entity template or pending delete node");
						return false;
					}

					ActivityListDataItem activityListDataItem = new ActivityListDataItem();
					JSONObject data = new JSONObject();
					data.put(PROP_COMMENT_NODEREF, commentNodeRef);
					data.put(PROP_ACTIVITY_EVENT, activityEvent.toString());

					NodeRef itemNodeRef = commentService.getDiscussableAncestor(commentNodeRef);

					QName itemType = nodeService.getType(itemNodeRef);

					if (entityDictionaryService.isSubClass(itemType, BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
						data.put(PROP_DATALIST_NODEREF, itemNodeRef);

						NodeRef charactNodeRef = getMatchingCharactNodeRef(itemNodeRef);

						if (charactNodeRef != null) {
							data.put(PROP_TITLE, attributeExtractorService.extractPropName(charactNodeRef));
						} else {
							data.put(PROP_TITLE, itemType.toPrefixString());
						}

					} else if (entityDictionaryService.isSubClass(itemType, BeCPGModel.TYPE_ENTITY_V2)) {
						if (itemNodeRef != entityNodeRef) {
							data.put(PROP_ENTITY_NODEREF, itemNodeRef);
						}
					} else {
						// Case comment on other nodes under entity
						return false;
					}

					if (!data.has(PROP_TITLE) || (data.get(PROP_TITLE) == null)) {
						data.put(PROP_TITLE, attributeExtractorService.extractPropName(itemType, itemNodeRef));
					}
					data.put(PROP_CLASSNAME, attributeExtractorService.extractMetadata(itemType, itemNodeRef));

					activityListDataItem.setActivityType(ActivityType.Comment);
					activityListDataItem.setActivityData(data.toString());
					activityListDataItem.setParentNodeRef(activityListNodeRef);

					alfrescoRepository.save(activityListDataItem);

					notifyListeners(entityNodeRef, activityListDataItem);

					return true;
				}
			} catch (JSONException e) {
				logger.error(e, e);
			}
		}

		return false;

	}

	@Override
	public boolean postContentActivity(NodeRef entityNodeRef, NodeRef contentNodeRef, ActivityEvent activityEvent) {
		if ((contentNodeRef != null) && !nodeService.hasAspect(contentNodeRef, ContentModel.ASPECT_WORKING_COPY)) {
			try {

				NodeRef activityListNodeRef = getActivityList(entityNodeRef);

				// No list no activity
				if (activityListNodeRef != null) {

					if (nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_TPL)
							|| nodeService.hasAspect(activityListNodeRef, ContentModel.ASPECT_PENDING_DELETE)) {
						logger.debug("No activity on entity template or pending delete node");
						return false;
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

					alfrescoRepository.save(activityListDataItem);

					notifyListeners(entityNodeRef, activityListDataItem);

					return true;
				}
			} catch (JSONException e) {
				logger.error(e, e);
			}
		}

		return false;

	}

	@Override
	public boolean postMergeBranchActivity(NodeRef branchNodeRef, NodeRef branchToNodeRef, VersionType versionType, String description) {
		if ((branchNodeRef != null) && (branchToNodeRef != null)) {
			try {

				NodeRef activityListNodeRef = getActivityList(branchToNodeRef);

				// No list no activity
				if (activityListNodeRef != null) {

					if (nodeService.hasAspect(branchToNodeRef, BeCPGModel.ASPECT_ENTITY_TPL)
							|| nodeService.hasAspect(activityListNodeRef, ContentModel.ASPECT_PENDING_DELETE)) {
						logger.debug("No activity on entity template or pending delete node");
						return false;
					}

					// Project activity
					ActivityListDataItem activityListDataItem = new ActivityListDataItem();
					JSONObject data = new JSONObject();

					data.put(PROP_TITLE, nodeService.getProperty(branchToNodeRef, ContentModel.PROP_NAME));
					data.put(PROP_BRANCH_TITLE, nodeService.getProperty(branchNodeRef, ContentModel.PROP_NAME));

					activityListDataItem.setActivityType(ActivityType.Merge);
					activityListDataItem.setActivityData(data.toString());
					activityListDataItem.setParentNodeRef(activityListNodeRef);

					alfrescoRepository.save(activityListDataItem);

					notifyListeners(branchToNodeRef, activityListDataItem);
					return true;
				}
			} catch (JSONException e) {
				logger.error(e, e);
			}
		}

		return false;

	}

	@Override
	public boolean postDatalistActivity(NodeRef entityNodeRef, NodeRef datalistNodeRef, ActivityEvent activityEvent) {
		if ((datalistNodeRef != null)) {
			try {

				NodeRef activityListNodeRef = getActivityList(entityNodeRef);

				// No list no activity
				if (activityListNodeRef != null) {

					if (nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_TPL)
							|| nodeService.hasAspect(activityListNodeRef, ContentModel.ASPECT_PENDING_DELETE)) {
						logger.debug("No activity on entity template or pending delete node");
						return false;
					}

					// Project activity
					ActivityListDataItem activityListDataItem = new ActivityListDataItem();
					JSONObject data = new JSONObject();
					data.put(PROP_DATALIST_NODEREF, datalistNodeRef);
					data.put(PROP_ACTIVITY_EVENT, activityEvent.toString());

					QName type = nodeService.getType(datalistNodeRef);

					data.put(PROP_CLASSNAME, attributeExtractorService.extractMetadata(type, datalistNodeRef));

					NodeRef charactNodeRef = getMatchingCharactNodeRef(datalistNodeRef);

					if (charactNodeRef != null) {
						data.put(PROP_TITLE, attributeExtractorService.extractPropName(charactNodeRef));
					} else {
						data.put(PROP_TITLE, type.toPrefixString());
					}

					activityListDataItem.setActivityType(ActivityType.Datalist);
					activityListDataItem.setActivityData(data.toString());
					activityListDataItem.setParentNodeRef(activityListNodeRef);

					alfrescoRepository.save(activityListDataItem);

					notifyListeners(entityNodeRef, activityListDataItem);

					return true;
				}
			} catch (JSONException e) {
				logger.error(e, e);
			}
		}
		return false;

	}

	@Override
	public boolean postStateChangeActivity(NodeRef entityNodeRef, NodeRef datalistNodeRef, String beforeState, String afterState) {
		if ((entityNodeRef != null) && (beforeState != null) && (afterState != null)) {
			try {

				NodeRef activityListNodeRef = getActivityList(entityNodeRef);

				// No list no activity
				if (activityListNodeRef != null) {

					if (nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_TPL)
							|| nodeService.hasAspect(activityListNodeRef, ContentModel.ASPECT_PENDING_DELETE)) {
						logger.debug("No activity on entity template or pending delete node");
						return false;
					}

					ActivityListDataItem activityListDataItem = new ActivityListDataItem();
					JSONObject data = new JSONObject();

					data.put(PROP_ENTITY_NODEREF, entityNodeRef);

					if (datalistNodeRef != null) {

						data.put(PROP_DATALIST_NODEREF, datalistNodeRef);

						QName type = nodeService.getType(datalistNodeRef);

						data.put(PROP_CLASSNAME, attributeExtractorService.extractMetadata(type, datalistNodeRef));

						NodeRef charactNodeRef = getMatchingCharactNodeRef(datalistNodeRef);

						if (charactNodeRef != null) {
							data.put(PROP_TITLE, attributeExtractorService.extractPropName(charactNodeRef));
						} else {
							data.put(PROP_TITLE, type.toPrefixString());
						}

					} else {
						data.put(PROP_TITLE, attributeExtractorService.extractPropName(entityNodeRef));
					}

					data.put("beforeState", beforeState);
					data.put("afterState", afterState);

					activityListDataItem.setActivityType(ActivityType.State);
					activityListDataItem.setActivityData(data.toString());
					activityListDataItem.setParentNodeRef(activityListNodeRef);

					alfrescoRepository.save(activityListDataItem);

					notifyListeners(entityNodeRef, activityListDataItem);

					return true;

				}

			} catch (JSONException e) {
				logger.error(e, e);
			}
		}

		return false;

	}

	@Override
	public boolean postVersionActivity(NodeRef entityNodeRef, NodeRef versionNodeRef, String versionLabel) {
		if ((versionNodeRef != null) && (versionNodeRef != null)) {
			try {

				NodeRef activityListNodeRef = getActivityList(entityNodeRef);

				// No list no activity
				if (activityListNodeRef != null) {

					if (nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_TPL)
							|| nodeService.hasAspect(activityListNodeRef, ContentModel.ASPECT_PENDING_DELETE)) {
						logger.debug("No activity on entity template or pending delete node");
						return false;
					}

					// Project activity
					ActivityListDataItem activityListDataItem = new ActivityListDataItem();
					JSONObject data = new JSONObject();

					data.put(PROP_TITLE, nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME));
					data.put(PROP_VERSION_NODEREF, versionNodeRef);
					data.put(PROP_VERSION_LABEL, versionLabel);

					activityListDataItem.setActivityType(ActivityType.Version);
					activityListDataItem.setActivityData(data.toString());
					activityListDataItem.setParentNodeRef(activityListNodeRef);

					alfrescoRepository.save(activityListDataItem);

					notifyListeners(entityNodeRef, activityListDataItem);
					return true;
				}
			} catch (JSONException e) {
				logger.error(e, e);
			}
		}

		return false;
	}

	@Override
	public void mergeActivities(NodeRef fromNodeRef, NodeRef toNodeRef) {
		try {
			policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);

			NodeRef toActivityListNodeRef = getActivityList(toNodeRef);
			if (toActivityListNodeRef != null) {
				NodeRef activityListNodeRef = getActivityList(fromNodeRef);
				if (activityListNodeRef != null) {
					for (NodeRef listItem : entityListDAO.getListItems(activityListNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST)) {
						nodeService.moveNode(listItem, toActivityListNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS);
					}
				}
			}
		} finally {
			policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
		}

	}

	@Override
	public boolean postEntityActivity(NodeRef entityNodeRef, ActivityType activityType, ActivityEvent activityEvent) {

		try {

			NodeRef activityListNodeRef = getActivityList(entityNodeRef);

			// No list no activity
			if (activityListNodeRef != null) {

				if (nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_TPL)
						|| nodeService.hasAspect(activityListNodeRef, ContentModel.ASPECT_PENDING_DELETE)) {
					logger.debug("No activity on entity template or pending delete node");
					return false;
				}

				ActivityListDataItem activityListDataItem = new ActivityListDataItem();
				// Don't save System activities
				if (!AuthenticationUtil.getSystemUserName().equals(activityListDataItem.getUserId())) {
					JSONObject data = new JSONObject();
					if (activityEvent != null) {
						data.put(PROP_ACTIVITY_EVENT, activityEvent.toString());
					}

					data.put(PROP_TITLE, nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME));

					activityListDataItem.setActivityType(activityType);
					activityListDataItem.setActivityData(data.toString());
					activityListDataItem.setParentNodeRef(activityListNodeRef);

					alfrescoRepository.save(activityListDataItem);

					notifyListeners(entityNodeRef, activityListDataItem);

					return true;
				}
			}
		} catch (JSONException e) {
			logger.error(e, e);
		}
		return false;
	}

	private void notifyListeners(NodeRef entityNodeRef, ActivityListDataItem activityListDataItem) {

		for (EntityActivityListener entityActivityListener : entityActivityListeners) {

			entityActivityListener.notify(entityNodeRef, activityListDataItem);
		}

	}

	@Override
	public NodeRef getEntityNodeRef(NodeRef nodeRef, QName itemType) {
		return getEntityNodeRef(nodeRef, itemType, new HashSet<NodeRef>());
	}

	private NodeRef getMatchingCharactNodeRef(NodeRef listItemNodeRef) {
		QName pivotAssoc = entityDictionaryService.getDefaultPivotAssoc(nodeService.getType(listItemNodeRef));
		if (pivotAssoc != null) {
			NodeRef part = associationService.getTargetAssoc(listItemNodeRef, pivotAssoc);
			if ((part != null)) {
				return part;
			}
		}

		return null;
	}

	private NodeRef getEntityNodeRef(NodeRef nodeRef, QName itemType, Set<NodeRef> visitedNodeRefs) {
		if (nodeService.exists(nodeRef) && !nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)
				&& !nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)) {

			if (entityDictionaryService.isSubClass(itemType, BeCPGModel.TYPE_ENTITY_V2)) {
				return nodeRef;
			}

			if (entityDictionaryService.isSubClass(itemType, BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
				return entityListDAO.getEntity(nodeRef);
			}

			// Create the visited nodes set if it has not already been created
			if (visitedNodeRefs == null) {
				visitedNodeRefs = new HashSet<>();
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

					NodeRef entityNodeRef = getEntityNodeRef(parent.getParentRef(), nodeService.getType(parent.getParentRef()), visitedNodeRefs);
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

	@Override
	//TODO
	// Supprimer également toutes les activités de formulation / rapport (pas la peine de les mergers)
	// Prévoir un webscript pour apeller la methode pour les tests
	public void cleanActivities() {

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			// Only entities that might could be have activities
			BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().ofType(BeCPGModel.TYPE_ENTITY_V2).inDB();
			List<NodeRef> entityNodeRefs = queryBuilder.list();

			try {
				policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
				L2CacheSupport.doInCacheContext(() -> {

					// Clean by Entity (product, project, ..)
					for (NodeRef entityNodeRef : entityNodeRefs) {
						NodeRef activityListNodeRef = getActivityList(entityNodeRef);
						if (activityListNodeRef != null) {
							// Get Activity List
							List<NodeRef> activityListNodeRefs = entityListDAO.getListItems(activityListNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST);
							Set<String> users = new HashSet<>();
							Date cronDate = new Date();

							// Clean if more than 50 Activity
							if (activityListNodeRefs.size() > MAX_PAGE) {
								Map<ActivityType, List<NodeRef>> activitiesByType = new HashMap<>();
								ActivityListDataItem activity;
								for (NodeRef activityListItemNodeRef : activityListNodeRefs) {

									Date created = (Date) nodeService.getProperty(activityListItemNodeRef, ContentModel.PROP_CREATED);
									if (cronDate.after(created)) {
										cronDate = created;
									}

									activity = alfrescoRepository.findOne(activityListItemNodeRef);

									// Group by type
									if (!activitiesByType.containsKey(activity.getActivityType())) {
										activitiesByType.put(activity.getActivityType(), new ArrayList<>());
									}
									activitiesByType.get(activity.getActivityType()).add(activityListItemNodeRef);
									users.add(activity.getUserId());

								}
								int activityNumberAfterGroupByMonth = 0;
								// Group by week foreach ActivityList type
								for (Map.Entry<ActivityType, List<NodeRef>> entry : activitiesByType.entrySet()) {

									if (!entry.getKey().equals(ActivityType.Comment)) {
										activitiesByType.put(entry.getKey(), groupByWeek(entityNodeRef, entry.getValue(), users, cronDate));
									}
									activityNumberAfterGroupByMonth += activitiesByType.get(entry.getKey()).size();
								}

								// Still more than 50 activity > groupByMonth
								if (activityNumberAfterGroupByMonth > MAX_PAGE) {
									groupByMonth(null, null, null);
								}

								// Finally delete non important older activities
								// (Still under R&D)
							}
						}
					}

				}, false, true);

			} finally {

				policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
			}

			return null;
		}, false, true);

	}

	// Group activities by type and week and user
	private List<NodeRef> groupByWeek(NodeRef entityNodeRef, List<NodeRef> activitiesNodeRefs, Set<String> users, Date cronDate) {

		// Ignore the last week
		Calendar endWeek = Calendar.getInstance();
		endWeek.setTime(new Date());
		endWeek.add(Calendar.WEEK_OF_YEAR, -1);

		Calendar startWeek = Calendar.getInstance();
		startWeek.setTime(new Date());
		startWeek.add(Calendar.WEEK_OF_YEAR, -2);

		// Repeat till the oldest activity
		while (endWeek.getTime().after(cronDate)) {

			for (String userId : users) {
				List<NodeRef> deletedNodes = new ArrayList<>();

				for (NodeRef activityNodeRef : activitiesNodeRefs) {
					Date createdDate = (Date) nodeService.getProperty(activityNodeRef, ContentModel.PROP_CREATED);
					// Group by week each activity match user and week criteria
					if (createdDate.after(startWeek.getTime()) && createdDate.before(endWeek.getTime())
							&& alfrescoRepository.findOne(activityNodeRef).getUserId().equals(userId)) {
						deletedNodes.add(activityNodeRef);
						if (deletedNodes.size() > 1) {
							nodeService.deleteNode(activityNodeRef);
						}
					}
				}

				// Group Activities
				if (deletedNodes.size() > 1) {
					// Delete ignored Activity
					ActivityListDataItem firstDeletedActivity = alfrescoRepository.findOne(deletedNodes.get(0));
					ActivityListDataItem activity = new ActivityListDataItem();
					// Group deleted activities by new activity
					activity.setParentNodeRef(entityNodeRef);
					activity.setActivityData(firstDeletedActivity.getActivityData());
					activity.setActivityType(firstDeletedActivity.getActivityType());
					activity.setUserId(firstDeletedActivity.getUserId());

					activitiesNodeRefs.removeAll(deletedNodes);
					nodeService.deleteNode(deletedNodes.get(0));
					activitiesNodeRefs.add(alfrescoRepository.save(activity).getNodeRef());
				}

			}

			// Move to previous week
			endWeek.add(Calendar.WEEK_OF_MONTH, -1);
			startWeek.add(Calendar.WEEK_OF_MONTH, -1);
		}

		return activitiesNodeRefs;
	}

	// Group activities by month
	private List<NodeRef> groupByMonth(List<NodeRef> activityList, Set<String> users, Date cronDate) {

		// TODO groupByWeek refactoring !!

		return null;
	}

}
