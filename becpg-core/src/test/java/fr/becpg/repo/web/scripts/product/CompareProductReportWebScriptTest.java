/*
 * 
 */
package fr.becpg.repo.web.scripts.product;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.ProductDictionaryService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.DeclarationType;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.report.client.ReportFormat;

/**
 * The Class CompareProductReportWebScriptTest.
 *
 * @author querephi
 */
public class CompareProductReportWebScriptTest extends BaseWebScriptTest{

	private static final String COMPARE_ENTITIES_REPORT_PATH = "beCPG/birt/CompareEntities.rptdesign";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(CompareProductReportWebScriptTest.class);
	
	
	/** The PAT h_ testfolder. */
	private static String PATH_TESTFOLDER = "TestFolder";
	
	/** The Constant USER_ADMIN. */
	private static final String USER_ADMIN = "admin";
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The file folder service. */
	private FileFolderService fileFolderService;
	
    /** The authentication component. */
    private AuthenticationComponent authenticationComponent;
    

    /** The product dao. */
    private AlfrescoRepository<ProductData> alfrescoRepository;
    
    /** The product dictionary service. */
    private ProductDictionaryService productDictionaryService;
    
    /** The transaction service. */
    private TransactionService transactionService;
    
    /** The repository. */
    private Repository repositoryHelper;
    
    
    private RepoService repoService;
    
    private ReportTplService reportTplService;
    
    private CheckOutCheckInService checkOutCheckInService;
    
	/** The folder node ref. */
	private NodeRef folderNodeRef;
	
	/** The local s f1 node ref. */
	private NodeRef  localSF1NodeRef;
    
    /** The raw material1 node ref. */
    private NodeRef  rawMaterial1NodeRef;
    
    /** The raw material2 node ref. */
    private NodeRef  rawMaterial2NodeRef;
    
    /** The local s f2 node ref. */
    private NodeRef  localSF2NodeRef;
    
    /** The raw material3 node ref. */
    private NodeRef  rawMaterial3NodeRef;
    
    /** The raw material4 node ref. */
    private NodeRef  rawMaterial4NodeRef;
    
    
    /** The fp1 node ref. */
    private NodeRef fpNodeRef;
    
    /** The costs. */
    private List<NodeRef> costs = new ArrayList<NodeRef>();
	
	/** The allergens. */
	private List<NodeRef> allergens = new ArrayList<NodeRef>();
	
	/* (non-Javadoc)
	 * @see org.alfresco.repo.web.scripts.BaseWebScriptTest#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
				
		nodeService = (NodeService) getServer().getApplicationContext().getBean("NodeService");
		fileFolderService = (FileFolderService) getServer().getApplicationContext().getBean("FileFolderService");
		authenticationComponent = (AuthenticationComponent) getServer().getApplicationContext().getBean("authenticationComponent");
		alfrescoRepository = (AlfrescoRepository) getServer().getApplicationContext().getBean("alfrescoRepository");
		productDictionaryService = (ProductDictionaryService) getServer().getApplicationContext().getBean("productDictionaryService");
		transactionService = (TransactionService) getServer().getApplicationContext().getBean("transactionService");
		repositoryHelper = (Repository) getServer().getApplicationContext().getBean("repositoryHelper");
		repoService = (RepoService) getServer().getApplicationContext().getBean("repoService");
		reportTplService = (ReportTplService) getServer().getApplicationContext().getBean("reportTplService");
		checkOutCheckInService = (CheckOutCheckInService) getServer().getApplicationContext().getBean("checkOutCheckInService");
		
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
 * Inits the objects.
 */
private void initObjects(){
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			@Override
			public NodeRef execute() throws Throwable {
				
				/*-- Create test folder --*/
				folderNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);			
				if(folderNodeRef != null)
				{
					fileFolderService.delete(folderNodeRef);    		
				}			
				folderNodeRef = fileFolderService.create(repositoryHelper.getCompanyHome(), PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
													
		
				//costs
				NodeRef systemFolder = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
				NodeRef costFolder = nodeService.getChildByName(systemFolder, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_COSTS));
				if(costFolder == null){
					costFolder = fileFolderService.create(systemFolder, TranslateHelper.getTranslatedPath(RepoConsts.PATH_COSTS), ContentModel.TYPE_FOLDER).getNodeRef();
				}
				List<FileInfo> costsFileInfo = fileFolderService.listFiles(costFolder);
				if(costsFileInfo.size() == 0){
					for(int i=0 ; i<10 ; i++)
			    	{    		
			    		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			    		properties.put(ContentModel.PROP_NAME, "Cost " + i);
			    		properties.put(BeCPGModel.PROP_COSTCURRENCY, "€");
			    		ChildAssociationRef childAssocRef = nodeService.createNode(costFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_COST, properties);
			    		costs.add(childAssocRef.getChildRef());
			    	}
				}
				else{
					for(FileInfo fileInfo : costsFileInfo){
						costs.add(fileInfo.getNodeRef());
					}
				}
				
				//allergens
				NodeRef allergensFolder = nodeService.getChildByName(systemFolder, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_ALLERGENS));
				if(allergensFolder == null){
					allergensFolder = fileFolderService.create(systemFolder, TranslateHelper.getTranslatedPath(RepoConsts.PATH_ALLERGENS), ContentModel.TYPE_FOLDER).getNodeRef();
				}
				List<FileInfo> allergensFileInfo = fileFolderService.listFiles(allergensFolder);
				if(allergensFileInfo.size() == 0){
					for(int i=0 ; i<10 ; i++)
			    	{    		
			    		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			    		properties.put(ContentModel.PROP_NAME, "Allergen " + i);
			    		ChildAssociationRef childAssocRef = nodeService.createNode(allergensFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ALLERGEN, properties);
			    		allergens.add(childAssocRef.getChildRef());
			    	}
				}
				else{
					for(FileInfo fileInfo : allergensFileInfo){
						allergens.add(fileInfo.getNodeRef());
					}
				}							
			
				/*-- Create raw materials --*/
				logger.debug("/*-- Create raw materials --*/");
				Collection<QName> dataLists = productDictionaryService.getDataLists();
				/*-- Raw material 1 --*/
				RawMaterialData rawMaterial1 = new RawMaterialData();
				rawMaterial1.setName("Raw material 1");
				rawMaterial1.setLegalName("Legal Raw material 1");				
				rawMaterial1NodeRef = alfrescoRepository.create(folderNodeRef, rawMaterial1).getNodeRef();
				
				/*-- Raw material 2 --*/
				RawMaterialData rawMaterial2 = new RawMaterialData();
				rawMaterial2.setName("Raw material 2");
				rawMaterial2.setLegalName("Legal Raw material 2");					
				rawMaterial2NodeRef = alfrescoRepository.create(folderNodeRef, rawMaterial2).getNodeRef();
				
				/*-- Raw material 3 --*/
				RawMaterialData rawMaterial3 = new RawMaterialData();
				rawMaterial3.setName("Raw material 3");
				rawMaterial3.setLegalName("Legal Raw material 3");				
				rawMaterial3NodeRef = alfrescoRepository.create(folderNodeRef, rawMaterial3).getNodeRef();
				
				/*-- Raw material 4 --*/
				RawMaterialData rawMaterial4 = new RawMaterialData();
				rawMaterial4.setName("Raw material 4");
				rawMaterial4.setLegalName("Legal Raw material 4");					
				rawMaterial4NodeRef = alfrescoRepository.create(folderNodeRef, rawMaterial4).getNodeRef();
				
				/*-- Raw material 5 --*/
				RawMaterialData rawMaterial5 = new RawMaterialData();
				rawMaterial5.setName("Raw material 5");
				rawMaterial5.setLegalName("Legal Raw material 5");				
				alfrescoRepository.create(folderNodeRef, rawMaterial5).getNodeRef();
				
				/*-- Local semi finished product 1 --*/
				LocalSemiFinishedProductData localSF1 = new LocalSemiFinishedProductData();
				localSF1.setName("Local semi finished 1");
				localSF1.setLegalName("Legal Local semi finished 1");
				localSF1NodeRef = alfrescoRepository.create(folderNodeRef, localSF1).getNodeRef();
				
				/*-- Local semi finished product 1 --*/
				LocalSemiFinishedProductData localSF2 = new LocalSemiFinishedProductData();
				localSF2.setName("Local semi finished 2");
				localSF2.setLegalName("Legal Local semi finished 2");							
				localSF2NodeRef = alfrescoRepository.create(folderNodeRef, localSF2).getNodeRef();	
		
		return null;
		
			}},false,true);
		
	}

	/**
	 * Inits the tests.
	 * @throws IOException 
	 */
	private void initTests() throws IOException{
		
		NodeRef systemFolder = repoService.createFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_SYSTEM, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
	   	NodeRef reportsFolder = repoService.createFolderByPath(systemFolder, RepoConsts.PATH_REPORTS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS));
	   	NodeRef compareReportFolder = repoService.createFolderByPath(reportsFolder, RepoConsts.PATH_REPORTS_COMPARE_PRODUCTS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS_COMPARE_PRODUCTS));
	   	
	   	// compare report
		reportTplService.createTplRptDesign(compareReportFolder, 
											TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS_COMPARE_PRODUCTS), 
											COMPARE_ENTITIES_REPORT_PATH, 
											ReportType.System, 	
											ReportFormat.PDF,
											null, 
											true, 
											true, 
											false);
	
	}
		
		/**
		 * Test compare products.
		 */
		public void testCompareProducts(){

			//TODO : merge CompareProductReportWebScript avec CompareProductServiceTest => beaucoup de code en commun !
			// init objects
			initObjects();
		
			transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
				@Override
				public NodeRef execute() throws Throwable {
										
					//Create comparison product report
					initTests();
								
					 Collection<QName> dataLists = productDictionaryService.getDataLists();
					
					logger.debug("createRawMaterial 1");
					
					FinishedProductData fp1 = new FinishedProductData();
					fp1.setName("FP 1");			
			
					//Costs
					List<CostListDataItem> costList = new ArrayList<CostListDataItem>();		    		
					for(int j=0 ; j<costs.size() ; j++)
					{		    			
						CostListDataItem costListItemData = new CostListDataItem(null, 12.2d, "€/kg", null, costs.get(j), false);
						costList.add(costListItemData);
					}		
					fp1.setCostList(costList);
					
					// create an MP for the allergens
					RawMaterialData allergenRawMaterial = new RawMaterialData();
					allergenRawMaterial.setName("MP allergen");
					NodeRef allergenRawMaterialNodeRef = alfrescoRepository.create(folderNodeRef, allergenRawMaterial).getNodeRef();
					
					//Allergens
					List<AllergenListDataItem> allergenList = new ArrayList<AllergenListDataItem>();		    		
					for(int j=0 ; j<allergens.size() ; j++)
					{		    			
						List<NodeRef> voluntarySources = new ArrayList<NodeRef>();
						voluntarySources.add(allergenRawMaterialNodeRef);
						
						AllergenListDataItem allergenListItemData = new AllergenListDataItem(null, true, false, voluntarySources, null, allergens.get(j), false);
						allergenList.add(allergenListItemData);
					}		
					fp1.setAllergenList(allergenList);
						
					List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
					compoList.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF1NodeRef));
					compoList.add(new CompoListDataItem(null, 2, 1d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
					compoList.add(new CompoListDataItem(null, 2, 2d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
					compoList.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF2NodeRef));
					compoList.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
					fp1.setCompoList(compoList);
					
					fpNodeRef = alfrescoRepository.create(folderNodeRef, fp1).getNodeRef();		
					
					Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>();
					aspectProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
					nodeService.addAspect(fpNodeRef, ContentModel.ASPECT_VERSIONABLE, aspectProperties);
					
					// CheckOut/CheckIn
					NodeRef workingCopyNodeRef = checkOutCheckInService.checkout(fpNodeRef);
					Map<String, Serializable> properties = new HashMap<String, Serializable>();
					properties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
					NodeRef fpv1NodeRef = checkOutCheckInService.checkin(workingCopyNodeRef, properties);
					
					// CheckOut
					workingCopyNodeRef = checkOutCheckInService.checkout(fpv1NodeRef);
					
					logger.debug("update workingCopy");
					
					ProductData workingCopy = alfrescoRepository.findOne(workingCopyNodeRef); 
					workingCopy.setName("FP new version");			
			
					//Costs
					costList = new ArrayList<CostListDataItem>();		    		
					for(int j=0 ; j<costs.size() ; j++)
					{		    			
						CostListDataItem costListItemData = new CostListDataItem(null, 12.4d, "$/kg", null, costs.get(j), false);
						costList.add(costListItemData);
					}		
					workingCopy.setCostList(costList);
						
					//Allergens
					allergenList = new ArrayList<AllergenListDataItem>();		    		
					for(int j=0 ; j<allergens.size() ; j++)
					{		    			
						List<NodeRef> allSources = new ArrayList<NodeRef>();
						allSources.add(allergenRawMaterialNodeRef);
						AllergenListDataItem allergenListItemData = null;
						
						if(j < 5){
							allergenListItemData = new AllergenListDataItem(null, true, false, allSources, null, allergens.get(j), false);
						}
						else{
							allergenListItemData = new AllergenListDataItem(null, false, true, null, allSources, allergens.get(j), false);
						}						
						
						allergenList.add(allergenListItemData);
					}		
					workingCopy.setAllergenList(allergenList);
					
					compoList = new ArrayList<CompoListDataItem>();
					compoList.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF1NodeRef));
					compoList.add(new CompoListDataItem(null, 2, 2d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
					compoList.add(new CompoListDataItem(null, 2, 2d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
					compoList.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF2NodeRef));
					compoList.add(new CompoListDataItem(null, 2, 2d, 0d, 0d, CompoListUnit.P, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
					compoList.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial4NodeRef));
					workingCopy.setCompoList(compoList);
					
					alfrescoRepository.save(workingCopy);
					
					properties = new HashMap<String, Serializable>();
					properties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
					NodeRef fpv2NodeRef = checkOutCheckInService.checkin(workingCopyNodeRef, properties);
					assertEquals("check version", "2.0", nodeService.getProperty(fpv2NodeRef, ContentModel.PROP_VERSION_LABEL));
					
					return null;
					
				}},false,true);
			
			try{
			
				String url = String.format("/becpg/entity/compare/%s/%s/Produit", fpNodeRef.toString().replace("://", "/"), "1.0");;
				Response response = sendRequest(new GetRequest(url), 200, "admin");
				
				logger.debug("response: " + response.getContentAsString());
			}
			catch(Exception e){
				logger.error("Failed to execute webscript", e);
			}
			
	   }
	
	
    	
}
