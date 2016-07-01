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
import java.util.function.Consumer;

import org.alfresco.model.ContentModel;
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
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.ProcessListUnit;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.variant.filters.VariantFilters;

/**
 * The Class CostCalculatingVisitor.
 *
 * @author querephi
 */
public class CostsCalculatingFormulationHandler extends AbstractSimpleListFormulationHandler<CostListDataItem> {

	public static final Double DEFAULT_LOSS_RATIO = 0d;

	private static final Log logger = LogFactory.getLog(CostsCalculatingFormulationHandler.class);

	private EntityTplService entityTplService;

	private PackagingHelper packagingHelper;

	private AlfrescoRepository<ProductData> alfrescoRepositoryProductData;
 
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

			if (formulatedProduct.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))
					|| formulatedProduct.hasPackagingListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))
					|| formulatedProduct.hasProcessListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

				formulateSimpleList(formulatedProduct, formulatedProduct.getCostList());

				// simulation: take in account cost of components defined on
				// formulated product
				calculateSimulationCosts(formulatedProduct);
			}

			if (formulatedProduct.getCostList() != null) {

				computeFormulatedList(formulatedProduct, formulatedProduct.getCostList(), PLMModel.PROP_COST_FORMULA,
						"message.formulate.costList.error");

				for (CostListDataItem c : formulatedProduct.getCostList()) {
					c.setUnit(calculateUnit(formulatedProduct.getUnit(), (String) nodeService.getProperty(c.getCost(), PLMModel.PROP_COSTCURRENCY),
							(Boolean) nodeService.getProperty(c.getCost(), PLMModel.PROP_COSTFIXED)));

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
			visitCompoListChildren(formulatedProduct, composite, costList, DEFAULT_LOSS_RATIO, netQty, mandatoryCharacts1);

			addReqCtrlList(formulatedProduct.getReqCtrlList(), mandatoryCharacts1, getRequirementDataType());

		}

		if (formulatedProduct.hasPackagingListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {

			/*
			 * PackagingList
			 */
			Map<NodeRef, List<NodeRef>> mandatoryCharacts2 = getMandatoryCharacts(formulatedProduct, PLMModel.TYPE_PACKAGINGMATERIAL);

			for (PackagingListDataItem packagingListDataItem : formulatedProduct
					.getPackagingList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
				Double qty = FormulationHelper.getQtyForCostByPackagingLevel(formulatedProduct, packagingListDataItem, nodeService);

				visitPart(packagingListDataItem.getProduct(), costList, qty, null, netQty, mandatoryCharacts2, null, false);
			}

			addReqCtrlList(formulatedProduct.getReqCtrlList(), mandatoryCharacts2,getRequirementDataType());
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
					if (ProcessListUnit.P.equals(processListDataItem.getUnit()) && ProductUnit.P.equals(formulatedProduct.getUnit())) {
						netQty = FormulationHelper.QTY_FOR_PIECE;
					}

					visitPart(processListDataItem.getResource(), costList, qty, null, netQty, mandatoryCharacts3, null, false);
				}
			}

			addReqCtrlList(formulatedProduct.getReqCtrlList(), mandatoryCharacts3,getRequirementDataType());
		}

	}

	private void visitCompoListChildren(ProductData formulatedProduct, Composite<CompoListDataItem> composite, List<CostListDataItem> costList,
			Double parentLossRatio, Double netQty, Map<NodeRef, List<NodeRef>> mandatoryCharacts) throws FormulateException {

		Map<NodeRef, Double> totalQtiesValue = new HashMap<>();
		for (Composite<CompoListDataItem> component : composite.getChildren()) {

			if (!component.isLeaf()) {

				// take in account the loss perc
				Double lossPerc = component.getData().getLossPerc() != null ? component.getData().getLossPerc() : 0d;
				Double newLossPerc = FormulationHelper.calculateLossPerc(parentLossRatio, lossPerc);
				if (logger.isDebugEnabled()) {
					logger.debug("parentLossRatio: " + parentLossRatio + " - lossPerc: " + lossPerc + " - newLossPerc: " + newLossPerc);
				}

				// calculate children
				Composite<CompoListDataItem> c = component;
				visitCompoListChildren(formulatedProduct, c, costList, newLossPerc, netQty, mandatoryCharacts);
			} else {
				CompoListDataItem compoListDataItem = component.getData();
				Double qty = FormulationHelper.getQtyForCost(compoListDataItem, parentLossRatio,
						ProductUnit.getUnit((String) nodeService.getProperty(compoListDataItem.getProduct(), PLMModel.PROP_PRODUCT_UNIT)));
				visitPart(compoListDataItem.getProduct(), costList, qty, qty, netQty, mandatoryCharacts, totalQtiesValue,
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
		if ((productUnit == null) || productUnit.equals(ProductUnit.kg) || productUnit.equals(ProductUnit.g)) {
			return UNIT_SEPARATOR + ProductUnit.kg;
		} else if (productUnit.equals(ProductUnit.L) || productUnit.equals(ProductUnit.mL) || productUnit.equals(ProductUnit.cL)) {
			return UNIT_SEPARATOR + ProductUnit.L;
		} else {
			return UNIT_SEPARATOR + productUnit.toString();
		}
	}

	private void calculateProfitability(ProductData formulatedProduct) {

		Double netQty = FormulationHelper.getNetQtyInLorKg(formulatedProduct, 1d);
		Double unitTotalVariableCost = 0d;// for 1 product
		Double unitTotalFixedCost = 0d;

		for (CostListDataItem c : formulatedProduct.getCostList()) {

			Boolean isFixed = (Boolean) nodeService.getProperty(c.getCost(), PLMModel.PROP_COSTFIXED);
			Double costPerProduct = null;

			if (c.getValue() != null) {
				if ((isFixed != null) && (isFixed == Boolean.TRUE)) {
					unitTotalFixedCost += c.getValue();
					if ((formulatedProduct.getProjectedQty() != null) && !formulatedProduct.getProjectedQty().equals(0)) {
						costPerProduct = c.getValue() / formulatedProduct.getProjectedQty();
					}

				} else if (FormulationHelper.isProductUnitP(formulatedProduct.getUnit())) {
					costPerProduct = c.getValue();
					if(formulatedProduct.getQty()!=null){
						costPerProduct *=formulatedProduct.getQty();
					}
					
				} else {
					costPerProduct = netQty * c.getValue();
				}
			}

			c.setValuePerProduct(null);
			if (costPerProduct != null) {
				if ((c.getDepthLevel() == null) || (c.getDepthLevel() == 1)) {
					unitTotalVariableCost += costPerProduct;
				}
				if (formulatedProduct instanceof FinishedProductData || formulatedProduct instanceof SemiFinishedProductData) {
					c.setValuePerProduct(costPerProduct);
				}
			}
		}

		if (formulatedProduct instanceof FinishedProductData) {
			formulatedProduct.setUnitTotalCost(unitTotalVariableCost);
		} else {
			// €/Kg, €/L or €/P
			formulatedProduct.setUnitTotalCost(unitTotalVariableCost / netQty);
		}

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
					if ((c.getCost() != null) && c.getCost().equals(costListDataItem.getCost())) {
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
		// TODO : manage multiple plants
		NodeRef plantNodeRef = formulatedProduct.getPlants().isEmpty() ? null : formulatedProduct.getPlants().get(0);

		Consumer<CostListDataItem> synchConsumer = templateCostList -> {

			boolean addCost = true;
			for (CostListDataItem costList : simpleListDataList) {
				// plants
				if (templateCostList.getPlants().isEmpty() || templateCostList.getPlants().contains(plantNodeRef)) {
					// same cost
					if ((costList.getCost() != null) && costList.getCost().equals(templateCostList.getCost())) {
						// manual
						if ((costList.getIsManual() == null) || !costList.getIsManual()) {
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
				
				
				copyTemplateCost(formulatedProduct, templateCostList, costListDataItem);
				simpleListDataList.add(costListDataItem);
			}
		};

		if ((formulatedProduct.getEntityTpl() != null) && !formulatedProduct.getEntityTpl().equals(formulatedProduct)) {
			formulatedProduct.getEntityTpl().getCostList().forEach(synchConsumer);

		}
		if (formulatedProduct.getClients() != null) {
			for (ClientData client : formulatedProduct.getClients()) {
				client.getCostList().forEach(synchConsumer);
			}
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
				if (FormulationHelper.isProductUnitP(formulatedProduct.getUnit())) {
					if (templateCostList.getUnit().endsWith("kg") || templateCostList.getUnit().endsWith("L")) {
						calculateValues(templateCostList, costList, false, FormulationHelper.getNetQtyInLorKg(formulatedProduct, 0d));
						isCalculated = true;
					} else if (templateCostList.getUnit().endsWith("Pal")) {
						if (formulatedProduct.getDefaultVariantPackagingData() != null
								&& formulatedProduct.getDefaultVariantPackagingData().getProductPerPallet() != null) {
							calculateValues(templateCostList, costList, true,
									(double) formulatedProduct.getDefaultVariantPackagingData().getProductPerPallet());
						}
						isCalculated = true;
					}
				} else if (FormulationHelper.isProductUnitKg(formulatedProduct.getUnit())
						|| FormulationHelper.isProductUnitLiter(formulatedProduct.getUnit())) {
					if (templateCostList.getUnit().endsWith("P")) {
						calculateValues(templateCostList, costList, true, FormulationHelper.getNetQtyInLorKg(formulatedProduct, 0d));
						isCalculated = true;
					} else if (templateCostList.getUnit().endsWith("Pal")) {
						if (formulatedProduct.getDefaultVariantPackagingData() != null
								&& formulatedProduct.getDefaultVariantPackagingData().getProductPerPallet() != null) {
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
			if (!composite.isLeaf()) {
				composite.getData().setValue(value);
				composite.getData().setMaxi(maxi);
				composite.getData().setPreviousValue(previousValue);
				composite.getData().setFutureValue(futureValue);
			}
		}
	}

	private double getCompoListQty(ProductData productData, NodeRef componentNodeRef, double parentQty) {
		double totalQty = 0d;
		for (CompoListDataItem compoList : productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
			NodeRef productNodeRef = compoList.getProduct();
			Double qty = FormulationHelper.getQtyForCost(compoList, 0d,
					ProductUnit.getUnit((String) nodeService.getProperty(productNodeRef, PLMModel.PROP_PRODUCT_UNIT)));
			if (logger.isDebugEnabled()) {
				logger.debug("Get component " + nodeService.getProperty(productNodeRef, ContentModel.PROP_NAME) + "qty: " + qty + " recipeQtyUsed "
						+ productData.getRecipeQtyUsed());
			}
			if ((qty != null) && (productData.getRecipeQtyUsed() != null) && (productData.getRecipeQtyUsed() != 0d)) {
				qty = (parentQty * qty) / productData.getRecipeQtyUsed();

				if (productNodeRef.equals(componentNodeRef)) {
					totalQty += qty;
				} else {
					totalQty += getCompoListQty(alfrescoRepositoryProductData.findOne(productNodeRef), componentNodeRef, qty);
				}
			}
		}
		return totalQty;
	}

	private double getPackagingListQty(ProductData productData, NodeRef componentNodeRef) {
		double totalQty = 0d;
		if(productData.hasPackagingListEl()){
			for (PackagingListDataItem packList : productData.getPackagingList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
				NodeRef productNodeRef = packList.getProduct();
				Double qty = FormulationHelper.getQtyForCost(packList);
				if (logger.isDebugEnabled()) {
					logger.debug("Get component " + nodeService.getProperty(productNodeRef, ContentModel.PROP_NAME) + "qty: " + qty);
				}
				if (productNodeRef.equals(componentNodeRef)) {
					totalQty += qty;
				}
			}
		}
		return totalQty;
	}

	private void calculateSimulationCosts(ProductData formulatedProduct) {
		Double netQty = FormulationHelper.getNetQtyInLorKg(formulatedProduct, FormulationHelper.DEFAULT_NET_WEIGHT);
		for (CostListDataItem c : formulatedProduct.getCostList()) {
			if ((c.getComponentNodeRef() != null) && (c.getParent() != null)) {
				Double qtyComponent;
				ProductData componentData = alfrescoRepositoryProductData.findOne(c.getComponentNodeRef());
				if (componentData instanceof PackagingMaterialData) {
					qtyComponent = getPackagingListQty(formulatedProduct, c.getComponentNodeRef());
				} else {
					qtyComponent = getCompoListQty(formulatedProduct, c.getComponentNodeRef(), formulatedProduct.getRecipeQtyUsed());
				}
				for (CostListDataItem c2 : componentData.getCostList()) {
					if (c2.getCost().equals(c.getParent().getCost()) && (c.getSimulatedValue() != null)) {
						if (logger.isDebugEnabled()) {
							logger.debug("add simulationCost " + "c2 value " + c2.getValue() + "c simulated value " + c.getSimulatedValue()
									+ " qty component " + qtyComponent + " netQty " + netQty);
						}
						c.setValue(((c.getSimulatedValue() - c2.getValue()) * qtyComponent) / netQty);
						c.getParent().setValue(c.getParent().getValue() + c.getValue());
						break;
					}
				}
			}
		}
	}

	@Override
	protected boolean accept(ProductData formulatedProduct) {

		if (formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL)
				|| (formulatedProduct.getCostList() == null && !alfrescoRepository.hasDataList(formulatedProduct, PLMModel.TYPE_COSTLIST))) {
			return false;
		}
		return true;
	}

	@Override
	protected RequirementDataType getRequirementDataType() {
		return RequirementDataType.Cost;
	}

	@Override
	protected String getSpecErrorMessageKey() {
		return null;
	}
}
