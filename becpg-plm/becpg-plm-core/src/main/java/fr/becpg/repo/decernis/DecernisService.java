package fr.becpg.repo.decernis;

import java.util.List;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;

public interface DecernisService {

	List<ReqCtrlListDataItem> extractDecernisRequirements(ProductData product, List<String> countries, List<String> usages);

	String createDecernisChecksum(List<String> countries, List<String> usages);
	
}
