/*
 * 
 */
package fr.becpg.repo.admin;

import java.util.List;
import java.util.Locale;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.BaseAlfrescoTestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.common.RepoConsts;
import fr.becpg.repo.admin.InitDemoVisitorImpl;
import fr.becpg.repo.admin.InitVisitor;
import fr.becpg.repo.product.ProductDictionaryService;

// TODO: Auto-generated Javadoc
/**
 * The Class InitDemoVisitorImplTest.
 *
 * @author querephi
 */
public class InitDemoVisitorImplTest extends BaseAlfrescoTestCase {	
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(InitDemoVisitorImplTest.class);
	
	/** The app ctx. */
	private static ApplicationContext appCtx = ApplicationContextHelper.getApplicationContext();	
	
	/** The site service. */
	private SiteService siteService;
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The file folder service. */
	private FileFolderService fileFolderService;
	
	/** The init repo visitor. */
	private InitVisitor initRepoVisitor;
	
	/** The init demo visitor. */
	private InitVisitor initDemoVisitor;
	
	/** The repository. */
	private Repository repository;
	
	/** The authentication component. */
	private AuthenticationComponent authenticationComponent;
	
	/** The product dictionary service. */
	private ProductDictionaryService productDictionaryService;
	
	/** The person service. */
	private PersonService personService;
	
	/* (non-Javadoc)
	 * @see org.alfresco.util.BaseAlfrescoTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
    	super.setUp();		
    	
    	logger.debug("InitSiteVisitorImplTest::setUp");
    	
    	siteService = (SiteService)appCtx.getBean("siteService");
    	nodeService = (NodeService)appCtx.getBean("nodeService");
    	fileFolderService = (FileFolderService)appCtx.getBean("FileFolderService");
    	initRepoVisitor = (InitVisitor)appCtx.getBean("initRepoVisitor");
    	initDemoVisitor = (InitVisitor)appCtx.getBean("initDemoVisitor");
    	repository = (Repository)appCtx.getBean("repositoryHelper");
    	authenticationComponent = (AuthenticationComponent)appCtx.getBean("authenticationComponent");
    	productDictionaryService = (ProductDictionaryService)appCtx.getBean("productDictionaryService");
    	personService	 = (PersonService)appCtx.getBean("personService");
    	
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
	 * Clear repo.
	 */
	private void clearRepo(){
		
		// Clear repository		
		NodeRef systemNodeRef = nodeService.getChildByName(repository.getCompanyHome(), ContentModel.ASSOC_CONTAINS, RepoConsts.PATH_SYSTEM);
		List<FileInfo> folders = fileFolderService.listFolders(systemNodeRef);
		for(FileInfo fileInfo : folders)
			fileFolderService.delete(fileInfo.getNodeRef());
		
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
