/*
 * 
 */
package fr.becpg.repo.product.data.ing;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

// TODO: Auto-generated Javadoc
/**
 * The Class IngItem.
 *
 * @author querephi
 */
public class IngItem extends AbstractIng {
	
	/** The qty. */
	private Double qty;	
	
	/* (non-Javadoc)
	 * @see fr.becpg.repo.food.ing.Ing#getQty()
	 */
	@Override
	public Double getQty() {
		return qty;
	}		
	
	/**
	 * Sets the qty.
	 *
	 * @param qty the new qty
	 */
	public void setQty(Double qty) {
		this.qty = qty;
	}
	
	/**
	 * Instantiates a new ing item.
	 *
	 * @param ing
	 * @param mlName the ml name
	 * @param qty the qty
	 */
	public IngItem(NodeRef ing, MLText mlName, Double qty){
		super(ing, mlName);
		this.qty = qty;
	}
}
