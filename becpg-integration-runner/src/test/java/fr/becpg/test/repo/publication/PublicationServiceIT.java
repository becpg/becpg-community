/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.publication;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import fr.becpg.model.PublicationModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.activity.data.ActivityListDataItem;
import fr.becpg.repo.activity.data.ActivityType;
import fr.becpg.repo.activity.helper.AuditActivityHelper;
import fr.becpg.repo.audit.model.AuditQuery;
import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.plugin.impl.ActivityAuditPlugin;
import fr.becpg.repo.audit.service.BeCPGAuditService;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.catalog.EntityCatalogService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.publication.PublicationChannelService;
import fr.becpg.repo.publication.PublicationChannelService.PublicationChannelAction;
import fr.becpg.repo.publication.PublicationChannelService.PublicationChannelStatus;
import fr.becpg.repo.system.SystemConfigurationService;
import fr.becpg.test.PLMBaseTestCase;

/**
 * The Class PublicationServiceIT.
 *
 * @author matthieu
 */
public class PublicationServiceIT extends PLMBaseTestCase {

	private static final String CHANNEL_ID = "test-channel";
	private static final String CHANNEL_ID1 = "test-channel-1";
	private static final String CHANNEL_ID2 = "test-channel-2";
	private static final String CHANNEL_ID3 = "test-channel-3";

	@Autowired
	private PublicationChannelService publicationChannelService;

	@Autowired
	private BeCPGCacheService cacheService;

	@Autowired
	private EntityListDAO entityListDAO;
	
	@Autowired
	private BeCPGAuditService beCPGAuditService;
	
	@Autowired
	private SystemConfigurationService systemConfigurationService;

	@Override
	public void setUp() throws Exception {
		super.setUp();

		cacheService.clearCache(EntityCatalogService.class.getName());
		ClassPathResource resource = new ClassPathResource("beCPG/test/audited_channel.json");
		try (InputStream in = resource.getInputStream()) {
			List<JSONArray> res = new ArrayList<>();
			res.add(new JSONArray(IOUtils.toString(in, "UTF-8")));
			cacheService.storeInCache(EntityCatalogService.class.getName(), EntityCatalogService.CATALOG_DEFS, res);
		}
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		cacheService.clearCache(EntityCatalogService.class.getName());
	}

	@Test
	public void testChannelPolicy() throws FileNotFoundException {

		final NodeRef channel1NodeRef = createPublicationChannelNode(CHANNEL_ID1);
		final NodeRef channel2NodeRef = createPublicationChannelNode(CHANNEL_ID2);
		final NodeRef pfNodeRef = createFinishedProductNode("finished-product 1", CHANNEL_ID1 + "01");

		final NodeRef channelListItemNodeRef1 = createChannelListItemNode(channel1NodeRef, pfNodeRef);
		final NodeRef channelListItemNodeRef2 = createChannelListItemNode(channel2NodeRef, pfNodeRef);

		inReadTx(() -> {

			List<String> failedChannelIds = getPropertyOrDefault(pfNodeRef, PublicationModel.PROP_FAILED_CHANNELIDS);
			List<String> publishedChannelIds = getPropertyOrDefault(pfNodeRef, PublicationModel.PROP_PUBLISHED_CHANNELIDS);
			List<String> channelIds = getPropertyOrDefault(pfNodeRef, PublicationModel.PROP_CHANNELIDS);

			Assert.assertTrue(failedChannelIds.isEmpty());
			Assert.assertTrue(publishedChannelIds.isEmpty());
			Assert.assertEquals(2L,channelIds.size());
			Assert.assertTrue(channelIds.contains(CHANNEL_ID1));
			Assert.assertTrue(channelIds.contains(CHANNEL_ID2));

			Assert.assertTrue(!getEntities(channel1NodeRef).isEmpty());
			Assert.assertEquals(pfNodeRef, getEntities(channel1NodeRef).get(0));
			Assert.assertTrue(!getEntities(channel2NodeRef).isEmpty());
			Assert.assertEquals(pfNodeRef, getEntities(channel2NodeRef).get(0));

			return true;
		});

		//Publish Channel 1
		inWriteTx(() -> {
			nodeService.setProperty(pfNodeRef, ContentModel.PROP_TITLE, "Update");
			return pfNodeRef;

		});
		
		publishSuccess(channel1NodeRef, channelListItemNodeRef1);
		
	

		inReadTx(() -> {

			List<String> failedChannelIds = getPropertyOrDefault(pfNodeRef, PublicationModel.PROP_FAILED_CHANNELIDS);
			List<String> publishedChannelIds = getPropertyOrDefault(pfNodeRef, PublicationModel.PROP_PUBLISHED_CHANNELIDS);
			List<String> channelIds = getPropertyOrDefault(pfNodeRef, PublicationModel.PROP_CHANNELIDS);

			Assert.assertTrue(failedChannelIds.isEmpty());
			Assert.assertEquals(1L,publishedChannelIds.size());
			Assert.assertTrue(publishedChannelIds.contains(CHANNEL_ID1));
			Assert.assertEquals(2L, channelIds.size());
			Assert.assertTrue(channelIds.contains(CHANNEL_ID1));
			Assert.assertTrue(channelIds.contains(CHANNEL_ID2));

			Assert.assertTrue(getEntities(channel1NodeRef).isEmpty());
			Assert.assertTrue(!getEntities(channel2NodeRef).isEmpty());
			Assert.assertEquals(pfNodeRef, getEntities(channel2NodeRef).get(0));

			return true;
		});

		//Failed Channel 2
		inWriteTx(() -> {
			nodeService.setProperty(channelListItemNodeRef2, PublicationModel.PROP_PUBCHANNELLIST_STATUS, PublicationChannelStatus.FAILED.toString());
			return pfNodeRef;

		});

		inReadTx(() -> {

			List<String> failedChannelIds = getPropertyOrDefault(pfNodeRef, PublicationModel.PROP_FAILED_CHANNELIDS);
			List<String> publishedChannelIds = getPropertyOrDefault(pfNodeRef, PublicationModel.PROP_PUBLISHED_CHANNELIDS);
			List<String> channelIds = getPropertyOrDefault(pfNodeRef, PublicationModel.PROP_CHANNELIDS);

			Assert.assertEquals(1L,failedChannelIds.size());
			Assert.assertTrue(failedChannelIds.contains(CHANNEL_ID2));

			Assert.assertEquals( 1L, publishedChannelIds.size());
			Assert.assertTrue(publishedChannelIds.contains(CHANNEL_ID1));
			Assert.assertEquals(2L, channelIds.size() );
			Assert.assertTrue(channelIds.contains(CHANNEL_ID1));
			Assert.assertTrue(channelIds.contains(CHANNEL_ID2));

			Assert.assertTrue(getEntities(channel1NodeRef).isEmpty());
			Assert.assertTrue(getEntities(channel2NodeRef).isEmpty());

			return true;
		});

		//Retry channel 2
		inWriteTx(() -> {
			nodeService.setProperty(pfNodeRef, ContentModel.PROP_TITLE, "Update 2");
			nodeService.setProperty(channel2NodeRef, PublicationModel.PROP_PUBCHANNEL_ACTION, PublicationChannelAction.RETRY.toString());
			return pfNodeRef;

		});

		inReadTx(() -> {

			List<String> failedChannelIds = getPropertyOrDefault(pfNodeRef, PublicationModel.PROP_FAILED_CHANNELIDS);
			List<String> publishedChannelIds = getPropertyOrDefault(pfNodeRef, PublicationModel.PROP_PUBLISHED_CHANNELIDS);
			List<String> channelIds = getPropertyOrDefault(pfNodeRef, PublicationModel.PROP_CHANNELIDS);

			Assert.assertEquals(1L, failedChannelIds.size());
			Assert.assertTrue(failedChannelIds.contains(CHANNEL_ID2));

			Assert.assertTrue(publishedChannelIds.isEmpty());
			Assert.assertEquals(2L,channelIds.size());
			Assert.assertTrue(channelIds.contains(CHANNEL_ID1));
			Assert.assertTrue(channelIds.contains(CHANNEL_ID2));

			Assert.assertTrue(!getEntities(channel1NodeRef).isEmpty());
			Assert.assertTrue(!getEntities(channel2NodeRef).isEmpty());
			Assert.assertEquals(pfNodeRef, getEntities(channel2NodeRef).get(0));
			Assert.assertEquals(pfNodeRef, getEntities(channel1NodeRef).get(0));

			return true;
		});
		

		publishSuccess(channel1NodeRef, channelListItemNodeRef1);
		publishSuccess(channel2NodeRef, channelListItemNodeRef2);
		

		inReadTx(() -> {

			List<String> failedChannelIds = getPropertyOrDefault(pfNodeRef, PublicationModel.PROP_FAILED_CHANNELIDS);
			List<String> publishedChannelIds = getPropertyOrDefault(pfNodeRef, PublicationModel.PROP_PUBLISHED_CHANNELIDS);

			Assert.assertEquals(0L, failedChannelIds.size());
			Assert.assertEquals(2L,publishedChannelIds.size());
			Assert.assertTrue(publishedChannelIds.contains(CHANNEL_ID1));
			Assert.assertTrue(publishedChannelIds.contains(CHANNEL_ID2));

			Assert.assertTrue(getEntities(channel1NodeRef).isEmpty());
			Assert.assertTrue(getEntities(channel2NodeRef).isEmpty());

			return true;
		});
		
		
		inWriteTx(() -> {
			nodeService.deleteNode(channelListItemNodeRef2);
			return pfNodeRef;

		});
		
		inReadTx(() -> {

			List<String> failedChannelIds = getPropertyOrDefault(pfNodeRef, PublicationModel.PROP_FAILED_CHANNELIDS);
			List<String> publishedChannelIds = getPropertyOrDefault(pfNodeRef, PublicationModel.PROP_PUBLISHED_CHANNELIDS);
			List<String> channelIds = getPropertyOrDefault(pfNodeRef, PublicationModel.PROP_CHANNELIDS);

			Assert.assertTrue(failedChannelIds.isEmpty());
			Assert.assertEquals(1L,publishedChannelIds.size());
			Assert.assertTrue(publishedChannelIds.contains(CHANNEL_ID1));
			Assert.assertEquals(1L, channelIds.size() );
			Assert.assertTrue(channelIds.contains(CHANNEL_ID1));

			Assert.assertTrue(getEntities(channel1NodeRef).isEmpty());
			Assert.assertTrue(getEntities(channel2NodeRef).isEmpty());

			return true;
		});
		
	}

	private void publishSuccess(NodeRef channel1NodeRef, NodeRef channelListItemNodeRef1) {
		inWriteTx(() -> {
			nodeService.setProperty(channel1NodeRef, PublicationModel.PROP_PUBCHANNEL_LASTDATE, new Date());
			nodeService.setProperty(channelListItemNodeRef1, PublicationModel.PROP_PUBCHANNELLIST_PUBLISHEDDATE, new Date());
			nodeService.setProperty(channelListItemNodeRef1, PublicationModel.PROP_PUBCHANNELLIST_STATUS,
					PublicationChannelStatus.COMPLETED.toString());

			return null;

		});
		
	}

	@SuppressWarnings("unchecked")
	private List<String> getPropertyOrDefault(NodeRef entityNodeRef, QName propertyQName) {
		List<String> propertyValue = (List<String>) nodeService.getProperty(entityNodeRef, propertyQName);
		return propertyValue != null ? propertyValue : new ArrayList<>();
	}

	private NodeRef createPublicationChannelNode(String channelId) {
		return inWriteTx(() -> {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, channelId);
			properties.put(PublicationModel.PROP_PUBCHANNEL_ID, channelId);
			properties.put(PublicationModel.PROP_PUBCHANNEL_LASTDATE, new Date());
			return nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, channelId), PublicationModel.TYPE_PUBLICATION_CHANNEL, properties)
					.getChildRef();
		});
	}

	private NodeRef createFinishedProductNode(String name, String erpCode) {
		return inWriteTx(() -> {
			FinishedProductData productData = new FinishedProductData();
			productData.setParentNodeRef(getTestFolderNodeRef());
			productData.setName(name);
			productData.setErpCode(erpCode);
			alfrescoRepository.save(productData);
			return productData.getNodeRef();
		});
	}

	private NodeRef createChannelListItemNode(NodeRef channelNodeRef, NodeRef pfNodeRef) {
		return inWriteTx(() -> {
			NodeRef listContainerNodeRef = entityListDAO.getListContainer(pfNodeRef);
			NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST);
			if (listNodeRef == null) {
				listNodeRef = entityListDAO.createList(listContainerNodeRef, PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST);
			}
			Map<QName, Serializable> properties = new HashMap<>();
			Map<QName, List<NodeRef>> associations = new HashMap<>();
			associations.put(PublicationModel.ASSOC_PUBCHANNELLIST_CHANNEL, Arrays.asList(channelNodeRef));
			return entityListDAO.createListItem(listNodeRef, PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST, properties, associations);
		});
	}

	/*
	 * In channel inDB mode is enforced and date is set to lastChannelDate
	 * 
	 * { query: (@cm\\:created:[%s TO MAX] OR @cm\\:modified:[%s TO MAX]) AND ( TYPE:\"bcpg:product\" OR TYPE:\"bcpg:client\" OR TYPE:\"bcpg:supplier\" ) dateFilter: { dateFilterField: "cm:modified"
	 * dateFilterType: Before, After, From, To, Equals, dateFilterDelay: 1, dateFilterDelayUnit: Min,Hour,Day } versionFilter : { versionFilterType: MAJOR, MINOR, NONE
	 * 
	 * }, "entityFilter": { "entityType": "bcpg:semiFinishedProduct", "criteria": { "assoc_bcpg_plants_added": "nodeRef" } }, "nodeFilter": { nodeType: nodePath: "criteria": {
	 * "assoc_bcpg_clients_added": "nodeRef" } } }
	 */

	@Test
	public void testChannelQuery() throws FileNotFoundException {

		final NodeRef channelNodeRef = inWriteTx(() -> {

			JSONObject channelConfig = new JSONObject();
			channelConfig.put("query", "=@bcpg\\:erpCode:\"" + CHANNEL_ID + "01\" AND (@cm\\:created:[%s TO MAX] OR @cm\\:modified:[%s TO MAX])");

			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, CHANNEL_ID);
			properties.put(PublicationModel.PROP_PUBCHANNEL_ID, CHANNEL_ID);
			properties.put(PublicationModel.PROP_PUBCHANNEL_CONFIG, channelConfig.toString());
			properties.put(PublicationModel.PROP_PUBCHANNEL_LASTDATE, new Date());
			properties.put(PublicationModel.PROP_PUBCHANNEL_CATALOG_ID, "channel-catalod");
			return nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(PublicationModel.PROP_PUBCHANNEL_ID)),
					PublicationModel.TYPE_PUBLICATION_CHANNEL, properties).getChildRef();

		});

		final NodeRef pfNodeRef = createFinishedProductNode("finished-product 1", CHANNEL_ID + "01");
		final NodeRef pf2NodeRef = createFinishedProductNode("finished-product 2", CHANNEL_ID + "02");

		//Test query search
		inReadTx(() -> {
			Assert.assertEquals(channelNodeRef, publicationChannelService.getChannelById(CHANNEL_ID));
			Assert.assertFalse(getEntities(channelNodeRef).isEmpty());
			Assert.assertEquals(pfNodeRef, getEntities(channelNodeRef).get(0));
			return true;
		});

		//New date
		inWriteTx(() -> {
			nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_LASTDATE, new Date());
			return true;
		});

		//No results
		inReadTx(() -> {
			Assert.assertTrue(getEntities(channelNodeRef).isEmpty());
			return true;
		});

		//Test query members
		inWriteTx(() -> {
			nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_LASTDATE, null);
			nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_CONFIG, "");
			return true;
		});

		final NodeRef channelListItemNodeRef = createChannelListItemNode(channelNodeRef, pfNodeRef);

		//Query with no date
		inReadTx(() -> {
			Assert.assertTrue(!getEntities(channelNodeRef).isEmpty());
			Assert.assertEquals(pfNodeRef, getEntities(channelNodeRef).get(0));
			return true;
		});

		//Test catalog update
		inWriteTx(() -> {
			nodeService.setProperty(pfNodeRef, ContentModel.PROP_TITLE, "Update");
			return pfNodeRef;

		});

		//Modified date is updated
		inReadTx(() -> {
			Assert.assertNotNull(nodeService.getProperty(channelListItemNodeRef, PublicationModel.PROP_PUBCHANNELLIST_MODIFIED_DATE));
			return true;
		});

		//Still having a result
		inReadTx(() -> {
			Assert.assertTrue(!getEntities(channelNodeRef).isEmpty());
			Assert.assertEquals(pfNodeRef, getEntities(channelNodeRef).get(0));
			return true;
		});

		//Publish
		publishSuccess(channelNodeRef, channelListItemNodeRef);
		

		//No results
		inReadTx(() -> {
			Assert.assertTrue(getEntities(channelNodeRef).isEmpty());
			return true;
		});

		//Force
		inWriteTx(() -> {
			nodeService.setProperty(channelListItemNodeRef, PublicationModel.PROP_PUBCHANNELLIST_ACTION, PublicationChannelAction.RETRY);
			return true;
		});

		//One result
		inReadTx(() -> {
			Assert.assertTrue(!getEntities(channelNodeRef).isEmpty());
			Assert.assertEquals(pfNodeRef, getEntities(channelNodeRef).get(0));
			return true;
		});

		//Test catalog update
		inWriteTx(() -> {
			nodeService.setProperty(pfNodeRef, ContentModel.PROP_TITLE, "Update 2");
			nodeService.setProperty(channelListItemNodeRef, PublicationModel.PROP_PUBCHANNELLIST_ACTION, null);
			return pfNodeRef;

		});

		inReadTx(() -> {
			Assert.assertTrue(!getEntities(channelNodeRef).isEmpty());
			Assert.assertEquals(pfNodeRef, getEntities(channelNodeRef).get(0));
			return true;
		});

		//Test Stop
		inWriteTx(() -> {
			nodeService.setProperty(pfNodeRef, ContentModel.PROP_TITLE, "Update 3");
			nodeService.setProperty(channelListItemNodeRef, PublicationModel.PROP_PUBCHANNELLIST_ACTION, PublicationChannelAction.STOP);
			return pfNodeRef;

		});

		inReadTx(() -> {
			Assert.assertTrue(getEntities(channelNodeRef).isEmpty());
			return true;
		});

		//Test is filter
		inWriteTx(() -> {

			JSONObject channelConfig = new JSONObject();
			channelConfig.put("query", "=@bcpg\\:erpCode:\"" + CHANNEL_ID + "01\"");
			channelConfig.put("isFilter", true);

			nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_LASTDATE, null);
			nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_CONFIG, channelConfig.toString());
			nodeService.setProperty(channelListItemNodeRef, PublicationModel.PROP_PUBCHANNELLIST_ACTION, null);
			return true;
		});

		inReadTx(() -> {
			Assert.assertTrue(!getEntities(channelNodeRef).isEmpty());
			Assert.assertEquals(pfNodeRef, getEntities(channelNodeRef).get(0));
			return true;
		});

		inWriteTx(() -> {

			JSONObject channelConfig = new JSONObject();
			channelConfig.put("query", "=@bcpg\\:erpCode:\"" + CHANNEL_ID + "02\"");
			channelConfig.put("isFilter", false);

			nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_LASTDATE, null);
			nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_CONFIG, channelConfig.toString());
			return true;
		});

		inReadTx(() -> {
			Assert.assertTrue(!getEntities(channelNodeRef).isEmpty());
			Assert.assertEquals(pf2NodeRef, getEntities(channelNodeRef).get(0));
			return true;
		});

		inWriteTx(() -> {

			JSONObject channelConfig = new JSONObject();
			channelConfig.put("query", "=@bcpg\\:erpCode:\"" + CHANNEL_ID + "02\"");
			channelConfig.put("isFilter", true);

			nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_LASTDATE, null);
			nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_CONFIG, channelConfig.toString());
			return true;
		});

		inReadTx(() -> {
			Assert.assertTrue(getEntities(channelNodeRef).isEmpty());
			return true;
		});

	}
	
	@Test
	public void testAuditAndActivity() {
		
		final NodeRef channelNodeRef = inWriteTx(() -> {

			JSONObject channelConfig = new JSONObject();
			channelConfig.put("query", "=@bcpg\\:erpCode:\"" + CHANNEL_ID3 + "01\" AND (@cm\\:created:[%s TO MAX] OR @cm\\:modified:[%s TO MAX])");

			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, CHANNEL_ID3);
			properties.put(PublicationModel.PROP_PUBCHANNEL_ID, CHANNEL_ID3);
			properties.put(PublicationModel.PROP_PUBCHANNEL_CONFIG, channelConfig.toString());
			properties.put(PublicationModel.PROP_PUBCHANNEL_LASTDATE, new Date());
			return nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(PublicationModel.PROP_PUBCHANNEL_ID)),
					PublicationModel.TYPE_PUBLICATION_CHANNEL, properties).getChildRef();

		});

		final NodeRef pfNodeRef = createFinishedProductNode("finished-product 3", CHANNEL_ID3 + "01");
		
		final NodeRef channelListItemNodeRef = createChannelListItemNode(channelNodeRef, pfNodeRef);
		
		List<ActivityListDataItem> channelActivities = getChannelListActivityNumber(pfNodeRef);
		assertEquals(1, channelActivities.size());
		
		Date beforeModifiedDate = (Date) inReadTx(() -> {
			return nodeService.getProperty(pfNodeRef, ContentModel.PROP_MODIFIED);
		});

		inWriteTx(() -> {
			nodeService.setProperty(channelListItemNodeRef, PublicationModel.PROP_PUBCHANNELLIST_PUBLISHEDDATE, new Date());
			return true;
		});
		
		Date currentModifiedDate = (Date) inReadTx(() -> {
			return nodeService.getProperty(pfNodeRef, ContentModel.PROP_MODIFIED);
		});

		assertEquals(currentModifiedDate, beforeModifiedDate);
		
		channelActivities = getChannelListActivityNumber(pfNodeRef);
		assertEquals(2, channelActivities.size());
		
		Date beforeChannelModifiedDate = (Date) inReadTx(() -> {
			return nodeService.getProperty(channelListItemNodeRef, PublicationModel.PROP_PUBCHANNELLIST_MODIFIED_DATE);
		});

		inWriteTx(() -> {
			nodeService.removeProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_CATALOG_ID);
			nodeService.setProperty(channelListItemNodeRef, PublicationModel.PROP_PUBCHANNELLIST_ACTION, PublicationChannelAction.RETRY);
			return true;
		});
		
		channelActivities = getChannelListActivityNumber(pfNodeRef);
		assertEquals(2, channelActivities.size());
		assertTrue(channelActivities.stream().anyMatch(a -> a.getActivityData().contains(PublicationModel.PROP_PUBCHANNELLIST_ACTION.getLocalName())));
		
		currentModifiedDate = (Date) inReadTx(() -> {
			return nodeService.getProperty(pfNodeRef, ContentModel.PROP_MODIFIED);
		});

		assertTrue(currentModifiedDate.compareTo(beforeModifiedDate) > 0);

		Date currentChannelModifiedDate = (Date) inReadTx(() -> {
			return nodeService.getProperty(channelListItemNodeRef, PublicationModel.PROP_PUBCHANNELLIST_MODIFIED_DATE);
		});

		assertEquals(beforeChannelModifiedDate, currentChannelModifiedDate);
		
		inWriteTx(() -> {
			nodeService.setProperty(channelListItemNodeRef, PublicationModel.PROP_PUBCHANNELLIST_ACTION, PublicationChannelAction.RESET);
			nodeService.setProperty(channelListItemNodeRef, PublicationModel.PROP_PUBCHANNELLIST_BATCHID, "1");
			return true;
		});
		
		channelActivities = getChannelListActivityNumber(pfNodeRef);
		assertEquals(2, channelActivities.size());
		assertTrue(channelActivities.stream().anyMatch(a -> a.getActivityData().contains(PublicationModel.PROP_PUBCHANNELLIST_ACTION.getLocalName())));
		assertFalse(channelActivities.stream().anyMatch(a -> a.getActivityData().contains(PublicationModel.PROP_PUBCHANNELLIST_BATCHID.getLocalName())));
				
		inWriteTx(() -> {
			systemConfigurationService.updateConfValue("beCPG.connector.channel.register.activity", "true");
			nodeService.setProperty(channelListItemNodeRef, PublicationModel.PROP_PUBCHANNELLIST_STATUS, "UNKNOWN");
			nodeService.setProperty(channelListItemNodeRef, PublicationModel.PROP_PUBCHANNELLIST_BATCHID, "2");
			return true;
		});
		
		channelActivities = getChannelListActivityNumber(pfNodeRef);
		assertEquals(2, channelActivities.size());
		assertTrue(channelActivities.stream().anyMatch(a -> a.getActivityData().contains(PublicationModel.PROP_PUBCHANNELLIST_STATUS.getLocalName())));
		assertTrue(channelActivities.stream().anyMatch(a -> a.getActivityData().contains(PublicationModel.PROP_PUBCHANNELLIST_BATCHID.getLocalName())));
		
		inWriteTx(() -> {
			systemConfigurationService.resetConfValue("beCPG.connector.channel.register.activity");
			nodeService.setProperty(channelListItemNodeRef, PublicationModel.PROP_PUBCHANNELLIST_ERROR, "error1");
			nodeService.setProperty(channelListItemNodeRef, PublicationModel.PROP_PUBCHANNELLIST_BATCHID, "3");
			return true;
		});
		
		channelActivities = getChannelListActivityNumber(pfNodeRef);
		assertEquals(2, channelActivities.size());
		assertFalse(channelActivities.stream().allMatch(a -> a.getActivityData().contains(PublicationModel.PROP_PUBCHANNELLIST_ERROR.getLocalName())));

		inWriteTx(() -> {
			JSONObject config = new JSONObject();
			config.put("registerConnectorChannelActivity", true);
			nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_CONFIG, config.toString());
			cacheService.clearAllCaches();
			nodeService.setProperty(channelListItemNodeRef, PublicationModel.PROP_PUBCHANNELLIST_BATCHID, "4");
			nodeService.setProperty(channelListItemNodeRef, PublicationModel.PROP_PUBCHANNELLIST_ERROR, "error2");
			return true;
		});
		
		channelActivities = getChannelListActivityNumber(pfNodeRef);
		assertEquals(2, channelActivities.size());
		assertTrue(channelActivities.stream().anyMatch(a -> a.getActivityData().contains(PublicationModel.PROP_PUBCHANNELLIST_ERROR.getLocalName())));
		
	}
	
	private List<ActivityListDataItem> getChannelListActivityNumber(NodeRef pfNodeRef) {
		return inWriteTx(() -> {
			AuditQuery auditFilter = AuditQuery.createQuery().asc(false).dbAsc(false)
					.sortBy(ActivityAuditPlugin.PROP_CM_CREATED).filter("entityNodeRef", pfNodeRef.toString());
			
			List<ActivityListDataItem> activities = beCPGAuditService.listAuditEntries(AuditType.ACTIVITY, auditFilter).stream().map(json -> AuditActivityHelper.parseActivity(json)).collect(Collectors.toList());
			activities.removeIf(a -> !a.getActivityType().equals(ActivityType.Datalist) || !a.getActivityData().contains("pubChannelList"));
			
			return activities;
		});
	}

	private List<NodeRef> getEntities(NodeRef channelNodeRef) {
		return publicationChannelService.getEntitiesByChannel(channelNodeRef, new PagingRequest(RepoConsts.MAX_RESULTS_UNLIMITED)).getPage();
	}

}
