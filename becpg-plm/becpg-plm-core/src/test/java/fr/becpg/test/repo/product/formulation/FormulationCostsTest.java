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
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.ClientData;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.CompoListUnit;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.PackagingListUnit;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.TareUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

/**
 * Test costs of template are taken in account
 * @author quere
 *
 */
public class FormulationCostsTest extends AbstractFinishedProductTest {

	protected static final Log logger = LogFactory.getLog(FormulationCostsTest.class);
	
	@Autowired
	protected AlfrescoRepository<ClientData> clientRepository;

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

		final ProductData entityTpl = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<ProductData>() {
			public ProductData execute() throws Throwable {

				// template
				FinishedProductData templateFinishedProduct = new FinishedProductData();
				templateFinishedProduct.setName("Template Produit fini");
				List<CostListDataItem> costList = new LinkedList<>();
				costList.add(new CostListDataItem(null, null, "€/kg", null, parentCost, false));
				costList.add(new CostListDataItem(null, 12d, "€/kg", 24d, cost1, true));	
				costList.get(1).setParent(costList.get(0));
				costList.add(new CostListDataItem(null, 16d, "€/P", 24d, cost2, false));
				costList.get(2).setParent(costList.get(0));
				List<NodeRef> plants = new ArrayList<>();
				plants.add(plant1);
				costList.add(new CostListDataItem(null, 2000d, "€/Pal", 2400d, cost3, false, plants, null, null));
				plants = new ArrayList<>();
				plants.add(plant2);
				costList.add(new CostListDataItem(null, 4000d, "€/Pal", 4400d, cost3, false, plants, null, null));
				templateFinishedProduct.setCostList(costList);
				ProductData entityTpl = alfrescoRepository.create(getTestFolderNodeRef(), templateFinishedProduct);
				nodeService.addAspect(entityTpl.getNodeRef(), BeCPGModel.ASPECT_ENTITY_TPL, null);
				
				return entityTpl;
			}
		}, false, true);
		
		final NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {
		
				// Client
				ClientData client = new ClientData();
				client.setName("client");				
				List<CostListDataItem> costList = new ArrayList<>();
				costList.add(new CostListDataItem(new CostListDataItem(null, 1d, "€/kg", 3d, cost4, false)));
				client.setCostList(costList);
				client = clientRepository.create(getTestFolderNodeRef(), client);
				
				// product
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 1");
				finishedProduct.setUnit(ProductUnit.kg);
				finishedProduct.setQty(2d);
				finishedProduct.setEntityTpl(entityTpl);
				List<NodeRef> plants = new ArrayList<>();
				plants.add(plant1);
				finishedProduct.setPlants(plants);
				
				List<PackagingListDataItem> packList = new ArrayList<>();
				packList.add(new PackagingListDataItem(null, 25d, PackagingListUnit.PP, PackagingLevel.Secondary, true, packagingKit1NodeRef));			
				finishedProduct.getPackagingListView().setPackagingList(packList);
				
				finishedProduct =  (FinishedProductData)alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct);
				// assoc is readonly
				ArrayList<NodeRef> clientNodeRefs = new ArrayList<>();
				clientNodeRefs.add(client.getNodeRef());
				associationService.update(finishedProduct.getNodeRef(), PLMModel.ASSOC_CLIENTS, clientNodeRefs);
								
				return finishedProduct.getNodeRef();

			}
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {
				
				productService.formulate(finishedProductNodeRef);
				ProductData formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef);
				
				assertEquals(5, formulatedProduct.getCostList().size());
				assertEquals(TareUnit.g, formulatedProduct.getTareUnit());
				
				for(CostListDataItem c : formulatedProduct.getCostList()){
					String trace = "cost: " + nodeService.getProperty(c.getCost(), BeCPGModel.PROP_CHARACT_NAME) + " - value: " + c.getValue()
							+ " - unit: " + c.getUnit()
							+ " level: " + c.getDepthLevel();
					logger.info(trace);
					
					assertEquals("€/kg", c.getUnit());
					if(c.getCost().equals(parentCost)){
						assertEquals(20d, c.getValue());
						assertEquals(36d, c.getMaxi());
					}
					else if(c.getCost().equals(cost1)){
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
					else if(c.getCost().equals(cost4)){
						assertEquals(1d, c.getValue());
						assertEquals(3d, c.getMaxi());
					}		
					else{
						assertFalse(true);
					}
				}				
				
				assertEquals(44d, formulatedProduct.getUnitTotalCost());
				
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
					else if(c.getCost().equals(cost4)){
						c.setValue(20d);
					}					
				}
				
				alfrescoRepository.save(formulatedProduct);
				productService.formulate(finishedProductNodeRef);
				formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef);
								
				for(CostListDataItem c : formulatedProduct.getCostList()){
					
					String trace = "cost: " + nodeService.getProperty(c.getCost(), BeCPGModel.PROP_CHARACT_NAME) + " - value: " + c.getValue()
							+ " - unit: " + c.getUnit();
					logger.info(trace);
					
					assertEquals("€/kg", c.getUnit());
					if(c.getCost().equals(parentCost)){
						assertEquals(19d, c.getValue());
						assertEquals(36d, c.getMaxi());
					}
					else if(c.getCost().equals(cost1)){
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
					else if(c.getCost().equals(cost4)){
						assertEquals(1d, c.getValue());
						assertEquals(3d, c.getMaxi());
					}					
					else{
						assertFalse(true);
					}
				}	
				
				assertEquals(42d, formulatedProduct.getUnitTotalCost());
							
				return null;

			}
		}, false, true);

	}
	
	@Test
	public void testFormulationCostsWithSimulation() throws Exception {
		

		final NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {
		
				/*-- Create finished product --*/
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 1");
				finishedProduct.setUnit(ProductUnit.kg);
				finishedProduct.setQty(2d);
				List<CompoListDataItem> compoList = new ArrayList<>();
				compoList.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(0), null, 1d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(0), null, 2d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(3), null, 3d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(3), null, 3d, CompoListUnit.kg, 0d, DeclarationType.Omit, rawMaterial4NodeRef));
				finishedProduct.getCompoListView().setCompoList(compoList);
		
				List<CostListDataItem> costList = new LinkedList<>();
				costList.add(new CostListDataItem(null, null, null, null, cost1, null));
				costList.add(new CostListDataItem(null, null, null, null, cost2, null));
				costList.add(new CostListDataItem(null, null, "€/kg", null, cost5, true));
				costList.get(2).setParent(costList.get(0));
				costList.get(2).setComponentNodeRef(rawMaterial1NodeRef);
				costList.get(2).setSimulatedValue(2d);
				finishedProduct.setCostList(costList);
				
				return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
				
			}
		}, false, true);
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				productService.formulate(finishedProductNodeRef);
				ProductData formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef);

				// costs
				int checks = 0;
				assertNotNull("CostList is null", formulatedProduct.getCostList());
				for (CostListDataItem costListDataItem : formulatedProduct.getCostList()) {
					String trace = "cost: " + nodeService.getProperty(costListDataItem.getCost(), BeCPGModel.PROP_CHARACT_NAME) + " - value: " + costListDataItem.getValue()
							+ " - previous value: " + costListDataItem.getPreviousValue()
							+ " - future value: " + costListDataItem.getFutureValue()
							+ " - unit: " + costListDataItem.getUnit();
					logger.info(trace);
					if (costListDataItem.getCost().equals(cost1) && costListDataItem.getComponentNodeRef() == null) {
						assertEquals(3.5d, costListDataItem.getValue());
						assertEquals(2d, costListDataItem.getPreviousValue());
						assertEquals(8d, costListDataItem.getFutureValue());						
						assertEquals("€/kg", costListDataItem.getUnit());
						assertEquals(7d, costListDataItem.getValuePerProduct());
						checks++;
					}
					if (costListDataItem.getCost().equals(cost2)) {
						assertEquals(6.0d, costListDataItem.getValue());
						assertEquals(3d, costListDataItem.getPreviousValue());
						assertEquals(12d, costListDataItem.getFutureValue());
						assertEquals("€/kg", costListDataItem.getUnit());
						assertEquals(12d, costListDataItem.getValuePerProduct());
						checks++;
					}
					if (costListDataItem.getCost().equals(cost5) && costListDataItem.getComponentNodeRef() != null) {
						assertEquals(2d, costListDataItem.getSimulatedValue());
						assertEquals(-0.5d, costListDataItem.getValue());
						assertEquals("€/kg", costListDataItem.getUnit());
						assertEquals(-1d, costListDataItem.getValuePerProduct());
						checks++;
					}
				}
				assertEquals(3, checks);

				return null;

			}
		}, false, true);
	}

}
