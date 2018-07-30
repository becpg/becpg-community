package fr.becpg.repo.product.formulation.rounding;

public class UsNutrientRegulation extends AbstractNutrientRegulation {

	public UsNutrientRegulation() {

		rules.put(NutrientTypeCode.NRJ, (value) -> {
			return nearByValueNRJUS(value);
		});

		rules.put(NutrientTypeCode.Fat, (value) -> {
			return nearByValueUS(value);
		});

		rules.put(NutrientTypeCode.SatFat, (value) -> {
			return nearByValueUS(value);
		});

		rules.put(NutrientTypeCode.Su, (value) -> {
			return nearByValueSuFiberPUS(value);

		});

		rules.put(NutrientTypeCode.Fiber, (value) -> {
			return nearByValueSuFiberPUS(value);
		});

		rules.put(NutrientTypeCode.P, (value) -> {
			return nearByValueSuFiberPUS(value);
		});
		rules.put(NutrientTypeCode.Na, (value) -> {
			return nearByValueNaUS(value);
		});

		rules.put(NutrientTypeCode.Salt, (value) -> {
			return value;
		});
		
		rules.put(NutrientTypeCode.Cholesterol, (value) -> {
			return nearByValuecholesterolUS(value);
		});

		rules.put(NutrientTypeCode.K, (value) -> {
			return nearByValueNaUS(value);
		});

	}

	// RoundingRole method for Fat according to european guide in Kcal
	private Double nearByValueNRJUS(Double value) {

		if (value == null) {
			return null;
		} else if (value > 0.05) {
			return (double) (0.01 * (int) (Math.ceil(value / 0.01)));
		} else if ((value >= 0.005) && (value <= 0.05)) {
			return (double) (0.005 * (int) (Math.ceil(value / 0.005)));
		} else if (value < 0.005) {
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
