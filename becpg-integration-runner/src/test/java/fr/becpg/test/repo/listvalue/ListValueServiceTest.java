package fr.becpg.test.repo.listvalue;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.listvalue.ListValueEntry;
import fr.becpg.repo.listvalue.ListValueService;
import fr.becpg.repo.listvalue.impl.EntityListValuePlugin;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.test.BeCPGPLMTestHelper;

/**
 * The Class ListValueServiceTest.
 *
 * @author querephi
 */
public class ListValueServiceTest extends AbstractListValuePluginTest {

	private static final Log logger = LogFactory.getLog(ListValueServiceTest.class);

	@Resource
	private EntityListValuePlugin entityListValuePlugin;

	/**
	 * Test suggest supplier.
	 */
	@Test
	public void testSuggestSupplier() {

		Date startTime = new Date();

		createFinishProductNodeRef();

		final String supplierName = UUID.randomUUID().toString();

		// First delete all existing suppliers
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			for (NodeRef tmpNodeRef : BeCPGQueryBuilder.createQuery().inDB().ofType(PLMModel.TYPE_SUPPLIER).list()) {
				nodeService.deleteNode(tmpNodeRef);
			}

			return null;
		} , false, true);

		final NodeRef supplierNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			authenticationComponent.setSystemUserAsCurrentUser();

			// Create supplier 1 with allowed constraint
			logger.debug("create temp supplier 1");
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, supplierName);
			return nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					PLMModel.TYPE_SUPPLIER, properties).getChildRef();
		} , false, true);

		waitForSolr(startTime);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			// suggest supplier 1
			String[] arrClassNames = { "bcpg:supplier" };
			List<ListValueEntry> suggestions = entityListValuePlugin
					.suggestTargetAssoc(null,PLMModel.TYPE_SUPPLIER, supplierName, 0, 10, arrClassNames, null).getResults();

			boolean containsSupplier = false;
			for (ListValueEntry s1 : suggestions) {
				logger.debug("supplier test 1: " + s1.getName() + " " + s1.getValue());
				if (s1.getValue().equals(supplierNodeRef.toString()) && s1.getName().equals(supplierName)) {
					containsSupplier = true;
				}
			}

			assertEquals("1 suggestion", 1, suggestions.size());
			assertTrue("check supplier key", containsSupplier);

			// suggest supplier (return supplier 1 and template
			suggestions = entityListValuePlugin.suggestTargetAssoc(null,PLMModel.TYPE_SUPPLIER, "*", 0, 10, arrClassNames, null).getResults();

			containsSupplier = false;
			for (ListValueEntry s2 : suggestions) {
				logger.debug("supplier test 2: " + s2.getName() + " " + s2.getValue());
				if (s2.getValue().equals(supplierNodeRef.toString()) && s2.getName().equals(supplierName)) {
					containsSupplier = true;
				}
			}

			assertEquals("1 suggestions", 1, suggestions.size());
			assertTrue("check supplier key", containsSupplier);

			// suggest supplier and exclude entityTplAspect (return supplier
			// 1 and template
			Map<String, Serializable> props = new HashMap<>();
			props.put(ListValueService.PROP_EXCLUDE_CLASS_NAMES, "bcpg:entityTplAspect");
			suggestions = entityListValuePlugin.suggestTargetAssoc(null,PLMModel.TYPE_SUPPLIER, "*", 0, 10, arrClassNames, props).getResults();

			containsSupplier = false;
			for (ListValueEntry s3 : suggestions) {
				logger.debug("supplier: " + s3.getName());
				if (s3.getValue().equals(supplierNodeRef.toString()) && s3.getName().equals(supplierName)) {
					containsSupplier = true;
				}
			}

			assertEquals("1 suggestions", 1, suggestions.size());
			assertTrue("check supplier key", containsSupplier);

			// filter by client : no results
			String[] arrClassNames2 = { "bcpg:client" };
			suggestions = entityListValuePlugin.suggestTargetAssoc(null,PLMModel.TYPE_SUPPLIER, supplierName, 0, 10, arrClassNames2, null).getResults();

			assertEquals("0 suggestion", 0, suggestions.size());

			// test permissions
			authenticationComponent.setSystemUserAsCurrentUser();
			suggestions = entityListValuePlugin.suggestTargetAssoc(null,PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT, "*", 0, 10, null, null).getResults();
			for (ListValueEntry s4 : suggestions) {
				logger.debug("SF for system user: " + s4.getName());

			}

			assertEquals("2 suggestion", 2, suggestions.size());

			authenticationComponent.setCurrentUser(BeCPGPLMTestHelper.USER_ONE);
			suggestions = entityListValuePlugin.suggestTargetAssoc(null,PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT, "*", 0, 10, null, null).getResults();
			for (ListValueEntry s5 : suggestions) {
				logger.debug("SF for user one: " + s5.getName());

			}
			assertEquals("1 suggestion", 1, suggestions.size());

			
			return null;
		} , false, true);
	}

}
