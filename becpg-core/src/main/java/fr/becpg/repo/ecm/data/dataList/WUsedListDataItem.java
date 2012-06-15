package fr.becpg.repo.ecm.data.dataList;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.Leaf;

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
	
	//TODO : SAME CODE FOR COMPOLISTDATAITEM !!!
	public static Composite<WUsedListDataItem> getHierarchicalCompoList(List<WUsedListDataItem> items){
		
		Composite<WUsedListDataItem> composite = new Composite<WUsedListDataItem>();
		loadChildren(composite, 1, 0, items);
		return composite;
	}
	
	private static int loadChildren(Composite<WUsedListDataItem> composite, int level, int startPos, List<WUsedListDataItem> items){
		
		int z_idx = startPos; 
		
		for( ; z_idx<items.size() ; z_idx++){
			
			WUsedListDataItem compoListDataItem = items.get(z_idx);
			
			if(compoListDataItem.getDepthLevel() == level){				
				
				// is composite ?
				boolean isComposite = false;
				if((z_idx+1) < items.size()){
				
					WUsedListDataItem nextComponent = items.get(z_idx+1);
					if(nextComponent.getDepthLevel() > compoListDataItem.getDepthLevel()){
						isComposite = true;
					}
				}
				
				if(isComposite){
					Composite<WUsedListDataItem> c = new Composite<WUsedListDataItem>(compoListDataItem);
					composite.addChild(c);
					z_idx = loadChildren(c, level+1, z_idx+1, items);
				}
				else{
					Leaf<WUsedListDataItem> leaf = new Leaf<WUsedListDataItem>(compoListDataItem);
					composite.addChild(leaf);
				}				
			}
			else if(compoListDataItem.getDepthLevel() < level){
				z_idx--;
				break;				
			}
		}
		
		return z_idx;
	}
}
