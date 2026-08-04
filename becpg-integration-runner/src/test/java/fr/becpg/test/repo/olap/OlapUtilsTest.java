/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.olap;

import java.util.Locale;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.repo.olap.OlapUtils;

/**
 * Unit tests for {@link OlapUtils#convert(String)}.
 *
 * <p>
 * These exist because the defect they cover was invisible: Saiku returns the
 * cell's <b>display</b> value, so a count of 1 148 arrives as {@code "1.148"} and
 * the previous {@code Double.parseDouble} turned it into 1.148 — a figure that is
 * wrong by a factor of a thousand and looks perfectly plausible on a chart.
 * Measured on dev.becpg.fr, a refusal rate derived from those counts read 97 %
 * where the truth was near 3.5 %.
 * </p>
 *
 * @author matthieu
 */
public class OlapUtilsTest {

	@After
	public void resetLocale() {
		I18NUtil.setLocale(Locale.getDefault());
	}

	@Test
	public void plainIntegersAndDecimals() {
		Assert.assertEquals(Long.valueOf(305L), OlapUtils.convert("305"));
		Assert.assertEquals(Long.valueOf(-42L), OlapUtils.convert("-42"));
		Assert.assertEquals(Long.valueOf(0L), OlapUtils.convert(""));
		Assert.assertEquals(Long.valueOf(0L), OlapUtils.convert(null));
	}

	/** Both separators present: the rightmost one carries the decimals. */
	@Test
	public void mixedSeparatorsAreUnambiguous() {
		Assert.assertEquals(Double.valueOf(1234.56d), OlapUtils.convert("1.234,56"));
		Assert.assertEquals(Double.valueOf(1234.56d), OlapUtils.convert("1,234.56"));
		Assert.assertEquals(Double.valueOf(1234567.89d), OlapUtils.convert("1.234.567,89"));
	}

	/** The same separator twice can only be grouping. */
	@Test
	public void repeatedSeparatorIsGrouping() {
		Assert.assertEquals(Long.valueOf(1234567L), OlapUtils.convert("1.234.567"));
		Assert.assertEquals(Long.valueOf(1234567L), OlapUtils.convert("1,234,567"));
	}

	/** Anything but exactly three digits after the separator is a decimal part. */
	@Test
	public void nonTripleFractionIsDecimal() {
		Assert.assertEquals(Double.valueOf(19.4d), OlapUtils.convert("19.4"));
		Assert.assertEquals(Double.valueOf(0.25d), OlapUtils.convert("0,25"));
		Assert.assertEquals(Double.valueOf(27.9481d), OlapUtils.convert("27.9481"));
	}

	/** Spaces — including the non-breaking ones — are grouping, never decimal. */
	@Test
	public void spacesAreGrouping() {
		Assert.assertEquals(Long.valueOf(1148L), OlapUtils.convert("1 148"));
		Assert.assertEquals(Long.valueOf(1148L), OlapUtils.convert("1 148"));
		Assert.assertEquals(Long.valueOf(1148L), OlapUtils.convert("1 148"));
		Assert.assertEquals(Double.valueOf(1234.56d), OlapUtils.convert("1 234,56"));
	}

	/**
	 * The undecidable shape: a single separator followed by exactly three digits.
	 *
	 * <p>
	 * It is read as a decimal, and that is <b>not</b> claimed to be right — it is
	 * the historical behaviour, kept so nobody is handed a different wrong answer.
	 * The locale cannot arbitrate: on dev.becpg.fr the same cellset carries
	 * {@code "1.148"} for a count of 1 148 and {@code "19.468"} for an average of
	 * 19.468. This test pins the choice so that the day the raw Saiku value is
	 * plumbed through, the change shows up here rather than silently in a chart.
	 * </p>
	 */
	@Test
	public void tripleFractionStaysUndecided() {
		for (Locale locale : new Locale[] { Locale.GERMANY, Locale.US, Locale.FRANCE }) {
			I18NUtil.setLocale(locale);
			Assert.assertEquals("locale " + locale, Double.valueOf(1.148d), OlapUtils.convert("1.148"));
			Assert.assertEquals("locale " + locale, Double.valueOf(19.468d), OlapUtils.convert("19.468"));
		}
	}

	/**
	 * What the fix actually buys, on the values it can decide. Each string below
	 * was produced by the dev cube or is the shape Mondrian emits for one.
	 */
	@Test
	public void formattedCountsAreNoLongerDivided() {
		// Before: 1.234567 (parseDouble on "1.234.567" throws, so it fell back to
		// returning the string) — now an integer.
		Assert.assertEquals(Long.valueOf(1234567L), OlapUtils.convert("1.234.567"));
		// Before: the string itself, because parseDouble chokes on two separators.
		Assert.assertEquals(Double.valueOf(2047.5d), OlapUtils.convert("2.047,5"));
		// Before: the string, a space being no number at all.
		Assert.assertEquals(Long.valueOf(2047L), OlapUtils.convert("2 047"));
	}

	/** A member caption is not a number and must survive untouched. */
	@Test
	public void nonNumericValuesArePreserved() {
		Assert.assertEquals("Refusé", OlapUtils.convert("Refusé"));
		Assert.assertEquals("null", OlapUtils.convert("null"));
		Assert.assertEquals("W41/2011", OlapUtils.convert("W41/2011"));
	}
}
