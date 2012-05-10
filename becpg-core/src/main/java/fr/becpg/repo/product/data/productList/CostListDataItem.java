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
public class CostListDataItem implements IManualDataItem{
	
	/** The node ref. */
	private NodeRef nodeRef;
	
	/** The value. */
	private Double value = 0d;
	
	/** The unit. */
	private String unit;	
	
	private Double maxi = null;
	
	/** The cost. */
	private NodeRef cost;
	
	private Boolean isManual;
		
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
	
	public Double getMaxi() {
		return maxi;
	}

	public void setMaxi(Double maxi) {
		this.maxi = maxi;
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
	
	@Override
	public Boolean getIsManual() {

		return isManual;
	}

	@Override
	public void setIsManual(Boolean isManual) {
		
		this.isManual = isManual;		
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
	public CostListDataItem(NodeRef nodeRef, Double value, String unit, Double maxi, NodeRef cost, Boolean isManual){
		
		setNodeRef(nodeRef);		
		setValue(value);
		setUnit(unit);
		setMaxi(maxi);
		setCost(cost);
		setIsManual(isManual);
	}
	
	/**
	 * Copy constructor
	 * @param c
	 */
	public CostListDataItem(CostListDataItem c){
		
		setNodeRef(c.getNodeRef());
		setValue(c.getValue());
		setUnit(c.getUnit());
		setMaxi(c.getMaxi());
		setCost(c.getCost());
		setIsManual(c.getIsManual());
	}

	@Override
	public String toString() {
		return "CostListDataItem [nodeRef=" + nodeRef + ", value=" + value + ", unit=" + unit + ", maxi=" + maxi
				+ ", cost=" + cost + ", isManual=" + isManual + "]";
	}
}

