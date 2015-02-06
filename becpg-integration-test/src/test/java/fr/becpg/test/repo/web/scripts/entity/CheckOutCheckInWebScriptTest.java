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

import fr.becpg.test.BeCPGPLMTestHelper;
import fr.becpg.test.PLMBaseTestCase;
import fr.becpg.test.utils.TestWebscriptExecuters;
import fr.becpg.test.utils.TestWebscriptExecuters.PostRequest;
import fr.becpg.test.utils.TestWebscriptExecuters.Response;

/**
 * The Class CheckOutCheckInWebScriptTest.
 *
 * @author querephi
 */
public class CheckOutCheckInWebScriptTest extends PLMBaseTestCase {

	private static Log logger = LogFactory.getLog(CheckOutCheckInWebScriptTest.class);

	@Resource
	private CheckOutCheckInService checkOutCheckInService;

	private NodeRef createRawMaterial(final String name ) {
	 return transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				/*-- create folders --*/
				return BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), name);

			}
		}, false, true);
	}

	@Test
	public void testCheckOutCheckInProduct() throws Exception {

	   NodeRef rawMaterialNodeRef = createRawMaterial( "Raw material test - testCheckOutCheckInProduct");

		// Call webscript on raw material to check out
		String url = "/slingshot/doclib/action/checkout/node/" + rawMaterialNodeRef.toString().replace(":/", "");
		String data = "{}";
		logger.debug("url : " + url);

		Response response = TestWebscriptExecuters.sendRequest(new PostRequest(url, data, "application/json"), 200, "admin");
		logger.error("content checkout: " + response.getContentAsString());

		NodeRef workingCopyNodeRef = checkOutCheckInService.getWorkingCopy(rawMaterialNodeRef);

		assertNotNull("working copy should exist", workingCopyNodeRef);

		// Call webscript on raw material to check in
		url = "/slingshot/doclib/action/checkin/node/" + workingCopyNodeRef.toString().replace(":/", "");
		logger.error("url : " + url);

		response = TestWebscriptExecuters.sendRequest(new PostRequest(url, data, "application/json"), 200, "admin");
		logger.error("content checkin: " + response.getContentAsString());
		
		assertNull(checkOutCheckInService.getWorkingCopy(rawMaterialNodeRef));

	}

	@Test
	public void testCheckOutCancelCheckOutProduct() throws Exception {

		 NodeRef rawMaterialNodeRef =  createRawMaterial("Raw material test - testCheckOutCancelCheckOutProduct");

		// Call webscript on raw material to check out
		String url = "/slingshot/doclib/action/checkout/node/" + rawMaterialNodeRef.toString().replace(":/", "");
		String data = "{}";
		logger.debug("url : " + url);

		Response response = TestWebscriptExecuters.sendRequest(new PostRequest(url, data, "application/json"), 200, "admin");
		logger.debug("content checkout: " + response.getContentAsString());

		NodeRef workingCopyNodeRef = checkOutCheckInService.getWorkingCopy(rawMaterialNodeRef);

		assertNotNull("working copy should exist", workingCopyNodeRef);

		// Call webscript on raw material to cancel check out
		url = "/slingshot/doclib/action/cancel-checkout/node/" + workingCopyNodeRef.toString().replace(":/", "");
		logger.debug("url : " + url);

		response = TestWebscriptExecuters.sendRequest(new PostRequest(url, data, "application/json"), 200, "admin");
		logger.debug("content checkin: " + response.getContentAsString());
		
		assertNull(checkOutCheckInService.getWorkingCopy(rawMaterialNodeRef));

	}

}
