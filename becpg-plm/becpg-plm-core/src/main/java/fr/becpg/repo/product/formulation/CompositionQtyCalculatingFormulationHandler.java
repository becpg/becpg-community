/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.product.formulation;

import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.CompoListUnit;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.variant.filters.VariantFilters;

public class CompositionQtyCalculatingFormulationHandler extends FormulationBaseHandler<ProductData> {
	
	private static Log logger = LogFactory.getLog(CompositionQtyCalculatingFormulationHandler.class);
	
	private NodeService nodeService;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {

		logger.debug("Composition calculating visitor");		
		
		// no compo => no formulation
		if(!formulatedProduct.hasCompoListEl(EffectiveFilters.ALL, VariantFilters.DEFAULT_VARIANT)){			
			logger.debug("no compo => no formulation");
			return true;
		}
		
		//Take in account net weight
		if(formulatedProduct.getQty() != null && formulatedProduct.getUnit() != null){
			if(ProductUnit.g.equals(formulatedProduct.getUnit())){
				formulatedProduct.setNetWeight(formulatedProduct.getQty() / 1000);
			}
			else if(ProductUnit.kg.equals(formulatedProduct.getUnit())){
				formulatedProduct.setNetWeight(formulatedProduct.getQty());
			}
		}
		Double netWeight = formulatedProduct.getNetWeight();			
		Composite<CompoListDataItem> compositeAll = CompositeHelper.getHierarchicalCompoList(formulatedProduct.getCompoList(EffectiveFilters.ALL));
		
		// calculate on every item		
		visitQtyChildren(formulatedProduct, netWeight, compositeAll);
		
		return true;
	}
	
	private void visitQtyChildren(ProductData formulatedProduct, Double parentQty, Composite<CompoListDataItem> composite) throws FormulateException{				
		
		for(Composite<CompoListDataItem> component : composite.getChildren()){					
			
			Double qtySubFormula = FormulationHelper.getQtySubFormula(component.getData(), nodeService);
			logger.debug("qtySubFormula: " + qtySubFormula + " parentQty: " + parentQty);
			if(qtySubFormula != null){
									
				// take in account percentage
				if(CompoListUnit.Perc.equals(component.getData().getCompoListUnit()) &&
						parentQty != null && !parentQty.equals(0d)){
					qtySubFormula = qtySubFormula * parentQty / 100;
				}
				
				// Take in account yield that is defined on component
				Double qty = null;
				if(component.isLeaf()){						
					qty = qtySubFormula * 100 / FormulationHelper.getYield(component.getData());
				}
				else{
					qty = qtySubFormula;
				}													
				component.getData().setQty(qty);					
			}
			
			// calculate children
			if(!component.isLeaf()){
				
				// take in account percentage
				if(	CompoListUnit.Perc.equals(component.getData().getCompoListUnit()) &&
						parentQty != null && !parentQty.equals(0d)){	
					
					visitQtyChildren(formulatedProduct, parentQty, component);
					
					// no yield but calculate % of composite
					Double compositePerc = 0d;
					boolean isUnitPerc = true;
					for(Composite<CompoListDataItem> child : component.getChildren()){	
						compositePerc += child.getData().getQtySubFormula();
						isUnitPerc = isUnitPerc && CompoListUnit.Perc.equals(child.getData().getCompoListUnit());
						if(!isUnitPerc){
							break;
						}
					}
					if(isUnitPerc){
						component.getData().setQtySubFormula(compositePerc);
						component.getData().setQty(compositePerc * parentQty / 100);
					}
				}
				else{
					visitQtyChildren(formulatedProduct, component.getData().getQty(),component);					
				}				
			}			
		}
	}
}
