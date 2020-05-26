/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG. 
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
package fr.becpg.repo.formulation.spel;

import java.util.Collection;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.model.CompositionDataItem;
import fr.becpg.repo.variant.model.VariantData;
import fr.becpg.repo.variant.model.VariantDataItem;
import fr.becpg.repo.variant.model.VariantEntity;

public class  DataListItemSpelContext<T extends RepositoryEntity> implements SpelFormulaContext<T>{
	
	private  T entity;
	private  RepositoryEntity dataListItem;
	private  SpelFormulaService formulaService;
	


	public DataListItemSpelContext(SpelFormulaService formulaService) {
		this.formulaService = formulaService;
	}


	public T getEntity() {
		return entity;
	}


	public void setEntity(T entity) {
		this.entity = entity;
	}


	public RepositoryEntity getDataListItem() {
		return dataListItem;
	}

	public void setDataListItem(RepositoryEntity dataListItem) {
		this.dataListItem = dataListItem;
	}


	public RepositoryEntity getDataListItemEntity() {
		if (dataListItem instanceof CompositionDataItem) {

			return ((CompositionDataItem) dataListItem).getComponent() != null
					? formulaService.findOne(((CompositionDataItem) dataListItem).getComponent())
					: null;
		}
		return null;
	}

    public VariantData getVariantData() {
		if (entity != null && entity instanceof VariantEntity) {
			if (((VariantEntity)entity).getVariants() != null) {
				if (dataListItem instanceof VariantDataItem) {
					List<NodeRef> variantsNodeRef = ((VariantDataItem) dataListItem).getVariants();
					if ((variantsNodeRef != null) && !variantsNodeRef.isEmpty()) {
						for (VariantData variant : ((VariantEntity)entity).getVariants()) {
							if (variant.getNodeRef().equals(variantsNodeRef.get(0))) {
								return variant;
							}
						}
					}
				}
			}
			return ((VariantEntity)entity).getDefaultVariantData();
		}
		return new VariantData();
	}

// TODO 
//	public Collection<CompositionDataItem> children(CompoListDataItem parent) {
//		List<CompositionDataItem> ret = new ArrayList<>();
//		for (CompoListDataItem item : entity.getCompoListView().getCompoList()) {
//			if (item.getParent() != null) {
//				if (parent.equals(item.getParent())) {
//					ret.add(item);
//				}
//			}
//		}
//		return ret;
//	}
	
	public Double sum(Collection<RepositoryEntity> range, String formula) {
		return formulaService.aggreate(entity, range, formula, Operator.SUM);
	}
	
	
	public Double avg(Collection<RepositoryEntity> range, String formula) {
		return formulaService.aggreate(entity, range, formula, Operator.AVG);
	}




}

