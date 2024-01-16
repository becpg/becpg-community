package fr.becpg.repo.product.helper;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.formulation.score.NutriScoreContext;
import fr.becpg.repo.system.SystemConfigurationService;

@Component
public class NutrientRegulatoryHelper {
	
	private static NutrientRegulatoryHelper instance;
	
	@Autowired
	private SystemConfigurationService systemConfigurationService;
	
	public NutrientRegulatoryHelper() {
		instance = this;
	}
	
	@Autowired
	private NutrientRegulatoryPlugin[] nutrientPlugins;
	
	public static Double computeScore(ProductData productData) throws JSONException {
		return computeScore(buildContext(productData));
	}
	
	public static Double computeScore(NutriScoreContext context) throws JSONException {
		return retrieveNutrientPlugin(context.getVersion()).computeScore(context);
	}
	
	public static String extractClass(ProductData productData) throws JSONException {
		NutriScoreContext context = buildContext(productData);
		computeScore(context);
		return extractClass(context);
	}
	
	public static String extractClass(NutriScoreContext context) {
		return retrieveNutrientPlugin(context.getVersion()).extractClass(context);
	}
	
	public static NutriScoreContext buildContext(ProductData productData) throws JSONException {
		return retrieveNutrientPlugin(productData.getNutrientProfileVersion()).buildContext(productData);
	}
	
	private static NutrientRegulatoryPlugin retrieveNutrientPlugin(String version) {
		
		for (NutrientRegulatoryPlugin helper : instance.nutrientPlugins) {
			if (helper.getVersion().equals(version)) {
				return helper;
			}
		}
		
		String regulatoryClassName = instance.systemConfigurationService.confValue("beCPG.formulation.score.nutriscore.regulatoryClass");
		for (NutrientRegulatoryPlugin helper : instance.nutrientPlugins) {
			if (helper.getClass().getName().equals(regulatoryClassName)) {
				return helper;
			}
		}
		throw new IllegalStateException("Nutrient regulatory class unknown: " + regulatoryClassName);
	}
}
