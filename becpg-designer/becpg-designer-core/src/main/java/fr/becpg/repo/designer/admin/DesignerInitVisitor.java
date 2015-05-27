/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.designer.admin;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.CompareMimeTypeEvaluator;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.evaluator.compare.ComparePropertyValueOperation;
import org.alfresco.repo.action.executer.AddFeaturesActionExecuter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.admin.impl.AbstractInitVisitorImpl;
import fr.becpg.repo.designer.DesignerModel;
import fr.becpg.repo.helper.ContentHelper;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * Init designer files and folders
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 * 
 */
@Service
public class DesignerInitVisitor extends AbstractInitVisitorImpl  {
	
	private static final String PATH_CONFIGS = "configs";

	private static final String XPATH_DICTIONNARY = "./app:dictionary";

	private static final String PATH_MODELS = "../app:models";
	
	@Autowired
	private ContentHelper contentHelper;
	
	
	@Override
	public void visitContainer(NodeRef companyHome) {
		
		logger.info("Run DesignerInitVisitor");
		
		// System
		NodeRef dictionaryNodeRef = BeCPGQueryBuilder.createQuery().selectNodeByPath(companyHome, XPATH_DICTIONNARY);
		

		// Security
		visitFolder(dictionaryNodeRef, PATH_CONFIGS);
		
		

	}
	
	@Override
	protected void visitRules(NodeRef nodeRef, String folderName) {
		if (folderName == PATH_CONFIGS) {
			addAspectRule(nodeRef, "Add config aspect", "Add model config to xml file", DesignerModel.ASPECT_CONFIG);
			addAspectRule(getModelNodeRef(nodeRef), "Add model aspect", "Add model aspect to xml file", DesignerModel.ASPECT_MODEL);
		}
	}
	


	
	private NodeRef getModelNodeRef(NodeRef configNodeRef) {
		return BeCPGQueryBuilder.createQuery().selectNodeByPath(configNodeRef, PATH_MODELS);
	}

	@Override
	protected void visitFiles(NodeRef folderNodeRef, String folderName) {
		if (folderName == PATH_CONFIGS) {
			contentHelper.addFilesResources(folderNodeRef, "classpath:beCPG/designer/extCustomForm.xml");
			contentHelper.addFilesResources(getModelNodeRef(folderNodeRef), "classpath:beCPG/designer/extCustomModel.xml");
		}
	}
	



	private void addAspectRule(NodeRef nodeRef, String ruleName, String ruleDescription, QName aspectModel) {

		// action
		CompositeAction compositeAction = actionService.createCompositeAction();
		Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, aspectModel);
		Action action = actionService.createAction(AddFeaturesActionExecuter.NAME, params);
		compositeAction.addAction(action);

		// compare-mime-type == text/xml
		ActionCondition conditionOnMimeType = actionService.createActionCondition(CompareMimeTypeEvaluator.NAME);
		conditionOnMimeType.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, MimetypeMap.MIMETYPE_XML);
		conditionOnMimeType.setParameterValue(ComparePropertyValueEvaluator.PARAM_PROPERTY, ContentModel.PROP_CONTENT);
		conditionOnMimeType.setInvertCondition(false);
		compositeAction.addActionCondition(conditionOnMimeType);

		// compare-name == *.xml
		ActionCondition conditionOnName = actionService.createActionCondition(ComparePropertyValueEvaluator.NAME);
		conditionOnName.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.ENDS.toString());
		conditionOnName.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, ".xml");
		conditionOnName.setParameterValue(ComparePropertyValueEvaluator.PARAM_PROPERTY, ContentModel.PROP_NAME);
		conditionOnName.setInvertCondition(false);
		compositeAction.addActionCondition(conditionOnName);

		// rule
		Rule rule = new Rule();
		rule.setRuleType(RuleType.INBOUND);
		rule.setAction(compositeAction);
		rule.applyToChildren(true);
		rule.setTitle(ruleName);
		rule.setExecuteAsynchronously(false);
		rule.setDescription(ruleDescription);
		ruleService.saveRule(nodeRef, rule);

	}


}
