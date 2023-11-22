package fr.becpg.test.repo.helper;

import java.util.Arrays;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import fr.becpg.repo.product.formulation.score.NutriScoreContext;
import fr.becpg.repo.product.helper.Nutrient5C2021Helper;
import fr.becpg.repo.product.helper.Nutrient5C2023Helper;
import fr.becpg.repo.product.helper.Nutrient5CHelper;

public class Nutrient5CTest {

	@Test
	public void test5C() {
		//#5500 
		Assert.assertEquals(12,Nutrient5CHelper.compute5CScore(2084d, 2.8d, 22.9d, 4.73d, 672d, 0d, 0d, 4.13d, 5.81d, "Others"));
		
		Assert.assertEquals("D",Nutrient5CHelper.extractNutrientClass(12d,
				Arrays.asList(new Double[]{18d,10d,2d,-1d}),Arrays.asList(new String[]{"E","D","C","B","A"})));
		
		
		Assert.assertEquals(8,Nutrient5CHelper.compute5CScore(1809d, 1.1d, 11d, 5.11d, 593d, 0d, 0d, 10d, 19.3d, "Others"));
		
		Assert.assertEquals("C",Nutrient5CHelper.extractNutrientClass(8d,
				Arrays.asList(new Double[]{18d,10d,2d,-1d}),Arrays.asList(new String[]{"E","D","C","B","A"})));
		
		
		Assert.assertEquals(4,Nutrient5CHelper.compute5CScore(2084d, 2.8d, 22.9d, 4.73d, 672d, 90d, 0d, 4.13d, 5.81d, "Others"));
		
		
		//#5807
		Assert.assertEquals(0,Nutrient5CHelper.compute5CScore(368d, 1.68d, 4.32d, 1.46d, 263d,0d, 0d, 1.9d, 6d, "Others"));
	
		Assert.assertEquals(0,Nutrient5CHelper.compute5CScore(368.09999999999997d, 1.6826328584934984d, 4.320793803940095d, 1.4564453550672227d, 262.851279672858d, null, null, 1.9020344065737682d, 5.996492947498318d, "Others"));
		
		//#5863
//		Double energyKj, Double satFat, Double totalFat, Double totalSugar, Double sodium, Double percFruitsAndVetgs,
//		Double nspFibre, Double aoacFibre, Double protein, String category
	
		Assert.assertEquals(20,Nutrient5CHelper.compute5CScore(1166d, 9.3d, 25.3d, 1.4d, (1.91d/2.54d)*1000d,null, null,null, 12d, "Others"));
		
		Assert.assertEquals("E",Nutrient5CHelper.extractNutrientClass(20d,
				Arrays.asList(new Double[]{18d,10d,2d,-1d}),Arrays.asList(new String[]{"E","D","C","B","A"})));
		
		//#5828
		
		Assert.assertEquals("C",Nutrient5CHelper.extractNutrientClass(
				(double) Nutrient5CHelper.compute5CScore(365d, 1.21d, 2.22d, 0.413d, (0.624d/2.54d)*1000d,  5.26, 0.899, null, null, "Others"),
				Arrays.asList(new Double[]{18d,10d,2d,-1d}),Arrays.asList(new String[]{"E","D","C","B","A"})));
		
		//#5868
		Assert.assertEquals(8,Nutrient5CHelper.compute5CScore(1761d, 3.44d, null, 17.3d, 224d,  0d, 7.71d, null, 6.5d, "Others"));
		
		Assert.assertEquals("C",Nutrient5CHelper.extractNutrientClass(8d,
				Arrays.asList(new Double[]{18d,10d,2d,-1d}),Arrays.asList(new String[]{"E","D","C","B","A"})));
	
                 //#8871
		
		// Double energyKj, Double satFat, Double totalFat, Double totalSugar,
				// Double sodium, Double percFruitsAndVetgs,
				// Double nspFibre, Double aoacFibre, Double protein, String category

		
	
	
	}
	
	@Test
	public void test5C2021() throws JSONException {
		
		//#5500 
		
		NutriScoreContext nutriScoreContext = new NutriScoreContext(2084d, 2.8d, 22.9d, 4.73d, 672d, 0d, 0d, 4.13d, 5.81d, "Others");
		Nutrient5C2021Helper.compute5CScore(nutriScoreContext);
		Nutrient5C2021Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals(12, nutriScoreContext.getNutriScore());
		Assert.assertEquals("D", nutriScoreContext.getNutrientClass());
		
		nutriScoreContext = new NutriScoreContext(1809d, 1.1d, 11d, 5.11d, 593d, 0d, 0d, 10d, 19.3d, "Others");
		Nutrient5C2021Helper.compute5CScore(nutriScoreContext);
		Nutrient5C2021Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals(8, nutriScoreContext.getNutriScore());
		Assert.assertEquals("C", nutriScoreContext.getNutrientClass());
		
		nutriScoreContext = new NutriScoreContext(2084d, 2.8d, 22.9d, 4.73d, 672d, 90d, 0d, 4.13d, 5.81d, "Others");
		Nutrient5C2021Helper.compute5CScore(nutriScoreContext);
		Nutrient5C2021Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals(4, nutriScoreContext.getNutriScore());
		
		
		//#5807
		nutriScoreContext = new NutriScoreContext(368d, 1.68d, 4.32d, 1.46d, 263d,0d, 0d, 1.9d, 6d, "Others");
		Nutrient5C2021Helper.compute5CScore(nutriScoreContext);
		Nutrient5C2021Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals(0, nutriScoreContext.getNutriScore());
		
		nutriScoreContext = new NutriScoreContext(368.09999999999997d, 1.6826328584934984d, 4.320793803940095d, 1.4564453550672227d, 262.851279672858d, 0d, 0d, 1.9020344065737682d, 5.996492947498318d, "Others");
		Nutrient5C2021Helper.compute5CScore(nutriScoreContext);
		Nutrient5C2021Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals(-1, nutriScoreContext.getNutriScore());

		
		//#5863
		nutriScoreContext = new NutriScoreContext(1166d, 9.3d, 25.3d, 1.4d, (1.91d/2.54d)*1000d,0d, 0d,0d, 12d, "Others");
		Nutrient5C2021Helper.compute5CScore(nutriScoreContext);
		Nutrient5C2021Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals(20, nutriScoreContext.getNutriScore());
		Assert.assertEquals("E", nutriScoreContext.getNutrientClass());
		
		//#5828
		nutriScoreContext = new NutriScoreContext(365d, 1.21d, 2.22d, 0.413d, (0.624d/2.54d)*1000d,  5.26, 0.899, 0d, 0d, "Others");
		Nutrient5C2021Helper.compute5CScore(nutriScoreContext);
		Nutrient5C2021Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("C", nutriScoreContext.getNutrientClass());
		
		//#5868
		nutriScoreContext = new NutriScoreContext(1761d, 3.44d, 0d, 17.3d, 224d,  0d, 7.71d, 0d, 6.5d, "Others");
		Nutrient5C2021Helper.compute5CScore(nutriScoreContext);
		Nutrient5C2021Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals(8, nutriScoreContext.getNutriScore());
		Assert.assertEquals("C", nutriScoreContext.getNutrientClass());

		
		//#8871
		nutriScoreContext = new NutriScoreContext(2596.0642d, 15.16d, 70.172d, 0.003d, 159d, 0d, 0d, 0d,  0.15d, "Fats");
		Nutrient5C2021Helper.compute5CScore(nutriScoreContext);
		Nutrient5C2021Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals(10, nutriScoreContext.getNutriScore());
		
		nutriScoreContext = new NutriScoreContext(151d, 0d, 0d, 7.2d, 10d, 0d, 0d, 0d, 0d, "Beverages");
		Nutrient5C2021Helper.compute5CScore(nutriScoreContext);
		Nutrient5C2021Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals(6d, nutriScoreContext.getParts().getJSONObject(NutriScoreContext.ENERGY_CODE).getDouble(NutriScoreContext.SCORE), 0.001);
		Assert.assertEquals(5d, nutriScoreContext.getParts().getJSONObject(NutriScoreContext.SUGAR_CODE).getDouble(NutriScoreContext.SCORE), 0.001);
		Assert.assertEquals(0d, nutriScoreContext.getParts().getJSONObject(NutriScoreContext.FRUIT_VEGETABLE_CODE).getDouble(NutriScoreContext.SCORE), 0.001);
		
		nutriScoreContext = new NutriScoreContext(151d, 0d, 0d, 0d, 0d, 40d, 0d, 0d, 1.7d, "Beverages");
		Nutrient5C2021Helper.compute5CScore(nutriScoreContext);
		Nutrient5C2021Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals(1d, nutriScoreContext.getParts().getJSONObject(NutriScoreContext.PROTEIN_CODE).getDouble(NutriScoreContext.SCORE), 0.001);
		Assert.assertEquals(2d, nutriScoreContext.getParts().getJSONObject(NutriScoreContext.FRUIT_VEGETABLE_CODE).getDouble(NutriScoreContext.SCORE), 0.001);
		
		nutriScoreContext = new NutriScoreContext(30d, 0d, 0d, 12d, 0d, 80d, 0d, 0d, 0d, "Beverages");
		Nutrient5C2021Helper.compute5CScore(nutriScoreContext);
		Nutrient5C2021Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals(1d, nutriScoreContext.getParts().getJSONObject(NutriScoreContext.ENERGY_CODE).getDouble(NutriScoreContext.SCORE), 0.001);
		Assert.assertEquals(8d, nutriScoreContext.getParts().getJSONObject(NutriScoreContext.SUGAR_CODE).getDouble(NutriScoreContext.SCORE), 0.001);
		Assert.assertEquals(10d, nutriScoreContext.getParts().getJSONObject(NutriScoreContext.FRUIT_VEGETABLE_CODE).getDouble(NutriScoreContext.SCORE), 0.001);
		
		nutriScoreContext = new NutriScoreContext(0d, 0d, 0d, 11d, 550d, 10d, 0d, 3.5d, 5d, "Others");
		Nutrient5C2021Helper.compute5CScore(nutriScoreContext);
		Nutrient5C2021Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals(2d, nutriScoreContext.getParts().getJSONObject(NutriScoreContext.SUGAR_CODE).getDouble(NutriScoreContext.SCORE), 0.001);
		Assert.assertEquals(6d, nutriScoreContext.getParts().getJSONObject(NutriScoreContext.SODIUM_CODE).getDouble(NutriScoreContext.SCORE), 0.001);
		Assert.assertEquals(3d, nutriScoreContext.getParts().getJSONObject(NutriScoreContext.AOAC_CODE).getDouble(NutriScoreContext.SCORE), 0.001);
		Assert.assertEquals(3d, nutriScoreContext.getParts().getJSONObject(NutriScoreContext.PROTEIN_CODE).getDouble(NutriScoreContext.SCORE), 0.001);
		Assert.assertEquals(0d, nutriScoreContext.getParts().getJSONObject(NutriScoreContext.FRUIT_VEGETABLE_CODE).getDouble(NutriScoreContext.SCORE), 0.001);
		
		nutriScoreContext = new NutriScoreContext(0d, 0d, 0d, 42d, 1160d, 80d, 0d, 0d, 0d, "Others");
		Nutrient5C2021Helper.compute5CScore(nutriScoreContext);
		Nutrient5C2021Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals(9d, nutriScoreContext.getParts().getJSONObject(NutriScoreContext.SUGAR_CODE).getDouble(NutriScoreContext.SCORE), 0.001);
		Assert.assertEquals(10d, nutriScoreContext.getParts().getJSONObject(NutriScoreContext.SODIUM_CODE).getDouble(NutriScoreContext.SCORE), 0.001);
		Assert.assertEquals(5d, nutriScoreContext.getParts().getJSONObject(NutriScoreContext.FRUIT_VEGETABLE_CODE).getDouble(NutriScoreContext.SCORE), 0.001);
	}
	
	@Test
	public void test5C2023() throws JSONException {
		
		//#5500 
		NutriScoreContext nutriScoreContext = new NutriScoreContext(2084d, 2.8d, 22.9d, 4.73d, 672d, 0d, 0d, 4.13d, 5.81d, "Others");
		Nutrient5C2023Helper.compute5CScore(nutriScoreContext);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		// change of protein and sodium range
		Assert.assertEquals(15, nutriScoreContext.getNutriScore());
		Assert.assertEquals("D", nutriScoreContext.getNutrientClass());
		
		nutriScoreContext = new NutriScoreContext(1809d, 1.1d, 11d, 5.11d, 593d, 0d, 0d, 10d, 19.3d, "Others");
		Nutrient5C2023Helper.compute5CScore(nutriScoreContext);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		// change of sodium range
		Assert.assertEquals(9, nutriScoreContext.getNutriScore());
		Assert.assertEquals("C", nutriScoreContext.getNutrientClass());
		
		nutriScoreContext = new NutriScoreContext(2084d, 2.8d, 22.9d, 4.73d, 672d, 90d, 0d, 4.13d, 5.81d, "Others");
		Nutrient5C2023Helper.compute5CScore(nutriScoreContext);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		// change of fibers and sodium ranges
		Assert.assertEquals(10, nutriScoreContext.getNutriScore());
		
		
		//#5807
		nutriScoreContext = new NutriScoreContext(368d, 1.68d, 4.32d, 1.46d, 263d,0d, 0d, 1.9d, 6d, "Others");
		Nutrient5C2023Helper.compute5CScore(nutriScoreContext);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		// change of sodium, fibers and proteins ranges
		Assert.assertEquals(3, nutriScoreContext.getNutriScore());
		
		nutriScoreContext = new NutriScoreContext(368.09999999999997d, 1.6826328584934984d, 4.320793803940095d, 1.4564453550672227d, 262.851279672858d, 0d, 0d, 1.9020344065737682d, 5.996492947498318d, "Others");
		Nutrient5C2023Helper.compute5CScore(nutriScoreContext);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		// change of sodium, fibers and proteins ranges
		Assert.assertEquals(3, nutriScoreContext.getNutriScore());

		
		//#5863
		nutriScoreContext = new NutriScoreContext(1166d, 9.3d, 25.3d, 1.4d, (1.91d/2.54d)*1000d,0d, 0d,0d, 12d, "Others");
		Nutrient5C2023Helper.compute5CScore(nutriScoreContext);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		// change of sodium range
		Assert.assertEquals(21, nutriScoreContext.getNutriScore());
		Assert.assertEquals("E", nutriScoreContext.getNutrientClass());
		
		//#5828
		nutriScoreContext = new NutriScoreContext(365d, 1.21d, 2.22d, 0.413d, (0.624d/2.54d)*1000d,  5.26, 0.899, 0d, 0d, "Others");
		Nutrient5C2023Helper.compute5CScore(nutriScoreContext);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("C", nutriScoreContext.getNutrientClass());
		
		//#5868
		nutriScoreContext = new NutriScoreContext(1761d, 3.44d, 0d, 17.3d, 224d,  0d, 7.71d, 0d, 6.5d, "Others");
		Nutrient5C2023Helper.compute5CScore(nutriScoreContext);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		// change of sugar range
		Assert.assertEquals(10, nutriScoreContext.getNutriScore());
		Assert.assertEquals("C", nutriScoreContext.getNutrientClass());

		
		//#8871
		nutriScoreContext = new NutriScoreContext(2596.0642d, 15.16d, 70.172d, 0.003d, 159d, 0d, 0d, 0d,  0.15d, "Fats");
		Nutrient5C2023Helper.compute5CScore(nutriScoreContext);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		// change fats energy range
		Assert.assertEquals(7, nutriScoreContext.getNutriScore());
				
		nutriScoreContext = new NutriScoreContext(151d, 0d, 0d, 7.2d, 10d, 0d, 0d, 0d, 0d, "Beverages");
		Nutrient5C2023Helper.compute5CScore(nutriScoreContext);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals(3d, nutriScoreContext.getParts().getJSONObject(NutriScoreContext.ENERGY_CODE).getDouble(NutriScoreContext.SCORE), 0.001);
		Assert.assertEquals(6d, nutriScoreContext.getParts().getJSONObject(NutriScoreContext.SUGAR_CODE).getDouble(NutriScoreContext.SCORE), 0.001);
		Assert.assertEquals(0d, nutriScoreContext.getParts().getJSONObject(NutriScoreContext.FRUIT_VEGETABLE_CODE).getDouble(NutriScoreContext.SCORE), 0.001);
		
		nutriScoreContext = new NutriScoreContext(151d, 0d, 0d, 0d, 0d, 40d, 0d, 0d, 1.7d, "Beverages");
		Nutrient5C2023Helper.compute5CScore(nutriScoreContext);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals(2d, nutriScoreContext.getParts().getJSONObject(NutriScoreContext.PROTEIN_CODE).getDouble(NutriScoreContext.SCORE), 0.001);
		Assert.assertEquals(2d, nutriScoreContext.getParts().getJSONObject(NutriScoreContext.FRUIT_VEGETABLE_CODE).getDouble(NutriScoreContext.SCORE), 0.001);
		
		nutriScoreContext = new NutriScoreContext(30d, 0d, 0d, 12d, 0d, 80d, 0d, 0d, 0d, "Beverages");
		Nutrient5C2023Helper.compute5CScore(nutriScoreContext);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals(0d, nutriScoreContext.getParts().getJSONObject(NutriScoreContext.ENERGY_CODE).getDouble(NutriScoreContext.SCORE), 0.001);
		Assert.assertEquals(10d, nutriScoreContext.getParts().getJSONObject(NutriScoreContext.SUGAR_CODE).getDouble(NutriScoreContext.SCORE), 0.001);
		Assert.assertEquals(6d, nutriScoreContext.getParts().getJSONObject(NutriScoreContext.FRUIT_VEGETABLE_CODE).getDouble(NutriScoreContext.SCORE), 0.001);
		
		nutriScoreContext = new NutriScoreContext(0d, 0d, 0d, 11d, 700d, 10d, 0d, 3.5d, 5d, "Others");
		Nutrient5C2023Helper.compute5CScore(nutriScoreContext);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals(3d, nutriScoreContext.getParts().getJSONObject(NutriScoreContext.SUGAR_CODE).getDouble(NutriScoreContext.SCORE), 0.001);
		Assert.assertEquals(8d, nutriScoreContext.getParts().getJSONObject(NutriScoreContext.SODIUM_CODE).getDouble(NutriScoreContext.SCORE), 0.001);
		Assert.assertEquals(1d, nutriScoreContext.getParts().getJSONObject(NutriScoreContext.AOAC_CODE).getDouble(NutriScoreContext.SCORE), 0.001);
		Assert.assertEquals(0d, nutriScoreContext.getParts().getJSONObject(NutriScoreContext.FRUIT_VEGETABLE_CODE).getDouble(NutriScoreContext.SCORE), 0.001);
		
		nutriScoreContext = new NutriScoreContext(0d, 0d, 0d, 42d, 1160d, 80d, 0d, 0d, 0d, "Others");
		Nutrient5C2023Helper.compute5CScore(nutriScoreContext);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals(12d, nutriScoreContext.getParts().getJSONObject(NutriScoreContext.SUGAR_CODE).getDouble(NutriScoreContext.SCORE), 0.001);
		Assert.assertEquals(14d, nutriScoreContext.getParts().getJSONObject(NutriScoreContext.SODIUM_CODE).getDouble(NutriScoreContext.SCORE), 0.001);
		Assert.assertEquals(5d, nutriScoreContext.getParts().getJSONObject(NutriScoreContext.FRUIT_VEGETABLE_CODE).getDouble(NutriScoreContext.SCORE), 0.001);
		
		//#19177
		nutriScoreContext = new NutriScoreContext(1833.9d, 3.3d, 0d, 32.5d, 200d, 17d, 0d, 1.4d, 4.9d, "Others");
		Nutrient5C2023Helper.compute5CScore(nutriScoreContext);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals(19, nutriScoreContext.getNutriScore());

		// test red meats (proteins must not be > 2)
		nutriScoreContext = new NutriScoreContext(833.9d, 3.3d, 0d, 2.5d, 200d, 17d, 0d, 1.4d, 9.9d, "Others");
		Nutrient5C2023Helper.compute5CScore(nutriScoreContext);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals(4d, nutriScoreContext.getParts().getJSONObject(NutriScoreContext.PROTEIN_CODE).getDouble(NutriScoreContext.SCORE), 0.001);
		nutriScoreContext = new NutriScoreContext(833.9d, 3.3d, 0d, 2.5d, 200d, 17d, 0d, 1.4d, 9.9d, "RedMeats");
		Nutrient5C2023Helper.compute5CScore(nutriScoreContext);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals(2d, nutriScoreContext.getParts().getJSONObject(NutriScoreContext.PROTEIN_CODE).getDouble(NutriScoreContext.SCORE), 0.001);

		// test new Fats
		nutriScoreContext = new NutriScoreContext(2596.0642d, 15.16d, 70.172d, 0.003d, 159d, 0d, 0d, 0d,  0.15d, "Fats");
		Nutrient5C2023Helper.compute5CScore(nutriScoreContext);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals(7, nutriScoreContext.getNutriScore());
		Assert.assertEquals("C", nutriScoreContext.getNutrientClass());
		
		nutriScoreContext = new NutriScoreContext(2596.0642d, 5.16d, 70.172d, 0.003d, 159d, 0d, 0d, 0d,  0.15d, "Fats");
		Nutrient5C2023Helper.compute5CScore(nutriScoreContext);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals(2, nutriScoreContext.getNutriScore());
		Assert.assertEquals("B", nutriScoreContext.getNutrientClass());
		
		// test classifications
		nutriScoreContext = new NutriScoreContext();
		nutriScoreContext.setCategory("Others");
		nutriScoreContext.setNutriScore(0);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("A", nutriScoreContext.getNutrientClass());
		nutriScoreContext.setNutriScore(1);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("B", nutriScoreContext.getNutrientClass());
		nutriScoreContext.setNutriScore(2);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("B", nutriScoreContext.getNutrientClass());
		nutriScoreContext.setNutriScore(3);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("C", nutriScoreContext.getNutrientClass());
		nutriScoreContext.setNutriScore(10);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("C", nutriScoreContext.getNutrientClass());
		nutriScoreContext.setNutriScore(11);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("D", nutriScoreContext.getNutrientClass());
		nutriScoreContext.setNutriScore(18);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("D", nutriScoreContext.getNutrientClass());
		nutriScoreContext.setNutriScore(19);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("E", nutriScoreContext.getNutrientClass());
		
		nutriScoreContext.setCategory("RedMeats");
		nutriScoreContext.setNutriScore(0);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("A", nutriScoreContext.getNutrientClass());
		nutriScoreContext.setNutriScore(1);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("B", nutriScoreContext.getNutrientClass());
		nutriScoreContext.setNutriScore(2);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("B", nutriScoreContext.getNutrientClass());
		nutriScoreContext.setNutriScore(3);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("C", nutriScoreContext.getNutrientClass());
		nutriScoreContext.setNutriScore(10);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("C", nutriScoreContext.getNutrientClass());
		nutriScoreContext.setNutriScore(11);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("D", nutriScoreContext.getNutrientClass());
		nutriScoreContext.setNutriScore(18);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("D", nutriScoreContext.getNutrientClass());
		nutriScoreContext.setNutriScore(19);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("E", nutriScoreContext.getNutrientClass());
		
		nutriScoreContext.setCategory("Fats");
		nutriScoreContext.setNutriScore(-6);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("A", nutriScoreContext.getNutrientClass());
		nutriScoreContext.setNutriScore(-5);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("B", nutriScoreContext.getNutrientClass());
		nutriScoreContext.setNutriScore(2);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("B", nutriScoreContext.getNutrientClass());
		nutriScoreContext.setNutriScore(3);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("C", nutriScoreContext.getNutrientClass());
		nutriScoreContext.setNutriScore(10);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("C", nutriScoreContext.getNutrientClass());
		nutriScoreContext.setNutriScore(11);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("D", nutriScoreContext.getNutrientClass());
		nutriScoreContext.setNutriScore(18);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("D", nutriScoreContext.getNutrientClass());
		nutriScoreContext.setNutriScore(19);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("E", nutriScoreContext.getNutrientClass());
		
		nutriScoreContext.setCategory("Beverages");
		nutriScoreContext.setNutriScore(-6);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("B", nutriScoreContext.getNutrientClass());
		nutriScoreContext.setNutriScore(2);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("B", nutriScoreContext.getNutrientClass());
		nutriScoreContext.setNutriScore(2);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("B", nutriScoreContext.getNutrientClass());
		nutriScoreContext.setNutriScore(3);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("C", nutriScoreContext.getNutrientClass());
		nutriScoreContext.setNutriScore(6);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("C", nutriScoreContext.getNutrientClass());
		nutriScoreContext.setNutriScore(7);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("D", nutriScoreContext.getNutrientClass());
		nutriScoreContext.setNutriScore(9);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("D", nutriScoreContext.getNutrientClass());
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("D", nutriScoreContext.getNutrientClass());
		nutriScoreContext.setNutriScore(10);
		Nutrient5C2023Helper.extractNutrientClass(nutriScoreContext);
		Assert.assertEquals("E", nutriScoreContext.getNutrientClass());
		
	}

}
