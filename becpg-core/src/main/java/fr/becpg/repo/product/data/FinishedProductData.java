/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.product.data;

import fr.becpg.repo.product.ProductVisitor;

// TODO: Auto-generated Javadoc
/**
 * Class that represents a finished product.
 *
 * @author querephi
 */
public class FinishedProductData extends ProductData implements ProductElement {


	/**
	 * Instantiates a new finished product data.
	 */
	public FinishedProductData(){
				
	}
	
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
