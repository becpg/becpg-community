package fr.becpg.repo.publication.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.PublicationModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.catalog.EntityCatalogObserver;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.impl.AssociationCriteriaFilter;
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
public class PublicationChannelServiceImpl implements PublicationChannelService, EntityCatalogObserver {

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

	enum PublicationChannelAction {
		RESET, RETRY, STOP
	}

	enum PublicationChannelStatus {
		COMPLETED, STARTING, STARTED, STOPPING, STOPPED, FAILED, ABANDONED, UNKNOWN
	}

	@Override
	public void notifyAuditedFieldChange(String catalogId, NodeRef entityNodeRef) {
		NodeRef listContainer = entityListDAO.getListContainer(entityNodeRef);
		if (listContainer != null) {
			NodeRef listNodeRef = entityListDAO.getList(listContainer, PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST);
			if (listNodeRef != null) {
				for (NodeRef channelListItemNodeRef : entityListDAO.getListItems(listNodeRef, PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST)) {
					NodeRef channelNodeRef = associationService.getTargetAssoc(channelListItemNodeRef, PublicationModel.ASSOC_PUBCHANNELLIST_CHANNEL);
					String channelCatalog = (String) nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_CATALOG_ID);
					if ((catalogId== null && (channelCatalog ==null || channelCatalog.isBlank())) 
							|| (catalogId!=null &&  catalogId.equals(channelCatalog))) {
						nodeService.setProperty(channelListItemNodeRef, PublicationModel.PROP_PUBCHANNELLIST_MODIFIED_DATE, new Date());
					}
				}
			}
		}
	}

	@Override
	public boolean acceptCatalogEvents(QName type, NodeRef entityNodeRef) {

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
	public List<NodeRef> getEntitiesByChannel(NodeRef channelNodeRef) {

		String action = (String) nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_ACTION);
		Date lastDate = (Date) nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_LASTDATE);

		if (PublicationChannelAction.STOP.toString().equals(action)) {
			return new ArrayList<>();
		}

		if (PublicationChannelAction.RESET.toString().equals(action)) {
			lastDate = null;
		}

		if (PublicationChannelAction.RETRY.toString().equals(action)) {

			List<EntitySourceAssoc> sourceAssocs = associationService.getEntitySourceAssocs(Arrays.asList(channelNodeRef),
					PublicationModel.ASSOC_PUBCHANNELLIST_CHANNEL, PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST, false,
					Arrays.asList(new AssociationCriteriaFilter(PublicationModel.PROP_PUBCHANNELLIST_STATUS,
							PublicationChannelStatus.FAILED.toString(), false)));

			return sourceAssocs.stream().map(EntitySourceAssoc::getEntityNodeRef).collect(Collectors.toList());

		}

		String jsonConfig = (String) nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_CONFIG);

		SearchRuleFilter searchRuleFilter = new SearchRuleFilter();
		searchRuleFilter.setCurrentDate(lastDate);
		searchRuleFilter.getEnsureDbQuery();
		searchRuleFilter.setDateFilterType(DateFilterType.After);
		searchRuleFilter.setDateFilterDelayUnit(DateFilterDelayUnit.MINUTE);
		searchRuleFilter.fromJsonString(jsonConfig, namespaceService);

		Set<NodeRef> ret = new HashSet<>();

		//Two modes:
		//  - query is specified in JSON, we append forced entity
		//  - or we get all members of channel filtered by date

		//Add all forced = true
	

		if (!searchRuleFilter.isEmptyJsonQuery()) {
			ret.addAll(associationService
					.getEntitySourceAssocs(Arrays.asList(channelNodeRef), PublicationModel.ASSOC_PUBCHANNELLIST_CHANNEL,
							PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST, true,
							Arrays.asList(new AssociationCriteriaFilter(PublicationModel.PROP_PUBCHANNELLIST_FORCEPUBLICATION, "true", false)))
					.stream().map(EntitySourceAssoc::getEntityNodeRef).collect(Collectors.toList()));

			SearchRuleResult result = searchRuleService.search(searchRuleFilter);

			ret.addAll(result.getResults());

		} else if (lastDate != null) {
			ret.addAll(associationService
					.getEntitySourceAssocs(Arrays.asList(channelNodeRef), PublicationModel.ASSOC_PUBCHANNELLIST_CHANNEL,
							PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST, true,
							Arrays.asList(new AssociationCriteriaFilter(PublicationModel.PROP_PUBCHANNELLIST_FORCEPUBLICATION, "true", false)))
					.stream().map(EntitySourceAssoc::getEntityNodeRef).collect(Collectors.toList()));

			ret.addAll(associationService
					.getEntitySourceAssocs(Arrays.asList(channelNodeRef), PublicationModel.ASSOC_PUBCHANNELLIST_CHANNEL,
							PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST, true,
							Arrays.asList(new AssociationCriteriaFilter(PublicationModel.PROP_PUBCHANNELLIST_MODIFIED_DATE,
									ISO8601DateFormat.format(lastDate), true)))
					.stream().map(EntitySourceAssoc::getEntityNodeRef).collect(Collectors.toList()));

		} else {
			ret.addAll(associationService
					.getEntitySourceAssocs(Arrays.asList(channelNodeRef), PublicationModel.ASSOC_PUBCHANNELLIST_CHANNEL,
							PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST, true,
							Arrays.asList())
					.stream().map(EntitySourceAssoc::getEntityNodeRef).collect(Collectors.toList()));
		}

		return new ArrayList<>(ret);
	}

}
