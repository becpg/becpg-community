/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.product.data;

import fr.becpg.repo.product.ProductVisitor;
import fr.becpg.repo.product.formulation.FormulateException;

// TODO: Auto-generated Javadoc
/**
 * Class that represents a local semi finished product.
 *
 * @author querephi
 */
public class LocalSemiFinishedProduct extends ProductData  implements ProductElement {

	/* (non-Javadoc)
	 * @see fr.becpg.repo.food.ProductData#accept(fr.becpg.repo.product.ProductVisitor)
	 */
	@Override
	public void accept(ProductVisitor productVisitor) throws FormulateException {
				
		productVisitor.visit(this);		
	}
}
