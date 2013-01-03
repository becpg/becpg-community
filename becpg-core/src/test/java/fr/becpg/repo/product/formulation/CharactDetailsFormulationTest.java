/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Assert;
import org.junit.Test;

import com.ibm.icu.text.DecimalFormat;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.AbstractFinishedProductTest;
import fr.becpg.repo.product.data.CharactDetails;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.DeclarationType;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListUnit;
import fr.becpg.repo.web.scripts.product.CharactDetailsHelper;

/**
 * The Class FormulationTest.
 * 
 * @author querephi
 */
public class CharactDetailsFormulationTest extends AbstractFinishedProductTest {

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
	public void testFormulateCharactDetails() throws Exception {

		logger.info("testFormulateCharactDetails");

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				/*-- Create finished product --*/
				logger.debug("/*-- Create finished product --*/");
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 1");
				finishedProduct.setLegalName("Legal Produit fini 1");
				finishedProduct.setUnit(ProductUnit.kg);
				finishedProduct.setQty(2d);
				finishedProduct.setUnitPrice(12.4d);
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				CompoListDataItem item = new CompoListDataItem(null,(CompoListDataItem) null, 1d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail,
						localSF1NodeRef);
				
				compoList.add(item);
				compoList.add(new CompoListDataItem(null, item, 1d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare,
						rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, item, 2d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail,
						rawMaterial2NodeRef));
				item = new CompoListDataItem(null, (CompoListDataItem)null, 1d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail,
						localSF2NodeRef);
				compoList.add(item);
				compoList.add(new CompoListDataItem(null, item, 3d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare,
						rawMaterial3NodeRef));
				compoList.add(new CompoListDataItem(null, item, 3d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Omit,
						rawMaterial4NodeRef));
				finishedProduct.setCompoList(compoList);

				NodeRef finishedProductNodeRef = alfrescoRepository.create(testFolderNodeRef, finishedProduct).getNodeRef();

				/*-- Formulate product --*/
				logger.debug("/*-- Formulate details --*/");
				CharactDetails ret = productService.formulateDetails(finishedProductNodeRef, BeCPGModel.TYPE_NUTLIST,
						"nutList", null);

				Assert.assertNotNull(ret);

				System.out.println(CharactDetailsHelper.toJSONObject(ret, nodeService).toString(3));

				return null;

			}
		}, false, true);

	}
	
	/**
	 * Test formulate product and check cost details
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testCalculateCostDetails() throws Exception{
		   
		logger.info("testCalculateCostDetails");
		
		final NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			public NodeRef execute() throws Throwable {					   							
					
				/*
				 * Prepare packaging 
				 */
				
				/*-- Packaging material 1 --*/					
				PackagingMaterialData packagingMaterial1 = new PackagingMaterialData();
				packagingMaterial1.setName("Packaging material 1");
				packagingMaterial1.setLegalName("Legal Packaging material 1");
				//costList
				List<CostListDataItem> costList = new ArrayList<CostListDataItem>();
				costList.add(new CostListDataItem(null, 3d, "€/P", null, pkgCost1, false));
				costList.add(new CostListDataItem(null, 2d, "€/P", null, pkgCost2, false));
				packagingMaterial1.setCostList(costList);					
				packagingMaterial1NodeRef = alfrescoRepository.create(testFolderNodeRef, packagingMaterial1).getNodeRef();
				
				/*-- Packaging material 2 --*/					
				PackagingMaterialData packagingMaterial2 = new PackagingMaterialData();
				packagingMaterial2.setName("Packaging material 2");
				packagingMaterial2.setLegalName("Legal Packaging material 2");
				//costList
				costList.clear();
				costList.add(new CostListDataItem(null, 1d, "€/m", null, pkgCost1, false));
				costList.add(new CostListDataItem(null, 2d, "€/m", null, pkgCost2, false));
				packagingMaterial2.setCostList(costList);					
				packagingMaterial2NodeRef = alfrescoRepository.create(testFolderNodeRef, packagingMaterial2).getNodeRef();
				
				/*-- Packaging material 1 --*/					
				PackagingMaterialData packagingMaterial3 = new PackagingMaterialData();
				packagingMaterial3.setName("Packaging material 3");
				packagingMaterial3.setLegalName("Legal Packaging material 3");
				//costList
				costList.clear();
				costList.add(new CostListDataItem(null, 1d, "€/P", null, pkgCost1, false));
				costList.add(new CostListDataItem(null, 2d, "€/P", null, pkgCost2, false));
				packagingMaterial3.setCostList(costList);					
				packagingMaterial3NodeRef = alfrescoRepository.create(testFolderNodeRef, packagingMaterial3).getNodeRef();
				
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 1");
				finishedProduct.setLegalName("Legal Produit fini 1");
				finishedProduct.setUnit(ProductUnit.kg);
				finishedProduct.setQty(2d);
				List<PackagingListDataItem> packagingList = new ArrayList<PackagingListDataItem>();
				packagingList.add(new PackagingListDataItem(null, 1d, PackagingListUnit.P, PACKAGING_PRIMAIRE, true, packagingMaterial1NodeRef));
				packagingList.add(new PackagingListDataItem(null, 3d, PackagingListUnit.m, PACKAGING_PRIMAIRE, true, packagingMaterial2NodeRef));
				packagingList.add(new PackagingListDataItem(null, 8d, PackagingListUnit.PP, PACKAGING_TERTIAIRE, true, packagingMaterial3NodeRef));
				finishedProduct.setPackagingList(packagingList);		
				
				
				/*
				 * Composition
				 */				
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				CompoListDataItem item = new CompoListDataItem(null, (CompoListDataItem)null, 1d, 0d, 0d, CompoListUnit.kg, 10d, DeclarationType.Detail, localSF1NodeRef);
				
				compoList.add(item);
				compoList.add(new CompoListDataItem(null, item, 1d, 0d, 0d, CompoListUnit.kg, 5d, DeclarationType.Declare, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, item, 2d, 0d, 0d, CompoListUnit.kg, 10d, DeclarationType.Detail, rawMaterial2NodeRef));
				 item = new CompoListDataItem(null, (CompoListDataItem)null, 1d, 0d, 0d, CompoListUnit.kg, 20d, DeclarationType.Detail, localSF2NodeRef);
				compoList.add(item);
				compoList.add(new CompoListDataItem(null,item, 3d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
				compoList.add(new CompoListDataItem(null, item, 3d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Omit, rawMaterial4NodeRef));
				finishedProduct.setCompoList(compoList);
				return alfrescoRepository.create(testFolderNodeRef, finishedProduct).getNodeRef();				
				
			}},false,true);
	   
	   transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				//formulate Details
				List<NodeRef> costNodeRefs = new ArrayList<NodeRef>();			
				CharactDetails ret = productService.formulateDetails(finishedProductNodeRef, BeCPGModel.TYPE_COSTLIST,
						"costList", costNodeRefs);
				
				Assert.assertNotNull(ret);
				System.out.println(CharactDetailsHelper.toJSONObject(ret, nodeService).toString(3));
				
				//costs
				int checks = 0;
				DecimalFormat df = new DecimalFormat("0.####");
				for(Map.Entry<NodeRef, Map<NodeRef, Double>> kv : ret.getData().entrySet()){
					
					for(Map.Entry<NodeRef, Double> kv2 : kv.getValue().entrySet()){
						
						String trace = "cost: " + nodeService.getProperty(kv.getKey(), ContentModel.PROP_NAME) + "source: " + nodeService.getProperty(kv.getKey(), ContentModel.PROP_NAME) + " - value: " + kv.getValue();
						logger.debug(trace);
						
						//cost1
						if(kv.getKey().equals(cost1)){
							
							if(kv2.getKey().equals(rawMaterial1NodeRef)){
							
								checks++;
								assertEquals("cost.getValue() == 1.7325, actual values: " + trace, df.format(1.7325d), df.format(kv2.getValue()));
								//assertEquals("cost.getPercentage() == 36.5314, actual values: " + trace, df.format(36.5314), df.format(kv2.getPercentage()));
							}
							else if(kv2.getKey().equals(rawMaterial2NodeRef)){
								
								checks++;
								assertEquals("cost.getValue() == 1.21, actual values: " + trace, df.format(1.21d), df.format(kv2.getValue()));
								//assertEquals("cost.getPercentage() == 25.5140, actual values: " + trace, df.format(25.5140), df.format(kv2.getPercentage()));
							}
							else if(kv2.getKey().equals(rawMaterial3NodeRef)){
								
								checks++;
								assertEquals("cost.getValue() == 1.8, actual values: " + trace, df.format(1.8d), df.format(kv2.getValue()));
								//assertEquals("cost.getPercentage() == 37.9547, actual values: " + trace, df.format(37.9547), df.format(kv2.getPercentage()));
							}	
							else{
								checks++;
							}
						}
						
						//cost2
						else if(kv.getKey().equals(cost2)){
							
							if(kv2.getKey().equals(rawMaterial1NodeRef)){
							
								checks++;
								assertEquals("cost.getValue() == 1.155, actual values: " + trace, df.format(1.155d), df.format(kv2.getValue()));
								//assertEquals("cost.getPercentage() == 16.0976, actual values: " + trace, df.format(16.0976), df.format(kv2.getPercentage()));
							}
							else if(kv2.getKey().equals(rawMaterial2NodeRef)){
								
								checks++;
								assertEquals("cost.getValue() == 2.42, actual values: " + trace, df.format(2.42d), df.format(kv2.getValue()));
								//assertEquals("cost.getPercentage() == 33.7282, actual values: " + trace, df.format(33.7282), df.format(kv2.getPercentage()));
							}
							else if(kv2.getKey().equals(rawMaterial3NodeRef)){
								
								checks++;
								assertEquals("cost.getValue() == 3.6, actual values: " + trace, df.format(3.6d), df.format(kv2.getValue()));
								//assertEquals("cost.getPercentage() == 50.1742, actual values: " + trace, df.format(50.1742), df.format(kv2.getPercentage()));
							}	
							else{
								checks++;
							}
						}
						
						//pkgCost1
						else if(kv.getKey().equals(pkgCost1)){
							
							if(kv2.getKey().equals(packagingMaterial1NodeRef)){
								
								checks++;
								assertEquals("cost.getValue() == 1.5, actual values: " + trace, df.format(1.5d), df.format(kv2.getValue()));
								//assertEquals("cost.getPercentage() == 48.9796, actual values: " + trace, df.format(48.9796), df.format(kv2.getPercentage()));
							}						
							else if(kv2.getKey().equals(packagingMaterial2NodeRef)){
								
								checks++;
								assertEquals("cost.getValue() == 1.5, actual values: " + trace, df.format(1.5d), df.format(kv2.getValue()));
								//assertEquals("cost.getPercentage() == 48.9796, actual values: " + trace, df.format(48.9796), df.format(kv2.getPercentage()));
							}						
							else if(kv2.getKey().equals(packagingMaterial3NodeRef)){
								
								checks++;
								assertEquals("cost.getValue() == 0.0625, actual values: " + trace, df.format(0.0625d), df.format(kv2.getValue()));
								//assertEquals("cost.getPercentage() == 2.0408, actual values: " + trace, df.format(2.0408), df.format(kv2.getPercentage()));
							}
							else{
								checks++;
							}
						}		
						
						//pkgCost2
						else if(kv.getKey().equals(pkgCost2)){
							
							if(kv2.getKey().equals(packagingMaterial1NodeRef)){
								
								checks++;
								assertEquals("cost.getValue() == 1, actual values: " + trace, df.format(1d), df.format(kv2.getValue()));
								//assertEquals("cost.getPercentage() == 24.2424, actual values: " + trace, df.format(24.2424), df.format(kv2.getPercentage()));
							}
							else if(kv2.getKey().equals(packagingMaterial2NodeRef)){
								
								checks++;
								assertEquals("cost.getValue() == 3, actual values: " + trace, df.format(3d), df.format(kv2.getValue()));
								//assertEquals("cost.getPercentage() == 72.7273, actual values: " + trace, df.format(72.7273), df.format(kv2.getPercentage()));
							}						
							else if(kv2.getKey().equals(packagingMaterial3NodeRef)){
								
								checks++;
								assertEquals("cost.getValue() == 0.125, actual values: " + trace, df.format(0.125d), df.format(kv2.getValue()));
								//assertEquals("cost.getPercentage() == 3.0303, actual values: " + trace, df.format(3.0303), df.format(kv2.getPercentage()));
							}
							else{
								checks++;
							}
						}
						else{
							checks++;
						}
					}
					
				}
				
				assertEquals("Verify checks done", 12, checks);
							
				return null;

			}
		}, false, true);
		   
	   }

}