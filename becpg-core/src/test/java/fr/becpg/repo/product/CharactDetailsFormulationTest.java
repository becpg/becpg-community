/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.product;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Assert;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.data.CharactDetails;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.DeclarationType;
import fr.becpg.repo.product.data.productList.DynamicCharactListItem;
import fr.becpg.repo.web.scripts.product.CharactDetailsHelper;

/**
 * The Class FormulationTest.
 *
 * @author querephi
 */
public class CharactDetailsFormulationTest extends AbstractFormuationTest {
	
    
    /* (non-Javadoc)
     * @see fr.becpg.test.RepoBaseTestCase#setUp()
     */
    @Override
	protected void setUp() throws Exception {
    	super.setUp();		
   
    	productService = (ProductService)ctx.getBean("productService");       
        productDAO = (ProductDAO)ctx.getBean("productDAO");

 		//create RM and lSF
 		initParts();
 		       
        
    }
    
    
	/* (non-Javadoc)
	 * @see fr.becpg.test.RepoBaseTestCase#tearDown()
	 */
	@Override
    public void tearDown() throws Exception
    {
        super.tearDown();

    }
	

	/**
	 * Test formulate product.
	 *
	 * @throws Exception the exception
	 */
	public void testFormulateCharactDetails() throws Exception{
		   
		logger.info("testFormulateCharactDetails");
		
	   transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			public NodeRef execute() throws Throwable {					   							
				
				//fixed cost
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();				
				properties.put(ContentModel.PROP_NAME, "fixedCost");			 					 				
				properties.put(BeCPGModel.PROP_COSTCURRENCY, "€");
				properties.put(BeCPGModel.PROP_COSTFIXED, true);
				NodeRef fixedCost = nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_COST, properties).getChildRef();
				
				/*-- Create finished product --*/
				logger.debug("/*-- Create finished product --*/");
				 Collection<QName> dataLists = productDictionaryService.getDataLists();
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 1");
				finishedProduct.setLegalName("Legal Produit fini 1");
				finishedProduct.setUnit(ProductUnit.kg);
				finishedProduct.setQty(2d);
				finishedProduct.setUnitPrice(12.4d);
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 1d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 2d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Omit, rawMaterial4NodeRef));
				finishedProduct.setCompoList(compoList);
				List<CostListDataItem> costList = new ArrayList<CostListDataItem>();
				costList.add(new CostListDataItem(null, 4000d, "€", null, fixedCost, true));
				finishedProduct.setCostList(costList);
				
				List<DynamicCharactListItem> dynamicCharactListItems = new ArrayList<DynamicCharactListItem>();
				//Literal formula
				dynamicCharactListItems.add(new DynamicCharactListItem("Literal 1","'Hello World'" ));
				dynamicCharactListItems.add(new DynamicCharactListItem("Literal 2","6.0221415E+23" ));
				dynamicCharactListItems.add(new DynamicCharactListItem("Literal 3","1+1+10-(4/100)" ));
				dynamicCharactListItems.add(new DynamicCharactListItem("Literal 4","0x7dFFFFFF" ));
				dynamicCharactListItems.add(new DynamicCharactListItem("Literal 5","true" ));
				dynamicCharactListItems.add(new DynamicCharactListItem("Literal 6","null" ));
				//Properties formulae
				dynamicCharactListItems.add(new DynamicCharactListItem("Property  1","costList[0].value" ));
				dynamicCharactListItems.add(new DynamicCharactListItem("Property  1Bis","costList[1].value" ));
				dynamicCharactListItems.add(new DynamicCharactListItem("Property  2","costList[0].unit" ));
				dynamicCharactListItems.add(new DynamicCharactListItem("Property  3","costList[0].value / costList[1].value" ));
				dynamicCharactListItems.add(new DynamicCharactListItem("Property  4","profitability" ));
				dynamicCharactListItems.add(new DynamicCharactListItem("Collection Selection  1","costList.?[value == 4.0][0].unit" ));
				dynamicCharactListItems.add(new DynamicCharactListItem("Collection Selection  2","costList.?[value < 5.0][0].value" ));
				dynamicCharactListItems.add(new DynamicCharactListItem("Collection Projection  1","costList.![value]" ));
				//Template need Template Context
				//dynamicCharactListItems.add(new DynamicCharactListItem("Template  1","Cost1/Cost2 : #{costList[1].value / costList[2].value}% Profitability : #{profitability}" ));
				//Elvis 
				dynamicCharactListItems.add(new DynamicCharactListItem("Elvis  1","null?:'Unknown'" ));
				//Boolean
				dynamicCharactListItems.add(new DynamicCharactListItem("Boolean  1","costList[1].value > 1" ));
				//Assignment
				dynamicCharactListItems.add(new DynamicCharactListItem("Assignement  1","nutList.?[nut.toString() == '"+nut1+"' ][0].value = 4d" ));
				
				
				finishedProduct.setDynamicCharactList(dynamicCharactListItems);
				
				
				NodeRef finishedProductNodeRef = productDAO.create(testFolderNodeRef, finishedProduct, dataLists);
				
				logger.debug("unit of product to formulate: " + finishedProduct.getUnit());
				
				/*-- Formulate product --*/
				logger.debug("/*-- Formulate details --*/");
				CharactDetails ret =  productService.formulateDetails(finishedProductNodeRef,BeCPGModel.TYPE_NUTLIST,  "nutList", null);
				
				Assert.assertNotNull(ret);
				
				System.out.println(CharactDetailsHelper.toJSONObject(ret,nodeService).toString(3));
				
				return null;

			}},false,true);
		   
	   }

}