/*
 *
 */
package fr.becpg.repo.product.formulation;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.ClientData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.SupplierData;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.repository.model.SimpleListDataItem;

/**
 * The Class CostCalculatingVisitor.
 *
 * @author querephi
 * @version $Id: $Id
 */
public class CostsCalculatingFormulationHandler extends AbstractCostCalculatingFormulationHandler<CostListDataItem> {

	private static final String MESSAGE_FORMULATE_COST_LIST_ERROR = "message.formulate.costList.error";
	private static final Log logger = LogFactory.getLog(CostsCalculatingFormulationHandler.class);

	/** {@inheritDoc} */
	@Override
	protected Class<CostListDataItem> getInstanceClass() {
		return CostListDataItem.class;
	}

	/** {@inheritDoc} */
	@Override
	protected List<CostListDataItem> getDataListVisited(ProductData partProduct) {
		return partProduct.getCostList();
	}

	/** {@inheritDoc} */
	@Override
	protected List<CostListDataItem> getDataListVisited(ClientData client) {
		return client.getCostList();
	}

	/** {@inheritDoc} */
	@Override
	protected List<CostListDataItem> getDataListVisited(SupplierData supplier) {
		return supplier.getCostList();
	}

	/** {@inheritDoc} */
	@Override
	protected void afterProcess(ProductData formulatedProduct) {
		calculateProfitability(formulatedProduct);
	}

	/** {@inheritDoc} */
	@Override
	protected boolean accept(ProductData formulatedProduct) {
		return !(formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL) || (formulatedProduct instanceof ProductSpecificationData)
				|| ((formulatedProduct.getCostList() == null) && !alfrescoRepository.hasDataList(formulatedProduct, PLMModel.TYPE_COSTLIST)));
	}
	
	/** {@inheritDoc} */
	@Override
	protected Double extractValue(ProductData formulatedProduct, ProductData partProduct, SimpleListDataItem slDataItem) {
		return CostCalculatingHelper.extractValue(formulatedProduct, partProduct, slDataItem);
	}

	/** {@inheritDoc} */
	@Override
	protected RequirementDataType getRequirementDataType() {
		return RequirementDataType.Cost;
	}
	
	/** {@inheritDoc} */
	@Override
	protected String getFormulationErrorMessage() {
		return MESSAGE_FORMULATE_COST_LIST_ERROR;
	}

	private void calculateProfitability(ProductData formulatedProduct) {
	
		Double netQty = FormulationHelper.getNetQtyInLorKg(formulatedProduct, 1d);
		if ((formulatedProduct.getUnit() != null) && formulatedProduct.getUnit().isLb()) {
			netQty = ProductUnit.kgToLb(netQty);
		}
	
		Double unitTotalVariableCost = 0d;// for 1 product
		Double previousTotalVariableCost = 0d;
		Double futureTotalVariableCost = 0d;
		double unitTotalFixedCost = 0d;
	
		for (CostListDataItem c : formulatedProduct.getCostList()) {
	
			if (c.getCost() != null) {
	
				Boolean isFixed = (Boolean) nodeService.getProperty(c.getCost(), PLMModel.PROP_COSTFIXED);
				Double costPerProduct = null;
				Double previousCostPerProduct = null;
				Double futureCostPerProduct = null;
				Double futureCostPerProduct2 = null;
				Double futureCostPerProduct3 = null;
				Double futureCostPerProduct4 = null;
				String costCurrency = (String) nodeService.getProperty(c.getCost(), PLMModel.PROP_COSTCURRENCY);
				String productCurrency = (String) nodeService.getProperty(formulatedProduct.getNodeRef(), PLMModel.PROP_PRICE_CURRENCY);
	
				if (c.getValue() != null) {
					if (Boolean.TRUE.equals(isFixed)) {
						unitTotalFixedCost += c.getValue();
	
						if ((formulatedProduct.getProjectedQty() != null) && !formulatedProduct.getProjectedQty().equals(0l)) {
							costPerProduct = c.getValue() / formulatedProduct.getProjectedQty();
	
							if (c.getFutureValue() != null) {
								futureCostPerProduct = c.getFutureValue() / formulatedProduct.getProjectedQty();
							}
							
							if (c.getFutureValue2() != null) {
								futureCostPerProduct2 = c.getFutureValue2() / formulatedProduct.getProjectedQty();
							}
							
							if (c.getFutureValue3() != null) {
								futureCostPerProduct3 = c.getFutureValue3() / formulatedProduct.getProjectedQty();
							}
							
							if (c.getFutureValue4() != null) {
								futureCostPerProduct4 = c.getFutureValue4() / formulatedProduct.getProjectedQty();
							}
	
							if (c.getPreviousValue() != null) {
								previousCostPerProduct = c.getPreviousValue() / formulatedProduct.getProjectedQty();
							}
	
						}
	
					} else if ((formulatedProduct.getUnit() != null) && formulatedProduct.getUnit().isP()) {
						costPerProduct = c.getValue();
	
						if (c.getFutureValue() != null) {
							futureCostPerProduct = c.getFutureValue();
						}
						
						if (c.getFutureValue2() != null) {
							futureCostPerProduct2 = c.getFutureValue2();
						}
						
						if (c.getFutureValue3() != null) {
							futureCostPerProduct3 = c.getFutureValue3();
						}
						
						if (c.getFutureValue4() != null) {
							futureCostPerProduct4 = c.getFutureValue4();
						}
	
						if (c.getPreviousValue() != null) {
							previousCostPerProduct = c.getPreviousValue();
						}
	
						if (formulatedProduct.getQty() != null) {
							if (costPerProduct != null) {
								costPerProduct *= formulatedProduct.getQty();
							}
							if (futureCostPerProduct != null) {
								futureCostPerProduct *= formulatedProduct.getQty();
							}
							if (futureCostPerProduct2 != null) {
								futureCostPerProduct2 *= formulatedProduct.getQty();
							}
							if (futureCostPerProduct3 != null) {
								futureCostPerProduct3 *= formulatedProduct.getQty();
							}
							if (futureCostPerProduct4 != null) {
								futureCostPerProduct4 *= formulatedProduct.getQty();
							}
							if (previousCostPerProduct != null) {
								previousCostPerProduct *= formulatedProduct.getQty();
							}
						}
	
					} else {
	
						costPerProduct = netQty * c.getValue();
	
						if (c.getFutureValue() != null) {
							futureCostPerProduct = netQty * c.getFutureValue();
						}
						
						if (c.getFutureValue2() != null) {
							futureCostPerProduct2 = netQty * c.getFutureValue2();
						}
						
						if (c.getFutureValue3() != null) {
							futureCostPerProduct3 = netQty * c.getFutureValue3();
						}
						
						if (c.getFutureValue4() != null) {
							futureCostPerProduct4 = netQty * c.getFutureValue4();
						}
	
						if (c.getPreviousValue() != null) {
							previousCostPerProduct = netQty * c.getPreviousValue();
						}
	
					}
				}
	
				boolean isCostForUnitTotalCost = ((c.getDepthLevel() == null) || (c.getDepthLevel() == 1))
						&& ((productCurrency == null) || (costCurrency == null) || productCurrency.equals(costCurrency));
				c.setValuePerProduct(null);
				if (costPerProduct != null) {
					if (isCostForUnitTotalCost) {
						unitTotalVariableCost += costPerProduct;
					}
					c.setValuePerProduct(costPerProduct);
				}
	
				c.setFutureValuePerProduct(null);
				if (futureCostPerProduct != null) {
					if (isCostForUnitTotalCost) {
						futureTotalVariableCost += futureCostPerProduct;
					}
					c.setFutureValuePerProduct(futureCostPerProduct);
				}
				
				c.setFutureValuePerProduct2(null);
				if (futureCostPerProduct2 != null) {
					c.setFutureValuePerProduct2(futureCostPerProduct2);
				}
				
				c.setFutureValuePerProduct3(null);
				if (futureCostPerProduct3 != null) {
					c.setFutureValuePerProduct3(futureCostPerProduct3);
				}
				
				c.setFutureValuePerProduct4(null);
				if (futureCostPerProduct4 != null) {
					c.setFutureValuePerProduct4(futureCostPerProduct4);
				}
	
				c.setPreviousValuePerProduct(null);
				if (previousCostPerProduct != null) {
					if (isCostForUnitTotalCost) {
						previousTotalVariableCost += previousCostPerProduct;
					}
					c.setPreviousValuePerProduct(previousCostPerProduct);
				}
			}
		}
	
		formulatedProduct.setUnitTotalCost(unitTotalVariableCost);
		formulatedProduct.setPreviousUnitTotalCost(previousTotalVariableCost);
		formulatedProduct.setFutureUnitTotalCost(futureTotalVariableCost);
	
		if ((formulatedProduct.getUnitPrice() != null) && formulatedProduct.getUnitPrice()!=0d && (formulatedProduct.getUnitTotalCost() != null)) {
	
			// profitability
			double profit = formulatedProduct.getUnitPrice() - formulatedProduct.getUnitTotalCost();
			Double profitability = (100 * profit) / formulatedProduct.getUnitPrice();
			logger.debug("profitability: " + profitability);
			formulatedProduct.setProfitability(profitability);
	
			// breakEven
			if (profit > 0) {
	
				Long breakEven = Math.round(unitTotalFixedCost / profit);
				formulatedProduct.setBreakEven(breakEven);
			} else {
				formulatedProduct.setBreakEven(null);
			}
	
		} else {
			formulatedProduct.setProfitability(null);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void setDataListVisited(ProductData formulatedProduct) {
		formulatedProduct.setCostList(new LinkedList<>());
	}

	/** {@inheritDoc} */
	@Override
	protected QName getCostFormulaPropName() {
		return PLMModel.PROP_COST_FORMULA;
	}

	/** {@inheritDoc} */
	@Override
	protected QName getCostFixedPropName() {
		return PLMModel.PROP_COSTFIXED;
	}

	/** {@inheritDoc} */
	@Override
	protected QName getCostUnitPropName() {
		return PLMModel.PROP_COSTCURRENCY;
	}


	/** {@inheritDoc} */
	@Override
	protected CostListDataItem newSimpleListDataItem(NodeRef charactNodeRef) {
		CostListDataItem costListDataItem = new CostListDataItem();
		costListDataItem.setCharactNodeRef(charactNodeRef);
		return costListDataItem;
	}

}
