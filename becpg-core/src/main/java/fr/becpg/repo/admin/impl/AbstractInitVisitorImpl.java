/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.admin.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.HasAspectEvaluator;
import org.alfresco.repo.action.evaluator.IsSubTypeEvaluator;
import org.alfresco.repo.action.executer.AddFeaturesActionExecuter;
import org.alfresco.repo.action.executer.SpecialiseTypeActionExecuter;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.admin.InitVisitor;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;

/**
 * Abstract class used to initialize repository, modules.
 *
 * @author querephi
 * @version $Id: $Id
 */
public abstract class AbstractInitVisitorImpl implements InitVisitor {

	/** Constant <code>logger</code> */
	protected static final Log logger = LogFactory.getLog(AbstractInitVisitorImpl.class);

	private static final String LOCALIZATION_PFX_GROUP = "becpg.group";

	/**
	 * The action service for managing actions.
	 */
	@Autowired
	protected ActionService actionService;

	/**
	 * The authority service for managing authorities.
	 */
	@Autowired
	protected AuthorityService authorityService;

	/**
	 * The file folder service used for file operations.
	 */
	@Autowired
	protected FileFolderService fileFolderService;

	/**
	 * The node service for node operations.
	 */
	@Autowired
	protected NodeService nodeService;

	/**
	 * The repository service for repository operations.
	 */
	@Autowired
	protected RepoService repoService;

	/**
	 * The rule service for managing rules.
	 */
	@Autowired
	protected RuleService ruleService;

	/**
	 * <p>visitFolder.</p>
	 *
	 * @param parentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param folderPath a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	protected NodeRef visitFolder(NodeRef parentNodeRef, String folderPath) {

		boolean folderExists = true;
		
		// get translated message
		String folderName = TranslateHelper.getTranslatedPath(folderPath);
		if (folderName == null) {
			folderName = folderPath;
		}
		NodeRef folderNodeRef = repoService.getFolderByPath(parentNodeRef, folderPath);
		MLText mlTitle = TranslateHelper.getTranslatedPathMLText(folderPath);

		if (folderNodeRef == null) {
			
			folderExists = false;

			logger.info("Create folder, path: " + folderPath + " - translatedName: " + folderName);

			folderNodeRef = repoService.getOrCreateFolderByPath(parentNodeRef, folderPath, folderName);
			nodeService.setProperty(folderNodeRef, ContentModel.PROP_TITLE, mlTitle);

			visitWF(folderNodeRef, folderPath);
		} else {
			if(!folderName.equals(nodeService.getProperty(folderNodeRef, ContentModel.PROP_NAME))) {
				nodeService.setProperty(folderNodeRef, ContentModel.PROP_NAME, folderName);
			}
			
			boolean isMLAware = MLPropertyInterceptor.setMLAware(true);
			try {

				MLText title = (MLText) nodeService.getProperty(folderNodeRef, ContentModel.PROP_TITLE);
				if (title == null) {
					nodeService.setProperty(folderNodeRef, ContentModel.PROP_TITLE, mlTitle);
				} else {
					nodeService.setProperty(folderNodeRef, ContentModel.PROP_TITLE, MLTextHelper.merge(title, mlTitle));
				}
			} finally {
				MLPropertyInterceptor.setMLAware(isMLAware);
			}

		}

		visitRules(folderNodeRef, folderPath);
		visitPermissions(folderNodeRef, folderPath);
		visitFiles(folderNodeRef, folderPath, folderExists);
		vivitFolderAspects(folderNodeRef, folderPath);

		return folderNodeRef;
	}

	/**
	 * <p>vivitFolderAspects.</p>
	 *
	 * @param folderNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param folderName a {@link java.lang.String} object.
	 */
	protected void vivitFolderAspects(NodeRef folderNodeRef, String folderName) {

	}

	/**
	 * <p>visitFiles.</p>
	 *
	 * @param folderNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param folderName a {@link java.lang.String} object.
	 * @param folderExists a boolean.
	 */
	protected void visitFiles(NodeRef folderNodeRef, String folderName, boolean folderExists) {

	}

	/**
	 * <p>visitRules.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param folderName a {@link java.lang.String} object.
	 */
	protected void visitRules(NodeRef nodeRef, String folderName) {
	}

	/**
	 * <p>visitWF.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param folderName a {@link java.lang.String} object.
	 */
	protected void visitWF(NodeRef nodeRef, String folderName) {
	}

	/**
	 * <p>visitPermissions.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param folderName a {@link java.lang.String} object.
	 */
	protected void visitPermissions(NodeRef nodeRef, String folderName) {
	}

	/**
	 * <p>createRuleSpecialiseType.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param applyToChildren a boolean.
	 * @param type a {@link org.alfresco.service.namespace.QName} object.
	 */
	protected void createRuleSpecialiseType(NodeRef nodeRef, boolean applyToChildren, QName type) {

		// Action : apply type
		Map<String, Serializable> params = new HashMap<>();
		params.put(SpecialiseTypeActionExecuter.PARAM_TYPE_NAME, type);
		CompositeAction compositeAction = actionService.createCompositeAction();
		Action myAction = actionService.createAction(SpecialiseTypeActionExecuter.NAME, params);
		compositeAction.addAction(myAction);

		// Conditions for the Rule : type must be different
		ActionCondition actionCondition = actionService.createActionCondition(IsSubTypeEvaluator.NAME);
		actionCondition.setParameterValue(IsSubTypeEvaluator.PARAM_TYPE, type);
		actionCondition.setInvertCondition(true);
		compositeAction.addActionCondition(actionCondition);

		createRule(nodeRef, "Specialise type", "Every item created will have this type", applyToChildren, false, List.of(RuleType.INBOUND), compositeAction);
	}

	/**
	 * <p>createRule.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param title a {@link java.lang.String} object
	 * @param description a {@link java.lang.String} object
	 * @param applyToChildren a boolean
	 * @param compositeAction a {@link org.alfresco.service.cmr.action.CompositeAction} object
	 * @param executeAsync a boolean
	 * @param ruleTypes a {@link java.util.List} object
	 */
	protected void createRule(NodeRef nodeRef, String title, String description, boolean applyToChildren, boolean executeAsync, List<String> ruleTypes,  CompositeAction compositeAction) {
		
		if (ruleExists(nodeRef, title, description)) {
			return;
		}
		
		// Create Rule
		Rule rule = new Rule();
		rule.setTitle(title);
		rule.setDescription(description);
		rule.applyToChildren(applyToChildren);
		rule.setExecuteAsynchronously(executeAsync);
		rule.setRuleDisabled(false);
		rule.setRuleTypes(ruleTypes);
		rule.setAction(compositeAction);
		ruleService.saveRule(nodeRef, rule);
	}

	private boolean ruleExists(NodeRef nodeRef, String ruleTitle, String ruleDescription) {
		if (ruleService.getRules(nodeRef, false).stream().anyMatch(r -> ruleTitle.equals(r.getTitle()) && ruleDescription.equals(r.getDescription()))) {
			return true;
		}
		logger.info("Rule does not exist: " + ruleTitle);
		return false;
	}

	/**
	 * <p>createRuleAspect.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param applyToChildren a boolean.
	 * @param type a {@link org.alfresco.service.namespace.QName} object.
	 * @param aspect a {@link org.alfresco.service.namespace.QName} object.
	 */
	protected void createRuleAspect(NodeRef nodeRef, boolean applyToChildren, QName type, QName aspect) {

		// action
		CompositeAction compositeAction = actionService.createCompositeAction();
		Map<String, Serializable> params = new HashMap<>();
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

		createRule(nodeRef, "Add entityTpl aspect", "Add entityTpl aspect to the created node", applyToChildren, false, List.of(RuleType.INBOUND), compositeAction);
		
	}

	/**
	 * <p>createGroups.</p>
	 *
	 * @param groups an array of {@link java.lang.String} objects.
	 */
	protected void createGroups(String[] groups) {

		Set<String> zones = new HashSet<>();
		zones.add(AuthorityService.ZONE_APP_DEFAULT);
		// #3204 zones.add(AuthorityService.ZONE_APP_SHARE);
		zones.add(AuthorityService.ZONE_AUTH_ALFRESCO);

		for (String group : groups) {

			logger.debug("group: " + group);
			String groupName = I18NUtil.getMessage(String.format("%s.%s", LOCALIZATION_PFX_GROUP, group).toLowerCase(), Locale.getDefault());

			if (!authorityService.authorityExists(PermissionService.GROUP_PREFIX + group)) {
				logger.debug("create group: " + groupName);
				authorityService.createAuthority(AuthorityType.GROUP, group, groupName, zones);
			} else {
				Set<String> zonesAdded = authorityService.getAuthorityZones(PermissionService.GROUP_PREFIX + group);
				Set<String> zonesToAdd = new HashSet<>();
				for (String zone : zones) {
					if (!zonesAdded.contains(zone)) {
						zonesToAdd.add(zone);
					}
				}

				if (!zonesToAdd.isEmpty()) {
					logger.debug("Add group to zone: " + groupName + " - " + zonesToAdd.toString());
					authorityService.addAuthorityToZones(PermissionService.GROUP_PREFIX + group, zonesToAdd);
				}
			}
		}

	}

	/**
	 * <p>addSystemFolderAspect.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	protected void addSystemFolderAspect(NodeRef nodeRef) {
		if (!nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_SYSTEM_FOLDER)) {
			nodeService.addAspect(nodeRef, BeCPGModel.ASPECT_SYSTEM_FOLDER, null);
		}
	}
}
