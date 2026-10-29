package fr.becpg.repo.score;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.model.GS1Model;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.formulation.nutrient.NutrientCode;
import fr.becpg.repo.product.formulation.nutrient.RegulationFormulationHelper;

/**
 * Unit tests of the nutrient values the front of pack schemes compare to their thresholds.
 *
 * <p>The EU rounding stores the sodium in grams whereas the thresholds are written in
 * milligrams, the unit of the nutrient list: the value read must be back in milligrams.</p>
 */
public class NutrientValueProviderTest {

	private static final double PRECISION = 0.0001d;

	private static final String MILLIGRAMS_PER_100G = "mg/100g";

	private static final String KCAL_PER_100G = "kcal/100g";

	private final NodeRef sodium = new NodeRef("workspace://SpacesStore/sodium");

	private final NodeRef energy = new NodeRef("workspace://SpacesStore/energy");

	private NutrientValueProvider provider;

	private FinishedProductData product;

	@Before
	public void setUp() {
		MLTextHelper.flushCache();
		MLTextHelper.setSupportedLocales("en,fr");

		NodeService nodeService = mock(NodeService.class);
		when(nodeService.getProperty(sodium, GS1Model.PROP_NUTRIENT_TYPE_CODE)).thenReturn(NutrientCode.Sodium);
		when(nodeService.getProperty(energy, GS1Model.PROP_NUTRIENT_TYPE_CODE)).thenReturn(NutrientCode.Energykcal);
		provider = new NutrientValueProvider(nodeService);

		product = new FinishedProductData();
		product.setUnit(ProductUnit.kg);
		product.setQty(1d);
	}

	@After
	public void tearDown() {
		MLTextHelper.flushCache();
	}

	@Test
	public void readsTheRoundedSodiumInMilligrams() {
		NutListDataItem sodiumLine = nutLine(sodium, NutrientCode.Sodium, 437d, MILLIGRAMS_PER_100G);
		product.setNutList(List.of(sodiumLine));

		double roundedInGrams = sodiumLine.value("EU");

		assertEquals(roundedInGrams * 1000d, provider.extractNutrients(product).get(NutrientCode.Sodium), PRECISION);
	}

	@Test
	public void readsAnExactlyRoundedSodiumWithoutFloatingPointDrift() {
		product.setNutList(List.of(nutLine(sodium, NutrientCode.Sodium, 300d, MILLIGRAMS_PER_100G)));

		assertEquals(300d, provider.extractNutrients(product).get(NutrientCode.Sodium), 0d);
	}

	@Test
	public void readsTheUnroundedSodiumAsItStands() {
		product.setUseUnroundedNutrients(true);
		product.setNutList(List.of(nutLine(sodium, NutrientCode.Sodium, 437d, MILLIGRAMS_PER_100G)));

		assertEquals(437d, provider.extractNutrients(product).get(NutrientCode.Sodium), PRECISION);
	}

	@Test
	public void leavesTheEnergyInKilocalories() {
		product.setNutList(List.of(nutLine(energy, NutrientCode.Energykcal, 124d, KCAL_PER_100G)));

		Map<String, Double> values = provider.extractNutrients(product);

		assertEquals(124d, values.get(NutrientCode.Energykcal), PRECISION);
	}

	@Test
	public void keepsTheRoundedValueWhenTheLineHasNoUnit() {
		assertEquals(0.4d, RegulationFormulationHelper.toNutUnit(0.4d, NutrientCode.Sodium, null, "EU"), 0d);
	}

	private NutListDataItem nutLine(NodeRef nut, String code, double value, String unit) {
		NutListDataItem line = NutListDataItem.build().withNut(nut).withValue(value).withUnit(unit);
		RegulationFormulationHelper.extractRoundedValue(product, code, line);
		return line;
	}

}
