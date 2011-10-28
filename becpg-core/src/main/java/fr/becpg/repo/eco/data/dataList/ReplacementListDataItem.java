package fr.becpg.repo.eco.data.dataList;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.eco.data.RevisionType;

public class ReplacementListDataItem {

	private NodeRef nodeRef;
	private RevisionType revision;
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
	
	public ReplacementListDataItem(NodeRef nodeRef, RevisionType revision, NodeRef sourceItem, NodeRef targetItem){
		setNodeRef(nodeRef);
		setRevision(revision);
		setSourceItem(sourceItem);
		setTargetItem(targetItem);
	}
	
}
