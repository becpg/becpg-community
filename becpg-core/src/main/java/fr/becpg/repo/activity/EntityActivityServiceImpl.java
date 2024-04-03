package fr.becpg.repo.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.forum.CommentService;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ISO8601DateFormat;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.activity.data.ActivityEvent;
import fr.becpg.repo.activity.data.ActivityListDataItem;
import fr.becpg.repo.activity.data.ActivityType;
import fr.becpg.repo.activity.helper.AuditActivityHelper;
import fr.becpg.repo.audit.model.AuditQuery;
import fr.becpg.repo.audit.model.AuditScope;
import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.plugin.impl.ActivityAuditPlugin;
import fr.becpg.repo.audit.service.BeCPGAuditService;
import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.batch.BatchPriority;
import fr.becpg.repo.batch.BatchQueueService;
import fr.becpg.repo.batch.EntityListBatchProcessWorkProvider;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.LargeTextHelper;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>EntityActivityServiceImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("entityActivityService")
public class EntityActivityServiceImpl implements EntityActivityService {

	private static final String NO_ACTIVITY_MESSAGE = "No activity on entity template or pending delete node";

	private static Log logger = LogFactory.getLog(EntityActivityServiceImpl.class);

	private static final int MAX_PAGE = 50;

	public static final int ML_TEXT_SIZE_LIMIT = 200;

	private static final String EXPORT_ACTIVITY = "fr.becpg.export";

	private static final Map<String, Boolean> SORT_MAP;
	static {
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

	@Autowired
	NamespaceService namespaceService;

	@Autowired
	public DictionaryService dictionaryService;

	@Autowired
	private BatchQueueService batchQueueService;

	@Autowired
	private ActivityService activityService;

	@Autowired
	private BeCPGAuditService beCPGAuditService;
	
	/** {@inheritDoc} */
	@Override
	public boolean isMatchingStateProperty(QName propName) {

		for (EntityActivityPlugin entityActivityPlugin : entityActivityPlugins) {
			if (entityActivityPlugin.isMatchingStateProperty(propName)) {
				return true;
			}
		}

		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isIgnoreStateProperty(QName propName) {

		for (EntityActivityPlugin entityActivityPlugin : entityActivityPlugins) {
			if (entityActivityPlugin.isIgnoreStateProperty(propName)) {
				return true;
			}
		}

		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean postCommentActivity(NodeRef entityNodeRef, NodeRef commentNodeRef, ActivityEvent activityEvent) {
		return postCommentActivity(entityNodeRef, commentNodeRef, activityEvent, true) != null;
	}

	/** {@inheritDoc} */
	@Override
	public ActivityListDataItem postCommentActivity(NodeRef entityNodeRef, NodeRef commentNodeRef, ActivityEvent activityEvent, boolean notifyObservers) {
		if (commentNodeRef != null) {
			try {
				policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
				NodeRef activityListNodeRef = getActivityList(entityNodeRef);

				// No list no activity
				if (activityListNodeRef != null) {

					if (nodeService.hasAspect(activityListNodeRef, ContentModel.ASPECT_PENDING_DELETE)) {
						logger.debug(NO_ACTIVITY_MESSAGE);
						return null;
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
							if (attributeExtractorService.hasAttributeExtractorPlugin(itemNodeRef)) {
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
						return null;
					}

					if (!data.has(PROP_TITLE) || (data.get(PROP_TITLE) == null)) {
						data.put(PROP_TITLE, attributeExtractorService.extractPropName(itemType, itemNodeRef));
					}
					data.put(PROP_CLASSNAME, attributeExtractorService.extractMetadata(itemType, itemNodeRef));

					activityListDataItem.setActivityType(ActivityType.Comment);
					activityListDataItem.setActivityData(data.toString());
					activityListDataItem.setParentNodeRef(activityListNodeRef);
					
					recordAuditActivity(entityNodeRef, activityListDataItem);

					if (notifyObservers) {
						notifyListeners(entityNodeRef, activityListDataItem);
					}

					return activityListDataItem;
				}
			} catch (JSONException e) {
				logger.error(e, e);
			} finally {
				policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
			}
		}

		return null;

	}

	/** {@inheritDoc} */
	@Override
	public boolean postContentActivity(NodeRef entityNodeRef, NodeRef contentNodeRef, ActivityEvent activityEvent) {
		if ((contentNodeRef != null) && !nodeService.hasAspect(contentNodeRef, ContentModel.ASPECT_WORKING_COPY)) {
			try {
				policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
				NodeRef activityListNodeRef = getActivityList(entityNodeRef);

				// No list no activity
				if (activityListNodeRef != null) {

					if (nodeService.hasAspect(activityListNodeRef, ContentModel.ASPECT_PENDING_DELETE)) {
						logger.debug(NO_ACTIVITY_MESSAGE);
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

					recordAuditActivity(entityNodeRef, activityListDataItem);

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
	public boolean postChangeOrderActivity(NodeRef entityNodeRef, NodeRef changeOrderNodeRef) {
		try {
			policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);

			NodeRef activityListNodeRef = getActivityList(entityNodeRef);

			// No list no activity
			if (activityListNodeRef != null) {

				ActivityListDataItem activityListDataItem = new ActivityListDataItem();
				
				JSONObject data = new JSONObject();

				data.put(PROP_TITLE, nodeService.getProperty(changeOrderNodeRef, ContentModel.PROP_NAME));
				data.put(PROP_ENTITY_NODEREF, changeOrderNodeRef);

				activityListDataItem.setActivityType(ActivityType.ChangeOrder);
				activityListDataItem.setActivityData(data.toString());
				activityListDataItem.setParentNodeRef(activityListNodeRef);

				recordAuditActivity(entityNodeRef, activityListDataItem);

				notifyListeners(entityNodeRef, activityListDataItem);
			}
		} catch (JSONException e) {
			logger.error(e, e);
		} finally {
			policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean postMergeBranchActivity(NodeRef branchNodeRef, NodeRef branchToNodeRef, VersionType versionType, String description) {
		if ((branchNodeRef != null) && (branchToNodeRef != null)) {
			try {
				policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
				NodeRef activityListNodeRef = getActivityList(branchToNodeRef);

				// No list no activity
				if (activityListNodeRef != null) {

					if (nodeService.hasAspect(activityListNodeRef, ContentModel.ASPECT_PENDING_DELETE)) {
						logger.debug(NO_ACTIVITY_MESSAGE);
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

					recordAuditActivity(branchToNodeRef, activityListDataItem);

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

	/** {@inheritDoc} */
	@Override
	public boolean postDatalistActivity(NodeRef entityNodeRef, NodeRef datalistNodeRef, ActivityEvent activityEvent,
			Map<QName, Pair<Serializable, Serializable>> updatedProperties) {
		if ((datalistNodeRef != null)) {
			try {
				policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
				NodeRef activityListNodeRef = getActivityList(entityNodeRef);

				// No list no activity
				if (activityListNodeRef != null) {

					if (nodeService.hasAspect(activityListNodeRef, ContentModel.ASPECT_PENDING_DELETE)) {
						logger.debug(NO_ACTIVITY_MESSAGE);
						return false;
					}

					// Project activity
					ActivityListDataItem activityListDataItem = new ActivityListDataItem();
					JSONObject data = new JSONObject();
					data.put(PROP_DATALIST_NODEREF, datalistNodeRef);
					data.put(PROP_ACTIVITY_EVENT, activityEvent.toString());
					data.put(PROP_ENTITY_NODEREF, entityNodeRef);
					data.put(PROP_ENTITY_TYPE, nodeService.getType(entityNodeRef));

					QName type = nodeService.getType(datalistNodeRef);

					data.put(PROP_CLASSNAME, attributeExtractorService.extractMetadata(type, datalistNodeRef));
					data.put(PROP_DATALIST_TYPE, entityDictionaryService.toPrefixString(type));

					NodeRef charactNodeRef = getMatchingCharactNodeRef(datalistNodeRef);

					if (charactNodeRef != null) {
						QName charactType = nodeService.getType(charactNodeRef);
						if (charactType != null) {
							data.put(PROP_CHARACT_TYPE, attributeExtractorService.extractMetadata(charactType, charactNodeRef));
						}
						data.put(PROP_CHARACT_NODEREF, charactNodeRef);
						data.put(PROP_TITLE, attributeExtractorService.extractPropName(charactNodeRef));
					} else {
						if (attributeExtractorService.hasAttributeExtractorPlugin(datalistNodeRef)) {
							data.put(PROP_TITLE, attributeExtractorService.extractPropName(datalistNodeRef));
						} else {
							data.put(PROP_TITLE, nodeService.getProperty(datalistNodeRef, ContentModel.PROP_NAME));
						}
					}
					if (activityEvent.equals(ActivityEvent.Update) && (updatedProperties != null)) {
						List<JSONObject> properties = new ArrayList<>();
						for (Map.Entry<QName, Pair<Serializable, Serializable>> entry : updatedProperties.entrySet()) {
							JSONObject property = new JSONObject();

							MLText mlTextBefore = null;
							MLText mlTextAfter = null;
							MLText newMlTextBefore = null;
							MLText newMlTextAfter = null;

							if (entry.getValue().getFirst() instanceof List) {
								for (Object obj : (List<?>) entry.getValue().getFirst()) {
									if (obj instanceof MLText) {
										mlTextBefore = (MLText) obj;
									}
								}
							}
							if (entry.getValue().getSecond() instanceof List) {
								for (Object obj : (List<?>) entry.getValue().getSecond()) {
									if (obj instanceof MLText) {
										mlTextAfter = (MLText) obj;
									}
								}
							}

							if (mlTextBefore != null) {
								newMlTextBefore = compareMLTexts(mlTextBefore, mlTextAfter);
							}

							if (mlTextAfter != null) {
								newMlTextAfter = compareMLTexts(mlTextAfter, mlTextBefore);
							}

							if (newMlTextBefore != null) {
								Iterator<Entry<Locale, String>> it = mlTextBefore.entrySet().iterator();

								while (it.hasNext()) {
									Locale locale = it.next().getKey();
									mlTextBefore.put(locale, newMlTextBefore.get(locale));
								}
							}

							if (newMlTextAfter != null) {
								Iterator<Entry<Locale, String>> it = mlTextAfter.entrySet().iterator();

								while (it.hasNext()) {
									Locale locale = it.next().getKey();
									mlTextAfter.put(locale, newMlTextAfter.get(locale));
								}
							}

							property.put(PROP_TITLE, entry.getKey());

							if (entry.getValue().getFirst() instanceof List) {
								ArrayList<Object> beforeList = new ArrayList<>();

								for (Object ent : (List<?>) entry.getValue().getFirst()) {
									if (ent instanceof Date) {
										beforeList.add(ISO8601DateFormat.format((Date) ent));
									} else if (ent instanceof Pair || ent instanceof NodeRef) {
										beforeList.add(ent.toString());
									} else if (ent instanceof List) {
										beforeList.add(((List<?>) ent).stream().map(o -> o.toString()).collect(Collectors.toList()));
									} else {
										beforeList.add(ent);
									}
								}

								property.put(BEFORE, beforeList);
							} else {
								property.put(BEFORE, entry.getValue().getFirst());
							}

							if (entry.getKey().equals(ContentModel.PROP_NAME)) {
								property.put(AFTER, data.get(PROP_TITLE));
							} else {
								if (entry.getValue().getSecond() instanceof List) {
									ArrayList<Object> afterList = new ArrayList<>();

									for (Object ent : (List<?>) entry.getValue().getSecond()) {
										if (ent instanceof Date) {
											afterList.add(ISO8601DateFormat.format((Date) ent));
										} else if (ent instanceof Pair || ent instanceof NodeRef) {
											afterList.add(ent.toString());
										} else if (ent instanceof List) {
											afterList.add(((List<?>) ent).stream().map(o -> o.toString()).collect(Collectors.toList()));
										} else {
											afterList.add(ent);
										}
									}

									property.put(AFTER, afterList);
								} else {
									property.put(AFTER, entry.getValue().getSecond());
								}
							}
							properties.add(property);
						}
						data.put(PROP_PROPERTIES, new JSONArray(properties));
					}
					if (!activityEvent.equals(ActivityEvent.Update) || (updatedProperties != null)) {
						activityListDataItem.setActivityType(ActivityType.Datalist);
						activityListDataItem.setActivityData(data.toString());
						activityListDataItem.setParentNodeRef(activityListNodeRef);

						mergeWithLastActivity(activityListDataItem);

						recordAuditActivity(entityNodeRef, activityListDataItem);
						
						notifyListeners(entityNodeRef, activityListDataItem);
					}

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
	
	private MLText compareMLTexts(MLText mlText, MLText otherMlText) {
		LargeTextHelper.elipse(mlText);
		MLText newMlText = new MLText();
		Iterator<Entry<Locale, String>> it = mlText.entrySet().iterator();

		while (it.hasNext()) {
			Locale locale = it.next().getKey();
			String text = mlText.get(locale);
			newMlText.put(locale, text);
			if ((text != null) && (text.length() > ML_TEXT_SIZE_LIMIT)) {
				String otherText = otherMlText != null ? otherMlText.get(locale) : null;
				if ((otherText == null) || (otherText.length() <= ML_TEXT_SIZE_LIMIT)) {
					text = text.substring(0, ML_TEXT_SIZE_LIMIT) + " ...";
					newMlText.put(locale, text);
				} else {
					Pair<String, String> diffs = LargeTextHelper.createTextDiffs(text, otherText);
					text = diffs.getFirst().replace(" ", "").equals("") ? text : diffs.getFirst();
					text = text.length() > ML_TEXT_SIZE_LIMIT ? text.substring(0, ML_TEXT_SIZE_LIMIT) + " ..." : text;
					newMlText.put(locale, text);
				}
			}
		}
		return newMlText;
	}

	private void recordAuditActivity(NodeRef entityNodeRef, ActivityListDataItem activityListDataItem) {
		
		Date createdDate = activityListDataItem.getCreatedDate() != null ? activityListDataItem.getCreatedDate() : new Date();
		
		try (AuditScope auditScope = beCPGAuditService.startAudit(AuditType.ACTIVITY)) {
			auditScope.putAttribute(ActivityAuditPlugin.ENTITY_NODEREF, entityNodeRef.toString());
			auditScope.putAttribute(ActivityAuditPlugin.PROP_BCPG_AL_USER_ID, activityListDataItem.getUserId());
			auditScope.putAttribute(ActivityAuditPlugin.PROP_BCPG_AL_TYPE, activityListDataItem.getActivityType().toString());
			auditScope.putAttribute(ActivityAuditPlugin.PROP_BCPG_AL_DATA, activityListDataItem.getActivityData());
			auditScope.putAttribute(ActivityAuditPlugin.PROP_CM_CREATED, ISO8601DateFormat.format(createdDate));
		}
	}

	private void deleteAuditActivity(ActivityListDataItem lastActivity) {
		beCPGAuditService.deleteAuditEntries(AuditType.ACTIVITY, lastActivity.getId(), lastActivity.getId() + 1);
	}

	// TODO Slow better to have it async
	private void mergeWithLastActivity(ActivityListDataItem newActivity) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, -1);

		NodeRef activityListNodeRef = newActivity.getParentNodeRef();
		
		NodeRef entityNodeRef = nodeService.getPrimaryParent(nodeService.getPrimaryParent(activityListNodeRef).getParentRef()).getParentRef();
		
		// Activities in the last hour
		AuditQuery auditQuery = AuditQuery.createQuery().asc(false).dbAsc(false)
				.sortBy(ActivityAuditPlugin.PROP_CM_CREATED).timeRange(cal.getTime(), new Date())
				.filter(ActivityAuditPlugin.ENTITY_NODEREF, entityNodeRef.toString());
		
		List<ActivityListDataItem> sortedActivityList = new ArrayList<>();
		
		List<JSONObject> results = beCPGAuditService.listAuditEntries(AuditType.ACTIVITY, auditQuery);

		for (JSONObject result : results) {
			sortedActivityList.add(AuditActivityHelper.parseActivity(result));
		}
		
		// The last created activity
		if (sortedActivityList.isEmpty()) {
			auditQuery.timeRange(null, null).maxResults(1);
			results = beCPGAuditService.listAuditEntries(AuditType.ACTIVITY, auditQuery);
			for (JSONObject result : results) {
				sortedActivityList.add(AuditActivityHelper.parseActivity(result));
			}
		}
		
		for (ActivityListDataItem lastActivity : sortedActivityList) {

			JSONObject lastActivityData = null;
			
			JSONObject newActivityData = null;
			try {
				lastActivityData = new JSONObject(lastActivity.getActivityData());
				newActivityData = new JSONObject(newActivity.getActivityData());
				if (((lastActivity.getActivityData().equals(newActivity.getActivityData()))
						|| (!newActivityData.has(PROP_TITLE) && !lastActivityData.has(PROP_TITLE) && newActivity.getActivityType().equals(ActivityType.Datalist)
								&& lastActivityData.get(PROP_CLASSNAME).equals(newActivityData.get(PROP_CLASSNAME))
								&& lastActivityData.get(PROP_ACTIVITY_EVENT).equals(newActivityData.get(PROP_ACTIVITY_EVENT))))
						&& lastActivity.getUserId().equals(newActivity.getUserId()) && lastActivity.getActivityType().equals(newActivity.getActivityType())) {

					deleteAuditActivity(lastActivity);
					
					if (logger.isDebugEnabled()) {
						logger.debug("Merge with the last activity " + lastActivity.getActivityType());
					}

					return;
					// Add previous updated properties in the data of the last activity
				} else if (newActivityData.has(PROP_PROPERTIES) && lastActivityData.has(PROP_PROPERTIES)
						&& newActivityData.get(PROP_ACTIVITY_EVENT).equals(lastActivityData.get(PROP_ACTIVITY_EVENT))
						&& newActivity.getUserId().equals(lastActivity.getUserId()) && newActivity.getActivityType().equals(lastActivity.getActivityType())
						&& newActivityData.has(PROP_TITLE) && lastActivityData.has(PROP_TITLE) && newActivityData.get(PROP_TITLE).equals(lastActivityData.get(PROP_TITLE))
						&& ((!newActivityData.has(PROP_CLASSNAME) && !lastActivityData.has(PROP_CLASSNAME)) || (newActivityData.has(PROP_CLASSNAME)
								&& lastActivityData.has(PROP_CLASSNAME) && newActivityData.get(PROP_CLASSNAME).equals(lastActivityData.get(PROP_CLASSNAME))))) {
					
					
					// Check if the last activity is less than 4 hours old, otherwise do not merge it
					if (sortedActivityList.size() == 1) {
						cal.add(Calendar.HOUR, -3);
						Date createdDate = lastActivity.getCreatedDate();
						
						if (createdDate != null && createdDate.compareTo(cal.getTime()) < 0) {
							continue;
						}
					}
					
					JSONArray activityProperties = lastActivityData.getJSONArray(PROP_PROPERTIES);
					JSONArray itemProperties = newActivityData.getJSONArray(PROP_PROPERTIES);
					for (int i = 0; i < activityProperties.length(); i++) {
						JSONObject activityProperty = activityProperties.getJSONObject(i);
						boolean isSameProperty = false;
						for (int j = 0; j < itemProperties.length(); j++) {
							JSONObject itemProperty = itemProperties.getJSONObject(j);
							if (itemProperty.get(PROP_TITLE).equals(activityProperty.get(PROP_TITLE))) {
								isSameProperty = true;
								PropertyDefinition property = dictionaryService
										.getProperty(QName.createQName((String) activityProperty.get(PROP_TITLE)));
								
								if ((property == null) || (property.getDataType() == null)
										|| (!DataTypeDefinition.TEXT.equals(property.getDataType().getName())
												&& !DataTypeDefinition.MLTEXT.equals(property.getDataType().getName()))) {
									if (activityProperty.has(BEFORE)) {
										itemProperty.put(BEFORE, activityProperty.get(BEFORE));
									} else {
										itemProperty.put(BEFORE, "");
									}
								}
								
							}
						}
						if (!isSameProperty) {
							itemProperties.put(activityProperty);
						}
					}
					newActivityData.put(PROP_PROPERTIES, itemProperties);
					newActivity.setActivityData(newActivityData.toString());
					
					deleteAuditActivity(lastActivity);
					
				}

			} catch (JSONException e) {
				logger.error("parse json", e);
			}

		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean postStateChangeActivity(NodeRef entityNodeRef, NodeRef datalistNodeRef, String beforeState, String afterState) {
		if ((entityNodeRef != null) && (beforeState != null) && (afterState != null)) {
			try {
				policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
				NodeRef activityListNodeRef = getActivityList(entityNodeRef);

				// No list no activity
				if (activityListNodeRef != null) {

					if (nodeService.hasAspect(activityListNodeRef, ContentModel.ASPECT_PENDING_DELETE)) {
						logger.debug(NO_ACTIVITY_MESSAGE);
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

					data.put(PROP_BEFORE_STATE, beforeState);
					data.put(PROP_AFTER_STATE, afterState);
					activityListDataItem.setActivityType(ActivityType.State);
					activityListDataItem.setActivityData(data.toString());
					activityListDataItem.setParentNodeRef(activityListNodeRef);
					
					recordAuditActivity(entityNodeRef, activityListDataItem);

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

	/** {@inheritDoc} */
	@Override
	public boolean postVersionActivity(NodeRef entityNodeRef, NodeRef versionNodeRef, String versionLabel) {
		if ((entityNodeRef != null) && (versionNodeRef != null)) {
			try {
				policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
				NodeRef activityListNodeRef = getActivityList(entityNodeRef);

				// No list no activity
				if (activityListNodeRef != null) {

					if (nodeService.hasAspect(activityListNodeRef, ContentModel.ASPECT_PENDING_DELETE)) {
						logger.debug(NO_ACTIVITY_MESSAGE);
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

					recordAuditActivity(entityNodeRef, activityListDataItem);

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

	/** {@inheritDoc} */
	@Override
	public void mergeActivities(NodeRef fromNodeRef, NodeRef toNodeRef) {

		AuditQuery auditQuery = AuditQuery.createQuery()
				.filter(ActivityAuditPlugin.ENTITY_NODEREF, toNodeRef.toString()).maxResults(RepoConsts.MAX_RESULTS_1000000);
		
		List<JSONObject> fromActivities = beCPGAuditService.listAuditEntries(AuditType.ACTIVITY, auditQuery);
		
		for (JSONObject fromActivity : fromActivities) {
			ActivityListDataItem activityItem = AuditActivityHelper.parseActivity(fromActivity);
			recordAuditActivity(fromNodeRef, activityItem);
		}

	}

	/** {@inheritDoc} */
	@Override
	public boolean postEntityActivity(NodeRef entityNodeRef, ActivityType activityType, ActivityEvent activityEvent,
			Map<QName, Pair<List<Serializable>, List<Serializable>>> updatedProperties) {
		try {
			policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);

			NodeRef activityListNodeRef = getActivityList(entityNodeRef);

			// No list no activity
			if (activityListNodeRef != null) {

				if (nodeService.hasAspect(activityListNodeRef, ContentModel.ASPECT_PENDING_DELETE)) {
					logger.debug(NO_ACTIVITY_MESSAGE);
					return false;
				}

				ActivityListDataItem activityListDataItem = new ActivityListDataItem();
				// Don't save System activities
				if (!AuthenticationUtil.getSystemUserName().equals(activityListDataItem.getUserId())) {
					JSONObject data = new JSONObject();
					if (activityEvent != null) {
						data.put(PROP_ACTIVITY_EVENT, activityEvent.toString());
					}

					data.put(PROP_ENTITY_NODEREF, entityNodeRef);
					data.put(PROP_ENTITY_TYPE, nodeService.getType(entityNodeRef));
					data.put(PROP_TITLE, nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME));
					if (activityEvent.equals(ActivityEvent.Update) && (updatedProperties != null)) {
						List<JSONObject> properties = new ArrayList<>();
						for (Map.Entry<QName, Pair<List<Serializable>, List<Serializable>>> entry : updatedProperties.entrySet()) {
							JSONObject property = new JSONObject();

							property.put(PROP_TITLE, entry.getKey());

							MLText mlTextBefore = null;
							MLText mlTextAfter = null;
							MLText newMlTextBefore = null;
							MLText newMlTextAfter = null;

							if (entry.getValue().getFirst() instanceof List) {
								for (Object obj : (List<?>) entry.getValue().getFirst()) {
									if (obj instanceof MLText) {
										mlTextBefore = (MLText) obj;
									}
								}
							}
							if (entry.getValue().getSecond() instanceof List) {
								for (Object obj : (List<?>) entry.getValue().getSecond()) {
									if (obj instanceof MLText) {
										mlTextAfter = (MLText) obj;
									}
								}
							}

							if (mlTextBefore != null) {
								newMlTextBefore = compareMLTexts(mlTextBefore, mlTextAfter);
							}

							if (mlTextAfter != null) {
								newMlTextAfter = compareMLTexts(mlTextAfter, mlTextBefore);
							}

							if (newMlTextBefore != null) {
								Iterator<Entry<Locale, String>> it = mlTextBefore.entrySet().iterator();

								while (it.hasNext()) {
									Locale locale = it.next().getKey();
									mlTextBefore.put(locale, newMlTextBefore.get(locale));
								}
							}

							if (newMlTextAfter != null) {
								Iterator<Entry<Locale, String>> it = mlTextAfter.entrySet().iterator();

								while (it.hasNext()) {
									Locale locale = it.next().getKey();
									mlTextAfter.put(locale, newMlTextAfter.get(locale));
								}
							}

							if (entry.getValue().getFirst() != null) {
								ArrayList<Object> beforeList = new ArrayList<>();

								for (Serializable ent : entry.getValue().getFirst()) {
									if (ent instanceof Date) {
										beforeList.add(ISO8601DateFormat.format((Date) ent));
									} else if (ent instanceof Pair || ent instanceof NodeRef) {
										beforeList.add(ent.toString());
									} else {
										beforeList.add(ent);
									}
								}
								property.put(BEFORE, beforeList);
							} else {
								property.put(BEFORE, entry.getValue().getFirst());
							}

							if (data.has(PROP_TITLE) && (data.get(PROP_TITLE) != null) && entry.getKey().equals(ContentModel.PROP_NAME)) {
								property.put(AFTER, data.get(PROP_TITLE));
							} else {

								if (entry.getValue().getSecond() != null) {
									ArrayList<Object> afterList = new ArrayList<>();

									for (Serializable ent : entry.getValue().getSecond()) {
										if (ent instanceof Date) {
											afterList.add(ISO8601DateFormat.format((Date) ent));
										} else if (ent instanceof Pair || ent instanceof NodeRef) {
											afterList.add(ent.toString());
										} else {
											afterList.add(ent);
										}
									}
									property.put(AFTER, afterList);
								} else {
									property.put(AFTER, entry.getValue().getSecond());
								}

							}
							properties.add(property);
						}
						data.put(PROP_PROPERTIES, new JSONArray(properties));
					}

					if (!activityType.equals(ActivityType.Entity) || !activityEvent.equals(ActivityEvent.Update) || (updatedProperties != null)) {
						activityListDataItem.setActivityType(activityType);
						activityListDataItem.setActivityData(data.toString());
						activityListDataItem.setParentNodeRef(activityListNodeRef);

						mergeWithLastActivity(activityListDataItem);

						recordAuditActivity(entityNodeRef, activityListDataItem);

						notifyListeners(entityNodeRef, activityListDataItem);
					}

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

	/** {@inheritDoc} */
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
	 *
	 * @see fr.becpg.repo.activity.EntityActivityService#cleanActivities() This methods will lunched by a scheduled job which will allow system to merge activities System will merge activities of type
	 * formulation,report and datalist of the same user.
	 *
	 */

	/** {@inheritDoc} */
	@Override
	public BatchInfo cleanActivities() {

		BatchInfo batchInfo = new BatchInfo("cleanActivities", "becpg.batch.activity.cleanActivities");
		batchInfo.setRunAsSystem(true);
		batchInfo.setPriority(BatchPriority.VERY_LOW);

		BatchProcessWorkProvider<NodeRef> workProvider = createActivityProcessWorkProvider();

		BatchProcessWorker<NodeRef> processWorker = new BatchProcessor.BatchProcessWorkerAdaptor<>() {

			@Override
			public void process(NodeRef entityNodeRef) throws Throwable {

				try {

					policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);

					if (logger.isDebugEnabled()) {
						logger.debug("group activities of entity : " + entityNodeRef);
					}

					NodeRef activityListNodeRef = getActivityList(entityNodeRef);
					if (activityListNodeRef != null) {

						Set<String> users = new HashSet<>();
						Date cronDate = new Date();

						// Get Activity list ordered by the date of creation
						AuditQuery auditQuery = AuditQuery.createQuery()
								.asc(false)
								.dbAsc(false)
								.sortBy(ActivityAuditPlugin.PROP_CM_CREATED)
								.filter(ActivityAuditPlugin.ENTITY_NODEREF, entityNodeRef.toString())
								.maxResults(RepoConsts.MAX_RESULTS_1000000);
						
						List<ActivityListDataItem> sortedActivityList = new ArrayList<>();
						
						List<JSONObject> results = beCPGAuditService.listAuditEntries(AuditType.ACTIVITY, auditQuery);

						for (JSONObject result : results) {
							sortedActivityList.add(AuditActivityHelper.parseActivity(result));
						}

						int nbrActivity = sortedActivityList.size();
						// Keep the first 50 activities
						sortedActivityList = sortedActivityList.subList(nbrActivity > MAX_PAGE ? MAX_PAGE : 0, nbrActivity > MAX_PAGE ? nbrActivity : 0);

						nbrActivity = sortedActivityList.size();

						if (logger.isDebugEnabled()) {
							logger.debug("nbrActivity: " + nbrActivity);
						}

						// Clean activities which are not in the first page.
						if (nbrActivity > 0) {
							Map<ActivityType, List<ActivityListDataItem>> activitiesByType = new EnumMap<>(ActivityType.class);
							int activityInPage = 0;
							boolean hasFormulation = false;
							boolean hasReport = false;
							Set<String> contentSet = new HashSet<>();

							for (ActivityListDataItem activity : sortedActivityList) {
								if (activityInPage == MAX_PAGE) {
									hasFormulation = false;
									hasReport = false;
									activityInPage = 0;
								}

								Date created = activity.getCreatedDate();
								if (cronDate.after(created)) {
									cronDate = created;
								}

								ActivityType activityType = activity.getActivityType();

								if ((activityType.equals(ActivityType.Formulation) && hasFormulation)
										|| (activityType.equals(ActivityType.Report) && hasReport)) {
									deleteAuditActivity(activity);
									nbrActivity--;
								} else if (activityType.equals(ActivityType.Content)) {
									String contentNodeRef = extractContentNode(activity.getActivityData());
									if (contentNodeRef != null) {
										if (!contentSet.contains(contentNodeRef)) {
											contentSet.add(contentNodeRef);
										} else {
											deleteAuditActivity(activity);
											nbrActivity--;
										}
									}
								}
								// Arrange activities by type
								else {
									if (!activitiesByType.containsKey(activityType)) {
										activitiesByType.put(activityType, new ArrayList<>());
									}
									hasFormulation = activityType.equals(ActivityType.Formulation) ? true : hasFormulation;
									hasReport = activityType.equals(ActivityType.Report) ? true : hasReport;
									activitiesByType.get(activityType).add(activity);
									users.add(activity.getUserId());
								}
							}

							List<ActivityListDataItem> dlActivities = activitiesByType.get(ActivityType.Datalist);
							if (dlActivities != null) {
								// Group list by day/week/month/year
								int[] groupTime = { Calendar.DAY_OF_YEAR, Calendar.WEEK_OF_YEAR, Calendar.MONTH, Calendar.YEAR };
								for (int i = 0; (i < groupTime.length) && (nbrActivity > MAX_PAGE); i++) {
									dlActivities = group(dlActivities, users, groupTime[i], cronDate);
									nbrActivity += (dlActivities.size() - activitiesByType.get(ActivityType.Datalist).size());
								}
							}
						}
					}

				} finally {
					policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
				}

			}
		};

		batchQueueService.queueBatch(batchInfo, workProvider, processWorker, null);

		return batchInfo;
	}

	private String extractContentNode(String alData) {
		JSONObject data = new JSONObject(alData);
		if (data.has("contentNodeRef")) {
			return data.getString("contentNodeRef");
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void clearAllActivities(NodeRef entityTplNodeRef) {
		try {
			policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
			NodeRef activityListNodeRef = getActivityList(entityTplNodeRef);
			if (activityListNodeRef != null) {
				AuditQuery auditQuery = AuditQuery.createQuery().asc(false).dbAsc(false)
						.sortBy(ActivityAuditPlugin.PROP_CM_CREATED)
						.filter(ActivityAuditPlugin.ENTITY_NODEREF, entityTplNodeRef.toString())
						.maxResults(RepoConsts.MAX_RESULTS_1000000);
				
				List<ActivityListDataItem> sortedActivityList = new ArrayList<>();
				
				List<JSONObject> results = beCPGAuditService.listAuditEntries(AuditType.ACTIVITY, auditQuery);

				for (JSONObject result : results) {
					sortedActivityList.add(AuditActivityHelper.parseActivity(result));
				}
				
				for (ActivityListDataItem activity : sortedActivityList) {
					deleteAuditActivity(activity);
				}

			}
		} finally {
			policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
		}

	}

	// Group activities
	private List<ActivityListDataItem> group(List<ActivityListDataItem> activities, Set<String> users, int timePeriod, Date cronDate) {
		// Ignore the last day/week/month/year
		Calendar maxLimit = Calendar.getInstance();
		maxLimit.setTime(new Date());
		maxLimit.set(Calendar.HOUR_OF_DAY, 23);
		Calendar minLimit = Calendar.getInstance();
		minLimit.setTime(new Date());
		minLimit.set(Calendar.HOUR_OF_DAY, 0);

		switch (timePeriod) {
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
		default:
			break;
		}

		// Repeat till the oldest activity
		while (maxLimit.getTime().after(cronDate)) {

			for (String userId : users) {

				if (activities != null) {
					Map<NodeRef, List<String>> activitiesByEntity = new HashMap<>();
					List<ActivityListDataItem> removedNodes = new ArrayList<>();

					for (ActivityListDataItem activity : activities) {

						Date createdDate = activity.getCreatedDate();

						NodeRef activityParentNodeRef = activity.getParentNodeRef();
						String strData = activity.getActivityData();
						JSONTokener tokener = new JSONTokener(strData);
						String datalistClassName = null;
						try {
							JSONObject data = new JSONObject(tokener);
							datalistClassName = (String) data.get(PROP_CLASSNAME);
						} catch (JSONException e) {
							logger.error("Problem occurred while parsing data activity!! ", e);
						}

						if (createdDate.after(minLimit.getTime()) && createdDate.before(maxLimit.getTime()) && activity.getUserId().equals(userId)) {
							// group same data-list activity
							if (activitiesByEntity.containsKey(activityParentNodeRef)
									&& activitiesByEntity.get(activityParentNodeRef).contains(datalistClassName)) {
								removedNodes.add(activity);
								
								deleteAuditActivity(activity);
							} else {

								if (!activitiesByEntity.containsKey(activityParentNodeRef)) {
									activitiesByEntity.put(activityParentNodeRef, new ArrayList<>());
								}
								activitiesByEntity.get(activityParentNodeRef).add(datalistClassName);
							}
						}
					}
					activities.removeAll(removedNodes);
				}
			}

			// Move to previous period
			maxLimit.add(timePeriod, -1);
			minLimit.add(timePeriod, -1);
		}

		return activities;
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getEntityNodeRefForActivity(NodeRef nodeRef, QName itemType) {
		if (nodeService.exists(nodeRef) && !nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)
				&& !nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)) {
			return entityService.getEntityNodeRef(nodeRef, itemType);
		}
		return null;
	}

	private BatchProcessWorkProvider<NodeRef> createActivityProcessWorkProvider() {
		List<NodeRef> entityNodeRefs = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().ofType(BeCPGModel.TYPE_ENTITY_V2).excludeVersions().inDB().ftsLanguage()
					.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED);
			return queryBuilder.list();
		}, true, true);

		return new EntityListBatchProcessWorkProvider<>(entityNodeRefs);

	}

	@Override
	public void postExportActivity(NodeRef entityNodeRef, QName dataType, String fileName) {
		logger.info("Exporting:" + fileName + " " + dataType + " " + entityNodeRef + " " + AuthenticationUtil.getFullyAuthenticatedUser());

		if (entityNodeRef != null) {
			postEntityExportActivity(entityNodeRef, dataType, fileName);
		} else {
			postAlfrescoExportActivity(fileName);
		}
	}

	private void postAlfrescoExportActivity(String fileName) {
		JSONObject data = new JSONObject();

		try {
			data.put("title", fileName);
		} catch (JSONException e) {
			logger.error(e.getMessage(), e);
		}

		activityService.postActivity(EXPORT_ACTIVITY, null, "export", data.toString());
	}

	private void postEntityExportActivity(NodeRef entityNodeRef, QName dataType, String fileName) {
		try {
			policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);

			NodeRef activityListNodeRef = getActivityList(entityNodeRef);

			// No list no activity
			if (activityListNodeRef != null) {

				NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
				NodeRef datalistNodeRef = null;

				if (listContainerNodeRef != null) {
					datalistNodeRef = entityListDAO.getList(listContainerNodeRef, dataType);
				}

				ActivityListDataItem activityListDataItem = new ActivityListDataItem();
				// Don't save System activities
				if (!AuthenticationUtil.getSystemUserName().equals(activityListDataItem.getUserId())) {
					JSONObject data = new JSONObject();

					data.put(PROP_TITLE, fileName);
					if (datalistNodeRef != null) {
						data.put(PROP_CLASSNAME, attributeExtractorService.extractMetadata(dataType, datalistNodeRef));
					}

					activityListDataItem.setActivityType(ActivityType.Export);
					activityListDataItem.setActivityData(data.toString());
					activityListDataItem.setParentNodeRef(activityListNodeRef);

					recordAuditActivity(entityNodeRef, activityListDataItem);

					notifyListeners(entityNodeRef, activityListDataItem);

				}
			}
		} catch (JSONException e) {
			logger.error(e, e);
		} finally {
			policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
		}
	}

}

