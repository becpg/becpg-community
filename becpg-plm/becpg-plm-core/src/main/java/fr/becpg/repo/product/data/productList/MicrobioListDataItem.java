/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

@AlfType
@AlfQname(qname = "bcpg:microbioList")
public class MicrobioListDataItem extends BeCPGDataObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6910021090157153860L;

	private Double value;
	
	private String unit;
	
	private Double maxi;
	
	private String textCriteria;
	
	private NodeRef microbio;
	
	
	@AlfProp
	@AlfQname(qname="bcpg:mblValue")
	public Double getValue() {
		return value;
	}
	
	
	public void setValue(Double value) {
		this.value = value;
	}
	
	@AlfProp
	@AlfQname(qname="bcpg:mblUnit")
	public String getUnit() {
		return unit;
	}
	
	
	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	@AlfProp
	@AlfQname(qname="bcpg:mblMaxi")
	public Double getMaxi() {
		return maxi;
	}
	
	
	public void setMaxi(Double maxi) {
		this.maxi = maxi;
	}
	
	@AlfProp
	@AlfQname(qname="bcpg:mblTextCriteria")
	public String getTextCriteria() {
		return textCriteria;
	}

	public void setTextCriteria(String textCriteria) {
		this.textCriteria = textCriteria;
	}

	@AlfSingleAssoc
	@DataListIdentifierAttr
	@InternalField
	@AlfQname(qname="bcpg:mblMicrobio")
	public NodeRef getMicrobio() {
		return microbio;
	}
	
	
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
