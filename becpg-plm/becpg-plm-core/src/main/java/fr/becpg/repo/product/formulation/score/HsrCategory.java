package fr.becpg.repo.product.formulation.score;

/**
 * Category of a product for the Health Star Rating.
 *
 * <p>The category decides which baseline table applies, whether the product may earn
 * protein and fibre points, and how its score converts into stars. Source: Health Star
 * Rating System Implementation Guide, version 9, tables 1 to 7.</p>
 *
 * @author matthieu
 */
public enum HsrCategory {

	/** Non-dairy beverages, jellies and water-based ice confections */
	CATEGORY_1("1"),

	/** Dairy beverages */
	CATEGORY_1D("1D"),

	/** All foods other than those of the other categories */
	CATEGORY_2("2"),

	/** Dairy foods */
	CATEGORY_2D("2D"),

	/** Oils and oil-based spreads */
	CATEGORY_3("3"),

	/** Cheeses */
	CATEGORY_3D("3D");

	/** Upper bound of each rating, from five stars down to one, half a star being the rest */
	private static final double[] RATINGS = { 5d, 4.5d, 4d, 3.5d, 3d, 2.5d, 2d, 1.5d, 1d };

	/**
	 * Table 7, the highest score still earning each rating, per category.
	 *
	 * <p>The five and four and a half star rows of category 1 are reserved to water and to
	 * unsweetened flavoured water, which the guide rates by identity rather than by score, so
	 * no score reaches them.</p>
	 */
	private static final int[] SCORES_1 = { Integer.MIN_VALUE, Integer.MIN_VALUE, 0, 1, 3, 5, 7, 9, 11 };
	private static final int[] SCORES_1D = { -2, -1, 0, 1, 2, 3, 4, 5, 6 };
	private static final int[] SCORES_2 = { -11, -7, -2, 2, 6, 11, 15, 20, 24 };
	private static final int[] SCORES_2D = { -2, 0, 2, 3, 5, 7, 8, 10, 12 };
	private static final int[] SCORES_3 = { 13, 16, 20, 23, 27, 30, 34, 37, 41 };
	private static final int[] SCORES_3D = { 24, 26, 28, 30, 31, 33, 35, 37, 39 };

	/** Constant <code>LOWEST_RATING=0.5d</code> */
	private static final double LOWEST_RATING = 0.5d;

	private final String code;

	/**
	 * <p>Constructor for HsrCategory.</p>
	 *
	 * @param code the code of the category
	 */
	HsrCategory(String code) {
		this.code = code;
	}

	/**
	 * <p>Getter for the field <code>code</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getCode() {
		return code;
	}

	/**
	 * <p>Parses a category code, falling back on the catch-all category 2.</p>
	 *
	 * @param value the stored category code
	 * @return the matching category, never null
	 */
	public static HsrCategory of(String value) {
		for (HsrCategory category : values()) {
			if (category.code.equalsIgnoreCase(value)) {
				return category;
			}
		}
		return CATEGORY_2;
	}

	/**
	 * Category 1 scores its baseline on energy and total sugars only.
	 *
	 * @return a boolean
	 */
	public boolean hasFatAndSodiumBaseline() {
		return !CATEGORY_1.equals(this);
	}

	/**
	 * <p>isEligibleToProtein.</p>
	 *
	 * @return a boolean
	 */
	public boolean isEligibleToProtein() {
		return !CATEGORY_1.equals(this);
	}

	/**
	 * <p>isEligibleToFibre.</p>
	 *
	 * @return a boolean
	 */
	public boolean isEligibleToFibre() {
		return !CATEGORY_1.equals(this) && !CATEGORY_1D.equals(this);
	}

	/**
	 * <p>energyBounds.</p>
	 *
	 * @return an array of double
	 */
	public double[] energyBounds() {
		if (CATEGORY_1.equals(this)) {
			return HsrTables.ENERGY_1;
		}
		return isOilOrCheese() ? HsrTables.ENERGY_3 : HsrTables.ENERGY_1D_2;
	}

	/**
	 * <p>satFatBounds.</p>
	 *
	 * @return an array of double
	 */
	public double[] satFatBounds() {
		return isOilOrCheese() ? HsrTables.SATFAT_3 : HsrTables.SATFAT_1D_2;
	}

	/**
	 * <p>sugarBounds.</p>
	 *
	 * @return an array of double
	 */
	public double[] sugarBounds() {
		if (CATEGORY_1.equals(this)) {
			return HsrTables.SUGAR_1;
		}
		return isOilOrCheese() ? HsrTables.SUGAR_3 : HsrTables.SUGAR_1D_2;
	}

	/**
	 * <p>sodiumBounds.</p>
	 *
	 * @return an array of double
	 */
	public double[] sodiumBounds() {
		return isOilOrCheese() ? HsrTables.SODIUM_3 : HsrTables.SODIUM_1D_2;
	}

	/**
	 * <p>fvnlBounds.</p>
	 *
	 * @return an array of double
	 */
	public double[] fvnlBounds() {
		return CATEGORY_1.equals(this) ? HsrTables.FVNL_1 : HsrTables.FVNL_1D_2;
	}

	/**
	 * <p>Converts a final score into a rating, from half a star to five.</p>
	 *
	 * @param score the final HSR score
	 * @return a double
	 */
	public double stars(int score) {
		int[] bounds = scoreBounds();

		for (int i = 0; i < bounds.length; i++) {
			if (score <= bounds[i]) {
				return RATINGS[i];
			}
		}

		return LOWEST_RATING;
	}

	/**
	 * <p>scoreBounds.</p>
	 *
	 * @return an array of int
	 */
	private int[] scoreBounds() {
		switch (this) {
		case CATEGORY_1:
			return SCORES_1;
		case CATEGORY_1D:
			return SCORES_1D;
		case CATEGORY_2D:
			return SCORES_2D;
		case CATEGORY_3:
			return SCORES_3;
		case CATEGORY_3D:
			return SCORES_3D;
		default:
			return SCORES_2;
		}
	}

	/**
	 * <p>isOilOrCheese.</p>
	 *
	 * @return a boolean
	 */
	private boolean isOilOrCheese() {
		return CATEGORY_3.equals(this) || CATEGORY_3D.equals(this);
	}

}
