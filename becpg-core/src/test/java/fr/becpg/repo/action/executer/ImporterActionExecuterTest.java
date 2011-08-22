/*
 * 
 */
package fr.becpg.repo.action.executer;

import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.BaseAlfrescoTestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import fr.becpg.common.RepoConsts;
import fr.becpg.repo.helper.TranslateHelper;

// TODO: Auto-generated Javadoc
/**
 * The Class ImporterActionExecuterTest.
 *
 * @author querephi
 */
public class ImporterActionExecuterTest  extends BaseAlfrescoTestCase {
	
	private static String FILENAME_IMPORT_CSV = "import.csv";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ImporterActionExecuterTest.class);
	
	/** The application context. */
	private static ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();	
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The file folder service. */
	private FileFolderService fileFolderService;
	
	/** The repository. */
	private Repository repository;
	
	private MimetypeService mimetypeService;
	
	/* (non-Javadoc)
	 * @see org.alfresco.util.BaseAlfrescoTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
    	super.setUp();		
    	
    	nodeService = (NodeService)applicationContext.getBean("NodeService");
    	fileFolderService = (FileFolderService)applicationContext.getBean("FileFolderService");
        authenticationComponent = (AuthenticationComponent)applicationContext.getBean("authenticationComponent");
        repository = (Repository)applicationContext.getBean("repositoryHelper");
        mimetypeService = (MimetypeService)applicationContext.getBean("mimetypeService");
    }
    
    
	/* (non-Javadoc)
	 * @see org.alfresco.util.BaseAlfrescoTestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception
    {
		try
        {
            authenticationComponent.clearCurrentSecurityContext();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            // Don't let this mask any previous exceptions
        }
        super.tearDown();

    }		

	/**
	 * Test add content to import.
	 *
	 * @throws Exception the exception
	 */
	public void testAddContentToImport() throws Exception{
			
    	transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
 			@Override
			public NodeRef execute() throws Throwable {
 				
 				NodeRef exchangeFolder = nodeService.getChildByName(repository.getCompanyHome(), ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_EXCHANGE));    	
 		    	if(exchangeFolder == null){
 		    		throw new Exception("Missing exchange folder.");    		
 		    	}
 		    	NodeRef importFolder = nodeService.getChildByName(exchangeFolder, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_IMPORT));    	
 		    	if(importFolder == null){
 		    		throw new Exception("Missing import folder.");    		
 		    	}
 		    	NodeRef importToTreatFolder = nodeService.getChildByName(importFolder, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_IMPORT_TO_TREAT));    	
 		    	if(importToTreatFolder == null){
 		    		throw new Exception("Missing import folder.");    		
 		    	}
 		    	
 		    	//Create file to import
 		    	Map<QName, Serializable> properties = new HashMap<QName, Serializable>();		
 		    	properties.put(ContentModel.PROP_NAME, FILENAME_IMPORT_CSV);
 		    	
 		    	NodeRef importNodeRef = nodeService.getChildByName(importToTreatFolder, ContentModel.ASSOC_CONTAINS, (String)properties.get(ContentModel.PROP_NAME));    	
 		    	if(importNodeRef != null){
 		    		nodeService.deleteNode(importNodeRef);   		
 		    	}    	
 		    	NodeRef contentNodeRef = nodeService.createNode(importToTreatFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), ContentModel.TYPE_CONTENT, properties).getChildRef();
 		    	
 		    	ContentWriter writer = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
 		    	logger.debug("import.csv");
 		    	InputStream in = ClassLoader.getSystemResourceAsStream("beCPG/import/Import.csv");			
 		    	
 		    	String mimetype = mimetypeService.guessMimetype(FILENAME_IMPORT_CSV); 		    	
 		    	ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
 				Charset charset = charsetFinder.getCharset(in, mimetype);
 				String encoding = charset.name(); 		    	
 		    	writer.setMimetype(mimetype);
 		    	writer.setEncoding(encoding);
 		    	writer.putContent(in);
 		    	
 				return null;

 			}},false,true);

	}
	
	/**
	 * Check that import goes in the failed folder even if there is IntegrityException
	 * @throws Exception
	 */
	public void testAddContentToImportThatFailed() throws Exception{
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
 			@Override
			public NodeRef execute() throws Throwable {
 				
 				NodeRef exchangeFolder = nodeService.getChildByName(repository.getCompanyHome(), ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_EXCHANGE));    	
 		    	if(exchangeFolder == null){
 		    		throw new Exception("Missing exchange folder.");    		
 		    	}
 		    	NodeRef importFolder = nodeService.getChildByName(exchangeFolder, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_IMPORT));    	
 		    	if(importFolder == null){
 		    		throw new Exception("Missing import folder.");    		
 		    	}
 		    	NodeRef importToTreatFolder = nodeService.getChildByName(importFolder, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_IMPORT_TO_TREAT));    	
 		    	if(importToTreatFolder == null){
 		    		throw new Exception("Missing import folder.");    		
 		    	}
 		    	
 		    	//Create file to import
 		    	Map<QName, Serializable> properties = new HashMap<QName, Serializable>();		
 		    	properties.put(ContentModel.PROP_NAME, "import.csv");
 		    	
 		    	NodeRef importNodeRef = nodeService.getChildByName(importToTreatFolder, ContentModel.ASSOC_CONTAINS, (String)properties.get(ContentModel.PROP_NAME));    	
 		    	if(importNodeRef != null){
 		    		nodeService.deleteNode(importNodeRef);   		
 		    	}    	
 		    	NodeRef contentNodeRef = nodeService.createNode(importToTreatFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), ContentModel.TYPE_CONTENT, properties).getChildRef();
 		    	
 		    	ContentWriter writer = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
 		    	logger.debug("Import-with-IntegrityException.csv");
 		    	InputStream in = ClassLoader.getSystemResourceAsStream("beCPG/import/Import-with-IntegrityException.csv");
 		    	
 		    	String mimetype = mimetypeService.guessMimetype(FILENAME_IMPORT_CSV);
 		    	ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
 				Charset charset = charsetFinder.getCharset(in, mimetype);
 				String encoding = charset.name(); 		    	
 		    	writer.setMimetype(mimetype);
 		    	writer.setEncoding(encoding);
 		    	writer.putContent(in);
 		    	
 				return null;

 			}},false,true);
		
		

	}

}
