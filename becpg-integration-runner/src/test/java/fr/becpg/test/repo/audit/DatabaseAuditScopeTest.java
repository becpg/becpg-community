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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import fr.becpg.repo.audit.model.AuditDataType;
import fr.becpg.repo.audit.plugin.AuditPlugin;
import fr.becpg.repo.audit.plugin.DatabaseAuditPlugin;
import fr.becpg.repo.audit.service.DatabaseAuditScope;
import fr.becpg.repo.audit.service.DatabaseAuditService;

/**
 * Checks how many entries a scope records, and in which state, depending on the way the audited
 * operation ends.
 *
 * @author matthieu
 */
public class DatabaseAuditScopeTest {

	private static final String FILENAME = "filename";

	private DatabaseAuditService databaseAuditService;

	private List<Map<String, Serializable>> recordedEntries;

	private List<Boolean> replacements;

	@Before
	public void setUp() {
		recordedEntries = new ArrayList<>();
		replacements = new ArrayList<>();
		databaseAuditService = mock(DatabaseAuditService.class);

		doAnswer(invocation -> {
			recordedEntries.add(new HashMap<>(invocation.getArgument(1)));
			replacements.add(invocation.getArgument(2));
			return 0;
		}).when(databaseAuditService).recordAuditEntry(any(), any(), anyBoolean());
	}

	@Test
	public void testOperationRecordedAtTheEndWritesASingleEntry() {
		DatabaseAuditScope auditScope = createScope(false);
		auditScope.start();
		auditScope.putAttribute(FILENAME, "Export.xlsx");
		auditScope.recordStart();
		auditScope.close();

		assertEquals("A plugin recording at the end ignores the start", 1, recordedEntries.size());
		assertFalse("There is no started entry to replace", replacements.get(0));
		assertNull("Completion is not tracked", recordedEntries.get(0).get(AuditPlugin.IS_COMPLETED));
	}

	@Test
	public void testStartedOperationIsRecordedBeforeItEnds() {
		DatabaseAuditScope auditScope = createScope(true);
		auditScope.start();
		auditScope.putAttribute(FILENAME, "Export.xlsx");
		auditScope.recordStart();

		assertEquals("The operation is traced as soon as it starts", 1, recordedEntries.size());
		assertEquals("It is not completed yet", Boolean.FALSE, recordedEntries.get(0).get(AuditPlugin.IS_COMPLETED));
		assertNull("It has no end date yet", recordedEntries.get(0).get(AuditPlugin.COMPLETED_AT));
		assertEquals("It carries the context of the request", "Export.xlsx", recordedEntries.get(0).get(FILENAME));
	}

	@Test
	public void testCompletedOperationReplacesItsStartedEntry() {
		DatabaseAuditScope auditScope = createScope(true);
		auditScope.start();
		auditScope.recordStart();
		auditScope.close();

		assertEquals("The started entry is written, then the completed one", 2, recordedEntries.size());
		assertFalse("The started entry replaces nothing", replacements.get(0));
		assertTrue("The completed entry replaces the started one", replacements.get(1));
		assertEquals("The operation is completed", Boolean.TRUE, recordedEntries.get(1).get(AuditPlugin.IS_COMPLETED));
		assertNotNull("The operation is dated", recordedEntries.get(1).get(AuditPlugin.COMPLETED_AT));
		assertNotNull("The operation is measured", recordedEntries.get(1).get(AuditPlugin.DURATION));
		assertEquals("Both entries share the same identifier", recordedEntries.get(0).get(AuditPlugin.ID),
				recordedEntries.get(1).get(AuditPlugin.ID));
	}

	@Test
	public void testInterruptedOperationKeepsItsStartedEntry() {
		DatabaseAuditScope auditScope = createScope(true);
		auditScope.start();
		auditScope.putAttribute(FILENAME, "Export.xlsx");
		auditScope.recordStart();

		// the scope is never closed, as when the server goes down while running the operation

		assertEquals("The requester of an interrupted operation stays traced", 1, recordedEntries.size());
		assertEquals("An interrupted operation is never completed", Boolean.FALSE, recordedEntries.get(0).get(AuditPlugin.IS_COMPLETED));
	}

	@Test
	public void testDeferredOperationOnlyRecordsItsStartedEntry() {
		DatabaseAuditScope auditScope = createScope(true);
		auditScope.start();
		auditScope.deferCompletion();
		auditScope.close();

		assertEquals("The thread requesting the operation only traces its start", 1, recordedEntries.size());
		assertEquals("The completion is left to the thread running the operation", Boolean.FALSE,
				recordedEntries.get(0).get(AuditPlugin.IS_COMPLETED));
		assertNull("The duration of the request is not the one of the operation", recordedEntries.get(0).get(AuditPlugin.DURATION));
	}

	@Test
	public void testDeferredOperationRecordsItsStartedEntryOnlyOnce() {
		DatabaseAuditScope auditScope = createScope(true);
		auditScope.start();
		auditScope.recordStart();
		auditScope.deferCompletion();
		auditScope.close();

		assertEquals("The started entry is not written twice", 1, recordedEntries.size());
	}

	@Test
	public void testDisabledScopeRecordsNothing() {
		DatabaseAuditScope auditScope = createScope(true);
		auditScope.start();
		auditScope.disableAuditRecord();
		auditScope.recordStart();
		auditScope.close();

		assertTrue("A disabled scope writes nothing at all", recordedEntries.isEmpty());
	}

	@Test
	public void testConcurrentOperationsGetTheirOwnIdentifier() {
		DatabaseAuditScope firstScope = createScope(true);
		DatabaseAuditScope secondScope = createScope(true);

		firstScope.start();
		secondScope.start();

		firstScope.recordStart();
		secondScope.recordStart();

		assertNotEquals("Two operations started at the same time must not share an identifier",
				recordedEntries.get(0).get(AuditPlugin.ID), recordedEntries.get(1).get(AuditPlugin.ID));
	}

	private DatabaseAuditScope createScope(boolean recordOnStart) {
		Map<String, AuditDataType> keyMap = new HashMap<>();
		keyMap.put(AuditPlugin.STARTED_AT, AuditDataType.DATE);
		keyMap.put(AuditPlugin.COMPLETED_AT, AuditDataType.DATE);
		keyMap.put(AuditPlugin.DURATION, AuditDataType.INTEGER);
		keyMap.put(FILENAME, AuditDataType.STRING);

		DatabaseAuditPlugin auditPlugin = mock(DatabaseAuditPlugin.class);
		when(auditPlugin.getKeyMap()).thenReturn(keyMap);
		when(auditPlugin.isRecordOnStart()).thenReturn(recordOnStart);

		return new DatabaseAuditScope(auditPlugin, databaseAuditService);
	}

}
