package fr.becpg.repo.product.formulation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.data.hierarchicalList.AbstractComponent;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.repository.filters.EffectiveFilters;

public class CompositionCalculatingVisitor extends AbstractProductFormulationHandler {

	public static final Double DEFAULT_DENSITY = 1d;
	public static final Double DEFAULT_QUANTITY = 0d;
	
	private static Log logger = LogFactory.getLog(CompositionCalculatingVisitor.class);
	
	
	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {

		logger.debug("Composition calculating visitor");
		
		// no compo => no formulation
		if(!formulatedProduct.hasCompoListEl(EffectiveFilters.ALL)){			
			logger.debug("no compo => no formulation");
			return true;
		}
		
		//Take in account net weight
		Double netWeight;
		if(formulatedProduct.getUnit() == ProductUnit.P){
			netWeight = formulatedProduct.getDensity();
		}
		else{
			Double qty = (formulatedProduct.getQty()!=null) ? formulatedProduct.getQty() : DEFAULT_QUANTITY;
			Double density = (formulatedProduct.getDensity() != null) ? formulatedProduct.getDensity():DEFAULT_DENSITY; //density is null => 1
			netWeight = qty * density;
		}
		
		Composite<CompoListDataItem> composite = CompoListDataItem.getHierarchicalCompoList(formulatedProduct.getCompoList(EffectiveFilters.ALL));		
		visitChildren(netWeight, netWeight, composite);
		
		// Yield
		Double qtyUsed = calculateQtyUsedBeforeProcess(composite);
		if(qtyUsed != null && qtyUsed != 0d){
			formulatedProduct.setYield(100 * netWeight / qtyUsed);
		}			
		
		return true;
	}
	
	private void visitChildren(Double parentQty, Double qtyAfterProcess, Composite<CompoListDataItem> composite) throws FormulateException{				
		
		for(AbstractComponent<CompoListDataItem> component : composite.getChildren()){					
			
			// qty and sub formula qty are defined and not equal to 0
			if(parentQty != null && qtyAfterProcess != null && !qtyAfterProcess.equals(0d)){
				
				Double qtySubFormula = component.getData().getQtySubFormula();
				if(qtySubFormula != null){
					
					Double qty = null;
					// take in account percentage
					if(component.getData().getCompoListUnit() != null && 
							component.getData().getCompoListUnit().equals(CompoListUnit.Perc)){
						qty = qtySubFormula * qtyAfterProcess / 100;
					}
					else{
						qty = qtySubFormula * parentQty / qtyAfterProcess;
					}					
					
					component.getData().setQty(qty);									
				}
			}	
			
			// calculate children
			if(component instanceof Composite){
				
				Composite<CompoListDataItem> c = (Composite<CompoListDataItem>)component;
				
				// take in account percentage
				if(component.getData().getCompoListUnit() != null && 
						component.getData().getCompoListUnit().equals(CompoListUnit.Perc)){	
					
					visitChildren(parentQty, qtyAfterProcess, c);
					
					// no yield but calculate % of composite
					Double compositePerc = 0d;
					for(AbstractComponent<CompoListDataItem> child : c.getChildren()){	
						compositePerc += child.getData().getQtySubFormula();
					}
					c.getData().setQtySubFormula(compositePerc);
					c.getData().setQty(compositePerc * parentQty / 100);
					c.getData().setYieldPerc(null);
				}
				else{
														
					Double afterProcess = c.getData().getQtyAfterProcess() != null ? c.getData().getQtyAfterProcess() : c.getData().getQtySubFormula();
					visitChildren(c.getData().getQty(), afterProcess, c);
					
					// Yield				
					c.getData().setYieldPerc(calculateYield(c));
				}				
			}			
		}
	}
	
	private Double calculateYield(Composite<CompoListDataItem> composite) throws FormulateException{
		
		Double yieldPerc = 100d;
		
		// qty Used in the sub formula
		Double qtyUsed = 0d;				
		for(AbstractComponent<CompoListDataItem> component : composite.getChildren()){
			
			Double qty = FormulationHelper.getQty(component.getData(), nodeService);
			if(qty != null){
				qtyUsed += qty;
			}
		}
		
		// qty after process
		Double qtyAfterProcess = FormulationHelper.getQty(composite.getData(), nodeService);
		if(qtyAfterProcess != 0 && qtyUsed != 0){
			yieldPerc = qtyAfterProcess / qtyUsed * 100;
		}
		
		return yieldPerc;
	}	
	
	private Double calculateQtyUsedBeforeProcess(Composite<CompoListDataItem> composite) throws FormulateException{				
		
		Double qty = 0d;
		
		for(AbstractComponent<CompoListDataItem> component : composite.getChildren()){						
			
			if(component instanceof Composite){
				
				// calculate children
				Composite<CompoListDataItem> c = (Composite<CompoListDataItem>)component;
				qty += calculateQtyUsedBeforeProcess(c);
			}else{
				qty += FormulationHelper.getQty(component.getData(), nodeService);
			}
		}
		
		return qty;
	}
	
}
