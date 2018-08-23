package fr.becpg.repo.product.formulation.rounding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.productList.NutListDataItem;

/**
 * 
 * @author matthieu
 *
 */
public class NutrientRoundingRules {
	protected static final Log logger = LogFactory.getLog(NutrientRoundingRules.class);

	public static List<Locale> getAvailableLocales() {
		List<Locale> ret = new ArrayList<>();
		ret.add(Locale.FRENCH);

		if (MLTextHelper.getSupportedLocales().contains(Locale.US)) {
			ret.add(Locale.US);
		}

		return ret;
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
		if (Locale.FRENCH.equals(locale) || Locale.FRANCE.equals(locale)) {
			key = "default";
		}

		return key;
	}

	public static void extractXMLAttribute(Element nutListElt, String roundedValue, Locale locale) {
		if (roundedValue != null) {
			String localKey = getLocalKey(locale);
			try {
				JSONObject jsonRound = new JSONObject(roundedValue);
				for (Iterator<?> i = jsonRound.keys(); i.hasNext();) {
					String valueKey = (String) i.next();
					JSONObject value = (JSONObject) jsonRound.get(valueKey);
					if (value.has(localKey)) {
						nutListElt.addAttribute("rounded" + StringUtils.capitalize(valueKey), "" + value.get(localKey));
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
	public static String extractRoundedValue(NutListDataItem n, String nutrientTypeCode) {

		JSONObject jsonRound = new JSONObject();

		try {

			JSONObject value = new JSONObject();
			JSONObject mini = new JSONObject();
			JSONObject maxi = new JSONObject();
			JSONObject valuePerServing = new JSONObject();

			for (Locale locale : getAvailableLocales()) {

				String key = getLocalKey(locale);
				String nutUnit = n.getUnit();

				value.put(key, round(n.getValue(), nutrientTypeCode, locale, nutUnit));
				mini.put(key, round(n.getMini(), nutrientTypeCode, locale, nutUnit));
				maxi.put(key, round(n.getMaxi(), nutrientTypeCode, locale, nutUnit));
				valuePerServing.put(key, round(n.getValuePerServing(), nutrientTypeCode, locale, nutUnit));

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

	public static Double round(Double value, String nutrientTypeCode, Locale locale, String nutUnit) {

		if (value == null) {
			return null;
		}

		AbstractNutrientRegulation regulation;

		if (Locale.US.equals(locale)) {
			regulation = new UsNutrientRegulation();
		} else {
			regulation = new EuropeanNutrientRegulation();
		}

		return regulation.round(value, nutrientTypeCode, nutUnit);

	}

}
