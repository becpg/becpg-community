package fr.becpg.repo.product.helper;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.PriceListDataItem;

public class SimulationCostHelper {

	
	/*
	 *  Rang 1 Coût MP 0
	 *  Coût MP 100
	 *  Coût MP 200
	 *  
	 *  Rang 1 Coût transport FRANCE
	 *  Coût transport IT
	 *  Coût transport BELGIQUE
	 */
	
	public static PriceListDataItem priceListItemByCriteria(ProductData productData, NodeRef cost, Double qtyInKg, List<NodeRef> geoOrigins) {
		PriceListDataItem ret = null;
		for (PriceListDataItem priceListDataItem : productData.getPriceList()) {
			if (cost != null && cost.equals(priceListDataItem.getCost())) {
				Double purchaseQtyInKg = priceListDataItem.getPurchaseValue();
				if (purchaseQtyInKg != null) {
					ProductUnit unit = ProductUnit.getUnit(priceListDataItem.getPurchaseUnit());
					purchaseQtyInKg /= unit.getUnitFactor();
					if(unit.isVolume() && productData.getDensity()!=null) {
						purchaseQtyInKg*=productData.getDensity();
					}
					
				}
				if ((qtyInKg == null || (purchaseQtyInKg == null || qtyInKg <= purchaseQtyInKg))) {
					if (geoOrigins == null
							|| (priceListDataItem.getGeoOrigins() == null || priceListDataItem.getGeoOrigins().containsAll(geoOrigins))) {
						
							if (ret == null) {
								ret = priceListDataItem;
							} else {
								Double retPurchaseQtyInKgorL = ret.getPurchaseValue();
								if (retPurchaseQtyInKgorL != null) {
									ProductUnit unit = ProductUnit.getUnit(ret.getPurchaseUnit());
									retPurchaseQtyInKgorL /= unit.getUnitFactor();
									if(unit.isVolume() && productData.getDensity()!=null) {
										retPurchaseQtyInKgorL*=productData.getDensity();
									}
								}

								if (purchaseQtyInKg != null && (retPurchaseQtyInKgorL == null

										|| purchaseQtyInKg > retPurchaseQtyInKgorL)) {
									ret = priceListDataItem;
								}
							}

						}
					
				}
			}
		}
		return ret;
	}
	
	
	
	
	
	
	
	
}
