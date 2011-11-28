package fr.becpg.repo.eco.data.dataList;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.BeCPGDataObject;
import fr.becpg.repo.eco.data.RevisionType;
import fr.becpg.repo.product.data.productList.RequirementType;

/**
 * Change Unit class
 * @author quere
 *
 */
public class ChangeUnitDataItem {

	private NodeRef nodeRef;
	private RevisionType revision;
	private RequirementType reqType;
	private String reqDetails;
	private Boolean treated;
	private NodeRef sourceItem;
	private NodeRef targetItem;
	private NodeRef simulationItem;
	
	public NodeRef getNodeRef() {
		return nodeRef;
	}
	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}
	public RevisionType getRevision() {
		return revision;
	}
	public void setRevision(RevisionType revision) {
		this.revision = revision;
	}	
	public RequirementType getReqType() {
		return reqType;
	}
	public void setReqType(RequirementType reqType) {
		this.reqType = reqType;
	}
	public String getReqDetails() {
		return reqDetails;
	}
	public void setReqDetails(String reqDetails) {
		this.reqDetails = reqDetails;
	}
	public Boolean getTreated() {
		return treated;
	}
	public void setTreated(Boolean treated) {
		this.treated = treated;
	}	
	public NodeRef getSourceItem() {
		return sourceItem;
	}
	public void setSourceItem(NodeRef sourceItem) {
		this.sourceItem = sourceItem;
	}
	public NodeRef getTargetItem() {
		return targetItem;
	}
	public void setTargetItem(NodeRef targetItem) {
		this.targetItem = targetItem;
	}
	
	public NodeRef getSimulationItem() {
		return simulationItem;
	}
	public void setSimulationItem(NodeRef simulationItem) {
		this.simulationItem = simulationItem;
	}
	public ChangeUnitDataItem(NodeRef nodeRef, RevisionType revision, RequirementType reqType, String reqDetails, Boolean treated, NodeRef sourceItem, NodeRef targetItem, NodeRef simulationItem){
		
		setNodeRef(nodeRef);
		setRevision(revision);
		setReqType(reqType);
		setReqDetails(reqDetails);
		setTreated(treated);
		setSourceItem(sourceItem);
		setTargetItem(targetItem);
		setSimulationItem(simulationItem);
	}
}