/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG. 
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
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class MultiLevelListData {
	
	private int depth = 0;
	
	//Keep order
	private final Map<NodeRef, MultiLevelListData> tree = new LinkedHashMap<>();
	
	private final List<NodeRef> entityNodeRefs;

	private boolean isLeaf = false;
	
	/**
	 * <p>Constructor for MultiLevelListData.</p>
	 *
	 * @param entityNodeRefs a {@link java.util.List} object.
	 * @param depth a int.
	 */
	public MultiLevelListData(List<NodeRef> entityNodeRefs,int depth ) {
		super();
		this.depth = depth;
		this.entityNodeRefs = entityNodeRefs;
	}
	
	/**
	 * <p>Constructor for MultiLevelListData.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param depth a int.
	 */
	public MultiLevelListData(NodeRef entityNodeRef,int depth ) {
		super();
		this.depth = depth;
		this.entityNodeRefs = Collections.singletonList(entityNodeRef);
	}

	/**
	 * <p>Getter for the field <code>depth</code>.</p>
	 *
	 * @return a int.
	 */
	public int getDepth() {
		return depth;
	}

	/**
	 * <p>Getter for the field <code>tree</code>.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<NodeRef, MultiLevelListData> getTree() {
		return tree;
	}
	

	/**
	 * <p>getEntityNodeRef.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef getEntityNodeRef() {
		return entityNodeRefs!=null && !entityNodeRefs.isEmpty() ? entityNodeRefs.get(0) : null;
	}

	
	/**
	 * <p>Getter for the field <code>entityNodeRefs</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<NodeRef> getEntityNodeRefs() {
		return entityNodeRefs;
	}

	/**
	 * <p>getSize.</p>
	 *
	 * @return a int.
	 */
	public int getSize() {
		return getSize(this,0);
	}
	
	
	public boolean isEmpty() {
		return  tree.isEmpty();
	}
	
	/**
	 * <p>isLeaf.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isLeaf() {
		return isLeaf && isEmpty();
	}

	/**
	 * <p>setLeaf.</p>
	 *
	 * @param isLeaf a boolean.
	 */
	public void setLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}

	private int getSize(MultiLevelListData multiLevelListData, int currSize) {
		for (Entry<NodeRef, MultiLevelListData> entry : multiLevelListData.getTree().entrySet()) {
			currSize = getSize(entry.getValue(), currSize+1);
		}
		return currSize;
	}
	
	
	/**
	 * <p>getAllChilds.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<NodeRef> getAllChilds() {
		List<NodeRef> ret = new ArrayList<>();
		
		for (Entry<NodeRef, MultiLevelListData> entry : getTree().entrySet()) {
			ret.add(entry.getValue().getEntityNodeRef());
			ret.addAll(entry.getValue().getAllChilds());
		}
		return ret;
	}
	
	/**
	 * <p>getAllLeafs.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
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
	

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "MultiLevelListData [depth=" + depth + ", tree=" + tree + ", entityNodeRefs=" + entityNodeRefs + "]";
	}
	
	
	@Override
	public int hashCode() {
		return Objects.hash(depth, entityNodeRefs, isLeaf, tree);
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
		return depth == other.depth && Objects.equals(entityNodeRefs, other.entityNodeRefs) && isLeaf == other.isLeaf
				&& Objects.equals(tree, other.tree);
	}
	
	
	
}
