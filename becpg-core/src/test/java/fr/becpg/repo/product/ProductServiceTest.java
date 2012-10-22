/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.product;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.repo.security.authority.AuthorityDAO;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.entity.datalist.WUsedListService;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProduct;
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.DeclarationType;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListUnit;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;
import fr.becpg.report.client.ReportFormat;
import fr.becpg.test.RepoBaseTestCase;

// TODO: Auto-generated Javadoc
/**
 * The Class ProductServiceTest.
 * 
 * @author querephi
 */
public class ProductServiceTest extends RepoBaseTestCase {

	private static final int WUSED_LEVEL = 1;

	/** The logger. */
	private static Log logger = LogFactory.getLog(ProductServiceTest.class);

	@Resource
	private ProductService productService;

	@Resource
	private PermissionService permissionService;

	@Resource
	private AuthorityService authorityService;

	@Resource
	private AuthorityDAO authorityDAO;

	@Resource
	private EntityReportService entityReportService;

	@Resource
	private ReportTplService reportTplService;

	@Resource
	private EntityTplService entityTplService;

	@Resource
	private DictionaryService dictionaryService;
	
	@Resource
	private WUsedListService wUsedListService;


	
	/**
	 * Test create product.
	 */
	@Test
	public void testCreateProduct() {

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				createRawMaterial(testFolderNodeRef, "MP test report");

				return null;

			}
		}, false, true);

	}

	/**
	 * Test report product.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testReportProduct() throws Exception {

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				/*-- Add report template --*/
				NodeRef systemFolder = repoService.createFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_SYSTEM,
						TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
				NodeRef reportsFolder = repoService.createFolderByPath(systemFolder, RepoConsts.PATH_REPORTS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS));
				NodeRef productReportTplsFolder = repoService.createFolderByPath(reportsFolder, RepoConsts.PATH_PRODUCT_REPORTTEMPLATES,
						TranslateHelper.getTranslatedPath(RepoConsts.PATH_PRODUCT_REPORTTEMPLATES));

				QName productType = BeCPGModel.TYPE_RAWMATERIAL;
				ClassDefinition classDef = dictionaryService.getClass(productType);
				NodeRef productReportTplFolder = repoService.createFolderByPath(productReportTplsFolder, classDef.getTitle(), classDef.getTitle());
				reportTplService.createTplRptDesign(productReportTplFolder, classDef.getTitle(), "beCPG/birt/document/product/default/ProductReport.rptdesign",
						ReportType.Document, ReportFormat.PDF, productType, true, true, true);

				/*-- Create images folder --*/
				NodeRef imagesNodeRef = fileFolderService.create(testFolderNodeRef, TranslateHelper.getTranslatedPath(RepoConsts.PATH_IMAGES), ContentModel.TYPE_FOLDER).getNodeRef();

				addProductImage(imagesNodeRef);

				/*-- Create product --*/
				logger.debug("Create product");
				NodeRef rawMaterialNodeRef = createRawMaterial(testFolderNodeRef, "MP test report");

				/*-- Generate report --*/
				entityReportService.generateReport(rawMaterialNodeRef);

				/*-- Check report --*/
				logger.debug("/*-- Check report --*/");
				ContentReader reader = contentService.getReader(rawMaterialNodeRef, ContentModel.PROP_CONTENT);
				assertNotNull("Reader should not be null", reader);
				InputStream in = reader.getContentInputStream();
				assertNotNull("Input stream should not be null", in);

				OutputStream out = new FileOutputStream(new File("/tmp/becpg_product_report.pdf"));
				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				out.close();

				/*-- Product template --*/
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
				properties = new HashMap<QName, Serializable>();
				properties.put(ContentModel.PROP_NAME, "Product Tpl");
				nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ENTITY, properties)
						.getChildRef();

				entityReportService.generateReport(rawMaterialNodeRef);

				return null;

			}
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

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				/*-- create folders : Test, system, product templates--*/
				
				NodeRef systemFolder = repoService.createFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_SYSTEM,
						TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));

				// clear folderTpls
				NodeRef folderTplsFolder = nodeService.getChildByName(systemFolder, ContentModel.ASSOC_CONTAINS,
						TranslateHelper.getTranslatedPath(RepoConsts.PATH_FOLDER_TEMPLATES));
				if (folderTplsFolder != null) {
					nodeService.deleteNode(folderTplsFolder);
				}
				folderTplsFolder = repoService.createFolderByPath(systemFolder, RepoConsts.PATH_FOLDER_TEMPLATES,
						TranslateHelper.getTranslatedPath(RepoConsts.PATH_FOLDER_TEMPLATES));

				// clear entityTpls
				NodeRef entityTplsFolder = nodeService.getChildByName(systemFolder, ContentModel.ASSOC_CONTAINS,
						TranslateHelper.getTranslatedPath(RepoConsts.PATH_ENTITY_TEMPLATES));
				if (entityTplsFolder != null) {
					nodeService.deleteNode(entityTplsFolder);
				}
				entityTplsFolder = repoService.createFolderByPath(systemFolder, RepoConsts.PATH_ENTITY_TEMPLATES,
						TranslateHelper.getTranslatedPath(RepoConsts.PATH_ENTITY_TEMPLATES));

				/*-- Create raw material Tpl --*/
				logger.debug("/*-- Create raw material Tpl --*/");
				entityTplService.createEntityTpl(entityTplsFolder, BeCPGModel.TYPE_RAWMATERIAL, true, null);

				/*-- Create finished product Tpl with product folder and product image --*/
				logger.debug("/*-- Create finished product Tpl --*/");

				NodeRef folderTplFolder = entityTplService.createFolderTpl(folderTplsFolder, BeCPGModel.TYPE_FINISHEDPRODUCT, true, null);

				entityTplService.createEntityTpl(entityTplsFolder, BeCPGModel.TYPE_FINISHEDPRODUCT, true, null);

				NodeRef imagesFolder = fileFolderService.create(folderTplFolder, TranslateHelper.getTranslatedPath(RepoConsts.PATH_IMAGES), ContentModel.TYPE_FOLDER).getNodeRef();
				addProductImage(imagesFolder);

				// add permissions on image folder Tpl
				Set<String> zones = new HashSet<String>();
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
				nodeService.createAssociation(imagesFolder, groupNodeRef, BeCPGModel.ASSOC_PERMISSIONS_TPL_COLLABORATOR_GROUPS);

				return null;

			}
		}, false, true);

		/*-- Create raw material --*/
		NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				logger.debug("/*-- Create raw material --*/");
				RawMaterialData rawMaterial = new RawMaterialData();
				rawMaterial.setName("Raw material");
				NodeRef rawMaterialNodeRef = productDAO.create(testFolderNodeRef, rawMaterial, null);
				// productService.initializeProductFolder(rawMaterialNodeRef);

				return rawMaterialNodeRef;

			}
		}, false, true);

		// Check
		logger.debug("//Check raw material");
		NodeRef parentRawMaterialNodeRef = nodeService.getPrimaryParent(rawMaterialNodeRef).getParentRef();
		assertEquals("Parent of raw material must be the testFolderNodeRef", testFolderNodeRef, parentRawMaterialNodeRef);
		assertEquals("Parent of raw material must have the type FOLDER", ContentModel.TYPE_FOLDER, nodeService.getType(parentRawMaterialNodeRef));

		/*-- Create finished product --*/
		NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				logger.debug("/*-- Create finished product --*/");
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Finished Product");
				NodeRef finishedProductNodeRef = productDAO.create(testFolderNodeRef, finishedProduct, null);
				// productService.initializeProductFolder(finishedProductNodeRef);

				return finishedProductNodeRef;

			}
		}, false, true);

		// Check
		logger.debug("//Check finished product");
		NodeRef parentFinishedProductNodeRef = nodeService.getPrimaryParent(finishedProductNodeRef).getParentRef();
		assertNotSame("Parent of finished product should not be the testFolderNodeRef", testFolderNodeRef, parentRawMaterialNodeRef);
		assertEquals("Parent of finished product must have the type PRODUCT_FOLDER", BeCPGModel.TYPE_ENTITY_FOLDER, nodeService.getType(parentFinishedProductNodeRef));
		NodeRef imagesFolder = nodeService.getChildByName(parentFinishedProductNodeRef, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_IMAGES));
		assertNotNull("Images folder must be not null", imagesFolder);
		String imageName = I18NUtil.getMessage(RepoConsts.PATH_PRODUCT_IMAGE) + ".jpg";
		NodeRef imageProductNodeRef = nodeService.getChildByName(imagesFolder, ContentModel.ASSOC_CONTAINS, imageName);
		assertNotNull("Image product must be not null", imageProductNodeRef);
	}

	/**
	 * Adds the product image.
	 * 
	 * @param parentNodeRef
	 *            the parent node ref
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	private void addProductImage(NodeRef parentNodeRef) throws FileNotFoundException {
		/*-- add product image--*/
		logger.debug("/*-- add product image--*/");
		String imageName = I18NUtil.getMessage(RepoConsts.PATH_PRODUCT_IMAGE) + ".jpg";
		logger.debug("image name: " + imageName);
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, imageName);
		NodeRef imageNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), ContentModel.TYPE_CONTENT, properties).getChildRef();

		ContentWriter writer = contentService.getWriter(imageNodeRef, ContentModel.PROP_CONTENT, true);
		String imageFullPath = System.getProperty("user.dir") + "/src/test/resources/beCPG/birt/productImage.jpg";
		logger.debug("Load image file " + imageFullPath);
		FileInputStream imageStream = new FileInputStream(imageFullPath);
		logger.debug("image file loaded " + imageStream);

		String mimetype = mimetypeService.guessMimetype(imageFullPath);
		ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
		Charset charset = charsetFinder.getCharset(imageStream, mimetype);
		String encoding = charset.name();

		logger.debug("mimetype : " + mimetype);
		logger.debug("encoding : " + encoding);
		writer.setMimetype(mimetype);
		writer.setEncoding(encoding);
		writer.putContent(imageStream);
	}

	/**
	 * Test classify product.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testClassifyProduct() throws Exception {

		logger.debug("testClassifyProduct");

		final NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				/*-- Clean --*/
				NodeRef productsNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS,
						TranslateHelper.getTranslatedPath(RepoConsts.PATH_PRODUCTS));

				if (productsNodeRef != null) {
					nodeService.deleteNode(productsNodeRef);
				}

				/*-- Create raw material --*/
				logger.debug("/*-- Create raw material --*/");
				RawMaterialData rawMaterial = new RawMaterialData();
				rawMaterial.setName("Raw material");
				rawMaterial.setHierarchy1(HIERARCHY1_FROZEN_REF);
				rawMaterial.setHierarchy2(HIERARCHY2_PIZZA_REF);
				rawMaterial.setState(SystemState.Valid);
				return productDAO.create(testFolderNodeRef, rawMaterial, null);
				
			}
		}, false, true);
		
		final NodeRef rawMaterial2NodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {


				/*-- classify --*/
				logger.debug("/*-- classify --*/");
				productService.classifyProduct(repositoryHelper.getCompanyHome(), rawMaterialNodeRef);

				/*-- Check --*/
				List<Path> paths = nodeService.getPaths(rawMaterialNodeRef, true);
				logger.debug("/*-- Check --*/");
				logger.debug("path: " + paths.get(0));
				String displayPath = paths.get(0).toDisplayPath(nodeService, permissionService);
				logger.debug("display path: " + displayPath);
				String[] arrDisplayPaths = displayPath.split(RepoConsts.PATH_SEPARATOR);
				assertEquals("1st Path should be ''", "", arrDisplayPaths[0]);
				assertEquals("2nd Path should be 'Espace racine'", "Espace racine", arrDisplayPaths[1]);
				assertEquals("3rd Path should be 'Produits'", "Produits", arrDisplayPaths[2]);
				assertEquals("4th Path should be 'Validés'", "Validés", arrDisplayPaths[3]);
				assertEquals("5th Path should be 'Matières premières'", "Matières premières", arrDisplayPaths[4]);
				assertEquals("6th Path should be 'Frozen'", HIERARCHY1_FROZEN, arrDisplayPaths[5]);
				assertEquals("7th Path should be 'Pizza'", HIERARCHY2_PIZZA, arrDisplayPaths[6]);
				assertEquals("check name", "Raw material", nodeService.getProperty(rawMaterialNodeRef, ContentModel.PROP_NAME));

				/*-- classify twice --*/
				logger.debug("/*-- classify twice --*/");
				productService.classifyProduct(repositoryHelper.getCompanyHome(), rawMaterialNodeRef);

				/*-- Check --*/
				paths = nodeService.getPaths(rawMaterialNodeRef, true);
				logger.debug("/*-- Check --*/");
				logger.debug("path: " + paths.get(0));
				displayPath = paths.get(0).toDisplayPath(nodeService, permissionService);
				logger.debug("display path: " + displayPath);
				arrDisplayPaths = displayPath.split(RepoConsts.PATH_SEPARATOR);
				assertEquals("1st Path should be ''", "", arrDisplayPaths[0]);
				assertEquals("2nd Path should be 'Espace racine'", "Espace racine", arrDisplayPaths[1]);
				assertEquals("3rd Path should be 'Produits'", "Produits", arrDisplayPaths[2]);
				assertEquals("4th Path should be 'Validés'", "Validés", arrDisplayPaths[3]);
				assertEquals("5th Path should be 'Matières premières'", "Matières premières", arrDisplayPaths[4]);
				assertEquals("6th Path should be 'Frozen'", HIERARCHY1_FROZEN, arrDisplayPaths[5]);
				assertEquals("7th Path should be 'Pizza'", HIERARCHY2_PIZZA, arrDisplayPaths[6]);
				assertEquals("check name", "Raw material", nodeService.getProperty(rawMaterialNodeRef, ContentModel.PROP_NAME));

				/*-- Create raw material 2 --*/
				// logger.debug("/*-- Create raw material --*/");
				RawMaterialData rawMaterial2 = new RawMaterialData();
				rawMaterial2.setName("Raw material");
				rawMaterial2.setHierarchy1(HIERARCHY1_FROZEN_REF);
				rawMaterial2.setHierarchy2(HIERARCHY2_PIZZA_REF);
				rawMaterial2.setState(SystemState.Valid);
				return productDAO.create(testFolderNodeRef, rawMaterial2, null);
				
			}
		}, false, true);
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {


				/*-- classify --*/
				logger.debug("/*-- classify --*/");
				productService.classifyProduct(repositoryHelper.getCompanyHome(), rawMaterial2NodeRef);

				/*-- Check --*/
				List<Path> paths = nodeService.getPaths(rawMaterial2NodeRef, true);
				logger.debug("/*-- Check --*/");
				logger.debug("path: " + paths.get(0));
				String displayPath = paths.get(0).toDisplayPath(nodeService, permissionService);
				logger.debug("display path: " + displayPath);
				String []arrDisplayPaths = displayPath.split(RepoConsts.PATH_SEPARATOR);
				assertEquals("1st Path should be ''", "", arrDisplayPaths[0]);
				assertEquals("2nd Path should be 'Espace racine'", "Espace racine", arrDisplayPaths[1]);
				assertEquals("3rd Path should be 'Produits'", "Produits", arrDisplayPaths[2]);
				assertEquals("4th Path should be 'Validés'", "Validés", arrDisplayPaths[3]);
				assertEquals("5th Path should be 'Matières premières'", "Matières premières", arrDisplayPaths[4]);
				assertEquals("6th Path should be 'Frozen'", HIERARCHY1_FROZEN, arrDisplayPaths[5]);
				assertEquals("7th Path should be 'Pizza'", HIERARCHY2_PIZZA, arrDisplayPaths[6]);
				assertEquals("8th Path should be 'Raw material (1)'", "Raw material (1)", arrDisplayPaths[7]);

				return null;

			}
		}, false, true);
	}
	
	

	/**
	 * Test get WUsed of the compoList
	 */
	@Test
	public void testGetWUsedCompoList() {

		logger.debug("testGetWUsedProduct");

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				/*-- Create raw material --*/
				logger.debug("/*-- Create raw material --*/");
				RawMaterialData rawMaterial = new RawMaterialData();
				rawMaterial.setName("Raw material");
				NodeRef rawMaterialNodeRef = productDAO.create(testFolderNodeRef, rawMaterial, null);
				LocalSemiFinishedProduct lSF1 = new LocalSemiFinishedProduct();
				lSF1.setName("Local semi finished 1");
				NodeRef lSF1NodeRef = productDAO.create(testFolderNodeRef, lSF1, null);

				LocalSemiFinishedProduct lSF2 = new LocalSemiFinishedProduct();
				lSF2.setName("Local semi finished 2");
				NodeRef lSF2NodeRef = productDAO.create(testFolderNodeRef, lSF2, null);

				/*-- Create finished product --*/
				logger.debug("/*-- Create finished product --*/");
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Finished Product");
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, 1, 1d, 1d, 0d, CompoListUnit.P, 0d, null, DeclarationType.Declare, lSF1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 1d, 4d, 0d, CompoListUnit.P, 0d, null, DeclarationType.Declare, lSF2NodeRef));
				compoList.add(new CompoListDataItem(null, 3, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Omit, rawMaterialNodeRef));
				finishedProduct.setCompoList(compoList);
				Collection<QName> dataLists = new ArrayList<QName>();
				dataLists.add(BeCPGModel.TYPE_COMPOLIST);
				NodeRef finishedProductNodeRef = productDAO.create(testFolderNodeRef, finishedProduct, dataLists);

				logger.debug("local semi finished 1: " + lSF1NodeRef);
				logger.debug("local semi finished 2: " + lSF2NodeRef);
				logger.debug("finishedProductNodeRef: " + finishedProductNodeRef);
				List<CompoListDataItem> wUsedProducts = getWUsedCompoList(rawMaterialNodeRef);

				for (CompoListDataItem wUsedProduct : wUsedProducts) {
					logger.debug(String.format("wUsedProduct.getProduct(): %s - level: %d - qty: %e - unit: %s", wUsedProduct.getProduct(), wUsedProduct.getDepthLevel(),
							wUsedProduct.getQty(), wUsedProduct.getCompoListUnit()));
				}

				assertEquals("MP should have 1 where Useds", 1, wUsedProducts.size());
				CompoListDataItem wUsed0 = wUsedProducts.get(0);

				assertEquals("check PF", finishedProductNodeRef, wUsed0.getProduct());
				assertEquals("check PF level", new Integer(1), wUsed0.getDepthLevel());
				assertEquals("check PF qty", 3d, wUsed0.getQty());
				assertEquals("check PF qty sub formula", 0d, wUsed0.getQtySubFormula());
				assertEquals("check PF unit", CompoListUnit.kg, wUsed0.getCompoListUnit());
				assertEquals("check PF declaration", DeclarationType.Omit, wUsed0.getDeclType());
				logger.debug("end");

				return null;

			}
		}, false, true);
	}
	
	private List<CompoListDataItem> getWUsedCompoList(NodeRef productNodeRef) {
		
		logger.debug("getWUsedProduct");
		
		List<CompoListDataItem> wUsedList = new ArrayList<CompoListDataItem>();		
		MultiLevelListData wUsedData = wUsedListService.getWUsedEntity(productNodeRef, BeCPGModel.ASSOC_COMPOLIST_PRODUCT, WUSED_LEVEL);
		
		for(Map.Entry<NodeRef, MultiLevelListData> kv : wUsedData.getTree().entrySet()){
			
			Map<QName, Serializable> properties = nodeService.getProperties(kv.getKey());

			CompoListUnit compoListUnit = CompoListUnit.valueOf((String)properties.get(BeCPGModel.PROP_COMPOLIST_UNIT));
			DeclarationType declType = DeclarationType.valueOf((String)properties.get(BeCPGModel.PROP_COMPOLIST_DECL_TYPE));
			
			CompoListDataItem compoListDataItem = new CompoListDataItem(kv.getKey(), WUSED_LEVEL, 
										(Double)properties.get(BeCPGModel.PROP_COMPOLIST_QTY), 
										(Double)properties.get(BeCPGModel.PROP_COMPOLIST_QTY_SUB_FORMULA), 
										(Double)properties.get(BeCPGModel.PROP_COMPOLIST_QTY_AFTER_PROCESS), 
										compoListUnit, 
										(Double)properties.get(BeCPGModel.PROP_COMPOLIST_LOSS_PERC), 
										null, 
										declType, 
										kv.getValue().getEntityNodeRef());
			
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

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				/*-- Create raw material --*/
				logger.debug("/*-- Create pkg material --*/");
				PackagingMaterialData packagingMaterial = new PackagingMaterialData();
				packagingMaterial.setName("Packaging material");
				NodeRef packagingMaterialNodeRef = productDAO.create(testFolderNodeRef, packagingMaterial, null);

				/*-- Create finished product --*/
				logger.debug("/*-- Create finished product --*/");
				Collection<QName> dataLists = new ArrayList<QName>();
				dataLists.add(BeCPGModel.TYPE_PACKAGINGLIST);

				FinishedProductData finishedProduct1 = new FinishedProductData();
				finishedProduct1.setName("Finished Product 1");
				List<PackagingListDataItem> packagingList1 = new ArrayList<PackagingListDataItem>();
				packagingList1.add(new PackagingListDataItem(null, 1d, PackagingListUnit.P, "Primaire", true, packagingMaterialNodeRef));
				finishedProduct1.setPackagingList(packagingList1);
				NodeRef finishedProductNodeRef1 = productDAO.create(testFolderNodeRef, finishedProduct1, dataLists);

				FinishedProductData finishedProduct2 = new FinishedProductData();
				finishedProduct2.setName("Finished Product");
				List<PackagingListDataItem> packagingList2 = new ArrayList<PackagingListDataItem>();
				packagingList2.add(new PackagingListDataItem(null, 8d, PackagingListUnit.PP, "Secondaire", true, packagingMaterialNodeRef));
				finishedProduct2.setPackagingList(packagingList2);
				NodeRef finishedProductNodeRef2 = productDAO.create(testFolderNodeRef, finishedProduct2, dataLists);

				List<PackagingListDataItem> wUsedProducts = getWUsedPackagingList(packagingMaterialNodeRef);

				assertEquals("MP should have 2 where Useds", 2, wUsedProducts.size());

				for (PackagingListDataItem packagingListDataItem : wUsedProducts) {

					if (packagingListDataItem.getProduct().equals(finishedProductNodeRef1)) {
						assertEquals("check qty", 1d, packagingListDataItem.getQty());
						assertEquals("check qty", PackagingListUnit.P, packagingListDataItem.getPackagingListUnit());
						assertEquals("check qty", "Primaire", packagingListDataItem.getPkgLevel());
					} else if (packagingListDataItem.getProduct().equals(finishedProductNodeRef2)) {
						assertEquals("check qty", 8d, packagingListDataItem.getQty());
						assertEquals("check qty", PackagingListUnit.PP, packagingListDataItem.getPackagingListUnit());
						assertEquals("check qty", "Secondaire", packagingListDataItem.getPkgLevel());
					}
				}

				return null;

			}
		}, false, true);
	}
	
	
	private List<PackagingListDataItem> getWUsedPackagingList(NodeRef productNodeRef) {
		
		logger.debug("getWUsedProduct");
		
		List<PackagingListDataItem> wUsedList = new ArrayList<PackagingListDataItem>();
		MultiLevelListData wUsedData = wUsedListService.getWUsedEntity(productNodeRef, BeCPGModel.ASSOC_PACKAGINGLIST_PRODUCT, WUSED_LEVEL);
		
		for(Map.Entry<NodeRef, MultiLevelListData> kv : wUsedData.getTree().entrySet()){
			
			Map<QName, Serializable> properties = nodeService.getProperties(kv.getKey());			
			PackagingListUnit packagingListUnit = PackagingListUnit.valueOf((String)properties.get(BeCPGModel.PROP_PACKAGINGLIST_UNIT));							
			
			PackagingListDataItem packagingListDataItem = new PackagingListDataItem(kv.getKey(), 									
						(Double)properties.get(BeCPGModel.PROP_PACKAGINGLIST_QTY), 
						packagingListUnit, 
						(String)properties.get(BeCPGModel.PROP_PACKAGINGLIST_PKG_LEVEL), true, 
						kv.getValue().getEntityNodeRef());
			
			wUsedList.add(packagingListDataItem);
		}
		
		logger.debug("wUsedList size" + wUsedList.size());
		
		return wUsedList;
	}
}
