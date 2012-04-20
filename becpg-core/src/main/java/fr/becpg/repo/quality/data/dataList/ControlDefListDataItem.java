package fr.becpg.repo.quality.data.dataList;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

public class ControlDefListDataItem {

	NodeRef nodeRef;
	String type;
	Double mini;
	Double maxi;
	Boolean required;
	NodeRef method;
	List<NodeRef> characts = new ArrayList<NodeRef>();
	
	public NodeRef getNodeRef() {
		return nodeRef;
	}
	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
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
	public Boolean getRequired() {
		return required;
	}
	public void setRequired(Boolean required) {
		this.required = required;
	}
	public NodeRef getMethod() {
		return method;
	}
	public void setMethod(NodeRef method) {
		this.method = method;
	}
	public List<NodeRef> getCharacts() {
		return characts;
	}
	public void setCharacts(List<NodeRef> characts) {
		this.characts = characts;
	}
	
	public ControlDefListDataItem(NodeRef nodeRef, String type, Double mini, Double maxi, Boolean required, NodeRef method, List<NodeRef> characts){
		setNodeRef(nodeRef);
		setType(type);
		setMini(mini);
		setMaxi(maxi);
		setRequired(required);
		setMethod(method);
		setCharacts(characts);
	}
}
