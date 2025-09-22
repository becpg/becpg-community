package fr.becpg.test.repo.supplier;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.model.ReportModel;
import fr.becpg.model.SystemGroup;
import fr.becpg.repo.admin.SupplierPortalInitRepoVisitor;
import fr.becpg.repo.authentication.UserAlreadyExistsException;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.jscript.SupplierPortalHelper;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.SupplierData;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.DeliverableState;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.signature.SignatureProjectHelper;
import fr.becpg.repo.supplier.SupplierPortalService;
import fr.becpg.test.PLMBaseTestCase;

public class SupplierPortalIT extends PLMBaseTestCase {

	// Constants
	private static final String TEST_SUPPLIER_NAME = "Junit Supplier test";
	private static final String TEST_SUPPLIER_PLANT_NAME = "Junit Supplier Plant test";
	private static final String TEST_SUPPLIER_EMAIL = "sample-supplier@becpg.fr";
	private static final String TEST_SUPPLIER_FIRST_NAME = "Junit";
	private static final String TEST_SUPPLIER_LAST_NAME = "Supplier";
	private static final String TEST_RAW_MATERIAL_NAME = "Junit Supplier portal MP test";

	@Autowired
	private SupplierPortalService supplierPortalService;

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	@Autowired
	private AssociationService associationService;

	@Autowired
	private SiteService siteService;

	@Autowired
	private SignatureProjectHelper signatureProjectHelper;

	@Test
	public void testSupplierPortal() {
		// Setup test data
		NodeRef supplierNodeRef = createSupplier(TEST_SUPPLIER_NAME);
		NodeRef supplierNodeRef2 = createSupplier(TEST_SUPPLIER_PLANT_NAME);
		NodeRef supplierAccountNodeRef = createSupplierAccount(supplierNodeRef, supplierNodeRef2);
		NodeRef supplierRMNodeRef = createRawMaterial(supplierNodeRef);

		// Create projects
		NodeRef projectNodeRef = createSupplierProject(supplierRMNodeRef, supplierAccountNodeRef);
		NodeRef projectNodeRef2 = createSupplierProject(supplierNodeRef, supplierAccountNodeRef);
		NodeRef projectNodeRef3 = createSupplierProject(supplierNodeRef2, supplierAccountNodeRef);

		// Verify initial setup
		verifyInitialSetup(supplierNodeRef, supplierRMNodeRef);

		// Test project workflow
		testProjectWorkflow(projectNodeRef);
		testProjectWorkflow(projectNodeRef2);
		testProjectWorkflow(projectNodeRef3);
	}

	private NodeRef createSupplier(String supplierName) {
		return inWriteTx(() -> {
			SupplierData supplier = new SupplierData();
			supplier.setName(supplierName);
			supplier.setParentNodeRef(getTestFolderNodeRef());
			return alfrescoRepository.save(supplier).getNodeRef();
		});
	}

	private NodeRef createSupplierAccount(NodeRef supplierNodeRef, NodeRef supplierNodeRef2) {
		return inWriteTx(() -> {
			try {
				NodeRef personNodeRef = supplierPortalService.createExternalUser(TEST_SUPPLIER_EMAIL, TEST_SUPPLIER_FIRST_NAME,
						TEST_SUPPLIER_LAST_NAME, false, null);

				associationService.update(supplierNodeRef, PLMModel.ASSOC_SUPPLIER_ACCOUNTS, Arrays.asList(personNodeRef));
				associationService.update(supplierNodeRef2, BeCPGModel.ASSOC_PARENT_ENTITY, Arrays.asList(supplierNodeRef));
				return personNodeRef;
			} catch (UserAlreadyExistsException e) {
				return personService.getPerson(TEST_SUPPLIER_EMAIL);
			}

		});
	}

	private NodeRef createRawMaterial(NodeRef supplierNodeRef) {
		return inWriteTx(() -> {
			RawMaterialData rawMaterial = new RawMaterialData();
			rawMaterial.setName(TEST_RAW_MATERIAL_NAME);
			rawMaterial.setSuppliers(Arrays.asList(supplierNodeRef));
			rawMaterial.setParentNodeRef(getTestFolderNodeRef());
			return alfrescoRepository.save(rawMaterial).getNodeRef();
		});
	}

	private NodeRef createSupplierProject(NodeRef entityNodeRef, NodeRef supplierAccountNodeRef) {
		return inWriteTx(() -> {
			NodeRef templateNodeRef = getSupplierProjectTemplate();
			return supplierPortalService.createSupplierProject(entityNodeRef, templateNodeRef, Arrays.asList(supplierAccountNodeRef));
		});
	}

	private NodeRef getSupplierProjectTemplate() {
		return BeCPGQueryBuilder.createQuery().ofType(ProjectModel.TYPE_PROJECT).withAspect(BeCPGModel.ASPECT_ENTITY_TPL)
				.withAspect(PLMModel.ASPECT_SUPPLIERS)
				.andPropEquals(ContentModel.PROP_NAME, I18NUtil.getMessage(SupplierPortalInitRepoVisitor.SUPPLIER_PJT_TPL_NAME)).inDB().singleValue();
	}

	private void verifyInitialSetup(NodeRef supplierNodeRef, NodeRef supplierRMNodeRef) {
		inWriteTx(() -> {
			verifyUserAuthorities();
			verifySupplierNodeRefs(supplierNodeRef, supplierRMNodeRef);
			verifySiteInfo(supplierNodeRef);
			return true;
		});
	}

	private void verifyUserAuthorities() {
		assertTrue("User should have external user authority", authorityService.getAuthoritiesForUser(TEST_SUPPLIER_EMAIL)
				.contains(PermissionService.GROUP_PREFIX + SystemGroup.ExternalUser.toString()));
	}

	private void verifySupplierNodeRefs(NodeRef supplierNodeRef, NodeRef supplierRMNodeRef) {
		assertEquals("Supplier node ref should match", supplierPortalService.getSupplierNodeRef(supplierNodeRef), supplierNodeRef);
		assertEquals("Raw material supplier node ref should match", supplierPortalService.getSupplierNodeRef(supplierRMNodeRef), supplierNodeRef);
	}

	private void verifySiteInfo(NodeRef supplierNodeRef) {
		SiteInfo siteInfo = siteService.getSite(supplierNodeRef);
		assertNotNull("Site info should not be null", siteInfo);
		assertEquals("Site short name should match", SupplierPortalHelper.SUPPLIER_SITE_ID, siteInfo.getShortName());
	}

	private void testProjectWorkflow(NodeRef projectNodeRef) {
		// Complete referencing task
		ProjectData project = completeInProgressTask(projectNodeRef);
		verifyProjectState(project, "completing referencing task", 7, 0, 4, 1);

		// Reject validation task
		project = rejectInProgressTask(projectNodeRef);
		verifyProjectState(project, "rejecting validation task",  7, 1, 5, 1);

		// Complete referencing task again after rejection
		project = completeInProgressTask(projectNodeRef);
		verifyProjectState(project, "completing referencing task again after rejection", 7, 0, 4, 1);

		project = completeInProgressTask(projectNodeRef);
		verifyProjectState(project, "completing validation task ", 7, 2, 1, 1);

		project = completeInProgressTask(projectNodeRef);
		verifyProjectState(project, "completing signature task ", 7, 1, 0, 1);


		// Complete validation task
		project = completeInProgressTask(projectNodeRef);
		verifyFinalProjectState(project, projectNodeRef);
	}

	private ProjectData completeInProgressTask(NodeRef projectNodeRef) {
		return updateTaskState(projectNodeRef, TaskState.Completed);
	}

	private ProjectData rejectInProgressTask(NodeRef projectNodeRef) {
		return updateTaskState(projectNodeRef, TaskState.Refused);
	}

	private ProjectData updateTaskState(NodeRef projectNodeRef, TaskState newState) {
		return inWriteTx(() -> {
			ProjectData proj = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			for (TaskListDataItem task : proj.getTaskList()) {
				if (TaskState.InProgress.equals(task.getTaskState())) {
					task.setTaskState(newState);
					break;
				}
			}

			return (ProjectData) alfrescoRepository.save(proj);
		});
	}

	private void verifyFinalProjectState(ProjectData project, NodeRef projectNodeRef) {
		inReadTx(() -> {
			verifyProjectState(project, "completing validation task", 7, 0, 0, 1);
			verifyCleanupWorked(projectNodeRef);
			return null;
		});
	}

	private void verifyCleanupWorked(NodeRef projectNodeRef) {
		NodeRef projectEntityNodeRef = associationService.getTargetAssoc(projectNodeRef, ProjectModel.ASSOC_PROJECT_ENTITY);
		String supplierDocumentsName = TranslateHelper.getTranslatedPath("SupplierDocuments");
		NodeRef supplierDocumentsNodeRef = nodeService.getChildByName(projectEntityNodeRef, ContentModel.ASSOC_CONTAINS, supplierDocumentsName);

		List<NodeRef> allDocs = associationService.getChildAssocs(supplierDocumentsNodeRef, ContentModel.ASSOC_CONTAINS);
		long reportCount = allDocs.stream().filter(doc -> nodeService.hasAspect(doc, ReportModel.ASPECT_REPORT_TEMPLATES) && !nodeService.hasAspect(doc,ContentModel.ASPECT_WORKING_COPY)).count();

		assertEquals("Should have exactly one report after cleanup and re-creation", 1, reportCount);
	}

	private void verifyProjectState(ProjectData project, String testName, int expectedDeliverables, int expectedInProgress, int expectedPlanned,
			int expectedReports) {

		inReadTx(() -> {
			verifyReports(project, testName, expectedReports);
			verifyDeliverables(project, testName, expectedDeliverables, expectedInProgress, expectedPlanned);
			return null;
		});
	}

	private void verifyReports(ProjectData project, String testName, int expectedReports) {
			Map<NodeRef, List<DeliverableListDataItem>> deliverableByDocuments = signatureProjectHelper.getDeliverableByDocuments(project);
	
			List<NodeRef> reports = deliverableByDocuments.keySet().stream()
					.filter(doc -> nodeService.exists(doc) && nodeService.hasAspect(doc, ReportModel.ASPECT_REPORT_TEMPLATES))
					.toList();
	
			assertNotNull("Reports should not be null for " + testName, reports);
			assertEquals("Expected " + expectedReports + " reports for " + testName, expectedReports, reports.size());
		}

	private void verifyDeliverables(ProjectData project, String testName, int expectedDeliverables, int expectedInProgress, int expectedPlanned) {

		DeliverableStats stats = calculateDeliverableStats(project);

		assertEquals("Expected " + expectedDeliverables + " deliverables for " + testName, expectedDeliverables, stats.totalDeliverables);
		assertEquals("Expected " + expectedInProgress + " in-progress deliverables for " + testName, expectedInProgress, stats.inProgress);
		assertEquals("Expected " + expectedPlanned + " planned deliverables for " + testName, expectedPlanned, stats.planned);
	}

	private DeliverableStats calculateDeliverableStats(ProjectData project) {
		int totalDeliverables = 0;
		int inProgress = 0;
		int planned = 0;

		for (DeliverableListDataItem del : project.getDeliverableList()) {
				totalDeliverables++;
				if (DeliverableState.InProgress.equals(del.getState())) {
					inProgress++;
				} else if (DeliverableState.Planned.equals(del.getState())) {
					planned++;
				}
		}

		return new DeliverableStats(totalDeliverables, inProgress, planned);
	}

	private static class DeliverableStats {
		final int totalDeliverables;
		final int inProgress;
		final int planned;

		DeliverableStats(int totalDeliverables, int inProgress, int planned) {
			this.totalDeliverables = totalDeliverables;
			this.inProgress = inProgress;
			this.planned = planned;
		}
	}
}