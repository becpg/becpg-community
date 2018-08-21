package fr.becpg.repo.product.formulation.rounding;


public class UsNutrientRegulation extends AbstractNutrientRegulation {

	public UsNutrientRegulation() {

	}
	
	protected Double roundByCode(Double value, String nutrientTypeCode) {
		
		if(nutrientTypeCode.startsWith(NutrientTypeCode.ENER.toString()) || nutrientTypeCode.equals(NutrientTypeCode.ENERSF.toString()) 
				|| nutrientTypeCode.equals(NutrientTypeCode.ENERPF.toString()) ){
			return nearByValueNRJUS(value);
		}
		else if (nutrientTypeCode.equals(NutrientTypeCode.FAPUCIS.toString()) || nutrientTypeCode.equals(NutrientTypeCode.FAMSCIS.toString())
				|| nutrientTypeCode.equals(NutrientTypeCode.FAT.toString()) || nutrientTypeCode.equals(NutrientTypeCode.FASAT.toString())){
			return nearByValueUS(value);
		}
		else if (nutrientTypeCode.equals(NutrientTypeCode.NA.toString()) || nutrientTypeCode.equals(NutrientTypeCode.K.toString())){
			return nearByValueNaUS(value);
		}
		else if (nutrientTypeCode.equals(NutrientTypeCode.CHO.toString()) || nutrientTypeCode.equals(NutrientTypeCode.FIBTG.toString())
				|| nutrientTypeCode.startsWith(NutrientTypeCode.PRO.toString())){
			return nearByValueSuFiberPUS(value);
		}
		else if (nutrientTypeCode.equals(NutrientTypeCode.FIBSOL.toString()) || nutrientTypeCode.equals(NutrientTypeCode.FIBINS.toString())
				|| nutrientTypeCode.equals(NutrientTypeCode.POLYL.toString()) || nutrientTypeCode.equals(NutrientTypeCode.SUGAR.toString())
				){
			return nearByValueNaUS(value);
		} else if (nutrientTypeCode.startsWith(NutrientTypeCode.CHOL.toString())){
		    return nearByValuecholesterolUS(value);
		}
		return null;
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

		return null;
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

		return null;
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
		} else {

			return null;
		}
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
		} else {
			return null;
		}
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
		return null;
	}

}
