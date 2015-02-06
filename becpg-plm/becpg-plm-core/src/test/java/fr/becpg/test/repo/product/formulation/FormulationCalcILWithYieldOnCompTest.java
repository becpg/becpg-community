/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class FormulationCalcILWithYieldOnCompTest extends AbstractFinishedProductTest {

	protected static Log logger = LogFactory.getLog(FormulationCalcILWithYieldOnCompTest.class);

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// create RM and lSF
		initParts();
	}

	/**
	 * Calculate the ingList when a component has yield -> don't add water to ingList (#685)
	 * RM2 has 200% Yield so water is added in a sub process but not displayed
	 * @throws Exception
	 */
	@Test
	public void testCalculateIngListWithYieldOnComponents() throws Exception{
		
	   final NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			public NodeRef execute() throws Throwable {					   							
					
				/*-- Create finished product --*/
				logger.debug("/*-- Create finished product --*/");
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 1");
				finishedProduct.setLegalName("Legal Produit fini 1");
				finishedProduct.setUnit(ProductUnit.kg);
				finishedProduct.setQty(4d);
				finishedProduct.setDensity(1d);
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, (CompoListDataItem)null, null, 1d, CompoListUnit.kg, 10d, DeclarationType.Declare, rawMaterial1NodeRef));
				
				CompoListDataItem temp = new CompoListDataItem(null, (CompoListDataItem)null, null, 3d, CompoListUnit.kg, 10d, DeclarationType.Declare, rawMaterial2NodeRef);
				temp.setYieldPerc(200d);
				compoList.add(temp); 

				finishedProduct.getCompoListView().setCompoList(compoList);
				return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();				
				
			}},false,true);
	   
	   transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			public NodeRef execute() throws Throwable {	
	   
//				productService.formulate(finishedProductNodeRef);
				ProductData formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef);					
				formulatedProduct = productService.formulate(formulatedProduct);
				
				DecimalFormat df = new DecimalFormat("0.00");				
				assertEquals(1d, formulatedProduct.getCompoListView().getCompoList().get(0).getQty());
				assertEquals(1.5d, formulatedProduct.getCompoListView().getCompoList().get(1).getQty());
				
//				RM1 : 1 * (100/3 ing1 + 200/3 ing2)
//				RM2 : 3 * (100/4 ing1 + 300/4 ing2)
//				
//				ing 1 : 27,083333333
//				ing 2 : 72,916666667
				
				assertEquals(100d, formulatedProduct.getYield());
				assertEquals(2, formulatedProduct.getIngList().size());
				assertEquals(ing2, formulatedProduct.getIngList().get(0).getIng());
				assertEquals(ing1, formulatedProduct.getIngList().get(1).getIng());
				
				logger.info("###df.format(formulatedProduct.getIngList().get(0).getQtyPerc()) " + df.format(formulatedProduct.getIngList().get(0).getQtyPerc()));
				logger.info("###df.format(formulatedProduct.getIngList().get(1).getQtyPerc()) " + df.format(formulatedProduct.getIngList().get(1).getQtyPerc()));
				assertEquals(df.format(72.916666667d), df.format(formulatedProduct.getIngList().get(0).getQtyPerc()));
				assertEquals(df.format(27.083333333d), df.format(formulatedProduct.getIngList().get(1).getQtyPerc()));
				
				return null;

			}},false,true);
	   
	   final NodeRef finishedProduct2NodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			public NodeRef execute() throws Throwable {					   							
					
				/*-- Create finished product --*/
				logger.debug("/*-- Create finished product --*/");
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 2");
				finishedProduct.setLegalName("Legal Produit fini 2");
				finishedProduct.setUnit(ProductUnit.kg);
				finishedProduct.setQty(2d);
				finishedProduct.setDensity(1d);
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, (CompoListDataItem)null, null, 65d, CompoListUnit.g, null, DeclarationType.Declare, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(0), null, 80d, CompoListUnit.Perc, null, DeclarationType.Declare, rawMaterial1NodeRef));
				CompoListDataItem temp = new CompoListDataItem(null, compoList.get(0), null, 10d, CompoListUnit.Perc, null, DeclarationType.Declare, rawMaterial2NodeRef);
				temp.setYieldPerc(200d);
				compoList.add(temp);
				compoList.add(new CompoListDataItem(null, compoList.get(0), null, 10d, CompoListUnit.Perc, null, DeclarationType.Declare, rawMaterial3NodeRef));				
				
				finishedProduct.getCompoListView().setCompoList(compoList);
				return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();				
				
			}},false,true);
	   
	   transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			public NodeRef execute() throws Throwable {	
	   
				/*-- Formulate product --*/
				logger.debug("/*-- Formulate product --*/");
				productService.formulate(finishedProduct2NodeRef);
				
				/*-- Verify formulation --*/
				logger.debug("/*-- Verify formulation --*/");
				ProductData formulatedProduct = alfrescoRepository.findOne(finishedProduct2NodeRef);				
				DecimalFormat df = new DecimalFormat("0.00");
				
				assertEquals(df.format(0.065d), df.format(formulatedProduct.getCompoListView().getCompoList().get(0).getQty()));
				assertEquals(df.format(100d), df.format(formulatedProduct.getCompoListView().getCompoList().get(0).getYieldPerc()));
				
				assertEquals(df.format(0.052d), df.format(formulatedProduct.getCompoListView().getCompoList().get(1).getQty()));
				assertEquals(df.format(0.00375d), df.format(formulatedProduct.getCompoListView().getCompoList().get(2).getQty())); // 3.75g because of yield of 200%
				assertEquals(df.format(0.0065d), df.format(formulatedProduct.getCompoListView().getCompoList().get(3).getQty()));
				
				
//				65 g (80% RM1(100/3 ing1 + 200/3 ing2), 10% RM2(100/4 ing1 + 300/4 ing2), 10% RM3( 100 ing3))
//				52g RM1(100/3 ing1 + 200/3 ing2), 6.5g RM2(100/4 ing1 + 300/4 ing2), 6.5g RM3( 100 ing3))
//
//				ing1 : 29,166666667
//				ing2 : 60,833333333
//				ing3 : 10
				
				assertEquals(3, formulatedProduct.getIngList().size());
				assertEquals(ing2, formulatedProduct.getIngList().get(0).getIng());
				assertEquals(ing1, formulatedProduct.getIngList().get(1).getIng());
				assertEquals(ing3, formulatedProduct.getIngList().get(2).getIng());
				
				assertEquals(df.format(60.833333333d), df.format(formulatedProduct.getIngList().get(0).getQtyPerc()));
				assertEquals(df.format(29.166666667d), df.format(formulatedProduct.getIngList().get(1).getQtyPerc()));
				assertEquals(df.format(10d), df.format(formulatedProduct.getIngList().get(2).getQtyPerc()));
				
				
				return null;

			}},false,true);
		   
	   }

}
