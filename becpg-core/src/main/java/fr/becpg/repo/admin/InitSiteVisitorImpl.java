/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.admin;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.RepoConsts;

// TODO: Auto-generated Javadoc
/**
 * Initialize the folders of a site (create folder, rules, WF).
 *
 * @author querephi
 */
public class InitSiteVisitorImpl extends AbstractInitVisitorImpl implements InitVisitor{


	/* (non-Javadoc)
	 * @see fr.becpg.repo.admin.InitVisitor#visitContainer(org.alfresco.service.cmr.repository.NodeRef, java.util.Locale)
	 */
	@Override
	public void visitContainer(NodeRef docLibNodeRef) {
		
		visitFolder(docLibNodeRef, RepoConsts.PATH_DOCUMENTS);
		visitFolder(docLibNodeRef, RepoConsts.PATH_PRODUCTS);		
	}
}
