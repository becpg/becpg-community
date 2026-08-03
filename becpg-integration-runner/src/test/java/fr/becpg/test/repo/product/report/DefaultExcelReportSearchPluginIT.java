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
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
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
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.report.search.ExportSearchService;
import fr.becpg.report.client.ReportFormat;
import fr.becpg.test.PLMBaseTestCase;

/**
 * Integration test for DefaultExcelReportSearchPlugin, which handles every datalist that has no dedicated plugin.
 *
 * <p>Covers ticket #35715: the <code>IncludeEmpty</code> parameter used to be honoured only by the multi-level plugin
 * (bcpg:compoList, bcpg:packagingList, mpm:processList), so entities without the exported list were silently dropped
 * from every other sheet.</p>
 *
 * @author matthieu
 */
public class DefaultExcelReportSearchPluginIT extends PLMBaseTestCase {

	private static final String HEADER_VALUES = "VALUES";

	private static final String XLSX_EXTENSION = ".xlsx";

	private static final String EXPORT_PRODUCTS_FOLDER_PATH = "/app:company_home/cm:System/cm:Reports/cm:ExportSearch/cm:ExportProducts";

	private static final int ALLERGEN_LIST_SHEET_INDEX = 1;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private ExportSearchService exportSearchService;

	/**
	 * A product without allergen list is exported as an empty row when IncludeEmpty is set.
	 */
	@Test
	public void testIncludeEmptyExportsEntityWithoutList() throws IOException {

		NodeRef reportTpl = findAllergensReportTpl();
		NodeRef productNodeRef = createProductWithoutAllergenList("Empty allergen list product");

		byte[] reportData = exportProduct(reportTpl, productNodeRef, new String[] { "IncludeEmpty" });

		assertEquals("Product without allergen list should be exported when IncludeEmpty is set", 1, countDataRows(reportData));
	}

	/**
	 * The same product is not exported at all when IncludeEmpty is not set.
	 */
	@Test
	public void testProductWithoutListIsSkippedByDefault() throws IOException {

		NodeRef reportTpl = findAllergensReportTpl();
		NodeRef productNodeRef = createProductWithoutAllergenList("Skipped allergen list product");

		byte[] reportData = exportProduct(reportTpl, productNodeRef, null);

		assertEquals("Product without allergen list should not be exported without IncludeEmpty", 0, countDataRows(reportData));
	}

	private NodeRef createProductWithoutAllergenList(String name) {
		return inWriteTx(() -> {
			FinishedProductData product = FinishedProductData.build().withName(name).withQty(100d).withUnit(ProductUnit.kg);
			return alfrescoRepository.create(getTestFolderNodeRef(), product).getNodeRef();
		});
	}

	private byte[] exportProduct(NodeRef reportTpl, NodeRef productNodeRef, String[] parameters) {
		return inReadTx(() -> {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			exportSearchService.createReport(PLMModel.TYPE_FINISHEDPRODUCT, reportTpl, List.of(productNodeRef), ReportFormat.XLSX, out, parameters);
			return out.toByteArray();
		});
	}

	/**
	 * Counts the rows written by the plugin on the allergen list sheet, ie. the rows flagged as VALUES.
	 */
	private int countDataRows(byte[] reportData) throws IOException {

		assertNotNull("Report data should not be null", reportData);
		assertTrue("Report data should not be empty", reportData.length > 0);

		try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(reportData))) {
			XSSFSheet sheet = workbook.getSheetAt(ALLERGEN_LIST_SHEET_INDEX);
			assertNotNull("Allergen list sheet should not be null", sheet);

			int dataRowCount = 0;
			for (int rownum = 0; rownum <= sheet.getLastRowNum(); rownum++) {
				Row row = sheet.getRow(rownum);
				Cell cell = row != null ? row.getCell(0) : null;
				if ((cell != null) && HEADER_VALUES.equals(cell.getStringCellValue())) {
					dataRowCount++;
				}
			}
			return dataRowCount;
		}
	}

	private NodeRef findAllergensReportTpl() {

		NodeRef templateFolder = repoService.getFolderByPath(EXPORT_PRODUCTS_FOLDER_PATH);
		assertNotNull("Export products template folder should exist", templateFolder);

		String templateName = TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_REPORTS_EXPORT_SEARCH_ALLERGENS) + XLSX_EXTENSION;

		NodeRef template = nodeService.getChildByName(templateFolder, ContentModel.ASSOC_CONTAINS, templateName);
		if (template == null) {
			template = nodeService.getChildByName(templateFolder, ContentModel.ASSOC_CONTAINS, templateName + RepoConsts.INITIAL_VERSION);
		}

		assertNotNull("ExportAllergens template should exist (looked for " + templateName + ")", template);
		return template;
	}

}
