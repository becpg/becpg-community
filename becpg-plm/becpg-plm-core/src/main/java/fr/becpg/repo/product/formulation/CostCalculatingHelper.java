package fr.becpg.repo.product.formulation;

import java.util.Date;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.PriceListDataItem;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;

/**
 * <p>CostCalculatingHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class CostCalculatingHelper {

	private CostCalculatingHelper() {
		// None
	}

	/**
	 * <p>extractValue.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param partProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param simpleCharact a {@link fr.becpg.repo.repository.model.SimpleCharactDataItem} object
	 * @param effectiveDate a {@link java.util.Date} object
	 * @return a {@link java.lang.Double} object
	 */
	public static Double extractValue(ProductData formulatedProduct, ProductData partProduct, SimpleCharactDataItem simpleCharact,
			Date effectiveDate) {

		if ((partProduct.getPriceList() != null) && !partProduct.getPriceList().isEmpty()) {
			PriceListDataItem item = null;
			boolean matchPlant = false;

			for (PriceListDataItem priceListDataItem : partProduct.getPriceList()) {
				if (((priceListDataItem.getStartEffectivity() == null)
						|| (priceListDataItem.getStartEffectivity().getTime() <= effectiveDate.getTime()))
						&& ((priceListDataItem.getEndEffectivity() == null)
								|| (priceListDataItem.getEndEffectivity().getTime() > effectiveDate.getTime()))) {
					if ((priceListDataItem.getPlants().isEmpty() || formulatedProduct.getPlants().containsAll(priceListDataItem.getPlants()))) {
						matchPlant = true;
						if ((priceListDataItem.getCost() != null) && priceListDataItem.getCost().equals(simpleCharact.getCharactNodeRef())) {

							if (((item == null) || (item.getPrefRank() == null))
									|| ((priceListDataItem.getPrefRank() != null) && (priceListDataItem.getPrefRank() < item.getPrefRank()
											&& (priceListDataItem.getPrefRank() > 0 || item == null)))
									|| ((priceListDataItem.getPrefRank() != null) && (item.getPrefRank() != null && item.getPrefRank() < 0))) {

								item = priceListDataItem;
							}
						}
					}
				}
			}

			if ((item != null) && (item.getValue() != null)
					&& (item.getPrefRank() == null || (item.getPrefRank() != null && item.getPrefRank() >= 0))) {
				return item.getValue();
			} else if ((item == null) && matchPlant) {
				return 0d;
			}
		}

		return simpleCharact.getValue();
	}

	/**
	 * <p>extractValue.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param partProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param simpleCharact a {@link fr.becpg.repo.repository.model.SimpleCharactDataItem} object
	 * @return a {@link java.lang.Double} object
	 */
	public static Double extractValue(ProductData formulatedProduct, ProductData partProduct, SimpleCharactDataItem simpleCharact) {
		return extractValue(formulatedProduct, partProduct, simpleCharact, new Date());
	}

}
