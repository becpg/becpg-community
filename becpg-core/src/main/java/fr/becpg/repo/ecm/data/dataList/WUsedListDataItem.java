package fr.becpg.repo.ecm.data.dataList;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.data.hierarchicalList.CompositeDataItem;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;
@AlfType
@AlfQname(qname="ecm:wUsedList")
public class WUsedListDataItem  extends BeCPGDataObject implements CompositeDataItem{

	private Integer depthLevel;
	private Boolean isWUsedImpacted;
	private QName impactedDataList;
	private NodeRef link;
	private NodeRef sourceItem;
	private WUsedListDataItem parent;
	

	
	@AlfProp
	@AlfQname(qname="bcpg:parentLevel")
	public WUsedListDataItem getParent() {
		return parent;
	}
	
	public void setParent(WUsedListDataItem parent) {
		this.parent = parent;
	}

	@AlfProp
	@AlfQname(qname="bcpg:depthLevel")
	public Integer getDepthLevel() {
		return depthLevel;
	}
	public void setDepthLevel(Integer depthLevel) {
		this.depthLevel = depthLevel;
	}
	@AlfProp
	@AlfQname(qname="ecm:isWUsedImpacted")
	public Boolean getIsWUsedImpacted() {
		return isWUsedImpacted;
	}
	public void setIsWUsedImpacted(Boolean isWUsedImpacted) {
		this.isWUsedImpacted = isWUsedImpacted;
	}
	@AlfProp
	@AlfQname(qname="ecm:impactedDataList")
	public QName getImpactedDataList() {
		return impactedDataList;
	}
	public void setImpactedDataList(QName impactedDataList) {
		this.impactedDataList = impactedDataList;
	}
	@AlfSingleAssoc
	@AlfQname(qname="ecm:wulLink")
	public NodeRef getLink() {
		return link;
	}
	public void setLink(NodeRef link) {
		this.link = link;
	}	
	@AlfSingleAssoc
	@AlfQname(qname="ecm:wulSourceItem")
	public NodeRef getSourceItem() {
		return sourceItem;
	}
	public void setSourceItem(NodeRef sourceItem) {
		this.sourceItem = sourceItem;
	}

	
	public WUsedListDataItem() {
		super();
	}
	
	public WUsedListDataItem(NodeRef nodeRef, WUsedListDataItem parent, QName impactedDataList,
			Boolean isWUsedImpacted, NodeRef link, NodeRef sourceItem){
		super();
		this.nodeRef=nodeRef;
		this.parent= parent;
		if(parent == null || parent.getDepthLevel() == null){
			depthLevel = 1;
		} else {
			depthLevel = parent.getDepthLevel()+1;
		}
		this.impactedDataList=impactedDataList;
		this.isWUsedImpacted=isWUsedImpacted;
		this.link=link;
		this.sourceItem=sourceItem;
	}
	
	
	@Override
	public String toString() {
		return "WUsedListDataItem [depthLevel=" + depthLevel + ", isWUsedImpacted=" + isWUsedImpacted + ", impactedDataList=" + impactedDataList + ", link=" + link
				+ ", sourceItem=" + sourceItem + "]";
	}

	

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((depthLevel == null) ? 0 : depthLevel.hashCode());
		result = prime * result + ((impactedDataList == null) ? 0 : impactedDataList.hashCode());
		result = prime * result + ((isWUsedImpacted == null) ? 0 : isWUsedImpacted.hashCode());
		result = prime * result + ((link == null) ? 0 : link.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + ((sourceItem == null) ? 0 : sourceItem.hashCode());
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
		WUsedListDataItem other = (WUsedListDataItem) obj;
		if (depthLevel == null) {
			if (other.depthLevel != null)
				return false;
		} else if (!depthLevel.equals(other.depthLevel))
			return false;
		if (impactedDataList == null) {
			if (other.impactedDataList != null)
				return false;
		} else if (!impactedDataList.equals(other.impactedDataList))
			return false;
		if (isWUsedImpacted == null) {
			if (other.isWUsedImpacted != null)
				return false;
		} else if (!isWUsedImpacted.equals(other.isWUsedImpacted))
			return false;
		if (link == null) {
			if (other.link != null)
				return false;
		} else if (!link.equals(other.link))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (sourceItem == null) {
			if (other.sourceItem != null)
				return false;
		} else if (!sourceItem.equals(other.sourceItem))
			return false;
		return true;
	}

	
}
