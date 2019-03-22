package fr.becpg.repo.product.requirement;

import java.util.List;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;

public interface RequirementScanner {

	List<ReqCtrlListDataItem> checkRequirements(ProductData formulatedProduct, List<ProductSpecificationData> specifications);

}
