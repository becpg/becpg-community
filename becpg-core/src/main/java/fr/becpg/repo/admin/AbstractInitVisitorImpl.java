/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.admin;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.IsSubTypeEvaluator;
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
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;

// TODO: Auto-generated Javadoc
/**
 * Abstract class used to initialize repository, modules.
 *
 * @author querephi
 */
public abstract class AbstractInitVisitorImpl {		
	
	/** The logger. */
	protected static Log logger = LogFactory.getLog(InitRepoVisitorImpl.class);
	
	/** The node service. */
	protected NodeService nodeService = null;
	
	/** The file folder service. */
	protected FileFolderService fileFolderService = null;
	
	/** The rule service. */
	protected RuleService ruleService = null;
	
	/** The repo service. */
	protected RepoService repoService = null;
	
	/** The action service. */
	protected ActionService actionService = null;
					
	/**
	 * Sets the node service.
	 *
	 * @param nodeService the new node service
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	/**
	 * Sets the file folder service.
	 *
	 * @param fileFolderService the new file folder service
	 */
	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}
	
	/**
	 * Sets the rule service.
	 *
	 * @param ruleService the new rule service
	 */
	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}
	
	/**
	 * Sets the repo service.
	 *
	 * @param repoService the new repo service
	 */
	public void setRepoService(RepoService repoService) {
		this.repoService = repoService;
	}	
	
	/**
	 * Sets the action service.
	 *
	 * @param actionService the new action service
	 */
	public void setActionService(ActionService actionService) {
		this.actionService = actionService;
	}
		
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
		
		NodeRef folderNodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, folderName);
	    if(folderNodeRef == null){

	    	logger.debug("Create folder, path: " + folderPath + " - translatedName: " + folderName);	    		    	
	    	//logger.debug("QName: " + QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, folderPath));
	    	
	    	Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
	    	properties.put(ContentModel.PROP_NAME, folderName);	    		    	
	    	
	    	folderNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS, 
	    											QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, folderPath), 
	    											ContentModel.TYPE_FOLDER, properties).getChildRef();
	    	
	    	visitRules(folderNodeRef, folderPath);
	    	visitWF(folderNodeRef, folderPath);	    	
	    }
	    
	    visitPermissions(folderNodeRef, folderName);
	    
	    return folderNodeRef;
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
}
