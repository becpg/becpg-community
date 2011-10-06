/*
 * 
 */
package fr.becpg.repo.admin;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.BaseAlfrescoTestCase;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import fr.becpg.common.RepoConsts;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemProductType;
import fr.becpg.model.SystemState;
import fr.becpg.repo.admin.InitVisitor;
import fr.becpg.repo.admin.SystemGroup;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.ProductDictionaryService;
import fr.becpg.test.RepoBaseTestCase;

// TODO: Auto-generated Javadoc
/**
 * The Class InitSiteVisitorImplTest.
 *
 * @author querephi
 */
public class InitSiteVisitorImplTest extends RepoBaseTestCase {
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(InitSiteVisitorImplTest.class);
	
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
	
	/** The init site visitor. */
	private InitVisitor initSiteVisitor;
	
	/** The repository. */
	private Repository repository;
	
	/** The authentication component. */
	private AuthenticationComponent authenticationComponent;
	
	/** The product dictionary service. */
	private ProductDictionaryService productDictionaryService;
	
	/** The authority service. */
	private AuthorityService authorityService;
	
	/** The site info. */
	private SiteInfo siteInfo;
	
	private RepoService repoService;
	
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
    	initSiteVisitor = (InitVisitor)appCtx.getBean("initSiteVisitor");
    	repository = (Repository)appCtx.getBean("repositoryHelper");
    	authenticationComponent = (AuthenticationComponent)appCtx.getBean("authenticationComponent");
    	productDictionaryService = (ProductDictionaryService)appCtx.getBean("productDictionaryService");
    	authorityService = (AuthorityService)appCtx.getBean("authorityService");
    	repoService = (RepoService)appCtx.getBean("repoService");
    	
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
	 * Test init repo and site.
	 */
	public void testInitRepoAndSite(){
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			@Override
			public NodeRef execute() throws Throwable {
				
				//clearRepo();
				initRepoAndSite();
				
				return null;

			}},false,true);
	}
	
	/**
	 * Clear repo.
	 */
	private void clearRepo(){
		
		// Clear system folder	
		NodeRef systemNodeRef = repoService.getFolderByPath(repository.getCompanyHome(), RepoConsts.PATH_SYSTEM);
		if(systemNodeRef != null){
			nodeService.deleteNode(systemNodeRef);
		}
		
		// Clear exchange folder	
		NodeRef exchangeNodeRef = repoService.getFolderByPath(repository.getCompanyHome(), RepoConsts.PATH_EXCHANGE);
		if(exchangeNodeRef != null){
			nodeService.deleteNode(exchangeNodeRef);
		}
				
		// Clear products folder	
		NodeRef productsNodeRef = repoService.getFolderByPath(repository.getCompanyHome(), RepoConsts.PATH_PRODUCTS);
		if(productsNodeRef != null){
			logger.debug("delete products folder");
			nodeService.deleteNode(productsNodeRef);				
		}
		
		// Clear company folder
		NodeRef companysNodeRef = repoService.getFolderByPath(repository.getCompanyHome(), RepoConsts.PATH_COMPANIES);
		if(companysNodeRef != null){
			logger.debug("delete companies folder");
			nodeService.deleteNode(companysNodeRef);				
		}
		
	}
	
	/**
	 * Inits the repo and site.
	 */
	private void initRepoAndSite(){
		
		// Clear repo
		clearRepo();
				
		//init repo and hierarchy lists
		initRepoAndHierarchyLists();
		
		//check groups
		String [] groups = {SystemGroup.RDUser.toString(), SystemGroup.RDMgr.toString(), SystemGroup.QualityUser.toString(), SystemGroup.QualityMgr.toString(), SystemGroup.PurchasingUser.toString(), SystemGroup.PurchasingMgr.toString(), SystemGroup.ProductReviewer.toString()};
		for(String group : groups)
			assertEquals("Group not found" + group, true, authorityService.authorityExists(PermissionService.GROUP_PREFIX + group));
		
					
		//create site
		try{
		logger.debug("create site");
		siteInfo = siteService.createSite("myPreset", GUID.generate(), "R&D site", "This site iss used by the R&D team.", SiteVisibility.PRIVATE);
		logger.debug("site created, name: " + siteInfo.getShortName());
		}
		catch(Exception e){
			logger.error("Failed to create site", e);					
		}					
		assertNotNull("siteInfo", siteInfo);		
		
		//initialize site
		NodeRef docLibNodeRef = siteService.getContainer(siteInfo.getShortName(), RepoConsts.CONTAINER_DOCUMENT_LIBRARY);
		if(docLibNodeRef == null){
			docLibNodeRef = siteService.createContainer(siteInfo.getShortName(), RepoConsts.CONTAINER_DOCUMENT_LIBRARY, null, null);
		}
		assertNotNull("documentLibraryNodeRef", docLibNodeRef);
		initSiteVisitor.visitContainer(docLibNodeRef);
		
		//check site
		String productsFolderName = TranslateHelper.getTranslatedPath(RepoConsts.PATH_PRODUCTS);
		NodeRef productsNodeRef = nodeService.getChildByName(docLibNodeRef, ContentModel.ASSOC_CONTAINS, productsFolderName);
		assertNotNull(String.format("Folder does not exist. folderName:%s", productsFolderName), productsNodeRef);
		
		/*-- Repo Products hierarchy --*/
		productsNodeRef = nodeService.getChildByName(repository.getCompanyHome(), ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_PRODUCTS));
		productDictionaryService.initializeRepoHierarchy(productsNodeRef);
		
		/*-- ToValidate --*/
		String folderName = productDictionaryService.getFolderName(SystemState.ToValidate);
		NodeRef productsToValidateNodeRef = nodeService.getChildByName(productsNodeRef, ContentModel.ASSOC_CONTAINS, folderName);
		assertNotNull(String.format("Folder does not exist. folderName:%s", folderName), productsToValidateNodeRef);
		
		//RawMaterial ToValidate
		folderName = productDictionaryService.getFolderName(SystemProductType.RawMaterial);
		NodeRef rawMaterialToValidateNodeRef = nodeService.getChildByName(productsToValidateNodeRef, ContentModel.ASSOC_CONTAINS, folderName);
		assertNotNull(String.format("Folder of raw materials ToValidate does not exist. folderName:%s", folderName), rawMaterialToValidateNodeRef);

		//Sea food, Fish, crustacean 
		NodeRef seaFoodNodeRef = nodeService.getChildByName(rawMaterialToValidateNodeRef, ContentModel.ASSOC_CONTAINS, HIERARCHY1_SEA_FOOD);
		assertNotNull("Folder of raw materials ToValidate sea food does not exist. folderName.", seaFoodNodeRef);
		NodeRef fishNodeRef = nodeService.getChildByName(seaFoodNodeRef, ContentModel.ASSOC_CONTAINS, HIERARCHY2_FISH);
		assertNotNull("Folder of raw materials ToValidate sea food, fish does not exist. folderName.", fishNodeRef);
		NodeRef crustaceanNodeRef = nodeService.getChildByName(seaFoodNodeRef, ContentModel.ASSOC_CONTAINS, HIERARCHY2_CRUSTACEAN);
		assertNotNull("Folder of raw materials ToValidate sea food, crustacean does not exist. folderName.", crustaceanNodeRef);			
		
		//FinishedProduct ToValidate
		folderName = productDictionaryService.getFolderName(SystemProductType.FinishedProduct);
		NodeRef finishedProductToValidateNodeRef = nodeService.getChildByName(productsToValidateNodeRef, ContentModel.ASSOC_CONTAINS, folderName);
		assertNotNull(String.format("Folder of finished products ToValidate does not exist. folderName:%s", folderName), finishedProductToValidateNodeRef);
		
		//Frozen, Pizza, Quiche 
		NodeRef frozenNodeRef = nodeService.getChildByName(finishedProductToValidateNodeRef, ContentModel.ASSOC_CONTAINS, HIERARCHY1_FROZEN);
		assertNotNull("Folder of finished product ToValidate frozen does not exist. folderName.", frozenNodeRef);
		NodeRef pizzaNodeRef = nodeService.getChildByName(frozenNodeRef, ContentModel.ASSOC_CONTAINS, HIERARCHY2_PIZZA);
		assertNotNull("Folder of finished product ToValidate frozen, pizza does not exist. folderName.", pizzaNodeRef);
		NodeRef quicheNodeRef = nodeService.getChildByName(frozenNodeRef, ContentModel.ASSOC_CONTAINS, HIERARCHY2_QUICHE);
		assertNotNull("Folder of finished product ToValidate frozen, quiche does not exist. folderName.", quicheNodeRef);
		
		/*-- Valid --*/
		folderName = productDictionaryService.getFolderName(SystemState.Valid);
		NodeRef productsValidNodeRef = nodeService.getChildByName(productsNodeRef, ContentModel.ASSOC_CONTAINS, folderName);
		assertNotNull(String.format("Folder does not exist. folderName:%s", folderName), productsValidNodeRef);
		
		//RawMaterial Valid
		folderName = productDictionaryService.getFolderName(SystemProductType.RawMaterial);
		NodeRef rawMaterialValidNodeRef = nodeService.getChildByName(productsValidNodeRef, ContentModel.ASSOC_CONTAINS, folderName);
		assertNotNull(String.format("Folder of raw materials Valid does not exist. folderName:%s", folderName), rawMaterialToValidateNodeRef);
		
		//Frozen, Pizza, Quiche 
		seaFoodNodeRef = nodeService.getChildByName(rawMaterialValidNodeRef, ContentModel.ASSOC_CONTAINS, HIERARCHY1_SEA_FOOD);
		assertNotNull("Folder of raw materials Valid sea food does not exist. folderName.", seaFoodNodeRef);
		fishNodeRef = nodeService.getChildByName(seaFoodNodeRef, ContentModel.ASSOC_CONTAINS, HIERARCHY2_FISH);
		assertNotNull("Folder of raw materials Valid sea food, fish does not exist. folderName.", fishNodeRef);
		crustaceanNodeRef = nodeService.getChildByName(seaFoodNodeRef, ContentModel.ASSOC_CONTAINS, HIERARCHY2_CRUSTACEAN);
		assertNotNull("Folder of raw materials Valid sea food, crustacean does not exist. folderName.", crustaceanNodeRef);
		
		//FinishedProduct Valid
		folderName = productDictionaryService.getFolderName(SystemProductType.FinishedProduct);
		NodeRef finishedProductValidNodeRef = nodeService.getChildByName(productsValidNodeRef, ContentModel.ASSOC_CONTAINS, folderName);
		assertNotNull(String.format("Folder of finished products Valid does not exist. folderName:%s", folderName), finishedProductValidNodeRef);

		//Frozen, Pizza, Quiche 
		frozenNodeRef = nodeService.getChildByName(finishedProductValidNodeRef, ContentModel.ASSOC_CONTAINS, HIERARCHY1_FROZEN);
		assertNotNull("Folder of finished product Valid frozen does not exist. folderName.", frozenNodeRef);
		pizzaNodeRef = nodeService.getChildByName(frozenNodeRef, ContentModel.ASSOC_CONTAINS, HIERARCHY2_PIZZA);
		assertNotNull("Folder of finished product Valid frozen, pizza does not exist. folderName.", pizzaNodeRef);
		quicheNodeRef = nodeService.getChildByName(frozenNodeRef, ContentModel.ASSOC_CONTAINS, HIERARCHY2_QUICHE);
		assertNotNull("Folder of finished product Valid frozen, quiche does not exist. folderName.", quicheNodeRef);
		
	}
	
}
