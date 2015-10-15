/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.product.formulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ibm.icu.text.DecimalFormat;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.product.data.CharactDetails;
import fr.becpg.repo.product.data.CharactDetailsValue;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.constraints.CompoListUnit;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.PackagingListUnit;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.web.scripts.product.CharactDetailsHelper;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

/**
 * The Class FormulationTest.
 * 
 * @author querephi
 */
public class CharactDetailsFormulationTest extends AbstractFinishedProductTest {

	protected static final Log logger = LogFactory.getLog(CharactDetailsFormulationTest.class);
	
	@Autowired
	private AttributeExtractorService attributeExtractorService;
	
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

		final NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				/*-- Create finished product --*/
				logger.debug("/*-- Create finished product --*/");
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 1");
				finishedProduct.setLegalName("Legal Produit fini 1");
				finishedProduct.setUnit(ProductUnit.kg);
				finishedProduct.setQty(2d);
				finishedProduct.setUnitPrice(12.4d);
				List<CompoListDataItem> compoList = new ArrayList<>();
				CompoListDataItem item = new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Detail,
						localSF1NodeRef);
				
				compoList.add(item);
				compoList.add(new CompoListDataItem(null, item, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Declare,
						rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, item, null, 2d, CompoListUnit.kg, 0d, DeclarationType.Detail,
						rawMaterial2NodeRef));
				item = new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Detail,
						localSF2NodeRef);
				compoList.add(item);
				compoList.add(new CompoListDataItem(null, item, null, 3d, CompoListUnit.kg, 0d, DeclarationType.Declare,
						rawMaterial3NodeRef));
				compoList.add(new CompoListDataItem(null, item, null, 3d, CompoListUnit.kg, 0d, DeclarationType.Omit,
						rawMaterial4NodeRef));
				finishedProduct.getCompoListView().setCompoList(compoList);

				NodeRef finishedProductNodeRef =  alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

				/*-- Formulate product --*/
				logger.debug("/*-- Formulate details --*/");
				productService.formulate(finishedProductNodeRef);
				CharactDetails ret = productService.formulateDetails(finishedProductNodeRef, PLMModel.TYPE_NUTLIST,
						"nutList", null, null);

				Assert.assertNotNull(ret);

				logger.info(CharactDetailsHelper.toJSONObject(ret, nodeService,attributeExtractorService).toString(3));
				return finishedProductNodeRef;
				

			}
		}, false, true);
		
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				productService.formulate(finishedProductNodeRef);
				
				FinishedProductData finishedProduct = (FinishedProductData) alfrescoRepository.findOne(finishedProductNodeRef);
				
				Assert.assertNotNull(finishedProduct.getNutList());
				for (NutListDataItem nutItem : finishedProduct.getNutList()){
					Assert.assertTrue(nodeService.hasAspect(nutItem.getNodeRef(), BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM));
				}
				return null;

			}
		}, false, true);

	}
	
	/**
	 * Test formulate product and check cost details
	 *message
	 * @throws Exception the exception
	 */
	@Test
	public void testCalculateCostDetails() throws Exception{
		   
		logger.info("testCalculateCostDetails");
		
		final NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			public NodeRef execute() throws Throwable {					   							
				
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 1");
				finishedProduct.setLegalName("Legal Produit fini 1");
				finishedProduct.setUnit(ProductUnit.kg);
				finishedProduct.setQty(2d);
				List<PackagingListDataItem> packagingList = new ArrayList<>();
				packagingList.add(new PackagingListDataItem(null, 1d, PackagingListUnit.P, PackagingLevel.Primary, true, packagingMaterial1NodeRef));
				packagingList.add(new PackagingListDataItem(null, 3d, PackagingListUnit.m, PackagingLevel.Primary, true, packagingMaterial2NodeRef));
				packagingList.add(new PackagingListDataItem(null, 8d, PackagingListUnit.PP, PackagingLevel.Tertiary, true, packagingMaterial3NodeRef));
				finishedProduct.getPackagingListView().setPackagingList(packagingList);		
				
				
				/*
				 * Composition
				 */				
				List<CompoListDataItem> compoList = new ArrayList<>();
				CompoListDataItem item = new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 10d, DeclarationType.Detail, localSF1NodeRef);
				
				compoList.add(item);
				compoList.add(new CompoListDataItem(null, item, null, 1d, CompoListUnit.kg, 5d, DeclarationType.Declare, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, item, null, 2d, CompoListUnit.kg, 10d, DeclarationType.Detail, rawMaterial2NodeRef));
				 item = new CompoListDataItem(null, null, 1d, 0d, CompoListUnit.kg, 20d, DeclarationType.Detail, localSF2NodeRef);
				compoList.add(item);
				compoList.add(new CompoListDataItem(null,item, null, 3d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
				compoList.add(new CompoListDataItem(null, item, null, 3d, CompoListUnit.kg, 0d, DeclarationType.Omit, rawMaterial4NodeRef));
				finishedProduct.getCompoListView().setCompoList(compoList);
				return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();				
				
			}},false,true);
	   
	   transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				//formulate Details
				List<NodeRef> costNodeRefs = new ArrayList<>();
				productService.formulate(finishedProductNodeRef);
				CharactDetails ret = productService.formulateDetails(finishedProductNodeRef, PLMModel.TYPE_COSTLIST,
						"costList", costNodeRefs, null);
				
				Assert.assertNotNull(ret);
				logger.info(CharactDetailsHelper.toJSONObject(ret, nodeService, attributeExtractorService).toString(3));
				
				//costs
				int checks = 0;
				DecimalFormat df = new DecimalFormat("0.####");
				for(Map.Entry<NodeRef, List< CharactDetailsValue>> kv : ret.getData().entrySet()){
					
					for( CharactDetailsValue kv2 : kv.getValue()){
						
						String trace = "cost: " + nodeService.getProperty(kv.getKey(), BeCPGModel.PROP_CHARACT_NAME) + 
								" - source: " + nodeService.getProperty(kv2.getKeyNodeRef(),BeCPGModel.PROP_CHARACT_NAME) + 
								" - value: " + kv.getValue();
						logger.debug(trace);
						
						//cost1
						if(kv.getKey().equals(cost1)){
							
							if(kv2.getKeyNodeRef().equals(rawMaterial1NodeRef)){
							
								checks++;
								assertEquals("cost.getValue() == 1.7325, actual values: " + trace, df.format(1.7325d), df.format(kv2.getValue()));
								//assertEquals("cost.getPercentage() == 36.5314, actual values: " + trace, df.format(36.5314), df.format(kv2.getPercentage()));
							}
							else if(kv2.getKeyNodeRef().equals(rawMaterial2NodeRef)){
								
								checks++;
								assertEquals("cost.getValue() == 1.21, actual values: " + trace, df.format(1.21d), df.format(kv2.getValue()));
								//assertEquals("cost.getPercentage() == 25.5140, actual values: " + trace, df.format(25.5140), df.format(kv2.getPercentage()));
							}
							else if(kv2.getKeyNodeRef().equals(rawMaterial3NodeRef)){
								
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
							
							if(kv2.getKeyNodeRef().equals(rawMaterial1NodeRef)){
							
								checks++;
								assertEquals("cost.getValue() == 1.155, actual values: " + trace, df.format(1.155d), df.format(kv2.getValue()));
								//assertEquals("cost.getPercentage() == 16.0976, actual values: " + trace, df.format(16.0976), df.format(kv2.getPercentage()));
							}
							else if(kv2.getKeyNodeRef().equals(rawMaterial2NodeRef)){
								
								checks++;
								assertEquals("cost.getValue() == 2.42, actual values: " + trace, df.format(2.42d), df.format(kv2.getValue()));
								//assertEquals("cost.getPercentage() == 33.7282, actual values: " + trace, df.format(33.7282), df.format(kv2.getPercentage()));
							}
							else if(kv2.getKeyNodeRef().equals(rawMaterial3NodeRef)){
								
								checks++;
								assertEquals("cost.getValue() == 3.6, actual values: " + trace, df.format(3.6d), df.format(kv2.getValue()));
								//assertEquals("cost.getPercentage() == 50.1742, actual values: " + trace, df.format(50.1742), df.format(kv2.getPercentage()));
							}
							else if(kv2.getKeyNodeRef().equals(rawMaterial4NodeRef)){								
								checks++;
							}
							else{
								checks++;
							}
						}
						
						//pkgCost1
						else if(kv.getKey().equals(pkgCost1)){
							
							if(kv2.getKeyNodeRef().equals(packagingMaterial1NodeRef)){
								
								checks++;
								assertEquals("cost.getValue() == 1.5, actual values: " + trace, df.format(1.5d), df.format(kv2.getValue()));
								//assertEquals("cost.getPercentage() == 48.9796, actual values: " + trace, df.format(48.9796), df.format(kv2.getPercentage()));
							}						
							else if(kv2.getKeyNodeRef().equals(packagingMaterial2NodeRef)){
								
								checks++;
								assertEquals("cost.getValue() == 1.5, actual values: " + trace, df.format(1.5d), df.format(kv2.getValue()));
								//assertEquals("cost.getPercentage() == 48.9796, actual values: " + trace, df.format(48.9796), df.format(kv2.getPercentage()));
							}						
							else if(kv2.getKeyNodeRef().equals(packagingMaterial3NodeRef)){
								
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
							
							if(kv2.getKeyNodeRef().equals(packagingMaterial1NodeRef)){
								
								checks++;
								assertEquals("cost.getValue() == 1, actual values: " + trace, df.format(1d), df.format(kv2.getValue()));
								//assertEquals("cost.getPercentage() == 24.2424, actual values: " + trace, df.format(24.2424), df.format(kv2.getPercentage()));
							}
							else if(kv2.getKeyNodeRef().equals(packagingMaterial2NodeRef)){
								
								checks++;
								assertEquals("cost.getValue() == 3, actual values: " + trace, df.format(3d), df.format(kv2.getValue()));
								//assertEquals("cost.getPercentage() == 72.7273, actual values: " + trace, df.format(72.7273), df.format(kv2.getPercentage()));
							}						
							else if(kv2.getKeyNodeRef().equals(packagingMaterial3NodeRef)){
								
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
				
				assertEquals("Verify checks done", 13, checks);
							
				return null;

			}
		}, false, true);
		   
	   }

}