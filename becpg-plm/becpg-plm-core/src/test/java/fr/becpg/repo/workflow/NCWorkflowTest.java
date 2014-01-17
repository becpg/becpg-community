package fr.becpg.repo.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
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

import fr.becpg.model.QualityModel;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.quality.NonConformityService;
import fr.becpg.repo.quality.data.NonConformityData;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.test.BeCPGPLMTestHelper;

public class NCWorkflowTest extends AbstractWorkflowTest {

	
	private static final String NC_URI = "http://www.bcpg.fr/model/nc-workflow/1.0";
	private static final QName PROP_NEED_PREV_ACTION = QName.createQName(NC_URI, "needPrevAction");
	private static final QName PROP_STATE = QName.createQName(NC_URI, "ncState");
	private static final QName PROP_ASSIGNEE = QName.createQName(NC_URI, "assignee");
	private static final QName ASSOC_CORR_ACTION_ACTOR = QName.createQName(NC_URI, "corrActionActor");
	private static final QName ASSOC_CHECK_ACTOR = QName.createQName(NC_URI, "checkActor");


	@Resource
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(NCWorkflowTest.class);

	private NodeRef rawMaterial1NodeRef;

	private NodeRef rawMaterial2NodeRef;
	
	private String workflowInstanceId = null;

	@Resource
	private NonConformityService nonConformityService;


	
	@Test
	public void testWorkFlow() {

		authenticationComponent.setSystemUserAsCurrentUser();
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				BeCPGPLMTestHelper.createUsers();

				
				RawMaterialData rawMaterial1 = new RawMaterialData();
				rawMaterial1.setName("Raw material 1");

				rawMaterial1NodeRef = alfrescoRepository.create(testFolderNodeRef, rawMaterial1).getNodeRef();

				RawMaterialData rawMaterial2 = new RawMaterialData();
				rawMaterial2.setName("Raw material 2");

				rawMaterial2NodeRef = alfrescoRepository.create(testFolderNodeRef, rawMaterial2).getNodeRef();
				
				// clean default storage folder
				NodeRef folderNodeRef = nonConformityService.getStorageFolder(null);
				for(FileInfo fileInfo : fileFolderService.list(folderNodeRef)){
					nodeService.deleteNode(fileInfo.getNodeRef());
				}
				return null;

			}
		}, false, true);

		authenticationComponent.setCurrentUser(BeCPGPLMTestHelper.USER_ONE);
		
		executeNonConformityWF(false);

		executeNonConformityWF(true);
		
		executeNonConformityAdhoc();
	}


	private void executeNonConformityWF(final boolean needPrevAction) {

		final WorkflowTask task1 = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<WorkflowTask>() {
			public WorkflowTask execute() throws Throwable {

				WorkflowDefinition wfDef = workflowService.getDefinitionByName("activiti$nonConformityProcess");
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
		
		assertEquals("ncwf:analysisTask", task1.getName());
		
		NodeRef ncNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {
				
				NodeRef pkgNodeRef = workflowService.getWorkflowById(workflowInstanceId).getWorkflowPackage();
				List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(pkgNodeRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS, RegexQNamePattern.MATCH_ALL);
				for (ChildAssociationRef childAssoc : childAssocs) {
					if (QualityModel.TYPE_NC.equals(nodeService.getType(childAssoc.getChildRef()))) {
						return childAssoc.getChildRef();
					}
				}
				
				return null;
			}
		}, false, true);

		/*
		 * Update analysisTask task
		 */
		assertNotNull(ncNodeRef);
		checkStorageFolder(ncNodeRef);
		
		final WorkflowTask task2 = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<WorkflowTask>() {
			public WorkflowTask execute() throws Throwable {

				logger.info("Set analysisTask information " + task1.getName());
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
				properties.put(WorkflowModel.PROP_COMMENT, "commentaire émetteur");
				properties.put(PROP_NEED_PREV_ACTION, needPrevAction);
				properties.put(PROP_STATE, "new");

				java.util.Map<QName, List<NodeRef>> assocs = new HashMap<QName, List<NodeRef>>();
				List<NodeRef> assignees = new ArrayList<NodeRef>();
				assignees.add(personService.getPerson(BeCPGPLMTestHelper.USER_ONE));
				assocs.put(ASSOC_CORR_ACTION_ACTOR, assignees);
				assignees = new ArrayList<NodeRef>();
				assignees.add(personService.getPerson(BeCPGPLMTestHelper.USER_TWO));
				assocs.put(ASSOC_CHECK_ACTOR, assignees);

				workflowService.updateTask(task1.getId(), properties, assocs, new HashMap<QName, List<NodeRef>>());
				return workflowService.endTask(task1.getId(), null);			
			}
		}, false, true);

		if (needPrevAction) {
			assertEquals("prevActionTask", task2.getPath().getNode().getName());
		} else {
			assertEquals("corrActionTask", task2.getPath().getNode().getName());
		}

		checkWorkLog(ncNodeRef, 1, "new", "commentaire émetteur");
		/*
		 * do corrActionTask
		 */
		WorkflowTask task = submitTask(workflowInstanceId, "ncwf:corrActionTask", null, "analysis", "commentaire émetteur 2");
		assertEquals("checkTask", task.getPath().getNode().getName());

		checkWorkLog(ncNodeRef, 2, "analysis", "commentaire émetteur 2");

		/*
		 * do checkTask
		 */
		task = submitTask(workflowInstanceId, "ncwf:checkTask", null, "closing", "commentaire émetteur 3");
		assertEquals("notificationTask", task.getPath().getNode().getName());

		checkWorkLog(ncNodeRef, 3, "closing", "commentaire émetteur 3");

		/*
		 * do notificationTask
		 */
		task = submitTask(workflowInstanceId, "ncwf:notificationTask", null, null, "commentaire émetteur");

		/*
		 * do prevActionTask
		 */
		if (needPrevAction) {
			assertTrue(workflowService.getWorkflowById(workflowInstanceId).isActive());
			task = submitTask(workflowInstanceId, "ncwf:prevActionTask", null, null, "commentaire émetteur");
		}

		assertFalse(workflowService.getWorkflowById(workflowInstanceId).isActive());
	}

	private void executeNonConformityAdhoc() {
		
		final WorkflowTask task1 = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<WorkflowTask>() {
			public WorkflowTask execute() throws Throwable {

				WorkflowDefinition wfDef = workflowService.getDefinitionByName("activiti$nonConformityAdhoc");
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
		
		assertEquals("ncwf:workTask", task1.getName());
		
		NodeRef ncNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {
				
				NodeRef pkgNodeRef = workflowService.getWorkflowById(workflowInstanceId).getWorkflowPackage();
				List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(pkgNodeRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS, RegexQNamePattern.MATCH_ALL);
				for (ChildAssociationRef childAssoc : childAssocs) {

					if (QualityModel.TYPE_NC.equals(nodeService.getType(childAssoc.getChildRef()))) {
						return childAssoc.getChildRef();
					}
				}
				
				return null;
			}
		}, false, true);
		
		assertNotNull(ncNodeRef);

		checkStorageFolder(ncNodeRef);

		/*
		 * Update workTask (analysis)
		 */

		WorkflowTask task = submitTask(workflowInstanceId, "ncwf:workTask", personService.getPerson(BeCPGPLMTestHelper.USER_ONE), "new",
				"commentaire émetteur");
		assertEquals("workTask", task.getPath().getNode().getName());

		checkWorkLog(ncNodeRef, 1, "new", "commentaire émetteur");
		/*
		 * do corrActionTask
		 */
		task = submitTask(workflowInstanceId, "ncwf:workTask", personService.getPerson(BeCPGPLMTestHelper.USER_ONE), "analysis",
				"commentaire émetteur 2");
		assertEquals("workTask", task.getPath().getNode().getName());

		checkWorkLog(ncNodeRef, 2, "analysis", "commentaire émetteur 2");

		/*
		 * do checkTask
		 */
		task = submitTask(workflowInstanceId, "ncwf:workTask", personService.getPerson(BeCPGPLMTestHelper.USER_TWO), "closing",
				"commentaire émetteur 3");
		assertEquals("workTask", task.getPath().getNode().getName());

		checkWorkLog(ncNodeRef, 3, "closing", "commentaire émetteur 3");

		/*
		 * do notificationTask
		 */
		task = submitTask(workflowInstanceId, "ncwf:workTask", null, "closed", "commentaire émetteur");

		assertFalse(workflowService.getWorkflowById(workflowInstanceId).isActive());
	}

	private WorkflowTask submitTask(final String workflowInstanceId, final String taskName, final NodeRef assigneeNodeRef, final String state,
			final String comment) {
		
		return transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<WorkflowTask>() {
			public WorkflowTask execute() throws Throwable {

				WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
				taskQuery.setProcessId(workflowInstanceId);
				taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);

				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
				java.util.Map<QName, List<NodeRef>> assocs = new HashMap<QName, List<NodeRef>>();
				properties.put(WorkflowModel.PROP_COMMENT, comment);
				if (state != null) {
					properties.put(PROP_STATE, state);
				}

				if (assigneeNodeRef != null) {

					List<NodeRef> assignees = new ArrayList<NodeRef>();
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
			}
		}, false, true);	
	}

	private void checkWorkLog(final NodeRef ncNodeRef, final int workLogSize, final String state, final String comment) {

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				NonConformityData ncData = (NonConformityData)alfrescoRepository.findOne(ncNodeRef);
				assertNotNull(ncData.getWorkLog());
				assertEquals(workLogSize, ncData.getWorkLog().size());
				assertEquals(state, ncData.getState());
				assertEquals(null, ncData.getComment());
				assertEquals(state, ncData.getWorkLog().get(workLogSize - 1).getState());
				assertEquals(comment, ncData.getWorkLog().get(workLogSize - 1).getComment());
				return null;
			}
		}, true, true);		
	}

	private void checkStorageFolder(final NodeRef ncNodeRef) {

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				/*
				 * We should have: Folder Product Folder NonConformities Folder
				 * NonConformity NC1 Node NC1
				 */
				NodeRef nonConformitiesFolderNodeRef = nodeService.getPrimaryParent(ncNodeRef).getParentRef();
				NodeRef productFolderNodeRef = nodeService.getPrimaryParent(nonConformitiesFolderNodeRef).getParentRef();
								
				assertEquals("Check NC moved in product", rawMaterial1NodeRef, productFolderNodeRef);

				// remove assoc
				nodeService.removeAssociation(ncNodeRef, rawMaterial1NodeRef, QualityModel.ASSOC_PRODUCT);

				nonConformitiesFolderNodeRef = nodeService.getPrimaryParent(ncNodeRef).getParentRef();
				assertEquals(
						"/{http://www.alfresco.org/model/application/1.0}company_home/{http://www.alfresco.org/model/content/1.0}Quality/{http://www.alfresco.org/model/content/1.0}NonConformities",
						nodeService.getPath(nonConformitiesFolderNodeRef).toString());

				// create assoc rawMaterial 2
				nodeService.createAssociation(ncNodeRef, rawMaterial2NodeRef, QualityModel.ASSOC_PRODUCT);

				nonConformitiesFolderNodeRef = nodeService.getPrimaryParent(ncNodeRef).getParentRef();
				productFolderNodeRef = nodeService.getPrimaryParent(nonConformitiesFolderNodeRef).getParentRef();
				assertEquals("Check NC moved in product", rawMaterial2NodeRef, productFolderNodeRef);
				
				return null;
			}
		}, false, true);
		
	}
}
