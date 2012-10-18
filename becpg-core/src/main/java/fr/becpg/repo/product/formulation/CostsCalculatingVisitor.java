/*
 * 
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.data.hierarchicalList.AbstractComponent;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.product.ProductVisitor;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.data.productList.SimpleListDataItem;

/**
 * The Class CostCalculatingVisitor.
 *
 * @author querephi
 */
public class CostsCalculatingVisitor extends AbstractCalculatingVisitor implements ProductVisitor {

	public static final Double DEFAULT_LOSS_RATIO = 1d;
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(CostsCalculatingVisitor.class);

	@Override
	public ProductData visit(ProductData formulatedProduct) throws FormulateException{		
		logger.debug("Cost calculating visitor");
		
		Map<NodeRef, SimpleListDataItem> simpleListMap = getFormulatedList(formulatedProduct);		
		
		if(simpleListMap != null){
		
			List<CostListDataItem> dataList = new ArrayList<CostListDataItem>();
			
			for(SimpleListDataItem sl : simpleListMap.values()){
				CostListDataItem c = new CostListDataItem(sl);
				if(sl.getIsManual() != null && sl.getIsManual()){
					//manual so it's a CostListDataItem instance (we want to keep the unit defined manually, ie: €)
					if(sl instanceof CostListDataItem){
						c = new CostListDataItem((CostListDataItem)sl);
					}
				}
				else{
					String unit = calculateUnit(formulatedProduct.getUnit(), (String)nodeService.getProperty(c.getCost(), BeCPGModel.PROP_COSTCURRENCY));
					c.setUnit(unit);
				}
				
				dataList.add(c);				
			}
			
			formulatedProduct.setCostList(sortCost(dataList));
		}						
		
		//profitability		
		formulatedProduct = calculateProfitability(formulatedProduct);

		return formulatedProduct;
	}
	
	@Override
	protected void visitChildren(ProductData formulatedProduct, Map<NodeRef, SimpleListDataItem> simpleListDataMap) throws FormulateException{				
		
		Double netWeight = FormulationHelper.getNetWeight(formulatedProduct);
		
		if(formulatedProduct.hasCompoListEl(EffectiveFilters.EFFECTIVE)){									
			Composite<CompoListDataItem> composite = CompoListDataItem.getHierarchicalCompoList(formulatedProduct.getCompoList(EffectiveFilters.EFFECTIVE));		
			visitCompoListChildren(formulatedProduct, composite, simpleListDataMap, DEFAULT_LOSS_RATIO, netWeight);
		}

		if(formulatedProduct.hasPackagingListEl(EffectiveFilters.EFFECTIVE)){
			for(PackagingListDataItem packagingListDataItem : formulatedProduct.getPackagingList(EffectiveFilters.EFFECTIVE)){
				Double qty = FormulationHelper.getQty(packagingListDataItem);
				visitPart(packagingListDataItem.getProduct(), simpleListDataMap, qty, netWeight);
			}
		}

		if(formulatedProduct.hasProcessListEl(EffectiveFilters.EFFECTIVE)){			
			for(ProcessListDataItem processListDataItem : formulatedProduct.getProcessList(EffectiveFilters.EFFECTIVE)){
				
				Double qty = FormulationHelper.getQty(formulatedProduct, processListDataItem);
				if(processListDataItem.getResource() != null && qty != null){
					visitPart(processListDataItem.getResource(), simpleListDataMap, qty, netWeight);
				}																		
			}
		}
	}
		
	private void visitCompoListChildren(ProductData formulatedProduct, Composite<CompoListDataItem> composite, Map<NodeRef, SimpleListDataItem> compositeList, Double parentLossRatio, Double netWeight) throws FormulateException{
		
		for(AbstractComponent<CompoListDataItem> component : composite.getChildren()){					
			
			if(component instanceof Composite){
				
				// take in account the loss perc			
				Double lossPerc = component.getData().getLossPerc() != null ? component.getData().getLossPerc() : 0d;
				Double newLossPerc = FormulationHelper.calculateLossPerc(parentLossRatio, lossPerc);			
				if(logger.isDebugEnabled()){
					logger.debug("parentLossRatio: " + parentLossRatio + " - lossPerc: " + lossPerc + " - newLossPerc: " + newLossPerc);
				}
				
				// calculate children				
				Composite<CompoListDataItem> c = (Composite<CompoListDataItem>)component;
				visitCompoListChildren(formulatedProduct, c, compositeList, newLossPerc, netWeight);							
			}
			else{
				CompoListDataItem compoListDataItem = component.getData();
				Double qty = FormulationHelper.getQtyWithLost(compoListDataItem, nodeService, parentLossRatio);				
				visitPart(compoListDataItem.getProduct(), compositeList, qty, netWeight);
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
		
		ProductUnit unit = ProductUnit.kg;
		
		if(ProductUnit.L.equals(productUnit)){
			unit = ProductUnit.L;
		}						
		
		return costUnit + calculateSuffixUnit(unit);
	}
	
	/**
	 * Calculate the suffix of the costListUnit
	 * @param productUnit
	 * @return
	 */
	public static String calculateSuffixUnit(ProductUnit productUnit){
		return productUnit != null ? UNIT_SEPARATOR + productUnit : UNIT_SEPARATOR + ProductUnit.kg;
	}
	
	private ProductData calculateProfitability(ProductData formulatedProduct){
		
		Double netWeight = FormulationHelper.getNetWeight(formulatedProduct);
		Double unitTotalVariableCost = 0d;
		Double unitTotalFixedCost = 0d;
		
		for(CostListDataItem c : formulatedProduct.getCostList()){
			
			Boolean isFixed = (Boolean)nodeService.getProperty(c.getCost(), BeCPGModel.PROP_COSTFIXED);
			
			if(c.getValue() != null){
				if(isFixed != null && isFixed == Boolean.TRUE){
					unitTotalFixedCost += c.getValue();					
				}
				else{
					unitTotalVariableCost += c.getValue() * netWeight;
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
	
	/**
	 * Sort costs by name.
	 *
	 * @param costList the cost list
	 * @return the list
	 */
	private List<CostListDataItem> sortCost(List<CostListDataItem> costList){
			
		Collections.sort(costList, new Comparator<CostListDataItem>(){
        	
            @Override
			public int compare(CostListDataItem c1, CostListDataItem c2){
            	
            	String costName1 = (String)nodeService.getProperty(c1.getCost(), ContentModel.PROP_NAME);
            	String costName2 = (String)nodeService.getProperty(c2.getCost(), ContentModel.PROP_NAME);
            	
            	// increase
                return costName1.compareTo(costName2);                
            }

        });
        
        return costList;
	}
}
