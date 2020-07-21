/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG.
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
package fr.becpg.test.repo.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.QualityModel;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.quality.NonConformityService;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.test.BeCPGPLMTestHelper;

public class NCWorkflowIT extends AbstractWorkflowTest {

	private static final String NC_URI = "http://www.bcpg.fr/model/nc-workflow/1.0";
	private static final QName PROP_NEED_PREV_ACTION = QName.createQName(NC_URI, "needPrevAction");
	private static final QName PROP_STATE = QName.createQName(NC_URI, "ncState");
	private static final QName PROP_ASSIGNEE = QName.createQName(NC_URI, "assignee");
	private static final QName ASSOC_CORR_ACTION_ACTOR = QName.createQName(NC_URI, "corrActionActor");
	private static final QName ASSOC_CHECK_ACTOR = QName.createQName(NC_URI, "checkActor");

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	
	private static final Log logger = LogFactory.getLog(NCWorkflowIT.class);

	private NodeRef rawMaterial1NodeRef;

	private String workflowInstanceId = null;

	@Autowired
	private NonConformityService nonConformityService;

	@Test
	public void testWorkFlow() {

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			BeCPGPLMTestHelper.createUsers();

			RawMaterialData rawMaterial1 = new RawMaterialData();
			rawMaterial1.setName("Raw material 1");

			rawMaterial1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial1).getNodeRef();

			// clean default storage folder
			NodeRef folderNodeRef = nonConformityService.getStorageFolder(null);
			for (FileInfo fileInfo : fileFolderService.list(folderNodeRef)) {
				nodeService.deleteNode(fileInfo.getNodeRef());
			}
			return null;

		}, false, true);

		executeNonConformityWF(false);

		executeNonConformityWF(true);

		executeNonConformityAdhoc();
	}

	private void executeNonConformityWF(final boolean needPrevAction) {

		final WorkflowTask task1 = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			authenticationComponent.setCurrentUser(BeCPGPLMTestHelper.USER_ONE);

			WorkflowDefinition wfDef = workflowService.getDefinitionByName("activiti$nonConformityProcess");
			logger.debug("wfDefId found : " + wfDef.getId());

			// Fill a map of default properties to start the workflow with
			Map<QName, Serializable> properties = new HashMap<>();
			Date dueDate = Calendar.getInstance().getTime();
			properties.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, dueDate);
			properties.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, 2);
			Serializable workflowPackage = workflowService.createPackage(null);
			properties.put(WorkflowModel.ASSOC_PACKAGE, workflowPackage);
			properties.put(QualityModel.ASSOC_PRODUCT, rawMaterial1NodeRef);

			WorkflowPath path = workflowService.startWorkflow(wfDef.getId(), properties);
			assertNotNull("The workflow path is null!", path);

			workflowInstanceId = path.getInstance().getId();
			assertNotNull("The workflow instance is null!", workflowInstanceId);

			WorkflowTask startTask = workflowService.getStartTask(workflowInstanceId);
			workflowService.endTask(startTask.getId(), null);

			List<WorkflowPath> paths = workflowService.getWorkflowPaths(workflowInstanceId);
			assertEquals(1, paths.size());
			path = paths.get(0);

			List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path.getId());
			assertEquals(1, tasks.size());
			return tasks.get(0);
		}, false, true);

		assertEquals("ncwf:analysisTask", task1.getName());

		NodeRef ncNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			NodeRef pkgNodeRef = workflowService.getWorkflowById(workflowInstanceId).getWorkflowPackage();
			List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(pkgNodeRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS,
					RegexQNamePattern.MATCH_ALL);
			for (ChildAssociationRef childAssoc : childAssocs) {
				if (QualityModel.TYPE_NC.equals(nodeService.getType(childAssoc.getChildRef()))) {
					return childAssoc.getChildRef();
				}
			}

			return null;
		}, false, true);

		/*
		 * Update analysisTask task
		 */
		assertNotNull(ncNodeRef);

		final WorkflowTask task2 = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			logger.info("Set analysisTask information " + task1.getName());
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(WorkflowModel.PROP_COMMENT, "commentaire émetteur");
			properties.put(PROP_NEED_PREV_ACTION, needPrevAction);
			properties.put(PROP_STATE, "new");

			java.util.Map<QName, List<NodeRef>> assocs = new HashMap<>();
			List<NodeRef> assignees = new ArrayList<>();
			assignees.add(personService.getPerson(BeCPGPLMTestHelper.USER_ONE));
			assocs.put(ASSOC_CORR_ACTION_ACTOR, assignees);
			assignees = new ArrayList<>();
			assignees.add(personService.getPerson(BeCPGPLMTestHelper.USER_TWO));
			assocs.put(ASSOC_CHECK_ACTOR, assignees);

			workflowService.updateTask(task1.getId(), properties, assocs, new HashMap<QName, List<NodeRef>>());
			return workflowService.endTask(task1.getId(), null);
		}, false, true);

		if (needPrevAction) {
			assertEquals("prevActionTask", task2.getPath().getNode().getName());
		} else {
			assertEquals("corrActionTask", task2.getPath().getNode().getName());
		}

		/*
		 * do corrActionTask
		 */
		WorkflowTask task = submitTask(workflowInstanceId, "ncwf:corrActionTask", null, "analysis", "commentaire émetteur 2");
		assertEquals("checkTask", task.getPath().getNode().getName());

		/*
		 * do checkTask
		 *
		 *
		 */
		task = submitTask(workflowInstanceId, "ncwf:checkTask", null, "closing", "commentaire émetteur 3");
		assertEquals("notificationTask", task.getPath().getNode().getName());

		/*
		 * do notificationTask
		 */
		task = submitTask(workflowInstanceId, "ncwf:notificationTask", null, null, "commentaire émetteur");

		/*
		 * do prevActionTask
		 */
		if (needPrevAction) {
			transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
				assertTrue(workflowService.getWorkflowById(workflowInstanceId).isActive());
				return null;
			}, false, true);
			task = submitTask(workflowInstanceId, "ncwf:prevActionTask", null, null, "commentaire émetteur");
		}
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			assertFalse(workflowService.getWorkflowById(workflowInstanceId).isActive());
			return null;
		}, false, true);
	}

	private void executeNonConformityAdhoc() {

		final WorkflowTask task1 = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			WorkflowDefinition wfDef = workflowService.getDefinitionByName("activiti$nonConformityAdhoc");
			logger.debug("wfDefId found : " + wfDef.getId());

			// Fill a map of default properties to start the workflow with
			Map<QName, Serializable> properties = new HashMap<>();
			Date dueDate = Calendar.getInstance().getTime();
			properties.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, dueDate);
			properties.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, 2);
			Serializable workflowPackage = workflowService.createPackage(null);
			properties.put(WorkflowModel.ASSOC_PACKAGE, workflowPackage);
			properties.put(QualityModel.ASSOC_PRODUCT, rawMaterial1NodeRef);

			WorkflowPath path = workflowService.startWorkflow(wfDef.getId(), properties);
			assertNotNull("The workflow path is null!", path);

			workflowInstanceId = path.getInstance().getId();
			assertNotNull("The workflow instance is null!", workflowInstanceId);

			WorkflowTask startTask = workflowService.getStartTask(workflowInstanceId);
			workflowService.endTask(startTask.getId(), null);

			List<WorkflowPath> paths = workflowService.getWorkflowPaths(workflowInstanceId);
			assertEquals(1, paths.size());
			path = paths.get(0);

			List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path.getId());
			assertEquals(1, tasks.size());
			return tasks.get(0);
		}, false, true);

		assertEquals("ncwf:workTask", task1.getName());

		NodeRef ncNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			NodeRef pkgNodeRef = workflowService.getWorkflowById(workflowInstanceId).getWorkflowPackage();
			List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(pkgNodeRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS,
					RegexQNamePattern.MATCH_ALL);
			for (ChildAssociationRef childAssoc : childAssocs) {

				if (QualityModel.TYPE_NC.equals(nodeService.getType(childAssoc.getChildRef()))) {
					return childAssoc.getChildRef();
				}
			}

			return null;
		}, false, true);

		assertNotNull(ncNodeRef);

		/*
		 * Update workTask (analysis)
		 */

		WorkflowTask task = submitTask(workflowInstanceId, "ncwf:workTask", personService.getPerson(BeCPGPLMTestHelper.USER_ONE), "new",
				"commentaire émetteur");
		assertEquals("workTask", task.getPath().getNode().getName());

		/*
		 * do corrActionTask
		 */
		task = submitTask(workflowInstanceId, "ncwf:workTask", personService.getPerson(BeCPGPLMTestHelper.USER_ONE), "analysis",
				"commentaire émetteur 2");
		assertEquals("workTask", task.getPath().getNode().getName());

		/*
		 * do checkTask
		 */
		task = submitTask(workflowInstanceId, "ncwf:workTask", personService.getPerson(BeCPGPLMTestHelper.USER_TWO), "closing",
				"commentaire émetteur 3");
		assertEquals("workTask", task.getPath().getNode().getName());

		/*
		 * do notificationTask
		 */
		task = submitTask(workflowInstanceId, "ncwf:workTask", null, "closed", "commentaire émetteur");

		// BUG !!
		// assertFalse(workflowService.getWorkflowById(workflowInstanceId).isActive());
	}

	private WorkflowTask submitTask(final String workflowInstanceId, final String taskName, final NodeRef assigneeNodeRef, final String state,
			final String comment) {

		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
			taskQuery.setProcessId(workflowInstanceId);
			taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);

			Map<QName, Serializable> properties = new HashMap<>();
			java.util.Map<QName, List<NodeRef>> assocs = new HashMap<>();
			properties.put(WorkflowModel.PROP_COMMENT, comment);
			if (state != null) {
				properties.put(PROP_STATE, state);
			}

			if (assigneeNodeRef != null) {

				List<NodeRef> assignees = new ArrayList<>();
				assignees.add(assigneeNodeRef);
				assocs.put(PROP_ASSIGNEE, assignees);
			}

			List<WorkflowTask> workflowTasks = workflowService.queryTasks(taskQuery, false);

			for (WorkflowTask task : workflowTasks) {
				if (taskName.equals(task.getName())) {

					logger.debug("submit task" + task.getName());
					workflowService.updateTask(task.getId(), properties, assocs, new HashMap<QName, List<NodeRef>>());
					task = workflowService.endTask(task.getId(), null);

					return task;
				}
			}

			return null;
		}, false, true);
	}

}
