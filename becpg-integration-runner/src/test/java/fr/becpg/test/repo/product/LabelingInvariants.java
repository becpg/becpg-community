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
package fr.becpg.test.repo.product;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;

/**
 * The invariants a rendered label must hold whatever the yield and the evaporation around it.
 *
 * The ingredient list has guarded its own for a long time while the labeling had none, so every fix
 * on it advanced blind and three of them contradicted each other (#34702). This is checked from
 * {@code checkILL}, which every labeling test goes through.
 *
 * Three properties of the ingredient list are deliberately NOT checked here, each having been
 * measured as false on a product that is behaving correctly:
 * <ul>
 * <li><i>no negative quantity</i> - a product losing more than its evaporating ingredients can
 * supply carries the difference as a negative, and that debt is intended: it is what keeps the
 * quantities honest when levels are not grouped (#21401, April 2024). The ingredient list caps it
 * instead, which is why the two views legitimately differ on those products. Several products of
 * the reference test set hold one, from -6.3 % to -87.5 %.</li>
 * <li><i>the level 1 quantities sum to 100</i> - the tree carries raw quantities, and the reference
 * pizza sums to 90.17 when it is perfectly correct. Normalising to 100 belongs to the rendering,
 * under {@code force100Perc}.</li>
 * <li><i>a parent equals the sum of its children, in the tree</i> - the tree scales sub ingredients
 * against the item that brings them, not against their parent, so the reference pizza carries 21.58
 * against 27.30. The renderer converts between the two spaces, which is why the invariant below
 * reads the rendered text rather than the tree.</li>
 * </ul>
 *
 * @author matthieu
 */
public final class LabelingInvariants {

	/** Percentages are rendered rounded, so a bracket is allowed to miss its parent by this much. */
	private static final double RENDERING_TOLERANCE = 0.35d;

	/** A rendered ingredient followed by the bracket detailing it, ie "tomato puree 21.6% (...)". */
	private static final Pattern DETAILED_ING = Pattern.compile("([0-9]+(?:[.,][0-9]+)?)\\s*%\\s*\\(([^()]*)\\)");

	private static final Pattern PERCENTAGE = Pattern.compile("([0-9]+(?:[.,][0-9]+)?)\\s*%");

	private static final Pattern HTML_TAG = Pattern.compile("<[^>]+>");

	private LabelingInvariants() {
		// utility class
	}

	/**
	 * Checks every invariant on one rendered label.
	 *
	 * @param rendered the label the formulation rendered
	 * @param context what to name in the failure message, so a red test says which product broke
	 */
	public static void assertHolds(String rendered, String context) {
		assertBracketsAddUpToTheirParent(rendered, context);
	}

	/**
	 * A bracket details the ingredient it follows, so it cannot add up to more than that ingredient.
	 *
	 * This is what #34702 broke twice: the sub ingredients were scaled against a total that was not
	 * the one their parent was rendered against, so "tomato puree 21.6 % (tomato 25.3 %, oil 0.3 %)"
	 * came out of a product whose ingredient list said 21.4 and 0.2.
	 *
	 * Only brackets whose every entry carries a percentage are checked : a composite that is only
	 * partly quantified legitimately shows fewer percentages than it has sub ingredients, and its
	 * bracket is then not expected to reach its parent.
	 *
	 * @param rendered the label the formulation rendered
	 * @param context what to name in the failure message
	 */
	public static void assertBracketsAddUpToTheirParent(String rendered, String context) {
		if ((rendered == null) || rendered.isEmpty()) {
			return;
		}
		String plain = HTML_TAG.matcher(rendered).replaceAll("");
		Matcher detailed = DETAILED_ING.matcher(plain);

		while (detailed.find()) {
			double parent = toDouble(detailed.group(1));
			String bracket = detailed.group(2);
			if (!isFullyQuantified(bracket)) {
				continue;
			}
			double children = sumPercentages(bracket);
			Assert.assertEquals("A bracket must add up to the ingredient it details, in " + context + " : parent " + parent + " %, bracket \""
					+ bracket.trim() + "\" = " + children + " %", parent, children, RENDERING_TOLERANCE);
		}
	}

	/**
	 * Tells whether every entry of a bracket carries a percentage.
	 *
	 * @param bracket the text between the brackets
	 * @return true when each comma separated entry holds a percentage
	 */
	private static boolean isFullyQuantified(String bracket) {
		String[] entries = bracket.split(",");
		for (String entry : entries) {
			if (!PERCENTAGE.matcher(entry).find()) {
				return false;
			}
		}
		return entries.length > 0;
	}

	/**
	 * Sums the percentages a text holds.
	 *
	 * @param text the text to read
	 * @return the sum of the percentages found
	 */
	private static double sumPercentages(String text) {
		double sum = 0d;
		Matcher matcher = PERCENTAGE.matcher(text);
		while (matcher.find()) {
			sum += toDouble(matcher.group(1));
		}
		return sum;
	}

	/**
	 * Reads a rendered percentage, whatever the decimal separator of the locale.
	 *
	 * @param value the rendered number
	 * @return its value
	 */
	private static double toDouble(String value) {
		return Double.parseDouble(value.replace(',', '.'));
	}

}
