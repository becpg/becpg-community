package fr.becpg.repo.quality.data.dataList;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

public class ControlListDataItem {

	NodeRef nodeRef;
	String type;
	Double mini;
	Double maxi;
	Boolean required;
	String sampleId;
	Double value;
	Double target;
	String unit;
	String state;		
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

	public String getSampleId() {
		return sampleId;
	}

	public void setSampleId(String sampleId) {
		this.sampleId = sampleId;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	public Double getTarget() {
		return target;
	}

	public void setTarget(Double target) {
		this.target = target;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
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

	public ControlListDataItem(NodeRef nodeRef, String type, Double mini, Double maxi, Boolean required, String sampleId, Double value, Double target, String unit, String state,  NodeRef method, List<NodeRef> characts){
		
		setNodeRef(nodeRef);
		setType(type);
		setMini(mini);
		setMaxi(maxi);
		setRequired(required);
		setSampleId(sampleId);
		setValue(value);
		setTarget(target);
		setUnit(unit);
		setState(state);
		setMethod(method);
		setCharacts(characts);
	}
	
	public ControlListDataItem(NodeRef nodeRef, String sampleId, Double value, Double target, String unit, String state, ControlDefListDataItem controlDefListDataItem){
		
		setNodeRef(nodeRef);
		setSampleId(sampleId);
		setValue(value);
		setTarget(target);
		setUnit(unit);
		setState(state);
		
		setType(controlDefListDataItem.getType());
		setMini(controlDefListDataItem.getMini());
		setMaxi(controlDefListDataItem.getMaxi());
		setRequired(controlDefListDataItem.getRequired());		
		setMethod(controlDefListDataItem.getMethod());
		setCharacts(controlDefListDataItem.getCharacts());
	}
}
