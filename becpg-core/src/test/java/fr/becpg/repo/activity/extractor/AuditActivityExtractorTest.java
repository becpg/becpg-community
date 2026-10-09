package fr.becpg.repo.activity.extractor;

import java.util.List;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import fr.becpg.repo.audit.plugin.impl.ActivityAuditPlugin;

/**
 * Unit tests of the creation date range filter of {@link AuditActivityExtractor}.
 */
public class AuditActivityExtractorTest {

	private static final String SEPTEMBER_RANGE = "2026-09-01T00:00:00+02:00|2026-09-30T00:00:00+02:00";

	@Test
	public void keepsEntryCreatedWithinRange() {
		List<JSONObject> result = AuditActivityExtractor.filterByCreatedDateRange(List.of(entry("2026-09-15T10:00:00.000+02:00")),
				SEPTEMBER_RANGE);

		Assert.assertEquals(1, result.size());
	}

	@Test
	public void keepsEntryCreatedOnLastDayOfRange() {
		List<JSONObject> result = AuditActivityExtractor.filterByCreatedDateRange(List.of(entry("2026-09-30T15:00:00.000+02:00")),
				SEPTEMBER_RANGE);

		Assert.assertEquals(1, result.size());
	}

	@Test
	public void dropsEntriesCreatedOutsideRange() {
		List<JSONObject> entries = List.of(entry("2026-08-31T23:00:00.000+02:00"), entry("2026-10-01T00:00:00.000+02:00"));

		Assert.assertTrue(AuditActivityExtractor.filterByCreatedDateRange(entries, SEPTEMBER_RANGE).isEmpty());
	}

	@Test
	public void supportsOpenBounds() {
		List<JSONObject> entries = List.of(entry("2026-09-15T10:00:00.000+02:00"));

		Assert.assertEquals(1, AuditActivityExtractor.filterByCreatedDateRange(entries, "2026-09-01T00:00:00+02:00|").size());
		Assert.assertEquals(1, AuditActivityExtractor.filterByCreatedDateRange(entries, "|2026-09-30T00:00:00+02:00").size());
	}

	@Test
	public void skipsEntryWithoutCreationDate() {
		List<JSONObject> result = AuditActivityExtractor.filterByCreatedDateRange(List.of(new JSONObject()), SEPTEMBER_RANGE);

		Assert.assertTrue(result.isEmpty());
	}

	private static JSONObject entry(String created) {
		JSONObject entry = new JSONObject();
		entry.put(ActivityAuditPlugin.PROP_CM_CREATED, created);
		return entry;
	}
}
