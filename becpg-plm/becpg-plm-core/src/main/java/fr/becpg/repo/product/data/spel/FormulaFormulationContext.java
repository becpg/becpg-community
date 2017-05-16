/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG. 
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
package fr.becpg.repo.product.data.spel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompositionDataItem;
import fr.becpg.repo.product.formulation.FormulaService;

public class FormulaFormulationContext {
	
	private final ProductData entity;
	private final CompositionDataItem dataListItem;
	private final FormulaService formulaService;
	
	public enum Operator {
		SUM,AVG,PERC
	}

	public FormulaFormulationContext(FormulaService formulaService, ProductData entity, CompositionDataItem dataListItem) {
		super();
		this.entity = entity;
		this.dataListItem = dataListItem;
		this.formulaService = formulaService;
	}

	public ProductData getEntity() {
		return entity;
	}

	public CompositionDataItem getDataListItem() {
		return dataListItem;
	}

	public ProductData getDataListItemEntity() {
		return dataListItem.getComponent() != null ? formulaService.findOne(dataListItem.getComponent()) : null;
	}

	
	public Collection<CompositionDataItem> children(CompoListDataItem parent){
		List<CompositionDataItem> ret = new ArrayList<>();
		for (CompoListDataItem item : entity.getCompoListView().getCompoList()) {
			if(item.getParent()!=null){
				if(parent.equals(item.getParent())){
					ret.add(item);
				}
			}
		}
		return ret;
	}
	
	public Double sum(Collection<CompositionDataItem> range, String formula) {
		return formulaService.aggreate(entity, range, formula, Operator.SUM);
	}
	
	
	public Double avg(Collection<CompositionDataItem> range, String formula) {
		return formulaService.aggreate(entity, range, formula, Operator.AVG);
	}
	

}

