/*
 * 
 */
package fr.becpg.repo.product.data;

import fr.becpg.repo.product.ProductVisitor;

// TODO: Auto-generated Javadoc
/**
 * The Interface ProductElement.
 *
 * @author querephi
 */
public interface ProductElement {
	
	/**
	 * Accept.
	 *
	 * @param productVisitor the product visitor
	 */
	public void accept(ProductVisitor productVisitor);
}
