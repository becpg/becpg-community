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
import org.apache.commons.io.IOUtils;
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
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.DeclarationType;
import fr.becpg.repo.product.data.productList.PackagingLevel;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListUnit;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.test.BeCPGTestHelper;
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

	private NodeRef productsFolder = null; 

	/**
	 * Test create product.
	 */
	@Test
	public void testCreateProduct() {

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				BeCPGTestHelper.createRawMaterial(testFolderNodeRef, "MP test report");

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

		final NodeRef rawMaterialNodeRef =	transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				/*-- Create images folder --*/
				NodeRef imagesNodeRef = fileFolderService.create(testFolderNodeRef, TranslateHelper.getTranslatedPath(RepoConsts.PATH_IMAGES), ContentModel.TYPE_FOLDER).getNodeRef();

				addProductImage(imagesNodeRef);

				/*-- Create product --*/
				logger.debug("Create product");
				return BeCPGTestHelper.createRawMaterial(testFolderNodeRef, "MP test report");
				


			}
		}, false, true);
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {


				/*-- Generate report --*/
				entityReportService.generateReport(rawMaterialNodeRef);

				/*-- Check report --*/
				logger.debug("/*-- Check report --*/");
				NodeRef documentsNodeRef = nodeService.getChildByName(rawMaterialNodeRef, ContentModel.ASSOC_CONTAINS, "Documents");
				assertNotNull(documentsNodeRef);								
				NodeRef documentNodeRef = nodeService.getChildByName(documentsNodeRef, ContentModel.ASSOC_CONTAINS, "MP test report - Fiche Technique Client.pdf");
				ContentReader reader = contentService.getReader(documentNodeRef, ContentModel.PROP_CONTENT);
				assertNotNull("Reader should not be null", reader);
				InputStream in = reader.getContentInputStream();
				assertNotNull("Input stream should not be null", in);

				OutputStream out = new FileOutputStream(new File("/tmp/becpg_product_report.pdf"));

				IOUtils.copy( in, out);
				
				in.close();
				out.close();

				/*-- Product template --*/
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
				properties = new HashMap<QName, Serializable>();
				properties.put(ContentModel.PROP_NAME, "Product Tpl");
				nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ENTITY_V2, properties)
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

				NodeRef entityTplNodeRef = entityTplService.getEntityTpl(BeCPGModel.TYPE_FINISHEDPRODUCT);
				NodeRef imagesFolder = nodeService.getChildByName(entityTplNodeRef, ContentModel.ASSOC_CONTAINS,
						TranslateHelper.getTranslatedPath(RepoConsts.PATH_IMAGES));
				assertNotNull(imagesFolder);
				addProductImage(imagesFolder);

				// add permissions on image folder Tpl
				if(nodeService.hasAspect(imagesFolder, BeCPGModel.ASPECT_PERMISSIONS_TPL)){
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
					logger.info("imagesFolder" + imagesFolder);
					logger.info("groupNodeRef" + groupNodeRef);
					nodeService.createAssociation(imagesFolder, groupNodeRef, BeCPGModel.ASSOC_PERMISSIONS_TPL_COLLABORATOR_GROUPS);
				}

				return null;

			}
		}, false, true);

		/*-- Create raw material --*/
		NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				logger.debug("/*-- Create raw material --*/");
				RawMaterialData rawMaterial = new RawMaterialData();
				rawMaterial.setName("Raw material");
				NodeRef rawMaterialNodeRef = alfrescoRepository.create(testFolderNodeRef, rawMaterial).getNodeRef();
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
				NodeRef finishedProductNodeRef = alfrescoRepository.create(testFolderNodeRef, finishedProduct).getNodeRef();
				// productService.initializeProductFolder(finishedProductNodeRef);

				return finishedProductNodeRef;

			}
		}, false, true);

		// Check
		logger.debug("//Check finished product");
		NodeRef imagesFolder = nodeService.getChildByName(finishedProductNodeRef, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_IMAGES));
		assertNotNull("Images folder must be not null", imagesFolder);
		String imageName = I18NUtil.getMessage(RepoConsts.PATH_PRODUCT_IMAGE) + ".jpg";
		NodeRef imageProductNodeRef = nodeService.getChildByName(imagesFolder, ContentModel.ASSOC_CONTAINS, imageName);
		assertNotNull("Image product must be not null", imageProductNodeRef);
		
		/*-- init repo --*/
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				initRepoVisitor.visitContainer(repositoryHelper.getCompanyHome());
				return null;
			}
		}, false, true);
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
		NodeRef imageNodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS,
				imageName);
		
		if(imageNodeRef == null){
			logger.debug("image name: " + imageName);
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, imageName);
			imageNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS,
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
				return alfrescoRepository.create(testFolderNodeRef, rawMaterial).getNodeRef();
				
			}
		}, false, true);
		
		final NodeRef rawMaterial2NodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {
    			
				// products
    			productsFolder = repoService.getOrCreateFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_PRODUCTS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_PRODUCTS));
    			
				/*-- classify --*/
				logger.debug("/*-- classify --*/");
				productService.classifyProductByHierarchy(productsFolder, rawMaterialNodeRef);

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
				assertEquals("5th Path should be 'Matières premières'", "Matières premières", arrDisplayPaths[3]);
				assertEquals("6th Path should be 'Frozen'", HIERARCHY1_FROZEN, arrDisplayPaths[4]);
				assertEquals("7th Path should be 'Pizza'", HIERARCHY2_PIZZA, arrDisplayPaths[5]);
				assertEquals("check name", "Raw material", nodeService.getProperty(rawMaterialNodeRef, ContentModel.PROP_NAME));

				/*-- classify twice --*/
				logger.debug("/*-- classify twice --*/");
				productService.classifyProductByHierarchy(productsFolder, rawMaterialNodeRef);

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
				assertEquals("5th Path should be 'Matières premières'", "Matières premières", arrDisplayPaths[3]);
				assertEquals("6th Path should be 'Frozen'", HIERARCHY1_FROZEN, arrDisplayPaths[4]);
				assertEquals("7th Path should be 'Pizza'", HIERARCHY2_PIZZA, arrDisplayPaths[5]);
				assertEquals("check name", "Raw material", nodeService.getProperty(rawMaterialNodeRef, ContentModel.PROP_NAME));

				/*-- Create raw material 2 --*/
				// logger.debug("/*-- Create raw material --*/");
				RawMaterialData rawMaterial2 = new RawMaterialData();
				rawMaterial2.setName("Raw material");
				rawMaterial2.setHierarchy1(HIERARCHY1_FROZEN_REF);
				rawMaterial2.setHierarchy2(HIERARCHY2_PIZZA_REF);
				rawMaterial2.setState(SystemState.Valid);
				return alfrescoRepository.create(testFolderNodeRef, rawMaterial2).getNodeRef();
				
			}
		}, false, true);
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {


				/*-- classify --*/
				logger.debug("/*-- classify --*/");
				productService.classifyProductByHierarchy(productsFolder, rawMaterial2NodeRef);

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
				assertEquals("5th Path should be 'Matières premières'", "Matières premières", arrDisplayPaths[3]);
				assertEquals("6th Path should be 'Frozen'", HIERARCHY1_FROZEN, arrDisplayPaths[4]);
				assertEquals("7th Path should be 'Pizza'", HIERARCHY2_PIZZA, arrDisplayPaths[5]);

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

		final NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				/*-- Create raw material --*/
				logger.debug("/*-- Create raw material --*/");
				RawMaterialData rawMaterial = new RawMaterialData();
				rawMaterial.setName("Raw material");
				return alfrescoRepository.create(testFolderNodeRef, rawMaterial).getNodeRef();
		
			}
		}, false, true);
		
		final NodeRef lSF1NodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {
				
				LocalSemiFinishedProductData lSF1 = new LocalSemiFinishedProductData();
				lSF1.setName("Local semi finished 1");
				return alfrescoRepository.create(testFolderNodeRef, lSF1).getNodeRef();
				
			}
		}, false, true);
		
		final NodeRef lSF2NodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				LocalSemiFinishedProductData lSF2 = new LocalSemiFinishedProductData();
				lSF2.setName("Local semi finished 2");
				return alfrescoRepository.create(testFolderNodeRef, lSF2).getNodeRef();
				
			}
		}, false, true);
		
		final NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				/*-- Create finished product --*/
				logger.debug("/*-- Create finished product --*/");
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Finished Product");
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, (CompoListDataItem)null, 1d, 1d, CompoListUnit.P, 0d, DeclarationType.Declare, lSF1NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(0), 1d, 4d, CompoListUnit.P, 0d, DeclarationType.Declare, lSF2NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(1), 3d, 0d, CompoListUnit.kg, 0d, DeclarationType.Omit, rawMaterialNodeRef));
				finishedProduct.getCompoListView().setCompoList(compoList);
				Collection<QName> dataLists = new ArrayList<QName>();
				dataLists.add(BeCPGModel.TYPE_COMPOLIST);
				return alfrescoRepository.create(testFolderNodeRef, finishedProduct).getNodeRef();
				
			}
		}, false, true);
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

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
			
			CompoListDataItem compoListDataItem = new CompoListDataItem(kv.getKey(), (CompoListDataItem)null, 
										(Double)properties.get(BeCPGModel.PROP_COMPOLIST_QTY), 
										(Double)properties.get(BeCPGModel.PROP_COMPOLIST_QTY_SUB_FORMULA), 
										compoListUnit, 
										(Double)properties.get(BeCPGModel.PROP_COMPOLIST_LOSS_PERC), 
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
				NodeRef packagingMaterialNodeRef = alfrescoRepository.create(testFolderNodeRef, packagingMaterial).getNodeRef();

				/*-- Create finished product --*/
				logger.debug("/*-- Create finished product --*/");
				Collection<QName> dataLists = new ArrayList<QName>();
				dataLists.add(BeCPGModel.TYPE_PACKAGINGLIST);

				FinishedProductData finishedProduct1 = new FinishedProductData();
				finishedProduct1.setName("Finished Product 1");
				List<PackagingListDataItem> packagingList1 = new ArrayList<PackagingListDataItem>();
				packagingList1.add(new PackagingListDataItem(null, 1d, PackagingListUnit.P, PackagingLevel.Primary, true, packagingMaterialNodeRef));
				finishedProduct1.getPackagingListView().setPackagingList(packagingList1);
				NodeRef finishedProductNodeRef1 = alfrescoRepository.create(testFolderNodeRef, finishedProduct1).getNodeRef();

				FinishedProductData finishedProduct2 = new FinishedProductData();
				finishedProduct2.setName("Finished Product");
				List<PackagingListDataItem> packagingList2 = new ArrayList<PackagingListDataItem>();
				packagingList2.add(new PackagingListDataItem(null, 8d, PackagingListUnit.PP, PackagingLevel.Secondary, true, packagingMaterialNodeRef));
				finishedProduct2.getPackagingListView().setPackagingList(packagingList2);
				NodeRef finishedProductNodeRef2 = alfrescoRepository.create(testFolderNodeRef, finishedProduct2).getNodeRef();

				List<PackagingListDataItem> wUsedProducts = getWUsedPackagingList(packagingMaterialNodeRef);

				assertEquals("MP should have 2 where Useds", 2, wUsedProducts.size());

				for (PackagingListDataItem packagingListDataItem : wUsedProducts) {

					if (packagingListDataItem.getProduct().equals(finishedProductNodeRef1)) {
						assertEquals(1d, packagingListDataItem.getQty());
						assertEquals(PackagingListUnit.P, packagingListDataItem.getPackagingListUnit());
						assertEquals(PackagingLevel.Primary, packagingListDataItem.getPkgLevel());
					} else if (packagingListDataItem.getProduct().equals(finishedProductNodeRef2)) {
						assertEquals(8d, packagingListDataItem.getQty());
						assertEquals(PackagingListUnit.PP, packagingListDataItem.getPackagingListUnit());
						assertEquals(PackagingLevel.Secondary, packagingListDataItem.getPkgLevel());
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
			PackagingLevel packagingLevel = PackagingLevel.valueOf((String)properties.get(BeCPGModel.PROP_PACKAGINGLIST_PKG_LEVEL));
			
			PackagingListDataItem packagingListDataItem = new PackagingListDataItem(kv.getKey(), 									
						(Double)properties.get(BeCPGModel.PROP_PACKAGINGLIST_QTY), 
						packagingListUnit, 
						packagingLevel, true, 
						kv.getValue().getEntityNodeRef());
			
			wUsedList.add(packagingListDataItem);
		}
		
		logger.debug("wUsedList size" + wUsedList.size());
		
		return wUsedList;
	}
}
