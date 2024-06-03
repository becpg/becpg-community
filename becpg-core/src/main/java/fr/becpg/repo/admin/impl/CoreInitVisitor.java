/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG.
 *  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.admin.impl;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.evaluator.HasAspectEvaluator;
import org.alfresco.repo.action.evaluator.IsSubTypeEvaluator;
import org.alfresco.repo.action.evaluator.compare.ComparePropertyValueOperation;
import org.alfresco.repo.action.executer.AddFeaturesActionExecuter;
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
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;
import fr.becpg.model.SecurityModel;
import fr.becpg.model.SystemGroup;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.helper.ContentHelper;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.mail.BeCPGMailService;
import fr.becpg.repo.report.template.ReportTplInformation;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;
import fr.becpg.report.client.ReportFormat;

/**
 * <p>
 * CoreInitVisitor class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class CoreInitVisitor extends AbstractInitVisitorImpl {

	private static final String COMPARE_ENTITIES_REPORT_PATH = "beCPG/birt/system/CompareEntities.rptdesign";

	@Autowired
	private DictionaryDAO dictionaryDAO;

	@Autowired
	@Qualifier("qnameDAO")
	private QNameDAO qNameDAO;

	@Autowired
	private ContentHelper contentHelper;

	@Autowired
	private EntityTplService entityTplService;

	@Autowired
	private ReportTplService reportTplService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private BeCPGMailService beCPGMailService;

	@Autowired
	private EntityVersionService entityVersionService;

	/** {@inheritDoc} */
	@Override
	public List<SiteInfo> visitContainer(NodeRef companyHome) {
		logger.info("Run CoreInitVisitor");

		// Init QNames for dbQueries
		for (QName model : dictionaryDAO.getModels(true)) {
			for (PropertyDefinition propertyDef : dictionaryDAO.getProperties(model)) {
				qNameDAO.getOrCreateQName(propertyDef.getName());
			}
		}

		visitGroups();

		// System
		NodeRef systemNodeRef = visitFolder(companyHome, RepoConsts.PATH_SYSTEM);

		// Security
		visitFolder(systemNodeRef, RepoConsts.PATH_SECURITY);

		// Icons
		visitFolder(systemNodeRef, RepoConsts.PATH_ICON);

		// Reports
		visitFolder(systemNodeRef, RepoConsts.PATH_REPORTS);

		// AutoNum
		visitFolder(systemNodeRef, RepoConsts.PATH_AUTO_NUM);

		visitReports(systemNodeRef);

		// EntityTemplates
		visitEntityTpls(systemNodeRef);

		// MailTemplates
		contentHelper.addFilesResources(beCPGMailService.getEmailNotifyTemplatesFolder(), "classpath*:beCPG/mails/notify/*.ftl");
		contentHelper.addFilesResources(beCPGMailService.getEmailActivitiesTemplatesFolder(),
				"classpath*:alfresco/templates/activities-email-templates/*.ftl");
		contentHelper.addFilesResources(beCPGMailService.getEmailInviteTemplatesFolder(),
				"classpath*:alfresco/templates/invite-email-templates/*.ftl");
		contentHelper.addFilesResources(beCPGMailService.getEmailInviteTemplatesFolder(), "classpath*:alfresco/templates/new-user-templates/*.ftl");

		// license
		visitFolder(systemNodeRef, RepoConsts.PATH_LICENSE);

		// version
		visitVersionFolder(entityVersionService.getEntitiesHistoryFolder());
		
		// exchange/export/notifications
		 visitFolder(visitFolder(visitFolder(systemNodeRef, RepoConsts.PATH_EXCHANGE), RepoConsts.PATH_EXPORT), RepoConsts.PATH_NOTIFICATIONS);

		return new ArrayList<>();
	}

	private void visitVersionFolder(NodeRef entitiesHistoryFolder) {

		if (!ruleService.hasRules(entitiesHistoryFolder)) {
			CompositeAction compositeAction = actionService.createCompositeAction();
			Map<String, Serializable> params = new HashMap<>();
			params.put(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_INDEX_CONTROL);
			params.put(ContentModel.PROP_IS_INDEXED.toString(), Boolean.FALSE);
			Action action = actionService.createAction(AddFeaturesActionExecuter.NAME, params);
			compositeAction.addAction(action);

			// Conditions for the Rule : type must be equals
			ActionCondition typeCondition = actionService.createActionCondition(IsSubTypeEvaluator.NAME);
			typeCondition.setParameterValue(IsSubTypeEvaluator.PARAM_TYPE, ContentModel.TYPE_CMOBJECT);
			typeCondition.setInvertCondition(false);
			compositeAction.addActionCondition(typeCondition);

			ActionCondition aspectCondition = actionService.createActionCondition(HasAspectEvaluator.NAME);
			aspectCondition.setParameterValue(HasAspectEvaluator.PARAM_ASPECT, ContentModel.ASPECT_INDEX_CONTROL);
			aspectCondition.setInvertCondition(true);
			compositeAction.addActionCondition(aspectCondition);

			// rule
			Rule rule = new Rule();
			rule.setTitle("Add no Index aspect");
			rule.setDescription("Add no Index aspect to the created node");
			rule.applyToChildren(true);
			rule.setExecuteAsynchronously(true);
			rule.setRuleDisabled(false);
			rule.setRuleType(RuleType.INBOUND);
			rule.setAction(compositeAction);
			ruleService.saveRule(entitiesHistoryFolder, rule);
		}

	}

	private void visitGroups() {

		createGroups(new String[] { SystemGroup.SystemMgr.toString(), SystemGroup.OlapUser.toString(), SystemGroup.AiUser.toString(),
				SystemGroup.ExternalUserMgr.toString(), SystemGroup.ExternalUser.toString(), SystemGroup.SecurityRole.toString(),
				SystemGroup.LanguageMgr.toString() });

		createGroups(new String[] { SystemGroup.LicenseReadConcurrent.toString(), SystemGroup.LicenseWriteConcurrent.toString(),
				SystemGroup.LicenseReadNamed.toString(), SystemGroup.LicenseWriteNamed.toString(),
				SystemGroup.LicenseSupplierConcurrent.toString() });

		Set<String> authorities = authorityService.getContainedAuthorities(AuthorityType.GROUP,
				PermissionService.GROUP_PREFIX + SystemGroup.LicenseSupplierConcurrent.toString(), true);
		if (!authorities.contains(PermissionService.GROUP_PREFIX + SystemGroup.ExternalUser.toString())) {
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + SystemGroup.LicenseSupplierConcurrent.toString(),
					PermissionService.GROUP_PREFIX + SystemGroup.ExternalUser.toString());
		}

	}

	/**
	 * {@inheritDoc}
	 *
	 * Add resources to folder
	 */
	@Override
	protected void visitFiles(NodeRef folderNodeRef, String folderName, boolean folderExists) {

		if (RepoConsts.PATH_ICON.equals(folderName)) {
			contentHelper.addFilesResources(folderNodeRef, "classpath*:beCPG/images/*.png");
		}
		if (RepoConsts.PATH_LICENSE.equals(folderName)) {
			contentHelper.addFilesResources(folderNodeRef, "classpath*:beCPG/license/*.sample");
		}

	}

	/**
	 * {@inheritDoc}
	 *
	 * Initialize the rules of the repository
	 */
	@Override
	protected void visitRules(NodeRef nodeRef, String folderName) {

		QName specialiseType = null;
		boolean applyToChildren = false;

		if (RepoConsts.PATH_REPORTS.equals(folderName)) {

			// Action : apply type
			Map<String, Serializable> params = new HashMap<>();
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
			conditionOnName.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.ENDS.toString());
			conditionOnName.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, ReportTplService.PARAM_VALUE_DESIGN_EXTENSION);
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

		} else if (RepoConsts.PATH_SECURITY.equals(folderName)) {
			specialiseType = SecurityModel.TYPE_ACL_GROUP;
		}

		// specialise type
		if (specialiseType != null) {

			createRuleSpecialiseType(nodeRef, applyToChildren, specialiseType);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void vivitFolderAspects(NodeRef folderNodeRef, String folderName) {
		switch (folderName) {
		case RepoConsts.PATH_ENTITY_TEMPLATES:
		case RepoConsts.PATH_REPORTS:
		case RepoConsts.PATH_SECURITY:
		case RepoConsts.PATH_ICON:
			addSystemFolderAspect(folderNodeRef);
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
			List<NodeRef> resources = contentHelper.addFilesResources(compareProductFolderNodeRef, "classpath*:beCPG/birt/system/*.properties");

			ReportTplInformation reportTplInformation = new ReportTplInformation();
			reportTplInformation.setReportType(ReportType.Compare);
			reportTplInformation.setReportFormat(ReportFormat.PDF);
			reportTplInformation.setNodeType(null);
			reportTplInformation.setDefaultTpl(false);
			reportTplInformation.setSystemTpl(false);
			reportTplInformation.setResources(resources);

			reportTplService.createTplRptDesign(compareProductFolderNodeRef,
					TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS_COMPARE_ENTITIES), COMPARE_ENTITIES_REPORT_PATH, reportTplInformation,
					false);

		} catch (IOException e) {
			logger.error("Failed to create compare entity report tpl.", e);
		}

	}

	private void visitEntityTpls(NodeRef systemNodeRef) {

		NodeRef entityTplsNodeRef = visitFolder(systemNodeRef, RepoConsts.PATH_ENTITY_TEMPLATES);

		// visit acls
		Set<QName> dataLists = new LinkedHashSet<>();
		dataLists.add(SecurityModel.TYPE_ACL_ENTRY);
		NodeRef entityTplNodeRef = entityTplService.createEntityTpl(entityTplsNodeRef, SecurityModel.TYPE_ACL_GROUP, null, true, true, dataLists,
				null);
		entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_PROPERTIES);
	}

	/** {@inheritDoc} */
	@Override
	protected void visitPermissions(NodeRef nodeRef, String folderName) {
		if (Objects.equals(folderName, RepoConsts.PATH_SYSTEM)) {
			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + SystemGroup.SystemMgr.toString(), PermissionService.COORDINATOR,
					true);
		}
	}

	/** {@inheritDoc} */
	@Override
	public Integer initOrder() {
		return 0;
	}

}
