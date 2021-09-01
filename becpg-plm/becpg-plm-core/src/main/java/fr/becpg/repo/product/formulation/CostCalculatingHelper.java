package fr.becpg.repo.product.formulation;

import java.util.Date;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.PriceListDataItem;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;

public class CostCalculatingHelper {
	
	private CostCalculatingHelper() {
		// None
	}

	public static Double extractValue(ProductData formulatedProduct, ProductData partProduct, SimpleCharactDataItem simpleCharact) {

		final Date now = new Date();
		if ((partProduct.getPriceList() != null) && !partProduct.getPriceList().isEmpty()) {
			PriceListDataItem item = null;
			boolean matchPlant = false;

			for (PriceListDataItem priceListDataItem : partProduct.getPriceList()) {
				if ((priceListDataItem.getPlants().isEmpty() || formulatedProduct.getPlants().containsAll(priceListDataItem.getPlants()))) {
					matchPlant = true;
					if ((priceListDataItem.getCost() != null) && priceListDataItem.getCost().equals(simpleCharact.getCharactNodeRef())) {

						if (((item == null) || (item.getPrefRank() == null))
								|| ((priceListDataItem.getPrefRank() != null) && (priceListDataItem.getPrefRank() < item.getPrefRank() && (priceListDataItem.getPrefRank()>0 || item == null)))
								|| ((priceListDataItem.getPrefRank() != null) && (item.getPrefRank() !=null && item.getPrefRank() < 0))) {
							if (((priceListDataItem.getStartEffectivity() == null)
									|| (priceListDataItem.getStartEffectivity().getTime() <= now.getTime()))
									&& ((priceListDataItem.getEndEffectivity() == null)
											|| (priceListDataItem.getEndEffectivity().getTime() > now.getTime()))) {
								item = priceListDataItem;
							}
						}
					}
				}
			}

			if ((item != null) && (item.getValue() != null) && (item.getPrefRank() == null || (item.getPrefRank() !=null && item.getPrefRank() >= 0))) {
				return item.getValue();
			} else if ((item == null) && matchPlant) {
				return 0d;
			}
		}

		return simpleCharact.getValue();
	}

}
