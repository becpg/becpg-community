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

// TODO: Auto-generated Javadoc
/**
 * The Class AdminModuleWebScriptTest.
 *
 * @author querephi
 */
public class AdminModuleWebScriptTest extends BaseWebScriptTest{		
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(AdminModuleWebScriptTest.class);
	/** The app ctx. */
	private  ApplicationContext appCtx = getServer().getApplicationContext();
	
	/* (non-Javadoc)
	 * @see org.alfresco.repo.web.scripts.BaseWebScriptTest#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
    	super.setUp();		
    	
    	logger.debug("AdminModuleWebScriptTest::setUp");
    	
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
	public void testInitRepo() throws Exception {				
		
		//Init-repo
		logger.debug("test init repo webscript ");
		String url = "/becpg/admin/repository/init-repo";
		Response response = sendRequest(new GetRequest(url), 200, "admin");
						
	}	
	
}
