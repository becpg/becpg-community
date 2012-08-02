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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((qty == null) ? 0 : qty.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		IngItem other = (IngItem) obj;
		if (qty == null) {
			if (other.qty != null)
				return false;
		} else if (!qty.equals(other.qty))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "IngItem [qty=" + qty + "]";
	}
	
	
}
