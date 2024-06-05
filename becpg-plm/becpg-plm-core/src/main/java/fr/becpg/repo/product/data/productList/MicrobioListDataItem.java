/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfMlText;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.repository.model.ControlableListDataItem;
import fr.becpg.repo.repository.model.MinMaxValueDataItem;
import fr.becpg.repo.repository.model.UnitAwareDataItem;

/**
 * <p>MicrobioListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:microbioList")
public class MicrobioListDataItem extends BeCPGDataObject implements ControlableListDataItem, UnitAwareDataItem, MinMaxValueDataItem {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6910021090157153860L;

	private Double value;
	
	private String unit;
	
	private Double maxi;
	
	private MLText textCriteria;
	
	private NodeRef microbio;
	
	
	/**
	 * <p>Getter for the field <code>value</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:mblValue")
	public Double getValue() {
		return value;
	}
	
	
	/** {@inheritDoc} */
	public void setValue(Double value) {
		this.value = value;
	}
	
	/**
	 * <p>Getter for the field <code>unit</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:mblUnit")
	public String getUnit() {
		return unit;
	}
	
	
	/** {@inheritDoc} */
	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	/**
	 * <p>Getter for the field <code>maxi</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:mblMaxi")
	public Double getMaxi() {
		return maxi;
	}
	
	
	/** {@inheritDoc} */
	public void setMaxi(Double maxi) {
		this.maxi = maxi;
	}
	
	
	/** {@inheritDoc} */
	@Override
	public Double getMini() {
		return null;
	}


	/** {@inheritDoc} */
	@Override
	public void setMini(Double value) {
		//DO Nothing
	}
	
	/**
	 * <p>Getter for the field <code>textCriteria</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfMlText
	@AlfQname(qname="bcpg:mblTextCriteria")
	public MLText getTextCriteria() {
		return textCriteria;
	}

	/**
	 * <p>Setter for the field <code>textCriteria</code>.</p>
	 *
	 * @param textCriteria a {@link java.lang.String} object.
	 */
	public void setTextCriteria(MLText textCriteria) {
		this.textCriteria = textCriteria;
	}

	/**
	 * <p>Getter for the field <code>microbio</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@DataListIdentifierAttr
	@InternalField
	@AlfQname(qname="bcpg:mblMicrobio")
	public NodeRef getMicrobio() {
		return microbio;
	}
	
	
	/**
	 * <p>Setter for the field <code>microbio</code>.</p>
	 *
	 * @param microbio a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setMicrobio(NodeRef microbio) {
		this.microbio = microbio;
	}
	
	/** {@inheritDoc} */
	@Override
	public void setCharactNodeRef(NodeRef microbio) {
		setMicrobio(microbio);
	}


	/** {@inheritDoc} */
	@Override
	public NodeRef getCharactNodeRef() {
		return getMicrobio();
	}
	
	
	
	/**
	 * Instantiates a new microbio list data item.
	 */
	public MicrobioListDataItem(){
		super();
	}
	
	/**
	 * Instantiates a new microbio list data item.
	 *
	 * @param nodeRef the node ref
	 * @param value the value
	 * @param unit the unit
	 * @param maxi the maxi
	 * @param microbio the microbio
	 * @param textCriteria a {@link java.lang.String} object.
	 */
	public MicrobioListDataItem(NodeRef nodeRef, Double value, String unit, Double maxi, MLText textCriteria, NodeRef microbio){
		this.nodeRef = nodeRef;
		this.value = value;
		this.unit = unit;
		this.maxi = maxi;
		this.microbio = microbio;		
		this.textCriteria = textCriteria;
	}
	
	/**
	 * Copy constructor
	 *
	 * @param m a {@link fr.becpg.repo.product.data.productList.MicrobioListDataItem} object.
	 */
	public MicrobioListDataItem(MicrobioListDataItem m){
		super(m);
		this.value =  m.value;
		this.unit =  m.unit;
		this.maxi =  m.maxi;
		this.microbio =  m.microbio;		
		this.textCriteria =  m.textCriteria;
	}
	
	/** {@inheritDoc} */
	@Override
	public MicrobioListDataItem copy() {
		MicrobioListDataItem ret = new MicrobioListDataItem(this);
		ret.setName(null);
		ret.setNodeRef(null);
		ret.setParentNodeRef(null);
		return ret;	
	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "MicrobioListDataItem [nodeRef=" + nodeRef + ", value=" + value + ", unit=" + unit + ", maxi=" + maxi + ", textCriteria=" + textCriteria + ", microbio=" + microbio
				+ "]";
	}





	
}
