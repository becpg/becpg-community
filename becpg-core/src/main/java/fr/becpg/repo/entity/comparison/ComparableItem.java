/*
 * 
 */
package fr.becpg.repo.entity.comparison;

import org.alfresco.service.cmr.repository.NodeRef;

// TODO: Auto-generated Javadoc
/**
 * The Class ComparableItem.
 *
 * @author querephi
 */
public class ComparableItem extends AbstractComparableItem {
	
	/**
	 * Instantiates a new comparable item.
	 *
	 * @param depthLevel the depth level
	 * @param pivot the pivot
	 * @param nodeRef the node ref
	 */
	public ComparableItem(int depthLevel, String pivot, NodeRef nodeRef){
		super(depthLevel, pivot, nodeRef);
	}
}
