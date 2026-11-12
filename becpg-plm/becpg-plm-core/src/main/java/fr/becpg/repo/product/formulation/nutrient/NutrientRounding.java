package fr.becpg.repo.product.formulation.nutrient;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import fr.becpg.repo.product.formulation.nutrient.AbstractNutrientRegulation.NutrientDefinition;

/**
 * Rounds the values of one nutrient for one regulation. The rule of the regulation
 * applies unless the product declares its own rounding step for the nutrient (see
 * {@code ProductData#getNutrientRoundingSteps()}): the step then replaces the rule,
 * so that a value can be labelled with more precision than the regulation asks for
 * (salt to the milligram, for instance).
 *
 * @param regulation the regulation the value is rounded for
 * @param nutCode the regulatory code of the nutrient
 * @param nutUnit the unit of the nutrient list values
 * @param step the rounding step declared by the product, in the unit of the regulation, or {@code null}
 * @author matthieu
 */
record NutrientRounding(NutrientRegulation regulation, String nutCode, String nutUnit, Double step) {

	/**
	 * Whether the product replaces the rule of the regulation by its own step.
	 *
	 * @return {@code true} when a usable step is declared
	 */
	boolean isCustom() {
		return (step != null) && (step > 0d) && !step.isNaN() && !step.isInfinite();
	}

	/**
	 * Rounds a value of the nutrient list.
	 *
	 * @param value the value, in the unit of the nutrient list, may be {@code null}
	 * @return the rounded value, in the unit of the regulation
	 */
	Double round(Double value) {
		if (!isCustom() || (value == null) || value.isNaN() || value.isInfinite()) {
			return regulation.round(value, nutCode, nutUnit);
		}

		BigDecimal roundingStep = BigDecimal.valueOf(step);
		return BigDecimal.valueOf(toRegulationUnit(value)).divide(roundingStep, MathContext.DECIMAL64).setScale(0, RoundingMode.HALF_UP)
				.multiply(roundingStep).doubleValue();
	}

	private Double toRegulationUnit(Double value) {
		NutrientDefinition definition = regulation.getNutrientDefinition(nutCode);
		String regulationUnit = definition != null ? definition.getUnit() : nutUnit;
		return regulation.convertValue(value, nutUnit, regulationUnit);
	}
}
