/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.hierarchy.action;

import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.hierarchy.HierarchyService;

/**
 * Action used to classify an entity according to its hierarchy.
 *
 * @author querephi
 */
public class ClassifyByHierarchyActionExecuter extends ActionExecuterAbstractBase{

	/** The Constant NAME. */
	public static final String NAME = "classify-by-hierarchy";
	public static final String PARAM_DESTINATION_FOLDER = "destination-folder";
	public static final String PARAM_PROP_HIERARCHY = "prop-hierarchy";
	
	/** The logger. */
	private static final Log logger = LogFactory.getLog(ClassifyByHierarchyActionExecuter.class);
	
	private HierarchyService hierarchyService;
	
	private NamespaceService namespaceService;
	
	
	public void setHierarchyService(HierarchyService hierarchyService) {
		this.hierarchyService = hierarchyService;
	}
	
	

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}



	@Override
	protected void executeImpl(Action action, NodeRef nodeRef) {
		logger.debug("Start ClassifyByHierarchyActionExecuter");
		NodeRef approveFolder = (NodeRef)action.getParameterValue(PARAM_DESTINATION_FOLDER);
		QName hierarchyQname = null;
		String propHierarchy = (String)action.getParameterValue(PARAM_PROP_HIERARCHY);
		if(propHierarchy!=null && !propHierarchy.isEmpty()){
			hierarchyQname = QName.createQName(propHierarchy, namespaceService);
		}
		
		hierarchyService.classifyByHierarchy(approveFolder, nodeRef, hierarchyQname);		
	}

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		paramList.add(new ParameterDefinitionImpl(PARAM_DESTINATION_FOLDER, DataTypeDefinition.NODE_REF, true, getParamDisplayLabel(PARAM_DESTINATION_FOLDER)));
		paramList.add(new ParameterDefinitionImpl(PARAM_PROP_HIERARCHY, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_PROP_HIERARCHY)));
	}

	
}
