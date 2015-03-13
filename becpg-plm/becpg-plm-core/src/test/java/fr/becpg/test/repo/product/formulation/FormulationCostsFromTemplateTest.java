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

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.PackagingListUnit;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.TareUnit;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

/**
 * Test costs of template are taken in account
 * @author quere
 *
 */
public class FormulationCostsFromTemplateTest extends AbstractFinishedProductTest {

	protected static Log logger = LogFactory.getLog(FormulationCostsFromTemplateTest.class);

	@Resource
	private AssociationService associationService;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// create RM and lSF
		initParts();
	}

	/**
	 * Test cost product.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testFormulationCostsFromTemplate() throws Exception {

		logger.info("testFormulationCostsFromTemplate");

		final NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				FinishedProductData templateFinishedProduct = new FinishedProductData();
				templateFinishedProduct.setName("Template Produit fini");
				List<CostListDataItem> costList = new ArrayList<>();
				costList.add(new CostListDataItem(null, 12d, "€/kg", 24d, cost1, true));							
				costList.add(new CostListDataItem(null, 16d, "€/P", 24d, cost2, false));
				List<NodeRef> plants = new ArrayList<>();
				plants.add(plant1);
				costList.add(new CostListDataItem(null, 2000d, "€/Pal", 2400d, cost3, false, plants));
				plants = new ArrayList<>();
				plants.add(plant2);
				costList.add(new CostListDataItem(null, 4000d, "€/Pal", 2400d, cost3, false, plants));
				templateFinishedProduct.setCostList(costList);
				ProductData entityTpl = alfrescoRepository.create(getTestFolderNodeRef(), templateFinishedProduct);
				nodeService.addAspect(entityTpl.getNodeRef(), BeCPGModel.ASPECT_ENTITY_TPL, null);
				
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 1");
				finishedProduct.setUnit(ProductUnit.kg);
				finishedProduct.setQty(2d);
				finishedProduct.setEntityTpl(entityTpl);
				plants.clear();
				plants.add(plant1);
				finishedProduct.setPlants(plants);
				
				List<PackagingListDataItem> packList = new ArrayList<>();
				packList.add(new PackagingListDataItem(null, 25d, PackagingListUnit.PP, PackagingLevel.Secondary, true, packagingKit1NodeRef));			
				finishedProduct.getPackagingListView().setPackagingList(packList);
				
				return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

			}
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {
				
				productService.formulate(finishedProductNodeRef);
				ProductData formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef);
				
				assertEquals(3, formulatedProduct.getCostList().size());
				assertEquals(TareUnit.g, formulatedProduct.getTareUnit());
				
				for(CostListDataItem c : formulatedProduct.getCostList()){
					assertEquals("€/kg", c.getUnit());
					if(c.getCost().equals(cost1)){
						assertEquals(12d, c.getValue());
						assertEquals(24d, c.getMaxi());
					}
					else if(c.getCost().equals(cost2)){
						assertEquals(8d, c.getValue());
						assertEquals(12d, c.getMaxi());
					}
					else if(c.getCost().equals(cost3)){
						// 1000 finished product on pallet
						assertEquals(1d, c.getValue());
						assertEquals(1.2d, c.getMaxi());
					}
					else{
						assertFalse(true);
					}
				}							
				
				// change data
				for(CostListDataItem c : formulatedProduct.getCostList()){
					if(c.getCost().equals(cost1)){
						c.setValue(11d);
					}
					else if(c.getCost().equals(cost2)){
						c.setValue(300d);
					}
					else if(c.getCost().equals(cost3)){
						c.setValue(20d);
					}
				}
				
				alfrescoRepository.save(formulatedProduct);
				productService.formulate(finishedProductNodeRef);
				formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef);
								
				for(CostListDataItem c : formulatedProduct.getCostList()){
					assertEquals("€/kg", c.getUnit());
					if(c.getCost().equals(cost1)){
						assertEquals(11d, c.getValue());
						assertEquals(24d, c.getMaxi());	
					}
					else if(c.getCost().equals(cost2)){
						assertEquals(8d, c.getValue());
						assertEquals(12d, c.getMaxi());
					}
					else if(c.getCost().equals(cost3)){
						// 1000 finished product on pallet
						assertEquals(1d, c.getValue());
						assertEquals(1.2d, c.getMaxi());
					}
					else{
						assertFalse(true);
					}
				}	
							
				return null;

			}
		}, false, true);

	}

}
