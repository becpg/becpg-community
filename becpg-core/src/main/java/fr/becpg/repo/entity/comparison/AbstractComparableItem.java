/*
 * 
 */
package fr.becpg.repo.entity.comparison;

import org.alfresco.service.cmr.repository.NodeRef;

// TODO: Auto-generated Javadoc
/**
 * The Class AbstractComparableItem.
 *
 * @author querephi
 */
public class AbstractComparableItem implements Comparable {

	/** The depth level. */
	protected int depthLevel;
	
	/** The pivot. */
	protected String pivot;
	
	/** The node ref. */
	protected NodeRef nodeRef;

	/**
	 * Gets the depth level.
	 *
	 * @return the depth level
	 */
	public int getDepthLevel() {
		return depthLevel;
	}

	/**
	 * Sets the depth level.
	 *
	 * @param depthLevel the new depth level
	 */
	public void setDepthLevel(int depthLevel) {
		this.depthLevel = depthLevel;
	}

	/**
	 * Gets the pivot.
	 *
	 * @return the pivot
	 */
	public String getPivot() {
		return pivot;
	}

	/**
	 * Sets the pivot.
	 *
	 * @param pivot the new pivot
	 */
	public void setPivot(String pivot) {
		this.pivot = pivot;
	}

	/**
	 * Gets the node ref.
	 *
	 * @return the node ref
	 */
	public NodeRef getNodeRef() {
		return nodeRef;
	}

	/**
	 * Sets the node ref.
	 *
	 * @param nodeRef the new node ref
	 */
	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}
	
	/**
	 * Instantiates a new abstract comparable item.
	 *
	 * @param depthLevel the depth level
	 * @param pivot the pivot
	 * @param nodeRef the node ref
	 */
	public AbstractComparableItem(int depthLevel, String pivot, NodeRef nodeRef){
		this.depthLevel = depthLevel;
		this.pivot = pivot;
		this.nodeRef = nodeRef;
	}
	
	
}
