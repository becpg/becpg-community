/*
 * 
 */
package fr.becpg.test.repo.web.scripts.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

import fr.becpg.test.PLMBaseWebScriptTest;
import fr.becpg.test.BeCPGPLMTestHelper;

/**
 * The Class VersionHistoryWebScriptTest.
 *
 * @author querephi
 */
public class EntityVersionWebScriptTest extends PLMBaseWebScriptTest{

	/** The logger. */
	private static Log logger = LogFactory.getLog(EntityVersionWebScriptTest.class);

	@Resource
    private CheckOutCheckInService checkOutCheckInService;
    
	@Test
	public void testGetVersionHistory() throws Exception {
		
		final NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<NodeRef>() {

					@Override
					public NodeRef execute() throws Throwable {
						logger.debug("Add versionnable aspect");

						NodeRef rawMaterialNodeRef = BeCPGPLMTestHelper.createRawMaterial(testFolderNodeRef, "MP test report");
						if (!nodeService.hasAspect(rawMaterialNodeRef, ContentModel.ASPECT_VERSIONABLE)) {
							Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>();
							aspectProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
							nodeService.addAspect(rawMaterialNodeRef, ContentModel.ASPECT_VERSIONABLE, aspectProperties);
						}
						return rawMaterialNodeRef;
					}

				}, false, true);
		
		
		 transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
				@Override
				public NodeRef execute() throws Throwable {					   
			
	 				
	 				NodeRef checkedOutNodeRef = checkOutCheckInService.checkout(rawMaterialNodeRef);
	 				NodeRef checkedInNodeRef = checkOutCheckInService.checkin(checkedOutNodeRef, null);
	 				
	 				NodeRef checkedOutNodeRef2 = checkOutCheckInService.checkout(checkedInNodeRef);
	 				checkOutCheckInService.checkin(checkedOutNodeRef2, null);
	 				
					return null;

				}},false,true);
		 
			//Call webscript on raw material to check out
			String url = "/api/version?nodeRef=" + rawMaterialNodeRef;
			logger.debug("url : " + url);				

			Response response = sendRequest(new GetRequest(url), 200, "admin");
			logger.debug("version history: " + response.getContentAsString());

    }
    	
}
