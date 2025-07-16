package fr.becpg.repo.product.formulation.nutrient;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * <p>KoreanNutrientRegulation class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class KoreanNutrientRegulation extends AbstractNutrientRegulation {

	/**
	 * <p>Constructor for KoreanNutrientRegulation.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 */
	public KoreanNutrientRegulation(String path)  {
		super(path);
	}

	/** {@inheritDoc} */
	@Override
	protected Double roundByCode(Double value, String nutrientTypeCode) {
		if (value == null) {
			return null;
		}
		
		if(nutrientTypeCode != null){
			if (nutrientTypeCode.equals(NutrientCode.Energykcal)){
				if (value < 5) {
					return 0.0;
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Fat) || nutrientTypeCode.equals(NutrientCode.FatSaturated)) {
				if (value < 0.5) {
					return 0.0;
				} else if (value < 5) {
					return roundValue(value,0.1d);
				} else {
					return roundValue(value,1d);
				}
			} else if (nutrientTypeCode.equals(NutrientCode.FatTrans)) {
				if (value < 0.2) {
					return 0.0;
				} else if (value >= 0.5) {
					return roundValue(value,1d);
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Cholesterol)) {
				if (value < 2) {
					return 0.0;
				} else if (value >= 5) {
					return roundValue(value,1d);
				}
			} else if (nutrientTypeCode.contentEquals(NutrientCode.Sodium)) {
				if (value < 5) {
					return 0.0;
				} else if (value <= 120){
					return roundValue(value,5d);
				} else {
					return roundValue(value,10d);
				}
			} else if (nutrientTypeCode.contentEquals(NutrientCode.CarbohydrateWithFiber) 
					|| nutrientTypeCode.contentEquals(NutrientCode.Sugar)) {
				if (value < 0.5) {
					return 0.0;
				} else if (value >= 1) {
					return roundValue(value,1d);
				}
			} else if (nutrientTypeCode.contentEquals(NutrientCode.Protein)) {
				if (value < 0.5) {
					return 0.0;
				} else if (value >= 1) {
					return roundValue(value,1d);
				}
			}
		}
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.round(new MathContext(3,RoundingMode.HALF_EVEN));
		return bd.doubleValue();
	}

	/** {@inheritDoc} */
	@Override
	protected String displayValueByCode(Double value, Double roundedValue, String nutrientTypeCode, String measurementPrecision, Locale locale) {

		if(value != null && roundedValue != null && nutrientTypeCode != null){
			if (nutrientTypeCode.equals(NutrientCode.FatTrans) && value > 0.2 && value < 0.5) {
				return "less than 0.5g";
			} else if (nutrientTypeCode.equals(NutrientCode.Cholesterol) && value > 2 && value < 5) {
				return "less than 5mg";
			} else if ((nutrientTypeCode.equals(NutrientCode.Protein)
					|| nutrientTypeCode.equals(NutrientCode.CarbohydrateWithFiber)
					|| nutrientTypeCode.equals(NutrientCode.Sugar))
					&& value > 0.5 && value < 1){
				return "less than 1g";
			}
		}
		return formatDouble(roundedValue, locale);
	}

	/** {@inheritDoc} */
	@Override
	public Double roundGDA(Double value, String nutrientTypeCode) {
		if(value != null){
			if (isVitamin(nutrientTypeCode) || isMineral(nutrientTypeCode)){
				if (value < 2) {
					return 0.0;
				}
			}
		}
		return roundValue(value, 1d);
	}

}
