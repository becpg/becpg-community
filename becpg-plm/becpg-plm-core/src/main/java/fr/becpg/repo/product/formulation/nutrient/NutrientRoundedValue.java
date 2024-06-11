package fr.becpg.repo.product.formulation.nutrient;

import java.math.BigDecimal;

/**
 * <p>NutrientRoundedValue class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class NutrientRoundedValue {

	String nutrientTypeCode;
	Double value;


	interface NutrientRoundedRuleFunction {
		public abstract NutrientRoundedRule getRule(Double value);
	}

	NutrientRoundedRuleFunction rule;

	Double toleranceMax;
	Double toleranceMin;
	Boolean isPerc;

	/**
	 * <p>Constructor for NutrientRoundedValue.</p>
	 *
	 * @param nutrientTypeCode a {@link java.lang.String} object
	 * @param value a {@link java.lang.Double} object
	 */
	public NutrientRoundedValue(String nutrientTypeCode, Double value) {
		super();
		this.nutrientTypeCode = nutrientTypeCode;
		this.value = value;
	}

	/**
	 * <p>setTolerances.</p>
	 *
	 * @param tolerance a {@link java.lang.Double} object
	 * @param isPerc a boolean
	 */
	public void setTolerances(Double tolerance, boolean isPerc) {
		this.toleranceMax = tolerance;
		this.toleranceMin = tolerance;
		this.isPerc = isPerc;
	}

	/**
	 * <p>setTolerances.</p>
	 *
	 * @param toleranceMax a {@link java.lang.Double} object
	 * @param toleranceMin a {@link java.lang.Double} object
	 * @param isPerc a boolean
	 */
	public void setTolerances(Double toleranceMax, Double toleranceMin, boolean isPerc) {
		this.toleranceMax = toleranceMax;
		this.toleranceMin = toleranceMin;
		this.isPerc = isPerc;
	}

	/**
	 * <p>Setter for the field <code>rule</code>.</p>
	 *
	 * @param rule a {@link fr.becpg.repo.product.formulation.nutrient.NutrientRoundedValue.NutrientRoundedRuleFunction} object
	 */
	public void setRule(NutrientRoundedRuleFunction rule) {
		this.rule = rule;
	}

	/**
	 * <p>Getter for the field <code>nutrientTypeCode</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getNutrientTypeCode() {
		return nutrientTypeCode;
	}

	/**
	 * <p>Getter for the field <code>value</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getValue() {
		return value;
	}

	/**
	 * <p>getRoundedValue.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getRoundedValue() {
		return rule.getRule(value).round(value);
	}

	/**
	 * <p>Getter for the field <code>toleranceMax</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getToleranceMax() {
		return toleranceMax;
	}

	/**
	 * <p>Getter for the field <code>toleranceMin</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getToleranceMin() {
		return toleranceMin;
	}

	/**
	 * <p>getMaxRoundedValue.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getMaxRoundedValue() {
		Double roundedValue = getRoundedValue();
		if (roundedValue != null) {
			if (rule.getRule(value).delta != null) {
				return roundedValue + (rule.getRule(value).delta * 0.4);
			}

		}
		return null;
	}

	/**
	 * <p>getMinRoundedValue.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getMinRoundedValue() {
		Double roundedValue = getRoundedValue();
		
		if (roundedValue != null) {
			if (rule.getRule(value).delta != null) {
				return roundedValue - (rule.getRule(value).delta * 0.5);
			}

		}
		return null;
	}

	/**
	 * <p>getMaxToleratedValue.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getMaxToleratedValue() {
		Double ret = null;
		if (toleranceMax != null) {
			ret = getMaxRoundedValue();
			if (ret != null) {
				if (Boolean.TRUE.equals(isPerc)) {
					ret = BigDecimal.valueOf(ret).add(applyPerc(ret, toleranceMax)).doubleValue();
				} else {
					ret = BigDecimal.valueOf(ret).add(BigDecimal.valueOf(toleranceMax)).doubleValue();
				}

				ret = rule.getRule(ret).round(ret);
			}

		}

		return ret;
	}

	/**
	 * <p>getMinToleratedValue.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getMinToleratedValue() {
		Double ret = null;
		if (toleranceMin != null) {
			ret = getMinRoundedValue();
			if (ret != null) {
				if (Boolean.TRUE.equals(isPerc)) {
					ret = BigDecimal.valueOf(ret).subtract(applyPerc(ret, toleranceMin)).doubleValue();
				} else {
					ret = BigDecimal.valueOf(ret).subtract(BigDecimal.valueOf(toleranceMin)).doubleValue();
				}
				ret = rule.getRule(ret).round(ret);
			}

		}

		return ret;
	}


	private BigDecimal applyPerc(Double value, Double perc) {
		return BigDecimal.valueOf(value).multiply(BigDecimal.valueOf(perc)).divide(BigDecimal.valueOf(100d));
	}

}
