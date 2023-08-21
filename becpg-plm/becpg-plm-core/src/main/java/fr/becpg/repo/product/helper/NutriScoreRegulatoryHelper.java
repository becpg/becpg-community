package fr.becpg.repo.product.helper;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.formulation.score.NutriScoreContext;

public interface NutriScoreRegulatoryHelper {
	
	NutriScoreContext buildContext(ProductData productData);
	
	Double computeScore(NutriScoreContext context);
	
	String extractClass(NutriScoreContext context);
}
