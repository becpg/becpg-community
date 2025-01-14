package fr.becpg.repo.toxicology;

import fr.becpg.repo.product.data.productList.IngListDataItem;

public class ToxHelper {

	private ToxHelper() {
		
	}
	
	public static Double extractIngMaxQuantity(IngListDataItem ingListDataItem) {
		return ingListDataItem.getMaxi() != null ? ingListDataItem.getMaxi() : ingListDataItem.getQtyPerc();
	}
}
