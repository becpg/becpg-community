/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.product.data.BaseObject;

// TODO: Auto-generated Javadoc
/**
 * The Class NutListDataItem.
 *
 * @author querephi
 */
public class NutListDataItem extends BaseObject implements IManualDataItem{

	/** The node ref. */
	private NodeRef nodeRef;
	
	/** The value. */
	private Double value;
	
	/** The unit. */
	private String unit;
	
	private Double mini;
	
	private Double maxi;
	
	/** The group. */
	private String group;
	
	/** The nut. */
	private NodeRef nut;
	
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
	
	public Double getMini() {
		return mini;
	}

	public void setMini(Double mini) {
		this.mini = mini;
	}

	public Double getMaxi() {
		return maxi;
	}

	public void setMaxi(Double maxi) {
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
	
	@Override
	public Boolean getIsManual() {

		return isManual;
	}

	@Override
	public void setIsManual(Boolean isManual) {
		
		this.isManual = isManual;		
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
	public NutListDataItem(NodeRef nodeRef,	Double value, String unit, Double mini, Double maxi, String group, NodeRef nut, Boolean isManual)
	{
		setNodeRef(nodeRef);
		setValue(value);
		setUnit(unit);
		setMini(mini);
		setMaxi(maxi);
		setGroup(group);
		setNut(nut);
		setIsManual(isManual);
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
		setIsManual(n.getIsManual());
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + ((isManual == null) ? 0 : isManual.hashCode());
		result = prime * result + ((maxi == null) ? 0 : maxi.hashCode());
		result = prime * result + ((mini == null) ? 0 : mini.hashCode());
		result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
		result = prime * result + ((nut == null) ? 0 : nut.hashCode());
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
		NutListDataItem other = (NutListDataItem) obj;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
		if (isManual == null) {
			if (other.isManual != null)
				return false;
		} else if (!isManual.equals(other.isManual))
			return false;
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
		if (nut == null) {
			if (other.nut != null)
				return false;
		} else if (!nut.equals(other.nut))
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
		return "NutListDataItem [nodeRef=" + nodeRef + ", value=" + value + ", unit=" + unit + ", mini=" + mini + ", maxi=" + maxi + ", group=" + group + ", nut=" + nut
				+ ", isManual=" + isManual + "]";
	}
	
	
}
