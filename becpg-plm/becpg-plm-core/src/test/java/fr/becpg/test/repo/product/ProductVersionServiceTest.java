/*
 * 
 */
package fr.becpg.test.repo.product;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.NodeRef;
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

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.ReportModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.hierarchy.HierarchyService;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.CompoListUnit;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.test.BeCPGPLMTestHelper;
import fr.becpg.test.PLMBaseTestCase;

/**
 * The Class ProductVersionServiceTest.
 * 
 * @author querephi
 */
public class ProductVersionServiceTest extends PLMBaseTestCase {

	/** The logger. */
	private static Log logger = LogFactory.getLog(ProductVersionServiceTest.class);

	@Resource
	private CheckOutCheckInService checkOutCheckInService;

	@Resource
	private VersionService versionService;

	@Resource
	private ProductService productService;

	@Resource
	private NamespaceService namespaceService;

	@Resource
	private EntityVersionService entityVersionService;

	@Resource
	private AssociationService associationService;

	@Resource
	private EntityReportService entityReportService;

	@Resource
	private EntityService entityService;

	@Resource
	private HierarchyService hierarchyService;

	private NodeRef rawMaterialNodeRef;
	private NodeRef finishedProductNodeRef;

	/**
	 * Test check out check in.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testCheckOutCheckIn() throws InterruptedException {

		final ProductUnit productUnit = ProductUnit.L;
		final int valueAdded = 1;

		final NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<NodeRef>() {
					@Override
					public NodeRef execute() throws Throwable {

						/*-- Create raw material --*/
						NodeRef r = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test report");

						return r;
					}
				}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {
				entityReportService.generateReport(rawMaterialNodeRef);
				return null;
			}
		}, false, true);

		if (!nodeService.hasAspect(rawMaterialNodeRef, ContentModel.ASPECT_VERSIONABLE)) {
			transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {

				@Override
				public NodeRef execute() throws Throwable {
					logger.debug("Add versionnable aspect");
					Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>();
					aspectProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
					nodeService.addAspect(rawMaterialNodeRef, ContentModel.ASPECT_VERSIONABLE, aspectProperties);
					return rawMaterialNodeRef;
				}

			}, false, true);

		}

		final NodeRef workingCopyNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<NodeRef>() {
					@Override
					public NodeRef execute() throws Throwable {

						List<NodeRef> dbReports = associationService.getTargetAssocs(rawMaterialNodeRef, ReportModel.ASSOC_REPORTS);
						
						assertEquals(1, dbReports.size());
						// Check out
						logger.debug("checkout nodeRef: " + rawMaterialNodeRef);
						return checkOutCheckInService.checkout(rawMaterialNodeRef);
						
					}
				}, false, true);

		final ProductData rawMaterial = transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<ProductData>() {
					@Override
					public ProductData execute() throws Throwable {

						assertNotNull("Check working copy exists", workingCopyNodeRef);
						List<NodeRef> dbReports = associationService.getTargetAssocs(workingCopyNodeRef, ReportModel.ASSOC_REPORTS);
						assertEquals(1, dbReports.size());

						// Documents is moved on working copy
						assertNotNull(getFolderDocuments(rawMaterialNodeRef));
						assertNotNull(getFolderDocuments(workingCopyNodeRef));

						// Check productCode
						assertEquals("productCode should be the same after checkout",
								nodeService.getProperty(rawMaterialNodeRef, BeCPGModel.PROP_CODE),
								nodeService.getProperty(workingCopyNodeRef, BeCPGModel.PROP_CODE));

						// Check costs on working copy
						ProductData rawMaterial = alfrescoRepository.findOne(rawMaterialNodeRef);
						ProductData workingCopyRawMaterial = alfrescoRepository.findOne(workingCopyNodeRef);
						assertEquals("Check costs size", rawMaterial.getCostList().size(), workingCopyRawMaterial.getCostList().size());

						for (int i = 0; i < rawMaterial.getCostList().size(); i++) {
							CostListDataItem costListDataItem = rawMaterial.getCostList().get(i);
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

						return rawMaterial;
					}
				}, false, true);

		final NodeRef newRawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<NodeRef>() {
					@Override
					public NodeRef execute() throws Throwable {

						// Check in
						Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
						versionProperties.put(Version.PROP_DESCRIPTION, "This is a test version");
						versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
						return checkOutCheckInService.checkin(workingCopyNodeRef, versionProperties);
					}

				}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

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

				// Check productCode
				assertEquals("productCode should be the same after checkin", nodeService.getProperty(rawMaterialNodeRef, BeCPGModel.PROP_CODE),
						nodeService.getProperty(newRawMaterialNodeRef, BeCPGModel.PROP_CODE));

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
				NodeRef workingCopy2NodeRef = checkOutCheckInService.checkout(rawMaterialNodeRef);

				Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
				versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
				versionProperties.put(Version.PROP_DESCRIPTION, "description");
				return checkOutCheckInService.checkin(workingCopy2NodeRef, versionProperties);

			}
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

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
					if (fixedCost == null || fixedCost.equals(Boolean.FALSE)) {
						assertTrue("Check cost unit", vCostListDataItem.getUnit().endsWith("/L"));
					}
				}

				// documents are restored under orig node
				assertNotNull(getFolderDocuments(rawMaterialNodeRef));

				return null;

			}

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

		final NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<NodeRef>() {
					@Override
					public NodeRef execute() throws Throwable {

						/*-- Create raw material --*/
						return BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test report");

					}
				}, false, true);

		for (int i = 0; i < 2; i++) {

			final NodeRef workingCopyNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(
					new RetryingTransactionCallback<NodeRef>() {
						@Override
						public NodeRef execute() throws Throwable {

							entityReportService.generateReport(rawMaterialNodeRef);
							return checkOutCheckInService.checkout(rawMaterialNodeRef);

						}
					}, false, true);

			transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
				@Override
				public NodeRef execute() throws Throwable {

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
					assertTrue(versionService.getVersionHistory(rawMaterialNodeRef)==null || versionService.getVersionHistory(rawMaterialNodeRef).getAllVersions().size() == 1);
					// Check
					rawMaterial = alfrescoRepository.findOne(rawMaterialNodeRef);
					assertEquals("Check unit", ProductUnit.kg, rawMaterial.getUnit());
					return null;

				}
			}, false, true);
		}
	}

	/**
	 * Test get version history.
	 */
	@Test
	public void testGetVersionHistory() {

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				NodeRef rawMaterialNodeRef = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test report");
				Map<String, Serializable> properties = new HashMap<String, Serializable>();
				properties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
				Version vRawMaterialNodeRef = versionService.createVersion(rawMaterialNodeRef, properties);

				assertEquals("1.0", vRawMaterialNodeRef.getVersionLabel());

				properties = new HashMap<String, Serializable>();
				properties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
				Version vRawMaterialNodeRefV0_2 = versionService.createVersion(rawMaterialNodeRef, properties);
				assertEquals("1.1", vRawMaterialNodeRefV0_2.getVersionLabel());

				VersionHistory versionHistory = versionService.getVersionHistory(rawMaterialNodeRef);
				assertEquals("Should have 2 versions", 2, versionHistory.getAllVersions().size());
				Version[] versions = versionHistory.getAllVersions().toArray(new Version[2]);

				assertEquals("Check 1st version", versions[0].getVersionedNodeRef(), vRawMaterialNodeRef.getVersionedNodeRef());
				assertEquals("Check 2nd version", versions[1].getVersionedNodeRef(), vRawMaterialNodeRefV0_2.getVersionedNodeRef());

				return null;

			}
		}, false, true);
	}

	/**
	 * Test check out check in.
	 */
	@Test
	public void testCheckOutCheckInValidProduct() {

		logger.info("testCheckOutCheckInValidProduct");

		final NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<NodeRef>() {

					@Override
					public NodeRef execute() throws Throwable {

						NodeRef rawMaterialNodeRef = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test report");

						logger.debug("Add versionnable aspect");
						Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>();
						aspectProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
						nodeService.addAspect(rawMaterialNodeRef, ContentModel.ASPECT_VERSIONABLE, aspectProperties);
						return rawMaterialNodeRef;
					}

				}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				// Valid it
				nodeService.setProperty(rawMaterialNodeRef, PLMModel.PROP_PRODUCT_STATE, SystemState.Valid);
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
				NodeRef newRawMaterialNodeRef = null;

				Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
				versionProperties.put(Version.PROP_DESCRIPTION, "This is a test version");
				versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
				newRawMaterialNodeRef = checkOutCheckInService.checkin(workingCopyNodeRef, versionProperties);

				assertNotNull("Check new version exists", newRawMaterialNodeRef);
				assertEquals("Check state new version", SystemState.Simulation.toString(),
						nodeService.getProperty(newRawMaterialNodeRef, PLMModel.PROP_PRODUCT_STATE));

				VersionHistory versionHistory = versionService.getVersionHistory(newRawMaterialNodeRef);
				Version version = versionHistory.getVersion("1.1");
				assertNotNull(version);
				assertNotNull(entityVersionService.getEntityVersion(version));
				path = nodeService.getPath(rawMaterialNodeRef).toPrefixString(namespaceService);
				expected = "/app:company_home/cm:rawMaterial/cm:Sea_x0020_food/cm:Fish/";
				assertEquals("check path", expected, path.substring(0, expected.length()));

				return null;

			}
		}, false, true);
	}

	/**
	 * Test variants are not lost with checkOut/checkIn
	 */
	@Test
	public void testCheckOutCheckInVariant() {

		logger.info("testVariant");

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				finishedProductNodeRef = BeCPGPLMTestHelper.createMultiLevelProduct(getTestFolderNodeRef());

				// create variant
				Map<QName, Serializable> props = new HashMap<>();
				props.put(ContentModel.PROP_NAME, "variant");
				props.put(PLMModel.PROP_IS_DEFAULT_VARIANT, true);
				NodeRef variantNodeRef = nodeService.createNode(finishedProductNodeRef, PLMModel.ASSOC_VARIANTS, PLMModel.ASSOC_VARIANTS,
						PLMModel.TYPE_VARIANT, props).getChildRef();
				
				logger.debug("Add versionnable aspect");
				Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>();
				aspectProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
				nodeService.addAspect(finishedProductNodeRef, ContentModel.ASPECT_VERSIONABLE, aspectProperties);
				

				ProductData productData = alfrescoRepository.findOne(finishedProductNodeRef);
				productData.getCompoListView().getCompoList().get(2).setVariants(Arrays.asList(variantNodeRef));
				alfrescoRepository.save(productData);

				// check variant before checkOut
				productData = alfrescoRepository.findOne(finishedProductNodeRef);
				assertEquals(1, productData.getCompoListView().getCompoList().get(2).getVariants().size());
				logger.info("finishedProductNodeRef compoList is " + productData.getCompoListView().getCompoList().get(2).getNodeRef() + " "
						+ nodeService.getPath(productData.getCompoListView().getCompoList().get(2).getNodeRef()));
				logger.info("finishedProductNodeRef variant is " + productData.getCompoListView().getCompoList().get(2).getVariants() + " "
						+ nodeService.getPath(productData.getCompoListView().getCompoList().get(2).getVariants().get(0)));

				return null;
			}
		}, false, true);

		final NodeRef workingCopyNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<NodeRef>() {
					@Override
					public NodeRef execute() throws Throwable {
						logger.info("checkout");
						// Check out
						return checkOutCheckInService.checkout(finishedProductNodeRef);

					}
				}, false, true);

		final NodeRef versionNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				// check variant before checkin
				ProductData productData = alfrescoRepository.findOne(workingCopyNodeRef);
				assertEquals(1, productData.getCompoListView().getCompoList().get(2).getVariants().size());
				logger.info("finishedProductNodeRef compoList is " + productData.getCompoListView().getCompoList().get(2).getNodeRef() + " "
						+ nodeService.getPath(productData.getCompoListView().getCompoList().get(2).getNodeRef()));
				logger.info("finishedProductNodeRef variant is " + productData.getCompoListView().getCompoList().get(2).getVariants() + " "
						+ nodeService.getPath(productData.getCompoListView().getCompoList().get(2).getVariants().get(0)));

				// Check in
				Map<String, Serializable> versionProperties = new HashMap<String, Serializable>(1);
				versionProperties.put(Version.PROP_DESCRIPTION, "This is a test version");
				versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
				return checkOutCheckInService.checkin(workingCopyNodeRef, versionProperties);

			}
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				assertNotNull("Check history version exists", versionNodeRef);

				ProductData productData = alfrescoRepository.findOne(versionNodeRef);
				assertEquals(1, nodeService.getChildAssocs(versionNodeRef, PLMModel.ASSOC_VARIANTS, RegexQNamePattern.MATCH_ALL).size());
				logger.info("finishedProductNodeRef compoList is " + productData.getCompoListView().getCompoList().get(2).getNodeRef());
				logger.info("finishedProductNodeRef variant is " + productData.getCompoListView().getCompoList().get(2).getVariants());
				assertEquals(1, productData.getCompoListView().getCompoList().get(2).getVariants().size());
				assertEquals(nodeService.getChildAssocs(versionNodeRef, PLMModel.ASSOC_VARIANTS, RegexQNamePattern.MATCH_ALL).get(0).getChildRef(),
						productData.getCompoListView().getCompoList().get(2).getVariants().get(0));

				return null;

			}
		}, false, true);

		// Check orig node
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				ProductData productData = alfrescoRepository.findOne(finishedProductNodeRef);
				assertEquals(1, nodeService.getChildAssocs(versionNodeRef, PLMModel.ASSOC_VARIANTS, RegexQNamePattern.MATCH_ALL).size());
				logger.info("finishedProductNodeRef compoList is " + productData.getCompoListView().getCompoList().get(2).getNodeRef());
				logger.info("finishedProductNodeRef variant is " + productData.getCompoListView().getCompoList().get(2).getVariants());
				assertEquals(1, productData.getCompoListView().getCompoList().get(2).getVariants().size());
				assertEquals(nodeService.getChildAssocs(finishedProductNodeRef, PLMModel.ASSOC_VARIANTS, RegexQNamePattern.MATCH_ALL).get(0)
						.getChildRef(), productData.getCompoListView().getCompoList().get(2).getVariants().get(0));

				return null;

			}
		}, false, true);
	}

	/**
	 * Test some lists are version sensitive
	 */
	public void xxcommentedtestVersionSensitiveList() {

		logger.info("testVersionSensitiveList");

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				rawMaterialNodeRef = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test report");

				ProductData fpData = new FinishedProductData();
				fpData.setName("FP");
				fpData.setHierarchy1(HIERARCHY1_FROZEN_REF);
				fpData.setHierarchy2(HIERARCHY2_PIZZA_REF);
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, (CompoListDataItem) null, 2d, null, CompoListUnit.kg, null, DeclarationType.Declare,
						rawMaterialNodeRef));
				fpData.getCompoListView().setCompoList(compoList);
				finishedProductNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), fpData).getNodeRef();

				// add Checkout aspect
				Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
				props.put(ContentModel.PROP_VERSION_LABEL, "0.1");
				nodeService.addAspect(rawMaterialNodeRef, ContentModel.ASPECT_VERSIONABLE, props);

				// Check out
				NodeRef workingCopyNodeRef = checkOutCheckInService.checkout(rawMaterialNodeRef);

				// Check in
				NodeRef newRawMaterialNodeRef = null;

				Map<String, Serializable> versionProperties = new HashMap<String, Serializable>(1);
				versionProperties.put(Version.PROP_DESCRIPTION, "This is a test version");
				versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
				newRawMaterialNodeRef = checkOutCheckInService.checkin(workingCopyNodeRef, versionProperties);

				assertNotNull("Check new version exists", newRawMaterialNodeRef);

				return null;

			}
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				// check version sensitive
				VersionHistory versionHistory = versionService.getVersionHistory(rawMaterialNodeRef);
				Version version = versionHistory.getVersion("1.0");
				assertNotNull(version);

				NodeRef entityVersionRef = entityVersionService.getEntityVersion(version);
				ProductData fpData = alfrescoRepository.findOne(finishedProductNodeRef);

				assertEquals(entityVersionRef, fpData.getCompoListView().getCompoList().get(0).getProduct());

				return null;

			}
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

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				NodeRef rawMaterialNodeRef = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test report");

				// suppliers
				String[] supplierNames = { "Supplier1", "Supplier2", "Supplier3" };
				List<NodeRef> supplierNodeRefs = new LinkedList<NodeRef>();
				for (String supplierName : supplierNames) {
					NodeRef supplierNodeRef = null;
					NodeRef entityFolder = nodeService.getChildByName(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS, supplierName);
					if (entityFolder != null) {
						supplierNodeRef = nodeService.getChildByName(entityFolder, ContentModel.ASSOC_CONTAINS, supplierName);
					}

					if (supplierNodeRef == null) {
						Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
						properties.put(ContentModel.PROP_NAME, supplierName);
						supplierNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
								QName.createQName((String) properties.get(ContentModel.PROP_NAME)), PLMModel.TYPE_SUPPLIER, properties).getChildRef();
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
				Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
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

			}
		}, false, true);
	}

}
