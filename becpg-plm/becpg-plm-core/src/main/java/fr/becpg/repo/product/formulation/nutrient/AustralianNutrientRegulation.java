package fr.becpg.repo.product.formulation.nutrient;

/**
 *
 * TODO
 *
 */
public class AustralianNutrientRegulation extends AbstractNutrientRegulation {

	public AustralianNutrientRegulation(String path)  {
		super(path);
	}

	@Override
	protected Double roundByCode(Double value, String nutrientTypeCode) {

		if (nutrientTypeCode.startsWith(NutrientCode.ENER.toString())) {
			return (double) Math.round(value);
		} else if (nutrientTypeCode.equals(NutrientCode.FAT.toString()) || nutrientTypeCode.equals(NutrientCode.CHOAVL.toString())
				|| nutrientTypeCode.equals(NutrientCode.SUGAR.toString()) || nutrientTypeCode.equals(NutrientCode.FIBTG.toString())
				|| nutrientTypeCode.startsWith(NutrientCode.PRO.toString())) {
			return nearByValueEur(value, 0.5);
		} else if (nutrientTypeCode.equals(NutrientCode.FASAT.toString())) {
			return nearByValueEur(value, 0.1);
		} else if (nutrientTypeCode.equals(NutrientCode.NA.toString())) {
			return nearByValueNaSaltEur(value, 0.005);
		} else if (nutrientTypeCode.equals(NutrientCode.NACL.toString())) {
			return nearByValueNaSaltEur(value, 0.0125);
		}

		return nearByDefault(value);
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
		return value;
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
		return value;
	}

}
