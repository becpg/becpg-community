package fr.becpg.repo.product.formulation.labeling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.Test;

import fr.becpg.repo.product.data.constraints.DeclarationType;

/**
 * #37613 : the components of a detailed semi-finished product only carry its own yield, so that its bracket keeps
 * the proportions of its recipe, while declared components still carry the yield inherited from the levels above.
 * #37354 : declaring the legal name of a reconstitutable raw material turns its reconstitution off.
 */
public class LabelingFormulationHandlerTest {

	private static final double DELTA = 0.000001d;

	private static final BigDecimal INHERITED_YIELD = BigDecimal.valueOf(90.76d);

	@Test
	public void detailedSemiFinishedDoesNotHandDownTheInheritedYield() {
		BigDecimal subCompositionYield = LabelingFormulationHandler.computeSubCompositionYield(null, INHERITED_YIELD, DeclarationType.Detail);

		assertEquals(100d, subCompositionYield.doubleValue(), DELTA);
	}

	@Test
	public void detailedSemiFinishedHandsDownItsOwnYield() {
		BigDecimal subCompositionYield = LabelingFormulationHandler.computeSubCompositionYield(80d, INHERITED_YIELD, DeclarationType.Detail);

		assertEquals(80d, subCompositionYield.doubleValue(), DELTA);
	}

	@Test
	public void declaredSemiFinishedCombinesItsOwnYieldWithTheInheritedOne() {
		BigDecimal subCompositionYield = LabelingFormulationHandler.computeSubCompositionYield(80d, BigDecimal.valueOf(90d), DeclarationType.Declare);

		assertEquals(72d, subCompositionYield.doubleValue(), DELTA);
	}

	@Test
	public void semiFinishedWithoutYieldOfItsOwnHandsDownTheInheritedOne() {
		BigDecimal subCompositionYield = LabelingFormulationHandler.computeSubCompositionYield(0d, INHERITED_YIELD, DeclarationType.Declare);

		assertEquals(90.76d, subCompositionYield.doubleValue(), DELTA);
	}

	@Test
	public void kitStillCarriesTheInheritedYield() {
		BigDecimal subCompositionYield = LabelingFormulationHandler.computeSubCompositionYield(null, INHERITED_YIELD, DeclarationType.Kit);

		assertEquals(90.76d, subCompositionYield.doubleValue(), DELTA);
	}

	@Test
	public void legalNameDeclarationTurnsTheReconstitutionOff() {
		assertFalse(LabelingFormulationHandler.isReconstitutionRequested(DeclarationType.DoNotDetails));
	}

	@Test
	public void legalNameDeclaredAtTheEndTurnsTheReconstitutionOff() {
		assertFalse(LabelingFormulationHandler.isReconstitutionRequested(DeclarationType.DoNotDetailsAtEnd));
	}

	@Test
	public void defaultDeclarationKeepsTheReconstitution() {
		assertTrue(LabelingFormulationHandler.isReconstitutionRequested(DeclarationType.Declare));
		assertTrue(LabelingFormulationHandler.isReconstitutionRequested(null));
	}

	@Test
	public void ingredientDeclarationsKeepTheReconstitution() {
		assertTrue(LabelingFormulationHandler.isReconstitutionRequested(DeclarationType.Detail));
		assertTrue(LabelingFormulationHandler.isReconstitutionRequested(DeclarationType.DoNotDeclare));
	}
}
