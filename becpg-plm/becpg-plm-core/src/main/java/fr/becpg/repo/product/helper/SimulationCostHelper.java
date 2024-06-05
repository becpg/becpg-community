package fr.becpg.repo.product.helper;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.PriceListDataItem;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.system.SystemConfigurationService;
import fr.becpg.repo.variant.filters.VariantFilters;

/**
 * <p>SimulationCostHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class SimulationCostHelper implements InitializingBean {

	@Autowired
	private AlfrescoRepository<ProductData> alfrescoRepository;

	@Autowired
	private SystemConfigurationService systemConfigurationService;
	
	private boolean keepProductUnit() {
		return Boolean.parseBoolean(systemConfigurationService.confValue("beCPG.formulation.costList.keepProductUnit"));
	}

	static SimulationCostHelper INSTANCE;

	/** {@inheritDoc} */
	@Override
	public void afterPropertiesSet() throws Exception {
		INSTANCE = this;

	}

	private SimulationCostHelper() {
		// Make creation private

	}

	private static Log logger = LogFactory.getLog(SimulationCostHelper.class);

	// T(fr.becpg.repo.product.helper.SimulationCostHelper).priceListItemByCriteria(ProductData
	// productData, NodeRef cost, Double qtyInKg, List<NodeRef> geoOrigins, Date effectiveDate)

	/*
	 * Rang 1 Coût MP 0 Coût MP 100 Coût MP 200
	 *
	 * Rang 1 Coût transport FRANCE Coût transport IT Coût transport BELGIQUE
	 */

	/*
	 * Spel helper do not remove
	 */
	/**
	 * <p>priceListItemByCriteria.</p>
	 *
	 * @param productData a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param cost a {@link java.lang.String} object.
	 * @param geoOrigins a {@link java.util.List} object.
	 * @return a {@link fr.becpg.repo.product.data.productList.PriceListDataItem} object.
	 */
	public static PriceListDataItem priceListItemByCriteria(ProductData productData, String cost, List<NodeRef> geoOrigins) {
		return priceListItemByCriteria(productData, new NodeRef(cost), null, geoOrigins);
	}

	/**
	 * <p>priceListItemByCriteria.</p>
	 *
	 * @param productData a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param cost a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.product.data.productList.PriceListDataItem} object
	 */
	public static PriceListDataItem priceListItemByCriteria(ProductData productData, String cost) {
		return priceListItemByCriteria(productData, new NodeRef(cost), null, null);
	}

	/**
	 * <p>priceListItemByCriteria.</p>
	 *
	 * @param productData a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param cost a {@link java.lang.String} object
	 * @param effectiveDate a {@link java.util.Date} object
	 * @return a {@link fr.becpg.repo.product.data.productList.PriceListDataItem} object
	 */
	public static PriceListDataItem priceListItemByCriteria(ProductData productData, String cost, Date effectiveDate) {
		return priceListItemByCriteria(productData, new NodeRef(cost), null, null, effectiveDate);
	}

	/*
	 * Spel helper do not remove
	 */
	/**
	 * <p>priceListItemByCriteria.</p>
	 *
	 * @param productData a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param cost a {@link java.lang.String} object.
	 * @param qtyInKg a {@link java.lang.Double} object.
	 * @return a {@link fr.becpg.repo.product.data.productList.PriceListDataItem} object.
	 */
	public static PriceListDataItem priceListItemByCriteria(ProductData productData, String cost, Double qtyInKg) {
		return priceListItemByCriteria(productData, new NodeRef(cost), qtyInKg, null);
	}

	/*
	 * Spel helper do not remove
	 */
	/**
	 * <p>priceListItemByCriteria.</p>
	 *
	 * @param productData a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param cost a {@link java.lang.String} object.
	 * @param qtyInKg a {@link java.lang.Double} object.
	 * @param geoOrigins a {@link java.util.List} object.
	 * @return a {@link fr.becpg.repo.product.data.productList.PriceListDataItem} object.
	 */
	public static PriceListDataItem priceListItemByCriteria(ProductData productData, String cost, Double qtyInKg, List<NodeRef> geoOrigins) {
		return priceListItemByCriteria(productData, new NodeRef(cost), qtyInKg, geoOrigins);
	}

	/**
	 * <p>priceListItemByCriteria.</p>
	 *
	 * @param productData a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param cost a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param qtyInKg a {@link java.lang.Double} object
	 * @param geoOrigins a {@link java.util.List} object
	 * @return a {@link fr.becpg.repo.product.data.productList.PriceListDataItem} object
	 */
	public static PriceListDataItem priceListItemByCriteria(ProductData productData, NodeRef cost, Double qtyInKg, List<NodeRef> geoOrigins) {
		return priceListItemByCriteria(productData, cost, qtyInKg, geoOrigins, new Date());
	}

	/**
	 * <p>priceListItemByCriteria.</p>
	 *
	 * @param productData a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param cost a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param qtyInKg a {@link java.lang.Double} object.
	 * @param geoOrigins a {@link java.util.List} object.
	 * @return a {@link fr.becpg.repo.product.data.productList.PriceListDataItem} object.
	 * @param effectiveDate a {@link java.util.Date} object
	 */
	public static PriceListDataItem priceListItemByCriteria(ProductData productData, NodeRef cost, Double qtyInKg, List<NodeRef> geoOrigins,
			Date effectiveDate) {
		PriceListDataItem ret = null;
		for (PriceListDataItem priceListDataItem : productData.getPriceList()) {
			if (((priceListDataItem.getStartEffectivity() == null) || (priceListDataItem.getStartEffectivity().getTime() <= effectiveDate.getTime()))
					&& ((priceListDataItem.getEndEffectivity() == null)
							|| (priceListDataItem.getEndEffectivity().getTime() > effectiveDate.getTime()))) {
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

								if ((purchaseQtyInKg != null) && ((retPurchaseQtyInKgorL == null) || (purchaseQtyInKg > retPurchaseQtyInKgorL))) {
									ret = priceListDataItem;
								}
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
	/**
	 * <p>getComponentQuantity.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param componentData a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double getComponentQuantity(ProductData formulatedProduct, ProductData componentData) {

		Double netQty = FormulationHelper.getNetQtyForCost(formulatedProduct);

		if (componentData instanceof PackagingMaterialData) {
			return getPackagingListQty(formulatedProduct, componentData.getNodeRef(), netQty);
		}

		return getCompoListQty(formulatedProduct, componentData.getNodeRef(), netQty);
	}

	private static double getCompoListQty(ProductData productData, NodeRef componentNodeRef, Double parentQty) {
		double totalQty = 0d;
		if (productData.hasCompoListEl()) {

			Double netQty = FormulationHelper.getNetQtyForCost(productData);

			for (CompoListDataItem compoList : productData
					.getCompoList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
				NodeRef productNodeRef = compoList.getProduct();

				ProductData componentProduct = INSTANCE.alfrescoRepository.findOne(productNodeRef);

				Double qty = FormulationHelper.getQtyForCost(compoList, 0d, componentProduct, INSTANCE.keepProductUnit());
				if (logger.isDebugEnabled()) {
					logger.debug("Get CompoListQty " + componentProduct.getName() + "qty: " + qty + " netQty " + netQty);
				}
				if ((qty != null) && (netQty != null) && (netQty != 0d) && parentQty != null) {
					qty = (parentQty * qty) / netQty;

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

	private static double getPackagingListQty(ProductData productData, NodeRef componentNodeRef, Double parentQty) {
		double totalQty = 0d;
		if (productData.hasPackagingListEl()) {

			Double netQty = FormulationHelper.getNetQtyForCost(productData);

			for (PackagingListDataItem packList : productData
					.getPackagingList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
				
				

				ProductData subProductData = INSTANCE.alfrescoRepository.findOne(packList.getProduct());

				Double qty = FormulationHelper.getQtyForCostByPackagingLevel(productData, packList, subProductData);

				
				if (qty != null) {
					if ((netQty != null) && (netQty != 0d) && parentQty != null) {
						qty = (parentQty * qty) / netQty;
					}
					if (logger.isDebugEnabled()) {
						logger.debug("Get packagingListQty " + subProductData.getName() + "qty: " + qty);
					}
					if (subProductData.getNodeRef().equals(componentNodeRef)) {
						totalQty += qty;
					} else if (subProductData.isPackagingKit()) {
						totalQty += qty * getPackagingListQty(subProductData, componentNodeRef, null);

					}
				}
			}

			if (productData.hasCompoListEl()) {
				for (CompoListDataItem compoList : productData
						.getCompoList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
					NodeRef productNodeRef = compoList.getProduct();

					ProductData componentProduct = INSTANCE.alfrescoRepository.findOne(productNodeRef);

					Double qty = FormulationHelper.getQtyForCost(compoList, 0d, componentProduct, INSTANCE.keepProductUnit());
					if (logger.isDebugEnabled()) {
						logger.debug("Get packagingListQty " + componentProduct.getName() + "qty: " + qty + " netQty " + netQty);
					}
					if ((qty != null) && (netQty != null) && (netQty != 0d) && parentQty != null) {
						qty = (parentQty * qty) / netQty;
						totalQty += getPackagingListQty(componentProduct, componentNodeRef, qty);

					}
				}
			}

		}
		return totalQty;
	}

}
