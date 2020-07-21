package fr.becpg.repo.product.data.meat;

/**
 * <p>MeatType class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public enum MeatType {
	Mammals, Porcines, BirdsAndRabbits;

	/**
	 * <p>isMeatType.</p>
	 *
	 * @param meatType a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public static boolean isMeatType(String meatType) {

		return meatType != null && (meatType.startsWith("Mammals") || meatType.startsWith("Porcines") || meatType.startsWith("BirdsAndRabbits"));

	}

}
