package fr.becpg.repo.product.helper;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.formulation.score.NutriScoreContext;

/**
 * <p>NutrientRegulatoryPlugin interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface NutrientRegulatoryPlugin {
	
	/**
	 * <p>buildContext.</p>
	 *
	 * @param productData a {@link fr.becpg.repo.product.data.ProductData} object
	 * @return a {@link fr.becpg.repo.product.formulation.score.NutriScoreContext} object
	 */
	NutriScoreContext buildContext(ProductData productData);
	
	/**
	 * <p>computeScore.</p>
	 *
	 * @param context a {@link fr.becpg.repo.product.formulation.score.NutriScoreContext} object
	 * @return a {@link java.lang.Double} object
	 */
	Double computeScore(NutriScoreContext context);
	
	/**
	 * <p>extractClass.</p>
	 *
	 * @param context a {@link fr.becpg.repo.product.formulation.score.NutriScoreContext} object
	 * @return a {@link java.lang.String} object
	 */
	String extractClass(NutriScoreContext context);
	
	/**
	 * <p>getVersion.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	String getVersion();
}
