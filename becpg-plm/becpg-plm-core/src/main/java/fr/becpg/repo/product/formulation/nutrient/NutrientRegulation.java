package fr.becpg.repo.product.formulation.nutrient;

import java.util.Locale;

import org.alfresco.util.Pair;

import fr.becpg.repo.product.formulation.nutrient.AbstractNutrientRegulation.NutrientDefinition;

/**
 * <p>NutrientRegulation interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface NutrientRegulation {

	/**
	 * <p>round.</p>
	 *
	 * @param value a {@link java.lang.Double} object.
	 * @param nutrientTypeCode a {@link java.lang.String} object.
	 * @param nutUnit a {@link java.lang.String} object.
	 * @return a {@link java.lang.Double} object.
	 */
	Double round(Double value, String nutrientTypeCode, String nutUnit);

	/**
	 * <p>roundGDA.</p>
	 *
	 * @param value a {@link java.lang.Double} object.
	 * @param nutrientTypeCode a {@link java.lang.String} object.
	 * @return a {@link java.lang.Double} object.
	 */
	Double roundGDA(Double value, String nutrientTypeCode);

	/**
	 * <p>getNutrientDefinition.</p>
	 *
	 * @param nutCode a {@link java.lang.String} object.
	 * @return a {@link fr.becpg.repo.product.formulation.nutrient.AbstractNutrientRegulation.NutrientDefinition} object.
	 */
	NutrientDefinition getNutrientDefinition(String nutCode);
	
	/**
	 * <p>displayValue.</p>
	 *
	 * @param value a {@link java.lang.Double} object.
	 * @param roundedValue a {@link java.lang.Double} object.
	 * @param nutrientTypeCode a {@link java.lang.String} object.
	 * @param locale a {@link java.util.Locale} object.
	 * @return a {@link java.lang.String} object.
	 * @param measurementPrecision a {@link java.lang.String} object
	 */
	String displayValue(Double value, Double roundedValue, String nutrientTypeCode, String measurementPrecision, Locale locale);

	/**
	 * <p>convertValue.</p>
	 *
	 * @param value a {@link java.lang.Double} object.
	 * @param nutUnit a {@link java.lang.String} object.
	 * @param regulUnit a {@link java.lang.String} object.
	 * @return a {@link java.lang.Double} object.
	 */
	Double convertValue(Double value, String nutUnit, String regulUnit);
	
	
	/**
	 * <p>tolerances.</p>
	 *
	 * @param value a {@link java.lang.Double} object
	 * @param nutrientTypeCode a {@link java.lang.String} object
	 * @param nutUnit a {@link java.lang.String} object
	 * @return a {@link org.alfresco.util.Pair} object
	 */
	Pair<Double, Double> tolerances(Double value, String nutrientTypeCode, String nutUnit);

	/**
	 * <p>tolerances.</p>
	 *
	 * <p>Claim-aware tolerances. When {@code isClaimed} is {@code true} the nutrient is subject to a
	 * nutritional or health claim and the claim-specific tolerances (EU Table 3, "side 1 / side 2")
	 * are applied instead of the standard ones (EU Table 1). Regulations that do not distinguish the
	 * claim case fall back to the standard tolerances.</p>
	 *
	 * @param value a {@link java.lang.Double} object
	 * @param nutrientTypeCode a {@link java.lang.String} object
	 * @param nutUnit a {@link java.lang.String} object
	 * @param isClaimed whether the nutrient is subject to a claim
	 * @return a {@link org.alfresco.util.Pair} object
	 */
	default Pair<Double, Double> tolerances(Double value, String nutrientTypeCode, String nutUnit, boolean isClaimed) {
		return tolerances(value, nutrientTypeCode, nutUnit);
	}
}
