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
package fr.becpg.test.repo.report;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.junit.Assert;
import org.junit.Test;

import fr.becpg.repo.helper.impl.AttributeExtractorField;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;
import fr.becpg.repo.report.search.impl.ExcelExportCache;

/**
 * Covers the cache of an excel search export: two columns reading the same node do not read the same
 * fields, and the entry of one is not an answer for the other.
 *
 * @author matthieu
 */
public class ExcelExportCacheTest {

	private static final NodeRef NODE_REF = new NodeRef("workspace://SpacesStore/2b1a5a41-3f21-4a1e-9a5a-413f218a1e00");

	private static final QName RAW_MATERIAL = QName.createQName("http://www.bcpg.fr/model/becpg/1.0", "rawMaterial");

	private static final QName COMPO_LIST = QName.createQName("http://www.bcpg.fr/model/becpg/1.0", "compoList");

	private final AttributeExtractorServiceImpl attributeExtractorService = new AttributeExtractorServiceImpl();

	@Test
	public void testEntryIsServedToTheSameFields() {

		ExcelExportCache cache = new ExcelExportCache();
		List<AttributeExtractorStructure> fields = fields("bcpg:code");

		cache.put(NODE_REF, RAW_MATERIAL, fields, values("prop_bcpg_code", "MP001"));

		Assert.assertEquals("MP001", cache.get(NODE_REF, RAW_MATERIAL, fields("bcpg:code")).get("prop_bcpg_code"));
	}

	@Test
	public void testEntryIsNotServedToOtherFields() {

		ExcelExportCache cache = new ExcelExportCache();

		cache.put(NODE_REF, RAW_MATERIAL, fields("bcpg:code"), values("prop_bcpg_code", "MP001"));

		Assert.assertNull("The fields of another column should not be answered with this entry", cache.get(NODE_REF, RAW_MATERIAL, fields("bcpg:erpCode")));
	}

	@Test
	public void testEntryIsNotChangedByItsReader() {

		ExcelExportCache cache = new ExcelExportCache();
		List<AttributeExtractorStructure> fields = fields("bcpg:code");

		cache.put(NODE_REF, RAW_MATERIAL, fields, values("prop_bcpg_code", "MP001"));
		cache.get(NODE_REF, RAW_MATERIAL, fields).put("prop_bcpg_code", "changed by the row");

		Assert.assertEquals("MP001", cache.get(NODE_REF, RAW_MATERIAL, fields).get("prop_bcpg_code"));
	}

	@Test
	public void testEntryIsNotServedToAnotherType() {

		ExcelExportCache cache = new ExcelExportCache();

		cache.put(NODE_REF, RAW_MATERIAL, fields("bcpg:code"), values("prop_bcpg_code", "MP001"));

		Assert.assertNull("The same node extracted as another type should not be answered with this entry",
				cache.get(NODE_REF, COMPO_LIST, fields("bcpg:code")));
	}

	private List<AttributeExtractorStructure> fields(String fieldName) {
		return List.of(attributeExtractorService.new AttributeExtractorStructure(new AttributeExtractorField(fieldName, null), fieldName));
	}

	private Map<String, Object> values(String key, Object value) {
		Map<String, Object> values = new HashMap<>();
		values.put(key, value);
		return values;
	}

}
