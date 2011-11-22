package fr.becpg.repo.eco.data.dataList;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.eco.data.RevisionType;

public class WUsedListDataItem {

	private NodeRef nodeRef;
	private Integer depthLevel;
	private Boolean isWUsedImpacted;
	private QName impactedDataList;
	private NodeRef link;
	private NodeRef sourceItem;
	
	public NodeRef getNodeRef() {
		return nodeRef;
	}
	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}
	public Integer getDepthLevel() {
		return depthLevel;
	}
	public void setDepthLevel(Integer depthLevel) {
		this.depthLevel = depthLevel;
	}
	public Boolean getIsWUsedImpacted() {
		return isWUsedImpacted;
	}
	public void setIsWUsedImpacted(Boolean isWUsedImpacted) {
		this.isWUsedImpacted = isWUsedImpacted;
	}			
	public QName getImpactedDataList() {
		return impactedDataList;
	}
	public void setImpactedDataList(QName impactedDataList) {
		this.impactedDataList = impactedDataList;
	}
	public NodeRef getLink() {
		return link;
	}
	public void setLink(NodeRef link) {
		this.link = link;
	}	
	public NodeRef getSourceItem() {
		return sourceItem;
	}
	public void setSourceItem(NodeRef sourceItem) {
		this.sourceItem = sourceItem;
	}
	public WUsedListDataItem(NodeRef nodeRef, Integer depthLevel, QName impactedDataList,
			Boolean isWUsedImpacted, NodeRef link, NodeRef sourceItem){
		
		setNodeRef(nodeRef);
		setDepthLevel(depthLevel);
		setImpactedDataList(impactedDataList);
		setIsWUsedImpacted(isWUsedImpacted);
		setLink(link);
		setSourceItem(sourceItem);
	}
}
