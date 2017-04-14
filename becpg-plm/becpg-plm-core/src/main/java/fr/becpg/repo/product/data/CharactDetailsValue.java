package fr.becpg.repo.product.data;

import org.alfresco.service.cmr.repository.NodeRef;

public class CharactDetailsValue {

	private NodeRef parentNodeRef;
	private NodeRef keyNodeRef;
	private Double value;
	private Double previousValue;
	private Double futureValue;
	private Double mini;
	private Double maxi;
	private Integer level;
	private String unit;
	private String name;

	public CharactDetailsValue(NodeRef parentNodeRef, NodeRef keyNodeRef, Double value, Integer level, String unit) {
		super();
		this.parentNodeRef = parentNodeRef;
		this.keyNodeRef = keyNodeRef;
		this.value = value;
		this.level = level;
		this.unit = unit;
	}

	public NodeRef getKeyNodeRef() {
		return keyNodeRef;
	}

	public Double getValue() {
		return value != 0d ? value : null;
	}

	public Integer getLevel() {
		return level;
	}

	public String getUnit() {
		return unit;
	}

	public Double getPreviousValue() {
		return previousValue;
	}

	public void setPreviousValue(Double previousValue) {
		this.previousValue = previousValue;
	}

	public Double getFutureValue() {
		return futureValue;
	}

	public void setFutureValue(Double futureValue) {
		this.futureValue = futureValue;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void add(Double val) {
		if ((value != null) && (val != null)) {
			value += val;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((futureValue == null) ? 0 : futureValue.hashCode());
		result = prime * result + ((keyNodeRef == null) ? 0 : keyNodeRef.hashCode());
		result = prime * result + ((level == null) ? 0 : level.hashCode());
		result = prime * result + ((maxi == null) ? 0 : maxi.hashCode());
		result = prime * result + ((mini == null) ? 0 : mini.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((parentNodeRef == null) ? 0 : parentNodeRef.hashCode());
		result = prime * result + ((previousValue == null) ? 0 : previousValue.hashCode());
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	
	///IMPORTANT DO NOT MODIFIED THIS
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CharactDetailsValue other = (CharactDetailsValue) obj;
		if (futureValue == null) {
			if (other.futureValue != null)
				return false;
		} else if (!futureValue.equals(other.futureValue))
			return false;
		if (keyNodeRef == null) {
			if (other.keyNodeRef != null)
				return false;
		} else if (!keyNodeRef.equals(other.keyNodeRef))
			return false;
		if (level == null) {
			if (other.level != null)
				return false;
		} else if (!level.equals(other.level))
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
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (parentNodeRef == null) {
			if (other.parentNodeRef != null)
				return false;
		} else if (!parentNodeRef.equals(other.parentNodeRef))
			return false;
		if (previousValue == null) {
			if (other.previousValue != null)
				return false;
		} else if (!previousValue.equals(other.previousValue))
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
		return "CharactDetailsValue [parentNodeRef=" + parentNodeRef + ", keyNodeRef=" + keyNodeRef + ", value=" + value + ", previousValue="
				+ previousValue + ", futureValue=" + futureValue + ", mini=" + mini + ", maxi=" + maxi + ", level=" + level + ", unit=" + unit
				+ ", name=" + name + "]";
	}

	

	
	
}
