/*
 * 
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.PackagingKitData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductUnit;
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

	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {
		logger.debug("Cost calculating visitor");
		
		formulateSimpleList(formulatedProduct, formulatedProduct.getCostList());
		
		if(formulatedProduct.getCostList() != null){
		
			for(CostListDataItem c : formulatedProduct.getCostList()){			
				if(isCharactFormulated(c)){
					String unit = calculateUnit(formulatedProduct.getUnit(), (String)nodeService.getProperty(c.getCost(), BeCPGModel.PROP_COSTCURRENCY));
					c.setUnit(unit);
				}							
			}			
		}						
		
		//profitability		
		formulatedProduct = calculateProfitability(formulatedProduct);
		
		return true;
	}
	
	@Override
	protected void visitChildren(ProductData formulatedProduct, List<CostListDataItem> costList) throws FormulateException{				
		
		Double netQty = null;
		if(formulatedProduct instanceof PackagingKitData){
			netQty = FormulationHelper.QTY_FOR_PIECE;
		}
		else{
			netQty = FormulationHelper.getNetQtyInLorKg(formulatedProduct.getNodeRef(), nodeService,FormulationHelper.DEFAULT_NET_WEIGHT);
		}		
		
		/*
		 * Composition
		 */
		Map<NodeRef, List<NodeRef>> mandatoryCharacts1 = getMandatoryCharacts(formulatedProduct, BeCPGModel.TYPE_RAWMATERIAL);
		
		if(formulatedProduct.hasCompoListEl(EffectiveFilters.EFFECTIVE, VariantFilters.DEFAULT_VARIANT)){									
			Composite<CompoListDataItem> composite = CompositeHelper.getHierarchicalCompoList(formulatedProduct.getCompoList(EffectiveFilters.EFFECTIVE, VariantFilters.DEFAULT_VARIANT));
			visitCompoListChildren(formulatedProduct, composite, costList, DEFAULT_LOSS_RATIO, netQty, mandatoryCharacts1);
		}
		
		addReqCtrlList(formulatedProduct.getCompoListView().getReqCtrlList(), mandatoryCharacts1);
		
		/*
		 * PackagingList
		 */
		Map<NodeRef, List<NodeRef>> mandatoryCharacts2 = getMandatoryCharacts(formulatedProduct, BeCPGModel.TYPE_PACKAGINGMATERIAL);

		if(formulatedProduct.hasPackagingListEl(EffectiveFilters.EFFECTIVE, VariantFilters.DEFAULT_VARIANT)){
			for(PackagingListDataItem packagingListDataItem : formulatedProduct.getPackagingList(EffectiveFilters.EFFECTIVE, VariantFilters.DEFAULT_VARIANT)){
				Double qty = FormulationHelper.getQtyWithLost(packagingListDataItem);
				visitPart(packagingListDataItem.getProduct(), costList, qty, netQty, mandatoryCharacts2);
			}
		}

		addReqCtrlList(formulatedProduct.getPackagingListView().getReqCtrlList(), mandatoryCharacts2);
		
		/*
		 * ProcessList 
		 */
		Map<NodeRef, List<NodeRef>> mandatoryCharacts3 = getMandatoryCharacts(formulatedProduct, BeCPGModel.TYPE_RESOURCEPRODUCT);
		
		if(formulatedProduct.hasProcessListEl(EffectiveFilters.EFFECTIVE, VariantFilters.DEFAULT_VARIANT)){			
			for(ProcessListDataItem processListDataItem : formulatedProduct.getProcessList(EffectiveFilters.EFFECTIVE, VariantFilters.DEFAULT_VARIANT)){
				
				Double qty = FormulationHelper.getQty(formulatedProduct, processListDataItem);
				if(processListDataItem.getResource() != null && qty != null){
					visitPart(processListDataItem.getResource(), costList, qty, netQty, mandatoryCharacts3);
				}																		
			}
		}
		
		addReqCtrlList(formulatedProduct.getProcessListView().getReqCtrlList(), mandatoryCharacts3);
	}
		
	private void visitCompoListChildren(ProductData formulatedProduct, 
			Composite<CompoListDataItem> composite, 
			List<CostListDataItem> costList,
			Double parentLossRatio, 
			Double netQty,
			Map<NodeRef, List<NodeRef>> mandatoryCharacts) throws FormulateException{
		
		for(Composite<CompoListDataItem> component : composite.getChildren()){					
			
			if(!component.isLeaf()){
				
				// take in account the loss perc			
				Double lossPerc = component.getData().getLossPerc() != null ? component.getData().getLossPerc() : 0d;
				Double newLossPerc = FormulationHelper.calculateLossPerc(parentLossRatio, lossPerc);			
				if(logger.isDebugEnabled()){
					logger.debug("parentLossRatio: " + parentLossRatio + " - lossPerc: " + lossPerc + " - newLossPerc: " + newLossPerc);
				}
				
				// calculate children				
				Composite<CompoListDataItem> c = (Composite<CompoListDataItem>)component;
				visitCompoListChildren(formulatedProduct, c, costList, newLossPerc, netQty, mandatoryCharacts);							
			}
			else{
				CompoListDataItem compoListDataItem = component.getData();
				Double qty = FormulationHelper.getQtyWithLost(compoListDataItem, parentLossRatio);				
				visitPart(compoListDataItem.getProduct(), costList, qty, netQty, mandatoryCharacts);
			}			
		}
	}
	
	@Override
	protected QName getDataListVisited(){
		
		return BeCPGModel.TYPE_COSTLIST;
	}
	
	/**
	 * Calculate the costListUnit
	 * @param productUnit
	 * @param costUnit
	 * @return
	 */
	public static String calculateUnit(ProductUnit productUnit, String costUnit){										
		
		return costUnit + calculateSuffixUnit(productUnit);
	}
	
	/**
	 * Calculate the suffix of the costListUnit
	 * @param productUnit
	 * @return
	 */
	public static String calculateSuffixUnit(ProductUnit productUnit){		
		if(productUnit == null || productUnit.equals(ProductUnit.kg) || productUnit.equals(ProductUnit.g)){
			return UNIT_SEPARATOR + ProductUnit.kg;
		}
		else if(productUnit.equals(ProductUnit.L) || productUnit.equals(ProductUnit.mL)){
			return UNIT_SEPARATOR + ProductUnit.L;
		}
		else{
			return UNIT_SEPARATOR + productUnit.toString();
		}
	}
	
	private ProductData calculateProfitability(ProductData formulatedProduct){
		
		Double netQty = FormulationHelper.getNetQtyInLorKg(formulatedProduct.getNodeRef(), nodeService,FormulationHelper.DEFAULT_NET_WEIGHT);
		Double unitTotalVariableCost = 0d;
		Double unitTotalFixedCost = 0d;
		
		for(CostListDataItem c : formulatedProduct.getCostList()){
			
			Boolean isFixed = (Boolean)nodeService.getProperty(c.getCost(), BeCPGModel.PROP_COSTFIXED);
			
			if(c.getValue() != null){
				if(isFixed != null && isFixed == Boolean.TRUE){
					unitTotalFixedCost += c.getValue();					
				}
				else{
					unitTotalVariableCost += c.getValue() * netQty;
				}
			}			
		}
				
		formulatedProduct.setUnitTotalCost(unitTotalVariableCost);
		
		if(formulatedProduct.getUnitPrice() != null && formulatedProduct.getUnitTotalCost() != null){
			
			// profitability
			Double profit = formulatedProduct.getUnitPrice() - formulatedProduct.getUnitTotalCost();
			Double profitability = 100 * profit / formulatedProduct.getUnitPrice();
			logger.debug("profitability: " + profitability);
			formulatedProduct.setProfitability(profitability);
			
			// breakEven
			if(profit > 0){
				
				Long breakEven = Math.round(unitTotalFixedCost / profit);
				formulatedProduct.setBreakEven(breakEven);
			}
			else{
				formulatedProduct.setBreakEven(null);
			}
			
		}
		else{
			formulatedProduct.setProfitability(null);
		}
		
		return formulatedProduct;
	}	
	
	protected Map<NodeRef, List<NodeRef>> getMandatoryCharacts(ProductData formulatedProduct, QName componentType){
		
		Map<NodeRef, List<NodeRef>> mandatoryCharacts = new HashMap<NodeRef, List<NodeRef>>();
		
		NodeRef entityTplNodeRef = entityTplService.getEntityTpl(componentType);
		
		if(entityTplNodeRef != null){			
			
			List<CostListDataItem> costList = alfrescoRepository.loadDataList(entityTplNodeRef, BeCPGModel.TYPE_COSTLIST, BeCPGModel.TYPE_COSTLIST);
			
			for(CostListDataItem costListDataItem : formulatedProduct.getCostList()){
				for(CostListDataItem c : costList){
					if(c.getCost()!=null && c.getCost().equals(costListDataItem.getCost())){
						mandatoryCharacts.put(c.getCost(), new ArrayList<NodeRef>());
						break;
					}
				}				
			}
		}
		return mandatoryCharacts;
	}
}
