package fr.becpg.repo.product.helper;

import java.util.Arrays;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.PackagingKitData;
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.PriceListDataItem;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.variant.filters.VariantFilters;

public class SimulationCostHelper {

	
	
	
	// ON AJoute 
	
//	
//	
//	SimulationCostHelper.simulate("Coût MP","MP1", priceListItemByCriteria("MP1", ) )
//	
//	
//	private double getCompoListQty(ProductData productData, NodeRef componentNodeRef, double parentQty) {
//		double totalQty = 0d;
//		for (CompoListDataItem compoList : productData
//				.getCompoList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
//			NodeRef productNodeRef = compoList.getProduct();
//
//			ProductData componentProduct = alfrescoRepositoryProductData.findOne(productNodeRef);
//
//			Double qty = FormulationHelper.getQtyForCost(compoList, 0d, componentProduct, keepProductUnit);
//			if (logger.isDebugEnabled()) {
//				logger.debug("Get component " + componentProduct.getName() + "qty: " + qty + " recipeQtyUsed " + productData.getRecipeQtyUsed());
//			}
//			if ((qty != null) && (productData.getRecipeQtyUsed() != null) && (productData.getRecipeQtyUsed() != 0d)) {
//				qty = (parentQty * qty) / productData.getRecipeQtyUsed();
//
//				if (productNodeRef.equals(componentNodeRef)) {
//					totalQty += qty;
//				} else {
//					totalQty += getCompoListQty(componentProduct, componentNodeRef, qty);
//				}
//			}
//		}
//		return totalQty;
//	}
//
//	private double getPackagingListQty(ProductData productData, NodeRef componentNodeRef, int palletBoxesPerPallet) {
//		double totalQty = 0d;
//		if (productData.hasPackagingListEl()) {
//			for (PackagingListDataItem packList : productData
//					.getPackagingList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
//		
//				ProductData subProductData = (ProductData) alfrescoRepository.findOne(packList.getProduct());
//				
//				Double qty = FormulationHelper.getQtyForCost(productData, packList, subProductData);
//				if (logger.isDebugEnabled()) {
//					logger.debug("Get component " + subProductData.getName() + "qty: " + qty);
//				}
//				if (subProductData.getNodeRef().equals(componentNodeRef)) {
//					if (PackagingLevel.Tertiary.equals(packList.getPkgLevel())) {
//						totalQty = qty / palletBoxesPerPallet;
//					} else {
//						totalQty += qty;
//					}
//					break;
//				} else if (subProductData instanceof PackagingKitData) {
//					totalQty = qty * getPackagingListQty(subProductData, componentNodeRef,((PackagingKitData)subProductData).getPalletBoxesPerPallet());
//				}
//			}
//		}
//		return totalQty;
//	}
//
//	private void calculateSimulationCosts(ProductData formulatedProduct) {
//		Double netQty = FormulationHelper.getNetQtyForCost(formulatedProduct);
//
//		for (CostListDataItem c : formulatedProduct.getCostList()) {
//			if ((c.getComponentNodeRef() != null) && (c.getParent() != null)) {
//				Double qtyComponent;
//				ProductData componentData = alfrescoRepositoryProductData.findOne(c.getComponentNodeRef());
//				if (componentData instanceof PackagingMaterialData) {
//					qtyComponent = getPackagingListQty(formulatedProduct, c.getComponentNodeRef(), 1);
//				} else {
//					qtyComponent = getCompoListQty(formulatedProduct, c.getComponentNodeRef(), formulatedProduct.getRecipeQtyUsed());
//				}
//				
//				if (c.getSimulatedValue() != null && c.getAspects().contains(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM)) {
//					c.getAspectsToRemove().add(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM);
//				}
//				
//				for (CostListDataItem c2 : componentData.getCostList()) {
//					if (c2.getCost().equals(c.getParent().getCost()) && (c.getSimulatedValue() != null)) {
//						
//						if (logger.isDebugEnabled()) {
//							logger.debug("add simulationCost " + "c2 value " + c2.getValue() + "c simulated value " + c.getSimulatedValue()
//									+ " qty component " + qtyComponent + " netQty " + netQty);
//						}
//						if (c2.getValue() != null) {
//							c.setValue(((c.getSimulatedValue() - c2.getValue()) * qtyComponent) / (netQty != 0 ? netQty : 1d));
//						} else {
//							c.setValue(((c.getSimulatedValue()) * qtyComponent) / (netQty != 0 ? netQty : 1d));
//						}
//						if (c.getParent().getValue() != null) {
//							c.getParent().setValue(c.getParent().getValue() + c.getValue());
//						} else {
//							c.getParent().setValue(c.getValue());
//						}
//						break;
//					}
//				}
//			}
//			if (c.getAspects().contains(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM) && c.getSimulatedValue() == null && c.getParent() != null 
//					&& !nodeService.hasAspect(c.getParent().getCost(),BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM) ) {
//				c.getParent().getAspectsToRemove().add(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM);
//			}
//		}
//	}
//	
//	

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
