package fr.becpg.repo.product.formulation.nutrient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Locale;
import java.util.Map;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.NutListDataItem;

/**
 * Unit tests of the rounding step a product declares for a nutrient: it replaces the
 * rounding rule of the regulation for the labelled value (#37516). Salt is the reference
 * case: the EU rule rounds it to 0.1 g from 1 g and to 0.01 g below.
 */
public class NutrientRoundingStepTest {

	private static final String EU = "EU";
	private static final String ROUNDED_VALUE_KEY = "v";
	private static final String SUPPORTED_LOCALES = "en,fr";
	private static final String GRAMS_PER_100G = "g/100g";
	private static final double MILLIGRAM_STEP = 0.001d;
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

	@Test
	public void saltFollowsTheRegulationWithoutStep() {
		assertEquals(1d, euValue(roundedSalt(product(), 1.0204d)), DELTA);
		assertEquals(0.99d, euValue(roundedSalt(product(), 0.9912d)), DELTA);
	}

	@Test
	public void saltKeepsThreeDecimalsWithAMilligramStep() {
		assertEquals(1.02d, euValue(roundedSalt(productWithSaltStep(), 1.0204d)), DELTA);
		assertEquals(0.991d, euValue(roundedSalt(productWithSaltStep(), 0.9912d)), DELTA);
		assertEquals(1.271d, euValue(roundedSalt(productWithSaltStep(), 1.2705d)), DELTA);
	}

	@Test
	public void lowSaltIsNoLongerForcedToZero() {
		assertEquals(0d, euValue(roundedSalt(product(), 0.0114d)), DELTA);
		assertEquals(0.011d, euValue(roundedSalt(productWithSaltStep(), 0.0114d)), DELTA);
	}

	@Test
	public void stepOfANutrientLeavesTheOthersOnTheRegulation() {
		NutListDataItem fat = NutListDataItem.build().withValue(18.94d).withUnit(GRAMS_PER_100G);

		RegulationFormulationHelper.extractRoundedValue(productWithSaltStep(), NutrientCode.Fat, fat);

		assertEquals(19d, euValue(new JSONObject(fat.getRoundedValue())), DELTA);
		assertFalse(RegulationFormulationHelper.hasCustomRounding(fat.getRoundedValue(), EU));
	}

	@Test
	public void customRoundingIsRecordedOnTheLine() {
		NutListDataItem salt = saltLine(0.9912d);

		RegulationFormulationHelper.extractRoundedValue(productWithSaltStep(), NutrientCode.Salt, salt);

		assertTrue(RegulationFormulationHelper.hasCustomRounding(salt.getRoundedValue(), EU));
	}

	@Test
	public void lowSaltIsDisplayedAsRoundedWithAStep() {
		NutListDataItem salt = saltLine(0.0114d);
		RegulationFormulationHelper.extractRoundedValue(productWithSaltStep(), NutrientCode.Salt, salt);

		RegulatedNutrient displayed = RegulationFormulationHelper.extractRegulatedNutrient(salt, NutrientCode.Salt, Locale.FRENCH, EU);

		assertEquals("0,011", displayed.displayValue());
	}

	@Test
	public void lowSaltKeepsTheRegulatoryDisplayWithoutStep() {
		NutListDataItem salt = saltLine(0.0114d);
		RegulationFormulationHelper.extractRoundedValue(product(), NutrientCode.Salt, salt);

		RegulatedNutrient displayed = RegulationFormulationHelper.extractRegulatedNutrient(salt, NutrientCode.Salt, Locale.FRENCH, EU);

		assertEquals("< 0,01", displayed.displayValue());
	}

	@Test
	public void unusableStepFallsBackOnTheRegulation() {
		FinishedProductData product = product();
		product.setNutrientRoundingSteps(Map.of(NutrientCode.Salt, 0d));

		assertEquals(0.99d, euValue(roundedSalt(product, 0.9912d)), DELTA);
	}

	@Test
	public void missingValueStaysEmpty() {
		NutListDataItem salt = NutListDataItem.build().withUnit(GRAMS_PER_100G);

		RegulationFormulationHelper.extractRoundedValue(productWithSaltStep(), NutrientCode.Salt, salt);

		assertFalse(new JSONObject(salt.getRoundedValue()).getJSONObject(ROUNDED_VALUE_KEY).has(EU));
	}

	@Test
	public void clearingTheStepsRestoresTheRegulation() {
		FinishedProductData product = productWithSaltStep();
		product.setNutrientRoundingSteps(null);

		assertEquals(0.99d, euValue(roundedSalt(product, 0.9912d)), DELTA);
	}

	private static FinishedProductData product() {
		FinishedProductData product = new FinishedProductData();
		product.setUnit(ProductUnit.kg);
		product.setQty(1d);
		return product;
	}

	private static FinishedProductData productWithSaltStep() {
		FinishedProductData product = product();
		product.setNutrientRoundingSteps(Map.of(NutrientCode.Salt, MILLIGRAM_STEP));
		return product;
	}

	private static NutListDataItem saltLine(double value) {
		return NutListDataItem.build().withValue(value).withUnit(GRAMS_PER_100G);
	}

	private static JSONObject roundedSalt(FinishedProductData product, double value) {
		NutListDataItem salt = saltLine(value);
		RegulationFormulationHelper.extractRoundedValue(product, NutrientCode.Salt, salt);
		return new JSONObject(salt.getRoundedValue());
	}

	private static double euValue(JSONObject rounded) {
		return rounded.getJSONObject(ROUNDED_VALUE_KEY).getDouble(EU);
	}
}
