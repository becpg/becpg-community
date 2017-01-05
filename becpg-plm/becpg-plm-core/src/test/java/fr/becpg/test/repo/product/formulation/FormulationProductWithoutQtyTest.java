/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG. 
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

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.CompoListUnit;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

/**
 * Formulate a product without qty
 * @author quere
 *
 */
public class FormulationProductWithoutQtyTest extends AbstractFinishedProductTest {

	protected static final Log logger = LogFactory.getLog(FormulationFullTest.class);
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		// create RM and lSF
		initParts();
	}
	
	/**
	 * Test the formulation of the costs and nuts in kg and g.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testFormulateCostAndNutOfProductInkgAndg() throws Exception{
		   
		logger.info("testFormulateCostAndNutOfProductInkgAndg");
		
		   transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
				public NodeRef execute() throws Throwable {					   				
								
				/*-- Create finished product --*/
				logger.debug("/*-- Create finished product --*/");
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 1");
				finishedProduct.setLegalName("Legal Produit fini 1");
				finishedProduct.setUnit(ProductUnit.kg);				
				List<CompoListDataItem> compoList = new ArrayList<>();
				compoList.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(0), null, 1d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(0), null, 2d, CompoListUnit.g, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(3), null, 3d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(3), null, 3d, CompoListUnit.kg, 0d, DeclarationType.Omit, rawMaterial4NodeRef));
				finishedProduct.getCompoListView().setCompoList(compoList);
				
				List<CostListDataItem> costList = new ArrayList<>();
				costList.add(new CostListDataItem(null, null, null, null, cost1, null));
				costList.add(new CostListDataItem(null, null, null, null, cost2, null));
				finishedProduct.setCostList(costList);

				List<NutListDataItem> nutList = new ArrayList<>();
				nutList.add(new NutListDataItem(null, null, null, null, null, null, nut1, null));
				nutList.add(new NutListDataItem(null, null, null, null, null, null, nut2, null));
				finishedProduct.setNutList(nutList);
				
				NodeRef finishedProductNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
				
				/*-- Formulate product --*/
				ProductData formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef);
				formulatedProduct = productService.formulate(formulatedProduct);
				
				logger.debug("getRecipeQtyUsed: " + formulatedProduct.getRecipeQtyUsed());
				logger.debug("getRecipeVolumeUsed: " + formulatedProduct.getRecipeVolumeUsed());
				assertEquals(4.002d,formulatedProduct.getRecipeQtyUsed());
				
				
				//costs
				int checks=0;
				DecimalFormat df = new DecimalFormat("0.000");
				assertNotNull("CostList is null", formulatedProduct.getCostList());
				for(CostListDataItem costListDataItem : formulatedProduct.getCostList()){
					String trace = "cost: " + nodeService.getProperty(costListDataItem.getCost(), BeCPGModel.PROP_CHARACT_NAME) + " - value: " + costListDataItem.getValue() + " - unit: " + costListDataItem.getUnit();
					logger.debug(trace);
					if(costListDataItem.getCost().equals(cost1)){
						assertEquals(df.format(1.499750125), df.format(costListDataItem.getValue()));
						assertEquals("€/kg", costListDataItem.getUnit());
						checks++;
					}
					if(costListDataItem.getCost().equals(cost2)){
						assertEquals(df.format(2), df.format(costListDataItem.getValue()));
						assertEquals("€/kg", costListDataItem.getUnit());
						checks++;
					}
				}
				assertEquals(2, checks);
				//nuts
				checks=0;
				assertNotNull("NutList is null", formulatedProduct.getNutList());
				for(NutListDataItem nutListDataItem : 	formulatedProduct.getNutList()){
					String trace = "nut: " + nodeService.getProperty(nutListDataItem.getNut(), BeCPGModel.PROP_CHARACT_NAME) + " - value: " + nutListDataItem.getValue() + " - unit: " + nutListDataItem.getUnit();
					logger.info(trace);
					if(nutListDataItem.getNut().equals(nut1)){
						assertEquals(df.format(1d), df.format(nutListDataItem.getValue()));
						assertEquals("kJ/100g", nutListDataItem.getUnit());
						assertEquals(GROUP1, nutListDataItem.getGroup());
						checks++;
					}
					if(nutListDataItem.getNut().equals(nut2)){
						assertEquals(df.format(2d), df.format(nutListDataItem.getValue()));
						assertEquals("kcal/100g", nutListDataItem.getUnit());
						assertEquals(GROUP2, nutListDataItem.getGroup());
						checks++;
					}
				}				
				
				return null;

				}},false,true);
		   
	   }
	
	/**
	 * Test the formulation of the costs and nuts in kg, g, mL and m.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testFormulateCostAndNutOfProductInkgAndgAndmLAndm() throws Exception{
		   
		logger.info("testFormulateCostAndNutOfProductInkgAndgAndmLAndm");
		
		   transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
				public NodeRef execute() throws Throwable {					   				
								
				/*-- Create finished product --*/
				logger.debug("/*-- Create finished product --*/");
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 1");
				finishedProduct.setLegalName("Legal Produit fini 1");
				finishedProduct.setUnit(ProductUnit.P);
				List<CompoListDataItem> compoList = new ArrayList<>();
				compoList.add(new CompoListDataItem(null, null, null, 42d, CompoListUnit.g, 0d, DeclarationType.Detail, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(0), null, 40d, CompoListUnit.g, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(0), null, 2d, CompoListUnit.mL, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, null, null, 30d, CompoListUnit.g, 0d, DeclarationType.Detail, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(3), null, 30d, CompoListUnit.g, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(3), null, 0.05d, CompoListUnit.P, 0d, DeclarationType.Omit, rawMaterial5NodeRef));
				finishedProduct.getCompoListView().setCompoList(compoList);				

				List<CostListDataItem> costList = new ArrayList<>();
				costList.add(new CostListDataItem(null, null, null, null, cost1, null));
				costList.add(new CostListDataItem(null, null, null, null, cost2, null));
				finishedProduct.setCostList(costList);

				List<NutListDataItem> nutList = new ArrayList<>();
				nutList.add(new NutListDataItem(null, null, null, null, null, null, nut1, null));
				nutList.add(new NutListDataItem(null, null, null, null, null, null, nut2, null));
				finishedProduct.setNutList(nutList);
							
				NodeRef finishedProductNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
				
				/*-- Formulate product --*/
				ProductData formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef);
				formulatedProduct = productService.formulate(formulatedProduct);
				
				logger.debug("getRecipeQtyUsed: " + formulatedProduct.getRecipeQtyUsed());
				logger.debug("getRecipeVolumeUsed: " + formulatedProduct.getRecipeVolumeUsed());
				DecimalFormat df = new DecimalFormat("0.000");
				assertEquals(df.format(0.072d),df.format(formulatedProduct.getRecipeQtyUsed()));
				
				logger.debug("unit of product formulated: " + finishedProduct.getUnit());				
				//costs
				int checks=0;
				assertNotNull("CostList is null", formulatedProduct.getCostList());
				for(CostListDataItem costListDataItem : formulatedProduct.getCostList()){
					String trace = "cost: " + nodeService.getProperty(costListDataItem.getCost(), BeCPGModel.PROP_CHARACT_NAME) + " - value: " + costListDataItem.getValue() + " - unit: " + costListDataItem.getUnit();
					logger.info(trace);

					if(costListDataItem.getCost().equals(cost1)){
						assertEquals("check cost", df.format(0.402d), df.format(costListDataItem.getValue()));
						assertEquals("check cost unit", "€/P", costListDataItem.getUnit());
						checks++;
					}
					if(costListDataItem.getCost().equals(cost2)){
						assertEquals("check cost", df.format(0.444d), df.format(costListDataItem.getValue()));
						assertEquals("check cost unit", "€/P", costListDataItem.getUnit());
						checks++;
					}
				}
				assertEquals(2, checks);
				//nuts
				checks = 0;
				assertNotNull("NutList is null", formulatedProduct.getNutList());
				for(NutListDataItem nutListDataItem : 	formulatedProduct.getNutList()){
					String trace = "nut: " + nodeService.getProperty(nutListDataItem.getNut(), BeCPGModel.PROP_CHARACT_NAME) + " - value: " + nutListDataItem.getValue() + " - unit: " + nutListDataItem.getUnit();
					logger.info(trace);
					if(nutListDataItem.getNut().equals(nut1)){
						assertEquals("check nut", df.format(1.0694444444444444d), df.format(nutListDataItem.getValue()));
						assertEquals("check nut unit", "kJ/100g", nutListDataItem.getUnit());
						assertEquals("must be group1", GROUP1, nutListDataItem.getGroup());
						checks++;
					}
					if(nutListDataItem.getNut().equals(nut2)){
						assertEquals("check nut", df.format(2.208333333333333d), df.format(nutListDataItem.getValue()));
						assertEquals("check nut unit", "kcal/100g", nutListDataItem.getUnit());
						assertEquals("must be group2", GROUP2, nutListDataItem.getGroup());
						checks++;
					}
				}				
				assertEquals(2, checks);
				
				return null;

				}},false,true);
		   
	   }
}
