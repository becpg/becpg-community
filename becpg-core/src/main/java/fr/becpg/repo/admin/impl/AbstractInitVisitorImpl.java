/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.admin.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.action.evaluator.HasAspectEvaluator;
import org.alfresco.repo.action.evaluator.IsSubTypeEvaluator;
import org.alfresco.repo.action.executer.AddFeaturesActionExecuter;
import org.alfresco.repo.action.executer.SpecialiseTypeActionExecuter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.repo.admin.InitVisitor;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;

/**
 * Abstract class used to initialize repository, modules.
 *
 * @author querephi
 */
public abstract class AbstractInitVisitorImpl implements InitVisitor {		
	
	protected static Log logger = LogFactory.getLog(AbstractInitVisitorImpl.class);
	
	@Autowired
	protected NodeService nodeService;
	
	@Autowired
	protected FileFolderService fileFolderService;
	
	@Autowired
	protected RuleService ruleService;
	
	@Autowired
	protected RepoService repoService;

	@Autowired
	protected ActionService actionService;

		
	/**
	 * Visit folder.
	 *
	 * @param parentNodeRef the parent node ref
	 * @param folderPath the folder path
	 * @param locale the locale
	 * @return the node ref
	 */
	protected NodeRef visitFolder(NodeRef parentNodeRef, String folderPath) {
		
		//get translated message			
		String folderName = TranslateHelper.getTranslatedPath(folderPath);		
		if(folderName == null){
			folderName = folderPath;
		}				
		NodeRef folderNodeRef = repoService.getFolderByPath(parentNodeRef, folderPath);		
	    if(folderNodeRef == null){

	    	logger.debug("Create folder, path: " + folderPath + " - translatedName: " + folderName);	    		    	
	    	//logger.debug("QName: " + QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, folderPath));
	    	
	    	folderNodeRef = repoService.getOrCreateFolderByPath(parentNodeRef, folderPath, folderName);
	    	
	    	visitRules(folderNodeRef, folderPath);
	    	visitWF(folderNodeRef, folderPath);	    	
	    }
	    
	    visitPermissions(folderNodeRef, folderName);
	    visitFiles(folderNodeRef, folderPath);
	    vivitFolderAspects(folderNodeRef, folderPath);
	    
	    return folderNodeRef;
	}
	
	protected void vivitFolderAspects(NodeRef folderNodeRef, String folderName) {

		
	}

	protected void visitFiles(NodeRef folderNodeRef, String folderName) {
		
	}

	/**
	 * Visit rules.
	 *
	 * @param nodeRef the node ref
	 * @param folderName the folder name
	 */
	protected void visitRules(NodeRef nodeRef, String folderName) {
	}
	
	/**
	 * Visit wf.
	 *
	 * @param nodeRef the node ref
	 * @param folderName the folder name
	 */
	protected void visitWF(NodeRef nodeRef, String folderName) {
	}
	
	/**
	 * Visit permissions.
	 *
	 * @param nodeRef the node ref
	 * @param folderName the folder name
	 */
	protected void visitPermissions(NodeRef nodeRef, String folderName) {
	}
	
	/**
	 * Creates the rule specialise type.
	 *
	 * @param nodeRef the node ref
	 * @param applyToChildren the apply to children
	 * @param type the type
	 */
	protected void createRuleSpecialiseType(NodeRef nodeRef, boolean applyToChildren, QName type){
		
	    // Action : apply type
	    Map<String,Serializable> params = new HashMap<String, Serializable>();
  	  	params.put(SpecialiseTypeActionExecuter.PARAM_TYPE_NAME, type);
	    CompositeAction compositeAction = actionService.createCompositeAction();
	    Action myAction= actionService.createAction(SpecialiseTypeActionExecuter.NAME, params);
	    compositeAction.addAction(myAction);
	    	   
	    // Conditions for the Rule : type must be different
	    ActionCondition actionCondition = actionService.createActionCondition(IsSubTypeEvaluator.NAME);
	    actionCondition.setParameterValue(IsSubTypeEvaluator.PARAM_TYPE, type);
	    actionCondition.setInvertCondition(true);
	    compositeAction.addActionCondition(actionCondition);
	   	    
	    // Create Rule
		Rule rule = new Rule();
	    rule.setTitle("Specialise type");
	    rule.setDescription("Every item created will have this type");
	    rule.applyToChildren(applyToChildren);
	    rule.setExecuteAsynchronously(false);
	    rule.setRuleDisabled(false);
	    rule.setRuleType(RuleType.INBOUND);
	    rule.setAction(compositeAction);	    	       	    
	    ruleService.saveRule(nodeRef, rule);
	}
	
	protected void createRuleAspect(NodeRef nodeRef, boolean applyToChildren, QName type , QName aspect) {

		// action
		CompositeAction compositeAction = actionService.createCompositeAction();
		Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, aspect);
		Action action = actionService.createAction(AddFeaturesActionExecuter.NAME, params);
		compositeAction.addAction(action);

		// Conditions for the Rule : type must be equals
	    ActionCondition typeCondition = actionService.createActionCondition(IsSubTypeEvaluator.NAME);
	    typeCondition.setParameterValue(IsSubTypeEvaluator.PARAM_TYPE, type);
	    typeCondition.setInvertCondition(false);
		compositeAction.addActionCondition(typeCondition);
		
		ActionCondition aspectCondition = actionService.createActionCondition(HasAspectEvaluator.NAME);
		aspectCondition.setParameterValue(HasAspectEvaluator.PARAM_ASPECT, aspect);
		aspectCondition.setInvertCondition(true);
		compositeAction.addActionCondition(aspectCondition);

		// rule
		Rule rule = new Rule();
		rule.setTitle("Add entityTpl aspect");
		rule.setDescription("Add entityTpl aspect to the created node");
		rule.applyToChildren(applyToChildren);
	    rule.setExecuteAsynchronously(false);
	    rule.setRuleDisabled(false);
		rule.setRuleType(RuleType.INBOUND);
		rule.setAction(compositeAction);		
		ruleService.saveRule(nodeRef, rule);

	}
}
