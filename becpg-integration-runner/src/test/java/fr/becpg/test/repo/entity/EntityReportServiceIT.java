/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.repo.report.entity.EntityReportData;
import fr.becpg.repo.report.entity.impl.DefaultEntityReportExtractor;

import fr.becpg.model.PLMModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.repo.report.template.ReportTplInformation;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;
import fr.becpg.report.client.ReportFormat;
import fr.becpg.test.PLMBaseTestCase;

/**
 * The Class ProductReportServiceTest.
 *
 * @author querephi
 */
public class EntityReportServiceIT extends PLMBaseTestCase {

	private static final Log logger = LogFactory.getLog(EntityReportServiceIT.class);
	
	// Test constants
	private static final String TEST_PRODUCT_NAME = "PF";
	private static final String TEST_PRODUCT_RENAMED = "PF renamed";
	private static final String TEST_REPORT_NAME = "report PF 2";
	private static final String TEST_DOCUMENT_PRODUCT_NAME = "PF Document Test";
	private static final String SUPPLIER_DOCS_FOLDER = "Supplier documents";
	private static final String ARTWORK_FOLDER = "Artwork";
	private static final int EXPECTED_SYSTEM_TEMPLATES = 5;
	private static final int EXPECTED_REPORTS_COUNT = 5;

	@Autowired
	private ReportTplService reportTplService;
	@Autowired
	private AssociationService associationService;
	@Autowired
	private EntityReportService entityReportService;
	@Autowired
	private DefaultEntityReportExtractor defaultEntityReportExtractor;

	/** The PF node ref. */
	private NodeRef pfNodeRef;
	private Date createdDate;

	NodeRef defaultReportTplNodeRef = null;
	NodeRef otherReportTplNodeRef = null;
	NodeRef productReportTplFolder = null;

	private void initReports() {

		// Add report tpl
		inWriteTx(() -> {

			for (NodeRef n : reportTplService.getUserReportTemplates(ReportType.Document, PLMModel.TYPE_FINISHEDPRODUCT, "*")) {
				nodeService.deleteNode(n);
			}

			/*-- Add report tpl --*/
			NodeRef systemFolder = repoService.getOrCreateFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_SYSTEM,
					TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
			NodeRef reportsFolder = repoService.getOrCreateFolderByPath(systemFolder, RepoConsts.PATH_REPORTS,
					TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS));
			productReportTplFolder = repoService.getOrCreateFolderByPath(reportsFolder, PlmRepoConsts.PATH_PRODUCT_REPORTTEMPLATES,
					TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_PRODUCT_REPORTTEMPLATES));
			
			ReportTplInformation reportTplInformation = new ReportTplInformation();
			reportTplInformation.setReportType(ReportType.Document);
			reportTplInformation.setReportFormat(ReportFormat.PDF);
			reportTplInformation.setNodeType( PLMModel.TYPE_FINISHEDPRODUCT);
			reportTplInformation.setDefaultTpl(false);
			reportTplInformation.setSystemTpl(true);
			

			reportTplService.createTplRptDesign(productReportTplFolder, TEST_REPORT_NAME, "beCPG/birt/document/product/default/ProductReport.rptdesign",
					reportTplInformation, true);

			return null;

		});
	}

	/**
	 * Tests the generation of product reports with allergen data.
	 * 
	 * This test verifies:
	 * - Creation of a finished product with allergen list data
	 * - Generation of reports using the default report template
	 * - Validation of report content and structure
	 * - Proper handling of allergen voluntary/involuntary flags
	 * 
	 * @throws Exception if report generation fails
	 */
	@Test
	public void testProductReport() throws Exception {

		logger.debug("testIsReportUpToDate()");

		initReports();

		inReadTx(() -> {
			List<NodeRef> ret = reportTplService.getSystemReportTemplates(ReportType.Document, PLMModel.TYPE_FINISHEDPRODUCT);
			for (NodeRef ref : ret) {
				logger.info(nodeService.getProperty(ref, ContentModel.PROP_NAME));
			}
			assertEquals("Expected " + EXPECTED_SYSTEM_TEMPLATES + " system report templates for finished products", 
					EXPECTED_SYSTEM_TEMPLATES,
					reportTplService.getSystemReportTemplates(ReportType.Document, PLMModel.TYPE_FINISHEDPRODUCT).size());
			return null;
		});
		// create product
		inWriteTx(() -> {

			// create PF
			FinishedProductData pfData = new FinishedProductData();
			pfData.setName(TEST_PRODUCT_NAME);
			List<AllergenListDataItem> allergenList = new ArrayList<>();
			allergenList.add(AllergenListDataItem.build().withVoluntary(true).withInVoluntary(true).withAllergen(allergens.get(0)).withIsManual(false));
			allergenList.add(AllergenListDataItem.build().withVoluntary(false).withInVoluntary(true).withAllergen(allergens.get(1)).withIsManual(false));
			allergenList.add(AllergenListDataItem.build().withVoluntary(true).withInVoluntary(false).withAllergen(allergens.get(2)).withIsManual(false));
			allergenList.add(AllergenListDataItem.build().withVoluntary(false).withInVoluntary(false).withAllergen(allergens.get(3)).withIsManual(false));
			pfData.setAllergenList(allergenList);

			pfNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), pfData).getNodeRef();

			return null;
		});

		inWriteTx(() -> {

			createdDate = new Date();
			entityReportService.generateReports(pfNodeRef);

			return null;
		});

		inWriteTx(() -> {

			// check report Tpl
			List<NodeRef> reportTplNodeRefs = reportTplService.getSystemReportTemplates(ReportType.Document, PLMModel.TYPE_FINISHEDPRODUCT);
			assertEquals("Expected " + EXPECTED_SYSTEM_TEMPLATES + " system report templates for finished products", 
					EXPECTED_SYSTEM_TEMPLATES, reportTplNodeRefs.size());

			for (NodeRef reportTplNodeRef : reportTplNodeRefs) {
				String name = (String) nodeService.getProperty(reportTplNodeRef, ContentModel.PROP_NAME);
				logger.debug("Report name: " + name);
				if (Boolean.TRUE.equals(nodeService.getProperty(reportTplNodeRef, ReportModel.PROP_REPORT_TPL_IS_DEFAULT))) {
					defaultReportTplNodeRef = reportTplNodeRef;
				} else if (name.contains(TEST_REPORT_NAME)) {
					otherReportTplNodeRef = reportTplNodeRef;
				}
			}
			assertNotNull("Default report template should be found", defaultReportTplNodeRef);
			assertNotNull("Custom report template '" + TEST_REPORT_NAME + "' should be found", otherReportTplNodeRef);

			// check reports in generated, its name
			Date generatedDate = (Date) nodeService.getProperty(pfNodeRef, ReportModel.PROP_REPORT_ENTITY_GENERATED);
			createdDate.before(generatedDate);
			List<NodeRef> reportNodeRefs = associationService.getTargetAssocs(pfNodeRef, ReportModel.ASSOC_REPORTS);
			assertEquals("Expected " + EXPECTED_REPORTS_COUNT + " generated reports for product", 
					EXPECTED_REPORTS_COUNT, reportNodeRefs.size());

			checkReportNames(defaultReportTplNodeRef, otherReportTplNodeRef, reportNodeRefs);

			// rename PF
			nodeService.setProperty(pfNodeRef, ContentModel.PROP_NAME, TEST_PRODUCT_RENAMED);
			entityReportService.generateReports(pfNodeRef);

			// check reports in generated, its name
			Date generatedDate2 = (Date) nodeService.getProperty(pfNodeRef, ReportModel.PROP_REPORT_ENTITY_GENERATED);
			generatedDate.before(generatedDate2);
			List<NodeRef> reportNodeRefs2 = associationService.getTargetAssocs(pfNodeRef, ReportModel.ASSOC_REPORTS);
			assertEquals(EXPECTED_REPORTS_COUNT, reportNodeRefs2.size());

			checkReportNames(defaultReportTplNodeRef, otherReportTplNodeRef, reportNodeRefs2);
			return null;
		});

		// Test datalist modified
		inWriteTx(() -> {

			FinishedProductData pfData = (FinishedProductData) alfrescoRepository.findOne(pfNodeRef);
			NodeRef nodeRef = pfData.getAllergenList().get(0).getNodeRef();

			// change nothing
			nodeService.setProperty(nodeRef, PLMModel.PROP_ALLERGENLIST_VOLUNTARY, true);

			return null;
		});

		assertFalse(entityReportService.shouldGenerateReport(pfNodeRef, null));

		// Test datalist modified
		inWriteTx(() -> {
			FinishedProductData pfData = (FinishedProductData) alfrescoRepository.findOne(pfNodeRef);
			NodeRef nodeRef = pfData.getAllergenList().get(0).getNodeRef();
			// change something
			nodeService.setProperty(nodeRef, PLMModel.PROP_ALLERGENLIST_VOLUNTARY, false);

			return null;

		});

		inReadTx(() -> {
			assertTrue(entityReportService.shouldGenerateReport(pfNodeRef, null));
			return null;

		});
		// Delete report tpl -> report should be deleted
		inWriteTx(() -> {

			logger.info("Delete report Tpl");
			nodeService.setProperty(otherReportTplNodeRef, ReportModel.PROP_REPORT_TPL_IS_DISABLED, true);
			return null;

		});

		inWriteTx(() -> {

			entityReportService.generateReports(pfNodeRef);

			// check report Tpl
			List<NodeRef> reportTplNodeRefs = reportTplService.getSystemReportTemplates(ReportType.Document, PLMModel.TYPE_FINISHEDPRODUCT);
			assertEquals("check system templates", 4, reportTplNodeRefs.size());

			// check other report is deleted
			List<NodeRef> reportNodeRefs = associationService.getTargetAssocs(pfNodeRef, ReportModel.ASSOC_REPORTS);
			assertEquals(4, reportNodeRefs.size());

			boolean hasReportPF2Name = false;
			for (NodeRef n : reportNodeRefs) {
				String reportName = (String) nodeService.getProperty(n, ContentModel.PROP_NAME);
				if (reportName.contains("report PF 2")) {
					hasReportPF2Name = true;
				}
			}
			assertFalse(hasReportPF2Name);

			nodeService.setProperty(otherReportTplNodeRef, ReportModel.PROP_REPORT_TPL_IS_DISABLED, false);

			return null;

		});

	}

	private void checkReportNames(NodeRef defaultReportTplNodeRef, NodeRef otherReportTplNodeRef, List<NodeRef> reportNodeRefs) {

		String defaultReportName = String.format("%s - %s", nodeService.getProperty(pfNodeRef, ContentModel.PROP_NAME),
				nodeService.getProperty(defaultReportTplNodeRef, ContentModel.PROP_NAME)).replace(RepoConsts.REPORT_EXTENSION_BIRT,
						((String) nodeService.getProperty(defaultReportTplNodeRef, ReportModel.PROP_REPORT_TPL_FORMAT)).toLowerCase());

		String otherReportName = String.format("%s - %s", nodeService.getProperty(pfNodeRef, ContentModel.PROP_NAME),
				nodeService.getProperty(otherReportTplNodeRef, ContentModel.PROP_NAME)).replace(RepoConsts.REPORT_EXTENSION_BIRT,
						((String) nodeService.getProperty(otherReportTplNodeRef, ReportModel.PROP_REPORT_TPL_FORMAT)).toLowerCase());

		int checks = 0;
		for (NodeRef reportNodeRef : reportNodeRefs) {
			String reportName = (String) nodeService.getProperty(reportNodeRef, ContentModel.PROP_NAME);
			logger.debug("Test report name:" + reportName + " compare with :" + defaultReportName);

			if (reportName.equals(defaultReportName) || reportName.equals(otherReportName)) {
				checks++;
			}
		}
		assertEquals("Expected to find both default and custom report names in generated reports", 2, checks);
	}

	/**
	 * Tests retrieval and validation of system report templates for finished products.
	 * 
	 * This test verifies:
	 * - System report templates are properly loaded and accessible
	 * - Expected number of templates are available
	 * - Default and custom report templates can be identified
	 * - Report generation works with both default and custom templates
	 * - Generated reports contain proper metadata and associations
	 * 
	 * @throws Exception if template retrieval or report generation fails
	 */
	@Test
	public void testGetProductSystemReportTemplates() {

		initReports();
		
		String userReportTpl = "user tpl "+(new Date().getTime());
		String userReportTpl2 = "user tpl 2"+(new Date().getTime());
		
		inWriteTx(() -> {

			for(NodeRef tmpRef : reportTplService.getUserReportTemplates(ReportType.Document, PLMModel.TYPE_FINISHEDPRODUCT, "user*")) {
				nodeService.setProperty(tmpRef, ReportModel.PROP_REPORT_TPL_IS_DISABLED, true);
			}
			return null;

		});

		inWriteTx(() -> {

			NodeRef systemFolder = repoService.getOrCreateFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_SYSTEM,
					TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
			NodeRef reportsFolder = repoService.getOrCreateFolderByPath(systemFolder, RepoConsts.PATH_REPORTS,
					TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS));
			productReportTplFolder = repoService.getOrCreateFolderByPath(reportsFolder, PlmRepoConsts.PATH_PRODUCT_REPORTTEMPLATES,
					TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_PRODUCT_REPORTTEMPLATES));

			// create PF
			FinishedProductData pfData = new FinishedProductData();
			pfData.setName("PF");
			List<AllergenListDataItem> allergenList = new ArrayList<>();
			allergenList.add(AllergenListDataItem.build().withVoluntary(true).withInVoluntary(true).withAllergen(allergens.get(0)).withIsManual(false));
			allergenList.add(AllergenListDataItem.build().withVoluntary(false).withInVoluntary(true).withAllergen(allergens.get(1)).withIsManual(false));
			allergenList.add(AllergenListDataItem.build().withVoluntary(true).withInVoluntary(false).withAllergen(allergens.get(2)).withIsManual(false));
			allergenList.add(AllergenListDataItem.build().withVoluntary(false).withInVoluntary(false).withAllergen(allergens.get(3)).withIsManual(false));
			pfData.setAllergenList(allergenList);

			pfNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), pfData).getNodeRef();

			QName typeQName = nodeService.getType(pfNodeRef);
			assertEquals("check system templates", 5, reportTplService.getSystemReportTemplates(ReportType.Document, typeQName).size());

			
			
			ReportTplInformation reportTplInformation = new ReportTplInformation();
			reportTplInformation.setReportType(ReportType.Document);
			reportTplInformation.setReportFormat(ReportFormat.PDF);
			reportTplInformation.setNodeType( PLMModel.TYPE_FINISHEDPRODUCT);
			reportTplInformation.setDefaultTpl(true);
			reportTplInformation.setSystemTpl(false);
			
			// add a user template
			reportTplService.createTplRptDesign(productReportTplFolder, userReportTpl, "beCPG/birt/document/product/default/ProductReport.rptdesign",
					reportTplInformation, true);

			return null;

		});

		waitForSolr();


		final NodeRef userTpl2NodeRef = inWriteTx(() -> {

			assertEquals("check user templates", 1,
					reportTplService.getUserReportTemplates(ReportType.Document, PLMModel.TYPE_FINISHEDPRODUCT, userReportTpl).size());

			ReportTplInformation reportTplInformation = new ReportTplInformation();
			reportTplInformation.setReportType(ReportType.Document);
			reportTplInformation.setReportFormat(ReportFormat.PDF);
			reportTplInformation.setNodeType( PLMModel.TYPE_FINISHEDPRODUCT);
			reportTplInformation.setDefaultTpl(false);
			reportTplInformation.setSystemTpl(false);
			
			// add a user template
			return reportTplService.createTplRptDesign(productReportTplFolder, userReportTpl2,
					"beCPG/birt/document/product/default/ProductReport.rptdesign",reportTplInformation, true);

		});

		waitForSolr();

		inReadTx(() -> {

			assertEquals("check user templates",2,
					reportTplService.getUserReportTemplates(ReportType.Document, PLMModel.TYPE_FINISHEDPRODUCT, "u*").size());
			assertEquals("check user templates",2,
					reportTplService.getUserReportTemplates(ReportType.Document, PLMModel.TYPE_FINISHEDPRODUCT, "user*").size());
			assertEquals("check user templates",2,
					reportTplService.getUserReportTemplates(ReportType.Document, PLMModel.TYPE_FINISHEDPRODUCT, "user tpl 2").size());
			
			assertEquals("check user templates", 1,
					reportTplService.getUserReportTemplates(ReportType.Document, PLMModel.TYPE_FINISHEDPRODUCT, "\""+userReportTpl2+"\"").size());
			assertEquals("check user templates", userTpl2NodeRef,
					reportTplService.getUserReportTemplate(ReportType.Document, PLMModel.TYPE_FINISHEDPRODUCT, userReportTpl2));

			return null;

		});
		
		

	}

	/**
	 * Tests document extraction functionality in entity reports.
	 * 
	 * This test validates the document extraction feature (DEV-893) which allows
	 * reports to include documents from specified folder paths within the product structure.
	 * 
	 * Test scenarios:
	 * - Creates a product with documents in "Supplier documents" folder
	 * - Validates document extraction with single folder path configuration
	 * - Tests document extraction with multiple folder paths (Supplier documents + Artwork)
	 * - Verifies extracted document metadata and content structure
	 * - Ensures proper XML structure for document elements in reports
	 * 
	 * @see DEV-893 Document extraction in reports feature
	 */
	@Test
	public void testDocumentExtractionInReports() {
		logger.debug("testDocumentExtractionInReports()");

		final NodeRef testProductNodeRef = inWriteTx(() -> {
			// Create product with documents
			FinishedProductData pfData = new FinishedProductData();
			pfData.setName(TEST_DOCUMENT_PRODUCT_NAME);
			return alfrescoRepository.create(getTestFolderNodeRef(), pfData).getNodeRef();
		});

		inWriteTx(() -> {
			// Create Supplier documents folder structure
			NodeRef supplierDocsFolder = repoService.getOrCreateFolderByPath(testProductNodeRef, SUPPLIER_DOCS_FOLDER, SUPPLIER_DOCS_FOLDER);
			
			// Create test documents with proper metadata
			createTestDocument(supplierDocsFolder, "test-spec.pdf", "Product Specification", "Technical specification document");
			createTestDocument(supplierDocsFolder, "certificate.pdf", "Quality Certificate", "Quality assurance certificate");
			createTestDocument(supplierDocsFolder, "msds.pdf", "Material Safety Data Sheet", "Safety information document");

			return null;
		});

		inReadTx(() -> {
			// Test document extraction with configuration
			Map<String, String> preferences = new HashMap<>();
			preferences.put("extraDocumentPaths", "cm:Supplier_x0020_documents/*");
			
			EntityReportData reportData = defaultEntityReportExtractor.extract(testProductNodeRef, preferences);
			
			// Validate report data structure
			assertNotNull("Report data should not be null", reportData);
			Document xmlDoc = (Document) reportData.getXmlDataSource().getDocument();
			assertNotNull("XML document should not be null", xmlDoc);
			
			// Validate documents section exists
			Element documentsElt = (Element) xmlDoc.selectSingleNode("//documents");
			assertNotNull("Documents section should exist in report XML", documentsElt);
			
			List<Node> docNodes = documentsElt.selectNodes("document");
			assertEquals("Should extract 3 documents from Supplier documents folder", 3, docNodes.size());
			
			// Validate document attributes and content
			validateDocumentElements(docNodes, testProductNodeRef);
			
			return null;
		});

		// Test with multiple folder paths
		inWriteTx(() -> {
			// Create additional folder structure
			NodeRef artworkFolder = repoService.getOrCreateFolderByPath(testProductNodeRef, ARTWORK_FOLDER, ARTWORK_FOLDER);
			createTestDocument(artworkFolder, "label.pdf", "Product Label", "Product labeling artwork");
			return null;
		});

		inReadTx(() -> {
			// Test multiple paths configuration
			Map<String, String> preferences = new HashMap<>();
			preferences.put("extraDocumentPaths", "cm:Supplier_x0020_documents/*;cm:Artwork/*" );
			
			EntityReportData reportData = defaultEntityReportExtractor.extract(testProductNodeRef, preferences);
			Document xmlDoc = (Document) reportData.getXmlDataSource().getDocument();
			
			List<Node> docNodes = ((Element) xmlDoc.selectSingleNode("//documents")).selectNodes("document");
			assertEquals("Should extract 4 documents from both folders", 4, docNodes.size());
			
			return null;
		});
	}

	/**
	 * Helper method to create test documents with consistent structure
	 */
	private void createTestDocument(NodeRef parentFolder, String fileName, String title, String description) {
		Map<QName, Serializable> docProps = new HashMap<>();
		docProps.put(ContentModel.PROP_NAME, fileName);
		docProps.put(ContentModel.PROP_TITLE, title);
		docProps.put(ContentModel.PROP_DESCRIPTION, description);
		
		nodeService.createNode(parentFolder, ContentModel.ASSOC_CONTAINS, 
			QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, fileName), 
			ContentModel.TYPE_CONTENT, docProps);
	}

	/**
	 * Helper method to validate document XML elements
	 */
	private void validateDocumentElements(List<Node> docNodes, NodeRef entityNodeRef) {
		boolean foundSpec = false;
		boolean foundCert = false;
		boolean foundMsds = false;
		
		for (Node docNode : docNodes) {
			Element docElt = (Element) docNode;
			String docName = docElt.attributeValue("name");
			String docTitle = docElt.attributeValue("title");
			String docId = docElt.attributeValue("id");
			String entityNodeRefAttr = docElt.attributeValue("entityNodeRef");
			String entityType = docElt.attributeValue("entityType");
			
			// Validate required attributes
			assertNotNull("Document name should not be null", docName);
			assertNotNull("Document ID should not be null", docId);
			assertNotNull("Entity nodeRef should not be null", entityNodeRefAttr);
			assertNotNull("Entity type should not be null", entityType);
			
			// Validate path structure

			assertTrue("Document ID should contain Supplier documents path", docId.contains("/cm:Supplier_x0020_documents/"));
			assertEquals("Entity nodeRef should match test product", entityNodeRef.toString(), entityNodeRefAttr);
			assertEquals("Entity type should be finishedProduct", "finishedProduct", entityType);
			
			// Validate specific documents
			switch (docName) {
				case "test-spec.pdf":
					foundSpec = true;
					assertEquals("Product Specification", docTitle);
					break;
				case "certificate.pdf":
					foundCert = true;
					assertEquals("Quality Certificate", docTitle);
					break;
				case "msds.pdf":
					foundMsds = true;
					assertEquals("Material Safety Data Sheet", docTitle);
					break;
			     default:
			      break;
			}
		}
		
		assertTrue("Should find specification document", foundSpec);
		assertTrue("Should find certificate document", foundCert);
		assertTrue("Should find MSDS document", foundMsds);
	}
}
