package fr.becpg.repo.product.formulation.nutrient;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.json.JSONException;
import org.json.JSONObject;

import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.formulation.nutrient.AbstractNutrientRegulation.NutrientDefinition;

/**
 * 
 * @author matthieu
 *
 */
public class NutrientFormulationHelper {

	protected static final Log logger = LogFactory.getLog(NutrientFormulationHelper.class);

	public static final String ATTR_NUT_CODE = "nutCode";
	private static final String KEY_VALUE = "v";
	private static final String KEY_MINI = "min";
	private static final String KEY_MAXI = "max";
	private static final String KEY_VALUE_PER_SERVING = "vps";
	private static final String KEY_GDA_PERC = "gda";

	
	private static Map<String, NutrientRegulation> regulations = new HashMap<>();
	
	static {
		regulations.put("EU", new EuropeanNutrientRegulation("beCPG/databases/nuts/EuNutrientRegulation.csv"));

		regulations.put("US", new UsNutrientRegulation("beCPG/databases/nuts/UsNutrientRegulation_2016.csv"));
		regulations.put("US_2013", new UsNutrientRegulation("beCPG/databases/nuts/UsNutrientRegulation_2013_2020.csv"));
		
		regulations.put("CA", new CanadianNutrientRegulation("beCPG/databases/nuts/CanadianNutrientRegulation_2017.csv"));
		regulations.put("CA_2013", new CanadianNutrientRegulation("beCPG/databases/nuts/CanadianNutrientRegulation_2013_2022.csv"));
		
		regulations.put("CN", new ChineseNutrientRegulation("beCPG/databases/nuts/ChineseNutrientRegulation.csv"));
		regulations.put("AU", new AustralianNutrientRegulation("beCPG/databases/nuts/AUNutrientRegulation.csv"));
	}
	

	public static Double extractValuePerServing(String roundedValue, Locale locale) {
		return extractValuePerServing(roundedValue, getLocalKey(locale));
	}

	public static Double extractMini(String roundedValue, Locale locale) {
		return extractMini(roundedValue, getLocalKey(locale));
	}

	public static Double extractMaxi(String roundedValue, Locale locale) {
		return extractMaxi(roundedValue, getLocalKey(locale));
	}

	public static Double extractValue(String roundedValue, Locale locale) {
		return extractValue(roundedValue, getLocalKey(locale));
	}
	
	public static Double extractValuePerServing(String roundedValue, String key) {
		return extractValueByKey(roundedValue, KEY_VALUE_PER_SERVING, key );
	}

	public static Double extractMini(String roundedValue, String key) {
		return extractValueByKey(roundedValue, KEY_MINI, key);
	}

	public static Double extractMaxi(String roundedValue, String key) {
		return extractValueByKey(roundedValue, KEY_MAXI, key);
	}

	public static Double extractValue(String roundedValue, String key) {
		return extractValueByKey(roundedValue, KEY_VALUE,key);
	}

	public static Double extractGDAPerc(String roundedValue, String key) {
		return extractValueByKey(roundedValue, KEY_GDA_PERC,key);
	}

	private static Double extractValueByKey(String roundedValue, String item, String key) {

		JSONObject jsonRound;
		try {
			jsonRound = new JSONObject(roundedValue);

			if (jsonRound.has(item)) {
				JSONObject value = (JSONObject) jsonRound.get(item);
				if (value.has(key)) {
					return parseDouble(value.get(key));
				}
			}

		} catch (JSONException e) {
			logger.error(e, e);
		}

		return null;
	}

	private static String getLocalKey(Locale locale) {
		if (locale.getCountry().equals("US") || 
				locale.getCountry().equals("CA") || 
				locale.getCountry().equals("CN") || 
				locale.getCountry().equals("AU") ) {
			return locale.getCountry();
		} 
		return "EU";
	}

	public static void extractXMLAttribute(Element nutListElt, String roundedValue, Locale locale) {
		if (roundedValue != null) {
			String localKey = getLocalKey(locale);
			String nutCode = nutListElt.attributeValue(ATTR_NUT_CODE);
			try {
				JSONObject jsonRound = new JSONObject(roundedValue);
				for (Iterator<?> i = jsonRound.keys(); i.hasNext();) {
					String valueKey = (String) i.next();
					JSONObject value = (JSONObject) jsonRound.get(valueKey);
					for (Iterator<?> j = value.keys(); j.hasNext();) {
						String locKey = (String) j.next();

						NutrientRegulation regulation = getRegulation(locKey);
						NutrientDefinition def = regulation.getNutrientDefinition(nutCode);

						String prefix = "";
						if (!locKey.equals(localKey)) {
							prefix = "_" + locKey;
						}

						if (def != null) {
							if( def.getSort()!=null) {
								nutListElt.addAttribute("regulSort" + prefix, "" + def.getSort());
							}
							if(def.getDepthLevel()!=null) {
								nutListElt.addAttribute("regulDepthLevel" + prefix, "" + def.getDepthLevel());
							}
							if(Boolean.TRUE.equals(def.getMandatory())) {
								nutListElt.addAttribute("regulDisplayMode" + prefix, "M");
							} else  if(Boolean.TRUE.equals(def.getOptional())) {
								nutListElt.addAttribute("regulDisplayMode" + prefix, "O");
							}
							if(def.getBold()!=null) {
								nutListElt.addAttribute("regulBold" + prefix, "" + def.getBold());
							}
							if(def.getGda()!=null) {
								nutListElt.addAttribute("regulGDA" + prefix, "" + def.getGda());
							}
							if( def.getUl()!=null) {
								nutListElt.addAttribute("regulUL" + prefix, "" + def.getUl());
							}
							
						}

						nutListElt.addAttribute("rounded" + keyToXml(valueKey) + prefix, "" + value.get(locKey));

					}

				}
			} catch (JSONException e) {
				logger.error(e, e);
			}
		}
	}

	private static Double parseDouble(Object vps) {
		if(vps instanceof Integer) {
			return ((Integer) vps).doubleValue();
		} else if(vps instanceof Long) {
			return ((Long) vps).doubleValue();
		} else if(vps instanceof Number) {
			return ((Number) vps).doubleValue();
		}
		return (Double) vps;
	}

	private static String keyToXml(String abrv) {
		switch (abrv) {
		case KEY_MINI:
			return "Mini";
		case KEY_MAXI:
			return "Maxi";
		case KEY_VALUE_PER_SERVING:
			return "ValuePerServing";
		case KEY_GDA_PERC:
			return "GDAPerc";
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
	public static void extractRoundedValue(String nutCode, NutListDataItem n) {

		JSONObject jsonRound = new JSONObject();

		try {

			JSONObject value = new JSONObject();
			JSONObject mini = new JSONObject();
			JSONObject maxi = new JSONObject();
			JSONObject valuePerServing = new JSONObject();
			JSONObject gda = new JSONObject();

			for (String key : getAvailableRegulations()) {
				String nutUnit = n.getUnit();
				


				NutrientRegulation regulation = getRegulation(key);
				NutrientDefinition def = regulation.getNutrientDefinition(nutCode);

				value.put(key, regulation.round(n.getValue(), nutCode, nutUnit));
				mini.put(key, regulation.round(n.getMini(), nutCode, nutUnit));
				maxi.put(key, regulation.round(n.getMaxi(), nutCode, nutUnit));
				Double vps =  regulation.round(n.getValuePerServing(), nutCode, nutUnit);
				
				valuePerServing.put(key,vps);

				Double gdaPerc = null;
				
				
				if(def!=null && vps!=null &&  def.getGda()!=null &&  def.getGda()!=0) {
					gdaPerc  = regulation.roundGDA(100 * vps / def.getGda());
				}
				gda.put(key, gdaPerc);
				
			}

			jsonRound.put(KEY_VALUE, value);
			jsonRound.put(KEY_MINI, mini);
			jsonRound.put(KEY_MAXI, maxi);
			jsonRound.put(KEY_VALUE_PER_SERVING, valuePerServing);
			jsonRound.put(KEY_GDA_PERC, gda);

		} catch (JSONException e) {
			logger.error(e, e);
		}
		n.setRoundedValue(jsonRound.toString());
	}
	

	private static List<String> getAvailableRegulations() {
		 List<String> ret = new LinkedList<>();
		 if(MLTextHelper.isSupportedLocale(Locale.US)) {
			 ret.add("US");
			 ret.add("US_2013");
		 }
		 if(MLTextHelper.isSupportedLocale(Locale.CANADA) 
				 || MLTextHelper.isSupportedLocale(Locale.CANADA_FRENCH)
				){
			 ret.add("CA");
			 ret.add("CA_2013");
		 }
		 if(MLTextHelper.isSupportedLocale(Locale.CHINESE) 
				 || MLTextHelper.isSupportedLocale(Locale.SIMPLIFIED_CHINESE)
				 || MLTextHelper.isSupportedLocale(Locale.TRADITIONAL_CHINESE)
				){
			 ret.add("CN");
		 } 
		 if(MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("en_AU"))
				){
			 ret.add("AU");
		 }

		 ret.add("EU");
		 
		return ret;
	}

	public static Double round(Double value, String nutCode, Locale locale, String nutUnit) {
		return round(value, nutCode, getLocalKey(locale), nutUnit);
	}

	private static Double round(Double value, String nutCode, String key, String nutUnit) {
		if (value == null) {
			return null;
		}
		return getRegulation(key).round(value, nutCode, nutUnit);
	}

	private static NutrientRegulation getRegulation(String key) {
		if(regulations.containsKey(key)) {
			return regulations.get(key);
		}
		
		return regulations.get("EU");
	}


}
