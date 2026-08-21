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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.constraints.AllergenType;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.report.search.impl.ExcelReportSearchRenderer;
import fr.becpg.report.client.ReportFormat;
import fr.becpg.test.PLMBaseTestCase;

/**
 * Covers the export search columns filtering a datalist on a property reached through one of its
 * associations, as in
 * <code>bcpg:allergenList[bcpg:allergenListAllergen#bcpg:allergenCode == "AW"]_bcpg:allergenListQtyPerc</code>.
 *
 * @author matthieu
 */
public class FilteredDatalistColumnExportSearchIT extends PLMBaseTestCase {

	private static final String TYPE_HEADER = "TYPE";

	private static final String COLUMNS_HEADER = "COLUMNS";

	private static final String LABEL_HEADER = "#";

	private static final String NAME_COLUMN = "cm:name";

	private static final String TEMPLATE_NAME = "FilteredDatalistColumn.xlsx";

	private static final String GLUTEN_CODE = "IT-GLU";

	private static final String MILK_CODE = "IT-MLK";

	private static final double GLUTEN_QTY = 1.5d;

	private static final double MILK_QTY = 20d;

	private static final int FIRST_DATA_ROW = 3;

	private static final int GLUTEN_CELL = 2;

	private static final int MILK_CELL = 3;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private ExcelReportSearchRenderer excelReportSearchRenderer;

	@Test
	public void testFilteredColumnExtractsItsOwnListItem() throws IOException {

		NodeRef glutenNodeRef = createAllergen("IT Gluten", GLUTEN_CODE);
		NodeRef milkNodeRef = createAllergen("IT Milk", MILK_CODE);

		NodeRef productNodeRef = createProduct(glutenNodeRef, milkNodeRef);
		NodeRef templateNodeRef = createTemplate();

		byte[] report = renderReport(templateNodeRef, productNodeRef);

		try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(report))) {
			Row row = workbook.getSheetAt(0).getRow(FIRST_DATA_ROW);

			assertNotNull("The product should have been exported", row);
			assertNotNull("The gluten column should hold the quantity of the gluten line", row.getCell(GLUTEN_CELL));
			assertNotNull("The milk column should hold the quantity of the milk line", row.getCell(MILK_CELL));
			assertEquals(GLUTEN_QTY, row.getCell(GLUTEN_CELL).getNumericCellValue(), 0.001d);
			assertEquals(MILK_QTY, row.getCell(MILK_CELL).getNumericCellValue(), 0.001d);
		}
	}

	private NodeRef createAllergen(String name, String code) {
		return inWriteTx(() -> {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, name);
			properties.put(PLMModel.PROP_ALLERGEN_CODE, code);
			properties.put(PLMModel.PROP_ALLERGEN_TYPE, AllergenType.Major.toString());

			return nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQNameWithValidLocalName(NamespaceService.CONTENT_MODEL_1_0_URI, name), PLMModel.TYPE_ALLERGEN, properties)
					.getChildRef();
		});
	}

	private NodeRef createProduct(NodeRef glutenNodeRef, NodeRef milkNodeRef) {
		return inWriteTx(() -> {
			FinishedProductData product = new FinishedProductData();
			product.setName("Filtered datalist column product");

			List<AllergenListDataItem> allergenList = new ArrayList<>();
			allergenList.add(AllergenListDataItem.build().withQtyPerc(GLUTEN_QTY).withVoluntary(true).withAllergen(glutenNodeRef).withIsManual(true));
			allergenList.add(AllergenListDataItem.build().withQtyPerc(MILK_QTY).withVoluntary(true).withAllergen(milkNodeRef).withIsManual(true));
			product.setAllergenList(allergenList);

			return alfrescoRepository.create(getTestFolderNodeRef(), product).getNodeRef();
		});
	}

	private NodeRef createTemplate() throws IOException {
		byte[] content = buildTemplateContent();

		return inWriteTx(() -> {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, TEMPLATE_NAME);

			NodeRef templateNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQNameWithValidLocalName(NamespaceService.CONTENT_MODEL_1_0_URI, TEMPLATE_NAME), ReportModel.TYPE_REPORT_TPL,
					properties).getChildRef();

			ContentWriter writer = contentService.getWriter(templateNodeRef, ContentModel.PROP_CONTENT, true);
			writer.putContent(new ByteArrayInputStream(content));

			return templateNodeRef;
		});
	}

	private byte[] buildTemplateContent() throws IOException {
		try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			Sheet sheet = workbook.createSheet("Product");

			writeRow(sheet, 0, TYPE_HEADER, PLMModel.TYPE_FINISHEDPRODUCT.toPrefixString(namespaceService));
			writeRow(sheet, 1, COLUMNS_HEADER, NAME_COLUMN, filteredColumn(GLUTEN_CODE), filteredColumn(MILK_CODE));
			writeRow(sheet, 2, LABEL_HEADER, "Name", "Gluten", "Milk");

			workbook.write(outputStream);

			return outputStream.toByteArray();
		}
	}

	private String filteredColumn(String allergenCode) {
		return "bcpg:allergenList[bcpg:allergenListAllergen#bcpg:allergenCode == \"" + allergenCode + "\"]_bcpg:allergenListQtyPerc";
	}

	private void writeRow(Sheet sheet, int rownum, String... values) {
		Row row = sheet.createRow(rownum);
		for (int cellnum = 0; cellnum < values.length; cellnum++) {
			row.createCell(cellnum).setCellValue(values[cellnum]);
		}
	}

	private byte[] renderReport(NodeRef templateNodeRef, NodeRef productNodeRef) throws IOException {
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			inReadTx(() -> {
				excelReportSearchRenderer.renderReport(templateNodeRef, List.of(productNodeRef), ReportFormat.XLSX, outputStream);
				return null;
			});

			return outputStream.toByteArray();
		}
	}

}
