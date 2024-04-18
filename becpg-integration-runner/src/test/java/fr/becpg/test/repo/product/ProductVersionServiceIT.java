/*
 *
 */
package fr.becpg.test.repo.product;

import static org.junit.Assert.assertNotEquals;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
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
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
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
import org.springframework.beans.factory.annotation.Qualifier;

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
import fr.becpg.test.BeCPGDBTestMetricReporter;
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

	@Autowired
	private BeCPGDBTestMetricReporter beCPGDBTestMetricReporter;

	@Autowired
	@Qualifier("mtAwareNodeService")
	private NodeService dbNodeService;

	/**
	 * Test check out check in.
	 *
	 * @throws InterruptedException
	 */
	@Test
	public void testCheckOutCheckIn() throws InterruptedException {

		final ProductUnit productUnit = ProductUnit.L;
		final int valueAdded = 1;

		final NodeRef rawMaterialNodeRef = inWriteTx(() -> {

			/*-- Create raw material --*/
			NodeRef r = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test report");
			dbNodeService.setProperty(r, BeCPGModel.PROP_ERP_CODE, ERP_CODE);

			dbNodeService.setProperty(r, PLMWorkflowModel.PROP_PV_VALIDATION_DATE, new Date());

			dbNodeService.createAssociation(r, personService.getPerson(BeCPGTestHelper.USER_ONE),
					PLMWorkflowModel.ASSOC_PV_CALLER_ACTOR);

			return r;
		});

		inWriteTx(() -> {

			entityReportService.generateReports(rawMaterialNodeRef);

			return true;

		});

		final NodeRef workingCopyNodeRef = inWriteTx(() -> {

			List<NodeRef> dbReports = associationService.getTargetAssocs(rawMaterialNodeRef, ReportModel.ASSOC_REPORTS);

			assertEquals(1, dbReports.size());
			// Check out
			logger.debug("checkout nodeRef: " + rawMaterialNodeRef);

			NodeRef destNodeRef = dbNodeService.getPrimaryParent(rawMaterialNodeRef).getParentRef();

			return entityVersionService.createBranch(rawMaterialNodeRef, destNodeRef);

		});

		final ProductData rawMaterial = inWriteTx(() -> {

			assertNotNull("Check working copy exists", workingCopyNodeRef);
			List<NodeRef> dbReports = associationService.getTargetAssocs(workingCopyNodeRef, ReportModel.ASSOC_REPORTS);
			assertEquals(1, dbReports.size());

			// Documents is moved on working copy
			assertNotNull(getFolderDocuments(rawMaterialNodeRef));
			assertNotNull(getFolderDocuments(workingCopyNodeRef));

			// Check aspect validation
			assertTrue(!dbNodeService.hasAspect(workingCopyNodeRef, PLMWorkflowModel.ASPECT_PRODUCT_VALIDATION_ASPECT));

			// Check costs on working copy
			ProductData rawMaterial1 = alfrescoRepository.findOne(rawMaterialNodeRef);
			ProductData workingCopyRawMaterial = alfrescoRepository.findOne(workingCopyNodeRef);
			assertEquals("Check costs size", rawMaterial1.getCostList().size(),
					workingCopyRawMaterial.getCostList().size());

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
		});

		final NodeRef newRawMaterialNodeRef = inWriteTx(() -> {

			try {
				beCPGDBTestMetricReporter.setEnabled(true);

				// Check in
				return entityVersionService.mergeBranch(workingCopyNodeRef, rawMaterialNodeRef, VersionType.MAJOR,
						"This is a test version", false, false);

			} finally {
				beCPGDBTestMetricReporter.setEnabled(false);
			}
		});

		validateNewVersion(newRawMaterialNodeRef, rawMaterialNodeRef, rawMaterial, productUnit, valueAdded, true);
		inWriteTx(() -> {
			assertTrue(
					!dbNodeService.hasAspect(newRawMaterialNodeRef, PLMWorkflowModel.ASPECT_PRODUCT_VALIDATION_ASPECT));
			assertNull(dbNodeService.getProperty(newRawMaterialNodeRef, PLMWorkflowModel.PROP_PV_VALIDATION_DATE));
			return null;
		});

	}

	private String getVersionLabel(ProductData newRawMaterial) {
		return (String) dbNodeService.getProperty(newRawMaterial.getNodeRef(), ContentModel.PROP_VERSION_LABEL);
	}

	/**
	 * Test get version history.
	 */
	@Test
	public void testGetVersionHistory() {

		inWriteTx(() -> {

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

			assertEquals("Check 1st version", versions[0].getVersionedNodeRef(),
					vRawMaterialNodeRef.getVersionedNodeRef());
			assertEquals("Check 2nd version", versions[1].getVersionedNodeRef(),
					vRawMaterialNodeRefV0_2.getVersionedNodeRef());

			return null;

		});
	}

	/**
	 * Test check out check in.
	 */
	// @Test
	public void testCheckOutCheckInValidProduct() {

		logger.info("testCheckOutCheckInValidProduct");

		final NodeRef rawMaterialNodeRef = inWriteTx(() -> {

			NodeRef rawMaterialNodeRef1 = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(),
					"MP test report");

			logger.debug("Add versionnable aspect");
			Map<QName, Serializable> aspectProperties = new HashMap<>();
			aspectProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
			dbNodeService.addAspect(rawMaterialNodeRef1, ContentModel.ASPECT_VERSIONABLE, aspectProperties);
			return rawMaterialNodeRef1;
		});

		inWriteTx(() -> {

			// Valid it
			dbNodeService.setProperty(rawMaterialNodeRef, PLMModel.PROP_PRODUCT_STATE, SystemState.Valid);
			dbNodeService.setProperty(rawMaterialNodeRef, BeCPGModel.PROP_ERP_CODE, ERP_CODE);
			// products
			hierarchyService.classifyByHierarchy(repositoryHelper.getCompanyHome(), rawMaterialNodeRef);

			String path = dbNodeService.getPath(rawMaterialNodeRef).toPrefixString(namespaceService);
			String expected = "/app:company_home/cm:rawMaterial/cm:Sea_x0020_food/cm:Fish/";
			assertEquals("check path", expected, path.substring(0, expected.length()));

			// Check out
			NodeRef destNodeRef = dbNodeService.getPrimaryParent(rawMaterialNodeRef).getParentRef();

			NodeRef workingCopyNodeRef = entityVersionService.createBranch(rawMaterialNodeRef, destNodeRef);

			logger.info("state " + rawMaterialNodeRef + " - "
					+ dbNodeService.getProperty(workingCopyNodeRef, PLMModel.PROP_PRODUCT_STATE));
			assertEquals("Check state new version", SystemState.Simulation.toString(),
					dbNodeService.getProperty(workingCopyNodeRef, PLMModel.PROP_PRODUCT_STATE));

			// Check in
			NodeRef newRawMaterialNodeRef;

			newRawMaterialNodeRef = entityVersionService.mergeBranch(workingCopyNodeRef, rawMaterialNodeRef,
					VersionType.MINOR, "This is a test version", false, false);

			assertNotNull("Check new version exists", newRawMaterialNodeRef);
			assertEquals("Check state new version", SystemState.Valid.toString(),
					dbNodeService.getProperty(newRawMaterialNodeRef, PLMModel.PROP_PRODUCT_STATE));

			VersionHistory versionHistory = versionService.getVersionHistory(newRawMaterialNodeRef);
			Version version = versionHistory.getVersion("1.1");
			assertNotNull(version);
			assertNotNull(entityVersionService.getEntityVersion(version));

			path = dbNodeService.getPath(rawMaterialNodeRef).toPrefixString(namespaceService);
			expected = "/app:company_home/cm:rawMaterial/cm:Sea_x0020_food/cm:Fish/";
			assertEquals("check path", expected, path.substring(0, expected.length()));

			// Check out
			destNodeRef = dbNodeService.getPrimaryParent(rawMaterialNodeRef).getParentRef();

			workingCopyNodeRef = entityVersionService.createBranch(rawMaterialNodeRef, destNodeRef);

			logger.info("state " + rawMaterialNodeRef + " - "
					+ dbNodeService.getProperty(workingCopyNodeRef, PLMModel.PROP_PRODUCT_STATE));
			assertEquals("Check state new version", SystemState.Simulation.toString(),
					dbNodeService.getProperty(workingCopyNodeRef, PLMModel.PROP_PRODUCT_STATE));

			// Check in
			newRawMaterialNodeRef = entityVersionService.mergeBranch(workingCopyNodeRef, rawMaterialNodeRef,
					VersionType.MINOR, "This is a test version", false, false);

			assertNotNull("Check new version exists", newRawMaterialNodeRef);
			assertEquals("Check state new version", SystemState.Valid.toString(),
					dbNodeService.getProperty(newRawMaterialNodeRef, PLMModel.PROP_PRODUCT_STATE));

			assertEquals("erpCode should be the same after checkout", ERP_CODE,
					dbNodeService.getProperty(newRawMaterialNodeRef, BeCPGModel.PROP_ERP_CODE));

			assertEquals("Check state new version", SystemState.Valid.toString(), dbNodeService
					.getProperty(entityVersionService.getEntityVersion(version), PLMModel.PROP_PRODUCT_STATE));

			return null;

		});
	}

	/**
	 * Test variants are not lost with checkOut/checkIn
	 */
	@Test
	public void testCheckOutCheckInVariant() {

		logger.info("testVariant");

		final NodeRef finishedProductNodeRef = inWriteTx(() -> {

			NodeRef finishedProductNodeRef1 = BeCPGPLMTestHelper.createMultiLevelProduct(getTestFolderNodeRef());

			// create variant
			Map<QName, Serializable> props = new HashMap<>();
			props.put(ContentModel.PROP_NAME, "variant");
			props.put(BeCPGModel.PROP_IS_DEFAULT_VARIANT, true);
			NodeRef variantNodeRef = dbNodeService.createNode(finishedProductNodeRef1, BeCPGModel.ASSOC_VARIANTS,
					BeCPGModel.ASSOC_VARIANTS, BeCPGModel.TYPE_VARIANT, props).getChildRef();

			ProductData productData = alfrescoRepository.findOne(finishedProductNodeRef1);
			productData.getCompoListView().getCompoList().get(2).setVariants(Collections.singletonList(variantNodeRef));
			alfrescoRepository.save(productData);

			// check variant before checkOut
			productData = alfrescoRepository.findOne(finishedProductNodeRef1);
			assertEquals(1, productData.getCompoListView().getCompoList().get(2).getVariants().size());
			logger.info("finishedProductNodeRef compoList is "
					+ productData.getCompoListView().getCompoList().get(2).getNodeRef() + " "
					+ dbNodeService.getPath(productData.getCompoListView().getCompoList().get(2).getNodeRef()));
			logger.info("finishedProductNodeRef variant is "
					+ productData.getCompoListView().getCompoList().get(2).getVariants() + " "
					+ dbNodeService.getPath(productData.getCompoListView().getCompoList().get(2).getVariants().get(0)));

			return finishedProductNodeRef1;
		});

		final NodeRef workingCopyNodeRef = inWriteTx(() -> {
			logger.info("checkout");
			// Check out
			NodeRef destNodeRef = dbNodeService.getPrimaryParent(finishedProductNodeRef).getParentRef();

			return entityVersionService.createBranch(finishedProductNodeRef, destNodeRef);
		});

		final NodeRef versionNodeRef = inWriteTx(() -> {

			// check variant before checkin
			ProductData productData = alfrescoRepository.findOne(workingCopyNodeRef);
			assertEquals(1, productData.getCompoListView().getCompoList().get(2).getVariants().size());
			logger.info("finishedProductNodeRef compoList is "
					+ productData.getCompoListView().getCompoList().get(2).getNodeRef() + " "
					+ dbNodeService.getPath(productData.getCompoListView().getCompoList().get(2).getNodeRef()));
			logger.info("finishedProductNodeRef variant is "
					+ productData.getCompoListView().getCompoList().get(2).getVariants() + " "
					+ dbNodeService.getPath(productData.getCompoListView().getCompoList().get(2).getVariants().get(0)));

			// Check in
			return entityVersionService.mergeBranch(workingCopyNodeRef, finishedProductNodeRef, VersionType.MAJOR,
					"This is a test version", false, false);

		});

		inWriteTx(() -> {

			assertNotNull("Check history version exists", versionNodeRef);

			ProductData productData = alfrescoRepository.findOne(versionNodeRef);
			assertEquals(1, dbNodeService
					.getChildAssocs(versionNodeRef, BeCPGModel.ASSOC_VARIANTS, RegexQNamePattern.MATCH_ALL).size());
			logger.info("finishedProductNodeRef compoList is "
					+ productData.getCompoListView().getCompoList().get(2).getNodeRef());
			logger.info("finishedProductNodeRef variant is "
					+ productData.getCompoListView().getCompoList().get(2).getVariants());
			assertEquals(1, productData.getCompoListView().getCompoList().get(2).getVariants().size());
			assertEquals(
					dbNodeService.getChildAssocs(versionNodeRef, BeCPGModel.ASSOC_VARIANTS, RegexQNamePattern.MATCH_ALL)
							.get(0).getChildRef(),
					productData.getCompoListView().getCompoList().get(2).getVariants().get(0));

			return null;

		});

		// Check orig node
		inWriteTx(() -> {

			ProductData productData = alfrescoRepository.findOne(finishedProductNodeRef);
			assertEquals(1, dbNodeService
					.getChildAssocs(versionNodeRef, BeCPGModel.ASSOC_VARIANTS, RegexQNamePattern.MATCH_ALL).size());
			logger.info("finishedProductNodeRef compoList is "
					+ productData.getCompoListView().getCompoList().get(2).getNodeRef());
			logger.info("finishedProductNodeRef variant is "
					+ productData.getCompoListView().getCompoList().get(2).getVariants());
			assertEquals(1, productData.getCompoListView().getCompoList().get(2).getVariants().size());
			assertEquals(
					dbNodeService.getChildAssocs(finishedProductNodeRef, BeCPGModel.ASSOC_VARIANTS,
							RegexQNamePattern.MATCH_ALL).get(0).getChildRef(),
					productData.getCompoListView().getCompoList().get(2).getVariants().get(0));

			return null;

		});
	}

	private NodeRef getFolderDocuments(NodeRef nodeRef) {
		return dbNodeService.getChildByName(nodeRef, ContentModel.ASSOC_CONTAINS, "Documents");
	}

	/**
	 * Test check out check in.
	 */
	@Test
	public void testCheckOutCheckInAssociations() {

		final NodeRef rawMaterialNodeRef = inWriteTx(() -> {
			logger.debug("Add versionnable aspect");

			NodeRef rawMaterialNodeRef1 = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(),
					"MP test report");
			if (!dbNodeService.hasAspect(rawMaterialNodeRef1, ContentModel.ASPECT_VERSIONABLE)) {
				Map<QName, Serializable> aspectProperties = new HashMap<>();
				aspectProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
				dbNodeService.addAspect(rawMaterialNodeRef1, ContentModel.ASPECT_VERSIONABLE, aspectProperties);
			}
			return rawMaterialNodeRef1;
		});

		inWriteTx(() -> {

			// suppliers
			String[] supplierNames = { "Supplier1", "Supplier2", "Supplier3" };
			List<NodeRef> supplierNodeRefs = new LinkedList<>();
			for (String supplierName : supplierNames) {
				NodeRef supplierNodeRef = null;
				NodeRef entityFolder = dbNodeService.getChildByName(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
						supplierName);
				if (entityFolder != null) {
					supplierNodeRef = dbNodeService.getChildByName(entityFolder, ContentModel.ASSOC_CONTAINS,
							supplierName);
				}

				if (supplierNodeRef == null) {
					Map<QName, Serializable> properties = new HashMap<>();
					properties.put(ContentModel.PROP_NAME, supplierName);
					supplierNodeRef = dbNodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
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
			NodeRef destNodeRef = dbNodeService.getPrimaryParent(rawMaterialNodeRef).getParentRef();

			NodeRef workingCopyNodeRef = entityVersionService.createBranch(rawMaterialNodeRef, destNodeRef);

			// add new Supplier
			associationService.update(workingCopyNodeRef, PLMModel.ASSOC_SUPPLIERS, supplierNodeRefs);

			// check-in
			entityVersionService.mergeBranch(workingCopyNodeRef, rawMaterialNodeRef, VersionType.MINOR,
					"This is a test version", false, false);

			return null;

		});

		inWriteTx(() -> {

			// check
			List<AssociationRef> targetNodeRefs = nodeService.getTargetAssocs(rawMaterialNodeRef,
					PLMModel.ASSOC_SUPPLIERS);

			assertEquals("Assert 3", 3, targetNodeRefs.size());

			// Check out

			NodeRef destNodeRef = dbNodeService.getPrimaryParent(rawMaterialNodeRef).getParentRef();

			NodeRef workingCopyNodeRef = entityVersionService.createBranch(rawMaterialNodeRef, destNodeRef);

			// remove Suppliers
			associationService.update(workingCopyNodeRef, PLMModel.ASSOC_SUPPLIERS, new ArrayList<NodeRef>());

			// check-in
			entityVersionService.mergeBranch(workingCopyNodeRef, rawMaterialNodeRef, VersionType.MINOR,
					"This is a test version", false, false);

			// check
			targetNodeRefs = nodeService.getTargetAssocs(rawMaterialNodeRef, PLMModel.ASSOC_SUPPLIERS);
			assertEquals(0, targetNodeRefs.size());

			return null;

		});
	}

	/**
	 * Test check out check in.
	 *
	 * @throws InterruptedException
	 */
	@Test
	public void testBranches() throws InterruptedException {

		final ProductUnit productUnit = ProductUnit.L;
		final int valueAdded = 1;

		final NodeRef rawMaterialNodeRef = inWriteTx(() -> {

			/*-- Create raw material --*/
			NodeRef r = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "Test Check in Check out branch");
			dbNodeService.setProperty(r, BeCPGModel.PROP_ERP_CODE, ERP_CODE);
			dbNodeService.setProperty(r, PLMWorkflowModel.PROP_PV_VALIDATION_DATE, new Date());
			dbNodeService.createAssociation(r, personService.getPerson(BeCPGTestHelper.USER_ONE),
					PLMWorkflowModel.ASSOC_PV_CALLER_ACTOR);
			return r;
		});

		inWriteTx(() -> {

			entityReportService.generateReports(rawMaterialNodeRef);

			return true;

		});

		final NodeRef destFolder = inWriteTx(() -> {
			return repoService.getOrCreateFolderByPath(getTestFolderNodeRef(), "Test ACL", "Test ACL");
		});

		final NodeRef branchNodeRef = inWriteTx(() -> {

			List<NodeRef> dbReports = associationService.getTargetAssocs(rawMaterialNodeRef, ReportModel.ASSOC_REPORTS);
			assertEquals(1, dbReports.size());

			// Check out
			logger.warn("branch nodeRef: " + rawMaterialNodeRef);

			try {
				beCPGDBTestMetricReporter.setEnabled(true);

				return entityVersionService.createBranch(rawMaterialNodeRef, getTestFolderNodeRef());

			} finally {
				beCPGDBTestMetricReporter.setEnabled(false);
			}

		});

		final Date validationDate = new Date();

		final ProductData rawMaterial = inWriteTx(() -> {

			assertNotNull("Check branch exists", branchNodeRef);
			List<NodeRef> dbReports = associationService.getTargetAssocs(branchNodeRef, ReportModel.ASSOC_REPORTS);
			assertEquals(1, dbReports.size());

			// Documents is moved on working copy
			assertNotNull(getFolderDocuments(rawMaterialNodeRef));
			assertNotNull(getFolderDocuments(branchNodeRef));

			// Check productCode
			assertNotSame("productCode should be different in branch",
					dbNodeService.getProperty(rawMaterialNodeRef, BeCPGModel.PROP_CODE),
					dbNodeService.getProperty(branchNodeRef, BeCPGModel.PROP_CODE));

			assertNull("ERP code should be null", dbNodeService.getProperty(branchNodeRef, BeCPGModel.PROP_ERP_CODE));

			// Check aspect validation
			assertTrue(!dbNodeService.hasAspect(branchNodeRef, PLMWorkflowModel.ASPECT_PRODUCT_VALIDATION_ASPECT));

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
			dbNodeService.setProperty(branchNodeRef, PLMWorkflowModel.PROP_PV_VALIDATION_DATE, validationDate);
			dbNodeService.createAssociation(branchNodeRef, personService.getPerson(BeCPGTestHelper.USER_ONE),
					PLMWorkflowModel.ASSOC_PV_CALLER_ACTOR);

			return rawMaterial1;
		});

		assertTrue(dbNodeService.hasAspect(branchNodeRef, PLMWorkflowModel.ASPECT_PRODUCT_VALIDATION_ASPECT));
		assertEquals(dbNodeService.getProperty(branchNodeRef, PLMWorkflowModel.PROP_PV_VALIDATION_DATE),
				validationDate);

		final NodeRef newRawMaterialNodeRef = inWriteTx(() -> {

			// Check out
			logger.warn("merge and move branch: " + rawMaterialNodeRef);

			try {
				beCPGDBTestMetricReporter.setEnabled(true);

				NodeRef ret = entityVersionService.mergeBranch(branchNodeRef, rawMaterialNodeRef, VersionType.MAJOR,
						"This is a test version");
				repoService.moveNode(ret, destFolder);
				return ret;
			} finally {
				beCPGDBTestMetricReporter.setEnabled(false);
			}

		});

		validateNewVersion(newRawMaterialNodeRef, rawMaterialNodeRef, rawMaterial, productUnit, valueAdded, false);

		assertTrue(dbNodeService.hasAspect(newRawMaterialNodeRef, PLMWorkflowModel.ASPECT_PRODUCT_VALIDATION_ASPECT));
		assertEquals(dbNodeService.getProperty(newRawMaterialNodeRef, PLMWorkflowModel.PROP_PV_VALIDATION_DATE),
				validationDate);

	}

	@Test
	public void testDeleteVersion() throws InterruptedException {

		final NodeRef rawMaterialNodeRef = inWriteTx(() -> {

			/*-- Create raw material --*/
			NodeRef r = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test report");
			dbNodeService.setProperty(r, BeCPGModel.PROP_ERP_CODE, ERP_CODE);
			return r;
		});

		inWriteTx(() -> {

			entityReportService.generateReports(rawMaterialNodeRef);

			return true;

		});

		if (!dbNodeService.hasAspect(rawMaterialNodeRef, ContentModel.ASPECT_VERSIONABLE)) {
			inWriteTx(() -> {
				logger.debug("Add versionnable aspect");
				Map<QName, Serializable> aspectProperties = new HashMap<>();
				aspectProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
				dbNodeService.addAspect(rawMaterialNodeRef, ContentModel.ASPECT_VERSIONABLE, aspectProperties);
				return rawMaterialNodeRef;
			});

		}

		final NodeRef workingCopyNodeRef = inWriteTx(() -> {

			List<NodeRef> dbReports = associationService.getTargetAssocs(rawMaterialNodeRef, ReportModel.ASSOC_REPORTS);

			assertEquals(1, dbReports.size());
			// Check out
			logger.debug("checkout nodeRef: " + rawMaterialNodeRef);

			NodeRef destNodeRef = dbNodeService.getPrimaryParent(rawMaterialNodeRef).getParentRef();

			return entityVersionService.createBranch(rawMaterialNodeRef, destNodeRef);

		});

		final NodeRef newRawMaterialNodeRef = inWriteTx(() -> {

			// Check in
			return entityVersionService.mergeBranch(workingCopyNodeRef, rawMaterialNodeRef, VersionType.MAJOR,
					"This is a test version", false, false);
		});

		inWriteTx(() -> {
			assertEquals(newRawMaterialNodeRef, rawMaterialNodeRef);
			assertNotNull(versionService.getVersionHistory(rawMaterialNodeRef));
			return null;

		});

		inWriteTx(() -> {
			for (EntityVersion entityVersion : entityVersionService.getAllVersions(rawMaterialNodeRef)) {
				assertNull(versionService.getVersionHistory(entityVersion.getEntityVersionNodeRef()));
			}
			return null;

		});

		inWriteTx(() -> {
			dbNodeService.deleteNode(rawMaterialNodeRef);
			return null;

		});
		inWriteTx(() -> {
			assertNotNull(versionService.getVersionHistory(rawMaterialNodeRef));
			assertFalse(dbNodeService.exists(rawMaterialNodeRef));
			return null;

		});

		inWriteTx(() -> {

			NodeRef archiveNodeRef = nodeArchiveService.getArchivedNode(rawMaterialNodeRef);
			nodeArchiveService.restoreArchivedNode(archiveNodeRef);

			return null;

		});
		inWriteTx(() -> {
			assertTrue(dbNodeService.exists(rawMaterialNodeRef));
			assertNotNull(versionService.getVersionHistory(rawMaterialNodeRef));
			return null;

		});

		inWriteTx(() -> {
			dbNodeService.addAspect(rawMaterialNodeRef, ContentModel.ASPECT_TEMPORARY, null);
			dbNodeService.deleteNode(rawMaterialNodeRef);

			return null;

		});

		inWriteTx(() -> {
			assertNull(versionService.getVersionHistory(rawMaterialNodeRef));
			assertFalse(dbNodeService.exists(rawMaterialNodeRef));
			return null;

		});

	}

	@Test
	public void testPurgeVersion() throws InterruptedException {

		final NodeRef rawMaterialNodeRef = inWriteTx(() -> {

			/*-- Create raw material --*/
			NodeRef r = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test report");
			dbNodeService.setProperty(r, BeCPGModel.PROP_ERP_CODE, ERP_CODE);
			return r;
		});

		inWriteTx(() -> {
			logger.info("Generate reports");
			entityReportService.generateReports(rawMaterialNodeRef);

			return true;

		});

		if (!dbNodeService.hasAspect(rawMaterialNodeRef, ContentModel.ASPECT_VERSIONABLE)) {
			inWriteTx(() -> {
				logger.debug("Add versionnable aspect");
				Map<QName, Serializable> aspectProperties = new HashMap<>();
				aspectProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
				dbNodeService.addAspect(rawMaterialNodeRef, ContentModel.ASPECT_VERSIONABLE, aspectProperties);
				return rawMaterialNodeRef;
			});

		}

		final NodeRef workingCopyNodeRef = inWriteTx(() -> {
			logger.info("createBranch");
			List<NodeRef> dbReports = associationService.getTargetAssocs(rawMaterialNodeRef, ReportModel.ASSOC_REPORTS);

			assertEquals(1, dbReports.size());
			// Check out
			logger.debug("checkout nodeRef: " + rawMaterialNodeRef);

			NodeRef destNodeRef = dbNodeService.getPrimaryParent(rawMaterialNodeRef).getParentRef();

			return entityVersionService.createBranch(rawMaterialNodeRef, destNodeRef);

		});

		NodeRef newRawMaterialNodeRef = inWriteTx(() -> {
			logger.info("mergeBranch");
			// Check in
			return entityVersionService.mergeBranch(workingCopyNodeRef, rawMaterialNodeRef, VersionType.MAJOR,
					"This is a test version", false, false);
		});

		inWriteTx(() -> {

			assertEquals(newRawMaterialNodeRef, rawMaterialNodeRef);
			assertNotNull(getVersionHistoryNodeRef(rawMaterialNodeRef));
			assertNotNull(versionService.getVersionHistory(rawMaterialNodeRef));
			return null;
		});

		inWriteTx(() -> {
			logger.info("deleteNode");
			dbNodeService.deleteNode(rawMaterialNodeRef);
			return null;

		});

		inWriteTx(() -> {
			assertNotNull(getVersionHistoryNodeRef(rawMaterialNodeRef));
			assertFalse(dbNodeService.exists(rawMaterialNodeRef));
			return null;

		});

		inWriteTx(() -> {

			NodeRef archiveNodeRef = nodeArchiveService.getArchivedNode(rawMaterialNodeRef);
			nodeArchiveService.purgeArchivedNode(archiveNodeRef);

			return null;

		});

		inWriteTx(() -> {
			assertNull(getVersionHistoryNodeRef(rawMaterialNodeRef));
			assertFalse(dbNodeService.exists(rawMaterialNodeRef));
			return null;

		});

	}

	private Object getVersionHistoryNodeRef(NodeRef rawMaterialNodeRef) {
		NodeRef rootNode = dbNodeService.getRootNode(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, Version2Model.STORE_ID));
		return dbNodeService.getChildByName(rootNode, Version2Model.CHILD_QNAME_VERSION_HISTORIES,
				rawMaterialNodeRef.getId());
	}

	private void validateNewVersion(final NodeRef newRawMaterialNodeRef, final NodeRef rawMaterialNodeRef,
			final ProductData rawMaterial, final ProductUnit productUnit, final int valueAdded, boolean secondCheckin) {

		inWriteTx(() -> {

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
					dbNodeService.getProperty(newRawMaterialNodeRef, BeCPGModel.PROP_ERP_CODE));

			// Check costs on new version
			assertEquals("Check costs size", rawMaterial.getCostList().size(), newRawMaterial.getCostList().size());

			for (int i = 0; i < rawMaterial.getCostList().size(); i++) {
				CostListDataItem costListDataItem = rawMaterial.getCostList().get(i);
				CostListDataItem vCostListDataItem = newRawMaterial.getCostList().get(i);

				assertEquals("Check cost", costListDataItem.getCost(), vCostListDataItem.getCost());
				assertEquals("Check cost value", (costListDataItem.getValue() + valueAdded),
						vCostListDataItem.getValue());
				assertNotSame("Check cost noderef", costListDataItem.getNodeRef(), vCostListDataItem.getNodeRef());
			}

			assertEquals("Check products are the same", rawMaterialNodeRef, newRawMaterialNodeRef);

			// 2nd Check out, Check in
			if (secondCheckin) {

				NodeRef destNodeRef = dbNodeService.getPrimaryParent(rawMaterialNodeRef).getParentRef();

				NodeRef workingCopy2NodeRef = entityVersionService.createBranch(rawMaterialNodeRef, destNodeRef);

				return entityVersionService.mergeBranch(workingCopy2NodeRef, rawMaterialNodeRef, VersionType.MAJOR,
						"This is a test version", false, false);
			}
			return rawMaterialNodeRef;

		});

		if (secondCheckin) {
			inWriteTx(() -> {

				ProductData newRawMaterial = alfrescoRepository.findOne(newRawMaterialNodeRef);
				assertEquals("Check version", "3.0", getVersionLabel(newRawMaterial));
				VersionHistory versionHistory = versionService.getVersionHistory(newRawMaterialNodeRef);
				Version version = versionHistory.getVersion("3.0");
				assertNotNull(version);
				assertNotNull(entityVersionService.getEntityVersion(version));

				// Check cost Unit has changed after transaction
				for (int i = 0; i < newRawMaterial.getCostList().size(); i++) {
					CostListDataItem vCostListDataItem = newRawMaterial.getCostList().get(i);
					Boolean fixedCost = (Boolean) dbNodeService.getProperty(vCostListDataItem.getCost(),
							PLMModel.PROP_COSTFIXED);
					if ((fixedCost == null) || fixedCost.equals(Boolean.FALSE)) {
						assertTrue("Check cost unit", vCostListDataItem.getUnit().endsWith("/L"));
					}
				}

				// documents are restored under orig node
				assertNotNull(getFolderDocuments(rawMaterialNodeRef));

				return null;

			});
		}

	}

	@Test
	public void testModifiedDateAfterMerge() throws InterruptedException {

		final long waitTime = 10000;

		NodeRef originalBranch = inWriteTx(() -> {
			return BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "Test modified date afer merge");
		});

		Date originalModifiedDate = (Date) nodeService.getProperty(originalBranch, ContentModel.PROP_MODIFIED);

		assertNotNull(originalModifiedDate);

		final NodeRef newBbranch = inWriteTx(() -> {
			return entityVersionService.createBranch(originalBranch, getTestFolderNodeRef());
		});

		Date branchModifiedDate = (Date) nodeService.getProperty(newBbranch, ContentModel.PROP_MODIFIED);

		assertNotNull(branchModifiedDate);

		assertNotEquals(originalModifiedDate, branchModifiedDate);

		Thread.sleep(waitTime);

		Date mergedModifiedDate = inWriteTx(() -> {
			NodeRef mergedBranch = entityVersionService.mergeBranch(newBbranch, originalBranch, VersionType.MAJOR,
					"test merge");
			return (Date) nodeService.getProperty(mergedBranch, ContentModel.PROP_MODIFIED);
		});

		assertNotNull(mergedModifiedDate);

		assertNotEquals(originalModifiedDate, mergedModifiedDate);

		assertNotEquals(branchModifiedDate, mergedModifiedDate);

		assertTrue(mergedModifiedDate.getTime() - originalModifiedDate.getTime() >= waitTime);

		assertTrue(mergedModifiedDate.getTime() - branchModifiedDate.getTime() >= waitTime);

	}

	@Test
	public void testEffectivityAfterMerge() {

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		Date firstDate = cal.getTime();

		NodeRef entity = inWriteTx(() -> {
			NodeRef nodeRef = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(),
					"Test effectivity date after merge");
			nodeService.setProperty(nodeRef, BeCPGModel.PROP_START_EFFECTIVITY, firstDate);
			return nodeRef;
		});

		cal.add(Calendar.DAY_OF_MONTH, 1);
		Date secondDate = cal.getTime();

		NodeRef branch1 = inWriteTx(() -> {
			NodeRef branch = entityVersionService.createBranch(entity, getTestFolderNodeRef());
			associationService.update(branch, BeCPGModel.ASSOC_AUTO_MERGE_TO, entity);
			return branch;
		});

		NodeRef merged1 = inWriteTx(() -> {
			return entityVersionService.mergeBranch(branch1, secondDate);
		});

		inWriteTx(() -> {
			VersionHistory versionHistory = versionService.getVersionHistory(merged1);
			Version version = versionHistory.getVersion("1.0");
			NodeRef entityVersion = entityVersionService.getEntityVersion(version);
			assertEquals(firstDate, nodeService.getProperty(entityVersion, BeCPGModel.PROP_START_EFFECTIVITY));
			assertEquals(secondDate, nodeService.getProperty(entityVersion, BeCPGModel.PROP_END_EFFECTIVITY));

			version = versionHistory.getVersion("1.1");
			entityVersion = entityVersionService.getEntityVersion(version);
			assertEquals(secondDate, nodeService.getProperty(entityVersion, BeCPGModel.PROP_START_EFFECTIVITY));

			return null;
		});

		cal.add(Calendar.DAY_OF_MONTH, 1);
		Date thirdDate = cal.getTime();

		NodeRef branch2 = inWriteTx(() -> {
			NodeRef branch = entityVersionService.createBranch(entity, getTestFolderNodeRef());
			associationService.update(branch, BeCPGModel.ASSOC_AUTO_MERGE_TO, entity);
			return branch;
		});

		NodeRef merged2 = inWriteTx(() -> {
			return entityVersionService.mergeBranch(branch2, thirdDate);
		});

		inWriteTx(() -> {
			VersionHistory versionHistory = versionService.getVersionHistory(merged2);
			Version version = versionHistory.getVersion("1.1");
			NodeRef entityVersion = entityVersionService.getEntityVersion(version);
			assertEquals(secondDate, nodeService.getProperty(entityVersion, BeCPGModel.PROP_START_EFFECTIVITY));
			assertEquals(thirdDate, nodeService.getProperty(entityVersion, BeCPGModel.PROP_END_EFFECTIVITY));

			version = versionHistory.getVersion("1.2");
			entityVersion = entityVersionService.getEntityVersion(version);
			assertEquals(thirdDate, nodeService.getProperty(entityVersion, BeCPGModel.PROP_START_EFFECTIVITY));

			return null;
		});

	}

}
