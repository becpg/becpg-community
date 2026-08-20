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
package fr.becpg.repo.report.search.impl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.poi.ss.usermodel.Workbook;

import fr.becpg.repo.helper.ExcelHelper.ExcelCellStyles;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;

/**
 * <p>Holds what an excel export builds once and reuses on every row of a sheet: the data extracted
 * for a node, and the styles of the workbook.</p>
 *
 * An extraction entry belongs to a node <b>and</b> to the fields it was extracted with: two columns
 * pointing at the same association rarely ask for the same fields, and serving the entry of the
 * first one to the second would leave that second column empty. Entries are copied in and out, so
 * that a row completing its own data - with the columns of its entity or the result of its formulas
 * - does not write them into the entry the next rows will read.
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ExcelExportCache {

	private static final String MAX_ENTRIES_PROPERTY = "becpg.excel.cache.maxEntries";

	private static final int DEFAULT_MAX_ENTRIES = 10000;

	private final Map<String, Map<String, Object>> entries;

	private ExcelCellStyles cellStyles;

	/**
	 * <p>Constructor for ExcelExportCache.</p>
	 */
	public ExcelExportCache() {

		final int maxEntries = Integer.getInteger(MAX_ENTRIES_PROPERTY, DEFAULT_MAX_ENTRIES);

		this.entries = new LinkedHashMap<>(16, 0.75f, true) {

			private static final long serialVersionUID = 1L;

			@Override
			protected boolean removeEldestEntry(Map.Entry<String, Map<String, Object>> eldest) {
				return size() > maxEntries;
			}
		};
	}

	/**
	 * <p>The data extracted for the given node with the given fields, or null when it has not been
	 * extracted yet.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param fields a {@link java.util.List} object
	 * @return a {@link java.util.Map} object
	 */
	public Map<String, Object> get(NodeRef nodeRef, List<AttributeExtractorStructure> fields) {

		Map<String, Object> extracted = entries.get(key(nodeRef, fields));

		return extracted != null ? new HashMap<>(extracted) : null;
	}

	/**
	 * <p>Keep the data extracted for the given node with the given fields.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param fields a {@link java.util.List} object
	 * @param extracted a {@link java.util.Map} object
	 */
	public void put(NodeRef nodeRef, List<AttributeExtractorStructure> fields, Map<String, Object> extracted) {
		entries.put(key(nodeRef, fields), new HashMap<>(extracted));
	}

	/**
	 * <p>Forget everything extracted so far.</p>
	 */
	public void clear() {
		entries.clear();
	}

	/**
	 * <p>The styles of the given workbook, created on the first call.</p>
	 *
	 * A workbook only holds so many cell styles: creating a set of them for every exported entity
	 * fills that budget with duplicates, and gives a file no spreadsheet application opens quickly.
	 *
	 * @param workbook a {@link org.apache.poi.ss.usermodel.Workbook} object
	 * @return a {@link fr.becpg.repo.helper.ExcelHelper.ExcelCellStyles} object
	 */
	public ExcelCellStyles getCellStyles(Workbook workbook) {
		if (cellStyles == null) {
			cellStyles = new ExcelCellStyles(workbook);
		}
		return cellStyles;
	}

	private static String key(NodeRef nodeRef, List<AttributeExtractorStructure> fields) {

		StringBuilder key = new StringBuilder(nodeRef.getId());

		for (AttributeExtractorStructure field : fields) {
			key.append('|').append(field.getStructureKey());
		}

		return key.toString();
	}

}
