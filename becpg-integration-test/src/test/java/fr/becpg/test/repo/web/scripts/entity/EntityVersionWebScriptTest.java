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
import fr.becpg.test.utils.TestWebscriptExecuters.GetRequest;
import fr.becpg.test.utils.TestWebscriptExecuters.Response;

/**
 * The Class VersionHistoryWebScriptTest.
 *
 * @author querephi
 */
public class EntityVersionWebScriptTest extends PLMBaseTestCase{

	/** The logger. */
	private static Log logger = LogFactory.getLog(EntityVersionWebScriptTest.class);

	@Resource
    private CheckOutCheckInService checkOutCheckInService;
    
	private NodeRef rawMaterialNodeRef = null;

	@Test
	public void testGetVersionHistory() throws Exception {
		
		
		 transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
				@Override
				public NodeRef execute() throws Throwable {					   
			
				
	 				rawMaterialNodeRef = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "Test MP");	 					
	 				
	 				NodeRef checkedOutNodeRef = checkOutCheckInService.checkout(rawMaterialNodeRef);
	 				NodeRef checkedInNodeRef = checkOutCheckInService.checkin(checkedOutNodeRef, null);
	 				
	 				NodeRef checkedOutNodeRef2 = checkOutCheckInService.checkout(checkedInNodeRef);
	 				checkOutCheckInService.checkin(checkedOutNodeRef2, null);
	 				
					return null;

				}},false,true);
		 
			//Call webscript on raw material to check out
			String url = "/api/version?nodeRef=" + rawMaterialNodeRef;
			logger.debug("url : " + url);				

			Response response = TestWebscriptExecuters.sendRequest(new GetRequest(url), 200, "admin");
			logger.debug("version history: " + response.getContentAsString());

    }
    	
}
