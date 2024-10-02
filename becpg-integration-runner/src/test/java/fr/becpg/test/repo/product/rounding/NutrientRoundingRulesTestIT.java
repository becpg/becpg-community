package fr.becpg.test.repo.product.rounding;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.formulation.nutrient.NutrientCode;
import fr.becpg.repo.product.formulation.nutrient.RegulationFormulationHelper;

public class NutrientRoundingRulesTestIT {

	public Log logger = LogFactory.getLog(NutrientRoundingRulesTestIT.class);

	@Autowired
	protected MLTextHelper mlTextHelper;


	@Test
	public void testUSRoundingRules() {
		assertEquals(110d, RegulationFormulationHelper.round(111d, NutrientCode.EnergykcalUS, Locale.US, "kcal"), 0);
		assertEquals(110d, RegulationFormulationHelper.round(109d, NutrientCode.EnergykcalUS, Locale.US, "kcal"), 0);
		assertEquals(10d, RegulationFormulationHelper.round(11d, NutrientCode.EnergykcalUS, Locale.US, "kcal"), 0);
		assertEquals(15d, RegulationFormulationHelper.round(16d, NutrientCode.EnergykcalUS, Locale.US, "kcal"), 0);
		assertEquals(20d, RegulationFormulationHelper.round(18d, NutrientCode.EnergykcalUS, Locale.US, "kcal"), 0);
		assertEquals(0d, RegulationFormulationHelper.round(4d, NutrientCode.EnergykcalUS, Locale.US, "kcal"), 0);

		assertEquals(11d, RegulationFormulationHelper.round(11.45d, NutrientCode.Fat, Locale.US, "g/100g"), 0);
		assertEquals(4.5d, RegulationFormulationHelper.round(4.33d, NutrientCode.Fat, Locale.US, "g/100g"), 0);
		assertEquals(4.5d, RegulationFormulationHelper.round(4.55d, NutrientCode.Fat, Locale.US, "g/100g"), 0);
		assertEquals(5d, RegulationFormulationHelper.round(4.95d, NutrientCode.Fat, Locale.US, "g/100g"), 0);
		assertEquals(0d, RegulationFormulationHelper.round(0.2d, NutrientCode.Fat, Locale.US, "g/100g"), 0);

		assertEquals(150d, RegulationFormulationHelper.round(149d, NutrientCode.Sodium, Locale.US, "mg/100g"), 0);
		assertEquals(150d, RegulationFormulationHelper.round(151d, NutrientCode.Sodium, Locale.US, "mg/100g"), 0);
		assertEquals(160d, RegulationFormulationHelper.round(156d, NutrientCode.Sodium, Locale.US, "mg/100g"), 0);
		assertEquals(160d, RegulationFormulationHelper.round(159d, NutrientCode.Sodium, Locale.US, "mg/100g"), 0);
		assertEquals(100d, RegulationFormulationHelper.round(99d, NutrientCode.Sodium, Locale.US, "mg/100g"), 0);
		assertEquals(100d, RegulationFormulationHelper.round(101d, NutrientCode.Sodium, Locale.US, "mg/100g"), 0);
		assertEquals(105d, RegulationFormulationHelper.round(106d, NutrientCode.Sodium, Locale.US, "mg/100g"), 0);
		assertEquals(110d, RegulationFormulationHelper.round(109d, NutrientCode.Sodium, Locale.US, "mg/100g"), 0);
		assertEquals(0d, RegulationFormulationHelper.round(2d, NutrientCode.Sodium, Locale.US, "mg/100g"), 0);

		assertEquals(null, RegulationFormulationHelper.round(null, NutrientCode.Cholesterol, Locale.US, "mg/100g"));
		assertEquals(20d, RegulationFormulationHelper.round(19d, NutrientCode.Cholesterol, Locale.US, "mg/100g"), 0);
		assertEquals(20d, RegulationFormulationHelper.round(21d, NutrientCode.Cholesterol, Locale.US, "mg/100g"), 0);
		assertEquals(25d, RegulationFormulationHelper.round(26d, NutrientCode.Cholesterol, Locale.US, "mg/100g"), 0);
		assertEquals(30d, RegulationFormulationHelper.round(29d, NutrientCode.Cholesterol, Locale.US, "mg/100g"), 0);
		assertEquals(35d, RegulationFormulationHelper.round(33.7d, NutrientCode.Cholesterol, Locale.US, "mg/100g"), 0);
		assertEquals(3d, RegulationFormulationHelper.round(3d, NutrientCode.Cholesterol, Locale.US, "mg/100g"), 0);
		assertEquals(0d, RegulationFormulationHelper.round(1d, NutrientCode.Cholesterol, Locale.US, "mg/100g"), 0);

		assertEquals(null, RegulationFormulationHelper.round(null, NutrientCode.Sugar, Locale.US, "g/100g"));
		assertEquals(19d, RegulationFormulationHelper.round(18.8d, NutrientCode.Sugar, Locale.US, "g/100g"), 0);
		assertEquals(19d, RegulationFormulationHelper.round(19.1d, NutrientCode.Sugar, Locale.US, "g/100g"), 0);
		assertEquals(19d, RegulationFormulationHelper.round(19.4d, NutrientCode.Sugar, Locale.US, "g/100g"), 0);
		assertEquals(20d, RegulationFormulationHelper.round(19.6d, NutrientCode.Sugar, Locale.US, "g/100g"), 0);
		assertEquals(1d, RegulationFormulationHelper.round(0.7d, NutrientCode.Sugar, Locale.US, "g/100g"), 0);
		assertEquals(1d, RegulationFormulationHelper.round(0.6d, NutrientCode.Sugar, Locale.US, "g/100g"), 0);
		assertEquals(0d, RegulationFormulationHelper.round(0.4d, NutrientCode.Sugar, Locale.US, "g/100g"), 0);
		assertEquals(0d, RegulationFormulationHelper.round(0.18d, NutrientCode.Sugar, Locale.US, "g/100g"), 0);

		assertEquals(1.2d, RegulationFormulationHelper.round(1.23d, NutrientCode.VitD, Locale.US, "μg/100g"), 0);
		assertEquals(30d, RegulationFormulationHelper.round(26.3d, NutrientCode.Calcium, Locale.US, "mg/100g"), 0);
		assertEquals(2.4d, RegulationFormulationHelper.round(2.38d, NutrientCode.Iron, Locale.US, "mg/100g"), 0);
		assertEquals(40d, RegulationFormulationHelper.round(42.4d, NutrientCode.Potassium, Locale.US, "mg/100g"), 0);
		

		assertEquals(0.2d,RegulationFormulationHelper.round(0.15d, NutrientCode.VitD, Locale.US, "μg/100g"),0);
		assertEquals(0.8d, RegulationFormulationHelper.round(0.76d, NutrientCode.VitD, Locale.US, "μg/100g"),0);

		assertEquals(40d, RegulationFormulationHelper.round(39d, NutrientCode.VitA, Locale.US, "μg/100g"), 0);
		assertEquals(1d, RegulationFormulationHelper.round(0.59d, NutrientCode.VitC, Locale.US, "mg/100g"), 0);
		assertEquals(3.3d, RegulationFormulationHelper.round(3.28d, NutrientCode.VitE, Locale.US, "mg/100g"), 0);
		assertEquals(4d, RegulationFormulationHelper.round(4.25d, NutrientCode.VitK1, Locale.US, "μg/100g"), 0);
		assertEquals(3d, RegulationFormulationHelper.round(2.86d, NutrientCode.VitK2, Locale.US, "μg/100g"), 0);
		assertEquals(6.39d, RegulationFormulationHelper.round(6.392d, NutrientCode.Thiamin, Locale.US, "mg/100g"), 0);
		assertEquals(2.13d, RegulationFormulationHelper.round(2.127d, NutrientCode.Riboflavin, Locale.US, "mg/100g"), 0);
		assertEquals(4d, RegulationFormulationHelper.round(3.98d, NutrientCode.Niacin, Locale.US, "mg/100g"), 0);
		assertEquals(6.75d, RegulationFormulationHelper.round(6.754d, NutrientCode.VitB6, Locale.US, "mg/100g"), 0);
		assertEquals(15d, RegulationFormulationHelper.round(13.5d, NutrientCode.Folate, Locale.US, "μg/100g"), 0);
		assertEquals(1.23d, RegulationFormulationHelper.round(1.232d, NutrientCode.VitB12, Locale.US, "μg/100g"), 0);
		assertEquals(0.2d, RegulationFormulationHelper.round(0.16d, NutrientCode.Biotin, Locale.US, "μg/100g"), 0);
		assertEquals(1.2d, RegulationFormulationHelper.round(1.23d, NutrientCode.PantoAcid, Locale.US, "mg/100g"), 0);
		assertEquals(20d, RegulationFormulationHelper.round(16.9d, NutrientCode.Phosphorus, Locale.US, "mg/100g"), 0);
		assertEquals(2d, RegulationFormulationHelper.round(1.83d, NutrientCode.Iodine, Locale.US, "μg/100g"), 0);
		assertEquals(15d, RegulationFormulationHelper.round(12.6d, NutrientCode.Magnesium, Locale.US, "mg/100g"), 0);
		assertEquals(8.3d, RegulationFormulationHelper.round(8.29d, NutrientCode.Zinc, Locale.US, "mg/100g"), 0);
		assertEquals(9d, RegulationFormulationHelper.round(8.6d, NutrientCode.Selenium, Locale.US, "μg/100g"), 0);
		assertEquals(5.16d, RegulationFormulationHelper.round(5.157d, NutrientCode.Copper, Locale.US, "mg/100g"), 0);
		assertEquals(2.41d, RegulationFormulationHelper.round(2.411d, NutrientCode.Manganese, Locale.US, "mg/100g"), 0);
		assertEquals(1.7d, RegulationFormulationHelper.round(1.69d, NutrientCode.Chromium, Locale.US, "μg/100g"), 0);
		assertEquals(1.2d, RegulationFormulationHelper.round(1.23d, NutrientCode.Molybdenum, Locale.US, "μg/100g"), 0);
		assertEquals(50d, RegulationFormulationHelper.round(54d, NutrientCode.Chloride, Locale.US, "mg/100g"), 0);
		assertEquals(80d, RegulationFormulationHelper.round(78d, NutrientCode.Choline, Locale.US, "mg/100g"), 0);

		// less than
		assertEquals("<5", RegulationFormulationHelper.displayValue(3d,
				RegulationFormulationHelper.round(3d, NutrientCode.Cholesterol, Locale.US, "mg/100g"), NutrientCode.Cholesterol, Locale.US));
		assertEquals("0", RegulationFormulationHelper.displayValue(1d,
				RegulationFormulationHelper.round(1d, NutrientCode.Cholesterol, Locale.US, "mg/100g"), NutrientCode.Cholesterol, Locale.US));

		assertEquals("35", RegulationFormulationHelper.displayValue(33.7d,
				RegulationFormulationHelper.round(33.7d, NutrientCode.Cholesterol, Locale.US, "mg/100g"), NutrientCode.Cholesterol, Locale.US));

		String[] codes = { NutrientCode.CarbohydrateWithFiber, NutrientCode.Sugar, NutrientCode.SugarAdded, NutrientCode.FiberDietary,
				NutrientCode.FiberInsoluble, NutrientCode.FiberSoluble, NutrientCode.Protein, NutrientCode.Polyols };

		for (String code : codes) {
			assertEquals("<1",
					RegulationFormulationHelper.displayValue(0.9d, RegulationFormulationHelper.round(0.9d, code, Locale.US, "g/100g"), code, Locale.US));

			assertEquals("9",
					RegulationFormulationHelper.displayValue(9.1d, RegulationFormulationHelper.round(9.1d, code, Locale.US, "g/100g"), code, Locale.US));
		}

		// gda
		assertEquals(6d, RegulationFormulationHelper.roundGDA(5.4d, NutrientCode.VitA, Locale.US), 0);
		assertEquals(15d, RegulationFormulationHelper.roundGDA(15.5d, NutrientCode.VitA, Locale.US), 0);
		assertEquals(20d, RegulationFormulationHelper.roundGDA(18d, NutrientCode.VitA, Locale.US), 0);
		assertEquals(60d, RegulationFormulationHelper.roundGDA(56d, NutrientCode.VitA, Locale.US), 0);
		assertEquals(5d, RegulationFormulationHelper.roundGDA(5.4d, NutrientCode.Sodium, Locale.US), 0);

		assertEquals(3.7d, RegulationFormulationHelper.round(3.7d, NutrientCode.VitD, Locale.US, "µg/100g"), 0);
		assertEquals("3.7", RegulationFormulationHelper.displayValue(3.7d, RegulationFormulationHelper.round(3.7d, NutrientCode.VitD, Locale.US, "µg/100g"),
				NutrientCode.VitD, Locale.US));
		assertEquals("0", RegulationFormulationHelper.displayValue(3.7d, RegulationFormulationHelper.round(3.7d, NutrientCode.VitA, Locale.US, "µg/100g"),
				NutrientCode.VitA, Locale.US));
	}

	@Test
	public void testEuropeanRoundingRules() {

		assertEquals(11d, RegulationFormulationHelper.round(11.45d, NutrientCode.Fat, Locale.FRENCH, null), 0);
		assertEquals(4254d, RegulationFormulationHelper.round(4253.6d, NutrientCode.Energykcal, Locale.FRENCH, "kcal"), 0);

		assertEquals(null, RegulationFormulationHelper.round(null, NutrientCode.Fat, Locale.FRENCH, "g/100g"));
		assertEquals(0d, RegulationFormulationHelper.round(0.2d, NutrientCode.Fat, Locale.FRENCH, "g/100g"), 0);
		assertEquals(4.3d, RegulationFormulationHelper.round(4.33d, NutrientCode.Fat, Locale.FRENCH, "g/100g"), 0);

		assertEquals(null, RegulationFormulationHelper.round(null, NutrientCode.FatSaturated, Locale.FRENCH, null));
		assertEquals(12d, RegulationFormulationHelper.round(11.6d, NutrientCode.FatSaturated, Locale.FRENCH, null), 0);
		assertEquals(12d, RegulationFormulationHelper.round(11.6d, NutrientCode.FatSaturated, Locale.GERMAN, null), 0);
		assertEquals(8.6d, RegulationFormulationHelper.round(8.63d, NutrientCode.FatSaturated, Locale.FRENCH, null), 0);
		assertEquals(0.2d, RegulationFormulationHelper.round(0.2d, NutrientCode.FatSaturated, Locale.FRENCH, null), 0);
		assertEquals(0.0d, RegulationFormulationHelper.round(0.01d, NutrientCode.FatSaturated, Locale.FRENCH, null), 0);
		assertEquals(0d, RegulationFormulationHelper.round(0d, NutrientCode.FatSaturated, Locale.FRENCH, null), 0);

		assertEquals(null, RegulationFormulationHelper.round(null, NutrientCode.Sodium, Locale.FRENCH, "g/100g"));
		assertEquals(10.4d, RegulationFormulationHelper.round(10.36d, NutrientCode.Sodium, Locale.FRENCH, "g/100g"), 0);
		assertEquals(10.4d, RegulationFormulationHelper.round(10.36d, NutrientCode.Sodium, Locale.GERMAN, "g/100g"), 0);
		assertEquals(0.34d, RegulationFormulationHelper.round(0.337d, NutrientCode.Sodium, Locale.FRENCH, "g/100g"), 0);
		assertEquals(0.0d, RegulationFormulationHelper.round(0.002d, NutrientCode.Sodium, Locale.FRENCH, "g/100g"), 0);
		assertEquals(0d, RegulationFormulationHelper.round(0d, NutrientCode.Sodium, Locale.FRENCH, "g/100g"), 0);

		assertEquals(null, RegulationFormulationHelper.round(null, NutrientCode.Salt, Locale.FRENCH, "g/100g"));
		assertEquals(1.5d, RegulationFormulationHelper.round(1.46d, NutrientCode.Salt, Locale.FRENCH, "g/100g"), 0);
		assertEquals(0.04d, RegulationFormulationHelper.round(0.037d, NutrientCode.Salt, Locale.FRENCH, "g/100g"), 0);
		assertEquals(0.0d, RegulationFormulationHelper.round(0.0125d, NutrientCode.Salt, Locale.FRENCH, "g/100g"), 0);
		assertEquals(0.0d, RegulationFormulationHelper.round(0.002d, NutrientCode.Salt, Locale.FRENCH, "g/100g"), 0);

		// display in g
		assertEquals(0.01d, RegulationFormulationHelper.round(10.36d, NutrientCode.Sodium, Locale.FRENCH, "mg/100g"), 0);
		assertEquals(0.10d, RegulationFormulationHelper.round(101d, NutrientCode.Sodium, Locale.FRENCH, "mg/100g"), 0);
		assertEquals(1.1d, RegulationFormulationHelper.round(1150d, NutrientCode.Sodium, Locale.FRENCH, "mg/100g"), 0);

		// less than
		assertEquals(null, RegulationFormulationHelper.displayValue(null,
				RegulationFormulationHelper.round(null, NutrientCode.Fat, Locale.FRENCH, "g/100g"), NutrientCode.Fat, Locale.FRENCH));
		assertEquals("< 0,5", RegulationFormulationHelper.displayValue(0.4d,
				RegulationFormulationHelper.round(0.4d, NutrientCode.Fat, Locale.FRENCH, "g/100g"), NutrientCode.Fat, Locale.FRENCH));
		assertEquals("< 0,1",
				RegulationFormulationHelper.displayValue(0.09d,
						RegulationFormulationHelper.round(0.09d, NutrientCode.FatSaturated, Locale.FRENCH, "g/100g"), NutrientCode.FatSaturated,
						Locale.FRENCH));
		assertEquals("< 0,005", RegulationFormulationHelper.displayValue(0.004d,
				RegulationFormulationHelper.round(0.004d, NutrientCode.Sodium, Locale.FRENCH, "g/100g"), NutrientCode.Sodium, Locale.FRENCH));
		assertEquals("< 0,01", RegulationFormulationHelper.displayValue(0.0122d,
				RegulationFormulationHelper.round(0.0122d, NutrientCode.Salt, Locale.FRENCH, "g/100g"), NutrientCode.Salt, Locale.FRENCH));
		assertEquals("< 0.01", RegulationFormulationHelper.displayValue(0.0122d,
				RegulationFormulationHelper.round(0.0122d, NutrientCode.Salt, Locale.FRENCH, "g/100g"), NutrientCode.Salt, Locale.ENGLISH));

		String[] codes = { NutrientCode.CarbohydrateByDiff, NutrientCode.Sugar, NutrientCode.FiberDietary, NutrientCode.Protein };

		for (String code : codes) {
			assertEquals("< 0,5", RegulationFormulationHelper.displayValue(0.4d, RegulationFormulationHelper.round(0.4d, code, Locale.FRENCH, "g/100g"),
					code, Locale.FRENCH));
			assertEquals("19", RegulationFormulationHelper.displayValue(19.1d, RegulationFormulationHelper.round(19.1d, code, Locale.FRENCH, "g/100g"),
					code, Locale.FRENCH));
		}

		assertEquals(3.78d, RegulationFormulationHelper.round(3.7777d, NutrientCode.VitA, Locale.FRENCH, "µg/100g"), 0);
		assertEquals(3.8d, RegulationFormulationHelper.round(3.7777d, NutrientCode.VitD, Locale.FRENCH, "µg/100g"), 0);

		assertEquals(5d, RegulationFormulationHelper.roundGDA(5.41d, NutrientCode.VitA, Locale.US), 5.4d);
		assertEquals(5d, RegulationFormulationHelper.roundGDA(5.46d, NutrientCode.VitA, Locale.US), 5.5d);

	}

	@Test
	public void testCanadianRoundingRules() {
		assertEquals(110d, RegulationFormulationHelper.round(111d, NutrientCode.EnergykcalUS, Locale.CANADA, "kcal"), 0);
		assertEquals(110d, RegulationFormulationHelper.round(109d, NutrientCode.EnergykcalUS, Locale.CANADA, "kcal"), 0);
		assertEquals(10d, RegulationFormulationHelper.round(11d, NutrientCode.EnergykcalUS, Locale.CANADA, "kcal"), 0);
		assertEquals(15d, RegulationFormulationHelper.round(16d, NutrientCode.EnergykcalUS, Locale.CANADA, "kcal"), 0);
		assertEquals(20d, RegulationFormulationHelper.round(18d, NutrientCode.EnergykcalUS, Locale.CANADA, "kcal"), 0);
		assertEquals(4d, RegulationFormulationHelper.round(4d, NutrientCode.EnergykcalUS, Locale.CANADA, "kcal"), 0);

		assertEquals(11d, RegulationFormulationHelper.round(11.45d, NutrientCode.Fat, Locale.CANADA, "g/100g"), 0);
		assertEquals(4.5d, RegulationFormulationHelper.round(4.33d, NutrientCode.Fat, Locale.CANADA, "g/100g"), 0);
		assertEquals(4.5d, RegulationFormulationHelper.round(4.55d, NutrientCode.Fat, Locale.CANADA, "g/100g"), 0);
		assertEquals(5d, RegulationFormulationHelper.round(4.95d, NutrientCode.Fat, Locale.CANADA, "g/100g"), 0);
		assertEquals(0.2d, RegulationFormulationHelper.round(0.22d, NutrientCode.Fat, Locale.CANADA, "g/100g"), 0);
		assertEquals(0.3d, RegulationFormulationHelper.round(0.26d, NutrientCode.Fat, Locale.CANADA, "g/100g"), 0);

		assertEquals(150d, RegulationFormulationHelper.round(149d, NutrientCode.Sodium, Locale.CANADA, "mg/100g"), 0);
		assertEquals(150d, RegulationFormulationHelper.round(151d, NutrientCode.Sodium, Locale.CANADA, "mg/100g"), 0);
		assertEquals(160d, RegulationFormulationHelper.round(156d, NutrientCode.Sodium, Locale.CANADA, "mg/100g"), 0);
		assertEquals(160d, RegulationFormulationHelper.round(159d, NutrientCode.Sodium, Locale.CANADA, "mg/100g"), 0);
		assertEquals(100d, RegulationFormulationHelper.round(99d, NutrientCode.Sodium, Locale.CANADA, "mg/100g"), 0);
		assertEquals(100d, RegulationFormulationHelper.round(101d, NutrientCode.Sodium, Locale.CANADA, "mg/100g"), 0);
		assertEquals(105d, RegulationFormulationHelper.round(106d, NutrientCode.Sodium, Locale.CANADA, "mg/100g"), 0);
		assertEquals(110d, RegulationFormulationHelper.round(109d, NutrientCode.Sodium, Locale.CANADA, "mg/100g"), 0);
		assertEquals(2d, RegulationFormulationHelper.round(2d, NutrientCode.Sodium, Locale.CANADA, "mg/100g"), 0);

		assertEquals(null, RegulationFormulationHelper.round(null, NutrientCode.Cholesterol, Locale.CANADA, "mg/100g"));
		assertEquals(20d, RegulationFormulationHelper.round(19d, NutrientCode.Cholesterol, Locale.CANADA, "mg/100g"), 0);
		assertEquals(20d, RegulationFormulationHelper.round(21d, NutrientCode.Cholesterol, Locale.CANADA, "mg/100g"), 0);
		assertEquals(25d, RegulationFormulationHelper.round(26d, NutrientCode.Cholesterol, Locale.CANADA, "mg/100g"), 0);
		assertEquals(30d, RegulationFormulationHelper.round(29d, NutrientCode.Cholesterol, Locale.CANADA, "mg/100g"), 0);
		assertEquals(5d, RegulationFormulationHelper.round(3d, NutrientCode.Cholesterol, Locale.CANADA, "mg/100g"), 0);
		assertEquals(0d, RegulationFormulationHelper.round(1d, NutrientCode.Cholesterol, Locale.CANADA, "mg/100g"), 0);

		assertEquals(null, RegulationFormulationHelper.round(null, NutrientCode.Sugar, Locale.CANADA, "g/100g"));
		assertEquals(19d, RegulationFormulationHelper.round(18.8d, NutrientCode.Sugar, Locale.CANADA, "g/100g"), 0);
		assertEquals(19d, RegulationFormulationHelper.round(19.1d, NutrientCode.Sugar, Locale.CANADA, "g/100g"), 0);
		assertEquals(19d, RegulationFormulationHelper.round(19.4d, NutrientCode.Sugar, Locale.CANADA, "g/100g"), 0);
		assertEquals(20d, RegulationFormulationHelper.round(19.6d, NutrientCode.Sugar, Locale.CANADA, "g/100g"), 0);
		assertEquals(1d, RegulationFormulationHelper.round(0.7d, NutrientCode.Sugar, Locale.CANADA, "g/100g"), 0);
		assertEquals(0d, RegulationFormulationHelper.round(0.4d, NutrientCode.Sugar, Locale.CANADA, "g/100g"), 0);

		assertEquals(50d, RegulationFormulationHelper.round(51d, NutrientCode.VitA, Locale.CANADA, "µg/100g"), 0);
		assertEquals(300d, RegulationFormulationHelper.round(251d, NutrientCode.VitA, Locale.CANADA, "µg/100g"), 0);
		assertEquals(50d, RegulationFormulationHelper.round(0.056d, NutrientCode.VitA, Locale.CANADA, "mg/100g"), 0);

		assertEquals(10d, RegulationFormulationHelper.round(5.6d, NutrientCode.VitA, Locale.CANADA, "µg/100g"), 0);
		assertEquals(0d, RegulationFormulationHelper.round(1.2d, NutrientCode.VitA, Locale.CANADA, "µg/100g"), 0);
		
		
		
		assertEquals(4.5d, RegulationFormulationHelper.round(4.6d, NutrientCode.VitD, Locale.CANADA, "µg/100g"), 0);

		assertEquals("", RegulationFormulationHelper.displayValue(51d,
				RegulationFormulationHelper.round(51d, NutrientCode.VitA, Locale.CANADA, "µg/100g"), NutrientCode.VitA, Locale.US, "CA_2013"));
		assertEquals("50", RegulationFormulationHelper.displayValue(51d,
				RegulationFormulationHelper.round(51d, NutrientCode.VitA, Locale.CANADA, "µg/100g"), NutrientCode.VitA, Locale.US, "CA"));

		assertEquals("150", RegulationFormulationHelper.displayValue(150d,
				RegulationFormulationHelper.round(150d, NutrientCode.Sodium, Locale.CANADA, "mg/100g"), NutrientCode.Sodium, Locale.CANADA, "CA_2013"));
	}

	@Test
	public void testChineseRoundingRules() {

		assertEquals(0d, RegulationFormulationHelper.round(11.45d, NutrientCode.EnergykJ, Locale.CHINESE, "kJ/100g"), 0);
		assertEquals(21d, RegulationFormulationHelper.round(21.1d, NutrientCode.EnergykJ, Locale.CHINESE, "kJ/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(0.4d, NutrientCode.Protein, Locale.CHINESE, "g/100g"), 0);
		assertEquals(6.6d, RegulationFormulationHelper.round(6.61d, NutrientCode.Protein, Locale.CHINESE, "g/100g"), 0);
		assertEquals(6.7d, RegulationFormulationHelper.round(6.66d, NutrientCode.Protein, Locale.CHINESE, "g/100g"), 0);
		assertEquals(8.9d, RegulationFormulationHelper.round(8.89d, NutrientCode.Protein, Locale.CHINESE, "g/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(0.4d, NutrientCode.Fat, Locale.CHINESE, "g/100g"), 0);
		assertEquals(0.8, RegulationFormulationHelper.round(0.78d, NutrientCode.Fat, Locale.CHINESE, "g/100g"), 0);
		assertEquals(0.8, RegulationFormulationHelper.round(0.81d, NutrientCode.Fat, Locale.CHINESE, "g/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(0.4d, NutrientCode.CarbohydrateByDiff, Locale.CHINESE, "g/100g"), 0);
		assertEquals(0.8, RegulationFormulationHelper.round(0.78d, NutrientCode.CarbohydrateByDiff, Locale.CHINESE, "g/100g"), 0);
		assertEquals(0.8, RegulationFormulationHelper.round(0.81d, NutrientCode.CarbohydrateByDiff, Locale.CHINESE, "g/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(4d, NutrientCode.Sodium, Locale.CHINESE, "mg/100g"), 0);
		assertEquals(21d, RegulationFormulationHelper.round(21.1d, NutrientCode.Sodium, Locale.CHINESE, "mg/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(0.1d, NutrientCode.FatMonounsaturated, Locale.CHINESE, "g/100g"), 0);
		assertEquals(21.1d, RegulationFormulationHelper.round(21.1d, NutrientCode.FatMonounsaturated, Locale.CHINESE, "g/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(7d, NutrientCode.VitA, Locale.CHINESE, "µg/100g"), 0);
		assertEquals(21d, RegulationFormulationHelper.round(21.1d, NutrientCode.VitA, Locale.CHINESE, "µg/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(4d, NutrientCode.Calcium, Locale.CHINESE, "mg/100g"), 0);
		assertEquals(21d, RegulationFormulationHelper.round(21.1d, NutrientCode.Calcium, Locale.CHINESE, "mg/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(0.0001d, NutrientCode.VitD, Locale.CHINESE, "mg/100g"), 0);
		assertEquals(21.1d, RegulationFormulationHelper.round(21.1d, NutrientCode.VitD, Locale.CHINESE, "mg/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(0.2d, NutrientCode.VitE, Locale.CHINESE, "mg/100g"), 0);
		assertEquals(21.11d, RegulationFormulationHelper.round(21.11d, NutrientCode.VitE, Locale.CHINESE, "mg/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(1.6d, NutrientCode.VitK1, Locale.CHINESE, "µg/100g"), 0);
		assertEquals(21.1d, RegulationFormulationHelper.round(21.11d, NutrientCode.VitK1, Locale.CHINESE, "µg/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(0.03d, NutrientCode.VitB1, Locale.CHINESE, "mg/100g"), 0);
		assertEquals(21.11d, RegulationFormulationHelper.round(21.11d, NutrientCode.VitB1, Locale.CHINESE, "mg/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(0.05d, NutrientCode.VitB12, Locale.CHINESE, "µg/100g"), 0);
		assertEquals(21.11d, RegulationFormulationHelper.round(21.11d, NutrientCode.VitB12, Locale.CHINESE, "µg/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(2d, NutrientCode.VitC, Locale.CHINESE, "mg/100g"), 0);
		assertEquals(21.1d, RegulationFormulationHelper.round(21.11d, NutrientCode.VitC, Locale.CHINESE, "mg/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(0.1d, NutrientCode.PantoAcid, Locale.CHINESE, "mg/100g"), 0);
		assertEquals(21.11d, RegulationFormulationHelper.round(21.11d, NutrientCode.PantoAcid, Locale.CHINESE, "mg/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(0.6d, NutrientCode.Biotin, Locale.CHINESE, "µg/100g"), 0);
		assertEquals(21.1d, RegulationFormulationHelper.round(21.11d, NutrientCode.Biotin, Locale.CHINESE, "µg/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(9d, NutrientCode.Choline, Locale.CHINESE, "mg/100g"), 0);
		assertEquals(21.1d, RegulationFormulationHelper.round(21.11d, NutrientCode.Choline, Locale.CHINESE, "mg/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(14d, NutrientCode.Phosphorus, Locale.CHINESE, "mg/100g"), 0);
		assertEquals(21d, RegulationFormulationHelper.round(21.11d, NutrientCode.Phosphorus, Locale.CHINESE, "mg/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(20d, NutrientCode.Potassium, Locale.CHINESE, "mg/100g"), 0);
		assertEquals(21d, RegulationFormulationHelper.round(21.11d, NutrientCode.Potassium, Locale.CHINESE, "mg/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(6d, NutrientCode.Magnesium, Locale.CHINESE, "mg/100g"), 0);
		assertEquals(21d, RegulationFormulationHelper.round(21.11d, NutrientCode.Magnesium, Locale.CHINESE, "mg/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(0.3d, NutrientCode.Iron, Locale.CHINESE, "mg/100g"), 0);
		assertEquals(21.1d, RegulationFormulationHelper.round(21.11d, NutrientCode.Iron, Locale.CHINESE, "mg/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(0.3d, NutrientCode.Zinc, Locale.CHINESE, "mg/100g"), 0);
		assertEquals(21.11d, RegulationFormulationHelper.round(21.11d, NutrientCode.Zinc, Locale.CHINESE, "mg/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(3d, NutrientCode.Iodine, Locale.CHINESE, "µg/100g"), 0);
		assertEquals(21.1d, RegulationFormulationHelper.round(21.11d, NutrientCode.Iodine, Locale.CHINESE, "µg/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(0.02d, NutrientCode.Fluoride, Locale.CHINESE, "mg/100g"), 0);
		assertEquals(21.11d, RegulationFormulationHelper.round(21.11d, NutrientCode.Fluoride, Locale.CHINESE, "mg/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(0.06d, NutrientCode.Manganese, Locale.CHINESE, "mg/100g"), 0);
		assertEquals(21.11d, RegulationFormulationHelper.round(21.11d, NutrientCode.Manganese, Locale.CHINESE, "mg/100g"), 0);
	}

	@Test
	public void testAustralianRoundingRules() {
		assertEquals(35.2d, RegulationFormulationHelper.round(35.24d, NutrientCode.EnergykJ, MLTextHelper.parseLocale("en_AU"), "kJ/100g"), 0);
		assertEquals(35.3d, RegulationFormulationHelper.round(35.28d, NutrientCode.EnergykJ, MLTextHelper.parseLocale("en_AU"), "kJ/100g"), 0);
		assertEquals(35.2d, RegulationFormulationHelper.round(35.25d, NutrientCode.EnergykJ, MLTextHelper.parseLocale("en_AU"), "kJ/100g"), 0);
		assertEquals(35.8d, RegulationFormulationHelper.round(35.75d, NutrientCode.EnergykJ, MLTextHelper.parseLocale("en_AU"), "kJ/100g"), 0);
		assertEquals(35.0d, RegulationFormulationHelper.round(35d, NutrientCode.EnergykJ, MLTextHelper.parseLocale("en_AU"), "kJ/100g"), 0);
	}

	@Test
	public void testMexicanRoundingRules() {

		assertEquals(90d, RegulationFormulationHelper.round(87d, NutrientCode.Energykcal, MLTextHelper.parseLocale("es_MX"), "kcal"), 0);
		assertEquals(45d, RegulationFormulationHelper.round(46d, NutrientCode.Energykcal, MLTextHelper.parseLocale("es_MX"), "kcal"), 0);
		assertEquals(0d, RegulationFormulationHelper.round(3.2d, NutrientCode.Energykcal, MLTextHelper.parseLocale("es_MX"), "kcal"), 0);

		assertEquals(11d, RegulationFormulationHelper.round(11.45d, NutrientCode.Fat, MLTextHelper.parseLocale("es_MX"), "g/100g"), 0);
		assertEquals(4.5d, RegulationFormulationHelper.round(4.3d, NutrientCode.Fat, MLTextHelper.parseLocale("es_MX"), "g/100g"), 0);
		assertEquals(0d, RegulationFormulationHelper.round(0.2d, NutrientCode.Fat, MLTextHelper.parseLocale("es_MX"), "g/100g"), 0);


		assertEquals(0d, RegulationFormulationHelper.round(1.9d, NutrientCode.Cholesterol, MLTextHelper.parseLocale("es_MX"), "mg/100g"), 0);
		assertEquals(10d, RegulationFormulationHelper.round(8d, NutrientCode.Cholesterol, MLTextHelper.parseLocale("es_MX"), "mg/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(0.23d, NutrientCode.CarbohydrateByDiff, MLTextHelper.parseLocale("es_MX"), "g/100g"), 0);
		assertEquals(4d, RegulationFormulationHelper.round(3.87d, NutrientCode.CarbohydrateByDiff, MLTextHelper.parseLocale("es_MX"), "g/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(0.46d, NutrientCode.Sugar, MLTextHelper.parseLocale("es_MX"), "g/100g"), 0);
		assertEquals(2d, RegulationFormulationHelper.round(2.1d, NutrientCode.Sugar, MLTextHelper.parseLocale("es_MX"), "g/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(0.41d, NutrientCode.Protein, MLTextHelper.parseLocale("es_MX"), "g/100g"), 0);
		assertEquals(2d, RegulationFormulationHelper.round(1.79d, NutrientCode.Protein, MLTextHelper.parseLocale("es_MX"), "g/100g"), 0);

		assertEquals(4d, RegulationFormulationHelper.round(3.59d, NutrientCode.FiberDietary, MLTextHelper.parseLocale("es_MX"), "g/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(4.56d, NutrientCode.Salt, MLTextHelper.parseLocale("es_MX"), "g/100g"), 0);
		assertEquals(105d, RegulationFormulationHelper.round(103.98d, NutrientCode.Salt, MLTextHelper.parseLocale("es_MX"), "g/100g"), 0);
		assertEquals(190d, RegulationFormulationHelper.round(194.27d, NutrientCode.Salt, MLTextHelper.parseLocale("es_MX"), "g/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(4.56d, NutrientCode.Sodium, MLTextHelper.parseLocale("es_MX"), "mg/100g"), 0);
		assertEquals(125d, RegulationFormulationHelper.round(123.32d, NutrientCode.Sodium, MLTextHelper.parseLocale("es_MX"), "mg/100g"), 0);
		assertEquals(250d, RegulationFormulationHelper.round(246d, NutrientCode.Sodium, MLTextHelper.parseLocale("es_MX"), "mg/100g"), 0);

		assertEquals(80d, RegulationFormulationHelper.round(81.56d, NutrientCode.Potassium, MLTextHelper.parseLocale("es_MX"), "mg/100g"), 0);
		assertEquals(170d, RegulationFormulationHelper.round(168.6d, NutrientCode.Potassium, MLTextHelper.parseLocale("es_MX"), "mg/100g"), 0);

		// less than
		assertEquals("less than 5mg", RegulationFormulationHelper.displayValue(3d,
				RegulationFormulationHelper.round(3d, NutrientCode.Cholesterol, MLTextHelper.parseLocale("es_MX"), "mg/100g"), NutrientCode.Cholesterol, MLTextHelper.parseLocale("es_MX")));

		assertEquals("less than 1g", RegulationFormulationHelper.displayValue(0.9d,
				RegulationFormulationHelper.round(0.9d, NutrientCode.Protein, MLTextHelper.parseLocale("es_MX"), "g/100g"), NutrientCode.Protein, MLTextHelper.parseLocale("es_MX")));

		assertEquals("less than 1g", RegulationFormulationHelper.displayValue(0.2d,
				RegulationFormulationHelper.round(0.2d, NutrientCode.FiberDietary, MLTextHelper.parseLocale("es_MX"), "g/100g"), NutrientCode.FiberDietary, MLTextHelper.parseLocale("es_MX")));


		// gda
		assertEquals(0d, RegulationFormulationHelper.roundGDA(1.4d, NutrientCode.VitA, MLTextHelper.parseLocale("es_MX")), 0);
		assertEquals(8d, RegulationFormulationHelper.roundGDA(7.3d, NutrientCode.VitA, MLTextHelper.parseLocale("es_MX")), 0);
		assertEquals(45d, RegulationFormulationHelper.roundGDA(43.97d, NutrientCode.VitA, MLTextHelper.parseLocale("es_MX")), 0);
		assertEquals(100d, RegulationFormulationHelper.roundGDA(104.5d, NutrientCode.VitA, MLTextHelper.parseLocale("es_MX")), 0);


		assertEquals(0d, RegulationFormulationHelper.roundGDA(1.4d, NutrientCode.Calcium, MLTextHelper.parseLocale("es_MX")), 0);
		assertEquals(8d, RegulationFormulationHelper.roundGDA(7.3d, NutrientCode.Calcium, MLTextHelper.parseLocale("es_MX")), 0);
		assertEquals(45d, RegulationFormulationHelper.roundGDA(43.97d, NutrientCode.Calcium, MLTextHelper.parseLocale("es_MX")), 0);
		assertEquals(100d, RegulationFormulationHelper.roundGDA(104.5d, NutrientCode.Calcium, MLTextHelper.parseLocale("es_MX")), 0);
	}

	@Test
	public void testIndonesianRoundingRules() {

		assertEquals(90d, RegulationFormulationHelper.round(87d, NutrientCode.Energykcal, MLTextHelper.parseLocale("in_ID"), "kcal"), 0);
		assertEquals(45d, RegulationFormulationHelper.round(46d, NutrientCode.Energykcal, MLTextHelper.parseLocale("in_ID"), "kcal"), 0);
		assertEquals(0d, RegulationFormulationHelper.round(3.2d, NutrientCode.Energykcal, MLTextHelper.parseLocale("in_ID"), "kcal"), 0);

		assertEquals(11d, RegulationFormulationHelper.round(11.45d, NutrientCode.Fat, MLTextHelper.parseLocale("in_ID"), "g/100g"), 0);
		assertEquals(4.5d, RegulationFormulationHelper.round(4.3d, NutrientCode.Fat, MLTextHelper.parseLocale("in_ID"), "g/100g"), 0);
		assertEquals(0d, RegulationFormulationHelper.round(0.2d, NutrientCode.Fat, MLTextHelper.parseLocale("in_ID"), "g/100g"), 0);
		assertEquals(11d, RegulationFormulationHelper.round(11.45d, NutrientCode.FatSaturated, MLTextHelper.parseLocale("in_ID"), "g/100g"), 0);
		assertEquals(4.5d, RegulationFormulationHelper.round(4.3d, NutrientCode.FatSaturated, MLTextHelper.parseLocale("in_ID"), "g/100g"), 0);
		assertEquals(0d, RegulationFormulationHelper.round(0.2d, NutrientCode.FatSaturated, MLTextHelper.parseLocale("in_ID"), "g/100g"), 0);


		assertEquals(0d, RegulationFormulationHelper.round(1.9d, NutrientCode.Cholesterol, MLTextHelper.parseLocale("in_ID"), "mg/100g"), 0);
		assertEquals(3d, RegulationFormulationHelper.round(2.9d, NutrientCode.Cholesterol, MLTextHelper.parseLocale("in_ID"), "mg/100g"), 0);
		assertEquals(10d, RegulationFormulationHelper.round(8d, NutrientCode.Cholesterol, MLTextHelper.parseLocale("in_ID"), "mg/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(0.23d, NutrientCode.CarbohydrateByDiff, MLTextHelper.parseLocale("in_ID"), "g/100g"), 0);
		assertEquals(4d, RegulationFormulationHelper.round(3.87d, NutrientCode.CarbohydrateByDiff, MLTextHelper.parseLocale("in_ID"), "g/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(0.46d, NutrientCode.Sugar, MLTextHelper.parseLocale("in_ID"), "g/100g"), 0);
		assertEquals(2d, RegulationFormulationHelper.round(2.1d, NutrientCode.Sugar, MLTextHelper.parseLocale("in_ID"), "g/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(0.41d, NutrientCode.Protein, MLTextHelper.parseLocale("in_ID"), "g/100g"), 0);
		assertEquals(2d, RegulationFormulationHelper.round(1.79d, NutrientCode.Protein, MLTextHelper.parseLocale("in_ID"), "g/100g"), 0);

		assertEquals(4d, RegulationFormulationHelper.round(3.59d, NutrientCode.FiberDietary, MLTextHelper.parseLocale("in_ID"), "g/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(4.56d, NutrientCode.Salt, MLTextHelper.parseLocale("in_ID"), "g/100g"), 0);
		assertEquals(105d, RegulationFormulationHelper.round(103.98d, NutrientCode.Salt, MLTextHelper.parseLocale("in_ID"), "g/100g"), 0);
		assertEquals(190d, RegulationFormulationHelper.round(194.27d, NutrientCode.Salt, MLTextHelper.parseLocale("in_ID"), "g/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(4.56d, NutrientCode.Sodium, MLTextHelper.parseLocale("in_ID"), "mg/100g"), 0);
		assertEquals(125d, RegulationFormulationHelper.round(123.32d, NutrientCode.Sodium, MLTextHelper.parseLocale("in_ID"), "mg/100g"), 0);
		assertEquals(250d, RegulationFormulationHelper.round(246d, NutrientCode.Sodium, MLTextHelper.parseLocale("in_ID"), "mg/100g"), 0);


		// gda
		assertEquals(1d, RegulationFormulationHelper.roundGDA(1.4d, NutrientCode.VitA, MLTextHelper.parseLocale("in_ID")), 0);
		assertEquals(8d, RegulationFormulationHelper.roundGDA(7.3d, NutrientCode.VitA, MLTextHelper.parseLocale("in_ID")), 0);
		assertEquals(45d, RegulationFormulationHelper.roundGDA(43.97d, NutrientCode.VitA, MLTextHelper.parseLocale("in_ID")), 0);
		assertEquals(105d, RegulationFormulationHelper.roundGDA(104.5d, NutrientCode.VitA, MLTextHelper.parseLocale("in_ID")), 0);


		assertEquals(1d, RegulationFormulationHelper.roundGDA(1.4d, NutrientCode.Calcium, MLTextHelper.parseLocale("in_ID")), 0);
		assertEquals(8d, RegulationFormulationHelper.roundGDA(7.3d, NutrientCode.Calcium, MLTextHelper.parseLocale("in_ID")), 0);
		assertEquals(45d, RegulationFormulationHelper.roundGDA(43.97d, NutrientCode.Calcium, MLTextHelper.parseLocale("in_ID")), 0);
		assertEquals(105d, RegulationFormulationHelper.roundGDA(104.5d, NutrientCode.Calcium, MLTextHelper.parseLocale("in_ID")), 0);
	}

	@Test
	public void testHongKongRoundingRules() {
		assertEquals(3d, RegulationFormulationHelper.round(2.84d, NutrientCode.Energykcal, MLTextHelper.parseLocale("zh_HK"), "kcal"), 0);
		assertEquals(13d, RegulationFormulationHelper.round(13.14d, NutrientCode.EnergykJ, MLTextHelper.parseLocale("zh_HK"), "kcal"), 0);

		assertEquals(0.4d, RegulationFormulationHelper.round(0.41d, NutrientCode.Protein, MLTextHelper.parseLocale("zh_HK"), "g/100g"), 0);

		assertEquals(3.9d, RegulationFormulationHelper.round(3.87d, NutrientCode.CarbohydrateByDiff, MLTextHelper.parseLocale("zh_HK"), "g/100g"), 0);

		assertEquals(11.4d, RegulationFormulationHelper.round(11.42d, NutrientCode.Fat, MLTextHelper.parseLocale("zh_HK"), "g/100g"), 0);
		assertEquals(1.3d, RegulationFormulationHelper.round(1.272d, NutrientCode.FatSaturated, MLTextHelper.parseLocale("zh_HK"), "g/100g"), 0);
		assertEquals(6.6d, RegulationFormulationHelper.round(6.57d, NutrientCode.FatTrans, MLTextHelper.parseLocale("zh_HK"), "g/100g"), 0);

		assertEquals(35d, RegulationFormulationHelper.round(35.2d, NutrientCode.Sodium, MLTextHelper.parseLocale("zh_HK"), "mg/100g"), 0);

		assertEquals(2d, RegulationFormulationHelper.round(1.9d, NutrientCode.Cholesterol, MLTextHelper.parseLocale("zh_HK"), "mg/100g"), 0);

		assertEquals(8.5d, RegulationFormulationHelper.round(8.46d, NutrientCode.Sugar, MLTextHelper.parseLocale("zh_HK"), "g/100g"), 0);

		assertEquals(3.6d, RegulationFormulationHelper.round(3.59d, NutrientCode.FiberDietary, MLTextHelper.parseLocale("zh_HK"), "g/100g"), 0);

		// gda
		assertEquals(18d, RegulationFormulationHelper.roundGDA(18.4d, NutrientCode.VitA, MLTextHelper.parseLocale("zh_HK")), 0);		
		assertEquals(28d, RegulationFormulationHelper.roundGDA(28.44d, NutrientCode.Calcium, MLTextHelper.parseLocale("zh_HK")), 0);
	}


	@Test
	public void testKoreanRoundingRules() {
		assertEquals(0d, RegulationFormulationHelper.round(2.84d, NutrientCode.Energykcal, MLTextHelper.parseLocale("ko_KR"), "kcal"), 0);
		assertEquals(13.1d, RegulationFormulationHelper.round(13.1d, NutrientCode.Energykcal, MLTextHelper.parseLocale("ko_KR"), "kcal"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(0.22d, NutrientCode.Fat, MLTextHelper.parseLocale("ko_KR"), "g/100g"), 0);
		assertEquals(3.8d, RegulationFormulationHelper.round(3.814d, NutrientCode.Fat, MLTextHelper.parseLocale("ko_KR"), "g/100g"), 0);
		assertEquals(9d, RegulationFormulationHelper.round(9.42d, NutrientCode.Fat, MLTextHelper.parseLocale("ko_KR"), "g/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(0.371, NutrientCode.FatSaturated, MLTextHelper.parseLocale("ko_KR"), "g/100g"), 0);
		assertEquals(4.1d, RegulationFormulationHelper.round(4.108, NutrientCode.FatSaturated, MLTextHelper.parseLocale("ko_KR"), "g/100g"), 0);
		assertEquals(9d, RegulationFormulationHelper.round(8.741, NutrientCode.FatSaturated, MLTextHelper.parseLocale("ko_KR"), "g/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(0.14d, NutrientCode.FatTrans, MLTextHelper.parseLocale("ko_KR"), "g/100g"), 0);
		assertEquals(2d, RegulationFormulationHelper.round(1.57d, NutrientCode.FatTrans, MLTextHelper.parseLocale("ko_KR"), "g/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(1.9d, NutrientCode.Cholesterol, MLTextHelper.parseLocale("ko_KR"), "mg/100g"), 0);
		assertEquals(6d, RegulationFormulationHelper.round(5.9d, NutrientCode.Cholesterol, MLTextHelper.parseLocale("ko_KR"), "mg/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(3.2d, NutrientCode.Sodium, MLTextHelper.parseLocale("ko_KR"), "mg/100g"), 0);
		assertEquals(110d, RegulationFormulationHelper.round(108.61d, NutrientCode.Sodium, MLTextHelper.parseLocale("ko_KR"), "mg/100g"), 0);
		assertEquals(140d, RegulationFormulationHelper.round(135.2d, NutrientCode.Sodium, MLTextHelper.parseLocale("ko_KR"), "mg/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(0.18d, NutrientCode.CarbohydrateWithFiber, MLTextHelper.parseLocale("ko_KR"), "g/100g"), 0);
		assertEquals(3d, RegulationFormulationHelper.round(3.4d, NutrientCode.CarbohydrateWithFiber, MLTextHelper.parseLocale("ko_KR"), "g/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(0.146d, NutrientCode.Sugar, MLTextHelper.parseLocale("ko_KR"), "g/100g"), 0);
		assertEquals(3d, RegulationFormulationHelper.round(2.51d, NutrientCode.Sugar, MLTextHelper.parseLocale("ko_KR"), "g/100g"), 0);


		assertEquals(0d, RegulationFormulationHelper.round(0.41d, NutrientCode.Protein, MLTextHelper.parseLocale("ko_KR"), "g/100g"), 0);
		assertEquals(1d, RegulationFormulationHelper.round(1.2d, NutrientCode.Protein, MLTextHelper.parseLocale("ko_KR"), "g/100g"), 0);

		// gda
		assertEquals(0d, RegulationFormulationHelper.roundGDA(1.814d, NutrientCode.VitA, MLTextHelper.parseLocale("ko_KR")), 0);		
		assertEquals(0d, RegulationFormulationHelper.roundGDA(1.44d, NutrientCode.Calcium, MLTextHelper.parseLocale("ko_KR")), 0);

		// less than
		assertEquals("less than 0.5g", RegulationFormulationHelper.displayValue(0.37d,
				RegulationFormulationHelper.round(0.37d, NutrientCode.FatTrans, MLTextHelper.parseLocale("ko_KR"), "g/100g"), NutrientCode.FatTrans, MLTextHelper.parseLocale("ko_KR")));

		assertEquals("less than 5mg", RegulationFormulationHelper.displayValue(3d,
				RegulationFormulationHelper.round(3d, NutrientCode.Cholesterol, MLTextHelper.parseLocale("ko_KR"), "mg/100g"), NutrientCode.Cholesterol, MLTextHelper.parseLocale("ko_KR")));

		assertEquals("less than 1g", RegulationFormulationHelper.displayValue(0.9d,
				RegulationFormulationHelper.round(0.9d, NutrientCode.Protein, MLTextHelper.parseLocale("ko_KR"), "g/100g"), NutrientCode.Protein, MLTextHelper.parseLocale("ko_KR")));

		assertEquals("less than 1g", RegulationFormulationHelper.displayValue(0.8d,
				RegulationFormulationHelper.round(0.8d, NutrientCode.CarbohydrateWithFiber, MLTextHelper.parseLocale("ko_KR"), "g/100g"), NutrientCode.CarbohydrateWithFiber, MLTextHelper.parseLocale("ko_KR")));
		
		assertEquals("less than 1g", RegulationFormulationHelper.displayValue(0.9d,
				RegulationFormulationHelper.round(0.9d, NutrientCode.Sugar, MLTextHelper.parseLocale("ko_KR"), "g/100g"), NutrientCode.Sugar, MLTextHelper.parseLocale("ko_KR")));
	}
	
	
	@Test
	public void testThailandRoundingRules() {
		assertEquals(0d, RegulationFormulationHelper.round(2.84d, NutrientCode.Energykcal, MLTextHelper.parseLocale("th_TH"), "kcal"), 0);
		assertEquals(15d, RegulationFormulationHelper.round(13.1d, NutrientCode.Energykcal, MLTextHelper.parseLocale("th_TH"), "kcal"), 0);
		assertEquals(80d, RegulationFormulationHelper.round(76.31d, NutrientCode.Energykcal, MLTextHelper.parseLocale("th_TH"), "kcal"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(0.22d, NutrientCode.Fat, MLTextHelper.parseLocale("th_TH"), "g/100g"), 0);
		assertEquals(4d, RegulationFormulationHelper.round(3.814d, NutrientCode.Fat, MLTextHelper.parseLocale("th_TH"), "g/100g"), 0);
		assertEquals(9d, RegulationFormulationHelper.round(9.42d, NutrientCode.Fat, MLTextHelper.parseLocale("th_TH"), "g/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(0.371, NutrientCode.FatSaturated, MLTextHelper.parseLocale("th_TH"), "g/100g"), 0);
		assertEquals(5d, RegulationFormulationHelper.round(4.87, NutrientCode.FatSaturated, MLTextHelper.parseLocale("th_TH"), "g/100g"), 0);
		assertEquals(9d, RegulationFormulationHelper.round(8.741, NutrientCode.FatSaturated, MLTextHelper.parseLocale("th_TH"), "g/100g"), 0);
		
		assertEquals(0d, RegulationFormulationHelper.round(1.9d, NutrientCode.Cholesterol, MLTextHelper.parseLocale("th_TH"), "mg/100g"), 0);
		assertEquals(5d, RegulationFormulationHelper.round(5.9d, NutrientCode.Cholesterol, MLTextHelper.parseLocale("th_TH"), "mg/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(0.41d, NutrientCode.Protein, MLTextHelper.parseLocale("th_TH"), "g/100g"), 0);
		assertEquals(1d, RegulationFormulationHelper.round(1.2d, NutrientCode.Protein, MLTextHelper.parseLocale("th_TH"), "g/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(0.34d, NutrientCode.CarbohydrateByDiff, MLTextHelper.parseLocale("th_TH"), "g/100g"), 0);
		assertEquals(3d, RegulationFormulationHelper.round(2.84d, NutrientCode.CarbohydrateByDiff, MLTextHelper.parseLocale("th_TH"), "g/100g"), 0);

		assertEquals(0d, RegulationFormulationHelper.round(0.47d, NutrientCode.FiberDietary, MLTextHelper.parseLocale("th_TH"), "g/100g"), 0);
		assertEquals(6d, RegulationFormulationHelper.round(6.27d, NutrientCode.FiberDietary, MLTextHelper.parseLocale("th_TH"), "g/100g"), 0);
		
		assertEquals(0d, RegulationFormulationHelper.round(0.146d, NutrientCode.Sugar, MLTextHelper.parseLocale("th_TH"), "g/100g"), 0);
		assertEquals(3d, RegulationFormulationHelper.round(2.51d, NutrientCode.Sugar, MLTextHelper.parseLocale("th_TH"), "g/100g"), 0);
		
		assertEquals(0d, RegulationFormulationHelper.round(3.2d, NutrientCode.Sodium, MLTextHelper.parseLocale("th_TH"), "mg/100g"), 0);
		assertEquals(110d, RegulationFormulationHelper.round(108.61d, NutrientCode.Sodium, MLTextHelper.parseLocale("th_TH"), "mg/100g"), 0);
		assertEquals(190d, RegulationFormulationHelper.round(194.2d, NutrientCode.Sodium, MLTextHelper.parseLocale("th_TH"), "mg/100g"), 0);
	
		// gda
		assertEquals(0d, RegulationFormulationHelper.roundGDA(1.814d, NutrientCode.VitA, MLTextHelper.parseLocale("th_TH")), 0);
		assertEquals(10d, RegulationFormulationHelper.roundGDA(9.8, NutrientCode.VitA, MLTextHelper.parseLocale("th_TH")), 0);
		assertEquals(30d, RegulationFormulationHelper.roundGDA(28.67, NutrientCode.VitA, MLTextHelper.parseLocale("th_TH")), 0);
		assertEquals(70d, RegulationFormulationHelper.roundGDA(65.72, NutrientCode.VitA, MLTextHelper.parseLocale("th_TH")), 0);

		assertEquals(0d, RegulationFormulationHelper.roundGDA(1.814d, NutrientCode.Calcium, MLTextHelper.parseLocale("th_TH")), 0);
		assertEquals(10d, RegulationFormulationHelper.roundGDA(9.8, NutrientCode.Calcium, MLTextHelper.parseLocale("th_TH")), 0);
		assertEquals(30d, RegulationFormulationHelper.roundGDA(28.67, NutrientCode.Calcium, MLTextHelper.parseLocale("th_TH")), 0);
		assertEquals(70d, RegulationFormulationHelper.roundGDA(65.72, NutrientCode.Calcium, MLTextHelper.parseLocale("th_TH")), 0);
		
		// less than
		assertEquals("less than 5mg", RegulationFormulationHelper.displayValue(3d,
				RegulationFormulationHelper.round(3d, NutrientCode.Cholesterol, MLTextHelper.parseLocale("th_TH"), "mg/100g"), NutrientCode.Cholesterol, MLTextHelper.parseLocale("th_TH")));

		assertEquals("less than 1g", RegulationFormulationHelper.displayValue(0.9d,
				RegulationFormulationHelper.round(0.9d, NutrientCode.Protein, MLTextHelper.parseLocale("th_TH"), "g/100g"), NutrientCode.Protein, MLTextHelper.parseLocale("th_TH")));

		assertEquals("less than 1g", RegulationFormulationHelper.displayValue(0.6d,
				RegulationFormulationHelper.round(0.2d, NutrientCode.CarbohydrateByDiff, MLTextHelper.parseLocale("th_TH"), "g/100g"), NutrientCode.CarbohydrateByDiff, MLTextHelper.parseLocale("th_TH")));
		
		assertEquals("less than 1g", RegulationFormulationHelper.displayValue(0.9d,
				RegulationFormulationHelper.round(0.9d, NutrientCode.Sugar, MLTextHelper.parseLocale("th_TH"), "g/100g"), NutrientCode.Sugar, MLTextHelper.parseLocale("th_TH")));
		
		assertEquals("less than 1g", RegulationFormulationHelper.displayValue(0.6d,
				RegulationFormulationHelper.round(0.6d, NutrientCode.FiberDietary, MLTextHelper.parseLocale("th_TH"), "g/100g"), NutrientCode.FiberDietary, MLTextHelper.parseLocale("th_TH")));
	}
	
	@Test
	public void testMalaysianRoundingRules() {
		assertEquals(0d, RegulationFormulationHelper.round(0.02d, NutrientCode.Fat, MLTextHelper.parseLocale("ms_MY"), "g/100g"), 0);
		assertEquals(0.2d, RegulationFormulationHelper.round(0.23d, NutrientCode.Fat, MLTextHelper.parseLocale("ms_MY"), "g/100g"), 0);
		
		assertEquals(0d, RegulationFormulationHelper.round(0.02d, NutrientCode.CarbohydrateByDiff, MLTextHelper.parseLocale("ms_MY"), "g/100g"), 0);
		assertEquals(0.2d, RegulationFormulationHelper.round(0.23d, NutrientCode.CarbohydrateByDiff, MLTextHelper.parseLocale("ms_MY"), "g/100g"), 0);
		
		assertEquals(0d, RegulationFormulationHelper.round(0.02d, NutrientCode.Sugar, MLTextHelper.parseLocale("ms_MY"), "g/100g"), 0);
		assertEquals(0.2d, RegulationFormulationHelper.round(0.23d, NutrientCode.Sugar, MLTextHelper.parseLocale("ms_MY"), "g/100g"), 0);
		
		assertEquals(0d, RegulationFormulationHelper.round(0.02d, NutrientCode.Protein, MLTextHelper.parseLocale("ms_MY"), "g/100g"), 0);
		assertEquals(0.2d, RegulationFormulationHelper.round(0.23d, NutrientCode.Protein, MLTextHelper.parseLocale("ms_MY"), "g/100g"), 0);

		assertEquals(4.8d, RegulationFormulationHelper.round(4.83, NutrientCode.FatSaturated, MLTextHelper.parseLocale("ms_MY"), "g/100g"), 0);
		assertEquals(4.8d, RegulationFormulationHelper.round(4.83, NutrientCode.FatMonounsaturated, MLTextHelper.parseLocale("ms_MY"), "g/100g"), 0);
		assertEquals(4.8d, RegulationFormulationHelper.round(4.83, NutrientCode.FatPolyunsaturated, MLTextHelper.parseLocale("ms_MY"), "g/100g"), 0);

		assertEquals(4.8d, RegulationFormulationHelper.round(4.83, NutrientCode.FiberDietary, MLTextHelper.parseLocale("ms_MY"), "g/100g"), 0);

		assertEquals(6.33d, RegulationFormulationHelper.round(6.331, NutrientCode.Potassium, MLTextHelper.parseLocale("ms_MY"), "g/100g"), 0);
		assertEquals(5.58d, RegulationFormulationHelper.round(5.581, NutrientCode.VitA, MLTextHelper.parseLocale("ms_MY"), "mcg/100g"), 0);
		assertEquals(5.58d, RegulationFormulationHelper.round(5.581, NutrientCode.Calcium, MLTextHelper.parseLocale("ms_MY"), "mg/100g"), 0);

		// less than
		assertEquals("3", RegulationFormulationHelper.displayValue(3.3d,
				RegulationFormulationHelper.round(3.3d, NutrientCode.Energykcal, MLTextHelper.parseLocale("ms_MY"), "kCal/100g"), NutrientCode.Energykcal, MLTextHelper.parseLocale("ms_MY")));
		assertEquals("9", RegulationFormulationHelper.displayValue(9.6d,
				RegulationFormulationHelper.round(9.6d, NutrientCode.Cholesterol, MLTextHelper.parseLocale("ms_MY"), "mg/100g"), NutrientCode.Cholesterol, MLTextHelper.parseLocale("ms_MY")));
		assertEquals("5", RegulationFormulationHelper.displayValue(5.3d,
				RegulationFormulationHelper.round(5.3d, NutrientCode.Salt, MLTextHelper.parseLocale("ms_MY"), "g/100g"), NutrientCode.Salt, MLTextHelper.parseLocale("ms_MY")));
		assertEquals("8", RegulationFormulationHelper.displayValue(8.7d,
				RegulationFormulationHelper.round(8.7d, NutrientCode.Sodium, MLTextHelper.parseLocale("ms_MY"), "mg/100g"), NutrientCode.Sodium, MLTextHelper.parseLocale("ms_MY")));

		}
	
	@Test
	public void testIndianRoundingRules() {
		assertEquals(35.2d, RegulationFormulationHelper.round(35.24d, NutrientCode.EnergykJ, MLTextHelper.parseLocale("hi_IN"), "kJ/100g"), 0);
		assertEquals(35.3d, RegulationFormulationHelper.round(35.28d, NutrientCode.Energykcal, MLTextHelper.parseLocale("hi_IN"), "kCal/100g"), 0);
		assertEquals(26.2d, RegulationFormulationHelper.round(26.25d, NutrientCode.Cholesterol, MLTextHelper.parseLocale("hi_IN"), "mg/100g"), 0);
		assertEquals(16.8d, RegulationFormulationHelper.round(16.76d, NutrientCode.Sodium, MLTextHelper.parseLocale("hi_IN"), "mg/100g"), 0);
		assertEquals(41.6d, RegulationFormulationHelper.round(41.59d, NutrientCode.Sugar, MLTextHelper.parseLocale("hi_IN"), "g/100g"), 0);
	}
	
	@Test
	public void testGSORoundingRules() {
		assertEquals(35d, RegulationFormulationHelper.round(35.24d, NutrientCode.EnergykJ, MLTextHelper.parseLocale("ar_AE"), "kJ/100g"), 0);
		assertEquals(36d, RegulationFormulationHelper.round(35.88d, NutrientCode.Energykcal, MLTextHelper.parseLocale("ar_AE"), "kCal/100g"), 0);
		assertEquals(17d, RegulationFormulationHelper.round(16.58d, NutrientCode.Fat, MLTextHelper.parseLocale("ar_BH"), "g/100g"), 0);
		assertEquals(8.8d, RegulationFormulationHelper.round(8.76d, NutrientCode.CarbohydrateByDiff, MLTextHelper.parseLocale("ar_BH"), "g/100g"), 0);
		assertEquals(0d, RegulationFormulationHelper.round(0.09d, NutrientCode.Sugar, MLTextHelper.parseLocale("ar_BH"), "g/100g"), 0);
		
		assertEquals(19d, RegulationFormulationHelper.round(19.35d, NutrientCode.FatSaturated, MLTextHelper.parseLocale("ar_SA"), "g/100g"), 0);
		assertEquals(6.2d, RegulationFormulationHelper.round(6.24d, NutrientCode.FatMonounsaturated, MLTextHelper.parseLocale("ar_SA"), "g/100g"), 0);
		assertEquals(0d, RegulationFormulationHelper.round(0.04d, NutrientCode.FatPolyunsaturated, MLTextHelper.parseLocale("ar_SA"), "g/100g"), 0);

		assertEquals(1.9d, RegulationFormulationHelper.round(1.92d, NutrientCode.Salt, MLTextHelper.parseLocale("ar_OM"), "g/100g"), 0);
		assertEquals(0.03d, RegulationFormulationHelper.round(0.0285d, NutrientCode.Salt, MLTextHelper.parseLocale("ar_OM"), "g/100g"), 0);
		assertEquals(0d, RegulationFormulationHelper.round(0.01d, NutrientCode.Salt, MLTextHelper.parseLocale("ar_OM"), "g/100g"), 0);

		assertEquals(3.78d, RegulationFormulationHelper.round(3.7777d, NutrientCode.VitA, MLTextHelper.parseLocale("ar_KW"), "µg/100g"), 0);
		assertEquals(3.8d, RegulationFormulationHelper.round(3.7777d, NutrientCode.VitD, MLTextHelper.parseLocale("ar_KW"), "µg/100g"), 0);
	}
	

}
