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
package fr.becpg.repo.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Enumeration;

import org.alfresco.util.TempFileProvider;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * <p>Writes a streamed workbook to a file every spreadsheet application can open.</p>
 *
 * A streamed workbook is written to a plain output stream, so its writer does not know the size of
 * an entry before writing it and leaves that size out of the local header of the zip entry, to
 * publish it in a trailing data descriptor. Spreadsheet applications reading the local headers -
 * LibreOffice among them - then declare the file corrupted and offer to repair it, although its
 * content is complete. The entries are therefore copied, still compressed, into a file the zip
 * writer can seek back into to complete each header.
 *
 * @author matthieu
 * @version $Id: $Id
 */
public final class ExcelStreamHelper {

	private static final Log logger = LogFactory.getLog(ExcelStreamHelper.class);

	private static final String TEMP_FILE_PREFIX = "becpg-streamed-workbook";

	private static final String XLSX_EXTENSION = ".xlsx";

	private ExcelStreamHelper() {
		// Helper class
	}

	/**
	 * <p>Write the given streamed workbook to the target file.</p>
	 *
	 * @param workbook a {@link org.apache.poi.xssf.streaming.SXSSFWorkbook} object
	 * @param target a {@link java.io.File} object
	 * @throws java.io.IOException if the workbook cannot be written
	 */
	public static void writeWorkbook(SXSSFWorkbook workbook, File target) throws IOException {

		File streamedFile = TempFileProvider.createTempFile(TEMP_FILE_PREFIX, XLSX_EXTENSION);

		try {
			try (OutputStream outputStream = new FileOutputStream(streamedFile)) {
				workbook.write(outputStream);
			}

			repack(streamedFile, target);
		} finally {
			deleteQuietly(streamedFile);
		}
	}

	/**
	 * Copy every entry of a zip file, as compressed as it already is, into a seekable one.
	 *
	 * @param source a {@link java.io.File} object
	 * @param target a {@link java.io.File} object
	 * @throws java.io.IOException if an entry cannot be copied
	 */
	private static void repack(File source, File target) throws IOException {

		try (ZipFile sourceZip = ZipFile.builder().setFile(source).get(); ZipArchiveOutputStream targetZip = new ZipArchiveOutputStream(target)) {

			Enumeration<ZipArchiveEntry> entries = sourceZip.getEntriesInPhysicalOrder();

			while (entries.hasMoreElements()) {
				ZipArchiveEntry entry = entries.nextElement();

				try (InputStream rawContent = sourceZip.getRawInputStream(entry)) {
					targetZip.addRawArchiveEntry(entry, rawContent);
				}
			}
		}
	}

	private static void deleteQuietly(File file) {
		try {
			Files.deleteIfExists(file.toPath());
		} catch (IOException e) {
			logger.warn("Cannot delete temporary workbook: " + file.getAbsolutePath(), e);
		}
	}

}
