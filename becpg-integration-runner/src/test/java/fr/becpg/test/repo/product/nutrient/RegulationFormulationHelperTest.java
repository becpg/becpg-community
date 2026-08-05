package fr.becpg.test.repo.product.nutrient;

import java.util.Locale;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.formulation.nutrient.NutrientDisplayRule;
import fr.becpg.repo.product.formulation.nutrient.RegulatedNutrient;
import fr.becpg.repo.product.formulation.nutrient.RegulationFormulationHelper;

/**
 * Guards {@code RegulationFormulationHelper.extractRegulatedNutrient} against drifting away from
 * {@code extractXMLAttribute}. Both read the same regulation definition and the same rounded value
 * blob, so a nutrient rendered on a label must say exactly what the BIRT data source declares for
 * that same nutrient.
 */
public class RegulationFormulationHelperTest {

	private static final String US_REGULATION_KEY = "US";

	private static final String CHOLESTEROL_NUT_CODE = "CHOL-";

	private static final String ADDED_SUGARS_NUT_CODE = "SUGAD";

	private static final String SUPPORTED_LOCALES = "en_US, fr_FR";

	private static final String NUT_LIST_ELEMENT = "nutList";

	@Before
	public void setUpSupportedLocales() {
		MLTextHelper.setSupportedLocales(SUPPORTED_LOCALES);
	}

	@After
	public void resetSupportedLocales() {
		MLTextHelper.setSupportedLocales(null);
	}

	@Test
	public void testCholesterolMatchesReportAttributes() {
		assertMatchesReportAttributes(CHOLESTEROL_NUT_CODE, 5d, 2d, 4d, 300d);
	}

	@Test
	public void testAddedSugarsMatchesReportAttributes() {
		assertMatchesReportAttributes(ADDED_SUGARS_NUT_CODE, 12d, 10d, 20d, 50d);
	}

	@Test
	public void testCholesterolDisplayRule() {
		NutrientDisplayRule rule = extractRegulated(CHOLESTEROL_NUT_CODE, 5d, 2d, 4d).displayRule();

		Assert.assertTrue("Cholesterol is mandatory under the FDA regulation", rule.mandatory());
		Assert.assertEquals(NutrientDisplayRule.DISPLAY_MODE_MANDATORY, rule.displayMode());
		Assert.assertTrue("Cholesterol is a bold, top level nutrient", rule.bold());
		Assert.assertEquals(1, rule.indentLevel());
		Assert.assertTrue(rule.showGDAPerc());
	}

	@Test
	public void testAddedSugarsIsIndentedTwiceUnderTotalSugars() {
		NutrientDisplayRule rule = extractRegulated(ADDED_SUGARS_NUT_CODE, 12d, 10d, 20d).displayRule();

		Assert.assertEquals(3, rule.indentLevel());
		Assert.assertFalse("Added sugars is an indented line, never bold", rule.bold());
	}

	@Test
	public void testUnknownNutrientYieldsAnUndefinedRule() {
		RegulatedNutrient regulated = extractRegulated("NOT_A_NUT_CODE", 1d, 1d, 1d);

		Assert.assertFalse(regulated.displayRule().isDefined());
		Assert.assertNull(regulated.displayRule().displayMode());
		Assert.assertEquals(1, regulated.displayRule().indentLevel());
	}

	private void assertMatchesReportAttributes(String nutCode, Double value, Double valuePerServing, Double valuePerContainer, Double gda) {

		String roundedValue = buildRoundedValue(value, valuePerServing, valuePerContainer, gda);
		RegulatedNutrient regulated = RegulationFormulationHelper.extractRegulatedNutrient(
				buildNutListItem(value, valuePerServing, roundedValue), nutCode, Locale.US, US_REGULATION_KEY);

		Element nutListElt = buildNutListElement(nutCode, value, valuePerServing);
		RegulationFormulationHelper.extractXMLAttribute(nutListElt, roundedValue, Locale.US, true, null);

		assertSameInteger(nutListElt, "regulSort", regulated.displayRule().sort());
		assertSameInteger(nutListElt, "regulDepthLevel", regulated.displayRule().depthLevel());
		Assert.assertEquals(nutListElt.attributeValue("regulDisplayMode"), regulated.displayRule().displayMode());
		assertSameBoolean(nutListElt, "regulBold", regulated.displayRule().bold());
		assertSameBoolean(nutListElt, "regulShowGDAPerc", regulated.displayRule().showGDAPerc());
		assertSameDouble(nutListElt, "regulGDA", regulated.displayRule().gda());
		Assert.assertEquals(nutListElt.attributeValue("regulUnit"), regulated.displayRule().unit());

		assertSameDouble(nutListElt, "roundedValue", regulated.value());
		assertSameDouble(nutListElt, "roundedValuePerServing", regulated.valuePerServing());
		assertSameDouble(nutListElt, "roundedValuePerContainer", regulated.valuePerContainer());
		assertSameDouble(nutListElt, "roundedGDAPerc", regulated.gdaPerc());
		assertSameDouble(nutListElt, "roundedGDAPercPerContainer", regulated.gdaPercPerContainer());

		Assert.assertEquals(nutListElt.attributeValue("roundedDisplayValue"), regulated.displayValue());
		Assert.assertEquals(nutListElt.attributeValue("roundedDisplayValuePerServing"), regulated.displayValuePerServing());
		Assert.assertEquals(nutListElt.attributeValue("roundedDisplayValuePerContainer"), regulated.displayValuePerContainer());
	}

	private RegulatedNutrient extractRegulated(String nutCode, Double value, Double valuePerServing, Double valuePerContainer) {
		String roundedValue = buildRoundedValue(value, valuePerServing, valuePerContainer, 300d);
		return RegulationFormulationHelper.extractRegulatedNutrient(buildNutListItem(value, valuePerServing, roundedValue), nutCode,
				Locale.US, US_REGULATION_KEY);
	}

	private String buildRoundedValue(Double value, Double valuePerServing, Double valuePerContainer, Double gda) {
		return "{\"v\":{\"US\":" + value + "},\"vps\":{\"US\":" + valuePerServing + "},\"vpc\":{\"US\":" + valuePerContainer + "},\"gda\":{\"US\":"
				+ (100 * valuePerServing / gda) + "},\"gdapc\":{\"US\":" + (100 * valuePerContainer / gda) + "},\"unit\":{\"US\":\"mg\"}}";
	}

	private NutListDataItem buildNutListItem(Double value, Double valuePerServing, String roundedValue) {
		NutListDataItem nutListItem = new NutListDataItem();
		nutListItem.setValue(value);
		nutListItem.setValuePerServing(valuePerServing);
		nutListItem.setRoundedValue(roundedValue);
		return nutListItem;
	}

	private Element buildNutListElement(String nutCode, Double value, Double valuePerServing) {
		Element nutListElt = DocumentHelper.createElement(NUT_LIST_ELEMENT);
		nutListElt.addAttribute(RegulationFormulationHelper.ATTR_NUT_CODE, nutCode);
		nutListElt.addAttribute(PLMModel.PROP_NUTLIST_VALUE.getLocalName(), String.valueOf(value));
		nutListElt.addAttribute(PLMModel.PROP_NUTLIST_VALUE_PER_SERVING.getLocalName(), String.valueOf(valuePerServing));
		return nutListElt;
	}

	private void assertSameInteger(Element nutListElt, String attribute, Integer actual) {
		String expected = nutListElt.attributeValue(attribute);
		Assert.assertEquals(attribute, expected, actual != null ? String.valueOf(actual) : null);
	}

	private void assertSameBoolean(Element nutListElt, String attribute, boolean actual) {
		String expected = nutListElt.attributeValue(attribute);
		Assert.assertEquals(attribute, Boolean.parseBoolean(expected), actual);
	}

	private void assertSameDouble(Element nutListElt, String attribute, Double actual) {
		String expected = nutListElt.attributeValue(attribute);
		if (expected == null) {
			Assert.assertNull(attribute, actual);
			return;
		}
		Assert.assertNotNull(attribute, actual);
		Assert.assertEquals(attribute, Double.parseDouble(expected), actual, 0.0001d);
	}

}
