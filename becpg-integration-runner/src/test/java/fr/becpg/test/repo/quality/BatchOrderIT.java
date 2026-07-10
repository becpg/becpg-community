package fr.becpg.test.repo.quality;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.quality.data.BatchData;
import fr.becpg.repo.quality.data.dataList.AllocationListDataItem;
import fr.becpg.repo.quality.data.dataList.StockListDataItem;
import fr.becpg.repo.quality.web.scripts.ScanBatchWebScript;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.sample.StandardCakeWithLocalSemiFinishedTestProduct;
import fr.becpg.repo.sample.StandardChocolateEclairTestProduct;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class BatchOrderIT extends AbstractFinishedProductTest {

	@Autowired
	private ScanBatchWebScript scanBatchWebScript;

	@Autowired
	AlfrescoRepository<BatchData> batchRepository;

	@Autowired
	FormulationService<BatchData> formulationService;

	@Test
	public void testBatchOrder() {

		NodeRef batchNodeRef = inWriteTx(() -> {

			StandardChocolateEclairTestProduct testProduct = new StandardChocolateEclairTestProduct.Builder()
					.withAlfrescoRepository(alfrescoRepository).withNodeService(nodeService).withDestFolder(getTestFolderNodeRef()).withCompo(true)
					.withLabeling(false).withGenericRawMaterial(true).withStocks(true).build();

			BatchData batchData = new BatchData();
			batchData.setName("2Kg d'éclair au chocolat");
			batchData.setPlants(List.of(testProduct.getOrCreateCharact(StandardChocolateEclairTestProduct.PLANT_USINE_1, PLMModel.TYPE_PLANT)));
			batchData.setLaboratories(
					List.of(testProduct.getOrCreateCharact(StandardChocolateEclairTestProduct.LABORATORY_1, PLMModel.TYPE_LABORATORY)));
			batchData.setBatchQty(2d);

			batchData.setProduct(testProduct.createTestProduct());

			batchData = batchRepository.create(getTestFolderNodeRef(), batchData);

			formulationService.formulate(batchData.getProduct().getNodeRef());

			return batchData.getNodeRef();

		});

		inWriteTx(() -> {

			BatchData batchData = batchRepository.findOne(batchNodeRef);

			formulationService.formulate(batchData.getProduct().getNodeRef());
			formulationService.formulate(batchNodeRef);
			
			Assert.assertEquals(3L,batchData.getCompoList().size());

			return batchData;

		});

	}

	/**
	 * Local semi-finished products and their children 
	 * should be properly copied to batch composition with correct parent-child hierarchy
	 */
	@Test
	public void testBatchCompositionWithLocalSemiFinishedProducts() {

		NodeRef batchNodeRef = inWriteTx(() -> {

			StandardCakeWithLocalSemiFinishedTestProduct testProduct = new StandardCakeWithLocalSemiFinishedTestProduct.Builder()
					.withAlfrescoRepository(alfrescoRepository).withNodeService(nodeService).withDestFolder(getTestFolderNodeRef()).withCompo(true)
					.build();

			FinishedProductData cake = testProduct.createTestProduct();

			// Formulate the product first
			formulationService.formulate(cake.getNodeRef());

			// Create batch
			BatchData batchData = new BatchData();
			batchData.setName("5Kg Cake Batch");
			batchData.setPlants(List.of(testProduct.getOrCreateCharact(StandardCakeWithLocalSemiFinishedTestProduct.PLANT_USINE_1, PLMModel.TYPE_PLANT)));
			batchData.setLaboratories(
					List.of(testProduct.getOrCreateCharact(StandardCakeWithLocalSemiFinishedTestProduct.LABORATORY_1, PLMModel.TYPE_LABORATORY)));
			batchData.setBatchQty(5d);
			batchData.setProduct(cake);

			batchData = batchRepository.create(getTestFolderNodeRef(), batchData);

			return batchData.getNodeRef();

		});

		inWriteTx(() -> {

			BatchData batchData = batchRepository.findOne(batchNodeRef);

			// Formulate the batch
			formulationService.formulate(batchNodeRef);

			// Verify composition list contains expected items
			Assert.assertNotNull("Batch composition list should not be null", batchData.getCompoList());
			Assert.assertTrue("Batch composition should contain items", batchData.getCompoList().size() > 0);

			// Find parent and child items in batch composition
			CompoListDataItem batchDoughItem = null;
			CompoListDataItem batchButterItem = null;

			for (CompoListDataItem item : batchData.getCompoList()) {
				String productName = (String) nodeService.getProperty(item.getProduct(), ContentModel.PROP_NAME);
				if (StandardCakeWithLocalSemiFinishedTestProduct.CAKE_BATTER_NAME.equals(productName)) {
					batchDoughItem = item;
				} else if (StandardCakeWithLocalSemiFinishedTestProduct.BUTTERCREAM_NAME.equals(productName)) {
					batchButterItem = item;
				}
			}

			// Verify both items exist in batch composition
			Assert.assertNotNull("Cake Batter should be present in batch composition", batchDoughItem);
			Assert.assertNotNull("Buttercream should be present in batch composition", batchButterItem);

			// Critical test: Verify parent-child relationship is preserved in batch
			Assert.assertNotNull("Buttercream should have a parent in batch composition", batchButterItem.getParent());
			Assert.assertEquals("Buttercream's parent should be the cloned cake batter item, not original", 
					batchDoughItem, batchButterItem.getParent());

			// Verify quantities are scaled correctly (5kg batch vs 1.2kg product = ~4.17x ratio)
			Assert.assertEquals("Cake Batter quantity should be scaled", 3333d, batchDoughItem.getQtySubFormula(), 10d);
			Assert.assertEquals("Buttercream quantity should be scaled", 1667d, batchButterItem.getQtySubFormula(), 10d);

			return batchData;

		});

	}

	@Test
	public void testBatchScannerInput() {

		NodeRef batchNodeRef = inWriteTx(() -> {

			StandardChocolateEclairTestProduct testProduct = new StandardChocolateEclairTestProduct.Builder()
					.withAlfrescoRepository(alfrescoRepository).withNodeService(nodeService).withDestFolder(getTestFolderNodeRef()).withCompo(true)
					.withLabeling(false).withGenericRawMaterial(true).withStocks(true).build();

			BatchData batchData = new BatchData();
			batchData.setName("Batch for Scanning");
			batchData.setPlants(List.of(testProduct.getOrCreateCharact(StandardChocolateEclairTestProduct.PLANT_USINE_1, PLMModel.TYPE_PLANT)));
			batchData.setLaboratories(
					List.of(testProduct.getOrCreateCharact(StandardChocolateEclairTestProduct.LABORATORY_1, PLMModel.TYPE_LABORATORY)));
			batchData.setBatchQty(2d);

			batchData.setProduct(testProduct.createTestProduct());

			batchData = batchRepository.create(getTestFolderNodeRef(), batchData);

			formulationService.formulate(batchData.getProduct().getNodeRef());

			return batchData.getNodeRef();

		});

		final NodeRef matchingAllocationNodeRef = inWriteTx(() -> {

			BatchData batchData = batchRepository.findOne(batchNodeRef);

			// Formulate the batch
			formulationService.formulate(batchNodeRef);

			// Get lists container
			NodeRef listContainer = entityListDAO.getListContainer(batchNodeRef);
			Assert.assertNotNull(listContainer);

			// Check allocation list has items
			NodeRef allocationList = entityListDAO.getList(listContainer, fr.becpg.model.QualityModel.TYPE_BATCH_ALLOCATION_LIST);
			Assert.assertNotNull(allocationList);
			List<NodeRef> allocationItems = entityListDAO.getListItems(allocationList, fr.becpg.model.QualityModel.TYPE_BATCH_ALLOCATION_LIST);
			Assert.assertFalse(allocationItems.isEmpty());

			// Find an allocation with a product having an ERP code
			AllocationListDataItem matchingAllocation = null;
			String targetErpCode = null;
			for (NodeRef allocationNodeRef : allocationItems) {
				AllocationListDataItem item = (AllocationListDataItem) alfrescoRepository.findOne(allocationNodeRef);
				NodeRef productNodeRef = item.getProduct();
				if (productNodeRef != null) {
					String erpCode = (String) nodeService.getProperty(productNodeRef, fr.becpg.model.BeCPGModel.PROP_ERP_CODE);
					if (erpCode == null) {
						erpCode = "ERP-TEST-" + System.currentTimeMillis();
						nodeService.setProperty(productNodeRef, fr.becpg.model.BeCPGModel.PROP_ERP_CODE, erpCode);
					}
					matchingAllocation = item;
					targetErpCode = erpCode;
					break;
				}
			}

			Assert.assertNotNull(matchingAllocation);
			Assert.assertNotNull(targetErpCode);

			// Get stock list
			NodeRef rawMaterialNodeRef = matchingAllocation.getProduct();
			NodeRef rawMaterialListContainer = entityListDAO.getListContainer(rawMaterialNodeRef);
			if (rawMaterialListContainer == null) {
				rawMaterialListContainer = entityListDAO.createListContainer(rawMaterialNodeRef);
			}
			Assert.assertNotNull(rawMaterialListContainer);

			NodeRef stockList = entityListDAO.getList(rawMaterialListContainer, fr.becpg.model.QualityModel.TYPE_STOCK_LIST);
			if (stockList == null) {
				stockList = entityListDAO.createList(rawMaterialListContainer, fr.becpg.model.QualityModel.TYPE_STOCK_LIST);
			}
			Assert.assertNotNull(stockList);
			List<NodeRef> stockItems = entityListDAO.getListItems(stockList, fr.becpg.model.QualityModel.TYPE_STOCK_LIST);

			// Find or create a matching stock list item
			StockListDataItem matchingStock = null;
			if (stockItems != null && !stockItems.isEmpty()) {
				for (NodeRef stockNodeRef : stockItems) {
					StockListDataItem stockItem = (StockListDataItem) alfrescoRepository.findOne(stockNodeRef);
					if (stockItem.getProduct() != null && stockItem.getProduct().equals(matchingAllocation.getProduct())) {
						matchingStock = stockItem;
						break;
					}
				}
			}

			if (matchingStock == null) {
				// Create a stock item
				matchingStock = new StockListDataItem();
				matchingStock.setProduct(matchingAllocation.getProduct());
				matchingStock.setBatchId("LOT-TEST-123");
				matchingStock.setBatchQty(10d);
				matchingStock = (StockListDataItem) alfrescoRepository.create(stockList, matchingStock);
			}

			String targetBatchId = matchingStock.getBatchId();

			// Set the scan input property
			String scanInputValue = targetErpCode + " - " + targetBatchId;
			scanBatchWebScript.executeScan(batchNodeRef, scanInputValue);

			// Also test Order B: batchId - erpCode
			String scanInputValueOrderB = targetBatchId + " - " + targetErpCode;
			scanBatchWebScript.executeScan(batchNodeRef, scanInputValueOrderB);

			return matchingAllocation.getNodeRef();

		});

		inReadTx(() -> {
			AllocationListDataItem updatedAllocation = (AllocationListDataItem) alfrescoRepository.findOne(matchingAllocationNodeRef);
			Assert.assertNotNull(updatedAllocation);
			Assert.assertNotNull(updatedAllocation.getStockListItems());
			Assert.assertFalse(updatedAllocation.getStockListItems().isEmpty());
			return null;
		});

	}
}
