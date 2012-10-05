/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The Class PhysicoChemListDataItem.
 *
 * @author querephi
 */
public class PhysicoChemListDataItem implements SimpleListDataItem, SimpleCharactDataItem{

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
	
	@Override
	public NodeRef getCharactNodeRef() {
	
		return getPhysicoChem();
	}
	
	@Override
	public void setCharactNodeRef(NodeRef nodeRef) {
		setPhysicoChem(nodeRef);		
	}
	
	/**
	 * Sets the physico chem.
	 *
	 * @param physicoChem the new physico chem
	 */
	public void setPhysicoChem(NodeRef physicoChem) {
		this.physicoChem = physicoChem;
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
	
	/**
	 * Copy constructor
	 * @param p
	 */
	public PhysicoChemListDataItem(SimpleListDataItem p){
		setValue(p.getValue());
		setMini(p.getMini());
		setMaxi(p.getMaxi());
		setIsManual(p.getIsManual());
		setPhysicoChem(p.getCharactNodeRef());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((maxi == null) ? 0 : maxi.hashCode());
		result = prime * result + ((mini == null) ? 0 : mini.hashCode());
		result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
		result = prime * result + ((physicoChem == null) ? 0 : physicoChem.hashCode());
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
		if (nodeRef == null) {
			if (other.nodeRef != null)
				return false;
		} else if (!nodeRef.equals(other.nodeRef))
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

	@Override
	public String toString() {
		return "PhysicoChemListDataItem [nodeRef=" + nodeRef + ", value=" + value + ", unit=" + unit + ", mini=" + mini + ", maxi=" + maxi + ", physicoChem=" + physicoChem + "]";
	}
}
