package fr.becpg.repo.product.formulation.score;

import fr.becpg.repo.product.data.ProductData;

public interface ScoreCalculatingPlugin {

	public boolean accept(ProductData productData);
	
	public boolean formulateScore(ProductData productData);
	
}
