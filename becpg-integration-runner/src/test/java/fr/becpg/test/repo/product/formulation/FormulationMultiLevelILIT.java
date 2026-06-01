/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
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
package fr.becpg.test.repo.product.formulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class FormulationMultiLevelILIT extends AbstractFinishedProductTest {

	protected static final Log logger = LogFactory.getLog(FormulationMultiLevelILIT.class);

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// create RM and lSF
		initParts();
	}

	@Test
	public void testFormulationMultiLevelILTest() throws Exception {

		final NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Create finished product --*/
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 1");
			finishedProduct.setLegalName("Legal Produit fini 1");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(4d);
			finishedProduct.setDensity(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withQtyUsed(3d).withUnit(ProductUnit.kg).withLossPerc(3d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial7NodeRef));
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(1d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial6NodeRef));

			finishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			productService.formulate(finishedProductNodeRef);

			return null;

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);

			assertEquals(5, formulatedProduct.getIngList().size());
			assertEquals(ing5, formulatedProduct.getIngList().get(0).getIng());
			assertEquals(75d, formulatedProduct.getIngList().get(0).getQtyPerc());
			assertEquals(ing1, formulatedProduct.getIngList().get(1).getIng());
			assertEquals(52.5d, formulatedProduct.getIngList().get(1).getQtyPerc());
			assertEquals(ing4, formulatedProduct.getIngList().get(2).getIng());
			assertEquals(22, 5d, formulatedProduct.getIngList().get(2).getQtyPerc());
			assertEquals(ing1, formulatedProduct.getIngList().get(3).getIng());
			assertEquals(20d, formulatedProduct.getIngList().get(3).getQtyPerc());
			assertEquals(ing2, formulatedProduct.getIngList().get(4).getIng());
			assertEquals(5d, formulatedProduct.getIngList().get(4).getQtyPerc());

			return null;

		}, false, true);

	}
	
	@Test
	public void testCachedUpToDateInLaterFormulations() {
		List<NodeRef> nodeRefs = inWriteTx(() -> {
			RawMaterialData rm2 = new RawMaterialData();
			rm2.setName("RM2");
			NodeRef rm2NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rm2).getNodeRef();

			RawMaterialData rm1 = new RawMaterialData();
			rm1.setName("RM1");
			rm1.getCompoListView().setCompoList(Arrays.asList(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(rm2NodeRef)));
			NodeRef rm1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rm1).getNodeRef();

			FinishedProductData fp1 = new FinishedProductData();
			fp1.setName("FP1");
			fp1.getCompoListView().setCompoList(Arrays.asList(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(rm1NodeRef)));
			NodeRef fp1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), fp1).getNodeRef();

			return Arrays.asList(rm2NodeRef, rm1NodeRef, fp1NodeRef);
		});

		NodeRef rm2NodeRef = nodeRefs.get(0);
		NodeRef rm1NodeRef = nodeRefs.get(1);
		NodeRef fp1NodeRef = nodeRefs.get(2);

		inWriteTx(() -> {
			L2CacheSupport.doInCacheContext(
					() -> AuthenticationUtil
							.runAsSystem(() -> formulationService.formulate(fp1NodeRef, FormulationService.DEFAULT_CHAIN_ID)),
					false, true);
			return true;
		});

		long initialRm1FormulatedTime = inWriteTx(() -> {
			RawMaterialData rm1 = (RawMaterialData) alfrescoRepository.findOne(rm1NodeRef);
			assertNotNull(rm1.getFormulatedDate());
			long formTime = rm1.getFormulatedDate().getTime();

			RawMaterialData rm2 = (RawMaterialData) alfrescoRepository.findOne(rm2NodeRef);
			rm2.setModifiedDate(new java.util.Date(formTime + 10000));
			alfrescoRepository.save(rm2);

			return formTime;
		});

		inWriteTx(() -> {
			L2CacheSupport.doInCacheContext(
					() -> AuthenticationUtil
							.runAsSystem(() -> formulationService.formulate(fp1NodeRef, FormulationService.DEFAULT_CHAIN_ID)),
					false, true);
			return true;
		});

		inReadTx(() -> {
			RawMaterialData rm1 = (RawMaterialData) alfrescoRepository.findOne(rm1NodeRef);
			assertNotNull(rm1.getFormulatedDate());
			assertTrue("RM1 should have been reformulated because RM2 was changed!", rm1.getFormulatedDate().getTime() > initialRm1FormulatedTime);
			return null;
		});
	}

}
