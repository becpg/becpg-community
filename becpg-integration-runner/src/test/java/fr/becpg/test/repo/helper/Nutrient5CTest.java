package fr.becpg.test.repo.helper;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import fr.becpg.repo.product.helper.Nutrient5CHelper;

public class Nutrient5CTest {

	@Test
	public void test5C() {
		// Double energyKj, Double satFat, Double totalFat, Double totalSugar,
		// Double sodium, Double percFruitsAndVetgs,
		// Double nspFibre, Double aoacFibre, Double protein, String category

		// #5500
		Assert.assertEquals(12, Nutrient5CHelper.compute5CScore(2084d, 2.8d, 22.9d, 4.73d, 672d, 0d, 0d, 4.13d, 5.81d, "Others"));

		Assert.assertEquals("D", Nutrient5CHelper.buildNutrientClass(12d, Arrays.asList(new Double[] { 18d, 10d, 2d, -1d }),
				Arrays.asList(new String[] { "E", "D", "C", "B", "A" })));

		Assert.assertEquals(8, Nutrient5CHelper.compute5CScore(1809d, 1.1d, 11d, 5.11d, 593d, 0d, 0d, 10d, 19.3d, "Others"));

		Assert.assertEquals("C", Nutrient5CHelper.buildNutrientClass(8d, Arrays.asList(new Double[] { 18d, 10d, 2d, -1d }),
				Arrays.asList(new String[] { "E", "D", "C", "B", "A" })));

		Assert.assertEquals(4, Nutrient5CHelper.compute5CScore(2084d, 2.8d, 22.9d, 4.73d, 672d, 90d, 0d, 4.13d, 5.81d, "Others"));

		// #5087

		Assert.assertEquals(0, Nutrient5CHelper.compute5CScore(368d, 1.68d, 4.32d, 1.46d, 263d, 0d, 0d, 1.9d, 6d, "Others"));

		Assert.assertEquals(0, Nutrient5CHelper.compute5CScore(368.09999999999997d, 1.6826328584934984d, 4.320793803940095d, 1.4564453550672227d,
				262.851279672858d, null, null, 1.9020344065737682d, 5.996492947498318d, "Others"));

		// #5807
		Assert.assertEquals(0, Nutrient5CHelper.compute5CScore(368d, 1.68d, 4.32d, 1.46d, 263d, 0d, 0d, 1.9d, 6d, "Others"));

		Assert.assertEquals(0, Nutrient5CHelper.compute5CScore(368.09999999999997d, 1.6826328584934984d, 4.320793803940095d, 1.4564453550672227d,
				262.851279672858d, null, null, 1.9020344065737682d, 5.996492947498318d, "Others"));

		Assert.assertEquals("B",
				Nutrient5CHelper.buildNutrientClass(
						Double.valueOf(Nutrient5CHelper.compute5CScore(335d, 1.21d, 2.22d, 0.413d, 1.585d, 5.26d, 0d, 0.899d, 2.94d, "Others")),
						Arrays.asList(new Double[] { 18d, 10d, 2d, -1d }), Arrays.asList(new String[] { "E", "D", "C", "B", "A" })));

                //#5863

//		Assert.assertEquals(0,Nutrient5CHelper.compute5CScore(1166d, 9.3d, 25.3d, 1.4d, 1,91d*2.54d,null, null,null, 12d, "Others"));
		
		//#5828
		
		Assert.assertEquals("B",Nutrient5CHelper.buildNutrientClass(
				(double) Nutrient5CHelper.compute5CScore(365d, 1.21d, 2.22d, 0.413d, 0.624d*2.54d,  5.26, 0.899, null, null, "Others"),
				Arrays.asList(new Double[]{18d,10d,2d,-1d}),Arrays.asList(new String[]{"E","D","C","B","A"})));
		
		

	}

}
