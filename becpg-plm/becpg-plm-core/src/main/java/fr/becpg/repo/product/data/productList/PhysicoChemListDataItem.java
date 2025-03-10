/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.product.data.RegulatoryEntityItem;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.repository.annotation.AlfMlText;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.repository.model.ControlableListDataItem;
import fr.becpg.repo.repository.model.FormulatedCharactDataItem;
import fr.becpg.repo.repository.model.MinMaxValueDataItem;
import fr.becpg.repo.repository.model.SimpleListDataItem;
import fr.becpg.repo.repository.model.UnitAwareDataItem;
import fr.becpg.repo.repository.model.VariantAwareDataItem;

/**
 * <p>PhysicoChemListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:physicoChemList")
public class PhysicoChemListDataItem extends VariantAwareDataItem implements SimpleListDataItem, MinMaxValueDataItem, UnitAwareDataItem, FormulatedCharactDataItem, ControlableListDataItem, RegulatoryEntityItem {
			
	
	private static final long serialVersionUID = -3018711765028656339L;

	private Double value;
	
	private String unit;
	
	private Double mini;
	
	private Double maxi;
	
	private NodeRef physicoChem;
	
	private Boolean isFormulated;
	
	private String type;
	
	private RequirementType regulatoryType;
	private MLText regulatoryMessage;
	
	private List<NodeRef> regulatoryCountriesRef = new ArrayList<>();
	
	private List<NodeRef> regulatoryUsagesRef = new ArrayList<>();
	

	/**
	 * <p>Getter for the field <code>type</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:pclType")
	public String getType() {
		return type;
	}


	/**
	 * <p>Setter for the field <code>type</code>.</p>
	 *
	 * @param type a {@link java.lang.String} object.
	 */
	public void setType(String type) {
		this.type = type;
	}

	
	/**
	 * <p>Getter for the field <code>value</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:pclValue")
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
	@AlfQname(qname="bcpg:pclUnit")
	public String getUnit() {
		return unit;
	}
	
	
	/** {@inheritDoc} */
	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	/**
	 * <p>Getter for the field <code>mini</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:pclMini")
	public Double getMini() {
		return mini;
	}
	
	
	/** {@inheritDoc} */
	public void setMini(Double mini) {
		this.mini = mini;
	}
	
	/**
	 * <p>Getter for the field <code>maxi</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:pclMaxi")
	public Double getMaxi() {
		return maxi;
	}
	
	/** {@inheritDoc} */
	public void setMaxi(Double maxi) {
		this.maxi = maxi;
	}
	
	/**
	 * <p>Getter for the field <code>physicoChem</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@DataListIdentifierAttr
	@AlfQname(qname="bcpg:pclPhysicoChem")
	@InternalField
	public NodeRef getPhysicoChem() {
		return physicoChem;
	}
	
	/** {@inheritDoc} */
	@Override
	@InternalField
	public NodeRef getCharactNodeRef() {
		return getPhysicoChem();
	}
	
	/** {@inheritDoc} */
	@Override
	public void setCharactNodeRef(NodeRef nodeRef) {
		setPhysicoChem(nodeRef);		
	}
	
	
	/**
	 * <p>Getter for the field <code>isFormulated</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	@AlfProp
	@InternalField
	@AlfQname(qname="bcpg:physicoChemIsFormulated")
	public Boolean getIsFormulated() {
		return isFormulated;
	}


	/** {@inheritDoc} */
	public void setIsFormulated(Boolean isFormulated) {
		this.isFormulated = isFormulated;
	}

	/** {@inheritDoc} */
	@Override
	public Double getFormulatedValue() {
		return getValue();
	}
	
	/** {@inheritDoc} */
	@Override
	public void setFormulatedValue(Double formulatedValue) {
		 setValue(formulatedValue);
	}

	/**
	 * <p>Getter for the field <code>regulatoryCountriesRef</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:regulatoryCountries")
	public List<NodeRef> getRegulatoryCountriesRef() {
		return regulatoryCountriesRef;
	}

	/** {@inheritDoc} */
	public void setRegulatoryCountriesRef(List<NodeRef> regulatoryCountries) {
		this.regulatoryCountriesRef = regulatoryCountries;
	}

	/**
	 * <p>Getter for the field <code>regulatoryUsagesRef</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:regulatoryUsageRef")
	public List<NodeRef> getRegulatoryUsagesRef() {
		return regulatoryUsagesRef;
	}

	/** {@inheritDoc} */
	public void setRegulatoryUsagesRef(List<NodeRef> regulatoryUsages) {
		this.regulatoryUsagesRef = regulatoryUsages;
	}
	
	
	
	
	/**
	 * <p>Getter for the field <code>regulatoryType</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.constraints.RequirementType} object
	 */
	@AlfProp
	@AlfQname(qname="bcpg:regulatoryType")
	public RequirementType getRegulatoryType() {
		return regulatoryType;
	}

	/** {@inheritDoc} */
	public void setRegulatoryType(RequirementType regulatoryType) {
		this.regulatoryType = regulatoryType;
	}

	/**
	 * <p>Getter for the field <code>regulatoryMessage</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object
	 */
	@AlfProp
	@AlfMlText
	@AlfQname(qname="bcpg:regulatoryText")
	public MLText getRegulatoryMessage() {
		return regulatoryMessage;
	}

	/** {@inheritDoc} */
	public void setRegulatoryMessage(MLText regulatoryMessage) {
		this.regulatoryMessage = regulatoryMessage;
	}
	
	/** {@inheritDoc} */
	@Override
	public MLText getTextCriteria() {
		return null;
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
	 * <p>Constructor for PhysicoChemListDataItem.</p>
	 */
	public PhysicoChemListDataItem() {
		super();
	}
	
	/**
	 * Copy constructor
	 *
	 * @param p a {@link fr.becpg.repo.product.data.productList.PhysicoChemListDataItem} object.
	 */
	public PhysicoChemListDataItem(PhysicoChemListDataItem p){
		super(p);
		
		this.value = p.value;
		this.unit = p.unit;
		this.mini = p.mini;
		this.maxi = p.maxi;
		this.physicoChem = p.physicoChem;
		this.regulatoryCountriesRef = new ArrayList<>(p.regulatoryCountriesRef);
		this.regulatoryUsagesRef = new ArrayList<>(p.regulatoryUsagesRef);
		this.regulatoryMessage = p.regulatoryMessage;
		this.regulatoryType = p.regulatoryType;
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
	
	/** {@inheritDoc} */
	@Override
	public PhysicoChemListDataItem copy() {
		PhysicoChemListDataItem ret = new PhysicoChemListDataItem(this);
		ret.setName(null);
		ret.setNodeRef(null);
		ret.setParentNodeRef(null);
		return ret;
	}


	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((maxi == null) ? 0 : maxi.hashCode());
		result = prime * result + ((mini == null) ? 0 : mini.hashCode());
		result = prime * result + ((physicoChem == null) ? 0 : physicoChem.hashCode());
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}


	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PhysicoChemListDataItem other = (PhysicoChemListDataItem) obj;
		if (maxi == null) {
			if (other.maxi != null)
				return false;
		} else if (!maxi.equals(other.maxi))
			return false;
		if (mini == null) {
			if (other.mini != null)
				return false;
		} else if (!mini.equals(other.mini))
			return false;
		if (physicoChem == null) {
			if (other.physicoChem != null)
				return false;
		} else if (!physicoChem.equals(other.physicoChem))
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
		return "PhysicoChemListDataItem [value=" + value + ", unit=" + unit + ", mini=" + mini + ", maxi=" + maxi + ", physicoChem=" + physicoChem + ", isManual=" + isManual
				+ ", nodeRef=" + nodeRef + ", name=" + name + "]";
	}

	/** {@inheritDoc} */
	@Override
	public Boolean shouldDetailIfZero() {
		return true;
	}
	

	
	
}
