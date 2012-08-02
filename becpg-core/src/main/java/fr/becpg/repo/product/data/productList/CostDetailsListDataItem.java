/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.product.data.BaseObject;

/**
 * The Class CostDetailsListDataItem.
 *
 * @author querephi
 */
public class CostDetailsListDataItem extends BaseObject{
	
	/** The node ref. */
	private NodeRef nodeRef;
	
	/** The value. */
	private Double value = 0d;
	
	/** The unit. */
	private String unit;	
	
	private Double percentage;
	
	/** The cost. */
	private NodeRef cost;
	
	private NodeRef source;
		
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
	public Double getValue() {
		return value;
	}
	
	/**
	 * Sets the value.
	 *
	 * @param value the new value
	 */
	public void setValue(Double value) {
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
	
	public Double getPercentage() {
		return percentage;
	}

	public void setPercentage(Double percentage) {
		this.percentage = percentage;
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
	
	public NodeRef getSource() {
		return source;
	}

	public void setSource(NodeRef source) {
		this.source = source;
	}

	/**
	 * Instantiates a new cost list data item.
	 */
	public CostDetailsListDataItem() {
		
	}
	
	/**
	 * Instantiates a new cost list data item.
	 *
	 * @param nodeRef the node ref
	 * @param value the value
	 * @param unit the unit
	 * @param cost the cost
	 */
	public CostDetailsListDataItem(NodeRef nodeRef, Double value, String unit, Double percentage, NodeRef cost, NodeRef source){
		
		setNodeRef(nodeRef);		
		setValue(value);
		setUnit(unit);
		setPercentage(percentage);
		setCost(cost);
		setSource(source);
	}
	
	/**
	 * Copy constructor
	 * @param c
	 */
	public CostDetailsListDataItem(CostDetailsListDataItem c){
		
		setNodeRef(c.getNodeRef());
		setValue(c.getValue());
		setUnit(c.getUnit());
		setPercentage(c.getPercentage());
		setCost(c.getCost());
		setSource(c.getSource());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cost == null) ? 0 : cost.hashCode());
		result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
		result = prime * result + ((percentage == null) ? 0 : percentage.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CostDetailsListDataItem other = (CostDetailsListDataItem) obj;
		if (cost == null) {
			if (other.cost != null)
				return false;
		} else if (!cost.equals(other.cost))
			return false;
		if (nodeRef == null) {
			if (other.nodeRef != null)
				return false;
		} else if (!nodeRef.equals(other.nodeRef))
			return false;
		if (percentage == null) {
			if (other.percentage != null)
				return false;
		} else if (!percentage.equals(other.percentage))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (unit == null) {
			if (other.unit != null)
				return false;
		} else if (!unit.equals(other.unit))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CostDetailsListDataItem [nodeRef=" + nodeRef + ", value=" + value + ", unit=" + unit + ", percentage=" + percentage + ", cost=" + cost + ", source=" + source + "]";
	}
	
	
}

