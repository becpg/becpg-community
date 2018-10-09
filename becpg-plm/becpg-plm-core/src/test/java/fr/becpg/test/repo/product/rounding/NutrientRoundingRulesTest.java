package fr.becpg.test.repo.product.rounding;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.repo.product.formulation.nutrient.NutrientFormulationHelper;
import fr.becpg.repo.product.formulation.nutrient.NutrientCode;

public class NutrientRoundingRulesTest{

	public Log logger = LogFactory.getLog(NutrientRoundingRulesTest.class);
	
	@Test
	public void testRoundingRules() {
		
		//Test NRJ
		assertEquals(NutrientFormulationHelper.round(11.45d, NutrientCode.FAT.toString(), Locale.FRENCH, null), 11d,0);
		 assertEquals(NutrientFormulationHelper.round(4253.6d, NutrientCode.ENER.toString(), Locale.FRENCH, "kcal"), 4254d,0);
		 assertEquals(NutrientFormulationHelper.round(51d, NutrientCode.ENER.toString(), Locale.US, "kcal"), 60d,0);
		 assertEquals(NutrientFormulationHelper.round(11d, NutrientCode.ENER.toString(), Locale.US, "kcal"), 15d,0);
		 assertEquals(NutrientFormulationHelper.round(4d, NutrientCode.ENER.toString(), Locale.US, "kcal"), 0d,0);
		 

         //Test NRJ en KJ/100g

         assertEquals(NutrientFormulationHelper.round(250d, NutrientCode.ENER.toString(), Locale.US, "KJ/100g"), 251d,0);
         assertEquals(NutrientFormulationHelper.round(50d, NutrientCode.ENER.toString(), Locale.US, "KJ/100g"), 63d,0);
         assertEquals(NutrientFormulationHelper.round(10d, NutrientCode.ENER.toString(), Locale.US, "KJ/100g"), 0d,0);
		 
		 //Test fat
		 assertEquals(NutrientFormulationHelper.round(null, NutrientCode.FAT.toString(), Locale.FRENCH, null), null);
		 assertEquals(NutrientFormulationHelper.round(null, NutrientCode.FAT.toString(), Locale.US, null), null);
		 assertEquals(NutrientFormulationHelper.round(11.45d, NutrientCode.FAT.toString(), Locale.US, null), 12d,0);
		 assertEquals(NutrientFormulationHelper.round(0.2d, NutrientCode.FAT.toString(), Locale.FRENCH, null), 0d,0);
		 assertEquals(NutrientFormulationHelper.round(0.2d, NutrientCode.FAT.toString(), Locale.US, null), 0d,0);
		 assertEquals(NutrientFormulationHelper.round(4.33d, NutrientCode.FAT.toString(), Locale.FRENCH, null), 4.3d,0);
		 assertEquals(NutrientFormulationHelper.round(4.33d, NutrientCode.FAT.toString(), Locale.US, null), 4.5d,0);
		
		 
		 //Test saturated fat 
		 assertEquals(NutrientFormulationHelper.round(null, NutrientCode.FASAT.toString(), Locale.FRENCH, null), null);
		 assertEquals(NutrientFormulationHelper.round(11.6d, NutrientCode.FASAT.toString(), Locale.FRENCH, null), 12d,0);
		 assertEquals(NutrientFormulationHelper.round(11.6d, NutrientCode.FASAT.toString(), Locale.GERMAN, null), 12d,0);
		 assertEquals(NutrientFormulationHelper.round(8.63d, NutrientCode.FASAT.toString(), Locale.FRENCH, null), 8.6d,0);
		 assertEquals(NutrientFormulationHelper.round(0.2d, NutrientCode.FASAT.toString(), Locale.FRENCH, null), 0.2d,0);
		 assertEquals(NutrientFormulationHelper.round(0.01d, NutrientCode.FASAT.toString(), Locale.FRENCH, null), 0.0d,0);
		 assertEquals(NutrientFormulationHelper.round(0d, NutrientCode.FASAT.toString(), Locale.FRENCH, null), 0d,0);
		 assertEquals(NutrientFormulationHelper.round(null, NutrientCode.FASAT.toString(), Locale.US, null), null);
		 assertEquals(NutrientFormulationHelper.round(4.3d, NutrientCode.FASAT.toString(), Locale.US, null), 4.5d,0);
		 assertEquals(NutrientFormulationHelper.round(8.63d, NutrientCode.FASAT.toString(), Locale.US, null), 9d,0);
		 assertEquals(NutrientFormulationHelper.round(0.2d, NutrientCode.FASAT.toString(), Locale.US, null), 0.0d,0);
				 
		//Test Sodium en g
		 assertEquals(NutrientFormulationHelper.round(null, NutrientCode.NA.toString(), Locale.FRENCH, null), null);
		 assertEquals(NutrientFormulationHelper.round(10.36d, NutrientCode.NA.toString(), Locale.FRENCH, null), 10.4d,0);
		 assertEquals(NutrientFormulationHelper.round(10.36d, NutrientCode.NA.toString(), Locale.GERMAN,null), 10.4d,0);
		 assertEquals(NutrientFormulationHelper.round(0.337d, NutrientCode.NA.toString(), Locale.FRENCH, null), 0.34d,0);
		 assertEquals(NutrientFormulationHelper.round(0.002d, NutrientCode.NA.toString(), Locale.FRENCH, null), 0.0d,0);
		 assertEquals(NutrientFormulationHelper.round(0d, NutrientCode.NA.toString(), Locale.FRENCH, null), 0d,0);
		 assertEquals(NutrientFormulationHelper.round(null, NutrientCode.NA.toString(), Locale.US, null), null);
		 assertEquals(NutrientFormulationHelper.round(0.004d, NutrientCode.NA.toString(), Locale.US, null), 0d,0);
		 assertEquals(NutrientFormulationHelper.round(0.121d, NutrientCode.NA.toString(), Locale.US, null), 0.125d,0);
		 assertEquals(NutrientFormulationHelper.round(0.145d, NutrientCode.NA.toString(), Locale.US, null), 0.150d,0);
		
		 //Test Salt
		 assertEquals(NutrientFormulationHelper.round(null, NutrientCode.NACL.toString(), Locale.FRENCH, null), null);
		 assertEquals(NutrientFormulationHelper.round(1.46d, NutrientCode.NACL.toString(), Locale.FRENCH, null), 1.5d,0);
		 assertEquals(NutrientFormulationHelper.round(0.037d, NutrientCode.NACL.toString(), Locale.FRENCH, null), 0.04d,0);
		 assertEquals(NutrientFormulationHelper.round(0.0125d, NutrientCode.NACL.toString(), Locale.FRENCH, null), 0.0d,0);
		 assertEquals(NutrientFormulationHelper.round(0.002d, NutrientCode.NACL.toString(), Locale.FRENCH, null), 0.0d,0);

         //Test Sodium en mg/100g

         assertEquals(NutrientFormulationHelper.round(10.36d, NutrientCode.NA.toString(), Locale.FRENCH, "mg/100g"), 10d,0);
         assertEquals(NutrientFormulationHelper.round(101d, NutrientCode.NA.toString(), Locale.FRENCH, "mg/100g"), 100d,0);
         assertEquals(NutrientFormulationHelper.round(1150d, NutrientCode.NA.toString(), Locale.FRENCH, "mg/100g"), 1200d,0);
         
         assertEquals(NutrientFormulationHelper.round(10.36d, NutrientCode.NA.toString(), Locale.US, "mg/100g"), 15d,0);
         assertEquals(NutrientFormulationHelper.round(101d, NutrientCode.NA.toString(), Locale.US, "mg/100g"), 105d,0);

		 
		 //Cholesterol
		 
		 assertEquals(NutrientFormulationHelper.round(null, NutrientCode.CHOL.toString(), Locale.US, null), null);
		 assertEquals(NutrientFormulationHelper.round(0.002d, NutrientCode.CHOL.toString(), Locale.US, null), 0d,0);
		 assertEquals(NutrientFormulationHelper.round(0.003d, NutrientCode.CHOL.toString(), Locale.US, null), 0.005d,0);
		 assertEquals(NutrientFormulationHelper.round(0.006d, NutrientCode.CHOL.toString(), Locale.US, null), 0.01d,0);
		 
	 
	}
	
}
















