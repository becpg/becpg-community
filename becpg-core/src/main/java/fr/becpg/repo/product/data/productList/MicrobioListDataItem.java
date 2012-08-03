/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.product.data.BaseObject;

// TODO: Auto-generated Javadoc
/**
 * The Class MicrobioListDataItem.
 *
 * @author querephi
 */
public class MicrobioListDataItem extends BaseObject {

	/** The node ref. */
	private NodeRef nodeRef;		
	
	/** The value. */
	private Double value;
	
	/** The unit. */
	private String unit;
	
	/** The maxi. */
	private Double maxi;
	
	private String textCriteria;
	
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
	
	public String getTextCriteria() {
		return textCriteria;
	}

	public void setTextCriteria(String textCriteria) {
		this.textCriteria = textCriteria;
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
	public MicrobioListDataItem(NodeRef nodeRef, Double value, String unit, Double maxi, String textCriteria, NodeRef microbio){
		this.nodeRef = nodeRef;
		this.value = value;
		this.unit = unit;
		this.maxi = maxi;
		this.microbio = microbio;		
		this.textCriteria = textCriteria;
	}
	
	/**
	 * Copy constructor
	 * @param m
	 */
	public MicrobioListDataItem(MicrobioListDataItem m){
		setNodeRef(m.getNodeRef());
		setValue(m.getValue());
		setUnit(m.getUnit());
		setMaxi(m.getMaxi());
		setTextCriteria(m.getTextCriteria());
		setMicrobio(m.getMicrobio());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((maxi == null) ? 0 : maxi.hashCode());
		result = prime * result + ((microbio == null) ? 0 : microbio.hashCode());
		result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
		result = prime * result + ((textCriteria == null) ? 0 : textCriteria.hashCode());
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
		MicrobioListDataItem other = (MicrobioListDataItem) obj;
		if (maxi == null) {
			if (other.maxi != null)
				return false;
		} else if (!maxi.equals(other.maxi))
			return false;
		if (microbio == null) {
			if (other.microbio != null)
				return false;
		} else if (!microbio.equals(other.microbio))
			return false;
		if (nodeRef == null) {
			if (other.nodeRef != null)
				return false;
		} else if (!nodeRef.equals(other.nodeRef))
			return false;
		if (textCriteria == null) {
			if (other.textCriteria != null)
				return false;
		} else if (!textCriteria.equals(other.textCriteria))
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
		return "MicrobioListDataItem [nodeRef=" + nodeRef + ", value=" + value + ", unit=" + unit + ", maxi=" + maxi + ", textCriteria=" + textCriteria + ", microbio=" + microbio
				+ "]";
	}
	
	
}
