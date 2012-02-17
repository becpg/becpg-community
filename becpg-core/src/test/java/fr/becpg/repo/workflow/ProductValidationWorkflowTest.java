package fr.becpg.repo.workflow;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowNode;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.admin.NPDGroup;
import fr.becpg.repo.admin.SystemGroup;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.test.RepoBaseTestCase;

public class ProductValidationWorkflowTest extends RepoBaseTestCase {

		
	
	private static final String USER_ONE = "matthieuWF";
	private static final String USER_TWO = "philippeWF";
	private static String PATH_PRODUCTFOLDER = "TestProductFolder";

	protected static String[] groups = { SystemGroup.ProductReviewer.toString() };

	/** The logger. */
	private static Log logger = LogFactory.getLog(ProductValidationWorkflowTest.class);

	private AuthorityService authorityService;

	private MutableAuthenticationDao authenticationDAO;

	private MutableAuthenticationService authenticationService;

	private PersonService personService;

	private WorkflowService workflowService;
	
	
	private NodeRef folderNodeRef;
	
	private NodeRef rawMaterial1NodeRef;
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.util.BaseAlfrescoTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		workflowService = serviceRegistry.getWorkflowService();
		
		authenticationService =  serviceRegistry.getAuthenticationService();
		authenticationDAO = (MutableAuthenticationDao) ctx.getBean("authenticationDao");
		authorityService = (AuthorityService) ctx.getBean("authorityService");
		personService = (PersonService) ctx.getBean("PersonService");
	
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
		Set<String> authorities = authorityService.getContainedAuthorities(AuthorityType.GROUP,
				PermissionService.GROUP_PREFIX + NPDGroup.FaisabilityAssignersGroup.toString(), true);
		if (!authorities.contains(PermissionService.GROUP_PREFIX + NPDGroup.ValidateFaisability.toString()))
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + NPDGroup.FaisabilityAssignersGroup.toString(),
					PermissionService.GROUP_PREFIX + NPDGroup.ValidateFaisability.toString());
		
		
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

			authorityService.addAuthority(PermissionService.GROUP_PREFIX + SystemGroup.ProductReviewer.toString(),
					USER_TWO);
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
		 transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
	 			public NodeRef execute() throws Throwable {
	 				
	 				createUsers();
	 		        
	 				folderNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_PRODUCTFOLDER);			
	 				if(folderNodeRef != null)
	 				{
	 					nodeService.deleteNode(folderNodeRef);    		
	 				}			
	 				folderNodeRef = fileFolderService.create(repositoryHelper.getCompanyHome(), PATH_PRODUCTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
	 				
	 				RawMaterialData rawMaterial1 = new RawMaterialData();
	 				rawMaterial1.setName("Raw material 1");
	 				
	 				rawMaterial1NodeRef = productDAO.create(folderNodeRef, rawMaterial1, null);
	 				
	 				return null;

	 			}},false,true); 		

		String workflowId = "";
		for (WorkflowDefinition def : workflowService.getAllDefinitions()) {
			logger.debug(def.getId() + " " + def.getName());
			if ("jbpm$bcpgwf:productValidationWF".equals(def.getName())) {
				try {
					for (WorkflowInstance instance : workflowService.getWorkflows(def.getId())) {
						workflowService.deleteWorkflow(instance.getId());
					}

				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
			if ("jbpm$bcpgwf:productValidationWF".equals(def.getName())) {
				workflowId = def.getId();
				break;
			}

		}
		
		authenticationComponent.setCurrentUser(USER_ONE);
		
		// validate 1
		validateProduct(workflowId, rawMaterial1NodeRef);
		
		//validate 2
		//validateProduct(workflowId, rawMaterial1NodeRef);
		
	}

	protected WorkflowTask getNextTaskForWorkflow(String workflowInstanceId) {
		WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
		taskQuery.setProcessId(workflowInstanceId);
		taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);

		List<WorkflowTask> workflowTasks = workflowService.queryTasks(taskQuery);
		assertEquals(1, workflowTasks.size());
		return workflowTasks.get(0);
	}

	private void validateProduct(String workflowId, NodeRef productNodeRef){
		
		// Fill a map of default properties to start the workflow with
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		Date dueDate = Calendar.getInstance().getTime();
		properties.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, dueDate);
		properties.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, 2);

		WorkflowPath path = workflowService.startWorkflow(workflowId, properties);
		assertNotNull("The workflow path is null!", path);

		WorkflowInstance instance = path.getInstance();
		assertNotNull("The workflow instance is null!", instance);

		String workflowInstanceId = instance.getId();

		WorkflowNode node = path.getNode();
		assertNotNull("The workflow node is null!", node);

		assertEquals("start", node.getName());
		
		/*
		 *  Update start task
		 */

		WorkflowTask task = getNextTaskForWorkflow(workflowInstanceId);

		logger.info("Set start information " + task.getName());
		properties = new HashMap<QName, Serializable>();		
		NodeRef  workflowPackage = workflowService.createPackage(null);
		properties.put(WorkflowModel.PROP_COMMENT, "commentaire émetteur");
		properties.put(WorkflowModel.ASSOC_PACKAGE, workflowPackage);
		
		java.util.Map<QName, List<NodeRef>> assocs = new HashMap<QName, List<NodeRef>>();
		
		// add product		
		ChildAssociationRef childAssoc = nodeService.getPrimaryParent(productNodeRef);
		nodeService.addChild(workflowPackage, rawMaterial1NodeRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS, childAssoc.getQName());		

		workflowService.updateTask(task.getId(), properties, assocs, new HashMap<QName, List<NodeRef>>());

		task = workflowService.endTask(task.getId(), "");
		assertEquals("review", task.getPath().getNode().getName());

		/*
		 *  do validation : approve
		 */
		task = submitTask(workflowInstanceId, "bcpgwf:doProductValidationTask", "approve", "OK comment");
		assertEquals("OK comment", task.getProperties().get(WorkflowModel.PROP_COMMENT));
		assertEquals("approved", task.getPath().getNode().getName());				

		/*
		 * submit approved task
		 */
		task = submitTask(workflowInstanceId, "bcpgwf:approveProductTask", "", "Thanks");
		assertEquals("end", task.getPath().getNode().getName());		
		
		printInProgressTasks(workflowInstanceId);
		
		assertFalse(workflowService.getWorkflowById(workflowInstanceId).isActive());
	}
	
	private WorkflowTask submitTask(String workflowInstanceId, String taskName, String transitionName, String comment) {
		
		WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
		taskQuery.setProcessId(workflowInstanceId);
		taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);
		
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		java.util.Map<QName, List<NodeRef>> assocs = new HashMap<QName, List<NodeRef>>();
		properties.put(WorkflowModel.PROP_COMMENT, comment);

		List<WorkflowTask> workflowTasks = workflowService.queryTasks(taskQuery);
		
		for (WorkflowTask task : workflowTasks) {
			if (taskName.equals(task.getName())) {
				
				logger.debug("submit task"+task.getName());				
				workflowService.updateTask(task.getId(), properties, assocs, new HashMap<QName, List<NodeRef>>());				
				task = workflowService.endTask(task.getId(), transitionName);	
				
				return task;
			}
		}
		
		return null;
	}	
	
	private void printInProgressTasks(String workflowInstanceId) {
		WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
		taskQuery.setProcessId(workflowInstanceId);
		taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);
		
		List<WorkflowTask> workflowTasks = workflowService.queryTasks(taskQuery);
		
		for(WorkflowTask task : workflowTasks){
			
			logger.debug("iter task "+ task.getName() + " - " + task.getState() );			
		}		
	}

}
