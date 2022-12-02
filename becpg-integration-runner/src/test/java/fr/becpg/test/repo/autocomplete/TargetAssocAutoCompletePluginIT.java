package fr.becpg.test.repo.autocomplete;

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

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.autocomplete.AutoCompleteEntry;
import fr.becpg.repo.autocomplete.AutoCompleteService;
import fr.becpg.repo.autocomplete.impl.plugins.TargetAssocAutoCompletePlugin;
import fr.becpg.test.BeCPGPLMTestHelper;

/**
 * The Class AutoCompleteServiceTest.
 *
 * @author querephi
 */
public class TargetAssocAutoCompletePluginIT extends AbstractAutoCompletePluginTest {

	private static final Log logger = LogFactory.getLog(TargetAssocAutoCompletePluginIT.class);

	@Autowired
	private TargetAssocAutoCompletePlugin targetAssocAutoCompletePlugin;
	
		
	
	/**
	 * Test suggest supplier.
	 */
	@Test
	public void testTargetAssocPlugin() {

		
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
			List<AutoCompleteEntry> suggestions = targetAssocAutoCompletePlugin
					.suggestTargetAssoc(null, PLMModel.TYPE_SUPPLIER, supplierName, 0, 10, arrClassNames, null).getResults();


			assertEquals("1 suggestion", 1, suggestions.size());
			assertTrue("check supplier key", contains(supplierNodeRef,supplierName, suggestions ));

			// suggest supplier (return supplier 1 and template
			suggestions = targetAssocAutoCompletePlugin.suggestTargetAssoc(null, PLMModel.TYPE_SUPPLIER, "*", 0, RepoConsts.MAX_RESULTS_UNLIMITED, arrClassNames, null).getResults();


			assertTrue("check supplier key",  contains(supplierNodeRef,supplierName, suggestions ));

			// suggest supplier and exclude entityTplAspect (return supplier
			// 1 and template
			Map<String, Serializable> props = new HashMap<>();
			props.put(AutoCompleteService.PROP_EXCLUDE_CLASS_NAMES, "bcpg:entityTplAspect");
			suggestions = targetAssocAutoCompletePlugin.suggestTargetAssoc(null, PLMModel.TYPE_SUPPLIER, "*", 0, RepoConsts.MAX_RESULTS_UNLIMITED, arrClassNames, props).getResults();


			assertTrue("check supplier key",  contains(supplierNodeRef,supplierName, suggestions ));

			// filter by client : no results
			String[] arrClassNames2 = { "bcpg:client" };
			suggestions = targetAssocAutoCompletePlugin.suggestTargetAssoc(null, PLMModel.TYPE_SUPPLIER, supplierName, 0, RepoConsts.MAX_RESULTS_UNLIMITED, arrClassNames2, null)
					.getResults();

			assertEquals("0 suggestion", 0, suggestions.size());

			// test permissions
			authenticationComponent.setSystemUserAsCurrentUser();
			suggestions = targetAssocAutoCompletePlugin.suggestTargetAssoc(null, PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT, "*", 0, RepoConsts.MAX_RESULTS_UNLIMITED, null, null).getResults();
			for (AutoCompleteEntry s4 : suggestions) {
				logger.debug("SF for system user: " + s4.getName());

			}

			assertTrue("2 suggestion", suggestions.size()>=2);
			
			props = new HashMap<>();
			props.put(AutoCompleteService.PROP_NODEREF, finishProductNodeRef.toString());
			
			//Suggest supplier plants of supplier 1
			suggestions = targetAssocAutoCompletePlugin.suggestTargetAssoc(PLMModel.ASSOC_SUPPLIERS.toString(), PLMModel.TYPE_PLANT, "µ", 0, RepoConsts.MAX_RESULTS_UNLIMITED, null, props)
					.getResults();

			assertEquals("0 suggestion", 0, suggestions.size());
			
			/*
			 *  filter=prop_to_filter|value
			 *	filter=cm:name|samplename
			 *	filter=cm:name|{cm:title}
			 *  filter=bcpg:code|{bcpg:code},cm:name|MP*
			 *  filter=au:market|{au:market}
			 *  filter=gs1:sortingBonusCriteria_or|{gs1:sortingBonusCriteria}  (when field is multiple default operator is and _or allow to change that)
			 *  filter=bcpg:ingTypeV2|{htmlPropValue} use the value of parent or parentAssoc control-param (@Since 4.2)
			 */
			
			props = new HashMap<>();
			props.put(AutoCompleteService.PROP_FILTER, "cm:name|"+supplierName);
			
			suggestions = targetAssocAutoCompletePlugin
					.suggestTargetAssoc(null, PLMModel.TYPE_SUPPLIER, "*", 0, 10, null, props).getResults();


			assertEquals("1 suggestion", 1, suggestions.size());
			assertTrue("check supplier key", contains(supplierNodeRef,supplierName, suggestions ));
			
			props = new HashMap<>();
			props.put(AutoCompleteService.PROP_FILTER, "bcpg:code|{bcpg:code},cm:name|"+supplierName);
			props.put(AutoCompleteService.PROP_ENTITYNODEREF,supplierNodeRef.toString());
			
			suggestions = targetAssocAutoCompletePlugin
					.suggestTargetAssoc(null, PLMModel.TYPE_SUPPLIER, "*", 0, 10, null, props).getResults();


			assertEquals("1 suggestion", 1, suggestions.size());
			assertTrue("check supplier key", contains(supplierNodeRef,supplierName, suggestions ));

			props = new HashMap<>();
			props.put(AutoCompleteService.PROP_FILTER, "bcpg:code|{htmlPropValue}");
			props.put(AutoCompleteService.PROP_PARENT,"NORESULT");
			
			suggestions = targetAssocAutoCompletePlugin
					.suggestTargetAssoc(null, PLMModel.TYPE_SUPPLIER, "*", 0, 10, null, props).getResults();


			assertEquals("0 suggestion", 0, suggestions.size());
			

			props = new HashMap<>();
			props.put(AutoCompleteService.PROP_FILTER, "bcpg:code|{htmlPropValue}");
			props.put(AutoCompleteService.PROP_PARENT, nodeService.getProperty(supplierNodeRef, BeCPGModel.PROP_CODE));
			
			suggestions = targetAssocAutoCompletePlugin
					.suggestTargetAssoc(null, PLMModel.TYPE_SUPPLIER, "*", 0, 10, null, props).getResults();


			assertEquals("1 suggestion", 1, suggestions.size());
			assertTrue("check supplier key", contains(supplierNodeRef,supplierName, suggestions ));
			
			return null;
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			authenticationComponent.setCurrentUser(BeCPGPLMTestHelper.USER_ONE);
			List<AutoCompleteEntry> suggestions = targetAssocAutoCompletePlugin.suggestTargetAssoc(null, PLMModel.TYPE_FINISHEDPRODUCT, "*", 0, RepoConsts.MAX_RESULTS_UNLIMITED, null, null).getResults();
			for (AutoCompleteEntry s5 : suggestions) {
				logger.debug("SF for user one: " + s5.getName());

			}
			assertTrue("1 suggestion", suggestions.size()>=1);

			return null;
		}, false, true);
	}



	private boolean contains(NodeRef supplierNodeRef, String supplierName, List<AutoCompleteEntry> suggestions) {

		boolean containsSupplier = false;
		for (AutoCompleteEntry s1 : suggestions) {
			logger.debug("supplier test 1: " + s1.getName() + " " + s1.getValue());
			if (s1.getValue().equals(supplierNodeRef.toString()) && s1.getName().equals(supplierName)) {
				containsSupplier = true;
			}
		}
		
		return containsSupplier;
	}

}
