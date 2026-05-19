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
package fr.becpg.test.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.model.SecurityModel;
import fr.becpg.repo.form.BecpgFormService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.security.SecurityService;
import fr.becpg.repo.security.data.ACLGroupData;
import fr.becpg.repo.security.data.PermissionModel;
import fr.becpg.repo.security.data.dataList.ACLEntryDataItem;
import fr.becpg.repo.security.filter.SecurityContextHelper;
import fr.becpg.test.BeCPGTestHelper;
import fr.becpg.test.RepoBaseTestCase;
import fr.becpg.test.utils.TestWebscriptExecuters;
import fr.becpg.test.utils.TestWebscriptExecuters.GetRequest;
import fr.becpg.test.utils.TestWebscriptExecuters.Response;

/**
 * Integration test for wizard security rules functionality.
 * Tests the complete security flow from wizard to backend with skipSecurityRules parameter.
 * 
 * @author matthieu
 */
public class WizardSecurityRulesIT extends RepoBaseTestCase {

	private static final Log logger = LogFactory.getLog(WizardSecurityRulesIT.class);

	@Autowired
	private SecurityService securityService;

	@Autowired
	@Qualifier("WorkflowService")
	private WorkflowService workflowService;

	@Autowired
	private BecpgFormService becpgFormService;

	// Test project
	private NodeRef projectNodeRef;

	// Test workflow
	private String workflowInstanceId;

	private static final String GROUP_WIZARD_SECURITY_WRITE = "GRP_WIZARD_SECURITY_WRITE";

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		inWriteTx(() -> {
			AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

			// Create test users
			BeCPGTestHelper.createUser("userOne");
			BeCPGTestHelper.createUser("userTwo");
			BeCPGTestHelper.createUser("userThree");

			NodeRef userOneRef = personService.getPerson("userOne");
			NodeRef userTwoRef = personService.getPerson("userTwo");
			NodeRef userThreeRef = personService.getPerson("userThree");

			nodeService.setProperty(userOneRef, fr.becpg.model.BeCPGModel.PROP_EMAIL_TASK_OBSERVER_DISABLED, true);
			nodeService.setProperty(userTwoRef, fr.becpg.model.BeCPGModel.PROP_EMAIL_TASK_OBSERVER_DISABLED, true);
			nodeService.setProperty(userThreeRef, fr.becpg.model.BeCPGModel.PROP_EMAIL_TASK_OBSERVER_DISABLED, true);

			permissionService.setPermission(getTestFolderNodeRef(), "userOne", PermissionService.COORDINATOR, true);
			permissionService.setPermission(getTestFolderNodeRef(), "userTwo", PermissionService.COORDINATOR, true);
			permissionService.setPermission(getTestFolderNodeRef(), "userThree", PermissionService.COORDINATOR, true);

			String groupName = PermissionService.GROUP_PREFIX + GROUP_WIZARD_SECURITY_WRITE;
			if (!authorityService.authorityExists(groupName)) {
				authorityService.createAuthority(AuthorityType.GROUP, GROUP_WIZARD_SECURITY_WRITE);
			}
			Set<String> groupUsers = authorityService.getContainedAuthorities(AuthorityType.USER, groupName, false);
			if (!groupUsers.contains("userOne")) {
				authorityService.addAuthority(groupName, "userOne");
			}

			// Add users to ExternalUser group to test SupplierSecurityPlugin
			String externalGroupName = PermissionService.GROUP_PREFIX + "ExternalUser";
			if (!authorityService.authorityExists(externalGroupName)) {
				authorityService.createAuthority(AuthorityType.GROUP, "ExternalUser");
			}
			Set<String> externalGroupUsers = authorityService.getContainedAuthorities(AuthorityType.USER, externalGroupName, false);
			if (!externalGroupUsers.contains("userOne")) {
				authorityService.addAuthority(externalGroupName, "userOne");
			}
			if (!externalGroupUsers.contains("userTwo")) {
				authorityService.addAuthority(externalGroupName, "userTwo");
			}
			if (!externalGroupUsers.contains("userThree")) {
				authorityService.addAuthority(externalGroupName, "userThree");
			}

			// Create test project
			projectNodeRef = createTestProject();

			// Create a supplier
			NodeRef supplierNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(BeCPGModel.BECPG_URI, "testSupplier"), PLMModel.TYPE_SUPPLIER).getChildRef();
			nodeService.setProperty(supplierNodeRef, BeCPGModel.PROP_CODE, "SUP001");

			// Link project to supplier
			nodeService.createAssociation(projectNodeRef, supplierNodeRef, PLMModel.ASSOC_SUPPLIERS);

			// Link userThree to supplier
			nodeService.createAssociation(supplierNodeRef, userThreeRef, PLMModel.ASSOC_SUPPLIER_ACCOUNTS);

			NodeRef aclGroupNodeRef = createProjectSecurityACLGroup();
			if (!nodeService.hasAspect(projectNodeRef, SecurityModel.ASPECT_SECURITY)) {
				nodeService.addAspect(projectNodeRef, SecurityModel.ASPECT_SECURITY, null);
			}
			nodeService.createAssociation(projectNodeRef, aclGroupNodeRef, SecurityModel.ASSOC_SECURITY_REF);
			securityService.refreshAcls();

			return null;
		});
	}

	private NodeRef createProjectSecurityACLGroup() {
		String groupName = PermissionService.GROUP_PREFIX + GROUP_WIZARD_SECURITY_WRITE;
		if (!authorityService.authorityExists(groupName)) {
			authorityService.createAuthority(AuthorityType.GROUP, GROUP_WIZARD_SECURITY_WRITE);
		}

		ACLGroupData aclGroupData = new ACLGroupData();
		aclGroupData.setName("Wizard Security ACL");
		aclGroupData.setNodeType(ProjectModel.TYPE_PROJECT.toPrefixString(namespaceService));

		List<NodeRef> writeGroups = new ArrayList<>();
		writeGroups.add(authorityService.getAuthorityNodeRef(groupName));

		List<ACLEntryDataItem> acls = new ArrayList<>();
		acls.add(new ACLEntryDataItem("cm:name", PermissionModel.READ_WRITE, writeGroups));
		aclGroupData.setAcls(acls);

		return alfrescoRepository.create(getTestFolderNodeRef(), aclGroupData).getNodeRef();
	}

	/**
	 * Test 1: User with task assignment should see fields in edit mode in wizard
	 */
	@Test
	public void testWizardSecurityWithTaskAssignment() {
		logger.info("=== Test 1: Wizard security with task assignment ===");

		inWriteTx(() -> {
			// Start workflow and assign task to userOne
			startProjectWorkflow(projectNodeRef, "userOne");
			assignTaskToUser("userOne");

			// Test as userOne (has task assignment)
			authenticationComponent.setCurrentUser("userOne");
			
			// Test form with skipSecurityRules (simulating wizard call)
			testFormFieldProtection(projectNodeRef, true, false, "userOne should see fields in edit mode");

			return null;
		});
	}

	/**
	 * Test 2: User without task assignment should see fields in read-only mode in wizard
	 */
	@Test
	public void testWizardSecurityWithoutTaskAssignment() {
		logger.info("=== Test 2: Wizard security without task assignment ===");

		inWriteTx(() -> {
			// Start workflow and assign task to userTwo (not userThree)
			startProjectWorkflow(projectNodeRef, "userTwo");
			assignTaskToUser("userTwo");

			// Test as userThree (no task assignment)
			authenticationComponent.setCurrentUser("userThree");
			
			// Test form with skipSecurityRules (simulating wizard call)
			testFormFieldProtection(projectNodeRef, true, false, "userThree should see fields in edit mode");

			return null;
		});
	}

	/**
	 * Test 3: User with task assignment should see fields in edit mode outside wizard
	 */
	@Test
	public void testNormalFormWithTaskAssignment() {
		logger.info("=== Test 3: Normal form with task assignment ===");

		inWriteTx(() -> {
			// Start workflow and assign task to userOne
			startProjectWorkflow(projectNodeRef, "userOne");
			assignTaskToUser("userOne");

			// Test as userOne (has task assignment)
			authenticationComponent.setCurrentUser("userOne");
			
			// Test form without skipSecurityRules (normal form call)
			testFormFieldProtection(projectNodeRef, false, false, "userOne should see fields in edit mode outside wizard");

			return null;
		});
	}

	/**
	 * Test 4: User without task assignment should see fields in read-only mode outside wizard
	 */
	@Test
	public void testNormalFormWithoutTaskAssignment() {
		logger.info("=== Test 4: Normal form without task assignment ===");

		inWriteTx(() -> {
			// Start workflow and assign task to userTwo (not userThree)
			startProjectWorkflow(projectNodeRef, "userTwo");
			assignTaskToUser("userTwo");

			// Test as userThree (no task assignment)
			authenticationComponent.setCurrentUser("userThree");
			
			// Test form without skipSecurityRules (normal form call)
			testFormFieldProtection(projectNodeRef, false, true, "userThree should see fields in read-only mode outside wizard");

			return null;
		});
	}

	/**
	 * Test 5: SecurityContextHelper should be properly cleared
	 */
	@Test
	public void testSecurityContextHelperCleanup() {
		logger.info("=== Test 5: SecurityContextHelper cleanup ===");

		inWriteTx(() -> {
			// Start workflow and assign task to userOne
			startProjectWorkflow(projectNodeRef, "userOne");
			assignTaskToUser("userOne");

			// Test as userOne
			authenticationComponent.setCurrentUser("userOne");
			
			// Verify SecurityContextHelper is initially false
			Assert.assertFalse("SecurityContextHelper should be initially false", 
				SecurityContextHelper.skipSecurityRules());

			// Test form with skipSecurityRules
			testFormFieldProtection(projectNodeRef, true, false, "userOne should see fields in edit mode");

			// Verify SecurityContextHelper is cleared after the call
			Assert.assertFalse("SecurityContextHelper should be cleared after form processing", 
				SecurityContextHelper.skipSecurityRules());

			return null;
		});
	}

	/**
	 * Test 6: Test EntitySecurityWebScript with skipSecurityRules
	 */
	@Test
	public void testEntitySecurityWebScript() throws Exception {
		logger.info("=== Test 6: EntitySecurityWebScript with skipSecurityRules ===");

		inWriteTx(() -> {
			startProjectWorkflow(projectNodeRef, "userOne");
			assignTaskToUser("userOne");
			return null;
		});

		Response userOneResponse = callEntitySecurityCheck(projectNodeRef, true, true, "userOne", "PWD");
		JSONObject jsonUserOne = new JSONObject(userOneResponse.getContentAsString());
		Assert.assertTrue("userOne should have assigned task", jsonUserOne.getBoolean("hasAssignedTask"));
		Assert.assertEquals("userOne should have WRITE access", SecurityService.WRITE_ACCESS, jsonUserOne.getInt("accessMode"));
		userOneResponse.release();

		Response userThreeResponse = callEntitySecurityCheck(projectNodeRef, true, true, "userThree", "PWD");
		JSONObject jsonUserThree = new JSONObject(userThreeResponse.getContentAsString());
		Assert.assertFalse("userThree should not have assigned task", jsonUserThree.getBoolean("hasAssignedTask"));
		Assert.assertEquals("userThree should have READ access", SecurityService.READ_ACCESS, jsonUserThree.getInt("accessMode"));
		userThreeResponse.release();
	}

	/**
	 * Helper method to create a test project
	 */
	private NodeRef createTestProject() {
		ProjectData project = new ProjectData();
		project.setName("TestProject");
		project.setCode("TP001");
		project.setState("Planned");
		project.setStartDate(new Date());

		return alfrescoRepository.create(getTestFolderNodeRef(), project).getNodeRef();
	}

	/**
	 * Helper method to start project workflow
	 */
	private void startProjectWorkflow(NodeRef projectNodeRef, String startedByUser) {
		authenticationComponent.setCurrentUser(startedByUser);

		WorkflowDefinition wfDef = workflowService.getDefinitionByName("activiti$projectAdhoc");
		Assert.assertNotNull("Workflow definition should exist", wfDef);

		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(WorkflowModel.PROP_DESCRIPTION, "Test project workflow");
		Serializable workflowPackage = workflowService.createPackage(null);
		properties.put(WorkflowModel.ASSOC_PACKAGE, workflowPackage);

		ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(projectNodeRef);
		nodeService.addChild((NodeRef) workflowPackage, projectNodeRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS, parentAssoc.getQName());

		WorkflowPath path = workflowService.startWorkflow(wfDef.getId(), properties);
		Assert.assertNotNull("Workflow path should not be null", path);
		workflowInstanceId = path.getInstance().getId();
		Assert.assertNotNull("Workflow instance should be started", workflowInstanceId);

		WorkflowTask startTask = workflowService.getStartTask(workflowInstanceId);
		if (startTask != null) {
			workflowService.endTask(startTask.getId(), null);
		}
	}

	/**
	 * Recursively finds a field definition in a merged form definition.
	 *
	 * @param jsonRoot the merged definition root
	 * @param fieldIds the acceptable field ids
	 * @return the field definition or null if not found
	 */
	private JSONObject findFieldDefinition(Object jsonRoot, List<String> fieldIds) {
		if (jsonRoot instanceof JSONObject) {
			JSONObject jsonObject = (JSONObject) jsonRoot;
			String id = jsonObject.optString("id", null);
			if (id != null && fieldIds.contains(id)) {
				return jsonObject;
			}
			for (String key : jsonObject.keySet()) {
				Object child = jsonObject.opt(key);
				JSONObject found = findFieldDefinition(child, fieldIds);
				if (found != null) {
					return found;
				}
			}
			return null;
		}
		if (jsonRoot instanceof JSONArray) {
			JSONArray array = (JSONArray) jsonRoot;
			for (int i = 0; i < array.length(); i++) {
				Object child = array.opt(i);
				JSONObject found = findFieldDefinition(child, fieldIds);
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}

	/**
	 * Helper method to assign task to user
	 */
	private void assignTaskToUser(String userName) {
		WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
		taskQuery.setProcessId(workflowInstanceId);
		taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);

		List<WorkflowTask> workflowTasks = workflowService.queryTasks(taskQuery, false);
		Assert.assertTrue("Should have at least one task in progress", !workflowTasks.isEmpty());

		WorkflowTask task = workflowTasks.get(0);
		Map<QName, Serializable> taskProp = new HashMap<>();
		taskProp.put(ContentModel.PROP_OWNER, userName);
		workflowService.updateTask(task.getId(), taskProp, null, null);
	}

	/**
	 * Helper method to test form field protection
	 */
	private void testFormFieldProtection(NodeRef entityNodeRef, boolean skipSecurityRules,
			boolean shouldHaveProtectedFields, String message) {
		
		// Set security context if needed
		if (skipSecurityRules) {
			SecurityContextHelper.setSkipSecurityRules(true);
		}

		try {
			List<String> fields = new ArrayList<>();
			fields.add("cm:name");
			fr.becpg.repo.form.impl.BecpgFormDefinition formDef = becpgFormService.getForm("node",
					entityNodeRef.toString(), null, null, fields, null, entityNodeRef);
			Assert.assertNotNull("Form definition should not be null", formDef);

			JSONObject mergeDef = formDef.getMergeDef();
			Assert.assertNotNull("Merge definition should not be null", mergeDef);
			Assert.assertTrue("Merge definition should contain fields", mergeDef.has("fields"));

			JSONObject field = findFieldDefinition(mergeDef, List.of("cm:name", "prop_cm_name"));
			Assert.assertNotNull("Field cm:name should exist in form definition", field);
			boolean isProtected = field.has("protectedField") && field.getBoolean("protectedField");
			Assert.assertEquals(message + " - protectedField flag mismatch on cm:name", shouldHaveProtectedFields, isProtected);

		} finally {
			// Always clear the security context
			SecurityContextHelper.clear();
		}
	}

	/**
	 * Helper method to test entity security check
	 */
	private Response callEntitySecurityCheck(NodeRef entityNodeRef, boolean checkTaskAssignment, boolean skipSecurityRules,
			String username, String password) throws Exception {
		String storeType = entityNodeRef.getStoreRef().getProtocol();
		String storeId = entityNodeRef.getStoreRef().getIdentifier();
		String id = entityNodeRef.getId();

		String uri = "/becpg/security/entitylists/check/" + storeType + "/" + storeId + "/" + id
				+ "?checkTaskAssignment=" + checkTaskAssignment + "&skipSecurityRules=" + skipSecurityRules;

		GetRequest request = new GetRequest(uri);
		return TestWebscriptExecuters.sendRequest(request, 200, username, password);
	}

	@Override
	@After
	public void tearDown() throws Exception {
		// Clean up workflow if running
		if (workflowInstanceId != null) {
			try {
				inWriteTx(() -> {
					workflowService.cancelWorkflow(workflowInstanceId);
					return null;
				});
			} catch (Exception e) {
				logger.warn("Could not cancel workflow: " + e.getMessage());
			}
		}

		// Clean up test data
		inWriteTx(() -> {
			if (projectNodeRef != null) {
				nodeService.deleteNode(projectNodeRef);
			}
			return null;
		});

		super.tearDown();
	}
}
