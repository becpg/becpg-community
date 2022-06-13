package fr.becpg.repo.publication.impl;

import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.PublicationModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.catalog.EntityCatalogObserver;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.publication.PublicationChannelService;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.search.SearchRuleService;
import fr.becpg.repo.search.data.SearchRuleFilter;
import fr.becpg.repo.search.data.SearchRuleResult;

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
	
	
	@Override
	public void notifyAuditedFieldChange(String catalogId, NodeRef entityNodeRef) {
		NodeRef listContainer = entityListDAO.getListContainer(entityNodeRef);
		if (listContainer != null) {
			NodeRef listNodeRef = entityListDAO.getList(listContainer, PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST);
			if (listNodeRef != null) {
				for (NodeRef channelListItemNodeRef : entityListDAO.getListItems(listNodeRef, PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST)) {
					NodeRef channelNodeRef = associationService.getTargetAssoc(channelListItemNodeRef, PublicationModel.ASSOC_PUBCHANNELLIST_CHANNEL);
					if (catalogId.equals(nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_CATALOG_ID))) {
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

		String jsonConfig = (String) nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_CONFIG);
		
		SearchRuleFilter searchRuleFilter = new SearchRuleFilter(namespaceService);
		searchRuleFilter.fromJsonString(jsonConfig);
		
		

		SearchRuleResult ret = searchRuleService.search(searchRuleFilter);
		
		
		return ret.getResults();
	}

}
