package fr.becpg.repo.decernis;

import java.util.List;
import java.util.Set;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;

public interface DecernisService {


	 static final String DECERNIS_CHAIN_ID = "decernis";
	
	List<ReqCtrlListDataItem> extractDecernisRequirements(ProductData product, Set<String> countries, Set<String> usages);

	String createDecernisChecksum(Set<String> countries, Set<String> usages);
	
}
