package fr.becpg.repo.product.data.meat;

public enum MeatType {
	Mammals, Porcines, BirdsAndRabbits;

	public static boolean isMeatType(String meatType) {

		return "Mammals".equals(meatType) || "Porcines".equals(meatType) || "BirdsAndRabbits".equals(meatType);

	}

}