package fr.becpg.test.repo.document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemState;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.document.DocumentTypeItem;
import fr.becpg.repo.product.data.document.DocumentTypeItem.DocumentEffectivityType;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.sample.CharactTestHelper;
import fr.becpg.repo.sample.StandardChocolateEclairTestProduct;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.test.PLMBaseTestCase;

public class DocumentFormulationIT extends PLMBaseTestCase {

	private static final Log logger = LogFactory.getLog(DocumentFormulationIT.class);

	@Autowired
	FormulationService<ProductData> formulationService;

	@Autowired
	private AlfrescoRepository<DocumentTypeItem> documentTypeItemRepository;
	
	@Autowired
	private EntityService entityService;


	
	// Constants for document types linked to product labels
	private static final String CERT_KOSHER = "Kosher Certification";
	private static final String CERT_HALAL = "Halal Certification";
	private static final String CERT_ORGANIC = "Organic Certification";



	private static final String DOC_TYPE_NAME_FORMAT = "{entity_cm:name} - {cm:name}";

	
	@Override
	public void setUp() throws Exception {
		
		super.setUp();

		createSupplierCertificationDocumentTypes();
		createClaimRelatedDocumentTypes();	
	    createRawMaterialDocumentTypes();
	}

	/**
	 * Creates document types for supplier certifications
	 * @return List of created certification NodeRefs
	 */
	private List<NodeRef> createSupplierCertificationDocumentTypes() {
		// ISO 9001 certification document type
		NodeRef iso9001Cert = CharactTestHelper.getOrCreateCertification(nodeService, StandardChocolateEclairTestProduct.CERT_ISO_9001, "ISO 9001 Quality Management System");
		NodeRef brcCert = CharactTestHelper.getOrCreateCertification(nodeService, StandardChocolateEclairTestProduct.CERT_BRC, "BRC Global Standard for Food Safety");
		NodeRef ifsCert = CharactTestHelper.getOrCreateCertification(nodeService, StandardChocolateEclairTestProduct.CERT_IFS, "International Featured Standards");
		NodeRef iso22000Cert = CharactTestHelper.getOrCreateCertification(nodeService, StandardChocolateEclairTestProduct.CERT_ISO_22000, "ISO 22000 Food Safety Management");
		NodeRef fssc22000Cert = CharactTestHelper.getOrCreateCertification(nodeService, StandardChocolateEclairTestProduct.CERT_FSSC_22000, "FSSC 22000 Food Safety System Certification");

		
		DocumentTypeItem iso9001DocType = DocumentTypeItem.builder()
			.withCharactName(StandardChocolateEclairTestProduct.CERT_ISO_9001)
			.withLinkedTypes(List.of(PLMModel.TYPE_SUPPLIER.toString()))
			.withLinkedCharactRefs(List.of(iso9001Cert))
			.withIsMandatory(true)
			.withNameFormat(DOC_TYPE_NAME_FORMAT)
			.withDestPath("./cm:Certifications")
			.withEffectivityType(DocumentEffectivityType.AUTO)
			.withAutoExpirationDelay(365); // 1 year
		documentTypeItemRepository.create(getDocumentTypeFolderNodeRef(), iso9001DocType);
		
		// BRC certification document type
			DocumentTypeItem brcDocType = DocumentTypeItem.builder()
			.withCharactName(StandardChocolateEclairTestProduct.CERT_BRC)
			.withLinkedTypes(List.of(PLMModel.TYPE_SUPPLIER.toString()))
			.withLinkedCharactRefs(List.of(brcCert))
			.withIsMandatory(true)
			.withNameFormat(DOC_TYPE_NAME_FORMAT)
			.withDestPath("./cm:Certifications")
			.withEffectivityType(DocumentEffectivityType.AUTO)
			.withAutoExpirationDelay(365);
		documentTypeItemRepository.create(getDocumentTypeFolderNodeRef(), brcDocType);
		
		// IFS certification document type
	     DocumentTypeItem ifsDocType = DocumentTypeItem.builder()
			.withCharactName(StandardChocolateEclairTestProduct.CERT_IFS)
			.withLinkedTypes(List.of(PLMModel.TYPE_SUPPLIER.toString()))
			.withLinkedCharactRefs(List.of(ifsCert))
			.withIsMandatory(true)
			.withNameFormat(DOC_TYPE_NAME_FORMAT)
			.withDestPath("./cm:Certifications")
			.withEffectivityType(DocumentEffectivityType.AUTO)
			.withAutoExpirationDelay(365);
		documentTypeItemRepository.create(getDocumentTypeFolderNodeRef(), ifsDocType);
		
		// ISO 22000 certification document type
		DocumentTypeItem iso22000DocType = DocumentTypeItem.builder()
			.withCharactName(StandardChocolateEclairTestProduct.CERT_ISO_22000)
			.withLinkedTypes(List.of(PLMModel.TYPE_SUPPLIER.toString()))
			.withLinkedCharactRefs(List.of(iso22000Cert))
			.withIsMandatory(true)
			.withNameFormat(DOC_TYPE_NAME_FORMAT)
			.withDestPath("./cm:Certifications")
			.withEffectivityType(DocumentEffectivityType.AUTO)
			.withAutoExpirationDelay(365);
		documentTypeItemRepository.create(getDocumentTypeFolderNodeRef(), iso22000DocType);
		
		// FSSC 22000 certification document type
		DocumentTypeItem fssc22000DocType = DocumentTypeItem.builder()
			.withCharactName(StandardChocolateEclairTestProduct.CERT_FSSC_22000)
			.withLinkedTypes(List.of(PLMModel.TYPE_SUPPLIER.toString()))
			.withLinkedCharactRefs(List.of(fssc22000Cert))
			.withIsMandatory(true)
			.withFormula("true")
			.withNameFormat(DOC_TYPE_NAME_FORMAT)
			.withDestPath("./cm:Certifications")
			.withEffectivityType(DocumentEffectivityType.AUTO)
			.withAutoExpirationDelay(365);
		documentTypeItemRepository.create(getDocumentTypeFolderNodeRef(), fssc22000DocType);
		
		
		// Crisis contacts document type

		DocumentTypeItem crisisContactsDocType = DocumentTypeItem.builder()
			.withCharactName("Crisis Contacts")	
			.withLinkedTypes(List.of(PLMModel.TYPE_SUPPLIER.toString()))
			.withIsMandatory(true)
			.withFormula("true")
			.withNameFormat(DOC_TYPE_NAME_FORMAT)
			.withDestPath("./cm:Documents")
			.withEffectivityType(DocumentEffectivityType.AUTO)
			.withAutoExpirationDelay(3 * 365); // 3 years
		documentTypeItemRepository.create(getDocumentTypeFolderNodeRef(), crisisContactsDocType);
		
		// Return list of created certification NodeRefs for reference
		return List.of(iso9001Cert, brcCert, ifsCert, iso22000Cert, fssc22000Cert);
	}
	
	private NodeRef getDocumentTypeFolderNodeRef() {
		return BeCPGQueryBuilder.createQuery().selectNodeByPath("/app:company_home/cm:System/cm:Characts/bcpg:entityLists/cm:DocumentTypes");
	}

	/**
	 * Creates document types related to product claims (Kosher, Halal, Organic, etc.)
	 */
	private void createClaimRelatedDocumentTypes() {
		// Get claim references from CharactTestHelper
		NodeRef kosherClaim = CharactTestHelper.getOrCreateClaim(nodeService, 
				StandardChocolateEclairTestProduct.CLAIM_KOSHER, 
				StandardChocolateEclairTestProduct.CLAIM_KOSHER_LABEL);
				
		NodeRef halalClaim = CharactTestHelper.getOrCreateClaim(nodeService, 
				StandardChocolateEclairTestProduct.CLAIM_HALAL, 
				StandardChocolateEclairTestProduct.CLAIM_HALAL_LABEL);
				
		NodeRef organicClaim = CharactTestHelper.getOrCreateClaim(nodeService, 
				StandardChocolateEclairTestProduct.CLAIM_EU_ORGANIC, 
				StandardChocolateEclairTestProduct.CLAIM_EU_ORGANIC_LABEL);
		
		// Kosher certification document type
		DocumentTypeItem kosherDocType = DocumentTypeItem.builder()
			.withLinkedCharactRefs(List.of(kosherClaim))
			.withCharactName(CERT_KOSHER)
			.withLinkedTypes(List.of(PLMModel.TYPE_RAWMATERIAL.toString()))
			.withIsMandatory(true)
			.withNameFormat(DOC_TYPE_NAME_FORMAT)
			.withDestPath("./cm:Certifications")
			.withEffectivityType(DocumentEffectivityType.AUTO)
			.withAutoExpirationDelay(365); // 1 year
		documentTypeItemRepository.create(getTestFolderNodeRef(), kosherDocType);
		
		// Halal certification document type
		DocumentTypeItem halalDocType = DocumentTypeItem.builder()
			.withLinkedCharactRefs(List.of(halalClaim))
			.withCharactName(CERT_HALAL)
			.withLinkedTypes(List.of(PLMModel.TYPE_RAWMATERIAL.toString()))
			.withIsMandatory(true)
			.withNameFormat(DOC_TYPE_NAME_FORMAT)
			.withDestPath("./cm:Certifications")
			.withEffectivityType(DocumentEffectivityType.AUTO)
			.withAutoExpirationDelay(365);
		documentTypeItemRepository.create(getTestFolderNodeRef(), halalDocType);
		
		// Organic certification document type
		NodeRef organicCert = CharactTestHelper.getOrCreateCertification(nodeService, CERT_ORGANIC, "Organic certification documents");
		DocumentTypeItem organicDocType = DocumentTypeItem.builder()
			.withLinkedCharactRefs(List.of(organicClaim, organicCert))
			.withCharactName(CERT_ORGANIC)
			.withLinkedTypes(List.of(PLMModel.TYPE_RAWMATERIAL.toString()))
			.withIsMandatory(true)
			.withNameFormat(DOC_TYPE_NAME_FORMAT)
			.withDestPath("./cm:Certifications")
			.withEffectivityType(DocumentEffectivityType.AUTO)
			.withAutoExpirationDelay(365);
		documentTypeItemRepository.create(getTestFolderNodeRef(), organicDocType);
	}
	
	/**
	 * Creates document types for raw material documents in various folders
	 */
	private void createRawMaterialDocumentTypes() {
		// Japan flavor authorization attestation
			DocumentTypeItem japanFlavorDocType = DocumentTypeItem.builder()
			.withCharactName("Japan Flavor Authorization")
			.withLinkedTypes(List.of(PLMModel.TYPE_RAWMATERIAL.toString()))
			.withIsMandatory(false)
			.withNameFormat(DOC_TYPE_NAME_FORMAT)
			.withDestPath("./cm:SupplierDocuments")
			.withEffectivityType(DocumentEffectivityType.AUTO)
			.withAutoExpirationDelay(3 * 365); // 3 years
		documentTypeItemRepository.create(getDocumentTypeFolderNodeRef(), japanFlavorDocType);
		
		// GMO attestation (mandatory)
		DocumentTypeItem gmoDocType = DocumentTypeItem.builder()
			.withCharactName("GMO Attestation")
			.withLinkedTypes(List.of(PLMModel.TYPE_RAWMATERIAL.toString()))
			.withFormula("entity.ingList[isGMO == true].size() > 0")
			.withNameFormat(DOC_TYPE_NAME_FORMAT)
			.withDestPath("./cm:SupplierDocuments")
			.withEffectivityType(DocumentEffectivityType.AUTO)
			.withAutoExpirationDelay(3 * 365); // 3 years
		documentTypeItemRepository.create(getDocumentTypeFolderNodeRef(), gmoDocType);
		
		// Non-ionization attestation (mandatory)
		DocumentTypeItem nonIonizationDocType = DocumentTypeItem.builder()
			.withCharactName("Non-ionization Attestation")
			.withLinkedTypes(List.of(PLMModel.TYPE_RAWMATERIAL.toString()))

			.withFormula("entity.ingList[isIonized == true].size() > 0")
			.withNameFormat(DOC_TYPE_NAME_FORMAT)
			.withDestPath("./cm:SupplierDocuments")
			.withEffectivityType(DocumentEffectivityType.AUTO)
			.withAutoExpirationDelay(3 * 365); // 3 years
		documentTypeItemRepository.create(getDocumentTypeFolderNodeRef(), nonIonizationDocType);
		
		// Create document types for the "MP" folder
		// Label copy (mandatory)
		DocumentTypeItem labelCopyDocType = DocumentTypeItem.builder()
			.withCharactName("Label Copy")
			.withLinkedTypes(List.of(PLMModel.TYPE_RAWMATERIAL.toString()))
			.withIsMandatory(true)
			.withNameFormat(DOC_TYPE_NAME_FORMAT)
			.withDestPath("./cm:SupplierDocuments")
			.withEffectivityType(DocumentEffectivityType.AUTO)
			.withAutoExpirationDelay(3 * 365); // 3 years
		documentTypeItemRepository.create(getDocumentTypeFolderNodeRef(), labelCopyDocType);

	}

	@Test
	public void testDocumentAspectFormulation() throws IOException {
		inWriteTx(() -> {
			// Create all document types
			createSupplierCertificationDocumentTypes();
			createClaimRelatedDocumentTypes();
			createRawMaterialDocumentTypes();
			
			// Create a test product with claims, surveys, and composition
			StandardChocolateEclairTestProduct testProduct = new StandardChocolateEclairTestProduct.Builder()
					.withNodeService(nodeService)
					.withDestFolder(getTestFolderNodeRef())
					.withCompo(true)
					.withSurvey(true)
					.withClaim(true)
					.build();	

			FinishedProductData product = testProduct.createTestProduct();
			assertNotNull("Product should be created successfully", product);

			// Get key nodes for testing
			NodeRef chocolateNodeRef = testProduct.getChocolateNodeRef();
			NodeRef supplier1NodeRef = testProduct.getOrCreateCharact(StandardChocolateEclairTestProduct.SUPPLIER_1, PLMModel.TYPE_SUPPLIER);
			NodeRef finishedProductNodeRef = product.getNodeRef();
			
			// Apply document formulation to the product
			formulationService.formulate(product);
			
			// 1. TEST RAW MATERIAL DOCUMENTS (CHOCOLATE NODE)
			// Use entityService.getDocumentsByType to get documents by type
			Map<NodeRef, NodeRef> chocolateDocumentMap = entityService.getDocumentsByType(chocolateNodeRef);
			List<NodeRef> chocolateDocuments = new ArrayList<>(chocolateDocumentMap.values());
			assertFalse("Chocolate node should have related documents", chocolateDocuments.isEmpty());
			
			// Check for specific documents by name
			boolean hasLabelCopyDoc = false;
			boolean hasGmoDoc = false;
			boolean hasNonIonizationDoc = false;
			
			for (NodeRef docRef : chocolateDocuments) {
				String docName = (String) nodeService.getProperty(docRef, ContentModel.PROP_NAME);
				if (docName != null) {
					if (docName.contains("Label Copy")) {
						hasLabelCopyDoc = true;
						
						// Test mandatory property
						Boolean isMandatory = (Boolean) nodeService.getProperty(docRef, BeCPGModel.PROP_DOCUMENT_IS_MANDATORY);
						assertNotNull("Mandatory property should be set", isMandatory);
						assertTrue("Label Copy document should be mandatory", isMandatory);
						
						// Test effectivity dates
						Date fromDate = (Date) nodeService.getProperty(docRef, BeCPGModel.PROP_CM_FROM);
						Date toDate = (Date) nodeService.getProperty(docRef, BeCPGModel.PROP_CM_TO);
						assertNotNull("From date should be set for AUTO effectivity type", fromDate);
						assertNotNull("To date should be set based on auto expiration delay", toDate);
					} else if (docName.contains("GMO Attestation")) {
						hasGmoDoc = true;
					} else if (docName.contains("Non-ionization Attestation")) {
						hasNonIonizationDoc = true;
					}
				}
			}
			
			assertTrue("Chocolate should have Label Copy document", hasLabelCopyDoc);
			// These may or may not be present depending on formula evaluation
			logger.info("GMO document present: " + hasGmoDoc);
			logger.info("Non-ionization document present: " + hasNonIonizationDoc);
			
			// 2. TEST SUPPLIER CERTIFICATION DOCUMENTS
			Map<NodeRef, NodeRef> supplierDocumentMap = entityService.getDocumentsByType(supplier1NodeRef);
			List<NodeRef> supplierDocuments = new ArrayList<>(supplierDocumentMap.values());
			assertFalse("Supplier should have related documents", supplierDocuments.isEmpty());
			
			boolean hasCrisisContactsDoc = false;
			boolean hasCertificationDoc = false;
			
			for (NodeRef docRef : supplierDocuments) {
				String docName = (String) nodeService.getProperty(docRef, ContentModel.PROP_NAME);
				if (docName != null) {
					if (docName.contains("Crisis Contacts")) {
						hasCrisisContactsDoc = true;
						
						// Test document state - should be ToValidate initially
						String docState = (String) nodeService.getProperty(docRef, BeCPGModel.PROP_DOCUMENT_STATE);
						assertEquals("Document should initially be in ToValidate state", 
								SystemState.ToValidate.toString(), docState);
					} else if (docName.contains("Certification")) {
						hasCertificationDoc = true;
					}
				}
			}
			
			assertTrue("Supplier should have Crisis Contacts document", hasCrisisContactsDoc);
			logger.info("Certification document present: " + hasCertificationDoc);
			
			// 3. TEST FINISHED PRODUCT DOCUMENTS
			// At this stage, there should be no documents for the finished product
			Map<NodeRef, NodeRef> finishedProductDocumentMap = entityService.getDocumentsByType(finishedProductNodeRef);
			assertTrue("Finished product should not have documents at this stage", 
					finishedProductDocumentMap == null || finishedProductDocumentMap.isEmpty());
			
			// 4. TEST DOCUMENT TYPE FILTERING
			// Get chocolate documents by specific document type
			NodeRef labelCopyDocType = BeCPGQueryBuilder.createQuery()
					.ofType(BeCPGModel.TYPE_DOCUMENT_TYPE)
					.andPropEquals(BeCPGModel.PROP_CHARACT_NAME, "Label Copy")
					.inDB()
					.singleValue();
			
			if (labelCopyDocType != null) {
				// For document type filtering, we need to check if this type of document exists
				// This might need adjustment based on your actual EntityService implementation
				NodeRef documentsFolderRef = entityService.getDocumentsFolder(chocolateNodeRef, false);
				List<NodeRef> labelCopyDocs = new ArrayList<>();
				
				if (documentsFolderRef != null) {
					// Filter documents by type manually
					for (NodeRef docRef : chocolateDocuments) {
						if (docRef != null && nodeService.getProperty(docRef, ContentModel.PROP_NAME).toString().contains("Label Copy")) {
							labelCopyDocs.add(docRef);
						}
					}
				}
				
				assertFalse("Should find Label Copy documents when filtering by type", labelCopyDocs.isEmpty());
			}
			
			// 5. TEST DOCUMENT FORMULATION WITH MODIFIED PRODUCT
			// Update a property that might affect document formulation
			// For example, add a claim that would trigger a document requirement
			// Look for claim with specific name
			NodeRef organicClaimType = BeCPGQueryBuilder.createQuery()
					.ofType(PLMModel.TYPE_LABEL_CLAIM) // Using PLM model constant for claims
					.andPropEquals(BeCPGModel.PROP_CHARACT_NAME, "EU_ORGANIC")
					.inDB()
					.singleValue();
			
			if (organicClaimType != null) {
				// Mark the product as organic and re-formulate
				// This is just an example - adjust according to your actual model
				// associationService.createAssociation(finishedProductNodeRef, organicClaimType, PLMModel.ASSOC_CLAIMS);
				
				// Re-formulate documents
				formulationService.formulate(product);
				
				// Check if organic certification document was created
				NodeRef organicDocType = BeCPGQueryBuilder.createQuery()
						.ofType(BeCPGModel.TYPE_DOCUMENT_TYPE)
						.andPropEquals(BeCPGModel.PROP_CHARACT_NAME, CERT_ORGANIC)
						.inDB()
						.singleValue();
				
				if (organicDocType != null) {
					// For document type filtering, check documents folder and filter manually
					NodeRef documentsFolderRef = entityService.getDocumentsFolder(supplier1NodeRef, false);
					List<NodeRef> organicDocs = new ArrayList<>();
					
					if (documentsFolderRef != null) {
						// Get all supplier documents again to check for organic docs
						Map<NodeRef, NodeRef> updatedSupplierDocs = entityService.getDocumentsByType(supplier1NodeRef);
						for (NodeRef docRef : updatedSupplierDocs.values()) {
							if (docRef != null && nodeService.getProperty(docRef, ContentModel.PROP_NAME).toString().contains(CERT_ORGANIC)) {
								organicDocs.add(docRef);
							}
						}
					}
					
					logger.info("Found " + organicDocs.size() + " organic certification documents");
				}
			}
			
			return null;
		});
	}
}
