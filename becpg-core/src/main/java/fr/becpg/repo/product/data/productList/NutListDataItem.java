/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

// TODO: Auto-generated Javadoc
/**
 * The Class NutListDataItem.
 *
 * @author querephi
 */
public class NutListDataItem{

	/** The node ref. */
	private NodeRef nodeRef;
	
	/** The value. */
	private Float value;
	
	/** The unit. */
	private String unit;
	
	private Float mini;
	
	private Float maxi;
	
	/** The group. */
	private String group;
	
	/** The nut. */
	private NodeRef nut;
			
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
	
	public Float getMini() {
		return mini;
	}

	public void setMini(Float mini) {
		this.mini = mini;
	}

	public Float getMaxi() {
		return maxi;
	}

	public void setMaxi(Float maxi) {
		this.maxi = maxi;
	}

	/**
	 * Gets the group.
	 *
	 * @return the group
	 */
	public String getGroup() {
		return group;
	}
	
	/**
	 * Sets the group.
	 *
	 * @param group the new group
	 */
	public void setGroup(String group) {
		this.group = group;
	}
	
	/**
	 * Gets the nut.
	 *
	 * @return the nut
	 */
	public NodeRef getNut() {
		return nut;
	}
	
	/**
	 * Sets the nut.
	 *
	 * @param nut the new nut
	 */
	public void setNut(NodeRef nut) {
		this.nut = nut;
	}	
	
	/**
	 * Instantiates a new nut list data item.
	 */
	public NutListDataItem()
	{
	}
	
	/**
	 * Instantiates a new nut list data item.
	 *
	 * @param nodeRef the node ref
	 * @param value the value
	 * @param unit the unit
	 * @param group the group
	 * @param nut the nut
	 */
	public NutListDataItem(NodeRef nodeRef,	Float value, String unit, Float mini, Float maxi, String group, NodeRef nut)
	{
		setNodeRef(nodeRef);
		setValue(value);
		setUnit(unit);
		setMini(mini);
		setMaxi(maxi);
		setGroup(group);
		setNut(nut);
	}
	
	/**
	 * Copy constructor
	 * @param n
	 */
	public NutListDataItem(NutListDataItem n){

		setNodeRef(n.getNodeRef());
		setValue(n.getValue());
		setUnit(n.getUnit());
		setMini(n.getMini());
		setMaxi(n.getMaxi());
		setGroup(n.getGroup());
		setNut(n.getNut());
    }
}
