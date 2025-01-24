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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.project.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectGroup;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.ProjectRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.admin.impl.AbstractInitVisitorImpl;
import fr.becpg.repo.entity.EntitySystemService;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.helper.ContentHelper;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.DeliverableScriptOrder;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.report.template.ReportTplInformation;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.report.client.ReportFormat;

/**
 * <p>ProjectInitVisitor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class ProjectInitVisitor extends AbstractInitVisitorImpl {

	private static final String PATH_REPORTS_EXPORT_SEARCH_PROJECTS = "ExportProjects";

	private static final String EXPORT_PROJECTS_REPORT_RPTFILE_PATH = "beCPG/birt/project/ProjectsReport.rptdesign";

	private static final String EXPORT_PROJECTS_REPORT_XMLFILE_PATH = "beCPG/birt/project/ExportSearchQuery.xml";

	/** Constant <code>EMAIL_TEMPLATES="./app:dictionary/app:email_templates"</code> */
	public static final String EMAIL_TEMPLATES = "./app:dictionary/app:email_templates";

	private static final String PROJECT_REPORT_CSS_RESOURCE = "beCPG/birt/project/project-report.css";

	private static final String PROJECT_REPORT_EN_RESOURCE = "beCPG/birt/project/ProjectReport_en.properties";

	private static final String PROJECT_REPORT_FR_RESOURCE = "beCPG/birt/project/ProjectReport_fr.properties";
	
	private static final String ARCHIVE_PJT_TPL_NAME = "plm.project.archive.tpl.name";
	
	private static final String ARCHIVE_PJT_TASK_NAME = "plm.project.archive.task.name";
	
	private static final String ARCHIVE_PJT_DELIVERABLE_NAME = "plm.project.archive.deliverable.name";

	private static final String XPATH_DICTIONARY_SCRIPTS = "./app:dictionary/app:scripts";

	@Autowired
	private EntitySystemService entitySystemService;

	@Autowired
	private EntityTplService entityTplService;

	@Autowired
	private ReportTplService reportTplService;

	@Autowired
	private ContentHelper contentHelper;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private Repository repository;
	
	@Autowired
	private AlfrescoRepository<ProjectData> alfrescoRepository;
	

	/** {@inheritDoc} */
	@Override
	public List<SiteInfo> visitContainer(NodeRef companyHome) {

		logger.info("Run ProjectInitVisitor ...");

		NodeRef systemNodeRef = visitFolder(companyHome, RepoConsts.PATH_SYSTEM);

		// Lists of characteristics for Project
		visitSystemProjectListValuesEntity(systemNodeRef, ProjectRepoConsts.PATH_PROJECT_LISTS);

		// EntityTemplates
		visitEntityTpls(systemNodeRef);

		visitReports(systemNodeRef);

		createSystemGroups(new String[] { ProjectGroup.ProjectRoles.toString(), createRoleGroup(ContentModel.PROP_CREATOR),
				createRoleGroup(ProjectModel.ASSOC_PROJECT_MANAGER) });

		// MailTemplates
		NodeRef emailsProject = visitFolder(BeCPGQueryBuilder.createQuery().selectNodeByPath(companyHome, EMAIL_TEMPLATES),
				ProjectRepoConsts.PATH_EMAILS_PROJECT);
		contentHelper.addFilesResources(emailsProject, "classpath*:beCPG/mails/project/*.ftl");

		return new ArrayList<>();
	}

	/**
	 * Create the entity templates
	 *
	 * @param productTplsNodeRef
	 */
	private void visitEntityTpls(NodeRef systemNodeRef) {

		NodeRef entityTplsNodeRef = visitFolder(systemNodeRef, RepoConsts.PATH_ENTITY_TEMPLATES);

		createDefaultProjectTpl(entityTplsNodeRef);

		createArchiveProjectTpl(entityTplsNodeRef);
		
		
	}

	private void createArchiveProjectTpl(NodeRef entityTplsNodeRef) {
		
		NodeRef entityTplNodeRef = nodeService.getChildByName(entityTplsNodeRef, ContentModel.ASSOC_CONTAINS,
				I18NUtil.getMessage(ARCHIVE_PJT_TPL_NAME));

		if (entityTplNodeRef == null) {
			NodeRef scriptFolderNodeRef = BeCPGQueryBuilder.createQuery().selectNodeByPath(repository.getCompanyHome(), XPATH_DICTIONARY_SCRIPTS);

			List<NodeRef> scriptResources = contentHelper.addFilesResources(scriptFolderNodeRef, "classpath*:beCPG/script/project/*.js");
			
			Set<QName> dataLists = new LinkedHashSet<>();
			dataLists.add(ProjectModel.TYPE_TASK_LIST);
			dataLists.add(ProjectModel.TYPE_DELIVERABLE_LIST);
			dataLists.add(BeCPGModel.TYPE_ACTIVITY_LIST);
			entityTplNodeRef = entityTplService.createEntityTpl(entityTplsNodeRef, ProjectModel.TYPE_PROJECT,
					I18NUtil.getMessage(ARCHIVE_PJT_TPL_NAME), true, false, dataLists, null);

			entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_PROPERTIES);
			entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_DOCUMENTS);

			ProjectData pjtTpl = alfrescoRepository.findOne(entityTplNodeRef);

			TaskListDataItem task = new TaskListDataItem();
			task.setTaskName(I18NUtil.getMessage(ARCHIVE_PJT_TASK_NAME));
			pjtTpl.getTaskList().add(task);

			alfrescoRepository.save(pjtTpl);

			DeliverableListDataItem archiveScript = new DeliverableListDataItem();
			archiveScript.setDescription(I18NUtil.getMessage(ARCHIVE_PJT_DELIVERABLE_NAME));
			archiveScript.setTasks(Collections.singletonList(task.getNodeRef()));
			archiveScript.setScriptOrder(DeliverableScriptOrder.Pre);

			for (NodeRef scriptNodeRef : scriptResources) {
				archiveScript.setContent(scriptNodeRef);
			}

			pjtTpl.getDeliverableList().add(archiveScript);

			alfrescoRepository.save(pjtTpl);
		}
		
	}

	private void createDefaultProjectTpl(NodeRef entityTplsNodeRef) {
		// visit supplier
		Set<QName> dataLists = new LinkedHashSet<>();
		dataLists.add(ProjectModel.TYPE_TASK_LIST);
		dataLists.add(ProjectModel.TYPE_DELIVERABLE_LIST);
		dataLists.add(ProjectModel.TYPE_SCORE_LIST);
		dataLists.add(BeCPGModel.TYPE_ACTIVITY_LIST);
		dataLists.add(ProjectModel.TYPE_LOG_TIME_LIST);
		dataLists.add(ProjectModel.TYPE_BUDGET_LIST);
		dataLists.add(ProjectModel.TYPE_INVOICE_LIST);
		dataLists.add(ProjectModel.TYPE_EXPENSE_LIST);

		NodeRef entityTplNodeRef = entityTplService.createEntityTpl(entityTplsNodeRef, ProjectModel.TYPE_PROJECT, null, true, true, dataLists, null);

		entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_PROPERTIES);
		entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_DOCUMENTS);
	}

	/**
	 * Create NPD List values
	 *
	 * @param parentNodeRef
	 * @param path
	 * @return
	 */
	private NodeRef visitSystemProjectListValuesEntity(NodeRef parentNodeRef, String path) {

		Map<String, QName> entityLists = new LinkedHashMap<>();

		entityLists.put(ProjectRepoConsts.PATH_TASK_LEGENDS, ProjectModel.TYPE_TASK_LEGEND);
		entityLists.put(ProjectRepoConsts.PATH_PROJECT_HIERARCHY, BeCPGModel.TYPE_LINKED_VALUE);
		entityLists.put(ProjectRepoConsts.PATH_REQUEST_STATES, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(ProjectRepoConsts.PATH_REQUEST_ORIGINS, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(ProjectRepoConsts.PATH_SCORE_CRITERION_TYPES, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(ProjectRepoConsts.PATH_SCORE_CRITERIA, ProjectModel.TYPE_SCORE_CRITERION);
		entityLists.put(ProjectRepoConsts.PATH_SPONSORS, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(ProjectRepoConsts.PATH_TIME_TYPES, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(ProjectRepoConsts.PATH_RESOURCE_COSTS, ProjectModel.TYPE_RESOURCE_COST);

		return entitySystemService.createSystemEntity(parentNodeRef, path, entityLists);
	}

	private void visitReports(NodeRef systemNodeRef) {

		// reports folder
		NodeRef reportsNodeRef = visitFolder(systemNodeRef, RepoConsts.PATH_REPORTS);

		/*
		 * Export Search reports
		 */
		NodeRef exportSearchNodeRef = visitFolder(reportsNodeRef, RepoConsts.PATH_REPORTS_EXPORT_SEARCH);

		try {

			NodeRef exportSearchProjectNodeRef = visitFolder(exportSearchNodeRef, PATH_REPORTS_EXPORT_SEARCH_PROJECTS);

			String[] projectReportResources = { PROJECT_REPORT_CSS_RESOURCE, PROJECT_REPORT_FR_RESOURCE, PROJECT_REPORT_EN_RESOURCE };
			List<NodeRef> resources = new ArrayList<>();

			for (String element : projectReportResources) {
				resources.add(reportTplService.createTplRessource(exportSearchProjectNodeRef, element, false));
			}

			ReportTplInformation reportTplInformation = new ReportTplInformation();
			reportTplInformation.setReportType(ReportType.ExportSearch);
			reportTplInformation.setReportFormat(ReportFormat.PDF);
			reportTplInformation.setNodeType(ProjectModel.TYPE_PROJECT);
			reportTplInformation.setDefaultTpl(true);
			reportTplInformation.setSystemTpl(false);
			reportTplInformation.setResources(resources);
			reportTplInformation.setSupportedLocale(Arrays.asList("fr", "en"));

			reportTplService.createTplRptDesign(exportSearchProjectNodeRef, TranslateHelper.getTranslatedPath(PATH_REPORTS_EXPORT_SEARCH_PROJECTS),
					EXPORT_PROJECTS_REPORT_RPTFILE_PATH, reportTplInformation, false);

			reportTplService.createTplRessource(exportSearchProjectNodeRef, EXPORT_PROJECTS_REPORT_XMLFILE_PATH, false);

		} catch (IOException e) {
			logger.error("Failed to create export search report tpl.", e);
		}

	}

	private void createSystemGroups(String[] groups) {

		createGroups(groups);

		// Group hierarchy
		Set<String> authorities = authorityService.getContainedAuthorities(AuthorityType.GROUP,
				PermissionService.GROUP_PREFIX + ProjectGroup.ProjectRoles.toString(), true);
		if (!authorities.contains(PermissionService.GROUP_PREFIX + createRoleGroup(ContentModel.PROP_CREATOR))) {
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + ProjectGroup.ProjectRoles.toString(),
					PermissionService.GROUP_PREFIX + createRoleGroup(ContentModel.PROP_CREATOR));
		}
		if (!authorities.contains(PermissionService.GROUP_PREFIX + createRoleGroup(ProjectModel.ASSOC_PROJECT_MANAGER))) {
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + ProjectGroup.ProjectRoles.toString(),
					PermissionService.GROUP_PREFIX + createRoleGroup(ProjectModel.ASSOC_PROJECT_MANAGER));
		}
	}

	/**
	 * <p>createRoleGroup.</p>
	 *
	 * @param qName a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String createRoleGroup(QName qName) {
		return ProjectRepoConsts.PROJECT_GROUP_PREFIX + qName.toPrefixString(namespaceService).replace(":", "_");
	}

	/** {@inheritDoc} */
	@Override
	public Integer initOrder() {
		return 2;
	}

}
