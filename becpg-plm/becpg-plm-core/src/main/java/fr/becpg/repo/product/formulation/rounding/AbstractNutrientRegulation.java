package fr.becpg.repo.product.formulation.rounding;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AbstractNutrientRegulation {

	private static Log logger = LogFactory.getLog(AbstractNutrientRegulation.class);


	protected interface RoundingRule {
		Double round(Double value);
	}

	protected Map<NutrientTypeCode, RoundingRule> rules = new LinkedHashMap<>();

	public Double round(Double value, String nutrientTypeCode, String nutUnit) {
		if ((nutrientTypeCode != null) && !nutrientTypeCode.isEmpty()) {
			try {
				RoundingRule roundingRule = nutrientTypeCode != null ? rules.get(NutrientTypeCode.valueOf(nutrientTypeCode)) : null;

				if (roundingRule != null) {
					if ((nutUnit != null) && nutUnit.equals("mg/100g")) { // convert
																		// mg
																			// to
																			// g
						value = value / 1000;
						value = roundingRule.round(value);
						return value * 1000;
					}
					if ((nutUnit != null) && nutUnit.equals("KJ/100g")) { // convert
																			// KJ
																			// to
																			// Kcal
						value = value / 4.184;
						value = roundingRule.round(value);
						return (double) Math.round(value * 4.184 * 100) / 100;
					}
					return roundingRule.round(value);
				}
			} catch (IllegalStateException e) {
				logger.debug(e, e);
			}
		}
		return (double) Math.round(value);
	}

}
