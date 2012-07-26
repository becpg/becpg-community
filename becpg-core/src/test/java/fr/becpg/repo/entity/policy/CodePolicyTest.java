/*
 * 
 */
package fr.becpg.repo.entity.policy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.AutoNumService;
import fr.becpg.test.RepoBaseTestCase;

/**
 * The Class CodePolicyTest.
 *
 * @author querephi
 */
public class CodePolicyTest extends RepoBaseTestCase  {

	/** The logger. */
	private static Log logger = LogFactory.getLog(CodePolicyTest.class);
	
	/** The node service. */
	private NodeService nodeService;

	/** The auto num service. */
	private AutoNumService autoNumService;
	
	private String code1 = null;
	private String code2 = null;
	private String code3 = null;
	private String code4 = null;
	
	@Override
	protected void setUp() throws Exception {		
		super.setUp();	
		
    	logger.debug("ProductServiceTest:setUp");
    
    	nodeService = (NodeService)ctx.getBean("nodeService");
    	fileFolderService = (FileFolderService)ctx.getBean("fileFolderService");  
    	authenticationComponent = (AuthenticationComponent)ctx.getBean("authenticationComponent");
        repositoryHelper = (Repository)ctx.getBean("repositoryHelper");
        autoNumService = (AutoNumService)ctx.getBean("autoNumService");         
    }
    
	@Override
    public void tearDown() throws Exception
    {
        super.tearDown();
    }	
	
	/**
	 * Test supplier code.
	 */
	public void testSupplierCode() {

		final NodeRef supplier1NodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<NodeRef>() {
					@Override
					public NodeRef execute() throws Throwable {

						// delete autonum value
						autoNumService.deleteAutoNumValue(BeCPGModel.TYPE_SUPPLIER, BeCPGModel.PROP_CODE);

						Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
						String name = "Supplier 1";
						properties.put(ContentModel.PROP_NAME, name);
						return nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS,
								QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
								BeCPGModel.TYPE_SUPPLIER, properties).getChildRef();

					}
				}, false, true);

		final NodeRef supplier2NodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<NodeRef>() {
					@Override
					public NodeRef execute() throws Throwable {

						// Check
						assertNotNull("Check supplier created", supplier1NodeRef);
						code1 = (String) nodeService.getProperty(supplier1NodeRef, BeCPGModel.PROP_CODE);

						Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
						String name = "Supplier 2";
						properties.put(ContentModel.PROP_NAME, name);
						return nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS,
								QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
								BeCPGModel.TYPE_SUPPLIER, properties).getChildRef();

					}
				}, false, true);

		final NodeRef supplier3NodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<NodeRef>() {
					@Override
					public NodeRef execute() throws Throwable {

						// check
						assertNotNull("Check supplier created", supplier2NodeRef);
						code2 = (String) nodeService.getProperty(supplier2NodeRef, BeCPGModel.PROP_CODE);

						// auto num defined not taken
						Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
						String name = "Supplier 3";
						properties.put(ContentModel.PROP_NAME, name);
						properties.put(BeCPGModel.PROP_CODE, "F3");
						return nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS,
								QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
								BeCPGModel.TYPE_SUPPLIER, properties).getChildRef();

					}
				}, false, true);

		final NodeRef supplier4NodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<NodeRef>() {
					@Override
					public NodeRef execute() throws Throwable {

						// check
						assertNotNull("Check supplier created", supplier3NodeRef);
						code3 = (String) nodeService.getProperty(supplier3NodeRef, BeCPGModel.PROP_CODE);

						// auto num defined but already taken
						Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
						String name = "Supplier 4";
						properties.put(ContentModel.PROP_NAME, name);
						properties.put(BeCPGModel.PROP_CODE, "F3");
						return nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS,
								QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
								BeCPGModel.TYPE_SUPPLIER, properties).getChildRef();

					}
				}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				// check
				assertNotNull("Check supplier created", supplier4NodeRef);
				code4 = (String) nodeService.getProperty(supplier4NodeRef, BeCPGModel.PROP_CODE);
				Pattern p = Pattern.compile(autoNumService.getAutoNumMatchPattern(BeCPGModel.TYPE_SUPPLIER,
						BeCPGModel.PROP_CODE));
				Matcher m1 = p.matcher(code1);
				System.out.println(code1 + " " + p.toString() + " " + m1.matches());
				assertTrue(m1.matches());
				assertEquals("Check code 1", "1", m1.group(2));
				Matcher m2 = p.matcher(code2);
				System.out.println(code2 + " " + p.toString() + " " + m2.matches());
				assertTrue(m2.matches());
				assertEquals("Check code 2", "2", m2.group(2));

				Matcher m3 = p.matcher(code3);
				System.out.println(code3 + " " + p.toString() + " " + m3.matches());
				assertTrue(m3.matches());
				assertEquals("Check code 3", "3", m3.group(2));

				Matcher m4 = p.matcher(code4);
				System.out.println(code4 + " " + p.toString() + " " + m4.matches());
				assertTrue(m4.matches());
				assertEquals("Check code 4", "4", m4.group(2));

				return null;

			}
		}, false, true);

	}
 	
}
