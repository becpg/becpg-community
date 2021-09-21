package fr.becpg.test.repo.listvalue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.listvalue.ListValueEntry;
import fr.becpg.repo.listvalue.ListValueService;
import fr.becpg.repo.listvalue.impl.EntityListValuePlugin;
import fr.becpg.test.BeCPGPLMTestHelper;

/**
 * The Class ListValueServiceTest.
 *
 * @author querephi
 */
public class ListValueServiceIT extends AbstractListValuePluginTest {

	private static final Log logger = LogFactory.getLog(ListValueServiceIT.class);

	@Autowired
	private EntityListValuePlugin entityListValuePlugin;

	/**
	 * Test suggest supplier.
	 */
	@Test
	public void testSuggestSupplier() {

		
		final NodeRef  finishProductNodeRef = createFinishProductNodeRef();

		final String supplierName = "Supplier-" + UUID.randomUUID().toString();

		
		final NodeRef supplierNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			authenticationComponent.setSystemUserAsCurrentUser();

			// Create supplier 1 with allowed constraint
			logger.debug("create temp supplier 1");
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, supplierName);
			return nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					PLMModel.TYPE_SUPPLIER, properties).getChildRef();
		}, false, true);

		waitForSolr();
		
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			// suggest supplier 1
			String[] arrClassNames = { "bcpg:supplier" };
			List<ListValueEntry> suggestions = entityListValuePlugin
					.suggestTargetAssoc(null, PLMModel.TYPE_SUPPLIER, supplierName, 0, 10, arrClassNames, null).getResults();

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
			suggestions = entityListValuePlugin.suggestTargetAssoc(null, PLMModel.TYPE_SUPPLIER, "*", 0, RepoConsts.MAX_RESULTS_UNLIMITED, arrClassNames, null).getResults();

			containsSupplier = false;
			for (ListValueEntry s2 : suggestions) {
				logger.debug("supplier test 2: " + s2.getName() + " supplierName "+supplierName);
				if (s2.getValue().equals(supplierNodeRef.toString()) && s2.getName().equals(supplierName)) {
					containsSupplier = true;
				}
			}

			assertTrue("check supplier key", containsSupplier);

			// suggest supplier and exclude entityTplAspect (return supplier
			// 1 and template
			Map<String, Serializable> props = new HashMap<>();
			props.put(ListValueService.PROP_EXCLUDE_CLASS_NAMES, "bcpg:entityTplAspect");
			suggestions = entityListValuePlugin.suggestTargetAssoc(null, PLMModel.TYPE_SUPPLIER, "*", 0, RepoConsts.MAX_RESULTS_UNLIMITED, arrClassNames, props).getResults();

			containsSupplier = false;
			for (ListValueEntry s3 : suggestions) {
			//	logger.debug("supplier: " + s3.getName());
				if (s3.getValue().equals(supplierNodeRef.toString()) && s3.getName().equals(supplierName)) {
					containsSupplier = true;
				}
			}

			assertTrue("check supplier key", containsSupplier);

			// filter by client : no results
			String[] arrClassNames2 = { "bcpg:client" };
			suggestions = entityListValuePlugin.suggestTargetAssoc(null, PLMModel.TYPE_SUPPLIER, supplierName, 0, RepoConsts.MAX_RESULTS_UNLIMITED, arrClassNames2, null)
					.getResults();

			assertEquals("0 suggestion", 0, suggestions.size());

			// test permissions
			authenticationComponent.setSystemUserAsCurrentUser();
			suggestions = entityListValuePlugin.suggestTargetAssoc(null, PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT, "*", 0, RepoConsts.MAX_RESULTS_UNLIMITED, null, null).getResults();
			for (ListValueEntry s4 : suggestions) {
				logger.debug("SF for system user: " + s4.getName());

			}

			assertTrue("2 suggestion", suggestions.size()>=2);
			
			props = new HashMap<>();
			props.put(ListValueService.PROP_NODEREF, finishProductNodeRef.toString());
			
			//Suggest supplier plants of supplier 1
			suggestions = entityListValuePlugin.suggestTargetAssoc(PLMModel.ASSOC_SUPPLIERS.toString(), PLMModel.TYPE_PLANT, "µ", 0, RepoConsts.MAX_RESULTS_UNLIMITED, null, props)
					.getResults();

			assertEquals("0 suggestion", 0, suggestions.size());

			
			return null;
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			authenticationComponent.setCurrentUser(BeCPGPLMTestHelper.USER_ONE);
			List<ListValueEntry> suggestions = entityListValuePlugin.suggestTargetAssoc(null, PLMModel.TYPE_FINISHEDPRODUCT, "*", 0, RepoConsts.MAX_RESULTS_UNLIMITED, null, null).getResults();
			for (ListValueEntry s5 : suggestions) {
				logger.debug("SF for user one: " + s5.getName());

			}
			assertTrue("1 suggestion", suggestions.size()>=1);

			return null;
		}, false, true);
	}

}
