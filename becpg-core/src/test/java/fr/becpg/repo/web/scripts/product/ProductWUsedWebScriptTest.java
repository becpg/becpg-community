/*
 * 
 */
package fr.becpg.repo.web.scripts.product;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.DeclarationType;
import fr.becpg.repo.repository.AlfrescoRepository;

// TODO: Auto-generated Javadoc
/**
 * The Class ProductWUsedWebScriptTest.
 *
 * @author querephi
 */
public class ProductWUsedWebScriptTest extends BaseWebScriptTest{

	/** The logger. */
	private static Log logger = LogFactory.getLog(ProductWUsedWebScriptTest.class);
	
	/** The Constant PATH_TEMPFOLDER. */
	private static final String PATH_TEMPFOLDER = "TempFolder";
	
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
    
    /** The transaction service. */
    private TransactionService transactionService;
    
    /** The repository helper. */
    private Repository repositoryHelper;
    
	/** The raw material node ref. */
	private NodeRef rawMaterialNodeRef = null;
	
	/** The finished product node ref. */
	private NodeRef finishedProductNodeRef = null;
	
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
		transactionService = (TransactionService) getServer().getApplicationContext().getBean("transactionService");
		repositoryHelper = (Repository) getServer().getApplicationContext().getBean("repositoryHelper");
		
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
	 * Testget product wused.
	 *
	 * @throws Exception the exception
	 */
	public void testgetProductWused() throws Exception {
		
		
		 transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
				@Override
				public NodeRef execute() throws Throwable {					   
			
					/*-- create folders --*/
					logger.debug("/*-- create folders --*/");
					NodeRef tempFolder = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TEMPFOLDER);    	
			    	if(tempFolder != null){
			    		fileFolderService.delete(tempFolder);    		
			    	}
			    	tempFolder = fileFolderService.create(repositoryHelper.getCompanyHome(), PATH_TEMPFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
			    	
			    	/*-- Create raw material --*/
	 				logger.debug("/*-- Create raw material --*/");
	 				RawMaterialData rawMaterial = new RawMaterialData();
	 				rawMaterial.setName("Raw material");
	 				rawMaterialNodeRef = alfrescoRepository.create(tempFolder, rawMaterial).getNodeRef();
	 				LocalSemiFinishedProductData lSF = new LocalSemiFinishedProductData();
	 				lSF.setName("Local semi finished");
	 				NodeRef lSFNodeRef = alfrescoRepository.create(tempFolder, lSF).getNodeRef();
	 				 				 			
	 				/*-- Create finished product --*/
	 				logger.debug("/*-- Create finished product --*/");
	 				FinishedProductData finishedProduct = new FinishedProductData();
	 				finishedProduct.setName("Finished Product");
	 				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>(); 				
	 				compoList.add(new CompoListDataItem(null, (CompoListDataItem)null, 1d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Omit, lSFNodeRef));
	 				compoList.add(new CompoListDataItem(null, compoList.get(0), 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Omit, rawMaterialNodeRef));
					finishedProduct.getCompoListView().setCompoList(compoList); 				
					Collection<QName> dataLists = new ArrayList<QName>();		
					dataLists.add(BeCPGModel.TYPE_COMPOLIST);
	 				finishedProductNodeRef = alfrescoRepository.create(tempFolder, finishedProduct).getNodeRef();
	 				
	 				logger.debug("local semi finished: " + lSFNodeRef);
	 				logger.debug("finishedProductNodeRef: " + finishedProductNodeRef);
	 				
					return null;

				}},false,true);
		 
			//Call webscript on raw material
			String url = "/becpg/entity/datalists/data/node?entityNodeRef="+rawMaterialNodeRef.toString()+"&itemType=bcpg%3AcompoList&dataListName=WUsed";
			String data = "{\"fields\":[\"bcpg_costListCost\",\"bcpg_costListValue\",\"bcpg_costListUnit\"],\"filter\":{\"filterId\":\"all\",\"filterData\":\"\"}}";
			logger.debug("url : " + url);				

			Response response = sendRequest(new PostRequest(url, data, "application/json"), 200, "admin");
			logger.debug("content : " + response.getContentAsString());
			response = sendRequest(new PostRequest(url, data, "application/json"), 200, "admin");
			logger.debug("content : " + response.getContentAsString());			
		

    }
    	
}
