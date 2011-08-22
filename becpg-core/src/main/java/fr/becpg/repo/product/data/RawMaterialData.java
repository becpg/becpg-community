/*
 * 
 */
package fr.becpg.repo.product.data;

import fr.becpg.repo.product.ProductVisitor;

// TODO: Auto-generated Javadoc
/**
 * The Class RawMaterialData.
 *
 * @author querephi
 */
public class RawMaterialData extends ProductData  implements ProductElement {

	/* (non-Javadoc)
	 * @see fr.becpg.repo.food.ProductData#accept(fr.becpg.repo.product.ProductVisitor)
	 */
	@Override
	public void accept(ProductVisitor productVisitor) {
				
		productVisitor.visit(this);		
	}
}
