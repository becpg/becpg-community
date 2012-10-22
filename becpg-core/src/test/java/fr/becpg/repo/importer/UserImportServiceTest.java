package fr.becpg.repo.importer;

import java.io.IOException;
import java.util.Date;

import javax.annotation.Resource;

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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.subethamail.wiser.Wiser;

import fr.becpg.repo.importer.user.UserImporterService;
import fr.becpg.test.RepoBaseTestCase;


public class UserImportServiceTest  extends RepoBaseTestCase {

	
	public static final String COMPANY_HOME_PATH_QUERY = "PATH:\"/app:company_home/.\"";
	

	protected Wiser wiser = new Wiser(2500);


	/** The node service. */
	@Resource
	private SearchService searchService;
	

	@Resource
	UserImporterService userImporterService;

	private static Log logger = LogFactory.getLog(UserImportServiceTest.class);
	
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		
		// First start wiser
		try {
			wiser.start();
		} catch (Exception e) {
			logger.warn("cannot open wiser!");
		}

	}
	
	@Override
	@After
	public void tearDown() throws Exception {
		
		super.tearDown();
		try {
			wiser.stop();
		} catch (Exception e) {
			logger.warn("cannot stop wiser!");
		}
		
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
	
	@Test
	public void testImportUserCSV(){
	 transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
 			public NodeRef execute() throws Throwable {
 				NodeRef csv  = createCSV();
 				userImporterService.importUser(csv);
 				//Assert.assertEquals(1, wiser.getMessages().size());
 				return null;

 			}},false,true);
 			
		
	}
	
}
