/*
 * 
 */
package fr.becpg.repo.web.scripts.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

import fr.becpg.test.BaseWebScriptTest;

/**
 * The Class ReloadModelsWebScriptTest.
 *
 * @author querephi
 */
@Deprecated //Merge with admin module test
public class ReloadModelsWebScriptTest extends BaseWebScriptTest{		
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ReloadModelsWebScriptTest.class);


	/**
	 * Test init repo.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testReloadModels() throws Exception {				
		
		logger.debug("test reload models webscript ");
		String url = "/becpg/admin/repository/reload-model";
		Response response = sendRequest(new GetRequest(url), 200, "admin");
		logger.debug(response.getContentAsString());			
	}	
	
}
