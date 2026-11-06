/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.test.repo.product.report;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.report.search.ExportSearchService;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;
import fr.becpg.repo.sample.StandardChocolateEclairTestProduct;
import fr.becpg.report.client.ReportFormat;
import fr.becpg.test.PLMBaseTestCase;
import fr.becpg.test.utils.TestWebscriptExecuters;
import fr.becpg.test.utils.TestWebscriptExecuters.GetRequest;
import fr.becpg.test.utils.TestWebscriptExecuters.Response;

/**
 * Integration test for MultiLevelExcelReportSearchPluginV2
 * 
 * @author matthieu
 */
public class MultiLevelExcelReportSearchPluginIT extends PLMBaseTestCase {

	@Autowired
	private NodeService nodeService;

	@Autowired
	private ReportTplService reportTplService;

	@Autowired
	private ExportSearchService exportSearchService;

	private static final String HEADER_VALUES = "VALUES";

	private static final int COMPOSITION_SHEET_INDEX = 1;

	private static final int COLUMNS_ROW_INDEX = 1;

	private static final String COMPONENT_NAME = "Component of a composition line";

	private static final String COMPONENT_ERP_CODE = "ERP-COMPONENT-1";

	private static final String COMPONENT_NAME_COLUMN = "bcpg:compoListProduct|cm:name";

	private static final String COMPONENT_ERP_CODE_COLUMN = "bcpg:compoListProduct|bcpg:erpCode";

	private static final int TYPE_ROW_INDEX = 0;

	private static final int TYPE_PARAMETER_CELL_INDEX = 2;

	private static final String ONLY_LEVEL_2 = "OnlyLevel2";

	private static final String WUSED_ONLY_LEVEL_2 = "wUsedOnlyLevel2";

	private static final String LEVEL_1_SEMI_FINISHED = "Level 1 semi-finished product";

	private static final String LEVEL_1_COMPONENT = "Level 1 raw material";

	private static final String LEVEL_2_COMPONENT_A = "Level 2 raw material A";

	private static final String LEVEL_2_COMPONENT_B = "Level 2 raw material B";

	private static final String COMPOSITION_TEMPLATE_FOLDER_PATH = "/app:company_home/cm:System/cm:Reports/cm:ExportSearch/cm:ExportProducts";
	private static final String COMPOSITION_TEMPLATE_FILE_NAME = "Export des listes composition et emballages.xlsx";

	private NodeRef compositionPackagingReportTpl;

	/**
	 * Initialize test report templates - use existing ExportCompositionPackaging.xlsx
	 */
	private void initTestReports() {
		List<String> candidateNames = buildCandidateNames();

		compositionPackagingReportTpl = findTemplateByPath();
		if (compositionPackagingReportTpl == null) {
			compositionPackagingReportTpl = resolveTemplate(candidateNames);
		}

		String failureMessage = "ExportCompositionPackaging.xlsx template should exist";
		if (compositionPackagingReportTpl == null) {
			List<NodeRef> allTemplates = reportTplService.getSystemReportTemplates(ReportType.ExportSearch, null);
			failureMessage = failureMessage + ". Available templates: " + buildAvailableTemplateNames(allTemplates);
		}

		assertNotNull(failureMessage, compositionPackagingReportTpl);
	}

	private List<String> buildCandidateNames() {
		String translatedName = TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_REPORTS_EXPORT_SEARCH_COMPOSITIONPACKAGING);
		String localizedWithExtension = translatedName.endsWith(".xlsx") ? translatedName : translatedName + ".xlsx";
		List<String> candidateNames = new ArrayList<>();
		candidateNames.add(localizedWithExtension);
		candidateNames.add(localizedWithExtension + RepoConsts.INITIAL_VERSION);
		candidateNames.add(translatedName);
		candidateNames.add(translatedName + RepoConsts.INITIAL_VERSION);
		candidateNames.add("ExportCompositionPackaging.xlsx");
		candidateNames.add("ExportCompositionPackaging.xlsx" + RepoConsts.INITIAL_VERSION);
		candidateNames.add("ExportCompositionPackaging");
		candidateNames.add(COMPOSITION_TEMPLATE_FILE_NAME);
		candidateNames.add(COMPOSITION_TEMPLATE_FILE_NAME + RepoConsts.INITIAL_VERSION);
		return candidateNames;
	}

	private NodeRef findTemplateByPath() {
		NodeRef templateFolder = repoService.getFolderByPath(COMPOSITION_TEMPLATE_FOLDER_PATH);
		if (templateFolder == null) {
			return null;
		}
		NodeRef template = nodeService.getChildByName(templateFolder, ContentModel.ASSOC_CONTAINS, COMPOSITION_TEMPLATE_FILE_NAME);
		if (template != null) {
			return template;
		}
		return nodeService.getChildByName(templateFolder, ContentModel.ASSOC_CONTAINS, COMPOSITION_TEMPLATE_FILE_NAME + RepoConsts.INITIAL_VERSION);
	}

	private NodeRef resolveTemplate(List<String> candidateNames) {
		List<NodeRef> templates = reportTplService.getSystemReportTemplates(ReportType.ExportSearch, PLMModel.TYPE_PRODUCT);
		NodeRef template = findTemplateByName(templates, candidateNames);
		if (template != null) {
			return template;
		}

		templates = reportTplService.getSystemReportTemplates(ReportType.ExportSearch, null);
		return findTemplateByName(templates, candidateNames);
	}

	private NodeRef findTemplateByName(List<NodeRef> templates, List<String> candidateNames) {
		for (NodeRef template : templates) {
			String templateName = getTemplateName(template);
			if (templateName == null) {
				continue;
			}
			for (String candidate : candidateNames) {
				if ((candidate != null) && (templateName.equals(candidate) || templateName.startsWith(candidate))) {
					return template;
				}
			}
		}
		return null;
	}

	private String buildAvailableTemplateNames(List<NodeRef> templates) {
		StringBuilder builder = new StringBuilder();
		for (NodeRef template : templates) {
			String templateName = getTemplateName(template);
			if (templateName == null) {
				continue;
			}
			if (!builder.isEmpty()) {
				builder.append(", ");
			}
			builder.append(templateName);
		}
		return builder.toString();
	}

	private String getTemplateName(NodeRef template) {
		return (String) nodeService.getProperty(template, ContentModel.PROP_NAME);
	}

	/**
	 * Test level filtering with AllLevel parameter
	 */
	@Test
	public void testMultiLevelExcelReportAllLevel() throws IOException {
		initTestReports();

		// Create test product with multi-level composition
		StandardChocolateEclairTestProduct testProduct = new StandardChocolateEclairTestProduct.Builder()
				.withAlfrescoRepository(alfrescoRepository)
				.withNodeService(nodeService)
				.withDestFolder(getTestFolderNodeRef())
				.withCompo(true)
				.withLabeling(true)
				.withIngredients(false)
				.build();

		FinishedProductData product = testProduct.createTestProduct();

		// Test V2 plugin with AllLevel parameter - use direct webscript call instead of ReportSearchService
		// Generate report using existing ExportCompositionPackaging template with AllLevel
		String query = "{\"datatype\":\"bcpg:finishedProduct\",\"prop_cm_name\":\"" + product.getName() + "\"}";
		String url = "/becpg/report/exportsearch/" + compositionPackagingReportTpl.toString().replace("://", "/") 
			+ "/Excel.xlsx?repo=true&term=&query=" + URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
			+ "&parameter=AllLevel";

		Response response = TestWebscriptExecuters.sendRequest(new GetRequest(url), 200, "admin");
		assertNotNull("Response should not be null", response);
		assertNotNull("Response content should not be null", response.getContentAsString());
		
		byte[] reportData = response.getContentAsByteArray();

		assertNotNull("Report data should not be null", reportData);
		assertTrue("Report data should not be empty", reportData.length > 0);

		// Parse Excel and verify structure
		try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(reportData))) {
			XSSFSheet sheet = workbook.getSheetAt(0);
			assertNotNull("Sheet should not be null", sheet);

			// Count all rows (excluding header)
			int rowCount = sheet.getLastRowNum();
			assertTrue("Should have multiple rows for multi-level composition", rowCount > 1);
		}
	}

	/**
	 * Test level filtering with MaxLevel2 parameter
	 */
	@Test
	public void testMultiLevelExcelReportMaxLevel2() throws IOException {
		initTestReports();

		// Create test product
		StandardChocolateEclairTestProduct testProduct = new StandardChocolateEclairTestProduct.Builder()
				.withAlfrescoRepository(alfrescoRepository)
				.withNodeService(nodeService)
				.withDestFolder(getTestFolderNodeRef())
				.withCompo(true)
				.withLabeling(true)
				.withIngredients(false)
				.build();

		FinishedProductData product = testProduct.createTestProduct();

		// Generate report using existing template with MaxLevel2 - use webscript call
		String query = "{\"datatype\":\"bcpg:finishedProduct\",\"prop_cm_name\":\"" + product.getName() + "\"}";
		String url = "/becpg/report/exportsearch/" + compositionPackagingReportTpl.toString().replace("://", "/") 
			+ "/Excel.xlsx?repo=true&term=&query=" + URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
			+ "&parameter=MaxLevel2";

		Response response = TestWebscriptExecuters.sendRequest(new GetRequest(url), 200, "admin");
		byte[] reportData = response.getContentAsByteArray();

		assertNotNull("Report data should not be null", reportData);
		assertTrue("Report data should not be empty", reportData.length > 0);

		// Parse Excel and verify structure
		try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(reportData))) {
			XSSFSheet sheet = workbook.getSheetAt(0);
			assertNotNull("Sheet should not be null", sheet);
			assertTrue("Should have data rows", sheet.getLastRowNum() > 0);
		}
	}

	/**
	 * A composition sheet set to OnlyLevel2 holds the components of the semi-finished products, and
	 * them alone: the export goes through level 1 to reach them but leaves its rows out (#38193).
	 */
	@Test
	public void testMultiLevelExcelReportOnlyLevel2() throws IOException {
		initTestReports();

		CompositionTree tree = createProductWithSemiFinishedComponent();
		NodeRef onlyLevel2Tpl = createTemplateWithCompositionParameter(ONLY_LEVEL_2);

		List<String> componentNames = exportCompositionComponentNames(PLMModel.TYPE_FINISHEDPRODUCT, onlyLevel2Tpl, tree.product());

		assertEquals(List.of(LEVEL_2_COMPONENT_A, LEVEL_2_COMPONENT_B), componentNames);
	}

	/**
	 * A where-used composition sheet set to wUsedOnlyLevel2 holds the lines using the product that uses
	 * the exported raw material, and them alone (#38193).
	 */
	@Test
	public void testMultiLevelExcelReportWUsedOnlyLevel2() throws IOException {
		initTestReports();

		CompositionTree tree = createProductWithSemiFinishedComponent();
		NodeRef wUsedOnlyLevel2Tpl = createTemplateWithCompositionParameter(WUSED_ONLY_LEVEL_2);

		List<String> componentNames = exportCompositionComponentNames(PLMModel.TYPE_RAWMATERIAL, wUsedOnlyLevel2Tpl, tree.level2Component());

		assertEquals(List.of(LEVEL_1_SEMI_FINISHED), componentNames);
	}

	/**
	 * The product of a test composition and one of the raw materials of its semi-finished component.
	 */
	private record CompositionTree(NodeRef product, NodeRef level2Component) {
	}

	/**
	 * Exports one entity with the given template and reads the component names of its composition sheet.
	 */
	private List<String> exportCompositionComponentNames(QName searchType, NodeRef templateNodeRef, NodeRef exportedNodeRef)
			throws IOException {
		byte[] reportData = inReadTx(() -> {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			exportSearchService.createReport(searchType, templateNodeRef, List.of(exportedNodeRef), ReportFormat.XLSX, out, new String[0]);
			return out.toByteArray();
		});

		try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(reportData))) {
			return readComponentNames(workbook.getSheetAt(COMPOSITION_SHEET_INDEX));
		}
	}

	private CompositionTree createProductWithSemiFinishedComponent() {
		return inWriteTx(() -> {
			NodeRef componentA = createRawMaterial(LEVEL_2_COMPONENT_A);
			NodeRef componentB = createRawMaterial(LEVEL_2_COMPONENT_B);
			NodeRef directComponent = createRawMaterial(LEVEL_1_COMPONENT);

			SemiFinishedProductData semiFinished = SemiFinishedProductData.build().withName(LEVEL_1_SEMI_FINISHED).withQty(1d)
					.withUnit(ProductUnit.kg);
			semiFinished.withCompoList(List.of(compoLine(componentA), compoLine(componentB)));
			NodeRef semiFinishedNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), semiFinished).getNodeRef();

			FinishedProductData product = FinishedProductData.build().withName("Product of a semi-finished component").withQty(1d)
					.withUnit(ProductUnit.kg);
			product.withCompoList(List.of(compoLine(semiFinishedNodeRef), compoLine(directComponent)));

			return new CompositionTree(alfrescoRepository.create(getTestFolderNodeRef(), product).getNodeRef(), componentA);
		});
	}

	private NodeRef createRawMaterial(String name) {
		RawMaterialData rawMaterial = RawMaterialData.build().withName(name).withQty(1d).withUnit(ProductUnit.kg);
		return alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial).getNodeRef();
	}

	private CompoListDataItem compoLine(NodeRef componentNodeRef) {
		return CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withProduct(componentNodeRef);
	}

	/**
	 * A copy of the composition and packaging template whose composition sheet carries the given level
	 * parameter: the parameter of a sheet comes from its TYPE row, not from the export request.
	 */
	private NodeRef createTemplateWithCompositionParameter(String parameter) throws IOException {
		byte[] templateContent = inReadTx(() -> {
			try (InputStream in = contentService.getReader(compositionPackagingReportTpl, ContentModel.PROP_CONTENT).getContentInputStream();
					XSSFWorkbook workbook = new XSSFWorkbook(in);
					ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				workbook.getSheetAt(COMPOSITION_SHEET_INDEX).getRow(TYPE_ROW_INDEX).getCell(TYPE_PARAMETER_CELL_INDEX).setCellValue(parameter);
				workbook.write(out);
				return out.toByteArray();
			}
		});

		return inWriteTx(() -> {
			NodeRef templateNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS,
					ContentModel.TYPE_CONTENT, Map.of(ContentModel.PROP_NAME, parameter + ".xlsx")).getChildRef();
			ContentWriter writer = contentService.getWriter(templateNodeRef, ContentModel.PROP_CONTENT, true);
			writer.setMimetype(MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET);
			writer.putContent(new ByteArrayInputStream(templateContent));
			return templateNodeRef;
		});
	}

	/**
	 * The component names of the rows holding values, in the order of the sheet.
	 */
	private List<String> readComponentNames(XSSFSheet sheet) {
		List<String> names = new ArrayList<>();
		for (int rownum = 0; rownum <= sheet.getLastRowNum(); rownum++) {
			Row row = sheet.getRow(rownum);

			if ((row != null) && (row.getCell(0) != null) && HEADER_VALUES.equals(row.getCell(0).getStringCellValue())) {
				names.add(readColumn(sheet, row, COMPONENT_NAME_COLUMN));
			}
		}
		return names;
	}

	/**
	 * Test packaging list with multi-level extraction
	 */
	@Test
	public void testMultiLevelPackagingReport() throws IOException {
		initTestReports();

		// Create test product with packaging
		StandardChocolateEclairTestProduct testProduct = new StandardChocolateEclairTestProduct.Builder()
				.withAlfrescoRepository(alfrescoRepository)
				.withNodeService(nodeService)
				.withDestFolder(getTestFolderNodeRef())
				.withCompo(true)
				.withLabeling(true)
				.withIngredients(false)
				.build();

		FinishedProductData product = inWriteTx(() -> {
			FinishedProductData createdProduct = testProduct.createTestProduct();
			
			// Add packaging to the finished product
			PackagingMaterialData boxMaterial = PackagingMaterialData.build().withName("Box Material");
			NodeRef boxMaterialNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), boxMaterial).getNodeRef();

			PackagingMaterialData palletMaterial = PackagingMaterialData.build().withName("Pallet Material");
			NodeRef palletMaterialNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), palletMaterial).getNodeRef();

			FinishedProductData productData = (FinishedProductData) alfrescoRepository.findOne(createdProduct.getNodeRef());
			productData.withPackagingList(List.of(
					PackagingListDataItem.build().withQty(1.0).withUnit(ProductUnit.P).withPkgLevel(PackagingLevel.Primary)
							.withProduct(boxMaterialNodeRef),
					PackagingListDataItem.build().withQty(1.0).withUnit(ProductUnit.P).withPkgLevel(PackagingLevel.Secondary)
							.withProduct(palletMaterialNodeRef)));

			alfrescoRepository.save(productData);
			return productData;
		});

		// Generate packaging report - use existing template and webscript call
		String query = "{\"datatype\":\"bcpg:finishedProduct\",\"prop_cm_name\":\"" + product.getName() + "\"}";
		String url = "/becpg/report/exportsearch/" + compositionPackagingReportTpl.toString().replace("://", "/") 
			+ "/Excel.xlsx?repo=true&term=&query=" + URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
			+ "&parameter=AllLevel";

		Response response = TestWebscriptExecuters.sendRequest(new GetRequest(url), 200, "admin");
		byte[] reportData = response.getContentAsByteArray();

		assertNotNull("Packaging report data should not be null", reportData);
		assertTrue("Packaging report should contain data", reportData.length > 0);

		// Parse Excel and verify packaging structure
		try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(reportData))) {
			XSSFSheet sheet = workbook.getSheetAt(0);
			assertTrue("Should have packaging rows", sheet.getLastRowNum() > 1);
		}
	}

	/**
	 * Test process list with multi-level extraction
	 */
	@Test
	public void testMultiLevelProcessReport() throws IOException {
		initTestReports();

		// Create test product with process
		StandardChocolateEclairTestProduct testProduct = new StandardChocolateEclairTestProduct.Builder()
				.withAlfrescoRepository(alfrescoRepository)
				.withNodeService(nodeService)
				.withDestFolder(getTestFolderNodeRef())
				.withCompo(true)
				.withLabeling(true)
				.withIngredients(false)
				.withProcess(true)
				.build();

		FinishedProductData product = testProduct.createTestProduct();

		// Generate process report - use existing template and webscript call
		String query = "{\"datatype\":\"bcpg:finishedProduct\",\"prop_cm_name\":\"" + product.getName() + "\"}";
		String url = "/becpg/report/exportsearch/" + compositionPackagingReportTpl.toString().replace("://", "/") 
			+ "/Excel.xlsx?repo=true&term=&query=" + URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
			+ "&parameter=AllLevel";

		Response response = TestWebscriptExecuters.sendRequest(new GetRequest(url), 200, "admin");
		byte[] reportData = response.getContentAsByteArray();

		assertNotNull("Process report data should not be null", reportData);
		assertTrue("Process report should contain data (length=" + response.getContentLength() + ", type=" + response.getContentType() + ")",
			response.getContentLength() > 0);
		assertTrue("Process report should contain data", reportData.length > 0);

		// Parse Excel and verify process structure
		try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(reportData))) {
			XSSFSheet sheet = workbook.getSheetAt(0);
			assertTrue("Should have process rows", sheet.getLastRowNum() > 1);
		}
	}

	/**
	 * Test IncludeEmpty parameter with empty lists
	 */
	@Test
	public void testMultiLevelExcelReportIncludeEmpty() throws IOException {
		initTestReports();

		// Create test product with no composition, packaging, or process lists
		FinishedProductData product = (FinishedProductData) inWriteTx(() -> {
			FinishedProductData finishedProduct = FinishedProductData.build()
					.withName("Empty Product Test")
					.withQty(100d)
					.withUnit(ProductUnit.kg);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct);
		});

		// Test with IncludeEmpty parameter - should create a row even though lists are empty
		String query = "{\"datatype\":\"bcpg:finishedProduct\",\"prop_cm_name\":\"" + product.getName() + "\"}";
		String url = "/becpg/report/exportsearch/" + compositionPackagingReportTpl.toString().replace("://", "/") 
			+ "/Excel.xlsx?repo=true&term=&query=" + URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
			+ "&parameter=AllLevelIncludeEmpty";

		Response response = TestWebscriptExecuters.sendRequest(new GetRequest(url), 200, "admin");
		byte[] reportData = response.getContentAsByteArray();

		assertNotNull("Report data should not be null", reportData);
		assertTrue("Report data should not be empty", reportData.length > 0);

		// Parse Excel and verify structure - should have at least one data row despite empty lists
		try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(reportData))) {
			XSSFSheet sheet = workbook.getSheetAt(0);
			assertNotNull("Sheet should not be null", sheet);
			
			// Should have header + at least one data row
			int rowCount = sheet.getLastRowNum();
			assertTrue("Should have at least one data row when IncludeEmpty is true (row count: " + rowCount + ")", rowCount >= 1);
		}
	}

	/**
	 * Test MaxLevel2IncludeEmpty parameter
	 */
	@Test
	public void testMultiLevelExcelReportMaxLevel2IncludeEmpty() throws IOException {
		initTestReports();

		// Create test product with no lists
		FinishedProductData product = (FinishedProductData) inWriteTx(() -> {
			FinishedProductData finishedProduct = FinishedProductData.build()
					.withName("Empty Product Test 2")
					.withQty(50d)
					.withUnit(ProductUnit.L);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct);
		});

		// Test with MaxLevel2IncludeEmpty parameter
		String query = "{\"datatype\":\"bcpg:finishedProduct\",\"prop_cm_name\":\"" + product.getName() + "\"}";
		String url = "/becpg/report/exportsearch/" + compositionPackagingReportTpl.toString().replace("://", "/") 
			+ "/Excel.xlsx?repo=true&term=&query=" + URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
			+ "&parameter=MaxLevel2IncludeEmpty";

		Response response = TestWebscriptExecuters.sendRequest(new GetRequest(url), 200, "admin");
		byte[] reportData = response.getContentAsByteArray();

		assertNotNull("Report data should not be null", reportData);
		assertTrue("Report data should not be empty", reportData.length > 0);

		// Parse Excel and verify structure
		try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(reportData))) {
			XSSFSheet sheet = workbook.getSheetAt(0);
			assertNotNull("Sheet should not be null", sheet);
			
			// Should have header + at least one data row
			int rowCount = sheet.getLastRowNum();
			assertTrue("Should have at least one data row when MaxLevel2IncludeEmpty is true (row count: " + rowCount + ")", rowCount >= 1);
		}
	}

	/**
	 * Test wUsedAllLevelIncludeEmpty parameter
	 */
	@Test
	public void testMultiLevelExcelReportWUsedIncludeEmpty() throws IOException {
		initTestReports();

		// Create a semi-finished product that is NOT used anywhere
		NodeRef sfNodeRef = inWriteTx(() -> {
			FinishedProductData product = FinishedProductData.build()
					.withName("Unused SF")
					.withQty(100d)
					.withUnit(ProductUnit.kg);
			NodeRef created = alfrescoRepository.create(getTestFolderNodeRef(), product).getNodeRef();
			nodeService.setType(created, PLMModel.TYPE_SEMIFINISHEDPRODUCT);
			return created;
		});

		// Use ExportSearchService directly to avoid Solr indexing dependency
		byte[] reportData = inReadTx(() -> {
			java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
			exportSearchService.createReport(PLMModel.TYPE_SEMIFINISHEDPRODUCT, compositionPackagingReportTpl,
					List.of(sfNodeRef), ReportFormat.XLSX, baos, new String[] { "wUsedAllLevelIncludeEmpty" });
			return baos.toByteArray();
		});

		assertNotNull("Report data should not be null", reportData);
		assertTrue("Report data should not be empty", reportData.length > 0);

		// Parse Excel and verify structure
		try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(reportData))) {
			XSSFSheet sheet = workbook.getSheetAt(0);
			assertNotNull("Sheet should not be null", sheet);

			// Count data rows (those starting with VALUES)
			int dataRowCount = 0;
			for (int i = 0; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				if (row != null && row.getCell(0) != null && "VALUES".equals(row.getCell(0).getStringCellValue())) {
					dataRowCount++;
				}
			}
			assertTrue("Should have at least one data row when wUsedAllLevelIncludeEmpty is true (data row count: " + dataRowCount + ")", dataRowCount >= 1);
		}
	}

	/**
	 * Test wUsedAllLevel and IncludeEmpty passed as separate parameters (ticket #34545)
	 */
	@Test
	public void testMultiLevelExcelReportWUsedSeparateIncludeEmpty() throws IOException {
		initTestReports();

		NodeRef sfNodeRef = inWriteTx(() -> {
			FinishedProductData product = FinishedProductData.build()
					.withName("Unused SF Separate")
					.withQty(100d)
					.withUnit(ProductUnit.kg);
			NodeRef created = alfrescoRepository.create(getTestFolderNodeRef(), product).getNodeRef();
			nodeService.setType(created, PLMModel.TYPE_SEMIFINISHEDPRODUCT);
			return created;
		});

		byte[] reportData = inReadTx(() -> {
			java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
			exportSearchService.createReport(PLMModel.TYPE_SEMIFINISHEDPRODUCT, compositionPackagingReportTpl,
					List.of(sfNodeRef), ReportFormat.XLSX, baos, new String[] { "wUsedAllLevel", "IncludeEmpty" });
			return baos.toByteArray();
		});

		assertNotNull("Report data should not be null", reportData);
		assertTrue("Report data should not be empty", reportData.length > 0);

		try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(reportData))) {
			XSSFSheet sheet = workbook.getSheetAt(0);
			assertNotNull("Sheet should not be null", sheet);

			int dataRowCount = 0;
			for (int i = 0; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				if (row != null && row.getCell(0) != null && "VALUES".equals(row.getCell(0).getStringCellValue())) {
					dataRowCount++;
				}
			}
			assertTrue("Should have at least one data row when wUsedAllLevel and IncludeEmpty are separate parameters (data row count: " + dataRowCount + ")", dataRowCount >= 1);
		}
	}

	/**
	 * Every column reading the component of a composition line is exported, not only the first one:
	 * the template asks for its name, its ERP code and its beCPG code, which are three extractions of
	 * the same node.
	 */
	@Test
	public void testEveryComponentColumnIsExported() throws IOException {
		initTestReports();

		NodeRef productNodeRef = createProductWithNamedComponent();

		byte[] reportData = inReadTx(() -> {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			exportSearchService.createReport(PLMModel.TYPE_FINISHEDPRODUCT, compositionPackagingReportTpl, List.of(productNodeRef), ReportFormat.XLSX,
					out, new String[] { "AllLevel" });
			return out.toByteArray();
		});

		try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(reportData))) {
			XSSFSheet sheet = workbook.getSheetAt(COMPOSITION_SHEET_INDEX);

			Row componentRow = findFirstDataRow(sheet);
			assertNotNull("The composition sheet should hold a component row", componentRow);

			assertEquals(COMPONENT_NAME, readColumn(sheet, componentRow, COMPONENT_NAME_COLUMN));
			assertEquals(COMPONENT_ERP_CODE, readColumn(sheet, componentRow, COMPONENT_ERP_CODE_COLUMN));
		}
	}

	private NodeRef createProductWithNamedComponent() {
		return inWriteTx(() -> {
			RawMaterialData component = RawMaterialData.build().withName(COMPONENT_NAME).withQty(1d).withUnit(ProductUnit.kg);
			component.setErpCode(COMPONENT_ERP_CODE);
			NodeRef componentNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), component).getNodeRef();

			FinishedProductData product = FinishedProductData.build().withName("Product of a named component").withQty(1d)
					.withUnit(ProductUnit.kg);
			product.withCompoList(List.of(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withProduct(componentNodeRef)));

			return alfrescoRepository.create(getTestFolderNodeRef(), product).getNodeRef();
		});
	}

	/**
	 * The first row holding values, ie. the first component of the exported composition.
	 */
	private Row findFirstDataRow(XSSFSheet sheet) {
		for (int rownum = 0; rownum <= sheet.getLastRowNum(); rownum++) {
			Row row = sheet.getRow(rownum);

			if ((row != null) && (row.getCell(0) != null) && HEADER_VALUES.equals(row.getCell(0).getStringCellValue())) {
				return row;
			}
		}
		return null;
	}

	/**
	 * The value the given row holds in the column the template declares under the given name.
	 */
	private String readColumn(XSSFSheet sheet, Row row, String columnName) {
		Row headerRow = sheet.getRow(COLUMNS_ROW_INDEX);

		for (int cellnum = 0; cellnum < headerRow.getLastCellNum(); cellnum++) {
			Cell headerCell = headerRow.getCell(cellnum);

			if ((headerCell != null) && columnName.equals(headerCell.getStringCellValue())) {
				Cell cell = row.getCell(cellnum);
				return cell != null ? cell.getStringCellValue() : null;
			}
		}

		throw new AssertionError("The template should declare the column " + columnName);
	}

}
