package fr.becpg.test.repo.product.formulation;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

import fr.becpg.repo.product.formulation.labeling.QtyFormater;

/**
 * Unit tests for {@link QtyFormater} percentage formatting, covering the
 * floating-point precision issue reported in #34948 where a value mathematically
 * equal to 29% was displayed as 28% with {@link RoundingMode#DOWN}.
 *
 * @author matthieu
 */
public class QtyFormaterTest {

	private static QtyFormater buildFormater(String pattern, RoundingMode roundingMode) {
		DecimalFormat decimalFormat = new DecimalFormat(pattern, new DecimalFormatSymbols(Locale.ENGLISH));
		decimalFormat.setRoundingMode(roundingMode);
		return new QtyFormater(decimalFormat, roundingMode, null);
	}

	@Test
	public void testDownRoundingKeepsExactInteger() {
		// 0.29 as a double is 0.28999999999999998 ; DOWN must still yield 29%
		QtyFormater formater = buildFormater("#%", RoundingMode.DOWN);
		Assert.assertEquals("29%", formater.format(0.29d));
	}

	@Test
	public void testDownRoundingFromTypicalComputation() {
		// 72.5% * 40% = 29% (#34948 client scenario)
		QtyFormater formater = buildFormater("#%", RoundingMode.DOWN);
		Assert.assertEquals("29%", formater.format(0.725d * 0.40d));
	}

	@Test
	public void testDownRoundingStillTruncatesRealDecimals() {
		// A genuine 28.9% must keep rounding down to 28% with DOWN
		QtyFormater formater = buildFormater("#%", RoundingMode.DOWN);
		Assert.assertEquals("28%", formater.format(0.289d));
	}

	@Test
	public void testDownRoundingPreservesDecimalPrecision() {
		QtyFormater formater = buildFormater("#.##%", RoundingMode.DOWN);
		Assert.assertEquals("29%", formater.format(0.29d));
		Assert.assertEquals("28.99%", formater.format(0.2899d));
	}

	@Test
	public void testHalfUpRoundingUnaffected() {
		QtyFormater formater = buildFormater("#%", RoundingMode.HALF_UP);
		Assert.assertEquals("29%", formater.format(0.29d));
		Assert.assertEquals("29%", formater.format(0.286d));
	}
}
