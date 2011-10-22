package fr.becpg.repo.product.formulation;

import java.text.DecimalFormat;

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

	public static final float DEFAULT_DENSITY = 1f;
	
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
		Float netWeight;
		if(formulatedProduct.getUnit() == ProductUnit.P){
			netWeight = formulatedProduct.getDensity();
		}
		else{
			Float qty = formulatedProduct.getQty();
			Float density = (formulatedProduct.getDensity() != null) ? formulatedProduct.getDensity():DEFAULT_DENSITY; //density is null => 1
			netWeight = qty * density;
		}
		
		Composite<CompoListDataItem> composite = CompoListDataItem.getHierarchicalCompoList(formulatedProduct.getCompoList());		
		visitChildren(netWeight, netWeight, composite);
		
		return formulatedProduct;
	}
	
	private void visitChildren(Float parentQty, Float qtyAfterProcess, Composite<CompoListDataItem> composite) throws FormulateException{				
		
		for(AbstractComponent<CompoListDataItem> component : composite.getChildren()){					
			
			// qty and sub formula qty are defined and not equal to 0
			if(parentQty != null && qtyAfterProcess != null && !qtyAfterProcess.equals(0f)){
				
				Float qtySubFormula = component.getData().getQtySubFormula();
				if(qtySubFormula != null){
					
					Float qty = qtySubFormula * parentQty / qtyAfterProcess;
					component.getData().setQty(qty);
				}
			}	
			
			if(component instanceof Composite){
				
				// calculate children
				Composite<CompoListDataItem> c = (Composite<CompoListDataItem>)component;
				Float afterProcess = c.getData().getQtyAfterProcess() != null ? c.getData().getQtyAfterProcess() : c.getData().getQtySubFormula();
				visitChildren(c.getData().getQty(), afterProcess, c);
				
				// Yield				
				c.getData().setYieldPerc(calculateYield(c));
			}			
		}
	}
	
	private Float calculateYield(Composite<CompoListDataItem> composite) throws FormulateException{
		
		Float yieldPerc = 100f;
		
		// qty Used in the sub formula
		Float qtyUsed = 0f;				
		for(AbstractComponent<CompoListDataItem> component : composite.getChildren()){
			
			Float qty = getQtyInKg(component.getData());
			if(qty != null){
				qtyUsed += qty;
			}
		}
		
		// qty after process
		Float qtyAfterProcess = FormulationHelper.getQty(composite.getData());
		if(qtyAfterProcess != 0 && qtyUsed != 0){
			yieldPerc = qtyAfterProcess / qtyUsed * 100;
		}
		
		return yieldPerc;
	}	
	
	private Float getQtyInKg(CompoListDataItem compoListDataItem) throws FormulateException{
	
		Float qty = FormulationHelper.getQty(compoListDataItem);
		
		if(compoListDataItem.getCompoListUnit() != null && 
				(!compoListDataItem.getCompoListUnit().equals(CompoListUnit.kg) || 
				!compoListDataItem.getCompoListUnit().equals(CompoListUnit.g))){
			
			Float density = (Float)nodeService.getProperty(compoListDataItem.getProduct(), BeCPGModel.PROP_PRODUCT_DENSITY);
			if(density != null){
				qty = qty * density;
			}
		}
		
		return qty;
	}
	
}
