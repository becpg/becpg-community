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

import java.math.BigDecimal;

import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.PackModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.TareUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.variant.filters.VariantFilters;

public class TareFormulationHandler extends FormulationBaseHandler<ProductData> {
	
	private static Log logger = LogFactory.getLog(TareFormulationHandler.class);
	
	private NodeService nodeService;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {

		logger.debug("Tare visitor");		
		
		// no compo => no formulation
		if(!formulatedProduct.hasCompoListEl(EffectiveFilters.EFFECTIVE, VariantFilters.DEFAULT_VARIANT) && 
				!formulatedProduct.hasPackagingListEl(EffectiveFilters.EFFECTIVE, VariantFilters.DEFAULT_VARIANT)){			
			logger.debug("no compoList, no packagingList => no formulation");
			return true;
		}
		
		// Tare
		BigDecimal tare = calculateTareOfComposition(formulatedProduct);
		tare =  tare.add(calculateTareOfPackaging(formulatedProduct));
		formulatedProduct.setTareUnit(TareUnit.kg);
	
		if(tare.doubleValue() < 1){
			logger.debug("Calculating tare in g: "+tare);
			tare = tare.multiply(new BigDecimal(1000d));
			formulatedProduct.setTareUnit(TareUnit.g);
		}
		formulatedProduct.setTare(tare.doubleValue());
		return true;
	}	
	
	@SuppressWarnings("unchecked")
	private BigDecimal calculateTareOfComposition(ProductData formulatedProduct){
		BigDecimal totalTare = new BigDecimal(0d);
		for(CompoListDataItem compoList : formulatedProduct.getCompoList(EffectiveFilters.EFFECTIVE, VariantFilters.DEFAULT_VARIANT)){			
			totalTare = totalTare.add(FormulationHelper.getTareInKg(compoList, nodeService));
		}			
		return totalTare;
	}
	
	@SuppressWarnings("unchecked")
	private BigDecimal calculateTareOfPackaging(ProductData formulatedProduct){
		BigDecimal totalTare  = new BigDecimal(0d);
		for(PackagingListDataItem packList : formulatedProduct.getPackagingList(EffectiveFilters.EFFECTIVE, VariantFilters.DEFAULT_VARIANT)){
			// take in account only primary
			PackagingLevel level = PackagingLevel.Primary;
			if(nodeService.hasAspect(formulatedProduct.getNodeRef(), PackModel.ASPECT_PALLET)){
			   level = PackagingLevel.Secondary;
			}
			
			if(packList.getPkgLevel() != null && packList.getPkgLevel().equals(level)){			
				totalTare = totalTare.add(FormulationHelper.getTareInKg(packList, nodeService));
			}			
		}			
		return totalTare;
	}
	
}
