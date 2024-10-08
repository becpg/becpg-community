package fr.becpg.repo.publication.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.query.EmptyPagingResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.json.JSONObject;

import fr.becpg.model.DataListModel;
import fr.becpg.model.PublicationModel;
import fr.becpg.repo.behaviour.FieldBehaviourRegistry;
import fr.becpg.repo.behaviour.FieldBehaviourRegistry.FieldBehaviour;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.catalog.EntityCatalogObserver;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.publication.PublicationChannelService;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.search.data.SearchRuleFilter;
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * <p>PublicationChannelServiceImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class PublicationChannelServiceImpl extends AbstractBeCPGPolicy implements PublicationChannelService, EntityCatalogObserver,
		NodeServicePolicies.BeforeDeleteNodePolicy, NodeServicePolicies.OnUpdateNodePolicy, NodeServicePolicies.OnCreateNodePolicy {

	private static final String CACHE_KEY = PublicationChannelServiceImpl.class.getName();

	private EntityListDAO entityListDAO;

	private AssociationService associationService;

	private NamespaceService namespaceService;
	
	private BeCPGCacheService beCPGCacheService;
	
	private SystemConfigurationService systemConfigurationService;

	/** {@inheritDoc} */
	@Override
	public void doInit() {
		
		FieldBehaviourRegistry.registerIgnoredAuditFields(PublicationModel.PROP_PUBCHANNEL_BATCHSTARTTIME, PublicationModel.PROP_PUBCHANNEL_BATCHENDTIME,
				PublicationModel.PROP_PUBCHANNEL_BATCHDURATION, PublicationModel.PROP_PUBCHANNEL_BATCHID, PublicationModel.PROP_PUBCHANNEL_FAILCOUNT,
				PublicationModel.PROP_PUBCHANNEL_READCOUNT, PublicationModel.PROP_PUBCHANNEL_ERROR,
				PublicationModel.PROP_PUBCHANNEL_LASTSUCCESSBATCHID, PublicationModel.PROP_PUBCHANNEL_STATUS,
				PublicationModel.PROP_PUBCHANNELLIST_PUBLISHEDDATE, PublicationModel.PROP_PUBCHANNELLIST_BATCHID,
				PublicationModel.PROP_PUBCHANNELLIST_STATUS, PublicationModel.PROP_PUBCHANNELLIST_ERROR);
		
		FieldBehaviourRegistry.registerFieldBehaviour(new FieldBehaviour() {
			@Override
			public boolean shouldIgnoreActivity(NodeRef nodeRef, QName type, QName field, Map<QName, Serializable> before,
					Map<QName, Serializable> after) {
				if (before != null && after != null) {
					if (PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST.equals(type)) {
						if (after.get(PublicationModel.PROP_PUBCHANNELLIST_BATCHID) != null
								&& (before.get(PublicationModel.PROP_PUBCHANNELLIST_BATCHID) == null
								|| !before.get(PublicationModel.PROP_PUBCHANNELLIST_BATCHID)
								.equals(after.get(PublicationModel.PROP_PUBCHANNELLIST_BATCHID)))) {
							NodeRef channelNodeRef = associationService.getTargetAssoc(nodeRef, PublicationModel.ASSOC_PUBCHANNELLIST_CHANNEL);
							Boolean registerChannelListActivity = getRegisterConnectorChannelActivity(channelNodeRef);
							if (registerChannelListActivity != null) {
								return !registerChannelListActivity;
							}
							return !Boolean.TRUE.toString().equals(systemConfigurationService.confValue("beCPG.connector.channel.register.activity"));
						}
					} else if (PublicationModel.TYPE_PUBLICATION_CHANNEL.equals(type) && (after.get(PublicationModel.PROP_PUBCHANNEL_BATCHID) != null
							&& (before.get(PublicationModel.PROP_PUBCHANNEL_BATCHID) == null || !before.get(PublicationModel.PROP_PUBCHANNEL_BATCHID)
							.equals(after.get(PublicationModel.PROP_PUBCHANNEL_BATCHID))))) {
						Boolean registerChannelListActivity = getRegisterConnectorChannelActivity(nodeRef);
						if (registerChannelListActivity != null) {
							return !registerChannelListActivity;
						}
						return !Boolean.TRUE.toString().equals(systemConfigurationService.confValue("beCPG.connector.channel.register.activity"));
					}
				}
				return false;
			}

			private Boolean getRegisterConnectorChannelActivity(NodeRef channelNodeRef) {
				return beCPGCacheService.getFromCache(CACHE_KEY, channelNodeRef.toString(), () -> {
					String config = (String) nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_CONFIG);
					if (config != null && !config.isBlank()) {
						JSONObject configJson = new JSONObject(config);
						if (configJson.has("registerConnectorChannelActivity")) {
							return Boolean.TRUE.toString().equals(configJson.get("registerConnectorChannelActivity").toString());
						}
					}
					return null;
				});
			}
		});
		
		policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST,
				new JavaBehaviour(this, "beforeDeleteNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST,
				new JavaBehaviour(this, "onCreateNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdateNodePolicy.QNAME, PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST,
				new JavaBehaviour(this, "onUpdateNode"));
	}
	
	public void setBeCPGCacheService(BeCPGCacheService beCPGCacheService) {
		this.beCPGCacheService = beCPGCacheService;
	}

	public void setSystemConfigurationService(SystemConfigurationService systemConfigurationService) {
		this.systemConfigurationService = systemConfigurationService;
	}
	
	/**
	 * <p>Setter for the field <code>entityListDAO</code>.</p>
	 *
	 * @param entityListDAO a {@link fr.becpg.repo.entity.EntityListDAO} object
	 */
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/** {@inheritDoc} */
	@Override
	public void notifyAuditedFieldChange(String catalogId, NodeRef entityNodeRef) {
		NodeRef listContainer = entityListDAO.getListContainer(entityNodeRef);
		if (listContainer != null) {
			NodeRef listNodeRef = entityListDAO.getList(listContainer, PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST);
			if (listNodeRef != null) {
				try {
					policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
					for (NodeRef channelListItemNodeRef : entityListDAO.getListItems(listNodeRef, PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST)) {
						NodeRef channelNodeRef = associationService.getTargetAssoc(channelListItemNodeRef,
								PublicationModel.ASSOC_PUBCHANNELLIST_CHANNEL);
						String channelCatalog = (String) nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_CATALOG_ID);
						if ((catalogId == null && (channelCatalog == null || channelCatalog.isBlank()))
								|| (catalogId != null && catalogId.equals(channelCatalog))) {
							nodeService.setProperty(channelListItemNodeRef, PublicationModel.PROP_PUBCHANNELLIST_MODIFIED_DATE, new Date());
						}
					}
				} finally {
					policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
				}
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void onUpdateNode(NodeRef channelListItemNodeRef) {
		queueNode(channelListItemNodeRef);
	}

	/** {@inheritDoc} */
	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {
		queueNode(childAssocRef.getChildRef());
	}

	/** {@inheritDoc} */
	@Override
	protected boolean doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		try {
			policyBehaviourFilter.disableBehaviour(PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST);
			policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
			for (NodeRef channelListItemNodeRef : pendingNodes) {
				updateChannelStates(channelListItemNodeRef);
			}
		} finally {
			policyBehaviourFilter.enableBehaviour(PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST);
			policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
		}
		return true;
	}

	private void updateChannelStates(NodeRef channelListItemNodeRef) {

		if (nodeService.exists(channelListItemNodeRef)) {

			NodeRef entityNodeRef = entityListDAO.getEntity(channelListItemNodeRef);
			NodeRef channelNodeRef = associationService.getTargetAssoc(channelListItemNodeRef, PublicationModel.ASSOC_PUBCHANNELLIST_CHANNEL);
			if (channelNodeRef != null) {
				String channelId = (String) nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_ID);
				String action = (String) nodeService.getProperty(channelListItemNodeRef, PublicationModel.PROP_PUBCHANNELLIST_ACTION);
				String status = (String) nodeService.getProperty(channelListItemNodeRef, PublicationModel.PROP_PUBCHANNELLIST_STATUS);
				Date modifiedDate = (Date) nodeService.getProperty(channelListItemNodeRef, PublicationModel.PROP_PUBCHANNELLIST_MODIFIED_DATE);
				if (modifiedDate == null) {
					modifiedDate = (Date) nodeService.getProperty(entityNodeRef, ContentModel.PROP_MODIFIED);
					if (modifiedDate == null) {
						modifiedDate = (Date) nodeService.getProperty(entityNodeRef, ContentModel.PROP_CREATED);
					}
				}

				Date publishDate = (Date) nodeService.getProperty(channelListItemNodeRef, PublicationModel.PROP_PUBCHANNELLIST_PUBLISHEDDATE);

				List<String> channelIds = getPropertyOrDefault(entityNodeRef, PublicationModel.PROP_CHANNELIDS);

				List<String> failedChannelIds = getPropertyOrDefault(entityNodeRef, PublicationModel.PROP_FAILED_CHANNELIDS);
				List<String> publishedChannelIds = getPropertyOrDefault(entityNodeRef, PublicationModel.PROP_PUBLISHED_CHANNELIDS);

				if (!channelIds.contains(channelId)) {
					channelIds.add(channelId);
				}

				if (PublicationChannelStatus.FAILED.toString().equals(status)) {
					if (!failedChannelIds.contains(channelId)) {
						failedChannelIds.add(channelId);
					}
				} else {
					failedChannelIds.remove(channelId);
				}

				if (!PublicationChannelAction.RETRY.toString().equals(action) && PublicationChannelStatus.COMPLETED.toString().equals(status)
						&& modifiedDate != null && publishDate != null && (publishDate.after(modifiedDate) || publishDate.equals(modifiedDate))) {
					if (!publishedChannelIds.contains(channelId)) {
						publishedChannelIds.add(channelId);
					}
				} else {
					publishedChannelIds.remove(channelId);
				}

				if (PublicationChannelAction.STOP.toString().equals(action)) {
					if (!publishedChannelIds.contains(channelId)) {
						publishedChannelIds.add(channelId);
					}
				}

				nodeService.setProperty(entityNodeRef, PublicationModel.PROP_FAILED_CHANNELIDS, (Serializable) failedChannelIds);
				nodeService.setProperty(entityNodeRef, PublicationModel.PROP_PUBLISHED_CHANNELIDS, (Serializable) publishedChannelIds);
				nodeService.setProperty(entityNodeRef, PublicationModel.PROP_CHANNELIDS, (Serializable) channelIds);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void beforeDeleteNode(NodeRef channelListItemNodeRef) {
		try {
			policyBehaviourFilter.disableBehaviour(PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST);
			policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
			NodeRef entityNodeRef = entityListDAO.getEntity(channelListItemNodeRef);
			NodeRef channelNodeRef = associationService.getTargetAssoc(channelListItemNodeRef, PublicationModel.ASSOC_PUBCHANNELLIST_CHANNEL);
			String channelId = (String) nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_ID);

			List<String> failedChannelIds = getPropertyOrDefault(entityNodeRef, PublicationModel.PROP_FAILED_CHANNELIDS);
			List<String> publishedChannelIds = getPropertyOrDefault(entityNodeRef, PublicationModel.PROP_PUBLISHED_CHANNELIDS);
			List<String> channelIds = getPropertyOrDefault(entityNodeRef, PublicationModel.PROP_CHANNELIDS);

			failedChannelIds.remove(channelId);
			publishedChannelIds.remove(channelId);
			channelIds.remove(channelId);

			nodeService.setProperty(entityNodeRef, PublicationModel.PROP_FAILED_CHANNELIDS, (Serializable) failedChannelIds);
			nodeService.setProperty(entityNodeRef, PublicationModel.PROP_PUBLISHED_CHANNELIDS, (Serializable) publishedChannelIds);
			nodeService.setProperty(entityNodeRef, PublicationModel.PROP_CHANNELIDS, (Serializable) channelIds);
		} finally {
			policyBehaviourFilter.enableBehaviour(PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST);
			policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
		}
	}

	// Helper method to get property or default empty list
	@SuppressWarnings("unchecked")
	private List<String> getPropertyOrDefault(NodeRef entityNodeRef, QName propertyQName) {
		List<String> propertyValue = (List<String>) nodeService.getProperty(entityNodeRef, propertyQName);
		return propertyValue != null ? propertyValue : new ArrayList<>();
	}

	/** {@inheritDoc} */
	@Override
	public boolean acceptCatalogEvents(QName type, NodeRef entityNodeRef, Set<NodeRef> listNodeRefs) {

		if (listNodeRefs != null && listNodeRefs.stream().allMatch(n -> PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST
				.equals(QName.createQName((String) nodeService.getProperty(n, DataListModel.PROP_DATALISTITEMTYPE), namespaceService)))) {
			return false;
		}

		NodeRef listContainer = entityListDAO.getListContainer(entityNodeRef);
		if (listContainer != null) {
			NodeRef listNodeRef = entityListDAO.getList(listContainer, PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST);
			if (listNodeRef != null) {
				return !entityListDAO.isEmpty(listNodeRef, PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST);
			}
		}

		return false;
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getChannelById(String channelId) {
		return BeCPGQueryBuilder.createQuery().ofExactType(PublicationModel.TYPE_PUBLICATION_CHANNEL)
				.andPropEquals(PublicationModel.PROP_PUBCHANNEL_ID, channelId).ftsLanguage().inDB().singleValue();
	}

	/** {@inheritDoc} */
	@Override
	public PagingResults<NodeRef> getEntitiesByChannel(NodeRef channelNodeRef, PagingRequest pagingRequest) {

		String action = (String) nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_ACTION);
		Date lastDate = (Date) nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_LASTDATE);
		String channelId = (String) nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_ID);
		BeCPGQueryBuilder query = BeCPGQueryBuilder.createQuery().excludeDefaults().page(pagingRequest);

		if (PublicationChannelAction.STOP.toString().equals(action)) {
			return new EmptyPagingResults<>();
		}

		if (PublicationChannelAction.RETRY.toString().equals(action)) {
			return query.andPropEquals(PublicationModel.PROP_FAILED_CHANNELIDS, channelId).inDB().ftsLanguage().pagingResults();
		}

		String jsonConfig = (String) nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_CONFIG);

		SearchRuleFilter searchRuleFilter = new SearchRuleFilter();
		searchRuleFilter.fromJsonString(jsonConfig, namespaceService);

		if (PublicationChannelAction.RESET.toString().equals(action)) {
			lastDate = null;
		} else {
			if (searchRuleFilter.excludePublishedEntities()) {
				query.excludeProp(PublicationModel.PROP_PUBLISHED_CHANNELIDS, channelId);
			}
		}

		if (searchRuleFilter.getNodeType() != null) {
			query.ofType(searchRuleFilter.getNodeType());
		}

		if (!searchRuleFilter.getQuery().isEmpty()) {
			String dateQuery = lastDate != null ? ISO8601DateFormat.format(lastDate) : "MIN";
			query.andFTSQuery(String.format(searchRuleFilter.getQuery(), dateQuery, dateQuery, dateQuery));
		}

		query.excludeProp(PublicationModel.PROP_FAILED_CHANNELIDS, channelId);

		if (searchRuleFilter.isEmptyJsonQuery() || searchRuleFilter.isFilter()) {
			query.andPropEquals(PublicationModel.PROP_CHANNELIDS, channelId);
		}

		return query.inDB().ftsLanguage().pagingResults();
	}

}
