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
				BigDecimal deltaBd = BigDecimal.valueOf(delta);
				BigDecimal valBd = BigDecimal.valueOf(val);
				
				BigDecimal divided = valBd.divide(deltaBd, 15, RoundingMode.HALF_EVEN);
				BigDecimal rounded = new BigDecimal(Math.round(divided.doubleValue()));
				BigDecimal multiplied = deltaBd.multiply(rounded);
				
				// sometime roundedValue is 0.3000000001 when 0.1 * 3 --> we set scale
				return multiplied.setScale(3, RoundingMode.HALF_EVEN).doubleValue();
			}

			return BigDecimal.valueOf(val).round(new MathContext(precision, roundingMode)).doubleValue();

		}
		return null;

	}

}

