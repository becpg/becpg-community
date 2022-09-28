package fr.becpg.repo.product.formulation;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;

public interface SimpleListQtyProvider {

	Double getQty(CompoListDataItem compoListDataItem, Double parentLossRatio, ProductData componentProduct);

	Double getVolume(CompoListDataItem compoListDataItem, Double parentLossRatio, ProductData componentProduct);
	

	Double getQty(PackagingListDataItem packagingListDataItem, ProductData componentProduct);

	Double getQty(ProcessListDataItem processListDataItem);


	Double getNetWeight();

	Double getNetQty();

	Boolean omitElement(CompoListDataItem compoListDataItem);
}
