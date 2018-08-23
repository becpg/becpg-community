package fr.becpg.repo.product.formulation.rounding;

/**
 * 
 * @author rim
 *
 */
public class EuropeanNutrientRegulation extends AbstractNutrientRegulation {


	@Override
	protected Double roundByCode(Double value, String nutrientTypeCode) {
		if(logger.isDebugEnabled()) {
			logger.debug("Round EUR value: "+value +" code : "+ nutrientTypeCode);
		}
		if(nutrientTypeCode.startsWith(NutrientTypeCode.ENER.toString())){
			return (double) Math.round(value);
		}
		else if (nutrientTypeCode.equals(NutrientTypeCode.FAT.toString()) || nutrientTypeCode.equals(NutrientTypeCode.CHOAVL.toString())
				|| nutrientTypeCode.equals(NutrientTypeCode.SUGAR.toString()) || nutrientTypeCode.equals(NutrientTypeCode.FIBTG.toString())
				|| nutrientTypeCode.startsWith(NutrientTypeCode.PRO.toString())){
			return nearByValueEur(value, 0.5);
		}
		else if (nutrientTypeCode.equals(NutrientTypeCode.FASAT.toString())){
			return nearByValueEur(value, 0.1);
		}
		else if (nutrientTypeCode.equals(NutrientTypeCode.NA.toString())){
			return nearByValueNaSaltEur(value, 0.005);
		}
		else if (nutrientTypeCode.equals(NutrientTypeCode.NACL.toString())){
			return nearByValueNaSaltEur(value, 0.0125);
		}

		return null;
	}

	// RoundingRole method for Fat according to european guide in g
	private Double nearByValueEur(Double value, Double minValue) {
		if (value == null) {
			return null;
		} else if (value <= minValue) {
			return 0.0;
		} else if ((value > minValue) && (value < 10)) {
			return (double) Math.round(10 * value) / 10;
		} else if (value >= 10) {
			return (double) Math.round(value);
		}
		return null;
	}

	// RoundingRole method for sodium according to european guide (unit:g)
	private Double nearByValueNaSaltEur(Double value, Double minValue) {
		if (value == null) {
			return null;
		} else if ((value > minValue) && (value < 1)) {

			return (double) Math.round(100 * value) / 100;
		} else if (value >= 1) {

			return (double) Math.round(10 * value) / 10;
		} else if (value <= minValue) {
			return 0.0;
		}
		return null;
	}

}
