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

public class FormulationMultiLevelILTest extends AbstractFinishedProductTest {

	protected static final Log logger = LogFactory.getLog(FormulationMultiLevelILTest.class);

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// create RM and lSF
		initParts();
	}

	@Test
	public void testFormulationMultiLevelILTest() throws Exception{
		
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
				List<CompoListDataItem> compoList = new ArrayList<>();
				compoList.add(new CompoListDataItem(null, null, null, 3d, CompoListUnit.kg, 3d, DeclarationType.Declare, rawMaterial7NodeRef));
				compoList.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 1d, DeclarationType.Declare, rawMaterial6NodeRef));

				finishedProduct.getCompoListView().setCompoList(compoList);
				return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();				
				
			}},false,true);
	   
	   transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			public NodeRef execute() throws Throwable {	
	   
				productService.formulate(finishedProductNodeRef);
				
				return null;

			}},false,true);
	   
	   transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			public NodeRef execute() throws Throwable {	
	   
				ProductData formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef);		
				
				assertEquals(5, formulatedProduct.getIngList().size());
				assertEquals(ing5, formulatedProduct.getIngList().get(0).getIng());
				assertEquals(75d, formulatedProduct.getIngList().get(0).getQtyPerc());
				assertEquals(ing1, formulatedProduct.getIngList().get(1).getIng());
				assertEquals(52.5d, formulatedProduct.getIngList().get(1).getQtyPerc()); 
				assertEquals(ing4, formulatedProduct.getIngList().get(2).getIng());
				assertEquals(22,5d, formulatedProduct.getIngList().get(2).getQtyPerc());
				assertEquals(ing1, formulatedProduct.getIngList().get(3).getIng());
				assertEquals(20d, formulatedProduct.getIngList().get(3).getQtyPerc());
				assertEquals(ing2, formulatedProduct.getIngList().get(4).getIng());
				assertEquals(5d, formulatedProduct.getIngList().get(4).getQtyPerc());
				
				return null;

			}},false,true);
	   		   
	   }
	
	

}
