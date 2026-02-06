/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.entity.policy;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.VersionType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class CopyRestrictionServiceIT extends AbstractFinishedProductTest {

	@Autowired
	private EntityService entityService;
	
	@Autowired
	private EntityVersionService entityVersionService;
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		initParts();
	}

	@Test
	public void testPropertiesToReset() {
		try {
			
			inWriteTx(() -> {
				systemConfigurationService.updateConfValue("beCPG.copyOrBranch.propertiesToReset", "");
				return null;
			});
			
			NodeRef productNodeRef = inWriteTx(() -> {
				FinishedProductData productData = new FinishedProductData();
				productData.setParentNodeRef(getTestFolderNodeRef());
				productData.setName("Test Product");
				productData.setHierarchy1(HIERARCHY1_FROZEN_REF);
				productData.setLegalName("Original Legal Name");
				productData.setErpCode("007-TEST");
				productData.setIngList(List.of(IngListDataItem.build().withIngredient(ing1).withQtyPerc(10.0)));
				productData.setAllergenList(List.of(AllergenListDataItem.build().withAllergen(allergen1).withQtyPerc(5.0)));
				productData.setPlants(List.of(plant1));
				return alfrescoRepository.save(productData).getNodeRef();
			});
			
			NodeRef branchNodeRef1 = inWriteTx(() -> {
				return entityVersionService.createBranch(productNodeRef, getTestFolderNodeRef());
			});
			
			inReadTx(() -> {
				FinishedProductData branchedProductData = (FinishedProductData) alfrescoRepository.findOne(branchNodeRef1);
				assertEquals(HIERARCHY1_FROZEN_REF, branchedProductData.getHierarchy1());
				assertEquals("Original Legal Name", branchedProductData.getLegalName().getDefaultValue());
				assertEquals("007-TEST", branchedProductData.getErpCode());
				assertEquals(1, branchedProductData.getIngList().size());
				assertEquals(10.0, branchedProductData.getIngList().get(0).getQtyPerc(), 0.001);
				assertEquals(1, branchedProductData.getAllergenList().size());
				assertEquals(5.0, branchedProductData.getAllergenList().get(0).getQtyPerc(), 0.001);
				assertEquals(1, branchedProductData.getPlants().size());
				assertEquals(plant1, branchedProductData.getPlants().get(0));
				return null;
			});
			
			NodeRef copyNodeRef1 = inWriteTx(() -> {
				return entityService.createOrCopyFrom(productNodeRef, getTestFolderNodeRef(),
						PLMModel.TYPE_FINISHEDPRODUCT, "Copy of Test Product 1");
			});
			
			
			inReadTx(() -> {
				FinishedProductData copyProductData = (FinishedProductData) alfrescoRepository.findOne(copyNodeRef1);
				assertEquals(HIERARCHY1_FROZEN_REF, copyProductData.getHierarchy1());
				assertEquals("Original Legal Name", copyProductData.getLegalName().getDefaultValue());
				assertEquals("007-TEST", copyProductData.getErpCode());
				assertEquals(1, copyProductData.getIngList().size());
				assertEquals(10.0, copyProductData.getIngList().get(0).getQtyPerc(), 0.001);
				assertEquals(1, copyProductData.getAllergenList().size());
				assertEquals(5.0, copyProductData.getAllergenList().get(0).getQtyPerc(), 0.001);
				assertEquals(1, copyProductData.getPlants().size());
				assertEquals(plant1, copyProductData.getPlants().get(0));
				return null;
			});
			
			inWriteTx(() -> {
				systemConfigurationService.updateConfValue("beCPG.copyOrBranch.propertiesToReset", 
						"bcpg:erpCode,bcpg:plants,bcpg:ingList|bcpg:ingListQtyPerc,"
						+ "bcpg:allergenList|bcpg:allergenListQtyPerc|branch,bcpg:legalName|branchOnly,"
						+ "bcpg:productHierarchy1|copy");
				return null;
			});
			
			NodeRef branchNodeRef2 = inWriteTx(() -> {
				return entityVersionService.createBranch(productNodeRef, getTestFolderNodeRef());
			});
			
			inReadTx(() -> {
				FinishedProductData branchProductData = (FinishedProductData) alfrescoRepository.findOne(branchNodeRef2);
				assertNull(branchProductData.getErpCode());
				assertEquals(HIERARCHY1_FROZEN_REF, branchProductData.getHierarchy1());
				assertTrue(branchProductData.getLegalName() == null || branchProductData.getLegalName().getDefaultValue() == null);
				assertEquals(1, branchProductData.getIngList().size());
				assertNull(branchProductData.getIngList().get(0).getQtyPerc());
				assertEquals(1, branchProductData.getAllergenList().size());
				assertNull(branchProductData.getAllergenList().get(0).getQtyPerc());
				assertTrue(branchProductData.getPlants() == null || branchProductData.getPlants().isEmpty());
				return null;
			});
			
			NodeRef copyNodeRef2 = inWriteTx(() -> {
				return entityService.createOrCopyFrom(productNodeRef, getTestFolderNodeRef(),
						PLMModel.TYPE_FINISHEDPRODUCT, "Copy of Test Product 2");
			});
			
			inReadTx(() -> {
				FinishedProductData copyProductData = (FinishedProductData) alfrescoRepository.findOne(copyNodeRef2);
				assertNull(copyProductData.getHierarchy1());
				assertEquals("Original Legal Name", copyProductData.getLegalName().getDefaultValue());
				assertNull(copyProductData.getErpCode());
				assertEquals(1, copyProductData.getIngList().size());
				assertNull(copyProductData.getIngList().get(0).getQtyPerc());
				assertEquals(1, copyProductData.getAllergenList().size());
				assertEquals(5.0, copyProductData.getAllergenList().get(0).getQtyPerc(), 0.001);
				assertTrue(copyProductData.getPlants() == null || copyProductData.getPlants().isEmpty());
				return null;
			});
			
			inWriteTx(() -> {
				return entityVersionService.mergeBranch(branchNodeRef2, productNodeRef, VersionType.MAJOR, "test merge", false, false);
			});
			
			inReadTx(() -> {
				FinishedProductData productData = (FinishedProductData) alfrescoRepository.findOne(productNodeRef);
				assertEquals("007-TEST", productData.getErpCode());
				assertEquals(HIERARCHY1_FROZEN_REF, productData.getHierarchy1());
				assertTrue(productData.getLegalName() == null || productData.getLegalName().getDefaultValue() == null);
				assertEquals(1, productData.getIngList().size());
				assertNull(productData.getIngList().get(0).getQtyPerc());
				assertEquals(1, productData.getAllergenList().size());
				assertNull(productData.getAllergenList().get(0).getQtyPerc());
				assertEquals(1, productData.getPlants().size());
				assertEquals(plant1, productData.getPlants().get(0));
				return null;
			});

		} finally {
			inWriteTx(() -> {
				systemConfigurationService.resetConfValue("beCPG.copyOrBranch.propertiesToReset");
				return null;
			});
		}
	}
	
	@Test
	public void testTypesToReset() {
		try {
			
			inWriteTx(() -> {
				systemConfigurationService.updateConfValue("beCPG.copyOrBranch.typesToReset", "");
				return null;
			});
			
			NodeRef productNodeRef = inWriteTx(() -> {
				FinishedProductData productData = new FinishedProductData();
				productData.setParentNodeRef(getTestFolderNodeRef());
				productData.setName("Test Product");
				productData.setIngList(List.of(IngListDataItem.build().withIngredient(ing1).withQtyPerc(10.0)));
				productData.setAllergenList(List.of(AllergenListDataItem.build().withAllergen(allergen1).withQtyPerc(5.0)));
				productData.setNutList(List.of(NutListDataItem.build().withNut(nut1).withValue(2.0)));
				return alfrescoRepository.save(productData).getNodeRef();
			});
			
			NodeRef branchNodeRef1 = inWriteTx(() -> {
				return entityVersionService.createBranch(productNodeRef, getTestFolderNodeRef());
			});
			
			inReadTx(() -> {
				FinishedProductData branchProductData = (FinishedProductData) alfrescoRepository.findOne(branchNodeRef1);
				assertEquals(1, branchProductData.getIngList().size());
				assertEquals(10.0, branchProductData.getIngList().get(0).getQtyPerc(), 0.001);
				assertEquals(1, branchProductData.getAllergenList().size());
				assertEquals(5.0, branchProductData.getAllergenList().get(0).getQtyPerc(), 0.001);
				assertEquals(1, branchProductData.getNutList().size());
				assertEquals(2.0, branchProductData.getNutList().get(0).getValue(), 0.001);
				return null;
			});
			
			NodeRef copyNodeRef1 = inWriteTx(() -> {
				return entityService.createOrCopyFrom(productNodeRef, getTestFolderNodeRef(),
						PLMModel.TYPE_FINISHEDPRODUCT, "Copy of Test Product 1");
			});
			
			
			inReadTx(() -> {
				FinishedProductData copyProductData = (FinishedProductData) alfrescoRepository.findOne(copyNodeRef1);
				assertEquals(1, copyProductData.getIngList().size());
				assertEquals(10.0, copyProductData.getIngList().get(0).getQtyPerc(), 0.001);
				assertEquals(1, copyProductData.getAllergenList().size());
				assertEquals(5.0, copyProductData.getAllergenList().get(0).getQtyPerc(), 0.001);
				assertEquals(1, copyProductData.getNutList().size());
				assertEquals(2.0, copyProductData.getNutList().get(0).getValue(), 0.001);
				return null;
			});
			
			inWriteTx(() -> {
				systemConfigurationService.updateConfValue("beCPG.copyOrBranch.typesToReset", 
						"bcpg:ingList,bcpg:allergenList|branch,bcpg:nutList|copy");
				return null;
			});
			
			NodeRef branchNodeRef2 = inWriteTx(() -> {
				return entityVersionService.createBranch(productNodeRef, getTestFolderNodeRef());
			});
			
			inReadTx(() -> {
				FinishedProductData branchProductData = (FinishedProductData) alfrescoRepository.findOne(branchNodeRef2);
				assertTrue(branchProductData.getIngList() == null || branchProductData.getIngList().isEmpty());
				assertTrue(branchProductData.getAllergenList() == null || branchProductData.getAllergenList().isEmpty());
				assertEquals(1, branchProductData.getNutList().size());
				return null;
			});
			
			NodeRef copyNodeRef2 = inWriteTx(() -> {
				return entityService.createOrCopyFrom(productNodeRef, getTestFolderNodeRef(),
						PLMModel.TYPE_FINISHEDPRODUCT, "Copy of Test Product 2");
			});
			
			inReadTx(() -> {
				FinishedProductData copyProductData = (FinishedProductData) alfrescoRepository.findOne(copyNodeRef2);
				assertTrue(copyProductData.getIngList() == null || copyProductData.getIngList().isEmpty());
				assertEquals(1, copyProductData.getAllergenList().size());
				assertTrue(copyProductData.getNutList() == null || copyProductData.getNutList().isEmpty());
				return null;
			});
			
		} finally {
			inWriteTx(() -> {
				systemConfigurationService.resetConfValue("beCPG.copyOrBranch.typesToReset");
				return null;
			});
		}
	}
}
