/*
 * 
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.PLMModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.PackagingKitData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ResourceProductData;
import fr.becpg.repo.product.data.constraints.ProcessListUnit;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.variant.filters.VariantFilters;

/**
 * The Class CostCalculatingVisitor.
 *
 * @author querephi
 */
public class CostsCalculatingFormulationHandler extends AbstractSimpleListFormulationHandler<CostListDataItem> {

	public static final Double DEFAULT_LOSS_RATIO = 0d;

	/** The logger. */
	private static Log logger = LogFactory.getLog(CostsCalculatingFormulationHandler.class);

	private EntityTplService entityTplService;

	public void setEntityTplService(EntityTplService entityTplService) {
		this.entityTplService = entityTplService;
	}

	@Override
	protected Class<CostListDataItem> getInstanceClass() {

		return CostListDataItem.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {
		logger.debug("Cost calculating visitor");

		// no compo => no formulation
		if (!formulatedProduct.hasCompoListEl(EffectiveFilters.EFFECTIVE)
				&& !formulatedProduct.hasPackagingListEl(EffectiveFilters.EFFECTIVE)
				&& !formulatedProduct.hasProcessListEl(EffectiveFilters.EFFECTIVE)) {
			logger.debug("no compo => no formulation");
			return true;
		}

		if (formulatedProduct.getCostList() == null) {
			formulatedProduct.setCostList(new LinkedList<CostListDataItem>());
		}

		formulateSimpleList(formulatedProduct, formulatedProduct.getCostList());

		if (formulatedProduct.getCostList() != null) {

			for (CostListDataItem c : formulatedProduct.getCostList()) {
				if (isCharactFormulated(c)) {
					String unit = calculateUnit(formulatedProduct.getUnit(),
							(String) nodeService.getProperty(c.getCost(), PLMModel.PROP_COSTCURRENCY));
					c.setUnit(unit);
				}

				if (transientFormulation) {
					c.setTransient(true);
				}
			}
		}

		// profitability
		calculateProfitability(formulatedProduct);

		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void visitChildren(ProductData formulatedProduct, List<CostListDataItem> costList) throws FormulateException {

		Double netQty = null;
		if (formulatedProduct instanceof PackagingKitData) {
			netQty = FormulationHelper.QTY_FOR_PIECE;
		} else if (formulatedProduct instanceof ResourceProductData) {
			netQty = FormulationHelper.QTY_FOR_PIECE;
		}	else {
			if(ProductUnit.P.equals(formulatedProduct.getUnit())){
				netQty = FormulationHelper.QTY_FOR_PIECE;
			} else {	
				netQty = FormulationHelper.getNetQtyInLorKg(formulatedProduct, FormulationHelper.DEFAULT_NET_WEIGHT);
			}
		}


		if (formulatedProduct.hasCompoListEl(EffectiveFilters.EFFECTIVE, VariantFilters.DEFAULT_VARIANT)) {
			
			/*
			 * Composition
			 */
			Map<NodeRef, List<NodeRef>> mandatoryCharacts1 = getMandatoryCharacts(formulatedProduct, PLMModel.TYPE_RAWMATERIAL);
			
			Composite<CompoListDataItem> composite = CompositeHelper.getHierarchicalCompoList(formulatedProduct.getCompoList(
					EffectiveFilters.EFFECTIVE, VariantFilters.DEFAULT_VARIANT));
			visitCompoListChildren(formulatedProduct, composite, costList, DEFAULT_LOSS_RATIO, netQty, mandatoryCharacts1);
			
			addReqCtrlList(formulatedProduct.getCompoListView().getReqCtrlList(), mandatoryCharacts1);
		
		}

		

		if (formulatedProduct.hasPackagingListEl(EffectiveFilters.EFFECTIVE, VariantFilters.DEFAULT_VARIANT)) {
			
			/*
			 * PackagingList
			 */
			Map<NodeRef, List<NodeRef>> mandatoryCharacts2 = getMandatoryCharacts(formulatedProduct, PLMModel.TYPE_PACKAGINGMATERIAL);
			
			for (PackagingListDataItem packagingListDataItem : formulatedProduct.getPackagingList(EffectiveFilters.EFFECTIVE,
					VariantFilters.DEFAULT_VARIANT)) {
				Double qty = FormulationHelper.getQtyForCost(packagingListDataItem);
			
				if(PLMModel.TYPE_PACKAGINGKIT.equals(nodeService.getType(packagingListDataItem.getProduct()))){
					Integer nbByPalet = (Integer) nodeService.getProperty(packagingListDataItem.getProduct(), PackModel.PROP_PALLET_BOXES_PER_PALLET);
					if(nbByPalet!=null && nbByPalet>0){
						qty = qty/nbByPalet;
					}
				}
				
				visitPart(packagingListDataItem.getProduct(), costList, qty, null, netQty, mandatoryCharacts2, null);
			}
			
			addReqCtrlList(formulatedProduct.getPackagingListView().getReqCtrlList(), mandatoryCharacts2);
		}

		

		
		if (formulatedProduct.hasProcessListEl(EffectiveFilters.EFFECTIVE, VariantFilters.DEFAULT_VARIANT)) {
			/*
			 * ProcessList
			 */
			Map<NodeRef, List<NodeRef>> mandatoryCharacts3 = getMandatoryCharacts(formulatedProduct, PLMModel.TYPE_RESOURCEPRODUCT);

			
			for (ProcessListDataItem processListDataItem : formulatedProduct.getProcessList(EffectiveFilters.EFFECTIVE,
					VariantFilters.DEFAULT_VARIANT)) {

				Double qty = FormulationHelper.getQty(formulatedProduct, processListDataItem);
				if (processListDataItem.getResource() != null && qty != null) {
					if (ProcessListUnit.P.equals(processListDataItem.getUnit())) {
						netQty = FormulationHelper.QTY_FOR_PIECE;
					}
					
					visitPart(processListDataItem.getResource(), costList, qty, null, netQty, mandatoryCharacts3, null);
				}
			}
			
			addReqCtrlList(formulatedProduct.getProcessListView().getReqCtrlList(), mandatoryCharacts3);
		}

		
	}

	private void visitCompoListChildren(ProductData formulatedProduct, Composite<CompoListDataItem> composite, List<CostListDataItem> costList,
			Double parentLossRatio, Double netQty, Map<NodeRef, List<NodeRef>> mandatoryCharacts) throws FormulateException {

		for (Composite<CompoListDataItem> component : composite.getChildren()) {

			if (!component.isLeaf()) {

				// take in account the loss perc
				Double lossPerc = component.getData().getLossPerc() != null ? component.getData().getLossPerc() : 0d;
				Double newLossPerc = FormulationHelper.calculateLossPerc(parentLossRatio, lossPerc);
				if (logger.isDebugEnabled()) {
					logger.debug("parentLossRatio: " + parentLossRatio + " - lossPerc: " + lossPerc + " - newLossPerc: " + newLossPerc);
				}

				// calculate children
				Composite<CompoListDataItem> c = (Composite<CompoListDataItem>) component;
				visitCompoListChildren(formulatedProduct, c, costList, newLossPerc, netQty, mandatoryCharacts);
			} else {
				CompoListDataItem compoListDataItem = component.getData();
				Double qty = FormulationHelper.getQtyForCost(compoListDataItem, 
						parentLossRatio,
						ProductUnit.getUnit((String)nodeService.getProperty(compoListDataItem.getProduct(), PLMModel.PROP_PRODUCT_UNIT)));
				visitPart(compoListDataItem.getProduct(), costList, qty, null, netQty, mandatoryCharacts, null);
			}
		}
	}

	@Override
	protected QName getDataListVisited() {
		return PLMModel.TYPE_COSTLIST;
	}

	/**
	 * Calculate the costListUnit
	 * 
	 * @param productUnit
	 * @param costUnit
	 * @return
	 */
	public static String calculateUnit(ProductUnit productUnit, String costUnit) {

		return costUnit + calculateSuffixUnit(productUnit);
	}

	/**
	 * Calculate the suffix of the costListUnit
	 * 
	 * @param productUnit
	 * @return
	 */
	public static String calculateSuffixUnit(ProductUnit productUnit) {
		if (productUnit == null || productUnit.equals(ProductUnit.kg) || productUnit.equals(ProductUnit.g)) {
			return UNIT_SEPARATOR + ProductUnit.kg;
		} else if (productUnit.equals(ProductUnit.L) || productUnit.equals(ProductUnit.mL)) {
			return UNIT_SEPARATOR + ProductUnit.L;
		} else {
			return UNIT_SEPARATOR + productUnit.toString();
		}
	}

	private void calculateProfitability(ProductData formulatedProduct) {

		Double netQty = FormulationHelper.getNetQtyInLorKg(formulatedProduct, FormulationHelper.DEFAULT_NET_WEIGHT);
		Double unitTotalVariableCost = 0d;
		Double unitTotalFixedCost = 0d;

		for (CostListDataItem c : formulatedProduct.getCostList()) {

			Boolean isFixed = (Boolean) nodeService.getProperty(c.getCost(), PLMModel.PROP_COSTFIXED);

			if (c.getValue() != null) {
				if (isFixed != null && isFixed == Boolean.TRUE) {
					unitTotalFixedCost += c.getValue();
				} else {
					unitTotalVariableCost += c.getValue() * netQty;
				}
			}
		}

		formulatedProduct.setUnitTotalCost(unitTotalVariableCost);

		if (formulatedProduct.getUnitPrice() != null && formulatedProduct.getUnitTotalCost() != null) {

			// profitability
			Double profit = formulatedProduct.getUnitPrice() - formulatedProduct.getUnitTotalCost();
			Double profitability = 100 * profit / formulatedProduct.getUnitPrice();
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

	protected Map<NodeRef, List<NodeRef>> getMandatoryCharacts(ProductData formulatedProduct, QName componentType) {

		Map<NodeRef, List<NodeRef>> mandatoryCharacts = new HashMap<NodeRef, List<NodeRef>>();

		NodeRef entityTplNodeRef = entityTplService.getEntityTpl(componentType);

		if (entityTplNodeRef != null) {

			List<CostListDataItem> costList = alfrescoRepository.loadDataList(entityTplNodeRef, PLMModel.TYPE_COSTLIST, PLMModel.TYPE_COSTLIST);

			for (CostListDataItem costListDataItem : formulatedProduct.getCostList()) {
				for (CostListDataItem c : costList) {
					if (c.getCost() != null && c.getCost().equals(costListDataItem.getCost())) {
						mandatoryCharacts.put(c.getCost(), new ArrayList<NodeRef>());
						break;
					}
				}
			}
		}
		return mandatoryCharacts;
	}
}
