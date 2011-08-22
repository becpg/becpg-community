/*
 * 
 */
package fr.becpg.repo.product.data.productList.sort;

import fr.becpg.repo.product.data.productList.NutListDataItem;

// TODO: Auto-generated Javadoc
/**
 * The Class NutListDataItemDecorator.
 *
 * @author querephi
 */
public class NutListDataItemDecorator {

	/** The nut list data item. */
	private NutListDataItem nutListDataItem;	
	
	/** The nut name. */
	private String nutName;
	
	/**
	 * Gets the nut list data item.
	 *
	 * @return the nut list data item
	 */
	public NutListDataItem getNutListDataItem() {
		return nutListDataItem;
	}
	
	/**
	 * Sets the nut list data item.
	 *
	 * @param nutListDataItem the new nut list data item
	 */
	public void setNutListDataItem(NutListDataItem nutListDataItem) {
		this.nutListDataItem = nutListDataItem;
	}
	
	/**
	 * Gets the nut name.
	 *
	 * @return the nut name
	 */
	public String getNutName() {
		return nutName;
	}
	
	/**
	 * Sets the nut name.
	 *
	 * @param nutName the new nut name
	 */
	public void setNutName(String nutName) {
		this.nutName = nutName;
	}
}
