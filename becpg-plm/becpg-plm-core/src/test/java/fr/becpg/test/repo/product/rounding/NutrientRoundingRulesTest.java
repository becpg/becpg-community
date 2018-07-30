package fr.becpg.test.repo.product.rounding;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.repo.product.formulation.rounding.NutrientRoundingRules;
import fr.becpg.repo.product.formulation.rounding.NutrientTypeCode;

public class NutrientRoundingRulesTest{

	public Log logger = LogFactory.getLog(NutrientRoundingRulesTest.class);
	
	@Test
	public void testRoundingRules() {
		//logger.info(NutrientRoundingRules.round(1.55d, NutrientRoundingRuleType.fat.toString(), Locale.FRENCH));
		
		//Test NRJ
		 assertEquals(NutrientRoundingRules.round(4253.6d, NutrientTypeCode.NRJ.toString(), Locale.FRENCH, "kcal"), 4254d,0);
		 assertEquals(NutrientRoundingRules.round(0.055d, NutrientTypeCode.NRJ.toString(), Locale.US, "kcal"), 0.06d,0);
		 assertEquals(NutrientRoundingRules.round(0.014d, NutrientTypeCode.NRJ.toString(), Locale.US, "kcal"), 0.015d,0);
		 assertEquals(NutrientRoundingRules.round(0.004d, NutrientTypeCode.NRJ.toString(), Locale.US, "kcal"), 0d,0);
		 

         //Test NRJ en KJ/100g

         assertEquals(NutrientRoundingRules.round(0.013d, NutrientTypeCode.NRJ.toString(), Locale.US, "KJ/100g"), 0d,0);
         assertEquals(NutrientRoundingRules.round(0.16d, NutrientTypeCode.NRJ.toString(), Locale.US, "KJ/100g"), 0.17d,0);
         assertEquals(NutrientRoundingRules.round(0.433d, NutrientTypeCode.NRJ.toString(), Locale.US, "KJ/100g"), 0.46d,0);
		 
		 //Test fat
		 assertEquals(NutrientRoundingRules.round(null, NutrientTypeCode.Fat.toString(), Locale.FRENCH, null), null);
		 assertEquals(NutrientRoundingRules.round(null, NutrientTypeCode.Fat.toString(), Locale.US, null), null);
		 assertEquals(NutrientRoundingRules.round(11.45d, NutrientTypeCode.Fat.toString(), Locale.FRENCH, null), 11d,0);
		 assertEquals(NutrientRoundingRules.round(11.45d, NutrientTypeCode.Fat.toString(), Locale.US, null), 12d,0);
		 assertEquals(NutrientRoundingRules.round(0.2d, NutrientTypeCode.Fat.toString(), Locale.FRENCH, null), 0d,0);
		 assertEquals(NutrientRoundingRules.round(0.2d, NutrientTypeCode.Fat.toString(), Locale.US, null), 0d,0);
		 assertEquals(NutrientRoundingRules.round(4.33d, NutrientTypeCode.Fat.toString(), Locale.FRENCH, null), 4.3d,0);
		 assertEquals(NutrientRoundingRules.round(4.33d, NutrientTypeCode.Fat.toString(), Locale.US, null), 4.5d,0);
		
		 
		 //Test saturated fat 
		 assertEquals(NutrientRoundingRules.round(null, NutrientTypeCode.SatFat.toString(), Locale.FRENCH, null), null);
		 assertEquals(NutrientRoundingRules.round(11.6d, NutrientTypeCode.SatFat.toString(), Locale.FRENCH, null), 12d,0);
		 assertEquals(NutrientRoundingRules.round(11.6d, NutrientTypeCode.SatFat.toString(), Locale.GERMAN, null), 12d,0);
		 assertEquals(NutrientRoundingRules.round(8.63d, NutrientTypeCode.SatFat.toString(), Locale.FRENCH, null), 8.6d,0);
		 assertEquals(NutrientRoundingRules.round(0.2d, NutrientTypeCode.SatFat.toString(), Locale.FRENCH, null), 0.2d,0);
		 assertEquals(NutrientRoundingRules.round(0.01d, NutrientTypeCode.SatFat.toString(), Locale.FRENCH, null), 0.0d,0);
		 assertEquals(NutrientRoundingRules.round(0d, NutrientTypeCode.SatFat.toString(), Locale.FRENCH, null), 0d,0);
		 assertEquals(NutrientRoundingRules.round(null, NutrientTypeCode.SatFat.toString(), Locale.US, null), null);
		 assertEquals(NutrientRoundingRules.round(4.3d, NutrientTypeCode.SatFat.toString(), Locale.US, null), 4.5d,0);
		 assertEquals(NutrientRoundingRules.round(8.63d, NutrientTypeCode.SatFat.toString(), Locale.US, null), 9d,0);
		 assertEquals(NutrientRoundingRules.round(0.2d, NutrientTypeCode.SatFat.toString(), Locale.US, null), 0.0d,0);
				 
		//Test Sodium en g
		 assertEquals(NutrientRoundingRules.round(null, NutrientTypeCode.Na.toString(), Locale.FRENCH, null), null);
		 assertEquals(NutrientRoundingRules.round(10.36d, NutrientTypeCode.Na.toString(), Locale.FRENCH, null), 10.4d,0);
		 assertEquals(NutrientRoundingRules.round(10.36d, NutrientTypeCode.Na.toString(), Locale.GERMAN,null), 10.4d,0);
		 assertEquals(NutrientRoundingRules.round(0.337d, NutrientTypeCode.Na.toString(), Locale.FRENCH, null), 0.34d,0);
		 assertEquals(NutrientRoundingRules.round(0.002d, NutrientTypeCode.Na.toString(), Locale.FRENCH, null), 0.0d,0);
		 assertEquals(NutrientRoundingRules.round(0d, NutrientTypeCode.Na.toString(), Locale.FRENCH, null), 0d,0);
		 assertEquals(NutrientRoundingRules.round(null, NutrientTypeCode.Na.toString(), Locale.US, null), null);
		 assertEquals(NutrientRoundingRules.round(0.004d, NutrientTypeCode.Na.toString(), Locale.US, null), 0d,0);
		 assertEquals(NutrientRoundingRules.round(0.121d, NutrientTypeCode.Na.toString(), Locale.US, null), 0.125d,0);
		 assertEquals(NutrientRoundingRules.round(0.145d, NutrientTypeCode.Na.toString(), Locale.US, null), 0.150d,0);
		
		 //Test Salt
		 assertEquals(NutrientRoundingRules.round(null, NutrientTypeCode.Salt.toString(), Locale.FRENCH, null), null);
		 assertEquals(NutrientRoundingRules.round(1.46d, NutrientTypeCode.Salt.toString(), Locale.FRENCH, null), 1.5d,0);
		 assertEquals(NutrientRoundingRules.round(0.037d, NutrientTypeCode.Salt.toString(), Locale.FRENCH, null), 0.04d,0);
		 assertEquals(NutrientRoundingRules.round(0.0125d, NutrientTypeCode.Salt.toString(), Locale.FRENCH, null), 0.0d,0);
		 assertEquals(NutrientRoundingRules.round(0.002d, NutrientTypeCode.Salt.toString(), Locale.FRENCH, null), 0.0d,0);

		 

         //Test Sodium en mg/100g

         assertEquals(NutrientRoundingRules.round(10.36d, NutrientTypeCode.Na.toString(), Locale.FRENCH, "mg/100g"), 10d,0);
         assertEquals(NutrientRoundingRules.round(101d, NutrientTypeCode.Na.toString(), Locale.FRENCH, "mg/100g"), 100d,0);
         assertEquals(NutrientRoundingRules.round(1150d, NutrientTypeCode.Na.toString(), Locale.FRENCH, "mg/100g"), 1200d,0);
         
         assertEquals(NutrientRoundingRules.round(10.36d, NutrientTypeCode.Na.toString(), Locale.US, "mg/100g"), 15d,0);
         assertEquals(NutrientRoundingRules.round(101d, NutrientTypeCode.Na.toString(), Locale.US, "mg/100g"), 105d,0);

		 
		 //Cholesterol
		 
		 assertEquals(NutrientRoundingRules.round(null, NutrientTypeCode.Cholesterol.toString(), Locale.US, null), null);
		 assertEquals(NutrientRoundingRules.round(0.002d, NutrientTypeCode.Cholesterol.toString(), Locale.US, null), 0d,0);
		 assertEquals(NutrientRoundingRules.round(0.003d, NutrientTypeCode.Cholesterol.toString(), Locale.US, null), 0.005d,0);
		 assertEquals(NutrientRoundingRules.round(0.006d, NutrientTypeCode.Cholesterol.toString(), Locale.US, null), 0.01d,0);
		 
		 
		 
		 
	}
	
}
















