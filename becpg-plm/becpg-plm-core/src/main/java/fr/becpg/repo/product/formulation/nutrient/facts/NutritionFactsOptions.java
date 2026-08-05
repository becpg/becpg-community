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
package fr.becpg.repo.product.formulation.nutrient.facts;

import java.util.List;

/**
 * <p>What to put on a nutrition facts panel, beyond the product data itself.</p>
 *
 * <p>{@code micronutrientStartSort} is the sort order at which the vitamin and mineral block starts
 * in the regulation CSV; it is what separates the two blocks a panel draws on either side of its
 * thick rule. Under the 2016 FDA regulation, protein sorts at 22 and vitamin D at 23.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public record NutritionFactsOptions(String regulationKey, boolean showOptional, int micronutrientStartSort,
		List<SharedDailyValue> sharedDailyValues) {

	/**
	 * <p>A nutrient whose percent daily value also accounts for another one, the way the Canadian
	 * saturated fat line carries the value of saturated and trans fat together.</p>
	 *
	 * <p>The two percentages may only be added when both nutrients are measured against the same
	 * reference intake, which the builder checks before combining anything.</p>
	 *
	 * @param hostNutCode the nutrient carrying the combined percentage
	 * @param guestNutCode the nutrient folded into it, which shows no percentage of its own
	 */
	public record SharedDailyValue(String hostNutCode, String guestNutCode) {
	}

	/** Sort order of the energy line, which every panel prints as its large calories figure. */
	public static final int CALORIES_SORT = 1;

	/** Regulation key of the United States. */
	public static final String US_REGULATION_KEY = "US";

	/** Regulation key of Canada. */
	public static final String CA_REGULATION_KEY = "CA";

	/** Under the 2016 FDA regulation, protein sorts at 22 and vitamin D at 23. */
	private static final int US_MICRONUTRIENT_START_SORT = 23;

	/** Under the 2016 Canadian regulation, sodium sorts at 21 and potassium at 23. */
	private static final int CA_MICRONUTRIENT_START_SORT = 23;

	private static final int DEFAULT_MICRONUTRIENT_START_SORT = Integer.MAX_VALUE;

	private static final String SATURATED_FAT_NUT_CODE = "FASAT";

	private static final String TRANS_FAT_NUT_CODE = "FATRN";

	/**
	 * <p>Default options of a regulation. A regulation without a known vitamin block keeps all its
	 * nutrients in a single block rather than guessing where to split them.</p>
	 *
	 * @param regulationKey a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.product.formulation.nutrient.facts.NutritionFactsOptions} object
	 */
	public static NutritionFactsOptions forRegulation(String regulationKey) {
		if (US_REGULATION_KEY.equals(regulationKey)) {
			return new NutritionFactsOptions(regulationKey, false, US_MICRONUTRIENT_START_SORT, List.of());
		}
		if (CA_REGULATION_KEY.equals(regulationKey)) {
			return new NutritionFactsOptions(regulationKey, false, CA_MICRONUTRIENT_START_SORT,
					List.of(new SharedDailyValue(SATURATED_FAT_NUT_CODE, TRANS_FAT_NUT_CODE)));
		}
		return new NutritionFactsOptions(regulationKey, false, DEFAULT_MICRONUTRIENT_START_SORT, List.of());
	}

	/**
	 * <p>Same options, showing the nutrients the regulation allows but does not require.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.formulation.nutrient.facts.NutritionFactsOptions} object
	 */
	public NutritionFactsOptions withOptionalNutrients() {
		return new NutritionFactsOptions(regulationKey, true, micronutrientStartSort, sharedDailyValues);
	}

}
