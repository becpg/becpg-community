/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompositionDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;

public class FormulaFormulationContext {
	
	private static Log logger = LogFactory.getLog(FormulaFormulationContext.class);
	
	private ProductData entity;
	private CompositionDataItem dataListItem;
	private AlfrescoRepository<ProductData> alfrescoRepository;
	
	private enum Operator {
		SUM,AVG,PERC;
	}

	public FormulaFormulationContext(AlfrescoRepository<ProductData> alfrescoRepository, ProductData entity, CompositionDataItem dataListItem) {
		super();
		this.entity = entity;
		this.dataListItem = dataListItem;
		this.alfrescoRepository = alfrescoRepository;
	}

	public ProductData getEntity() {
		return entity;
	}

	public CompositionDataItem getDataListItem() {
		return dataListItem;
	}

	public ProductData getDataListItemEntity() {
		return dataListItem.getProduct() != null ? alfrescoRepository.findOne(dataListItem.getProduct()) : null;
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
		return aggreate(range, formula, Operator.SUM);
	}
	
	
	public Double avg(Collection<CompositionDataItem> range, String formula) {
		return aggreate(range, formula, Operator.AVG);
	}
	
	private Double aggreate(Collection<CompositionDataItem> range, String formula, Operator operator){

		if(logger.isDebugEnabled()){
			logger.debug("Running aggregate fonction ["+formula+"] on range ("+range.size()+") for operator "+operator );
		}
		
		ExpressionParser parser = new SpelExpressionParser();
		Expression exp = parser.parseExpression(formula);
		Double sum = 0d;
        int count = 0;
		for (CompositionDataItem item : range) {
			EvaluationContext dataContext = new StandardEvaluationContext(new FormulaFormulationContext(alfrescoRepository, entity, item));
			Double value = exp.getValue(dataContext, Double.class);
			if (value != null) {
				sum += value;
				count++;
			} else {
				logger.debug("Value is null for ["+formula+"] on "+item.toString());
			}
		}
		if(Operator.AVG.equals(operator)){
			sum /=count;
		}
		
		return sum;
	}
	

}

