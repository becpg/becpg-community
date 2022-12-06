/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.publication;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
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
import fr.becpg.test.PLMBaseTestCase;

/**
 * The Class RemoteEntityServiceTest.
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
	{
		query:
	 	dateFilter: {
	 	 dateFilterField: 
	 	 dateFilterType: Before, After, From, To, Equals,
	 	 dateFilterDelay: 1,
	 	 dateFilterDelayUnit: Min,Hour,Day
	 	}
		versionFilter : {
		  versionFilterType: MAJOR, MINOR, NONE
		
		},
		"entityFilter": {
	        "entityType": "bcpg:semiFinishedProduct",
	        "criteria": {
	            "assoc_bcpg_plants_added": "nodeRef" 
	        }
	    },
	    "nodeFilter": {
	        nodeType:
	        nodePath:
	        "criteria": {
	            "assoc_bcpg_clients_added": "nodeRef" 
	        }
	    }
	}
    */

	@Test
	public void testChannelQuery() throws FileNotFoundException {

		final NodeRef channelNodeRef = inWriteTx(() -> {
			
			JSONObject channelConfig = new JSONObject();
			channelConfig.put("query", "=@bcpg\\:erpCode:\"" + CHANNEL_ID + "01\"");

			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, CHANNEL_ID);
			properties.put(PublicationModel.PROP_PUBCHANNEL_ID, CHANNEL_ID);
			properties.put(PublicationModel.PROP_PUBCHANNEL_CONFIG, channelConfig.toString());
			properties.put(PublicationModel.PROP_PUBCHANNEL_CATALOG_ID, "channel-catalod");
			return nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(PublicationModel.PROP_PUBCHANNEL_ID)),
					PublicationModel.TYPE_PUBLICATION_CHANNEL, properties).getChildRef();

		});

		final NodeRef pfNodeRef = inWriteTx(() -> {

			FinishedProductData productData = new FinishedProductData();
			productData.setParentNodeRef(getTestFolderNodeRef());
			productData.setName("finished-product");
			productData.setErpCode(CHANNEL_ID + "01");
			alfrescoRepository.save(productData);
			return productData.getNodeRef();

		});

		//Test query search
		inReadTx(() -> {
			Assert.assertEquals(channelNodeRef, publicationChannelService.getChannelById(CHANNEL_ID));
			Assert.assertTrue(!publicationChannelService.getEntitiesByChannel(channelNodeRef).isEmpty());
			Assert.assertEquals(pfNodeRef, publicationChannelService.getEntitiesByChannel(channelNodeRef).get(0));
			return true;
		});

		//Test catalog update
		final NodeRef channelListItemNodeRef = inWriteTx(() -> {

			NodeRef listContainerNodeRef = entityListDAO.getListContainer(pfNodeRef);
			NodeRef listNodeRef = entityListDAO.createList(listContainerNodeRef, PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST);
			Map<QName, Serializable> properties = new HashMap<>();
			Map<QName, List<NodeRef>> associations = new HashMap<>();
			associations.put(PublicationModel.ASSOC_PUBCHANNELLIST_CHANNEL, Arrays.asList(channelNodeRef));

			return entityListDAO.createListItem(listNodeRef, PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST, properties, associations);

		});

		inWriteTx(() -> {

			nodeService.setProperty(pfNodeRef, ContentModel.PROP_TITLE, "Update");
			return pfNodeRef;

		});

		inReadTx(() -> {
			Assert.assertNotNull(nodeService.getProperty(channelListItemNodeRef, PublicationModel.PROP_PUBCHANNELLIST_MODIFIED_DATE));
			return true;
		});

	}

}
