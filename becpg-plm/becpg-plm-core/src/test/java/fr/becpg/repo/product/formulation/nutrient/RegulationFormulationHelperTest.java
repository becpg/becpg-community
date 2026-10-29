package fr.becpg.repo.product.formulation.nutrient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.NutListDataItem;

/**
 * Unit tests of the report columns built from the rounded values of a nutrient line: each
 * tolerance column must carry the value its name announces. Expected values are the EU tolerances
 * of 18.9 g of fat (maximum 23 g, minimum 15 g). Also covers the regulation each country is
 * labelled under.
 */
public class RegulationFormulationHelperTest {

	private static final String EU = "EU";

	private static final String TOLERANCE_MAX_COLUMN = "ToleranceMax";

	private static final String TOLERANCE_MIN_COLUMN = "ToleranceMin";

	private static final String SUPPORTED_LOCALES = "en,fr";

	private static final String CODEX = "CODEX";

	private static final String CENTRAL_AMERICA = "CTA";

	private static final String GSO = "GSO";

	private static final String ECUADOR = "EC";

	private static final String MILLIGRAMS = "mg";

	private static final Locale ECUADORIAN_SPANISH = new Locale("es", "EC");

	private static final String ROUNDED_VALUE_KEY = "v";

	private static final double DELTA = 1e-9;

	@Before
	public void setUpSupportedLocales() {
		MLTextHelper.flushCache();
		MLTextHelper.setSupportedLocales(SUPPORTED_LOCALES);
	}

	@After
	public void resetSupportedLocales() {
		MLTextHelper.flushCache();
	}

	private JSONObject roundedFat() {
		FinishedProductData product = new FinishedProductData();
		product.setUnit(ProductUnit.kg);
		product.setQty(1d);
		NutListDataItem fat = NutListDataItem.build().withValue(18.9d).withUnit("g/100g");
		RegulationFormulationHelper.extractRoundedValue(product, NutrientCode.Fat, fat);
		return new JSONObject(fat.getRoundedValue());
	}

	private double euValueOfColumn(JSONObject rounded, String column) {
		for (String key : rounded.keySet()) {
			if (column.equals(RegulationFormulationHelper.keyToXml(key))) {
				return rounded.getJSONObject(key).getDouble(EU);
			}
		}
		throw new AssertionError("No rounded value is exported as " + column);
	}

	@Test
	public void exportsTheMaximumToleratedValueAsToleranceMax() {
		assertEquals(23d, euValueOfColumn(roundedFat(), TOLERANCE_MAX_COLUMN), DELTA);
	}

	@Test
	public void exportsTheMinimumToleratedValueAsToleranceMin() {
		assertEquals(15d, euValueOfColumn(roundedFat(), TOLERANCE_MIN_COLUMN), DELTA);
	}

	@Test
	public void labelsLebanonAndSyriaUnderCodexLikeTheirNeighbours() {
		assertEquals(CODEX, RegulationFormulationHelper.getLocalKey(new Locale("ar", "LB")));
		assertEquals(CODEX, RegulationFormulationHelper.getLocalKey(new Locale("fr", "LB")));
		assertEquals(CODEX, RegulationFormulationHelper.getLocalKey(new Locale("ar", "SY")));
		assertEquals(CODEX, RegulationFormulationHelper.getLocalKey(new Locale("ar", "JO")));
	}

	@Test
	public void labelsHondurasNicaraguaAndCostaRicaUnderTheCentralAmericanRegulation() {
		assertEquals(CENTRAL_AMERICA, RegulationFormulationHelper.getLocalKey(new Locale("es", "HN")));
		assertEquals(CENTRAL_AMERICA, RegulationFormulationHelper.getLocalKey(new Locale("es", "NI")));
		assertEquals(CENTRAL_AMERICA, RegulationFormulationHelper.getLocalKey(new Locale("es", "CR")));
		assertEquals(CENTRAL_AMERICA, RegulationFormulationHelper.getLocalKey(new Locale("es", "GT")));
	}

	@Test
	public void keepsArabicWithoutCountryUnderGsoAndUnknownCountriesUnderEu() {
		assertEquals(GSO, RegulationFormulationHelper.getLocalKey(new Locale("ar")));
		assertEquals(GSO, RegulationFormulationHelper.getLocalKey(new Locale("ar", "AE")));
		assertEquals(EU, RegulationFormulationHelper.getLocalKey(new Locale("fr", "FR")));
	}

	@Test
	public void roundsForCodexWhenLebaneseArabicIsSupported() {
		MLTextHelper.flushCache();
		MLTextHelper.setSupportedLocales("en,ar_LB");
		assertTrue(roundedFat().getJSONObject(ROUNDED_VALUE_KEY).has(CODEX));
	}

	@Test
	public void roundsForCentralAmericaWhenHonduranSpanishIsSupported() {
		MLTextHelper.flushCache();
		MLTextHelper.setSupportedLocales("en,es_HN");
		assertTrue(roundedFat().getJSONObject(ROUNDED_VALUE_KEY).has(CENTRAL_AMERICA));
	}

	@Test
	public void labelsSriLankaUnderCodex() {
		assertEquals(CODEX, RegulationFormulationHelper.getLocalKey(new Locale("si", "LK")));
		assertEquals(CODEX, RegulationFormulationHelper.getLocalKey(new Locale("en", "LK")));
	}

	@Test
	public void labelsEcuadorUnderItsOwnRegulation() {
		assertEquals(ECUADOR, RegulationFormulationHelper.getLocalKey(ECUADORIAN_SPANISH));
	}

	@Test
	public void roundsEcuadorianSodiumWithThePre2016FdaSteps() {
		assertEquals(140d, RegulationFormulationHelper.round(143d, NutrientCode.Sodium, ECUADORIAN_SPANISH, MILLIGRAMS), DELTA);
		assertEquals(0d, RegulationFormulationHelper.round(3d, NutrientCode.Sodium, ECUADORIAN_SPANISH, MILLIGRAMS), DELTA);
	}

	@Test
	public void roundsForEcuadorWhenEcuadorianSpanishIsSupported() {
		MLTextHelper.flushCache();
		MLTextHelper.setSupportedLocales("en,es_EC");
		assertTrue(roundedFat().getJSONObject(ROUNDED_VALUE_KEY).has(ECUADOR));
	}
}
