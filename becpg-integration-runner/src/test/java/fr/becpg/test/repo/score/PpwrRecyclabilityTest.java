package fr.becpg.test.repo.score;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.productList.PackMaterialListDataItem;
import fr.becpg.repo.product.formulation.score.PpwrRecyclability;
import fr.becpg.repo.score.ScoreContext;
import fr.becpg.repo.score.ScorePart;
import fr.becpg.repo.score.data.ScoreDefinitionItem;
import fr.becpg.repo.score.data.ScoreThresholdListDataItem;

/**
 * Checks the PPWR grades against the thresholds of annex II table 3.
 *
 * <p>The materials are named by their code rather than read from a repository, so the
 * weighting of the rates is checked without standing one up.</p>
 *
 * @author matthieu
 */
public class PpwrRecyclabilityTest {

	private static final double PRECISION = 0.01d;

	private static final String CLEAR_PET = "POLYMER_PET_PLASTIC_CLEAR";

	private static final String PVC = "POLYMER_PVC";

	private static final String COMPLEX = "POLYMER_OTHER";

	private static final String CARDBOARD = "PAPER_OTHER";

	private static final String RANGE = "A: [95;100] B: [80;95) C: [70;80) NR: [0;70)";

	/** Eco-tax category of the Citeo scale for a rigid PET packaging */
	private static final String RIGID_PET_CATEGORY = "6.3.3";

	private final Set<String> flagged = new HashSet<>();

	private final Map<String, String> categories = new HashMap<>();

	private PpwrRecyclability plugin;

	private ProductData product;

	@Before
	public void setUp() {
		flagged.clear();
		categories.clear();
		product = new FinishedProductData();
		product.setPackMaterialList(new ArrayList<>());
		plugin = new PpwrRecyclability(null, null, null) {
			@Override
			protected String materialCode(NodeRef material) {
				return material.getId();
			}

			@Override
			protected boolean isFlaggedNotRecyclable(NodeRef material) {
				return flagged.contains(material.getId());
			}

			@Override
			protected String ecoTaxeCategory(NodeRef material) {
				return categories.get(material.getId());
			}
		};
	}

	@Test
	public void testFullyRecyclablePackagingReachesGradeA() {
		pack(CLEAR_PET, 24d, PackagingLevel.Primary);

		assertEquals(95d, value(), PRECISION);
		assertEquals("A", grade(PackagingLevel.Primary));
	}

	@Test
	public void testASleeveDragsTheBottleDownOneGrade() {
		pack(CLEAR_PET, 24d, PackagingLevel.Primary);
		pack(PVC, 2d, PackagingLevel.Primary);

		// (24 x 95 + 2 x 0) / 26, the sleeve weighing in the unit without being recyclable
		assertEquals(87.69d, value(), PRECISION);
		assertEquals("B", grade(PackagingLevel.Primary));
	}

	@Test
	public void testTheWorstUnitDecidesTheScoreOfTheProduct() {
		pack(CLEAR_PET, 24d, PackagingLevel.Primary);
		pack(COMPLEX, 100d, PackagingLevel.Secondary);

		// each packaging unit is graded on its own, the failing one blocks the product
		assertEquals(30d, value(), PRECISION);
		assertEquals("A", grade(PackagingLevel.Primary));
		assertEquals("NR", grade(PackagingLevel.Secondary));
	}

	@Test
	public void testAMaterialWithoutRateIsNotRecyclableAndIsPublished() {
		pack(CARDBOARD, 50d, PackagingLevel.Primary);
		pack("UNDOCUMENTED", 50d, PackagingLevel.Primary);

		assertEquals(47.5d, value(), PRECISION);
		assertEquals(50d, part("PPWR_UNKNOWN").getValue(), PRECISION);
	}

	@Test
	public void testTheMaterialFlagWinsOverTheReferenceData() {
		pack(CLEAR_PET, 24d, PackagingLevel.Primary);
		flagged.add(CLEAR_PET);

		assertEquals(0d, value(), PRECISION);
		assertEquals("NR", grade(PackagingLevel.Primary));
	}

	@Test
	public void testAPackagingWithoutWeightIsNotGraded() {
		pack(CLEAR_PET, null, PackagingLevel.Primary);

		assertNull(context().getValue());
	}

	@Test
	public void testAMaterialOfAnotherCodeGenerationIsMatchedByItsEcoTaxCategory() {
		pack("PLASTIC_RIGID_PET", 50d, PackagingLevel.Primary);
		categories.put("PLASTIC_RIGID_PET", RIGID_PET_CATEGORY);

		// the code says nothing to the reference data, the category names the same family
		assertEquals(80d, value(), PRECISION);
		assertEquals("B", grade(PackagingLevel.Primary));
	}

	@Test
	public void testTheMaterialCodeWinsOverItsCategory() {
		pack(CLEAR_PET, 50d, PackagingLevel.Primary);
		categories.put(CLEAR_PET, RIGID_PET_CATEGORY);

		assertEquals(95d, value(), PRECISION);
	}

	/**
	 * <p>Adds one packaging material line to the product.</p>
	 *
	 * @param code the code of the material
	 * @param weight the weight it takes in the packaging, in grams
	 * @param level a {@link fr.becpg.repo.product.data.constraints.PackagingLevel} object
	 */
	private void pack(String code, Double weight, PackagingLevel level) {
		product.getPackMaterialList().add(PackMaterialListDataItem.build()
				.withMaterial(new NodeRef("workspace://SpacesStore/" + code)).withWeight(weight).withPkgLevel(level));
	}

	/**
	 * <p>definition.</p>
	 *
	 * @return a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 */
	private ScoreDefinitionItem definition() {
		ScoreDefinitionItem definition = new ScoreDefinitionItem();

		definition.setRange(RANGE);
		definition.setThresholdList(List.of(threshold(CLEAR_PET, 95d), threshold(PVC, 0d), threshold(COMPLEX, 30d),
				threshold(CARDBOARD, 95d), threshold(RIGID_PET_CATEGORY, 80d)));

		return definition;
	}

	/**
	 * <p>threshold.</p>
	 *
	 * @param code the code of the material
	 * @param rate its recyclability rate, in percent
	 * @return a {@link fr.becpg.repo.score.data.ScoreThresholdListDataItem} object
	 */
	private ScoreThresholdListDataItem threshold(String code, Double rate) {
		ScoreThresholdListDataItem threshold = new ScoreThresholdListDataItem();

		threshold.setNutCode(code);
		threshold.setPoints(rate);

		return threshold;
	}

	private double value() {
		return context().getValue();
	}

	/**
	 * <p>Grade published for one packaging level.</p>
	 *
	 * @param level a {@link fr.becpg.repo.product.data.constraints.PackagingLevel} object
	 * @return a {@link java.lang.String} object
	 */
	private String grade(PackagingLevel level) {
		return part("PPWR_" + level.name().toUpperCase()).getScoreClass();
	}

	/**
	 * <p>part.</p>
	 *
	 * @param code the code of the wanted part
	 * @return a {@link fr.becpg.repo.score.ScorePart} object
	 */
	private ScorePart part(String code) {
		Optional<ScorePart> found = context().getParts().stream().filter(part -> code.equals(part.getCode())).findFirst();

		return found.orElseThrow(() -> new IllegalStateException("No part " + code + " in the breakdown"));
	}

	/**
	 * The context builder is private, the plugin publishing through the score writer, so the
	 * test reaches it by reflection rather than by standing up a repository.
	 *
	 * @return a {@link fr.becpg.repo.score.ScoreContext} object
	 */
	private ScoreContext context() {
		try {
			Method method = PpwrRecyclability.class.getDeclaredMethod("buildContext", ProductData.class, ScoreDefinitionItem.class);
			method.setAccessible(true);
			return (ScoreContext) method.invoke(plugin, product, definition());
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}

}
