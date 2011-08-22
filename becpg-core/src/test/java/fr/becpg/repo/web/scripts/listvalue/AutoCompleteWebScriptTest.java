/*
 * 
 */
package fr.becpg.repo.web.scripts.listvalue;

import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

// TODO: Auto-generated Javadoc
/**
 * The Class AutoCompleteWebScriptTest.
 *
 * @author querephi
 */
public class AutoCompleteWebScriptTest extends BaseWebScriptTest  {
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(AutoCompleteWebScriptTest.class);
	
	
	/* (non-Javadoc)
	 * @see org.alfresco.repo.web.scripts.BaseWebScriptTest#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{	
		super.setUp();		
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	/**
	 * Test suggest target assoc.
	 *
	 * @throws Exception the exception
	 */
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
	 * @throws Exception the exception
	 */
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
	 * @throws Exception the exception
	 */
	public void testSuggestLinkedValues() throws Exception {
		
		String url = "/becpg/autocomplete/linkedvalue/values/System/ProductHierarchy/RawMaterial_Hierarchy1?q=s&parent=Fam4";
		
		Response response = sendRequest(new GetRequest(url), 200, "admin");
		logger.debug("content : " + response.getContentAsString());
				
    }
    
	/**
	 * Test suggest product.
	 *
	 * @throws Exception the exception
	 */
	public void testSuggestProduct() throws Exception {
		
		String url = "/becpg/autocomplete/product?q=p";
				
		
		Response response = sendRequest(new GetRequest(url), 200, "admin");
		logger.debug("content : " + response.getContentAsString());
    }
	
	/**
	 * Test product report tpls.
	 *
	 * @throws Exception the exception
	 */
	public void testProductReportTpls() throws Exception {		
		
		String url = "/becpg/autocomplete/productreport/reports/SemiFinishedProduct?q=u";
				
		
		Response response = sendRequest(new GetRequest(url), 200, "admin");
		logger.debug("content : " + response.getContentAsString());
    }

}
