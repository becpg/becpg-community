/*
 * 
 */
package fr.becpg.repo.product;

import org.alfresco.service.cmr.repository.NodeRef;

// TODO: Auto-generated Javadoc
/**
 * The Interface NodeVisitor.
 *
 * @author querephi
 */
public interface NodeVisitor {

	/**
	 * Visit node.
	 *
	 * @param nodeRef the node ref
	 */
	public void visitNode(NodeRef nodeRef);
}
