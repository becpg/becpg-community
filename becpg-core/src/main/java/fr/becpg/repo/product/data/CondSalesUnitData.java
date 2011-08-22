/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.product.data;

import fr.becpg.repo.product.ProductVisitor;

// TODO: Auto-generated Javadoc
/**
 * Class that represents a cond sales unit.
 *
 * @author querephi
 */
public class CondSalesUnitData extends ProductData implements ProductElement {


	/**
	 * Instantiates a new cond sales unit data.
	 */
	public CondSalesUnitData(){
				
	}
	
	/* (non-Javadoc)
	 * @see fr.becpg.repo.food.ProductData#accept(fr.becpg.repo.product.ProductVisitor)
	 */
	@Override
	public void accept(ProductVisitor productVisitor) {
		
		productVisitor.visit(this);		
	}

}
