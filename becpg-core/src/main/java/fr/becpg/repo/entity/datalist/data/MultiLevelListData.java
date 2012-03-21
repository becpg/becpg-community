package fr.becpg.repo.entity.datalist.data;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Represent a multiLevel list data
 * @author matthieu
 *
 */
public class MultiLevelListData {

	
	private int depth = 0;
	
	//Keep order
	private Map<NodeRef, MultiLevelListData> tree = new LinkedHashMap<NodeRef, MultiLevelListData>();
	
	private NodeRef entityNodeRef;

	
	public MultiLevelListData(NodeRef entityNodeRef,int depth ) {
		super();
		this.depth = depth;
		this.entityNodeRef = entityNodeRef;
	}

	public int getDepth() {
		return depth;
	}

	public Map<NodeRef, MultiLevelListData> getTree() {
		return tree;
	}
	

	public NodeRef getEntityNodeRef() {
		return entityNodeRef;
	}

	@Override
	public String toString() {
		return "MultiLevelListData [depth=" + depth + ", tree=" + tree + ", entityNodeRef=" + entityNodeRef + "]";
	}

	public int getSize() {
		return getSize(this,0);
	}

	private int getSize(MultiLevelListData multiLevelListData, int currSize) {
		for (Entry<NodeRef, MultiLevelListData> entry : multiLevelListData.getTree().entrySet()) {
			currSize = getSize(entry.getValue(), currSize+1);
		}
		return currSize;
	}

	
	
	
	
	
}
