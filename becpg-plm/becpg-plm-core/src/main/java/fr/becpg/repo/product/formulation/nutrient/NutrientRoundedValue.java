package fr.becpg.repo.product.formulation.nutrient;

import java.math.BigDecimal;

/**
 *
 * @author matthieu
 *
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

	public NutrientRoundedValue(String nutrientTypeCode, Double value) {
		super();
		this.nutrientTypeCode = nutrientTypeCode;
		this.value = value;
	}

	public void setTolerances(Double tolerance, boolean isPerc) {
		this.toleranceMax = tolerance;
		this.toleranceMin = tolerance;
		this.isPerc = isPerc;
	}

	public void setTolerances(Double toleranceMax, Double toleranceMin, boolean isPerc) {
		this.toleranceMax = toleranceMax;
		this.toleranceMin = toleranceMin;
		this.isPerc = isPerc;
	}

	public void setRule(NutrientRoundedRuleFunction rule) {
		this.rule = rule;
	}

	public String getNutrientTypeCode() {
		return nutrientTypeCode;
	}

	public Double getValue() {
		return value;
	}

	public Double getRoundedValue() {
		return rule.getRule(value).round(value);
	}

	public Double getToleranceMax() {
		return toleranceMax;
	}

	public Double getToleranceMin() {
		return toleranceMin;
	}

	public Double getMaxRoundedValue() {
		Double roundedValue = getRoundedValue();
		if (roundedValue != null) {
			if (rule.getRule(value).delta != null) {
				return roundedValue + (rule.getRule(value).delta * 0.4);
			}

		}
		return null;
	}

	public Double getMinRoundedValue() {
		Double roundedValue = getRoundedValue();
		
		if (roundedValue != null) {
			if (rule.getRule(value).delta != null) {
				return roundedValue - (rule.getRule(value).delta * 0.5);
			}

		}
		return null;
	}

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
