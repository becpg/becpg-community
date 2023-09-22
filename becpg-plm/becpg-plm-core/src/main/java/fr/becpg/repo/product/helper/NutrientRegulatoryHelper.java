package fr.becpg.repo.product.helper;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.formulation.score.NutriScoreContext;

@Component
public class NutrientRegulatoryHelper {

	private static NutrientRegulatoryHelper instance;
	
	@Value("${beCPG.score.nutriscore.regulatoryClass}")
	private String regulatoryClassName;
	
	public NutrientRegulatoryHelper() {
		instance = this;
	}
	
	@Autowired
	private NutrientRegulatoryPlugin[] nutrientPlugins;
	
	public static Double computeScore(ProductData productData) throws JSONException {
		return computeScore(buildContext(productData));
	}
	
	public static Double computeScore(NutriScoreContext context) throws JSONException {
		return retrieveNutrientPlugin().computeScore(context);
	}
	
	public static String extractClass(ProductData productData) throws JSONException {
		NutriScoreContext context = buildContext(productData);
		computeScore(context);
		return extractClass(context);
	}
	
	public static String extractClass(NutriScoreContext context) {
		return retrieveNutrientPlugin().extractClass(context);
	}
	
	public static NutriScoreContext buildContext(ProductData productData) throws JSONException {
		return retrieveNutrientPlugin().buildContext(productData);
	}
	
	private static NutrientRegulatoryPlugin retrieveNutrientPlugin() {
		for (NutrientRegulatoryPlugin helper : instance.nutrientPlugins) {
			if (helper.getClass().getName().equals(instance.regulatoryClassName)) {
				return helper;
			}
		}
		throw new IllegalStateException("Nutrient regulatory class unknown: " + instance.regulatoryClassName);
	}
}
