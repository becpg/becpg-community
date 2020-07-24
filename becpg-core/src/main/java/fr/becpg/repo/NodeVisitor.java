/*
 * 
 */
package fr.becpg.repo;

import org.alfresco.service.cmr.repository.NodeRef;


/**
 * The Interface NodeVisitor.
 *
 * @author querephi
 * @version $Id: $Id
 */
public interface NodeVisitor {

	/**
	 * Visit node.
	 *
	 * @param nodeRef the node ref
	 */
	void visitNode(NodeRef nodeRef);
}
