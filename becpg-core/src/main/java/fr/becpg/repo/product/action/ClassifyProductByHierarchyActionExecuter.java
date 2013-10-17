/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.product.action;

import java.util.List;

import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.product.ProductService;

/**
 * Action used to classify a product according to its hierarchy.
 *
 * @author querephi
 */
public class ClassifyProductByHierarchyActionExecuter extends ActionExecuterAbstractBase{

	/** The Constant NAME. */
	public static final String NAME = "classify-product";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ClassifyProductByHierarchyActionExecuter.class);	
	
	/** The product service. */
	private ProductService productService;
	
	/**
	 * Sets the product service.
	 *
	 * @param productService the new product service
	 */
	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

	/* (non-Javadoc)
	 * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
	 */
	@Override
	protected void executeImpl(Action action, NodeRef nodeRef) {
		
		logger.debug("Start ClassifyProductActionExecuter");		
		productService.classifyProductByHierarchy(action.getNodeRef(), nodeRef);		
	}

	/* (non-Javadoc)
	 * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
	 */
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> arg0) {
		// TODO Auto-generated method stub
		
	}

}
