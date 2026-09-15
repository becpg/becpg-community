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
package fr.becpg.test.repo.helper;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import org.alfresco.util.TempFileProvider;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.junit.Test;

import fr.becpg.repo.helper.ExcelStreamHelper;

/**
 * Covers the packaging of a streamed workbook: a spreadsheet application reading the local header of
 * a zip entry declares a workbook corrupted when that header does not carry the size of the entry,
 * as the writer of a streamed workbook leaves it.
 *
 * @author matthieu
 */
public class ExcelStreamHelperTest {

	private static final int ROW_COUNT = 500;

	private static final String SHEET_NAME = "Data";

	@Test
	public void testWrittenWorkbookDeclaresItsEntrySizes() throws IOException {

		File target = TempFileProvider.createTempFile("test-streamed-workbook", ".xlsx");

		writeStreamedWorkbook(target);

		try (ZipFile workbookFile = ZipFile.builder().setFile(target).get()) {

			Enumeration<ZipArchiveEntry> entries = workbookFile.getEntries();

			Assert.assertTrue("The workbook should hold at least one entry", entries.hasMoreElements());

			while (entries.hasMoreElements()) {
				ZipArchiveEntry entry = entries.nextElement();

				Assert.assertFalse("The size of " + entry.getName() + " should not be deferred to a data descriptor",
						entry.getGeneralPurposeBit().usesDataDescriptor());
			}
		}
	}

	@Test
	public void testWrittenWorkbookKeepsEveryRow() throws IOException, InvalidFormatException {

		File target = TempFileProvider.createTempFile("test-streamed-workbook", ".xlsx");

		writeStreamedWorkbook(target);

		try (XSSFWorkbook written = new XSSFWorkbook(target)) {
			Sheet sheet = written.getSheet(SHEET_NAME);

			Assert.assertNotNull("The written workbook should hold the exported sheet", sheet);
			Assert.assertEquals(ROW_COUNT - 1, sheet.getLastRowNum());
			Assert.assertEquals("row " + (ROW_COUNT - 1), sheet.getRow(ROW_COUNT - 1).getCell(0).getStringCellValue());
		}
	}

	private void writeStreamedWorkbook(File target) throws IOException {

		try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
			Sheet sheet = workbook.createSheet(SHEET_NAME);

			for (int rownum = 0; rownum < ROW_COUNT; rownum++) {
				sheet.createRow(rownum).createCell(0).setCellValue("row " + rownum);
			}

			ExcelStreamHelper.writeWorkbook(workbook, target);
		}
	}

}
