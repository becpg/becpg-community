package fr.becpg.repo.product.formulation.rounding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.productList.NutListDataItem;

public class NutrientRoundingRules {
	protected static final Log logger = LogFactory.getLog(NutrientRoundingRules.class);

	public enum NutrientRoundingRuleType {
		NRJ, Fat, SatFat, Su, Fiber, P, Na, Salt, Cholesterol, K;
	};

	private interface RoundingRule {
		Double round(Double value, Locale locale);
	}

	public static List<Locale> getAvailableLocales() {
		List<Locale> ret = new ArrayList<>();
		ret.add(Locale.FRENCH);

		if (MLTextHelper.getSupportedLocales().contains(Locale.US)) {
			ret.add(Locale.US);
		}

		return ret;
	}

	static Map<NutrientRoundingRuleType, RoundingRule> rules = new LinkedHashMap<>();

	static {
		rules.put(NutrientRoundingRuleType.NRJ, (value, locale) -> {
			if (Locale.US.equals(locale)) {
				return nearByValueNRJUS(value);
			}
			return (double) Math.round(value);
		});

		rules.put(NutrientRoundingRuleType.Fat, (value, locale) -> {

			if (Locale.US.equals(locale)) {

				return nearByValueUS(value);
			}
			return nearByValueEur(value, 0.5);
		});

		rules.put(NutrientRoundingRuleType.SatFat, (value, locale) -> {
			if (Locale.US.equals(locale)) {
				return nearByValueUS(value);
			}
			return nearByValueEur(value, 0.1);
		});

		rules.put(NutrientRoundingRuleType.Su, (value, locale) -> {
			if (Locale.US.equals(locale)) {
				return nearByValueSuFiberPUS(value);
			}
			return nearByValueEur(value, 0.5);
		});

		rules.put(NutrientRoundingRuleType.Fiber, (value, locale) -> {
			if (Locale.US.equals(locale)) {
				return nearByValueSuFiberPUS(value);
			}
			return nearByValueEur(value, 0.5);
		});

		rules.put(NutrientRoundingRuleType.P, (value, locale) -> {
			if (Locale.US.equals(locale)) {
				return nearByValueSuFiberPUS(value);
			}
			return nearByValueEur(value, 0.5);
		});
		rules.put(NutrientRoundingRuleType.Na, (value, locale) -> {
			if (Locale.US.equals(locale)) {
				return nearByValueNaUS(value);
			}
			return nearByValueNaSaltEur(value, 0.005);
		});

		rules.put(NutrientRoundingRuleType.Salt, (value, locale) -> {
			if (Locale.US.equals(locale)) {
				return value;
			}
			return nearByValueNaSaltEur(value, 0.0125);
		});
		rules.put(NutrientRoundingRuleType.Cholesterol, (value, locale) -> {
			if (Locale.US.equals(locale)) {
				return nearByValuecholesterolUS(value);
			}
			return value;
		});

		rules.put(NutrientRoundingRuleType.K, (value, locale) -> {
			if (Locale.US.equals(locale)) {
				return nearByValueNaUS(value);
			}
			return value;
		});
	}

	public static Number extractValue(String roundedValue, Locale locale) {

		return extractValueByKey(roundedValue, "value", locale);
	}

	private static Number extractValueByKey(String roundedValue, String item, Locale locale) {
		String key = getLocalKey(locale);
		JSONObject jsonRound;
		try {
			jsonRound = new JSONObject(roundedValue);

			if (jsonRound.has(item)) {
				JSONObject value = (JSONObject) jsonRound.get(item);
				if (value.has(key)) {
					return (Number) value.get(key);
				}
			}

		} catch (JSONException e) {
			logger.error(e, e);
		}

		return null;
	}

	public static Number extractValuePerServing(String roundedValue, Locale locale) {
		return extractValueByKey(roundedValue, "valuePerServing", locale);
	}

	public static Number extractMini(String roundedValue, Locale locale) {
		return extractValueByKey(roundedValue, "mini", locale);
	}

	public static Number extractMaxi(String roundedValue, Locale locale) {
		return extractValueByKey(roundedValue, "maxi", locale);
	}

	private static String getLocalKey(Locale locale) {
		String key = MLTextHelper.localeKey(locale);
		if (!Locale.FRENCH.equals(locale)) {
			key = "default";
		}

		return key;
	}

	public static void extractXMLAttribute(Element nutListElt, String roundedValue, Locale locale) {
		if(roundedValue!=null) {
			String localKey = getLocalKey(locale);
			try {
				JSONObject jsonRound = new JSONObject(roundedValue);
				for (Iterator<?> i = jsonRound.keys(); i.hasNext();) {
					String valueKey = (String) i.next();
					JSONObject value = (JSONObject) jsonRound.get(valueKey);
					if (value.has(localKey)) {
						nutListElt.addAttribute("rounded"+StringUtils.capitalize(valueKey), "" + value.get(localKey));
					}
				}
			} catch (JSONException e) {
				logger.error(e, e);
			}	
		}
	}

	// {
	// value : {
	// default: 5.0,
	// US : 6.0
	// },
	// mini : {
	// default: 5.0,
	// US : 6.0
	// }
	// maxi : {
	// default: 5.0,
	// US : 6.0
	// }
	// }
	//
	public static String extractRoundedValue(NutListDataItem n, String roundingMode) {

		JSONObject jsonRound = new JSONObject();

		try {

			JSONObject value = new JSONObject();
			JSONObject mini = new JSONObject();
			JSONObject maxi = new JSONObject();
			JSONObject valuePerServing = new JSONObject();

			for (Locale locale : getAvailableLocales()) {

				String key = getLocalKey(locale);
				String nutUnit = n.getUnit();

				value.put(key, round(n.getValue(), roundingMode, locale, nutUnit));
				mini.put(key, round(n.getMini(), roundingMode, locale, nutUnit));
				maxi.put(key, round(n.getMaxi(), roundingMode, locale, nutUnit));
				valuePerServing.put(key, round(n.getValuePerServing(), roundingMode, locale, nutUnit));

				jsonRound.put("value", value);
				jsonRound.put("mini", mini);
				jsonRound.put("maxi", maxi);
				jsonRound.put("valuePerServing", valuePerServing);

			}

		} catch (JSONException e) {
			logger.error(e, e);
		}
		return jsonRound.toString();
	}

	// RoundingRole method for Fat according to european guide in Kcal
	private static Double nearByValueNRJUS(Double value) {

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

	// RoundingRole method for Fat according to european guide in g
	private static Double nearByValueEur(Double value, Double minValue) {
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

	// RoundingRole method for Fat according to US guide in g

	private static Double nearByValueUS(Double value) {

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
	private static Double nearByValueSuFiberPUS(Double value) {

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

	private static Double nearByValueNaUS(Double value) {
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

	// RoundingRole method for sodium according to european guide (unit:g)
	private static Double nearByValueNaSaltEur(Double value, Double minValue) {
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

	// RoundingRole method for cholesterol according to US guide (unit:g)

	private static Double nearByValuecholesterolUS(Double value) {
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

	public static Double round(Double value, String roundingRuleType, Locale locale, String nutUnit) {

		if (value == null) {
			return null;
		}
		RoundingRule roundingRule = roundingRuleType != null ? rules.get(NutrientRoundingRuleType.valueOf(roundingRuleType)) : null;

		if (roundingRule != null) {
			if ((nutUnit != null) && nutUnit.equals("mg/100g")) { // convert mg
																	// to g
				value = value / 1000;
				value = roundingRule.round(value, locale);
				return value * 1000;
			}
			if ((nutUnit != null) && nutUnit.equals("KJ/100g")) { // convert KJ
																	// to Kcal
				value = value * 4.18;
				value = roundingRule.round(value, locale);
				return value / 4.18;
			}
			return roundingRule.round(value, locale);
		}
		return value;
	}

}
