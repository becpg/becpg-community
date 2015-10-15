package fr.becpg.repo.product.data;

import org.alfresco.service.cmr.repository.NodeRef;

public class CharactDetailsValue {

	private NodeRef keyNodeRef;
	private Double value;
	private Integer level;
	private String unit;

	public CharactDetailsValue(NodeRef keyNodeRef, Double value, Integer level, String unit) {
		super();
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
		if ((value != null) && (val != null)) {
			value += val;
		}
	}

}
