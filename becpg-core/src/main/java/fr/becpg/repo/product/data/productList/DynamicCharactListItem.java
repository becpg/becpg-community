package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.product.data.BaseObject;

public class DynamicCharactListItem extends BaseObject{
	
	/** The node ref. */
	private NodeRef nodeRef;	
	
	private String name;
	
	private String formula;
   
	private Object value;
	
	private String groupColor;

	public NodeRef getNodeRef() {
		return nodeRef;
	}

	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}

	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFormula() {
		return formula;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public String getGroupColor() {
		return groupColor;
	}

	public void setGroupColor(String groupColor) {
		this.groupColor = groupColor;
	}

	public DynamicCharactListItem() {
		super();
	}

	public DynamicCharactListItem(NodeRef nodeRef, String dynamicCharactTitle, String dynamicCharactFormula, Object dynamicCharactValue, String dynamicCharactGroupColor) {
		super();
		this.nodeRef = nodeRef;
		this.name = dynamicCharactTitle;
		this.formula = dynamicCharactFormula;
		this.value = dynamicCharactValue;
		this.groupColor = dynamicCharactGroupColor;
	}
	
	
	public DynamicCharactListItem(String dynamicCharactTitle, String dynamicCharactFormula) {
		super();
		this.name = dynamicCharactTitle;
		this.formula = dynamicCharactFormula;
	}

	public DynamicCharactListItem(DynamicCharactListItem copy){
		this.nodeRef = copy.nodeRef;
		this.name = copy.name;
		this.formula = copy.formula;
		this.value = copy.value;
		this.groupColor = copy.groupColor;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((formula == null) ? 0 : formula.hashCode());
		result = prime * result + ((groupColor == null) ? 0 : groupColor.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
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
		DynamicCharactListItem other = (DynamicCharactListItem) obj;
		if (formula == null) {
			if (other.formula != null)
				return false;
		} else if (!formula.equals(other.formula))
			return false;
		if (groupColor == null) {
			if (other.groupColor != null)
				return false;
		} else if (!groupColor.equals(other.groupColor))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (nodeRef == null) {
			if (other.nodeRef != null)
				return false;
		} else if (!nodeRef.equals(other.nodeRef))
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
		return "DynamicCharactListItem [nodeRef=" + nodeRef + ", name=" + name + ", formula=" + formula + ", value=" + value + ", groupColor=" + groupColor + "]";
	}
	
	
}
