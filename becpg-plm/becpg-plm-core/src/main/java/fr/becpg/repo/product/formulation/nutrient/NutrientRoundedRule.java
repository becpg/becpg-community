package fr.becpg.repo.product.formulation.nutrient;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * <p>NutrientRoundedRule class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class NutrientRoundedRule {

	int precision = 3;
	RoundingMode roundingMode = RoundingMode.HALF_EVEN;
	Double delta = null;
	Boolean isZero;

	NutrientRoundedRule(Boolean isZero) {
		this.isZero = isZero;
	}

	 NutrientRoundedRule(int precision, RoundingMode roundingMode) {
		this.precision = precision;
		this.roundingMode = roundingMode;
	}

	NutrientRoundedRule(Double delta) {
		this.delta = delta;
	}


	/**
	 * <p>round.</p>
	 *
	 * @param val a {@link java.lang.Double} object
	 * @return a {@link java.lang.Double} object
	 */
	public Double round(Double val) {

		if (val != null) {

			if (Boolean.TRUE.equals(isZero)) {
				return 0d;
			}
			if (delta != null) {
				// round by delta (eg: 0.5)
				double roundedValue = (delta * Math.round(val / delta));
				// sometime roundedValue is 0.3000000001 when 0.1 * 3 --> we round twice
				return (Math.round(roundedValue * 1000d) / 1000d);
			}

			return BigDecimal.valueOf(val).round(new MathContext(precision, roundingMode)).doubleValue();

		}
		return null;

	}

}

