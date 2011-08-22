package fr.becpg.repo.product.formulation;

import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.data.hierarchicalList.AbstractComponent;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.product.ProductVisitor;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;

public class CompositionCalculatingVisitor implements ProductVisitor {

	private static Log logger = LogFactory.getLog(CompositionCalculatingVisitor.class);
	
	private NodeService nodeService;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@Override
	public ProductData visit(ProductData formulatedProduct) {

		logger.debug("Composition calculating visitor");
		
		// no compo => no formulation
		if(formulatedProduct.getCompoList() == null){			
			logger.debug("no compo => no formulation");
			return formulatedProduct;
		}
		
		Composite<CompoListDataItem> composite = CompoListDataItem.getHierarchicalCompoList(formulatedProduct.getCompoList());		
		visitChildren(null, null, composite);
		
		return formulatedProduct;
	}
	
	private void visitChildren(Float parentQty, Float parentQtySubFormula, Composite<CompoListDataItem> composite){				
		
		for(AbstractComponent<CompoListDataItem> component : composite.getChildren()){					
			
			if(component instanceof Composite){
				
				// calculate children
				Composite<CompoListDataItem> c = (Composite<CompoListDataItem>)component;
				visitChildren(c.getData().getQty(), c.getData().getQtySubFormula(), c);							
			}
			else{
			
				// qty and sub formula qty are defined and not equal to 0
				if(parentQty != null && parentQtySubFormula != null && !parentQtySubFormula.equals(0f)){
					
					Float qtySubFormula = component.getData().getQtySubFormula();
					if(qtySubFormula != null){
						
						Float qty = qtySubFormula * parentQty / parentQtySubFormula;
						component.getData().setQty(qty);
					}
				}
			}			
		}		
	}

}
