package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

public class DynamicCharactListItem {
	
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
	
	
}
