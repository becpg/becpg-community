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

import org.alfresco.model.ContentModel;
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
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.catalog.EntityCatalogService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.publication.PublicationChannelService;
import fr.becpg.repo.publication.PublicationChannelService.PublicationChannelAction;
import fr.becpg.test.PLMBaseTestCase;

/**
 * The Class PublicationServiceIT.
 *
 * @author matthieu
 */
public class PublicationServiceIT extends PLMBaseTestCase {

	private static final String CHANNEL_ID = "test-channel";

	@Autowired
	private PublicationChannelService publicationChannelService;

	@Autowired
	private BeCPGCacheService cacheService;

	@Autowired
	private EntityListDAO entityListDAO;

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

		final NodeRef pfNodeRef = inWriteTx(() -> {

			FinishedProductData productData = new FinishedProductData();
			productData.setParentNodeRef(getTestFolderNodeRef());
			productData.setName("finished-product 1");
			productData.setErpCode(CHANNEL_ID + "01");
			alfrescoRepository.save(productData);
			return productData.getNodeRef();

		});
		
		final NodeRef pf2NodeRef = inWriteTx(() -> {

			FinishedProductData productData = new FinishedProductData();
			productData.setParentNodeRef(getTestFolderNodeRef());
			productData.setName("finished-product 2");
			productData.setErpCode(CHANNEL_ID + "02");
			alfrescoRepository.save(productData);
			return productData.getNodeRef();

		});


		//Test query search
		inReadTx(() -> {
			Assert.assertEquals(channelNodeRef, publicationChannelService.getChannelById(CHANNEL_ID));
			Assert.assertFalse(publicationChannelService.getEntitiesByChannel(channelNodeRef).isEmpty());
			Assert.assertEquals(pfNodeRef, publicationChannelService.getEntitiesByChannel(channelNodeRef).get(0));
			return true;
		});

		//New date
		inWriteTx(() -> {
			nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_LASTDATE, new Date());
			return true;
		});

		//No results
		inReadTx(() -> {
			Assert.assertTrue(publicationChannelService.getEntitiesByChannel(channelNodeRef).isEmpty());
			return true;
		});

		//Test query members
		inWriteTx(() -> {
			nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_LASTDATE, null);
			nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_CONFIG, "");
			return true;
		});

		final NodeRef channelListItemNodeRef = inWriteTx(() -> {

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

		//Query with no date
		inReadTx(() -> {
			Assert.assertTrue(!publicationChannelService.getEntitiesByChannel(channelNodeRef).isEmpty());
			Assert.assertEquals(pfNodeRef, publicationChannelService.getEntitiesByChannel(channelNodeRef).get(0));
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
			Assert.assertTrue(!publicationChannelService.getEntitiesByChannel(channelNodeRef).isEmpty());
			Assert.assertEquals(pfNodeRef, publicationChannelService.getEntitiesByChannel(channelNodeRef).get(0));
			return true;
		});

		//New date
		inWriteTx(() -> {
			nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_LASTDATE, new Date());
			return true;
		});

		//No results
		inReadTx(() -> {
			Assert.assertTrue(publicationChannelService.getEntitiesByChannel(channelNodeRef).isEmpty());
			return true;
		});

		//Force
		inWriteTx(() -> {
			nodeService.setProperty(channelListItemNodeRef, PublicationModel.PROP_PUBCHANNELLIST_ACTION, PublicationChannelAction.RETRY);
			return true;
		});

		//One result
		inReadTx(() -> {
			Assert.assertTrue(!publicationChannelService.getEntitiesByChannel(channelNodeRef).isEmpty());
			Assert.assertEquals(pfNodeRef, publicationChannelService.getEntitiesByChannel(channelNodeRef).get(0));
			return true;
		});

		//Test catalog update
		inWriteTx(() -> {
			nodeService.setProperty(pfNodeRef, ContentModel.PROP_TITLE, "Update 2");
			nodeService.setProperty(channelListItemNodeRef, PublicationModel.PROP_PUBCHANNELLIST_ACTION, null);
			return pfNodeRef;

		});

		inReadTx(() -> {
			Assert.assertTrue(!publicationChannelService.getEntitiesByChannel(channelNodeRef).isEmpty());
			Assert.assertEquals(pfNodeRef, publicationChannelService.getEntitiesByChannel(channelNodeRef).get(0));
			return true;
		});

		//Test Stop
		inWriteTx(() -> {
			nodeService.setProperty(pfNodeRef, ContentModel.PROP_TITLE, "Update 3");
			nodeService.setProperty(channelListItemNodeRef, PublicationModel.PROP_PUBCHANNELLIST_ACTION, PublicationChannelAction.STOP);
			return pfNodeRef;

		});

		inReadTx(() -> {
			Assert.assertTrue(publicationChannelService.getEntitiesByChannel(channelNodeRef).isEmpty());
			return true;
		});

		//Test is filter
		inWriteTx(() -> {

			JSONObject channelConfig = new JSONObject();
			channelConfig.put("query", "=@bcpg\\:erpCode:\"" + CHANNEL_ID + "01\"");
			channelConfig.put("isFilter", true);

			nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_LASTDATE, null);
			nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_CONFIG, channelConfig.toString());
			nodeService.setProperty(channelListItemNodeRef, PublicationModel.PROP_PUBCHANNELLIST_ACTION,null);
			return true;
		});
		
		inReadTx(() -> {
			Assert.assertTrue(!publicationChannelService.getEntitiesByChannel(channelNodeRef).isEmpty());
			Assert.assertEquals(pfNodeRef, publicationChannelService.getEntitiesByChannel(channelNodeRef).get(0));
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
			Assert.assertTrue(!publicationChannelService.getEntitiesByChannel(channelNodeRef).isEmpty());
			Assert.assertEquals(pf2NodeRef, publicationChannelService.getEntitiesByChannel(channelNodeRef).get(0));
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
			Assert.assertTrue(publicationChannelService.getEntitiesByChannel(channelNodeRef).isEmpty());
			return true;
		});
		
		
		// test audit
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
		
		Date beforeChannelModifiedDate = (Date) inReadTx(() -> {
			return nodeService.getProperty(channelListItemNodeRef, PublicationModel.PROP_PUBCHANNELLIST_MODIFIED_DATE);
		});
		
		inWriteTx(() -> {
			nodeService.removeProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_CATALOG_ID);
			nodeService.setProperty(channelListItemNodeRef, PublicationModel.PROP_PUBCHANNELLIST_ACTION, PublicationChannelAction.RETRY);
			return true;
		});
		
		currentModifiedDate = (Date) inReadTx(() -> {
			return nodeService.getProperty(pfNodeRef, ContentModel.PROP_MODIFIED);
		});
		
		assertTrue(currentModifiedDate.compareTo(beforeModifiedDate) > 0);
		
		Date currentChannelModifiedDate = (Date) inReadTx(() -> {
			return nodeService.getProperty(channelListItemNodeRef, PublicationModel.PROP_PUBCHANNELLIST_MODIFIED_DATE);
		});
		
		assertEquals(beforeChannelModifiedDate, currentChannelModifiedDate);

	}

}
