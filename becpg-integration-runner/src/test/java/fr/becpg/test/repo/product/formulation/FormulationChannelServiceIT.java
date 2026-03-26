package fr.becpg.test.repo.product.formulation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authentication.AbstractAuthenticationService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PublicationModel;
import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.formulation.job.FormulationChannelService;
import fr.becpg.repo.publication.PublicationChannelService.PublicationChannelAction;
import fr.becpg.repo.publication.PublicationChannelService.PublicationChannelStatus;
import fr.becpg.test.PLMBaseTestCase;

public class FormulationChannelServiceIT extends PLMBaseTestCase {

	protected static final Log logger = LogFactory.getLog(FormulationChannelServiceIT.class);

	private FormulationChannelService formulationChannelService;
	
	@Autowired
	@Qualifier("authenticationService")
	private AbstractAuthenticationService authenticationService;

	@Before
	public void init() {
		publicationChannelService = Mockito.spy(publicationChannelService);
		systemConfigurationService = Mockito.spy(systemConfigurationService);
		doReturn("0").when(systemConfigurationService).confValue("beCPG.formulation.channel.minHoursSinceModification");
		doReturn("").when(systemConfigurationService).confValue("beCPG.formulation.channel.excludedTimeSlot");
		doReturn("true").when(systemConfigurationService).confValue("beCPG.formulation.channel.force");
		formulationChannelService = new FormulationChannelService(batchQueueService, publicationChannelService, systemConfigurationService,
				transactionService, nodeService, alfrescoRepository, entityDictionaryService, wUsedListService, formulationService,
				policyBehaviourFilter, namespaceService, authenticationService, 1, 1);
	}
	
	@Test
	public void testWhereUsedFormulation() throws InterruptedException {
		
		NodeRef finishedProductNodeRef = inWriteTx(() -> {
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("FP formulate test");
			finishedProduct.setParentNodeRef(getTestFolderNodeRef());
			return alfrescoRepository.save(finishedProduct).getNodeRef();
		});
		
		NodeRef rawMaterialNodeRef = inWriteTx(() -> {
			RawMaterialData rawMaterial = new RawMaterialData();
			rawMaterial.setName("RM 1 formulate test");
			rawMaterial.setParentNodeRef(getTestFolderNodeRef());
			return alfrescoRepository.save(rawMaterial).getNodeRef();
		});
		
		inWriteTx(() -> {
			FinishedProductData finishedProduct = (FinishedProductData) alfrescoRepository.findOne(finishedProductNodeRef);
			List<CompoListDataItem> compoList = new ArrayList<>();
			CompoListDataItem compoItem = new CompoListDataItem();
			compoItem.setQtySubFormula(1d);
			compoItem.setCompoListUnit(ProductUnit.kg);
			compoItem.setProduct(rawMaterialNodeRef);
			compoList.add(compoItem);
			finishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.save(finishedProduct);
		});
		
		assertIsNotPublished(finishedProductNodeRef);
		assertIsNotPublished(rawMaterialNodeRef);
		
		doReturn(new PagingResults<NodeRef>() {
			@Override
			public List<NodeRef> getPage() {
				return List.of(rawMaterialNodeRef);
			}
			@Override
			public boolean hasMoreItems() {
				return false;
			}
			@Override
			public Pair<Integer, Integer> getTotalResultCount() {
				return null;
			}
			@Override
			public String getQueryExecutionId() {
				return null;
			}
		})
		.when(publicationChannelService).getEntitiesByChannel(any(), any());
		
		BatchInfo batchInfo = inWriteTx(() -> {
			return formulationChannelService.reformulateEntities();
		});
		
		waitForBatchEnd(batchInfo);
		assertIsPublished(finishedProductNodeRef);
		assertIsPublished(rawMaterialNodeRef);
	}
	
	@Test
	public void testMultiLevelWhereUsedFormulation() throws InterruptedException {
		
		NodeRef finishedProductNodeRef = inWriteTx(() -> {
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("FP 2 formulate test");
			finishedProduct.setParentNodeRef(getTestFolderNodeRef());
			return alfrescoRepository.save(finishedProduct).getNodeRef();
		});
		
		NodeRef semiFinishedProductNodeRef = inWriteTx(() -> {
			SemiFinishedProductData semiFinishedProduct = new SemiFinishedProductData();
			semiFinishedProduct.setName("SF 2 formulate test");
			semiFinishedProduct.setParentNodeRef(getTestFolderNodeRef());
			return alfrescoRepository.save(semiFinishedProduct).getNodeRef();
		});
		
		NodeRef rawMaterialNodeRef = inWriteTx(() -> {
			RawMaterialData rawMaterial = new RawMaterialData();
			rawMaterial.setName("RM 2 formulate test");
			rawMaterial.setParentNodeRef(getTestFolderNodeRef());
			return alfrescoRepository.save(rawMaterial).getNodeRef();
		});
		
		inWriteTx(() -> {
			SemiFinishedProductData semiFinishedProduct = (SemiFinishedProductData) alfrescoRepository.findOne(semiFinishedProductNodeRef);
			List<CompoListDataItem> compoList = new ArrayList<>();
			CompoListDataItem compoItem = new CompoListDataItem();
			compoItem.setQtySubFormula(1d);
			compoItem.setCompoListUnit(ProductUnit.kg);
			compoItem.setProduct(rawMaterialNodeRef);
			compoList.add(compoItem);
			semiFinishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.save(semiFinishedProduct);
		});
		
		inWriteTx(() -> {
			FinishedProductData finishedProduct = (FinishedProductData) alfrescoRepository.findOne(finishedProductNodeRef);
			List<CompoListDataItem> compoList = new ArrayList<>();
			CompoListDataItem compoItem = new CompoListDataItem();
			compoItem.setQtySubFormula(1d);
			compoItem.setCompoListUnit(ProductUnit.kg);
			compoItem.setProduct(semiFinishedProductNodeRef);
			compoList.add(compoItem);
			finishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.save(finishedProduct);
		});
		
		doReturn(new PagingResults<NodeRef>() {
			@Override
			public List<NodeRef> getPage() {
				return List.of(rawMaterialNodeRef, semiFinishedProductNodeRef, finishedProductNodeRef);
			}
			@Override
			public boolean hasMoreItems() {
				return false;
			}
			@Override
			public Pair<Integer, Integer> getTotalResultCount() {
				return null;
			}
			@Override
			public String getQueryExecutionId() {
				return null;
			}
		})
		.when(publicationChannelService).getEntitiesByChannel(any(), any());
		
		BatchInfo batchInfo = inWriteTx(() -> {
			return formulationChannelService.reformulateEntities();
		});
		
		waitForBatchEnd(batchInfo);
		
		assertIsPublished(rawMaterialNodeRef);
		assertIsPublished(semiFinishedProductNodeRef);
		assertIsPublished(finishedProductNodeRef);
		
		inWriteTx(() -> {
			productService.formulate(semiFinishedProductNodeRef);
			productService.formulate(finishedProductNodeRef);
			return null;
		});
		
		inWriteTx(() -> {
			nodeService.setProperty(rawMaterialNodeRef, BeCPGModel.PROP_START_EFFECTIVITY, new Date());
			return null;
		});
		
		assertIsNotPublished(rawMaterialNodeRef);
		assertIsNotPublished(semiFinishedProductNodeRef);
		assertIsNotPublished(finishedProductNodeRef);

		doReturn(new PagingResults<NodeRef>() {
			@Override
			public List<NodeRef> getPage() {
				return List.of(rawMaterialNodeRef);
			}
			@Override
			public boolean hasMoreItems() {
				return false;
			}
			@Override
			public Pair<Integer, Integer> getTotalResultCount() {
				return null;
			}
			@Override
			public String getQueryExecutionId() {
				return null;
			}
		})
		.when(publicationChannelService).getEntitiesByChannel(any(), any());
		
		Date whereUsedModifiedDate = inReadTx(() -> (Date) nodeService.getProperty(semiFinishedProductNodeRef, ContentModel.PROP_MODIFIED));
		
		batchInfo = inWriteTx(() -> {
			return formulationChannelService.reformulateEntities();
		});
		
		waitForBatchEnd(batchInfo);
		
		assertIsPublished(rawMaterialNodeRef);
		assertIsPublished(semiFinishedProductNodeRef);
		assertIsPublished(finishedProductNodeRef);
		
		assertEquals(whereUsedModifiedDate, inReadTx(() -> (Date) nodeService.getProperty(semiFinishedProductNodeRef, ContentModel.PROP_MODIFIED)));

	}
	
	@Test
	public void testFormulationOnlyIfNeeded() throws InterruptedException {
		
		NodeRef rawMaterialNodeRef = inWriteTx(() -> {
			RawMaterialData rawMaterial = new RawMaterialData();
			rawMaterial.setName("RM formulate test");
			rawMaterial.setParentNodeRef(getTestFolderNodeRef());
			return alfrescoRepository.save(rawMaterial).getNodeRef();
		});
		
		inWriteTx(() -> {
			productService.formulate(rawMaterialNodeRef);
			return null;
		});
		
		Date formulatedDate = inReadTx(() -> (Date) nodeService.getProperty(rawMaterialNodeRef, BeCPGModel.PROP_FORMULATED_DATE));
		
		assertIsNotPublished(rawMaterialNodeRef);
		
		doReturn(new PagingResults<NodeRef>() {
			@Override
			public List<NodeRef> getPage() {
				return List.of(rawMaterialNodeRef);
			}
			@Override
			public boolean hasMoreItems() {
				return false;
			}
			@Override
			public Pair<Integer, Integer> getTotalResultCount() {
				return null;
			}
			@Override
			public String getQueryExecutionId() {
				return null;
			}
		})
		.when(publicationChannelService).getEntitiesByChannel(any(), any());
		
		BatchInfo batchInfo = inWriteTx(() -> {
			return formulationChannelService.reformulateEntities();
		});
		
		waitForBatchEnd(batchInfo);
		assertIsPublished(rawMaterialNodeRef);
		
		// assert that RM was not reformulated by the Job
		Date newFormulatedDate = inReadTx(() -> (Date) nodeService.getProperty(rawMaterialNodeRef, BeCPGModel.PROP_FORMULATED_DATE));
		assertEquals(formulatedDate, newFormulatedDate);
	}
	
	@Test
	public void testSystemConditionsThresholds() throws InterruptedException {
		
		doReturn("false").when(systemConfigurationService).confValue("beCPG.formulation.channel.force");

		NodeRef rawMaterialNodeRef = inWriteTx(() -> {
			RawMaterialData rawMaterial = new RawMaterialData();
			rawMaterial.setName("RM threshold test");
			rawMaterial.setParentNodeRef(getTestFolderNodeRef());
			return alfrescoRepository.save(rawMaterial).getNodeRef();
		});
		
		doReturn(new PagingResults<NodeRef>() {
			@Override
			public List<NodeRef> getPage() {
				return List.of(rawMaterialNodeRef);
			}
			@Override
			public boolean hasMoreItems() {
				return false;
			}
			@Override
			public Pair<Integer, Integer> getTotalResultCount() {
				return null;
			}
			@Override
			public String getQueryExecutionId() {
				return null;
			}
		})
		.when(publicationChannelService).getEntitiesByChannel(any(), any());
		
		// Test 1: maxCpuUsage = 0 (very restrictive, system load will likely exceed)
		doReturn("0").when(systemConfigurationService).confValue("beCPG.formulation.channel.maxCpuUsage");
		doReturn("999").when(systemConfigurationService).confValue("beCPG.formulation.channel.maxActiveUsers");
		
		BatchInfo batchInfo = inWriteTx(() -> formulationChannelService.reformulateEntities());
		// With maxCpuUsage=0, batch will likely not run unless system has negative load (impossible)
		// but depends on actual system conditions
		
		waitForBatchEnd(batchInfo);
		
		// Test 2: maxActiveUsers = 0 (very restrictive)
		doReturn("999").when(systemConfigurationService).confValue("beCPG.formulation.channel.maxCpuUsage");
		doReturn("0").when(systemConfigurationService).confValue("beCPG.formulation.channel.maxActiveUsers");
		
		batchInfo = inWriteTx(() -> formulationChannelService.reformulateEntities());
		// With maxActiveUsers=0, batch should NOT run if any user is connected
		// In test environment, at least the test user is connected
		assertNull("Batch should NOT run when maxActiveUsers=0 and users are connected", batchInfo);
		
		// Test 3: High thresholds - batch should run
		doReturn("999").when(systemConfigurationService).confValue("beCPG.formulation.channel.maxCpuUsage");
		doReturn("999").when(systemConfigurationService).confValue("beCPG.formulation.channel.maxActiveUsers");
		
		batchInfo = inWriteTx(() -> formulationChannelService.reformulateEntities());
		assertNotNull("Batch should run with high thresholds", batchInfo);
		waitForBatchEnd(batchInfo);
	}

	@Test
	public void testChannelBatchMetadataOnSuccess() throws InterruptedException {
		NodeRef rawMaterialNodeRef = inWriteTx(() -> {
			RawMaterialData rawMaterial = new RawMaterialData();
			rawMaterial.setName("RM batch metadata test");
			rawMaterial.setParentNodeRef(getTestFolderNodeRef());
			return alfrescoRepository.save(rawMaterial).getNodeRef();
		});

		NodeRef channelNodeRef = inReadTx(() -> publicationChannelService.getChannelById(FormulationChannelService.FORMULATE_ENTITIES_CHANNEL_ID));
		inWriteTx(() -> {
			nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_BATCHID, "41");
			return null;
		});

		mockChannelEntities(List.of(rawMaterialNodeRef));

		BatchInfo batchInfo = inWriteTx(() -> formulationChannelService.reformulateEntities());
		waitForBatchEnd(batchInfo);

		assertChannelStatus(channelNodeRef, PublicationChannelStatus.COMPLETED.toString(), "42", 0, 1);
		assertChannelListStatus(rawMaterialNodeRef, PublicationChannelStatus.COMPLETED.toString(), "42", null, null);
	}

	@Test
	public void testFailedBatchPublishesErrorAndRetryAction() throws InterruptedException {
		NodeRef rawMaterialNodeRef = inWriteTx(() -> {
			RawMaterialData rawMaterial = new RawMaterialData();
			rawMaterial.setName("RM batch failure test");
			rawMaterial.setParentNodeRef(getTestFolderNodeRef());
			return alfrescoRepository.save(rawMaterial).getNodeRef();
		});

		formulationService = Mockito.spy(formulationService);
		doThrow(new RuntimeException("Simulated formulation error")).when(formulationService).formulate(rawMaterialNodeRef);
		formulationChannelService = new FormulationChannelService(batchQueueService, publicationChannelService, systemConfigurationService,
				transactionService, nodeService, alfrescoRepository, entityDictionaryService, wUsedListService, formulationService,
				policyBehaviourFilter, namespaceService, authenticationService, 1, 1);

		NodeRef channelNodeRef = inReadTx(() -> publicationChannelService.getChannelById(FormulationChannelService.FORMULATE_ENTITIES_CHANNEL_ID));
		inWriteTx(() -> {
			nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_BATCHID, "0");
			return null;
		});
		mockChannelEntities(List.of(rawMaterialNodeRef));

		BatchInfo batchInfo = inWriteTx(() -> formulationChannelService.reformulateEntities());
		waitForBatchEnd(batchInfo);

		assertChannelStatus(channelNodeRef, PublicationChannelStatus.FAILED.toString(), "1", 1, 1);
		assertChannelListStatus(rawMaterialNodeRef, PublicationChannelStatus.FAILED.toString(), "1", "Simulated formulation error", null);

		String batchFullId = batchInfo.getBatchId() + "|" + batchInfo.getBatchDescId();
		BatchInfo retryBatchInfo = inWriteTx(() -> batchQueueService.retryBatchInError(batchFullId));
		waitForBatchEnd(retryBatchInfo);

		inReadTx(() -> {
			@SuppressWarnings("unchecked")
			List<String> errorIds = (List<String>) nodeService.getProperty(rawMaterialNodeRef, BeCPGModel.PROP_BATCH_ERROR_IDS);
			if (errorIds != null) {
				assertFalse(errorIds.contains(batchFullId));
			}
			return null;
		});
		assertChannelListStatus(rawMaterialNodeRef, PublicationChannelStatus.FAILED.toString(), "1", "Simulated formulation error",
				PublicationChannelAction.RETRY.toString());
	}

	/**
	 * Mocks the channel entities returned by the publication channel service.
	 *
	 * @param nodeRefs the entities returned for the channel
	 */
	private void mockChannelEntities(List<NodeRef> nodeRefs) {
		doReturn(new PagingResults<NodeRef>() {
			@Override
			public List<NodeRef> getPage() {
				return nodeRefs;
			}

			@Override
			public boolean hasMoreItems() {
				return false;
			}

			@Override
			public Pair<Integer, Integer> getTotalResultCount() {
				return null;
			}

			@Override
			public String getQueryExecutionId() {
				return null;
			}
		}).when(publicationChannelService).getEntitiesByChannel(any(), any());
	}

	/**
	 * Asserts the batch metadata stored on the channel node.
	 *
	 * @param channelNodeRef the publication channel node
	 * @param expectedStatus the expected channel status
	 * @param expectedBatchId the expected batch identifier
	 * @param expectedFailCount the expected failure count
	 * @param expectedReadCount the expected read count
	 */
	private void assertChannelStatus(NodeRef channelNodeRef, String expectedStatus, String expectedBatchId, Integer expectedFailCount,
			Integer expectedReadCount) {
		inReadTx(() -> {
			assertEquals(expectedStatus, nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_STATUS));
			assertEquals(expectedBatchId, nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_BATCHID));
			assertEquals(expectedFailCount, nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_FAILCOUNT));
			assertEquals(expectedReadCount, nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_READCOUNT));
			assertNotNull(nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_BATCHSTARTTIME));
			assertNotNull(nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_BATCHENDTIME));
			assertNotNull(nodeService.getProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_BATCHDURATION));
			return null;
		});
	}

	/**
	 * Asserts the publication channel list metadata for an entity.
	 *
	 * @param entityNodeRef the published entity
	 * @param expectedStatus the expected list item status
	 * @param expectedBatchId the expected list item batch identifier
	 * @param expectedErrorPart the expected error message fragment, or {@code null}
	 * @param expectedAction the expected action, or {@code null}
	 */
	private void assertChannelListStatus(NodeRef entityNodeRef, String expectedStatus, String expectedBatchId, String expectedErrorPart,
			String expectedAction) {
		inReadTx(() -> {
			NodeRef channelListNodeRef = getChannelListNodeRef(entityNodeRef);
			assertNotNull(channelListNodeRef);
			assertEquals(expectedStatus, nodeService.getProperty(channelListNodeRef, PublicationModel.PROP_PUBCHANNELLIST_STATUS));
			assertEquals(expectedBatchId, nodeService.getProperty(channelListNodeRef, PublicationModel.PROP_PUBCHANNELLIST_BATCHID));
			assertEquals(expectedAction, nodeService.getProperty(channelListNodeRef, PublicationModel.PROP_PUBCHANNELLIST_ACTION));
			assertNotNull(nodeService.getProperty(channelListNodeRef, PublicationModel.PROP_PUBCHANNELLIST_PUBLISHEDDATE));
			String error = (String) nodeService.getProperty(channelListNodeRef, PublicationModel.PROP_PUBCHANNELLIST_ERROR);
			if (expectedErrorPart == null) {
				assertNull(error);
			} else {
				assertNotNull(error);
				assertTrue(error.contains(expectedErrorPart));
			}
			return null;
		});
	}

	/**
	 * Returns the publication channel list node for the given entity.
	 *
	 * @param entityNodeRef the entity node
	 * @return the associated publication channel list node
	 */
	private NodeRef getChannelListNodeRef(NodeRef entityNodeRef) {
		NodeRef listContainer = entityListDAO.getListContainer(entityNodeRef);
		assertNotNull(listContainer);
		NodeRef listNodeRef = entityListDAO.getList(listContainer, PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST);
		assertNotNull(listNodeRef);
		NodeRef channelNodeRef = publicationChannelService.getChannelById(FormulationChannelService.FORMULATE_ENTITIES_CHANNEL_ID);
		assertNotNull(channelNodeRef);
		return entityListDAO.getListItem(listNodeRef, PublicationModel.ASSOC_PUBCHANNELLIST_CHANNEL, channelNodeRef);
	}

	@SuppressWarnings("unchecked")
	private void assertIsNotPublished(NodeRef entityNodeRef) {
		inReadTx(() -> {
			NodeRef channelNodeRef = publicationChannelService.getChannelById(FormulationChannelService.FORMULATE_ENTITIES_CHANNEL_ID);
			NodeRef listContainer = entityListDAO.getListContainer(entityNodeRef);
			if (listContainer != null) {
				NodeRef listNodeRef = entityListDAO.getList(listContainer, PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST);
				if (listNodeRef != null) {
					NodeRef channelListNodeRef = entityListDAO.getListItem(listNodeRef, PublicationModel.ASSOC_PUBCHANNELLIST_CHANNEL, channelNodeRef);
					if (channelListNodeRef != null) {
						List<String> publishedChannelIds = (List<String>) nodeService.getProperty(entityNodeRef, PublicationModel.PROP_PUBLISHED_CHANNELIDS);
						assertTrue(publishedChannelIds == null || !publishedChannelIds.contains(FormulationChannelService.FORMULATE_ENTITIES_CHANNEL_ID));
					}
				}
			}
			return null;
		});
	}

	@SuppressWarnings("unchecked")
	private void assertIsPublished(NodeRef rawMaterialNodeRef) {
		inReadTx(() -> {
			NodeRef listContainer = entityListDAO.getListContainer(rawMaterialNodeRef);
			assertNotNull(listContainer);
			NodeRef listNodeRef = entityListDAO.getList(listContainer, PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST);
			assertNotNull(listNodeRef);
			NodeRef channelNodeRef = publicationChannelService.getChannelById(FormulationChannelService.FORMULATE_ENTITIES_CHANNEL_ID);
			assertNotNull(channelNodeRef);
			NodeRef channelListNodeRef = entityListDAO.getListItem(listNodeRef, PublicationModel.ASSOC_PUBCHANNELLIST_CHANNEL, channelNodeRef);
			assertNotNull(channelListNodeRef);
			List<String> channelIds = (List<String>) nodeService.getProperty(rawMaterialNodeRef, PublicationModel.PROP_CHANNELIDS);
			assertTrue(channelIds != null && channelIds.contains(FormulationChannelService.FORMULATE_ENTITIES_CHANNEL_ID));
			List<String> publishedChannelIds = (List<String>) nodeService.getProperty(rawMaterialNodeRef, PublicationModel.PROP_PUBLISHED_CHANNELIDS);
			assertTrue(publishedChannelIds != null && publishedChannelIds.contains(FormulationChannelService.FORMULATE_ENTITIES_CHANNEL_ID));
			return null;
		});
	}

}
