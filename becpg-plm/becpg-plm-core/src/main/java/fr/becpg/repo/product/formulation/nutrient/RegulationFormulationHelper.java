package fr.becpg.repo.product.formulation.nutrient;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.product.formulation.nutrient.AbstractNutrientRegulation.NutrientDefinition;
import fr.becpg.repo.repository.model.VariantAwareDataItem;

/**
 * <p>RegulationFormulationHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class RegulationFormulationHelper {

	private RegulationFormulationHelper() {
		//Do nothing
	}

	/** Constant <code>logger</code> */
	protected static final Log logger = LogFactory.getLog(RegulationFormulationHelper.class);

	/** Constant <code>ATTR_NUT_CODE="nutCode"</code> */
	public static final String ATTR_NUT_CODE = "nutCode";
	private static final String KEY_VALUE = "v";
	private static final String KEY_TOLERANCE_MAX = "tu";
	private static final String KEY_TOLERANCE_MIN = "tl";
	private static final String KEY_MINI = "min";
	private static final String KEY_MAXI = "max";
	private static final String KEY_SECONDARY_VALUE = "v2";
	private static final String KEY_SECONDARY_VALUE_PER_SERVING = "v2ps";
	private static final String KEY_VALUE_PER_SERVING = "vps";
	private static final String KEY_GDA_PERC = "gda";
	private static final String KEY_UL = "ul";
	private static final String KEY_VALUE_PER_CONTAINER = "vpc";
	private static final String KEY_GDA_PERC_PER_CONTAINER = "gdapc";
	private static final String KEY_UNIT = "unit";

	private static Map<String, NutrientRegulation> regulations = new HashMap<>();

	static {
		regulations.put("EU", new EuropeanNutrientRegulation("beCPG/databases/nuts/EuNutrientRegulation.csv"));
		regulations.put("US", new UsNutrientRegulation("beCPG/databases/nuts/UsNutrientRegulation_2016.csv"));
		regulations.put("TT", new UsNutrientRegulation("beCPG/databases/nuts/TrinidadTobagoNutrientRegulation.csv"));
		regulations.put("DO", new UsNutrientRegulation("beCPG/databases/nuts/DominicanRepublicanNutrientRegulation.csv"));
		regulations.put("PE", new UsNutrientRegulation("beCPG/databases/nuts/PeruvianNutrientRegulation.csv"));
		regulations.put("CA", new CanadianNutrientRegulation("beCPG/databases/nuts/CanadianNutrientRegulation_2017.csv"));
		regulations.put("CN", new ChineseNutrientRegulation("beCPG/databases/nuts/ChineseNutrientRegulation.csv"));
		regulations.put("AU", new AustralianNutrientRegulation("beCPG/databases/nuts/AUNutrientRegulation.csv"));
		regulations.put("ID", new IndonesianNutrientRegulation("beCPG/databases/nuts/IndonesianNutrientRegulation.csv"));
		regulations.put("MX", new MexicanNutrientRegulation("beCPG/databases/nuts/MexicanNutrientRegulation.csv"));
		regulations.put("HK", new HongKongNutrientRegulation("beCPG/databases/nuts/HongKongNutrientRegulation.csv"));
		regulations.put("KR", new KoreanNutrientRegulation("beCPG/databases/nuts/KoreanNutrientRegulation.csv"));
		regulations.put("MY", new MalaysianNutrientRegulation("beCPG/databases/nuts/MalaysianNutrientRegulation.csv"));
		regulations.put("TH", new ThailandNutrientRegulation("beCPG/databases/nuts/ThailandNutrientRegulation.csv"));
		regulations.put("IN", new IndianNutrientRegulation("beCPG/databases/nuts/IndianNutrientRegulation.csv"));
		regulations.put("GSO", new GSONutrientRegulation("beCPG/databases/nuts/GSONutrientRegulation.csv"));
		regulations.put("MA", new EuropeanNutrientRegulation("beCPG/databases/nuts/MoroccanNutrientRegulation.csv"));
		regulations.put("DZ", new EuropeanNutrientRegulation("beCPG/databases/nuts/AlgerianNutrientRegulation.csv"));
		regulations.put("IL", new IsraeliNutrientRegulation("beCPG/databases/nuts/IsraeliNutrientRegulation.csv"));
		regulations.put("TR", new EuropeanNutrientRegulation("beCPG/databases/nuts/TurkishNutrientRegulation.csv"));
		regulations.put("SG", new MalaysianNutrientRegulation("beCPG/databases/nuts/SingaporeanNutrientRegulation.csv"));
		regulations.put("CODEX", new EuropeanNutrientRegulation("beCPG/databases/nuts/CODEXNutrientRegulation.csv"));
		regulations.put("CO", new ColombianNutrientRegulation("beCPG/databases/nuts/ColombianNutrientRegulation.csv"));
		regulations.put("RU", new RussianNutrientRegulation("beCPG/databases/nuts/RussianNutrientRegulation.csv"));
		regulations.put("PK", new IndianNutrientRegulation("beCPG/databases/nuts/CODEXNutrientRegulation.csv"));
		regulations.put("ZA", new SouthAfricanNutrientRegulation("beCPG/databases/nuts/SouthAfricanNutrientRegulation.csv"));
		regulations.put("TN", new GSONutrientRegulation("beCPG/databases/nuts/TunisianNutrientRegulation.csv"));
		regulations.put("EG", new GSONutrientRegulation("beCPG/databases/nuts/EgyptianNutrientRegulation.csv"));
		regulations.put("CL", new ChileanNutrientRegulation("beCPG/databases/nuts/ChileanNutrientRegulation.csv"));
		regulations.put("UY", new BrazilianNutrientRegulation("beCPG/databases/nuts/UruguayanNutrientRegulation.csv"));
		regulations.put("BR", new BrazilianNutrientRegulation("beCPG/databases/nuts/BrazilianNutrientRegulation.csv"));
		regulations.put("CTA", new CentralAmericanNutrientRegulation("beCPG/databases/nuts/CentralAmericanNutrientRegulation.csv"));

		regulations.put("TW", new TaiwanNutrientRegulation("beCPG/databases/nuts/TaiwanNutrientRegulation.csv"));
		regulations.put("JP", new JapanNutrientRegulation("beCPG/databases/nuts/JapanNutrientRegulation.csv"));
		regulations.put("VN", new VietnamNutrientRegulation("beCPG/databases/nuts/VietnamNutrientRegulation.csv"));
	}

	/**
	 * <p>extractValuePerServing.</p>
	 *
	 * @param roundedValue a {@link java.lang.String} object.
	 * @param locale a {@link java.util.Locale} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double extractValuePerServing(String roundedValue, Locale locale) {
		return extractValuePerServing(roundedValue, getLocalKey(locale));
	}

	/**
	 * <p>extractMini.</p>
	 *
	 * @param roundedValue a {@link java.lang.String} object.
	 * @param locale a {@link java.util.Locale} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double extractMini(String roundedValue, Locale locale) {
		return extractMini(roundedValue, getLocalKey(locale));
	}

	/**
	 * <p>extractMaxi.</p>
	 *
	 * @param roundedValue a {@link java.lang.String} object.
	 * @param locale a {@link java.util.Locale} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double extractMaxi(String roundedValue, Locale locale) {
		return extractMaxi(roundedValue, getLocalKey(locale));
	}

	/**
	 * <p>extractValue.</p>
	 *
	 * @param roundedValue a {@link java.lang.String} object.
	 * @param locale a {@link java.util.Locale} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double extractValue(String roundedValue, Locale locale) {
		return extractValue(roundedValue, getLocalKey(locale));
	}

	/**
	 * <p>extractValuePerServing.</p>
	 *
	 * @param roundedValue a {@link java.lang.String} object.
	 * @param key a {@link java.lang.String} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double extractValuePerServing(String roundedValue, String key) {
		return extractValueByKey(roundedValue, KEY_VALUE_PER_SERVING, key);
	}

	/**
	 * <p>extractPreparedValuePerServing.</p>
	 *
	 * @param roundedValue a {@link java.lang.String} object
	 * @param key a {@link java.lang.String} object
	 * @return a {@link java.lang.Double} object
	 */
	public static Double extractPreparedValuePerServing(String roundedValue, String key) {
		return extractValueByKey(roundedValue, KEY_SECONDARY_VALUE_PER_SERVING, key);
	}

	/**
	 * <p>extractValuePerContainer.</p>
	 *
	 * @param roundedValue a {@link java.lang.String} object.
	 * @param key a {@link java.lang.String} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double extractValuePerContainer(String roundedValue, String key) {
		return extractValueByKey(roundedValue, KEY_VALUE_PER_CONTAINER, key);
	}

	/**
	 * <p>extractMini.</p>
	 *
	 * @param roundedValue a {@link java.lang.String} object.
	 * @param key a {@link java.lang.String} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double extractMini(String roundedValue, String key) {
		return extractValueByKey(roundedValue, KEY_MINI, key);
	}

	/**
	 * <p>extractMaxi.</p>
	 *
	 * @param roundedValue a {@link java.lang.String} object.
	 * @param key a {@link java.lang.String} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double extractMaxi(String roundedValue, String key) {
		return extractValueByKey(roundedValue, KEY_MAXI, key);
	}

	/**
	 * <p>extractValue.</p>
	 *
	 * @param roundedValue a {@link java.lang.String} object.
	 * @param key a {@link java.lang.String} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double extractValue(String roundedValue, String key) {
		return extractValueByKey(roundedValue, KEY_VALUE, key);
	}

	/**
	 * <p>extractPreparedValue.</p>
	 *
	 * @param roundedValue a {@link java.lang.String} object
	 * @param key a {@link java.lang.String} object
	 * @return a {@link java.lang.Double} object
	 */
	public static Double extractPreparedValue(String roundedValue, String key) {
		return extractValueByKey(roundedValue, KEY_SECONDARY_VALUE, key);
	}

	/**
	 * <p>extractValue.</p>
	 *
	 * @param roundedValue a {@link java.lang.String} object.
	 * @param key a {@link java.lang.String} object.
	 * @return a {@link java.lang.Double} object.
	 * @param variantKey a {@link java.lang.String} object
	 */
	public static Double extractVariantValue(String roundedValue, String variantKey, String key) {
		return extractValueByKey(roundedValue, variantKey, key);
	}

	/**
	 * <p>extractGDAPerc.</p>
	 *
	 * @param roundedValue a {@link java.lang.String} object.
	 * @param key a {@link java.lang.String} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double extractGDAPerc(String roundedValue, String key) {
		return extractValueByKey(roundedValue, KEY_GDA_PERC, key);
	}

	private static Double extractValueByKey(String roundedValue, String item, String key) {
		if (roundedValue != null) {
			try {
				JSONTokener tokener = new JSONTokener(roundedValue);
				JSONObject jsonRound = new JSONObject(tokener);
				if (jsonRound.has(item)) {
					JSONObject value = (JSONObject) jsonRound.get(item);
					if (value.has(key)) {
						return parseDouble(value.get(key));
					}
				}

			} catch (JSONException e) {
				logger.error(e, e);
			}
		}

		return null;
	}

	/**
	 * <p>getLocalKey.</p>
	 *
	 * @param locale a {@link java.util.Locale} object
	 * @return a {@link java.lang.String} object
	 */
	public static String getLocalKey(Locale locale) {
		if (locale.getCountry().equals("US") || locale.getCountry().equals("CA") || locale.getCountry().equals("AU")
				|| locale.getCountry().equals("ID") || locale.getCountry().equals("HK") || locale.getCountry().equals("MY")
				|| locale.getCountry().equals("IL") || locale.getCountry().equals("IN") || locale.getCountry().equals("KR")
				|| locale.getCountry().equals("MA") || locale.getCountry().equals("MX") || locale.getCountry().equals("DZ")
				|| locale.getCountry().equals("TR") || locale.getCountry().equals("SG") || locale.getCountry().equals("TH")
				|| locale.getCountry().equals("PK") || locale.getCountry().equals("ZA") || locale.getCountry().equals("TN")
				|| locale.getCountry().equals("EG") || locale.getCountry().equals("CL") || locale.getCountry().equals("UY")
				|| locale.getCountry().equals("BR") || locale.getCountry().equals("TT") || locale.getCountry().equals("DO")
				|| locale.getCountry().equals("PE") || locale.getCountry().equals("TW") || locale.getCountry().equals("JP")
				|| locale.getCountry().equals("VN")) {
			return locale.getCountry();
		} else if (locale.getLanguage().equals("zh")) {
			return "CN";
		} else if (locale.getLanguage().equals("ru")) {
			return "RU";
		} else if (locale.getCountry().equals("NZ")) {
			return "AU";
		} else if (locale.getCountry().equals("PR")) {
			return "US";
		} else if (locale.getCountry().equals("PY")) {
			return "BR";
		} else if (locale.getCountry().equals("GT") || locale.getCountry().equals("PA") || locale.getCountry().equals("SV")) {
			return "CTA";
		} else if ((locale.getLanguage().equals("ar") 
				&& (locale.getCountry() == null || locale.getCountry().isEmpty())) || locale.getCountry().equals("AE")
				|| locale.getCountry().equals("BH") || locale.getCountry().equals("SA") || locale.getCountry().equals("QA")
				|| locale.getCountry().equals("OM") || locale.getCountry().equals("KW")) {
			return "GSO";
		} else if (locale.getCountry().equals("KE") || locale.getCountry().equals("NG") || locale.getCountry().equals("GH")
				|| locale.getCountry().equals("CI") || locale.getCountry().equals("UG") || locale.getCountry().equals("MZ")
				|| locale.getCountry().equals("MW") || locale.getCountry().equals("TZ") || locale.getCountry().equals("ZM")
				|| locale.getCountry().equals("ZW") || locale.getCountry().equals("KH") || locale.getCountry().equals("MM")
				|| locale.getCountry().equals("JO") || locale.getCountry().equals("IQ") || locale.getCountry().equals("PS")) {
			return "CODEX";
		} else if (locale.getCountry().equals("CO")) {
			return "CO";
		}

		return "EU";
	}

	/**
	 * <p>extractXMLAttribute.</p>
	 *
	 * @param nutListElt a {@link org.dom4j.Element} object.
	 * @param roundedValue a {@link java.lang.String} object.
	 * @param locale a {@link java.util.Locale} object.
	 * @param isDisplayed a boolean.
	 * @param localesToDisplay a {@link java.lang.String} object
	 */
	public static void extractXMLAttribute(Element nutListElt, String roundedValue, Locale locale, boolean isDisplayed, String localesToDisplay) {
		if (roundedValue != null) {
			String localKey = getLocalKey(locale);
			String nutCode = nutListElt.attributeValue(ATTR_NUT_CODE);
			String measurementPrecision = nutListElt.attributeValue(PLMModel.PROP_NUTLIST_MEASUREMENTPRECISION.getLocalName());
			String nutListValue = nutListElt.attributeValue(PLMModel.PROP_NUTLIST_VALUE.getLocalName());
			String nutListValuePerServing = nutListElt.attributeValue(PLMModel.PROP_NUTLIST_VALUE_PER_SERVING.getLocalName());
			String nutListValuePrepared = nutListElt.attributeValue(PLMModel.PROP_NUTLIST_VALUE_PREPARED.getLocalName());

			try {
				JSONTokener tokener = new JSONTokener(roundedValue);
				JSONObject jsonRound = new JSONObject(tokener);

				for (String locKey : getAvailableRegulations()) {

					if (locKey.equals(localKey) || "all".equals(localesToDisplay)
							|| (localesToDisplay != null && localesToDisplay.contains(locKey))) {

						NutrientRegulation regulation = getRegulation(locKey);
						NutrientDefinition def = regulation.getNutrientDefinition(nutCode);

						String suffix = "";
						if (!locKey.equals(localKey)) {
							suffix = "_" + locKey;
						}

						if (def != null) {
							if (def.getSort() != null) {
								nutListElt.addAttribute("regulSort" + suffix, "" + def.getSort());
							}
							if (def.getDepthLevel() != null) {
								nutListElt.addAttribute("regulDepthLevel" + suffix, "" + def.getDepthLevel());
							}

							if (isDisplayed) {
								if (Boolean.TRUE.equals(def.getMandatory())) {
									nutListElt.addAttribute("regulDisplayMode" + suffix, "M");
								} else if (Boolean.TRUE.equals(def.getOptional())) {
									nutListElt.addAttribute("regulDisplayMode" + suffix, "O");
								}
							}

							if (def.getBold() != null) {
								nutListElt.addAttribute("regulBold" + suffix, "" + def.getBold());
							}
							if (def.getGda() != null) {
								nutListElt.addAttribute("regulGDA" + suffix, "" + def.getGda());
							}
							if (def.getUl() != null) {
								nutListElt.addAttribute("regulUL" + suffix, "" + def.getUl());
							}
							if (def.getUnit() != null) {
								nutListElt.addAttribute("regulUnit" + suffix, "" + def.getUnit());
							}
							if (def.getShowGDAPerc() != null) {
								nutListElt.addAttribute("regulShowGDAPerc" + suffix, "" + def.getShowGDAPerc());
							}
						}

						for (Iterator<?> i = jsonRound.keys(); i.hasNext();) {
							String valueKey = (String) i.next();
							JSONObject value = (JSONObject) jsonRound.get(valueKey);
							if (value.has(locKey)) {
								nutListElt.addAttribute("rounded" + keyToXml(valueKey) + suffix, "" + value.get(locKey));
							}
						}

						if ((nutListValue != null) && (!nutListValue.isBlank())) {
							nutListElt.addAttribute("roundedDisplayValue" + suffix,
									RegulationFormulationHelper.displayValue(Double.parseDouble(nutListValue), extractValue(roundedValue, locKey),
											nutCode, measurementPrecision, locale, locKey));
							if (locKey.equals("US")) {
								nutListElt.addAttribute("roundedDisplayValuePerContainer" + suffix,
										RegulationFormulationHelper.displayValue(extractValuePerContainer(roundedValue, locKey),
												extractValuePerContainer(roundedValue, locKey), nutCode, measurementPrecision, locale, locKey));
							}
						}
						if ((nutListValuePerServing != null) && (!nutListValuePerServing.isBlank())) {
							nutListElt.addAttribute("roundedDisplayValuePerServing" + suffix,
									RegulationFormulationHelper.displayValue(Double.parseDouble(nutListValuePerServing),
											extractValuePerServing(roundedValue, locKey), nutCode, measurementPrecision, locale, locKey));
						}

						if ((nutListValuePrepared != null) && (!nutListValuePrepared.isBlank())) {
							nutListElt.addAttribute("roundedDisplayValuePrepared" + suffix,
									RegulationFormulationHelper.displayValue(Double.parseDouble(nutListValuePrepared),
											extractPreparedValue(roundedValue, locKey), nutCode, measurementPrecision, locale, locKey));

							Double nutListValuePreparedPerServing = extractPreparedValuePerServing(roundedValue, locKey);

							if (nutListValuePreparedPerServing != null) {
								nutListElt.addAttribute("roundedDisplayValuePreparedPerServing" + suffix,
										RegulationFormulationHelper.displayValue(nutListValuePreparedPerServing, nutListValuePreparedPerServing,
												nutCode, measurementPrecision, locale, locKey));
							}
						}

					}
				}
			} catch (JSONException e) {
				logger.error(e, e);
			}
		}
	}

	private static Double parseDouble(Object vps) {
		if (vps instanceof Integer) {
			return ((Integer) vps).doubleValue();
		} else if (vps instanceof Long) {
			return ((Long) vps).doubleValue();
		} else if (vps instanceof Number) {
			return ((Number) vps).doubleValue();
		} else if (vps instanceof String) {
			try {
				return Double.valueOf((String) vps);
			} catch (NumberFormatException e) {
				logger.error("Cannot parse number:" + (String) vps);
				return null;
			}
		}
		return (Double) vps;
	}

	private static String keyToXml(String abrv) {
		switch (abrv) {
		case KEY_SECONDARY_VALUE:
			return "SecondaryValue";
		case KEY_SECONDARY_VALUE_PER_SERVING:
			return "SecondaryValuePerServing";
		case KEY_MINI:
			return "Mini";
		case KEY_MAXI:
			return "Maxi";
		case KEY_VALUE_PER_SERVING:
			return "ValuePerServing";
		case KEY_GDA_PERC:
			return "GDAPerc";
		case KEY_VALUE_PER_CONTAINER:
			return "ValuePerContainer";
		case KEY_GDA_PERC_PER_CONTAINER:
			return "GDAPercPerContainer";
		case KEY_UNIT:
			return "Unit";
		case KEY_TOLERANCE_MAX:
			return "ToleranceMax";
		case KEY_TOLERANCE_MIN:
			return "ToleranceMax";
		case KEY_UL:
			return "UpperLimit";
		default:
			break;
		}
		return "Value";
	}

	// {
	// v : {
	// eu: 5.0,
	// US : 6.0
	// },
	// min : {
	// eu: 5.0,
	// US : 6.0
	// }
	// max : {
	// eu: 5.0,
	// US : 6.0
	// }
	// }
	//
	/**
	 * <p>extractRoundedValue.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param nutCode a {@link java.lang.String} object.
	 * @param n a {@link fr.becpg.repo.product.data.productList.NutListDataItem} object.
	 */
	public static void extractRoundedValue(ProductData formulatedProduct, String nutCode, NutListDataItem n) {
		JSONObject jsonRound = new JSONObject();
		JSONObject jsonPreparedRound = new JSONObject();
		try {

			JSONObject value = new JSONObject();
			JSONObject secondaryValue = new JSONObject();
			JSONObject secondaryValuePerServing = new JSONObject();
			JSONObject secondaryTmin = new JSONObject();
			JSONObject secondaryTmax = new JSONObject();
			JSONObject secondaryGda = new JSONObject();
			JSONObject secondaryValuePerContainer = new JSONObject();
			JSONObject secondaryGdaPerContainer = new JSONObject();

			JSONObject tmin = new JSONObject();
			JSONObject tmax = new JSONObject();
			JSONObject mini = new JSONObject();
			JSONObject maxi = new JSONObject();
			JSONObject valuePerServing = new JSONObject();
			JSONObject gda = new JSONObject();
			JSONObject ul = new JSONObject();
			JSONObject valuePerContainer = new JSONObject();
			JSONObject gdaPerContainer = new JSONObject();
			Map<String, JSONObject> variants = new HashMap<>();

			for (String key : getAvailableRegulations()) {

				String nutUnit = n.getUnit();

				NutrientRegulation regulation = getRegulation(key);
				NutrientDefinition def = regulation.getNutrientDefinition(nutCode);
				value.put(key, regulation.round(n.getValue(), nutCode, nutUnit));
				secondaryValue.put(key, regulation.round(n.getPreparedValue(), nutCode, nutUnit));

				mini.put(key, regulation.round(n.getMini(), nutCode, nutUnit));
				maxi.put(key, regulation.round(n.getMaxi(), nutCode, nutUnit));

				Pair<Double, Double> tolerances = regulation.tolerances(n.getValue(), nutCode, nutUnit);
				if (tolerances != null) {
					tmin.put(key, tolerances.getFirst());
					tmax.put(key, tolerances.getSecond());
				}

				tolerances = regulation.tolerances(n.getPreparedValue(), nutCode, nutUnit);
				if (tolerances != null) {
					secondaryTmin.put(key, tolerances.getFirst());
					secondaryTmax.put(key, tolerances.getSecond());
				}

				if (n instanceof VariantAwareDataItem) {
					for (int i = 1; i <= VariantAwareDataItem.VARIANT_COLUMN_SIZE; i++) {
						if (((VariantAwareDataItem) n).getValue(VariantAwareDataItem.VARIANT_COLUMN_NAME + i) != null) {
							JSONObject variant = new JSONObject();
							variant.put(key, regulation.round(((VariantAwareDataItem) n).getValue(VariantAwareDataItem.VARIANT_COLUMN_NAME + i),
									nutCode, nutUnit));
							variants.put(VariantAwareDataItem.VARIANT_COLUMN_NAME + i, variant);
						}
					}
				}

				Double servingSize = getServingSize(key, formulatedProduct);

				if ((n.getValue() != null) && (servingSize != null)) {
					Double valuePerserving = (n.getValue() * (servingSize * 1000d)) / 100;
					if (n.getManualValuePerServing() != null) {
						valuePerserving = n.getManualValuePerServing();
					}

					Double vps = regulation.round(valuePerserving, nutCode, nutUnit);
					valuePerServing.put(key, vps);
					if ((def != null) && (def.getGda() != null) && (def.getGda() != 0)) {
						gda.put(key, regulation.roundGDA((100 * vps) / def.getGda(), nutCode));
						ul.put(key, def.getGda());
					}
				}

				if ((n.getPreparedValue() != null) && (servingSize != null)) {
					Double valuePerserving = (n.getPreparedValue() * (servingSize * 1000d)) / 100;

					Double vps = regulation.round(valuePerserving, nutCode, nutUnit);
					secondaryValuePerServing.put(key, vps);
					if ((def != null) && (def.getGda() != null) && (def.getGda() != 0)) {
						secondaryGda.put(key, regulation.roundGDA((100 * vps) / def.getGda(), nutCode));
					}

				}

				Double containerQty = FormulationHelper.getNetQtyInLorKg(formulatedProduct, 0d);
				if ((key.equals("US"))) {
					if ((n.getValue() != null)) {
						Double vpc = regulation.round(n.getValue() * containerQty * 10, nutCode, nutUnit);
						valuePerContainer.put(key, vpc);
						if ((def != null) && (def.getGda() != null) && (def.getGda() != 0)) {
							gdaPerContainer.put(key, regulation.roundGDA((100 * vpc) / def.getGda(), nutCode));
						}
					}
					if ((n.getPreparedValue() != null)) {
						Double vpc = regulation.round(n.getPreparedValue() * containerQty * 10, nutCode, nutUnit);
						secondaryValuePerContainer.put(key, vpc);
						if ((def != null) && (def.getGda() != null) && (def.getGda() != 0)) {
							secondaryGdaPerContainer.put(key, regulation.roundGDA((100 * vpc) / def.getGda(), nutCode));
						}
					}
				}

				jsonRound.put(KEY_VALUE, value);
				jsonRound.put(KEY_TOLERANCE_MIN, tmin);
				jsonRound.put(KEY_TOLERANCE_MAX, tmax);
				jsonRound.put(KEY_SECONDARY_VALUE, secondaryValue);
				jsonRound.put(KEY_SECONDARY_VALUE_PER_SERVING, secondaryValuePerServing);
				jsonRound.put(KEY_MINI, mini);
				jsonRound.put(KEY_MAXI, maxi);
				jsonRound.put(KEY_VALUE_PER_SERVING, valuePerServing);
				jsonRound.put(KEY_GDA_PERC, gda);
				jsonRound.put(KEY_UL, ul);
				jsonRound.put(KEY_VALUE_PER_CONTAINER, valuePerContainer);
				jsonRound.put(KEY_GDA_PERC_PER_CONTAINER, gdaPerContainer);

				jsonPreparedRound.put(KEY_VALUE, secondaryValue);
				jsonPreparedRound.put(KEY_TOLERANCE_MIN, secondaryTmin);
				jsonPreparedRound.put(KEY_TOLERANCE_MAX, secondaryTmax);
				jsonPreparedRound.put(KEY_VALUE_PER_SERVING, secondaryValuePerServing);
				jsonPreparedRound.put(KEY_GDA_PERC, secondaryGda);
				jsonPreparedRound.put(KEY_VALUE_PER_CONTAINER, secondaryValuePerContainer);
				jsonPreparedRound.put(KEY_GDA_PERC_PER_CONTAINER, secondaryGdaPerContainer);

				for (Map.Entry<String, JSONObject> entry : variants.entrySet()) {
					jsonRound.put(entry.getKey(), entry.getValue());
				}

			}

		} catch (JSONException e) {
			logger.error(e, e);
		}

		if (n.getPreparedValue() != null) {
			n.setRoundedValuePrepared(jsonPreparedRound.toString());
		} else {
			n.setRoundedValuePrepared(null);
		}
		n.setRoundedValue(jsonRound.toString());
	}

	private static Double getServingSize(String key, ProductData formulatedProduct) {
		Double servingSize = formulatedProduct.getServingSize();
		if (formulatedProduct.getServingSizeByCountry() != null) {
			for (Locale locale : formulatedProduct.getServingSizeByCountry().getLocales()) {
				if ((locale != null) && getLocalKey(locale).equals(key)) {
					String txt = formulatedProduct.getServingSizeByCountry().get(locale);
					if ((txt != null) && !txt.isEmpty()) {
						servingSize = parseDouble(txt);
						if (servingSize == null) {
							formulatedProduct.addError(I18NUtil.getMessage("message.formulate.nut.wrongServingSize", txt, key));
						}
						break;
					}
				}
			}
		}

		if ((servingSize != null) && (formulatedProduct.getServingSizeUnit() != null)) {
			return (servingSize / formulatedProduct.getServingSizeUnit().getUnitFactor());
		} else if (servingSize != null) {
			return servingSize / 1000d;
		}
		return null;
	}

	private static List<String> getAvailableRegulations() {

		List<String> ret = new LinkedList<>();
		if (MLTextHelper.isSupportedLocale(Locale.US) || MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("es_PR"))) {
			ret.add("US");
		}
		if (MLTextHelper.isSupportedLocale(Locale.CANADA) || MLTextHelper.isSupportedLocale(Locale.CANADA_FRENCH)) {
			ret.add("CA");
		}
		if (MLTextHelper.isSupportedLocale(Locale.CHINESE) || MLTextHelper.isSupportedLocale(Locale.SIMPLIFIED_CHINESE)
				|| MLTextHelper.isSupportedLocale(Locale.TRADITIONAL_CHINESE)) {
			ret.add("CN");
		}
		if (MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("en_AU")) || MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("en_NZ"))) {
			ret.add("AU");
		}
		if (MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("es_MX"))) {
			ret.add("MX");
		}
		if (MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("in_ID"))) {
			ret.add("ID");
		}
		if (MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("ms_MY"))) {
			ret.add("MY");
		}
		if (MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("en_SG")) || MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("zh_SG"))) {
			ret.add("SG");
		}
		if (MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("ko_KR"))) {
			ret.add("KR");
		}
		if (MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("th_TH"))) {
			ret.add("TH");
		}
		if (MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("zh_HK"))) {
			ret.add("HK");
		}

		if (MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("ja_JP"))) {
			ret.add("JP");
		}

		if (MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("ja_JP"))) {
			ret.add("JP");
		}
		if (MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("zh_TW"))) {
			ret.add("TW");
		}
		if (MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("vi_VN"))) {
			ret.add("VN");
		}
		if (MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("hi_IN")) || MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("en_IN"))) {
			ret.add("IN");
		}
		if (MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("ar_MA"))) {
			ret.add("MA");
		}
		if (MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("ar_DZ"))) {
			ret.add("DZ");
		}
		if (MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("iw_IL"))) {
			ret.add("IL");
		}
		if (MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("tr_TR"))) {
			ret.add("TR");
		}
		if (MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("es_CO"))) {
			ret.add("CO");
		}
		if (MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("en_PK"))) {
			ret.add("PK");
		}
		if (MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("ru"))) {
			ret.add("RU");
		}
		if (MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("ar_EG"))) {
			ret.add("EG");
		}
		if (MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("en_ZA"))) {
			ret.add("ZA");
		}
		if (MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("ar_TN"))) {
			ret.add("TN");
		}
		if (MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("es_CL"))) {
			ret.add("CL");
		}
		if (MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("es_UY"))) {
			ret.add("UY");
		}
		if (MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("pt_BR")) || MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("es_PY"))) {
			ret.add("BR");
		}
		if (MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("en_TT"))) {
			ret.add("TT");
		}
		if (MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("es_DO"))) {
			ret.add("DO");
		}
		if (MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("es_PE"))) {
			ret.add("PE");
		}
		if (MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("es_PA")) || MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("es_SV"))
				|| MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("es_GT"))) {
			ret.add("CTA");
		}
		if (MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("ar_AE")) || MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("ar_BH"))
				|| MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("ar_SA"))
				|| MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("ar_QA"))
				|| MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("ar_OM"))
				|| MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("ar_KW"))) {
			ret.add("GSO");
		}
		if (MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("ee_GH")) || MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("en_GH"))
				|| MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("en_NG"))
				|| MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("ig_NG"))
				|| MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("en_KE"))
				|| MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("pt_MZ"))
				|| MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("sw_TZ"))
				|| MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("fr_CI"))
				|| MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("en_UG"))
				|| MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("en_MW"))
				|| MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("en_ZW"))
				|| MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("bem_ZM"))
				|| MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("km_KH"))
				|| MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("my_MM"))
				|| MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("ar_IQ"))
				|| MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("ar_JO"))
				|| MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("ar_PS"))) {
			ret.add("CODEX");
		}

		ret.add("EU");
		return ret;
	}

	/**
	 * <p>round.</p>
	 *
	 * @param value a {@link java.lang.Double} object.
	 * @param nutCode a {@link java.lang.String} object.
	 * @param locale a {@link java.util.Locale} object.
	 * @param nutUnit a {@link java.lang.String} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double round(Double value, String nutCode, Locale locale, String nutUnit) {
		return round(value, nutCode, getLocalKey(locale), nutUnit);
	}

	private static Double round(Double value, String nutCode, String key, String nutUnit) {
		if (value == null) {
			return null;
		}
		return getRegulation(key).round(value, nutCode, nutUnit);
	}

	/**
	 * <p>tolerances.</p>
	 *
	 * @param value a {@link java.lang.Double} object
	 * @param nutCode a {@link java.lang.String} object
	 * @param locale a {@link java.util.Locale} object
	 * @param nutUnit a {@link java.lang.String} object
	 * @return a {@link org.alfresco.util.Pair} object
	 */
	public static Pair<Double, Double> tolerances(Double value, String nutCode, Locale locale, String nutUnit) {
		return tolerances(value, nutCode, getLocalKey(locale), nutUnit);
	}

	private static Pair<Double, Double> tolerances(Double value, String nutCode, String key, String nutUnit) {
		if (value == null) {
			return null;
		}
		return getRegulation(key).tolerances(value, nutCode, nutUnit);
	}

	/**
	 * <p>displayValue.</p>
	 *
	 * @param value a {@link java.lang.Double} object.
	 * @param roundedValue a {@link java.lang.Double} object.
	 * @param nutCode a {@link java.lang.String} object.
	 * @param locale a {@link java.util.Locale} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String displayValue(Double value, Double roundedValue, String nutCode, Locale locale) {
		if (value == null) {
			return null;
		}
		return getRegulation(getLocalKey(locale)).displayValue(value, roundedValue, nutCode, null, locale);
	}

	/**
	 * <p>displayValue.</p>
	 *
	 * @param value a {@link java.lang.Double} object.
	 * @param roundedValue a {@link java.lang.Double} object.
	 * @param nutCode a {@link java.lang.String} object.
	 * @param locale a {@link java.util.Locale} object.
	 * @return a {@link java.lang.String} object.
	 * @param measurementPrecision a {@link java.lang.String} object
	 */
	public static String displayValue(Double value, Double roundedValue, String nutCode, String measurementPrecision, Locale locale) {
		if (value == null) {
			return null;
		}
		return getRegulation(getLocalKey(locale)).displayValue(value, roundedValue, nutCode, measurementPrecision, locale);
	}

	/**
	 * <p>displayValue.</p>
	 *
	 * @param value a {@link java.lang.Double} object.
	 * @param roundedValue a {@link java.lang.Double} object.
	 * @param nutCode a {@link java.lang.String} object.
	 * @param locale a {@link java.util.Locale} object.
	 * @param regulation a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 * @param measurementPrecision a {@link java.lang.String} object
	 */
	public static String displayValue(Double value, Double roundedValue, String nutCode, String measurementPrecision, Locale locale,
			String regulation) {
		if (value == null) {
			return null;
		}
		return getRegulation(regulation).displayValue(value, roundedValue, nutCode, measurementPrecision, locale);
	}

	/**
	 * <p>roundGDA.</p>
	 *
	 * @param value a {@link java.lang.Double} object.
	 * @param nutCode a {@link java.lang.String} object.
	 * @param locale a {@link java.util.Locale} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double roundGDA(Double value, String nutCode, Locale locale) {
		if (value == null) {
			return null;
		}
		return getRegulation(getLocalKey(locale)).roundGDA(value, nutCode);
	}

	private static NutrientRegulation getRegulation(String key) {

		if (regulations.containsKey(key)) {
			return regulations.get(key);
		}

		return regulations.get("EU");
	}

}
