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
package fr.becpg.test.repo.audit;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.repo.audit.model.AuditPage;
import fr.becpg.repo.audit.model.AuditQuery;
import fr.becpg.repo.audit.model.AuditScope;
import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.plugin.AuditPlugin;
import fr.becpg.repo.audit.plugin.impl.FormulationAuditPlugin;
import fr.becpg.repo.audit.service.impl.DatabaseAuditServiceImpl;
import fr.becpg.test.RepoBaseTestCase;

/**
 * Checks that reading the audit table by keyset paging returns what reading it in one shot
 * returns.
 *
 * The audit query of Alfresco carries no 'limit', so a page is read window of entry identifiers
 * after window of entry identifiers. Both readings have to agree, in both database orders, and a
 * resumed scan has to carry on exactly where the previous page stopped.
 *
 * @author matthieu
 */
public class AuditKeysetPagingIT extends RepoBaseTestCase {

	private static final int RECORDED_ENTRIES = 12;

	private static final int PAGE_SIZE = 5;

	@Autowired
	private DatabaseAuditServiceImpl databaseAuditServiceImpl;

	private int initialMaxScannedWindows;

	/** The chain the entries of a single test are read back on, unique to keep the tests independent. */
	private String chainId;

	/** {@inheritDoc} */
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		initialMaxScannedWindows = databaseAuditServiceImpl.getMaxScannedWindows();
		chainId = UUID.randomUUID().toString();
		recordFormulationEntries();
	}

	/**
	 * <p>restoreWindowBudget.</p>
	 *
	 * The window budget is a property of a shared service: a test lowering it puts it back, so
	 * that the tests stay independent.
	 */
	@After
	public void restoreWindowBudget() {
		databaseAuditServiceImpl.setMaxScannedWindows(initialMaxScannedWindows);
	}

	@Test
	public void testPagedReadMatchesSingleShotRead() {
		assertEntryIdsMatchSingleShotRead(false);
	}

	@Test
	public void testPagedReadMatchesSingleShotReadInAscendingOrder() {
		assertEntryIdsMatchSingleShotRead(true);
	}

	@Test
	public void testResumedScanCarriesOnWithoutGapNorDuplicate() {
		List<Long> singleShotIds = entryIds(listAuditEntries(2 * PAGE_SIZE, false));

		AuditPage firstPage = listAuditPage(PAGE_SIZE, false, null);
		AuditPage secondPage = listAuditPage(PAGE_SIZE, false, firstPage.nextStartAfterId());

		List<Long> pagedIds = new ArrayList<>(entryIds(firstPage.entries()));
		pagedIds.addAll(entryIds(secondPage.entries()));

		assertEquals("Two pages hold what a single read of both pages holds", singleShotIds, pagedIds);
	}

	@Test
	public void testScanReachingTheEndOffersNoResume() {
		AuditPage page = listAuditPage(RECORDED_ENTRIES + 1, false, null);

		assertEquals("The whole chain is read", RECORDED_ENTRIES, page.entries().size());
		assertNull("A scan having reached the end of the application offers no resume", page.nextStartAfterId());
		assertFalse("A scan having reached the end of the application is not interrupted", page.scanInterrupted());
	}

	@Test
	public void testScanSpendingItsWindowBudgetIsResumable() {
		databaseAuditServiceImpl.setMaxScannedWindows(1);

		AuditPage page = listAuditPage(RECORDED_ENTRIES + 1, false, null);

		assertTrue("A scan stopping on its window budget is reported as interrupted", page.scanInterrupted());
		assertNotNull("An interrupted scan hands back where to carry on", page.nextStartAfterId());

		databaseAuditServiceImpl.setMaxScannedWindows(initialMaxScannedWindows);
		AuditPage resumed = listAuditPage(RECORDED_ENTRIES + 1, false, page.nextStartAfterId());

		assertEquals("Resuming an interrupted scan reads the entries it had not reached", RECORDED_ENTRIES,
				page.entries().size() + resumed.entries().size());
	}

	private void assertEntryIdsMatchSingleShotRead(boolean dbAscending) {
		List<Long> singleShotIds = entryIds(listAuditEntries(PAGE_SIZE, dbAscending));
		List<Long> pagedIds = entryIds(listAuditPage(PAGE_SIZE, dbAscending, null).entries());

		assertEquals("A page holds what a single read holds", singleShotIds, pagedIds);
	}

	private void recordFormulationEntries() {
		for (int entry = 0; entry < RECORDED_ENTRIES; entry++) {
			final String entityName = "Keyset paging " + entry;
			inWriteTx(() -> {
				try (AuditScope auditScope = beCPGAuditService.startAudit(AuditType.FORMULATION, getClass(), "keyset paging test")) {
					auditScope.putAttribute(FormulationAuditPlugin.CHAIN_ID, chainId);
					auditScope.putAttribute(FormulationAuditPlugin.ENTITY_NAME, entityName);
				}
				return null;
			});
		}
	}

	private List<JSONObject> listAuditEntries(int maxResults, boolean dbAscending) {
		return inReadTx(() -> beCPGAuditService.listAuditEntries(AuditType.FORMULATION, chainQuery(maxResults, dbAscending, null)));
	}

	private AuditPage listAuditPage(int maxResults, boolean dbAscending, Long startAfterId) {
		return inReadTx(() -> beCPGAuditService.listAuditPage(AuditType.FORMULATION, chainQuery(maxResults, dbAscending, startAfterId)));
	}

	private AuditQuery chainQuery(int maxResults, boolean dbAscending, Long startAfterId) {
		return AuditQuery.createQuery().filter(FormulationAuditPlugin.CHAIN_ID, chainId).maxResults(maxResults).dbAsc(dbAscending)
				.startAfterId(startAfterId);
	}

	private List<Long> entryIds(List<JSONObject> entries) {
		List<Long> ids = new ArrayList<>(entries.size());
		for (JSONObject entry : entries) {
			ids.add(entry.getLong(AuditPlugin.ID));
		}
		return ids;
	}

}
