/*
 * 
 */
package fr.becpg.test.repo.web.scripts.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.test.RepoBaseTestCase;
import fr.becpg.test.utils.TestWebscriptExecuters;
import fr.becpg.test.utils.TestWebscriptExecuters.GetRequest;
import fr.becpg.test.utils.TestWebscriptExecuters.Response;

/**
 * The Class AdminModuleWebScriptTest.
 *
 * @author querephi
 */
public class AdminModuleWebScriptTest extends RepoBaseTestCase{		
	
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
		logger.error("test init repo webscript ");
		String url = "/becpg/admin/repository/init-repo";
		Response response = TestWebscriptExecuters.sendRequest(new GetRequest(url), 200, "admin");
		logger.error(response.getContentAsString());		
	}	
	
	/**
	 * Test init repo.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testReloadModels() throws Exception {				
		
		logger.debug("test reload models webscript ");
		String url = "/becpg/admin/repository/reload-model";
		Response response = TestWebscriptExecuters.sendRequest(new GetRequest(url), 200, "admin");
		logger.debug(response.getContentAsString());			
	}	
	
}
