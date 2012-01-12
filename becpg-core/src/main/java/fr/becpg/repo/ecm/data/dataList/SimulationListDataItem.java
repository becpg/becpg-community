package fr.becpg.repo.ecm.data.dataList;

import org.alfresco.service.cmr.repository.NodeRef;

public class SimulationListDataItem {

	private NodeRef nodeRef;
	private NodeRef sourceItem;
	private NodeRef charact;
	private Float sourceValue;
	private Float targetValue;
	
	public NodeRef getNodeRef() {
		return nodeRef;
	}
	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}
	public NodeRef getSourceItem() {
		return sourceItem;
	}
	public void setSourceItem(NodeRef sourceItem) {
		this.sourceItem = sourceItem;
	}
	
	public NodeRef getCharact() {
		return charact;
	}
	public void setCharact(NodeRef charact) {
		this.charact = charact;
	}
	public Float getSourceValue() {
		return sourceValue;
	}
	public void setSourceValue(Float sourceValue) {
		this.sourceValue = sourceValue;
	}
	public Float getTargetValue() {
		return targetValue;
	}
	public void setTargetValue(Float targetValue) {
		this.targetValue = targetValue;
	}
	
	public SimulationListDataItem(NodeRef nodeRef, NodeRef sourceItem, NodeRef charact, Float sourceValue, Float targetValue){
		setNodeRef(nodeRef);
		setSourceItem(sourceItem);
		setCharact(charact);
		setSourceValue(sourceValue);
		setTargetValue(targetValue);
	}
}
