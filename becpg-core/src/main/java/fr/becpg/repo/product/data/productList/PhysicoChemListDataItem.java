/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

// TODO: Auto-generated Javadoc
/**
 * The Class PhysicoChemListDataItem.
 *
 * @author querephi
 */
public class PhysicoChemListDataItem {

	/** The node ref. */
	private NodeRef nodeRef;			
	
	/** The value. */
	private Double value;
	
	/** The unit. */
	private String unit;
	
	/** The mini. */
	private Double mini;
	
	/** The maxi. */
	private Double maxi;
	
	/** The physico chem. */
	private NodeRef physicoChem;
	
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
	
	/**
	 * Gets the mini.
	 *
	 * @return the mini
	 */
	public Double getMini() {
		return mini;
	}
	
	/**
	 * Sets the mini.
	 *
	 * @param mini the new mini
	 */
	public void setMini(Double mini) {
		this.mini = mini;
	}
	
	/**
	 * Gets the maxi.
	 *
	 * @return the maxi
	 */
	public Double getMaxi() {
		return maxi;
	}
	
	/**
	 * Sets the maxi.
	 *
	 * @param maxi the new maxi
	 */
	public void setMaxi(Double maxi) {
		this.maxi = maxi;
	}
	
	/**
	 * Gets the physico chem.
	 *
	 * @return the physico chem
	 */
	public NodeRef getPhysicoChem() {
		return physicoChem;
	}
	
	/**
	 * Sets the physico chem.
	 *
	 * @param physicoChem the new physico chem
	 */
	public void setPhysicoChem(NodeRef physicoChem) {
		this.physicoChem = physicoChem;
	}
	
	/**
	 * Instantiates a new physico chem list data item.
	 */
	public PhysicoChemListDataItem(){
		
	}
	
	/**
	 * Instantiates a new physico chem list data item.
	 *
	 * @param nodeRef the node ref
	 * @param value the value
	 * @param unit the unit
	 * @param mini the mini
	 * @param maxi the maxi
	 * @param physicoChem the physico chem
	 */
	public PhysicoChemListDataItem(NodeRef nodeRef, Double value, String unit, Double mini, Double maxi, NodeRef physicoChem){
		this.nodeRef = nodeRef;
		this.value = value;
		this.unit = unit;
		this.mini = mini;
		this.maxi = maxi;
		this.physicoChem = physicoChem;
	}
	
	/**
	 * Copy constructor
	 * @param p
	 */
	public PhysicoChemListDataItem(PhysicoChemListDataItem p){
		setNodeRef(p.getNodeRef());
		setValue(p.getValue());
		setUnit(p.getUnit());
		setMini(p.getMini());
		setMaxi(p.getMaxi());
		setPhysicoChem(p.getPhysicoChem());
	}
}
