package fr.becpg.repo.product.formulation.nutrient;

/**
 *
 * @author rim
 *
 */
public class UsNutrientRegulation extends AbstractNutrientRegulation {

	public UsNutrientRegulation(String path) {
		super(path);
	}

	@Override
	protected Double roundByCode(Double value, String nutrientTypeCode) {

		if (nutrientTypeCode.startsWith(NutrientCode.ENER.toString()) || nutrientTypeCode.equals(NutrientCode.ENERSF.toString())
				|| nutrientTypeCode.equals(NutrientCode.ENERPF.toString())) {
			return nearByValueNRJUS(value);
		} else if (nutrientTypeCode.equals(NutrientCode.FAPUCIS.toString()) || nutrientTypeCode.equals(NutrientCode.FAMSCIS.toString())
				|| nutrientTypeCode.equals(NutrientCode.FAT.toString()) || nutrientTypeCode.equals(NutrientCode.FASAT.toString())) {
			return nearByValueUS(value);
		} else if (nutrientTypeCode.equals(NutrientCode.NA.toString()) || nutrientTypeCode.equals(NutrientCode.K.toString())) {
			return nearByValueNaUS(value);
		} else if (nutrientTypeCode.equals(NutrientCode.CHO.toString()) || nutrientTypeCode.equals(NutrientCode.FIBTG.toString())
				|| nutrientTypeCode.startsWith(NutrientCode.PRO.toString())) {
			return nearByValueSuFiberPUS(value);
		} else if (nutrientTypeCode.equals(NutrientCode.FIBSOL.toString()) || nutrientTypeCode.equals(NutrientCode.FIBINS.toString())
				|| nutrientTypeCode.equals(NutrientCode.POLYL.toString()) || nutrientTypeCode.equals(NutrientCode.SUGAR.toString())) {
			return nearByValueNaUS(value);
		} else if (nutrientTypeCode.startsWith(NutrientCode.CHOL.toString())) {
			return nearByValuecholesterolUS(value);
		}
		return nearByDefault(value);
	}

	// RoundingRole method for Fat according to european guide in Kcal = cal
	private Double nearByValueNRJUS(Double value) {

		if (value == null) {
			return null;
		} else if (value > 50) {
			return (double) (10 * (int) (Math.ceil(value / 10)));
		} else if ((value >= 5) && (value <= 50)) {
			return (double) (5 * (int) (Math.ceil(value / 5)));
		} else if (value < 5) {
			return 0.0;
		}

		return value;
	}

	// RoundingRole method for Fat according to US guide in g
	private Double nearByValueUS(Double value) {

		if (value == null) {
			return null;
		} else if (value >= 5) {
			return (double) Math.ceil(value);
		} else if ((value >= 0.5) && (value < 5)) {
			return (double) (0.5 * (int) (Math.ceil(value / 0.5)));
		} else if (value < 0.5) {

			return 0.0;
		}

		return value;
	}

	// RoundingRole method for Sugars/Soluble & Insoluble fiber/Protein
	// According to the US guide
	private Double nearByValueSuFiberPUS(Double value) {

		if (value == null) {
			return null;
		} else if (value <= 0.5) {
			return 0.0;
		} else if (value >= 1) {
			return (double) Math.ceil(value);
		} else if ((value > 0.5) && (value < 1)) {
			return 1.0;
		}
		return value;

	}

	// RoundingRole method for sodium according to US guide (Unit:g)

	private Double nearByValueNaUS(Double value) {
		if (value == null) {
			return null;
		} else if (value < 0.005) {
			return 0.0;
		} else if ((value >= 0.005) && (value < 0.14)) {
			return (double) (0.005 * (int) (Math.ceil(value / 0.005)));
		} else if (value > 0.14) {
			return (double) (0.01 * (int) (Math.ceil(value / 0.01)));
		}
		return value;

	}

	// RoundingRole method for cholesterol according to US guide (unit:g)

	private Double nearByValuecholesterolUS(Double value) {
		if (value == null) {
			return null;
		} else if (value > 0.005) {

			return (double) (0.005 * (int) (Math.ceil(value / 0.005)));
		}

		else if (value <= 0.002) {
			return 0.0;
		} else if ((value > 0.002) && (value < 0.005)) {
			return 0.005;
		}
		return value;
	}

}
