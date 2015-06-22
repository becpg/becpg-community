/*
 * 
 */
package fr.becpg.repo;

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
	void visitNode(NodeRef nodeRef);
}
