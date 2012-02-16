/*
 * 
 */
package fr.becpg.repo.web.scripts.admin;

import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

import org.springframework.context.ApplicationContext;

/**
 * The Class ReloadModelsWebScriptTest.
 *
 * @author querephi
 */
public class ReloadModelsWebScriptTest extends BaseWebScriptTest{		
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ReloadModelsWebScriptTest.class);
	
	/** The app ctx. */
	private  ApplicationContext appCtx = getServer().getApplicationContext();
	/* (non-Javadoc)
	 * @see org.alfresco.repo.web.scripts.BaseWebScriptTest#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
    	super.setUp();		
    	
    	logger.debug("ReloadModelsWebScriptTest::setUp");
    	
    }
    
    
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
    public void tearDown() throws Exception
    {	
        super.tearDown();        
    }		

	/**
	 * Test init repo.
	 *
	 * @throws Exception the exception
	 */
	public void testReloadModels() throws Exception {				
		
		logger.debug("test reload models webscript ");
		String url = "/becpg/admin/models/reload";
		Response response = sendRequest(new GetRequest(url), 200, "admin");
						
	}	
	
}
