/*
 * 
 */
package fr.becpg.test.repo.web.scripts.entity;

import javax.annotation.Resource;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

import fr.becpg.test.PLMBaseWebScriptTest;
import fr.becpg.test.BeCPGPLMTestHelper;

/**
 * The Class CheckOutCheckInWebScriptTest.
 *
 * @author querephi
 */
public class CheckOutCheckInWebScriptTest extends PLMBaseWebScriptTest{

	private static Log logger = LogFactory.getLog(CheckOutCheckInWebScriptTest.class);

	private NodeRef rawMaterialNodeRef = null;
	
	private NodeRef workingCopyNodeRef = null;
	
	@Resource
    private CheckOutCheckInService checkOutCheckInService;
	
	
	private void init(){
		 transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
				@Override
				public NodeRef execute() throws Throwable {					   
			
					/*-- create folders --*/
	 				rawMaterialNodeRef = BeCPGPLMTestHelper.createRawMaterial(testFolderNodeRef, "Raw material test");	 					 				 		 				
	 				
					return null;

				}},false,true);
	}
	
	@Test
	public void testCheckOutCheckInProduct() throws Exception {
		
		
			init();
		 
			//Call webscript on raw material to check out
			String url = "/slingshot/doclib/action/checkout/node/" + rawMaterialNodeRef.toString().replace(":/", "");
			String data = "{}";
			logger.debug("url : " + url);				

			Response response = sendRequest(new PostRequest(url, data, "application/json"), 200, "admin");
			logger.debug("content checkout: " + response.getContentAsString());

			workingCopyNodeRef = checkOutCheckInService.getWorkingCopy(rawMaterialNodeRef);
			
			 
			 assertNotNull("working copy should exist", workingCopyNodeRef);
			 
			 //Call webscript on raw material to check in
			 url = "/slingshot/doclib/action/checkin/node/" + workingCopyNodeRef.toString().replace(":/", "");			
			 logger.debug("url : " + url);				

			 response = sendRequest(new PostRequest(url, data, "application/json"), 200, "admin");
			 logger.debug("content checkin: " + response.getContentAsString());

    }
	
	@Test
	public void testCheckOutCancelCheckOutProduct() throws Exception {
		
		
			init();
		 
			//Call webscript on raw material to check out
			String url = "/slingshot/doclib/action/checkout/node/" + rawMaterialNodeRef.toString().replace(":/", "");
			String data = "{}";
			logger.debug("url : " + url);				

			Response response = sendRequest(new PostRequest(url, data, "application/json"), 200, "admin");
			logger.debug("content checkout: " + response.getContentAsString());

			workingCopyNodeRef = checkOutCheckInService.getWorkingCopy(rawMaterialNodeRef);
			 
			 assertNotNull("working copy should exist", workingCopyNodeRef);
			
			 //Call webscript on raw material to cancel check out
			 url = "/slingshot/doclib/action/cancel-checkout/node/" + workingCopyNodeRef.toString().replace(":/", "");			
			 logger.debug("url : " + url);				

			 response = sendRequest(new PostRequest(url, data, "application/json"), 200, "admin");
			 logger.debug("content checkin: " + response.getContentAsString());
   }
    	
}
