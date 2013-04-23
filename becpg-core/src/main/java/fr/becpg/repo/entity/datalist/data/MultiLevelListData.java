package fr.becpg.repo.entity.datalist.data;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
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
	
	private List<NodeRef> entityNodeRefs;

	
	public MultiLevelListData(List<NodeRef> entityNodeRefs,int depth ) {
		super();
		this.depth = depth;
		this.entityNodeRefs = entityNodeRefs;
	}
	
	public MultiLevelListData(NodeRef entityNodeRef,int depth ) {
		super();
		this.depth = depth;
		this.entityNodeRefs = Arrays.asList(entityNodeRef);
	}

	public int getDepth() {
		return depth;
	}

	public Map<NodeRef, MultiLevelListData> getTree() {
		return tree;
	}
	

	public NodeRef getEntityNodeRef() {
		return entityNodeRefs!=null && entityNodeRefs.size()>0 ? entityNodeRefs.get(0) : null;
	}

	

	public List<NodeRef> getEntityNodeRefs() {
		return entityNodeRefs;
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
	
	

	@Override
	public String toString() {
		return "MultiLevelListData [depth=" + depth + ", tree=" + tree + ", entityNodeRefs=" + entityNodeRefs + "]";
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + depth;
		result = prime * result + ((entityNodeRefs == null) ? 0 : entityNodeRefs.hashCode());
		result = prime * result + ((tree == null) ? 0 : tree.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MultiLevelListData other = (MultiLevelListData) obj;
		if (depth != other.depth)
			return false;
		if (entityNodeRefs == null) {
			if (other.entityNodeRefs != null)
				return false;
		} else if (!entityNodeRefs.equals(other.entityNodeRefs))
			return false;
		if (tree == null) {
			if (other.tree != null)
				return false;
		} else if (!tree.equals(other.tree))
			return false;
		return true;
	}


	
	
	
	
	
}
