/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.action.executer;

import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.entity.EntityService;

/**
 * Action used to classify an entity according to its hierarchy.
 *
 * @author querephi
 */
public class ClassifyByHierarchyActionExecuter extends ActionExecuterAbstractBase{

	/** The Constant NAME. */
	public static final String NAME = "classify-by-hierarchy";
	public static final String PARAM_DESTINATION_FOLDER = "destination-folder";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ClassifyByHierarchyActionExecuter.class);	
	
	private EntityService entityService;
	
	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

	@Override
	protected void executeImpl(Action action, NodeRef nodeRef) {
		logger.debug("Start ClassifyByHierarchyActionExecuter");
		NodeRef approveFolder = (NodeRef)action.getParameterValue(PARAM_DESTINATION_FOLDER);		
		entityService.classifyByHierarchy(approveFolder, nodeRef);		
	}

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		paramList.add(new ParameterDefinitionImpl(PARAM_DESTINATION_FOLDER, DataTypeDefinition.NODE_REF, true, getParamDisplayLabel(PARAM_DESTINATION_FOLDER)));		
	}

	
}
