package fr.becpg.repo.product.helper;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.formulation.score.NutriScoreContext;
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * <p>NutrientRegulatoryHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Component
public class NutrientRegulatoryHelper {
	
	private static NutrientRegulatoryHelper instance;
	
	@Autowired
	private SystemConfigurationService systemConfigurationService;
	
	/**
	 * <p>Constructor for NutrientRegulatoryHelper.</p>
	 */
	public NutrientRegulatoryHelper() {
		instance = this;
	}
	
	@Autowired
	private NutrientRegulatoryPlugin[] nutrientPlugins;
	
	/**
	 * <p>computeScore.</p>
	 *
	 * @param productData a {@link fr.becpg.repo.product.data.ProductData} object
	 * @return a {@link java.lang.Double} object
	 * @throws org.json.JSONException if any.
	 */
	public static Double computeScore(ProductData productData) throws JSONException {
		return computeScore(buildContext(productData));
	}
	
	/**
	 * <p>computeScore.</p>
	 *
	 * @param context a {@link fr.becpg.repo.product.formulation.score.NutriScoreContext} object
	 * @return a {@link java.lang.Double} object
	 * @throws org.json.JSONException if any.
	 */
	public static Double computeScore(NutriScoreContext context) throws JSONException {
		return retrieveNutrientPlugin(context.getVersion()).computeScore(context);
	}
	
	/**
	 * <p>extractClass.</p>
	 *
	 * @param productData a {@link fr.becpg.repo.product.data.ProductData} object
	 * @return a {@link java.lang.String} object
	 * @throws org.json.JSONException if any.
	 */
	public static String extractClass(ProductData productData) throws JSONException {
		NutriScoreContext context = buildContext(productData);
		computeScore(context);
		return extractClass(context);
	}
	
	/**
	 * <p>extractClass.</p>
	 *
	 * @param context a {@link fr.becpg.repo.product.formulation.score.NutriScoreContext} object
	 * @return a {@link java.lang.String} object
	 */
	public static String extractClass(NutriScoreContext context) {
		return retrieveNutrientPlugin(context.getVersion()).extractClass(context);
	}
	
	/**
	 * <p>buildContext.</p>
	 *
	 * @param productData a {@link fr.becpg.repo.product.data.ProductData} object
	 * @return a {@link fr.becpg.repo.product.formulation.score.NutriScoreContext} object
	 * @throws org.json.JSONException if any.
	 */
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
