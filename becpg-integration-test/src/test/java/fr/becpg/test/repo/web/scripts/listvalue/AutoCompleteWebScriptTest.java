/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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
package fr.becpg.test.repo.web.scripts.listvalue;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.test.BeCPGPLMTestHelper;
import fr.becpg.test.utils.TestWebscriptExecuters;
import fr.becpg.test.utils.TestWebscriptExecuters.GetRequest;
import fr.becpg.test.utils.TestWebscriptExecuters.Response;

/**
 * The Class AutoCompleteWebScriptTest.
 * 
 * @author querephi
 */
public class AutoCompleteWebScriptTest extends fr.becpg.test.PLMBaseTestCase {

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

		Response response = TestWebscriptExecuters.sendRequest(new GetRequest(url), 200, "admin");
		logger.debug("content : " + response.getContentAsString());

		url = "/becpg/autocomplete/targetassoc/associations/bcpg:nut?q=nut11";

		response = TestWebscriptExecuters.sendRequest(new GetRequest(url), 200, "admin");
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

		Response response = TestWebscriptExecuters.sendRequest(new GetRequest(url), 200, "admin");
		logger.debug("testSuggestListValue content : " + response.getContentAsString());

		url = "/becpg/autocomplete/listvalue/values/System/ProductHierarchy/RawMaterial_Hierarchy1?q=F";

		response = TestWebscriptExecuters.sendRequest(new GetRequest(url), 200, "admin");
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

		Response response = TestWebscriptExecuters.sendRequest(new GetRequest(url), 200, "admin");
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

				BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "Test MP");

				return null;

			}
		}, false, true);

		String url = "/becpg/autocomplete/product?q=ra";
		Response response = TestWebscriptExecuters.sendRequest(new GetRequest(url), 200, "admin");
		logger.debug("testSuggestProduct : " + response.getContentAsString());

		url = "/becpg/autocomplete/product?classNames=bcpg:rawMaterial,bcpg:finishedProduct,bcpg:localSemiFinishedProduct,bcpg:semiFinishedProduct&q=ra";
		response = TestWebscriptExecuters.sendRequest(new GetRequest(url), 200, "admin");
		logger.debug("testSuggestProduct : " + response.getContentAsString());

		url = "/becpg/autocomplete/product?classNames=bcpg:packagingMaterial&q=ra";
		response = TestWebscriptExecuters.sendRequest(new GetRequest(url), 200, "admin");
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

		Response response = TestWebscriptExecuters.sendRequest(new GetRequest(url), 200, "admin");
		logger.debug("testProductReportTpls : " + response.getContentAsString());
	}

}
