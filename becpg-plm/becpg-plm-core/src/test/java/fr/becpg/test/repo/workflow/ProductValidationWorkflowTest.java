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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowNode;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.test.BeCPGTestHelper;

public class ProductValidationWorkflowTest extends AbstractWorkflowTest {

	private static String PATH_PRODUCTFOLDER = "TestProductFolder";

	private static final String WF_URI = "http://www.bcpg.fr/model/workflow/1.0";

//	private static final QName PROP_reviewQualityApproval = QName.createQName(WF_URI, "reviewQualityApproval");
//	private static final QName PROP_reviewQualityComment = QName.createQName(WF_URI, "reviewQualityComment");
//	private static final QName PROP_reviewProductionApproval = QName.createQName(WF_URI, "reviewProductionApproval");
	private static final QName PROP_pvTransmitterComment = QName.createQName(WF_URI, "pvTransmitterComment");
	private static final QName PROP_reviewRDApproval = QName.createQName(WF_URI, "reviewRDApproval");
	private static final QName PROP_reviewRDComment = QName.createQName(WF_URI, "reviewRDComment");
	private static final QName PROP_reviewCallerApproval = QName.createQName(WF_URI, "reviewCallerApproval");
	private static final QName PROP_pvCallerActor = QName.createQName(WF_URI, "pvCallerActor");
//	private static final QName PROP_pvQualityApprovalActor = QName.createQName(WF_URI, "pvQualityApprovalActor");
//	private static final QName PROP_pvProductionApprovalActor = QName.createQName(WF_URI, "pvProductionApprovalActor");
	private static final QName PROP_pvRDApprovalActor = QName.createQName(WF_URI, "pvRDApprovalActor");
	protected static final QName PROP_notifyUsers  = QName.createQName(WF_URI, "notifyUsers");

	/** The logger. */
	private static Log logger = LogFactory.getLog(ProductValidationWorkflowTest.class);

	@Test
	public void testWorkFlow() {

		authenticationComponent.setSystemUserAsCurrentUser();
		final NodeRef rawMaterial1NodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				BeCPGTestHelper.createUsers();

				NodeRef folderNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_PRODUCTFOLDER);
				if (folderNodeRef != null) {
					nodeService.deleteNode(folderNodeRef);
				}
				folderNodeRef = fileFolderService.create(repositoryHelper.getCompanyHome(), PATH_PRODUCTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();

				RawMaterialData rawMaterial1 = new RawMaterialData();
				rawMaterial1.setName("Raw material 1");

				return alfrescoRepository.create(folderNodeRef, rawMaterial1).getNodeRef();

			}
		}, false, true);

		authenticationComponent.setCurrentUser("admin");
//		
//		for(WorkflowDefinition wfDef : workflowService.getAllDefinitions()){
//			logger.error("Definition : "+wfDef.getId()+" - "+wfDef.getName());
//		}
		
		WorkflowDefinition wfDef = workflowService.getDefinitionByName("activiti$productValidationWF");
		logger.debug("wfDefId found : " + wfDef.getId());

		// validate 1
		validateProduct(wfDef.getId(), rawMaterial1NodeRef);

		// validate 2
		validateProduct(wfDef.getId(), rawMaterial1NodeRef);

	}

	private void validateProduct(final String workflowId, final NodeRef productNodeRef) {

		String workflowInstanceId =transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<String>() {
			public String execute() throws Throwable {

				// Fill a map of default properties to start the workflow with
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
				Date dueDate = Calendar.getInstance().getTime();
				properties.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, dueDate);
				properties.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, 2);
				properties.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, "test");
				properties.put(PROP_pvTransmitterComment, "test");

				Serializable workflowPackage = workflowService.createPackage(null);
				properties.put(WorkflowModel.ASSOC_PACKAGE, workflowPackage);
				
				ChildAssociationRef childAssoc = nodeService.getPrimaryParent(productNodeRef);
			     nodeService.addChild((NodeRef)workflowPackage, productNodeRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS, childAssoc.getQName());	

				List<NodeRef> assignees = new ArrayList<NodeRef>();
				assignees.add(personService.getPerson(BeCPGTestHelper.USER_ONE));
				properties.put(PROP_pvRDApprovalActor, (Serializable) assignees);
				assignees = new ArrayList<NodeRef>();
				assignees.add(personService.getPerson(BeCPGTestHelper.USER_TWO));
				properties.put(PROP_pvCallerActor, (Serializable) assignees);

				WorkflowPath path = workflowService.startWorkflow(workflowId, properties);
				assertNotNull("The workflow path is null!", path);

				WorkflowInstance instance = path.getInstance();
				assertNotNull("The workflow instance is null!", instance);


				WorkflowNode node = path.getNode();
				assertNotNull("The workflow node is null!", node);
				
				 return instance.getId();

			}
		}, false, true);
		


		WorkflowTask task =  getNextTaskForWorkflow(workflowInstanceId);
		
		logger.info(task.getPath().getNode().getName());
		assertEquals("doProductValidationRDTask", task.getPath().getNode().getName());

		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(PROP_reviewRDApproval, "Rejected");
		properties.put(PROP_reviewRDComment, "OK comment");

		task = submitTask(workflowInstanceId, "bcpgwf:doProductValidationRDTask", null, properties);
		assertNotNull(task);
		assertEquals("OK comment", task.getProperties().get(WorkflowModel.PROP_COMMENT));
		logger.info(task.getPath().getNode().getName());
		assertEquals("rejectProductTask", task.getPath().getNode().getName());

		properties = new HashMap<QName, Serializable>();
		properties.put(PROP_pvTransmitterComment, "OK comment");

		task = submitTask(workflowInstanceId, "bcpgwf:rejectProductTask", null, properties);
		assertNotNull(task);
		assertEquals("OK comment", task.getProperties().get(WorkflowModel.PROP_COMMENT));
		logger.info(task.getPath().getNode().getName());
		assertEquals("doProductValidationRDTask", task.getPath().getNode().getName());

		properties = new HashMap<QName, Serializable>();
		properties.put(PROP_reviewRDApproval, "Approved");
		properties.put(PROP_reviewRDComment, "OK comment");

		task = submitTask(workflowInstanceId, "bcpgwf:doProductValidationRDTask", null, properties);
		assertNotNull(task);
		assertEquals("OK comment", task.getProperties().get(WorkflowModel.PROP_COMMENT));
		assertEquals("Approved", task.getProperties().get(PROP_reviewRDApproval));
		logger.info(task.getPath().getNode().getName());
		assertEquals("doProductValidationCallerTask", task.getPath().getNode().getName());

		properties = new HashMap<QName, Serializable>();
		properties.put(PROP_reviewCallerApproval, "Approved");

		task = submitTask(workflowInstanceId, "bcpgwf:doProductValidationCallerTask", null, properties);
		assertNotNull(task);
		logger.info(task.getPath().getNode().getName());
		assertEquals("approveProductTask", task.getPath().getNode().getName());

		/*
		 * submit approved task
		 */
		task = submitTask(workflowInstanceId, "bcpgwf:approveProductTask", null, new HashMap<QName, Serializable>());
		//logger.info(task.getPath().getNode().getName());
		
		
		printInProgressTasks(workflowInstanceId);
//
//		assertFalse(workflowService.getWorkflowById(workflowInstanceId).isActive());
	}

}
