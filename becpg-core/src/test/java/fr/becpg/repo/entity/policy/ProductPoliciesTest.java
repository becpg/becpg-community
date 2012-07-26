/*
 * 
 */
package fr.becpg.repo.entity.policy;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.AutoNumService;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.ProductDictionaryService;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.test.RepoBaseTestCase;

/**
 * The Class ProductPoliciesTest.
 * 
 * @author querephi
 */
public class ProductPoliciesTest extends RepoBaseTestCase {

	/** The logger. */
	private static Log logger = LogFactory.getLog(ProductPoliciesTest.class);

	/** The node service. */
	private NodeService nodeService;

	/** The authentication component. */
	private AuthenticationComponent authenticationComponent;

	/** The product dictionary service. */
	private ProductDictionaryService productDictionaryService;

	/** The product dao. */
	private ProductDAO productDAO;

	/** The auto num service. */
	private AutoNumService autoNumService;

	private String productCode1 = null;
	private String productCode2 = null;


	@Override
	protected void setUp() throws Exception {
		super.setUp();

		logger.debug("ProductServiceTest:setUp");

		nodeService = (NodeService) ctx.getBean("nodeService");
		fileFolderService = (FileFolderService) ctx.getBean("fileFolderService");
		productDAO = (ProductDAO) ctx.getBean("productDAO");
		productDictionaryService = (ProductDictionaryService) ctx.getBean("productDictionaryService");
		authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");
		repositoryHelper = (Repository) ctx.getBean("repositoryHelper");
		autoNumService = (AutoNumService) ctx.getBean("autoNumService");
	}

	@Override
	public void tearDown() throws Exception {
		try {
			authenticationComponent.clearCurrentSecurityContext();
		} catch (Throwable e) {
			e.printStackTrace();
			// Don't let this mask any previous exceptions
		}
		super.tearDown();

	}

	/**
	 * Test product code.
	 */
	public void testProductCode() {

		final Collection<QName> dataLists = productDictionaryService.getDataLists();

		final NodeRef rawMaterial1NodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<NodeRef>() {
					@Override
					public NodeRef execute() throws Throwable {

						RawMaterialData rawMaterial1 = new RawMaterialData();
						rawMaterial1.setName("Raw material 1");
						return productDAO.create(testFolderNodeRef, rawMaterial1, dataLists);

					}
				}, false, true);

		final NodeRef rawMaterial2NodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<NodeRef>() {
					@Override
					public NodeRef execute() throws Throwable {

						assertNotNull("Check product creaed", rawMaterial1NodeRef);
						productCode1 = (String) nodeService.getProperty(rawMaterial1NodeRef, BeCPGModel.PROP_CODE);

						RawMaterialData rawMaterial2 = new RawMaterialData();
						rawMaterial2.setName("Raw material 2");
						return productDAO.create(testFolderNodeRef, rawMaterial2, dataLists);

					}
				}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				productCode2 = (String) nodeService.getProperty(rawMaterial2NodeRef, BeCPGModel.PROP_CODE);
				assertNotNull("Check product code 1", productCode1);
				assertNotNull("Check product code 2", productCode2);
				Pattern p = Pattern.compile(autoNumService.getAutoNumMatchPattern(BeCPGModel.TYPE_RAWMATERIAL,
						BeCPGModel.PROP_CODE));

				Matcher ma1 = p.matcher(productCode1);
				assertTrue(ma1.matches());
				Matcher ma2 = p.matcher(productCode2);
				assertTrue(ma2.matches());
				assertEquals("Compare product codes", Long.parseLong(ma1.group(2)) + 1, Long.parseLong(ma2.group(2)));

				return null;

			}
		}, false, true);
	}
}
