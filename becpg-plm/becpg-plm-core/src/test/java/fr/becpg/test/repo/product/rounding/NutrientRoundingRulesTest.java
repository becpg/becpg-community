package fr.becpg.test.repo.product.rounding;

import static org.junit.Assert.*;

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.repo.product.formulation.rounding.NutrientRoundingRules;
import fr.becpg.repo.product.formulation.rounding.NutrientRoundingRules.NutrientRoundingRuleType;

public class NutrientRoundingRulesTest {

	public Log logger = LogFactory.getLog(NutrientRoundingRulesTest.class);
	
	@Test
	public void testRoundingRules() {
		//logger.info(NutrientRoundingRules.round(1.55d, NutrientRoundingRuleType.fat.toString(), Locale.FRENCH));
		
		//Test NRJ
		 assertEquals(NutrientRoundingRules.round(4253.6d, NutrientRoundingRuleType.NRJ.toString(), Locale.FRENCH, "kcal"), 4254d,0);
		 assertEquals(NutrientRoundingRules.round(4d, NutrientRoundingRuleType.NRJ.toString(), Locale.US, "kcal"), 0d,0);
		 assertEquals(NutrientRoundingRules.round(6d, NutrientRoundingRuleType.NRJ.toString(), Locale.US, "kcal"), 10d,0);
		 assertEquals(NutrientRoundingRules.round(50.37d, NutrientRoundingRuleType.NRJ.toString(), Locale.US, "kcal"), 60d,0);
		 assertEquals(NutrientRoundingRules.round(50.6d, NutrientRoundingRuleType.NRJ.toString(), Locale.US, "kcal"), 60d,0);
		 
		 
		 //Test fat
		 assertEquals(NutrientRoundingRules.round(null, NutrientRoundingRuleType.Fat.toString(), Locale.FRENCH, null), null);
		 assertEquals(NutrientRoundingRules.round(null, NutrientRoundingRuleType.Fat.toString(), Locale.US, null), null);
		 assertEquals(NutrientRoundingRules.round(11.45d, NutrientRoundingRuleType.Fat.toString(), Locale.FRENCH, null), 11d,0);
		 assertEquals(NutrientRoundingRules.round(11.45d, NutrientRoundingRuleType.Fat.toString(), Locale.US, null), 12d,0);
		 assertEquals(NutrientRoundingRules.round(0.2d, NutrientRoundingRuleType.Fat.toString(), Locale.FRENCH, null), 0d,0);
		 assertEquals(NutrientRoundingRules.round(0.2d, NutrientRoundingRuleType.Fat.toString(), Locale.US, null), 0d,0);
		 assertEquals(NutrientRoundingRules.round(0d, NutrientRoundingRuleType.Fat.toString(), Locale.FRENCH, null),0d,0 );
		 assertEquals(NutrientRoundingRules.round(0d, NutrientRoundingRuleType.Fat.toString(), Locale.US, null),0d,0 );
		 assertEquals(NutrientRoundingRules.round(4.33d, NutrientRoundingRuleType.Fat.toString(), Locale.FRENCH, null), 4.3d,0);
		 assertEquals(NutrientRoundingRules.round(4.3d, NutrientRoundingRuleType.Fat.toString(), Locale.US, null), 4.5d,0);
		
		 
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
		 assertEquals(NutrientRoundingRules.round(0.011d, NutrientRoundingRuleType.Na.toString(), Locale.FRENCH, null), 0.01d,0);
		 assertEquals(NutrientRoundingRules.round(0d, NutrientRoundingRuleType.Na.toString(), Locale.FRENCH, null), 0d,0);
		 assertEquals(NutrientRoundingRules.round(null, NutrientRoundingRuleType.Na.toString(), Locale.US, null), null);
		 assertEquals(NutrientRoundingRules.round(0.004d, NutrientRoundingRuleType.Na.toString(), Locale.US, null), 0d,0);
		 assertEquals(NutrientRoundingRules.round(0.121d, NutrientRoundingRuleType.Na.toString(), Locale.US, null), 0.125d,0);
		 assertEquals(NutrientRoundingRules.round(0.145d, NutrientRoundingRuleType.Na.toString(), Locale.US, null), 0.150d,0);
		
//		 //Test Salt
		 assertEquals(NutrientRoundingRules.round(null, NutrientRoundingRuleType.Salt.toString(), Locale.FRENCH, null), null);
		 assertEquals(NutrientRoundingRules.round(1.46d, NutrientRoundingRuleType.Salt.toString(), Locale.FRENCH, null), 1.5d,0);
		 assertEquals(NutrientRoundingRules.round(0.037d, NutrientRoundingRuleType.Salt.toString(), Locale.FRENCH, null), 0.04d,0);
		 assertEquals(NutrientRoundingRules.round(0.0125d, NutrientRoundingRuleType.Salt.toString(), Locale.FRENCH, null), 0.0d,0);
		 assertEquals(NutrientRoundingRules.round(0.002d, NutrientRoundingRuleType.Salt.toString(), Locale.FRENCH, null), 0.0d,0);
		 
//		 //test cholesterol
		 
		 
	}
	
}
