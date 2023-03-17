package fr.becpg.test.repo.supplier;

import java.util.Arrays;

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
import fr.becpg.repo.jscript.SupplierPortalHelper;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.SupplierData;
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
			
			
			
			
		}
			

}
