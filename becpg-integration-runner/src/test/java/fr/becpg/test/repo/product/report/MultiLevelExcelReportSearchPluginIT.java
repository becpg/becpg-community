/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;
import fr.becpg.repo.sample.StandardChocolateEclairTestProduct;
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
			if (builder.length() > 0) {
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
	 * Test level filtering with OnlyLevel3 parameter
	 */
	@Test
	public void testMultiLevelExcelReportOnlyLevel3() throws IOException {
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

		// Generate report using existing template with OnlyLevel3 - use webscript call
		String query = "{\"datatype\":\"bcpg:finishedProduct\",\"prop_cm_name\":\"" + product.getName() + "\"}";
		String url = "/becpg/report/exportsearch/" + compositionPackagingReportTpl.toString().replace("://", "/") 
			+ "/Excel.xlsx?repo=true&term=&query=" + URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
			+ "&parameter=OnlyLevel3";

		Response response = TestWebscriptExecuters.sendRequest(new GetRequest(url), 200, "admin");
		byte[] reportData = response.getContentAsByteArray();

		assertNotNull("Report data should not be null", reportData);
		assertTrue("Report data should not be empty", reportData.length > 0);

		// Parse Excel and verify structure
		try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(reportData))) {
			XSSFSheet sheet = workbook.getSheetAt(0);
			assertNotNull("Sheet should not be null", sheet);
		}
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

}
