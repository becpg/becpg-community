/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.product.action;

import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.product.ProductService;

/**
 * Action used to classify a product according to its hierarchy.
 *
 * @author querephi
 */
public class ClassifyProductActionExecuter extends ActionExecuterAbstractBase{

	/** The Constant NAME. */
	public static final String NAME = "classify-product";
	public static final String PARAM_CLASSIFY_FOLDER = "classify-folder";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ClassifyProductActionExecuter.class);	
	
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

	@Override
	protected void executeImpl(Action action, NodeRef nodeRef) {
		logger.debug("Start ClassifyProductActionExecuter");
		NodeRef approveFolder = (NodeRef)action.getParameterValue(PARAM_CLASSIFY_FOLDER);
		productService.classifyProductByHierarchy(approveFolder, nodeRef);		
	}

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		paramList.add(new ParameterDefinitionImpl(PARAM_CLASSIFY_FOLDER, DataTypeDefinition.NODE_REF, true, getParamDisplayLabel(PARAM_CLASSIFY_FOLDER)));		
	}

}
