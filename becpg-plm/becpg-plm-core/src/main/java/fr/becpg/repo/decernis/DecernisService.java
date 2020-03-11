package fr.becpg.repo.decernis;

import java.util.List;

import fr.becpg.repo.product.data.ProductData;

public interface DecernisService {

	public String launchDecernisAnalysis(ProductData product, List<String> countries) throws Exception;
	
}
