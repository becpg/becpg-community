package fr.becpg.repo.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.forum.CommentService;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
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
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.repo.search.BeCPGQueryBuilder;

@Service("entityActivityService")
public class EntityActivityServiceImpl implements EntityActivityService {

	private static Log logger = LogFactory.getLog(EntityActivityServiceImpl.class);

	private static final int MAX_PAGE = 50;
	
	private static final Map<String, Boolean> SORT_MAP;
	static{
		SORT_MAP = new LinkedHashMap<>();
		SORT_MAP.put("@cm:created", true);
	}

	@Autowired
	private EntityListDAO entityListDAO;

	@Autowired
	private EntityService entityService;

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
				policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
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
							if(attributeExtractorService.hasAttributeExtractorPlugin(itemNodeRef)){
								data.put(PROP_TITLE, attributeExtractorService.extractPropName(itemNodeRef));
							} else {
								data.put(PROP_TITLE, attributeExtractorService.extractMetadata(itemType, itemNodeRef));
							}
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
			} finally {
				policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
			}
		}

		return false;

	}

	@Override
	public boolean postContentActivity(NodeRef entityNodeRef, NodeRef contentNodeRef, ActivityEvent activityEvent) {
		if ((contentNodeRef != null) && !nodeService.hasAspect(contentNodeRef, ContentModel.ASPECT_WORKING_COPY)) {
			try {
				policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
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
			} finally {
				policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
			}
		}

		return false;

	}

	@Override
	public boolean postMergeBranchActivity(NodeRef branchNodeRef, NodeRef branchToNodeRef, VersionType versionType, String description) {
		if ((branchNodeRef != null) && (branchToNodeRef != null)) {
			try {
				policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
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
			} finally {
				policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
			}
		}

		return false;

	}

	@Override
	public boolean postDatalistActivity(NodeRef entityNodeRef, NodeRef datalistNodeRef, ActivityEvent activityEvent) {
		if ((datalistNodeRef != null)) {
			try {
				policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
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
						if(attributeExtractorService.hasAttributeExtractorPlugin(datalistNodeRef)){
							data.put(PROP_TITLE, attributeExtractorService.extractPropName(datalistNodeRef));
						}
					}

					activityListDataItem.setActivityType(ActivityType.Datalist);
					activityListDataItem.setActivityData(data.toString());
					activityListDataItem.setParentNodeRef(activityListNodeRef);

					mergeWithLastActivity(activityListDataItem);

					alfrescoRepository.save(activityListDataItem);

					notifyListeners(entityNodeRef, activityListDataItem);

					return true;
				}
			} catch (JSONException e) {
				logger.error(e, e);
			} finally {
				policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
			}
		}
		return false;

	}

	private void mergeWithLastActivity(ActivityListDataItem item) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, -1); 
		NodeRef activityListNodeRef = item.getParentNodeRef();
		
		List<NodeRef> sortedActivityList = entityListDAO.getListItems(activityListNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST, SORT_MAP);
		Collections.reverse(sortedActivityList);
		
		int index = 0;
		for (NodeRef activityListItemNodeRef : sortedActivityList) {
			Date created = (Date) nodeService.getProperty(activityListItemNodeRef, ContentModel.PROP_CREATED);
			ActivityListDataItem activity = alfrescoRepository.findOne(activityListItemNodeRef);
			
			if (created.after(cal.getTime())) {
				if (activity.getActivityData().equals(item.getActivityData()) && activity.getUserId().equals(item.getUserId())
						&& activity.getActivityType().equals(item.getActivityType())) {
					nodeService.addAspect(activityListItemNodeRef, ContentModel.ASPECT_TEMPORARY, null);
					nodeService.deleteNode(activityListItemNodeRef);
					
					if(logger.isDebugEnabled()){						
						logger.debug("Delete same activity in last hour "+activity.getActivityType() );
					}
					break;
				} 		
			} 
			
			if(index==0 && (activity.getActivityData().equals(item.getActivityData()) && activity.getUserId().equals(item.getUserId())
					&& activity.getActivityType().equals(item.getActivityType()))){
				nodeService.addAspect(activityListItemNodeRef, ContentModel.ASPECT_TEMPORARY, null);
				nodeService.deleteNode(activityListItemNodeRef);
				
				if(logger.isDebugEnabled()){
					logger.debug("Delete same successor activity ");
				}
				break;
			}
			
			if (created.before(cal.getTime())){
				break;
			}
			
			index++;
		}
	}

	@Override
	public boolean postStateChangeActivity(NodeRef entityNodeRef, NodeRef datalistNodeRef, String beforeState, String afterState) {
		if ((entityNodeRef != null) && (beforeState != null) && (afterState != null)) {
			try {
				policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
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
							data.put(PROP_TITLE, attributeExtractorService.extractPropName(datalistNodeRef));
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
			} finally {
				policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
			}
		}

		return false;

	}

	@Override
	public boolean postVersionActivity(NodeRef entityNodeRef, NodeRef versionNodeRef, String versionLabel) {
		if ((entityNodeRef != null) && (versionNodeRef != null)) {
			try {
				policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
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
			} finally {
				policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
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
			policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);

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

					mergeWithLastActivity(activityListDataItem);

					alfrescoRepository.save(activityListDataItem);

					notifyListeners(entityNodeRef, activityListDataItem);

					return true;
				}
			}
		} catch (JSONException e) {
			logger.error(e, e);
		} finally {
			policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
		}
		return false;
	}

	private void notifyListeners(NodeRef entityNodeRef, ActivityListDataItem activityListDataItem) {

		for (EntityActivityListener entityActivityListener : entityActivityListeners) {

			entityActivityListener.notify(entityNodeRef, activityListDataItem);
		}

	}

	private NodeRef getMatchingCharactNodeRef(NodeRef listItemNodeRef) {
		try {
			QName pivotAssoc = entityDictionaryService.getDefaultPivotAssoc(nodeService.getType(listItemNodeRef));
			if (pivotAssoc != null) {
				NodeRef part = associationService.getTargetAssoc(listItemNodeRef, pivotAssoc);
				if ((part != null)) {
					return part;
				}
			}
		} catch (IllegalArgumentException e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e, e);
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
	
	/*
	 * (non-Javadoc)
	 * @see fr.becpg.repo.activity.EntityActivityService#cleanActivities()
	 * This methods will lunched by a scheduled job which will allow system to merge activities 
	 * System will merge activities of type formulation,report and datalist of the same user.
	 * 
	 */

	@Override
	public void cleanActivities() {
		
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			// Only entities that might could be have activities
			BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().ofType(BeCPGModel.TYPE_ENTITY_V2).inDB();
			List<NodeRef> entityNodeRefs = queryBuilder.list();

			try {
				policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);

				L2CacheSupport.doInCacheContext(() -> {

					for (NodeRef entityNodeRef : entityNodeRefs) {

						if(logger.isDebugEnabled()){							
							logger.debug("group activities of entity : "+entityNodeRef);
						}
						
						NodeRef activityListNodeRef = getActivityList(entityNodeRef);
						if (activityListNodeRef != null) {
							
							Set<String> users = new HashSet<>();
							Date cronDate = new Date();
							// Get Activity list ordered by the date of creation
							List<NodeRef> activityListDataItemNodeRefs = entityListDAO.getListItems(activityListNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST, SORT_MAP);
							Collections.reverse(activityListDataItemNodeRefs);
							
							int nbrActivity = activityListDataItemNodeRefs.size();
							//Keep the first 50 activities
							activityListDataItemNodeRefs = activityListDataItemNodeRefs.subList(nbrActivity>MAX_PAGE? MAX_PAGE : 0, nbrActivity>MAX_PAGE? nbrActivity : 0 );
							
							nbrActivity = activityListDataItemNodeRefs.size();
							
							// Clean activities which are not in the first page.
							if (nbrActivity > 0 ) {
								Map<ActivityType, List<NodeRef>> activitiesByType = new HashMap<>();
								ActivityListDataItem activity;
								int activityInPage = 0;
								boolean hasFormulation = false;
								boolean hasReport = false;
								
								for (NodeRef activityItemNodeRef : activityListDataItemNodeRefs) {
									if (activityInPage == MAX_PAGE){
										hasFormulation = false;
										hasReport = false;
										activityInPage = 0;
									}
									
									Date created = (Date) nodeService.getProperty(activityItemNodeRef, ContentModel.PROP_CREATED);
									if (cronDate.after(created)) {
										cronDate = created;
									}
									
									activity = alfrescoRepository.findOne(activityItemNodeRef);
									ActivityType activityType = activity.getActivityType();
									
									if((activityType.equals(ActivityType.Formulation) && hasFormulation) 
											|| (activityType.equals(ActivityType.Report) && hasReport)){
										nodeService.addAspect(activityItemNodeRef, ContentModel.ASPECT_TEMPORARY, null);
										nodeService.deleteNode(activityItemNodeRef);
										nbrActivity--;
										continue;
									}
									//Arrange activities by type
									else {
										if (!activitiesByType.containsKey(activityType)) {
											activitiesByType.put(activityType, new ArrayList<>());
										}
										hasFormulation = activityType.equals(ActivityType.Formulation) ? true : hasFormulation;
										hasReport = activityType.equals(ActivityType.Report) ? true : hasReport;
										activitiesByType.get(activityType).add(activityItemNodeRef);
										users.add(activity.getUserId());
									}	
								}
								
								List<NodeRef> dlActivities = activitiesByType.get(ActivityType.Datalist);
								// Group list by day/week/month/year
								int[] groupTime = { Calendar.DAY_OF_YEAR, Calendar.WEEK_OF_YEAR, Calendar.MONTH, Calendar.YEAR};
								for (int i = 0; (i < groupTime.length) && (nbrActivity > MAX_PAGE); i++) {
									dlActivities = group(entityNodeRef, dlActivities, users, groupTime[i], cronDate);
									nbrActivity += (dlActivities.size() - activitiesByType.get(ActivityType.Datalist).size());
								}
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

	// Group activities
	private List<NodeRef> group(NodeRef entityNodeRef, List<NodeRef> activitiesNodeRefs, Set<String> users, int timePeriod, Date cronDate) {
		// Ignore the last day/week/month/year
		Calendar maxLimit = Calendar.getInstance();
		maxLimit.setTime(new Date());
		maxLimit.set(Calendar.HOUR_OF_DAY, 23);
		Calendar minLimit = Calendar.getInstance();
		minLimit.setTime(new Date());
		minLimit.set(Calendar.HOUR_OF_DAY, 0);
		
		switch(timePeriod){
		case Calendar.DAY_OF_YEAR: 
			break;
			
		case Calendar.WEEK_OF_YEAR: 
			maxLimit.set(Calendar.DAY_OF_WEEK, maxLimit.getFirstDayOfWeek());
			minLimit.set(Calendar.DAY_OF_WEEK, minLimit.getFirstDayOfWeek());
			minLimit.add(timePeriod, -1);
			break;
			
		case Calendar.MONTH: 
			maxLimit.set(Calendar.DAY_OF_MONTH, 1);
			minLimit.set(Calendar.DAY_OF_MONTH, 1);
			minLimit.add(timePeriod, -1);
			break;
			
		case Calendar.YEAR: 
			maxLimit.add(timePeriod, -1);
			minLimit.add(timePeriod, -2);
			break;
		}
		
		// Repeat till the oldest activity
		while (maxLimit.getTime().after(cronDate)) {

			for (String userId : users) { 
				
				Map<NodeRef,List<String>> activitiesByEntity = new HashMap<>();
				List<NodeRef> removedNodes = new ArrayList<>();
				for (NodeRef activityNodeRef : activitiesNodeRefs) {
					
					Date createdDate = (Date) nodeService.getProperty(activityNodeRef, ContentModel.PROP_CREATED);
					
					ActivityListDataItem activity = alfrescoRepository.findOne(activityNodeRef);
					NodeRef activityParentNodeRef = activity.getParentNodeRef();
					String strData = activity.getActivityData();
					JSONTokener tokener = new JSONTokener(strData);
					String datalistClassName = null;
					try {
						JSONObject data = new JSONObject(tokener);
						datalistClassName = (String)data.get(PROP_CLASSNAME);
					} catch (JSONException e) {
						logger.error("Problem occurred while parsing data activity!! ", e);
					}
						
					if (createdDate.after(minLimit.getTime()) && createdDate.before(maxLimit.getTime()) && activity.getUserId().equals(userId)) {
						// group same data-list activity
						if(activitiesByEntity.containsKey(activityParentNodeRef) 
								&& activitiesByEntity.get(activityParentNodeRef).contains(datalistClassName)){
							removedNodes.add(activityNodeRef);
							
							nodeService.addAspect(activityNodeRef, ContentModel.ASPECT_TEMPORARY, null);
							nodeService.deleteNode(activityNodeRef);
						} else {
							
							if (!activitiesByEntity.containsKey(activityParentNodeRef)) {
								activitiesByEntity.put(activityParentNodeRef, new ArrayList<>());
							}
							activitiesByEntity.get(activityParentNodeRef).add(datalistClassName);
						}	
					} 
				}
				activitiesNodeRefs.removeAll(removedNodes);
			}

			// Move to previous period
			maxLimit.add(timePeriod, -1);
			minLimit.add(timePeriod, -1);
		}

		return activitiesNodeRefs;
	}

	@Override
	public NodeRef getEntityNodeRefForActivity(NodeRef nodeRef, QName itemType) {
		if (nodeService.exists(nodeRef) && !nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)
				&& !nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)) {
			return entityService.getEntityNodeRef(nodeRef, itemType);
		}
		return null;
	}
}
