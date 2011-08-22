/*
 * 
 */
package fr.becpg.repo.product.data;

import fr.becpg.repo.product.ProductVisitor;

// TODO: Auto-generated Javadoc
/**
 * The Class SemiFinishedProductData.
 *
 * @author querephi
 */
public class SemiFinishedProductData extends ProductData implements ProductElement {

	/* (non-Javadoc)
	 * @see fr.becpg.repo.food.ProductData#accept(fr.becpg.repo.product.ProductVisitor)
	 */
	@Override
	public void accept(ProductVisitor productVisitor) {
		
//		for(CompoListDataItem compoItem : this.getCompoList()){
//			ProductData part = compoItem.getProduct();
//			
//			if(part instanceof ProductElement){
//				((ProductElement)part).accept(productVisitor);
//			}
//		}
		
		productVisitor.visit(this);		
	}
}
