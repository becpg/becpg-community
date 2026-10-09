package fr.becpg.repo.product.formulation.nutrient;

import static org.junit.Assert.assertEquals;

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
 * of 18.9 g of fat (maximum 23 g, minimum 15 g).
 */
public class RegulationFormulationHelperTest {

	private static final String EU = "EU";

	private static final String TOLERANCE_MAX_COLUMN = "ToleranceMax";

	private static final String TOLERANCE_MIN_COLUMN = "ToleranceMin";

	private static final String SUPPORTED_LOCALES = "en,fr";

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
}
