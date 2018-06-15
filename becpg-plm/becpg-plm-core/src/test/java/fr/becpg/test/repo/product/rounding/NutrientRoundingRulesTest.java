package fr.becpg.test.repo.product.rounding;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.repo.product.formulation.rounding.NutrientRoundingRules;
import fr.becpg.repo.product.formulation.rounding.NutrientRoundingRules.NutrientRoundingRuleType;

public class NutrientRoundingRulesTest{

	public Log logger = LogFactory.getLog(NutrientRoundingRulesTest.class);
	
	@Test
	public void testRoundingRules() {
		//logger.info(NutrientRoundingRules.round(1.55d, NutrientRoundingRuleType.fat.toString(), Locale.FRENCH));
		
		//Test NRJ
		 assertEquals(NutrientRoundingRules.round(4253.6d, NutrientRoundingRuleType.NRJ.toString(), Locale.FRENCH, "kcal"), 4254d,0);
		 assertEquals(NutrientRoundingRules.round(0.055d, NutrientRoundingRuleType.NRJ.toString(), Locale.US, "kcal"), 0.06d,0);
		 assertEquals(NutrientRoundingRules.round(0.014d, NutrientRoundingRuleType.NRJ.toString(), Locale.US, "kcal"), 0.015d,0);
		 assertEquals(NutrientRoundingRules.round(0.004d, NutrientRoundingRuleType.NRJ.toString(), Locale.US, "kcal"), 0d,0);
		 

         //Test NRJ en KJ/100g

         assertEquals(NutrientRoundingRules.round(0.013d, NutrientRoundingRuleType.NRJ.toString(), Locale.US, "KJ/100g"), 0d,0);
         assertEquals(NutrientRoundingRules.round(0.16d, NutrientRoundingRuleType.NRJ.toString(), Locale.US, "KJ/100g"), 0.17d,0);
         assertEquals(NutrientRoundingRules.round(0.433d, NutrientRoundingRuleType.NRJ.toString(), Locale.US, "KJ/100g"), 0.46d,0);
		 
		 //Test fat
		 assertEquals(NutrientRoundingRules.round(null, NutrientRoundingRuleType.Fat.toString(), Locale.FRENCH, null), null);
		 assertEquals(NutrientRoundingRules.round(null, NutrientRoundingRuleType.Fat.toString(), Locale.US, null), null);
		 assertEquals(NutrientRoundingRules.round(11.45d, NutrientRoundingRuleType.Fat.toString(), Locale.FRENCH, null), 11d,0);
		 assertEquals(NutrientRoundingRules.round(11.45d, NutrientRoundingRuleType.Fat.toString(), Locale.US, null), 12d,0);
		 assertEquals(NutrientRoundingRules.round(0.2d, NutrientRoundingRuleType.Fat.toString(), Locale.FRENCH, null), 0d,0);
		 assertEquals(NutrientRoundingRules.round(0.2d, NutrientRoundingRuleType.Fat.toString(), Locale.US, null), 0d,0);
		 assertEquals(NutrientRoundingRules.round(4.33d, NutrientRoundingRuleType.Fat.toString(), Locale.FRENCH, null), 4.3d,0);
		 assertEquals(NutrientRoundingRules.round(4.33d, NutrientRoundingRuleType.Fat.toString(), Locale.US, null), 4.5d,0);
		
		 
		 //Test saturated fat 
		 assertEquals(NutrientRoundingRules.round(null, NutrientRoundingRuleType.SatFat.toString(), Locale.FRENCH, null), null);
		 assertEquals(NutrientRoundingRules.round(11.6d, NutrientRoundingRuleType.SatFat.toString(), Locale.FRENCH, null), 12d,0);
		 assertEquals(NutrientRoundingRules.round(11.6d, NutrientRoundingRuleType.SatFat.toString(), Locale.GERMAN, null), 12d,0);
		 assertEquals(NutrientRoundingRules.round(8.63d, NutrientRoundingRuleType.SatFat.toString(), Locale.FRENCH, null), 8.6d,0);
		 assertEquals(NutrientRoundingRules.round(0.2d, NutrientRoundingRuleType.SatFat.toString(), Locale.FRENCH, null), 0.2d,0);
		 assertEquals(NutrientRoundingRules.round(0.01d, NutrientRoundingRuleType.SatFat.toString(), Locale.FRENCH, null), 0.0d,0);
		 assertEquals(NutrientRoundingRules.round(0d, NutrientRoundingRuleType.SatFat.toString(), Locale.FRENCH, null), 0d,0);
		 assertEquals(NutrientRoundingRules.round(null, NutrientRoundingRuleType.SatFat.toString(), Locale.US, null), null);
		 assertEquals(NutrientRoundingRules.round(4.3d, NutrientRoundingRuleType.SatFat.toString(), Locale.US, null), 4.5d,0);
		 assertEquals(NutrientRoundingRules.round(8.63d, NutrientRoundingRuleType.SatFat.toString(), Locale.US, null), 9d,0);
		 assertEquals(NutrientRoundingRules.round(0.2d, NutrientRoundingRuleType.SatFat.toString(), Locale.US, null), 0.0d,0);
				 
		//Test Sodium en g
		 assertEquals(NutrientRoundingRules.round(null, NutrientRoundingRuleType.Na.toString(), Locale.FRENCH, null), null);
		 assertEquals(NutrientRoundingRules.round(10.36d, NutrientRoundingRuleType.Na.toString(), Locale.FRENCH, null), 10.4d,0);
		 assertEquals(NutrientRoundingRules.round(10.36d, NutrientRoundingRuleType.Na.toString(), Locale.GERMAN,null), 10.4d,0);
		 assertEquals(NutrientRoundingRules.round(0.337d, NutrientRoundingRuleType.Na.toString(), Locale.FRENCH, null), 0.34d,0);
		 assertEquals(NutrientRoundingRules.round(0.002d, NutrientRoundingRuleType.Na.toString(), Locale.FRENCH, null), 0.0d,0);
		 assertEquals(NutrientRoundingRules.round(0d, NutrientRoundingRuleType.Na.toString(), Locale.FRENCH, null), 0d,0);
		 assertEquals(NutrientRoundingRules.round(null, NutrientRoundingRuleType.Na.toString(), Locale.US, null), null);
		 assertEquals(NutrientRoundingRules.round(0.004d, NutrientRoundingRuleType.Na.toString(), Locale.US, null), 0d,0);
		 assertEquals(NutrientRoundingRules.round(0.121d, NutrientRoundingRuleType.Na.toString(), Locale.US, null), 0.125d,0);
		 assertEquals(NutrientRoundingRules.round(0.145d, NutrientRoundingRuleType.Na.toString(), Locale.US, null), 0.150d,0);
		
		 //Test Salt
		 assertEquals(NutrientRoundingRules.round(null, NutrientRoundingRuleType.Salt.toString(), Locale.FRENCH, null), null);
		 assertEquals(NutrientRoundingRules.round(1.46d, NutrientRoundingRuleType.Salt.toString(), Locale.FRENCH, null), 1.5d,0);
		 assertEquals(NutrientRoundingRules.round(0.037d, NutrientRoundingRuleType.Salt.toString(), Locale.FRENCH, null), 0.04d,0);
		 assertEquals(NutrientRoundingRules.round(0.0125d, NutrientRoundingRuleType.Salt.toString(), Locale.FRENCH, null), 0.0d,0);
		 assertEquals(NutrientRoundingRules.round(0.002d, NutrientRoundingRuleType.Salt.toString(), Locale.FRENCH, null), 0.0d,0);

		 

         //Test Sodium en mg/100g

         assertEquals(NutrientRoundingRules.round(10.36d, NutrientRoundingRuleType.Na.toString(), Locale.FRENCH, "mg/100g"), 10d,0);
         assertEquals(NutrientRoundingRules.round(101d, NutrientRoundingRuleType.Na.toString(), Locale.FRENCH, "mg/100g"), 100d,0);
         assertEquals(NutrientRoundingRules.round(1150d, NutrientRoundingRuleType.Na.toString(), Locale.FRENCH, "mg/100g"), 1200d,0);
         
         assertEquals(NutrientRoundingRules.round(10.36d, NutrientRoundingRuleType.Na.toString(), Locale.US, "mg/100g"), 15d,0);
         assertEquals(NutrientRoundingRules.round(101d, NutrientRoundingRuleType.Na.toString(), Locale.US, "mg/100g"), 105d,0);

		 
		 //Cholesterol
		 
		 assertEquals(NutrientRoundingRules.round(null, NutrientRoundingRuleType.Cholesterol.toString(), Locale.US, null), null);
		 assertEquals(NutrientRoundingRules.round(0.002d, NutrientRoundingRuleType.Cholesterol.toString(), Locale.US, null), 0d,0);
		 assertEquals(NutrientRoundingRules.round(0.003d, NutrientRoundingRuleType.Cholesterol.toString(), Locale.US, null), 0.005d,0);
		 assertEquals(NutrientRoundingRules.round(0.006d, NutrientRoundingRuleType.Cholesterol.toString(), Locale.US, null), 0.01d,0);
		 
		 
		 
		 
	}
	
}
















