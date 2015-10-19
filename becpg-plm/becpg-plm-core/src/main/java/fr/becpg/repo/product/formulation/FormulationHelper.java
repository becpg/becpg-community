/*
 * 
 */
package fr.becpg.repo.product.formulation;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.PLMModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.data.PackagingKitData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ResourceProductData;
import fr.becpg.repo.product.data.constraints.CompoListUnit;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.PackagingListUnit;
import fr.becpg.repo.product.data.constraints.ProcessListUnit;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.constraints.TareUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;

/**
 * The Class FormulationHelper.
 *
 * @author querephi
 */
public class FormulationHelper {

	public static final Double DEFAULT_NET_WEIGHT = 0d;

	public static final Double DEFAULT_COMPONANT_QUANTITY = 0d;

	public static final Double DEFAULT_DENSITY = 1d;

	public static final Double QTY_FOR_PIECE = 1d;

	public static final Double DEFAULT_YIELD = 100d;

	public static final Double DEFAULT_OVERRUN = 0d;
	
	public static final String MISSING_NUMBER_OF_PRODUCT_PER_BOX = "message.formulate.missing.numberOfProductPerBox";

	private static final Log logger = LogFactory.getLog(FormulationHelper.class);

	public static Double getQtyInKg(CompoListDataItem compoListDataItem) {
		return compoListDataItem.getQty() != null ? compoListDataItem.getQty() : DEFAULT_COMPONANT_QUANTITY;
	}

	public static Double getYield(CompoListDataItem compoListDataItem) {
		return compoListDataItem.getYieldPerc() != null && compoListDataItem.getYieldPerc() != 0d ? compoListDataItem.getYieldPerc() : DEFAULT_YIELD;
	}

	public static Double getQtyForCost(CompoListDataItem compoListDataItem, Double parentLossRatio, ProductUnit productUnit) {
		double lossPerc = FormulationHelper.calculateLossPerc(parentLossRatio, compoListDataItem.getLossPerc() != null ? compoListDataItem.getLossPerc() : 0d);
		double yieldPerc = compoListDataItem.getYieldPerc() != null ? compoListDataItem.getYieldPerc() : 100d;
		Double qtySubFormuala = compoListDataItem.getQtySubFormula() != null ? compoListDataItem.getQtySubFormula() : DEFAULT_COMPONANT_QUANTITY;
		Double qtyInKg = compoListDataItem.getQty() != null ? compoListDataItem.getQty() : DEFAULT_COMPONANT_QUANTITY;
		CompoListUnit compoListUnit = compoListDataItem.getCompoListUnit();

		if (compoListUnit != null && productUnit != null) {

			if (isCompoUnitKg(compoListUnit) && isProductUnitKg(productUnit)) {
				return FormulationHelper.getQtyWithLoss(qtyInKg, lossPerc);
			} else if (isCompoUnitLiter(compoListUnit) && isProductUnitLiter(productUnit)) {
				if (CompoListUnit.mL.equals(compoListUnit)) {
					qtySubFormuala = qtySubFormuala / 1000;
				}
				return FormulationHelper.getQtyWithLossAndYield(qtySubFormuala, lossPerc, yieldPerc);
			} else if (compoListUnit.toString().equals(productUnit.toString())) {
				// compoListUnit is P
				return FormulationHelper.getQtyWithLossAndYield(qtySubFormuala, lossPerc, yieldPerc);
			} else if (isProductUnitKg(productUnit)) {
				// compoListUnit is %
				return FormulationHelper.getQtyWithLoss(qtyInKg, lossPerc);
			} else if (isProductUnitLiter(productUnit)) {
				return FormulationHelper.getQtyWithLoss(compoListDataItem.getVolume(), lossPerc);
			}
		}
		return DEFAULT_COMPONANT_QUANTITY;
	}

	public static Double calculateLossPerc(Double parentLossRatio, Double lossPerc) {
		return 100 * ((1 + lossPerc / 100) * (1 + parentLossRatio / 100) - 1);
	}

	private static Double getQtyWithLossAndYield(double qty, double lossPerc, double yieldPerc) {
		return (1 + lossPerc / 100) * qty / (yieldPerc / 100);
	}

	private static Double getQtyWithLoss(double qty, double lossPerc) {
		return (1 + lossPerc / 100) * qty;
	}

	public static Double getQtyForCost(PackagingListDataItem packagingListDataItem) {
		Double lossPerc = packagingListDataItem.getLossPerc() != null ? packagingListDataItem.getLossPerc() : 0d;
		return FormulationHelper.getQtyWithLoss(FormulationHelper.getQty(packagingListDataItem), lossPerc);
	}

	/**
	 * Gets the qty of a packaging item
	 * 
	 * @param packagingListDataItem
	 * @return
	 */
	public static Double getQty(PackagingListDataItem packagingListDataItem) {

		if (packagingListDataItem.getQty() == null) {
			logger.warn("Packaging element doesn't have any quantity");
		}

		Double qty = packagingListDataItem.getQty() != null ? packagingListDataItem.getQty() : DEFAULT_COMPONANT_QUANTITY;
		PackagingListUnit packagingListUnit = packagingListDataItem.getPackagingListUnit();

		if (qty > 0 && packagingListUnit != null) {
			if (packagingListUnit.equals(PackagingListUnit.PP)) {
				qty = 1 / qty;
			} else if (packagingListUnit.equals(PackagingListUnit.g)) {
				qty = qty / 1000;
			}
		}

		return qty;
	}

	/**
	 * Gets the qty of a process item
	 * 
	 * @param processListDataItem
	 * @return
	 * @throws FormulateException
	 */
	public static Double getQty(ProductData formulatedProduct, ProcessListDataItem processListDataItem) throws FormulateException {

		Double qty = 0d;

		if (formulatedProduct instanceof ResourceProductData) {
			qty = QTY_FOR_PIECE;
		} else {
			Double productQtyToTransform = FormulationHelper.QTY_FOR_PIECE;
			if (ProcessListUnit.kg.equals(processListDataItem.getUnit()) || ProcessListUnit.L.equals(processListDataItem.getUnit())) {
				productQtyToTransform = processListDataItem.getQty() != null ? processListDataItem.getQty() : FormulationHelper.getNetWeight(formulatedProduct, null);
			}

			if (ProcessListUnit.Box.equals(processListDataItem.getUnit())) {
				if (formulatedProduct.getDefaultVariantPackagingData() != null && formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes() != null) {
					productQtyToTransform = productQtyToTransform / formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes();
				} else {
					String message = I18NUtil.getMessage(MISSING_NUMBER_OF_PRODUCT_PER_BOX);
					formulatedProduct.getProcessListView().getReqCtrlList().add(new ReqCtrlListDataItem(null, 
							RequirementType.Forbidden, 
							message, 
							null, new ArrayList<NodeRef>()));
				}
			}

			if (productQtyToTransform != null) {

				// process cost depends of rateProcess (€/h)
				if (processListDataItem.getRateResource() != null && processListDataItem.getRateResource() != 0d) {
					qty = productQtyToTransform / processListDataItem.getRateResource();
				}
				// process cost doesn't depend of rateProcess (€/kg)
				else {
					qty = productQtyToTransform;
				}
			}
		}

		if (processListDataItem.getQtyResource() == null) {
			return 0d;
		}
		else{
			return qty * processListDataItem.getQtyResource();
		}
	}

	public static Double getProductQty(NodeRef nodeRef, NodeService nodeService) {
		return (Double) nodeService.getProperty(nodeRef, PLMModel.PROP_PRODUCT_QTY);
	}

	public static Double getDensity(NodeRef nodeRef, NodeService nodeService) {
		Double density = (Double) nodeService.getProperty(nodeRef, PLMModel.PROP_PRODUCT_DENSITY);
		return density != null ? density : DEFAULT_DENSITY;
	}

	public static ProductUnit getProductUnit(NodeRef nodeRef, NodeService nodeService) {
		String strProductUnit = (String) nodeService.getProperty(nodeRef, PLMModel.PROP_PRODUCT_UNIT);
		return strProductUnit != null ? ProductUnit.valueOf(strProductUnit) : null;
	}

	public static boolean isProductUnitLiter(ProductUnit unit) {
		return unit != null && (unit.equals(ProductUnit.L) || unit.equals(ProductUnit.mL));
	}

	public static boolean isProductUnitKg(ProductUnit unit) {
		return unit != null && (unit.equals(ProductUnit.kg) || unit.equals(ProductUnit.g));
	}

	public static boolean isProductUnitP(ProductUnit unit) {
		return unit != null && unit.equals(ProductUnit.P);
	}

	public static boolean isCompoUnitLiter(CompoListUnit unit) {
		return unit != null && (unit.equals(CompoListUnit.L) || unit.equals(CompoListUnit.mL));
	}

	public static boolean isCompoUnitKg(CompoListUnit unit) {
		return unit != null && (unit.equals(CompoListUnit.kg) || unit.equals(CompoListUnit.g) || unit.equals(CompoListUnit.mg));
	}

	public static boolean isCompoUnitP(CompoListUnit unit) {
		return unit != null && unit.equals(CompoListUnit.P);
	}

	public static boolean isPackagingListUnitKg(PackagingListUnit unit) {
		return unit != null && (unit.equals(PackagingListUnit.kg) || unit.equals(PackagingListUnit.g));
	}

	/**
	 * 
	 * @param productData
	 * @return
	 */
	public static Double getNetWeight(NodeRef nodeRef, NodeService nodeService, Double defaultValue) {

		Double netWeight = (Double) nodeService.getProperty(nodeRef, PLMModel.PROP_PRODUCT_NET_WEIGHT);
		if (netWeight != null) {
			return netWeight;
		} else {
			ProductUnit productUnit = getProductUnit(nodeRef, nodeService);
			if (productUnit != null) {
				Double qty = getProductQty(nodeRef, nodeService);
				if (qty != null) {
					if (FormulationHelper.isProductUnitKg(productUnit) || FormulationHelper.isProductUnitLiter(productUnit)) {
						if (productUnit.equals(ProductUnit.g) || productUnit.equals(ProductUnit.mL)) {
							qty = qty / 1000;
						}
						if (FormulationHelper.isProductUnitLiter(productUnit)) {
							Double density = FormulationHelper.getDensity(nodeRef, nodeService);
							qty = qty * density;
						}
						return qty;
					}
				}
			}
		}

		netWeight = (Double) nodeService.getProperty(nodeRef, PLMModel.PROP_PRODUCT_COMPO_QTY_USED);
		if (netWeight != null) {
			return netWeight;
		}

		return defaultValue;
	}

	public static Double getNetWeight(ProductData productData, Double defaultValue) {

		Double netWeight = productData.getNetWeight();
		if (netWeight != null) {
			return netWeight;
		} else {
			ProductUnit productUnit = productData.getUnit();
			if (productUnit != null) {
				Double qty = productData.getQty();
				if (qty != null && FormulationHelper.isProductUnitKg(productUnit) || FormulationHelper.isProductUnitLiter(productUnit)) {
					if (productUnit.equals(ProductUnit.g) || productUnit.equals(ProductUnit.mL)) {
						qty = qty / 1000;
					}
					if (FormulationHelper.isProductUnitLiter(productUnit)) {
						Double density = productData.getDensity();
						if (density != null) {
							qty = qty * density;
						} else {
							return FormulationHelper.getQtyFromComposition(productData, productUnit, defaultValue);
						}
					}
					return qty;
				} else {
					return FormulationHelper.getQtyFromComposition(productData, productUnit, defaultValue);
				}
			}
		}

		return defaultValue;
	}

	public static Double getNetQtyInLorKg(ProductData formulatedProduct, Double defaultValue) {
		ProductUnit productUnit = formulatedProduct.getUnit();
		if (productUnit != null) {
			Double qty = formulatedProduct.getQty();
			if (qty == null) {
				return FormulationHelper.getQtyFromComposition(formulatedProduct, productUnit, defaultValue);
			}

			if (FormulationHelper.isProductUnitKg(productUnit) || FormulationHelper.isProductUnitLiter(productUnit)) {
				if (productUnit.equals(ProductUnit.g) || productUnit.equals(ProductUnit.mL)) {
					qty = qty / 1000;
				}
				return qty;
			} else if (FormulationHelper.isProductUnitP(productUnit)) {
				return FormulationHelper.getNetWeight(formulatedProduct, defaultValue);
			} else if (formulatedProduct instanceof PackagingKitData) {
				return qty;
			}
		}

		return defaultValue;
	}

	private static Double getQtyFromComposition(ProductData formulatedProduct, ProductUnit productUnit, Double defaultValue) {
		Double qty = defaultValue;
		if (productUnit != null && FormulationHelper.isProductUnitLiter(productUnit)) {
			if (formulatedProduct.getRecipeVolumeUsed() != null) {
				qty = formulatedProduct.getRecipeVolumeUsed();
			}
		} else if (formulatedProduct.getRecipeQtyUsed() != null) {
			qty = formulatedProduct.getRecipeQtyUsed();
		}
		return qty;
	}

	public static Double getNetVolume(NodeRef nodeRef, NodeService nodeService) {

		Double qty = getProductQty(nodeRef, nodeService);
		if (qty == null) {
			return null;
		} else {
			ProductUnit productUnit = getProductUnit(nodeRef, nodeService);
			if (productUnit != null && (productUnit.equals(ProductUnit.mL) || productUnit.equals(ProductUnit.L))) {
				if (productUnit.equals(ProductUnit.mL)) {
					return qty / 1000;
				} else if (productUnit.equals(ProductUnit.L)) {
					return qty;
				}
			}
			return null;
		}
	}

	public static Double getNetVolume(ProductData formulatedProduct) {
		if(formulatedProduct.getNetVolume()!=null && formulatedProduct.getNetVolume()>0){
			return formulatedProduct.getNetVolume();
		}
		
		
		Double qty = formulatedProduct.getQty();
		if (qty == null) {
			return null;
		} else {
			ProductUnit productUnit = formulatedProduct.getUnit();
			if (productUnit != null && (productUnit.equals(ProductUnit.mL) || productUnit.equals(ProductUnit.L))) {
				if (productUnit.equals(ProductUnit.mL)) {
					return qty / 1000;
				} else if (productUnit.equals(ProductUnit.L)) {
					return qty;
				}
			}
			return null;
		}
	}

	public static Double getNetVolume(Double qty, CompoListDataItem compoListDataItem, NodeService nodeService) {

		if (qty != null) {
			Double overrun = compoListDataItem.getOverrunPerc();
			Double yield = compoListDataItem.getYieldPerc();
			if (compoListDataItem.getOverrunPerc() == null) {
				overrun = FormulationHelper.DEFAULT_OVERRUN;
			}
			if (compoListDataItem.getYieldPerc() == null) {
				yield = FormulationHelper.DEFAULT_YIELD;
			}
			Double density = FormulationHelper.getDensity(compoListDataItem.getProduct(), nodeService);
			if (density == null || density.equals(0d)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Cannot calculate volume since density is null or equals to 0");
				}
			} else {
				return (100 + overrun) * (yield/100) * qty / (density * 100);
			}
		}

		return null;
	}

	public static Double getNetVolume(CompoListDataItem compoListDataItem, NodeService nodeService) {

		Double qty = FormulationHelper.getQtyInKg(compoListDataItem);

		return getNetVolume(qty, compoListDataItem, nodeService);
	}

	public static Double calculateValue(Double totalValue, Double qtyUsed, Double value, Double netWeight, String unit) {

		if (totalValue == null && value == null) {
			return null;
		}

		totalValue = totalValue != null ? totalValue : 0d;
		value = value != null ? value : 0d;
		value = value*qtyUsed;
		if(netWeight!=null && netWeight!=0d){
			value = value / netWeight;
		}					
		
		totalValue += value;		
		if(unit != null && unit.equals("%")){
			if(totalValue > 100d){
				totalValue = 100d;
			}			
		}		
		return totalValue;
	}

	
	
	public static BigDecimal getTareInKg(CompoListDataItem compoList, NodeService nodeService) {

		Double qty = compoList.getQty();
		CompoListUnit unit = compoList.getCompoListUnit();
		if (compoList.getProduct() != null && qty != null && unit != null) {
			Double productQty = FormulationHelper.getProductQty(compoList.getProduct(), nodeService);
			if (productQty == null) {
				productQty = 1d;
			}
			ProductUnit productUnit = FormulationHelper.getProductUnit(compoList.getProduct(), nodeService);
			BigDecimal tare = FormulationHelper.getTareInKg(compoList.getProduct(), nodeService);

			if (tare != null && productUnit != null) {

				if (FormulationHelper.isCompoUnitP(unit)) {
					qty = compoList.getQtySubFormula();
					if (!FormulationHelper.isProductUnitP(productUnit)) {
						productQty = 1d;
					}
				} else if (FormulationHelper.isCompoUnitLiter(unit)) {
					int compoFactor = unit.equals(CompoListUnit.L) ? 1000 : 1;
					int productFactor = productUnit.equals(ProductUnit.L) ? 1000 : 1;
					qty = compoList.getQtySubFormula() * compoFactor / productFactor;
				} else if (FormulationHelper.isCompoUnitKg(unit)) {
					if (unit.equals(CompoListUnit.g)) {
						qty = qty * 1000;
					} else if (unit.equals(CompoListUnit.mg)) {
						qty = qty * 1000000;
					}
				}
				logger.debug("compo tare: " + tare + " qty " + qty + " productQty " + productQty);
				if (qty != null && !qty.isNaN() && !qty.isInfinite() && productQty != null && !productQty.isNaN() && !productQty.isInfinite() && productQty != 0d) {
					return tare.multiply(new BigDecimal(qty)).divide(new BigDecimal(productQty), MathContext.DECIMAL64);
				} else {
					logger.error("Qty/ProductQty is NaN or 0 or infinite:" + qty + " " + productQty + " for " + compoList.getProduct());
				}
			}
		}
		return new BigDecimal(0d);
	}

	public static BigDecimal getTareInKg(PackagingListDataItem packList, NodeService nodeService) {

		BigDecimal tare = new BigDecimal(0d);
		Double qty = FormulationHelper.getQty(packList);

		if (qty != null && !qty.isNaN() && !qty.isInfinite()) {
			if (FormulationHelper.isPackagingListUnitKg(packList.getPackagingListUnit())) {
				tare = new BigDecimal(qty);
			} else {
				BigDecimal t = FormulationHelper.getTareInKg(packList.getProduct(), nodeService);
				if (t != null) {
					tare = t.multiply(new BigDecimal(qty));
				}
			}
		}
		logger.debug("pack tare " + tare);
		return tare;
	}

	public static BigDecimal getTareInKg(NodeRef productNodeRef, NodeService nodeService) {

		Double tare = (Double) nodeService.getProperty(productNodeRef, PackModel.PROP_TARE);
		String strTareUnit = (String) nodeService.getProperty(productNodeRef, PackModel.PROP_TARE_UNIT);
		if (tare == null || strTareUnit == null) {
			return null;
		} else {
			TareUnit tareUnit = TareUnit.valueOf(strTareUnit);
			return FormulationHelper.getTareInKg(tare, tareUnit);
		}
	}

	public static BigDecimal getTareInKg(Double tare, TareUnit tareUnit) {
		if (tare == null || tareUnit == null) {
			return null;
		} else {
			BigDecimal ret = new BigDecimal(tare);
			if (tareUnit == TareUnit.g) {
				ret = ret.divide(new BigDecimal(1000d));
			}
			return ret;
		}
	}

	public static Double getNetQtyForCost(ProductData formulatedProduct) {
		if (formulatedProduct instanceof PackagingKitData) {
			return FormulationHelper.QTY_FOR_PIECE;
		} else if (formulatedProduct instanceof ResourceProductData) {
			return FormulationHelper.QTY_FOR_PIECE;
		} else {
			if (ProductUnit.P.equals(formulatedProduct.getUnit())) {
				return FormulationHelper.QTY_FOR_PIECE;
			} else {
				return FormulationHelper.getNetQtyInLorKg(formulatedProduct, FormulationHelper.DEFAULT_NET_WEIGHT);
			}
		}
	}

	public static Double getQtyForCostByPackagingLevel(ProductData formulatedProduct, PackagingListDataItem packagingListDataItem, NodeService nodeService) {
		Double qty = FormulationHelper.getQtyForCost(packagingListDataItem);

		// secondary on packagingKit with pallet aspect -> nothing
		// tertiary on packagingKit with pallet aspect -> divide by
		// boxesPerPallet
		// secondary on finishedProduct (if it's not packagingKit with
		// pallet aspect) -> divide by productPerBoxes
		// tertiary on finishedProduct (if it's not packagingKit with
		// pallet aspect) -> divide by productPerBoxes * boxesPerPallet
		PackagingLevel packagingLevel = packagingListDataItem.getPkgLevel();
		if (packagingLevel != null) {
			if (formulatedProduct instanceof PackagingKitData && formulatedProduct.getAspects().contains(PackModel.ASPECT_PALLET)) {
				if (packagingLevel.equals(PackagingLevel.Tertiary)) {
					Integer nbByPalet = (Integer) nodeService.getProperty(formulatedProduct.getNodeRef(), PackModel.PROP_PALLET_BOXES_PER_PALLET);
					if (nbByPalet != null && nbByPalet > 0) {
						qty = qty / nbByPalet;
					}
				}
			} else if ((nodeService.hasAspect(packagingListDataItem.getProduct(), PackModel.ASPECT_PALLET) && PackagingLevel.Secondary.equals(packagingListDataItem.getPkgLevel())
					&& PackagingListUnit.PP.equals(packagingListDataItem.getPackagingListUnit())
					&& PLMModel.TYPE_PACKAGINGKIT.equals(nodeService.getType(packagingListDataItem.getProduct()))) == false && formulatedProduct.getDefaultVariantPackagingData() != null) {
				if (packagingLevel.equals(PackagingLevel.Secondary)) {
					if (formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes() != null && formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes() != 0d) {
						logger.debug("qty : " + qty + " product per boxes " + formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes());
						qty = qty / formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes();
					}
				} else if (packagingLevel.equals(PackagingLevel.Tertiary)) {
					if (formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes() != null && formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes() != 0d
							&& formulatedProduct.getDefaultVariantPackagingData().getBoxesPerPallet() != null && formulatedProduct.getDefaultVariantPackagingData().getBoxesPerPallet() != 0d) {
						logger.debug("qty : " + qty + " product per boxes " + formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes() + " boxes per pallet "
								+ formulatedProduct.getDefaultVariantPackagingData().getBoxesPerPallet());
						qty = qty / (formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes() * formulatedProduct.getDefaultVariantPackagingData().getBoxesPerPallet());
					}
				}
			}
		}
		
		return qty;
	}

}
