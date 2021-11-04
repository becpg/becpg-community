package fr.becpg.test.repo.listvalue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.listvalue.ListValueEntry;
import fr.becpg.repo.listvalue.ListValueService;
import fr.becpg.repo.listvalue.impl.ParentValuePlugin;
import fr.becpg.repo.product.data.SupplierData;
import fr.becpg.repo.product.data.productList.ContactListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;

/**
 * The Class ParentListValueServiceIT.
 *
 * @author matthieu
 */
public class ParentListValueServiceIT extends AbstractListValuePluginTest {

	private static final Log logger = LogFactory.getLog(ParentListValueServiceIT.class);

	@Autowired
	private ParentValuePlugin parentValuePlugin;

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	@Autowired
	private AssociationService associationService;

	private NodeRef createSupplierNodeRef() {

		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			ruleService.disableRules();

			authenticationComponent.setSystemUserAsCurrentUser();

			SupplierData supplierData = new SupplierData();
			supplierData.setName("Supplier 1");

			ContactListDataItem contact = new ContactListDataItem();
			contact.setName("contact 1");
             List<ContactListDataItem> contacts = new ArrayList<>();
             contacts.add(contact);
			
			supplierData.setContactList(contacts);

			alfrescoRepository.create(getTestFolderNodeRef(), supplierData);

			return alfrescoRepository.create(getTestFolderNodeRef(), supplierData).getNodeRef();

		}, false, true);

	}

	/**
	 * Test suggest supplier.
	 */
	@Test
	public void testSuggestContacts() {

		//
		final NodeRef finishProductNodeRef = createFinishProductNodeRef();
		final NodeRef supplierNodeRef = createSupplierNodeRef();

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			associationService.update(finishProductNodeRef, PLMModel.ASSOC_SUPPLIERS, supplierNodeRef);

			return null;

		}, false, true);

		waitForSolr();

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			Map<String, Serializable> props = new HashMap<>();
			props.put(ListValueService.PROP_PATH, "bcpg:suppliers");
			props.put(ListValueService.PROP_ENTITYNODEREF, finishProductNodeRef.toString());
			props.put(ListValueService.PROP_CLASS_NAME, "bcpg:contactList");
			props.put(ListValueService.PROP_ATTRIBUTE_NAME, "cm:name");

			List<ListValueEntry> suggestions = parentValuePlugin.suggest(null, "*", 0, 10, props).getResults();

			boolean containsContact = false;
			for (ListValueEntry s1 : suggestions) {
				logger.debug("contact test 1: " + s1.getName() + " " + s1.getValue());
				if (s1.getName().equals("contact 1")) {
					containsContact = true;
				}
			}

			assertEquals("1 suggestion", 1, suggestions.size());
			assertTrue("check contact key", containsContact);

			return null;
		}, false, true);

	}

}
