package fr.becpg.repo.product.data.meat;

public enum MeatType {
	Mammals, Porcines, BirdsAndRabbits;

	public static boolean isMeatType(String meatType) {

		return meatType != null && (meatType.startsWith("Mammals") || meatType.startsWith("Porcines") || meatType.startsWith("BirdsAndRabbits"));

	}

}