package fr.becpg.repo.toxicology;

import fr.becpg.repo.product.data.productList.IngListDataItem;

/**
 * <p>ToxHelper class.</p>
 *
 * @author matthieu
 */
public class ToxHelper {

	private ToxHelper() {
		
	}
	
	/**
	 * <p>extractIngMaxQuantity.</p>
	 *
	 * @param ingListDataItem a {@link fr.becpg.repo.product.data.productList.IngListDataItem} object
	 * @return a {@link java.lang.Double} object
	 */
	public static Double extractIngMaxQuantity(IngListDataItem ingListDataItem) {
		return ingListDataItem.getMaxi() != null ? ingListDataItem.getMaxi() : ingListDataItem.getQtyPerc();
	}
}
