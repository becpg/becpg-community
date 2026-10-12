/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.product.report;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.helper.json.JsonHelper;
import fr.becpg.repo.report.pdf.ReportPdfAggregator.AggregateReportConfig;
import fr.becpg.repo.report.pdf.ReportPdfAggregator.AnnexConfig;

/**
 * Unit tests of the product information file definition: the annexes it embeds.
 */
public class PIFReportDefinitionTest {

	private static final String PIF_DEFINITION = "/beCPG/birt/document/product/default/PIFReport.agg.json";
	private static final String SCOPE_COMPO_CHILDREN = "COMPO_CHILDREN";
	private static final String REPORT_KIND_RESOLVER_PREFIX = "rep:reportTplAssoc|rep:reportKinds=";

	@Test
	public void testRawMaterialAnnexEmbedsTheCosmeticRawMaterialSheets() throws IOException {
		AnnexConfig rawMaterialAnnex = findAnnexOfScope(readPifDefinition(), SCOPE_COMPO_CHILDREN);

		assertEquals(REPORT_KIND_RESOLVER_PREFIX + PlmRepoConsts.REPORT_KIND_COSMETIC_SUPPLIER_SHEET, rawMaterialAnnex.getAnnexIdResolver());
	}

	private AggregateReportConfig readPifDefinition() throws IOException {
		try (InputStream in = PIFReportDefinitionTest.class.getResourceAsStream(PIF_DEFINITION)) {
			assertNotNull("The PIF definition should be on the classpath", in);
			return JsonHelper.MAPPER.readValue(in, AggregateReportConfig.class);
		}
	}

	private AnnexConfig findAnnexOfScope(AggregateReportConfig config, String scope) {
		for (AnnexConfig annex : config.getAnnexes()) {
			if (scope.equals(annex.getScope())) {
				return annex;
			}
		}
		throw new AssertionError("The PIF definition should hold an annex of scope " + scope);
	}
}
