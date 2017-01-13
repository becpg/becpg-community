package fr.becpg.repo.product.data;

import org.alfresco.service.cmr.repository.NodeRef;

public class CharactDetailsValue {

	private NodeRef parentNodeRef;
	private NodeRef keyNodeRef;
	private Double value;
	private Integer level;
	private String unit;

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

	public void add(Double val) {
		System.out.println("ADD between "+value+" and "+val);
		if ((value != null) && (val != null)) {
			value += val;
		}
	}
	
	public void and(Double val){
		System.out.println("AND between "+value+" and "+val);
		if(val >= 0 && value >= 0){
			value = 1d;
		} else value = 0d;
		
		System.out.println("Res="+value);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((keyNodeRef == null) ? 0 : keyNodeRef.hashCode());
		result = prime * result + ((level == null) ? 0 : level.hashCode());
		result = prime * result + ((parentNodeRef == null) ? 0 : parentNodeRef.hashCode());
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
		if (parentNodeRef == null) {
			if (other.parentNodeRef != null)
				return false;
		} else if (!parentNodeRef.equals(other.parentNodeRef))
			return false;
		return true;
	}

	

	
	
}
