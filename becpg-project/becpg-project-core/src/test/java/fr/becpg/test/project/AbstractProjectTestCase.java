/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG. 
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
package fr.becpg.test.project;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.ProjectRepoConsts;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.repo.project.data.PlanningMode;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.ResourceCost;
import fr.becpg.repo.project.data.projectList.ScoreListDataItem;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.test.BeCPGTestHelper;
import fr.becpg.test.RepoBaseTestCase;
import fr.becpg.test.data.EntityTestData;

public abstract class AbstractProjectTestCase extends RepoBaseTestCase {

	protected static final String HIERARCHY1_SEA_FOOD = "Sea food";
	protected static final String HIERARCHY2_FISH = "Fish";
	protected static final String HIERARCHY2_CRUSTACEAN = "Crustacean";
	
	
	
	
	protected static final String HIERARCHY_PROJECT_PATH = "/app:company_home/cm:System/cm:ProjectLists/bcpg:entityLists/cm:" 
	       + ProjectRepoConsts.PATH_PROJECT_HIERARCHY;
	protected static final Double RESOURCE_COST_VALUE = 100d;
	protected static final Double RESOURCE_COST_BILL_RATE = 200d;

	protected NodeRef PROJECT_HIERARCHY1_SEA_FOOD_REF;
	protected NodeRef PROJECT_HIERARCHY2_FISH_REF;
	protected NodeRef PROJECT_HIERARCHY2_CRUSTACEAN_REF;

	protected final List<NodeRef> taskLegends = new ArrayList<>();

	@Resource(name = "WorkflowService")
	protected WorkflowService workflowService;
	@Resource
	protected AssociationService associationService;
	@Resource
	protected ProjectService projectService;
	@Resource
	protected EntityTplService entityTplService;
	

	protected NodeRef userOne;
	protected NodeRef userTwo;
	protected NodeRef groupOne;

	protected List<NodeRef> assigneesOne;
	protected List<NodeRef> assigneesTwo;
	protected NodeRef projectTplNodeRef;
	protected NodeRef rawMaterialNodeRef;
	protected ResourceCost resourceCost;

	private static final Log logger = LogFactory.getLog(AbstractProjectTestCase.class);

	private void initTasks() {

		NodeRef listsFolder = entitySystemService.getSystemEntity(systemFolderNodeRef, ProjectRepoConsts.PATH_PROJECT_LISTS);

		// taskLegends
		NodeRef taskLegendsFolder = entitySystemService.getSystemEntityDataList(listsFolder, ProjectRepoConsts.PATH_TASK_LEGENDS);
		String[] taskLegendNames = { "TaskLegend1", "TaskLegend2", "TaskLegend3" };
		for (String taskLegendName : taskLegendNames) {
			if (nodeService.getChildByName(taskLegendsFolder, ContentModel.ASSOC_CONTAINS, taskLegendName) == null) {
				Map<QName, Serializable> properties = new HashMap<>();
				properties.put(ContentModel.PROP_NAME, taskLegendName);
				nodeService.createNode(taskLegendsFolder, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
						ProjectModel.TYPE_TASK_LEGEND, properties).getChildRef();
			}
		}

		// score criteria
		NodeRef criteriaFolder = entitySystemService.getSystemEntityDataList(listsFolder, ProjectRepoConsts.PATH_SCORE_CRITERIA);
		for (int i = 0; i < 5; i++) {
			if (nodeService.getChildByName(criteriaFolder, ContentModel.ASSOC_CONTAINS, "Criterion" + i) == null) {
				Map<QName, Serializable> properties = new HashMap<>();
				properties.put(BeCPGModel.PROP_LV_VALUE, "Criterion" + i);
				properties.put(ContentModel.PROP_NAME, "Criterion" + i);
				nodeService.createNode(criteriaFolder, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_LV_VALUE)),
						BeCPGModel.TYPE_LIST_VALUE, properties).getChildRef();
			}
		}

		// resourceCost
		NodeRef resourceCostsFolder = entitySystemService.getSystemEntityDataList(listsFolder, ProjectRepoConsts.PATH_RESOURCE_COSTS);
		if (nodeService.getChildByName(resourceCostsFolder, ContentModel.ASSOC_CONTAINS, "ResourceCost") == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "ResourceCost");
			properties.put(ProjectModel.PROP_RESOURCE_COST_VALUE, RESOURCE_COST_VALUE);
			properties.put(ProjectModel.PROP_RESOURCE_COST_BILL_RATE, RESOURCE_COST_BILL_RATE);
			nodeService.createNode(resourceCostsFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					ProjectModel.TYPE_RESOURCE_COST, properties).getChildRef();
		}
	}

	@Override
	protected boolean shouldInit() {
		return super.shouldInit() || hierarchyService.getHierarchyByPath(HIERARCHY_PROJECT_PATH, null, HIERARCHY1_SEA_FOOD) == null;
	}

	@Override
	protected synchronized void doInitRepo(boolean shouldInit) {
		if (shouldInit) {
			transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Boolean>() {
				public Boolean execute() throws Throwable {

					// Project
					NodeRef projectListsNodeRef = entitySystemService.getSystemEntity(systemFolderNodeRef, ProjectRepoConsts.PATH_PROJECT_LISTS);
					
					
					
					NodeRef projectHierarchyNodeRef = entitySystemService.getSystemEntityDataList(projectListsNodeRef,
							ProjectRepoConsts.PATH_PROJECT_HIERARCHY);
					
					PROJECT_HIERARCHY1_SEA_FOOD_REF = hierarchyService.createRootHierarchy(projectHierarchyNodeRef, HIERARCHY1_SEA_FOOD);
					PROJECT_HIERARCHY2_FISH_REF = hierarchyService.createHierarchy(projectHierarchyNodeRef, PROJECT_HIERARCHY1_SEA_FOOD_REF,
							HIERARCHY2_FISH);
					PROJECT_HIERARCHY2_CRUSTACEAN_REF = hierarchyService.createHierarchy(projectHierarchyNodeRef, PROJECT_HIERARCHY1_SEA_FOOD_REF,
							HIERARCHY2_CRUSTACEAN);

					initTasks();
					return false;

				}
			}, false, true);

			transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
				public NodeRef execute() throws Throwable {

					dictionaryDAO.reset();
					return null;

				}
			}, false, true);
		} else {
			transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Boolean>() {
				public Boolean execute() throws Throwable {
					PROJECT_HIERARCHY1_SEA_FOOD_REF = hierarchyService.getHierarchyByPath(HIERARCHY_PROJECT_PATH, null, HIERARCHY1_SEA_FOOD);
					PROJECT_HIERARCHY2_FISH_REF = hierarchyService.getHierarchyByPath(HIERARCHY_PROJECT_PATH, PROJECT_HIERARCHY1_SEA_FOOD_REF,
							HIERARCHY2_FISH);
					PROJECT_HIERARCHY2_CRUSTACEAN_REF = hierarchyService.getHierarchyByPath(HIERARCHY_PROJECT_PATH, PROJECT_HIERARCHY1_SEA_FOOD_REF,
							HIERARCHY2_CRUSTACEAN);
					return false;
				}
			}, false, true);

		}

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				// taskLegends
				NodeRef npdListsFolder = entitySystemService.getSystemEntity(systemFolderNodeRef, ProjectRepoConsts.PATH_PROJECT_LISTS);
				NodeRef taskLegendFolder = entitySystemService.getSystemEntityDataList(npdListsFolder, ProjectRepoConsts.PATH_TASK_LEGENDS);
				List<NodeRef> taskLegendsFileInfo =  entityListDAO.getListItems(taskLegendFolder, ProjectModel.TYPE_TASK_LEGEND );
				for (NodeRef fileInfo : taskLegendsFileInfo) {
					taskLegends.add(fileInfo);
				}

				// resourceCost
				NodeRef resourceCostFolder = entitySystemService.getSystemEntityDataList(npdListsFolder, ProjectRepoConsts.PATH_RESOURCE_COSTS);
				List<NodeRef> resourceCostsFileInfo =  entityListDAO.getListItems(resourceCostFolder,ProjectModel.TYPE_RESOURCE_COST);
				resourceCost = (ResourceCost) alfrescoRepository.findOne(resourceCostsFileInfo.get(0));

				userOne = BeCPGTestHelper.createUser(BeCPGTestHelper.USER_ONE);
				userTwo = BeCPGTestHelper.createUser(BeCPGTestHelper.USER_TWO);

				groupOne = BeCPGTestHelper.createGroup("groupOne", BeCPGTestHelper.USER_TWO);

				assigneesOne = new ArrayList<>();
				assigneesOne.add(userOne);
				assigneesTwo = new ArrayList<>();
				assigneesTwo.add(groupOne);

				return null;
			}
		}, false, true);
	}

	@Before
	public void setUp() throws Exception {
		super.setUp();
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {
				// As system user
				AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
				// create project Tpl
				ProjectData projectTplData = new ProjectData(null, "Pjt Tpl", PROJECT_HIERARCHY1_SEA_FOOD_REF, PROJECT_HIERARCHY2_CRUSTACEAN_REF,
						null, null, null, null, null, null, null, 0, null);

				projectTplData.setParentNodeRef(getTestFolderNodeRef());
				projectTplData = (ProjectData) alfrescoRepository.save(projectTplData);
				projectTplNodeRef = projectTplData.getNodeRef();

				// add aspect entityTpl
				nodeService.addAspect(projectTplNodeRef, BeCPGModel.ASPECT_ENTITY_TPL, null);
				nodeService.addAspect(projectTplNodeRef, BeCPGModel.ASPECT_ENTITY_TPL_REF, null);

				// create documents in tpl folder
				assertNotNull(projectTplData.getNodeRef());
				NodeRef subFolder = nodeService.getChildByName(projectTplNodeRef, ContentModel.ASSOC_CONTAINS, "SubFolder");
				if (subFolder == null) {
					Map<QName, Serializable> properties = new HashMap<>();
					properties.put(ContentModel.PROP_NAME, "SubFolder");
					subFolder = nodeService.createNode(projectTplNodeRef, ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "SubFolder"), ContentModel.TYPE_FOLDER, properties)
							.getChildRef();
				}

				NodeRef doc1NodeRef = nodeService.getChildByName(subFolder, ContentModel.ASSOC_CONTAINS, "Doc1");
				if (doc1NodeRef == null) {
					Map<QName, Serializable> properties = new HashMap<>();
					properties.put(ContentModel.PROP_NAME, "Doc1");
					doc1NodeRef = nodeService.createNode(subFolder, ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Doc1"), ContentModel.TYPE_CONTENT, properties).getChildRef();
				}

				NodeRef doc2NodeRef = nodeService.getChildByName(subFolder, ContentModel.ASSOC_CONTAINS, "Doc2");
				if (doc2NodeRef == null) {
					Map<QName, Serializable> properties = new HashMap<>();
					properties.put(ContentModel.PROP_NAME, "Doc2");
					doc2NodeRef = nodeService.createNode(subFolder, ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Doc2"), ContentModel.TYPE_CONTENT, properties).getChildRef();
				}

				// create datalists
				List<TaskListDataItem> taskList = new LinkedList<>();

				taskList.add(new TaskListDataItem(null, "task1", false, 2, null, assigneesOne, taskLegends.get(0), "activiti$projectAdhoc"));
				taskList.add(new TaskListDataItem(null, "task2", false, 2, null, assigneesOne, taskLegends.get(0), "activiti$projectAdhoc"));
				taskList.add(new TaskListDataItem(null, "task3", false, 2, null, assigneesOne, taskLegends.get(1), "activiti$projectAdhoc"));
				taskList.add(new TaskListDataItem(null, "task4", false, 2, null, assigneesTwo, taskLegends.get(1), "activiti$projectAdhoc"));
				taskList.add(new TaskListDataItem(null, "task5", false, 2, null, assigneesTwo, taskLegends.get(1), "activiti$projectAdhoc"));
				taskList.add(new TaskListDataItem(null, "task6", false, 2, null, assigneesTwo, taskLegends.get(2), "activiti$projectAdhoc"));
				projectTplData.setTaskList(taskList);

				// create scoreList
				List<ScoreListDataItem> scoreList = new LinkedList<>();
				for (int i = 0; i < 5; i++) {
					scoreList.add(new ScoreListDataItem(null, "Criterion" + i, i * 10, null));
				}
				projectTplData.setScoreList(scoreList);

				projectTplData = (ProjectData) alfrescoRepository.save(projectTplData);

				// Project:
				// Task1 -> Task2 -> Task3 -> Task5 -> Task6
				// -> Task4

				// update a second time to manage prevTask

				List<NodeRef> prevTasks = new ArrayList<>();

				prevTasks.add(projectTplData.getTaskList().get(0).getNodeRef());
				projectTplData.getTaskList().get(1).setPrevTasks(prevTasks);

				prevTasks = new ArrayList<>();
				prevTasks.add(projectTplData.getTaskList().get(1).getNodeRef());
				projectTplData.getTaskList().get(2).setPrevTasks(prevTasks);

				prevTasks = new ArrayList<>();
				prevTasks.add(projectTplData.getTaskList().get(2).getNodeRef());
				projectTplData.getTaskList().get(3).setPrevTasks(prevTasks);

				prevTasks = new ArrayList<>();
				prevTasks.add(projectTplData.getTaskList().get(2).getNodeRef());
				projectTplData.getTaskList().get(4).setPrevTasks(prevTasks);

				prevTasks = new ArrayList<>();
				prevTasks.add(projectTplData.getTaskList().get(3).getNodeRef());
				prevTasks.add(projectTplData.getTaskList().get(4).getNodeRef());
				projectTplData.getTaskList().get(5).setPrevTasks(prevTasks);

				List<DeliverableListDataItem> deliverableList = new LinkedList<>();
				deliverableList.add(new DeliverableListDataItem(null, Collections.singletonList(projectTplData.getTaskList().get(0).getNodeRef()), null,
						"Deliveray descr 1", 100, doc1NodeRef));
				deliverableList.add(new DeliverableListDataItem(null, Collections.singletonList(projectTplData.getTaskList().get(1).getNodeRef()), null,
						"Deliveray descr 2.1", 30, doc2NodeRef));
				deliverableList.add(new DeliverableListDataItem(null, Collections.singletonList(projectTplData.getTaskList().get(1).getNodeRef()), null,
						"Deliveray descr 2.2", 70, doc1NodeRef));
				deliverableList.add(new DeliverableListDataItem(null, Collections.singletonList(projectTplData.getTaskList().get(2).getNodeRef()), null,
						"Deliveray descr 3", 100, null));
				projectTplData.setDeliverableList(deliverableList);

				alfrescoRepository.save(projectTplData);
				return null;

			}
		}, false, true);
	}

	protected NodeRef createProject(final ProjectState projectState, final Date startDate, final Date endDate) {
		return createProject(projectState, startDate, endDate, endDate != null ? PlanningMode.RetroPlanning : PlanningMode.Planning);
	}

	protected NodeRef createProject(final ProjectState projectState, final Date startDate, final Date endDate, final PlanningMode planningMode) {

		return transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				EntityTestData entityTestData = new EntityTestData();
				entityTestData.setName("Entity 1");
				entityTestData.setParentNodeRef(getTestFolderNodeRef());

				alfrescoRepository.save(entityTestData);

				List<NodeRef> productNodeRefs = new ArrayList<>(1);
				productNodeRefs.add(entityTestData.getNodeRef());
				ProjectData projectData = new ProjectData(null, "Pjt 1", PROJECT_HIERARCHY1_SEA_FOOD_REF, PROJECT_HIERARCHY2_CRUSTACEAN_REF,
						startDate, endDate, null, planningMode, 2, projectState, projectTplNodeRef, 0, productNodeRefs);

				projectData.setParentNodeRef(getTestFolderNodeRef());

				projectData = (ProjectData) alfrescoRepository.save(projectData);

				return projectData.getNodeRef();
			}
		}, false, true);
	}

	protected NodeRef createMultiLevelProject(final ProjectState projectState, final Date startDate, final Date endDate, final PlanningMode planningMode) {

		logger.info("Create multiLevel project");

		return transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				ProjectData projectData = new ProjectData(null, "Pjt", PROJECT_HIERARCHY1_SEA_FOOD_REF, PROJECT_HIERARCHY2_CRUSTACEAN_REF, startDate,
						endDate, null, planningMode, null, null, null, 0, null);
				projectData.setParentNodeRef(getTestFolderNodeRef());

				// multi level tasks
				List<TaskListDataItem> taskList = new LinkedList<>();
				taskList.add(new TaskListDataItem(null, "task1", false, 2, null, assigneesOne, taskLegends.get(0), "activiti$projectAdhoc"));
				taskList.add(new TaskListDataItem(null, "task2", false, 2, null, assigneesOne, taskLegends.get(0), "activiti$projectAdhoc"));
				taskList.get(1).setParent(taskList.get(0));
				taskList.add(new TaskListDataItem(null, "task3", false, 2, null, assigneesOne, taskLegends.get(1), "activiti$projectAdhoc"));
				taskList.get(2).setParent(taskList.get(0));
				taskList.add(new TaskListDataItem(null, "task4", false, 2, null, assigneesTwo, taskLegends.get(1), "activiti$projectAdhoc"));
				taskList.add(new TaskListDataItem(null, "task5", false, 2, null, assigneesTwo, taskLegends.get(1), "activiti$projectAdhoc"));
				taskList.get(4).setParent(taskList.get(3));
				taskList.add(new TaskListDataItem(null, "task6", false, 2, null, assigneesTwo, taskLegends.get(2), "activiti$projectAdhoc"));
				taskList.get(5).setParent(taskList.get(3));
				projectData.setTaskList(taskList);

				projectData = (ProjectData) alfrescoRepository.save(projectData);
				// add aspect entityTpl

				List<NodeRef> prevTasks;

				prevTasks = new ArrayList<>();
				prevTasks.add(projectData.getTaskList().get(1).getNodeRef());
				projectData.getTaskList().get(2).setPrevTasks(prevTasks);

				prevTasks = new ArrayList<>();
				prevTasks.add(projectData.getTaskList().get(2).getNodeRef());
				projectData.getTaskList().get(4).setPrevTasks(prevTasks);

				prevTasks = new ArrayList<>();
				prevTasks.add(projectData.getTaskList().get(4).getNodeRef());
				projectData.getTaskList().get(5).setPrevTasks(prevTasks);

				alfrescoRepository.save(projectData);

				return projectData.getNodeRef();
			}
		}, false, true);
	}
}
