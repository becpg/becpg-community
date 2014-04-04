/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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
package fr.becpg.repo.admin.impl;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.evaluator.IsSubTypeEvaluator;
import org.alfresco.repo.action.evaluator.compare.ComparePropertyValueOperation;
import org.alfresco.repo.action.executer.SpecialiseTypeActionExecuter;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;
import fr.becpg.model.SecurityModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.helper.ContentHelper;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;
import fr.becpg.report.client.ReportFormat;

@Service
public class CoreInitVisitor extends AbstractInitVisitorImpl {
	
	private static final String COMPARE_ENTITIES_REPORT_PATH = "beCPG/birt/system/CompareEntities.rptdesign";
	
	@Autowired
	private DictionaryDAO dictionaryDAO;
	
	@Autowired
	private QNameDAO qNameDAO;
	
	@Autowired
	private ContentHelper contentHelper;
	
	@Autowired
	private EntityTplService entityTplService;
	
	@Autowired
	private ReportTplService reportTplService;
	
	
	@Override
	public void visitContainer(NodeRef companyHome) {
		logger.info("Run CoreInitVisitor");

		//Init QNames for dbQueries
		for(QName model : dictionaryDAO.getModels()){
			for(PropertyDefinition propertyDef : dictionaryDAO.getProperties(model)){
				qNameDAO.getOrCreateQName(propertyDef.getName());
			}
		}		
		
		// System
		logger.debug("Visit folders");
		NodeRef systemNodeRef = visitFolder(companyHome, RepoConsts.PATH_SYSTEM);
		

		// Security
		visitFolder(systemNodeRef, RepoConsts.PATH_SECURITY);
		

		// Icons
		visitFolder(systemNodeRef, RepoConsts.PATH_ICON);


		//OLAP
		visitFolder(systemNodeRef, RepoConsts.PATH_OLAP_QUERIES);
		

		// Reports
		visitFolder(systemNodeRef, RepoConsts.PATH_REPORTS);

		// AutoNum
		visitFolder(systemNodeRef, RepoConsts.PATH_AUTO_NUM);


		visitReports(systemNodeRef);
		
		// EntityTemplates
		visitEntityTpls(systemNodeRef);

	}
	
	/**
	 * Add resources to folder
	 */
	@Override
	protected void visitFiles(NodeRef folderNodeRef, String folderName) {

		if (folderName == RepoConsts.PATH_ICON) {
			contentHelper.addFilesResources(folderNodeRef, "classpath:beCPG/images/*.png");
		}
		if (folderName == RepoConsts.PATH_OLAP_QUERIES) {
			contentHelper.addFilesResources(folderNodeRef, "classpath:beCPG/olap/*.saiku");
		}

	}
	
	


	

	/**
	 * Initialize the rules of the repository
	 */
	@Override
	protected void visitRules(NodeRef nodeRef, String folderName) {

		QName specialiseType = null;
		boolean applyToChildren = false;

		if (folderName == RepoConsts.PATH_ENTITY_TEMPLATES) {
			specialiseType = BeCPGModel.TYPE_ENTITY_V2;
		} else if (folderName == RepoConsts.PATH_REPORTS) {

			// Action : apply type
			Map<String, Serializable> params = new HashMap<String, Serializable>();
			params.put(SpecialiseTypeActionExecuter.PARAM_TYPE_NAME, ReportModel.TYPE_REPORT_TPL);
			CompositeAction compositeAction = actionService.createCompositeAction();
			Action myAction = actionService.createAction(SpecialiseTypeActionExecuter.NAME, params);
			compositeAction.addAction(myAction);

			// Conditions for the Rule : type must be different
			ActionCondition actionCondition = actionService.createActionCondition(IsSubTypeEvaluator.NAME);
			actionCondition.setParameterValue(IsSubTypeEvaluator.PARAM_TYPE, ReportModel.TYPE_REPORT_TPL);
			actionCondition.setInvertCondition(true);
			compositeAction.addActionCondition(actionCondition);

			// compare-name == *.rptdesign
			ActionCondition conditionOnName = actionService.createActionCondition(ComparePropertyValueEvaluator.NAME);
			conditionOnName.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION,
					ComparePropertyValueOperation.ENDS.toString());
			conditionOnName.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE,
					ReportTplService.PARAM_VALUE_DESIGN_EXTENSION);
			conditionOnName.setParameterValue(ComparePropertyValueEvaluator.PARAM_PROPERTY, ContentModel.PROP_NAME);
			conditionOnName.setInvertCondition(false);
			compositeAction.addActionCondition(conditionOnName);

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
			
			
		
		}  else if (folderName == RepoConsts.PATH_SECURITY) {
			specialiseType = SecurityModel.TYPE_ACL_GROUP;
		} 
		
		// specialise type
		if (specialiseType != null) {

			createRuleSpecialiseType(nodeRef, applyToChildren, specialiseType);
		}
	}
	
	@Override
	protected void vivitFolderAspects(NodeRef folderNodeRef, String folderName) {
		switch (folderName) {
		case RepoConsts.PATH_ENTITY_TEMPLATES:
		case RepoConsts.PATH_REPORTS:
		case RepoConsts.PATH_SECURITY:
		case RepoConsts.PATH_ICON:	
			if(!nodeService.hasAspect(folderNodeRef, BeCPGModel.ASPECT_SYSTEM_FOLDER)){
				nodeService.addAspect(folderNodeRef, BeCPGModel.ASPECT_SYSTEM_FOLDER, null);
			}
			break;
		default:
			break;
		}
		
		
	}
	
	private void visitReports(NodeRef systemNodeRef) {

		// reports folder
		NodeRef reportsNodeRef = visitFolder(systemNodeRef, RepoConsts.PATH_REPORTS);
		
		// compare report
		try {
			NodeRef compareProductFolderNodeRef = visitFolder(reportsNodeRef, RepoConsts.PATH_REPORTS_COMPARE_ENTITIES);
			reportTplService.createTplRptDesign(compareProductFolderNodeRef,
					TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS_COMPARE_ENTITIES),
					COMPARE_ENTITIES_REPORT_PATH, ReportType.System, ReportFormat.PDF, null, true, true, false);
		} catch (IOException e) {
			logger.error("Failed to create compare entity report tpl.", e);
		}
		
	}
	
	
	private void visitEntityTpls(NodeRef systemNodeRef) {

		NodeRef entityTplsNodeRef = visitFolder(systemNodeRef, RepoConsts.PATH_ENTITY_TEMPLATES);
		
		// visit acls
		Set<QName> dataLists = new LinkedHashSet<QName>();
		dataLists.add(SecurityModel.TYPE_ACL_ENTRY);
		entityTplService.createEntityTpl(entityTplsNodeRef, SecurityModel.TYPE_ACL_GROUP, true, dataLists, null);
	}
	
}
