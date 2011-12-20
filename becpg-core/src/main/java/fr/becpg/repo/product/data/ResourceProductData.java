/*
 * 
 */
package fr.becpg.repo.product.data;

import fr.becpg.repo.product.ProductVisitor;
import fr.becpg.repo.product.formulation.FormulateException;

/**
 * The Class ResourceProductData.
 *
 * @author querephi
 */
public class ResourceProductData extends ProductData implements ProductElement  {

	/* (non-Javadoc)
	 * @see fr.becpg.repo.food.ProductData#accept(fr.becpg.repo.product.ProductVisitor)
	 */
	@Override
	public void accept(ProductVisitor productVisitor) throws FormulateException {
				
		productVisitor.visit(this);		
	}
}
