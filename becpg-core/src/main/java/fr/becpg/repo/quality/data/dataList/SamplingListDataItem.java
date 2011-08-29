package fr.becpg.repo.quality.data.dataList;

import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;

public class SamplingListDataItem {

	private NodeRef nodeRef;
	private Date dateTime;
	private String sampleId;
	private String sampleState;
	private NodeRef controlPoint;
	private NodeRef controlStep;
	public NodeRef getNodeRef() {
		return nodeRef;
	}
	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}
	public Date getDateTime() {
		return dateTime;
	}
	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}
	public String getSampleId() {
		return sampleId;
	}
	public void setSampleId(String sampleId) {
		this.sampleId = sampleId;
	}
	public String getSampleState() {
		return sampleState;
	}
	public void setSampleState(String sampleState) {
		this.sampleState = sampleState;
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
	
	public SamplingListDataItem(NodeRef nodeRef, Date dateTime, String sampleId, String sampleState, NodeRef controlPoint, NodeRef controlStep){
		setNodeRef(nodeRef);
		setDateTime(dateTime);
		setSampleId(sampleId);
		setSampleState(sampleState);
		setControlPoint(controlPoint);
		setControlStep(controlStep);
	}
}
