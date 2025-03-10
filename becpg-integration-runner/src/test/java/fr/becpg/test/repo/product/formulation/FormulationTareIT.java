/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.TareUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class FormulationTareIT extends AbstractFinishedProductTest {

	protected static final Log logger = LogFactory.getLog(FormulationTareIT.class);

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

		final NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

				logger.info("/*-- Create finished product --*/");
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 1");
				finishedProduct.setLegalName("Legal Produit fini 1");
				finishedProduct.setUnit(ProductUnit.kg);
				finishedProduct.setQty(1d);
				finishedProduct.setDensity(1d);
				List<CompoListDataItem> compoList = new ArrayList<>();
				compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial5NodeRef));// 90g
				compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.P).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial5NodeRef));// 9g
				compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.lb).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial5NodeRef));// 9 * 0.453592 / 0.1 = 40.8233
				compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.oz).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial5NodeRef));// 2.5514 g
				finishedProduct.getCompoListView().setCompoList(compoList);

				List<PackagingListDataItem> packList = new ArrayList<>();
				packList.add(PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.P).withPkgLevel(PackagingLevel.Primary).withIsMaster(true).withProduct(packagingMaterial1NodeRef)
);// 15g
				packList.add(PackagingListDataItem.build().withQty(2d).withUnit(ProductUnit.P).withPkgLevel(PackagingLevel.Primary).withIsMaster(true).withProduct(packagingMaterial2NodeRef)
);// 2*5g
				packList.add(PackagingListDataItem.build().withQty(3d).withUnit(ProductUnit.g).withPkgLevel(PackagingLevel.Primary).withIsMaster(true).withProduct(packagingMaterial3NodeRef)
);// 3g
				packList.add(PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.P).withPkgLevel(PackagingLevel.Primary).withIsMaster(true).withProduct(packagingMaterial4NodeRef)
);// 50g
				packList.add(PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.oz).withPkgLevel(PackagingLevel.Primary).withIsMaster(true).withProduct(packagingMaterial5NodeRef)
);// 28.349523125g
				packList.add(PackagingListDataItem.build().withQty(10d).withUnit(ProductUnit.mL).withPkgLevel(PackagingLevel.Primary).withIsMaster(true).withProduct(packagingMaterial4NodeRef)
);// 0.5
				packList.add(PackagingListDataItem.build().withQty(0.2d).withUnit(ProductUnit.L).withPkgLevel(PackagingLevel.Primary).withIsMaster(true).withProduct(packagingMaterial6NodeRef)
);// 0.2 but was 0
				
				finishedProduct.getPackagingListView().setPackagingList(packList);
				
				return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

			}, false, true);
		
		final Double compoTare = 90d + 9d + 40.8233d + 2.5514d;
		final Double packTare = 15d +  2*5d + 3d + 50d + 28.349523125d + 0.5d + 0.2d;

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

				productService.formulate(finishedProductNodeRef);
				ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);

				DecimalFormat df = new DecimalFormat("0.####");
				assertEquals(df.format(compoTare + packTare), df.format(formulatedProduct.getTare()));
				assertEquals(TareUnit.g, formulatedProduct.getTareUnit());
				return null;

		}, false, true);
		
		final NodeRef finishedProduct2NodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			
			logger.info("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 2");
			finishedProduct.setLegalName("Legal Produit fini 2");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(1d);
			finishedProduct.setDensity(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.P).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(finishedProductNodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);
			
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
	
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
		
				productService.formulate(finishedProduct2NodeRef);
				ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProduct2NodeRef);
				
				DecimalFormat df = new DecimalFormat("0.####");
				assertEquals(df.format(compoTare + packTare), df.format(formulatedProduct.getTare()));
				assertEquals(TareUnit.g, formulatedProduct.getTareUnit());
				return null;
		
		}, false, true);
		
		final NodeRef finishedProduct3NodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			
			logger.info("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 3");
			finishedProduct.setLegalName("Legal Produit fini 3");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(1d);
			finishedProduct.setDensity(1d);
			finishedProduct.setDropPackagingOfComponents(true);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.P).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(finishedProductNodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);
			
			List<PackagingListDataItem> packList = new ArrayList<>();
			packList.add(PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.P).withPkgLevel(PackagingLevel.Primary).withIsMaster(true).withProduct(packagingMaterial1NodeRef)
);// 15g
			packList.add(PackagingListDataItem.build().withQty(2d).withUnit(ProductUnit.P).withPkgLevel(PackagingLevel.Primary).withIsMaster(true).withProduct(packagingMaterial2NodeRef)
);// 2*5g
			packList.add(PackagingListDataItem.build().withQty(3d).withUnit(ProductUnit.g).withPkgLevel(PackagingLevel.Primary).withIsMaster(true).withProduct(packagingMaterial3NodeRef)
);// 3g
			packList.add(PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.P).withPkgLevel(PackagingLevel.Primary).withIsMaster(true).withProduct(packagingMaterial4NodeRef)
);// 50g
			packList.add(PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.oz).withPkgLevel(PackagingLevel.Primary).withIsMaster(true).withProduct(packagingMaterial5NodeRef)
);// 28.349523125g
			packList.add(PackagingListDataItem.build().withQty(10d).withUnit(ProductUnit.mL).withPkgLevel(PackagingLevel.Primary).withIsMaster(true).withProduct(packagingMaterial4NodeRef)
);// 0.5
			packList.add(PackagingListDataItem.build().withQty(0.2d).withUnit(ProductUnit.L).withPkgLevel(PackagingLevel.Primary).withIsMaster(true).withProduct(packagingMaterial6NodeRef)
);// 0.2 but was 0
			

			finishedProduct.getPackagingListView().setPackagingList(packList);

			
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
	
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
		
				productService.formulate(finishedProduct3NodeRef);
				ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProduct3NodeRef);
		
				DecimalFormat df = new DecimalFormat("0.####");
				assertEquals(df.format(packTare), df.format(formulatedProduct.getTare()));
				assertEquals(TareUnit.g, formulatedProduct.getTareUnit());
				return null;
		
		}, false, true);
		
		final NodeRef finishedProduct4NodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			
			logger.info("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 4");
			finishedProduct.setLegalName("Legal Produit fini 4");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(1d);
			finishedProduct.setDensity(1d);
			
			List<PackagingListDataItem> packList = new ArrayList<>();
			PackagingListDataItem p = PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.P).withPkgLevel(PackagingLevel.Primary).withIsMaster(true).withProduct(packagingMaterial1NodeRef)
;
			p.setIsRecycle(true);
			packList.add(p);// 15g
			
			finishedProduct.getPackagingListView().setPackagingList(packList);
			
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
	
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

		
				productService.formulate(finishedProduct4NodeRef);
				ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProduct4NodeRef);
		
				DecimalFormat df = new DecimalFormat("0.####");
				assertEquals(df.format(15d), df.format(formulatedProduct.getTare()));
				assertEquals(TareUnit.g, formulatedProduct.getTareUnit());
				
				assertEquals(0d, formulatedProduct.getUnitTotalCost());
				return null;
		

		}, false, true);
	

	}

}
