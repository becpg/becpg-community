
package fr.becpg.repo.web.scripts.listvalue;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

import fr.becpg.test.BeCPGTestHelper;

/**
 * The Class AutoCompleteWebScriptTest.
 * 
 * @author querephi
 */
public class AutoCompleteWebScriptTest extends fr.becpg.test.BaseWebScriptTest {

	private static Log logger = LogFactory.getLog(AutoCompleteWebScriptTest.class);


	/**
	 * Test suggest target assoc.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testSuggestTargetAssoc() throws Exception {

		String url = "/becpg/autocomplete/targetassoc/associations/bcpg:nut?q=pro";

		Response response = sendRequest(new GetRequest(url), 200, "admin");
		logger.debug("content : " + response.getContentAsString());

		url = "/becpg/autocomplete/targetassoc/associations/bcpg:nut?q=nut11";

		response = sendRequest(new GetRequest(url), 200, "admin");
		logger.debug("content : " + response.getContentAsString());

	}

	/**
	 * Test suggest list value.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testSuggestListValue() throws Exception {

		String url = "/becpg/autocomplete/listvalue/values/System/ProductHierarchy/RawMaterial_Hierarchy1?q=f";

		Response response = sendRequest(new GetRequest(url), 200, "admin");
		logger.debug("testSuggestListValue content : " + response.getContentAsString());

		url = "/becpg/autocomplete/listvalue/values/System/ProductHierarchy/RawMaterial_Hierarchy1?q=F";

		response = sendRequest(new GetRequest(url), 200, "admin");
		logger.debug("testSuggestListValue content : " + response.getContentAsString());
	}

	/**
	 * Test suggest linked values.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testSuggestLinkedValues() throws Exception {

		String url = "/becpg/autocomplete/linkedvalue/values/System/ProductHierarchy/RawMaterial_Hierarchy1?q=s&parent=Fam4";

		Response response = sendRequest(new GetRequest(url), 200, "admin");
		logger.debug("testSuggestLinkedValues : " + response.getContentAsString());

	}

	/**
	 * Test suggest product.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testSuggestProduct() throws Exception {

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				BeCPGTestHelper.createRawMaterial(testFolderNodeRef, "Test MP");

				return null;

			}
		}, false, true);

		String url = "/becpg/autocomplete/product?q=ra";
		Response response = sendRequest(new GetRequest(url), 200, "admin");
		logger.debug("testSuggestProduct : " + response.getContentAsString());

		url = "/becpg/autocomplete/product?classNames=bcpg:rawMaterial,bcpg:finishedProduct,bcpg:localSemiFinishedProduct,bcpg:semiFinishedProduct&q=ra";
		response = sendRequest(new GetRequest(url), 200, "admin");
		logger.debug("testSuggestProduct : " + response.getContentAsString());

		url = "/becpg/autocomplete/product?classNames=bcpg:packagingMaterial&q=ra";
		response = sendRequest(new GetRequest(url), 200, "admin");
		logger.debug("testSuggestProduct : " + response.getContentAsString());
	}

	/**
	 * Test product report tpls.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testProductReportTpls() throws Exception {

		String url = "/becpg/autocomplete/productreport/reports/SemiFinishedProduct?q=u";

		Response response = sendRequest(new GetRequest(url), 200, "admin");
		logger.debug("testProductReportTpls : " + response.getContentAsString());
	}

}
