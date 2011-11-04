package fr.becpg.repo.entity.wused.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

public class WUsedData {
	
	private NodeRef entityNodeRef;
	private Map<NodeRef, WUsedData> rootList = new HashMap<NodeRef, WUsedData>();
		
	public NodeRef getEntityNodeRef() {
		return entityNodeRef;
	}

	public void setEntityNodeRef(NodeRef entityNodeRef) {
		this.entityNodeRef = entityNodeRef;
	}

	public Map<NodeRef, WUsedData> getRootList() {
		return rootList;
	}

	public void setRootList(Map<NodeRef, WUsedData> rootList) {
		this.rootList = rootList;
	}

	public WUsedData(NodeRef entityNodeRef, Map<NodeRef, WUsedData> rootList){
		setEntityNodeRef(entityNodeRef);
		setRootList(rootList);
	}
	
}
