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
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.constraints.TareUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;

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
		if ((compoListDataItem.getQty() == null) || compoListDataItem.getQty().isNaN() || compoListDataItem.getQty().isInfinite()) {
			compoListDataItem.setQty(DEFAULT_COMPONANT_QUANTITY);
		}
		return compoListDataItem.getQty();
	}

	public static Double getYield(CompoListDataItem compoListDataItem) {
		return (compoListDataItem.getYieldPerc() != null) && (compoListDataItem.getYieldPerc() != 0d) && !compoListDataItem.getYieldPerc().isNaN()
				&& !compoListDataItem.getYieldPerc().isInfinite() ? compoListDataItem.getYieldPerc() : DEFAULT_YIELD;
	}

	public static Double getQtyForCost(CompoListDataItem compoListDataItem, Double parentLossRatio, ProductData componentProduct,
			boolean keepProductUnit) {
		Double lossPerc = FormulationHelper.calculateLossPerc(parentLossRatio != null ? parentLossRatio : 0d, FormulationHelper.getComponentLossPerc(componentProduct, compoListDataItem));
		Double yieldPerc = compoListDataItem.getYieldPerc() != null ? compoListDataItem.getYieldPerc() : 100d;
		Double qtySubFormula = compoListDataItem.getQtySubFormula() != null ? compoListDataItem.getQtySubFormula() : DEFAULT_COMPONANT_QUANTITY;
		Double qtyInKg = compoListDataItem.getQty() != null ? compoListDataItem.getQty() : DEFAULT_COMPONANT_QUANTITY;
		Double qtyInL = compoListDataItem.getVolume() != null ? compoListDataItem.getVolume() : DEFAULT_COMPONANT_QUANTITY;
		ProductUnit compoListUnit = compoListDataItem.getCompoListUnit();
		ProductUnit componentProductUnit = componentProduct.getUnit();
		Double componentNetWeight = getNetWeight(componentProduct, DEFAULT_NET_WEIGHT);

		Double unitFactor = 1d;
		if (componentProductUnit != null) {
			if ((componentProductUnit.isWeight() || componentProductUnit.isVolume())) {
				if (keepProductUnit) {
					unitFactor = componentProductUnit.getUnitFactor();
				} else if (componentProductUnit.isLb()) {
					unitFactor = ProductUnit.lb.getUnitFactor();
				} else if(componentProductUnit.isGal()) {
					unitFactor = ProductUnit.gal.getUnitFactor();
				}
			}
		}

		if ((compoListUnit != null) && (componentProductUnit != null)) {
			if (componentProductUnit.isWeight()) {
				return FormulationHelper.getQtyWithLoss(qtyInKg, lossPerc) * unitFactor;
			} else if (componentProductUnit.isVolume()) {
				return FormulationHelper.getQtyWithLoss(qtyInL, lossPerc) * unitFactor;
			} else if (componentProductUnit.isP()) {
				if ((!compoListUnit.isP()) && (componentNetWeight != null) && (componentNetWeight != 0)) {
					return (FormulationHelper.getQtyWithLoss(qtyInKg, lossPerc) * unitFactor) / componentNetWeight;
				} else {
					return FormulationHelper.getQtyWithLossAndYield(qtySubFormula, lossPerc, yieldPerc) * unitFactor;
				}
			}
		}
		return DEFAULT_COMPONANT_QUANTITY;
	}

	public static Double calculateLossPerc(Double parentLossRatio, Double lossPerc) {
		return 100 * (((1 + (lossPerc / 100)) * (1 + (parentLossRatio / 100))) - 1);
	}

	private static Double getQtyWithLossAndYield(double qty, double lossPerc, double yieldPerc) {
		return ((1 + (lossPerc / 100)) * qty) / (yieldPerc / 100);
	}

	private static Double getQtyWithLoss(double qty, double lossPerc) {
		return (1 + (lossPerc / 100)) * qty;
	}

	public static Double getQtyForCost(ProductData formulatedProduct, PackagingListDataItem packagingListDataItem, ProductData subProductData) {
		Double lossPerc = packagingListDataItem.getLossPerc() != null ? packagingListDataItem.getLossPerc() : 0d;
		lossPerc = calculateLossPerc(formulatedProduct.getProductLossPerc(), lossPerc);
		return FormulationHelper.getQtyWithLoss(FormulationHelper.getQty(packagingListDataItem,subProductData), lossPerc);
	}

	/**
	 * Gets the qty of a packaging item
	 *
	 * @param packagingListDataItem
	 * @return
	 */
	private static Double getQty(PackagingListDataItem packagingListDataItem, ProductData subProductData) {

		if (packagingListDataItem.getQty() == null) {
			logger.warn("Packaging element doesn't have any quantity");
		}
		
		
		Double qty = packagingListDataItem.getQty() != null ? packagingListDataItem.getQty() : DEFAULT_COMPONANT_QUANTITY;
		ProductUnit packagingListUnit = packagingListDataItem.getPackagingListUnit();

		if ((qty > 0) && (packagingListUnit != null)) {
			if (packagingListUnit.equals(ProductUnit.PP)) {
				qty = 1 / qty;
			} else {
				//Convert qty in L M KG or P
				qty = qty / packagingListUnit.getUnitFactor();
				
				ProductUnit productUnit = subProductData.getUnit();
				//Convert cost or tare in product Unit
				if(productUnit!=null) {
					qty = qty * productUnit.getUnitFactor();
				}
						
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
			if ((processListDataItem.getUnit() != null) && (processListDataItem.getUnit().isWeight() || processListDataItem.getUnit().isVolume())) {
				productQtyToTransform = processListDataItem.getQty() != null
						? processListDataItem.getQty() : FormulationHelper.getNetWeight(formulatedProduct, null);
					
			}

			if (ProductUnit.Box.equals(processListDataItem.getUnit())) {
				if ((formulatedProduct.getDefaultVariantPackagingData() != null)
						&& (formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes() != null)) {
					productQtyToTransform = productQtyToTransform / formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes();
				} else {
					String message = I18NUtil.getMessage(MISSING_NUMBER_OF_PRODUCT_PER_BOX);
					formulatedProduct.getReqCtrlList().add(new ReqCtrlListDataItem(null, RequirementType.Forbidden, message, null,
							new ArrayList<NodeRef>(), RequirementDataType.Packaging));
				}
			}

			if (productQtyToTransform != null) {

				// process cost depends of rateProcess (€/h)
				if ((processListDataItem.getRateResource() != null) && (processListDataItem.getRateResource() != 0d)) {
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
		} else {
			return qty * processListDataItem.getQtyResource();
		}
	}

	public static Double getDensity(NodeRef nodeRef, NodeService nodeService) {
		Double density = (Double) nodeService.getProperty(nodeRef, PLMModel.PROP_PRODUCT_DENSITY);
		return density != null ? density : DEFAULT_DENSITY;
	}

	/**
	 *
	 * @param productData
	 * @return
	 */
	@Deprecated
	public static Double getNetWeight(NodeRef nodeRef, NodeService nodeService, Double defaultValue) {

		Double netWeight = (Double) nodeService.getProperty(nodeRef, PLMModel.PROP_PRODUCT_NET_WEIGHT);
		String strProductUnit = (String) nodeService.getProperty(nodeRef, PLMModel.PROP_PRODUCT_UNIT);
		ProductUnit productUnit = strProductUnit != null ? ProductUnit.valueOf(strProductUnit) : null;

		Double qty = (Double) nodeService.getProperty(nodeRef, PLMModel.PROP_PRODUCT_QTY);
		if (netWeight != null) {
			return netWeight;
		} else {
			if (productUnit != null) {
				if (qty != null) {
					if (productUnit.isWeight() || productUnit.isVolume()) {
						qty = qty / productUnit.getUnitFactor();
						if (productUnit.isVolume()) {
							Double density = FormulationHelper.getDensity(nodeRef, nodeService);
							if (density != null) {
								qty = qty * density;
							}
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
				if ((qty != null) && (productUnit.isWeight() || productUnit.isVolume())) {
					qty = qty / productUnit.getUnitFactor();
					if (productUnit.isVolume()) {
						Double density = productData.getDensity();
						if ((density != null)) {
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

			if (productUnit.isWeight() || productUnit.isVolume()) {
				return qty / productUnit.getUnitFactor();
			} else if (productUnit.isP()) {
				return FormulationHelper.getNetWeight(formulatedProduct, defaultValue);
			} else if (formulatedProduct instanceof PackagingKitData) {
				return qty;
			}
		}

		return defaultValue;
	}

	

	
	public static Double getServingSizeInLorKg(ProductData formulatedProduct) {
		if(formulatedProduct.getServingSize()!=null && formulatedProduct.getServingSizeUnit()!=null) {
			return (formulatedProduct.getServingSize() /  formulatedProduct.getServingSizeUnit().getUnitFactor());
		}
		return formulatedProduct.getServingSize();
	}
	
	
	public static Double getQtyFromComposition(ProductData formulatedProduct, Double defaultValue) {
		return getQtyFromComposition(formulatedProduct, formulatedProduct.getUnit(), defaultValue);

	}

	private static Double getQtyFromComposition(ProductData formulatedProduct, ProductUnit productUnit, Double defaultValue) {
		Double qty = defaultValue;
		if ((productUnit != null) && productUnit.isVolume()) {
			if (formulatedProduct.getRecipeVolumeUsed() != null) {
				qty = formulatedProduct.getRecipeVolumeUsed();
			}
		} else if (formulatedProduct.getRecipeQtyUsed() != null) {
			qty = formulatedProduct.getRecipeQtyUsed();
		}
		return qty;
	}

	public static Double getNetVolume(ProductData formulatedProduct) {
		if ((formulatedProduct.getNetVolume() != null) && (formulatedProduct.getNetVolume() > 0)) {
			return formulatedProduct.getNetVolume();
		}

		Double qty = formulatedProduct.getQty();
		if (qty == null) {
			return null;
		} else {
			ProductUnit productUnit = formulatedProduct.getUnit();
			if ((productUnit != null) && productUnit.isVolume()) {
				return qty / productUnit.getUnitFactor();
			}
			return null;
		}
	}

	public static Double getNetVolume(Double qty, CompoListDataItem compoListDataItem, NodeService nodeService) {

		if (qty != null) {
			Double overrun = compoListDataItem.getOverrunPerc();
			Double yield = compoListDataItem.getYieldPerc();
			if ((compoListDataItem.getOverrunPerc() == null) || compoListDataItem.getOverrunPerc().isNaN()
					|| compoListDataItem.getOverrunPerc().isInfinite()) {
				overrun = FormulationHelper.DEFAULT_OVERRUN;
			}
			if ((compoListDataItem.getYieldPerc() == null) || compoListDataItem.getYieldPerc().isNaN()
					|| compoListDataItem.getYieldPerc().isInfinite()) {
				yield = FormulationHelper.DEFAULT_YIELD;
			}
			Double density = FormulationHelper.getDensity(compoListDataItem.getProduct(), nodeService);
			if ((density == null) || density.equals(0d) || density.isNaN() || density.isInfinite()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Cannot calculate volume since density is null or equals to 0");
				}
			} else {
				return ((100 + overrun) * (yield / 100) * qty) / (density * 100);
			}
		}

		return null;
	}

	public static Double getNetVolume(CompoListDataItem compoListDataItem, NodeService nodeService) {

		Double qty = FormulationHelper.getQtyInKg(compoListDataItem);

		return getNetVolume(qty, compoListDataItem, nodeService);
	}

	public static Double calculateValue(Double totalValue, Double qtyUsed, Double value, Double netWeight, String unit) {

		if ((totalValue == null) && (value == null)) {
			return null;
		}

		totalValue = totalValue != null ? totalValue : 0d;
		value = value != null ? value : 0d;
		value = value * qtyUsed;
		if ((netWeight != null) && (netWeight != 0d)) {
			value = value / netWeight;
		}

		totalValue += value;
		if ((unit != null) && (unit.equals("%") || unit.equals("Perc"))) {
			if (totalValue > 100d) {
				totalValue = 100d;
			}
		}
		return totalValue;
	}

	public static BigDecimal getTareInKg(CompoListDataItem compoList, ProductData subProduct) {

		ProductUnit compoListUnit = compoList.getCompoListUnit();
		Double qty = compoList.getQtySubFormula();

		if ((subProduct != null) && (compoListUnit != null) && (qty != null)) {
			BigDecimal tare = FormulationHelper.getTareInKg(subProduct);
			if (tare != null) {
				Double productQty = subProduct.getQty();
				if (productQty == null) {
					productQty = 1d;
				}

				if (compoListUnit.isP()) {
					if ((subProduct.getUnit() != null) && !subProduct.getUnit().isP()) {
						productQty = 1d;
					}

				} else if (compoListUnit.isWeight() || compoListUnit.isVolume()) {

					productQty = getNetQtyInLorKg(subProduct, 1d);
					qty = getQtyInKg(compoList);

				}

				if ((qty != null) && !qty.isNaN() && !qty.isInfinite() && (productQty != null) && !productQty.isNaN() && !productQty.isInfinite()
						&& (productQty != 0d)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Compo tare [" + subProduct.getName() + "]: " + tare + " qty " + qty + " productQty " + productQty);
					}
					return tare.multiply(new BigDecimal(qty)).divide(new BigDecimal(productQty), MathContext.DECIMAL64);
				} else {
					logger.error("Qty/ProductQty is NaN or 0 or infinite:" + qty + " " + productQty + " for " + compoList.getProduct());
				}
			}

		}

		return new BigDecimal(0d);
	}

	public static BigDecimal getTareInKg(PackagingListDataItem packList, ProductData subProductData) {

		BigDecimal tare = new BigDecimal(0d);
		Double qty = FormulationHelper.getQty(packList, subProductData);

		if ((qty != null) && !qty.isNaN() && !qty.isInfinite()) {
			if ((packList.getPackagingListUnit() != null) && packList.getPackagingListUnit().isWeight()) {
				tare = new BigDecimal(qty);
			} else {
				BigDecimal t = FormulationHelper.getTareInKg(subProductData);
				if (t != null) {
					tare = t.multiply(new BigDecimal(qty));
				}
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Pack tare [" + subProductData.getName() + "] (" + packList.getPackagingListUnit() + ")"
					+ tare + " qty " + qty+" tareUnit "+FormulationHelper.getTareInKg(subProductData));
		}
		return tare;
	}

	public static BigDecimal getTareInKg(ProductData productData) {

		Double tare = productData.getTare();
		TareUnit tareUnit = productData.getTareUnit();
		if ((tare == null) || (tareUnit == null)) {
			return null;
		} else {
			return FormulationHelper.getTareInKg(tare, tareUnit);
		}
	}

	public static BigDecimal getTareInKg(Double tare, TareUnit tareUnit) {
		if ((tare == null) || (tareUnit == null)) {
			return null;
		} else {
			return (new BigDecimal(tare)).divide(new BigDecimal(tareUnit.getUnitFactor()), MathContext.DECIMAL64);
		}
	}

	public static Double getNetQtyForCost(ProductData formulatedProduct) {
		if (formulatedProduct instanceof PackagingKitData) {
			return FormulationHelper.QTY_FOR_PIECE;
		} else if (formulatedProduct instanceof ResourceProductData) {
			return FormulationHelper.QTY_FOR_PIECE;
		} else {
			if ((formulatedProduct.getUnit() != null) && formulatedProduct.getUnit().isP()) {
				if (formulatedProduct.getQty() != null) {
					return formulatedProduct.getQty();
				}
				return FormulationHelper.QTY_FOR_PIECE;
			} else {
				return FormulationHelper.getNetQtyInLorKg(formulatedProduct, FormulationHelper.DEFAULT_NET_WEIGHT);
			}
		}
	}

	public static Double getQtyForCostByPackagingLevel(ProductData formulatedProduct, PackagingListDataItem packagingListDataItem, ProductData subProductData) {
		
		Double qty = FormulationHelper.getQtyForCost(formulatedProduct, packagingListDataItem, subProductData);

		// secondary on packagingKit with pallet aspect -> nothing
		// tertiary on packagingKit with pallet aspect -> divide by
		// boxesPerPallet
		// secondary on finishedProduct (if it's not packagingKit with
		// pallet aspect) -> divide by productPerBoxes
		// tertiary on finishedProduct (if it's not packagingKit with
		// pallet aspect) -> divide by productPerBoxes * boxesPerPallet
		PackagingLevel packagingLevel = packagingListDataItem.getPkgLevel();
		if (packagingLevel != null) {
			if ((formulatedProduct instanceof PackagingKitData) && formulatedProduct.getAspects().contains(PackModel.ASPECT_PALLET)) {
				if (packagingLevel.equals(PackagingLevel.Tertiary)) {
					Integer nbByPalet = ((PackagingKitData) formulatedProduct).getPalletBoxesPerPallet();
					if ((nbByPalet != null) && (nbByPalet > 0)) {
						qty = qty / nbByPalet;
					}
				}
			} else if (((subProductData.getAspects().contains(PackModel.ASPECT_PALLET)
					&& PackagingLevel.Secondary.equals(packagingListDataItem.getPkgLevel())
					&& ProductUnit.PP.equals(packagingListDataItem.getPackagingListUnit())
					&& (subProductData instanceof PackagingKitData)) == false)
					&& (formulatedProduct.getDefaultVariantPackagingData() != null)) {
				if (packagingLevel.equals(PackagingLevel.Secondary)) {
					if ((formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes() != null)
							&& (formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes() != 0d)) {
						if (logger.isDebugEnabled()) {
							logger.debug(
									"qty : " + qty + " product per boxes " + formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes());
						}
						qty = qty / formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes();
					}
				} else if (packagingLevel.equals(PackagingLevel.Tertiary)) {
					if ((formulatedProduct.getDefaultVariantPackagingData().getProductPerPallet() != null)
							&& (formulatedProduct.getDefaultVariantPackagingData().getProductPerPallet() != 0d)) {
						if (logger.isDebugEnabled()) {
							logger.debug(
									"qty : " + qty + " product per boxes " + formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes()
											+ " boxes per pallet " + formulatedProduct.getDefaultVariantPackagingData().getBoxesPerPallet());
						}
						qty = qty / formulatedProduct.getDefaultVariantPackagingData().getProductPerPallet();
					}
				}
			}
		}

		return qty;
	}

	public static boolean isCharactFormulatedFromVol(NodeService nodeService, SimpleCharactDataItem sl) {
		if (sl instanceof PhysicoChemListDataItem) {
			Boolean isFormulatedFromVol = (Boolean) nodeService.getProperty(sl.getCharactNodeRef(), PLMModel.PROP_PHYSICO_CHEM_FORMULATED_FROM_VOL);
			return isFormulatedFromVol != null ? isFormulatedFromVol : false;
		}
		return false;
	}

	public static Double getComponentLossPerc(ProductData componentProduct, CompoListDataItem compoListDataItem) {
		if(componentProduct.getComponentLossPerc()!=null) {
			return componentProduct.getComponentLossPerc();
		}
		return  compoListDataItem.getLossPerc() != null ? compoListDataItem.getLossPerc() : 0d;
	}
}
