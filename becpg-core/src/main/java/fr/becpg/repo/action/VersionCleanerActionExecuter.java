/*
Copyright (C) 2010-2021 beCPG. 
 
This file is part of beCPG 
 
beCPG is free software: you can redistribute it and/or modify 
it under the terms of the GNU Lesser General Public License as published by 
the Free Software Foundation, either version 3 of the License, or 
(at your option) any later version. 
 
beCPG is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 

MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
GNU Lesser General Public License for more details. 
 
You should have received a copy of the GNU Lesser General Public License 
along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.becpg.repo.action;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.version.EntityVersionService;

/**
 * <p>VersionCleanerActionExecuter class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class VersionCleanerActionExecuter extends ActionExecuterAbstractBase {

	/** Constant <code>NAME="version-cleaner"</code> */
	public static final String NAME = "version-cleaner";

	/** Constant <code>PARAM_VERSION_TYPE="versionType"</code> */
	public static final String PARAM_VERSION_TYPE = "versionType";
	/** Constant <code>PARAM_NUMBER_OF_VERSION="numberOfVersion"</code> */
	public static final String PARAM_NUMBER_OF_VERSION = "numberOfVersion";
	/** Constant <code>PARAM_NUMBER_OF_DAY="numberOfDay"</code> */
	public static final String PARAM_NUMBER_OF_DAY = "numberOfDay";
	/** Constant <code>PARAM_NUMBER_BY_DAY="numberByDay"</code> */
	public static final String PARAM_NUMBER_BY_DAY = "numberByDay";

	private final Log logger = LogFactory.getLog(VersionCleanerActionExecuter.class);

	private NodeService nodeService;
	private RuleService ruleService;
	private VersionService versionService;
	private EntityVersionService entityVersionService;
	
	private EntityDictionaryService entityDictionaryService;

	/**
	 * <p>Setter for the field <code>versionService</code>.</p>
	 *
	 * @param versionService a {@link org.alfresco.service.cmr.version.VersionService} object.
	 */
	public void setVersionService(VersionService versionService) {
		this.versionService = versionService;
	}

	/**
	 * <p>Setter for the field <code>ruleService</code>.</p>
	 *
	 * @param ruleService a {@link org.alfresco.service.cmr.rule.RuleService} object.
	 */
	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	/**
	 * <p>Setter for the field <code>entityDictionaryService</code>.</p>
	 *
	 * @param entityDictionaryService a {@link fr.becpg.repo.entity.EntityDictionaryService} object.
	 */
	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}

	/**
	 * <p>Setter for the field <code>entityVersionService</code>.</p>
	 *
	 * @param entityVersionService a {@link fr.becpg.repo.entity.version.EntityVersionService} object.
	 */
	public void setEntityVersionService(EntityVersionService entityVersionService) {
		this.entityVersionService = entityVersionService;
	}

	/** {@inheritDoc} */
	@Override
	public void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef) {
		if (nodeService.exists(actionedUponNodeRef)) {

			VersionCleanerActionConfig versionConfig = new VersionCleanerActionConfig();

			boolean isLastAction = false;
			for (Rule rule : ruleService.getRules(actionedUponNodeRef)) {
				if (!rule.getRuleDisabled()) {

					if (rule.getAction() instanceof CompositeAction) {
						for (Action compositeAction : ((CompositeAction) rule.getAction()).getActions()) {
							isLastAction =  parseAction(ruleAction, compositeAction, versionConfig,isLastAction);
						}
					} else {
						isLastAction =  parseAction(ruleAction, rule.getAction(), versionConfig,isLastAction);
					}
				}
			}
			if (isLastAction) {
				logger.debug("Applying version config :" + versionConfig.toString());
				logger.debug("To node " + nodeService.getProperty(actionedUponNodeRef, ContentModel.PROP_NAME));
				VersionHistory versionHistory = versionService.getVersionHistory(actionedUponNodeRef);
				if (versionHistory != null) {
					for (Version version : versionConfig.versionsToDelete(versionHistory.getAllVersions())) {
						logger.info("Deleting version :" + version.getVersionLabel());
						if(entityDictionaryService.isSubClass(nodeService.getType(actionedUponNodeRef), BeCPGModel.TYPE_ENTITY_V2)){
							logger.info(" -- deleting  entity version ");
							entityVersionService.deleteEntityVersion(version);
						}
						versionService.deleteVersion(actionedUponNodeRef, version);
					}
				}

			}

		}
	}

	private boolean parseAction(Action ruleAction, Action compositeAction, VersionCleanerActionConfig versionConfig, boolean isLastAction) {
		
		if(logger.isDebugEnabled()) {
			logger.debug("Test matching rule : " + compositeAction.getActionDefinitionName() + " with " + ruleAction.getActionDefinitionName());
		}
		if (compositeAction.getActionDefinitionName().equals(ruleAction.getActionDefinitionName())) {
			logger.debug("Match OK");

			Integer numberOfDay = (Integer) compositeAction.getParameterValue(PARAM_NUMBER_OF_DAY);
			Integer numberByDay = (Integer) compositeAction.getParameterValue(PARAM_NUMBER_BY_DAY);
			Integer numberOfVersion = (Integer) compositeAction.getParameterValue(PARAM_NUMBER_OF_VERSION);
			String versionType = (String) compositeAction.getParameterValue(PARAM_VERSION_TYPE);

			versionConfig.setConfig(versionType, numberOfVersion, numberOfDay, numberByDay);
			if (ruleAction.equals(compositeAction)) {
				isLastAction = true;
			} else {
				isLastAction = false;
			}

		}
		return isLastAction;
	}

	/** {@inheritDoc} */
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		paramList.add(new ParameterDefinitionImpl(PARAM_VERSION_TYPE, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_VERSION_TYPE)));
		paramList.add(new ParameterDefinitionImpl(PARAM_NUMBER_BY_DAY, DataTypeDefinition.INT, false, getParamDisplayLabel(PARAM_NUMBER_BY_DAY)));
		paramList.add(new ParameterDefinitionImpl(PARAM_NUMBER_OF_DAY, DataTypeDefinition.INT, false, getParamDisplayLabel(PARAM_NUMBER_OF_DAY)));
		paramList.add(new ParameterDefinitionImpl(PARAM_NUMBER_OF_VERSION, DataTypeDefinition.INT, false, getParamDisplayLabel(PARAM_NUMBER_OF_VERSION)));
	}

}
