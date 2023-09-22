package fr.becpg.repo.product.helper;

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
	
	public static NutriScoreContext buildContext(ProductData productData) {
		return retrieveNutrientPlugin().buildContext(productData);
	}
	
	public static Double computeScore(NutriScoreContext context) {
		return retrieveNutrientPlugin().computeScore(context);
	}
	
	public static String extractClass(NutriScoreContext context) {
		return retrieveNutrientPlugin().extractClass(context);
	}
	
	private static NutrientRegulatoryPlugin retrieveNutrientPlugin() {
		String regulatoryClassName = instance.systemConfigurationService.confValue("beCPG.score.nutriscore.regulatoryClass");
		for (NutrientRegulatoryPlugin helper : instance.nutrientPlugins) {
			if (helper.getClass().getName().equals(regulatoryClassName)) {
				return helper;
			}
		}
		throw new IllegalStateException("Nutrient regulatory class unknown: " + regulatoryClassName);
	}
}
