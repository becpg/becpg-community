/*
 * 
 */
package fr.becpg.repo.product.data;

import fr.becpg.repo.product.ProductVisitor;

// TODO: Auto-generated Javadoc
/**
 * The Class PackagingKitData.
 *
 * @author querephi
 */
public class PackagingKitData extends ProductData implements ProductElement {


	/**
	 * Instantiates a new packaging kit data.
	 */
	public PackagingKitData(){
				
	}
	
	/* (non-Javadoc)
	 * @see fr.becpg.repo.food.ProductData#accept(fr.becpg.repo.product.ProductVisitor)
	 */
	@Override
	public void accept(ProductVisitor productVisitor) {
		
		productVisitor.visit(this);		
	}

}
