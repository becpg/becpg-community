package fr.becpg.repo.ecm.data.dataList;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.ecm.data.RevisionType;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

@AlfType
@AlfQname(qname="ecm:replacementList")
public class ReplacementListDataItem extends BeCPGDataObject {

	private RevisionType revision;
	private NodeRef sourceItem;
	private NodeRef targetItem;
	
	@AlfProp
	@AlfQname(qname="ecm:rlRevisionType")
	public RevisionType getRevision() {
		return revision;
	}
	public void setRevision(RevisionType revision) {
		this.revision = revision;
	}
	@AlfSingleAssoc
	@AlfQname(qname="ecm:rlSourceItem")
	public NodeRef getSourceItem() {
		return sourceItem;
	}
	public void setSourceItem(NodeRef sourceItem) {
		this.sourceItem = sourceItem;
	}
	@AlfSingleAssoc
	@AlfQname(qname="ecm:rlTargetItem")
	public NodeRef getTargetItem() {
		return targetItem;
	}
	public void setTargetItem(NodeRef targetItem) {
		this.targetItem = targetItem;
	}
	
	
	public ReplacementListDataItem() {
		super();
	}
	public ReplacementListDataItem(NodeRef nodeRef, RevisionType revision, NodeRef sourceItem, NodeRef targetItem){
		setNodeRef(nodeRef);
		setRevision(revision);
		setSourceItem(sourceItem);
		setTargetItem(targetItem);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((revision == null) ? 0 : revision.hashCode());
		result = prime * result + ((sourceItem == null) ? 0 : sourceItem.hashCode());
		result = prime * result + ((targetItem == null) ? 0 : targetItem.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReplacementListDataItem other = (ReplacementListDataItem) obj;
		if (revision != other.revision)
			return false;
		if (sourceItem == null) {
			if (other.sourceItem != null)
				return false;
		} else if (!sourceItem.equals(other.sourceItem))
			return false;
		if (targetItem == null) {
			if (other.targetItem != null)
				return false;
		} else if (!targetItem.equals(other.targetItem))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "ReplacementListDataItem [revision=" + revision + ", sourceItem=" + sourceItem + ", targetItem=" + targetItem + "]";
	}
	
	
	
}
