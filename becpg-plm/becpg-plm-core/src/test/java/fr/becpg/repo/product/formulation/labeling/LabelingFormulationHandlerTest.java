package fr.becpg.repo.product.formulation.labeling;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

import fr.becpg.repo.product.data.constraints.DeclarationType;

/**
 * #37613 : the components of a detailed semi-finished product only carry its own yield, so that its bracket keeps
 * the proportions of its recipe, while declared components still carry the yield inherited from the levels above.
 */
public class LabelingFormulationHandlerTest {

	private static final double DELTA = 0.000001d;

	private static final BigDecimal INHERITED_YIELD = BigDecimal.valueOf(90.76d);

	@Test
	public void detailedSemiFinishedDoesNotHandDownTheInheritedYield() {
		BigDecimal yield = LabelingFormulationHandler.computeSubCompositionYield(null, INHERITED_YIELD, DeclarationType.Detail);

		assertEquals(100d, yield.doubleValue(), DELTA);
	}

	@Test
	public void detailedSemiFinishedHandsDownItsOwnYield() {
		BigDecimal yield = LabelingFormulationHandler.computeSubCompositionYield(80d, INHERITED_YIELD, DeclarationType.Detail);

		assertEquals(80d, yield.doubleValue(), DELTA);
	}

	@Test
	public void declaredSemiFinishedCombinesItsOwnYieldWithTheInheritedOne() {
		BigDecimal yield = LabelingFormulationHandler.computeSubCompositionYield(80d, BigDecimal.valueOf(90d), DeclarationType.Declare);

		assertEquals(72d, yield.doubleValue(), DELTA);
	}

	@Test
	public void semiFinishedWithoutYieldOfItsOwnHandsDownTheInheritedOne() {
		BigDecimal yield = LabelingFormulationHandler.computeSubCompositionYield(0d, INHERITED_YIELD, DeclarationType.Declare);

		assertEquals(90.76d, yield.doubleValue(), DELTA);
	}

	@Test
	public void kitStillCarriesTheInheritedYield() {
		BigDecimal yield = LabelingFormulationHandler.computeSubCompositionYield(null, INHERITED_YIELD, DeclarationType.Kit);

		assertEquals(90.76d, yield.doubleValue(), DELTA);
	}
}
