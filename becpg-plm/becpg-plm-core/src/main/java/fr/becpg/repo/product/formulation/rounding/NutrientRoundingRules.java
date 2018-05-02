package fr.becpg.repo.product.formulation.rounding;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class NutrientRoundingRules {

//	Water
//	Sodium, Na
//	Magnesium, Mg
//	Phosphorus, P
//	Potassium, K
//	Calcium, Ca
//	Manganese
//	Iron, Fe
//	Copper
//	Zinc, Zn
//	Selenium
//	Iodine
//	Chromium
//	Protein
//	Raw proteins (N x 6.25)
//	Carbohydrate, by difference
//	Carbohydrate, by difference (with fiber)
//	Sugars, total
//	Starch
//	Polyols, total
//	Fiber, total dietary
//	NSP Fiber
//	Total lipid (fat)
//	Fatty acids, total saturated
//	Fatty acids, total monounsaturated
//	Fatty acids, total polyunsaturated
//	Retinol
//	Beta-carotene
//	Vitamin A
//	Vitamin A, IU
//	Vitamin A, RAE
//	Vitamin D
//	Vitamin D (D2 + D3)
//	Vitamin E (alpha-tocopherol)
//	Vitamin E
//	Vitamin K (phylloquinone)
//	Vitamine K2
//	Vitamin C, total ascorbic acid
//	Thiamin
//	Riboflavin
//	Niacin
//	Panto acid
//	Vitamin B-6
//	Vitamin B7 (Biotin)
//	Vitamin B-12
//	Food Folate
//	Folate DFE
//	Folic Acid
//	Folate, natural
//	CholineTot
//	Beta_Crypt
//	Lycopene
//	Lut+Zea
//	Alcohol (ethanol)
//	Organic acids, total
//	Cholesterol
//	FA 4:0, butyric
//	FA 6:0, caproic
//	FA 8:0, caprylic
//	FA 10:0, capric
//	FA 12:0, lauric
//	FA 14:0 , myristic
//	FA 16:0, palmitic
//	FA 18:0, stearic
//	FA 18:1 n-9 cis, oleic
//	FA 18:2 9c,12c (n-6), linoleic
//	FA 18:3 c9,c12,c15 (n-3), alpha-linolenic
//	FA 20:4 5c,8c,11c,14c (n-6), arachidonic
//	FA 20:5 5c,8c,11c,14c,17c (n-3), EPA
//	FA 22:5 7c,10c,13c,16c,19c (n-3), DPA
//	FA 22:6 4c,7c,10c,13c,16c,19c (n-3), DHA
//	Energy kcal
//	Energy kJ
//	Energy kcal, labelling
//	Energy kJ, labelling
//	Energy kcal Canada, USA
//	Energy kJ Canada, USA
//	Energy kJ, without dietary fibre
//	Salt
//	Points (SP)
//	Points (SP) (Arrondi)
//	Ash
//	Alpha_Carot
//	Carbohydrates, without sugar alcohol
//	Provitamin A (b-carotene equivalents)
//	Vitamin A retinol equivalents
//	Niacine (derived equivalents)
//	Caffein
//	Tryptophan
//	Total omega 3 fatty acids
//	Total trans fatty acids
//	Added Sugars
//	Soluble fiber
//	Insoluble fiber

	
	public enum NutrientRoundingRuleType {
		NRJ
	};
	
	
	
	private interface RoundingRule {
	
		Double round(Double value , Locale locale);
		
	}
	
	
	static Map<NutrientRoundingRuleType, RoundingRule> rules = new LinkedHashMap<>();
	
	{
		rules.put(NutrientRoundingRuleType.NRJ, new RoundingRule() {

			@Override
			public Double round(Double value, Locale locale) {
				return value;
			}
			
		});
		
		//TODO
		
	}
	
	
//
//	private Double nearbyValue (Double value,Double base,Double delta) {
//
// 	 if (value >= (base - delta) && value  < (base + delta) ) {
//  		return base;
//  	} else if (value > base) {
//  		return nearbyValue(value, (base + (delta * 2) ), delta);
//  	} 
//  	return nearbyValue(value, (base - (delta + 2) ), delta);
//}
//
//	private Double getNearValue(Double nutValue, Double delta ) {
//	
//		Double base = Math.floor(nutValue/10);
//		Double result = base * 10;
// 	return nearbyValue(nutValue, result, delta/2);
//}
//	
	//
//
//
//if( isNutPresent(rule_1, row["nutType"])) { // Règle pour les calories. A déplacer dans Aggrégation localNutListValuePerServing
//						
//						if ( row["nutListValuePerServing"] < 5) {
//									nutName + "0" + unit;
//						} else if ( row["nutListValuePerServing"] <= 50 ) {
//									nutName + getNearValue(row["nutListValuePerServing"], 5)+ unit;
//						} else{
//									nutName + getNearValue(row["nutListValuePerServing"], 10)+ unit;
//						}	
//						
//			} else if( isNutPresent(rule_2, row["nutListNut"] )) {
//						
//						if ( row["nutListValuePerServing"] < 0.5) {
//									nutName + "0"+ unit;
//						} else if ( row["nutListValuePerServing"] < 5 ) {
//									nutName + getNearValue(row["nutListValuePerServing"], 0.5)+ unit;
//						} else{
//									nutName + getNearValue(row["nutListValuePerServing"], 1)+ unit;
//						}	
//			
//			} else if( isNutPresent(rule_3, row["nutListNut"]) ) {
//						
//						if ( row["nutListValuePerServing"] < 2) {
//									nutName + "0"+ unit;
//						} else if ( row["nutListValuePerServing"] <= 5 ) {
//									nutName + "<5" + unit;
//						} else{
//									nutName + getNearValue(row["nutListValuePerServing"], 5)+ unit;
//						}
//			
//			} else if(isNutPresent(rule_4, row["nutListNut"] ) ){
//						
//						if ( row["nutListValuePerServing"] < 5) {
//									nutName + "0"+ unit;
//						} else if ( row["nutListValuePerServing"] <= 140 ) {
//									nutName + getNearValue(row["nutListValuePerServing"], 5)+ unit;
//						} else{
//									nutName + getNearValue(row["nutListValuePerServing"], 10)+ unit;
//						}
//			
//			} else if(isNutPresent(rule_5, row["nutListNut"]) ) {
//						var includes = (row["nutListNut"] == reportContext.getMessage("nut.added_sugars", reportContext.getLocale()) )? "includes {nb} {name} ": "{name} {nb}";
//					
//						
//						if ( row["nutListValuePerServing"] < 0.5) {
//									includes.replace("{nb}",   "0" + unit ).replace("{name}", nutName );
//						} else if ( row["nutListValuePerServing"] < 1 ) {
//									includes.replace("{nb}","<1"+ unit).replace("{name}",  nutName );
//						} else{
//									includes.replace("{nb}",	getNearValue(row["nutListValuePerServing"], 1) + unit ).replace("{name}",nutName );
//						}
//								
//			} else if( isNutPresent(rule_7, row["nutListNut"]) ) {//Beta-Carotene
//						
//						if ( row["nutListValuePerServing"] <= 10) {
//						
//									nutName + getNearValue(row["nutListValuePerServing"], 2)+ unit;
//									
//						} else if ( row["nutListValuePerServing"] <=50 ) {
//						
//									nutName + getNearValue(row["nutListValuePerServing"], 5)+ unit;
//									
//						} else{
//									nutName + getNearValue(row["nutListValuePerServing"], 10)+ unit;
//						}
//			
//			} else {
//						nutName + Formatter.format( row["nutListValuePerServing"], "#,##0.##") + unit;
//			}
	
	
	public static Double round(Double value, String roundingRuleType, Locale locale) {
		
		RoundingRule roundingRule = rules.get(NutrientRoundingRuleType.valueOf(roundingRuleType));
		
		if(roundingRule!=null) {
			return roundingRule.round(value, locale);
		}
		
		return value;
	}
	
}
