/*
 * 
 */
package fr.becpg.repo.listvalue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.test.BeCPGTestHelper;

// TODO: Auto-generated Javadoc
/**
 * The Class ListValueServiceTest.
 * 
 * @author querephi
 */
public class ListValueServiceTest extends AbstractListValuePluginTest {
	/** The logger. */
	private static Log logger = LogFactory.getLog(ListValueServiceTest.class);

	@Resource
	private EntityListValuePlugin entityListValuePlugin;


	/**
	 * Test suggest supplier.
	 */
	@Test
	public void testSuggestSupplier() {
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				authenticationComponent.setSystemUserAsCurrentUser();
				
				// Create supplier 1 with allowed constraint
				logger.debug("create temp supplier 1");
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
				properties.put(ContentModel.PROP_NAME, "Supplier 1");
				NodeRef supplierNodeRef = nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_SUPPLIER, properties).getChildRef();

				String[] arrClassNames = { "bcpg:supplier" };
				List<ListValueEntry> suggestions = entityListValuePlugin.suggestTargetAssoc(BeCPGModel.TYPE_SUPPLIER, "Supplier 1", 0, 10, arrClassNames,null).getResults();

				boolean containsSupplier = false;
				for (ListValueEntry s : suggestions) {
					logger.debug("supplier: " + s.getName());
					if (s.getValue().equals(supplierNodeRef.toString()) && s.getName().equals("Supplier 1")) {
						containsSupplier = true;
					}
				}

				assertEquals("1 suggestion", 1, suggestions.size());
				assertTrue("check supplier key", containsSupplier);

				// filter by client : no results
				String[] arrClassNames2 = { "bcpg:client" };
				suggestions = entityListValuePlugin.suggestTargetAssoc(BeCPGModel.TYPE_SUPPLIER, "Supplier 1", 0, 10, arrClassNames2,null).getResults();

				assertEquals("0 suggestion", 0, suggestions.size());
				
				//test permissions

				authenticationComponent.setSystemUserAsCurrentUser();
				
				suggestions = entityListValuePlugin.suggestTargetAssoc(BeCPGModel.TYPE_LOCALSEMIFINISHEDPRODUCT, "*", 0, 10, null ,null).getResults();

				assertEquals("2 suggestion", 2, suggestions.size());
				
				
				authenticationComponent.setCurrentUser(BeCPGTestHelper.USER_ONE);
				
				suggestions = entityListValuePlugin.suggestTargetAssoc(BeCPGModel.TYPE_LOCALSEMIFINISHEDPRODUCT, "*", 0, 10, null ,null).getResults();

				
				assertEquals("1 suggestion", 1, suggestions.size());
				

				
				
				return null;
			}
		}, false, true);		
	}

}
