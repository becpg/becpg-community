/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.test.repo.web.scripts.remote;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Status;

import fr.becpg.model.PublicationModel;
import fr.becpg.model.SystemGroup;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.publication.PublicationChannelService.PublicationChannelStatus;
import fr.becpg.test.BeCPGTestHelper;
import fr.becpg.test.PLMBaseTestCase;
import fr.becpg.test.utils.TestWebscriptExecuters;
import fr.becpg.test.utils.TestWebscriptExecuters.PostRequest;
import fr.becpg.test.utils.TestWebscriptExecuters.PutRequest;
import fr.becpg.test.utils.TestWebscriptExecuters.Response;

public class RemoteChannelBatchWebScriptIT extends PLMBaseTestCase {

	private static final String CONNECTOR_PASSWORD = "PWD";
	private static final String CONNECTOR_ACCOUNT_TEST = "connector_account_test";

	@Autowired
	private EntityListDAO entityListDAO;

	@Test
	public void testBatchAuthentication() throws IOException {
		inWriteTx(() -> {
			if (authenticationDAO.userExists(CONNECTOR_ACCOUNT_TEST)) {
				personService.deletePerson(CONNECTOR_ACCOUNT_TEST);
			}
			BeCPGTestHelper.createUser(CONNECTOR_ACCOUNT_TEST);
			return null;
		});

		JSONObject requestBody = new JSONObject();

		NodeRef channelNodeRef = createTestChannel("test-channel-auth");
		String channelId = (String) nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_ID);

		String url = "/becpg/remote/channel/batch/start?channelId=" + channelId;
		TestWebscriptExecuters.sendRequest(new PutRequest(url, requestBody.toString(), "application/json"), Status.STATUS_FORBIDDEN,
				CONNECTOR_ACCOUNT_TEST, CONNECTOR_PASSWORD);

		inWriteTx(() -> {
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + SystemGroup.ApiConnector, CONNECTOR_ACCOUNT_TEST);
			return null;
		});

		TestWebscriptExecuters.sendRequest(new PutRequest(url, requestBody.toString(), "application/json"), Status.STATUS_BAD_REQUEST,
				CONNECTOR_ACCOUNT_TEST, CONNECTOR_PASSWORD);

		inWriteTx(() -> {
			if (authenticationDAO.userExists(CONNECTOR_ACCOUNT_TEST)) {
				personService.deletePerson(CONNECTOR_ACCOUNT_TEST);
			}
			BeCPGTestHelper.createUser(CONNECTOR_ACCOUNT_TEST);
			return null;
		});

		TestWebscriptExecuters.sendRequest(new PutRequest(url, requestBody.toString(), "application/json"), Status.STATUS_FORBIDDEN,
				CONNECTOR_ACCOUNT_TEST, CONNECTOR_PASSWORD);

		inWriteTx(() -> {
			nodeService.createAssociation(channelNodeRef, personService.getPerson(CONNECTOR_ACCOUNT_TEST),
					PublicationModel.ASSOC_PUBCHANNEL_ACCOUNTS);
			return null;
		});

		Response response = TestWebscriptExecuters.sendRequest(new PutRequest(url, requestBody.toString(), "application/json"),
				Status.STATUS_BAD_REQUEST, CONNECTOR_ACCOUNT_TEST, CONNECTOR_PASSWORD);

		assertNotNull(response);
	}

	@Test
	public void testBatchStart() throws IOException {
		inWriteTx(() -> {
			if (authenticationDAO.userExists(CONNECTOR_ACCOUNT_TEST)) {
				personService.deletePerson(CONNECTOR_ACCOUNT_TEST);
			}
			BeCPGTestHelper.createUser(CONNECTOR_ACCOUNT_TEST);
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + SystemGroup.ApiConnector, CONNECTOR_ACCOUNT_TEST);
			return null;
		});

		NodeRef channelNodeRef = createTestChannel("test-channel-start");
		String channelId = (String) nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_ID);
		String url = "/becpg/remote/channel/batch/start?channelId=" + channelId;

		JSONObject attributes = new JSONObject();
		attributes.put("bp:pubChannelBatchId", "batch-001");
		JSONObject entity = new JSONObject();
		entity.put("attributes", attributes);
		JSONObject requestBody = new JSONObject();
		requestBody.put("entity", entity);

		TestWebscriptExecuters.sendRequest(new PutRequest(url, requestBody.toString(), "application/json"), Status.STATUS_OK, CONNECTOR_ACCOUNT_TEST,
				CONNECTOR_PASSWORD);

		inReadTx(() -> {
			assertEquals(PublicationChannelStatus.STARTED.toString(), nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_STATUS));
			assertEquals("batch-001", nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_BATCHID));
			assertNotNull(nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_BATCHSTARTTIME));
			assertNull(nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_BATCHENDTIME));
			return null;
		});
	}

	/**
	 * Test batch end with valid parameters.
	 */
	@Test
	public void testBatchEnd() throws IOException {
		inWriteTx(() -> {
			if (authenticationDAO.userExists(CONNECTOR_ACCOUNT_TEST)) {
				personService.deletePerson(CONNECTOR_ACCOUNT_TEST);
			}
			BeCPGTestHelper.createUser(CONNECTOR_ACCOUNT_TEST);
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + SystemGroup.ApiConnector, CONNECTOR_ACCOUNT_TEST);
			return null;
		});
		// Create a test channel
		NodeRef channelNodeRef = createTestChannel("test-channel-end");
		String channelId = (String) nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_ID);

		inWriteTx(() -> {
			// Set initial batch properties
			nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_BATCHID, "batch-002");
			nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_BATCHSTARTTIME, new Date());
			return null;
		});

		// Prepare request body
		JSONObject requestBody = new JSONObject();
		JSONObject entity = new JSONObject();
		JSONObject attributes = new JSONObject();

		attributes.put("bp:pubChannelStatus", PublicationChannelStatus.COMPLETED.toString());
		attributes.put("bp:pubChannelFailCount", 2);
		attributes.put("bp:pubChannelReadCount", 100);
		attributes.put("bp:pubChannelBatchDuration", 5000L);
		attributes.put("bp:pubChannelLastSuccessBatchId", "batch-002");

		entity.put("attributes", attributes);
		requestBody.put("entity", entity);

		// Execute request
		String url = "/becpg/remote/channel/batch/end?channelId=" + channelId;
		Response response = TestWebscriptExecuters.sendRequest(new PutRequest(url, requestBody.toString(), "application/json"), 200,
				CONNECTOR_ACCOUNT_TEST, CONNECTOR_PASSWORD);

		assertEquals(200, response.getStatus());

		// Verify response
		JSONObject jsonResponse = new JSONObject(response.getContentAsString());
		assertEquals("SUCCESS", jsonResponse.getString("status"));
		assertEquals(channelId, jsonResponse.getString("channelId"));

		inReadTx(() -> {
			// Verify channel properties were updated
			assertEquals(PublicationChannelStatus.COMPLETED.toString(), nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_STATUS));
			assertEquals(2, nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_FAILCOUNT));
			assertEquals(100, nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_READCOUNT));
			assertEquals(5000L, nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_BATCHDURATION));
			assertNotNull(nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_BATCHENDTIME));
			return null;
		});
	}

	/**
	 * Test batch acknowledgment with valid parameters.
	 */
	@Test
	public void testBatchAck() throws IOException {
		inWriteTx(() -> {
			if (authenticationDAO.userExists(CONNECTOR_ACCOUNT_TEST)) {
				personService.deletePerson(CONNECTOR_ACCOUNT_TEST);
			}
			BeCPGTestHelper.createUser(CONNECTOR_ACCOUNT_TEST);
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + SystemGroup.ApiConnector, CONNECTOR_ACCOUNT_TEST);
			return null;
		});
		// Create a test channel and entity
		NodeRef channelNodeRef = createTestChannel("test-channel-ack");
		String channelId = inReadTx(() -> (String) nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_ID));
		NodeRef entityNodeRef = createTestEntity("test-entity");

		// Prepare request body
		JSONObject requestBody = new JSONObject();
		JSONObject entity = new JSONObject();
		JSONObject attributes = new JSONObject();

		attributes.put("bp:pubChannelListStatus", PublicationChannelStatus.COMPLETED.toString());
		attributes.put("bp:pubChannelListBatchId", "batch-003");
		attributes.put("bp:pubChannelListPublishedDate", ISO8601DateFormat.format(new Date()));

		entity.put("attributes", attributes);
		requestBody.put("entity", entity);

		// Execute request
		String url = "/becpg/remote/channel/batch/ack?channelId=" + channelId + "&entityNodeRef=" + entityNodeRef.toString();
		Response response = TestWebscriptExecuters.sendRequest(new PostRequest(url, requestBody.toString(), "application/json"), 200,
				CONNECTOR_ACCOUNT_TEST, CONNECTOR_PASSWORD);

		assertEquals(200, response.getStatus());

		// Verify response
		JSONObject jsonResponse = new JSONObject(response.getContentAsString());
		assertEquals("SUCCESS", jsonResponse.getString("status"));
		assertEquals(channelId, jsonResponse.getString("channelId"));
		assertEquals(entityNodeRef.toString(), jsonResponse.getString("entityNodeRef"));

		inReadTx(() -> {
			// Verify channel list item was created/updated
			NodeRef listContainer = entityListDAO.getListContainer(entityNodeRef);
			assertNotNull(listContainer);
			NodeRef listNodeRef = entityListDAO.getList(listContainer, PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST);
			assertNotNull(listNodeRef);
			NodeRef channelListNodeRef = entityListDAO.getListItem(listNodeRef, PublicationModel.ASSOC_PUBCHANNELLIST_CHANNEL, channelNodeRef);
			assertNotNull(channelListNodeRef);
			
			assertEquals(PublicationChannelStatus.COMPLETED.toString(), nodeService.getProperty(channelListNodeRef, PublicationModel.PROP_PUBCHANNELLIST_STATUS));
			assertEquals("batch-003", nodeService.getProperty(channelListNodeRef, PublicationModel.PROP_PUBCHANNELLIST_BATCHID));
			return null;
		});
	}

	/**
	 * Helper method to create a test channel.
	 */
	private NodeRef createTestChannel(String channelId) {
		return inWriteTx(() -> {
			NodeRef channelNodeRef = createPublicationChannelNode(channelId);
			nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_ID, channelId);
			return channelNodeRef;
		});
	}

	/**
	 * Helper method to create a test entity.
	 */
	private NodeRef createTestEntity(String name) {
		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			return createFinishedProductNode(name);
		});
	}

	private NodeRef createFinishedProductNode(String name) {
		return inWriteTx(() -> {
			FinishedProductData productData = new FinishedProductData();
			productData.setParentNodeRef(getTestFolderNodeRef());
			productData.setName(name);
			alfrescoRepository.save(productData);
			return productData.getNodeRef();
		});
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
}