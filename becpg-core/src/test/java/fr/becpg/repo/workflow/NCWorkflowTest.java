package fr.becpg.repo.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.QualityModel;
import fr.becpg.repo.BeCPGDao;
import fr.becpg.repo.admin.SystemGroup;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.quality.NonConformityService;
import fr.becpg.repo.quality.data.NonConformityData;
import fr.becpg.test.RepoBaseTestCase;

public class NCWorkflowTest extends RepoBaseTestCase {

	private static final String USER_ONE = "matthieuWF";
	private static final String USER_TWO = "philippeWF";
	private static String PATH_NCFOLDER = "TestFolder";

	private static final String NC_URI = "http://www.bcpg.fr/model/nc-workflow/1.0";
	private static final QName PROP_NEED_PREV_ACTION = QName.createQName(NC_URI, "needPrevAction");
	private static final QName PROP_STATE = QName.createQName(NC_URI, "ncState");
	private static final QName PROP_ASSIGNEE = QName.createQName(NC_URI, "assignee");
	private static final QName ASSOC_CORR_ACTION_ACTOR = QName.createQName(NC_URI, "corrActionActor");
	private static final QName ASSOC_CHECK_ACTOR = QName.createQName(NC_URI, "checkActor");
	private static final QName ASSOC_PRODUCT = QName.createQName(NC_URI, "product");

	protected static String[] groups = { SystemGroup.QualityUser.toString(), SystemGroup.QualityMgr.toString() };

	/** The logger. */
	private static Log logger = LogFactory.getLog(NCWorkflowTest.class);

	private AuthorityService authorityService;

	private MutableAuthenticationDao authenticationDAO;

	private MutableAuthenticationService authenticationService;

	private PersonService personService;

	private WorkflowService workflowService;

	private NodeRef folderNodeRef;

	private NodeRef rawMaterial1NodeRef;

	private NodeRef rawMaterial2NodeRef;
	
	private String workflowInstanceId = null;

	private BeCPGDao<NonConformityData> nonConformityDAO;
	
	private NonConformityService nonConformityService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.util.BaseAlfrescoTestCase#setUp()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		workflowService = serviceRegistry.getWorkflowService();

		authenticationService = serviceRegistry.getAuthenticationService();
		authenticationDAO = (MutableAuthenticationDao) ctx.getBean("authenticationDao");
		authorityService = (AuthorityService) ctx.getBean("authorityService");
		personService = (PersonService) ctx.getBean("PersonService");
		nonConformityDAO = (BeCPGDao<NonConformityData>) ctx.getBean("nonConformityDAO");
		nonConformityService = (NonConformityService) ctx.getBean("nonConformityService");
	}

	private void createUsers() {

		/*
		 * Matthieu : user Philippe : validators
		 */

		for (String group : groups) {

			if (!authorityService.authorityExists(PermissionService.GROUP_PREFIX + group)) {
				logger.debug("create group: " + group);
				authorityService.createAuthority(AuthorityType.GROUP, group);
			}
		}

		// USER_ONE
		NodeRef userOne = this.personService.getPerson(USER_ONE);
		if (userOne != null) {
			this.personService.deletePerson(userOne);
		}

		if (!authenticationDAO.userExists(USER_ONE)) {
			createUser(USER_ONE);
		}

		// USER_TWO
		NodeRef userTwo = this.personService.getPerson(USER_TWO);
		if (userTwo != null) {
			this.personService.deletePerson(userTwo);
		}

		if (!authenticationDAO.userExists(USER_TWO)) {
			createUser(USER_TWO);

			authorityService
					.addAuthority(PermissionService.GROUP_PREFIX + SystemGroup.QualityUser.toString(), USER_TWO);
		}

		for (String s : authorityService.getAuthoritiesForUser(USER_ONE)) {
			logger.debug("user in group: " + s);
		}

	}

	private void createUser(String userName) {
		if (this.authenticationService.authenticationExists(userName) == false) {
			this.authenticationService.createAuthentication(userName, "PWD".toCharArray());

			PropertyMap ppOne = new PropertyMap(4);
			ppOne.put(ContentModel.PROP_USERNAME, userName);
			ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
			ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
			ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
			ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");

			this.personService.createPerson(ppOne);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.util.BaseAlfrescoTestCase#tearDown()
	 */
	@Override
	public void tearDown() throws Exception {
		try {
			authenticationComponent.clearCurrentSecurityContext();
		} catch (Throwable e) {
			e.printStackTrace();
			// Don't let this mask any previous exceptions
		}
		super.tearDown();

	}

	public void testWorkFlow() {

		authenticationComponent.setSystemUserAsCurrentUser();
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				createUsers();

				folderNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(),
						ContentModel.ASSOC_CONTAINS, PATH_NCFOLDER);
				if (folderNodeRef != null) {
					nodeService.deleteNode(folderNodeRef);
				}
				folderNodeRef = fileFolderService.create(repositoryHelper.getCompanyHome(), PATH_NCFOLDER,
						ContentModel.TYPE_FOLDER).getNodeRef();

				RawMaterialData rawMaterial1 = new RawMaterialData();
				rawMaterial1.setName("Raw material 1");

				rawMaterial1NodeRef = productDAO.create(folderNodeRef, rawMaterial1, null);

				RawMaterialData rawMaterial2 = new RawMaterialData();
				rawMaterial2.setName("Raw material 2");

				rawMaterial2NodeRef = productDAO.create(folderNodeRef, rawMaterial2, null);
				
				// clean default storage folder
				NodeRef folderNodeRef = nonConformityService.getStorageFolder(null);
				for(FileInfo fileInfo : fileFolderService.list(folderNodeRef)){
					nodeService.deleteNode(fileInfo.getNodeRef());
				}
				return null;

			}
		}, false, true);

		authenticationComponent.setCurrentUser(USER_ONE);
		
		executeNonConformityWF(false);

		executeNonConformityWF(true);
		
		executeNonConformityAdhoc();
	}

	protected WorkflowTask getNextTaskForWorkflow(String workflowInstanceId) {
		WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
		taskQuery.setProcessId(workflowInstanceId);
		taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);

		List<WorkflowTask> workflowTasks = workflowService.queryTasks(taskQuery);
		assertEquals(1, workflowTasks.size());
		return workflowTasks.get(0);
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
				properties.put(ASSOC_PRODUCT, rawMaterial1NodeRef);

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
				List<FileInfo> files = fileFolderService.listFiles(pkgNodeRef);
				for (FileInfo file : files) {

					if (QualityModel.TYPE_NC.equals(nodeService.getType(file.getNodeRef()))) {
						return file.getNodeRef();
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
				properties.put(PROP_STATE, "En cours");

				java.util.Map<QName, List<NodeRef>> assocs = new HashMap<QName, List<NodeRef>>();
				List<NodeRef> assignees = new ArrayList<NodeRef>();
				assignees.add(personService.getPerson(USER_ONE));
				assocs.put(ASSOC_CORR_ACTION_ACTOR, assignees);
				assignees = new ArrayList<NodeRef>();
				assignees.add(personService.getPerson(USER_TWO));
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

		checkWorkLog(ncNodeRef, 1, "En cours", "commentaire émetteur");
		/*
		 * do corrActionTask
		 */
		WorkflowTask task = submitTask(workflowInstanceId, "ncwf:corrActionTask", null, "À déclasser", "commentaire émetteur 2");
		assertEquals("checkTask", task.getPath().getNode().getName());

		checkWorkLog(ncNodeRef, 2, "À déclasser", "commentaire émetteur 2");

		/*
		 * do checkTask
		 */
		task = submitTask(workflowInstanceId, "ncwf:checkTask", null, "Résolu", "commentaire émetteur 3");
		assertEquals("notificationTask", task.getPath().getNode().getName());

		checkWorkLog(ncNodeRef, 3, "Résolu", "commentaire émetteur 3");

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
				properties.put(ASSOC_PRODUCT, rawMaterial1NodeRef);

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
				List<FileInfo> files = fileFolderService.listFiles(pkgNodeRef);
				for (FileInfo file : files) {

					if (QualityModel.TYPE_NC.equals(nodeService.getType(file.getNodeRef()))) {
						return file.getNodeRef();
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

		WorkflowTask task = submitTask(workflowInstanceId, "ncwf:workTask", personService.getPerson(USER_ONE), "En cours",
				"commentaire émetteur");
		assertEquals("workTask", task.getPath().getNode().getName());

		checkWorkLog(ncNodeRef, 1, "En cours", "commentaire émetteur");
		/*
		 * do corrActionTask
		 */
		task = submitTask(workflowInstanceId, "ncwf:workTask", personService.getPerson(USER_ONE), "À déclasser",
				"commentaire émetteur 2");
		assertEquals("workTask", task.getPath().getNode().getName());

		checkWorkLog(ncNodeRef, 2, "À déclasser", "commentaire émetteur 2");

		/*
		 * do checkTask
		 */
		task = submitTask(workflowInstanceId, "ncwf:workTask", personService.getPerson(USER_TWO), "Résolu",
				"commentaire émetteur 3");
		assertEquals("workTask", task.getPath().getNode().getName());

		checkWorkLog(ncNodeRef, 3, "Résolu", "commentaire émetteur 3");

		/*
		 * do notificationTask
		 */
		task = submitTask(workflowInstanceId, "ncwf:workTask", null, "Fermé", "commentaire émetteur");

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

				List<WorkflowTask> workflowTasks = workflowService.queryTasks(taskQuery);

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

				NonConformityData ncData = nonConformityDAO.find(ncNodeRef);
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
				NodeRef nonConformityFolderNodeRef = nodeService.getPrimaryParent(ncNodeRef).getParentRef();
				NodeRef nonConformitiesFolderNodeRef = nodeService.getPrimaryParent(nonConformityFolderNodeRef).getParentRef();
				NodeRef productFolderNodeRef = nodeService.getPrimaryParent(nonConformitiesFolderNodeRef).getParentRef();
				NodeRef rawMaterial1FolderNodeRef = nodeService.getPrimaryParent(rawMaterial1NodeRef).getParentRef();
								
				assertEquals("Check NC moved in product", rawMaterial1FolderNodeRef, productFolderNodeRef);

				// remove assoc
				nodeService.removeAssociation(ncNodeRef, rawMaterial1NodeRef, QualityModel.ASSOC_PRODUCT);

				nonConformityFolderNodeRef = nodeService.getPrimaryParent(ncNodeRef).getParentRef();
				nonConformitiesFolderNodeRef = nodeService.getPrimaryParent(nonConformityFolderNodeRef).getParentRef();
				assertEquals(
						"/{http://www.alfresco.org/model/application/1.0}company_home/{http://www.alfresco.org/model/content/1.0}Quality/{http://www.alfresco.org/model/content/1.0}NonConformities",
						nodeService.getPath(nonConformitiesFolderNodeRef).toString());

				// create assoc rawMaterial 2
				nodeService.createAssociation(ncNodeRef, rawMaterial2NodeRef, QualityModel.ASSOC_PRODUCT);

				nonConformityFolderNodeRef = nodeService.getPrimaryParent(ncNodeRef).getParentRef();
				nonConformitiesFolderNodeRef = nodeService.getPrimaryParent(nonConformityFolderNodeRef).getParentRef();
				productFolderNodeRef = nodeService.getPrimaryParent(nonConformitiesFolderNodeRef).getParentRef();
				NodeRef rawMaterial2FolderNodeRef = nodeService.getPrimaryParent(rawMaterial2NodeRef).getParentRef();
				assertEquals("Check NC moved in product", rawMaterial2FolderNodeRef, productFolderNodeRef);
				
				return null;
			}
		}, false, true);
		
	}
}
