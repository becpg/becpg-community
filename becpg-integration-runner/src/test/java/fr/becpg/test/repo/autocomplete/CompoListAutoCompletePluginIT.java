/*
 *
 */
package fr.becpg.test.repo.autocomplete;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.repo.autocomplete.AutoCompleteEntry;
import fr.becpg.repo.autocomplete.AutoCompletePage;
import fr.becpg.repo.autocomplete.AutoCompleteService;
import fr.becpg.repo.autocomplete.CompoListAutoCompletePlugin;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.test.BeCPGPLMTestHelper;

/**
 * The Class AutoCompleteServiceTest.
 *
 * @author querephi
 */
public class CompoListAutoCompletePluginIT extends AbstractAutoCompletePluginTest {

	@Autowired
	private CompoListAutoCompletePlugin compoListValuePlugin;

	private static final Log logger = LogFactory.getLog(CompoListAutoCompletePluginIT.class);

	/**
	 * Test suggest supplier.
	 */
	@Test
	public void testCompoListValuePlugin() {

		final NodeRef finishedProductNodeRef = createFinishProductNodeRef();

		waitForSolr();

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			Map<String, Serializable> props = new HashMap<>();
			props.put(AutoCompleteService.PROP_LOCALE, Locale.FRENCH);
			props.put(AutoCompleteService.PROP_ENTITYNODEREF, finishedProductNodeRef.toString());
			props.put(AutoCompleteService.PROP_CLASS_NAME, "bcpg:compoList");

			authenticationComponent.setCurrentUser(BeCPGPLMTestHelper.USER_ONE);

			AutoCompletePage AutoCompletePage = compoListValuePlugin.suggest("compoListParentLevel", "", null, AutoCompleteService.SUGGEST_PAGE_SIZE, props);

			for (AutoCompleteEntry AutoCompleteEntry1 : AutoCompletePage.getResults()) {
				logger.info("AutoCompleteEntry: " + AutoCompleteEntry1.getName() + " - " + AutoCompleteEntry1.getValue());
			}

			assertEquals(1, AutoCompletePage.getResults().size());

			authenticationComponent.setSystemUserAsCurrentUser();

			AutoCompletePage = compoListValuePlugin.suggest("compoListParentLevel", "", null, AutoCompleteService.SUGGEST_PAGE_SIZE, props);

			for (AutoCompleteEntry AutoCompleteEntry2 : AutoCompletePage.getResults()) {
				logger.info("AutoCompleteEntry: " + AutoCompleteEntry2.getName() + " - " + AutoCompleteEntry2.getValue());
			}

			assertEquals(3, AutoCompletePage.getResults().size());

			AutoCompletePage = compoListValuePlugin.suggest("compoListParentLevel", "Local semi finished 2", null, AutoCompleteService.SUGGEST_PAGE_SIZE,
					props);

			assertEquals(2, AutoCompletePage.getResults().size());

			// Check cycle detection (exclude localSF1NodeRef)
			ProductData finishedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);

			HashMap<String, String> extras = new HashMap<>();
			extras.put("itemId", finishedProduct.getCompoListView().getCompoList().get(0).getNodeRef().toString());
			props.put(AutoCompleteService.EXTRA_PARAM, extras);

			AutoCompletePage = compoListValuePlugin.suggest("compoListParentLevel", "", null, AutoCompleteService.SUGGEST_PAGE_SIZE, props);

			for (AutoCompleteEntry AutoCompleteEntry2 : AutoCompletePage.getResults()) {
				logger.debug("AutoCompleteEntry: " + AutoCompleteEntry2.getName() + " - " + AutoCompleteEntry2.getValue());
			}

			assertEquals(2, AutoCompletePage.getResults().size());

			return null;

		}, false, true);
	}

	@Test
	public void testIsQueryMatch() {
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			assertTrue(compoListValuePlugin.isQueryMatch("*", "Pâte de riz"));
			assertTrue(compoListValuePlugin.isQueryMatch("Pâte*", "Pâte de riz"));
			assertTrue(compoListValuePlugin.isQueryMatch("Pâtes*", "Pâte de riz"));
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
