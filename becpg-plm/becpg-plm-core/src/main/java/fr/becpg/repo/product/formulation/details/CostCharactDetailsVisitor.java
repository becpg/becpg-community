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
package fr.becpg.repo.product.formulation.details;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.data.CharactDetails;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.formulation.CostsCalculatingFormulationHandler;
import fr.becpg.repo.product.formulation.FormulationHelper;

@Service
public class CostCharactDetailsVisitor extends SimpleCharactDetailsVisitor {

	private static Log logger = LogFactory.getLog(CostCharactDetailsVisitor.class);

	@Override
	public CharactDetails visit(ProductData productData, List<NodeRef> dataListItems) throws FormulateException {

		CharactDetails ret = new CharactDetails(extractCharacts(dataListItems));
		Double netQty = FormulationHelper.getNetQtyInLorKg(productData,FormulationHelper.DEFAULT_NET_WEIGHT);

		/*
		 * Calculate cost details
		 */
		if (productData.hasCompoListEl(EffectiveFilters.EFFECTIVE)) {		
			Composite<CompoListDataItem> composite = CompositeHelper.getHierarchicalCompoList(productData.getCompoList(EffectiveFilters.EFFECTIVE));		
			visitCompoListChildren(productData, composite, ret, CostsCalculatingFormulationHandler.DEFAULT_LOSS_RATIO, netQty);
		}		

		/*
		 * Calculate the costs of the packaging
		 */
		if (productData.hasPackagingListEl(EffectiveFilters.EFFECTIVE)) {
			for (PackagingListDataItem packagingListDataItem : productData.getPackagingList(EffectiveFilters.EFFECTIVE)) {
				Double qty = FormulationHelper.getQty(packagingListDataItem).doubleValue();
				visitPart(packagingListDataItem.getProduct(), ret, qty, netQty);

			}
		}

		/*
		 * Calculate the costs of the processes
		 */
		if (productData.hasProcessListEl(EffectiveFilters.EFFECTIVE)) {
			for (ProcessListDataItem processListDataItem : productData.getProcessList(EffectiveFilters.EFFECTIVE)) {
				Double qty = FormulationHelper.getQty(productData, processListDataItem);
				visitPart(processListDataItem.getResource(), ret, qty, netQty);
			}
		}

		return ret;
	}
	
	private void visitCompoListChildren(ProductData formulatedProduct, Composite<CompoListDataItem> composite, CharactDetails ret, Double parentLossRatio, Double netQty) throws FormulateException{
		
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
				visitCompoListChildren(formulatedProduct, c, ret, newLossPerc, netQty);							
			}
			else{
				CompoListDataItem compoListDataItem = component.getData();
				Double qty = FormulationHelper.getQtyForCost(compoListDataItem, 
						parentLossRatio, 
						ProductUnit.getUnit((String)nodeService.getProperty(compoListDataItem.getProduct(), PLMModel.PROP_PRODUCT_UNIT)));
				visitPart(compoListDataItem.getProduct(), ret, qty, netQty);
			}			
		}
	}
}
