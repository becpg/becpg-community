/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.publication.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.model.PublicationModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;

/**
 * Unit tests of {@link PublicationChannelServiceImpl#beforeDeleteNode(NodeRef)}: removing a channel list item
 * keeps the channel ids of its entity in sync, and is skipped when the channel or the entity no longer exists.
 */
public class PublicationChannelServiceImplTest {

	private static final String CHANNEL_ID = "channel-a";

	private static final String OTHER_CHANNEL_ID = "channel-b";

	private final NodeRef channelListItemNodeRef = new NodeRef("workspace://SpacesStore/channel-list-item");

	private final NodeRef entityNodeRef = new NodeRef("workspace://SpacesStore/entity");

	private final NodeRef channelNodeRef = new NodeRef("workspace://SpacesStore/channel");

	private NodeService nodeService;

	private AssociationService associationService;

	private BehaviourFilter policyBehaviourFilter;

	private PublicationChannelServiceImpl service;

	@Before
	public void setUp() {
		nodeService = mock(NodeService.class);
		associationService = mock(AssociationService.class);
		policyBehaviourFilter = mock(BehaviourFilter.class);
		EntityListDAO entityListDAO = mock(EntityListDAO.class);
		when(entityListDAO.getEntity(channelListItemNodeRef)).thenReturn(entityNodeRef);

		service = new PublicationChannelServiceImpl();
		service.setNodeService(nodeService);
		service.setAssociationService(associationService);
		service.setEntityListDAO(entityListDAO);
		service.setPolicyBehaviourFilter(policyBehaviourFilter);
	}

	@Test
	public void testRemovesChannelIdFromEntityChannelIds() {
		givenChannel();
		when(nodeService.getProperty(entityNodeRef, PublicationModel.PROP_CHANNELIDS)).thenReturn(channelIds());

		service.beforeDeleteNode(channelListItemNodeRef);

		verify(nodeService).setProperty(entityNodeRef, PublicationModel.PROP_CHANNELIDS, (Serializable) List.of(OTHER_CHANNEL_ID));
	}

	@Test
	public void testSkipsEntityUpdateWhenChannelIsMissing() {
		when(associationService.getTargetAssoc(channelListItemNodeRef, PublicationModel.ASSOC_PUBCHANNELLIST_CHANNEL)).thenReturn(null);

		service.beforeDeleteNode(channelListItemNodeRef);

		verify(nodeService, never()).setProperty(eq(entityNodeRef), any(), any());
		verifyBehavioursReEnabled();
	}

	@Test
	public void testSkipsEntityUpdateWhenEntityIsBeingDeleted() {
		givenChannel();
		when(nodeService.hasAspect(entityNodeRef, ContentModel.ASPECT_PENDING_DELETE)).thenReturn(true);

		service.beforeDeleteNode(channelListItemNodeRef);

		verify(nodeService, never()).setProperty(eq(entityNodeRef), any(), any());
		verifyBehavioursReEnabled();
	}

	private void givenChannel() {
		when(associationService.getTargetAssoc(channelListItemNodeRef, PublicationModel.ASSOC_PUBCHANNELLIST_CHANNEL)).thenReturn(channelNodeRef);
		when(nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_ID)).thenReturn(CHANNEL_ID);
	}

	private ArrayList<String> channelIds() {
		ArrayList<String> channelIds = new ArrayList<>();
		channelIds.add(CHANNEL_ID);
		channelIds.add(OTHER_CHANNEL_ID);
		return channelIds;
	}

	private void verifyBehavioursReEnabled() {
		verify(policyBehaviourFilter).enableBehaviour(PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST);
		verify(policyBehaviourFilter).enableBehaviour(ContentModel.ASPECT_AUDITABLE);
	}
}
