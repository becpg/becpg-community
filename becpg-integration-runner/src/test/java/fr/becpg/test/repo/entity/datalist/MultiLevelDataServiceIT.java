/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.entity.datalist;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.datalist.MultiLevelDataListService;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.test.PLMBaseTestCase;

/**
 * The Class MultiLevelDataServiceTest.
 *
 * @author querephi
 */
public class MultiLevelDataServiceIT extends PLMBaseTestCase {

	
	private static final Log logger = LogFactory.getLog(MultiLevelDataServiceIT.class);

	@Autowired
	private MultiLevelDataListService multiLevelDataListService;

	private NodeRef rawMaterial1NodeRef = null;
	private NodeRef rawMaterial2NodeRef = null;
	private NodeRef lSF1NodeRef = null;
	private NodeRef lSF2NodeRef = null;
	private NodeRef lSF3NodeRef = null;
	private NodeRef lSF4NodeRef = null;
	private NodeRef finishedProductNodeRef = null;

	/**
	 * Test get Multilevel of the compoList
	 */
	@Test
	public void testGetMultiLevelCompoList() {

		logger.debug("testGetMultiLevelCompoList");

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Create raw material --*/
			logger.debug("/*-- Create raw material --*/");
			RawMaterialData rawMaterial1 = new RawMaterialData();
			rawMaterial1.setName("Raw material 1");
			rawMaterial1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial1).getNodeRef();
			RawMaterialData rawMaterial2 = new RawMaterialData();
			rawMaterial2.setName("Raw material 2");
			rawMaterial2NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial2).getNodeRef();
			LocalSemiFinishedProductData lSF1 = new LocalSemiFinishedProductData();
			lSF1.setName("Local semi finished 1");
			lSF1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), lSF1).getNodeRef();

			LocalSemiFinishedProductData lSF2 = new LocalSemiFinishedProductData();
			lSF2.setName("Local semi finished 2");
			lSF2NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), lSF2).getNodeRef();

			LocalSemiFinishedProductData lSF3 = new LocalSemiFinishedProductData();
			lSF3.setName("Local semi finished 3");
			lSF3NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), lSF3).getNodeRef();

			LocalSemiFinishedProductData lSF4 = new LocalSemiFinishedProductData();
			lSF4.setName("Local semi finished 4");
			lSF4NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), lSF4).getNodeRef();

			/*-- Create finished product --*/
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Finished Product");

			List<CompoListDataItem> compoList = new LinkedList<>();
			CompoListDataItem parent1 = CompoListDataItem.build().withParent(null).withQty(1d).withQtyUsed(1d).withUnit(ProductUnit.P).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(lSF1NodeRef);
			CompoListDataItem child1 = CompoListDataItem.build().withParent(parent1).withQty(1d).withQtyUsed(4d).withUnit(ProductUnit.P).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(lSF2NodeRef);
			CompoListDataItem child12 = CompoListDataItem.build().withParent(child1).withQty(3d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Omit).withProduct(rawMaterial1NodeRef);
			CompoListDataItem parent2 = CompoListDataItem.build().withParent(null).withQty(1d).withQtyUsed(4d).withUnit(ProductUnit.P).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(lSF3NodeRef);
			CompoListDataItem child2 = CompoListDataItem.build().withParent(parent2).withQty(3d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Omit).withProduct(rawMaterial2NodeRef);
			CompoListDataItem child21 = CompoListDataItem.build().withParent(parent2).withQty(3d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Omit).withProduct(lSF4NodeRef);

			compoList.add(parent1);
			compoList.add(child1);
			compoList.add(child12);
			compoList.add(parent2);
			compoList.add(child2);
			compoList.add(child21);
			finishedProduct.getCompoListView().setCompoList(compoList);

			finishedProductNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

			return null;
		}, false, true);

		waitForSolr();

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			final DataListFilter dataListFilter = new DataListFilter();
			dataListFilter.setDataType(PLMModel.TYPE_COMPOLIST);
			dataListFilter.updateMaxDepth(-1);
			dataListFilter.setEntityNodeRefs(Collections.singletonList(finishedProductNodeRef));

			MultiLevelListData mlld = multiLevelDataListService.getMultiLevelListData(dataListFilter);
			int checks = 0;

			assertNotNull(mlld);

			for (MultiLevelListData mlld2 : mlld.getTree().values()) {
				checks = doChecks(mlld2, checks);
			}

			assertEquals(6, checks);
			return null;

		}, false, true);
	}

	int doChecks(MultiLevelListData mlld2, int checks) {

		if (mlld2.getEntityNodeRef().equals(lSF1NodeRef)) {
			checks++;
			assertEquals(1, mlld2.getDepth());
		} else if (mlld2.getEntityNodeRef().equals(lSF2NodeRef)) {
			checks++;
			assertEquals(2, mlld2.getDepth());
		} else if (mlld2.getEntityNodeRef().equals(rawMaterial1NodeRef)) {
			checks++;
			assertEquals(3, mlld2.getDepth());
		} else if (mlld2.getEntityNodeRef().equals(lSF3NodeRef)) {
			checks++;
			assertEquals(1, mlld2.getDepth());
		} else if (mlld2.getEntityNodeRef().equals(rawMaterial2NodeRef)) {
			checks++;
			assertEquals(2, mlld2.getDepth());
		} else if (mlld2.getEntityNodeRef().equals(lSF4NodeRef)) {
			checks++;
			assertEquals(2, mlld2.getDepth());
		}

		if ((mlld2.getTree() != null) && !mlld2.getTree().values().isEmpty()) {
			logger.info("data list " + mlld2);
			for (MultiLevelListData mlld3 : mlld2.getTree().values()) {
				logger.info("nodeRef" + mlld3);
				checks = doChecks(mlld3, checks);
			}
		}

		return checks;
	}
}
