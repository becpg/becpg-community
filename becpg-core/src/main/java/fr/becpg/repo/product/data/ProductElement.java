/*
 * 
 */
package fr.becpg.repo.product.data;

import fr.becpg.repo.product.ProductVisitor;
import fr.becpg.repo.product.formulation.FormulateException;

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
	 * @throws FormulateException 
	 */
	public void accept(ProductVisitor productVisitor) throws FormulateException;
}
