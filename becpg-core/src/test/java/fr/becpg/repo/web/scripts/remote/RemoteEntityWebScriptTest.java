/*
 * 
 */
package fr.becpg.repo.web.scripts.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PutRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

// TODO: Auto-generated Javadoc
/**
 * The Class RemoteEntityWebScriptTest.
 * 
 * @author matthieu
 */
public class RemoteEntityWebScriptTest extends BaseWebScriptTest {

	/** The logger. */
	private static Log logger = LogFactory.getLog(RemoteEntityWebScriptTest.class);

	
	/** The Constant PATH_TEMPFOLDER. */
	private static final String PATH_TEMPFOLDER = "TempFolder";
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The file folder service. */
	private FileFolderService fileFolderService;

    /** The transaction service. */
    private TransactionService transactionService;
    
    /** The repository helper. */
    private Repository repositoryHelper;
    

    /** The authentication component. */
    private AuthenticationComponent authenticationComponent;
    
    
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.web.scripts.BaseWebScriptTest#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		nodeService = (NodeService) getServer().getApplicationContext().getBean("NodeService");
		fileFolderService = (FileFolderService) getServer().getApplicationContext().getBean("FileFolderService");		
		transactionService = (TransactionService) getServer().getApplicationContext().getBean("transactionService");
		repositoryHelper = (Repository) getServer().getApplicationContext().getBean("repositoryHelper");
		authenticationComponent = (AuthenticationComponent) getServer().getApplicationContext().getBean("authenticationComponent");

	    // Authenticate as user
	    this.authenticationComponent.setCurrentUser("admin");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	public void testCRUDEntity() throws Exception {


		NodeRef tempFolder =  transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
				@Override
				public NodeRef execute() throws Throwable {					   
			
					/*-- create folders --*/
					logger.debug("/*-- create folders --*/");
					
					NodeRef tempFolder = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TEMPFOLDER);    	
			    	if(tempFolder != null){
			    		fileFolderService.delete(tempFolder);    		
			    	}
			    	tempFolder = fileFolderService.create(repositoryHelper.getCompanyHome(), PATH_TEMPFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
			    	
					return tempFolder;

				}},false,true);
		 
		
		
		// Call webscript on raw material
		String url = "/becpg/remote/entity";

		Resource res = new ClassPathResource("beCPG/remote/entity.xml");
		Resource data = new ClassPathResource("beCPG/remote/data.xml");

		Response response = sendRequest(new PutRequest(url, convertStreamToString(res.getInputStream()), "application/xml"), 200, "admin");
		logger.debug("Resp : " + response.getContentAsString());
		
		
		
		final NodeRef nodeRef = parseNodeRef(response.getContentAsString());

		Assert.assertTrue(nodeService.exists(nodeRef));
		
		NodeRef imageNodeRef = null;
		for(FileInfo fi: fileFolderService.list(tempFolder)){
			logger.error("Create Image Folder : "+fi.getName()+" "+fi.getType());
			imageNodeRef = fileFolderService.create(fi.getNodeRef(), "Images", ContentModel.TYPE_FOLDER).getNodeRef();
		}
		
		
		response = sendRequest(new PostRequest(url + "/data?nodeRef="+nodeRef.toString(), convertStreamToString(data.getInputStream()), "application/xml"), 200, "admin");
		logger.debug("Resp : " + response.getContentAsString());
		
		Assert.assertEquals(1,fileFolderService.list(imageNodeRef).size());
		
	}

	private NodeRef parseNodeRef(String contentAsString) {
		return new NodeRef(contentAsString);
	}

	public void testFormulateEntity() throws Exception {

	}

	public String convertStreamToString(InputStream is) throws IOException {
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}

}
