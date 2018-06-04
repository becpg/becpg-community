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

import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.productList.NutListDataItem;

public class NutrientRoundingRules {
	protected static final Log logger = LogFactory.getLog(NutrientRoundingRules.class);
	
	
	
	public enum NutrientRoundingRuleType {
	 NRJ, Fat, SatFat, Su, Fiber, P, Na, Salt, Cholesterol, K;
	};
	
	private interface RoundingRule {
		Double round(Double value , Locale locale);
	}
	
	
	public static List<Locale>  getAvailableLocales() {
		List<Locale> ret = new ArrayList<Locale>();		
		ret.add(Locale.FRENCH); 

		if(MLTextHelper.getSupportedLocales().contains(Locale.US)){
			ret.add(Locale.US);
		}
		
		return ret;
	}
	
	static Map<NutrientRoundingRuleType, RoundingRule> rules = new LinkedHashMap<>();
	
	static {
		rules.put(NutrientRoundingRuleType.NRJ, new RoundingRule() {

			@Override
			public Double round(Double value, Locale locale) {
				if(Locale.US.equals(locale)){
					return nearByValueNRJUS(value);
				}
				return  (double) Math.round(value);
			}		
		});
		
		rules.put(NutrientRoundingRuleType.Fat, new RoundingRule() {

			@Override
			public Double round(Double value, Locale locale) {
				
				if(Locale.US.equals(locale)){
					
					return nearByValueUS(value);	
				}
			    return nearByValueEur(value, 0.5);
			}	
		});
		
		rules.put(NutrientRoundingRuleType.SatFat, new RoundingRule() {

			@Override
			public Double round(Double value, Locale locale) {
				if(Locale.US.equals(locale)){
					return nearByValueUS(value);
				} 	
				return nearByValueEur(value,0.1);
			}	
		});
				
		rules.put(NutrientRoundingRuleType.Su, new RoundingRule() {

			@Override
			public Double round(Double value, Locale locale) {
				if(Locale.US.equals(locale)){
					return nearByValueSuFiberPUS (value); 
				}
				return nearByValueEur(value, 0.5);
			}
		});
		
		rules.put(NutrientRoundingRuleType.Fiber, new RoundingRule() {

			@Override
			public Double round(Double value, Locale locale) {
				if(Locale.US.equals(locale)){
					return nearByValueSuFiberPUS (value);					
				}
				return nearByValueEur(value, 0.5);
			}	
		});
		
		rules.put(NutrientRoundingRuleType.P, new RoundingRule() {

			@Override
			public Double round(Double value, Locale locale) {
				if(Locale.US.equals(locale)){
					return nearByValueSuFiberPUS (value);
				}
				return nearByValueEur(value, 0.5);
			}
			
		});
		rules.put(NutrientRoundingRuleType.Na, new RoundingRule() {

			@Override
			public Double round(Double value, Locale locale) {
				if(Locale.US.equals(locale)){
					return nearByValueNaUS(value);
				}
				return nearByValueNaSaltEur(value, 0.005);
			}
		});
		
		rules.put(NutrientRoundingRuleType.Salt, new RoundingRule() {

			@Override
			public Double round(Double value, Locale locale) {
				if(Locale.US.equals(locale)){
					return value;
				}
				return nearByValueNaSaltEur(value, 0.0125 );
			}
		});
		rules.put(NutrientRoundingRuleType.Cholesterol, new RoundingRule() {

			@Override
			public Double round(Double value, Locale locale) {
				if(Locale.US.equals(locale)){
					return nearByValuecholesterolUS(value);
				}
				return value;
			}
		});
		
		rules.put(NutrientRoundingRuleType.K, new RoundingRule() {

			@Override
			public Double round(Double value, Locale locale) {
				if(Locale.US.equals(locale)){
					return nearByValueNaUS(value);
				}
				return value;
			}
		});	
	}	
	
	public static void extractValue(Element nutListElt, String roundingValue){

		Element roundedElement = nutListElt.addElement(PLMModel.PROP_NUTLIST_ROUNDED_VALUE.getLocalName());
		JSONObject obj1 = null;
		try {
			obj1 = new JSONObject(roundingValue);
			for(Iterator<?> i=obj1.keys(); i.hasNext();){
				String key1 = (String) i.next();
				JSONObject obj2 = new JSONObject(obj1.get(key1));
		
				for (Iterator<?> j = obj2.keys(); j.hasNext();) {		
					String key2 = (String) j.next();
				    String val = (String) obj2.get(key2);
				    roundedElement.addAttribute(key2, val);
				  }
			}
		
		}catch (JSONException e) {
			logger.error(e,e);
		}
	}

	public static String extractRoundedValue(NutListDataItem n, String roundingMode) {
		
		JSONObject jsonRound = new JSONObject();
		
		try {
				Double nutValRound = 0.d;
				Double nutMiniRound = 0.d;
				Double nutMaxiRound = 0.d;
				Double nutValuePerServingRound = 0.d;
				Double nutGdaRound = 0.d;
				
				JSONObject value = new JSONObject();
				JSONObject mini = new JSONObject();
				JSONObject maxi = new JSONObject();
				JSONObject valuePerServing = new JSONObject();
				JSONObject gda = new JSONObject();
				
				for(Locale locale :  NutrientRoundingRules.getAvailableLocales()){
					String nutUnit= n.getUnit();
					nutValRound = NutrientRoundingRules.round(n.getValue(), roundingMode, locale, nutUnit);
					nutMiniRound = NutrientRoundingRules.round(n.getMini(), roundingMode, locale, nutUnit);
					nutMaxiRound = NutrientRoundingRules.round(n.getMaxi(), roundingMode, locale, nutUnit);
					nutValuePerServingRound = NutrientRoundingRules.round(n.getValuePerServing(), roundingMode, locale, nutUnit);
					nutGdaRound = NutrientRoundingRules.round(n.getGdaPerc(), roundingMode, locale, nutUnit);
					
					if (!Locale.FRENCH.equals(locale)){
						
						value.put(locale.getLanguage(),nutValRound);
						mini.put(locale.getLanguage(), nutMiniRound);
						maxi.put(locale.getLanguage(), nutMaxiRound);
						valuePerServing.put(locale.getLanguage(), nutValuePerServingRound);
						gda.put(locale.getLanguage(),  nutGdaRound);
					}
					else{
						value.put("default", nutValRound);
						mini.put("default", nutMiniRound);
						maxi.put("default", nutMaxiRound);
						valuePerServing.put("default", nutValuePerServingRound);
						gda.put("default",  nutGdaRound);

					}
					jsonRound.put("value" ,value);
					jsonRound.put("mini", mini);
					jsonRound.put("maxi", maxi);
					jsonRound.put("valuePerServing", valuePerServing);
					jsonRound.put("gda", gda);
					
				}
	
			} catch (JSONException e) {
				logger.error(e,e);
			}
		return jsonRound.toString();
	}

	
//	RoundingRole method for Fat according to european guide	in Kcal
	private  static Double nearByValueNRJUS (Double value){
		
		if (value==null){
			return null;
		}
		else if (value >0.05){
			return (double) (0.01* (int)(Math.ceil(value /0.01)));
		}
		else if (value >=5 && value <= 50){
			return (double) (0.005* (int)(Math.ceil(value /0.005)));
		}
		else if (value<0.005){
		
			return 0.0;
		}
		
		return null;
	}

//	RoundingRole method for Fat according to european guide in g
	private static Double nearByValueEur (Double value, Double minValue){
		if (value==null){
			return null;
		}
		else if (value <= minValue){
			return 0.0;
		}
		else if (value >minValue && value <10){
			return (double) Math.round(10*value)/10;
		}
		else if (value >=10){
			return (double) Math.round(value);
		}
		return null;
	}
	
//	RoundingRole method for Fat according to US guide in g
	
	private  static Double nearByValueUS (Double value){
		
		if (value==null){
			return null;
		}
		else if (value >=5){
			return (double) Math.ceil(value);
		}
		else if (value >=0.5 && value < 5){
			return (double) (0.5* (int)(Math.ceil(value /0.5)));
		}
		else if (value<0.5){
		
			return 0.0;
		}
		
		return null;
	}
	
//RoundingRole method for Sugars/Soluble & Insoluble fiber/Protein 
//According to the US guide
	private static Double nearByValueSuFiberPUS (Double value){
		
		if (value==null){
			return null;
		}
		else if (value <=0.5){
			return 0.0;
		}
		else if (value >=1){
			return (double) Math.ceil(value);
		}
		else if (value >0.5 && value< 1){
			return 1.0;
		}
		else {
			
			return null;
		}
	}
	
//RoundingRole method for sodium according to US guide (Unit:g)
	
	private  static Double nearByValueNaUS (Double value){
		if (value==null){
			return null;
		}	
		else if (value <0.005){
			return 0.0;
		}
		else if (value >=0.005 && value <0.14){
			return (double) (0.005* (int)(Math.ceil(value /0.005)));
		}
		else if (value>0.14) {
			
			return (double) (0.01* (int)(Math.ceil(value /0.01)));	
		}
		else {
			return null; 
		}
	}
	
//RoundingRole method for sodium according to european guide (unit:g)
	private static Double nearByValueNaSaltEur (Double value, Double minValue){
		if (value==null){
			return null;
		}
		else if (value >minValue && value <1){
			
			return (double) Math.round(100*value)/100;
}
		else if (value >=1){
			
			return (double) Math.round(10*value)/10;			
		}
		else if(value <=minValue){
			return 0.0;
		}
		return null;
	}
	
//RoundingRole method for cholesterol according to US guide (unit:g)

	private static Double nearByValuecholesterolUS (Double value){
		if (value==null){
			return null;
		}
		else if (value >0.005){
			
			return (double) (0.005* (int)(Math.ceil(value /0.005)));
		}
		
		else if(value <=0.002){
			return 0.0;
		}
		else if (value>0.002 && value<0.005){
			return 0.005;
		}
		return null;
	}
	
	
	public static Double round(Double value, String roundingRuleType, Locale locale, String nutUnit) {
		
		if (value == null){
			return null; 
		}
		RoundingRule roundingRule = roundingRuleType !=null ? rules.get(NutrientRoundingRuleType.valueOf(roundingRuleType)) : null;
		
		
	  if(roundingRule!=null) {
			if (nutUnit != null && nutUnit.equals("mg/100g")){ //convert mg to g
				value = value / 1000;
				value = roundingRule.round(value, locale);
				return value * 1000;
			}
			if (nutUnit != null && nutUnit.equals("KJ/100g")){ //convert KJ to Kcal
				value = value * 4.18; 
				value = roundingRule.round(value, locale);
				return value / 4.18;
			}	
			return roundingRule.round(value, locale);
		}
		return value;
	}

}
	
	
	

