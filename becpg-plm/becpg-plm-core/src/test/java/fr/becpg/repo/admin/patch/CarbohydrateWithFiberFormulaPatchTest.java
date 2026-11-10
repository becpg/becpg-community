package fr.becpg.repo.admin.patch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * #37203 : the carbohydrate with fiber nutrient adds the carbohydrate and the fiber, an empty one counting for 0. Both
 * formulas shipped before are replaced, a formula customized on the instance is not.
 */
public class CarbohydrateWithFiberFormulaPatchTest {

	private static final double DELTA = 0.000001d;

	private static final NodeRef CARBOHYDRATE = new NodeRef("workspace://SpacesStore/02e32ab4-0dbf-494e-87f8-d667855a3c28");
	private static final NodeRef FIBER = new NodeRef("workspace://SpacesStore/5106481a-72f3-4e06-9077-a0bc05d966d1");

	private static final String FORMULA_WITHOUT_DEFAULT = "(nut['" + CARBOHYDRATE + "']?.value(\"EU\") == null and nut['" + FIBER
			+ "']?.value(\"EU\") == null) ? null : nut['" + CARBOHYDRATE + "']?.value(\"EU\") + nut['" + FIBER + "']?.value(\"EU\")";
	private static final String FORMULA_WITHOUT_PARENTHESES = "(nut['" + CARBOHYDRATE + "']?.value(\"EU\")== null and nut['" + FIBER
			+ "']?.value(\"EU\")== null) ? null : nut['" + CARBOHYDRATE + "']?.value(\"EU\") ?: 0.0 + nut['" + FIBER
			+ "']?.value(\"EU\") ?: 0.0";
	private static final String CUSTOM_FORMULA = "nut['" + CARBOHYDRATE + "']?.value(\"EU\")";

	@Test
	public void formulaWithoutDefaultIsCorrected() {
		Optional<String> corrected = CarbohydrateWithFiberFormulaPatch.correctFormula(FORMULA_WITHOUT_DEFAULT, CARBOHYDRATE, FIBER);

		assertEquals(55d, evaluate(corrected.orElseThrow(), 50d, 5d), DELTA);
	}

	@Test
	public void formulaWithoutParenthesesIsCorrected() {
		Optional<String> corrected = CarbohydrateWithFiberFormulaPatch.correctFormula(FORMULA_WITHOUT_PARENTHESES, CARBOHYDRATE, FIBER);

		assertEquals(55d, evaluate(corrected.orElseThrow(), 50d, 5d), DELTA);
	}

	@Test
	public void emptyCarbohydrateCountsForZero() {
		Optional<String> corrected = CarbohydrateWithFiberFormulaPatch.correctFormula(FORMULA_WITHOUT_DEFAULT, CARBOHYDRATE, FIBER);

		assertEquals(5d, evaluate(corrected.orElseThrow(), null, 5d), DELTA);
	}

	@Test
	public void emptyFiberCountsForZero() {
		Optional<String> corrected = CarbohydrateWithFiberFormulaPatch.correctFormula(FORMULA_WITHOUT_DEFAULT, CARBOHYDRATE, FIBER);

		assertEquals(50d, evaluate(corrected.orElseThrow(), 50d, null), DELTA);
	}

	@Test
	public void emptyCarbohydrateAndFiberGiveNoValue() {
		Optional<String> corrected = CarbohydrateWithFiberFormulaPatch.correctFormula(FORMULA_WITHOUT_DEFAULT, CARBOHYDRATE, FIBER);

		assertNull(evaluate(corrected.orElseThrow(), null, null));
	}

	@Test
	public void customizedFormulaIsLeftUntouched() {
		assertFalse(CarbohydrateWithFiberFormulaPatch.correctFormula(CUSTOM_FORMULA, CARBOHYDRATE, FIBER).isPresent());
	}

	@Test
	public void missingFormulaIsLeftUntouched() {
		assertFalse(CarbohydrateWithFiberFormulaPatch.correctFormula(null, CARBOHYDRATE, FIBER).isPresent());
	}

	@Test
	public void correctedFormulaIsNotCorrectedAgain() {
		String corrected = CarbohydrateWithFiberFormulaPatch.correctFormula(FORMULA_WITHOUT_DEFAULT, CARBOHYDRATE, FIBER).orElseThrow();

		assertFalse(CarbohydrateWithFiberFormulaPatch.correctFormula(corrected, CARBOHYDRATE, FIBER).isPresent());
	}

	private static Double evaluate(String formula, Double carbohydrate, Double fiber) {
		Map<String, NutrientValue> nutrients = new HashMap<>();
		nutrients.put(CARBOHYDRATE.toString(), new NutrientValue(carbohydrate));
		nutrients.put(FIBER.toString(), new NutrientValue(fiber));
		StandardEvaluationContext context = new StandardEvaluationContext(new FormulaContext(nutrients));
		return new SpelExpressionParser().parseExpression(formula).getValue(context, Double.class);
	}

	/**
	 * Stands for the formulation context, which exposes the nutrients by node reference.
	 */
	public static class FormulaContext {

		private final Map<String, NutrientValue> nut;

		FormulaContext(Map<String, NutrientValue> nut) {
			this.nut = nut;
		}

		public Map<String, NutrientValue> getNut() {
			return nut;
		}
	}

	/**
	 * Stands for a nutrient line, read by regulation.
	 */
	public static class NutrientValue {

		private final Double value;

		NutrientValue(Double value) {
			this.value = value;
		}

		public Double value(String regulation) {
			return value;
		}
	}

}
