/*
 * 
 */
package fr.becpg.repo.product.formulation;

import java.math.BigDecimal;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.PLMModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.data.PackagingKitData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ResourceProductData;
import fr.becpg.repo.product.data.constraints.CompoListUnit;
import fr.becpg.repo.product.data.constraints.PackagingListUnit;
import fr.becpg.repo.product.data.constraints.ProcessListUnit;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.TareUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;

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

	private static Log logger = LogFactory.getLog(FormulationHelper.class);

	/**
	 * Gets the qty.
	 *
	 * @param compoListDataItem
	 *            the compo list data item
	 * @return the qty
	 * @throws FormulateException
	 */
	public static Double getQty(CompoListDataItem compoListDataItem) {
		Double qty = null;
		if (FormulationHelper.isCompoUnitLiter(compoListDataItem.getCompoListUnit())) {
			if (compoListDataItem.getQtySubFormula() != null) {
				if (compoListDataItem.getCompoListUnit().equals(CompoListUnit.mL)) {
					qty = compoListDataItem.getQtySubFormula() / 1000;
				} else {
					qty = compoListDataItem.getQtySubFormula();
				}
			}
		} else {
			qty = compoListDataItem.getQty();
		}
		
		return qty != null ? qty : DEFAULT_COMPONANT_QUANTITY;
	}

	public static Double getQtyInKg(CompoListDataItem compoListDataItem) {
		return compoListDataItem.getQty();
	}

	public static Double getYield(CompoListDataItem compoListDataItem) {
		return compoListDataItem.getYieldPerc() != null && compoListDataItem.getYieldPerc() != 0d ? compoListDataItem.getYieldPerc() : DEFAULT_YIELD;
	}

	public static Double getQtySubFormula(CompoListDataItem compoListDataItem, NodeService nodeService) {

		Double qty = compoListDataItem.getQtySubFormula();
		CompoListUnit compoListUnit = compoListDataItem.getCompoListUnit();
		if (qty != null && compoListUnit != null) {

			if (compoListUnit.equals(CompoListUnit.kg)) {
				return qty;
			} else if (compoListUnit.equals(CompoListUnit.g)) {
				return qty / 1000;
			} else if (compoListUnit.equals(CompoListUnit.P)) {
//				Double productQty = null;
//				ProductUnit productUnit = FormulationHelper.getProductUnit(compoListDataItem.getProduct(), nodeService);
//				if (productUnit != null && productUnit.equals(ProductUnit.P)) {
//					productQty = FormulationHelper.getProductQty(compoListDataItem.getProduct(), nodeService);
//				}
//
//				if (productQty == null) {
//					productQty = 1d;
//				}

//				return FormulationHelper.getNetWeight(compoListDataItem.getProduct(), nodeService, FormulationHelper.DEFAULT_NET_WEIGHT) * qty
//						/ productQty;
				
				return FormulationHelper.getNetWeight(compoListDataItem.getProduct(), nodeService, FormulationHelper.DEFAULT_NET_WEIGHT) * qty;
			} else if (compoListUnit.equals(CompoListUnit.L) || compoListUnit.equals(CompoListUnit.mL)) {

				if (compoListUnit.equals(CompoListUnit.mL)) {
					qty = qty / 1000;
				}

				Double overrun = compoListDataItem.getOverrunPerc();
				if (compoListDataItem.getOverrunPerc() == null) {
					overrun = FormulationHelper.DEFAULT_OVERRUN;
				}

				Double density = FormulationHelper.getDensity(compoListDataItem.getProduct(), nodeService);
				if (density == null || density.equals(0d)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Cannot calculate volume since density is null or equals to 0");
					}
				} else {
					return (qty * density * 100) / (100 + overrun);

				}

			}
			return qty;
		}

		return null;
	}

	/**
	 * Gets the qty with lost.
	 *
	 * @param compoListDataItem
	 *            the compo list data item
	 * @return the qty
	 * @throws FormulateException
	 */
	public static Double getQtyWithLost(CompoListDataItem compoListDataItem, Double parentLossRatio) {
		Double lossPerc = compoListDataItem.getLossPerc() != null ? compoListDataItem.getLossPerc() : 0d;
		return FormulationHelper.getQtyWithLost(FormulationHelper.getQty(compoListDataItem),
				FormulationHelper.calculateLossPerc(parentLossRatio, lossPerc));
	}

	public static Double calculateLossPerc(Double parentLossRatio, Double lossPerc) {
		return 100 * ((1 + lossPerc / 100) * (1 + parentLossRatio / 100) - 1);
	}

	public static Double getQtyWithLost(Double qty, Double lossPerc) {
		return (1 + lossPerc / 100) * qty;
	}

	public static Double getQtyWithLost(PackagingListDataItem packagingListDataItem) {
		Double lossPerc = packagingListDataItem.getLossPerc() != null ? packagingListDataItem.getLossPerc() : 0d;
		return FormulationHelper.getQtyWithLost(FormulationHelper.getQty(packagingListDataItem), lossPerc);
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
	 */
	public static Double getQty(ProductData formulatedProduct, ProcessListDataItem processListDataItem) {

		Double qty = 0d;

		if (formulatedProduct instanceof ResourceProductData) {
			if (processListDataItem.getQtyResource() != null) {
				qty = processListDataItem.getQtyResource();
			}
		} else {

			Double productQtyToTransform = FormulationHelper.QTY_FOR_PIECE;
			if (!ProcessListUnit.P.equals(processListDataItem.getUnit())) {
				productQtyToTransform = processListDataItem.getQty() != null ? processListDataItem.getQty() : FormulationHelper.getNetWeight(formulatedProduct, null);
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

		return qty;
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
		return unit != null && (unit.equals(CompoListUnit.kg) || unit.equals(CompoListUnit.g));
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
		if(netWeight != null){
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
						}
						else{
							return FormulationHelper.getQtyFromComposition(productData, productUnit, defaultValue);
						}
					}
					return qty;
				}
				else{
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
	
	private static Double getQtyFromComposition(ProductData formulatedProduct, ProductUnit productUnit, Double defaultValue){
		Double qty = defaultValue;
		if (productUnit != null && FormulationHelper.isProductUnitLiter(productUnit)) {
			if(formulatedProduct.getRecipeVolumeUsed() != null){
				qty = formulatedProduct.getRecipeVolumeUsed();
			}
		}
		else if(formulatedProduct.getRecipeQtyUsed() != null){
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

	public static Double getNetVolume(CompoListDataItem compoListDataItem, NodeService nodeService) throws FormulateException {

		Double qty = FormulationHelper.getQtyInKg(compoListDataItem);
		if (qty != null) {
			Double overrun = compoListDataItem.getOverrunPerc();
			if (compoListDataItem.getOverrunPerc() == null) {
				overrun = FormulationHelper.DEFAULT_OVERRUN;
			}
			Double density = FormulationHelper.getDensity(compoListDataItem.getProduct(), nodeService);
			if (density == null || density.equals(0d)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Cannot calculate volume since density is null or equals to 0");
				}
			} else {
				return (100 + overrun) * qty / (density * 100);
			}
		}

		return null;
	}

	public static Double calculateValue(Double totalValue, Double qtyUsed, Double value, Double netWeight) {

		if (totalValue == null && value == null) {
			return null;
		}

		totalValue = totalValue != null ? totalValue : 0d;
		value = value != null ? value : 0d;
		totalValue += qtyUsed * value / netWeight;
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
					}
				}
				logger.debug("compo tare: " + tare + " qty " + qty + " productQty " + productQty);
				return tare.multiply(new BigDecimal(qty)).divide(new BigDecimal(productQty));
			}
		}
		return new BigDecimal(0d);
	}

	public static BigDecimal getTareInKg(PackagingListDataItem packList, NodeService nodeService) {

		BigDecimal tare = new BigDecimal(0d);
		Double qty = FormulationHelper.getQty(packList);

		if (qty != null) {
			if (FormulationHelper.isPackagingListUnitKg(packList.getPackagingListUnit())) {
				tare = new BigDecimal(qty);
			} else {
				BigDecimal t = FormulationHelper.getTareInKg(packList.getProduct(), nodeService);
				if (t != null) {
					tare =  t.multiply(new BigDecimal(qty));
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
		BigDecimal ret = new BigDecimal(tare);
		if (tare == null || tareUnit == null) {
			return null;
		} else {
			if (tareUnit == TareUnit.g) {
				ret = ret.divide(new BigDecimal(1000d));
			}
			return ret;
		}
	}

	public static Double getVolumeReconstitution(CompoListDataItem compoListDataItem, NodeService nodeService) {
		if (nodeService.hasAspect(compoListDataItem.getProduct(), PLMModel.ASPECT_RECONSTITUTABLE)) {
			Double reconstitionRate = (Double) nodeService.getProperty(compoListDataItem.getProduct(), PLMModel.PROP_RECONSTITUTION_RATE);
			if (reconstitionRate != null) {
				return compoListDataItem.getVolume() * reconstitionRate;
			}
		}
		return null;
	}

}
