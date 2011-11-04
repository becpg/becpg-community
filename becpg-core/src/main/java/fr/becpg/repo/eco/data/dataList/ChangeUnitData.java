package fr.becpg.repo.eco.data.dataList;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.BeCPGDataObject;
import fr.becpg.repo.eco.data.RevisionType;

/**
 * Change Unit class
 * @author quere
 *
 */
public class ChangeUnitData extends BeCPGDataObject {

	private NodeRef nodeRef;
	private RevisionType revision;
	private Boolean reqRespected;
	private String reqDetails;
	private Boolean treated;
	private NodeRef sourceItem;
	private NodeRef targetItem;
	
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
	public Boolean getReqRespected() {
		return reqRespected;
	}
	public void setReqRespected(Boolean reqRespected) {
		this.reqRespected = reqRespected;
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
	
	public ChangeUnitData(NodeRef nodeRef, RevisionType revision, Boolean reqRespected, String reqDetails, Boolean treated, NodeRef sourceItem, NodeRef targetItem){
		
		setNodeRef(nodeRef);
		setRevision(revision);
		setReqRespected(reqRespected);
		setReqDetails(reqDetails);
		setTreated(treated);
		setSourceItem(sourceItem);
		setTargetItem(targetItem);
	}
}