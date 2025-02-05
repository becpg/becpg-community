package fr.becpg.test.repo.product.formulation;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.VersionType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.product.data.LogisticUnitData;
import fr.becpg.repo.sample.StandardChocolateEclairTestProduct;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class LogisticUnitIT extends AbstractFinishedProductTest {
	
	protected static final Log logger = LogFactory.getLog(LogisticUnitIT.class);
	
	@Autowired
	private EntityVersionService entityVersionService;

	private LogisticUnitData updateLogisticUnit(LogisticUnitData logisticUnit) {
		return logisticUnit.withName("Palette d'éclairs au chocolat 2").withTertiaryWidth(600d);
	}
	
	private LogisticUnitData updateLogisticUnitBranch(LogisticUnitData logisticUnit) {
		return logisticUnit.withTertiaryWidth(700d);
	}
	
	@Test
	public void testLUCreation() {
		List<NodeRef> logisticUnitNodeRefList = inWriteTx(() -> {
			logger.info("Creating logistic units");
			
				StandardChocolateEclairTestProduct testProduct = new StandardChocolateEclairTestProduct.Builder()
						.withAlfrescoRepository(alfrescoRepository).withNodeService(nodeService).withDestFolder(getTestFolderNodeRef()).withCompo(false)
						.withLabeling(false).build();

	
			
			LogisticUnitData caseLogisticUnit = testProduct.createCaseLogisticUnit();
			NodeRef caseLogisticUnitNodeRef = caseLogisticUnit.getNodeRef();
			
			LogisticUnitData palletLogisticUnit = testProduct.createPalletLogisticUnit(caseLogisticUnitNodeRef);
			NodeRef palletLogisticUnitNodeRef =palletLogisticUnit.getNodeRef();
			
			return List.of(caseLogisticUnitNodeRef, palletLogisticUnitNodeRef);
		});

		inReadTx(() -> {
			LogisticUnitData caseLogisticUnitEntity = (LogisticUnitData) alfrescoRepository.findOne(logisticUnitNodeRefList.get(0));
			LogisticUnitData palletLogisticUnitEntity = (LogisticUnitData) alfrescoRepository.findOne(logisticUnitNodeRefList.get(1));
			
			assertEquals("Colis d'éclairs au chocolat", caseLogisticUnitEntity.getName());
			assertEquals(200d, caseLogisticUnitEntity.getSecondaryWidth());
			assertEquals("Éclair au chocolat", alfrescoRepository.findOne(caseLogisticUnitEntity.getCompoList().get(0).getProduct()).getName());
			assertEquals("Colis en carton", alfrescoRepository.findOne(caseLogisticUnitEntity.getPackagingList().get(0).getProduct()).getName());
			
			assertEquals("Palette d'éclairs au chocolat", palletLogisticUnitEntity.getName());
			assertEquals(500d, palletLogisticUnitEntity.getTertiaryWidth());
			assertEquals("Colis d'éclairs au chocolat", alfrescoRepository.findOne(palletLogisticUnitEntity.getCompoList().get(0).getProduct()).getName());
			assertEquals("Palette en bois", alfrescoRepository.findOne(palletLogisticUnitEntity.getPackagingList().get(0).getProduct()).getName());
			
			return null;
		});
		
		NodeRef updatedLogisticUnitNodeRef = inWriteTx(() -> {
			logger.info("Updating logistic unit");
			
			LogisticUnitData updatedLogisticUnit = updateLogisticUnit((LogisticUnitData) alfrescoRepository.findOne(logisticUnitNodeRefList.get(1)));
			
			return alfrescoRepository.save(updatedLogisticUnit).getNodeRef();
		});
		
		inReadTx(() -> {
			LogisticUnitData updatedLogisticUnitEntity = (LogisticUnitData) alfrescoRepository.findOne(updatedLogisticUnitNodeRef);
			
			assertEquals("Palette d'éclairs au chocolat 2", updatedLogisticUnitEntity.getName());
			assertEquals(600d, updatedLogisticUnitEntity.getTertiaryWidth());
			
			return null;
		});
		
		inWriteTx(() -> {
			logger.info("Formulating logistic unit");
			
			productService.formulate(updatedLogisticUnitNodeRef);
			
			return null;
		});
		
		NodeRef logisticUnitBranchNodeRef = inWriteTx(() -> {
			logger.info("Creating branch for logistic unit");
			
			NodeRef logisticUnitBranch = entityVersionService.createBranch(updatedLogisticUnitNodeRef, getTestFolderNodeRef());
			
			return logisticUnitBranch;
		});
		
		LogisticUnitData logisticUnitBranch = inReadTx(() -> {
			return (LogisticUnitData) alfrescoRepository.findOne(logisticUnitBranchNodeRef);
		});
		
		NodeRef updatedLogisticUnitBranchNodeRef = inWriteTx(() -> {
			return alfrescoRepository.save(updateLogisticUnitBranch(logisticUnitBranch)).getNodeRef();
		});
		
		inReadTx(() -> {
			LogisticUnitData updatedLogisticUnitEntity = (LogisticUnitData) alfrescoRepository.findOne(updatedLogisticUnitNodeRef);
			LogisticUnitData updatedLogisticUnitBranchEntity = (LogisticUnitData) alfrescoRepository.findOne(updatedLogisticUnitBranchNodeRef);
			
			assertEquals(600d, updatedLogisticUnitEntity.getTertiaryWidth());
			assertEquals(700d, updatedLogisticUnitBranchEntity.getTertiaryWidth());
			
			return null;
		});
		
		NodeRef mergedLogisticUnitNodeRef = inWriteTx(() -> {
			logger.info("Merging branches for logistic unit");
			
			return entityVersionService.mergeBranch(updatedLogisticUnitBranchNodeRef, updatedLogisticUnitNodeRef, VersionType.MINOR, "Updated pallet width");
		});
		
		inReadTx(() -> {
			LogisticUnitData mergedLogisticUnitEntity = (LogisticUnitData) alfrescoRepository.findOne(mergedLogisticUnitNodeRef);
			
			assertEquals(700d, mergedLogisticUnitEntity.getTertiaryWidth());
			
			return null;
		});
	}
	
}