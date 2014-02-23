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
package fr.becpg.test.project;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.ProjectRepoConsts;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.hierarchy.HierarchyHelper;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.ScoreListDataItem;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.test.BeCPGTestHelper;
import fr.becpg.test.RepoBaseTestCase;
import fr.becpg.test.data.EntityTestData;

public abstract class AbstractProjectTestCase extends RepoBaseTestCase {

	protected static final String HIERARCHY1_SEA_FOOD = "Sea food";
	protected static final String HIERARCHY2_FISH = "Fish";
	protected static final String HIERARCHY2_CRUSTACEAN = "Crustacean";

	protected NodeRef PROJECT_HIERARCHY1_SEA_FOOD_REF;
	protected NodeRef PROJECT_HIERARCHY2_FISH_REF;
	protected NodeRef PROJECT_HIERARCHY2_CRUSTACEAN_REF;

	protected List<NodeRef> taskLegends = new ArrayList<NodeRef>();

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
	protected List<NodeRef> assigneesOne;
	protected List<NodeRef> assigneesTwo;
	protected NodeRef projectTplNodeRef;
	protected NodeRef rawMaterialNodeRef;
	protected NodeRef projectNodeRef;

	private void initTasks() {

		NodeRef listsFolder = entitySystemService.getSystemEntity(systemFolderNodeRef, ProjectRepoConsts.PATH_PROJECT_LISTS);

		// taskLegends
		NodeRef taskLegendsFolder = entitySystemService.getSystemEntityDataList(listsFolder, ProjectRepoConsts.PATH_TASK_LEGENDS);
		String[] taskLegendNames = { "TaskLegend1", "TaskLegend2", "TaskLegend3" };
		for (String taskLegendName : taskLegendNames) {
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, taskLegendName);
			nodeService.createNode(taskLegendsFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), ProjectModel.TYPE_TASK_LEGEND, properties)
					.getChildRef();
		}

		// score criteria
		NodeRef criteriaFolder = entitySystemService.getSystemEntityDataList(listsFolder, ProjectRepoConsts.PATH_SCORE_CRITERIA);
		for (int i = 0; i < 5; i++) {
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(BeCPGModel.PROP_LV_VALUE, "Criterion" + i);
			nodeService.createNode(criteriaFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_LV_VALUE)), BeCPGModel.TYPE_LIST_VALUE, properties)
					.getChildRef();
		}

	}

	@Override
	protected void doInitRepo(boolean shouldInit) {
		
		if (shouldInit) {
			transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Boolean>() {
				public Boolean execute() throws Throwable {
					
					// Project
					NodeRef projectListsNodeRef = entitySystemService.getSystemEntity(systemFolderNodeRef, ProjectRepoConsts.PATH_PROJECT_LISTS);
					NodeRef projectHierarchyNodeRef = entitySystemService.getSystemEntityDataList(projectListsNodeRef, HierarchyHelper.getHierarchyPathName(ProjectModel.TYPE_PROJECT));
					PROJECT_HIERARCHY1_SEA_FOOD_REF = hierarchyService.createRootHierarchy(projectHierarchyNodeRef, HIERARCHY1_SEA_FOOD);
					PROJECT_HIERARCHY2_FISH_REF = hierarchyService.createHierarchy(projectHierarchyNodeRef, PROJECT_HIERARCHY1_SEA_FOOD_REF, HIERARCHY2_FISH);
					PROJECT_HIERARCHY2_CRUSTACEAN_REF = hierarchyService.createHierarchy(projectHierarchyNodeRef, PROJECT_HIERARCHY1_SEA_FOOD_REF, HIERARCHY2_CRUSTACEAN);
	
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
		}

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				// taskLegends
				NodeRef npdListsFolder = entitySystemService.getSystemEntity(systemFolderNodeRef, ProjectRepoConsts.PATH_PROJECT_LISTS);
				NodeRef taskLegendFolder = entitySystemService.getSystemEntityDataList(npdListsFolder, ProjectRepoConsts.PATH_TASK_LEGENDS);
				List<FileInfo> taskLegendsFileInfo = fileFolderService.listFiles(taskLegendFolder);
				for (FileInfo fileInfo : taskLegendsFileInfo) {
					taskLegends.add(fileInfo.getNodeRef());
				}

				userOne = BeCPGTestHelper.createUser(BeCPGTestHelper.USER_ONE);
				userTwo = BeCPGTestHelper.createUser(BeCPGTestHelper.USER_TWO);

				assigneesOne = new ArrayList<NodeRef>();
				assigneesOne.add(userOne);
				assigneesTwo = new ArrayList<NodeRef>();
				assigneesTwo.add(userTwo);

				// create project Tpl
				ProjectData projectTplData = new ProjectData(null, "Pjt Tpl", PROJECT_HIERARCHY1_SEA_FOOD_REF, PROJECT_HIERARCHY2_CRUSTACEAN_REF, null, null, null, null, null,
						null, 0, null);
				projectTplData.setParentNodeRef(testFolderNodeRef);
				projectTplData = (ProjectData) alfrescoRepository.save(projectTplData);
				projectTplNodeRef = projectTplData.getNodeRef();

				// add aspect entityTpl
				nodeService.addAspect(projectTplNodeRef, BeCPGModel.ASPECT_ENTITY_TPL, null);

				// create documents in tpl folder
				assertNotNull(projectTplData.getNodeRef());
				NodeRef subFolder = nodeService.getChildByName(projectTplNodeRef, ContentModel.ASSOC_CONTAINS, "SubFolder");
				if (subFolder == null) {
					Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
					properties.put(ContentModel.PROP_NAME, "SubFolder");
					subFolder = nodeService.createNode(projectTplNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "SubFolder"),
							ContentModel.TYPE_FOLDER, properties).getChildRef();
				}

				NodeRef doc1NodeRef = nodeService.getChildByName(subFolder, ContentModel.ASSOC_CONTAINS, "Doc1");
				if (doc1NodeRef == null) {
					Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
					properties.put(ContentModel.PROP_NAME, "Doc1");
					doc1NodeRef = nodeService.createNode(subFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Doc1"),
							ContentModel.TYPE_CONTENT, properties).getChildRef();
				}

				NodeRef doc2NodeRef = nodeService.getChildByName(subFolder, ContentModel.ASSOC_CONTAINS, "Doc2");
				if (doc2NodeRef == null) {
					Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
					properties.put(ContentModel.PROP_NAME, "Doc2");
					doc2NodeRef = nodeService.createNode(subFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Doc2"),
							ContentModel.TYPE_CONTENT, properties).getChildRef();
				}

				// create datalists
				List<TaskListDataItem> taskList = new LinkedList<TaskListDataItem>();

				taskList.add(new TaskListDataItem(null, "task1", false, 2, null, assigneesOne, taskLegends.get(0), "activiti$projectAdhoc"));
				taskList.add(new TaskListDataItem(null, "task2", false, 2, null, assigneesOne, taskLegends.get(0), "activiti$projectAdhoc"));
				taskList.add(new TaskListDataItem(null, "task3", false, 2, null, assigneesOne, taskLegends.get(1), "activiti$projectAdhoc"));
				taskList.add(new TaskListDataItem(null, "task4", false, 2, null, assigneesTwo, taskLegends.get(1), "activiti$projectAdhoc"));
				taskList.add(new TaskListDataItem(null, "task5", false, 2, null, assigneesTwo, taskLegends.get(1), "activiti$projectAdhoc"));
				taskList.add(new TaskListDataItem(null, "task6", false, 2, null, assigneesTwo, taskLegends.get(2), "activiti$projectAdhoc"));
				projectTplData.setTaskList(taskList);

				// create scoreList
				List<ScoreListDataItem> scoreList = new LinkedList<ScoreListDataItem>();
				for (int i = 0; i < 5; i++) {
					scoreList.add(new ScoreListDataItem(null, "Criterion" + i, i * 10, null));
				}
				projectTplData.setScoreList(scoreList);

				alfrescoRepository.save(projectTplData);

				// Project:
				// Task1 -> Task2 -> Task3 -> Task5 -> Task6
				// -> Task4

				// update a second time to manage prevTask
				// TODO : should avoid to save twice
				projectTplData = (ProjectData) alfrescoRepository.findOne(projectTplNodeRef);
				List<NodeRef> prevTasks = new ArrayList<NodeRef>();

				prevTasks.add(projectTplData.getTaskList().get(0).getNodeRef());
				projectTplData.getTaskList().get(1).setPrevTasks(prevTasks);

				prevTasks = new ArrayList<NodeRef>();
				prevTasks.add(projectTplData.getTaskList().get(1).getNodeRef());
				projectTplData.getTaskList().get(2).setPrevTasks(prevTasks);

				prevTasks = new ArrayList<NodeRef>();
				prevTasks.add(projectTplData.getTaskList().get(2).getNodeRef());
				projectTplData.getTaskList().get(3).setPrevTasks(prevTasks);

				prevTasks = new ArrayList<NodeRef>();
				prevTasks.add(projectTplData.getTaskList().get(2).getNodeRef());
				projectTplData.getTaskList().get(4).setPrevTasks(prevTasks);

				prevTasks = new ArrayList<NodeRef>();
				prevTasks.add(projectTplData.getTaskList().get(3).getNodeRef());
				prevTasks.add(projectTplData.getTaskList().get(4).getNodeRef());
				projectTplData.getTaskList().get(5).setPrevTasks(prevTasks);

				List<DeliverableListDataItem> deliverableList = new LinkedList<DeliverableListDataItem>();
				deliverableList.add(new DeliverableListDataItem(null, projectTplData.getTaskList().get(0).getNodeRef(), null, "Deliveray descr 1", 100, doc1NodeRef));
				deliverableList.add(new DeliverableListDataItem(null, projectTplData.getTaskList().get(1).getNodeRef(), null, "Deliveray descr 2.1", 30, doc2NodeRef));
				deliverableList.add(new DeliverableListDataItem(null, projectTplData.getTaskList().get(1).getNodeRef(), null, "Deliveray descr 2.2", 70, doc1NodeRef));
				deliverableList.add(new DeliverableListDataItem(null, projectTplData.getTaskList().get(2).getNodeRef(), null, "Deliveray descr 3", 100, null));
				projectTplData.setDeliverableList(deliverableList);

				alfrescoRepository.save(projectTplData);

				return null;
			}
		}, false, true);
	}

	protected void createProject(final ProjectState projectState, final Date startDate, final Date endDate) {

		projectNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				EntityTestData entityTestData = new EntityTestData();
				entityTestData.setName("Entity 1");
				entityTestData.setParentNodeRef(testFolderNodeRef);

				alfrescoRepository.save(entityTestData);

				List<NodeRef> productNodeRefs = new ArrayList<>(1);
				productNodeRefs.add(entityTestData.getNodeRef());
				ProjectData projectData = new ProjectData(null, "Pjt 1", PROJECT_HIERARCHY1_SEA_FOOD_REF, PROJECT_HIERARCHY2_CRUSTACEAN_REF, startDate, endDate, null, 2,
						projectState, projectTplNodeRef, 0, productNodeRefs);

				projectData.setParentNodeRef(testFolderNodeRef);

				projectData = (ProjectData) alfrescoRepository.save(projectData);
				return projectData.getNodeRef();
			}
		}, false, true);
	}
}
