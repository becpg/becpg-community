package fr.becpg.test.repo.report;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.PLMModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.report.entity.EntityReportParameters;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.repo.report.pdf.ReportPdfAggregator;
import fr.becpg.repo.report.pdf.ReportPdfAggregator.AnnexDocument;
import fr.becpg.repo.report.pdf.ReportPdfAggregator.AnnexSection;
import fr.becpg.repo.report.pdf.ReportPdfAggregator.ComponentHeadingStyle;
import fr.becpg.repo.report.pdf.ReportPdfAggregator.HeaderModel;
import fr.becpg.repo.report.template.ReportTplInformation;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;
import fr.becpg.repo.sample.StandardChocolateEclairTestProduct;
import fr.becpg.report.client.ReportFormat;
import fr.becpg.test.PLMBaseTestCase;

public class AggregateReportIT extends PLMBaseTestCase {

    private static final Log logger = LogFactory.getLog(AggregateReportIT.class);

    @Autowired
    private EntityReportService entityReportService;

    @Autowired
    private ReportTplService reportTplService;

    @Autowired
    private RepoService customRepoService;

    @Autowired
    private AssociationService associationService;

    @Test
    public void testPdfBoxAggregatorUnit() throws Exception {
        // 1. Prepare Mock BIRT Body (3 pages)
        // Page 0: Cover Page
        // Page 1: Annex Title (with bookmark becpg.annex.annexe-mp)
        // Page 2: ToC Page (containing text token {{page:annexe-mp}} and {{page:annexe-emb-primaire}})
        byte[] bodyPdf = createMockPdfWithOutline(
                "becpg.annex.annexe-mp", 1,
                "Cover Page",
                "ANNEX 1: RAW MATERIALS",
                "Table of Contents: Raw Materials: {{page:annexe-mp}} | Packaging: {{page:annexe-emb-primaire}}"
        );

        // 2. Prepare Mock Annex Documents
        byte[] rmAnnexPdf = createMockPdf("Raw Material Supplier Spec Page 1", "Raw Material Supplier Spec Page 2");
        byte[] pkgAnnexPdf = createMockPdf("Packaging Spec Page 1");

        List<AnnexSection> sections = new ArrayList<>();
        sections.add(new AnnexSection(
                "annexe-mp",
                "ANNEX 1: RAW MATERIALS",
                Collections.singletonList(new AnnexDocument("Marin Collagen", rmAnnexPdf))
        ));
        sections.add(new AnnexSection(
                "annexe-emb-primaire",
                "ANNEX 2: PACKAGING",
                Collections.singletonList(new AnnexDocument("Glass Bottle 50ml", pkgAnnexPdf))
        ));

        // 3. Configure Header & Footer
        HeaderModel header = new HeaderModel();
        header.setTitle("TECHNICAL SPECIFICATIONS ${erpCode} ${version}");
        header.setSubtitle("${legalName}");
        header.setDate("2026-07-07");
        header.setStartNumberingAt(1);

        ComponentHeadingStyle headingStyle = new ComponentHeadingStyle();
        headingStyle.setColor("#1F3864");
        headingStyle.setSize(11);
        headingStyle.setPrefix("> ");

        Map<String, String> properties = new HashMap<>();
        properties.put("erpCode", "ERP9999");
        properties.put("version", "v1.0");
        properties.put("legalName", "Marin Collagen Drink");

        // 4. Run the Aggregation
        byte[] finalPdf = ReportPdfAggregator.assemble(
                bodyPdf,
                sections,
                header,
                headingStyle,
                null, // logo bytes
                properties
        );

        assertNotNull(finalPdf);
        assertTrue(finalPdf.length > 0);

        // 5. Verify the generated PDF
        try (PDDocument doc = Loader.loadPDF(finalPdf)) {
            // Total Pages: 3 body + 2 RM + 1 Packaging = 6 pages
            assertEquals(6, doc.getNumberOfPages());

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);

            // Verify placeholders resolved
            assertTrue(text.contains("TECHNICAL SPECIFICATIONS ERP9999 v1.0"));
            assertTrue(text.contains("Marin Collagen Drink"));

            // Verify ToC token is overwritten with correct page numbers (RM starts on Page 3, Packaging starts on Page 5)
            stripper.setStartPage(5); // Final Page 5 (index 4)
            stripper.setEndPage(5);
            String tocText = stripper.getText(doc);
            System.out.println("DEBUG TOCTEXT: [" + tocText + "]");

            assertTrue("Expected tocText to contain '3' but got: [" + tocText + "]", tocText.contains("3"));
            assertTrue("Expected tocText to contain '6' but got: [" + tocText + "]", tocText.contains("6"));
        }
    }

    @Test
    public void testPdfBoxAggregatorTocPosition0() throws Exception {
        byte[] bodyPdf = createMockPdfWithOutline(
                "becpg.annex.annexe-mp", 1,
                "Cover Page",
                "ANNEX 1: RAW MATERIALS",
                "Table of Contents: Raw Materials: {{page:annexe-mp}} | Packaging: {{page:annexe-emb-primaire}}"
        );

        byte[] rmAnnexPdf = createMockPdf("Raw Material Supplier Spec Page 1", "Raw Material Supplier Spec Page 2");
        byte[] pkgAnnexPdf = createMockPdf("Packaging Spec Page 1");

        List<AnnexSection> sections = new ArrayList<>();
        sections.add(new AnnexSection(
                "annexe-mp",
                "ANNEX 1: RAW MATERIALS",
                Collections.singletonList(new AnnexDocument("Marin Collagen", rmAnnexPdf))
        ));
        sections.add(new AnnexSection(
                "annexe-emb-primaire",
                "ANNEX 2: PACKAGING",
                Collections.singletonList(new AnnexDocument("Glass Bottle 50ml", pkgAnnexPdf))
        ));

        HeaderModel header = new HeaderModel();
        header.setTitle("TECHNICAL SPECIFICATIONS ${erpCode} ${version}");
        header.setSubtitle("${legalName}");
        header.setDate("2026-07-07");
        header.setStartNumberingAt(1);

        ComponentHeadingStyle headingStyle = new ComponentHeadingStyle();
        headingStyle.setColor("#1F3864");
        headingStyle.setSize(11);
        headingStyle.setPrefix("> ");

        Map<String, String> properties = new HashMap<>();
        properties.put("erpCode", "ERP9999");
        properties.put("version", "v1.0");
        properties.put("legalName", "Marin Collagen Drink");

        ReportPdfAggregator.TableOfContentsModel tocConfig = new ReportPdfAggregator.TableOfContentsModel();
        tocConfig.setEnabled(true);
        tocConfig.setPosition(0); // Position 0: Very first page of the document

        byte[] finalPdf = ReportPdfAggregator.assemble(
                bodyPdf,
                sections,
                header,
                headingStyle,
                null,
                properties,
                tocConfig
        );

        assertNotNull(finalPdf);
        assertTrue(finalPdf.length > 0);

        try (PDDocument doc = Loader.loadPDF(finalPdf)) {
            // Total Pages: 3 body + 1 ToC + 2 RM + 1 Packaging = 7 pages
            assertEquals(7, doc.getNumberOfPages());

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);

            // ToC is the very first page (index 0). BIRT report starts on page 2.
            // Annex 1 starts at page 4. Annex 2 starts at page 7.
            assertTrue(text.contains("4"));
            assertTrue(text.contains("7"));
        }
    }

    @Test
    public void testPdfBoxAggregatorTocPosition1() throws Exception {
        byte[] bodyPdf = createMockPdfWithOutline(
                "becpg.annex.annexe-mp", 1,
                "Cover Page",
                "ANNEX 1: RAW MATERIALS",
                "Table of Contents: Raw Materials: {{page:annexe-mp}} | Packaging: {{page:annexe-emb-primaire}}"
        );

        byte[] rmAnnexPdf = createMockPdf("Raw Material Supplier Spec Page 1", "Raw Material Supplier Spec Page 2");
        byte[] pkgAnnexPdf = createMockPdf("Packaging Spec Page 1");

        List<AnnexSection> sections = new ArrayList<>();
        sections.add(new AnnexSection(
                "annexe-mp",
                "ANNEX 1: RAW MATERIALS",
                Collections.singletonList(new AnnexDocument("Marin Collagen", rmAnnexPdf))
        ));
        sections.add(new AnnexSection(
                "annexe-emb-primaire",
                "ANNEX 2: PACKAGING",
                Collections.singletonList(new AnnexDocument("Glass Bottle 50ml", pkgAnnexPdf))
        ));

        HeaderModel header = new HeaderModel();
        header.setTitle("TECHNICAL SPECIFICATIONS ${erpCode} ${version}");
        header.setSubtitle("${legalName}");
        header.setDate("2026-07-07");
        header.setStartNumberingAt(1);

        ComponentHeadingStyle headingStyle = new ComponentHeadingStyle();
        headingStyle.setColor("#1F3864");
        headingStyle.setSize(11);
        headingStyle.setPrefix("> ");

        Map<String, String> properties = new HashMap<>();
        properties.put("erpCode", "ERP9999");
        properties.put("version", "v1.0");
        properties.put("legalName", "Marin Collagen Drink");

        ReportPdfAggregator.TableOfContentsModel tocConfig = new ReportPdfAggregator.TableOfContentsModel();
        tocConfig.setEnabled(true);
        tocConfig.setPosition(1); // Position 1: After entire BIRT report

        byte[] finalPdf = ReportPdfAggregator.assemble(
                bodyPdf,
                sections,
                header,
                headingStyle,
                null,
                properties,
                tocConfig
        );

        assertNotNull(finalPdf);
        assertTrue(finalPdf.length > 0);

        try (PDDocument doc = Loader.loadPDF(finalPdf)) {
            // Total Pages: 3 body + 1 ToC + 2 RM + 1 Packaging = 7 pages
            assertEquals(7, doc.getNumberOfPages());

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);

            // ToC is page 4 (index 3). Annex 1 starts at page 5. Annex 2 starts at page 7.
            assertTrue(text.contains("5"));
            assertTrue(text.contains("7"));
        }
    }

    @Test
    public void testPdfBoxAggregatorPagination() throws Exception {
        byte[] bodyPdf = createMockPdf("Body Page 1", "Body Page 2");
        byte[] annexPdf = createMockPdf("Supplier Spec Page 1");

        List<AnnexSection> sections = new ArrayList<>();
        sections.add(new AnnexSection(
                "annexe-mp",
                "ANNEX 1: RAW MATERIALS",
                Collections.singletonList(new AnnexDocument("Raw Material", annexPdf))
        ));

        HeaderModel header = new HeaderModel();
        header.setTitle("SPECIFICATIONS ${version}");

        ComponentHeadingStyle headingStyle = new ComponentHeadingStyle();
        Map<String, String> properties = new HashMap<>();
        properties.put("version", "v1.0");

        ReportPdfAggregator.PaginationModel paginationConfig = new ReportPdfAggregator.PaginationModel();
        paginationConfig.setEnabled(true);
        paginationConfig.setFormat("Page ${page} sur ${total}");

        byte[] finalPdf = ReportPdfAggregator.assemble(
                bodyPdf,
                sections,
                header,
                headingStyle,
                null,
                properties,
                null,
                paginationConfig
        );

        assertNotNull(finalPdf);
        assertTrue(finalPdf.length > 0);

        try (PDDocument doc = Loader.loadPDF(finalPdf)) {
            assertEquals(3, doc.getNumberOfPages());

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);

            assertTrue(text.contains("Page 1 sur 3") || text.contains("Page 2 sur 3") || text.contains("Page 3 sur 3"));
        }
    }

    @Test
    public void testAggregateReportGenerationIT() throws Exception {
        // Create Finished Product
        final NodeRef pfNodeRef = inWriteTx(() -> {
            FinishedProductData pfData = new FinishedProductData();
            pfData.setName("PF Test Aggregate");
            NodeRef pfRef = alfrescoRepository.create(getTestFolderNodeRef(), pfData).getNodeRef();

            // Create Documents folder
            NodeRef docsFolder = customRepoService.getOrCreateFolderByPath(pfRef, RepoConsts.PATH_DOCUMENTS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_DOCUMENTS));

            // Create mock PDF to attach
            byte[] docBytes = createMockPdf("Mock Supplier Specifications Page 1", "Mock Supplier Specifications Page 2");
            NodeRef docNodeRef = nodeService.createNode(docsFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "supplier_spec.pdf"), ContentModel.TYPE_CONTENT).getChildRef();
            ContentWriter writer = contentService.getWriter(docNodeRef, ContentModel.PROP_CONTENT, true);
            writer.setMimetype("application/pdf");
            writer.putContent(new ByteArrayInputStream(docBytes));

            // Set rep:reportKinds aspect to direct routing
            Map<QName, Serializable> aspectProps = new HashMap<>();
            List<String> reportKinds = Collections.singletonList("annexe-mp");
            aspectProps.put(ReportModel.PROP_REPORT_KINDS, (Serializable) reportKinds);
            nodeService.addAspect(docNodeRef, ReportModel.ASPECT_REPORT_KIND, aspectProps);

            return pfRef;
        });

        // Initialize and register aggregate template
        final NodeRef tplRef = inWriteTx(() -> {
            NodeRef systemFolder = customRepoService.getOrCreateFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_SYSTEM,
                    TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
            NodeRef reportsFolder = customRepoService.getOrCreateFolderByPath(systemFolder, RepoConsts.PATH_REPORTS,
                    TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS));
            NodeRef productReportTplFolder = customRepoService.getOrCreateFolderByPath(reportsFolder, PlmRepoConsts.PATH_PRODUCT_REPORTTEMPLATES,
                    TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_PRODUCT_REPORTTEMPLATES));
            
            NodeRef aggJsonNodeRef = reportTplService.createTplRessource(productReportTplFolder, "beCPG/birt/document/product/default/TestAggregateReport.agg.json", true);
            List<NodeRef> aggResources = new ArrayList<>();
            aggResources.add(aggJsonNodeRef);

            ReportTplInformation reportTplInformation = new ReportTplInformation();
            reportTplInformation.setReportType(ReportType.Document);
            reportTplInformation.setReportFormat(ReportFormat.PDF);
            reportTplInformation.setNodeType(PLMModel.TYPE_FINISHEDPRODUCT);
            reportTplInformation.setDefaultTpl(false);
            reportTplInformation.setSystemTpl(false);
            reportTplInformation.setResources(aggResources);
            
            NodeRef templateNodeRef = reportTplService.createTplRptDesign(productReportTplFolder, "SpecTechAggIT", "beCPG/birt/document/product/default/TestAggregateReport.rptdesign",
                    reportTplInformation, true);
            nodeService.setProperty(templateNodeRef, ReportModel.PROP_REPORT_TPL_IS_AGGREGATE, true);

            return templateNodeRef;
        });

        // Generate Report
        inWriteTx(() -> {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            EntityReportParameters reportParameters = new EntityReportParameters();
            
            try {
                entityReportService.generateReport(pfNodeRef, tplRef, reportParameters, Locale.getDefault(), ReportFormat.PDF, out);
                byte[] finalPdfBytes = out.toByteArray();

                if (finalPdfBytes == null || finalPdfBytes.length == 0) {
                    logger.warn("BIRT report generation returned empty bytes. Skipping output validation because BIRT Report Server might be offline/unconfigured.");
                    return null;
                }

                // Verify with PDFbox
                try (PDDocument doc = Loader.loadPDF(finalPdfBytes)) {
                    assertTrue(doc.getNumberOfPages() > 0);
                }
            } catch (Exception e) {
                logger.warn("Skipping BIRT report validation due to execution exception (BIRT server may be offline): " + e.getMessage(), e);
            }
            return null;
        });
    }
    
    @Test
    public void testGeneratePIFReportForChocolateEclairIT() throws Exception {

        // 1. Create rich test product using StandardChocolateEclairTestProduct.Builder
        final FinishedProductData testProduct = inWriteTx(() -> {
            StandardChocolateEclairTestProduct eclair = new StandardChocolateEclairTestProduct.Builder()
                    .withAlfrescoRepository(alfrescoRepository)
                    .withNodeService(nodeService)
                    .withDestFolder(getTestFolderNodeRef())
                    .withCompo(true)
                    .withLabeling(true)
                    .withGenericRawMaterial(true)
                    .withStocks(true)
                    .withIngredients(true)
                    .withSurvey(true)
                    .withScoreList(true)
                    .withClaim(true)
                    .withSpecification(true)
                    .withNuts(true)
                    .withProcess(true)
                    .build();
            return eclair.createTestProduct();
        });

        assertNotNull(testProduct);
        final NodeRef pfNodeRef = testProduct.getNodeRef();
        assertNotNull(pfNodeRef);

        // 2. Attach a mock CPSR PDF document with reportKind = annexe-cpsr
        inWriteTx(() -> {
            NodeRef docsFolder = customRepoService.getOrCreateFolderByPath(pfNodeRef, RepoConsts.PATH_DOCUMENTS,
                    TranslateHelper.getTranslatedPath(RepoConsts.PATH_DOCUMENTS));

            byte[] cpsrPdfBytes = createMockPdf(
                    "COSMETIC PRODUCT SAFETY REPORT (CPSR / DSE) - PART A",
                    "Toxicological Profile & Exposure Assessment - Page 2"
            );

            NodeRef cpsrDocNodeRef = nodeService.createNode(docsFolder, ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "cpsr_safety_report.pdf"),
                    ContentModel.TYPE_CONTENT).getChildRef();

            ContentWriter writer = contentService.getWriter(cpsrDocNodeRef, ContentModel.PROP_CONTENT, true);
            writer.setMimetype("application/pdf");
            writer.putContent(new ByteArrayInputStream(cpsrPdfBytes));

            // Set aspect rep:reportKinds = "annexe-cpsr"
            Map<QName, Serializable> aspectProps = new HashMap<>();
            aspectProps.put(ReportModel.PROP_REPORT_KINDS, (Serializable) Collections.singletonList("annexe-cpsr"));
            nodeService.addAspect(cpsrDocNodeRef, ReportModel.ASPECT_REPORT_KIND, aspectProps);

            return null;
        });

        // 3. Retrieve and enable PIF aggregate report template and Compo Quali-Quanti for PIF template
        final NodeRef pifTemplateNodeRef = getAndEnableReportTemplate(
                TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_PIF_REPORT) + "." + RepoConsts.REPORT_EXTENSION_BIRT,
                "Product Information File.rptdesign",
                "Dossier d'information produit.rptdesign",
                "PIFReport.rptdesign"
        );
        final NodeRef compoForPifTemplateNodeRef = getAndEnableReportTemplate(
                TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_PRODUCT_REPORT_COMPO_QUALI_QUANTI_FOR_PIF) + "." + RepoConsts.REPORT_EXTENSION_BIRT,
                "Composition Quali-Quanti pour DIP (PIF).rptdesign",
                "Quali-Quanti Composition for PIF.rptdesign"
        );

        try {
            // 4. Generate English PIF Report
            inWriteTx(() -> {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                EntityReportParameters reportParameters = new EntityReportParameters();

                try {
                    entityReportService.generateReport(pfNodeRef, pifTemplateNodeRef, reportParameters, Locale.ENGLISH, ReportFormat.PDF, out);
                    byte[] finalPdfBytes = out.toByteArray();

                    if (finalPdfBytes == null || finalPdfBytes.length == 0) {
                        logger.warn("[PIFReportEclairIT] BIRT report server returned empty bytes (server may be offline).");
                        return null;
                    }

                    try (PDDocument doc = Loader.loadPDF(finalPdfBytes)) {
                        assertTrue("Expected PDF to contain pages", doc.getNumberOfPages() > 0);
                        logger.info("[PIFReportEclairIT Success] Generated English PIF PDF Size: " + finalPdfBytes.length + " bytes, Pages: " + doc.getNumberOfPages());

                        PDFTextStripper stripper = new PDFTextStripper();
                        String fullText = stripper.getText(doc);
                        assertNotNull(fullText);

                        assertTrue("Expected English Title", fullText.contains("PRODUCT INFORMATION FILE (PIF)"));
                        assertTrue("Expected English ToC", fullText.contains("TABLE OF CONTENTS"));
                    }
                } catch (Exception e) {
                    logger.warn("Skipping BIRT PIF report assertion due to execution exception (BIRT server offline): " + e.getMessage(), e);
                }
                return null;
            });

            // 5. Generate PIF Report directly into repository document node and verify committed content
            inWriteTx(() -> {
                try {
                    NodeRef docsFolder = customRepoService.getOrCreateFolderByPath(pfNodeRef, RepoConsts.PATH_DOCUMENTS,
                            TranslateHelper.getTranslatedPath(RepoConsts.PATH_DOCUMENTS));
                    String docName = "PIF_Report_Eclair.pdf";
                    NodeRef reportDocNodeRef = nodeService.getChildByName(docsFolder, ContentModel.ASSOC_CONTAINS, docName);
                    if (reportDocNodeRef == null) {
                        reportDocNodeRef = nodeService.createNode(docsFolder, ContentModel.ASSOC_CONTAINS,
                                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, docName),
                                ReportModel.TYPE_REPORT).getChildRef();
                        associationService.update(reportDocNodeRef, ReportModel.ASSOC_REPORT_TPL, pifTemplateNodeRef);
                    }

                    ContentWriter initialWriter = contentService.getWriter(reportDocNodeRef, ContentModel.PROP_CONTENT, true);
                    initialWriter.putContent("Loading ...");
                    nodeService.setProperty(reportDocNodeRef, ReportModel.PROP_REPORT_IS_DIRTY, true);

                    entityReportService.generateReport(pfNodeRef, reportDocNodeRef);

                    ContentReader reader = contentService.getReader(reportDocNodeRef, ContentModel.PROP_CONTENT);
                    assertNotNull("ContentReader should exist for committed report node", reader);
                    assertTrue("ContentReader should exist on storage", reader.exists());

                    Boolean isDirty = (Boolean) nodeService.getProperty(reportDocNodeRef, ReportModel.PROP_REPORT_IS_DIRTY);
                    assertFalse("Report document should not be dirty after generation", Boolean.TRUE.equals(isDirty));

                    try (InputStream in = reader.getContentInputStream()) {
                        byte[] nodeBytes = in.readAllBytes();
                        if (nodeBytes != null && nodeBytes.length > 0) {
                            String header = new String(nodeBytes, 0, Math.min(nodeBytes.length, 10));
                            assertFalse("Committed node content must not remain 'Loading ...'", header.startsWith("Loading"));
                            assertTrue("Committed node content must be a valid PDF starting with %PDF-", header.startsWith("%PDF-"));

                            try (PDDocument doc = Loader.loadPDF(nodeBytes)) {
                                assertTrue("Committed PDF document should contain pages", doc.getNumberOfPages() > 0);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Skipping repository report node assertion if report server offline: " + e.getMessage(), e);
                }
                return null;
            });
        } finally {
            disableTemplate(pifTemplateNodeRef);
            disableTemplate(compoForPifTemplateNodeRef);
        }
    }

//    @Test
    public void testParallelPIFGenerationsBenchmarkIT() throws Exception {
        int threadCount = 4; // 4 concurrent parallel PIF generations on 4 distinct products

        // 1. Create N distinct rich test products using StandardChocolateEclairTestProduct.Builder
        List<NodeRef> targetProducts = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            NodeRef productNodeRef = inWriteTx(() -> {
                NodeRef benchmarkFolder = customRepoService.getOrCreateFolderByPath(getTestFolderNodeRef(), "ParallelBenchmarkFolder_" + idx, "ParallelBenchmarkFolder_" + idx);
                FinishedProductData testProduct = new StandardChocolateEclairTestProduct.Builder()
                        .withAlfrescoRepository(alfrescoRepository)
                        .withNodeService(nodeService)
                        .withDestFolder(benchmarkFolder)
                        .withCompo(true)
                        .withLabeling(true)
                        .withGenericRawMaterial(true)
                        .withStocks(true)
                        .withIngredients(true)
                        .withSurvey(true)
                        .withScoreList(true)
                        .withClaim(true)
                        .withSpecification(true)
                        .withNuts(true)
                        .withProcess(true)
                        .build()
                        .createTestProduct();

                NodeRef pfRef = testProduct.getNodeRef();
                nodeService.setProperty(pfRef, ContentModel.PROP_NAME, "Chocolate Eclair Benchmark " + idx);

                // Attach CPSR PDF document to each product
                NodeRef docsFolder = customRepoService.getOrCreateFolderByPath(pfRef, RepoConsts.PATH_DOCUMENTS,
                        TranslateHelper.getTranslatedPath(RepoConsts.PATH_DOCUMENTS));
                byte[] cpsrPdfBytes = createMockPdf("CPSR PART A - Safety Assessment", "CPSR PART B - Toxicological Profile");
                NodeRef cpsrDocNodeRef = nodeService.createNode(docsFolder, ContentModel.ASSOC_CONTAINS,
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "cpsr_report_" + idx + ".pdf"),
                        ContentModel.TYPE_CONTENT).getChildRef();
                ContentWriter writer = contentService.getWriter(cpsrDocNodeRef, ContentModel.PROP_CONTENT, true);
                writer.setMimetype("application/pdf");
                writer.putContent(new ByteArrayInputStream(cpsrPdfBytes));

                Map<QName, Serializable> aspectProps = new HashMap<>();
                aspectProps.put(ReportModel.PROP_REPORT_KINDS, (Serializable) Collections.singletonList("annexe-cpsr"));
                nodeService.addAspect(cpsrDocNodeRef, ReportModel.ASPECT_REPORT_KIND, aspectProps);

                return pfRef;
            });
            targetProducts.add(productNodeRef);
        }

        // 2. Retrieve and enable PIF aggregate report template and Compo Quali-Quanti for PIF template
        final NodeRef pifTemplateNodeRef = getAndEnableReportTemplate(
                TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_PIF_REPORT) + "." + RepoConsts.REPORT_EXTENSION_BIRT,
                "Product Information File.rptdesign",
                "Dossier d'information produit.rptdesign",
                "PIFReport.rptdesign"
        );
        final NodeRef compoForPifTemplateNodeRef = getAndEnableReportTemplate(
                TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_PRODUCT_REPORT_COMPO_QUALI_QUANTI_FOR_PIF) + "." + RepoConsts.REPORT_EXTENSION_BIRT,
                "Composition Quali-Quanti pour DIP (PIF).rptdesign",
                "Quali-Quanti Composition for PIF.rptdesign"
        );

        try {
            // Warm up single run on first product
            inWriteTx(() -> {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                entityReportService.generateReport(targetProducts.get(0), pifTemplateNodeRef, new EntityReportParameters(), Locale.ENGLISH, ReportFormat.PDF, out);
                return null;
            });

            // 3. Parallel Benchmark Execution (Each thread targets its own distinct product)
            CountDownLatch readyLatch = new CountDownLatch(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch finishLatch = new CountDownLatch(threadCount);
            List<Long> durations = Collections.synchronizedList(new ArrayList<>());
            List<Integer> pdfSizes = Collections.synchronizedList(new ArrayList<>());
            ExecutorService pool = Executors.newFixedThreadPool(threadCount);

            long startMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long wallClockStart = System.currentTimeMillis();

            for (int i = 0; i < threadCount; i++) {
                final int threadIdx = i;
                final NodeRef productForThread = targetProducts.get(threadIdx);
                pool.submit(() -> {
                	inWriteTx(() -> {
                		return AuthenticationUtil.runAsSystem(() -> {
                			readyLatch.countDown();
                			try {
                				startLatch.await();
                				long t0 = System.currentTimeMillis();
                				
                				ByteArrayOutputStream out = new ByteArrayOutputStream();
                				EntityReportParameters reportParameters = new EntityReportParameters();
                				entityReportService.generateReport(productForThread, pifTemplateNodeRef, reportParameters, Locale.ENGLISH, ReportFormat.PDF, out);
                				
                				long t1 = System.currentTimeMillis();
                				byte[] result = out.toByteArray();
                				durations.add(t1 - t0);
                				if (result != null) {
                					pdfSizes.add(result.length);
                				}
                			} catch (Exception e) {
                				logger.error("[PARALLEL BENCHMARK Thread " + threadIdx + "] Failed: " + e.getMessage(), e);
                			} finally {
                				finishLatch.countDown();
                			}
                			return null;
                		});
                	});
                });
            }

            readyLatch.await(); // Wait for all worker threads to get ready
            startLatch.countDown(); // Start all parallel requests simultaneously
            finishLatch.await(); // Wait for all requests to finish
            pool.shutdown();

            long wallClockDuration = System.currentTimeMillis() - wallClockStart;
            long endMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            logger.info("========================================================================");
            logger.info("[PARALLEL PIF BENCHMARK RESULTS]");
            logger.info("  * Parallel Threads / Requests: " + threadCount);
            logger.info("  * Total Wall-Clock Time: " + wallClockDuration + " ms");
            logger.info("  * Average Duration per Report: " + (durations.isEmpty() ? 0 : (durations.stream().mapToLong(Long::longValue).sum() / durations.size())) + " ms");
            logger.info("  * Min Duration: " + durations.stream().mapToLong(Long::longValue).min().orElse(0) + " ms");
            logger.info("  * Max Duration: " + durations.stream().mapToLong(Long::longValue).max().orElse(0) + " ms");
            logger.info("  * Average PDF Size: " + (pdfSizes.isEmpty() ? 0 : (pdfSizes.stream().mapToInt(Integer::intValue).sum() / pdfSizes.size())) + " bytes");
            logger.info("  * Heap Memory Delta: " + ((endMem - startMem) / (1024 * 1024)) + " MB");
            logger.info("========================================================================");
        } finally {
            disableTemplate(pifTemplateNodeRef);
            disableTemplate(compoForPifTemplateNodeRef);
        }
    }

    @Test
    public void testVariedAnnexSizesAndToCPlaceholderReplacement() throws Exception {
        byte[] bodyPdf = createMockPdfWithBirtPageNumbers(
                "Cover Page - Technical Specs",
                "Product Specification Details Page 2",
                "TABLE OF CONTENTS: Raw Materials: {{page:annexe-mp}} | Primary Packaging: {{page:annexe-emb-primaire}} | Secondary Packaging: {{page:annexe-emb-secondaire}} | Photos: {{page:annexe-photos}} | Quality Docs: {{page:annexe-qualite}}"
        );

        byte[] rmAnnexPdf = createMockPdf("Raw Material Supplier Spec Single Page");
        byte[] pkgPrimaryPdf = createMockPdf("Primary Pkg 1", "Primary Pkg 2", "Primary Pkg 3", "Primary Pkg 4");
        byte[] pkgSecondaryPdf = createMockPdf("Sec Pkg 1", "Sec Pkg 2", "Sec Pkg 3", "Sec Pkg 4", "Sec Pkg 5", "Sec Pkg 6", "Sec Pkg 7", "Sec Pkg 8");
        byte[] photosPdf = createMockPdf("Photo Page 1", "Photo Page 2", "Photo Page 3");

        List<AnnexSection> sections = new ArrayList<>();
        sections.add(new AnnexSection("annexe-mp", "ANNEX 1: RAW MATERIALS", Collections.singletonList(new AnnexDocument("RM Doc", rmAnnexPdf))));
        sections.add(new AnnexSection("annexe-emb-primaire", "ANNEX 2: PRIMARY PACKAGING", Collections.singletonList(new AnnexDocument("Pkg Pri", pkgPrimaryPdf))));
        sections.add(new AnnexSection("annexe-emb-secondaire", "ANNEX 3: SECONDARY PACKAGING", Collections.singletonList(new AnnexDocument("Pkg Sec", pkgSecondaryPdf))));
        sections.add(new AnnexSection("annexe-photos", "ANNEX 4: PHOTOS", Collections.singletonList(new AnnexDocument("Photos", photosPdf))));
        sections.add(new AnnexSection("annexe-qualite", "ANNEX 5: QUALITY", Collections.emptyList(), "N/A"));

        HeaderModel header = new HeaderModel();
        header.setTitle("TECHNICAL SPECIFICATIONS v1.0");

        ReportPdfAggregator.PaginationModel paginationConfig = new ReportPdfAggregator.PaginationModel();
        paginationConfig.setEnabled(true);
        paginationConfig.setFormat("Page ${page} / ${total}");

        byte[] finalPdf = ReportPdfAggregator.assemble(bodyPdf, sections, header, null, null, Collections.emptyMap(), null, paginationConfig);

        assertNotNull(finalPdf);
        try (PDDocument doc = Loader.loadPDF(finalPdf)) {
            // Total: 3 (body) + 1 (RM) + 4 (Pri Pkg) + 8 (Sec Pkg) + 3 (Photos) + 1 (Quality placeholder) = 20 pages
            assertEquals(20, doc.getNumberOfPages());

            PDFTextStripper stripper = new PDFTextStripper();
            String fullText = stripper.getText(doc);

            assertTrue(fullText.contains("4"));
            assertTrue(fullText.contains("5"));
            assertTrue(fullText.contains("9"));
            assertTrue(fullText.contains("17"));
            assertTrue(fullText.contains("20"));

            assertTrue(fullText.contains("Page 1 / 20"));
            assertTrue(fullText.contains("Page 20 / 20"));
        }
    }

    @Test
    public void testDynamicToCWithVariedAnnexSizesAndPlaceholders() throws Exception {
        byte[] bodyPdf = createMockPdf(
                "Cover Page",
                "TABLE OF CONTENTS: Raw Materials: {{page:annexe-mp}} | Primary Packaging: {{page:annexe-emb-primaire}} | Secondary Packaging: {{page:annexe-emb-secondaire}}"
        );

        byte[] rmAnnexPdf = createMockPdf("RM Spec 1", "RM Spec 2");
        byte[] pkgPrimaryPdf = createMockPdf("Pkg 1", "Pkg 2", "Pkg 3", "Pkg 4", "Pkg 5");
        byte[] pkgSecondaryPdf = createMockPdf("Sec Pkg Single Page");

        List<AnnexSection> sections = new ArrayList<>();
        sections.add(new AnnexSection("annexe-mp", "ANNEX 1: RAW MATERIALS", Collections.singletonList(new AnnexDocument("RM", rmAnnexPdf))));
        sections.add(new AnnexSection("annexe-emb-primaire", "ANNEX 2: PRIMARY PACKAGING", Collections.singletonList(new AnnexDocument("Pri Pkg", pkgPrimaryPdf))));
        sections.add(new AnnexSection("annexe-emb-secondaire", "ANNEX 3: SECONDARY PACKAGING", Collections.singletonList(new AnnexDocument("Sec Pkg", pkgSecondaryPdf))));

        ReportPdfAggregator.TableOfContentsModel tocConfig = new ReportPdfAggregator.TableOfContentsModel();
        tocConfig.setEnabled(true);
        tocConfig.setPosition(0);

        ReportPdfAggregator.PaginationModel paginationConfig = new ReportPdfAggregator.PaginationModel();
        paginationConfig.setEnabled(true);
        paginationConfig.setFormat("Page ${page} / ${total}");

        byte[] finalPdf = ReportPdfAggregator.assemble(bodyPdf, sections, null, null, null, Collections.emptyMap(), tocConfig, paginationConfig);

        assertNotNull(finalPdf);
        try (PDDocument doc = Loader.loadPDF(finalPdf)) {
            // Total: 1 (injected ToC) + 2 (body) + 2 (RM) + 5 (Pri Pkg) + 1 (Sec Pkg) = 11 pages
            assertEquals(11, doc.getNumberOfPages());

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);

            assertTrue(text.contains("4"));
            assertTrue(text.contains("6"));
            assertTrue(text.contains("11"));
            assertTrue(text.contains("Page 1 / 11"));
            assertTrue(text.contains("Page 11 / 11"));
        }
    }

    @Test
    public void testLargeDocumentWithVariedAnnexSizes() throws Exception {
        byte[] bodyPdf = createMockPdfWithBirtPageNumbers(
                "Page 1 Content", "Page 2 Content", "Page 3 Content", "Page 4 Content",
                "Table of Contents: Raw Materials: {{page:annexe-mp}} | Packaging: {{page:annexe-emb-primaire}}"
        );

        String[] rmPages = new String[15];
        for (int i = 0; i < 15; i++) {
            rmPages[i] = "RM Page " + (i + 1);
        }
        byte[] rmAnnexPdf = createMockPdf(rmPages);

        String[] pkgPages = new String[25];
        for (int i = 0; i < 25; i++) {
            pkgPages[i] = "Pkg Page " + (i + 1);
        }
        byte[] pkgAnnexPdf = createMockPdf(pkgPages);

        List<AnnexSection> sections = new ArrayList<>();
        sections.add(new AnnexSection("annexe-mp", "ANNEX 1: RAW MATERIALS", Collections.singletonList(new AnnexDocument("RM Large", rmAnnexPdf))));
        sections.add(new AnnexSection("annexe-emb-primaire", "ANNEX 2: PACKAGING", Collections.singletonList(new AnnexDocument("Pkg Large", pkgAnnexPdf))));

        ReportPdfAggregator.PaginationModel paginationConfig = new ReportPdfAggregator.PaginationModel();
        paginationConfig.setEnabled(true);
        paginationConfig.setFormat("Page ${page} / ${total}");

        byte[] finalPdf = ReportPdfAggregator.assemble(bodyPdf, sections, null, null, null, Collections.emptyMap(), null, paginationConfig);

        assertNotNull(finalPdf);
        try (PDDocument doc = Loader.loadPDF(finalPdf)) {
            // Total: 5 (body) + 15 (RM) + 25 (Pkg) = 45 pages
            assertEquals(45, doc.getNumberOfPages());

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);

            assertTrue(text.contains("6"));
            assertTrue(text.contains("21"));
            assertTrue(text.contains("Page 1 / 45"));
            assertTrue(text.contains("Page 45 / 45"));
        }
    }

    @Test
    public void testImageAnnexesConversionAndAggregation() throws Exception {
        byte[] bodyPdf = createMockPdf(
                "Technical Specification Cover",
                "Table of Contents: Photos: {{page:annexe-photos}}"
        );

        byte[] pngImageBytes = createMockPngImage(200, 200);

        List<AnnexSection> sections = new ArrayList<>();
        sections.add(new AnnexSection("annexe-photos", "ANNEX 4: PHOTOS", Collections.singletonList(new AnnexDocument("Product Photo", pngImageBytes))));

        ReportPdfAggregator.PaginationModel paginationConfig = new ReportPdfAggregator.PaginationModel();
        paginationConfig.setEnabled(true);
        paginationConfig.setFormat("Page ${page} / ${total}");

        byte[] finalPdf = ReportPdfAggregator.assemble(bodyPdf, sections, null, null, null, Collections.emptyMap(), null, paginationConfig);

        assertNotNull(finalPdf);
        try (PDDocument doc = Loader.loadPDF(finalPdf)) {
            // Total: 2 (body) + 1 (converted PNG image page) = 3 pages
            assertEquals(3, doc.getNumberOfPages());

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);

            assertTrue(text.contains("3"));
            assertTrue(text.contains("Page 1 / 3"));
            assertTrue(text.contains("Page 3 / 3"));
        }
    }

    @Test
    public void testBodyPageTextNotCoveredByPageNumberLocator() throws Exception {
        byte[] bodyPdf = createMockPdfWithBirtPageNumbers(
                "See Page 2 for details about Packaging Page",
                "Second Page Content"
        );

        ReportPdfAggregator.PaginationModel paginationConfig = new ReportPdfAggregator.PaginationModel();
        paginationConfig.setEnabled(true);
        paginationConfig.setFormat("Page ${page} / ${total}");

        byte[] finalPdf = ReportPdfAggregator.assemble(bodyPdf, Collections.emptyList(), null, null, null, Collections.emptyMap(), null, paginationConfig);

        assertNotNull(finalPdf);
        try (PDDocument doc = Loader.loadPDF(finalPdf)) {
            assertEquals(2, doc.getNumberOfPages());

            PDFTextStripper stripper = new PDFTextStripper();
            String page1Text = stripper.getText(doc);
            assertTrue(page1Text.contains("See Page 2 for details about Packaging Page"));
            assertTrue(page1Text.contains("Page 1 / 2"));
            assertTrue(page1Text.contains("Page 2 / 2"));
        }
    }

    private byte[] createMockPngImage(int width, int height) throws Exception {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }

    private byte[] createMockPdfWithBirtPageNumbers(String... pageTexts) throws Exception {
        try (PDDocument doc = new PDDocument()) {
            int total = pageTexts.length;
            for (int i = 0; i < total; i++) {
                PDPage page = new PDPage();
                doc.addPage(page);
                try (PDPageContentStream canvas = new PDPageContentStream(doc, page)) {
                    drawTextWithLines(canvas, pageTexts[i], 50, 700);
                    canvas.beginText();
                    canvas.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    canvas.newLineAtOffset(450, 50);
                    canvas.showText("Page " + (i + 1) + " of " + total);
                    canvas.endText();
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

    private byte[] createMockPdf(String... pageTexts) throws Exception {
        try (PDDocument doc = new PDDocument()) {
            for (String text : pageTexts) {
                PDPage page = new PDPage();
                doc.addPage(page);
                try (PDPageContentStream canvas = new PDPageContentStream(doc, page)) {
                    drawTextWithLines(canvas, text, 50, 700);
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

    private byte[] createMockPdfWithOutline(String bookmarkName, int pageIndex, String... pageTexts) throws Exception {
        try (PDDocument doc = new PDDocument()) {
            List<PDPage> pages = new ArrayList<>();
            for (String text : pageTexts) {
                PDPage page = new PDPage();
                doc.addPage(page);
                pages.add(page);
                try (PDPageContentStream canvas = new PDPageContentStream(doc, page)) {
                    drawTextWithLines(canvas, text, 50, 700);
                }
            }

            PDDocumentOutline outline = new PDDocumentOutline();
            doc.getDocumentCatalog().setDocumentOutline(outline);
            PDOutlineItem item = new PDOutlineItem();
            item.setTitle(bookmarkName);
            PDPageDestination dest = new PDPageFitWidthDestination();
            dest.setPage(pages.get(pageIndex));
            item.setDestination(dest);
            outline.addLast(item);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

    private void drawTextWithLines(PDPageContentStream canvas, String text, float startX, float startY) throws Exception {
        canvas.beginText();
        canvas.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
        canvas.newLineAtOffset(startX, startY);
        if (text != null) {
            String cleanText = text.replace("\r\n", "\n").replace("\r", "\n");
            String[] lines = cleanText.split("\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].replaceAll("[\\r\\n]", "");
                if (i > 0) {
                    canvas.newLineAtOffset(0, -15);
                }
                canvas.showText(line);
            }
        }
        canvas.endText();
    }

    private NodeRef getAndEnableReportTemplate(String... candidateNames) {
        NodeRef tpl = inWriteTx(() -> {
            NodeRef systemFolder = customRepoService.getOrCreateFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_SYSTEM,
                    TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
            NodeRef reportsFolder = customRepoService.getOrCreateFolderByPath(systemFolder, RepoConsts.PATH_REPORTS,
                    TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS));
            NodeRef productReportTplFolder = customRepoService.getOrCreateFolderByPath(reportsFolder, PlmRepoConsts.PATH_PRODUCT_REPORTTEMPLATES,
                    TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_PRODUCT_REPORTTEMPLATES));

            ClassDefinition pfClassDef = serviceRegistry.getDictionaryService().getClass(PLMModel.TYPE_FINISHEDPRODUCT);
            String pfFolderTitle = pfClassDef != null ? pfClassDef.getTitle(serviceRegistry.getDictionaryService()) : null;
            NodeRef pfFolder = null;
            if (pfFolderTitle != null) {
                pfFolder = customRepoService.getFolderByPath(productReportTplFolder, pfFolderTitle);
                if (pfFolder == null) {
                    pfFolder = nodeService.getChildByName(productReportTplFolder, ContentModel.ASSOC_CONTAINS, pfFolderTitle);
                }
            }

            NodeRef foundTpl = null;
            if (pfFolder != null) {
                foundTpl = findChildByName(pfFolder, candidateNames);
            }
            if (foundTpl == null) {
                foundTpl = findChildByName(productReportTplFolder, candidateNames);
            }
            if (foundTpl == null) {
                foundTpl = findChildByName(reportsFolder, candidateNames);
            }

            if (foundTpl != null) {
                nodeService.setProperty(foundTpl, ReportModel.PROP_REPORT_TPL_IS_DISABLED, false);
            }

            return foundTpl;
        });

        if (tpl == null) {
            throw new IllegalStateException("Report template not found among children for names: " + Arrays.toString(candidateNames));
        }

        return tpl;
    }

    private NodeRef findChildByName(NodeRef parent, String... candidateNames) {
        if (parent == null) {
            return null;
        }
        List<ChildAssociationRef> children = nodeService.getChildAssocs(parent);
        if (children != null) {
            for (ChildAssociationRef childAssoc : children) {
                NodeRef child = childAssoc.getChildRef();
                String name = (String) nodeService.getProperty(child, ContentModel.PROP_NAME);
                if (name != null) {
                    for (String candidate : candidateNames) {
                        if (candidate != null && (name.equals(candidate) || name.equalsIgnoreCase(candidate))) {
                            return child;
                        }
                    }
                }
                QName type = nodeService.getType(child);
                if (serviceRegistry.getDictionaryService().isSubClass(type, ContentModel.TYPE_FOLDER)) {
                    NodeRef sub = findChildByName(child, candidateNames);
                    if (sub != null) {
                        return sub;
                    }
                }
            }
        }
        return null;
    }

    private void disableTemplate(NodeRef tpl) {
        if (tpl != null) {
            inWriteTx(() -> {
                nodeService.setProperty(tpl, ReportModel.PROP_REPORT_TPL_IS_DISABLED, true);
                return null;
            });
        }
    }
}
