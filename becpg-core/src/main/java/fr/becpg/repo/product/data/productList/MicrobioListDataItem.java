/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

// TODO: Auto-generated Javadoc
/**
 * The Class MicrobioListDataItem.
 *
 * @author querephi
 */
public class MicrobioListDataItem {

	/** The node ref. */
	private NodeRef nodeRef;		
	
	/** The value. */
	private Float value;
	
	/** The unit. */
	private String unit;
	
	/** The maxi. */
	private Float maxi;
	
	/** The microbio. */
	private NodeRef microbio;
	
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
	 * Gets the maxi.
	 *
	 * @return the maxi
	 */
	public Float getMaxi() {
		return maxi;
	}
	
	/**
	 * Sets the maxi.
	 *
	 * @param maxi the new maxi
	 */
	public void setMaxi(Float maxi) {
		this.maxi = maxi;
	}
	
	/**
	 * Gets the microbio.
	 *
	 * @return the microbio
	 */
	public NodeRef getMicrobio() {
		return microbio;
	}
	
	/**
	 * Sets the microbio.
	 *
	 * @param microbio the new microbio
	 */
	public void setMicrobio(NodeRef microbio) {
		this.microbio = microbio;
	}
	
	/**
	 * Instantiates a new microbio list data item.
	 */
	public MicrobioListDataItem(){
		
	}
	
	/**
	 * Instantiates a new microbio list data item.
	 *
	 * @param nodeRef the node ref
	 * @param value the value
	 * @param unit the unit
	 * @param maxi the maxi
	 * @param microbio the microbio
	 */
	public MicrobioListDataItem(NodeRef nodeRef, Float value, String unit, Float maxi, NodeRef microbio){
		this.nodeRef = nodeRef;
		this.value = value;
		this.unit = unit;
		this.maxi = maxi;
		this.microbio = microbio;		
	}
}
