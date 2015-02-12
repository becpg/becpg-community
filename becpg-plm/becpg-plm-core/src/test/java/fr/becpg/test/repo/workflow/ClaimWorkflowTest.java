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
package fr.becpg.test.repo.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;

import fr.becpg.model.ClaimWorkflowModel;
import fr.becpg.model.QualityModel;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.quality.NonConformityService;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.test.BeCPGPLMTestHelper;

public class ClaimWorkflowTest extends AbstractWorkflowTest {

	@Resource
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	/** The logger. */
	private static Log logger = LogFactory.getLog(ClaimWorkflowTest.class);

	private NodeRef rawMaterial1NodeRef;

	private String workflowInstanceId = null;

	@Resource
	private NonConformityService nonConformityService;

	@Test
	public void testWorkFlow() {

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				BeCPGPLMTestHelper.createUsers();

				RawMaterialData rawMaterial1 = new RawMaterialData();
				rawMaterial1.setName("Raw material 1");

				rawMaterial1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial1).getNodeRef();

				RawMaterialData rawMaterial2 = new RawMaterialData();
				rawMaterial2.setName("Raw material 2");

				alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial2).getNodeRef();

				// clean default storage folder
				NodeRef folderNodeRef = nonConformityService.getStorageFolder(null);
				for (FileInfo fileInfo : fileFolderService.list(folderNodeRef)) {
					nodeService.deleteNode(fileInfo.getNodeRef());
				}
				return null;

			}
		}, false, true);

		executeClaimWF(true);

		executeClaimWF(false);

		testDeleteNC();

	}

	private void testDeleteNC() {
		 transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<WorkflowTask>() {
			public WorkflowTask execute() throws Throwable {

				WorkflowDefinition wfDef = workflowService.getDefinitionByName("activiti$claimProcess");
				logger.debug("wfDefId found : " + wfDef.getId());

				// Fill a map of default properties to start the workflow with
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
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
				
				assertEquals("ncwf:claimAnalysisTask", tasks.get(0).getName());
				
				return tasks.get(0);
			}
		}, false, true);

		

		final NodeRef ncNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				NodeRef pkgNodeRef = workflowService.getWorkflowById(workflowInstanceId).getWorkflowPackage();
				List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(pkgNodeRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS,
						RegexQNamePattern.MATCH_ALL);
				for (ChildAssociationRef childAssoc : childAssocs) {
					if (QualityModel.TYPE_NC.equals(nodeService.getType(childAssoc.getChildRef()))) {
						return childAssoc.getChildRef();
					}
				}

				return null;
			}
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<WorkflowTask>() {
			public WorkflowTask execute() throws Throwable {
				nodeService.deleteNode(ncNodeRef);
				return null;
			}
		}, false, true);
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<WorkflowTask>() {
			public WorkflowTask execute() throws Throwable {
				Assert.assertNull(workflowService.getWorkflowById(workflowInstanceId));
				return null;
			}
		}, false, true);
	}

	private void executeClaimWF(boolean shortWay) {

		WorkflowTask task = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<WorkflowTask>() {
			public WorkflowTask execute() throws Throwable {
				AuthenticationUtil.setFullyAuthenticatedUser(BeCPGPLMTestHelper.USER_ONE);

				WorkflowDefinition wfDef = workflowService.getDefinitionByName("activiti$claimProcess");
				logger.debug("wfDefId found : " + wfDef.getId());

				// Fill a map of default properties to start the workflow with
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
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
			}
		}, false, true);

		assertEquals("ncwf:claimAnalysisTask", task.getName());

		// Assert NC of type Claim created

		NodeRef ncNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				NodeRef pkgNodeRef = workflowService.getWorkflowById(workflowInstanceId).getWorkflowPackage();
				List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(pkgNodeRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS,
						RegexQNamePattern.MATCH_ALL);
				for (ChildAssociationRef childAssoc : childAssocs) {
					if (QualityModel.TYPE_NC.equals(nodeService.getType(childAssoc.getChildRef()))) {
						return childAssoc.getChildRef();
					}
				}

				return null;
			}
		}, false, true);

		assertNotNull(ncNodeRef);

		assertEquals("Claim", (String) nodeService.getProperty(ncNodeRef, QualityModel.PROP_NC_TYPE));

		// checkStorageFolder(ncNodeRef);

		// Back to saisie
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ClaimWorkflowModel.PROP_REJECTED_STATE, "entry");

		task = submitTask(workflowInstanceId, "ncwf:claimAnalysisTask", null, properties);
		assertEquals("enteringClaimTask", task.getPath().getNode().getName());

		properties = new HashMap<QName, Serializable>();

		task = submitTask(workflowInstanceId, "ncwf:claimStartTask", null, properties);
		assertEquals("analysisTask", task.getPath().getNode().getName());

		if (!shortWay) {

			properties = new HashMap<QName, Serializable>();
			properties.put(WorkflowModel.PROP_COMMENT, "commentaire émetteur");

			java.util.Map<QName, List<NodeRef>> assocs = new HashMap<QName, List<NodeRef>>();
			List<NodeRef> assignees = new ArrayList<NodeRef>();
			assignees.add(personService.getPerson(BeCPGPLMTestHelper.USER_ONE));
			assocs.put(QualityModel.ASSOC_CLAIM_TREATEMENT_ACTOR, assignees);
			assignees = new ArrayList<NodeRef>();
			assignees.add(personService.getPerson(BeCPGPLMTestHelper.USER_TWO));
			assocs.put(QualityModel.ASSOC_CLAIM_RESPONSE_ACTOR, assignees);

			task = submitTask(workflowInstanceId, "ncwf:claimAnalysisTask", null, properties, assocs);

			assertEquals("claimTreatmentTask", task.getPath().getNode().getName());
			task = submitTask(workflowInstanceId, "ncwf:claimTreatmentTask", null, new HashMap<QName, Serializable>());
			assertEquals("claimResponseTask", task.getPath().getNode().getName());

			properties = new HashMap<QName, Serializable>();
			properties.put(ClaimWorkflowModel.PROP_REJECTED_STATE, "treatment");
			task = submitTask(workflowInstanceId, "ncwf:claimResponseTask", null, properties);
			assertEquals("claimTreatmentTask", task.getPath().getNode().getName());

			properties.put(ClaimWorkflowModel.PROP_REJECTED_STATE, "analysis");
			task = submitTask(workflowInstanceId, "ncwf:claimTreatmentTask", null, properties);
			// properties = new HashMap<QName, Serializable>();
			// task = submitTask(workflowInstanceId,
			// "ncwf:claimClassificationTask", null, properties);

			assertNotTask(workflowInstanceId, "ncwf:claimClassificationTask");
			// assertEquals("analysisTask", task.getPath().getNode().getName());
			properties = new HashMap<QName, Serializable>();
			task = submitTask(workflowInstanceId, "ncwf:claimAnalysisTask", null, properties);
			assertEquals("claimTreatmentTask", task.getPath().getNode().getName());

			properties = new HashMap<QName, Serializable>();
			task = submitTask(workflowInstanceId, "ncwf:claimTreatmentTask", null, properties);
			assertEquals("claimResponseTask", task.getPath().getNode().getName());

			properties = new HashMap<QName, Serializable>();
			task = submitTask(workflowInstanceId, "ncwf:claimResponseTask", null, properties);
			task = submitTask(workflowInstanceId, "ncwf:claimClassificationTask", null, properties);
			// assertEquals("claimClosingTask",
			// task.getPath().getNode().getName());

			properties.put(ClaimWorkflowModel.PROP_REJECTED_STATE, "classification");
			task = submitTask(workflowInstanceId, "ncwf:claimClosingTask", null, properties);
			assertEquals("classificationTask", task.getPath().getNode().getName());
			properties = new HashMap<QName, Serializable>();
			task = submitTask(workflowInstanceId, "ncwf:claimClassificationTask", null, properties);
			// assertEquals("claimClosingTask",
			// task.getPath().getNode().getName());

			properties.put(ClaimWorkflowModel.PROP_REJECTED_STATE, "response");
			task = submitTask(workflowInstanceId, "ncwf:claimClosingTask", null, properties);
			assertEquals("claimResponseTask", task.getPath().getNode().getName());
			properties = new HashMap<QName, Serializable>();
			task = submitTask(workflowInstanceId, "ncwf:claimResponseTask", null, properties);
			// assertEquals("claimClosingTask",
			// task.getPath().getNode().getName());

			properties = new HashMap<QName, Serializable>();
			task = submitTask(workflowInstanceId, "ncwf:claimClosingTask", null, properties);

			assertFalse(workflowService.getWorkflowById(workflowInstanceId).isActive());

		} else {
			properties = new HashMap<QName, Serializable>();
			task = submitTask(workflowInstanceId, "ncwf:claimAnalysisTask", null, properties);
			assertEquals("classificationTask", task.getPath().getNode().getName());
			task = submitTask(workflowInstanceId, "ncwf:claimClassificationTask", null, properties);
			// assertEquals("claimClosingTask",
			// task.getPath().getNode().getName());

			properties = new HashMap<QName, Serializable>();
			task = submitTask(workflowInstanceId, "ncwf:claimClosingTask", null, properties);

			assertFalse(workflowService.getWorkflowById(workflowInstanceId).isActive());

		}
	}

	// private void checkWorkLog(final NodeRef ncNodeRef, final int workLogSize,
	// final String state, final String comment) {
	//
	// transactionService.getRetryingTransactionHelper().doInTransaction(new
	// RetryingTransactionCallback<NodeRef>() {
	// public NodeRef execute() throws Throwable {
	//
	// NonConformityData ncData =
	// (NonConformityData)alfrescoRepository.findOne(ncNodeRef);
	// assertNotNull(ncData.getWorkLog());
	// assertEquals(workLogSize, ncData.getWorkLog().size());
	// assertEquals(state, ncData.getState());
	// assertEquals(null, ncData.getComment());
	// assertEquals(state, ncData.getWorkLog().get(workLogSize - 1).getState());
	// assertEquals(comment, ncData.getWorkLog().get(workLogSize -
	// 1).getComment());
	// return null;
	// }
	// }, true, true);
	// }
	//
	// private void checkStorageFolder(final NodeRef ncNodeRef) {
	//
	// transactionService.getRetryingTransactionHelper().doInTransaction(new
	// RetryingTransactionCallback<NodeRef>() {
	// public NodeRef execute() throws Throwable {
	//
	// /*
	// * We should have: Folder Product Folder NonConformities Folder
	// * NonConformity NC1 Node NC1
	// */
	// NodeRef nonConformitiesFolderNodeRef =
	// nodeService.getPrimaryParent(ncNodeRef).getParentRef();
	// NodeRef productFolderNodeRef =
	// nodeService.getPrimaryParent(nonConformitiesFolderNodeRef).getParentRef();
	//
	// assertEquals("Check NC moved in product", rawMaterial1NodeRef,
	// productFolderNodeRef);
	//
	//
	//
	// // remove assoc
	// nodeService.removeAssociation(ncNodeRef, rawMaterial1NodeRef,
	// QualityModel.ASSOC_PRODUCT);
	//
	// nonConformitiesFolderNodeRef =
	// nodeService.getPrimaryParent(ncNodeRef).getParentRef();
	// assertEquals(
	// "/{http://www.alfresco.org/model/application/1.0}company_home/{http://www.alfresco.org/model/content/1.0}Quality/{http://www.alfresco.org/model/content/1.0}NonConformities",
	// nodeService.getPath(nonConformitiesFolderNodeRef).toString());
	//
	// // create assoc rawMaterial 2
	// nodeService.createAssociation(ncNodeRef, rawMaterial2NodeRef,
	// QualityModel.ASSOC_PRODUCT);
	//
	// nonConformitiesFolderNodeRef =
	// nodeService.getPrimaryParent(ncNodeRef).getParentRef();
	// productFolderNodeRef =
	// nodeService.getPrimaryParent(nonConformitiesFolderNodeRef).getParentRef();
	// assertEquals("Check NC moved in product", rawMaterial2NodeRef,
	// productFolderNodeRef);
	//
	// return null;
	// }
	// }, false, true);
	//
	// }
}
