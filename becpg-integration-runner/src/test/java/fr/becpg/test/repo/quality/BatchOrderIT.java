package fr.becpg.test.repo.quality;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.quality.data.BatchData;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.test.repo.StandardChocolateEclairTestProduct;
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
			
			Assert.assertEquals(batchData.getCompoList().size(),3);

			return batchData;

		});

	

	}

}
