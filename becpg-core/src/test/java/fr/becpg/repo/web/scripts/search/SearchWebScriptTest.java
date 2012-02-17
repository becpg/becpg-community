package fr.becpg.repo.web.scripts.search;

import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

// TODO: Auto-generated Javadoc
/**
 * The Class ExportSearchWebScriptTest.
 *
 * @author querephi
 */
public class SearchWebScriptTest extends BaseWebScriptTest{

	/** The logger. */
	private static Log logger = LogFactory.getLog(SearchWebScriptTest.class);
	
	
	
	/** The Constant USER_ADMIN. */
	private static final String USER_ADMIN = "admin";
	
    /** The authentication component. */
    private AuthenticationComponent authenticationComponent;
  
 
	
	/* (non-Javadoc)
	 * @see org.alfresco.repo.web.scripts.BaseWebScriptTest#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
				
		
		authenticationComponent = (AuthenticationComponent) getServer().getApplicationContext().getBean("authenticationComponent");
		
		
	    // Authenticate as user
	    this.authenticationComponent.setCurrentUser(USER_ADMIN);
		
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
		 * Test get export search tpls.
		 */
	public void testSearch(){
		
		
		
		try{
		
			String url = "/becpg/search?site=&term=MP*&tag=&maxResults=251&sort=&query=&repo=true&metadataFields=bcpg_legalName%2Cbcpg_productHierarchy1%2Cbcpg_productHierarchy2%2Cbcpg_productState%2Cbcpg_code%2Cbcpg_erpCode%2Cbcpg_eanCode%2Cbcpg_suppliers%2Cbcpg_clients";
			
			Response response = sendRequest(new GetRequest(url), 200, "admin");
			
			logger.debug("response: " + response.getContentAsString());
		}
		catch(Exception e){
			logger.error("Failed to execute webscript", e);
		}
			
	   }
	
	
    	
}
