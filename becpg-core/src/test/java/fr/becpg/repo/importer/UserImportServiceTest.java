package fr.becpg.repo.importer;

import java.io.IOException;
import java.util.Date;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;

import fr.becpg.repo.importer.user.UserImporterService;
import fr.becpg.test.RepoBaseTestCase;


public class UserImportServiceTest  extends RepoBaseTestCase {

	UserImporterService userImporterService;
	
	public static final String COMPANY_HOME_PATH_QUERY = "PATH:\"/app:company_home/.\"";
	


	/** The node service. */
	
	private SearchService searchService;

	private static Log logger = LogFactory.getLog(UserImportServiceTest.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		
	   searchService = (SearchService)ctx.getBean("searchService");
         // Authenticate as the admin user
        authenticationComponent.setCurrentUser("admin");
		userImporterService = (UserImporterService) ctx.getBean("userImporterService");
	}
	
	
	private NodeRef createCSV() throws IOException {
		
		   ResultSet resultSet = searchService.query( new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"),
				   SearchService.LANGUAGE_LUCENE, COMPANY_HOME_PATH_QUERY);
	       NodeRef destNodeRef = resultSet.getNodeRef(0);
		
		Date now = new Date();
		
		String qname = QName.createValidLocalName("importusercsv-"+now.getTime());
        ChildAssociationRef assocRef = nodeService.createNode(
        		destNodeRef,
              ContentModel.ASSOC_CONTAINS,
              QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, qname),
              ContentModel.TYPE_CONTENT);
        
        NodeRef nodeRef = assocRef.getChildRef();
        nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, qname);
        nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, qname);
        nodeService.setProperty(nodeRef, ContentModel.PROP_DESCRIPTION, qname);
    	ContentWriter writer = this.contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
		if (writer != null) {
			
			
			ClassPathResource resource = new ClassPathResource("beCPG/import/User.csv");
	    	
			writer.setMimetype("text");
			writer.putContent(resource.getInputStream());
			
			
        	if (logger.isDebugEnabled()) {
        		logger.debug("File successfully modified");
        	} 
		} else {
			logger.error("Cannot write node");
		}
        
		return nodeRef;
	}
	
	
	public void testImportUserCSV(){
	 transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
 			public NodeRef execute() throws Throwable {
 				NodeRef csv  = createCSV();
 				userImporterService.importUser(csv);
 				//Assert.assertEquals(1, wiser.getMessages().size());
 				return null;

 			}},false,true);
 			
		
	}
	
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	    wiser.stop();
	}
	
}
