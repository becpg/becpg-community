package fr.becpg.repo.importer;

import java.io.IOException;
import java.util.Date;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;

import fr.becpg.repo.importer.user.UserImporterService;
import fr.becpg.test.RepoBaseTestCase;


public class UserImportServiceTest  extends RepoBaseTestCase {

	UserImporterService userImporterService;
	
	public static final String COMPANY_HOME_PATH_QUERY = "PATH:\"/app:company_home/.\"";
	
	/** The app ctx. */
	private static ApplicationContext applicationContext = ApplicationContextHelper
			.getApplicationContext();

	/** The node service. */
	
	private TransactionService transactionService;
	private AuthenticationComponent authenticationComponent;
	private SearchService searchService;

	
	private static Log logger = LogFactory.getLog(UserImportServiceTest.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		 transactionService = (TransactionService) applicationContext.getBean("transactionService");
     	 searchService = (SearchService)applicationContext.getBean("searchService");
         authenticationComponent = (AuthenticationComponent)applicationContext.getBean("authenticationComponent");
         transactionService = (TransactionService)applicationContext.getBean("transactionComponent");
          // Authenticate as the admin user
         authenticationComponent.setCurrentUser("admin");
		userImporterService = (UserImporterService) applicationContext.getBean("userImporterService");
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

 				return null;

 			}},false,true);
 			
		
	}
	
}
