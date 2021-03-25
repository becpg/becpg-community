/*
 *
 */
package fr.becpg.test.repo.product;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.PLMWorkflowModel;
import fr.becpg.model.ReportModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.entity.version.EntityVersion;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.hierarchy.HierarchyService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.test.BeCPGPLMTestHelper;
import fr.becpg.test.BeCPGTestHelper;
import fr.becpg.test.PLMBaseTestCase;

/**
 * The Class ProductVersionServiceTest.
 *
 * @author querephi
 */
public class ProductVersionServiceIT extends PLMBaseTestCase {

	private final static Log logger = LogFactory.getLog(ProductVersionServiceIT.class);

	private static final String ERP_CODE = "0001";

	@Autowired
	private CheckOutCheckInService checkOutCheckInService;

	@Autowired
	private VersionService versionService;


	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private EntityVersionService entityVersionService;

	@Autowired
	private AssociationService associationService;

	@Autowired
	private EntityReportService entityReportService;

	@Autowired
	private HierarchyService hierarchyService;

	@Autowired
	private NodeArchiveService nodeArchiveService;

	/**
	 * Test check out check in.
	 *
	 * @throws InterruptedException
	 */
	@Test
	public void testCheckOutCheckIn() throws InterruptedException {

		if (entityVersionService.isV2Service()) {
			return;
		}
		
		final ProductUnit productUnit = ProductUnit.L;
		final int valueAdded = 1;

		final NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Create raw material --*/
			NodeRef r = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test report");
			nodeService.setProperty(r, BeCPGModel.PROP_ERP_CODE, ERP_CODE);

			nodeService.setProperty(r, PLMWorkflowModel.PROP_PV_VALIDATION_DATE, new Date());

			nodeService.createAssociation(r, personService.getPerson(BeCPGTestHelper.USER_ONE), PLMWorkflowModel.ASSOC_PV_CALLER_ACTOR);

			return r;
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			entityReportService.generateReports(rawMaterialNodeRef);

			return true;

		}, false, true);

		if (!nodeService.hasAspect(rawMaterialNodeRef, ContentModel.ASPECT_VERSIONABLE)) {
			transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
				logger.debug("Add versionnable aspect");
				Map<QName, Serializable> aspectProperties = new HashMap<>();
				aspectProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
				nodeService.addAspect(rawMaterialNodeRef, ContentModel.ASPECT_VERSIONABLE, aspectProperties);
				return rawMaterialNodeRef;
			}, false, true);

		}

		final NodeRef workingCopyNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			List<NodeRef> dbReports = associationService.getTargetAssocs(rawMaterialNodeRef, ReportModel.ASSOC_REPORTS);

			assertEquals(1, dbReports.size());
			// Check out
			logger.debug("checkout nodeRef: " + rawMaterialNodeRef);
			return checkOutCheckInService.checkout(rawMaterialNodeRef);

		}, false, true);

		final ProductData rawMaterial = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			assertNotNull("Check working copy exists", workingCopyNodeRef);
			List<NodeRef> dbReports = associationService.getTargetAssocs(workingCopyNodeRef, ReportModel.ASSOC_REPORTS);
			assertEquals(1, dbReports.size());

			// Documents is moved on working copy
			assertNotNull(getFolderDocuments(rawMaterialNodeRef));
			assertNotNull(getFolderDocuments(workingCopyNodeRef));

			// Check productCode
			assertEquals("productCode should be the same after checkout", nodeService.getProperty(rawMaterialNodeRef, BeCPGModel.PROP_CODE),
					nodeService.getProperty(workingCopyNodeRef, BeCPGModel.PROP_CODE));

			assertEquals("erpCode should be the same after checkout", nodeService.getProperty(rawMaterialNodeRef, BeCPGModel.PROP_ERP_CODE),
					nodeService.getProperty(workingCopyNodeRef, BeCPGModel.PROP_ERP_CODE));

			// Check aspect validation
			assertTrue(!nodeService.hasAspect(workingCopyNodeRef, PLMWorkflowModel.ASPECT_PRODUCT_VALIDATION_ASPECT));

			// Check costs on working copy
			ProductData rawMaterial1 = alfrescoRepository.findOne(rawMaterialNodeRef);
			ProductData workingCopyRawMaterial = alfrescoRepository.findOne(workingCopyNodeRef);
			assertEquals("Check costs size", rawMaterial1.getCostList().size(), workingCopyRawMaterial.getCostList().size());

			for (int i = 0; i < rawMaterial1.getCostList().size(); i++) {
				CostListDataItem costListDataItem = rawMaterial1.getCostList().get(i);
				CostListDataItem vCostListDataItem = workingCopyRawMaterial.getCostList().get(i);

				assertEquals("Check cost", costListDataItem.getCost(), vCostListDataItem.getCost());
				assertEquals("Check cost unit", costListDataItem.getUnit(), vCostListDataItem.getUnit());
				assertEquals("Check cost value", costListDataItem.getValue(), vCostListDataItem.getValue());
				assertNotSame("Check cost noderef", costListDataItem.getNodeRef(), vCostListDataItem.getNodeRef());
			}

			// Modify working copy
			workingCopyRawMaterial.setUnit(productUnit);
			for (CostListDataItem c : workingCopyRawMaterial.getCostList()) {
				c.setValue(c.getValue() + valueAdded);
			}
			alfrescoRepository.save(workingCopyRawMaterial);

			return rawMaterial1;
		}, false, true);

		final NodeRef newRawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			// Check in
			Map<String, Serializable> versionProperties = new HashMap<>();
			versionProperties.put(Version.PROP_DESCRIPTION, "This is a test version");
			versionProperties.put(VersionBaseModel.PROP_VERSION_TYPE, VersionType.MAJOR);
			return checkOutCheckInService.checkin(workingCopyNodeRef, versionProperties);
		}, false, true);

		validateNewVersion(newRawMaterialNodeRef, rawMaterialNodeRef, rawMaterial, productUnit, valueAdded, true);
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			assertTrue(!nodeService.hasAspect(newRawMaterialNodeRef, PLMWorkflowModel.ASPECT_PRODUCT_VALIDATION_ASPECT));
			assertNull(nodeService.getProperty(newRawMaterialNodeRef, PLMWorkflowModel.PROP_PV_VALIDATION_DATE));
			return null;
		}, false, true);

	}

	private String getVersionLabel(ProductData newRawMaterial) {
		return (String) nodeService.getProperty(newRawMaterial.getNodeRef(), ContentModel.PROP_VERSION_LABEL);
	}

	/**
	 * Test cancel check out.
	 */
	@Test
	public void testCancelCheckOut() {

		if (entityVersionService.isV2Service()) {
			return;
		}
		
		final NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper()
				.doInTransaction(() -> BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test report"), false, true);

		for (int i = 0; i < 2; i++) {

			if (!nodeService.hasAspect(rawMaterialNodeRef, ContentModel.ASPECT_VERSIONABLE)) {
				transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
					logger.debug("Add versionnable aspect");
					Map<QName, Serializable> aspectProperties = new HashMap<>();
					aspectProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
					nodeService.addAspect(rawMaterialNodeRef, ContentModel.ASPECT_VERSIONABLE, aspectProperties);
					return rawMaterialNodeRef;
				}, false, true);

			}

			transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

				entityReportService.generateReports(rawMaterialNodeRef);

				return true;

			}, false, true);

			final NodeRef workingCopyNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

				return checkOutCheckInService.checkout(rawMaterialNodeRef);

			}, false, true);

			transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

				assertNotNull("Check working copy exists", workingCopyNodeRef);

				// Documents is moved on working copy
				assertNotNull(getFolderDocuments(rawMaterialNodeRef));
				assertNotNull(getFolderDocuments(workingCopyNodeRef));

				// modify
				ProductUnit productUnit2 = ProductUnit.m;
				ProductData workingCopyRawMaterial = alfrescoRepository.findOne(workingCopyNodeRef);
				workingCopyRawMaterial.setUnit(productUnit2);
				alfrescoRepository.save(workingCopyRawMaterial);
				workingCopyRawMaterial = alfrescoRepository.findOne(workingCopyNodeRef);

				ProductData rawMaterial = alfrescoRepository.findOne(rawMaterialNodeRef);
				assertEquals("Check unit", ProductUnit.kg, rawMaterial.getUnit());
				assertEquals("Check unit", productUnit2, workingCopyRawMaterial.getUnit());

				// cancel check out
				checkOutCheckInService.cancelCheckout(workingCopyNodeRef);

				// documents are restored under orig node
				assertNotNull(getFolderDocuments(rawMaterialNodeRef));

				assertNull(entityVersionService.getVersionHistoryNodeRef(rawMaterialNodeRef));
				assertTrue((versionService.getVersionHistory(rawMaterialNodeRef) == null)
						|| (versionService.getVersionHistory(rawMaterialNodeRef).getAllVersions().size() == 1));
				// Check
				rawMaterial = alfrescoRepository.findOne(rawMaterialNodeRef);
				assertEquals("Check unit", ProductUnit.kg, rawMaterial.getUnit());
				return null;

			}, false, true);
		}
	}

	/**
	 * Test get version history.
	 */
	@Test
	public void testGetVersionHistory() {

		if (entityVersionService.isV2Service()) {
			return;
		}
		
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			NodeRef rawMaterialNodeRef = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test report");
			Map<String, Serializable> properties = new HashMap<>();
			properties.put(VersionBaseModel.PROP_VERSION_TYPE, VersionType.MAJOR);
			Version vRawMaterialNodeRef = versionService.createVersion(rawMaterialNodeRef, properties);

			assertEquals("1.0", vRawMaterialNodeRef.getVersionLabel());

			properties = new HashMap<>();
			properties.put(VersionBaseModel.PROP_VERSION_TYPE, VersionType.MINOR);
			Version vRawMaterialNodeRefV0_2 = versionService.createVersion(rawMaterialNodeRef, properties);
			assertEquals("1.1", vRawMaterialNodeRefV0_2.getVersionLabel());

			VersionHistory versionHistory = versionService.getVersionHistory(rawMaterialNodeRef);
			assertEquals("Should have 2 versions", 2, versionHistory.getAllVersions().size());
			Version[] versions = versionHistory.getAllVersions().toArray(new Version[2]);

			assertEquals("Check 1st version", versions[0].getVersionedNodeRef(), vRawMaterialNodeRef.getVersionedNodeRef());
			assertEquals("Check 2nd version", versions[1].getVersionedNodeRef(), vRawMaterialNodeRefV0_2.getVersionedNodeRef());

			return null;

		}, false, true);
	}

	/**
	 * Test check out check in.
	 */
	@Test
	public void testCheckOutCheckInValidProduct() {

		if (entityVersionService.isV2Service()) {
			return;
		}
		
		logger.info("testCheckOutCheckInValidProduct");

		final NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			NodeRef rawMaterialNodeRef1 = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test report");

			logger.debug("Add versionnable aspect");
			Map<QName, Serializable> aspectProperties = new HashMap<>();
			aspectProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
			nodeService.addAspect(rawMaterialNodeRef1, ContentModel.ASPECT_VERSIONABLE, aspectProperties);
			return rawMaterialNodeRef1;
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			// Valid it
			nodeService.setProperty(rawMaterialNodeRef, PLMModel.PROP_PRODUCT_STATE, SystemState.Valid);
			nodeService.setProperty(rawMaterialNodeRef, BeCPGModel.PROP_ERP_CODE, ERP_CODE);
			// products
			hierarchyService.classifyByHierarchy(repositoryHelper.getCompanyHome(), rawMaterialNodeRef);

			String path = nodeService.getPath(rawMaterialNodeRef).toPrefixString(namespaceService);
			String expected = "/app:company_home/cm:rawMaterial/cm:Sea_x0020_food/cm:Fish/";
			assertEquals("check path", expected, path.substring(0, expected.length()));

			// Check out
			NodeRef workingCopyNodeRef = checkOutCheckInService.checkout(rawMaterialNodeRef);
			logger.info("state " + rawMaterialNodeRef + " - " + nodeService.getProperty(workingCopyNodeRef, PLMModel.PROP_PRODUCT_STATE));
			assertEquals("Check state new version", SystemState.Simulation.toString(),
					nodeService.getProperty(workingCopyNodeRef, PLMModel.PROP_PRODUCT_STATE));

			// Check in
			NodeRef newRawMaterialNodeRef;

			Map<String, Serializable> versionProperties = new HashMap<>();
			versionProperties.put(Version.PROP_DESCRIPTION, "This is a test version");
			versionProperties.put(VersionBaseModel.PROP_VERSION_TYPE, VersionType.MINOR);
			newRawMaterialNodeRef = checkOutCheckInService.checkin(workingCopyNodeRef, versionProperties);

			assertNotNull("Check new version exists", newRawMaterialNodeRef);
			assertEquals("Check state new version", SystemState.Valid.toString(),
					nodeService.getProperty(newRawMaterialNodeRef, PLMModel.PROP_PRODUCT_STATE));

			VersionHistory versionHistory = versionService.getVersionHistory(newRawMaterialNodeRef);
			Version version = versionHistory.getVersion("1.1");
			assertNotNull(version);
			assertNotNull(entityVersionService.getEntityVersion(version));

			path = nodeService.getPath(rawMaterialNodeRef).toPrefixString(namespaceService);
			expected = "/app:company_home/cm:rawMaterial/cm:Sea_x0020_food/cm:Fish/";
			assertEquals("check path", expected, path.substring(0, expected.length()));

			// Check out
			workingCopyNodeRef = checkOutCheckInService.checkout(rawMaterialNodeRef);
			logger.info("state " + rawMaterialNodeRef + " - " + nodeService.getProperty(workingCopyNodeRef, PLMModel.PROP_PRODUCT_STATE));
			assertEquals("Check state new version", SystemState.Simulation.toString(),
					nodeService.getProperty(workingCopyNodeRef, PLMModel.PROP_PRODUCT_STATE));

			// Check in
			versionProperties = new HashMap<>();
			versionProperties.put(Version.PROP_DESCRIPTION, "This is a test version");
			versionProperties.put(VersionBaseModel.PROP_VERSION_TYPE, VersionType.MINOR);
			newRawMaterialNodeRef = checkOutCheckInService.checkin(workingCopyNodeRef, versionProperties);

			assertNotNull("Check new version exists", newRawMaterialNodeRef);
			assertEquals("Check state new version", SystemState.Valid.toString(),
					nodeService.getProperty(newRawMaterialNodeRef, PLMModel.PROP_PRODUCT_STATE));

			assertEquals("erpCode should be the same after checkout", ERP_CODE,
					nodeService.getProperty(newRawMaterialNodeRef, BeCPGModel.PROP_ERP_CODE));

			assertEquals("Check state new version", SystemState.Valid.toString(),
					nodeService.getProperty(entityVersionService.getEntityVersion(version), PLMModel.PROP_PRODUCT_STATE));

			return null;

		}, false, true);
	}

	/**
	 * Test variants are not lost with checkOut/checkIn
	 */
	@Test
	public void testCheckOutCheckInVariant() {
		
		if (entityVersionService.isV2Service()) {
			return;
		}
		
		logger.info("testVariant");

		final NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			NodeRef finishedProductNodeRef1 = BeCPGPLMTestHelper.createMultiLevelProduct(getTestFolderNodeRef());

			// create variant
			Map<QName, Serializable> props = new HashMap<>();
			props.put(ContentModel.PROP_NAME, "variant");
			props.put(BeCPGModel.PROP_IS_DEFAULT_VARIANT, true);
			NodeRef variantNodeRef = nodeService
					.createNode(finishedProductNodeRef1, BeCPGModel.ASSOC_VARIANTS, BeCPGModel.ASSOC_VARIANTS, BeCPGModel.TYPE_VARIANT, props)
					.getChildRef();

			logger.debug("Add versionnable aspect");
			Map<QName, Serializable> aspectProperties = new HashMap<>();
			aspectProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
			nodeService.addAspect(finishedProductNodeRef1, ContentModel.ASPECT_VERSIONABLE, aspectProperties);

			ProductData productData = alfrescoRepository.findOne(finishedProductNodeRef1);
			productData.getCompoListView().getCompoList().get(2).setVariants(Collections.singletonList(variantNodeRef));
			alfrescoRepository.save(productData);

			// check variant before checkOut
			productData = alfrescoRepository.findOne(finishedProductNodeRef1);
			assertEquals(1, productData.getCompoListView().getCompoList().get(2).getVariants().size());
			logger.info("finishedProductNodeRef compoList is " + productData.getCompoListView().getCompoList().get(2).getNodeRef() + " "
					+ nodeService.getPath(productData.getCompoListView().getCompoList().get(2).getNodeRef()));
			logger.info("finishedProductNodeRef variant is " + productData.getCompoListView().getCompoList().get(2).getVariants() + " "
					+ nodeService.getPath(productData.getCompoListView().getCompoList().get(2).getVariants().get(0)));

			return finishedProductNodeRef1;
		}, false, true);

		final NodeRef workingCopyNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			logger.info("checkout");
			// Check out
			return checkOutCheckInService.checkout(finishedProductNodeRef);

		}, false, true);

		final NodeRef versionNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			// check variant before checkin
			ProductData productData = alfrescoRepository.findOne(workingCopyNodeRef);
			assertEquals(1, productData.getCompoListView().getCompoList().get(2).getVariants().size());
			logger.info("finishedProductNodeRef compoList is " + productData.getCompoListView().getCompoList().get(2).getNodeRef() + " "
					+ nodeService.getPath(productData.getCompoListView().getCompoList().get(2).getNodeRef()));
			logger.info("finishedProductNodeRef variant is " + productData.getCompoListView().getCompoList().get(2).getVariants() + " "
					+ nodeService.getPath(productData.getCompoListView().getCompoList().get(2).getVariants().get(0)));

			// Check in
			Map<String, Serializable> versionProperties = new HashMap<>(1);
			versionProperties.put(Version.PROP_DESCRIPTION, "This is a test version");
			versionProperties.put(VersionBaseModel.PROP_VERSION_TYPE, VersionType.MAJOR);
			return checkOutCheckInService.checkin(workingCopyNodeRef, versionProperties);

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			assertNotNull("Check history version exists", versionNodeRef);

			ProductData productData = alfrescoRepository.findOne(versionNodeRef);
			assertEquals(1, nodeService.getChildAssocs(versionNodeRef, BeCPGModel.ASSOC_VARIANTS, RegexQNamePattern.MATCH_ALL).size());
			logger.info("finishedProductNodeRef compoList is " + productData.getCompoListView().getCompoList().get(2).getNodeRef());
			logger.info("finishedProductNodeRef variant is " + productData.getCompoListView().getCompoList().get(2).getVariants());
			assertEquals(1, productData.getCompoListView().getCompoList().get(2).getVariants().size());
			assertEquals(nodeService.getChildAssocs(versionNodeRef, BeCPGModel.ASSOC_VARIANTS, RegexQNamePattern.MATCH_ALL).get(0).getChildRef(),
					productData.getCompoListView().getCompoList().get(2).getVariants().get(0));

			return null;

		}, false, true);

		// Check orig node
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			ProductData productData = alfrescoRepository.findOne(finishedProductNodeRef);
			assertEquals(1, nodeService.getChildAssocs(versionNodeRef, BeCPGModel.ASSOC_VARIANTS, RegexQNamePattern.MATCH_ALL).size());
			logger.info("finishedProductNodeRef compoList is " + productData.getCompoListView().getCompoList().get(2).getNodeRef());
			logger.info("finishedProductNodeRef variant is " + productData.getCompoListView().getCompoList().get(2).getVariants());
			assertEquals(1, productData.getCompoListView().getCompoList().get(2).getVariants().size());
			assertEquals(
					nodeService.getChildAssocs(finishedProductNodeRef, BeCPGModel.ASSOC_VARIANTS, RegexQNamePattern.MATCH_ALL).get(0).getChildRef(),
					productData.getCompoListView().getCompoList().get(2).getVariants().get(0));

			return null;

		}, false, true);
	}

	private NodeRef getFolderDocuments(NodeRef nodeRef) {
		return nodeService.getChildByName(nodeRef, ContentModel.ASSOC_CONTAINS, "Documents");
	}

	/**
	 * Test check out check in.
	 */
	@Test
	public void testCheckOutCheckInAssociations() {

		if (entityVersionService.isV2Service()) {
			return;
		}
		
		final NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			logger.debug("Add versionnable aspect");

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

	/**
	 * Test check out check in.
	 *
	 * @throws InterruptedException
	 */
	@Test
	public void testBranches() throws InterruptedException {

		if (entityVersionService.isV2Service()) {
			return;
		}
		
		final ProductUnit productUnit = ProductUnit.L;
		final int valueAdded = 1;

		final NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Create raw material --*/
			NodeRef r = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "Test Check in Check out branch");
			nodeService.setProperty(r, BeCPGModel.PROP_ERP_CODE, ERP_CODE);
			nodeService.setProperty(r, PLMWorkflowModel.PROP_PV_VALIDATION_DATE, new Date());
			nodeService.createAssociation(r, personService.getPerson(BeCPGTestHelper.USER_ONE), PLMWorkflowModel.ASSOC_PV_CALLER_ACTOR);
			return r;
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			entityReportService.generateReports(rawMaterialNodeRef);

			return true;

		}, false, true);

		final NodeRef branchNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			List<NodeRef> dbReports = associationService.getTargetAssocs(rawMaterialNodeRef, ReportModel.ASSOC_REPORTS);
			assertEquals(1, dbReports.size());

			// Check out
			logger.debug("branch nodeRef: " + rawMaterialNodeRef);
			return entityVersionService.createBranch(rawMaterialNodeRef, getTestFolderNodeRef());

		}, false, true);

		final Date validationDate = new Date();

		final ProductData rawMaterial = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			assertNotNull("Check branch exists", branchNodeRef);
			List<NodeRef> dbReports = associationService.getTargetAssocs(branchNodeRef, ReportModel.ASSOC_REPORTS);
			assertEquals(1, dbReports.size());

			// Documents is moved on working copy
			assertNotNull(getFolderDocuments(rawMaterialNodeRef));
			assertNotNull(getFolderDocuments(branchNodeRef));

			// Check productCode
			assertNotSame("productCode should be different in branch", nodeService.getProperty(rawMaterialNodeRef, BeCPGModel.PROP_CODE),
					nodeService.getProperty(branchNodeRef, BeCPGModel.PROP_CODE));

			assertNull("ERP code should be null", nodeService.getProperty(branchNodeRef, BeCPGModel.PROP_ERP_CODE));

			// Check aspect validation
			assertTrue(!nodeService.hasAspect(branchNodeRef, PLMWorkflowModel.ASPECT_PRODUCT_VALIDATION_ASPECT));

			// Check costs on working copy
			ProductData rawMaterial1 = alfrescoRepository.findOne(rawMaterialNodeRef);
			ProductData branchRawMaterial = alfrescoRepository.findOne(branchNodeRef);
			assertEquals("Check costs size", rawMaterial1.getCostList().size(), branchRawMaterial.getCostList().size());

			for (int i = 0; i < rawMaterial1.getCostList().size(); i++) {
				CostListDataItem costListDataItem = rawMaterial1.getCostList().get(i);
				CostListDataItem vCostListDataItem = branchRawMaterial.getCostList().get(i);

				assertEquals("Check cost", costListDataItem.getCost(), vCostListDataItem.getCost());
				assertEquals("Check cost unit", costListDataItem.getUnit(), vCostListDataItem.getUnit());
				assertEquals("Check cost value", costListDataItem.getValue(), vCostListDataItem.getValue());
				assertNotSame("Check cost noderef", costListDataItem.getNodeRef(), vCostListDataItem.getNodeRef());
			}

			// Modify working copy
			branchRawMaterial.setUnit(productUnit);
			for (CostListDataItem c : branchRawMaterial.getCostList()) {
				c.setValue(c.getValue() + valueAdded);
			}

			alfrescoRepository.save(branchRawMaterial);

			// Validate Branch
			nodeService.setProperty(branchNodeRef, PLMWorkflowModel.PROP_PV_VALIDATION_DATE, validationDate);
			nodeService.createAssociation(branchNodeRef, personService.getPerson(BeCPGTestHelper.USER_ONE), PLMWorkflowModel.ASSOC_PV_CALLER_ACTOR);

			return rawMaterial1;
		}, false, true);

		assertTrue(nodeService.hasAspect(branchNodeRef, PLMWorkflowModel.ASPECT_PRODUCT_VALIDATION_ASPECT));
		assertEquals(nodeService.getProperty(branchNodeRef, PLMWorkflowModel.PROP_PV_VALIDATION_DATE), validationDate);

		final NodeRef newRawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(
				() -> entityVersionService.mergeBranch(branchNodeRef, rawMaterialNodeRef, VersionType.MAJOR, "This is a test version"), false, true);

		validateNewVersion(newRawMaterialNodeRef, rawMaterialNodeRef, rawMaterial, productUnit, valueAdded, false);

		assertTrue(nodeService.hasAspect(newRawMaterialNodeRef, PLMWorkflowModel.ASPECT_PRODUCT_VALIDATION_ASPECT));
		assertEquals(nodeService.getProperty(newRawMaterialNodeRef, PLMWorkflowModel.PROP_PV_VALIDATION_DATE), validationDate);

	}

	@Test
	public void testDeleteVersion() throws InterruptedException {

		if (entityVersionService.isV2Service()) {
			return;
		}
		
		final NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Create raw material --*/
			NodeRef r = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test report");
			nodeService.setProperty(r, BeCPGModel.PROP_ERP_CODE, ERP_CODE);
			return r;
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			entityReportService.generateReports(rawMaterialNodeRef);

			return true;

		}, false, true);

		if (!nodeService.hasAspect(rawMaterialNodeRef, ContentModel.ASPECT_VERSIONABLE)) {
			transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
				logger.debug("Add versionnable aspect");
				Map<QName, Serializable> aspectProperties = new HashMap<>();
				aspectProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
				nodeService.addAspect(rawMaterialNodeRef, ContentModel.ASPECT_VERSIONABLE, aspectProperties);
				return rawMaterialNodeRef;
			}, false, true);

		}

		final NodeRef workingCopyNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			List<NodeRef> dbReports = associationService.getTargetAssocs(rawMaterialNodeRef, ReportModel.ASSOC_REPORTS);

			assertEquals(1, dbReports.size());
			// Check out
			logger.debug("checkout nodeRef: " + rawMaterialNodeRef);
			return checkOutCheckInService.checkout(rawMaterialNodeRef);

		}, false, true);

		final NodeRef newRawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			// Check in
			Map<String, Serializable> versionProperties = new HashMap<>();
			versionProperties.put(Version.PROP_DESCRIPTION, "This is a test version");
			versionProperties.put(VersionBaseModel.PROP_VERSION_TYPE, VersionType.MAJOR);
			return checkOutCheckInService.checkin(workingCopyNodeRef, versionProperties);
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			assertEquals(newRawMaterialNodeRef, rawMaterialNodeRef);
			assertNotNull(entityVersionService.getVersionHistoryNodeRef(rawMaterialNodeRef));
			assertNotNull(versionService.getVersionHistory(rawMaterialNodeRef));
			return null;

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			for (EntityVersion entityVersion : entityVersionService.getAllVersions(rawMaterialNodeRef)) {
				assertNull(versionService.getVersionHistory(entityVersion.getEntityVersionNodeRef()));
			}
			return null;

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			nodeService.deleteNode(rawMaterialNodeRef);
			return null;

		}, false, true);
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			assertNotNull(entityVersionService.getVersionHistoryNodeRef(rawMaterialNodeRef));
			assertNotNull(versionService.getVersionHistory(rawMaterialNodeRef));
			assertFalse(nodeService.exists(rawMaterialNodeRef));
			return null;

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			NodeRef archiveNodeRef = nodeArchiveService.getArchivedNode(rawMaterialNodeRef);
			nodeArchiveService.restoreArchivedNode(archiveNodeRef);

			return null;

		}, false, true);
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			assertTrue(nodeService.exists(rawMaterialNodeRef));
			assertNotNull(entityVersionService.getVersionHistoryNodeRef(rawMaterialNodeRef));
			assertNotNull(versionService.getVersionHistory(rawMaterialNodeRef));
			return null;

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			nodeService.addAspect(rawMaterialNodeRef, ContentModel.ASPECT_TEMPORARY, null);
			nodeService.deleteNode(rawMaterialNodeRef);

			return null;

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			assertNull(entityVersionService.getVersionHistoryNodeRef(rawMaterialNodeRef));
			assertNull(versionService.getVersionHistory(rawMaterialNodeRef));
			assertFalse(nodeService.exists(rawMaterialNodeRef));
			return null;

		}, false, true);

	}

	@Test
	public void testPurgeVersion() throws InterruptedException {

		if (entityVersionService.isV2Service()) {
			return;
		}
		
		final NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Create raw material --*/
			NodeRef r = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test report");
			nodeService.setProperty(r, BeCPGModel.PROP_ERP_CODE, ERP_CODE);
			return r;
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			entityReportService.generateReports(rawMaterialNodeRef);

			return true;

		}, false, true);

		if (!nodeService.hasAspect(rawMaterialNodeRef, ContentModel.ASPECT_VERSIONABLE)) {
			transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
				logger.debug("Add versionnable aspect");
				Map<QName, Serializable> aspectProperties = new HashMap<>();
				aspectProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
				nodeService.addAspect(rawMaterialNodeRef, ContentModel.ASPECT_VERSIONABLE, aspectProperties);
				return rawMaterialNodeRef;
			}, false, true);

		}

		final NodeRef workingCopyNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			List<NodeRef> dbReports = associationService.getTargetAssocs(rawMaterialNodeRef, ReportModel.ASSOC_REPORTS);

			assertEquals(1, dbReports.size());
			// Check out
			logger.debug("checkout nodeRef: " + rawMaterialNodeRef);
			return checkOutCheckInService.checkout(rawMaterialNodeRef);

		}, false, true);

		NodeRef newRawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			// Check in
			Map<String, Serializable> versionProperties = new HashMap<>();
			versionProperties.put(Version.PROP_DESCRIPTION, "This is a test version");
			versionProperties.put(VersionBaseModel.PROP_VERSION_TYPE, VersionType.MAJOR);
			return checkOutCheckInService.checkin(workingCopyNodeRef, versionProperties);
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			assertEquals(newRawMaterialNodeRef, rawMaterialNodeRef);
			assertNotNull(entityVersionService.getVersionHistoryNodeRef(rawMaterialNodeRef));
			assertNotNull(versionService.getVersionHistory(rawMaterialNodeRef));
			return null;
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			nodeService.deleteNode(rawMaterialNodeRef);
			return null;

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			assertNotNull(entityVersionService.getVersionHistoryNodeRef(rawMaterialNodeRef));
			assertNotNull(getVersionHistoryNodeRef(rawMaterialNodeRef));
			assertFalse(nodeService.exists(rawMaterialNodeRef));
			return null;

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			NodeRef archiveNodeRef = nodeArchiveService.getArchivedNode(rawMaterialNodeRef);
			nodeArchiveService.purgeArchivedNode(archiveNodeRef);

			return null;

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			assertNull(entityVersionService.getVersionHistoryNodeRef(rawMaterialNodeRef));
			assertNull(getVersionHistoryNodeRef(rawMaterialNodeRef));
			assertFalse(nodeService.exists(rawMaterialNodeRef));
			return null;

		}, false, true);

	}

	private Object getVersionHistoryNodeRef(NodeRef rawMaterialNodeRef) {
		NodeRef rootNode = nodeService.getRootNode(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, Version2Model.STORE_ID));
		return nodeService.getChildByName(rootNode, Version2Model.CHILD_QNAME_VERSION_HISTORIES, rawMaterialNodeRef.getId());
	}

	private void validateNewVersion(final NodeRef newRawMaterialNodeRef, final NodeRef rawMaterialNodeRef, final ProductData rawMaterial,
			final ProductUnit productUnit, final int valueAdded, boolean secondCheckin) {

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			assertNotNull("Check new version exists", newRawMaterialNodeRef);
			ProductData newRawMaterial = alfrescoRepository.findOne(newRawMaterialNodeRef);
			assertEquals("Check version", "2.0", getVersionLabel(newRawMaterial));
			assertEquals("Check unit", productUnit, newRawMaterial.getUnit());

			// checkVersion
			VersionHistory versionHistory = versionService.getVersionHistory(newRawMaterialNodeRef);
			Version version = versionHistory.getVersion("1.0");
			assertNotNull(version);
			NodeRef entityVersionNodeRef = entityVersionService.getEntityVersion(version);
			assertNotNull(entityVersionNodeRef);
			assertNotNull(getFolderDocuments(entityVersionNodeRef));

			assertEquals("erpCode should be the same after checkout", ERP_CODE,
					nodeService.getProperty(newRawMaterialNodeRef, BeCPGModel.PROP_ERP_CODE));

			// Check costs on new version
			assertEquals("Check costs size", rawMaterial.getCostList().size(), newRawMaterial.getCostList().size());

			for (int i = 0; i < rawMaterial.getCostList().size(); i++) {
				CostListDataItem costListDataItem = rawMaterial.getCostList().get(i);
				CostListDataItem vCostListDataItem = newRawMaterial.getCostList().get(i);

				assertEquals("Check cost", costListDataItem.getCost(), vCostListDataItem.getCost());
				assertEquals("Check cost value", (costListDataItem.getValue() + valueAdded), vCostListDataItem.getValue());
				assertNotSame("Check cost noderef", costListDataItem.getNodeRef(), vCostListDataItem.getNodeRef());
			}

			assertEquals("Check products are the same", rawMaterialNodeRef, newRawMaterialNodeRef);

			// 2nd Check out, Check in
			if (secondCheckin) {
				NodeRef workingCopy2NodeRef = checkOutCheckInService.checkout(rawMaterialNodeRef);

				Map<String, Serializable> versionProperties = new HashMap<>();
				versionProperties.put(VersionBaseModel.PROP_VERSION_TYPE, VersionType.MAJOR);
				versionProperties.put(Version.PROP_DESCRIPTION, "description");
				return checkOutCheckInService.checkin(workingCopy2NodeRef, versionProperties);
			}
			return rawMaterialNodeRef;

		}, false, true);

		if (secondCheckin) {
			transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

				ProductData newRawMaterial = alfrescoRepository.findOne(newRawMaterialNodeRef);
				assertEquals("Check version", "3.0", getVersionLabel(newRawMaterial));
				VersionHistory versionHistory = versionService.getVersionHistory(newRawMaterialNodeRef);
				Version version = versionHistory.getVersion("3.0");
				assertNotNull(version);
				assertNotNull(entityVersionService.getEntityVersion(version));

				// Check cost Unit has changed after transaction
				for (int i = 0; i < newRawMaterial.getCostList().size(); i++) {
					CostListDataItem vCostListDataItem = newRawMaterial.getCostList().get(i);
					Boolean fixedCost = (Boolean) nodeService.getProperty(vCostListDataItem.getCost(), PLMModel.PROP_COSTFIXED);
					if ((fixedCost == null) || fixedCost.equals(Boolean.FALSE)) {
						assertTrue("Check cost unit", vCostListDataItem.getUnit().endsWith("/L"));
					}
				}

				// documents are restored under orig node
				assertNotNull(getFolderDocuments(rawMaterialNodeRef));

				return null;

			}, false, true);
		}

	}

}
