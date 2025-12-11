/*
 *
 */
package fr.becpg.test.repo.helper;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import fr.becpg.model.SystemState;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.impl.AssociationCriteriaFilter;
import fr.becpg.repo.helper.impl.AssociationCriteriaFilter.AssociationCriteriaFilterMode;
import fr.becpg.repo.helper.impl.EntitySourceAssoc;
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

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	@Autowired
	private AssociationService associationService;
	
	@Autowired
	private EntityVersionService entityVersionService;

	/**
	 * Test check out check in.
	 */
	@Test
	public void testCheckinAssocs() {

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
			NodeRef destNodeRef = nodeService.getPrimaryParent(rawMaterialNodeRef).getParentRef();
			NodeRef workingCopyNodeRef = entityVersionService.createBranch(rawMaterialNodeRef, destNodeRef);

			// add new Supplier
			associationService.update(workingCopyNodeRef, PLMModel.ASSOC_SUPPLIERS, supplierNodeRefs);

			// check-in
			entityVersionService.mergeBranch(workingCopyNodeRef, rawMaterialNodeRef, VersionType.MAJOR, "This is a test version");

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
			entityVersionService.mergeBranch(workingCopyNodeRef, rawMaterialNodeRef, VersionType.MAJOR, "This is a test version");

			// check
			targetNodeRefs = associationService.getTargetAssocs(rawMaterialNodeRef, PLMModel.ASSOC_SUPPLIERS);
			assertEquals(0, targetNodeRefs.size());

			return null;

		}, false, true);
	}

	/**
	 * Test check out check in.
	 */
	@Test
	public void testCRUDAssocs() {
		NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			return BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test association");
		}, false, true);

		List<NodeRef> supplierNodeRefs = new LinkedList<>();

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			// suppliers
			String[] supplierNames = { "assoc-test1", "assoc-test2", "assoc-test3", "assoc-test4", "assoc-test5" };

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
			return true;
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			// add new Supplier
			associationService.update(rawMaterialNodeRef, PLMModel.ASSOC_SUPPLIERS, supplierNodeRefs.subList(0, 2));

			// check
			List<NodeRef> targetNodeRefs = associationService.getTargetAssocs(rawMaterialNodeRef, PLMModel.ASSOC_SUPPLIERS);

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
		}, false, true);

		NodeRef finishProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Create finished product --*/
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Test child Assoc");

			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterialNodeRef));

			finishedProduct.getCompoListView().setCompoList(compoList);

			List<DynamicCharactListItem> dynamicCharactListItems = new ArrayList<>();
			// Product
			dynamicCharactListItems.add(DynamicCharactListItem.build().withTitle("Product qty 1").withFormula("qty"));
			// Literal formula
			dynamicCharactListItems.add(DynamicCharactListItem.build().withTitle("Literal 1").withFormula("'Hello World'"));
			dynamicCharactListItems.add(DynamicCharactListItem.build().withTitle("Literal 2").withFormula("6.0221415E+23"));
			dynamicCharactListItems.add(DynamicCharactListItem.build().withTitle("Literal 3").withFormula("1+1+10-(4/100)"));
			dynamicCharactListItems.add(DynamicCharactListItem.build().withTitle("Literal 4").withFormula("0x7dFFFFFF"));
			dynamicCharactListItems.add(DynamicCharactListItem.build().withTitle("Literal 5").withFormula("true"));
			dynamicCharactListItems.add(DynamicCharactListItem.build().withTitle("Literal 6").withFormula("null"));
			// Properties formulae
			dynamicCharactListItems.add(DynamicCharactListItem.build().withTitle("Property  1").withFormula("costList[0].value"));
			dynamicCharactListItems.add(DynamicCharactListItem.build().withTitle("Property  1Bis").withFormula("costList[1].value"));
			dynamicCharactListItems.add(DynamicCharactListItem.build().withTitle("Property  2").withFormula("costList[0].unit"));
			dynamicCharactListItems.add(DynamicCharactListItem.build().withTitle("Property  3").withFormula("costList[0].value / costList[1].value"));
			dynamicCharactListItems.add(DynamicCharactListItem.build().withTitle("Property  4").withFormula("profitability"));
			dynamicCharactListItems.add(DynamicCharactListItem.build().withTitle("Collection Selection  1").withFormula("costList.?[value == 4.0][0].unit"));
			dynamicCharactListItems.add(DynamicCharactListItem.build().withTitle("Collection Selection  2").withFormula("costList.?[value < 5.0][0].value"));
			dynamicCharactListItems.add(DynamicCharactListItem.build().withTitle("Collection Projection  1").withFormula("costList.![value]"));
			// Variables
			dynamicCharactListItems.add(
				DynamicCharactListItem.build().withTitle("Variable  1").withFormula("compoListView.dynamicCharactList.?[title == 'Property  1' ][0].value"));

			finishedProduct.getCompoListView().setDynamicCharactList(dynamicCharactListItems);

			alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct);

			return finishedProduct.getNodeRef();
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			NodeRef compoListNodeRef = entityListDAO.getList(entityListDAO.getListContainer(finishProductNodeRef), PLMModel.TYPE_COMPOLIST);

			assertNotNull(compoListNodeRef);

			List<NodeRef> childNodeRefs = associationService.getChildAssocs(compoListNodeRef, ContentModel.ASSOC_CONTAINS);
			assertEquals("Assert 17", 17, childNodeRefs.size());

			childNodeRefs = associationService.getChildAssocs(compoListNodeRef, ContentModel.ASSOC_CONTAINS, PLMModel.TYPE_COMPOLIST);
			assertEquals("Assert 1", 1, childNodeRefs.size());

			childNodeRefs = associationService.getChildAssocs(compoListNodeRef, ContentModel.ASSOC_CONTAINS, PLMModel.TYPE_DYNAMICCHARACTLIST);
			assertEquals("Assert 16", 16, childNodeRefs.size());

			nodeService.deleteNode(
					associationService.getChildAssocs(compoListNodeRef, ContentModel.ASSOC_CONTAINS, PLMModel.TYPE_DYNAMICCHARACTLIST).get(0));

			
			childNodeRefs = associationService.getChildAssocs(compoListNodeRef, ContentModel.ASSOC_CONTAINS);
			assertEquals("Assert 16", 16, childNodeRefs.size());

			childNodeRefs = associationService.getChildAssocs(compoListNodeRef, ContentModel.ASSOC_CONTAINS, PLMModel.TYPE_COMPOLIST);
			assertEquals("Assert 1", 1, childNodeRefs.size());

			childNodeRefs = associationService.getChildAssocs(compoListNodeRef, ContentModel.ASSOC_CONTAINS, PLMModel.TYPE_DYNAMICCHARACTLIST);
			assertEquals("Assert 15", 15, childNodeRefs.size());

			return true;
		}, false, true);

	}
	
	@Test
	public void testSourceAssocs() {
		final NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			return BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test source assoc");
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			NodeRef supplierNodeRef = null;
			NodeRef entityFolder = nodeService.getChildByName(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS, "Supplier1");
			if (entityFolder != null) {
				supplierNodeRef = nodeService.getChildByName(entityFolder, ContentModel.ASSOC_CONTAINS, "Supplier1");
			}
			if (supplierNodeRef == null) {
				Map<QName, Serializable> properties = new HashMap<>();
				properties.put(ContentModel.PROP_NAME, "Supplier1");
				supplierNodeRef = nodeService
						.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
								QName.createQName((String) properties.get(ContentModel.PROP_NAME)), PLMModel.TYPE_SUPPLIER, properties)
						.getChildRef();
			}
			associationService.update(rawMaterialNodeRef, PLMModel.ASSOC_SUPPLIERS, supplierNodeRef);
			List<NodeRef> sourceNodeRefs = associationService.getSourcesAssocs(supplierNodeRef, PLMModel.ASSOC_SUPPLIERS);
			assertTrue(sourceNodeRefs.contains(rawMaterialNodeRef));
			sourceNodeRefs = associationService.getSourcesAssocs(supplierNodeRef);
			assertTrue(sourceNodeRefs.contains(rawMaterialNodeRef));
			return null;
		}, false, true);
	}
	
	@Test
	public void testEntitySourceAssocs() {
		final NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			return BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "RM test entity source assoc");
		}, false, true);
		
		NodeRef fpNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("FP test entity source assoc");
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withQtyUsed(5.5).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterialNodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		}, false, true);
		
		NodeRef fpNodeRef2 = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("FP test entity source assoc 2");
			List<CompoListDataItem> compoList = new ArrayList<>();
			finishedProduct.setState(SystemState.Valid);
			compoList.add(CompoListDataItem.build().withQtyUsed(5.5).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterialNodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		}, false, true);
		
		List<AssociationCriteriaFilter> filters = new ArrayList<>();
		AssociationCriteriaFilter filter = new AssociationCriteriaFilter(PLMModel.PROP_PRODUCT_STATE, SystemState.Simulation.toString());
		filter.setEntityFilter(true);
		filters.add(filter);
		List<EntitySourceAssoc> results = inReadTx(() -> associationService.getEntitySourceAssocs(List.of(rawMaterialNodeRef), PLMModel.ASSOC_COMPOLIST_PRODUCT, null, false, filters));
		assertEquals(1, results.size());
		assertTrue(results.stream().map(n -> n.getEntityNodeRef()).anyMatch(n -> n.equals(fpNodeRef)));
		
		filters.clear();
		filter = new AssociationCriteriaFilter(PLMModel.PROP_PRODUCT_STATE, SystemState.Valid.toString());
		filter.setEntityFilter(true);
		filters.add(filter);
		results = inReadTx(() -> associationService.getEntitySourceAssocs(List.of(rawMaterialNodeRef), PLMModel.ASSOC_COMPOLIST_PRODUCT, null, false, filters));
		assertEquals(1, results.size());
		assertTrue(results.stream().map(n -> n.getEntityNodeRef()).anyMatch(n -> n.equals(fpNodeRef2)));
		
		filters.clear();
		filter = new AssociationCriteriaFilter(PLMModel.PROP_PRODUCT_STATE, SystemState.Valid.toString(), AssociationCriteriaFilterMode.NOT_EQUALS);
		filter.setEntityFilter(true);
		filters.add(filter);
		results = inReadTx(() -> associationService.getEntitySourceAssocs(List.of(rawMaterialNodeRef), PLMModel.ASSOC_COMPOLIST_PRODUCT, null, false, filters));
		assertEquals(1, results.size());
		assertTrue(results.stream().map(n -> n.getEntityNodeRef()).anyMatch(n -> n.equals(fpNodeRef)));
		
		filters.clear();
		filter = new AssociationCriteriaFilter(PLMModel.PROP_COMPOLIST_QTY_SUB_FORMULA, "4.5|6", AssociationCriteriaFilterMode.RANGE);
		filters.add(filter);
		results = inReadTx(() -> associationService.getEntitySourceAssocs(List.of(rawMaterialNodeRef), PLMModel.ASSOC_COMPOLIST_PRODUCT, null, false, filters));
		assertEquals(2, results.size());
		assertTrue(results.stream().map(n -> n.getEntityNodeRef()).anyMatch(n -> n.equals(fpNodeRef)));
		assertTrue(results.stream().map(n -> n.getEntityNodeRef()).anyMatch(n -> n.equals(fpNodeRef2)));
		
		filters.clear();
		filter = new AssociationCriteriaFilter(PLMModel.PROP_COMPOLIST_QTY_SUB_FORMULA, "7.5|8", AssociationCriteriaFilterMode.RANGE);
		filters.add(filter);
		results = inReadTx(() -> associationService.getEntitySourceAssocs(List.of(rawMaterialNodeRef), PLMModel.ASSOC_COMPOLIST_PRODUCT, null, false, filters));
		assertEquals(0, results.size());
		
		filters.clear();
		filter = new AssociationCriteriaFilter(PLMModel.PROP_COMPOLIST_QTY_SUB_FORMULA, "4.5|6", AssociationCriteriaFilterMode.RANGE);
		filters.add(filter);
		filter = new AssociationCriteriaFilter(PLMModel.PROP_PRODUCT_STATE, SystemState.Simulation.toString());
		filter.setEntityFilter(true);
		filters.add(filter);
		results = inReadTx(() -> associationService.getEntitySourceAssocs(List.of(rawMaterialNodeRef), PLMModel.ASSOC_COMPOLIST_PRODUCT, null, false, filters));
		assertEquals(1, results.size());
		assertTrue(results.stream().map(n -> n.getEntityNodeRef()).anyMatch(n -> n.equals(fpNodeRef)));
		
		filters.clear();
		filter = new AssociationCriteriaFilter(PLMModel.PROP_COMPOLIST_QTY_SUB_FORMULA, "7.5|8", AssociationCriteriaFilterMode.RANGE);
		filters.add(filter);
		filter = new AssociationCriteriaFilter(PLMModel.PROP_PRODUCT_STATE, SystemState.Simulation.toString());
		filter.setEntityFilter(true);
		filters.add(filter);
		results = inReadTx(() -> associationService.getEntitySourceAssocs(List.of(rawMaterialNodeRef), PLMModel.ASSOC_COMPOLIST_PRODUCT, null, false, filters));
		assertEquals(0, results.size());
		
		filters.clear();
		filter = new AssociationCriteriaFilter(PLMModel.PROP_COMPOLIST_QTY_SUB_FORMULA, "4.5|6", AssociationCriteriaFilterMode.RANGE);
		filters.add(filter);
		filter = new AssociationCriteriaFilter(PLMModel.PROP_PRODUCT_STATE, SystemState.Valid.toString());
		filter.setEntityFilter(true);
		filters.add(filter);
		results = inReadTx(() -> associationService.getEntitySourceAssocs(List.of(rawMaterialNodeRef), PLMModel.ASSOC_COMPOLIST_PRODUCT, null, false, filters));
		assertEquals(1, results.size());
		assertTrue(results.stream().map(n -> n.getEntityNodeRef()).anyMatch(n -> n.equals(fpNodeRef2)));
		
		filters.clear();
		filter = new AssociationCriteriaFilter(PLMModel.PROP_PRODUCT_STATE, "TEST", AssociationCriteriaFilterMode.NOT_EQUALS);
		filters.add(filter);
		results = inReadTx(() -> associationService.getEntitySourceAssocs(List.of(rawMaterialNodeRef), PLMModel.ASSOC_COMPOLIST_PRODUCT, null, false, filters));
		assertEquals(2, results.size());
		assertTrue(results.stream().map(n -> n.getEntityNodeRef()).anyMatch(n -> n.equals(fpNodeRef)));
		assertTrue(results.stream().map(n -> n.getEntityNodeRef()).anyMatch(n -> n.equals(fpNodeRef2)));
		
		
		NodeRef fpNodeRef3 = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("FP test 'entity source assoc 3");
			List<CompoListDataItem> compoList = new ArrayList<>();
			finishedProduct.setState(SystemState.Valid);
			compoList.add(CompoListDataItem.build().withQtyUsed(5.5).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterialNodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		}, false, true);
		
		filters.clear();
		filter = new AssociationCriteriaFilter(ContentModel.PROP_NAME, "FP test 'entity source assoc 3", AssociationCriteriaFilterMode.EQUALS);
		filter.setEntityFilter(true);
		filters.add(filter);
		results = inReadTx(() -> associationService.getEntitySourceAssocs(List.of(rawMaterialNodeRef), PLMModel.ASSOC_COMPOLIST_PRODUCT, null, false, filters));
		assertEquals(1, results.size());
		assertTrue(results.stream().map(n -> n.getEntityNodeRef()).anyMatch(n -> n.equals(fpNodeRef3)));
	}
	
	/**
	 * Test auditable properties filters with all modes
	 */
	@Test
	public void testAuditablePropertiesFilters() {
		final NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			return BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "RM auditable test");
		}, false, true);
		
		// Create products with delay to ensure different creation times
		NodeRef fpNodeRef1 = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("FP Auditable 1");
			finishedProduct.setState(SystemState.Simulation);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withQtyUsed(3.0).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterialNodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		}, false, true);
		
		// Wait a bit to ensure different timestamps
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		
		NodeRef fpNodeRef2 = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("FP Auditable 2");
			finishedProduct.setState(SystemState.Valid);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withQtyUsed(4.0).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterialNodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		}, false, true);
		
		// Get creation dates for testing
		Date creationDate1 = inReadTx(() -> (Date) nodeService.getProperty(fpNodeRef1, ContentModel.PROP_CREATED));
		Date creationDate2 = inReadTx(() -> (Date) nodeService.getProperty(fpNodeRef2, ContentModel.PROP_CREATED));
		String creator1 = inReadTx(() -> (String) nodeService.getProperty(fpNodeRef1, ContentModel.PROP_CREATOR));
		String modifier1 = inReadTx(() -> (String) nodeService.getProperty(fpNodeRef1, ContentModel.PROP_MODIFIER));
		
		List<AssociationCriteriaFilter> filters = new ArrayList<>();
		AssociationCriteriaFilter filter;
		List<EntitySourceAssoc> results;
		
		// Test 1: Filter by cm:creator (EQUALS mode)
		filters.clear();
		filter = new AssociationCriteriaFilter(ContentModel.PROP_CREATOR, creator1, AssociationCriteriaFilterMode.EQUALS);
		filter.setEntityFilter(true);
		filters.add(filter);
		results = inReadTx(() -> associationService.getEntitySourceAssocs(List.of(rawMaterialNodeRef), 
				PLMModel.ASSOC_COMPOLIST_PRODUCT, null, false, filters));
		assertEquals("Filter by creator EQUALS", 2, results.size());
		
		// Test 2: Filter by cm:modifier (NOT_EQUALS mode)
		filters.clear();
		filter = new AssociationCriteriaFilter(ContentModel.PROP_MODIFIER, "nonexistent_user", AssociationCriteriaFilterMode.NOT_EQUALS);
		filter.setEntityFilter(true);
		filters.add(filter);
		results = inReadTx(() -> associationService.getEntitySourceAssocs(List.of(rawMaterialNodeRef), 
				PLMModel.ASSOC_COMPOLIST_PRODUCT, null, false, filters));
		assertEquals("Filter by modifier NOT_EQUALS", 2, results.size());
		
		// Test 3: Filter by cm:created (RANGE mode) - find products created after first product
		filters.clear();
		Date afterFirst = new Date(creationDate1.getTime() + 1000); // 500ms after first
		Date muchLater = new Date(creationDate2.getTime() + 10000); // well after second
		filter = new AssociationCriteriaFilter(ContentModel.PROP_CREATED, 
				dateFormat.format(afterFirst) + "|" + dateFormat.format(muchLater), AssociationCriteriaFilterMode.RANGE);
		filter.setEntityFilter(true);
		filters.add(filter);
		results = inReadTx(() -> associationService.getEntitySourceAssocs(List.of(rawMaterialNodeRef), 
				PLMModel.ASSOC_COMPOLIST_PRODUCT, null, false, filters));
		assertEquals("Filter by creation date RANGE", 1, results.size());
		assertTrue(results.stream().map(n -> n.getEntityNodeRef()).anyMatch(n -> n.equals(fpNodeRef2)));
		
		// Test 4: Filter by cm:modified (RANGE mode) - all products in wide range
		filters.clear();
		Date veryEarly = new Date(creationDate1.getTime() - 10000);
		Date veryLate = new Date(System.currentTimeMillis() + 10000);
		filter = new AssociationCriteriaFilter(ContentModel.PROP_MODIFIED, 
				dateFormat.format(veryEarly) + "|" + dateFormat.format(veryLate), AssociationCriteriaFilterMode.RANGE);
		filter.setEntityFilter(true);
		filters.add(filter);
		results = inReadTx(() -> associationService.getEntitySourceAssocs(List.of(rawMaterialNodeRef), 
				PLMModel.ASSOC_COMPOLIST_PRODUCT, null, false, filters));
		assertEquals("Filter by modified date RANGE wide", 2, results.size());
		
		// Test 5: Multiple filters - cm:creator AND cm:name (EQUALS)
		filters.clear();
		filter = new AssociationCriteriaFilter(ContentModel.PROP_CREATOR, creator1, AssociationCriteriaFilterMode.EQUALS);
		filter.setEntityFilter(true);
		filters.add(filter);
		filter = new AssociationCriteriaFilter(ContentModel.PROP_NAME, "FP Auditable 1", AssociationCriteriaFilterMode.EQUALS);
		filter.setEntityFilter(true);
		filters.add(filter);
		results = inReadTx(() -> associationService.getEntitySourceAssocs(List.of(rawMaterialNodeRef), 
				PLMModel.ASSOC_COMPOLIST_PRODUCT, null, false, filters));
		assertEquals("Multiple filters: creator AND name", 1, results.size());
		assertTrue(results.stream().map(n -> n.getEntityNodeRef()).anyMatch(n -> n.equals(fpNodeRef1)));
		
		// Test 6: Multiple filters - cm:modifier AND product state (EQUALS)
		filters.clear();
		filter = new AssociationCriteriaFilter(ContentModel.PROP_MODIFIER, modifier1, AssociationCriteriaFilterMode.EQUALS);
		filter.setEntityFilter(true);
		filters.add(filter);
		filter = new AssociationCriteriaFilter(PLMModel.PROP_PRODUCT_STATE, SystemState.Valid.toString(), 
				AssociationCriteriaFilterMode.EQUALS);
		filter.setEntityFilter(true);
		filters.add(filter);
		results = inReadTx(() -> associationService.getEntitySourceAssocs(List.of(rawMaterialNodeRef), 
				PLMModel.ASSOC_COMPOLIST_PRODUCT, null, false, filters));
		assertEquals("Multiple filters: modifier AND state Valid", 1, results.size());
		assertTrue(results.stream().map(n -> n.getEntityNodeRef()).anyMatch(n -> n.equals(fpNodeRef2)));
		
		// Test 7: Multiple filters with RANGE - quantity range AND creation date range
		filters.clear();
		filter = new AssociationCriteriaFilter(PLMModel.PROP_COMPOLIST_QTY_SUB_FORMULA, "2.5|4.5", 
				AssociationCriteriaFilterMode.RANGE);
		filters.add(filter);
		filter = new AssociationCriteriaFilter(ContentModel.PROP_CREATED, 
				dateFormat.format(veryEarly) + "|" + dateFormat.format(veryLate), AssociationCriteriaFilterMode.RANGE);
		filter.setEntityFilter(true);
		filters.add(filter);
		results = inReadTx(() -> associationService.getEntitySourceAssocs(List.of(rawMaterialNodeRef), 
				PLMModel.ASSOC_COMPOLIST_PRODUCT, null, false, filters));
		assertEquals("Multiple RANGE filters: quantity AND date", 2, results.size());
		
		// Test 8: Triple filter - creator, state, and quantity
		filters.clear();
		filter = new AssociationCriteriaFilter(ContentModel.PROP_CREATOR, creator1, AssociationCriteriaFilterMode.EQUALS);
		filter.setEntityFilter(true);
		filters.add(filter);
		filter = new AssociationCriteriaFilter(PLMModel.PROP_PRODUCT_STATE, SystemState.Simulation.toString(), 
				AssociationCriteriaFilterMode.EQUALS);
		filter.setEntityFilter(true);
		filters.add(filter);
		filter = new AssociationCriteriaFilter(PLMModel.PROP_COMPOLIST_QTY_SUB_FORMULA, "2.0|3.5", 
				AssociationCriteriaFilterMode.RANGE);
		filters.add(filter);
		results = inReadTx(() -> associationService.getEntitySourceAssocs(List.of(rawMaterialNodeRef), 
				PLMModel.ASSOC_COMPOLIST_PRODUCT, null, false, filters));
		assertEquals("Triple filter: creator, state, quantity", 1, results.size());
		assertTrue(results.stream().map(n -> n.getEntityNodeRef()).anyMatch(n -> n.equals(fpNodeRef1)));
	}
	
	/**
	 * Test comprehensive multi-property searches with all filter modes
	 */
	@Test
	public void testMultiPropertySearchAllModes() {
		final NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			return BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "RM multi-property test");
		}, false, true);
		
		// Create diverse test products
		NodeRef fpA = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			FinishedProductData fp = new FinishedProductData();
			fp.setName("Product Alpha");
			fp.setState(SystemState.Simulation);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withQtyUsed(10.0).withUnit(ProductUnit.kg)
					.withLossPerc(5d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterialNodeRef));
			fp.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), fp).getNodeRef();
		}, false, true);
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		
		NodeRef fpB = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			FinishedProductData fp = new FinishedProductData();
			fp.setName("Product Beta");
			fp.setState(SystemState.Valid);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withQtyUsed(20.0).withUnit(ProductUnit.kg)
					.withLossPerc(3d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterialNodeRef));
			fp.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), fp).getNodeRef();
		}, false, true);
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		
		NodeRef fpC = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			FinishedProductData fp = new FinishedProductData();
			fp.setName("Product Gamma");
			fp.setState(SystemState.ToValidate);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withQtyUsed(15.0).withUnit(ProductUnit.kg)
					.withLossPerc(7d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterialNodeRef));
			fp.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), fp).getNodeRef();
		}, false, true);
		
		String creator = inReadTx(() -> (String) nodeService.getProperty(fpA, ContentModel.PROP_CREATOR));
		Date createdA = inReadTx(() -> (Date) nodeService.getProperty(fpA, ContentModel.PROP_CREATED));
		Date createdC = inReadTx(() -> (Date) nodeService.getProperty(fpC, ContentModel.PROP_CREATED));
		
		List<AssociationCriteriaFilter> filters;
		AssociationCriteriaFilter filter;
		List<EntitySourceAssoc> results;
		
		// Test 1: EQUALS mode - exact name match
		filters = new ArrayList<>();
		filter = new AssociationCriteriaFilter(ContentModel.PROP_NAME, "Product Beta", 
				AssociationCriteriaFilterMode.EQUALS);
		filter.setEntityFilter(true);
		filters.add(filter);
		results = inReadTx(() -> associationService.getEntitySourceAssocs(List.of(rawMaterialNodeRef), 
				PLMModel.ASSOC_COMPOLIST_PRODUCT, null, false, filters));
		assertEquals("EQUALS mode - name", 1, results.size());
		assertTrue(results.stream().map(n -> n.getEntityNodeRef()).anyMatch(n -> n.equals(fpB)));
		
		// Test 2: NOT_EQUALS mode - exclude specific state
		filters.clear();
		filter = new AssociationCriteriaFilter(PLMModel.PROP_PRODUCT_STATE, SystemState.Valid.toString(), 
				AssociationCriteriaFilterMode.NOT_EQUALS);
		filter.setEntityFilter(true);
		filters.add(filter);
		results = inReadTx(() -> associationService.getEntitySourceAssocs(List.of(rawMaterialNodeRef), 
				PLMModel.ASSOC_COMPOLIST_PRODUCT, null, false, filters));
		assertEquals("NOT_EQUALS mode - state", 2, results.size());
		assertTrue(results.stream().map(n -> n.getEntityNodeRef()).noneMatch(n -> n.equals(fpB)));
		
		// Test 3: RANGE mode - quantity between values
		filters.clear();
		filter = new AssociationCriteriaFilter(PLMModel.PROP_COMPOLIST_QTY_SUB_FORMULA, "12|22", 
				AssociationCriteriaFilterMode.RANGE);
		filters.add(filter);
		results = inReadTx(() -> associationService.getEntitySourceAssocs(List.of(rawMaterialNodeRef), 
				PLMModel.ASSOC_COMPOLIST_PRODUCT, null, false, filters));
		assertEquals("RANGE mode - quantity", 2, results.size());
		assertTrue(results.stream().map(n -> n.getEntityNodeRef()).anyMatch(n -> n.equals(fpB)));
		assertTrue(results.stream().map(n -> n.getEntityNodeRef()).anyMatch(n -> n.equals(fpC)));
		
		// Test 4: Four filters - name NOT_EQUALS, state EQUALS, quantity RANGE, creator EQUALS
		filters.clear();
		filter = new AssociationCriteriaFilter(ContentModel.PROP_NAME, "Product Alpha", 
				AssociationCriteriaFilterMode.NOT_EQUALS);
		filter.setEntityFilter(true);
		filters.add(filter);
		filter = new AssociationCriteriaFilter(PLMModel.PROP_PRODUCT_STATE, SystemState.ToValidate.toString(), 
				AssociationCriteriaFilterMode.EQUALS);
		filter.setEntityFilter(true);
		filters.add(filter);
		filter = new AssociationCriteriaFilter(PLMModel.PROP_COMPOLIST_QTY_SUB_FORMULA, "10|20", 
				AssociationCriteriaFilterMode.RANGE);
		filters.add(filter);
		filter = new AssociationCriteriaFilter(ContentModel.PROP_CREATOR, creator, 
				AssociationCriteriaFilterMode.EQUALS);
		filter.setEntityFilter(true);
		filters.add(filter);
		results = inReadTx(() -> associationService.getEntitySourceAssocs(List.of(rawMaterialNodeRef), 
				PLMModel.ASSOC_COMPOLIST_PRODUCT, null, false, filters));
		assertEquals("Four filters combined", 1, results.size());
		assertTrue(results.stream().map(n -> n.getEntityNodeRef()).anyMatch(n -> n.equals(fpC)));
		
		// Test 5: Date RANGE and quantity RANGE combined
		filters.clear();
		Date beforeA = new Date(createdA.getTime() - 2000);
		Date afterC = new Date(createdC.getTime() + 2000);
		filter = new AssociationCriteriaFilter(ContentModel.PROP_CREATED, 
				dateFormat.format(beforeA) + "|" + dateFormat.format(afterC), AssociationCriteriaFilterMode.RANGE);
		filter.setEntityFilter(true);
		filters.add(filter);
		filter = new AssociationCriteriaFilter(PLMModel.PROP_COMPOLIST_QTY_SUB_FORMULA, "14|21", 
				AssociationCriteriaFilterMode.RANGE);
		filters.add(filter);
		results = inReadTx(() -> associationService.getEntitySourceAssocs(List.of(rawMaterialNodeRef), 
				PLMModel.ASSOC_COMPOLIST_PRODUCT, null, false, filters));
		assertEquals("Date RANGE + Quantity RANGE", 2, results.size());
		
		// Test 6: All modes combined - EQUALS, NOT_EQUALS, RANGE
		filters.clear();
		filter = new AssociationCriteriaFilter(ContentModel.PROP_CREATOR, creator, 
				AssociationCriteriaFilterMode.EQUALS);
		filter.setEntityFilter(true);
		filters.add(filter);
		filter = new AssociationCriteriaFilter(PLMModel.PROP_PRODUCT_STATE, SystemState.Archived.toString(), 
				AssociationCriteriaFilterMode.NOT_EQUALS);
		filter.setEntityFilter(true);
		filters.add(filter);
		filter = new AssociationCriteriaFilter(PLMModel.PROP_COMPOLIST_QTY_SUB_FORMULA, "8|18", 
				AssociationCriteriaFilterMode.RANGE);
		filters.add(filter);
		results = inReadTx(() -> associationService.getEntitySourceAssocs(List.of(rawMaterialNodeRef), 
				PLMModel.ASSOC_COMPOLIST_PRODUCT, null, false, filters));
		assertEquals("All modes: EQUALS + NOT_EQUALS + RANGE", 2, results.size());
		assertTrue(results.stream().map(n -> n.getEntityNodeRef()).anyMatch(n -> n.equals(fpA)));
		assertTrue(results.stream().map(n -> n.getEntityNodeRef()).anyMatch(n -> n.equals(fpC)));
		
		// Test 7: Loss percentage RANGE filter
		filters.clear();
		filter = new AssociationCriteriaFilter(PLMModel.PROP_COMPOLIST_LOSS_PERC, "2|6", 
				AssociationCriteriaFilterMode.RANGE);
		filters.add(filter);
		results = inReadTx(() -> associationService.getEntitySourceAssocs(List.of(rawMaterialNodeRef), 
				PLMModel.ASSOC_COMPOLIST_PRODUCT, null, false, filters));
		assertEquals("Loss percentage RANGE", 2, results.size());
		assertTrue(results.stream().map(n -> n.getEntityNodeRef()).anyMatch(n -> n.equals(fpA)));
		assertTrue(results.stream().map(n -> n.getEntityNodeRef()).anyMatch(n -> n.equals(fpB)));
		
		// Test 8: Complex scenario - five filters with mixed modes
		filters.clear();
		filter = new AssociationCriteriaFilter(ContentModel.PROP_CREATOR, creator, 
				AssociationCriteriaFilterMode.EQUALS);
		filter.setEntityFilter(true);
		filters.add(filter);
		filter = new AssociationCriteriaFilter(ContentModel.PROP_NAME, "Product Delta", 
				AssociationCriteriaFilterMode.NOT_EQUALS);
		filter.setEntityFilter(true);
		filters.add(filter);
		filter = new AssociationCriteriaFilter(PLMModel.PROP_PRODUCT_STATE, SystemState.Archived.toString(), 
				AssociationCriteriaFilterMode.NOT_EQUALS);
		filter.setEntityFilter(true);
		filters.add(filter);
		filter = new AssociationCriteriaFilter(PLMModel.PROP_COMPOLIST_QTY_SUB_FORMULA, "5|25", 
				AssociationCriteriaFilterMode.RANGE);
		filters.add(filter);
		filter = new AssociationCriteriaFilter(PLMModel.PROP_COMPOLIST_LOSS_PERC, "0|10", 
				AssociationCriteriaFilterMode.RANGE);
		filters.add(filter);
		results = inReadTx(() -> associationService.getEntitySourceAssocs(List.of(rawMaterialNodeRef), 
				PLMModel.ASSOC_COMPOLIST_PRODUCT, null, false, filters));
		assertEquals("Five filters complex scenario", 3, results.size());
	}

}
