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
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.DynamicCharactListItem;
import fr.becpg.test.BeCPGPLMTestHelper;
import fr.becpg.test.PLMBaseTestCase;

/**
 * The Class ProductVersionServiceTest.
 *
 * @author querephi
 */
public class AssociationServiceIT extends PLMBaseTestCase {

	@Autowired
	private AssociationService associationService;

	@Autowired
	private EntityVersionService entityVersionService;

	/**
	 * Test check out check in.
	 */
	@Test
	public void testCheckinAssocs() {

		final NodeRef rawMaterialNodeRef = inWriteTx(() -> {

			NodeRef rawMaterialNodeRef1 = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(),
					"MP test report");
			if (!nodeService.hasAspect(rawMaterialNodeRef1, ContentModel.ASPECT_VERSIONABLE)) {
				Map<QName, Serializable> aspectProperties = new HashMap<>();
				aspectProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
				nodeService.addAspect(rawMaterialNodeRef1, ContentModel.ASPECT_VERSIONABLE, aspectProperties);
			}
			return rawMaterialNodeRef1;
		});

		inWriteTx(() -> {

			// suppliers
			String[] supplierNames = { "Supplier1", "Supplier2", "Supplier3" };
			List<NodeRef> supplierNodeRefs = new LinkedList<>();
			for (String supplierName : supplierNames) {
				NodeRef supplierNodeRef = null;
				NodeRef entityFolder = nodeService.getChildByName(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
						supplierName);
				if (entityFolder != null) {
					supplierNodeRef = nodeService.getChildByName(entityFolder, ContentModel.ASSOC_CONTAINS,
							supplierName);
				}

				if (supplierNodeRef == null) {
					Map<QName, Serializable> properties = new HashMap<>();
					properties.put(ContentModel.PROP_NAME, supplierName);
					supplierNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
							QName.createQName((String) properties.get(ContentModel.PROP_NAME)), PLMModel.TYPE_SUPPLIER,
							properties).getChildRef();
				}

				supplierNodeRefs.add(supplierNodeRef);
			}

			associationService.update(rawMaterialNodeRef, PLMModel.ASSOC_SUPPLIERS, supplierNodeRefs.get(0));

			// check
			List<NodeRef> targetNodeRefs = associationService.getTargetAssocs(rawMaterialNodeRef,
					PLMModel.ASSOC_SUPPLIERS);
			assertEquals("", 1, targetNodeRefs.size());

			// Check out
			NodeRef destNodeRef = nodeService.getPrimaryParent(rawMaterialNodeRef).getParentRef();
			NodeRef workingCopyNodeRef = entityVersionService.createBranch(rawMaterialNodeRef, destNodeRef);

			// add new Supplier
			associationService.update(workingCopyNodeRef, PLMModel.ASSOC_SUPPLIERS, supplierNodeRefs);

			// check-in
			entityVersionService.mergeBranch(workingCopyNodeRef, rawMaterialNodeRef, VersionType.MAJOR,
					"This is a test version");

			// check
			targetNodeRefs = associationService.getTargetAssocs(rawMaterialNodeRef, PLMModel.ASSOC_SUPPLIERS);

			assertEquals("Assert 3", 3, targetNodeRefs.size());

			nodeService.deleteNode(supplierNodeRefs.get(0));

			// check
			targetNodeRefs = associationService.getTargetAssocs(rawMaterialNodeRef, PLMModel.ASSOC_SUPPLIERS);
			assertEquals("Assert 2", 2, targetNodeRefs.size());

			// Check out
			destNodeRef = nodeService.getPrimaryParent(rawMaterialNodeRef).getParentRef();
			workingCopyNodeRef = entityVersionService.createBranch(rawMaterialNodeRef, destNodeRef);

			// remove Suppliers
			associationService.update(workingCopyNodeRef, PLMModel.ASSOC_SUPPLIERS, new ArrayList<NodeRef>());

			// check-in
			entityVersionService.mergeBranch(workingCopyNodeRef, rawMaterialNodeRef, VersionType.MAJOR,
					"This is a test version");

			// check
			targetNodeRefs = associationService.getTargetAssocs(rawMaterialNodeRef, PLMModel.ASSOC_SUPPLIERS);
			assertEquals(0, targetNodeRefs.size());

			return null;

		});
	}

	/**
	 * Test check out check in.
	 */
	@Test
	public void testCRUDAssocs() {
		NodeRef rawMaterialNodeRef = inWriteTx(() -> {
			return BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test association");
		});

		List<NodeRef> supplierNodeRefs = new LinkedList<>();

		inWriteTx(() -> {

			// suppliers
			String[] supplierNames = { "assoc-test1", "assoc-test2", "assoc-test3", "assoc-test4", "assoc-test5" };

			for (String supplierName : supplierNames) {
				NodeRef supplierNodeRef = null;
				NodeRef entityFolder = nodeService.getChildByName(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
						supplierName);
				if (entityFolder != null) {
					supplierNodeRef = nodeService.getChildByName(entityFolder, ContentModel.ASSOC_CONTAINS,
							supplierName);
				}

				if (supplierNodeRef == null) {
					Map<QName, Serializable> properties = new HashMap<>();
					properties.put(ContentModel.PROP_NAME, supplierName);
					supplierNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
							QName.createQName((String) properties.get(ContentModel.PROP_NAME)), PLMModel.TYPE_SUPPLIER,
							properties).getChildRef();
				}

				supplierNodeRefs.add(supplierNodeRef);
			}
			return true;
		});

		inWriteTx(() -> {
			// add new Supplier
			associationService.update(rawMaterialNodeRef, PLMModel.ASSOC_SUPPLIERS, supplierNodeRefs.subList(0, 2));

			// check
			List<NodeRef> targetNodeRefs = associationService.getTargetAssocs(rawMaterialNodeRef,
					PLMModel.ASSOC_SUPPLIERS);

			assertEquals("Assert 2", 2, targetNodeRefs.size());

			associationService.update(rawMaterialNodeRef, PLMModel.ASSOC_SUPPLIERS, supplierNodeRefs.subList(0, 3));

			targetNodeRefs = associationService.getTargetAssocs(rawMaterialNodeRef, PLMModel.ASSOC_SUPPLIERS);

			assertEquals("Assert 3", 3, targetNodeRefs.size());

			associationService.update(rawMaterialNodeRef, PLMModel.ASSOC_SUPPLIERS, supplierNodeRefs.subList(2, 3));

			targetNodeRefs = associationService.getTargetAssocs(rawMaterialNodeRef, PLMModel.ASSOC_SUPPLIERS);

			assertEquals("Assert 1", 1, targetNodeRefs.size());

			nodeService.createAssociation(rawMaterialNodeRef, supplierNodeRefs.get(0), PLMModel.ASSOC_SUPPLIERS);

			targetNodeRefs = associationService.getTargetAssocs(rawMaterialNodeRef, PLMModel.ASSOC_SUPPLIERS);

			assertEquals("Assert 2", 2, targetNodeRefs.size());

			nodeService.removeAssociation(rawMaterialNodeRef, supplierNodeRefs.get(2), PLMModel.ASSOC_SUPPLIERS);

			targetNodeRefs = associationService.getTargetAssocs(rawMaterialNodeRef, PLMModel.ASSOC_SUPPLIERS);

			assertEquals("Assert 1", 1, targetNodeRefs.size());

			nodeService.deleteNode(supplierNodeRefs.get(0));

			targetNodeRefs = associationService.getTargetAssocs(rawMaterialNodeRef, PLMModel.ASSOC_SUPPLIERS);

			assertEquals("Assert 0", 0, targetNodeRefs.size());

			return true;
		});

		NodeRef finishProductNodeRef = inWriteTx(() -> {

			/*-- Create finished product --*/
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Test child Assoc");

			List<CompoListDataItem> compoList = new ArrayList<>();
			/*
			 * compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d,
			 * DeclarationType.Detail, rawMaterialNodeRef));
			 */
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(rawMaterialNodeRef));

			finishedProduct.getCompoListView().setCompoList(compoList);

			List<DynamicCharactListItem> dynamicCharactListItems = new ArrayList<>();
			// Product
			dynamicCharactListItems.add(new DynamicCharactListItem("Product qty 1", "qty"));
			// Literal formula
			dynamicCharactListItems.add(new DynamicCharactListItem("Literal 1", "'Hello World'"));
			dynamicCharactListItems.add(new DynamicCharactListItem("Literal 2", "6.0221415E+23"));
			dynamicCharactListItems.add(new DynamicCharactListItem("Literal 3", "1+1+10-(4/100)"));
			dynamicCharactListItems.add(new DynamicCharactListItem("Literal 4", "0x7dFFFFFF"));
			dynamicCharactListItems.add(new DynamicCharactListItem("Literal 5", "true"));
			dynamicCharactListItems.add(new DynamicCharactListItem("Literal 6", "null"));
			// Properties formulae
			dynamicCharactListItems.add(new DynamicCharactListItem("Property  1", "costList[0].value"));
			dynamicCharactListItems.add(new DynamicCharactListItem("Property  1Bis", "costList[1].value"));
			dynamicCharactListItems.add(new DynamicCharactListItem("Property  2", "costList[0].unit"));
			dynamicCharactListItems
					.add(new DynamicCharactListItem("Property  3", "costList[0].value / costList[1].value"));
			dynamicCharactListItems.add(new DynamicCharactListItem("Property  4", "profitability"));
			dynamicCharactListItems
					.add(new DynamicCharactListItem("Collection Selection  1", "costList.?[value == 4.0][0].unit"));
			dynamicCharactListItems
					.add(new DynamicCharactListItem("Collection Selection  2", "costList.?[value < 5.0][0].value"));
			dynamicCharactListItems.add(new DynamicCharactListItem("Collection Projection  1", "costList.![value]"));
			// Variables
			dynamicCharactListItems.add(new DynamicCharactListItem("Variable  1",
					"compoListView.dynamicCharactList.?[title == 'Property  1' ][0].value"));

			finishedProduct.getCompoListView().setDynamicCharactList(dynamicCharactListItems);

			alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct);

			return finishedProduct.getNodeRef();
		});

		inWriteTx(() -> {

			NodeRef compoListNodeRef = entityListDAO.getList(entityListDAO.getListContainer(finishProductNodeRef),
					PLMModel.TYPE_COMPOLIST);

			assertNotNull(compoListNodeRef);

			List<NodeRef> childNodeRefs = associationService.getChildAssocs(compoListNodeRef,
					ContentModel.ASSOC_CONTAINS);
			assertEquals("Assert 17", 17, childNodeRefs.size());

			childNodeRefs = associationService.getChildAssocs(compoListNodeRef, ContentModel.ASSOC_CONTAINS,
					PLMModel.TYPE_COMPOLIST);
			assertEquals("Assert 1", 1, childNodeRefs.size());

			childNodeRefs = associationService.getChildAssocs(compoListNodeRef, ContentModel.ASSOC_CONTAINS,
					PLMModel.TYPE_DYNAMICCHARACTLIST);
			assertEquals("Assert 16", 16, childNodeRefs.size());

			nodeService.deleteNode(associationService
					.getChildAssocs(compoListNodeRef, ContentModel.ASSOC_CONTAINS, PLMModel.TYPE_DYNAMICCHARACTLIST)
					.get(0));

			childNodeRefs = associationService.getChildAssocs(compoListNodeRef, ContentModel.ASSOC_CONTAINS);
			assertEquals("Assert 16", 16, childNodeRefs.size());

			childNodeRefs = associationService.getChildAssocs(compoListNodeRef, ContentModel.ASSOC_CONTAINS,
					PLMModel.TYPE_COMPOLIST);
			assertEquals("Assert 1", 1, childNodeRefs.size());

			childNodeRefs = associationService.getChildAssocs(compoListNodeRef, ContentModel.ASSOC_CONTAINS,
					PLMModel.TYPE_DYNAMICCHARACTLIST);
			assertEquals("Assert 15", 15, childNodeRefs.size());

			return true;
		});

	}

}
