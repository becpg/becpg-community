package fr.becpg.repo.product.helper;

import java.util.Arrays;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.PackagingKitData;
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.PriceListDataItem;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.variant.filters.VariantFilters;

@Service
public class SimulationCostHelper implements InitializingBean {

	@Autowired
	private AlfrescoRepository<ProductData> alfrescoRepository;

	@Value("${beCPG.formulation.costList.keepProductUnit}")
	private boolean keepProductUnit = false;

	static SimulationCostHelper INSTANCE;

	@Override
	public void afterPropertiesSet() throws Exception {
		INSTANCE = this;

	}

	private SimulationCostHelper() {
		// Make creation private

	}

	private static Log logger = LogFactory.getLog(SimulationCostHelper.class);

	// T(fr.becpg.repo.product.helper.SimulationCostHelper).priceListItemByCriteria(ProductData
	// productData, NodeRef cost, Double qtyInKg, List<NodeRef> geoOrigins)

	/*
	 * Rang 1 Coût MP 0 Coût MP 100 Coût MP 200
	 *
	 * Rang 1 Coût transport FRANCE Coût transport IT Coût transport BELGIQUE
	 */

	/*
	 * Spel helper do not remove
	 */
	public static PriceListDataItem priceListItemByCriteria(ProductData productData, String cost, List<NodeRef> geoOrigins) {
		return priceListItemByCriteria(productData, new NodeRef(cost), null, geoOrigins);
	}

	/*
	 * Spel helper do not remove
	 */
	public static PriceListDataItem priceListItemByCriteria(ProductData productData, String cost, Double qtyInKg) {
		return priceListItemByCriteria(productData, new NodeRef(cost), qtyInKg, null);
	}

	/*
	 * Spel helper do not remove
	 */
	public static PriceListDataItem priceListItemByCriteria(ProductData productData, String cost, Double qtyInKg, List<NodeRef> geoOrigins) {
		return priceListItemByCriteria(productData, new NodeRef(cost), qtyInKg, geoOrigins);
	}

	public static PriceListDataItem priceListItemByCriteria(ProductData productData, NodeRef cost, Double qtyInKg, List<NodeRef> geoOrigins) {
		PriceListDataItem ret = null;
		for (PriceListDataItem priceListDataItem : productData.getPriceList()) {
			if ((cost != null) && cost.equals(priceListDataItem.getCost())) {
				Double purchaseQtyInKg = priceListDataItem.getPurchaseValue();
				if (purchaseQtyInKg != null) {
					ProductUnit unit = ProductUnit.getUnit(priceListDataItem.getPurchaseUnit());
					purchaseQtyInKg /= unit.getUnitFactor();
					if (unit.isVolume() && (productData.getDensity() != null)) {
						purchaseQtyInKg *= productData.getDensity();
					}

				}
				if (((qtyInKg == null) || ((purchaseQtyInKg == null) || (qtyInKg >= purchaseQtyInKg)))) {
					if (((geoOrigins == null) && ((priceListDataItem.getGeoOrigins() == null) || priceListDataItem.getGeoOrigins().isEmpty()))
							|| ((priceListDataItem.getGeoOrigins() == null) || ((geoOrigins != null) && !geoOrigins.isEmpty()
									&& priceListDataItem.getGeoOrigins().containsAll(geoOrigins)))) {

						if (ret == null) {
							ret = priceListDataItem;
						} else {
							Double retPurchaseQtyInKgorL = ret.getPurchaseValue();
							if (retPurchaseQtyInKgorL != null) {
								ProductUnit unit = ProductUnit.getUnit(ret.getPurchaseUnit());
								retPurchaseQtyInKgorL /= unit.getUnitFactor();
								if (unit.isVolume() && (productData.getDensity() != null)) {
									retPurchaseQtyInKgorL *= productData.getDensity();
								}
							}

							if ((purchaseQtyInKg != null) && ((retPurchaseQtyInKgorL == null)

									|| (purchaseQtyInKg > retPurchaseQtyInKgorL))) {
								ret = priceListDataItem;
							}
						}

					}

				}
			}
		}
		return ret;
	}

	/*
	 * Spel helper do not remove
	 */
	public static Double getComponentQuantity(ProductData formulatedProduct, ProductData componentData) {
		if (componentData instanceof PackagingMaterialData) {
			return getPackagingListQty(formulatedProduct, componentData.getNodeRef(), 1, formulatedProduct.getRecipeQtyUsed());
		}

		return getCompoListQty(formulatedProduct, componentData.getNodeRef(), formulatedProduct.getRecipeQtyUsed());
	}

	private static double getCompoListQty(ProductData productData, NodeRef componentNodeRef, Double parentQty) {
		double totalQty = 0d;
		if (productData.hasCompoListEl()) {
			for (CompoListDataItem compoList : productData
					.getCompoList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
				NodeRef productNodeRef = compoList.getProduct();

				ProductData componentProduct = INSTANCE.alfrescoRepository.findOne(productNodeRef);

				Double qty = FormulationHelper.getQtyForCost(compoList, 0d, componentProduct, INSTANCE.keepProductUnit);
				if (logger.isDebugEnabled()) {
					logger.debug("Get component " + componentProduct.getName() + "qty: " + qty + " recipeQtyUsed " + productData.getRecipeQtyUsed());
				}
				if ((qty != null) && (productData.getRecipeQtyUsed() != null) && (productData.getRecipeQtyUsed() != 0d)) {
					qty = (parentQty * qty) / productData.getRecipeQtyUsed();

					if (productNodeRef.equals(componentNodeRef)) {
						totalQty += qty;
					} else {
						totalQty += getCompoListQty(componentProduct, componentNodeRef, qty);
					}
				}
			}
		}
		return totalQty;
	}

	private static double getPackagingListQty(ProductData productData, NodeRef componentNodeRef, int palletBoxesPerPallet, Double parentQty) {
		double totalQty = 0d;
		if (productData.hasPackagingListEl()) {
			for (PackagingListDataItem packList : productData
					.getPackagingList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {

				ProductData subProductData = INSTANCE.alfrescoRepository.findOne(packList.getProduct());

				Double qty = FormulationHelper.getQtyForCost(productData, packList, subProductData);
				if ((qty != null) && (productData.getRecipeQtyUsed() != null) && (productData.getRecipeQtyUsed() != 0d)) {
					qty = (parentQty * qty) / productData.getRecipeQtyUsed();
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Get component " + subProductData.getName() + "qty: " + qty);
				}
				if (subProductData.getNodeRef().equals(componentNodeRef)) {
					if (PackagingLevel.Tertiary.equals(packList.getPkgLevel())) {
						totalQty = qty / palletBoxesPerPallet;
					} else {
						totalQty += qty;
					}
					break;
				} else if (subProductData instanceof PackagingKitData) {
					totalQty = qty
							* getPackagingListQty(subProductData, componentNodeRef, ((PackagingKitData) subProductData).getPalletBoxesPerPallet(),null);
				}
			}

			if (productData.hasCompoListEl()) {
				for (CompoListDataItem compoList : productData
						.getCompoList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
					NodeRef productNodeRef = compoList.getProduct();

					ProductData componentProduct =  INSTANCE.alfrescoRepository.findOne(productNodeRef);

					Double qty = FormulationHelper.getQtyForCost(compoList, 0d, componentProduct, INSTANCE.keepProductUnit);
					if (logger.isDebugEnabled()) {
						logger.debug("Get component " + componentProduct.getName() + "qty: " + qty + " recipeQtyUsed " + productData.getRecipeQtyUsed());
					}
					if ((qty != null) && (productData.getRecipeQtyUsed() != null) && (productData.getRecipeQtyUsed() != 0d)) {
						qty = (parentQty * qty) / productData.getRecipeQtyUsed();
						totalQty += getPackagingListQty(componentProduct, componentNodeRef, palletBoxesPerPallet, qty);

					}
				}
			}

		}
		return totalQty;
	}

}
