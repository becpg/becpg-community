/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.importer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.config.mapping.AbstractAttributeMapping;

/**
 * Unit tests of {@link ImportContext}: how the lines of a batch rolled back at commit are reported.
 */
public class ImportContextTest {

	private static final String[] IMPORTED_LINE = { "VALUES", "value" };

	private static final String IMPORT_SERVICE_BUNDLE = "alfresco.module.becpg-plm-core.messages.import-service";

	private ImportFileReader importFileReader;

	private ImportContext importContext;

	/**
	 * Builds a context reading its lines from a mocked import file.
	 *
	 * @throws ImporterException never, the reader is mocked
	 */
	@Before
	public void setUp() throws ImporterException {
		I18NUtil.registerResourceBundle(IMPORT_SERVICE_BUNDLE);

		importFileReader = mock(ImportFileReader.class);
		when(importFileReader.getLineAt(anyInt(), any())).thenReturn(IMPORTED_LINE);

		importContext = new ImportContext();
		importContext.setImportFileReader(importFileReader);
	}

	/**
	 * A line reported as successful is reported as an error once its batch is rolled back.
	 */
	@Test
	public void testSuccessfulLineReportedAsErrorWhenBatchNotSaved() {
		importContext.startBatch(0);
		importSuccessfulLine();

		importContext.markBatchNotSaved(0, new IllegalStateException("Failed to execute script"));

		verify(importFileReader).reportError(eq(0), any(), anyInt());
	}

	/**
	 * When a batch is rolled back, only its successful lines are reported in error: a line already
	 * in error keeps its own message.
	 */
	@Test
	public void testLineInErrorNotReportedTwiceWhenBatchNotSaved() {
		importContext.startBatch(0);
		importSuccessfulLine();
		importContext.nextLine();
		importContext.markCurrLineError(new ImporterException("Unknown characteristic"));

		importContext.markBatchNotSaved(0, new IllegalStateException("Failed to execute script"));

		verify(importFileReader).reportError(eq(0), any(), anyInt());
		verify(importFileReader, times(1)).reportError(eq(1), any(), anyInt());
		verify(importFileReader, times(1)).reportError(eq(1), eq("Unknown characteristic"), anyInt());
	}

	/**
	 * Each line of the rolled back batch is logged, so that the report is written with its errors.
	 */
	@Test
	public void testEachLineLoggedWhenBatchNotSaved() {
		importContext.startBatch(0);
		importSuccessfulLine();
		importSuccessfulLine();

		importContext.markBatchNotSaved(0, new IllegalStateException("Failed to execute script"));

		assertEquals(2, importContext.getLog().size());
	}

	/**
	 * When a batch is rolled back, only the lines of that batch are reported in error: the lines
	 * saved by the previous batch are kept.
	 */
	@Test
	public void testPreviousBatchLinesKeptWhenNextBatchNotSaved() {
		importContext.startBatch(0);
		importSuccessfulLine();

		importContext.startBatch(10);
		importSuccessfulLine();
		importContext.markBatchNotSaved(10, new IllegalStateException("Failed to execute script"));

		verify(importFileReader).reportError(eq(10), any(), anyInt());
		verify(importFileReader, never()).reportError(eq(0), any(), anyInt());
	}

	/**
	 * A failure without message, such as a wrapper around the real error, is reported with the
	 * message of its cause.
	 */
	@Test
	public void testCauseMessageReportedWhenFailureHasNoMessage() {
		importContext.startBatch(0);
		importSuccessfulLine();

		importContext.markBatchNotSaved(0, new RuntimeException(null, new IllegalStateException("Formulation failed")));

		verify(importFileReader).reportError(eq(0), contains("Formulation failed"), anyInt());
	}

	/**
	 * A failure without any message in its causes, such as a NullPointerException, is reported with
	 * its type.
	 */
	@Test
	public void testFailureTypeReportedWhenNoMessageAvailable() {
		importContext.startBatch(0);
		importSuccessfulLine();

		importContext.markBatchNotSaved(0, new NullPointerException());

		verify(importFileReader).reportError(eq(0), contains("NullPointerException"), anyInt());
	}

	/**
	 * When the transaction of a batch could not even start, the lines saved by the previous batch are
	 * kept, although the new batch was never started.
	 */
	@Test
	public void testPreviousBatchLinesKeptWhenNextBatchNeverStarted() {
		importContext.startBatch(0);
		importSuccessfulLine();

		importContext.markBatchNotSaved(10, new IllegalStateException("Could not open transaction"));

		verify(importFileReader, never()).reportError(eq(0), any(), anyInt());
		assertTrue(importContext.getLog().isEmpty());
	}

	/**
	 * Each line of a rolled back batch gets its message after its own columns, even when the batch
	 * spans sections with a different number of columns, so that no imported value is overwritten.
	 */
	@Test
	public void testLineReportedAfterItsOwnColumnsWhenBatchNotSaved() {
		importContext.startBatch(0);
		importContext.setColumns(columns(8));
		importSuccessfulLine();
		importContext.setColumns(columns(3));
		importSuccessfulLine();

		importContext.markBatchNotSaved(0, new IllegalStateException("Failed to execute script"));

		verify(importFileReader).reportError(eq(0), any(), eq(8));
		verify(importFileReader).reportError(eq(1), any(), eq(3));
	}

	private List<AbstractAttributeMapping> columns(int count) {
		return Collections.nCopies(count, mock(AbstractAttributeMapping.class));
	}

	private void importSuccessfulLine() {
		importContext.nextLine();
		importContext.markCurrLineSuccess();
	}
}
