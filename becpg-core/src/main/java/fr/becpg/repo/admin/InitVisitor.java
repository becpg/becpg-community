/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.admin;

import java.util.Locale;

import org.alfresco.service.cmr.repository.NodeRef;

// TODO: Auto-generated Javadoc
/**
 * Interface used to initialize a node.
 *
 * @author querephi
 */
public interface InitVisitor {
	
	/**
	 * Visit container.
	 *
	 * @param nodeRef the node ref
	 * @param locale the locale
	 */
	public void visitContainer(NodeRef nodeRef, Locale locale);
}
