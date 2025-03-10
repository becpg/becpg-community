/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.product;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.repo.security.authority.AuthorityDAO;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.entity.datalist.WUsedListService;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.hierarchy.HierarchyService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.test.BeCPGPLMTestHelper;
import fr.becpg.test.PLMBaseTestCase;

// TODO: Auto-generated Javadoc
/**
 * The Class ProductServiceTest.
 *
 * @author querephi
 */
public class ProductServiceIT extends PLMBaseTestCase {

	private static final int WUSED_LEVEL = 1;

	private static final Log logger = LogFactory.getLog(ProductServiceIT.class);

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private AuthorityService authorityService;

	@Autowired
	private AuthorityDAO authorityDAO;

	@Autowired
	private EntityReportService entityReportService;

	@Autowired
	private EntityTplService entityTplService;

	@Autowired
	private HierarchyService hierarchyService;

	@Autowired
	private WUsedListService wUsedListService;

	@Autowired
	private EntityService entityService;

	/**
	 * Test create product.
	 */
	@Test
	public void testCreateProduct() {
		Assert.assertNotNull(inWriteTx(() -> {
			return BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test report");
		}));
	}

	/**
	 * Test report product.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testReportProduct() throws Exception {

		final NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Create images folder --*/
			NodeRef imagesNodeRef = fileFolderService
					.create(getTestFolderNodeRef(), TranslateHelper.getTranslatedPath(RepoConsts.PATH_IMAGES), ContentModel.TYPE_FOLDER).getNodeRef();

			addProductImage(imagesNodeRef, PLMModel.TYPE_FINISHEDPRODUCT);

			/*-- Create product --*/
			logger.debug("Create product");
			return BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test report");

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Generate report --*/
			entityReportService.generateReports(rawMaterialNodeRef);

			/*-- Check report --*/
			logger.debug("/*-- Check report --*/");
			NodeRef documentsNodeRef = nodeService.getChildByName(rawMaterialNodeRef, ContentModel.ASSOC_CONTAINS, "Documents");
			assertNotNull(documentsNodeRef);

			NodeRef documentNodeRef = nodeService.getChildByName(documentsNodeRef, ContentModel.ASSOC_CONTAINS,
					"MP test report - Fiche Technique.pdf");
			assertNotNull(documentNodeRef);

			ContentReader reader = contentService.getReader(documentNodeRef, ContentModel.PROP_CONTENT);
			assertNotNull("Reader should not be null", reader);
			InputStream in = reader.getContentInputStream();
			assertNotNull("Input stream should not be null", in);

			OutputStream out = new FileOutputStream(new File("/tmp/becpg_product_report.pdf"));

			IOUtils.copy(in, out);

			in.close();
			out.close();

			/*-- Product template --*/
			Map<QName, Serializable> properties;
			properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, "Product Tpl");
			nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					BeCPGModel.TYPE_ENTITY_V2, properties).getChildRef();

			entityReportService.generateReports(rawMaterialNodeRef);

			return null;

		}, false, true);
	}

	/**
	 * Test initialize product folder.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testInitializeProductFolder() throws Exception {

		logger.debug("testInitializeProductFolder");

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			NodeRef entityTplNodeRef = entityTplService.getEntityTpl(PLMModel.TYPE_FINISHEDPRODUCT);
			NodeRef imagesFolder = nodeService.getChildByName(entityTplNodeRef, ContentModel.ASSOC_CONTAINS,
					TranslateHelper.getTranslatedPath(RepoConsts.PATH_IMAGES));
			assertNotNull(imagesFolder);
			addProductImage(imagesFolder, PLMModel.TYPE_FINISHEDPRODUCT);

			// add permissions on image folder Tpl
			if (nodeService.hasAspect(imagesFolder, BeCPGModel.ASPECT_PERMISSIONS_TPL)) {
				Set<String> zones = new HashSet<>();
				String collaboratorGroupName = "Collaborator_Test";
				if (!authorityService.authorityExists(PermissionService.GROUP_PREFIX + collaboratorGroupName)) {
					zones.add(AuthorityService.ZONE_APP_DEFAULT);
					zones.add(AuthorityService.ZONE_APP_SHARE);
					zones.add(AuthorityService.ZONE_AUTH_ALFRESCO);
					authorityService.createAuthority(AuthorityType.GROUP, collaboratorGroupName, collaboratorGroupName, zones);
				}
				NodeRef groupNodeRef = authorityDAO.getAuthorityNodeRefOrNull(PermissionService.GROUP_PREFIX + collaboratorGroupName);
				logger.debug("imagesFolder: " + imagesFolder);
				logger.debug("groupNodeRef: " + groupNodeRef);
				nodeService.addAspect(imagesFolder, BeCPGModel.ASPECT_PERMISSIONS_TPL, null);
				logger.debug("aspects: " + nodeService.getAspects(imagesFolder));
				logger.info("imagesFolder" + imagesFolder);
				logger.info("groupNodeRef" + groupNodeRef);
				nodeService.createAssociation(imagesFolder, groupNodeRef, BeCPGModel.ASSOC_PERMISSIONS_TPL_COLLABORATOR_GROUPS);
			}

			return null;

		}, false, true);

		/*-- Create raw material --*/
		NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			logger.debug("/*-- Create raw material --*/");
			RawMaterialData rawMaterial = new RawMaterialData();
			rawMaterial.setName("Raw material");
			NodeRef rawMaterialNodeRef1 = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial).getNodeRef();
			// productService.initializeProductFolder(rawMaterialNodeRef);

			return rawMaterialNodeRef1;

		}, false, true);

		// Check
		logger.debug("//Check raw material");
		NodeRef parentRawMaterialNodeRef = nodeService.getPrimaryParent(rawMaterialNodeRef).getParentRef();
		assertEquals("Parent of raw material must be the getTestFolderNodeRef()", getTestFolderNodeRef(), parentRawMaterialNodeRef);
		assertEquals("Parent of raw material must have the type FOLDER", ContentModel.TYPE_FOLDER, nodeService.getType(parentRawMaterialNodeRef));

		/*-- Create finished product --*/
		NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Finished Product");
			NodeRef finishedProductNodeRef1 = alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
			// productService.initializeProductFolder(finishedProductNodeRef);

			return finishedProductNodeRef1;

		}, false, true);

		// Check
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			logger.debug("//Check finished product");
			NodeRef imagesFolder = nodeService.getChildByName(finishedProductNodeRef, ContentModel.ASSOC_CONTAINS,
					TranslateHelper.getTranslatedPath(RepoConsts.PATH_IMAGES));
			assertNotNull(imagesFolder);

			assertNotNull("Image product must be not null", entityService.getEntityDefaultImage(finishedProductNodeRef));
			return null;

		}, false, true);
	}

	/**
	 * Adds the product image.
	 *
	 * @param parentNodeRef
	 *            the parent node ref
	 * @param typeFinishedproduct
	 * @throws IOException
	 */
	@Deprecated
	// Use writeImages of entityService instead
	private void addProductImage(NodeRef parentNodeRef, QName typeQName) throws IOException {
		/*-- add product image--*/
		logger.debug("/*-- add product image--*/");
		String imageName = entityService.getDefaultImageName(typeQName);
		NodeRef imageNodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, imageName);

		if (imageNodeRef == null) {
			logger.debug("image name: " + imageName);
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, imageName);
			imageNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					ContentModel.TYPE_CONTENT, properties).getChildRef();

			ContentWriter writer = contentService.getWriter(imageNodeRef, ContentModel.PROP_CONTENT, true);

			ClassPathResource img = new ClassPathResource("beCPG/birt/productImage.jpg");

			String mimetype = mimetypeService.guessMimetype(img.getFilename());
			ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
			Charset charset = charsetFinder.getCharset(img.getInputStream(), mimetype);
			String encoding = charset.name();

			logger.debug("mimetype : " + mimetype);
			logger.debug("encoding : " + encoding);
			writer.setMimetype(mimetype);
			writer.setEncoding(encoding);
			writer.putContent(img.getInputStream());
		}
	}

	/**
	 * Test classify product.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testClassifyProductByHierarchy() throws Exception {

		logger.debug("testClassifyProductByHierarchy");

		final NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Create raw material --*/
			logger.debug("/*-- Create raw material --*/");
			RawMaterialData rawMaterial = new RawMaterialData();
			rawMaterial.setName("Raw material");
			rawMaterial.setHierarchy1(HIERARCHY1_SEA_FOOD_REF);
			rawMaterial.setHierarchy2(HIERARCHY2_FISH_REF);
			rawMaterial.setState(SystemState.Valid);
			return alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial).getNodeRef();

		}, false, true);

		waitForSolr();

		final NodeRef rawMaterial2NodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- classify --*/
			logger.debug("/*-- classify --*/");
			hierarchyService.classifyByHierarchy(repositoryHelper.getCompanyHome(), rawMaterialNodeRef);

			/*-- Check --*/
			List<Path> paths = nodeService.getPaths(rawMaterialNodeRef, true);
			logger.debug("/*-- Check --*/");
			logger.debug("path: " + paths.get(0));
			String displayPath = paths.get(0).toDisplayPath(nodeService, permissionService);
			logger.info("###display path: " + displayPath);
			String[] arrDisplayPaths = displayPath.split(RepoConsts.PATH_SEPARATOR);
			assertEquals("1st Path should be ''", "", arrDisplayPaths[0]);
			assertEquals("2nd Path should be 'Espace racine'", "Espace racine", arrDisplayPaths[1]);
			assertEquals("5th Path should be 'Matièrespremière'", "Matière première", arrDisplayPaths[2]);
			assertEquals("6th Path should be 'Frozen'", HIERARCHY1_SEA_FOOD, arrDisplayPaths[3]);
			assertEquals("7th Path should be 'Pizza'", HIERARCHY2_FISH, arrDisplayPaths[4]);
			assertEquals("check name", "Raw material", nodeService.getProperty(rawMaterialNodeRef, ContentModel.PROP_NAME));

			/*-- classify twice --*/
			logger.debug("/*-- classify twice --*/");
			hierarchyService.classifyByHierarchy(repositoryHelper.getCompanyHome(), rawMaterialNodeRef);

			/*-- Check --*/
			paths = nodeService.getPaths(rawMaterialNodeRef, true);
			logger.debug("/*-- Check --*/");
			logger.debug("path: " + paths.get(0));
			displayPath = paths.get(0).toDisplayPath(nodeService, permissionService);
			logger.debug("display path: " + displayPath);
			arrDisplayPaths = displayPath.split(RepoConsts.PATH_SEPARATOR);
			assertEquals("1st Path should be ''", "", arrDisplayPaths[0]);
			assertEquals("2nd Path should be 'Espace racine'", "Espace racine", arrDisplayPaths[1]);
			assertEquals("5th Path should be 'Matière première'", "Matière première", arrDisplayPaths[2]);
			assertEquals("6th Path should be 'Frozen'", HIERARCHY1_SEA_FOOD, arrDisplayPaths[3]);
			assertEquals("7th Path should be 'Pizza'", HIERARCHY2_FISH, arrDisplayPaths[4]);
			assertEquals("check name", "Raw material", nodeService.getProperty(rawMaterialNodeRef, ContentModel.PROP_NAME));

			/*-- Create raw material 2 --*/
			// logger.debug("/*-- Create raw material --*/");
			RawMaterialData rawMaterial2 = new RawMaterialData();
			rawMaterial2.setName("Raw material");
			rawMaterial2.setHierarchy1(HIERARCHY1_SEA_FOOD_REF);
			rawMaterial2.setHierarchy2(HIERARCHY2_FISH_REF);
			rawMaterial2.setState(SystemState.Valid);
			return alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial2).getNodeRef();

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- classify --*/
			logger.debug("/*-- classify --*/");
			hierarchyService.classifyByHierarchy(repositoryHelper.getCompanyHome(), rawMaterial2NodeRef);

			/*-- Check --*/
			List<Path> paths = nodeService.getPaths(rawMaterial2NodeRef, true);
			logger.debug("/*-- Check --*/");
			logger.debug("path: " + paths.get(0));
			String displayPath = paths.get(0).toDisplayPath(nodeService, permissionService);
			logger.debug("display path: " + displayPath);
			String[] arrDisplayPaths = displayPath.split(RepoConsts.PATH_SEPARATOR);
			assertEquals("1st Path should be ''", "", arrDisplayPaths[0]);
			assertEquals("2nd Path should be 'Espace racine'", "Espace racine", arrDisplayPaths[1]);
			assertEquals("5th Path should be 'Matière première'", "Matière première", arrDisplayPaths[2]);
			assertEquals("6th Path should be 'Frozen'", HIERARCHY1_SEA_FOOD, arrDisplayPaths[3]);
			assertEquals("7th Path should be 'Pizza'", HIERARCHY2_FISH, arrDisplayPaths[4]);

			return null;

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			// clean
			nodeService.deleteNode(rawMaterialNodeRef);
			nodeService.deleteNode(rawMaterial2NodeRef);

			return null;

		}, false, true);
	}

	/**
	 * Test get WUsed of the compoList
	 */
	@Test
	public void testGetWUsedCompoList() {

		logger.debug("testGetWUsedProduct");

		final NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Create raw material --*/
			logger.debug("/*-- Create raw material --*/");
			RawMaterialData rawMaterial = new RawMaterialData();
			rawMaterial.setName("Raw material");
			return alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial).getNodeRef();

		}, false, true);

		final NodeRef lSF1NodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			LocalSemiFinishedProductData lSF1 = new LocalSemiFinishedProductData();
			lSF1.setName("Local semi finished 1");
			return alfrescoRepository.create(getTestFolderNodeRef(), lSF1).getNodeRef();

		}, false, true);

		final NodeRef lSF2NodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			LocalSemiFinishedProductData lSF2 = new LocalSemiFinishedProductData();
			lSF2.setName("Local semi finished 2");
			return alfrescoRepository.create(getTestFolderNodeRef(), lSF2).getNodeRef();

		}, false, true);

		final NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Create finished product --*/
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Finished Product");
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(new CompoListDataItem(null, null, 1d, 1d, ProductUnit.P, 0d, DeclarationType.Declare, lSF1NodeRef));
			compoList.add(new CompoListDataItem(null, compoList.get(0), 1d, 4d, ProductUnit.P, 0d, DeclarationType.Declare, lSF2NodeRef));
			compoList.add(new CompoListDataItem(null, compoList.get(1), 3d, 0d, ProductUnit.kg, 0d, DeclarationType.Omit, rawMaterialNodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			logger.debug("local semi finished 1: " + lSF1NodeRef);
			logger.debug("local semi finished 2: " + lSF2NodeRef);
			logger.debug("finishedProductNodeRef: " + finishedProductNodeRef);
			List<CompoListDataItem> wUsedProducts = getWUsedCompoList(rawMaterialNodeRef);

			for (CompoListDataItem wUsedProduct : wUsedProducts) {
				logger.debug(String.format("wUsedProduct.getProduct(): %s - level: %d - qty: %e - unit: %s", wUsedProduct.getProduct(),
						wUsedProduct.getDepthLevel(), wUsedProduct.getQty(), wUsedProduct.getCompoListUnit()));
			}

			assertEquals("MP should have 1 where Useds", 1, wUsedProducts.size());
			CompoListDataItem wUsed0 = wUsedProducts.get(0);

			assertEquals("check PF", finishedProductNodeRef, wUsed0.getProduct());
			assertEquals("check PF level", (Integer) 1, wUsed0.getDepthLevel());
			assertEquals("check PF qty", 3d, wUsed0.getQty());
			assertEquals("check PF qty sub formula", 0d, wUsed0.getQtySubFormula());
			assertEquals("check PF unit", ProductUnit.kg, wUsed0.getCompoListUnit());
			assertEquals("check PF declaration", DeclarationType.Omit, wUsed0.getDeclType());
			logger.debug("end");

			return null;

		}, false, true);
	}

	private List<CompoListDataItem> getWUsedCompoList(NodeRef productNodeRef) {

		logger.debug("getWUsedProduct");

		List<CompoListDataItem> wUsedList = new ArrayList<>();
		MultiLevelListData wUsedData = wUsedListService.getWUsedEntity(productNodeRef, PLMModel.ASSOC_COMPOLIST_PRODUCT, WUSED_LEVEL);

		for (Map.Entry<NodeRef, MultiLevelListData> kv : wUsedData.getTree().entrySet()) {

			Map<QName, Serializable> properties = nodeService.getProperties(kv.getKey());

			ProductUnit compoListUnit = ProductUnit.valueOf((String) properties.get(PLMModel.PROP_COMPOLIST_UNIT));
			DeclarationType declType = DeclarationType.valueOf((String) properties.get(PLMModel.PROP_COMPOLIST_DECL_TYPE));

			CompoListDataItem compoListDataItem = new CompoListDataItem(kv.getKey(), null, (Double) properties.get(PLMModel.PROP_COMPOLIST_QTY),
					(Double) properties.get(PLMModel.PROP_COMPOLIST_QTY_SUB_FORMULA), compoListUnit,
					(Double) properties.get(PLMModel.PROP_COMPOLIST_LOSS_PERC), declType, kv.getValue().getEntityNodeRef());

			wUsedList.add(compoListDataItem);
		}

		logger.debug("wUsedList size" + wUsedList.size());

		return wUsedList;
	}

	/**
	 * Test get WUsed of the packagingList
	 */
	@Test
	public void testGetWUsedPackgingList() {

		logger.debug("testGetWUsedProduct");

		NodeRef packagingMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Create raw material --*/
			logger.debug("/*-- Create pkg material --*/");
			PackagingMaterialData packagingMaterial = new PackagingMaterialData();
			packagingMaterial.setName("Packaging material");
			return alfrescoRepository.create(getTestFolderNodeRef(), packagingMaterial).getNodeRef();

		}, false, true);

		NodeRef finishedProductNodeRef1 = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			/*-- Create finished product --*/
			logger.debug("/*-- Create finished product --*/");

			FinishedProductData finishedProduct1 = new FinishedProductData();
			finishedProduct1.setName("Finished Product 1");
			List<PackagingListDataItem> packagingList1 = new ArrayList<>();
			packagingList1.add(PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.P).withPkgLevel(PackagingLevel.Primary).withIsMaster(true).withProduct(packagingMaterialNodeRef)
);
			finishedProduct1.getPackagingListView().setPackagingList(packagingList1);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct1).getNodeRef();
		}, false, true);

		NodeRef finishedProductNodeRef2 = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			FinishedProductData finishedProduct2 = new FinishedProductData();
			finishedProduct2.setName("Finished Product");
			List<PackagingListDataItem> packagingList2 = new ArrayList<>();
			packagingList2.add(PackagingListDataItem.build().withQty(8d).withUnit(ProductUnit.PP).withPkgLevel(PackagingLevel.Secondary).withIsMaster(true).withProduct(packagingMaterialNodeRef)
);
			finishedProduct2.getPackagingListView().setPackagingList(packagingList2);
			alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct2).getNodeRef();

			return null;

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			List<PackagingListDataItem> wUsedProducts = getWUsedPackagingList(packagingMaterialNodeRef);

			assertEquals("MP should have 2 where Useds", 2, wUsedProducts.size());

			for (PackagingListDataItem packagingListDataItem : wUsedProducts) {

				if (packagingListDataItem.getProduct().equals(finishedProductNodeRef1)) {
					assertEquals(1d, packagingListDataItem.getQty());
					assertEquals(ProductUnit.P, packagingListDataItem.getPackagingListUnit());
					assertEquals(PackagingLevel.Primary, packagingListDataItem.getPkgLevel());
				} else if (packagingListDataItem.getProduct().equals(finishedProductNodeRef2)) {
					assertEquals(8d, packagingListDataItem.getQty());
					assertEquals(ProductUnit.PP, packagingListDataItem.getPackagingListUnit());
					assertEquals(PackagingLevel.Secondary, packagingListDataItem.getPkgLevel());
				}
			}

			return null;

		}, false, true);
	}

	private List<PackagingListDataItem> getWUsedPackagingList(NodeRef productNodeRef) {

		logger.debug("getWUsedProduct");

		List<PackagingListDataItem> wUsedList = new ArrayList<>();
		MultiLevelListData wUsedData = wUsedListService.getWUsedEntity(productNodeRef, PLMModel.ASSOC_PACKAGINGLIST_PRODUCT, WUSED_LEVEL);

		for (Map.Entry<NodeRef, MultiLevelListData> kv : wUsedData.getTree().entrySet()) {

			Map<QName, Serializable> properties = nodeService.getProperties(kv.getKey());
			ProductUnit packagingListUnit = ProductUnit.valueOf((String) properties.get(PLMModel.PROP_PACKAGINGLIST_UNIT));
			PackagingLevel packagingLevel = PackagingLevel.valueOf((String) properties.get(PLMModel.PROP_PACKAGINGLIST_PKG_LEVEL));

			PackagingListDataItem packagingListDataItem = PackagingListDataItem.build().withQty((Double) properties.get(PLMModel.PROP_PACKAGINGLIST_QTY)).withUnit(packagingListUnit).withPkgLevel(packagingLevel).withIsMaster(true).withProduct(kv.getValue()
.getEntityNodeRef());
			
			packagingListDataItem.setNodeRef(kv.getKey());

			wUsedList.add(packagingListDataItem);
		}

		logger.debug("wUsedList size" + wUsedList.size());

		return wUsedList;
	}
}
