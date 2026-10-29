package fr.becpg.repo.supplier.security;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowPackageComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.security.SecurityService;
import fr.becpg.repo.supplier.SupplierPortalService;

/**
 * Unit tests for {@link SupplierSecurityPlugin}: a supplier may only edit an entity while one of
 * its tasks opens the supplier wizard.
 */
public class SupplierSecurityPluginTest {

	private static final String SUPPLIER_ACCOUNT = "supplier@example.com";
	private static final String WORKFLOW_ID = "activiti$1001";
	private static final String WIZARD_URL = "/share/page/wizard?id=supplier-{pjt:projectEntity|@type}&nodeRef={pjt:projectEntity}";
	private static final String SIGNATURE_URL = "/share/page/document-details?nodeRef={doc}";

	private final NodeRef entityNodeRef = new NodeRef("workspace://SpacesStore/entity");
	private final NodeRef supplierNodeRef = new NodeRef("workspace://SpacesStore/supplier");
	private final NodeRef projectNodeRef = new NodeRef("workspace://SpacesStore/project");
	private final NodeRef wizardTaskNodeRef = new NodeRef("workspace://SpacesStore/wizardTask");
	private final NodeRef signatureTaskNodeRef = new NodeRef("workspace://SpacesStore/signatureTask");

	@Mock
	private SupplierPortalService supplierPortalService;

	@Mock
	private WorkflowService workflowService;

	@Mock
	private WorkflowPackageComponent workflowPackageComponent;

	@Mock
	private AlfrescoRepository<ProjectData> alfrescoRepository;

	@Mock
	private EntityDictionaryService entityDictionaryService;

	@InjectMocks
	private SupplierSecurityPlugin supplierSecurityPlugin;

	private AutoCloseable mocks;

	@Before
	public void setUp() throws Exception {
		mocks = MockitoAnnotations.openMocks(this);
		new AuthenticationUtil().afterPropertiesSet();
		AuthenticationUtil.setFullyAuthenticatedUser(SUPPLIER_ACCOUNT);

		when(supplierPortalService.getSupplierNodeRef(entityNodeRef)).thenReturn(supplierNodeRef);
		when(supplierPortalService.isCurrentUserInSupplierGroup(supplierNodeRef)).thenReturn(true);
		when(workflowPackageComponent.getWorkflowIdsForContent(entityNodeRef)).thenReturn(List.of(WORKFLOW_ID));
		when(workflowService.getPooledTasks(anyString())).thenReturn(new ArrayList<>());
		when(alfrescoRepository.findOne(projectNodeRef)).thenReturn(projectWithDeliverables());
	}

	@After
	public void tearDown() throws Exception {
		AuthenticationUtil.clearCurrentSecurityContext();
		mocks.close();
	}

	@Test
	public void supplierCanEditDuringWizardTask() {
		assignTasks(taskOf(wizardTaskNodeRef));

		assertEquals(SecurityService.WRITE_ACCESS, supplierSecurityPlugin.getMaxAccessMode(entityNodeRef));
	}

	@Test
	public void supplierCannotEditDuringSignatureTask() {
		assignTasks(taskOf(signatureTaskNodeRef));

		assertEquals(SecurityService.READ_ACCESS, supplierSecurityPlugin.getMaxAccessMode(entityNodeRef));
	}

	@Test
	public void supplierCannotEditWhenTaskHasNoProject() {
		WorkflowTask task = taskOf(wizardTaskNodeRef);
		task.getProperties().remove(BeCPGModel.ASSOC_WORKFLOW_ENTITY);
		assignTasks(task);

		assertEquals(SecurityService.READ_ACCESS, supplierSecurityPlugin.getMaxAccessMode(entityNodeRef));
	}

	@Test
	public void supplierCanEditDuringPooledWizardTask() {
		assignTasks();
		WorkflowTask pooledTask = taskOf(wizardTaskNodeRef);
		when(workflowService.getPooledTasks(SUPPLIER_ACCOUNT)).thenReturn(List.of(pooledTask));

		assertEquals(SecurityService.WRITE_ACCESS, supplierSecurityPlugin.getMaxAccessMode(entityNodeRef));
	}

	@Test
	public void taskOnAnotherWorkflowGivesNoEditRight() {
		when(workflowPackageComponent.getWorkflowIdsForContent(entityNodeRef)).thenReturn(List.of("activiti$2002"));
		assignTasks(taskOf(wizardTaskNodeRef));

		assertEquals(SecurityService.READ_ACCESS, supplierSecurityPlugin.getMaxAccessMode(entityNodeRef));
	}

	private void assignTasks(WorkflowTask... tasks) {
		when(workflowService.getAssignedTasks(SUPPLIER_ACCOUNT, WorkflowTaskState.IN_PROGRESS)).thenReturn(List.of(tasks));
	}

	private ProjectData projectWithDeliverables() {
		ProjectData project = new ProjectData();
		List<DeliverableListDataItem> deliverables = new ArrayList<>();
		deliverables.add(deliverable(WIZARD_URL, wizardTaskNodeRef));
		deliverables.add(deliverable(SIGNATURE_URL, signatureTaskNodeRef));
		project.setDeliverableList(deliverables);
		return project;
	}

	private DeliverableListDataItem deliverable(String url, NodeRef taskNodeRef) {
		DeliverableListDataItem deliverable = new DeliverableListDataItem();
		deliverable.setUrl(url);
		deliverable.setTasks(new ArrayList<>(List.of(taskNodeRef)));
		return deliverable;
	}

	private WorkflowTask taskOf(NodeRef projectTaskNodeRef) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.ASSOC_WORKFLOW_ENTITY, projectNodeRef);
		properties.put(ProjectModel.ASSOC_WORKFLOW_TASK, projectTaskNodeRef);

		WorkflowInstance instance = mock(WorkflowInstance.class);
		when(instance.getId()).thenReturn(WORKFLOW_ID);
		WorkflowPath path = mock(WorkflowPath.class);
		when(path.getInstance()).thenReturn(instance);

		WorkflowTask task = mock(WorkflowTask.class);
		when(task.getId()).thenReturn("activiti$task");
		when(task.getPath()).thenReturn(path);
		when(task.getProperties()).thenReturn(properties);
		return task;
	}
}
