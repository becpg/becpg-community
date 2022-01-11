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

import fr.becpg.model.PLMModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.helper.MLTextHelper;
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
import fr.becpg.repo.repository.model.SimpleCharactDataItem;

/**
 * The Class FormulationHelper.
 *
 * @author querephi
 * @version $Id: $Id
 */
public class FormulationHelper {

	/** Constant <code>DEFAULT_NET_WEIGHT</code> */
	public static final Double DEFAULT_NET_WEIGHT = 0d;

	/** Constant <code>DEFAULT_COMPONANT_QUANTITY</code> */
	public static final Double DEFAULT_COMPONANT_QUANTITY = 0d;

	/** Constant <code>DEFAULT_DENSITY</code> */
	public static final Double DEFAULT_DENSITY = 1d;

	/** Constant <code>QTY_FOR_PIECE</code> */
	public static final Double QTY_FOR_PIECE = 1d;

	/** Constant <code>DEFAULT_YIELD</code> */
	public static final Double DEFAULT_YIELD = 100d;

	/** Constant <code>DEFAULT_OVERRUN</code> */
	public static final Double DEFAULT_OVERRUN = 0d;

	/** Constant <code>MISSING_NUMBER_OF_PRODUCT_PER_BOX="message.formulate.missing.numberOfProdu"{trunked}</code> */
	public static final String MISSING_NUMBER_OF_PRODUCT_PER_BOX = "message.formulate.missing.numberOfProductPerBox";

	private static final Log logger = LogFactory.getLog(FormulationHelper.class);

	private FormulationHelper() {
		// Private
	}
	
	/**
	 * <p>getQtyInKg.</p>
	 *
	 * @param compoListDataItem a {@link fr.becpg.repo.product.data.productList.CompoListDataItem} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double getQtyInKg(CompoListDataItem compoListDataItem) {
		if ((compoListDataItem.getQty() == null) || compoListDataItem.getQty().isNaN() || compoListDataItem.getQty().isInfinite()) {
			compoListDataItem.setQty(DEFAULT_COMPONANT_QUANTITY);
		}
		return compoListDataItem.getQty();
	}

	/**
	 * <p>getYield.</p>
	 *
	 * @param compoListDataItem a {@link fr.becpg.repo.product.data.productList.CompoListDataItem} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double getYield(CompoListDataItem compoListDataItem) {
		return (compoListDataItem.getYieldPerc() != null) && (compoListDataItem.getYieldPerc() != 0d) && !compoListDataItem.getYieldPerc().isNaN()
				&& !compoListDataItem.getYieldPerc().isInfinite() ? compoListDataItem.getYieldPerc() : DEFAULT_YIELD;
	}

	/**
	 * <p>getQtyForCost.</p>
	 *
	 * @param compoListDataItem a {@link fr.becpg.repo.product.data.productList.CompoListDataItem} object.
	 * @param parentLossRatio a {@link java.lang.Double} object.
	 * @param componentProduct a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param keepProductUnit a boolean.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double getQtyForCost(CompoListDataItem compoListDataItem, Double parentLossRatio, ProductData componentProduct,
			boolean keepProductUnit) {
		Double lossPerc = FormulationHelper.calculateLossPerc(parentLossRatio != null ? parentLossRatio : 0d,
				FormulationHelper.getComponentLossPerc(componentProduct, compoListDataItem));
		Double yieldPerc = compoListDataItem.getYieldPerc() != null ? compoListDataItem.getYieldPerc() : 100d;
		Double qtySubFormula = compoListDataItem.getQtySubFormula() != null ? compoListDataItem.getQtySubFormula() : DEFAULT_COMPONANT_QUANTITY;
		Double qtyInKg = compoListDataItem.getQty() != null ? compoListDataItem.getQty() : DEFAULT_COMPONANT_QUANTITY;
		Double qtyInL = compoListDataItem.getVolume() != null ? compoListDataItem.getVolume() : DEFAULT_COMPONANT_QUANTITY;
		ProductUnit compoListUnit = compoListDataItem.getCompoListUnit();
		ProductUnit componentProductUnit = componentProduct.getUnit();
		Double componentNetWeight = getNetWeight(componentProduct, DEFAULT_NET_WEIGHT);

		Double unitFactor = 1d;
		if (componentProductUnit != null && (componentProductUnit.isWeight() || componentProductUnit.isVolume())) {
			if (keepProductUnit) {
				unitFactor = componentProductUnit.getUnitFactor();
			} else if (componentProductUnit.isLb()) {
				unitFactor = ProductUnit.lb.getUnitFactor();
			} else if (componentProductUnit.isGal()) {
				unitFactor = ProductUnit.gal.getUnitFactor();
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

	/**
	 * <p>calculateLossPerc.</p>
	 *
	 * @param parentLossRatio a {@link java.lang.Double} object.
	 * @param lossPerc a {@link java.lang.Double} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double calculateLossPerc(Double parentLossRatio, Double lossPerc) {
		if (parentLossRatio == null) {
			parentLossRatio = 0d;
		}
		if (lossPerc == null) {
			lossPerc = 0d;
		}
		return 100 * (((1 + (lossPerc / 100)) * (1 + (parentLossRatio / 100))) - 1);
	}

	private static Double getQtyWithLossAndYield(double qty, double lossPerc, double yieldPerc) {
		return ((1 + (lossPerc / 100)) * qty) / (yieldPerc / 100);
	}

	static Double getQtyWithLoss(double qty, double lossPerc) {
		return (1 + (lossPerc / 100)) * qty;
	}

	/**
	 * <p>getQtyForCost.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param packagingListDataItem a {@link fr.becpg.repo.product.data.productList.PackagingListDataItem} object.
	 * @param subProductData a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double getQtyForCost(ProductData formulatedProduct, PackagingListDataItem packagingListDataItem, ProductData subProductData) {
		Double lossPerc = packagingListDataItem.getLossPerc() != null ? packagingListDataItem.getLossPerc() : 0d;
		lossPerc = calculateLossPerc(formulatedProduct.getProductLossPerc(), lossPerc);
		return FormulationHelper.getQtyWithLoss(FormulationHelper.getQty(packagingListDataItem, subProductData), lossPerc);
	}

	/**
	 * Gets the qty of a packaging item
	 *
	 * @param packagingListDataItem a {@link fr.becpg.repo.product.data.productList.PackagingListDataItem} object.
	 * @param subProductData a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double getQty(PackagingListDataItem packagingListDataItem, ProductData subProductData) {

		if (packagingListDataItem.getQty() == null) {
			logger.warn("Packaging element doesn't have any quantity");
		}

		Double qty = packagingListDataItem.getQty() != null ? packagingListDataItem.getQty() : DEFAULT_COMPONANT_QUANTITY;
		ProductUnit packagingListUnit = packagingListDataItem.getPackagingListUnit();

		if ((qty > 0) && (packagingListUnit != null)) {
			if (packagingListUnit.equals(ProductUnit.PP)) {
				qty = 1 / qty;
			} else {
				// Convert qty in L M KG or P
				qty = qty / packagingListUnit.getUnitFactor();

				ProductUnit productUnit = subProductData.getUnit();
				// Convert cost or tare in product Unit
				if (productUnit != null) {
					qty = qty * productUnit.getUnitFactor();
				}

			}

		}

		return qty;
	}

	/**
	 * Gets the qty of a process item
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param processListDataItem a {@link fr.becpg.repo.product.data.productList.ProcessListDataItem} object.
	 * @return a {@link java.lang.Double} object.
	 * @throws fr.becpg.repo.formulation.FormulateException if any.
	 */
	public static Double getQty(ProductData formulatedProduct, ProcessListDataItem processListDataItem) {

		Double qty = 0d;

		if (formulatedProduct instanceof ResourceProductData) {
			qty = QTY_FOR_PIECE;
		} else {
			Double productQtyToTransform = FormulationHelper.QTY_FOR_PIECE;
			if ((processListDataItem.getUnit() != null) && (processListDataItem.getUnit().isWeight() || processListDataItem.getUnit().isVolume())) {
				productQtyToTransform = processListDataItem.getQty() != null ? processListDataItem.getQty()
						: FormulationHelper.getNetWeight(formulatedProduct, null);

			}

			if (ProductUnit.Box.equals(processListDataItem.getUnit())) {
				if ((formulatedProduct.getDefaultVariantPackagingData() != null)
						&& (formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes() != null)) {
					productQtyToTransform = productQtyToTransform / formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes();
				} else {
					formulatedProduct.getReqCtrlList()
							.add(new ReqCtrlListDataItem(null, RequirementType.Forbidden,
									MLTextHelper.getI18NMessage(MISSING_NUMBER_OF_PRODUCT_PER_BOX), null, new ArrayList<>(),
									RequirementDataType.Packaging));
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


	/**
	 * <p>getNetWeight.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 * @param defaultValue a {@link java.lang.Double} object.
	 * @return a {@link java.lang.Double} object.
	 */
	// Warning Slower than ProductData variant
	public static Double getNetWeight(NodeRef nodeRef, NodeService nodeService, Double defaultValue) {

		Double netWeight = (Double) nodeService.getProperty(nodeRef, PLMModel.PROP_PRODUCT_NET_WEIGHT);

		if (netWeight != null) {
			return netWeight;
		} else {
			Double qty = (Double) nodeService.getProperty(nodeRef, PLMModel.PROP_PRODUCT_QTY);
			String strProductUnit = (String) nodeService.getProperty(nodeRef, PLMModel.PROP_PRODUCT_UNIT);
			ProductUnit productUnit = strProductUnit != null ? ProductUnit.valueOf(strProductUnit) : null;
			if (productUnit != null && (productUnit.isWeight() || productUnit.isVolume()) && qty != null) {

				qty = qty / productUnit.getUnitFactor();
				if (productUnit.isVolume()) {
					Double density = (Double) nodeService.getProperty(nodeRef, PLMModel.PROP_PRODUCT_DENSITY);
					if (density != null) {
						qty = qty * density;
					} else {
						qty = 0d;
					}
				}
				return qty;
			}
		}

		netWeight = (Double) nodeService.getProperty(nodeRef, PLMModel.PROP_PRODUCT_COMPO_QTY_USED);
		if (netWeight != null) {
			return netWeight;
		}

		return defaultValue;
	}

	/**
	 * <p>getNetWeight.</p>
	 *
	 * @param productData a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param defaultValue a {@link java.lang.Double} object.
	 * @return a {@link java.lang.Double} object.
	 */
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

	/**
	 * <p>getNetQtyInLorKg.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param defaultValue a {@link java.lang.Double} object.
	 * @return a {@link java.lang.Double} object.
	 */
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

	/**
	 * <p>getNetQtyForNuts.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double getNetQtyForNuts(ProductData formulatedProduct) {
		if (formulatedProduct.isLiquid()) {
			return FormulationHelper.getNetVolume(formulatedProduct, FormulationHelper.DEFAULT_NET_WEIGHT);
		} else {
			return FormulationHelper.getNetWeight(formulatedProduct, FormulationHelper.DEFAULT_NET_WEIGHT);
		}
	}

	/**
	 * <p>getServingSizeInLorKg.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double getServingSizeInLorKg(ProductData formulatedProduct) {
		if ((formulatedProduct.getServingSize() != null) && (formulatedProduct.getServingSizeUnit() != null)) {
			return (formulatedProduct.getServingSize() / formulatedProduct.getServingSizeUnit().getUnitFactor());
		} else if (formulatedProduct.getServingSize() != null) {
			return formulatedProduct.getServingSize() / 1000d;
		}
		return null;
	}

	/**
	 * <p>getQtyFromComposition.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param defaultValue a {@link java.lang.Double} object.
	 * @return a {@link java.lang.Double} object.
	 */
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

	/**
	 * <p>getNetVolume.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param defaultValue a {@link java.lang.Double} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double getNetVolume(ProductData formulatedProduct, Double defaultValue) {
		if ((formulatedProduct.getNetVolume() != null) && (formulatedProduct.getNetVolume() > 0)) {
			return formulatedProduct.getNetVolume();
		}

		Double qty = formulatedProduct.getQty();
		if (qty == null) {
			if (formulatedProduct.getRecipeVolumeUsed() != null) {
				return formulatedProduct.getRecipeVolumeUsed();
			}

		} else {
			ProductUnit productUnit = formulatedProduct.getUnit();
			if ((productUnit != null) && productUnit.isVolume()) {
				return qty / productUnit.getUnitFactor();
			}

		}
		return defaultValue;
	}

	private static Double getNetVolume(Double qty, CompoListDataItem compoListDataItem, ProductData subProductData) {

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
			Double density = subProductData.getDensity();
			if ((density == null) || density.equals(0d) || density.isNaN() || density.isInfinite()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Use default density");
				}
				density = DEFAULT_DENSITY;
			}
			return ((100 + overrun) * (yield / 100) * qty) / (density * 100);
		}

		return null;
	}

	/**
	 * <p>getNetVolume.</p>
	 *
	 * @param compoListDataItem a {@link fr.becpg.repo.product.data.productList.CompoListDataItem} object.
	 * @param subProductData a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double getNetVolume(CompoListDataItem compoListDataItem, ProductData subProductData) {

		Double qty = FormulationHelper.getQtyInKg(compoListDataItem);

		return getNetVolume(qty, compoListDataItem, subProductData);
	}

	/**
	 * <p>calculateValue.</p>
	 *
	 * @param totalValue a {@link java.lang.Double} object.
	 * @param qtyUsed a {@link java.lang.Double} object.
	 * @param value a {@link java.lang.Double} object.
	 * @param netWeight a {@link java.lang.Double} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double calculateValue(Double totalValue, Double qtyUsed, Double value, Double netWeight) {

		if ((totalValue == null) && (value == null)) {
			return null;
		}

		totalValue = totalValue != null ? totalValue : 0d;
		value = value != null ? value : 0d;
		value = value * qtyUsed;
		if ((netWeight != null) && (netWeight != 0d)) {
			value = value / netWeight;
		}

		return totalValue + value;
	}

	/**
	 * <p>getTareInKg.</p>
	 *
	 * @param compoList a {@link fr.becpg.repo.product.data.productList.CompoListDataItem} object.
	 * @param subProduct a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @return a {@link java.math.BigDecimal} object.
	 */
	public static BigDecimal getTareInKg(CompoListDataItem compoList, ProductData subProduct) {

		ProductUnit compoListUnit = compoList.getCompoListUnit();
		Double qty = compoList.getQtySubFormula();

		if ((subProduct != null) && (compoListUnit != null) && (qty != null)) {
			BigDecimal tare = FormulationHelper.getTareInKg(subProduct);
			if ((tare != null) && (tare.doubleValue() != 0d)) {
				Double productQty = subProduct.getQty();
				if (productQty == null) {
					productQty = 1d;
				}

				if (compoListUnit.isP()) {
					if ((subProduct.getUnit() != null) && !subProduct.getUnit().isP()) {
						productQty = 1d;
					}

				} else if (compoListUnit.isWeight() || compoListUnit.isVolume() || compoListUnit.isPerc()) {

					productQty = getNetQtyInLorKg(subProduct, 1d);
					qty = getQtyInKg(compoList);

				}

				if ((qty != null) && !qty.isNaN() && !qty.isInfinite()) {
					if (logger.isDebugEnabled()) {
						logger.debug("Compo tare [" + subProduct.getName() + "]: " + tare + " qty " + qty + " productQty " + productQty);
					}
					if ((productQty != null) && !productQty.isNaN() && !productQty.isInfinite() && (productQty != 0d)) {
						return tare.multiply(BigDecimal.valueOf(qty)).divide(BigDecimal.valueOf(productQty), MathContext.DECIMAL64);
					} else {
						return tare.multiply(BigDecimal.valueOf(qty));
					}
				} else {
					logger.error("Qty/ProductQty is NaN or 0 or infinite:" + qty + " " + productQty + " for " + compoList.getProduct());
				}
			}

		}

		return BigDecimal.valueOf(0d);
	}

	/**
	 * <p>getTareInKg.</p>
	 *
	 * @param packList a {@link fr.becpg.repo.product.data.productList.PackagingListDataItem} object.
	 * @param subProductData a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @return a {@link java.math.BigDecimal} object.
	 */
	public static BigDecimal getTareInKg(PackagingListDataItem packList, ProductData subProductData) {

		BigDecimal tare = BigDecimal.valueOf(0d);
		Double qty = FormulationHelper.getQty(packList, subProductData);

		if ((qty != null) && !qty.isNaN() && !qty.isInfinite()) {
			if ((packList.getPackagingListUnit() != null) && packList.getPackagingListUnit().isWeight()) {
				tare = BigDecimal.valueOf(qty);
			} else {
				BigDecimal t = FormulationHelper.getTareInKg(subProductData);
				if (t != null) {
					tare = t.multiply(BigDecimal.valueOf(qty));
				}
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Pack tare [" + subProductData.getName() + "] (" + packList.getPackagingListUnit() + ")" + tare + " qty " + qty
					+ " tareUnit " + FormulationHelper.getTareInKg(subProductData));
		}
		return tare;
	}

	/**
	 * <p>getTareInKg.</p>
	 *
	 * @param productData a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @return a {@link java.math.BigDecimal} object.
	 */
	public static BigDecimal getTareInKg(ProductData productData) {

		Double tare = productData.getTare();
		TareUnit tareUnit = productData.getTareUnit();
		if ((tare == null) || (tareUnit == null)) {
			return null;
		} else {
			return FormulationHelper.getTareInKg(tare, tareUnit);
		}
	}

	/**
	 * <p>getTareInKg.</p>
	 *
	 * @param tare a {@link java.lang.Double} object.
	 * @param tareUnit a {@link fr.becpg.repo.product.data.constraints.TareUnit} object.
	 * @return a {@link java.math.BigDecimal} object.
	 */
	public static BigDecimal getTareInKg(Double tare, TareUnit tareUnit) {
		if ((tare == null) || (tareUnit == null)) {
			return null;
		} else {
			return (BigDecimal.valueOf(tare)).divide(BigDecimal.valueOf(tareUnit.getUnitFactor()), MathContext.DECIMAL64);
		}
	}

	/**
	 * <p>getNetQtyForCost.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @return a {@link java.lang.Double} object.
	 */
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

	/**
	 * <p>getQtyForCostByPackagingLevel.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param packagingListDataItem a {@link fr.becpg.repo.product.data.productList.PackagingListDataItem} object.
	 * @param subProductData a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double getQtyForCostByPackagingLevel(ProductData formulatedProduct, PackagingListDataItem packagingListDataItem,
			ProductData subProductData) {

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
			} else if ((
					!(subProductData.getAspects().contains(PackModel.ASPECT_PALLET)
					&& PackagingLevel.Secondary.equals(packagingListDataItem.getPkgLevel())
					&& ProductUnit.PP.equals(packagingListDataItem.getPackagingListUnit()) && (subProductData instanceof PackagingKitData)) )
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
				} else if (packagingLevel.equals(PackagingLevel.Tertiary)
						&& ((formulatedProduct.getDefaultVariantPackagingData().getProductPerPallet() != null)
								&& (formulatedProduct.getDefaultVariantPackagingData().getProductPerPallet() != 0d))) {
					if (logger.isDebugEnabled()) {
						logger.debug("qty : " + qty + " product per boxes " + formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes()
								+ " boxes per pallet " + formulatedProduct.getDefaultVariantPackagingData().getBoxesPerPallet());
					}
					qty = qty / formulatedProduct.getDefaultVariantPackagingData().getProductPerPallet();
				}

			}
		}

		return qty;
	}

	/**
	 * <p>isCharactFormulatedFromVol.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 * @param sl a {@link fr.becpg.repo.repository.model.SimpleCharactDataItem} object.
	 * @return a boolean.
	 */
	public static boolean isCharactFormulatedFromVol(NodeService nodeService, SimpleCharactDataItem sl) {
		if (sl instanceof PhysicoChemListDataItem) {
			Boolean isFormulatedFromVol = (Boolean) nodeService.getProperty(sl.getCharactNodeRef(), PLMModel.PROP_PHYSICO_CHEM_FORMULATED_FROM_VOL);
			return Boolean.TRUE.equals(isFormulatedFromVol);
		}
		return false;
	}

	/**
	 * <p>getComponentLossPerc.</p>
	 *
	 * @param componentProduct a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param compoListDataItem a {@link fr.becpg.repo.product.data.productList.CompoListDataItem} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double getComponentLossPerc(ProductData componentProduct, CompoListDataItem compoListDataItem) {
		if (compoListDataItem.getLossPerc() != null) {
			return compoListDataItem.getLossPerc();
		}
		return componentProduct.getComponentLossPerc() != null ? componentProduct.getComponentLossPerc() : 0d;
	}

	/**
	 * <p>flatPercValue.</p>
	 *
	 * @param formulatedValue a {@link java.lang.Double} object.
	 * @param unit a {@link java.lang.String} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double flatPercValue(Double formulatedValue, String unit) {
		if ((formulatedValue != null && formulatedValue > 100d) && (unit != null) && (unit.equals("%") || unit.equals("Perc"))) {
			return 100d;
		}
		return formulatedValue;
	}
}
