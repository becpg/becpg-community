package fr.becpg.repo.ecm.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Unit tests of the Excel export of the change units.
 */
public class ChangeUnitExcelDataListOutputPluginTest {

	private static final String SCORE_JSON = "{\"global\":48,\"newCount\":0,\"requirements\":[]}";

	private final ChangeUnitExcelDataListOutputPlugin plugin = new ChangeUnitExcelDataListOutputPlugin();

	@Test
	public void scoreIsExportedAsText() throws IOException {
		Map<String, Object> item = new HashMap<>();
		item.put(ChangeUnitExcelDataListOutputPlugin.ENTITY_SCORE_FIELD, SCORE_JSON);

		plugin.decorate(List.of(item));

		assertFalse(item.get(ChangeUnitExcelDataListOutputPlugin.ENTITY_SCORE_FIELD).toString().startsWith("{"));
	}

	@Test
	public void valueThatIsNotJsonIsKept() {
		assertEquals("not json", ChangeUnitExcelDataListOutputPlugin.toText("not json"));
	}

	@Test
	public void otherColumnsAreUntouched() throws IOException {
		Map<String, Object> item = new HashMap<>();
		item.put("prop_ecm_culRevision", "Major");

		plugin.decorate(List.of(item));

		assertEquals("Major", item.get("prop_ecm_culRevision"));
	}
}
