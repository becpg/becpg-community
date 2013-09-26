package fr.becpg.repo.ecm.data.dataList;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.ecm.data.RevisionType;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

@AlfType
@AlfQname(qname = "ecm:replacementList")
public class ReplacementListDataItem extends BeCPGDataObject {

	private RevisionType revision;
	private List<NodeRef> sourceItems;
	private NodeRef targetItem;
	private Integer qtyPerc;

	@AlfProp
	@AlfQname(qname = "ecm:rlQtyPerc")
	public Integer getQtyPerc() {
		return qtyPerc;
	}

	public void setQtyPerc(Integer qtyPerc) {
		this.qtyPerc = qtyPerc;
	}

	@AlfProp
	@AlfQname(qname = "ecm:rlRevisionType")
	public RevisionType getRevision() {
		return revision;
	}

	public void setRevision(RevisionType revision) {
		this.revision = revision;
	}

	@AlfMultiAssoc
	@AlfQname(qname = "ecm:rlSourceItems")
	public List<NodeRef> getSourceItems() {
		return sourceItems;
	}

	public void setSourceItems(List<NodeRef> sourceItems) {
		this.sourceItems = sourceItems;
	}

	@AlfSingleAssoc
	@AlfQname(qname = "ecm:rlTargetItem")
	public NodeRef getTargetItem() {
		return targetItem;
	}

	public void setTargetItem(NodeRef targetItem) {
		this.targetItem = targetItem;
	}

	public ReplacementListDataItem() {
		super();
	}

	public ReplacementListDataItem(RevisionType revision, List<NodeRef> sourceItems, NodeRef targetItem, Integer qtyPerc) {
		super();
		this.revision = revision;
		this.sourceItems = sourceItems;
		this.targetItem = targetItem;
		this.qtyPerc = qtyPerc;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((qtyPerc == null) ? 0 : qtyPerc.hashCode());
		result = prime * result + ((revision == null) ? 0 : revision.hashCode());
		result = prime * result + ((sourceItems == null) ? 0 : sourceItems.hashCode());
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
		if (qtyPerc == null) {
			if (other.qtyPerc != null)
				return false;
		} else if (!qtyPerc.equals(other.qtyPerc))
			return false;
		if (revision != other.revision)
			return false;
		if (sourceItems == null) {
			if (other.sourceItems != null)
				return false;
		} else if (!sourceItems.equals(other.sourceItems))
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
		return "ReplacementListDataItem [revision=" + revision + ", sourceItems=" + sourceItems + ", targetItem=" + targetItem + ", qtyPerc=" + qtyPerc + "]";
	}

}
