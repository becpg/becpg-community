package fr.becpg.repo.product.report;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for {@link ExportLevelFilter}.
 */
public class ExportLevelFilterTest {

	private static final ExportLevelFilter ALL_LEVELS = new ExportLevelFilter(ExportLevelFilter.UNLIMITED, false);
	private static final ExportLevelFilter MAX_LEVEL_2 = new ExportLevelFilter(2, false);
	private static final ExportLevelFilter ONLY_LEVEL_2 = new ExportLevelFilter(2, true);

	@Test
	public void testAllLevelsWritesEveryLevel() {
		assertTrue(ALL_LEVELS.isRowWritten(1));
		assertTrue(ALL_LEVELS.isRowWritten(5));
		assertFalse(ALL_LEVELS.isBeyondMaxLevel(20));
	}

	@Test
	public void testMaxLevelWritesEveryLevelUpToTheMaximum() {
		assertTrue(MAX_LEVEL_2.isRowWritten(1));
		assertTrue(MAX_LEVEL_2.isRowWritten(2));
		assertFalse(MAX_LEVEL_2.isRowWritten(3));
	}

	@Test
	public void testOnlyLevelWritesTheMaximumLevelAlone() {
		assertFalse(ONLY_LEVEL_2.isRowWritten(1));
		assertTrue(ONLY_LEVEL_2.isRowWritten(2));
		assertFalse(ONLY_LEVEL_2.isRowWritten(3));
	}

	@Test
	public void testOnlyLevelStillGoesDownToTheMaximumLevel() {
		assertFalse(ONLY_LEVEL_2.isBeyondMaxLevel(1));
		assertFalse(ONLY_LEVEL_2.isBeyondMaxLevel(2));
		assertTrue(ONLY_LEVEL_2.isBeyondMaxLevel(3));
	}

	@Test
	public void testOnlyLevel1WritesTheFirstLevelAlone() {
		ExportLevelFilter onlyLevel1 = new ExportLevelFilter(1, true);

		assertTrue(onlyLevel1.isRowWritten(1));
		assertTrue(onlyLevel1.isBeyondMaxLevel(2));
	}
}
