/*
 *
 */
package fr.becpg.test.repo.listvalue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.repo.listvalue.CompoListValuePlugin;
import fr.becpg.repo.listvalue.ListValueEntry;
import fr.becpg.repo.listvalue.ListValuePage;
import fr.becpg.repo.listvalue.ListValueService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.test.BeCPGPLMTestHelper;

/**
 * The Class ListValueServiceTest.
 *
 * @author querephi
 */
public class CompoListValuePluginIT extends AbstractListValuePluginTest {

	@Autowired
	private CompoListValuePlugin compoListValuePlugin;

	private static final Log logger = LogFactory.getLog(CompoListValuePluginIT.class);

	/**
	 * Test suggest supplier.
	 */
	@Test
	public void testCompoListValuePlugin() {

		final NodeRef finishedProductNodeRef = createFinishProductNodeRef();

		waitForSolr();

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			Map<String, Serializable> props = new HashMap<>();
			props.put(ListValueService.PROP_LOCALE, Locale.FRENCH);
			props.put(ListValueService.PROP_ENTITYNODEREF, finishedProductNodeRef.toString());
			props.put(ListValueService.PROP_CLASS_NAME, "bcpg:compoList");

			authenticationComponent.setCurrentUser(BeCPGPLMTestHelper.USER_ONE);

			ListValuePage listValuePage = compoListValuePlugin.suggest("compoListParentLevel", "", null, ListValueService.SUGGEST_PAGE_SIZE, props);

			for (ListValueEntry listValueEntry1 : listValuePage.getResults()) {
				logger.info("listValueEntry: " + listValueEntry1.getName() + " - " + listValueEntry1.getValue());
			}

			assertEquals(1, listValuePage.getResults().size());

			authenticationComponent.setSystemUserAsCurrentUser();

			listValuePage = compoListValuePlugin.suggest("compoListParentLevel", "", null, ListValueService.SUGGEST_PAGE_SIZE, props);

			for (ListValueEntry listValueEntry2 : listValuePage.getResults()) {
				logger.info("listValueEntry: " + listValueEntry2.getName() + " - " + listValueEntry2.getValue());
			}

			assertEquals(3, listValuePage.getResults().size());

			listValuePage = compoListValuePlugin.suggest("compoListParentLevel", "Local semi finished 2", null, ListValueService.SUGGEST_PAGE_SIZE,
					props);

			assertEquals(2, listValuePage.getResults().size());

			// Check cycle detection (exclude localSF1NodeRef)
			ProductData finishedProduct = alfrescoRepository.findOne(finishedProductNodeRef);

			HashMap<String, String> extras = new HashMap<>();
			extras.put("itemId", finishedProduct.getCompoListView().getCompoList().get(0).getNodeRef().toString());
			props.put(ListValueService.EXTRA_PARAM, extras);

			listValuePage = compoListValuePlugin.suggest("compoListParentLevel", "", null, ListValueService.SUGGEST_PAGE_SIZE, props);

			for (ListValueEntry listValueEntry2 : listValuePage.getResults()) {
				logger.debug("listValueEntry: " + listValueEntry2.getName() + " - " + listValueEntry2.getValue());
			}

			assertEquals(2, listValuePage.getResults().size());

			return null;

		}, false, true);
	}

	@Test
	public void testIsQueryMatch() {
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			assertTrue(compoListValuePlugin.isQueryMatch("*", "Pâte de riz"));
			assertTrue(compoListValuePlugin.isQueryMatch("Pâte*", "Pâte de riz"));
			assertTrue(compoListValuePlugin.isQueryMatch("Pâte", "Pâte de riz"));
			assertTrue(compoListValuePlugin.isQueryMatch("Pâte*", "Pate de riz"));
			assertTrue(compoListValuePlugin.isQueryMatch("Pâte*", "Pates de riz"));
			assertTrue(compoListValuePlugin.isQueryMatch("Pâte*", "pate de riz"));
			assertTrue(compoListValuePlugin.isQueryMatch("Pat*", "Patisserie de riz"));
			assertTrue(compoListValuePlugin.isQueryMatch("Riz*", "Pâte de riz"));
			assertTrue(compoListValuePlugin.isQueryMatch("Riz*", "Riz au lait"));
			assertFalse(compoListValuePlugin.isQueryMatch("Pâto*", "Patisserie"));
			assertFalse(compoListValuePlugin.isQueryMatch("Pâte*", "DesPates"));
			return null;
		}, false, true);

	}
}