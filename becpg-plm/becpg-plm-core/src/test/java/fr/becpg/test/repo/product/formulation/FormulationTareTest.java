/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.CompoListUnit;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.PackagingListUnit;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.TareUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class FormulationTareTest extends AbstractFinishedProductTest {

	protected static Log logger = LogFactory.getLog(FormulationTareTest.class);

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// create RM and lSF
		initParts();
	}

	/**
	 * Test formulate product.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testFormulationTare() throws Exception {

		logger.info("testFormulationFull");

		final NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				logger.info("/*-- Create finished product --*/");
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 1");
				finishedProduct.setLegalName("Legal Produit fini 1");
				finishedProduct.setUnit(ProductUnit.kg);
				finishedProduct.setQty(1d);
				finishedProduct.setDensity(1d);
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial5NodeRef));
				compoList.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.P, 0d, DeclarationType.Detail, rawMaterial5NodeRef));
				finishedProduct.getCompoListView().setCompoList(compoList);

				List<PackagingListDataItem> packList = new ArrayList<>();
				packList.add(new PackagingListDataItem(null, 1d, PackagingListUnit.P, PackagingLevel.Primary, true, packagingMaterial1NodeRef));
				packList.add(new PackagingListDataItem(null, 2d, PackagingListUnit.P, PackagingLevel.Primary, true, packagingMaterial2NodeRef));
				packList.add(new PackagingListDataItem(null, 3d, PackagingListUnit.g, PackagingLevel.Primary, true, packagingMaterial3NodeRef));
				finishedProduct.getPackagingListView().setPackagingList(packList);
				
				return alfrescoRepository.create(testFolderNodeRef, finishedProduct).getNodeRef();

			}
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				productService.formulate(finishedProductNodeRef);
				ProductData formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef);

				DecimalFormat df = new DecimalFormat("0.####");
				assertEquals(df.format(90d + 9d + 15d + 2*5d + 3d), df.format(formulatedProduct.getTare()));
				assertEquals(TareUnit.g, formulatedProduct.getTareUnit());
				return null;

			}
		}, false, true);

	}

}
