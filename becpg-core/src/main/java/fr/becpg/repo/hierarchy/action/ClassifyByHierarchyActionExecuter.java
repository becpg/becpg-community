/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.hierarchy.action;

import java.util.List;
import java.util.Locale;

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

import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.hierarchy.HierarchyService;

/**
 * Action used to classify an entity according to its hierarchy.
 *
 * @author querephi
 * @version $Id: $Id
 */
public class ClassifyByHierarchyActionExecuter extends ActionExecuterAbstractBase{

	/** The Constant NAME. */
	public static final String NAME = "classify-by-hierarchy";
	/** Constant <code>PARAM_DESTINATION_FOLDER="destination-folder"</code> */
	public static final String PARAM_DESTINATION_FOLDER = "destination-folder";
	/** Constant <code>PARAM_PROP_HIERARCHY="prop-hierarchy"</code> */
	public static final String PARAM_PROP_HIERARCHY = "prop-hierarchy";
	/** Constant <code>PARAM_PROP_LOCALE="prop-locale"</code> */
	public static final String PARAM_PROP_LOCALE = "prop-locale";
	
	
	private static final Log logger = LogFactory.getLog(ClassifyByHierarchyActionExecuter.class);
	
	private HierarchyService hierarchyService;
	
	private NamespaceService namespaceService;
	
	
	/**
	 * <p>Setter for the field <code>hierarchyService</code>.</p>
	 *
	 * @param hierarchyService a {@link fr.becpg.repo.hierarchy.HierarchyService} object.
	 */
	public void setHierarchyService(HierarchyService hierarchyService) {
		this.hierarchyService = hierarchyService;
	}
	
	

	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object.
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}



	/** {@inheritDoc} */
	@Override
	protected void executeImpl(Action action, NodeRef nodeRef) {
		logger.debug("Start ClassifyByHierarchyActionExecuter");
		NodeRef approveFolder = (NodeRef)action.getParameterValue(PARAM_DESTINATION_FOLDER);
		QName hierarchyQname = null;
		String propHierarchy = (String)action.getParameterValue(PARAM_PROP_HIERARCHY);
		if(propHierarchy!=null && !propHierarchy.isEmpty()){
			hierarchyQname = QName.createQName(propHierarchy, namespaceService);
		}
		
		Locale  locale = Locale.getDefault();
		
		String propLocale = (String)action.getParameterValue(PARAM_PROP_LOCALE);
		if(propLocale!=null && !propLocale.isEmpty()){
			locale = MLTextHelper.parseLocale(propLocale);
		}
		
		
		hierarchyService.classifyByHierarchy(approveFolder, nodeRef, hierarchyQname, locale);		
	}

	/** {@inheritDoc} */
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		paramList.add(new ParameterDefinitionImpl(PARAM_DESTINATION_FOLDER, DataTypeDefinition.NODE_REF, true, getParamDisplayLabel(PARAM_DESTINATION_FOLDER)));
		paramList.add(new ParameterDefinitionImpl(PARAM_PROP_HIERARCHY, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_PROP_HIERARCHY)));
		paramList.add(new ParameterDefinitionImpl(PARAM_PROP_LOCALE, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_PROP_LOCALE)));
	}

	
}
