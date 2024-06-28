package fr.becpg.test.repo.product.formulation;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.VersionType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LogisticUnitData;
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class LogisticUnitIT extends AbstractFinishedProductTest {
	
	@Autowired
	private EntityVersionService entityVersionService;
	
	private LogisticUnitData createCaseLogisticUnit() {
		FinishedProductData eclairAuChocolat = FinishedProductData.build().withName("Eclair au chocolat");
		NodeRef productNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), eclairAuChocolat).getNodeRef();
		
		PackagingMaterialData packagingMaterialData = PackagingMaterialData.build().withName("Colis en carton");
		NodeRef packagingMaterialNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), packagingMaterialData).getNodeRef();
		
		return LogisticUnitData.build()
				.withName("Colis d'éclairs au chocolat")
				.withSecondaryWidth(200d)
				.withCompoList(List.of(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Detail).withProduct(productNodeRef)))
				.withPackagingList(List.of(PackagingListDataItem.build().withQty(500d).withUnit(ProductUnit.g).withPkgLevel(PackagingLevel.Secondary).withProduct(packagingMaterialNodeRef)));
	}
	
	private LogisticUnitData createPalletLogisticUnit(NodeRef caseNodeRef) {
		PackagingMaterialData packagingMaterialData = PackagingMaterialData.build().withName("Palette en bois");
		NodeRef packagingMaterialNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), packagingMaterialData).getNodeRef();
		
		return LogisticUnitData.build()
				.withName("Palette d'éclairs au chocolat")
				.withTertiaryWidth(500d)
				.withCompoList(List.of(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Detail).withProduct(caseNodeRef)))
				.withPackagingList(List.of(PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.kg).withPkgLevel(PackagingLevel.Tertiary).withProduct(packagingMaterialNodeRef)));
	}
	
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
			
			LogisticUnitData caseLogisticUnit = createCaseLogisticUnit();
			NodeRef caseLogisticUnitNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), caseLogisticUnit).getNodeRef();
			
			LogisticUnitData palletLogisticUnit = createPalletLogisticUnit(caseLogisticUnitNodeRef);
			NodeRef palletLogisticUnitNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), palletLogisticUnit).getNodeRef();
			
			return List.of(caseLogisticUnitNodeRef, palletLogisticUnitNodeRef);
		});

		inReadTx(() -> {
			LogisticUnitData caseLogisticUnitEntity = (LogisticUnitData) alfrescoRepository.findOne(logisticUnitNodeRefList.get(0));
			LogisticUnitData palletLogisticUnitEntity = (LogisticUnitData) alfrescoRepository.findOne(logisticUnitNodeRefList.get(1));
			
			assertEquals("Colis d'éclairs au chocolat", caseLogisticUnitEntity.getName());
			assertEquals(200d, caseLogisticUnitEntity.getSecondaryWidth());
			assertEquals("Eclair au chocolat", alfrescoRepository.findOne(caseLogisticUnitEntity.getCompoList().get(0).getProduct()).getName());
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