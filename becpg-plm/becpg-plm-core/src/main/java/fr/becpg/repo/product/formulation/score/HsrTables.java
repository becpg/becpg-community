package fr.becpg.repo.product.formulation.score;

/**
 * Scoring tables of the Health Star Rating.
 *
 * <p>Every array holds the exclusive lower bound of each point, the index being the point
 * earned. Source: Health Star Rating System Implementation Guide, version 9.</p>
 *
 * @author matthieu
 */
public final class HsrTables {

	private HsrTables() {
		// tables
	}

	/** Table 1, categories 1D, 2 and 2D */
	public static final double[] ENERGY_1D_2 = { 335, 670, 1005, 1340, 1675, 2010, 2345, 2680, 3015, 3350, 3685 };
	public static final double[] SATFAT_1D_2 = { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.2, 12.5, 13.9, 15.5, 17.3, 19.3, 21.6, 24.1,
			26.9, 30.0, 33.5, 37.4, 41.7, 46.6, 52.0, 58.0, 64.7, 72.3, 80.6, 90 };
	public static final double[] SUGAR_1D_2 = { 5.0, 8.9, 12.8, 16.8, 20.7, 24.6, 28.5, 32.4, 36.3, 40.3, 44.2, 48.1, 52.0, 55.9, 59.8, 63.8, 67.7,
			71.6, 75.5, 79.4, 83.3, 87.3, 91.2, 95.1, 99.0 };
	public static final double[] SODIUM_1D_2 = { 90, 180, 270, 360, 450, 540, 630, 720, 810, 900, 990, 1080, 1170, 1260, 1350, 1440, 1530, 1620, 1710,
			1800, 1890, 1980, 2070, 2160, 2250, 2340, 2430, 2520, 2610, 2700 };

	/** Table 2, categories 3 and 3D */
	public static final double[] ENERGY_3 = ENERGY_1D_2;
	public static final double[] SATFAT_3 = { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0,
			20.0, 21.0, 22.0, 23.0, 24.0, 25.0, 26.0, 27.0, 28.0, 29.0, 30.0 };
	public static final double[] SUGAR_3 = { 5.0, 9.0, 13.5, 18.0, 22.5, 27.0, 31.0, 36.0, 40.0, 45.0 };
	public static final double[] SODIUM_3 = SODIUM_1D_2;

	/**
	 * Table 3, category 1. The energy row starts at one point: the table has no zero point
	 * for energy, a beverage of 31 kJ or less already scoring one.
	 */
	public static final double[] ENERGY_1 = { -1, 31, 61, 91, 121, 151, 181, 211, 241, 271 };
	public static final double[] SUGAR_1 = { 0.1, 1.6, 3.1, 4.6, 6.1, 7.6, 9.1, 10.6, 12.1, 13.6 };

	/** Table 4, non concentrated FVNL of categories 1D, 2, 2D, 3 and 3D */
	public static final double[] FVNL_1D_2 = { 40, 60, 67, 75, 80, 90, 95, 99.9999 };

	/** Table 5, category 1 */
	public static final double[] FVNL_1 = { 24.9999, 32.9999, 40.9999, 48.9999, 56.9999, 64.9999, 72.9999, 80.9999, 88.9999, 95.9999 };

	/** Table 6, protein and dietary fibre */
	public static final double[] PROTEIN_POINTS = { 1.6, 3.1999, 4.8, 6.4, 8.0, 9.6, 11.6, 13.9, 16.7, 20.0, 24.0, 28.9, 34.7, 41.6, 50.0 };
	public static final double[] FIBRE_POINTS = { 0.9, 1.9, 2.8, 3.7, 4.7, 5.4, 6.3, 7.3, 8.4, 9.7, 11.2, 13.0, 15.0, 17.3, 20.0 };

}
