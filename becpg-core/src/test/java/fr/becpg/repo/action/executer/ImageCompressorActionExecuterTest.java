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
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.content.transform.magick.ImageTransformationOptions;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.BaseAlfrescoTestCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

/**
 * The Class ImageCompressorActionExecuterTest.
 *
 * @author querephi
 */
public class ImageCompressorActionExecuterTest  extends BaseAlfrescoTestCase {
	
	private static String PATH_IMAGECOMPRESSOR_FOLDER = "ImageCompressorFolder"; 
	private static String FILENAME_IMAGE = "image.jpg";
	private static String FILENAME_IMAGE_TRANSFORMED = "imageTrasformed.jpg";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ImageCompressorActionExecuterTest.class);
	
	/** The application context. */
	private static ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();	
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The file folder service. */
	private FileFolderService fileFolderService;
	
	private MimetypeService mimetypeService;
	
	private Repository repositoryHelper;   
	
	private ContentTransformer imageMagickContentTransformer;
	
	/* (non-Javadoc)
	 * @see org.alfresco.util.BaseAlfrescoTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
    	super.setUp();		
    	
    	nodeService = (NodeService)applicationContext.getBean("NodeService");
    	fileFolderService = (FileFolderService)applicationContext.getBean("FileFolderService");
        authenticationComponent = (AuthenticationComponent)applicationContext.getBean("authenticationComponent");
        mimetypeService = (MimetypeService)applicationContext.getBean("mimetypeService");
        repositoryHelper = (Repository)applicationContext.getBean("repositoryHelper");
        imageMagickContentTransformer = (ContentTransformer)applicationContext.getBean("transformer.ImageMagick");
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

	public void testImageCompression() throws Exception{
			
    	transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
 			@Override
			public NodeRef execute() throws Throwable {
 				
 				/*-- Create test folder --*/
				NodeRef folderNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_IMAGECOMPRESSOR_FOLDER);			
				if(folderNodeRef != null)
				{
					fileFolderService.delete(folderNodeRef);    		
				}			
				folderNodeRef = fileFolderService.create(repositoryHelper.getCompanyHome(), PATH_IMAGECOMPRESSOR_FOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
 				
 		    	//Create image
 		    	Map<QName, Serializable> properties = new HashMap<QName, Serializable>();		
 		    	properties.put(ContentModel.PROP_NAME, FILENAME_IMAGE); 		    	
 		    	NodeRef importNodeRef = nodeService.getChildByName(folderNodeRef, ContentModel.ASSOC_CONTAINS, (String)properties.get(ContentModel.PROP_NAME));    	
 		    	if(importNodeRef != null){
 		    		nodeService.deleteNode(importNodeRef);   		
 		    	}    	
 		    	importNodeRef = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), ContentModel.TYPE_CONTENT, properties).getChildRef();
 		    	
 		    	ContentWriter writer = contentService.getWriter(importNodeRef, ContentModel.PROP_CONTENT, true);
 		    	InputStream in = ClassLoader.getSystemResourceAsStream("beCPG/import/images/sushi dorade/produit.jpg");			
 		    	
 		    	try{
 		    		String mimetype = mimetypeService.guessMimetype(FILENAME_IMAGE); 		    	
 	 		    	ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
 	 				Charset charset = charsetFinder.getCharset(in, mimetype);
 	 				String encoding = charset.name(); 		    	
 	 		    	writer.setMimetype(mimetype);
 	 		    	writer.setEncoding(encoding);
 	 		    	writer.putContent(in);
 		    	}
 		    	finally{
 		    		IOUtils.closeQuietly(in);
 		    	} 	
 		    	
 		    	writer = contentService.getWriter(importNodeRef, ContentModel.PROP_CONTENT, true);
 		    	
 		    	// Get the content reader
 		        ContentReader contentReader = contentService.getReader(importNodeRef, ContentModel.PROP_CONTENT); 		        
 		    	
 		        ImageTransformationOptions imageOptions = new ImageTransformationOptions();
 		        imageOptions.setCommandOptions("-resize 100x100");
 		    	imageMagickContentTransformer.transform(contentReader, writer, imageOptions);
 		    	
 		    	logger.debug("initialSize: " + contentReader.getSize() + " - afterSize: " + writer.getSize());
 				return null;

 			}},false,true);

	}
	
}
