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

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.variant.filters.VariantFilters;

public class CompositionCalculatingFormulationHandler extends FormulationBaseHandler<ProductData> {
	
	private static Log logger = LogFactory.getLog(CompositionCalculatingFormulationHandler.class);
	
	private NodeService nodeService;
	
	private AlfrescoRepository<ProductData> alfrescoRepository;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
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
		Composite<CompoListDataItem> compositeDefaultVariant = CompositeHelper.getHierarchicalCompoList(formulatedProduct.getCompoList(EffectiveFilters.ALL, VariantFilters.DEFAULT_VARIANT));
		
		// calculate on every item		
		visitQtyChildren(formulatedProduct, netWeight, compositeAll);
		
		// Yield		
		visitYieldChildren(formulatedProduct, compositeDefaultVariant);
			
		Double qtyUsed = calculateQtyUsed(compositeDefaultVariant);
		formulatedProduct.setRecipeQtyUsed(qtyUsed);
		if(netWeight != null && qtyUsed != null && qtyUsed != 0d){
			formulatedProduct.setYield(100 * netWeight / qtyUsed);
		}
		
		// Volume
		Double netVolume = FormulationHelper.getNetVolume(formulatedProduct.getNodeRef(), nodeService);
		if(netVolume != null){
			Double calculatedVolume = calculateVolumeFromChildren(compositeDefaultVariant);
			if(calculatedVolume != 0d){
				formulatedProduct.setYieldVolume(100 * netVolume / calculatedVolume);
			}			
		}
		
		// generic raw material
		if(formulatedProduct instanceof RawMaterialData){
			((RawMaterialData) formulatedProduct).setSuppliers(calculateSuppliers(compositeAll));
		}
		
		return true;
	}
	
	private void visitQtyChildren(ProductData formulatedProduct, Double parentQty, Composite<CompoListDataItem> composite) throws FormulateException{				
		
		for(Composite<CompoListDataItem> component : composite.getChildren()){					
			
			Double qtySubFormula = FormulationHelper.getQtySubFormula(component.getData(), nodeService);
			logger.debug("qtySubFormula: " + qtySubFormula + " parentQty: " + parentQty);
			if(qtySubFormula != null){
									
				// take in account percentage
				if(component.getData().getCompoListUnit() != null && 
						component.getData().getCompoListUnit().equals(CompoListUnit.Perc) &&
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
			
			// calculate volume ?			
			Double volume = FormulationHelper.getNetVolume(component.getData(), nodeService); 
			component.getData().setVolume(volume);
			
			// calculate children
			if(!component.isLeaf()){
				
				// take in account percentage
				if(component.getData().getCompoListUnit() != null && 
						component.getData().getCompoListUnit().equals(CompoListUnit.Perc) &&
						parentQty != null && !parentQty.equals(0d)){	
					
					visitQtyChildren(formulatedProduct, parentQty, component);
					
					// no yield but calculate % of composite
					Double compositePerc = 0d;
					for(Composite<CompoListDataItem> child : component.getChildren()){	
						compositePerc += child.getData().getQtySubFormula();
					}
					component.getData().setQtySubFormula(compositePerc);
					component.getData().setQty(compositePerc * parentQty / 100);
				}
				else{
					
					visitQtyChildren(formulatedProduct, component.getData().getQty(),component);					
				}				
			}			
		}
	}

	private void visitYieldChildren(ProductData formulatedProduct, Composite<CompoListDataItem> composite) throws FormulateException{				
		
		for(Composite<CompoListDataItem> component : composite.getChildren()){					
			
			// calculate children
			if(!component.isLeaf()){
				
				// take in account percentage
				if(component.getData().getCompoListUnit() != null && 
						component.getData().getCompoListUnit().equals(CompoListUnit.Perc)){	
					
					visitYieldChildren(formulatedProduct, component);
					component.getData().setYieldPerc(null);
				}
				else{
					visitYieldChildren(formulatedProduct,component);
					
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
			
			Double qty = FormulationHelper.getQty(component.getData());
			if(qty != null){
				// water can be taken in account on Raw Material
				if(component.isLeaf()){
					qtyUsed += qty * FormulationHelper.getYield(component.getData()) / 100;
				}
				else{
					qtyUsed += qty;
				}				
			}
		}
		
		// qty after process
		Double qtyAfterProcess = FormulationHelper.getQtySubFormula(composite.getData(), nodeService);		
		if(qtyAfterProcess != null && qtyAfterProcess != 0 && qtyUsed != 0){
			yieldPerc = qtyAfterProcess / qtyUsed * 100;
		}
		
		if(logger.isDebugEnabled()){
			logger.debug("component: " + nodeService.getProperty(composite.getData().getProduct(),  ContentModel.PROP_NAME) + 
					" qtyAfterProcess: " + qtyAfterProcess + " - qtyUsed: " + qtyUsed + " yieldPerc: " + yieldPerc);
		}
		
		return yieldPerc;
	}	
	
	private Double calculateQtyUsed(Composite<CompoListDataItem> composite) throws FormulateException{				
		
		Double qty = 0d;
		
		for(Composite<CompoListDataItem> component : composite.getChildren()){						
			
			if(!component.isLeaf()){
				// calculate children
				qty += calculateQtyUsed(component);
			}else{
				Double qtyInKg =  FormulationHelper.getQtyInKg(component.getData());
				if(qtyInKg!=null) {
					qty += qtyInKg * FormulationHelper.getYield(component.getData()) / 100;				
				}
			}
		}
		return qty;
	}
	
	private Double calculateVolumeFromChildren(Composite<CompoListDataItem> composite) throws FormulateException{				
		
		Double volume = 0d;
		
		for(Composite<CompoListDataItem> component : composite.getChildren()){						
			
			Double value = null;
			
			if(!component.isLeaf()){
				// calculate children
				value = calculateVolumeFromChildren(component);
				component.getData().setVolume(value);				
				
			}else{
				value = (Double)component.getData().getVolume();
				value = value != null ? value : 0d;
			}			
			volume += value;
		}		
		return volume;
	}
	
	private List<NodeRef> calculateSuppliers(Composite<CompoListDataItem> composite){
		List<NodeRef> supplierNodeRefs = new ArrayList<>();		
		for(Composite<CompoListDataItem> component : composite.getChildren()){			
			ProductData productData = alfrescoRepository.findOne(component.getData().getProduct());
			if(productData instanceof RawMaterialData){
				for(NodeRef supplierNodeRef : ((RawMaterialData)productData).getSuppliers()){
					if(!supplierNodeRefs.contains(supplierNodeRef)){
						supplierNodeRefs.add(supplierNodeRef);
					}
				}
			}			
		}
		return supplierNodeRefs;
	}
}
