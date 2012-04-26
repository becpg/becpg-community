package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

public class DynamicCharachListItem {
	
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

	public DynamicCharachListItem() {
		super();
	}

	public DynamicCharachListItem(NodeRef nodeRef, String dynamicCharachTitle, String dynamicCharachFormula, Object dynamicCharachValue, String dynamicCharachGroupColor) {
		super();
		this.nodeRef = nodeRef;
		this.name = dynamicCharachTitle;
		this.formula = dynamicCharachFormula;
		this.value = dynamicCharachValue;
		this.groupColor = dynamicCharachGroupColor;
	}
	
	
	public DynamicCharachListItem(String dynamicCharachTitle, String dynamicCharachFormula) {
		super();
		this.name = dynamicCharachTitle;
		this.formula = dynamicCharachFormula;
	}

	public DynamicCharachListItem(DynamicCharachListItem copy){
		this.nodeRef = copy.nodeRef;
		this.name = copy.name;
		this.formula = copy.formula;
		this.value = copy.value;
		this.groupColor = copy.groupColor;
	}
	
	
}
