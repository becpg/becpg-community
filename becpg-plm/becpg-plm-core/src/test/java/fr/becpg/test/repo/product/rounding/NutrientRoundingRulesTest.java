package fr.becpg.test.repo.product.rounding;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.formulation.nutrient.NutrientCode;
import fr.becpg.repo.product.formulation.nutrient.NutrientFormulationHelper;

public class NutrientRoundingRulesTest {

	public Log logger = LogFactory.getLog(NutrientRoundingRulesTest.class);
	
	@Autowired
	protected MLTextHelper mlTextHelper;

	@Test
	public void testUSRoundingRules() {
		assertEquals(110d, NutrientFormulationHelper.round(111d, NutrientCode.EnergykcalUS, Locale.US, "kcal"), 0);
		assertEquals(110d, NutrientFormulationHelper.round(109d, NutrientCode.EnergykcalUS, Locale.US, "kcal"), 0);
		assertEquals(10d, NutrientFormulationHelper.round(11d, NutrientCode.EnergykcalUS, Locale.US, "kcal"), 0);
		assertEquals(15d, NutrientFormulationHelper.round(16d, NutrientCode.EnergykcalUS, Locale.US, "kcal"), 0);
		assertEquals(20d, NutrientFormulationHelper.round(18d, NutrientCode.EnergykcalUS, Locale.US, "kcal"), 0);
		assertEquals(0d, NutrientFormulationHelper.round(4d, NutrientCode.EnergykcalUS, Locale.US, "kcal"), 0);

		assertEquals(11d, NutrientFormulationHelper.round(11.45d, NutrientCode.Fat, Locale.US, "g/100g"), 0);
		assertEquals(4.5d, NutrientFormulationHelper.round(4.33d, NutrientCode.Fat, Locale.US, "g/100g"), 0);
		assertEquals(4.5d, NutrientFormulationHelper.round(4.55d, NutrientCode.Fat, Locale.US, "g/100g"), 0);
		assertEquals(5d, NutrientFormulationHelper.round(4.95d, NutrientCode.Fat, Locale.US, "g/100g"), 0);
		assertEquals(0d, NutrientFormulationHelper.round(0.2d, NutrientCode.Fat, Locale.US, "g/100g"), 0);

		assertEquals(150d, NutrientFormulationHelper.round(149d, NutrientCode.Sodium, Locale.US, "mg/100g"), 0);
		assertEquals(150d, NutrientFormulationHelper.round(151d, NutrientCode.Sodium, Locale.US, "mg/100g"), 0);
		assertEquals(160d, NutrientFormulationHelper.round(156d, NutrientCode.Sodium, Locale.US, "mg/100g"), 0);
		assertEquals(160d, NutrientFormulationHelper.round(159d, NutrientCode.Sodium, Locale.US, "mg/100g"), 0);
		assertEquals(100d, NutrientFormulationHelper.round(99d, NutrientCode.Sodium, Locale.US, "mg/100g"), 0);
		assertEquals(100d, NutrientFormulationHelper.round(101d, NutrientCode.Sodium, Locale.US, "mg/100g"), 0);
		assertEquals(105d, NutrientFormulationHelper.round(106d, NutrientCode.Sodium, Locale.US, "mg/100g"), 0);
		assertEquals(110d, NutrientFormulationHelper.round(109d, NutrientCode.Sodium, Locale.US, "mg/100g"), 0);
		assertEquals(0d, NutrientFormulationHelper.round(2d, NutrientCode.Sodium, Locale.US, "mg/100g"), 0);
		
		assertEquals(null, NutrientFormulationHelper.round(null, NutrientCode.Cholesterol, Locale.US, "mg/100g"));
		assertEquals(20d, NutrientFormulationHelper.round(19d, NutrientCode.Cholesterol, Locale.US, "mg/100g"), 0);
		assertEquals(20d, NutrientFormulationHelper.round(21d, NutrientCode.Cholesterol, Locale.US, "mg/100g"), 0);
		assertEquals(25d, NutrientFormulationHelper.round(26d, NutrientCode.Cholesterol, Locale.US, "mg/100g"), 0);
		assertEquals(30d, NutrientFormulationHelper.round(29d, NutrientCode.Cholesterol, Locale.US, "mg/100g"), 0);
		assertEquals(35d, NutrientFormulationHelper.round(33.7d, NutrientCode.Cholesterol, Locale.US, "mg/100g"), 0);
		assertEquals(3d, NutrientFormulationHelper.round(3d, NutrientCode.Cholesterol, Locale.US, "mg/100g"), 0);
		assertEquals(0d, NutrientFormulationHelper.round(1d, NutrientCode.Cholesterol, Locale.US, "mg/100g"), 0);
		
		assertEquals(null, NutrientFormulationHelper.round(null, NutrientCode.Sugar, Locale.US, "g/100g"));
		assertEquals(19d, NutrientFormulationHelper.round(18.8d, NutrientCode.Sugar, Locale.US, "g/100g"), 0);
		assertEquals(19d, NutrientFormulationHelper.round(19.1d, NutrientCode.Sugar, Locale.US, "g/100g"), 0);
		assertEquals(19d, NutrientFormulationHelper.round(19.4d, NutrientCode.Sugar, Locale.US, "g/100g"), 0);
		assertEquals(20d, NutrientFormulationHelper.round(19.6d, NutrientCode.Sugar, Locale.US, "g/100g"), 0);
		assertEquals(1d, NutrientFormulationHelper.round(0.7d, NutrientCode.Sugar, Locale.US, "g/100g"), 0);
		assertEquals(1d, NutrientFormulationHelper.round(0.6d, NutrientCode.Sugar, Locale.US, "g/100g"), 0);
		assertEquals(0d, NutrientFormulationHelper.round(0.4d, NutrientCode.Sugar, Locale.US, "g/100g"), 0);
		assertEquals(0d, NutrientFormulationHelper.round(0.18d, NutrientCode.Sugar, Locale.US, "g/100g"), 0);
		
		assertEquals(1.2d, NutrientFormulationHelper.round(1.23d, NutrientCode.VitD, Locale.US, "μg/100g"), 0);
		assertEquals(30d, NutrientFormulationHelper.round(26.3d, NutrientCode.Calcium, Locale.US, "mg/100g"), 0);
		assertEquals(2.4d, NutrientFormulationHelper.round(2.38d, NutrientCode.Iron, Locale.US, "mg/100g"), 0);
		assertEquals(40d, NutrientFormulationHelper.round(42.4d, NutrientCode.Potassium, Locale.US, "mg/100g"), 0);
		
		assertEquals(40d, NutrientFormulationHelper.round(39d, NutrientCode.VitA, Locale.US, "μg/100g"), 0);
		assertEquals(1d, NutrientFormulationHelper.round(0.59d, NutrientCode.VitC, Locale.US, "mg/100g"), 0);
		assertEquals(3.3d, NutrientFormulationHelper.round(3.28d, NutrientCode.VitE, Locale.US, "mg/100g"), 0);
		assertEquals(4d, NutrientFormulationHelper.round(4.25d, NutrientCode.VitK1, Locale.US, "μg/100g"), 0);
		assertEquals(3d, NutrientFormulationHelper.round(2.86d, NutrientCode.VitK2, Locale.US, "μg/100g"), 0);
		assertEquals(6.39d, NutrientFormulationHelper.round(6.392d, NutrientCode.Thiamin, Locale.US, "mg/100g"), 0);
		assertEquals(2.13d, NutrientFormulationHelper.round(2.127d, NutrientCode.Riboflavin, Locale.US, "mg/100g"), 0);
		assertEquals(4d, NutrientFormulationHelper.round(3.98d, NutrientCode.Niacin, Locale.US, "mg/100g"), 0);
		assertEquals(6.75d, NutrientFormulationHelper.round(6.754d, NutrientCode.VitB6, Locale.US, "mg/100g"), 0);
		assertEquals(15d, NutrientFormulationHelper.round(13.5d, NutrientCode.Folate, Locale.US, "μg/100g"), 0);
		assertEquals(1.23d, NutrientFormulationHelper.round(1.232d, NutrientCode.VitB12, Locale.US, "μg/100g"), 0);
		assertEquals(0.2d, NutrientFormulationHelper.round(0.16d, NutrientCode.Biotin, Locale.US, "μg/100g"), 0);
		assertEquals(1.2d, NutrientFormulationHelper.round(1.23d, NutrientCode.PantoAcid, Locale.US, "mg/100g"), 0);
		assertEquals(20d, NutrientFormulationHelper.round(16.9d, NutrientCode.Phosphorus, Locale.US, "mg/100g"), 0);
		assertEquals(2d, NutrientFormulationHelper.round(1.83d, NutrientCode.Iodine, Locale.US, "μg/100g"), 0);
		assertEquals(15d, NutrientFormulationHelper.round(12.6d, NutrientCode.Magnesium, Locale.US, "mg/100g"), 0);
		assertEquals(8.3d, NutrientFormulationHelper.round(8.29d, NutrientCode.Zinc, Locale.US, "mg/100g"), 0);
		assertEquals(9d, NutrientFormulationHelper.round(8.6d, NutrientCode.Selenium, Locale.US, "μg/100g"), 0);
		assertEquals(5.16d, NutrientFormulationHelper.round(5.157d, NutrientCode.Copper, Locale.US, "mg/100g"), 0);
		assertEquals(2.41d, NutrientFormulationHelper.round(2.411d, NutrientCode.Manganese, Locale.US, "mg/100g"), 0);
		assertEquals(1.7d, NutrientFormulationHelper.round(1.69d, NutrientCode.Chromium, Locale.US, "μg/100g"), 0);
		assertEquals(1.2d, NutrientFormulationHelper.round(1.23d, NutrientCode.Molybdenum, Locale.US, "μg/100g"), 0);
		assertEquals(50d, NutrientFormulationHelper.round(54d, NutrientCode.Chloride, Locale.US, "mg/100g"), 0);
		assertEquals(80d, NutrientFormulationHelper.round(78d, NutrientCode.Choline, Locale.US, "mg/100g"), 0);
		
		// less than
		assertEquals("<5", NutrientFormulationHelper.displayValue(3d,
				NutrientFormulationHelper.round(3d, NutrientCode.Cholesterol, Locale.US, "mg/100g"), NutrientCode.Cholesterol, Locale.US));
		assertEquals("0", NutrientFormulationHelper.displayValue(1d,
				NutrientFormulationHelper.round(1d, NutrientCode.Cholesterol, Locale.US, "mg/100g"), NutrientCode.Cholesterol, Locale.US));

		assertEquals("35", NutrientFormulationHelper.displayValue(33.7d,
				NutrientFormulationHelper.round(33.7d, NutrientCode.Cholesterol, Locale.US, "mg/100g"), NutrientCode.Cholesterol, Locale.US));

		String[] codes = { NutrientCode.CarbohydrateWithFiber, NutrientCode.Sugar, NutrientCode.SugarAdded, NutrientCode.FiberDietary,
				NutrientCode.FiberInsoluble, NutrientCode.FiberSoluble, NutrientCode.Protein, NutrientCode.Polyols };

		for (String code : codes) {
			assertEquals("<1",
					NutrientFormulationHelper.displayValue(0.9d, NutrientFormulationHelper.round(0.9d, code, Locale.US, "g/100g"), code, Locale.US));

			assertEquals("9",
					NutrientFormulationHelper.displayValue(9.1d, NutrientFormulationHelper.round(9.1d, code, Locale.US, "g/100g"), code, Locale.US));
		}
		// gda
		assertEquals(6d, NutrientFormulationHelper.roundGDA(5.4d, NutrientCode.VitA, Locale.US), 0);
		assertEquals(15d, NutrientFormulationHelper.roundGDA(15.5d, NutrientCode.VitA, Locale.US), 0);
		assertEquals(20d, NutrientFormulationHelper.roundGDA(18d, NutrientCode.VitA, Locale.US), 0);
		assertEquals(60d, NutrientFormulationHelper.roundGDA(56d, NutrientCode.VitA, Locale.US), 0);
		assertEquals(5d, NutrientFormulationHelper.roundGDA(5.4d, NutrientCode.Sodium, Locale.US), 0);
		assertEquals(3.7d, NutrientFormulationHelper.round(3.7d, NutrientCode.VitD, Locale.US, "µg/100g"), 0);
		assertEquals("3.7", NutrientFormulationHelper.displayValue(3.7d, NutrientFormulationHelper.round(3.7d, NutrientCode.VitD, Locale.US, "µg/100g"),
				NutrientCode.VitD, Locale.US));
		assertEquals("0", NutrientFormulationHelper.displayValue(3.7d, NutrientFormulationHelper.round(3.7d, NutrientCode.VitA, Locale.US, "µg/100g"),
				NutrientCode.VitA, Locale.US));
	}

	@Test
	public void testEuropeanRoundingRules() {

		assertEquals(11d, NutrientFormulationHelper.round(11.45d, NutrientCode.Fat, Locale.FRENCH, null), 0);
		assertEquals(4254d, NutrientFormulationHelper.round(4253.6d, NutrientCode.Energykcal, Locale.FRENCH, "kcal"), 0);

		assertEquals(null, NutrientFormulationHelper.round(null, NutrientCode.Fat, Locale.FRENCH, "g/100g"));
		assertEquals(0d, NutrientFormulationHelper.round(0.2d, NutrientCode.Fat, Locale.FRENCH, "g/100g"), 0);
		assertEquals(4.3d, NutrientFormulationHelper.round(4.33d, NutrientCode.Fat, Locale.FRENCH, "g/100g"), 0);
		
		assertEquals(null, NutrientFormulationHelper.round(null, NutrientCode.FatSaturated, Locale.FRENCH, null));
		assertEquals(12d, NutrientFormulationHelper.round(11.6d, NutrientCode.FatSaturated, Locale.FRENCH, null), 0);
		assertEquals(12d, NutrientFormulationHelper.round(11.6d, NutrientCode.FatSaturated, Locale.GERMAN, null), 0);
		assertEquals(8.6d, NutrientFormulationHelper.round(8.63d, NutrientCode.FatSaturated, Locale.FRENCH, null), 0);
		assertEquals(0.2d, NutrientFormulationHelper.round(0.2d, NutrientCode.FatSaturated, Locale.FRENCH, null), 0);
		assertEquals(0.0d, NutrientFormulationHelper.round(0.01d, NutrientCode.FatSaturated, Locale.FRENCH, null), 0);
		assertEquals(0d, NutrientFormulationHelper.round(0d, NutrientCode.FatSaturated, Locale.FRENCH, null), 0);
		
		assertEquals(null, NutrientFormulationHelper.round(null, NutrientCode.Sodium, Locale.FRENCH, "g/100g"));
		assertEquals(10.4d, NutrientFormulationHelper.round(10.36d, NutrientCode.Sodium, Locale.FRENCH, "g/100g"), 0);
		assertEquals(10.4d, NutrientFormulationHelper.round(10.36d, NutrientCode.Sodium, Locale.GERMAN, "g/100g"), 0);
		assertEquals(0.34d, NutrientFormulationHelper.round(0.337d, NutrientCode.Sodium, Locale.FRENCH, "g/100g"), 0);
		assertEquals(0.0d, NutrientFormulationHelper.round(0.002d, NutrientCode.Sodium, Locale.FRENCH, "g/100g"), 0);
		assertEquals(0d, NutrientFormulationHelper.round(0d, NutrientCode.Sodium, Locale.FRENCH, "g/100g"), 0);
		
		assertEquals(null, NutrientFormulationHelper.round(null, NutrientCode.Salt, Locale.FRENCH, "g/100g"));
		assertEquals(1.5d, NutrientFormulationHelper.round(1.46d, NutrientCode.Salt, Locale.FRENCH, "g/100g"), 0);
		assertEquals(0.04d, NutrientFormulationHelper.round(0.037d, NutrientCode.Salt, Locale.FRENCH, "g/100g"), 0);
		assertEquals(0.0d, NutrientFormulationHelper.round(0.0125d, NutrientCode.Salt, Locale.FRENCH, "g/100g"), 0);
		assertEquals(0.0d, NutrientFormulationHelper.round(0.002d, NutrientCode.Salt, Locale.FRENCH, "g/100g"), 0);

		// display in g
		assertEquals(0.01d, NutrientFormulationHelper.round(10.36d, NutrientCode.Sodium, Locale.FRENCH, "mg/100g"), 0);
		assertEquals(0.10d, NutrientFormulationHelper.round(101d, NutrientCode.Sodium, Locale.FRENCH, "mg/100g"), 0);
		assertEquals(1.1d, NutrientFormulationHelper.round(1150d, NutrientCode.Sodium, Locale.FRENCH, "mg/100g"), 0);
		
		// less than
		assertEquals(null, NutrientFormulationHelper.displayValue(null,
				NutrientFormulationHelper.round(null, NutrientCode.Fat, Locale.FRENCH, "g/100g"), NutrientCode.Fat, Locale.FRENCH));
		assertEquals("< 0,5", NutrientFormulationHelper.displayValue(0.4d,
				NutrientFormulationHelper.round(0.4d, NutrientCode.Fat, Locale.FRENCH, "g/100g"), NutrientCode.Fat, Locale.FRENCH));
		assertEquals("< 0,1",
				NutrientFormulationHelper.displayValue(0.09d,
						NutrientFormulationHelper.round(0.09d, NutrientCode.FatSaturated, Locale.FRENCH, "g/100g"), NutrientCode.FatSaturated,
						Locale.FRENCH));
		assertEquals("< 0,005", NutrientFormulationHelper.displayValue(0.004d,
				NutrientFormulationHelper.round(0.004d, NutrientCode.Sodium, Locale.FRENCH, "g/100g"), NutrientCode.Sodium, Locale.FRENCH));
		assertEquals("< 0,01", NutrientFormulationHelper.displayValue(0.0122d,
				NutrientFormulationHelper.round(0.0122d, NutrientCode.Salt, Locale.FRENCH, "g/100g"), NutrientCode.Salt, Locale.FRENCH));
		assertEquals("< 0.01", NutrientFormulationHelper.displayValue(0.0122d,
				NutrientFormulationHelper.round(0.0122d, NutrientCode.Salt, Locale.FRENCH, "g/100g"), NutrientCode.Salt, Locale.ENGLISH));

		String[] codes = { NutrientCode.CarbohydrateByDiff, NutrientCode.Sugar, NutrientCode.FiberDietary, NutrientCode.Protein };

		for (String code : codes) {
			assertEquals("< 0,5", NutrientFormulationHelper.displayValue(0.4d, NutrientFormulationHelper.round(0.4d, code, Locale.FRENCH, "g/100g"),
					code, Locale.FRENCH));
			assertEquals("19", NutrientFormulationHelper.displayValue(19.1d, NutrientFormulationHelper.round(19.1d, code, Locale.FRENCH, "g/100g"),
					code, Locale.FRENCH));
		}
		
		assertEquals(3.78d, NutrientFormulationHelper.round(3.7777d, NutrientCode.VitA, Locale.FRENCH, "µg/100g"), 0);
		assertEquals(3.8d, NutrientFormulationHelper.round(3.7777d, NutrientCode.VitD, Locale.FRENCH, "µg/100g"), 0);
	}
	
	@Test
	public void testCanadianRoundingRules() {
		assertEquals(110d, NutrientFormulationHelper.round(111d, NutrientCode.EnergykcalUS, Locale.CANADA, "kcal"), 0);
		assertEquals(110d, NutrientFormulationHelper.round(109d, NutrientCode.EnergykcalUS, Locale.CANADA, "kcal"), 0);
		assertEquals(10d, NutrientFormulationHelper.round(11d, NutrientCode.EnergykcalUS, Locale.CANADA, "kcal"), 0);
		assertEquals(15d, NutrientFormulationHelper.round(16d, NutrientCode.EnergykcalUS, Locale.CANADA, "kcal"), 0);
		assertEquals(20d, NutrientFormulationHelper.round(18d, NutrientCode.EnergykcalUS, Locale.CANADA, "kcal"), 0);
		assertEquals(4d, NutrientFormulationHelper.round(4d, NutrientCode.EnergykcalUS, Locale.CANADA, "kcal"), 0);

		assertEquals(11d, NutrientFormulationHelper.round(11.45d, NutrientCode.Fat, Locale.CANADA, "g/100g"), 0);
		assertEquals(4.5d, NutrientFormulationHelper.round(4.33d, NutrientCode.Fat, Locale.CANADA, "g/100g"), 0);
		assertEquals(4.5d, NutrientFormulationHelper.round(4.55d, NutrientCode.Fat, Locale.CANADA, "g/100g"), 0);
		assertEquals(5d, NutrientFormulationHelper.round(4.95d, NutrientCode.Fat, Locale.CANADA, "g/100g"), 0);
		assertEquals(0.2d, NutrientFormulationHelper.round(0.22d, NutrientCode.Fat, Locale.CANADA, "g/100g"), 0);
		assertEquals(0.3d, NutrientFormulationHelper.round(0.26d, NutrientCode.Fat, Locale.CANADA, "g/100g"), 0);

		assertEquals(150d, NutrientFormulationHelper.round(149d, NutrientCode.Sodium, Locale.CANADA, "mg/100g"), 0);
		assertEquals(150d, NutrientFormulationHelper.round(151d, NutrientCode.Sodium, Locale.CANADA, "mg/100g"), 0);
		assertEquals(160d, NutrientFormulationHelper.round(156d, NutrientCode.Sodium, Locale.CANADA, "mg/100g"), 0);
		assertEquals(160d, NutrientFormulationHelper.round(159d, NutrientCode.Sodium, Locale.CANADA, "mg/100g"), 0);
		assertEquals(100d, NutrientFormulationHelper.round(99d, NutrientCode.Sodium, Locale.CANADA, "mg/100g"), 0);
		assertEquals(100d, NutrientFormulationHelper.round(101d, NutrientCode.Sodium, Locale.CANADA, "mg/100g"), 0);
		assertEquals(105d, NutrientFormulationHelper.round(106d, NutrientCode.Sodium, Locale.CANADA, "mg/100g"), 0);
		assertEquals(110d, NutrientFormulationHelper.round(109d, NutrientCode.Sodium, Locale.CANADA, "mg/100g"), 0);
		assertEquals(2d, NutrientFormulationHelper.round(2d, NutrientCode.Sodium, Locale.CANADA, "mg/100g"), 0);
		
		assertEquals(null, NutrientFormulationHelper.round(null, NutrientCode.Cholesterol, Locale.CANADA, "mg/100g"));
		assertEquals(20d, NutrientFormulationHelper.round(19d, NutrientCode.Cholesterol, Locale.CANADA, "mg/100g"), 0);
		assertEquals(20d, NutrientFormulationHelper.round(21d, NutrientCode.Cholesterol, Locale.CANADA, "mg/100g"), 0);
		assertEquals(25d, NutrientFormulationHelper.round(26d, NutrientCode.Cholesterol, Locale.CANADA, "mg/100g"), 0);
		assertEquals(30d, NutrientFormulationHelper.round(29d, NutrientCode.Cholesterol, Locale.CANADA, "mg/100g"), 0);
		assertEquals(5d, NutrientFormulationHelper.round(3d, NutrientCode.Cholesterol, Locale.CANADA, "mg/100g"), 0);
		assertEquals(0d, NutrientFormulationHelper.round(1d, NutrientCode.Cholesterol, Locale.CANADA, "mg/100g"), 0);
		
		assertEquals(null, NutrientFormulationHelper.round(null, NutrientCode.Sugar, Locale.CANADA, "g/100g"));
		assertEquals(19d, NutrientFormulationHelper.round(18.8d, NutrientCode.Sugar, Locale.CANADA, "g/100g"), 0);
		assertEquals(19d, NutrientFormulationHelper.round(19.1d, NutrientCode.Sugar, Locale.CANADA, "g/100g"), 0);
		assertEquals(19d, NutrientFormulationHelper.round(19.4d, NutrientCode.Sugar, Locale.CANADA, "g/100g"), 0);
		assertEquals(20d, NutrientFormulationHelper.round(19.6d, NutrientCode.Sugar, Locale.CANADA, "g/100g"), 0);
		assertEquals(1d, NutrientFormulationHelper.round(0.7d, NutrientCode.Sugar, Locale.CANADA, "g/100g"), 0);
		assertEquals(0d, NutrientFormulationHelper.round(0.4d, NutrientCode.Sugar, Locale.CANADA, "g/100g"), 0);
		
		assertEquals(50d, NutrientFormulationHelper.round(51d, NutrientCode.VitA, Locale.CANADA, "µg/100g"), 0);
		assertEquals(300d, NutrientFormulationHelper.round(251d, NutrientCode.VitA, Locale.CANADA, "µg/100g"), 0);
		assertEquals(50d, NutrientFormulationHelper.round(0.056d, NutrientCode.VitA, Locale.CANADA, "mg/100g"), 0);
		assertEquals(4.5d, NutrientFormulationHelper.round(4.6d, NutrientCode.VitD, Locale.CANADA, "µg/100g"), 0);
		assertEquals("", NutrientFormulationHelper.displayValue(51d,
				NutrientFormulationHelper.round(51d, NutrientCode.VitA, Locale.CANADA, "µg/100g"), NutrientCode.VitA, Locale.US, "CA_2013"));
		assertEquals("50", NutrientFormulationHelper.displayValue(51d,
				NutrientFormulationHelper.round(51d, NutrientCode.VitA, Locale.CANADA, "µg/100g"), NutrientCode.VitA, Locale.US, "CA"));

		assertEquals("150", NutrientFormulationHelper.displayValue(150d,
				NutrientFormulationHelper.round(150d, NutrientCode.Sodium, Locale.CANADA, "mg/100g"), NutrientCode.Sodium, Locale.CANADA, "CA_2013"));
	}

	@Test
	public void testChineseRoundingRules() {

		assertEquals(0d, NutrientFormulationHelper.round(11.45d, NutrientCode.EnergykJ, Locale.CHINESE, "kJ/100g"), 0);
		assertEquals(21d, NutrientFormulationHelper.round(21.1d, NutrientCode.EnergykJ, Locale.CHINESE, "kJ/100g"), 0);

		assertEquals(0d, NutrientFormulationHelper.round(0.4d, NutrientCode.Protein, Locale.CHINESE, "g/100g"), 0);
		assertEquals(6.6d, NutrientFormulationHelper.round(6.61d, NutrientCode.Protein, Locale.CHINESE, "g/100g"), 0);
		assertEquals(6.7d, NutrientFormulationHelper.round(6.66d, NutrientCode.Protein, Locale.CHINESE, "g/100g"), 0);
		assertEquals(8.9d, NutrientFormulationHelper.round(8.89d, NutrientCode.Protein, Locale.CHINESE, "g/100g"), 0);
		
		assertEquals(0d, NutrientFormulationHelper.round(0.4d, NutrientCode.Fat, Locale.CHINESE, "g/100g"), 0);
		assertEquals(0.8, NutrientFormulationHelper.round(0.78d, NutrientCode.Fat, Locale.CHINESE, "g/100g"), 0);
		assertEquals(0.8, NutrientFormulationHelper.round(0.81d, NutrientCode.Fat, Locale.CHINESE, "g/100g"), 0);
		
		assertEquals(0d, NutrientFormulationHelper.round(0.4d, NutrientCode.CarbohydrateByDiff, Locale.CHINESE, "g/100g"), 0);
		assertEquals(0.8, NutrientFormulationHelper.round(0.78d, NutrientCode.CarbohydrateByDiff, Locale.CHINESE, "g/100g"), 0);
		assertEquals(0.8, NutrientFormulationHelper.round(0.81d, NutrientCode.CarbohydrateByDiff, Locale.CHINESE, "g/100g"), 0);
		
		assertEquals(0d, NutrientFormulationHelper.round(4d, NutrientCode.Sodium, Locale.CHINESE, "mg/100g"), 0);
		assertEquals(21d, NutrientFormulationHelper.round(21.1d, NutrientCode.Sodium, Locale.CHINESE, "mg/100g"), 0);
		
		assertEquals(0d, NutrientFormulationHelper.round(0.1d, NutrientCode.FatMonounsaturated, Locale.CHINESE, "g/100g"), 0);
		assertEquals(21.1d, NutrientFormulationHelper.round(21.1d, NutrientCode.FatMonounsaturated, Locale.CHINESE, "g/100g"), 0);
		
		assertEquals(0d, NutrientFormulationHelper.round(7d, NutrientCode.VitA, Locale.CHINESE, "µg/100g"), 0);
		assertEquals(21d, NutrientFormulationHelper.round(21.1d, NutrientCode.VitA, Locale.CHINESE, "µg/100g"), 0);
		
		assertEquals(0d, NutrientFormulationHelper.round(4d, NutrientCode.Calcium, Locale.CHINESE, "mg/100g"), 0);
		assertEquals(21d, NutrientFormulationHelper.round(21.1d, NutrientCode.Calcium, Locale.CHINESE, "mg/100g"), 0);
		
		assertEquals(0d, NutrientFormulationHelper.round(0.0001d, NutrientCode.VitD, Locale.CHINESE, "mg/100g"), 0);
		assertEquals(21.1d, NutrientFormulationHelper.round(21.1d, NutrientCode.VitD, Locale.CHINESE, "mg/100g"), 0);
		
		assertEquals(0d, NutrientFormulationHelper.round(0.2d, NutrientCode.VitE, Locale.CHINESE, "mg/100g"), 0);
		assertEquals(21.11d, NutrientFormulationHelper.round(21.11d, NutrientCode.VitE, Locale.CHINESE, "mg/100g"), 0);
		
		assertEquals(0d, NutrientFormulationHelper.round(1.6d, NutrientCode.VitK1, Locale.CHINESE, "µg/100g"), 0);
		assertEquals(21.1d, NutrientFormulationHelper.round(21.11d, NutrientCode.VitK1, Locale.CHINESE, "µg/100g"), 0);
		
		assertEquals(0d, NutrientFormulationHelper.round(0.03d, NutrientCode.VitB1, Locale.CHINESE, "mg/100g"), 0);
		assertEquals(21.11d, NutrientFormulationHelper.round(21.11d, NutrientCode.VitB1, Locale.CHINESE, "mg/100g"), 0);
		
		assertEquals(0d, NutrientFormulationHelper.round(0.05d, NutrientCode.VitB12, Locale.CHINESE, "µg/100g"), 0);
		assertEquals(21.11d, NutrientFormulationHelper.round(21.11d, NutrientCode.VitB12, Locale.CHINESE, "µg/100g"), 0);
		
		assertEquals(0d, NutrientFormulationHelper.round(2d, NutrientCode.VitC, Locale.CHINESE, "mg/100g"), 0);
		assertEquals(21.1d, NutrientFormulationHelper.round(21.11d, NutrientCode.VitC, Locale.CHINESE, "mg/100g"), 0);
		
		assertEquals(0d, NutrientFormulationHelper.round(0.1d, NutrientCode.PantoAcid, Locale.CHINESE, "mg/100g"), 0);
		assertEquals(21.11d, NutrientFormulationHelper.round(21.11d, NutrientCode.PantoAcid, Locale.CHINESE, "mg/100g"), 0);
		
		assertEquals(0d, NutrientFormulationHelper.round(0.6d, NutrientCode.Biotin, Locale.CHINESE, "µg/100g"), 0);
		assertEquals(21.1d, NutrientFormulationHelper.round(21.11d, NutrientCode.Biotin, Locale.CHINESE, "µg/100g"), 0);
		
		assertEquals(0d, NutrientFormulationHelper.round(9d, NutrientCode.Choline, Locale.CHINESE, "mg/100g"), 0);
		assertEquals(21.1d, NutrientFormulationHelper.round(21.11d, NutrientCode.Choline, Locale.CHINESE, "mg/100g"), 0);
		
		assertEquals(0d, NutrientFormulationHelper.round(14d, NutrientCode.Phosphorus, Locale.CHINESE, "mg/100g"), 0);
		assertEquals(21d, NutrientFormulationHelper.round(21.11d, NutrientCode.Phosphorus, Locale.CHINESE, "mg/100g"), 0);
		
		assertEquals(0d, NutrientFormulationHelper.round(20d, NutrientCode.Potassium, Locale.CHINESE, "mg/100g"), 0);
		assertEquals(21d, NutrientFormulationHelper.round(21.11d, NutrientCode.Potassium, Locale.CHINESE, "mg/100g"), 0);
		
		assertEquals(0d, NutrientFormulationHelper.round(6d, NutrientCode.Magnesium, Locale.CHINESE, "mg/100g"), 0);
		assertEquals(21d, NutrientFormulationHelper.round(21.11d, NutrientCode.Magnesium, Locale.CHINESE, "mg/100g"), 0);
		
		assertEquals(0d, NutrientFormulationHelper.round(0.3d, NutrientCode.Iron, Locale.CHINESE, "mg/100g"), 0);
		assertEquals(21.1d, NutrientFormulationHelper.round(21.11d, NutrientCode.Iron, Locale.CHINESE, "mg/100g"), 0);
		
		assertEquals(0d, NutrientFormulationHelper.round(0.3d, NutrientCode.Zinc, Locale.CHINESE, "mg/100g"), 0);
		assertEquals(21.11d, NutrientFormulationHelper.round(21.11d, NutrientCode.Zinc, Locale.CHINESE, "mg/100g"), 0);
		
		assertEquals(0d, NutrientFormulationHelper.round(3d, NutrientCode.Iodine, Locale.CHINESE, "µg/100g"), 0);
		assertEquals(21.1d, NutrientFormulationHelper.round(21.11d, NutrientCode.Iodine, Locale.CHINESE, "µg/100g"), 0);
		
		assertEquals(0d, NutrientFormulationHelper.round(0.02d, NutrientCode.Fluoride, Locale.CHINESE, "mg/100g"), 0);
		assertEquals(21.11d, NutrientFormulationHelper.round(21.11d, NutrientCode.Fluoride, Locale.CHINESE, "mg/100g"), 0);
		
		assertEquals(0d, NutrientFormulationHelper.round(0.06d, NutrientCode.Manganese, Locale.CHINESE, "mg/100g"), 0);
		assertEquals(21.11d, NutrientFormulationHelper.round(21.11d, NutrientCode.Manganese, Locale.CHINESE, "mg/100g"), 0);
	}
	
	@Test
	public void testAustralianRoundingRules() {
		assertEquals(35.2d, NutrientFormulationHelper.round(35.24d, NutrientCode.EnergykJ, MLTextHelper.parseLocale("en_AU"), "kJ/100g"), 0);
		assertEquals(35.3d, NutrientFormulationHelper.round(35.28d, NutrientCode.EnergykJ, MLTextHelper.parseLocale("en_AU"), "kJ/100g"), 0);
		assertEquals(35.2d, NutrientFormulationHelper.round(35.25d, NutrientCode.EnergykJ, MLTextHelper.parseLocale("en_AU"), "kJ/100g"), 0);
		assertEquals(35.8d, NutrientFormulationHelper.round(35.75d, NutrientCode.EnergykJ, MLTextHelper.parseLocale("en_AU"), "kJ/100g"), 0);
		assertEquals(35.0d, NutrientFormulationHelper.round(35d, NutrientCode.EnergykJ, MLTextHelper.parseLocale("en_AU"), "kJ/100g"), 0);
	}

}
