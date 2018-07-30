package fr.becpg.repo.product.formulation.rounding;

public class EuropeanNutrientRegulation extends AbstractNutrientRegulation {

	public EuropeanNutrientRegulation() {

		rules.put(NutrientTypeCode.NRJ, (value) -> {

			return (double) Math.round(value);
		});

		rules.put(NutrientTypeCode.Fat, (value) -> {

			return nearByValueEur(value, 0.5);
		});

		rules.put(NutrientTypeCode.SatFat, (value) -> {

			return nearByValueEur(value, 0.1);
		});

		rules.put(NutrientTypeCode.Su, (value) -> {

			return nearByValueEur(value, 0.5);
		});

		rules.put(NutrientTypeCode.Fiber, (value) -> {

			return nearByValueEur(value, 0.5);
		});

		rules.put(NutrientTypeCode.P, (value) -> {

			return nearByValueEur(value, 0.5);
		});
		rules.put(NutrientTypeCode.Na, (value) -> {

			return nearByValueNaSaltEur(value, 0.005);
		});

		rules.put(NutrientTypeCode.Salt, (value) -> {

			return nearByValueNaSaltEur(value, 0.0125);
		});
		rules.put(NutrientTypeCode.Cholesterol, (value) -> {

			return value;
		});

		rules.put(NutrientTypeCode.K, (value) -> {

			return value;
		});

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
