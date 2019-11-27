/*
 *
 */
package fr.becpg.test.repo.helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.namespace.QName;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.test.BeCPGPLMTestHelper;
import fr.becpg.test.PLMBaseTestCase;

/**
 * The Class ProductVersionServiceTest.
 *
 * @author querephi
 */
public class AssociationServiceIT extends PLMBaseTestCase {

	@Autowired
	private CheckOutCheckInService checkOutCheckInService;

	@Autowired
	private AssociationService associationService;

	/**
	 * Test check out check in.
	 */
	@Test
	public void testUpdateCachedAssocs() {

		final NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			NodeRef rawMaterialNodeRef1 = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test report");
			if (!nodeService.hasAspect(rawMaterialNodeRef1, ContentModel.ASPECT_VERSIONABLE)) {
				Map<QName, Serializable> aspectProperties = new HashMap<>();
				aspectProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
				nodeService.addAspect(rawMaterialNodeRef1, ContentModel.ASPECT_VERSIONABLE, aspectProperties);
			}
			return rawMaterialNodeRef1;
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			// suppliers
			String[] supplierNames = { "Supplier1", "Supplier2", "Supplier3" };
			List<NodeRef> supplierNodeRefs = new LinkedList<>();
			for (String supplierName : supplierNames) {
				NodeRef supplierNodeRef = null;
				NodeRef entityFolder = nodeService.getChildByName(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS, supplierName);
				if (entityFolder != null) {
					supplierNodeRef = nodeService.getChildByName(entityFolder, ContentModel.ASSOC_CONTAINS, supplierName);
				}

				if (supplierNodeRef == null) {
					Map<QName, Serializable> properties = new HashMap<>();
					properties.put(ContentModel.PROP_NAME, supplierName);
					supplierNodeRef = nodeService
							.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
									QName.createQName((String) properties.get(ContentModel.PROP_NAME)), PLMModel.TYPE_SUPPLIER, properties)
							.getChildRef();
				}

				supplierNodeRefs.add(supplierNodeRef);
			}

			associationService.update(rawMaterialNodeRef, PLMModel.ASSOC_SUPPLIERS, supplierNodeRefs.get(0));

			// check
			List<NodeRef> targetNodeRefs = associationService.getTargetAssocs(rawMaterialNodeRef, PLMModel.ASSOC_SUPPLIERS);
			assertEquals("", 1, targetNodeRefs.size());

			// Check out
			NodeRef workingCopyNodeRef = checkOutCheckInService.checkout(rawMaterialNodeRef);

			// add new Supplier
			associationService.update(workingCopyNodeRef, PLMModel.ASSOC_SUPPLIERS, supplierNodeRefs);

			// check-in
			Map<String, Serializable> versionProperties = new HashMap<>();
			versionProperties.put(Version.PROP_DESCRIPTION, "This is a test version");
			checkOutCheckInService.checkin(workingCopyNodeRef, versionProperties);

			// check
			targetNodeRefs = associationService.getTargetAssocs(rawMaterialNodeRef, PLMModel.ASSOC_SUPPLIERS);

			assertEquals("Assert 3", 3, targetNodeRefs.size());

			nodeService.deleteNode(supplierNodeRefs.get(0));

			// check
			targetNodeRefs = associationService.getTargetAssocs(rawMaterialNodeRef, PLMModel.ASSOC_SUPPLIERS);
			assertEquals("Assert 2", 2, targetNodeRefs.size());

			// Check out
			workingCopyNodeRef = checkOutCheckInService.checkout(rawMaterialNodeRef);

			// remove Suppliers
			associationService.update(workingCopyNodeRef, PLMModel.ASSOC_SUPPLIERS, new ArrayList<NodeRef>());

			// check-in
			checkOutCheckInService.checkin(workingCopyNodeRef, versionProperties);

			// check
			targetNodeRefs = associationService.getTargetAssocs(rawMaterialNodeRef, PLMModel.ASSOC_SUPPLIERS);
			assertEquals(0, targetNodeRefs.size());

			return null;

		}, false, true);
	}

}
