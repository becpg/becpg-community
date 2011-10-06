/*
 * 
 */
package fr.becpg.repo.product;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.formulation.FormulateException;

/**
 * The Interface ProductVisitor.
 *
 * @author querephi
 */
public interface ProductVisitor {

	/**
 * Visit.
 *
 * @param productData the product data
 * @return the product data
	 * @throws FormulateException 
 */
public ProductData visit(ProductData productData) throws FormulateException;

}
