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
 * The Class AdminModuleWebScriptTest.
 *
 * @author querephi
 */
public class AdminModuleWebScriptTest extends BaseWebScriptTest{		
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(AdminModuleWebScriptTest.class);


	/**
	 * Test init repo.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testInitRepo() throws Exception {				
		
		//Init-repo
		logger.debug("test init repo webscript ");
		String url = "/becpg/admin/repository/init-repo";
		Response response = sendRequest(new GetRequest(url), 200, "admin");
		logger.debug(response.getContentAsString());		
	}	
	
}
