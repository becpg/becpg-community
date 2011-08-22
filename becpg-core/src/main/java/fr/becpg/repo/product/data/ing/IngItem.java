/*
 * 
 */
package fr.becpg.repo.product.data.ing;

import org.alfresco.service.cmr.repository.MLText;

// TODO: Auto-generated Javadoc
/**
 * The Class IngItem.
 *
 * @author querephi
 */
public class IngItem extends AbstractIng {
	
	/** The qty. */
	private float qty;	
	
	/* (non-Javadoc)
	 * @see fr.becpg.repo.food.ing.Ing#getQty()
	 */
	@Override
	public float getQty() {
		return qty;
	}		
	
	/**
	 * Sets the qty.
	 *
	 * @param qty the new qty
	 */
	public void setQty(float qty) {
		this.qty = qty;
	}
	
	/**
	 * Instantiates a new ing item.
	 *
	 * @param name the name
	 * @param mlName the ml name
	 * @param qty the qty
	 */
	public IngItem(String name, MLText mlName, Float qty){
		super(name, mlName);
		this.qty = qty;
	}
}
