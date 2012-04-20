package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

public class DynamicCharachListItem {
	
	/** The node ref. */
	private NodeRef nodeRef;	
	
	private String dynamicCharachTitle;
	
	private String dynamicCharachFormula;
   
	private Object dynamicCharachValue;

	public NodeRef getNodeRef() {
		return nodeRef;
	}

	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}

	public String getDynamicCharachTitle() {
		return dynamicCharachTitle;
	}

	public void setDynamicCharachTitle(String dynamicCharachTitle) {
		this.dynamicCharachTitle = dynamicCharachTitle;
	}

	public String getDynamicCharachFormula() {
		return dynamicCharachFormula;
	}

	public void setDynamicCharachFormula(String dynamicCharachFormula) {
		this.dynamicCharachFormula = dynamicCharachFormula;
	}

	public Object getDynamicCharachValue() {
		return dynamicCharachValue;
	}

	public void setDynamicCharachValue(Object dynamicCharachValue) {
		this.dynamicCharachValue = dynamicCharachValue;
	}

	
	
	public DynamicCharachListItem() {
		super();
	}

	public DynamicCharachListItem(NodeRef nodeRef, String dynamicCharachTitle, String dynamicCharachFormula, Object dynamicCharachValue) {
		super();
		this.nodeRef = nodeRef;
		this.dynamicCharachTitle = dynamicCharachTitle;
		this.dynamicCharachFormula = dynamicCharachFormula;
		this.dynamicCharachValue = dynamicCharachValue;
	}
	
	
	public DynamicCharachListItem(String dynamicCharachTitle, String dynamicCharachFormula) {
		super();
		this.dynamicCharachTitle = dynamicCharachTitle;
		this.dynamicCharachFormula = dynamicCharachFormula;
	}

	public DynamicCharachListItem(DynamicCharachListItem copy){
		this.nodeRef = copy.nodeRef;
		this.dynamicCharachTitle = copy.dynamicCharachTitle;
		this.dynamicCharachFormula = copy.dynamicCharachFormula;
		this.dynamicCharachValue = copy.dynamicCharachValue;
	}
	
	
}
