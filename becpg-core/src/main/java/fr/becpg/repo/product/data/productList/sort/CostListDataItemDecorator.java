/*
 * 
 */
package fr.becpg.repo.product.data.productList.sort;

import fr.becpg.repo.product.data.productList.CostListDataItem;

// TODO: Auto-generated Javadoc
/**
 * Decorate a CostListDataItem by adding name.
 *
 * @author querephi
 */
public class CostListDataItemDecorator {
	
	/** The cost list data item. */
	private CostListDataItem costListDataItem;			
	
	/** The cost name. */
	private String costName;
	
	/**
	 * Gets the cost list data item.
	 *
	 * @return the cost list data item
	 */
	public CostListDataItem getCostListDataItem() {
		return costListDataItem;
	}
	
	/**
	 * Sets the cost list data item.
	 *
	 * @param costListDataItem the new cost list data item
	 */
	public void setCostListDataItem(CostListDataItem costListDataItem) {
		this.costListDataItem = costListDataItem;
	}
	
	/**
	 * Gets the cost name.
	 *
	 * @return the cost name
	 */
	public String getCostName() {
		return costName;
	}
	
	/**
	 * Sets the cost name.
	 *
	 * @param costName the new cost name
	 */
	public void setCostName(String costName) {
		this.costName = costName;
	}
	
}
