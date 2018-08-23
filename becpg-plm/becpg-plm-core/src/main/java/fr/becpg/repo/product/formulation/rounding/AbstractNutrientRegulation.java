package fr.becpg.repo.product.formulation.rounding;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractNutrientRegulation {

	protected static Log logger = LogFactory.getLog(AbstractNutrientRegulation.class);

	protected interface RoundingRule {
		Double round(Double value);
	}

	protected Map<NutrientTypeCode, RoundingRule> rules = new LinkedHashMap<>();

	public Double round(Double value, String nutrientTypeCode, String nutUnit) {

		if (value == null) {
			return null;
		}
		if ((nutrientTypeCode != null) && !nutrientTypeCode.isEmpty()) {

			if ((nutUnit != null) && nutUnit.equals("mg/100g")) { // convert mg
																	// to g
				value = value / 1000;
				value = roundByCode(value, nutrientTypeCode);
				if (value != null) {
					value = value * 1000;
				}
				return value;
			}
			if ((nutUnit != null) && nutUnit.equals("KJ/100g")) { // convert KJ
																	// to Kcal
				value = value / 4.184;
				value = roundByCode(value, nutrientTypeCode);
				return (double) Math.round((value * 4.184 * 100) / 100);
			}
			return roundByCode(value, nutrientTypeCode);
		}
		return (double) Math.round(value);
	}

	protected abstract Double roundByCode(Double value, String nutrientTypeCode);

}
