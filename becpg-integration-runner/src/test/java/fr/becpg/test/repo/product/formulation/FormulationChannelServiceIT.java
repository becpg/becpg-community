package fr.becpg.test.repo.product.formulation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.formulation.job.FormulationChannelService;
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
		
		// Test 1: maxSystemLoad = 0 (very restrictive, system load will likely exceed)
		doReturn("0").when(systemConfigurationService).confValue("beCPG.formulation.channel.maxSystemLoad");
		doReturn("999").when(systemConfigurationService).confValue("beCPG.formulation.channel.maxActiveUsers");
		
		BatchInfo batchInfo = inWriteTx(() -> formulationChannelService.reformulateEntities());
		// With maxSystemLoad=0, batch will likely not run unless system has negative load (impossible)
		// but depends on actual system conditions
		
		waitForBatchEnd(batchInfo);
		
		// Test 2: maxActiveUsers = 0 (very restrictive)
		doReturn("999").when(systemConfigurationService).confValue("beCPG.formulation.channel.maxSystemLoad");
		doReturn("0").when(systemConfigurationService).confValue("beCPG.formulation.channel.maxActiveUsers");
		
		batchInfo = inWriteTx(() -> formulationChannelService.reformulateEntities());
		// With maxActiveUsers=0, batch should NOT run if any user is connected
		// In test environment, at least the test user is connected
		assertNull("Batch should NOT run when maxActiveUsers=0 and users are connected", batchInfo);
		
		// Test 3: High thresholds - batch should run
		doReturn("999").when(systemConfigurationService).confValue("beCPG.formulation.channel.maxSystemLoad");
		doReturn("999").when(systemConfigurationService).confValue("beCPG.formulation.channel.maxActiveUsers");
		
		batchInfo = inWriteTx(() -> formulationChannelService.reformulateEntities());
		assertNotNull("Batch should run with high thresholds", batchInfo);
		waitForBatchEnd(batchInfo);
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
