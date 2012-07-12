package fr.becpg.repo.product.formulation;

import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.data.hierarchicalList.AbstractComponent;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.product.ProductVisitor;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;

public class CompositionCalculatingVisitor implements ProductVisitor {

	public static final Double DEFAULT_DENSITY = 1d;
	public static final Double DEFAULT_QUANTITY = 0d;
	
	private static Log logger = LogFactory.getLog(CompositionCalculatingVisitor.class);
	
	private NodeService nodeService;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	@Override
	public ProductData visit(ProductData formulatedProduct) throws FormulateException {

		logger.debug("Composition calculating visitor");
		
		// no compo => no formulation
		if(formulatedProduct.getCompoList() == null){			
			logger.debug("no compo => no formulation");
			return formulatedProduct;
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
		
		Composite<CompoListDataItem> composite = CompoListDataItem.getHierarchicalCompoList(formulatedProduct.getCompoList());		
		visitChildren(netWeight, netWeight, composite);
		
		// Yield
		Double qtyUsed = calculateQtyUsedBeforeProcess(composite);
		if(qtyUsed != null && qtyUsed != 0d){
			formulatedProduct.setYield(100 * netWeight / qtyUsed);
		}			
		
		return formulatedProduct;
	}
	
	private void visitChildren(Double parentQty, Double qtyAfterProcess, Composite<CompoListDataItem> composite) throws FormulateException{				
		
		for(AbstractComponent<CompoListDataItem> component : composite.getChildren()){					
			
			// qty and sub formula qty are defined and not equal to 0
			if(parentQty != null && qtyAfterProcess != null && !qtyAfterProcess.equals(0d)){
				
				Double qtySubFormula = component.getData().getQtySubFormula();
				if(qtySubFormula != null){
					
					Double qty = qtySubFormula * parentQty / qtyAfterProcess;
					component.getData().setQty(qty);
				}
			}	
			
			if(component instanceof Composite){
				
				// calculate children
				Composite<CompoListDataItem> c = (Composite<CompoListDataItem>)component;
				Double afterProcess = c.getData().getQtyAfterProcess() != null ? c.getData().getQtyAfterProcess() : c.getData().getQtySubFormula();
				visitChildren(c.getData().getQty(), afterProcess, c);
				
				// Yield				
				c.getData().setYieldPerc(calculateYield(c));
			}			
		}
	}
	
	private Double calculateYield(Composite<CompoListDataItem> composite) throws FormulateException{
		
		Double yieldPerc = 100d;
		
		// qty Used in the sub formula
		Double qtyUsed = 0d;				
		for(AbstractComponent<CompoListDataItem> component : composite.getChildren()){
			
			Double qty = getQtyInKg(component.getData());
			if(qty != null){
				qtyUsed += qty;
			}
		}
		
		// qty after process
		Double qtyAfterProcess = FormulationHelper.getQty(composite.getData());
		if(qtyAfterProcess != 0 && qtyUsed != 0){
			yieldPerc = qtyAfterProcess / qtyUsed * 100;
		}
		
		return yieldPerc;
	}	
	
	private Double getQtyInKg(CompoListDataItem compoListDataItem) throws FormulateException{
	
		Double qty = FormulationHelper.getQty(compoListDataItem);
		
		if(compoListDataItem.getCompoListUnit() != null && 
				(!compoListDataItem.getCompoListUnit().equals(CompoListUnit.kg) || 
				!compoListDataItem.getCompoListUnit().equals(CompoListUnit.g))){
			
			Double density = (Double)nodeService.getProperty(compoListDataItem.getProduct(), BeCPGModel.PROP_PRODUCT_DENSITY);
			if(density != null){
				qty = qty * density;
			}
		}
		
		return qty;
	}
	
	private Double calculateQtyUsedBeforeProcess(Composite<CompoListDataItem> composite) throws FormulateException{				
		
		Double qty = 0d;
		
		for(AbstractComponent<CompoListDataItem> component : composite.getChildren()){						
			
			if(component instanceof Composite){
				
				// calculate children
				Composite<CompoListDataItem> c = (Composite<CompoListDataItem>)component;
				qty += calculateQtyUsedBeforeProcess(c);
			} else {
				if(component.getData()!=null && component.getData().getQty()!=null){
					qty += component.getData().getQty();
				}
			}
		}
		
		return qty;
	}
	
}
