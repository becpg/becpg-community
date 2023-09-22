package fr.becpg.repo.product.helper;

import org.json.JSONException;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.formulation.score.NutriScoreContext;

public interface NutrientRegulatoryPlugin {
	
	NutriScoreContext buildContext(ProductData productData) throws JSONException;
	
	Double computeScore(NutriScoreContext context) throws JSONException;
	
	String extractClass(NutriScoreContext context);
}
