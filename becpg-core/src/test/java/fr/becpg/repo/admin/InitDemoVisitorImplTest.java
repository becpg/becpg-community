/*
 * 
 */
package fr.becpg.repo.admin;

import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.test.RepoBaseTestCase;

// TODO: Auto-generated Javadoc
/**
 * The Class InitDemoVisitorImplTest.
 *
 * @author querephi
 */
public class InitDemoVisitorImplTest extends RepoBaseTestCase {	
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(InitDemoVisitorImplTest.class);
	
	/** The site service. */
	private SiteService siteService;
	
	
	/** The init repo visitor. */
	private InitVisitor initRepoVisitor;
	
	/** The init demo visitor. */
	private InitVisitor initDemoVisitor;
	
	/** The repository. */
	private Repository repository;
	
	/** The authentication component. */
	private AuthenticationComponent authenticationComponent;
	
	
	/** The person service. */
	private PersonService personService;
	
	/* (non-Javadoc)
	 * @see org.alfresco.util.BaseAlfrescoTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
    	super.setUp();		
    	
    	logger.debug("InitSiteVisitorImplTest::setUp");
    	
    	siteService = (SiteService)ctx.getBean("siteService");
    	nodeService = (NodeService)ctx.getBean("nodeService");
    	fileFolderService = (FileFolderService)ctx.getBean("FileFolderService");
    	initRepoVisitor = (InitVisitor)ctx.getBean("initRepoVisitor");
    	initDemoVisitor = (InitVisitor)ctx.getBean("initDemoVisitor");
    	repository = (Repository)ctx.getBean("repositoryHelper");
    	authenticationComponent = (AuthenticationComponent)ctx.getBean("authenticationComponent");
    	personService	 = (PersonService)ctx.getBean("personService");
    	
    	//Authenticate as user
	    authenticationComponent.setCurrentUser("admin");
    }
    
    
	/* (non-Javadoc)
	 * @see org.alfresco.util.BaseAlfrescoTestCase#tearDown()
	 */
	@Override
    public void tearDown() throws Exception
    {	
        super.tearDown();
        
    }	
	
	
	/**
	 * Test init repo and demo.
	 */
	public void testInitRepoAndDemo(){
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			@Override
			public NodeRef execute() throws Throwable {
				
				//clearRepo();
				initRepoAndDemo();
				
				return null;

			}},false,true);
	}
	
	
	
	/**
	 * Inits the repo and demo.
	 */
	private void initRepoAndDemo(){
		
		logger.debug("visit repo");
		initRepoVisitor.visitContainer(repository.getCompanyHome());
		
		logger.debug("visit demo");
		initDemoVisitor.visitContainer(repository.getCompanyHome());
		
		/*-- Check sites--*/
		logger.debug("/*-- Check sites--*/");
		SiteInfo siteRD = siteService.getSite(InitDemoVisitorImpl.SITE_RD);
		assertNotNull("Site RD should not be null", siteRD);
		
		SiteInfo siteQuality = siteService.getSite(InitDemoVisitorImpl.SITE_QUALITY);
		assertNotNull("Site Quality should not be null", siteQuality);
		
		SiteInfo sitePurchasing = siteService.getSite(InitDemoVisitorImpl.SITE_PURCHASING);
		assertNotNull("Site Purchasing should not be null", sitePurchasing);
		
		/*-- Check persons--*/
		logger.debug("/*-- Check persons--*/");
		String user = I18NUtil.getMessage(InitDemoVisitorImpl.LOCALIZATION_DEMO_USER);
		String mgr = I18NUtil.getMessage(InitDemoVisitorImpl.LOCALIZATION_DEMO_MGR);
		
		String groupSystem = I18NUtil.getMessage(InitDemoVisitorImpl.LOCALIZATION_DEMO_GROUP_SYSTEM);
		String groupRD = I18NUtil.getMessage(InitDemoVisitorImpl.LOCALIZATION_DEMO_GROUP_RD);
		String groupQuality = I18NUtil.getMessage(InitDemoVisitorImpl.LOCALIZATION_DEMO_GROUP_QUALITY);
		String groupPurchasing = I18NUtil.getMessage(InitDemoVisitorImpl.LOCALIZATION_DEMO_GROUP_PURCHASING);
		String groupProductReviewer = I18NUtil.getMessage(InitDemoVisitorImpl.LOCALIZATION_DEMO_GROUP_PRODUCTREVIEWER);
		
		assertNotNull("Person should exist", personService.getPerson(mgr + groupSystem));
		assertNotNull("Person should exist", personService.getPerson(mgr + groupRD));
		assertNotNull("Person should exist", personService.getPerson(user + groupRD));
		assertNotNull("Person should exist", personService.getPerson(mgr + groupQuality));
		assertNotNull("Person should exist", personService.getPerson(user + groupQuality));
		assertNotNull("Person should exist", personService.getPerson(mgr + groupPurchasing));
		assertNotNull("Person should exist", personService.getPerson(user + groupPurchasing));
		assertNotNull("Person should exist", personService.getPerson(mgr + groupProductReviewer));
		
	}
	
}
