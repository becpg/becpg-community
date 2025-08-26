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
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.sample.StandardCakeWithLocalSemiFinishedTestProduct;
import fr.becpg.repo.sample.StandardChocolateEclairTestProduct;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class BatchOrderIT extends AbstractFinishedProductTest {

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

}
