/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.admin;

import java.util.Locale;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.product.ProductDictionaryService;

// TODO: Auto-generated Javadoc
/**
 * Initialize the folders of a site (create folder, rules, WF).
 *
 * @author querephi
 */
public class InitSiteVisitorImpl extends AbstractInitVisitorImpl implements InitVisitor{

	/** The product dictionary service. */
	private ProductDictionaryService productDictionaryService;
	
	/**
	 * Sets the product dictionary service.
	 *
	 * @param productDictionaryService the new product dictionary service
	 */
	public void setProductDictionaryService(ProductDictionaryService productDictionaryService) {
		this.productDictionaryService = productDictionaryService;
	}

	/* (non-Javadoc)
	 * @see fr.becpg.repo.admin.InitVisitor#visitContainer(org.alfresco.service.cmr.repository.NodeRef, java.util.Locale)
	 */
	@Override
	public void visitContainer(NodeRef docLibNodeRef) {
		
		visitFolder(docLibNodeRef, RepoConsts.PATH_DOCUMENTS);
		visitFolder(docLibNodeRef, RepoConsts.PATH_PRODUCTS);		
	}
}
