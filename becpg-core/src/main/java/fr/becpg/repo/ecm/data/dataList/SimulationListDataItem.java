package fr.becpg.repo.ecm.data.dataList;

import org.alfresco.service.cmr.repository.NodeRef;

public class SimulationListDataItem {

	private NodeRef nodeRef;
	private NodeRef sourceItem;
	private NodeRef charact;
	private Double sourceValue;
	private Double targetValue;
	
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
	public Double getSourceValue() {
		return sourceValue;
	}
	public void setSourceValue(Double sourceValue) {
		this.sourceValue = sourceValue;
	}
	public Double getTargetValue() {
		return targetValue;
	}
	public void setTargetValue(Double targetValue) {
		this.targetValue = targetValue;
	}
	
	public SimulationListDataItem(NodeRef nodeRef, NodeRef sourceItem, NodeRef charact, Double sourceValue, Double targetValue){
		setNodeRef(nodeRef);
		setSourceItem(sourceItem);
		setCharact(charact);
		setSourceValue(sourceValue);
		setTargetValue(targetValue);
	}
}
