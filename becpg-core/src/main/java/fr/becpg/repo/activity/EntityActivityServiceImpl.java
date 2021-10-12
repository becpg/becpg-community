package fr.becpg.repo.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.forum.CommentService;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
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
import org.springframework.util.StopWatch;

import com.google.common.collect.Lists;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.activity.data.ActivityEvent;
import fr.becpg.repo.activity.data.ActivityListDataItem;
import fr.becpg.repo.activity.data.ActivityType;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.LargeTextHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>EntityActivityServiceImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("entityActivityService")
public class EntityActivityServiceImpl implements EntityActivityService {


	private static Log logger = LogFactory.getLog(EntityActivityServiceImpl.class);

	private static final int MAX_PAGE = 50;
	
	public static final int ML_TEXT_SIZE_LIMIT = 200;

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

	@Autowired
	NamespaceService namespaceService;

	@Autowired
	public DictionaryService dictionaryService;

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
	public NodeRef postCommentActivity(NodeRef entityNodeRef, NodeRef commentNodeRef, ActivityEvent activityEvent,
			boolean notifyObservers) {
		if (commentNodeRef != null) {
			try {
				policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
				NodeRef activityListNodeRef = getActivityList(entityNodeRef);

				// No list no activity
				if (activityListNodeRef != null) {

					if (nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_TPL)
							|| nodeService.hasAspect(activityListNodeRef, ContentModel.ASPECT_PENDING_DELETE)) {
						logger.debug("No activity on entity template or pending delete node");
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
					alfrescoRepository.save(activityListDataItem);

					if (notifyObservers) {
						notifyListeners(entityNodeRef, activityListDataItem);
					}

					return activityListDataItem.getNodeRef();
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

	/** {@inheritDoc} */
	@Override
	public boolean postMergeBranchActivity(NodeRef branchNodeRef, NodeRef branchToNodeRef, VersionType versionType,
			String description) {
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
						}
						else {
							data.put(PROP_TITLE, nodeService.getProperty(datalistNodeRef, ContentModel.PROP_NAME));
						}
					}
					if (activityEvent.equals(ActivityEvent.Update) && updatedProperties != null) {
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
								
								LargeTextHelper.elipse(mlTextBefore);
								
								newMlTextBefore = new MLText();
								
								Iterator<Entry<Locale, String>> it = mlTextBefore.entrySet().iterator();
								
								while (it.hasNext()) {
									Locale locale = it.next().getKey();
									
									String textBefore = mlTextBefore.get(locale);
									
									newMlTextBefore.put(locale, textBefore);
									
									if (textBefore != null && textBefore.length() > ML_TEXT_SIZE_LIMIT) {
										String textAfter = mlTextAfter != null ? mlTextAfter.get(locale) : null;
										if (textAfter == null || textAfter.length() <= ML_TEXT_SIZE_LIMIT) {
											textBefore = textBefore.substring(0, ML_TEXT_SIZE_LIMIT) + " ...";
											newMlTextBefore.put(locale, textBefore);
										} else {
											Pair<String,String> diffs = LargeTextHelper.createTextDiffs(textBefore, textAfter);
											
											textBefore = diffs.getFirst().replace(" ", "").equals("") ? textBefore :  diffs.getFirst();
											textBefore = textBefore.length() > ML_TEXT_SIZE_LIMIT ? textBefore.substring(0, ML_TEXT_SIZE_LIMIT) + " ..." : textBefore;
											
											newMlTextBefore.put(locale, textBefore);
										}
									}
								}
							}
							
							if (mlTextAfter != null) {
								
								LargeTextHelper.elipse(mlTextAfter);

								newMlTextAfter = new MLText();
								
								Iterator<Entry<Locale, String>> it = mlTextAfter.entrySet().iterator();

								while (it.hasNext()) {
									Locale locale = it.next().getKey();
									
									String textAfter = mlTextAfter.get(locale);
									
									newMlTextAfter.put(locale, textAfter);

									if (textAfter != null && textAfter.length() > ML_TEXT_SIZE_LIMIT) {
										String textBefore = mlTextBefore != null ? mlTextBefore.get(locale) : null;
										if (textBefore == null || textBefore.length() <= ML_TEXT_SIZE_LIMIT) {
											textAfter = textAfter.substring(0, ML_TEXT_SIZE_LIMIT) + " ...";
											newMlTextAfter.put(locale, textAfter);
										} else {
											Pair<String,String> diffs = LargeTextHelper.createTextDiffs(textBefore, textAfter);
											
											textAfter = diffs.getSecond().replace(" ", "").equals("") ? textAfter : diffs.getSecond();
											textAfter = textAfter.length() > ML_TEXT_SIZE_LIMIT ? textAfter.substring(0, ML_TEXT_SIZE_LIMIT) + " ..." : textAfter;

											newMlTextAfter.put(locale, textAfter);
										}
									}
								}
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
							if (entry.getValue().getFirst() instanceof List && ((List<?>) entry.getValue().getFirst()).get(0) instanceof Date) {
								property.put(BEFORE, ISO8601DateFormat.format((Date) ((List<?>) entry.getValue().getFirst()).get(0)));
							} else {
								property.put(BEFORE, entry.getValue().getFirst());
							}
							if (entry.getKey().equals(ContentModel.PROP_NAME)) {
								property.put(AFTER, data.get(PROP_TITLE));
							} else {
								if (entry.getValue().getSecond() instanceof List && ((List<?>) entry.getValue().getSecond()).get(0) instanceof Date) {
									property.put(AFTER, ISO8601DateFormat.format((Date) ((List<?>) entry.getValue().getSecond()).get(0)));
								} else {
									property.put(AFTER, entry.getValue().getSecond());
								}
							}
							properties.add(property);
						}
						data.put(PROP_PROPERTIES, new JSONArray(properties));
					}			
					if (!activityEvent.equals(ActivityEvent.Update) || updatedProperties != null) {
						activityListDataItem.setActivityType(ActivityType.Datalist);
						activityListDataItem.setActivityData(data.toString());
						activityListDataItem.setParentNodeRef(activityListNodeRef);

						mergeWithLastActivity(activityListDataItem);

						alfrescoRepository.save(activityListDataItem);

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

	

	// TODO Slow better to have it async
	private void mergeWithLastActivity(ActivityListDataItem item) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, -1);

		NodeRef activityListNodeRef = item.getParentNodeRef();
		// Activities in the last hour
		BeCPGQueryBuilder query = BeCPGQueryBuilder.createQuery().parent(activityListNodeRef)
				.ofType(BeCPGModel.TYPE_ACTIVITY_LIST)
				.andBetween(ContentModel.PROP_CREATED, "'" + ISO8601DateFormat.format(cal.getTime()) + "'",
						"'" + ISO8601DateFormat.format(new Date(Long.MAX_VALUE)) + "'")
				.addSort(ContentModel.PROP_CREATED, false).inDB();

		List<NodeRef> sortedActivityList = query.list();

		// The last created activity
		if (sortedActivityList.isEmpty()) {
			sortedActivityList = BeCPGQueryBuilder.createQuery().parent(activityListNodeRef)
					.ofType(BeCPGModel.TYPE_ACTIVITY_LIST).addSort(ContentModel.PROP_CREATED, false).maxResults(1)
					.inDB().list();
		}

		for (NodeRef activityListItemNodeRef : sortedActivityList) {

			ActivityListDataItem activity = alfrescoRepository.findOne(activityListItemNodeRef);

			JSONObject activityData = null, itemData = null;
			try {
				activityData = new JSONObject(activity.getActivityData());
				itemData = new JSONObject(item.getActivityData());
				if (((activity.getActivityData().equals(item.getActivityData())) || (!itemData.has(PROP_TITLE)
						&& !activityData.has(PROP_TITLE) && item.getActivityType().equals(ActivityType.Datalist)
						&& activityData.get(PROP_CLASSNAME).equals(itemData.get(PROP_CLASSNAME))
						&& activityData.get(PROP_ACTIVITY_EVENT).equals(itemData.get(PROP_ACTIVITY_EVENT))))
						&& activity.getUserId().equals(item.getUserId())
						&& activity.getActivityType().equals(item.getActivityType())) {
					nodeService.addAspect(activityListItemNodeRef, ContentModel.ASPECT_TEMPORARY, null);
					nodeService.deleteNode(activityListItemNodeRef);

					if (logger.isDebugEnabled()) {
						logger.debug("Merge with the last activity " + activity.getActivityType());
					}

					return;
					// Add previous updated properties in the data of the last activity
				} else if (itemData.has(PROP_PROPERTIES) && activityData.has(PROP_PROPERTIES)
						&& itemData.get(PROP_ACTIVITY_EVENT).equals(activityData.get(PROP_ACTIVITY_EVENT))
						&& item.getUserId().equals(activity.getUserId())
						&& item.getActivityType().equals(activity.getActivityType())
						&& itemData.has(PROP_TITLE) && activityData.has(PROP_TITLE)
						&& itemData.get(PROP_TITLE).equals(activityData.get(PROP_TITLE))
						&& ((!itemData.has(PROP_CLASSNAME) && !activityData.has(PROP_CLASSNAME))
								|| (itemData.has(PROP_CLASSNAME) && activityData.has(PROP_CLASSNAME) && itemData.get(PROP_CLASSNAME).equals(activityData.get(PROP_CLASSNAME))))) {
					JSONArray activityProperties = activityData.getJSONArray(PROP_PROPERTIES);
					JSONArray itemProperties = itemData.getJSONArray(PROP_PROPERTIES);
					for (int i = 0; i < activityProperties.length(); i++) {
						JSONObject activityProperty = activityProperties.getJSONObject(i);
						boolean isSameProperty = false;
						for (int j = 0; j < itemProperties.length(); j++) {
							JSONObject itemProperty = itemProperties.getJSONObject(j);
							if (itemProperty.get(PROP_TITLE).equals(activityProperty.get(PROP_TITLE))) {
								isSameProperty = true;
								PropertyDefinition property = dictionaryService.getProperty(QName.createQName((String) activityProperty.get(PROP_TITLE)));
								
								if (property == null || property.getDataType() == null || !DataTypeDefinition.TEXT.equals(property.getDataType().getName()) && !DataTypeDefinition.MLTEXT.equals(property.getDataType().getName())) {
									if (activityProperty.has(BEFORE)) {
										itemProperty.put(BEFORE,activityProperty.get(BEFORE));
									} else {
										itemProperty.put(BEFORE,"");
									}
								}
								
							}
						}
						if (!isSameProperty) {
							itemProperties.put(activityProperty);
						}
					}
					itemData.put(PROP_PROPERTIES, itemProperties);
					item.setActivityData(itemData.toString());
					alfrescoRepository.save(item);

					nodeService.addAspect(activityListItemNodeRef, ContentModel.ASPECT_TEMPORARY, null);
					nodeService.deleteNode(activityListItemNodeRef);

				}

			} catch (JSONException e) {
				logger.error("parse json", e);
			}

		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean postStateChangeActivity(NodeRef entityNodeRef, NodeRef datalistNodeRef, String beforeState,
			String afterState) {
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

					data.put(PROP_BEFORE_STATE, beforeState);
					data.put(PROP_AFTER_STATE, afterState);
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

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
	@Override
	public void mergeActivities(NodeRef fromNodeRef, NodeRef toNodeRef) {
		try {
			policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);

			NodeRef toActivityListNodeRef = getActivityList(toNodeRef);
			if (toActivityListNodeRef != null) {
				NodeRef activityListNodeRef = getActivityList(fromNodeRef);
				if (activityListNodeRef != null) {

					for (NodeRef listItem : entityListDAO.getListItems(activityListNodeRef,
							BeCPGModel.TYPE_ACTIVITY_LIST)) {
						nodeService.moveNode(listItem, toActivityListNodeRef, ContentModel.ASSOC_CONTAINS,
								ContentModel.ASSOC_CONTAINS);
					}
				}
			}
		} finally {
			policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
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

					data.put(PROP_ENTITY_NODEREF, entityNodeRef);
					data.put(PROP_ENTITY_TYPE, nodeService.getType(entityNodeRef));
					data.put(PROP_TITLE, nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME));
					if (activityEvent.equals(ActivityEvent.Update) && updatedProperties != null) {
						List<JSONObject> properties = new ArrayList<>();
						for (Map.Entry<QName, Pair<List<Serializable>, List<Serializable>>> entry : updatedProperties
								.entrySet()) {
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
								
								LargeTextHelper.elipse(mlTextBefore);

								newMlTextBefore = new MLText();
								
								Iterator<Entry<Locale, String>> it = mlTextBefore.entrySet().iterator();
								
								while (it.hasNext()) {
									Locale locale = it.next().getKey();
									
									String textBefore = mlTextBefore.get(locale);
									
									newMlTextBefore.put(locale, textBefore);
									
									if (textBefore != null && textBefore.length() > ML_TEXT_SIZE_LIMIT) {
										String textAfter = mlTextAfter != null ? mlTextAfter.get(locale) : null;
										if (textAfter == null || textAfter.length() <= ML_TEXT_SIZE_LIMIT) {
											textBefore = textBefore.substring(0, ML_TEXT_SIZE_LIMIT) + " ...";
											newMlTextBefore.put(locale, textBefore);
										} else {
											Pair<String,String> diffs = LargeTextHelper.createTextDiffs(textBefore, textAfter);
											
											textBefore = diffs.getFirst().replace(" ", "").equals("") ? textBefore : diffs.getFirst();
											textBefore = textBefore.length() > ML_TEXT_SIZE_LIMIT ? textBefore.substring(0, ML_TEXT_SIZE_LIMIT) + " ..." : textBefore;
											
											newMlTextBefore.put(locale, textBefore);
										}
									}
								}
							}
							
							if (mlTextAfter != null) {
								
								LargeTextHelper.elipse(mlTextAfter);

								newMlTextAfter = new MLText();
								
								Iterator<Entry<Locale, String>> it = mlTextAfter.entrySet().iterator();

								while (it.hasNext()) {
									Locale locale = it.next().getKey();
									
									String textAfter = mlTextAfter.get(locale);
									
									newMlTextAfter.put(locale, textAfter);

									if (textAfter != null && textAfter.length() > ML_TEXT_SIZE_LIMIT) {
										String textBefore = mlTextBefore != null ? mlTextBefore.get(locale) : null;
										if (textBefore == null || textBefore.length() <= ML_TEXT_SIZE_LIMIT) {
											textAfter = textAfter.substring(0, ML_TEXT_SIZE_LIMIT) + " ...";
											newMlTextAfter.put(locale, textAfter);
										} else {
											Pair<String,String> diffs = LargeTextHelper.createTextDiffs(textBefore, textAfter);
											
											textAfter = diffs.getSecond().replace(" ", "").equals("") ? textAfter :  diffs.getSecond();
											textAfter = textAfter.length() > ML_TEXT_SIZE_LIMIT ? textAfter.substring(0, ML_TEXT_SIZE_LIMIT) + " ..." : textAfter;

											newMlTextAfter.put(locale, textAfter);
										}
									}
								}
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
							
							if (entry.getValue().getFirst() instanceof List && ((List<?>) entry.getValue().getFirst()).get(0) instanceof Date) {
								property.put(BEFORE, ISO8601DateFormat.format((Date) ((List<?>) entry.getValue().getFirst()).get(0)));
							} else {
								property.put(BEFORE, entry.getValue().getFirst());
							}
							if (data.has(PROP_TITLE) && data.get(PROP_TITLE) != null
									&& entry.getKey().equals(ContentModel.PROP_NAME)) {
								property.put(AFTER, data.get(PROP_TITLE));
							} else {
								if (entry.getValue().getSecond() instanceof List && ((List<?>) entry.getValue().getSecond()).get(0) instanceof Date) {
									property.put(AFTER, ISO8601DateFormat.format((Date) ((List<?>) entry.getValue().getSecond()).get(0)));
								} else {
									property.put(AFTER, entry.getValue().getSecond());
								}
							}
							properties.add(property);
						}
						data.put(PROP_PROPERTIES, new JSONArray(properties));
					}

					if (!activityType.equals(ActivityType.Entity) || !activityEvent.equals(ActivityEvent.Update) || updatedProperties != null) {
						activityListDataItem.setActivityType(activityType);
						activityListDataItem.setActivityData(data.toString());
						activityListDataItem.setParentNodeRef(activityListNodeRef);

						mergeWithLastActivity(activityListDataItem);

						alfrescoRepository.save(activityListDataItem);

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
	 * @see fr.becpg.repo.activity.EntityActivityService#cleanActivities() This
	 * methods will lunched by a scheduled job which will allow system to merge
	 * activities System will merge activities of type formulation,report and
	 * datalist of the same user.
	 * 
	 */

	/** {@inheritDoc} */
	@Override
	public void cleanActivities() {

		List<NodeRef> entityNodeRefs = getEntitiesToPurge();
		if (logger.isDebugEnabled()) {
			logger.debug("Clean activities, Number of entities: " + entityNodeRefs.size());
		}
		doInBatch(entityNodeRefs, 10);
	}

	/** {@inheritDoc} */
	public void clearAllActivities(NodeRef entityTplNodeRef){
		try {
			policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
			NodeRef activityListNodeRef = getActivityList(entityTplNodeRef);
			if (activityListNodeRef != null) {
				List<NodeRef> activityListDataItemNodeRefs = entityListDAO.getListItems(activityListNodeRef,
						BeCPGModel.TYPE_ACTIVITY_LIST);
				for (NodeRef activityItemNodeRef : activityListDataItemNodeRefs) {
					nodeService.addAspect(activityItemNodeRef, ContentModel.ASPECT_TEMPORARY, null);
					nodeService.deleteNode(activityItemNodeRef);
				}
			}
		} finally {
			policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
		}

	}

	private void doInBatch(final List<NodeRef> entityNodeRefs, final int batchSize) {

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		L2CacheSupport.doInCacheContext(() -> {

			for (final List<NodeRef> subList : Lists.partition(entityNodeRefs, batchSize)) {
				RunAsWork<Object> actionRunAs = () -> {
					RetryingTransactionCallback<Object> actionCallback = () -> {
						try {
							policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
							for (final NodeRef entityNodeRef : subList) {
								if (logger.isDebugEnabled()) {
									logger.debug("group activities of entity : " + entityNodeRef);
								}

								NodeRef activityListNodeRef = getActivityList(entityNodeRef);
								if (activityListNodeRef != null) {

									Set<String> users = new HashSet<>();
									Date cronDate = new Date();
									
									//Clear all activities of entity templates
									if (nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_TPL)){
										clearAllActivities(entityNodeRef);
									}

									// Get Activity list ordered by the date of creation
									List<NodeRef> activityListDataItemNodeRefs = entityListDAO.getListItems(activityListNodeRef,
											BeCPGModel.TYPE_ACTIVITY_LIST, SORT_MAP);
									Collections.reverse(activityListDataItemNodeRefs);

									int nbrActivity = activityListDataItemNodeRefs.size();
									// Keep the first 50 activities
									activityListDataItemNodeRefs = activityListDataItemNodeRefs.subList(
											nbrActivity > MAX_PAGE ? MAX_PAGE : 0, nbrActivity > MAX_PAGE ? nbrActivity : 0);

									nbrActivity = activityListDataItemNodeRefs.size();

									if (logger.isDebugEnabled()) {
										logger.debug("nbrActivity: " + nbrActivity);
									}

									// Clean activities which are not in the first page.
									if (nbrActivity > 0) {
										Map<ActivityType, List<NodeRef>> activitiesByType = new HashMap<>();
										ActivityListDataItem activity;
										int activityInPage = 0;
										boolean hasFormulation = false;
										boolean hasReport = false;

										for (NodeRef activityItemNodeRef : activityListDataItemNodeRefs) {
											if (activityInPage == MAX_PAGE) {
												hasFormulation = false;
												hasReport = false;
												activityInPage = 0;
											}

											Date created = (Date) nodeService.getProperty(activityItemNodeRef,
													ContentModel.PROP_CREATED);
											if (cronDate.after(created)) {
												cronDate = created;
											}

											activity = alfrescoRepository.findOne(activityItemNodeRef);
											ActivityType activityType = activity.getActivityType();

											if ((activityType.equals(ActivityType.Formulation) && hasFormulation)
													|| (activityType.equals(ActivityType.Report) && hasReport)) {
												nodeService.addAspect(activityItemNodeRef, ContentModel.ASPECT_TEMPORARY, null);
												nodeService.deleteNode(activityItemNodeRef);
												nbrActivity--;
												continue;
											}
											// Arrange activities by type
											else {
												if (!activitiesByType.containsKey(activityType)) {
													activitiesByType.put(activityType, new ArrayList<>());
												}
												hasFormulation = activityType.equals(ActivityType.Formulation) ? true
														: hasFormulation;
												hasReport = activityType.equals(ActivityType.Report) ? true : hasReport;
												activitiesByType.get(activityType).add(activityItemNodeRef);
												users.add(activity.getUserId());
											}
										}

										List<NodeRef> dlActivities = activitiesByType.get(ActivityType.Datalist);
										if (dlActivities != null) {
											// Group list by day/week/month/year
											int[] groupTime = { Calendar.DAY_OF_YEAR, Calendar.WEEK_OF_YEAR, Calendar.MONTH,
													Calendar.YEAR };
											for (int i = 0; (i < groupTime.length) && (nbrActivity > MAX_PAGE); i++) {
												dlActivities = group(entityNodeRef, dlActivities, users, groupTime[i],
														cronDate);
												nbrActivity += (dlActivities.size()
														- activitiesByType.get(ActivityType.Datalist).size());
											}
										}
									}
								}
							}

						} catch (Exception e) {
							 if (RetryingTransactionHelper.extractRetryCause(e) != null) {
								 throw e;
			                  }
							logger.error(e,e);
						} finally {
							logger.debug("Purge terminated with sucess: ");
							policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
						}



						return null;
					};
					return transactionService.getRetryingTransactionHelper().doInTransaction(actionCallback, false, true);
				};
				AuthenticationUtil.runAsSystem(actionRunAs);

			}

		}, false, true);

		if (logger.isDebugEnabled() && watch!=null) {
			watch.stop();
			logger.debug("Clean activities batchs takes " + watch.getTotalTimeSeconds() + " seconds");
		}
	}

	// Group activities
	private List<NodeRef> group(NodeRef entityNodeRef, List<NodeRef> activitiesNodeRefs, Set<String> users,
			int timePeriod, Date cronDate) {
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
		}

		// Repeat till the oldest activity
		while (maxLimit.getTime().after(cronDate)) {

			for (String userId : users) {

				if (activitiesNodeRefs != null) {
					Map<NodeRef, List<String>> activitiesByEntity = new HashMap<>();
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
							datalistClassName = (String) data.get(PROP_CLASSNAME);
						} catch (JSONException e) {
							logger.error("Problem occurred while parsing data activity!! ", e);
						}

						if (createdDate.after(minLimit.getTime()) && createdDate.before(maxLimit.getTime())
								&& activity.getUserId().equals(userId)) {
							// group same data-list activity
							if (activitiesByEntity.containsKey(activityParentNodeRef)
									&& activitiesByEntity.get(activityParentNodeRef).contains(datalistClassName)) {
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
			}

			// Move to previous period
			maxLimit.add(timePeriod, -1);
			minLimit.add(timePeriod, -1);
		}

		return activitiesNodeRefs;
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

	private List<NodeRef> getEntitiesToPurge(){
		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().ofType(BeCPGModel.TYPE_ENTITY_V2)
					.excludeVersions()
					.inDB()
					.ftsLanguage()
					.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED);
			return queryBuilder.list();
		}, false, true);
	}

}
