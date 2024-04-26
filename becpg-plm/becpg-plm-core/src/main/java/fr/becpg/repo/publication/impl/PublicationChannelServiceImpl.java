package fr.becpg.repo.publication.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.query.EmptyPagingResults;
import org.alfresco.query.ListBackedPagingResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.DataListModel;
import fr.becpg.model.PublicationModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.catalog.EntityCatalogObserver;
import fr.becpg.repo.entity.datalist.policy.AuditEntityListItemPolicy;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.impl.AssociationCriteriaFilter;
import fr.becpg.repo.helper.impl.AssociationCriteriaFilter.AssociationCriteriaFilterMode;
import fr.becpg.repo.helper.impl.EntitySourceAssoc;
import fr.becpg.repo.publication.PublicationChannelService;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.search.SearchRuleService;
import fr.becpg.repo.search.data.DateFilterDelayUnit;
import fr.becpg.repo.search.data.DateFilterType;
import fr.becpg.repo.search.data.SearchRuleFilter;
import fr.becpg.repo.search.data.SearchRuleResult;

/**
 *
 * @author matthieu
 *
 */
@Service("publicationChannelService")
public class PublicationChannelServiceImpl implements PublicationChannelService, EntityCatalogObserver, InitializingBean {

	@Autowired
	private EntityListDAO entityListDAO;
	@Autowired
	private AssociationService associationService;
	@Autowired
	private NodeService nodeService;
	@Autowired
	private NamespaceService namespaceService;
	@Autowired
	private SearchRuleService searchRuleService;
	
	@Autowired
	private BehaviourFilter policyBehaviourFilter;

	@Override
	public void afterPropertiesSet() throws Exception {
		AuditEntityListItemPolicy.registerIgnoredType(PublicationModel.PROP_PUBCHANNEL_BATCHSTARTTIME);
		AuditEntityListItemPolicy.registerIgnoredType(PublicationModel.PROP_PUBCHANNEL_BATCHENDTIME);
		AuditEntityListItemPolicy.registerIgnoredType(PublicationModel.PROP_PUBCHANNEL_BATCHDURATION);
		AuditEntityListItemPolicy.registerIgnoredType(PublicationModel.PROP_PUBCHANNEL_BATCHID);
		AuditEntityListItemPolicy.registerIgnoredType(PublicationModel.PROP_PUBCHANNEL_FAILCOUNT);
		AuditEntityListItemPolicy.registerIgnoredType(PublicationModel.PROP_PUBCHANNEL_READCOUNT);
		AuditEntityListItemPolicy.registerIgnoredType(PublicationModel.PROP_PUBCHANNEL_ERROR);
		AuditEntityListItemPolicy.registerIgnoredType(PublicationModel.PROP_PUBCHANNEL_LASTSUCCESSBATCHID);
		AuditEntityListItemPolicy.registerIgnoredType(PublicationModel.PROP_PUBCHANNEL_STATUS);
		AuditEntityListItemPolicy.registerIgnoredType(PublicationModel.PROP_PUBCHANNELLIST_PUBLISHEDDATE);
		AuditEntityListItemPolicy.registerIgnoredType(PublicationModel.PROP_PUBCHANNELLIST_BATCHID);
		AuditEntityListItemPolicy.registerIgnoredType(PublicationModel.PROP_PUBCHANNELLIST_STATUS);
		AuditEntityListItemPolicy.registerIgnoredType(PublicationModel.PROP_PUBCHANNELLIST_ERROR);
	}
	
	@Override
	public void notifyAuditedFieldChange(String catalogId, NodeRef entityNodeRef) {
		NodeRef listContainer = entityListDAO.getListContainer(entityNodeRef);
		if (listContainer != null) {
			NodeRef listNodeRef = entityListDAO.getList(listContainer, PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST);
			if (listNodeRef != null) {
				try {
					policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
					for (NodeRef channelListItemNodeRef : entityListDAO.getListItems(listNodeRef, PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST)) {
						NodeRef channelNodeRef = associationService.getTargetAssoc(channelListItemNodeRef, PublicationModel.ASSOC_PUBCHANNELLIST_CHANNEL);
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

	@Override
	public boolean acceptCatalogEvents(QName type, NodeRef entityNodeRef, Set<NodeRef> listNodeRefs) {
		
		if (listNodeRefs != null && listNodeRefs.stream().allMatch(n -> PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST.equals(QName.createQName((String) nodeService.getProperty(n, DataListModel.PROP_DATALISTITEMTYPE),
				namespaceService)))) {
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

	@Override
	public NodeRef getChannelById(String channelId) {
		return BeCPGQueryBuilder.createQuery().ofExactType(PublicationModel.TYPE_PUBLICATION_CHANNEL)
				.andPropEquals(PublicationModel.PROP_PUBCHANNEL_ID, channelId).ftsLanguage().inDB().singleValue();
	}

	@Override
	public PagingResults<NodeRef> getEntitiesByChannel(NodeRef channelNodeRef, PagingRequest pagingRequest) {

		String action = (String) nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_ACTION);
		Date lastDate = (Date) nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_LASTDATE);

		if (PublicationChannelAction.STOP.toString().equals(action)) {
			return new EmptyPagingResults<>();
		}

		if (PublicationChannelAction.RESET.toString().equals(action)) {
			lastDate = null;
		}

		if (PublicationChannelAction.RETRY.toString().equals(action)) {

			List<EntitySourceAssoc> sourceAssocs = associationService.getEntitySourceAssocs(Arrays.asList(channelNodeRef),
					PublicationModel.ASSOC_PUBCHANNELLIST_CHANNEL, PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST, false,
					Arrays.asList(new AssociationCriteriaFilter(PublicationModel.PROP_PUBCHANNELLIST_STATUS,
							PublicationChannelStatus.FAILED.toString())));

			return asPagingResults(sourceAssocs.stream().map(EntitySourceAssoc::getEntityNodeRef).toList(),pagingRequest);

		}

		String jsonConfig = (String) nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_CONFIG);

		SearchRuleFilter searchRuleFilter = new SearchRuleFilter();
		searchRuleFilter.setCurrentDate(lastDate);
		searchRuleFilter.setEnsureDbQuery(true);
		searchRuleFilter.setDateFilterType(DateFilterType.After);
		searchRuleFilter.setDateFilterDelayUnit(DateFilterDelayUnit.MINUTE);
		searchRuleFilter.fromJsonString(jsonConfig, namespaceService);

		Set<NodeRef> ret = new HashSet<>();
		Set<NodeRef> results = new HashSet<>();

		//Two modes:
		//  - query is specified in JSON, we append forced entity
		//  - or we get all members of channel filtered by date

		//Add all PROP_PUBCHANNEL_ACTION = RETRY

		if (!searchRuleFilter.isEmptyJsonQuery() || lastDate != null) {
			ret.addAll(associationService
					.getEntitySourceAssocs(Arrays.asList(channelNodeRef), PublicationModel.ASSOC_PUBCHANNELLIST_CHANNEL,
							PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST, false,
							Arrays.asList(new AssociationCriteriaFilter(PublicationModel.PROP_PUBCHANNELLIST_ACTION,
									PublicationChannelAction.RETRY.toString())))
					.stream().map(EntitySourceAssoc::getEntityNodeRef).toList());
		}

		if (!searchRuleFilter.isEmptyJsonQuery()) {
			SearchRuleResult result = searchRuleService.search(searchRuleFilter);
			results.addAll(result.getResults());
		}

		if (searchRuleFilter.isEmptyJsonQuery() || searchRuleFilter.isFilter()) {
			if (lastDate != null) {

				ret.addAll(associationService
						.getEntitySourceAssocs(Arrays.asList(channelNodeRef), PublicationModel.ASSOC_PUBCHANNELLIST_CHANNEL,
								PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST, false,
								Arrays.asList(new AssociationCriteriaFilter(PublicationModel.PROP_PUBCHANNELLIST_MODIFIED_DATE,
										ISO8601DateFormat.format(lastDate), AssociationCriteriaFilterMode.RANGE),
									new AssociationCriteriaFilter(PublicationModel.PROP_PUBCHANNELLIST_ACTION,
											PublicationChannelAction.STOP.toString(), AssociationCriteriaFilterMode.NOT_EQUALS)
								))
						.stream().map(EntitySourceAssoc::getEntityNodeRef).toList());

			} else {
				ret.addAll(associationService
						.getEntitySourceAssocs(Arrays.asList(channelNodeRef), PublicationModel.ASSOC_PUBCHANNELLIST_CHANNEL,
								PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST, false, Arrays.asList(new AssociationCriteriaFilter(PublicationModel.PROP_PUBCHANNELLIST_ACTION,
										PublicationChannelAction.STOP.toString(), AssociationCriteriaFilterMode.NOT_EQUALS)))
						.stream().map(EntitySourceAssoc::getEntityNodeRef).toList());
			}

			if (searchRuleFilter.isFilter()) {
				ret.retainAll(results);
			}

		} else {
			ret.addAll(results);
		}

		return asPagingResults(new ArrayList<>(ret),pagingRequest);
	}

	private PagingResults<NodeRef> asPagingResults(List<NodeRef> ret, PagingRequest pagingRequest) {
		if(pagingRequest.getMaxItems() == RepoConsts.MAX_RESULTS_UNLIMITED) {
			return new ListBackedPagingResults<>(ret);
		}
		
		return new ListBackedPagingResults<>(ret,pagingRequest);
	}

}
