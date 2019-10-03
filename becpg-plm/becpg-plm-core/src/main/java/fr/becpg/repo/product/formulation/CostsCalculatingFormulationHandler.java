/*
 *
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.data.ClientData;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.PackagingKitData;
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.SupplierData;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.helper.SimulationCostHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.variant.filters.VariantFilters;

/**
 * The Class CostCalculatingVisitor.
 *
 * @author querephi
 */
public class CostsCalculatingFormulationHandler extends AbstractSimpleListFormulationHandler<CostListDataItem> {

	private static final Log logger = LogFactory.getLog(CostsCalculatingFormulationHandler.class);

	private EntityTplService entityTplService;

	private PackagingHelper packagingHelper;

	private AlfrescoRepository<ProductData> alfrescoRepositoryProductData;

	public static boolean keepProductUnit = false;

	public void setKeepProductUnit(boolean keepProductUnit) {
		CostsCalculatingFormulationHandler.keepProductUnit = keepProductUnit;
	}

	public void setEntityTplService(EntityTplService entityTplService) {
		this.entityTplService = entityTplService;
	}

	public void setPackagingHelper(PackagingHelper packagingHelper) {
		this.packagingHelper = packagingHelper;
	}

	public void setAlfrescoRepositoryProductData(AlfrescoRepository<ProductData> alfrescoRepositoryProductData) {
		this.alfrescoRepositoryProductData = alfrescoRepositoryProductData;
	}

	@Override
	protected Class<CostListDataItem> getInstanceClass() {

		return CostListDataItem.class;
	}

	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {

		if (accept(formulatedProduct)) {
			logger.debug("Cost calculating visitor");

			if (formulatedProduct.getCostList() == null) {
				formulatedProduct.setCostList(new LinkedList<CostListDataItem>());
			}

			if (formulatedProduct.getDefaultVariantPackagingData() == null) {
				formulatedProduct.setDefaultVariantPackagingData(packagingHelper.getDefaultVariantPackagingData(formulatedProduct));
			}

			boolean hasCompoEl = formulatedProduct.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))
					|| formulatedProduct.hasPackagingListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))
					|| formulatedProduct.hasProcessListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE));

			formulateSimpleList(formulatedProduct, formulatedProduct.getCostList(), hasCompoEl);

			// simulation: take in account cost of components defined on
			// formulated product

			if (hasCompoEl) {
				calculateSimulationCosts(formulatedProduct);
			}

			if (formulatedProduct.getCostList() != null) {

				computeFormulatedList(formulatedProduct, formulatedProduct.getCostList(), PLMModel.PROP_COST_FORMULA,
						"message.formulate.costList.error");

				ProductUnit unit = formulatedProduct.getUnit();
				
				for (CostListDataItem c : formulatedProduct.getCostList()) {
					if(unit!=null) {
						Boolean fixed = (Boolean) nodeService.getProperty(c.getCost(), PLMModel.PROP_COSTFIXED);
						
						c.setUnit(calculateUnit(unit, (String) nodeService.getProperty(c.getCost(), PLMModel.PROP_COSTCURRENCY),fixed));
	
						if(!fixed && hasCompoEl) {
							//TODO keepProductUnit bug ici si produit en g le coût est en kg
							
							if (!keepProductUnit && unit.isLb() ) { 
								c.setValue(ProductUnit.lbToKg(c.getValue()));
								c.setMaxi(ProductUnit.lbToKg(c.getMaxi()));
								c.setPreviousValue(ProductUnit.lbToKg(c.getPreviousValue()));
								c.setFutureValue(ProductUnit.lbToKg(c.getFutureValue()));
							} else if(!keepProductUnit && unit.isGal()) {
								c.setValue(ProductUnit.GalToL(c.getValue()));
								c.setMaxi(ProductUnit.GalToL(c.getMaxi()));
								c.setPreviousValue(ProductUnit.GalToL(c.getPreviousValue()));
								c.setFutureValue(ProductUnit.GalToL(c.getFutureValue()));
							}
						}
					}
					
					if (transientFormulation) {
						c.setTransient(true);
					}
				}
			}

			Composite<CostListDataItem> composite = CompositeHelper.getHierarchicalCompoList(formulatedProduct.getCostList());
			calculateParentCost(formulatedProduct, composite);

			// profitability
			calculateProfitability(formulatedProduct);
		}
		return true;
	}

	@Override
	protected void visitChildren(ProductData formulatedProduct, List<CostListDataItem> costList) throws FormulateException {

		Double netQty = FormulationHelper.getNetQtyForCost(formulatedProduct);

		if (formulatedProduct.hasCompoListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {

			/*
			 * Composition
			 */
			Map<NodeRef, List<NodeRef>> mandatoryCharacts1 = getMandatoryCharacts(formulatedProduct, PLMModel.TYPE_RAWMATERIAL);

			Composite<CompoListDataItem> composite = CompositeHelper.getHierarchicalCompoList(
					formulatedProduct.getCompoList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>())));
			visitCompoListChildren(formulatedProduct, composite, costList, formulatedProduct.getProductLossPerc(), netQty, mandatoryCharacts1);

			addReqCtrlList(formulatedProduct.getReqCtrlList(), mandatoryCharacts1, getRequirementDataType());

		}

		if (formulatedProduct.hasPackagingListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {

			/*
			 * PackagingList
			 */
			Map<NodeRef, List<NodeRef>> mandatoryCharacts2 = getMandatoryCharacts(formulatedProduct, PLMModel.TYPE_PACKAGINGMATERIAL);

			for (PackagingListDataItem packagingListDataItem : formulatedProduct
					.getPackagingList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
				
				if(packagingListDataItem.getProduct()!=null) {
					
					ProductData partProduct = (ProductData) alfrescoRepository.findOne(packagingListDataItem.getProduct());
					
					Double qty = FormulationHelper.getQtyForCostByPackagingLevel(formulatedProduct, packagingListDataItem,partProduct);

					visitPart(packagingListDataItem.getProduct(),partProduct, costList, qty, qty, netQty, netQty, mandatoryCharacts2, null, false);
				}
			}

			addReqCtrlList(formulatedProduct.getReqCtrlList(), mandatoryCharacts2, getRequirementDataType());
		}

		if (formulatedProduct.hasProcessListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
			/*
			 * ProcessList
			 */
			Map<NodeRef, List<NodeRef>> mandatoryCharacts3 = getMandatoryCharacts(formulatedProduct, PLMModel.TYPE_RESOURCEPRODUCT);
			for (ProcessListDataItem processListDataItem : formulatedProduct
					.getProcessList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {

				Double qty = FormulationHelper.getQty(formulatedProduct, processListDataItem);

				if ((processListDataItem.getResource() != null) && (qty != null)) {
					if (ProductUnit.P.equals(processListDataItem.getUnit()) && ProductUnit.P.equals(formulatedProduct.getUnit())) {
						netQty = FormulationHelper.QTY_FOR_PIECE;
					}

					ProductData partProduct = (ProductData) alfrescoRepository.findOne(processListDataItem.getResource());
					
					visitPart(processListDataItem.getResource(),partProduct, costList, qty, null, netQty, null, mandatoryCharacts3, null, false);
				}
			}

			addReqCtrlList(formulatedProduct.getReqCtrlList(), mandatoryCharacts3, getRequirementDataType());
		}

	}

	private void visitCompoListChildren(ProductData formulatedProduct, Composite<CompoListDataItem> composite, List<CostListDataItem> costList,
			Double parentLossRatio, Double netQty, Map<NodeRef, List<NodeRef>> mandatoryCharacts) throws FormulateException {

		Map<NodeRef, Double> totalQtiesValue = new HashMap<>();
		for (Composite<CompoListDataItem> component : composite.getChildren()) {
			CompoListDataItem compoListDataItem = component.getData();
			ProductData componentProduct = (ProductData) alfrescoRepository.findOne(compoListDataItem.getProduct());
			if (!component.isLeaf()) {
				
				// take in account the loss perc
				Double lossPerc = FormulationHelper.getComponentLossPerc( componentProduct,compoListDataItem);
				Double newLossPerc = FormulationHelper.calculateLossPerc(parentLossRatio, lossPerc);
				if (logger.isDebugEnabled()) {
					logger.debug("parentLossRatio: " + parentLossRatio + " - lossPerc: " + lossPerc + " - newLossPerc: " + newLossPerc);
				}

				// calculate children
				Composite<CompoListDataItem> c = component;
				visitCompoListChildren(formulatedProduct, c, costList, newLossPerc, netQty, mandatoryCharacts);
			} else {
				

				Double qty = FormulationHelper.getQtyForCost(compoListDataItem, parentLossRatio, componentProduct, keepProductUnit);
				visitPart(compoListDataItem.getProduct(),componentProduct, costList, qty, qty, netQty, netQty, mandatoryCharacts, totalQtiesValue,
						formulatedProduct instanceof RawMaterialData);
						
				
			}
		}
		// Case Generic MP
		if (formulatedProduct instanceof RawMaterialData) {
			formulateGenericRawMaterial(costList, totalQtiesValue, netQty);
		}
	}

	@Override
	protected List<CostListDataItem> getDataListVisited(ProductData partProduct) {
		return partProduct.getCostList();
	}

	/**
	 * Calculate the costListUnit
	 *
	 * @param productUnit
	 * @param costUnit
	 * @return
	 */
	public static String calculateUnit(ProductUnit productUnit, String costUnit, Boolean isFixed) {

		if ((isFixed != null) && isFixed) {
			return costUnit;
		} else {
			return costUnit + calculateSuffixUnit(productUnit);
		}
	}

	/**
	 * Calculate the suffix of the costListUnit
	 *
	 * @param productUnit
	 * @return
	 */
	public static String calculateSuffixUnit(ProductUnit productUnit) {
		if (!keepProductUnit) {
			return UNIT_SEPARATOR + productUnit.getMainUnit().toString();
		}
		return UNIT_SEPARATOR + productUnit.toString();
	}

	private void calculateProfitability(ProductData formulatedProduct) {


		Double netQty = FormulationHelper.getNetQtyInLorKg(formulatedProduct, 1d);
		if(formulatedProduct.getUnit()!=null && formulatedProduct.getUnit().isLb()) {
			netQty =  ProductUnit.kgToLb(netQty);
		} 
		
		Double unitTotalVariableCost = 0d;// for 1 product
		Double previousTotalVariableCost = 0d;
		Double futureTotalVariableCost = 0d;
		Double unitTotalFixedCost = 0d;

		for (CostListDataItem c : formulatedProduct.getCostList()) {

			Boolean isFixed = (Boolean) nodeService.getProperty(c.getCost(), PLMModel.PROP_COSTFIXED);
			Double costPerProduct = null;
			Double previousCostPerProduct = null;
			Double futureCostPerProduct = null;
			String costCurrency = (String) nodeService.getProperty(c.getCost(), PLMModel.PROP_COSTCURRENCY);
			String productCurrency = (String) nodeService.getProperty(formulatedProduct.getNodeRef(), PLMModel.PROP_PRICE_CURRENCY);

			if ((productCurrency == null) || (costCurrency == null) || productCurrency.equals(costCurrency)) {
				if (c.getValue() != null) {
					if ((isFixed != null) && (isFixed == Boolean.TRUE)) {
						unitTotalFixedCost += c.getValue();
	
						if ((formulatedProduct.getProjectedQty() != null) && !formulatedProduct.getProjectedQty().equals(0)) {
							costPerProduct = c.getValue() / formulatedProduct.getProjectedQty();
	
							if (c.getFutureValue() != null) {
								futureCostPerProduct = c.getFutureValue() / formulatedProduct.getProjectedQty();
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
							if (previousCostPerProduct != null) {
								previousCostPerProduct *= formulatedProduct.getQty();
							}
						}
	
					} else {
						
						
						costPerProduct = netQty * c.getValue();
	
						if (c.getFutureValue() != null) {
							futureCostPerProduct = netQty * c.getFutureValue();
						}
	
						if (c.getPreviousValue() != null) {
							previousCostPerProduct = netQty * c.getPreviousValue();
						}
	
					}
				}
			}

			c.setValuePerProduct(null);
			if (costPerProduct != null) {
				if ((c.getDepthLevel() == null) || (c.getDepthLevel() == 1)) {
					unitTotalVariableCost += costPerProduct;
				}
				c.setValuePerProduct(costPerProduct);
			}

			c.setFutureValuePerProduct(null);
			if (futureCostPerProduct != null) {
				if ((c.getDepthLevel() == null) || (c.getDepthLevel() == 1)) {
					futureTotalVariableCost += futureCostPerProduct;
				}
				c.setFutureValuePerProduct(futureCostPerProduct);
			}
			
			c.setPreviousValuePerProduct(null);
			if (previousCostPerProduct != null) {
				if ((c.getDepthLevel() == null) || (c.getDepthLevel() == 1)) {
					previousTotalVariableCost += previousCostPerProduct;
				}
				c.setPreviousValuePerProduct(previousCostPerProduct);
			}

		}

		formulatedProduct.setUnitTotalCost(unitTotalVariableCost);
		formulatedProduct.setPreviousUnitTotalCost(previousTotalVariableCost);
		formulatedProduct.setFutureUnitTotalCost(futureTotalVariableCost);

		if ((formulatedProduct.getUnitPrice() != null) && (formulatedProduct.getUnitTotalCost() != null)) {

			// profitability
			Double profit = formulatedProduct.getUnitPrice() - formulatedProduct.getUnitTotalCost();
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

	@Override
	protected Map<NodeRef, List<NodeRef>> getMandatoryCharacts(ProductData formulatedProduct, QName componentType) {

		Map<NodeRef, List<NodeRef>> mandatoryCharacts = new HashMap<>();

		NodeRef entityTplNodeRef = entityTplService.getEntityTpl(componentType);

		if (entityTplNodeRef != null) {

			List<CostListDataItem> costList = alfrescoRepositoryProductData.findOne(entityTplNodeRef).getCostList();

			for (CostListDataItem costListDataItem : formulatedProduct.getCostList()) {
				for (CostListDataItem c : costList) {
					if ((c.getCost() != null) && c.getCost().equals(costListDataItem.getCost()) && isCharactFormulated(costListDataItem)) {
						mandatoryCharacts.put(c.getCost(), new ArrayList<NodeRef>());
						break;
					}
				}
			}
		}
		return mandatoryCharacts;
	}

	@Override
	protected void synchronizeTemplate(ProductData formulatedProduct, List<CostListDataItem> simpleListDataList) {
		
		

		if ((formulatedProduct.getEntityTpl() != null) && !formulatedProduct.getEntityTpl().equals(formulatedProduct)) {
			formulatedProduct.getEntityTpl().getCostList().forEach(templateCostList -> {
				synchronizeCost(formulatedProduct, templateCostList, simpleListDataList, true);
			});

			// check sorting
			int lastSort = 0;
			for (CostListDataItem sl : simpleListDataList) {
				if (sl.getCharactNodeRef() != null) {
					boolean isFound = false;

					for (CostListDataItem tsl : formulatedProduct.getEntityTpl().getCostList()) {
						if (sl.getCharactNodeRef().equals(tsl.getCharactNodeRef())) {
							isFound = true;
							lastSort = tsl.getSort() * 100;
							sl.setSort(lastSort);
						}
					}

					if (!isFound) {
						sl.setSort(++lastSort);
					}
				}
			}

		}
		if (formulatedProduct.getClients() != null) {
			for (ClientData client : formulatedProduct.getClients()) {
				client.getCostList().forEach(templateCostList -> {
					synchronizeCost(formulatedProduct, templateCostList, simpleListDataList,false);
				});
			}
		}
		
		if ((formulatedProduct instanceof RawMaterialData) && ((RawMaterialData) formulatedProduct).getSuppliers() != null) {
			for (NodeRef supplierNodeRef : ((RawMaterialData) formulatedProduct).getSuppliers()) {
				SupplierData supplier = (SupplierData) alfrescoRepository.findOne(supplierNodeRef);
				supplier.getCostList().forEach(templateCostList -> {
					synchronizeCost(formulatedProduct, templateCostList, simpleListDataList,false);
				});
			}
		}
		
		if ((formulatedProduct instanceof PackagingMaterialData) && ((PackagingMaterialData) formulatedProduct).getSuppliers() != null) {
			for (NodeRef supplierNodeRef : ((PackagingMaterialData) formulatedProduct).getSuppliers()) {
				SupplierData supplier = (SupplierData) alfrescoRepository.findOne(supplierNodeRef);
				supplier.getCostList().forEach(templateCostList -> {
					synchronizeCost(formulatedProduct, templateCostList, simpleListDataList,false);
				});
			}
		}

	}
	
	
	private void synchronizeCost(ProductData formulatedProduct, CostListDataItem templateCostList, List<CostListDataItem> simpleListDataList, boolean isTemplateCost) {
		// TODO : manage multiple plants
		NodeRef plantNodeRef = formulatedProduct.getPlants().isEmpty() ? null : formulatedProduct.getPlants().get(0);

		boolean addCost = true;
		for (CostListDataItem costList : simpleListDataList) {
			// plants
			if (templateCostList.getPlants().isEmpty() || templateCostList.getPlants().contains(plantNodeRef)) {
				// same cost
				if ((costList.getCost() != null) && costList.getCost().equals(templateCostList.getCost())) {
					// manual
					if ((costList.getIsManual() == null) || !costList.getIsManual()) {

						if(isTemplateCost) {
							if ((templateCostList.getParent() != null) && (costList.getParent() == null)) {
								costList.setParent(findParentByCharactName(simpleListDataList, templateCostList.getParent().getCharactNodeRef()));
							} else if (templateCostList.getParent() == null) {
								costList.setParent(null);
							}
						}
						
						copyTemplateCost(formulatedProduct, templateCostList, costList);
					}
					addCost = false;
					break;
				}
			} else {
				addCost = false;
			}
		}
		if (addCost) {
			CostListDataItem costListDataItem = new CostListDataItem(templateCostList);
			costListDataItem.setNodeRef(null);
			costListDataItem.setParentNodeRef(null);
			if (costListDataItem.getParent() != null) {
				costListDataItem.setParent(findParentByCharactName(simpleListDataList, costListDataItem.getParent().getCharactNodeRef()));
			}
			copyTemplateCost(formulatedProduct, templateCostList, costListDataItem);
			simpleListDataList.add(costListDataItem);
		}
	
		
	}

	private void copyTemplateCost(ProductData formulatedProduct, CostListDataItem templateCostList, CostListDataItem costList) {

		if (logger.isDebugEnabled()) {
			logger.debug("copy cost " + nodeService.getProperty(templateCostList.getCost(), BeCPGModel.PROP_CHARACT_NAME) + " unit "
					+ templateCostList.getUnit() + " PackagingData " + formulatedProduct.getDefaultVariantPackagingData());
		}
		boolean isCalculated = false;

		if ((formulatedProduct.getUnit() != null) && (templateCostList.getUnit() != null)) {
			if (!templateCostList.getUnit().endsWith(formulatedProduct.getUnit().toString())) {
				if (formulatedProduct.getUnit().isP()) {
					if (templateCostList.getUnit().endsWith("kg") || templateCostList.getUnit().endsWith("L")) {
						calculateValues(templateCostList, costList, false, FormulationHelper.getNetQtyInLorKg(formulatedProduct, 0d));
						isCalculated = true;
					} else if (templateCostList.getUnit().endsWith("Pal")) {
						if ((formulatedProduct.getDefaultVariantPackagingData() != null)
								&& (formulatedProduct.getDefaultVariantPackagingData().getProductPerPallet() != null)) {
							calculateValues(templateCostList, costList, true,
									(double) formulatedProduct.getDefaultVariantPackagingData().getProductPerPallet());
						}
						isCalculated = true;
					}
				} else if (formulatedProduct.getUnit().isWeight() || formulatedProduct.getUnit().isVolume()) {
					if (templateCostList.getUnit().endsWith("P")) {
						calculateValues(templateCostList, costList, true, FormulationHelper.getNetQtyInLorKg(formulatedProduct, 0d));
						isCalculated = true;
					} else if (templateCostList.getUnit().endsWith("Pal")) {
						if ((formulatedProduct.getDefaultVariantPackagingData() != null)
								&& (formulatedProduct.getDefaultVariantPackagingData().getProductPerPallet() != null)) {
							calculateValues(templateCostList, costList, true,
									(double) formulatedProduct.getDefaultVariantPackagingData().getProductPerPallet()
											* FormulationHelper.getNetQtyInLorKg(formulatedProduct, 0d));
						}
						isCalculated = true;
					}
				}
			}
		}

		if (!isCalculated) {
			calculateValues(templateCostList, costList, null, null);
		}
	}

	private void calculateValues(CostListDataItem templateCostList, CostListDataItem costList, Boolean divide, Double qty) {

		if (logger.isDebugEnabled()) {
			logger.debug("calculateValues " + nodeService.getProperty(templateCostList.getCost(), BeCPGModel.PROP_CHARACT_NAME));
		}

		Double value = templateCostList.getValue();
		Double maxi = templateCostList.getMaxi();
		Double previousValue = templateCostList.getPreviousValue();
		Double futureValue = templateCostList.getFutureValue();

		if ((divide != null) && (qty != null)) {
			if (divide) {
				value = divide(value, qty);
				maxi = divide(maxi, qty);
				previousValue = divide(previousValue, qty);
				futureValue = divide(futureValue, qty);
			} else {
				value = multiply(value, qty);
				maxi = multiply(maxi, qty);
				previousValue = multiply(previousValue, qty);
				futureValue = multiply(futureValue, qty);
			}
		}

		if (value != null) {
			costList.setValue(value);
		}
		if (maxi != null) {
			costList.setMaxi(maxi);
		}
		if (previousValue != null) {
			costList.setPreviousValue(previousValue);
		}
		if (futureValue != null) {
			costList.setFutureValue(futureValue);
		}
	}

	private Double divide(Double a, Double b) {
		if ((a != null) && (b != null) && (b != 0d)) {
			return a / b;
		}
		return null;
	}

	private Double multiply(Double a, Double b) {
		if ((a != null) && (b != null)) {
			return a * b;
		}
		return null;
	}

	private void calculateParentCost(ProductData formulatedProduct, Composite<CostListDataItem> composite) {
		if (!composite.isLeaf()) {

			Double value = 0d;
			Double maxi = 0d;
			Double previousValue = 0d;
			Double futureValue = 0d;
			for (Composite<CostListDataItem> component : composite.getChildren()) {
				calculateParentCost(formulatedProduct, component);
				CostListDataItem costListDataItem = component.getData();
				if (costListDataItem.getComponentNodeRef() != null) {
					return;
				}
				if (costListDataItem.getValue() != null) {
					value += costListDataItem.getValue();
				}
				if (costListDataItem.getMaxi() != null) {
					maxi += costListDataItem.getMaxi();
				}
				if (costListDataItem.getPreviousValue() != null) {
					previousValue += costListDataItem.getPreviousValue();
				}
				if (costListDataItem.getFutureValue() != null) {
					futureValue += costListDataItem.getFutureValue();
				}
			}
			if (!composite.isRoot()) {
				composite.getData().setValue(value);
				composite.getData().setMaxi(maxi);
				composite.getData().setPreviousValue(previousValue);
				composite.getData().setFutureValue(futureValue);
			}
		}
	}

	private void calculateSimulationCosts(ProductData formulatedProduct) {
		Double netQty = FormulationHelper.getNetQtyForCost(formulatedProduct);

		for (CostListDataItem c : formulatedProduct.getCostList()) {
			if ((c.getComponentNodeRef() != null) && (c.getParent() != null)) {
				ProductData componentData = alfrescoRepositoryProductData.findOne(c.getComponentNodeRef());
				Double qtyComponent = SimulationCostHelper.getComponentQuantity(formulatedProduct, componentData );
				
				
				if (c.getSimulatedValue() != null && c.getAspects().contains(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM)) {
					c.getAspectsToRemove().add(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM);
				}
				
				for (CostListDataItem c2 : componentData.getCostList()) {
					if (c2.getCost().equals(c.getParent().getCost()) && (c.getSimulatedValue() != null)) {
						
						if (logger.isDebugEnabled()) {
							logger.debug("add simulationCost " + "c2 value " + c2.getValue() + "c simulated value " + c.getSimulatedValue()
									+ " qty component " + qtyComponent + " netQty " + netQty);
						}
						if (c2.getValue() != null) {
							c.setValue(((c.getSimulatedValue() - c2.getValue()) * qtyComponent) / (netQty != 0 ? netQty : 1d));
						} else {
							c.setValue(((c.getSimulatedValue()) * qtyComponent) / (netQty != 0 ? netQty : 1d));
						}
						if (c.getParent().getValue() != null) {
							c.getParent().setValue(c.getParent().getValue() + c.getValue());
						} else {
							c.getParent().setValue(c.getValue());
						}
						break;
					}
				}
			}
			if (c.getAspects().contains(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM) && c.getSimulatedValue() == null && c.getParent() != null 
					&& !nodeService.hasAspect(c.getParent().getCost(),BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM) ) {
				c.getParent().getAspectsToRemove().add(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM);
			}
		}
	}

	@Override
	protected boolean accept(ProductData formulatedProduct) {

		if (formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL)
				|| ((formulatedProduct.getCostList() == null) && !alfrescoRepository.hasDataList(formulatedProduct, PLMModel.TYPE_COSTLIST))) {
			return false;
		}
		return true;
	}

	@Override
	protected RequirementDataType getRequirementDataType() {
		return RequirementDataType.Cost;
	}

}
