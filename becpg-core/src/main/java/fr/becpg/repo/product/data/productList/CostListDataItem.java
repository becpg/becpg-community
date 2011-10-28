/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The Class CostListDataItem.
 *
 * @author querephi
 */
public class CostListDataItem{
	
	/** The node ref. */
	private NodeRef nodeRef;
	
	/** The value. */
	private Float value = 0f;
	
	/** The unit. */
	private String unit;	
	
	/** The cost. */
	private NodeRef cost;
		
	/**
	 * Gets the node ref.
	 *
	 * @return the node ref
	 */
	public NodeRef getNodeRef() {
		return nodeRef;
	}
	
	/**
	 * Sets the node ref.
	 *
	 * @param nodeRef the new node ref
	 */
	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}
	
	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public Float getValue() {
		return value;
	}
	
	/**
	 * Sets the value.
	 *
	 * @param value the new value
	 */
	public void setValue(Float value) {
		this.value = value;
	}
	
	/**
	 * Gets the unit.
	 *
	 * @return the unit
	 */
	public String getUnit() {
		return unit;
	}
	
	/**
	 * Sets the unit.
	 *
	 * @param unit the new unit
	 */
	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	/**
	 * Gets the cost.
	 *
	 * @return the cost
	 */
	public NodeRef getCost() {
		return cost;
	}
	
	/**
	 * Sets the cost.
	 *
	 * @param cost the new cost
	 */
	public void setCost(NodeRef cost) {
		this.cost = cost;
	}
	
	/**
	 * Instantiates a new cost list data item.
	 */
	public CostListDataItem() {
		
	}
	
	/**
	 * Instantiates a new cost list data item.
	 *
	 * @param nodeRef the node ref
	 * @param value the value
	 * @param unit the unit
	 * @param cost the cost
	 */
	public CostListDataItem(NodeRef nodeRef, Float value, String unit, NodeRef cost){
		
		setNodeRef(nodeRef);		
		setValue(value);
		setUnit(unit);
		setCost(cost);
	}
	
	/**
	 * Copy constructor
	 * @param c
	 */
	public CostListDataItem(CostListDataItem c){
		
		setNodeRef(c.getNodeRef());
		setValue(c.getValue());
		setUnit(c.getUnit());
		setCost(c.getCost());
	}
}

