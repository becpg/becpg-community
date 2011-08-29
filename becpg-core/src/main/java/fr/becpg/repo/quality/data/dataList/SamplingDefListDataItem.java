package fr.becpg.repo.quality.data.dataList;

import org.alfresco.service.cmr.repository.NodeRef;

public class SamplingDefListDataItem {
	
	private NodeRef nodeRef;
	private Integer qty;
	private Integer freq;
	private String freqUnit;
	private NodeRef controlPoint;
	private NodeRef controlStep;
	private NodeRef controlingGroup;
	
	public NodeRef getNodeRef() {
		return nodeRef;
	}
	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}
	public Integer getQty() {
		return qty;
	}
	public void setQty(Integer qty) {
		this.qty = qty;
	}
	public Integer getFreq() {
		return freq;
	}
	public void setFreq(Integer freq) {
		this.freq = freq;
	}	
	public String getFreqUnit() {
		return freqUnit;
	}
	public void setFreqUnit(String freqUnit) {
		this.freqUnit = freqUnit;
	}
	public NodeRef getControlPoint() {
		return controlPoint;
	}
	public void setControlPoint(NodeRef controlPoint) {
		this.controlPoint = controlPoint;
	}
	public NodeRef getControlStep() {
		return controlStep;
	}
	public void setControlStep(NodeRef controlStep) {
		this.controlStep = controlStep;
	}
	public NodeRef getControlingGroup() {
		return controlingGroup;
	}
	public void setControlingGroup(NodeRef controlingGroup) {
		this.controlingGroup = controlingGroup;
	}
	
	public SamplingDefListDataItem(NodeRef nodeRef, Integer qty, Integer freq, String freqUnit, NodeRef controlPoint, NodeRef controlStep, NodeRef controlingGroup){
		setNodeRef(nodeRef);
		setQty(qty);
		setFreq(freq);
		setFreqUnit(freqUnit);
		setControlPoint(controlPoint);
		setControlStep(controlStep);
		setControlingGroup(controlingGroup);
	}
}
