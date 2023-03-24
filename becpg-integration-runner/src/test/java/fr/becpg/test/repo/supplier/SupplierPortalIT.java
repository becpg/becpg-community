package fr.becpg.test.repo.supplier;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
import fr.becpg.model.SystemGroup;
import fr.becpg.repo.admin.SupplierPortalInitRepoVisitor;
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
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.supplier.SupplierPortalService;
import fr.becpg.test.PLMBaseTestCase;

public class SupplierPortalIT extends PLMBaseTestCase {
		@Autowired
		private SupplierPortalService supplierPortalService;
		
		@Autowired
		private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
		
		@Autowired
		private AssociationService associationService;
		
		@Autowired
		private SiteService siteService;
		
		@Autowired
		private EntityReportService entityReportService;
		
		
		@Test
		public void testSupplierPortal() throws InterruptedException {
			
			
			final NodeRef supplierNodeRef = inWriteTx(() -> {
				
				SupplierData supplier = new SupplierData();
				supplier.setName("Junit Supplier test");
				supplier.setParentNodeRef(getTestFolderNodeRef());
				
				return alfrescoRepository.save(supplier).getNodeRef();
				
			});
			

			assertNotNull(supplierNodeRef);
		

			 
			 final NodeRef supplierNodeRef2 = inWriteTx(() -> {
					
					SupplierData supplier = new SupplierData();
					supplier.setName("Junit Supplier Plant test");
					supplier.setParentNodeRef(getTestFolderNodeRef());
					
					return alfrescoRepository.save(supplier).getNodeRef();
					
				});
			 
			 
			
				
				NodeRef supplierAccountNodeRef  = inWriteTx(() -> {
		
					NodeRef personNodeRef = supplierPortalService.createExternalUser("sample-supplier@becpg.fr", "Junit", "Supplier", false, null);
		
					associationService.update(supplierNodeRef, PLMModel.ASSOC_SUPPLIER_ACCOUNTS, Arrays.asList(personNodeRef));
					
					associationService.update(supplierNodeRef2, BeCPGModel.ASSOC_PARENT_ENTITY, Arrays.asList(supplierNodeRef));
					
					return personNodeRef;
				 });

				
				 assertNotNull(supplierAccountNodeRef);
				 
			 
			final NodeRef supplierRMNodeRef = inWriteTx(() -> {
				
				RawMaterialData rawMaterial = new RawMaterialData();
				rawMaterial.setName("Junit Supplier portal MP test");
				rawMaterial.setSuppliers(Arrays.asList(supplierNodeRef));
				rawMaterial.setParentNodeRef(getTestFolderNodeRef());
				
				return alfrescoRepository.save(rawMaterial).getNodeRef();
				
			});
			
			assertNotNull(supplierRMNodeRef);
			
			
			NodeRef projectNodeRef = inWriteTx(() -> {
				return	supplierPortalService.createSupplierProject(supplierRMNodeRef,
						BeCPGQueryBuilder.createQuery().ofType(ProjectModel.TYPE_PROJECT).withAspect(BeCPGModel.ASPECT_ENTITY_TPL)
						.withAspect(PLMModel.ASPECT_SUPPLIERS)
						.andPropEquals(ContentModel.PROP_NAME, 	I18NUtil.getMessage(SupplierPortalInitRepoVisitor.SUPPLIER_PJT_TPL_NAME)).inDB().singleValue(), Arrays.asList(supplierAccountNodeRef));
	
			});
			
			assertNotNull(projectNodeRef);
			
			NodeRef projectNodeRef2 = inWriteTx(() -> {
				return	supplierPortalService.createSupplierProject(supplierNodeRef,
						BeCPGQueryBuilder.createQuery().ofType(ProjectModel.TYPE_PROJECT).withAspect(BeCPGModel.ASPECT_ENTITY_TPL)
						.withAspect(PLMModel.ASPECT_SUPPLIERS)
						.andPropEquals(ContentModel.PROP_NAME, 	I18NUtil.getMessage(SupplierPortalInitRepoVisitor.SUPPLIER_PJT_TPL_NAME)).inDB().singleValue(), Arrays.asList(supplierAccountNodeRef));
	
			});
			
			assertNotNull(projectNodeRef2);
			
			
			NodeRef projectNodeRef3 = inWriteTx(() -> {
				return	supplierPortalService.createSupplierProject(supplierNodeRef2,
						BeCPGQueryBuilder.createQuery().ofType(ProjectModel.TYPE_PROJECT).withAspect(BeCPGModel.ASPECT_ENTITY_TPL)
						.withAspect(PLMModel.ASPECT_SUPPLIERS)
						.andPropEquals(ContentModel.PROP_NAME, 	I18NUtil.getMessage(SupplierPortalInitRepoVisitor.SUPPLIER_PJT_TPL_NAME)).inDB().singleValue(), Arrays.asList(supplierAccountNodeRef));
	
			});
			
			assertNotNull(projectNodeRef3);
			
			
			
			inWriteTx(() -> {
				
				assertTrue(authorityService.getAuthoritiesForUser("sample-supplier@becpg.fr")
				.contains(PermissionService.GROUP_PREFIX + SystemGroup.ExternalUser.toString()));
				
				assertEquals(supplierPortalService.getSupplierNodeRef(supplierNodeRef),supplierNodeRef);
				assertEquals(supplierPortalService.getSupplierNodeRef(supplierRMNodeRef),supplierNodeRef);

				SiteInfo siteInfo  = siteService.getSite(supplierNodeRef);
				assertNotNull(siteInfo);
				assertEquals(SupplierPortalHelper.SUPPLIER_SITE_ID, siteInfo.getShortName());
				
				
				return true;
				
			});
			
			// complete referencing task
			inWriteTx(() -> {
			
				ProjectData project = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
				
				for (TaskListDataItem task : project.getTaskList()) {
					if (TaskState.InProgress.equals(task.getTaskState())) {
						task.setTaskState(TaskState.Completed);
						break;
					}
				}
				
				return alfrescoRepository.save(project);
				
			});
			
			// complete validation task
			inWriteTx(() -> {
				
				ProjectData project = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
				
				for (TaskListDataItem task : project.getTaskList()) {
					if (TaskState.InProgress.equals(task.getTaskState())) {
						task.setTaskState(TaskState.Completed);
						break;
					}
				}
				
				return alfrescoRepository.save(project);
				
			});
			
			
			inWriteTx(() -> {
				
				NodeRef projectEntityNodeRef = associationService.getTargetAssoc(projectNodeRef, ProjectModel.ASSOC_PROJECT_ENTITY);
				
				assertNotNull(projectEntityNodeRef);
				
				assertNotSame(supplierRMNodeRef.toString(), projectEntityNodeRef.toString());
				
				String supplierDocumentsName = TranslateHelper.getTranslatedPath("SupplierDocuments");
				
				NodeRef supplierDocumentsNodeRef = nodeService.getChildByName(projectEntityNodeRef, ContentModel.ASSOC_CONTAINS, supplierDocumentsName);
				
				assertNotNull(supplierDocumentsNodeRef);
				
				List<NodeRef> reports = entityReportService.getOrRefreshReportsOfKind(projectEntityNodeRef, "SupplierSheet");
				
				assertNotNull(reports);
				
				assertFalse(reports.isEmpty());
				
				String reportName = (String) nodeService.getProperty(reports.get(0), ContentModel.PROP_NAME);

				int lastDotIndex = reportName.lastIndexOf(".");

				if (lastDotIndex != -1) {
					String nameWithoutExtension = reportName.substring(0, lastDotIndex);
					String extension = reportName.substring(lastDotIndex);
					String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).substring(0, 10);
					reportName = nameWithoutExtension + " - " + date + extension;
				}
				
				NodeRef signedReport = nodeService.getChildByName(supplierDocumentsNodeRef, ContentModel.ASSOC_CONTAINS, reportName);
				
				assertNotNull(signedReport);
				
				ProjectData project = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
				
				int numberOfDel = 0;
				int inProgress = 0;
				int planned = 0;
				
				for (DeliverableListDataItem del : project.getDeliverableList()) {
					
					if (signedReport.equals(del.getContent())) {
						numberOfDel++;
						if (DeliverableState.InProgress.equals(del.getState())) {
							inProgress++;
						} else if (DeliverableState.Planned.equals(del.getState())) {
							planned++;
						}
					}
				}
				
				assertEquals(1, numberOfDel);
				assertEquals(1, inProgress);
				assertEquals(0, planned);
				
				return true;
				
			});
			
		}
			

}
