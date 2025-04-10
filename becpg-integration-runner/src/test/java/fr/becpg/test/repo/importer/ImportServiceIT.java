/*
 *
 */
package fr.becpg.test.repo.importer;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.hierarchy.HierarchyHelper;
import fr.becpg.repo.hierarchy.HierarchyService;
import fr.becpg.repo.importer.ImportService;
import fr.becpg.repo.importer.ImporterException;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.test.PLMBaseTestCase;

/**
 * The Class ImportServiceTest.
 *
 * @author querephi
 */
public class ImportServiceIT extends PLMBaseTestCase {

	private static final String PATH_TEMP = "Temp";
	private static final String PATH_PRODUCTS = "Products";
	private static final String PATH_SITE_FOLDER = "./st:sites/cm:folder";

	private static final Log logger = LogFactory.getLog(ImportServiceIT.class);

	@Autowired
	private ImportService importService;

	@Autowired
	@Qualifier("mlAwareNodeService")
	private NodeService mlNodeServiceImpl;

	@Autowired
	private SearchService searchService;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private HierarchyService hierarchyService;

	/*
	 * (non-Javadoc)
	 *
	 * @see fr.becpg.test.RepoBaseTestCase#setUp()
	 */
	@Override
	public void setUp() throws Exception {
		super.setUp();

		cleanTempFolder();
	}

	private void cleanTempFolder() {

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			NodeRef folderNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TEMP);
			if (folderNodeRef != null) {
				fileFolderService.delete(folderNodeRef);
			}
			return null;
		}, false, true);
	}

	/**
	 * Test import text.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ImporterException
	 *             the be cpg exception
	 * @throws InterruptedException 
	 */
	@Test
	public void testImportText() throws IOException, ImporterException, InterruptedException {

		BatchInfo batchInfo = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Create file to import --*/
			logger.debug("create file to import");
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, "import.xlsx");

			NodeRef nodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS,
					(String) properties.get(ContentModel.PROP_NAME));
			if (nodeRef != null) {
				nodeService.deleteNode(nodeRef);
			}
			nodeRef = nodeService.createNode(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					ContentModel.TYPE_CONTENT, properties).getChildRef();

			ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
			logger.debug("Load import.xlsx");
			InputStream in = (new ClassPathResource("beCPG/import/Import.xlsx")).getInputStream();

			logger.debug("import.xlsx loaded");
			writer.putContent(in);

			logger.debug("Start import");

			try {
				return importService.importText(nodeRef, true, false, null);

			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return null;

		}, false, true);

		waitForBatchEnd(batchInfo);
		
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Check MLText property --*/
			logger.debug("Check MLText properties");
			NodeRef systemFolder = repoService.getFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_SYSTEM);
			assertNotNull("system folder should exist", systemFolder);
			NodeRef ingsFolder = entitySystemService.getSystemEntityDataList(systemFolder, RepoConsts.PATH_CHARACTS, PlmRepoConsts.PATH_INGS);
			assertNotNull("ings folder should exist", ingsFolder);

			// Abricot
			NodeRef ingNodeRef = nodeService.getChildByName(ingsFolder, ContentModel.ASSOC_CONTAINS, "Abricot");
			assertNotNull("abricot ing should exist", ingNodeRef);
			MLText mlText = (MLText) mlNodeServiceImpl.getProperty(ingNodeRef, BeCPGModel.PROP_LEGAL_NAME);
			assertNotNull("MLText exist", mlText);
			assertEquals("MLText exist has 2 Locales", 3, mlText.getLocales().size());
			assertEquals("Check default value", "Abricot french", mlText.getValue(I18NUtil.getContentLocaleLang()));
			assertEquals("Check english value", "Abricot english", mlText.getValue(Locale.ENGLISH));
			assertEquals("Check english value", "Abricot english US", mlText.getValue(Locale.US));

			// Acerola
			ingNodeRef = nodeService.getChildByName(ingsFolder, ContentModel.ASSOC_CONTAINS, "Acerola");
			assertNotNull("Acerola ing should exist", ingNodeRef);
			mlText = (MLText) mlNodeServiceImpl.getProperty(ingNodeRef, BeCPGModel.PROP_LEGAL_NAME);
			assertNotNull("MLText exist", mlText);
			assertEquals("MLText exist has 3 Locales", 3, mlText.getLocales().size());
			assertEquals("Check default value", "Acerola french", mlText.getValue(I18NUtil.getContentLocaleLang()));
			assertEquals("Check english value", "Acerola english", mlText.getValue(Locale.ENGLISH));
			assertEquals("Check english value", "Acerola english US", mlText.getValue(Locale.US));

			// Abricot1
			ingNodeRef = nodeService.getChildByName(ingsFolder, ContentModel.ASSOC_CONTAINS, "Abricot1");
			assertNotNull("Abricot1 ing should exist", ingNodeRef);
			mlText = (MLText) mlNodeServiceImpl.getProperty(ingNodeRef, BeCPGModel.PROP_LEGAL_NAME);
			assertNotNull("MLText exist", mlText);
			assertEquals("MLText exist has 3 Locales", 3, mlText.getLocales().size());
			assertEquals("Check default value", "Abricot1 french", mlText.getValue(I18NUtil.getContentLocaleLang()));
			assertEquals("Check english value", "Abricot1 english", mlText.getValue(Locale.ENGLISH));
			assertEquals("Check english value", "Abricot1 english US", mlText.getValue(Locale.US));

			// Acerola1
			ingNodeRef = nodeService.getChildByName(ingsFolder, ContentModel.ASSOC_CONTAINS, "Acerola1");
			assertNotNull("Acerola1 ing should exist", ingNodeRef);
			mlText = (MLText) mlNodeServiceImpl.getProperty(ingNodeRef, BeCPGModel.PROP_LEGAL_NAME);
			assertNotNull("MLText exist", mlText);
			assertEquals("MLText exist has 3 Locales", 3, mlText.getLocales().size());
			assertEquals("Check default value", "Acerola1 french", mlText.getValue(I18NUtil.getContentLocaleLang()));
			assertEquals("Check english value", "Acerola1 english", mlText.getValue(Locale.ENGLISH));
			assertEquals("Check english value", "Acerola1 english US", mlText.getValue(Locale.US));

			return null;

		}, false, true);
	}

	/**
	 * Test import products.
	 *
	 * @throws Exception
	 * @throws ParseException
	 */
	@Test
	public void testImportProducts() throws Exception {

		/*
		 * Delete temp, products folder Add mapping file
		 */
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			NodeRef tempNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TEMP);
			if (tempNodeRef != null) {
				logger.debug("delete temp folder");
				fileFolderService.delete(tempNodeRef);
			}

			// remove companies
			NodeRef companiesFolder = repoService.getFolderByPath(repositoryHelper.getCompanyHome(), PlmRepoConsts.PATH_COMPANIES);

			if (companiesFolder != null) {
				logger.debug("delete companies folder");
				nodeService.deleteNode(companiesFolder);
			}

			// remove site folder
			List<NodeRef> siteFoldernode = searchService.selectNodes(repositoryHelper.getCompanyHome(), PATH_SITE_FOLDER, null, namespaceService,
					false);

			if ((siteFoldernode != null) && (siteFoldernode.size() > 0)) {
				logger.debug("delete site folder");
				nodeService.deleteNode(siteFoldernode.get(0));
			}

			return null;

		}, false, true);

		/*
		 * Create file
		 */

		BatchInfo batchInfo = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			logger.debug("create file to import");
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, "Import-Products.csv");

			NodeRef nodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS,
					(String) properties.get(ContentModel.PROP_NAME));
			if (nodeRef != null) {
				nodeService.deleteNode(nodeRef);
			}
			nodeRef = nodeService.createNode(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					ContentModel.TYPE_CONTENT, properties).getChildRef();

			ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
			logger.debug("Load import.csv");
			InputStream in = (new ClassPathResource("beCPG/import/Import-Products.csv")).getInputStream();
			logger.debug("import.csv loaded");
			writer.putContent(in);

			logger.debug("Start import");
			
			return importService.importText(nodeRef, true, false, null);
			
		}, false, true);
		
		waitForBatchEnd(batchInfo);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*
			 * check imported values
			 */
			NodeRef tempNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TEMP);
			assertNotNull("Temp folder should exist", tempNodeRef);
			NodeRef importFolderNodeRef = nodeService.getChildByName(tempNodeRef, ContentModel.ASSOC_CONTAINS, PATH_PRODUCTS);
			assertNotNull("import folder should exist", importFolderNodeRef);
			assertEquals(5, fileFolderService.list(importFolderNodeRef).size());

			/*
			 * check products in repo
			 */

			String productName = "Saumon surgelé 80x20x4";
			NodeRef product1NodeRef = nodeService.getChildByName(importFolderNodeRef, ContentModel.ASSOC_CONTAINS, productName);
			// productFolder => look for product

			assertNotNull("product 1 should exist", product1NodeRef);

			ProductData productData = (ProductData) alfrescoRepository.findOne(product1NodeRef);

			logger.debug("Props: " + nodeService.getProperties(product1NodeRef));

			// check Props : Saumon surgelé 80x20x4 saumon sugelé Sea food Fish
			// ToValidate Thu Mar 17 17:44:13 CET 2011 admin Mon Mar 21 22:44:26
			// CET
			// 2011 admin 0.0 0.0 0.0 0.0 0.0 0.0
			// /home/querephi/Documents/beCPG/projets/demo/jeu de
			// données/Sushi/sushi saumon/produit.jpg
			assertEquals("Saumon surgelé 80x20x4", productData.getName());
			assertEquals(SystemState.ToValidate, productData.getState());
			assertEquals("saumon sugelé", productData.getLegalName().getDefaultValue());
			assertEquals("Sea food", HierarchyHelper.getHierachyName(productData.getHierarchy1(), nodeService));
			assertEquals("Fish", HierarchyHelper.getHierachyName(productData.getHierarchy2(), nodeService));

			/*-- check associations --*/
			List<AssociationRef> supplierAssocRefs = nodeService.getTargetAssocs(product1NodeRef, PLMModel.ASSOC_SUPPLIERS);
			assertEquals("check product has 2 suppliers defined", 2, supplierAssocRefs.size());
			String supplier1Code = (String) nodeService.getProperty(supplierAssocRefs.get(0).getTargetRef(), BeCPGModel.PROP_ERP_CODE);
			String supplier2Code = (String) nodeService.getProperty(supplierAssocRefs.get(1).getTargetRef(), BeCPGModel.PROP_ERP_CODE);
			assertTrue("check supplier name", supplier1Code.equals("1000012") && supplier2Code.equals("1000013") || supplier2Code.equals("1000012") && supplier1Code.equals("1000013"));
			// does space between association values work ?

			/*
			 * Check Saumon
			 */
			productName = "Saumon 80x20x3";
			NodeRef product2NodeRef = nodeService.getChildByName(importFolderNodeRef, ContentModel.ASSOC_CONTAINS, productName);
			// productFolder => look for product

			assertNotNull("product 2 should exist", product2NodeRef);
			supplierAssocRefs = nodeService.getTargetAssocs(product2NodeRef, PLMModel.ASSOC_SUPPLIERS);
			assertEquals("check product has 2 suppliers defined", 2, supplierAssocRefs.size());
			supplier1Code = (String) nodeService.getProperty(supplierAssocRefs.get(0).getTargetRef(), BeCPGModel.PROP_ERP_CODE);
			supplier2Code = (String) nodeService.getProperty(supplierAssocRefs.get(1).getTargetRef(), BeCPGModel.PROP_ERP_CODE);
			assertTrue("check supplier name", supplier1Code.equals("1000012") && supplier2Code.equals("1000014") || supplier2Code.equals("1000012") && supplier1Code.equals("1000014"));

			/*-- check productLists --*/
			assertEquals("costs should exist", 2, productData.getCostList().size());
			assertEquals("nuts should exist", 3, productData.getNutList().size());
			String[] costNames = { "Coût MP", "Coût Emb" };
			double[] costValues = { 1.0d, 3.1d };
			String[] nutNames = { "Protéines", "Lipides", "Glucides" };
			double[] nutValues = { 2.5d, 3.6d, 5.6d };

			// check costs
			int costChecked = 0;
			int z_idx = 0;
			for (CostListDataItem c : productData.getCostList()) {
				String costName = (String) nodeService.getProperty(c.getCost(), BeCPGModel.PROP_CHARACT_NAME);

				for (String s : costNames) {
					if (s.equals(costName)) {
						assertEquals("Check cost value", costValues[z_idx], nodeService.getProperty(c.getNodeRef(), PLMModel.PROP_COSTLIST_VALUE));
						costChecked++;
						break;
					}
				}
				z_idx++;
			}
			assertEquals("2 costs have been checked", 2, costChecked);

			// check nuts
			int nutChecked = 0;
			z_idx = 0;
			for (NutListDataItem n : productData.getNutList()) {
				String nutName = (String) nodeService.getProperty(n.getNut(), BeCPGModel.PROP_CHARACT_NAME);

				for (String s : nutNames) {
					if (s.equals(nutName)) {
						assertEquals("Check nut value", nutValues[z_idx], nodeService.getProperty(n.getNodeRef(), PLMModel.PROP_NUTLIST_VALUE));
						nutChecked++;
						break;
					}
				}
				z_idx++;
			}
			assertEquals("3 nuts have been checked", 3, nutChecked);

			// check that file Images/produit.jpg has been imported and check
			// title
			NodeRef imagesNodeRef = nodeService.getChildByName(product2NodeRef, ContentModel.ASSOC_CONTAINS, "Images");
			assertNotNull("check Images exits", imagesNodeRef);

			NodeRef imgNodeRef = nodeService.getChildByName(imagesNodeRef, ContentModel.ASSOC_CONTAINS, "produit.jpg");
			assertNotNull("check produit.jpg exits", imgNodeRef);
			assertEquals("Check title on image", "sushi saumon", nodeService.getProperty(imgNodeRef, ContentModel.PROP_TITLE));

			/*
			 * check trim is done by CSVReader
			 */

			NodeRef product4NodeRef = nodeService.getChildByName(importFolderNodeRef, ContentModel.ASSOC_CONTAINS, "Thon 80x20x8");
			assertNotNull("product 4 should exist", product4NodeRef);

			/*
			 * check productTpl
			 */

			NodeRef productTplNodeRef = nodeService.getChildByName(importFolderNodeRef, ContentModel.ASSOC_CONTAINS, "productTpl");
			assertNotNull("productTpl should exist", productTplNodeRef);

			ProductData productTplData = (ProductData) alfrescoRepository.findOne(product1NodeRef);

			/*-- check productLists of productTpl --*/
			assertEquals("costs should exist", 2, productData.getCostList().size());
			assertEquals("nuts should exist", 3, productData.getNutList().size());

			// check costs
			costChecked = 0;
			z_idx = 0;
			for (CostListDataItem c : productTplData.getCostList()) {
				String costName = (String) nodeService.getProperty(c.getCost(), BeCPGModel.PROP_CHARACT_NAME);

				for (String s : costNames) {
					if (s.equals(costName)) {
						assertEquals("Check cost value", costValues[z_idx], nodeService.getProperty(c.getNodeRef(), PLMModel.PROP_COSTLIST_VALUE));
						costChecked++;
						break;
					}
				}
				z_idx++;
			}
			assertEquals("2 costs have been checked", 2, costChecked);

			// check nuts
			nutChecked = 0;
			z_idx = 0;
			for (NutListDataItem n : productTplData.getNutList()) {
				String nutName = (String) nodeService.getProperty(n.getNut(), BeCPGModel.PROP_CHARACT_NAME);

				for (String s : nutNames) {
					if (s.equals(nutName)) {
						assertEquals("Check nut value", nutValues[z_idx], nodeService.getProperty(n.getNodeRef(), PLMModel.PROP_NUTLIST_VALUE));
						nutChecked++;
						break;
					}
				}
				z_idx++;
			}

			assertEquals("3 nuts have been checked", 3, nutChecked);

			/*
			 * check products import in site, it is not classified
			 */

			List<NodeRef> siteFoldernode = BeCPGQueryBuilder.createQuery().selectNodesByPath(repositoryHelper.getCompanyHome(), PATH_SITE_FOLDER);
			assertEquals("classif folder should exist", 1, siteFoldernode.size());
			NodeRef siteFolderNodeRef = siteFoldernode.get(0);
			assertEquals("1 product should exist", 1, fileFolderService.list(siteFolderNodeRef).size());

			productName = "Saumon surgelé 80x20x4";
			product1NodeRef = nodeService.getChildByName(importFolderNodeRef, ContentModel.ASSOC_CONTAINS, productName);

			assertNotNull("product 1 should exist", product1NodeRef);

			return null;

		}, false, true);
	}

	@Test
	public void testCatchIntegrityException() throws IOException, ImporterException {

		Exception exception = null;

		/**
		 * Test the catch of integrity exception during import
		 */
		try {
			BatchInfo batchInfo = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

				/*-- Clean costs --*/
				NodeRef systemFolder = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS,
						TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
				NodeRef costsFolder = nodeService.getChildByName(systemFolder, ContentModel.ASSOC_CONTAINS,
						TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_COSTS));

				if (costsFolder != null) {
					nodeService.deleteNode(costsFolder);
				}

				/*-- Create file to import --*/
				logger.debug("create file to import");
				Map<QName, Serializable> properties = new HashMap<>();
				properties.put(ContentModel.PROP_NAME, "Import-with-IntegrityException.csv");

				NodeRef nodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS,
						(String) properties.get(ContentModel.PROP_NAME));
				if (nodeRef != null) {
					nodeService.deleteNode(nodeRef);
				}
				nodeRef = nodeService.createNode(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
						ContentModel.TYPE_CONTENT, properties).getChildRef();

				ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
				InputStream in = (new ClassPathResource("beCPG/import/Import-with-IntegrityException.csv")).getInputStream();
				writer.putContent(in);

				logger.debug("Start import");
				
				importService.importText(nodeRef, true, false, null);
				
				return null;

			}, false, true);
			
			waitForBatchEnd(batchInfo);
			
			transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

				/*-- Clean costs --*/
				NodeRef systemFolder = nodeService.getChildByName(repositoryHelper.getCompanyHome(),
						ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
				NodeRef costsFolder = nodeService.getChildByName(systemFolder, ContentModel.ASSOC_CONTAINS,
						TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_COSTS));

				/*-- check nothing is imported --*/
				systemFolder = nodeService.getChildByName(repositoryHelper.getCompanyHome(),
						ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
				costsFolder = nodeService.getChildByName(systemFolder, ContentModel.ASSOC_CONTAINS,
						TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_COSTS));
				assertNull("costs should not exist", costsFolder);
				return null;

			}, false, true);
			
		} catch (Exception e) {
			// logger.error("error as expected while importing file.", e);
			exception = e;
		}

		assertNotNull("Check exception was thrown", exception);
	}

	/**
	 * Test import product lists
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ImporterException
	 *             the be cpg exception
	 * @throws InterruptedException 
	 */
	@Test
	public void testImportProductLists() throws IOException, ImporterException, InterruptedException {

		BatchInfo batchInfo = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Delete temp, products folder --*/
			NodeRef tempNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TEMP);
			if (tempNodeRef != null) {
				fileFolderService.delete(tempNodeRef);
			}

			/*-- Create file to import --*/
			logger.debug("create file to import");
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, "Import-ProductLists.csv");

			NodeRef nodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS,
					(String) properties.get(ContentModel.PROP_NAME));
			if (nodeRef != null) {
				nodeService.deleteNode(nodeRef);
			}
			nodeRef = nodeService.createNode(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					ContentModel.TYPE_CONTENT, properties).getChildRef();

			ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
			logger.debug("Load import.csv");
			InputStream in = (new ClassPathResource("beCPG/import/Import-ProductLists.csv")).getInputStream();
			logger.debug("import.csv loaded");
			writer.putContent(in);

			logger.debug("Start import");
			
			return importService.importText(nodeRef, true, false, null);
			
		}, false, true);
		
		waitForBatchEnd(batchInfo);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- check imported values --*/
			NodeRef tempNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TEMP);
			assertNotNull("Temp folder should exist", tempNodeRef);
			NodeRef importFolderNodeRef = nodeService.getChildByName(tempNodeRef, ContentModel.ASSOC_CONTAINS, PATH_PRODUCTS);
			assertNotNull("import folder should exist", importFolderNodeRef);
			logger.info("###fileFolderService.listFiles(importFolderNodeRef).size()" + fileFolderService.listFiles(importFolderNodeRef).size());
			assertEquals(4, fileFolderService.list(importFolderNodeRef).size());

			/*
			 * check products
			 */

			NodeRef product1NodeRef = nodeService.getChildByName(importFolderNodeRef, ContentModel.ASSOC_CONTAINS, "Saumon surgelé 80x20x4");

			assertNotNull("product 1 should exist", product1NodeRef);
			ProductData productData = (ProductData) alfrescoRepository.findOne(product1NodeRef);

			/*-- check productLists --*/

			assertEquals("compoList should exist", 3, productData.getCompoListView().getCompoList().size());
			String[] rmNames = { "MP1", "MP2", "MP3" };
			double[] qtyValues = { 1.0d, 2.0d, 3.2d };
			String[] unitValues = { "g", "kg", "g" };

			// check MP
			int rmChecked = 0;
			int z_idx = 0;
			for (CompoListDataItem c : productData.getCompoListView().getCompoList()) {
				String rmName = (String) nodeService.getProperty(c.getProduct(), ContentModel.PROP_NAME);

				for (String s : rmNames) {
					if (s.equals(rmName)) {
						assertEquals("Check rm value", qtyValues[z_idx], nodeService.getProperty(c.getNodeRef(), PLMModel.PROP_COMPOLIST_QTY));
						assertEquals("Check rm unit", unitValues[z_idx], nodeService.getProperty(c.getNodeRef(), PLMModel.PROP_COMPOLIST_UNIT));
						rmChecked++;
						break;
					}
				}
				z_idx++;
			}
			assertEquals("3 rm have been checked", 3, rmChecked);

			return null;

		}, false, true);
	}

	/**
	 * Test import text.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ImporterException
	 *             the be cpg exception
	 * @throws InterruptedException 
	 */
	@Test
	public void testImportHierarchies() throws IOException, ImporterException, InterruptedException {

		importHierarchies();

		// 2nd time to check they are updated and not recreated
		importHierarchies();
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			// search by name
			List<NodeRef> ret = hierarchyService.getHierarchiesByPath(HIERARCHY_RAWMATERIAL_PATH, null, "USDA", false);
			assertEquals(1, ret.size());

			// search by code
			assertNotNull(hierarchyService.getHierarchyByPath(HIERARCHY_RAWMATERIAL_PATH, null,
					(String) nodeService.getProperty(ret.get(0), BeCPGModel.PROP_CODE)));

			NodeRef parentNodeRef = ret.get(0);

			// search by name
			ret = hierarchyService.getHierarchiesByPath(HIERARCHY_RAWMATERIAL_PATH, parentNodeRef, "Dairy and Egg Products", false);

			assertEquals(1, ret.size());

			// search by code
			assertNotNull(hierarchyService.getHierarchyByPath(HIERARCHY_RAWMATERIAL_PATH, parentNodeRef,
					(String) nodeService.getProperty(ret.get(0), BeCPGModel.PROP_CODE)));

			return null;

		}, false, true);
	}

	private void importHierarchiesFile(final int i) throws InterruptedException {
		BatchInfo batchInfo = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Create file to import --*/
			logger.debug("create file to import");
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, "import-productHierarchies" + i + ".csv");

			NodeRef nodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS,
					(String) properties.get(ContentModel.PROP_NAME));
			if (nodeRef != null) {
				nodeService.deleteNode(nodeRef);
			}
			nodeRef = nodeService.createNode(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					ContentModel.TYPE_CONTENT, properties).getChildRef();

			ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
			logger.debug("Load import.csv");
			InputStream in = (new ClassPathResource("beCPG/import/import-productHierarchies" + i + ".csv")).getInputStream();
			logger.debug("import.csv loaded");
			writer.putContent(in);

			logger.debug("Start import");
			
			return importService.importText(nodeRef, true, false, null);
			
		}, false, true);
		
		waitForBatchEnd(batchInfo);
	}

	private void importHierarchies() throws InterruptedException {
		/*-- Check hierarchies --*/
		logger.debug("Check hierarchies");
		importHierarchiesFile(1);

		NodeRef hierarchy1USDA = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			return hierarchyService.getHierarchyByPath(HIERARCHY_RAWMATERIAL_PATH, null, "USDA");

		}, false, true);

		assertNotNull(hierarchy1USDA);

		importHierarchiesFile(2);

		NodeRef hierarchy2Dairy = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			return hierarchyService.getHierarchyByPath(HIERARCHY_RAWMATERIAL_PATH, hierarchy1USDA, "Dairy and Egg Products");

		}, false, true);

		assertNotNull(hierarchy2Dairy);

		NodeRef hierarchy2Spices = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			return hierarchyService.getHierarchyByPath(HIERARCHY_RAWMATERIAL_PATH, hierarchy1USDA, "Spices and Herbs");

		}, false, true);

		assertNotNull(hierarchy2Spices);

		importHierarchiesFile(3);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			NodeRef hierarchy3Dairy = hierarchyService.getHierarchyByPath(HIERARCHY_RAWMATERIAL_PATH, hierarchy2Dairy, "Dairy");
			assertNotNull(hierarchy3Dairy);

			return null;

		}, false, true);

		importHierarchiesFile(4);
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			// check unicity
			List<NodeRef> listItems = BeCPGQueryBuilder.createQuery().andID(hierarchy1USDA).inDB().list();
			assertEquals(1, listItems.size());
			listItems = BeCPGQueryBuilder.createQuery().andID(hierarchy2Dairy).inDB().list();
			assertEquals(1, listItems.size());
			listItems = BeCPGQueryBuilder.createQuery().andID(hierarchy2Spices).inDB().list();
			assertEquals(1, listItems.size());

			return null;

		}, false, true);
	}

	@Test
	public void testImportFormula() throws IOException, ImporterException, InterruptedException {

		BatchInfo batchInfo = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Create file to import --*/
			logger.debug("create file to import");
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, "importClaim.csv");

			NodeRef nodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					ContentModel.TYPE_CONTENT, properties).getChildRef();

			ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
			logger.debug("Load importClaim.csv");
			InputStream in = (new ClassPathResource("beCPG/import/LabelClaims.csv")).getInputStream();
			logger.debug("import.csv loaded");
			writer.putContent(in);

			logger.debug("Start import");
			
			return importService.importText(nodeRef, true, false, null);
			
		}, false, true);
		
		waitForBatchEnd(batchInfo);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			NodeRef systemFolder = repoService.getFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_SYSTEM);

			NodeRef labelClaimListsFolder = entitySystemService.getSystemEntityDataList(systemFolder, RepoConsts.PATH_CHARACTS,
					PlmRepoConsts.PATH_LABELCLAIMS);
			List<NodeRef> labelClaimsFileInfo = entityListDAO.getListItems(labelClaimListsFolder, PLMModel.TYPE_LABEL_CLAIM);

			Assert.assertEquals(labelClaimsFileInfo.size(), 3);

			for (NodeRef fileInfo : labelClaimsFileInfo) {
				String formula = (String) nodeService.getProperty(fileInfo, PLMModel.PROP_LABEL_CLAIM_FORMULA);

				Assert.assertNotNull(formula);
				logger.info(formula);
			}

			return null;

		}, false, true);
	}

}
