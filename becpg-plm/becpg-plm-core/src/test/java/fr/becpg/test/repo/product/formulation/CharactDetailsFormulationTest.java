/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.product.formulation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
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
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.CompoListUnit;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.PackagingListUnit;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.TareUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.web.scripts.product.CharactDetailsHelper;
import fr.becpg.test.BeCPGPLMTestHelper;
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
								" - value: " + kv2.getValue();
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
				
				assertEquals("Verify checks done", 12, checks);
							
				return null;

			}
		}, false, true);
		   
	   }
	
	@Test
	public void testCalculateNutDetails() throws Exception {
		
		logger.info("testCalculateNutDetails");	
		
		
		final NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			public NodeRef execute() throws Throwable {	

				RawMaterialData rawMaterial = new RawMaterialData();
				rawMaterial.setName("Raw material");
				rawMaterial.setQty(0.1d);
				rawMaterial.setUnit(ProductUnit.kg);
				rawMaterial.setNetWeight(0.1d);
				rawMaterial.setDensity(0.1d);
				rawMaterial.setTare(9d);
				rawMaterial.setTareUnit(TareUnit.g);
				// nutList
				List<NutListDataItem> nutList = new ArrayList<>();
				nutList.add(new NutListDataItem(null, 1d, "g/100g", 0.75d, null, "Groupe 1", nut1, false));
				nutList.add(new NutListDataItem(null, 3d, "g/100g", null, 4d, "Groupe 1", nut2, false));
				rawMaterial.setNutList(nutList);
				return alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial).getNodeRef();
				
			}},false,true);	
		
		final NodeRef semiFinishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			public NodeRef execute() throws Throwable {	
				
				
				//Semi finished product
				 
				SemiFinishedProductData semiFinishedProduct = new SemiFinishedProductData();
				semiFinishedProduct.setName("Semi fini 1");
				semiFinishedProduct.setUnit(ProductUnit.kg);
				List<CompoListDataItem> compoListSF = new ArrayList<>();
				compoListSF.add(new CompoListDataItem(null, null, null, 0.75d, CompoListUnit.kg, 5d, DeclarationType.Declare, rawMaterial3NodeRef));
				compoListSF.add(new CompoListDataItem(null, null, null, 1.5d, CompoListUnit.kg, 5d, DeclarationType.Declare, rawMaterialNodeRef));
				semiFinishedProduct.getCompoListView().setCompoList(compoListSF);
				
				List<NutListDataItem> nutList = new ArrayList<>();
				nutList.add(new NutListDataItem(null, null, "g/100g", null, null, "Groupe 1", nut1, false));
				nutList.add(new NutListDataItem(null, null, "g/100g", null, null, "Groupe 1", nut2, false));
				nutList.add(new NutListDataItem(null, null, "g/100g", null, null, "Groupe 1", nut3, false));
				nutList.add(new NutListDataItem(null, null, "g/100g", null, null, "Groupe 1", nut4, false));
				
				semiFinishedProduct.setNutList(nutList);
				
				return alfrescoRepository.create(getTestFolderNodeRef(), semiFinishedProduct).getNodeRef();			

			}},false,true);
		
		final NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			public NodeRef execute() throws Throwable {	

				
				//Finished Product
				 
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 2");
				finishedProduct.setLegalName("Legal Produit fini 2");
				finishedProduct.setUnit(ProductUnit.kg);
				finishedProduct.setQty(1.5d);

				
				//Composition
							
				List<CompoListDataItem> compoList = new ArrayList<>();
				logger.info("semiFinishedNR: "+ semiFinishedProductNodeRef);
				compoList.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Declare, semiFinishedProductNodeRef));
				compoList.add(new CompoListDataItem(null, null, null, 2d, CompoListUnit.kg, 5d, DeclarationType.Declare, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, null, null, 1.5d, CompoListUnit.kg, 10d, DeclarationType.Declare, rawMaterial2NodeRef));	

				finishedProduct.getCompoListView().setCompoList(compoList);
				
				List<NutListDataItem> nutList = new ArrayList<>();
				nutList.add(new NutListDataItem(null, null, "g/100g", null, null, "Groupe 1", nut1, false));
				nutList.add(new NutListDataItem(null, null, "g/100g", null, null, "Groupe 1", nut2, false));
				nutList.add(new NutListDataItem(null, null, "g/100g", null, null, "Groupe 1", nut3, false));
				nutList.add(new NutListDataItem(null, null, "g/100g", null, null, "Groupe 1", nut4, false));
				
				finishedProduct.setNutList(nutList);

				return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();				

			}},false,true);		
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@SuppressWarnings("unchecked")
			public NodeRef execute() throws Throwable {

				//formulate Details
				List<NodeRef> nutsNodeRefs = new ArrayList<>();
				productService.formulate(finishedProductNodeRef);				
				CharactDetails ret = productService.formulateDetails(finishedProductNodeRef, PLMModel.TYPE_NUTLIST,
						"nutList", nutsNodeRefs, null);
				
				Assert.assertNotNull(ret);
				JSONObject jsonRet = CharactDetailsHelper.toJSONObject(ret, nodeService, attributeExtractorService);
				
				logger.info(jsonRet.toString(3));
				Assert.assertTrue("no metadata array",jsonRet.has("metadatas"));
				JSONArray metadataArray = jsonRet.getJSONArray("metadatas");
				
				Assert.assertEquals(13, metadataArray.length());
				Assert.assertEquals("nut1 unset", ((JSONObject)metadataArray.get(1)).get("colName"),"nut1");
				Assert.assertEquals("nut1 mini unset", ((JSONObject)metadataArray.get(2)).get("colName"),"nut1, Mini");
				Assert.assertEquals("nut1 maxi unset", ((JSONObject)metadataArray.get(3)).get("colName"),"nut1, Maxi");
				
				Assert.assertEquals("nut2 unset", ((JSONObject)metadataArray.get(4)).get("colName"),"nut2");
				Assert.assertEquals("nut2 maxi unset", ((JSONObject)metadataArray.get(5)).get("colName"),"nut2, Maxi");
				Assert.assertEquals("nut2 mini unset", ((JSONObject)metadataArray.get(6)).get("colName"),"nut2, Mini");
				
				Assert.assertEquals("nut3 unset", ((JSONObject)metadataArray.get(7)).get("colName"),"nut3");
				Assert.assertEquals("nut3 mini unset", ((JSONObject)metadataArray.get(8)).get("colName"),"nut3, Mini");
				Assert.assertEquals("nut3 maxi unset", ((JSONObject)metadataArray.get(9)).get("colName"),"nut3, Maxi");
				
				Assert.assertEquals("nut4 unset", ((JSONObject)metadataArray.get(10)).get("colName"),"nut4");
				Assert.assertEquals("nut4 maxi unset", ((JSONObject)metadataArray.get(11)).get("colName"),"nut4, Maxi");
				Assert.assertEquals("nut4 mini unset", ((JSONObject)metadataArray.get(12)).get("colName"),"nut4, Mini");
				
				Assert.assertTrue("no resultsets", jsonRet.has("resultsets"));
				JSONArray resultsArray = jsonRet.getJSONArray("resultsets");
				Assert.assertEquals("result array does not have 4 arrays inside", 4, resultsArray.length());
				
				DecimalFormat df = new DecimalFormat("0.###");
				
				/*
				 * SF 1
				 */
				ArrayList<Object> tmpResultsArray = (ArrayList<Object>) resultsArray.get(0);
				Assert.assertEquals("Semi fini 1", tmpResultsArray.get(0));
				Assert.assertEquals(df.format(0.667d), df.format(tmpResultsArray.get(1)));
				Assert.assertEquals(df.format(0.556d), df.format(tmpResultsArray.get(2)));
				Assert.assertEquals("\u2014",tmpResultsArray.get(3));
				
				Assert.assertEquals(df.format(1.778d), df.format(tmpResultsArray.get(4)));
				Assert.assertEquals(df.format(2.222d), df.format(tmpResultsArray.get(5)));
				Assert.assertEquals("\u2014",tmpResultsArray.get(6));
				
				Assert.assertEquals(df.format(0.889d), df.format(tmpResultsArray.get(7)));
				Assert.assertEquals(df.format(0.178d), df.format(tmpResultsArray.get(8)));
				Assert.assertEquals(df.format(0.467d), df.format(tmpResultsArray.get(9)));
				
				Assert.assertNull(tmpResultsArray.get(10));
				Assert.assertNull(tmpResultsArray.get(11));
				Assert.assertNull(tmpResultsArray.get(12));
				
				/*
				 * RM 1
				 */
				tmpResultsArray = (ArrayList<Object>) resultsArray.get(1);
				Assert.assertEquals("Raw material 1", tmpResultsArray.get(0));
				Assert.assertEquals(df.format(1.333d), df.format(tmpResultsArray.get(1)));
				Assert.assertEquals(df.format(1.067d), df.format(tmpResultsArray.get(2)));
				Assert.assertEquals(df.format(2.8d), df.format(tmpResultsArray.get(3)));
				
				Assert.assertEquals(df.format(2.667d), df.format(tmpResultsArray.get(4)));
				Assert.assertEquals(df.format(2.933d), df.format(tmpResultsArray.get(5)));
				Assert.assertEquals(df.format(2d), df.format(tmpResultsArray.get(6)));
				
				Assert.assertEquals(df.format(5.333d), df.format(tmpResultsArray.get(7)));
				Assert.assertEquals(df.format(1.067d), df.format(tmpResultsArray.get(8)));
				Assert.assertEquals(df.format(2.8d), df.format(tmpResultsArray.get(9)));
				
				Assert.assertEquals(df.format(4d), df.format(tmpResultsArray.get(10)));
				Assert.assertEquals(df.format(2.8d), df.format(tmpResultsArray.get(11)));
				Assert.assertEquals(df.format(1.067d), df.format(tmpResultsArray.get(12)));
				
				/*
				 * RM 2
				 */
				tmpResultsArray = (ArrayList<Object>) resultsArray.get(2);
				Assert.assertEquals("Raw material 2", tmpResultsArray.get(0));
				Assert.assertEquals(df.format(1d), df.format(tmpResultsArray.get(1)));
				Assert.assertEquals(df.format(0.8d), df.format(tmpResultsArray.get(2)));
				Assert.assertEquals(df.format(1.1d), df.format(tmpResultsArray.get(3)));
				
				Assert.assertEquals(df.format(2d), df.format(tmpResultsArray.get(4)));
				Assert.assertEquals(df.format(2.1d), df.format(tmpResultsArray.get(5)));
				Assert.assertEquals(df.format(0.8d), df.format(tmpResultsArray.get(6)));
				
				Assert.assertEquals(df.format(6d), df.format(tmpResultsArray.get(7)));
				Assert.assertEquals(df.format(0.8d), df.format(tmpResultsArray.get(8)));
				Assert.assertEquals(df.format(2.1d), df.format(tmpResultsArray.get(9)));
				
				Assert.assertNull(tmpResultsArray.get(10));
				Assert.assertNull(tmpResultsArray.get(11));
				Assert.assertNull(tmpResultsArray.get(12));
				
				/*
				 * Totals
				 */
				LinkedList<Object> totalArray = (LinkedList<Object>) resultsArray.get(3);
				//TODO put entity.datalist.item.details.totals language key instead ?
				Assert.assertEquals("Totaux ", totalArray.get(0));
				Assert.assertEquals(df.format(3d), df.format(totalArray.get(1)));
				Assert.assertEquals(df.format(2.422d), df.format(totalArray.get(2)));
				Assert.assertEquals(df.format(4.567d), df.format(totalArray.get(3)));
				
				Assert.assertEquals(df.format(6.444d), df.format(totalArray.get(4)));
				Assert.assertEquals(df.format(7.256d), df.format(totalArray.get(5)));
				Assert.assertEquals(df.format(4.578d), df.format(totalArray.get(6)));
				
				Assert.assertEquals(df.format(12.222d), df.format(totalArray.get(7)));
				Assert.assertEquals(df.format(2.044d), df.format(totalArray.get(8)));
				Assert.assertEquals(df.format(5.367d), df.format(totalArray.get(9)));
				
				Assert.assertEquals(df.format(4d), df.format(totalArray.get(10)));
				Assert.assertEquals(df.format(2.8d), df.format(totalArray.get(11)));
				Assert.assertEquals(df.format(1.067d), df.format(totalArray.get(12)));				
				
				return null;

			}
		}, false, true);
		
	}

}