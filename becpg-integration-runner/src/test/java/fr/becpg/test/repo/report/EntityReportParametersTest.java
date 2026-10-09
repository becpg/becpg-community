/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.report;

import org.junit.Assert;
import org.junit.Test;

import fr.becpg.repo.report.entity.EntityReportParameters;

/**
 * Unit tests for {@link fr.becpg.repo.report.entity.EntityReportParameters} JSON parsing.
 *
 * @author matthieu
 */
public class EntityReportParametersTest {

	private static final String EXTRACT_PACKAGING_MATERIALS = "extractPackagingMaterials";

	@Test
	public void testEmptyPreferences() {
		EntityReportParameters reportParameters = EntityReportParameters.createFromJSON("{\"prefs\":{}}");

		Assert.assertNotNull(reportParameters);
		Assert.assertTrue("Empty prefs must not fail", reportParameters.getPreferences().isEmpty());
	}

	@Test
	public void testPreferencesAreParsed() {
		EntityReportParameters reportParameters = EntityReportParameters
				.createFromJSON("{\"prefs\":{\"" + EXTRACT_PACKAGING_MATERIALS + "\":\"true\",\"maxCompoListLevelToExtract\":3}}");

		Assert.assertEquals("true", reportParameters.getPreferences().get(EXTRACT_PACKAGING_MATERIALS));
		Assert.assertEquals("3", reportParameters.getPreferences().get("maxCompoListLevelToExtract"));
	}

	@Test
	public void testBooleanPreferenceIsParsedAsString() {
		EntityReportParameters reportParameters = EntityReportParameters
				.createFromJSON("{\"prefs\":{\"" + EXTRACT_PACKAGING_MATERIALS + "\":true}}");

		Assert.assertEquals("true", reportParameters.getPreferences().get(EXTRACT_PACKAGING_MATERIALS));
	}
}
