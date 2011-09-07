/*
 * 
 */
package fr.becpg.repo.entity.comparison;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;


// TODO: Auto-generated Javadoc
/**
 * Comparable item that have children.
 *
 * @author querephi
 */
public class CompositeComparableItem extends AbstractComparableItem {

	//key : is composed from pivot fields and position (if the concatenation of pivot fields is present several times)
	//value : the node datalist 
	/** The item list. */
	protected Map<String, AbstractComparableItem> itemList = new HashMap<String, AbstractComparableItem>();
	
	/**
	 * Gets the item list.
	 *
	 * @return the item list
	 */
	public Map<String, AbstractComparableItem> getItemList() {
		return itemList;
	}

	/**
	 * Sets the item list.
	 *
	 * @param itemList the item list
	 */
	public void setItemList(Map<String, AbstractComparableItem> itemList) {
		this.itemList = itemList;
	}

	/**
	 * Adds the.
	 *
	 * @param pivot the pivot
	 * @param item the item
	 */
	public void add(String pivot, AbstractComparableItem item){
		itemList.put(pivot, item);
	}
	
	/**
	 * Removes the.
	 *
	 * @param pivot the pivot
	 * @param item the item
	 */
	public void remove(String pivot, AbstractComparableItem item){
		itemList.remove(pivot);
	}
	
	/**
	 * Gets the.
	 *
	 * @param pivot the pivot
	 * @return the abstract comparable item
	 */
	public AbstractComparableItem get(String pivot){		
		return itemList.get(pivot);
	}
	
	/**
	 * Instantiates a new composite comparable item.
	 *
	 * @param depthLevel the depth level
	 * @param pivot the pivot
	 * @param nodeRef the node ref
	 */
	public CompositeComparableItem(int depthLevel, String pivot, NodeRef nodeRef){
		super(depthLevel, pivot, nodeRef);
	}
}
