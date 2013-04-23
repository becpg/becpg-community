package fr.becpg.repo.product.formulation;

import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.repository.filters.EffectiveFilters;
import fr.becpg.repo.variant.filters.VariantFilters;

@Service
public class CompositionCalculatingFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static Log logger = LogFactory.getLog(CompositionCalculatingFormulationHandler.class);
	
	private NodeService nodeService;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {

		logger.debug("Composition calculating visitor");		
		
		// no compo => no formulation
		if(!formulatedProduct.hasCompoListEl(EffectiveFilters.ALL, VariantFilters.DEFAULT_VARIANT)){			
			logger.debug("no compo => no formulation");
			return true;
		}
		
		//Take in account net weight
		Double netWeight;
		if(formulatedProduct.getUnit() == ProductUnit.P){
			netWeight = formulatedProduct.getDensity();
		}
		else{
			Double qty = FormulationHelper.getQty(formulatedProduct);
			Double density = FormulationHelper.getDensity(formulatedProduct);
			netWeight = qty * density;
		}		
		
		Composite<CompoListDataItem> composite = CompositeHelper.getHierarchicalCompoList(formulatedProduct.getCompoList(EffectiveFilters.ALL, VariantFilters.DEFAULT_VARIANT));		
		visitChildren(netWeight, netWeight, composite);
		
		// Yield
		Double qtyUsed = calculateQtyUsedBeforeProcess(composite);
		if(qtyUsed != null && qtyUsed != 0d){
			formulatedProduct.setYield(100 * netWeight / qtyUsed);
		}			
		
		return true;
	}
	
	private void visitChildren(Double parentQty, Double qtyAfterProcess, Composite<CompoListDataItem> composite) throws FormulateException{				
		
		for(Composite<CompoListDataItem> component : composite.getChildren()){					
			
			// qty and sub formula qty are defined and not equal to 0
			if(parentQty != null && qtyAfterProcess != null && !qtyAfterProcess.equals(0d)){
				
				Double qtySubFormula = component.getData().getQtySubFormula();
				logger.debug("qtySubFormula: " + qtySubFormula + " qtyAfterProcess: " + qtyAfterProcess + " parentQty: " + parentQty);
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
			if(!component.isLeaf()){
				
				
				// take in account percentage
				if(component.getData().getCompoListUnit() != null && 
						component.getData().getCompoListUnit().equals(CompoListUnit.Perc)){	
					
					visitChildren(parentQty, qtyAfterProcess, component);
					
					// no yield but calculate % of composite
					Double compositePerc = 0d;
					for(Composite<CompoListDataItem> child : component.getChildren()){	
						compositePerc += child.getData().getQtySubFormula();
					}
					component.getData().setQtySubFormula(compositePerc);
					component.getData().setQty(compositePerc * parentQty / 100);
					component.getData().setYieldPerc(null);
				}
				else{
														
					Double afterProcess = component.getData().getQtyAfterProcess() != null ?component.getData().getQtyAfterProcess() : component.getData().getQtySubFormula();
					visitChildren(component.getData().getQty(), afterProcess,component);
					
					// Yield				
					component.getData().setYieldPerc(calculateYield(component));
				}				
			}			
		}
	}
	
	private Double calculateYield(Composite<CompoListDataItem> composite) throws FormulateException{
		
		Double yieldPerc = 100d;
		
		// qty Used in the sub formula
		Double qtyUsed = 0d;				
		for(Composite<CompoListDataItem> component : composite.getChildren()){
			
			Double qty = FormulationHelper.getQty(component.getData(), nodeService);
			if(qty != null){
				qtyUsed += qty;
			}
		}
		
		// qty after process
		Double qtyAfterProcess = FormulationHelper.getQty(composite.getData(), nodeService);
		logger.debug("qtyAfterProcess: " + qtyAfterProcess + " - qtyUsed: " + qtyUsed);
		if(qtyAfterProcess != 0 && qtyUsed != 0){
			yieldPerc = qtyAfterProcess / qtyUsed * 100;
		}
		
		return yieldPerc;
	}	
	
	private Double calculateQtyUsedBeforeProcess(Composite<CompoListDataItem> composite) throws FormulateException{				
		
		Double qty = 0d;
		
		for(Composite<CompoListDataItem> component : composite.getChildren()){						
			
			if(!component.isLeaf()){
				// calculate children
				qty += calculateQtyUsedBeforeProcess(component);
			}else{
				qty += FormulationHelper.getQty(component.getData(), nodeService);
			}
		}
		
		return qty;
	}
	
}
