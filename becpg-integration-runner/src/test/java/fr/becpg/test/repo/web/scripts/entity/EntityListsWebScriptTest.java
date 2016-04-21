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
package fr.becpg.test.repo.web.scripts.entity;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.test.BeCPGPLMTestHelper;
import fr.becpg.test.PLMBaseTestCase;
import fr.becpg.test.utils.TestWebscriptExecuters;
import fr.becpg.test.utils.TestWebscriptExecuters.GetRequest;
import fr.becpg.test.utils.TestWebscriptExecuters.Response;

/**
 * The Class EntityListsWebScriptTest.
 *
 * @author querephi
 */
public class EntityListsWebScriptTest extends PLMBaseTestCase{

	private static final Log logger = LogFactory.getLog(EntityListsWebScriptTest.class);
	
	private NodeRef rawMaterialNodeRef = null;
	
	private NodeRef finishedProductNodeRef = null;
	

	@Test
	public void testProductList() throws Exception {
		
		
		 transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
				@Override
				public NodeRef execute() throws Throwable {					   
			
					rawMaterialNodeRef = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "Test MP");
					finishedProductNodeRef = BeCPGPLMTestHelper.createMultiLevelProduct(getTestFolderNodeRef());
					
					return null;

				}},false,true);
		 
			//Call webscript on raw material
			String url = "/becpg/entitylists/node/" + rawMaterialNodeRef.toString().replace(":/", "");
			logger.debug("url : " + url);				
			Response response = TestWebscriptExecuters.sendRequest(new GetRequest(url), 200, "admin");
			logger.debug("content : " + response.getContentAsString());
			response = TestWebscriptExecuters.sendRequest(new GetRequest(url), 200, "admin");
			logger.debug("content : " + response.getContentAsString());
			
//			//Check product
//			RawMaterialData rawMaterial = (RawMaterialData)productFactory.getProduct(rawMaterialNodeRef, productDictionaryService.getDataLists());
//			assertEquals("Raw material must have 2 costs", rawMaterial.getCostList().size()==2);
//			assertEquals("Raw material must have 1 nut", rawMaterial.getNutList().size()==1);		
			
			//Call webscript on finished product
			url = "/becpg/entitylists/node/" + finishedProductNodeRef.toString().replace(":/", "");
			logger.debug("url : " + url);				
			response = TestWebscriptExecuters.sendRequest(new GetRequest(url), 200, "admin");
			logger.debug("content : " + response.getContentAsString());
			
//			//Check product
//			FinishedProductData finishedProduct = (FinishedProductData)productFactory.getProduct(finishedProductNodeRef, productDictionaryService.getDataLists());
//			assertEquals("Finished product don't have costs", finishedProduct.getCostList()==null);
//			assertEquals("Finished product don't have nuts", finishedProduct.getNutList()==null);
		

    }
    	
}
