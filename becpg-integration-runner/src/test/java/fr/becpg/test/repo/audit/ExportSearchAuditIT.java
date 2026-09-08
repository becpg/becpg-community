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

import java.util.List;
import java.util.UUID;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.repo.audit.model.AuditQuery;
import fr.becpg.repo.audit.model.AuditScope;
import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.plugin.AuditPlugin;
import fr.becpg.repo.audit.plugin.impl.ExportSearchAuditPlugin;
import fr.becpg.repo.audit.service.DatabaseAuditScope;
import fr.becpg.repo.audit.service.DatabaseAuditService;
import fr.becpg.test.RepoBaseTestCase;

/**
 * Checks the two phase recording of the export search audit against the audit database: an export
 * is traced before it runs, and the entry it left behind is replaced, not duplicated, when it ends.
 *
 * @author matthieu
 */
public class ExportSearchAuditIT extends RepoBaseTestCase {

	private static final String NODE_REF_PREFIX = "workspace://SpacesStore/";

	private static final int RESULTS_SIZE = 1000;

	@Autowired
	private ExportSearchAuditPlugin exportSearchAuditPlugin;

	@Autowired
	private DatabaseAuditService databaseAuditService;

	/** The template the entries of a single test are read back on, unique to keep the tests independent. */
	private String templateNodeRef;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		templateNodeRef = NODE_REF_PREFIX + UUID.randomUUID();
	}

	@Test
	public void testSynchronousExportReplacesItsStartedEntry() {
		AuditScope auditScope = startExportAudit(false, null);
		auditScope.recordStart();

		assertEquals("The export is traced before it renders", 1, listAuditEntries().size());

		auditScope.close();

		List<JSONObject> auditEntries = listAuditEntries();

		assertEquals("The started entry is replaced, not duplicated", 1, auditEntries.size());
		assertEquals("The export is completed", "true", auditEntries.get(0).get(AuditPlugin.IS_COMPLETED).toString());
		assertTrue("The export end is dated", auditEntries.get(0).has(AuditPlugin.COMPLETED_AT));
		assertTrue("The export duration is measured", Integer.parseInt(auditEntries.get(0).get(AuditPlugin.DURATION).toString()) >= 0);
	}

	@Test
	public void testAsynchronousExportIsCompletedByTheThreadRunningIt() {
		String downloadNodeRef = NODE_REF_PREFIX + UUID.randomUUID();

		AuditScope auditScope = startExportAudit(true, downloadNodeRef);
		auditScope.deferCompletion();
		auditScope.close();

		List<JSONObject> startedEntries = listAuditEntries();

		assertEquals("The request only traces the start of the export", 1, startedEntries.size());
		assertEquals("The export is not completed by the thread requesting it", "false",
				startedEntries.get(0).get(AuditPlugin.IS_COMPLETED).toString());
		assertFalse("A running export has no end date", startedEntries.get(0).has(AuditPlugin.COMPLETED_AT));
		assertEquals("The export is asynchronous", "true", startedEntries.get(0).get(ExportSearchAuditPlugin.ASYNC).toString());

		beCPGAuditService.completeAuditEntry(AuditType.EXPORT_SEARCH, ExportSearchAuditPlugin.DOWNLOAD_NODE_REF, downloadNodeRef);

		List<JSONObject> completedEntries = listAuditEntries();

		assertEquals("The started entry is replaced, not duplicated", 1, completedEntries.size());
		assertEquals("The export is completed", "true", completedEntries.get(0).get(AuditPlugin.IS_COMPLETED).toString());
		assertTrue("The export end is dated", completedEntries.get(0).has(AuditPlugin.COMPLETED_AT));
		assertTrue("The export duration is measured", Integer.parseInt(completedEntries.get(0).get(AuditPlugin.DURATION).toString()) >= 0);
		assertEquals("The requester is kept", AuthenticationUtil.getFullyAuthenticatedUser(),
				completedEntries.get(0).get(ExportSearchAuditPlugin.USERNAME).toString());
		assertEquals("The exported volume is kept", String.valueOf(RESULTS_SIZE),
				completedEntries.get(0).get(ExportSearchAuditPlugin.RESULTS_SIZE).toString());
	}

	@Test
	public void testInterruptedExportKeepsTheEntryOfItsRequester() {
		// the scope is built by hand and never closed, as when the server goes down while exporting
		DatabaseAuditScope auditScope = new DatabaseAuditScope(exportSearchAuditPlugin, databaseAuditService);
		auditScope.start();
		auditScope.putAttribute(ExportSearchAuditPlugin.USERNAME, AuthenticationUtil.getFullyAuthenticatedUser());
		auditScope.putAttribute(ExportSearchAuditPlugin.TEMPLATE, templateNodeRef);
		auditScope.putAttribute(ExportSearchAuditPlugin.RESULTS_SIZE, RESULTS_SIZE);
		auditScope.recordStart();

		List<JSONObject> auditEntries = listAuditEntries();

		assertEquals("An export that never ends is traced all the same", 1, auditEntries.size());
		assertEquals("It stays flagged as not completed", "false", auditEntries.get(0).get(AuditPlugin.IS_COMPLETED).toString());
		assertFalse("It has no end date", auditEntries.get(0).has(AuditPlugin.COMPLETED_AT));
		assertEquals("Its requester is known", AuthenticationUtil.getFullyAuthenticatedUser(),
				auditEntries.get(0).get(ExportSearchAuditPlugin.USERNAME).toString());
		assertEquals("The volume it was asked for is known", String.valueOf(RESULTS_SIZE),
				auditEntries.get(0).get(ExportSearchAuditPlugin.RESULTS_SIZE).toString());
	}

	@Test
	public void testStartedExportSurvivesTheRollbackOfItsRequest() {
		try {
			transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
				AuditScope auditScope = startExportAudit(false, null);
				auditScope.recordStart();

				throw new IllegalStateException("The export failed after having been traced");
			}, false, true);

			fail("The transaction of the export was expected to fail");
		} catch (IllegalStateException e) {
			// the request of the export is rolled back, its trace is not
		}

		assertEquals("The trace of an export does not depend on the outcome of that export", 1, listAuditEntries().size());
	}

	@Test
	public void testConcurrentExportsKeepTheirOwnEntry() {
		AuditScope firstExport = startExportAudit(false, null);
		AuditScope secondExport = startExportAudit(false, null);

		firstExport.recordStart();
		secondExport.recordStart();

		assertEquals("Two exports started at the same time are traced apart", 2, listAuditEntries().size());

		secondExport.close();
		firstExport.close();

		List<JSONObject> auditEntries = listAuditEntries();

		assertEquals("Completing an export leaves the entry of the other one alone", 2, auditEntries.size());
		assertEquals("The first export is completed", "true", auditEntries.get(0).get(AuditPlugin.IS_COMPLETED).toString());
		assertEquals("The second export is completed", "true", auditEntries.get(1).get(AuditPlugin.IS_COMPLETED).toString());
	}

	private AuditScope startExportAudit(boolean async, String downloadNodeRef) {
		AuditScope auditScope = beCPGAuditService.startAudit(AuditType.EXPORT_SEARCH, getClass(), "export search audit test");

		auditScope.putAttribute(ExportSearchAuditPlugin.FILENAME, "Export.xlsx");
		auditScope.putAttribute(ExportSearchAuditPlugin.USERNAME, AuthenticationUtil.getFullyAuthenticatedUser());
		auditScope.putAttribute(ExportSearchAuditPlugin.TEMPLATE, templateNodeRef);
		auditScope.putAttribute(ExportSearchAuditPlugin.RESULTS_SIZE, RESULTS_SIZE);
		auditScope.putAttribute(ExportSearchAuditPlugin.ASYNC, async);

		if (downloadNodeRef != null) {
			auditScope.putAttribute(ExportSearchAuditPlugin.DOWNLOAD_NODE_REF, downloadNodeRef);
		}

		return auditScope;
	}

	private List<JSONObject> listAuditEntries() {
		AuditQuery auditQuery = AuditQuery.createQuery().filter(ExportSearchAuditPlugin.TEMPLATE, templateNodeRef)
				.sortBy(AuditPlugin.STARTED_AT);

		return inReadTx(() -> beCPGAuditService.listAuditEntries(AuditType.EXPORT_SEARCH, auditQuery));
	}

}
