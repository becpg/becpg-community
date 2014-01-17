package fr.becpg.repo.admin.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.evaluator.IsSubTypeEvaluator;
import org.alfresco.repo.action.evaluator.compare.ComparePropertyValueOperation;
import org.alfresco.repo.action.executer.SpecialiseTypeActionExecuter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.CompositeAction;
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

@Service
public class CoreInitVisitor extends AbstractInitVisitorImpl {
	
	@Autowired
	private ContentHelper contentHelper;
	
	@Autowired
	private EntityTplService entityTplService;
	

	@Override
	public void visitContainer(NodeRef companyHome) {
		logger.info("Run CoreInitVisitor");

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
	 * Create the entity templates
	 * 
	 * @param productTplsNodeRef
	 */
	private void visitEntityTpls(NodeRef systemNodeRef) {

		NodeRef entityTplsNodeRef = visitFolder(systemNodeRef, RepoConsts.PATH_ENTITY_TEMPLATES);


		Set<String> subFolders = new HashSet<String>();		
		subFolders.add(RepoConsts.PATH_DOCUMENTS);
		subFolders.add(RepoConsts.PATH_IMAGES);

		// TODO remove only for test
		Set<QName> dataLists = new LinkedHashSet<QName>();
		entityTplService.createEntityTpl(entityTplsNodeRef, BeCPGModel.TYPE_RAWMATERIAL, true, dataLists, subFolders);

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

	@Override
	public boolean shouldInit(NodeRef companyHomeNodeRef) {
		return 	nodeService.getChildByName(companyHomeNodeRef, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM)) == null;
	}
	
}
