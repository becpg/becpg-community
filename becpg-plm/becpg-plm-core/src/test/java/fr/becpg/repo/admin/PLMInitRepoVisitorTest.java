/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * beCPG is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

/**
 * Languages the default report templates are generated in (#37522).
 */
public class PLMInitRepoVisitorTest {

	private static final String CLIENT_SHEET = "beCPG/birt/document/product/default/ProductReport.rptdesign";
	private static final String PRODUCTION_SHEET = "beCPG/birt/document/product/default/ProductReport_Prod.rptdesign";
	private static final String ARABIC = "ar";

	@Test
	public void theClientSheetIsGeneratedInArabic() {
		assertTrue(PLMInitRepoVisitor.getReportTemplateLocales(CLIENT_SHEET).contains(ARABIC));
	}

	@Test
	public void theClientSheetKeepsEveryOtherLanguage() {
		List<String> otherTemplateLocales = PLMInitRepoVisitor.getReportTemplateLocales(PRODUCTION_SHEET);
		List<String> clientSheetLocales = PLMInitRepoVisitor.getReportTemplateLocales(CLIENT_SHEET);

		assertTrue(clientSheetLocales.containsAll(otherTemplateLocales));
		assertEquals(otherTemplateLocales.size() + 1, clientSheetLocales.size());
	}

	@Test
	public void anUntranslatedTemplateIsNotGeneratedInArabic() {
		assertFalse(PLMInitRepoVisitor.getReportTemplateLocales(PRODUCTION_SHEET).contains(ARABIC));
	}

	@Test
	public void askingForTheClientSheetLeavesTheSharedLanguagesUntouched() {
		PLMInitRepoVisitor.getReportTemplateLocales(CLIENT_SHEET);

		assertFalse(PLMInitRepoVisitor.getReportTemplateLocales(PRODUCTION_SHEET).contains(ARABIC));
	}
}
