/*
 * 
 */
package fr.becpg.test.repo.listvalue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

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
public class CompoListValuePluginTest extends AbstractListValuePluginTest {

	@Resource
	private CompoListValuePlugin compoListValuePlugin;


	private static Log logger = LogFactory.getLog(CompoListValuePluginTest.class);



	/**
	 * Test suggest supplier.
	 */
	@Test
	public void testCompoListValuePlugin() {

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				Map<String, Serializable> props = new HashMap<String, Serializable>();
				props.put(ListValueService.PROP_LOCALE, Locale.FRENCH);
				props.put(ListValueService.PROP_NODEREF, finishedProductNodeRef.toString());
				props.put(ListValueService.PROP_CLASS_NAME, "bcpg:compoList");

				authenticationComponent.setCurrentUser(BeCPGPLMTestHelper.USER_ONE);

				ListValuePage listValuePage = compoListValuePlugin.suggest("compoListParentLevel", "", null,ListValueService.SUGGEST_PAGE_SIZE, props);

				for (ListValueEntry listValueEntry : listValuePage.getResults()) {
					logger.info("listValueEntry: " + listValueEntry.getName() + " - " + listValueEntry.getValue());
				}

				assertEquals(1, listValuePage.getResults().size());

				authenticationComponent.setSystemUserAsCurrentUser();

				listValuePage = compoListValuePlugin.suggest("compoListParentLevel", "", null,ListValueService.SUGGEST_PAGE_SIZE, props);

				for (ListValueEntry listValueEntry : listValuePage.getResults()) {
					logger.info("listValueEntry: " + listValueEntry.getName() + " - " + listValueEntry.getValue());
				}

				assertEquals(2, listValuePage.getResults().size());

				listValuePage = compoListValuePlugin.suggest("compoListParentLevel", "Local semi finished 2", null, ListValueService.SUGGEST_PAGE_SIZE, props);

				assertEquals(1, listValuePage.getResults().size());

				// Check cycle detection (exclude localSF1NodeRef)
				ProductData finishedProduct = alfrescoRepository.findOne(finishedProductNodeRef);

				HashMap<String, String> extras = new HashMap<String, String>();
				extras.put("itemId", finishedProduct.getCompoListView().getCompoList().get(0).getNodeRef().toString());
				props.put(ListValueService.EXTRA_PARAM, extras);

				listValuePage = compoListValuePlugin.suggest("compoListParentLevel", "", null, ListValueService.SUGGEST_PAGE_SIZE, props);

				assertEquals(1, listValuePage.getResults().size());

				return null;

			}
		}, false, true);
	}

	@Test
	public void testIsQueryMatch() {
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

	}

}
