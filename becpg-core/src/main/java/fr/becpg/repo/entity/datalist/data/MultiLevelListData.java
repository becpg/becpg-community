/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.entity.datalist.data;

import java.util.*;
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
	private final Map<NodeRef, MultiLevelListData> tree = new LinkedHashMap<>();
	
	private final List<NodeRef> entityNodeRefs;

	private boolean isLeaf = false;
	
	public MultiLevelListData(List<NodeRef> entityNodeRefs,int depth ) {
		super();
		this.depth = depth;
		this.entityNodeRefs = entityNodeRefs;
	}
	
	public MultiLevelListData(NodeRef entityNodeRef,int depth ) {
		super();
		this.depth = depth;
		this.entityNodeRefs = Collections.singletonList(entityNodeRef);
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
	
	
	public boolean isLeaf() {
		return isLeaf;
	}

	public void setLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}

	private int getSize(MultiLevelListData multiLevelListData, int currSize) {
		for (Entry<NodeRef, MultiLevelListData> entry : multiLevelListData.getTree().entrySet()) {
			currSize = getSize(entry.getValue(), currSize+1);
		}
		return currSize;
	}
	
	
	public List<NodeRef> getAllChilds() {
		List<NodeRef> ret = new ArrayList<>();
		
		for (Entry<NodeRef, MultiLevelListData> entry : getTree().entrySet()) {
			ret.add(entry.getValue().getEntityNodeRef());
			ret.addAll(entry.getValue().getAllChilds());
		}
		return ret;
	}
	
	public List<NodeRef> getAllLeafs() {
		List<NodeRef> ret = new ArrayList<>();
		
		for (Entry<NodeRef, MultiLevelListData> entry : getTree().entrySet()) {
			if(entry.getValue().tree.isEmpty()) {
				ret.add(entry.getValue().getEntityNodeRef());
			}
			ret.addAll(entry.getValue().getAllLeafs());
		}
		return ret;
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
