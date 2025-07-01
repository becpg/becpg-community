package fr.becpg.test.repo.document;

import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DocumentEffectivityType;
import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.document.DocumentTypeItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.sample.CharactTestHelper;
import fr.becpg.repo.sample.StandardChocolateEclairTestProduct;
import fr.becpg.repo.survey.data.SurveyListDataItem;
import fr.becpg.test.PLMBaseTestCase;

public class DocumentFormulationIT extends PLMBaseTestCase {

	private static final Log logger = LogFactory.getLog(DocumentFormulationIT.class);

	@Autowired
	FormulationService<ProductData> formulationService;

	@Autowired
	private AlfrescoRepository<DocumentTypeItem> documentTypeItemRepository;

	@Autowired
	private EntityService entityService;

	@Autowired
	private NamespaceService namespaceService;

	// Constants for document types linked to product labels
	private static final String CERT_KOSHER = "Kosher Certification";
	private static final String CERT_HALAL = "Halal Certification";
	private static final String CERT_ORGANIC = "Organic Certification";

	private static final String DOC_TYPE_NAME_FORMAT = "{cm:name} - {doc_bcpg:charactName}.pdf";

	@Override
	public void setUp() throws Exception {

		super.setUp();

		inWriteTx(() -> {
			createSupplierCertificationDocumentTypes();
			createClaimRelatedDocumentTypes();
			createRawMaterialDocumentTypes();
			return null;
		});
	}

	/**
	 * Creates document types for supplier certifications
	 */
	private void createSupplierCertificationDocumentTypes() {
		// ISO 9001 certification document type
		NodeRef iso9001Cert = CharactTestHelper.getOrCreateCertification(nodeService, StandardChocolateEclairTestProduct.CERT_ISO_9001,
				"ISO 9001 Quality Management System");
		NodeRef brcCert = CharactTestHelper.getOrCreateCertification(nodeService, StandardChocolateEclairTestProduct.CERT_BRC,
				"BRC Global Standard for Food Safety");
		NodeRef ifsCert = CharactTestHelper.getOrCreateCertification(nodeService, StandardChocolateEclairTestProduct.CERT_IFS,
				"International Featured Standards");

		//Test Auto expiration certification document type / no name format
		getOrCreateDocumentTypeItem(DocumentTypeItem.builder().withCharactName(StandardChocolateEclairTestProduct.CERT_ISO_9001)
				.withLinkedTypes(List.of(PLMModel.TYPE_SUPPLIER.toPrefixString(namespaceService))).withLinkedCharactRefs(List.of(iso9001Cert))
				.withIsMandatory(true).withDestPath("Certifications").withEffectivityType(DocumentEffectivityType.AUTO).withAutoExpirationDelay(365));

		//Test NONE expiration certification document type
		getOrCreateDocumentTypeItem(DocumentTypeItem.builder().withCharactName(StandardChocolateEclairTestProduct.CERT_BRC)
				.withLinkedTypes(List.of(PLMModel.TYPE_SUPPLIER.toPrefixString(namespaceService))).withLinkedCharactRefs(List.of(brcCert))
				.withIsMandatory(true).withNameFormat(DOC_TYPE_NAME_FORMAT).withDestPath("Certifications")
				.withEffectivityType(DocumentEffectivityType.NONE));

		// IFS certification document type
		getOrCreateDocumentTypeItem(DocumentTypeItem.builder().withCharactName(StandardChocolateEclairTestProduct.CERT_IFS)
				.withLinkedTypes(List.of(PLMModel.TYPE_SUPPLIER.toPrefixString(namespaceService))).withLinkedCharactRefs(List.of(ifsCert))
				.withIsMandatory(false).withNameFormat(DOC_TYPE_NAME_FORMAT).withDestPath("Certifications"));

		// Crisis contacts document type

		getOrCreateDocumentTypeItem(DocumentTypeItem.builder().withCharactName("Crisis Contacts")
				.withLinkedTypes(List.of(PLMModel.TYPE_SUPPLIER.toPrefixString(namespaceService))).withIsMandatory(true)
				.withNameFormat(DOC_TYPE_NAME_FORMAT).withEffectivityType(DocumentEffectivityType.AUTO).withAutoExpirationDelay(3 * 365));

	}

	private void getOrCreateDocumentTypeItem(DocumentTypeItem documentTypeItem) {

		documentTypeItem.setNodeRef(CharactTestHelper.getOrCreateDocumentType(nodeService, documentTypeItem.getCharactName()));

		documentTypeItemRepository.save(documentTypeItem);
	}

	/**
	 * Creates document types related to product claims (Kosher, Halal, Organic, etc.)
	 */
	private void createClaimRelatedDocumentTypes() {
		// Get claim references from CharactTestHelper
		NodeRef kosherClaim = CharactTestHelper.getOrCreateClaim(nodeService, StandardChocolateEclairTestProduct.CLAIM_KOSHER,
				StandardChocolateEclairTestProduct.CLAIM_KOSHER_LABEL);

		NodeRef halalClaim = CharactTestHelper.getOrCreateClaim(nodeService, StandardChocolateEclairTestProduct.CLAIM_HALAL,
				StandardChocolateEclairTestProduct.CLAIM_HALAL_LABEL);

		NodeRef organicClaim = CharactTestHelper.getOrCreateClaim(nodeService, StandardChocolateEclairTestProduct.CLAIM_EU_ORGANIC,
				StandardChocolateEclairTestProduct.CLAIM_EU_ORGANIC_LABEL);

		// Kosher certification document type
		DocumentTypeItem kosherDocType = DocumentTypeItem.builder().withLinkedCharactRefs(List.of(kosherClaim)).withCharactName(CERT_KOSHER)
				.withLinkedTypes(List.of(PLMModel.TYPE_RAWMATERIAL.toPrefixString(namespaceService))).withIsMandatory(false);
		getOrCreateDocumentTypeItem(kosherDocType);

		// Halal certification document type
		DocumentTypeItem halalDocType = DocumentTypeItem.builder().withLinkedCharactRefs(List.of(halalClaim)).withCharactName(CERT_HALAL)
				.withLinkedTypes(List.of(PLMModel.TYPE_RAWMATERIAL.toPrefixString(namespaceService))).withIsMandatory(true);
		getOrCreateDocumentTypeItem(halalDocType);

		// Organic certification document type
		DocumentTypeItem organicDocType = DocumentTypeItem.builder().withLinkedCharactRefs(List.of(organicClaim)).withCharactName(CERT_ORGANIC)
				.withLinkedTypes(List.of(PLMModel.TYPE_RAWMATERIAL.toPrefixString(namespaceService))).withIsMandatory(true);
		getOrCreateDocumentTypeItem(organicDocType);
	}

	/**
	 * Creates document types for raw material documents in various folders
	 */
	private void createRawMaterialDocumentTypes() {
		// Japan flavor authorization attestation
		getOrCreateDocumentTypeItem(DocumentTypeItem.builder().withCharactName("Japan Flavor Authorization")
				.withLinkedTypes(List.of(PLMModel.TYPE_RAWMATERIAL.toPrefixString(namespaceService))).withIsMandatory(false)
				.withNameFormat(DOC_TYPE_NAME_FORMAT).withDestPath("SupplierDocuments").withEffectivityType(DocumentEffectivityType.AUTO)
				.withAutoExpirationDelay(3 * 365));

		// GMO attestation (mandatory)
		DocumentTypeItem gmoDocType = DocumentTypeItem.builder().withCharactName("GMO Attestation")
				.withLinkedTypes(List.of(PLMModel.TYPE_RAWMATERIAL.toPrefixString(namespaceService)))
				.withFormula("ingList.?[isGMO == true].size() > 0").withNameFormat(DOC_TYPE_NAME_FORMAT).withDestPath("SupplierDocuments")
				.withEffectivityType(DocumentEffectivityType.AUTO).withAutoExpirationDelay(3 * 365); // 3 years
		getOrCreateDocumentTypeItem(gmoDocType);

		// Non-ionization attestation (mandatory)
		DocumentTypeItem nonIonizationDocType = DocumentTypeItem.builder().withCharactName("Non-ionization Attestation")
				.withLinkedTypes(List.of(PLMModel.TYPE_RAWMATERIAL.toPrefixString(namespaceService)))

				.withFormula("ingList.?[isIonized == false].size() > 0").withNameFormat(DOC_TYPE_NAME_FORMAT).withDestPath("SupplierDocuments")
				.withEffectivityType(DocumentEffectivityType.AUTO).withAutoExpirationDelay(3 * 365); // 3 years
		getOrCreateDocumentTypeItem(nonIonizationDocType);

		DocumentTypeItem labelCopyDocType = DocumentTypeItem.builder().withCharactName("Label Copy")
				.withLinkedTypes(List.of(PLMModel.TYPE_RAWMATERIAL.toPrefixString(namespaceService))).withIsMandatory(true)
				.withNameFormat(DOC_TYPE_NAME_FORMAT).withDestPath("SupplierDocuments").withEffectivityType(DocumentEffectivityType.AUTO)
				.withAutoExpirationDelay(3 * 365); // 3 years
		getOrCreateDocumentTypeItem(labelCopyDocType);

		getOrCreateDocumentTypeItem(DocumentTypeItem.builder().withCharactName("QMS Survey analysis results.xlsx")
				.withLinkedTypes(List.of(PLMModel.TYPE_FINISHEDPRODUCT.toPrefixString(namespaceService)))
				.withLinkedCharactRefs(List
						.of(CharactTestHelper.getOrCreateSurveyQuestion(nodeService, "Minor defects - Slight color variation, size within 17-21cm")))
				.withIsMandatory(true).withDestPath(".").withEffectivityType(DocumentEffectivityType.AUTO).withAutoExpirationDelay(3 * 365));

	}

	@Test
	public void testDocumentAspectFormulation() {
		StandardChocolateEclairTestProduct testProduct = inWriteTx(() -> {
			// Create a test product with claims, surveys, and composition
			StandardChocolateEclairTestProduct ret = new StandardChocolateEclairTestProduct.Builder().withAlfrescoRepository(alfrescoRepository)
					.withNodeService(nodeService).withDestFolder(getTestFolderNodeRef()).withCompo(true).withSurvey(true).withClaim(true).build();

			FinishedProductData product = ret.createTestProduct();
			assertNotNull("Product should be created successfully", product);

			return ret;
		});

		inWriteTx(() -> formulationService.formulate(testProduct.getProduct()));

		inWriteTx(() -> {
			// Apply document formulation to the product

			// Get key nodes for testing
			NodeRef chocolateNodeRef = testProduct.getChocolateNodeRef();

			NodeRef supplier1NodeRef = testProduct.getSugarSupplier1NodeRef(); // Supplier with EU_ORGANIC claim (100%, TRUE)
			NodeRef supplier2NodeRef = testProduct.getSugarSupplier2NodeRef(); // Supplier with KOSHER claim (75%, FALSE)
			NodeRef supplier3NodeRef = testProduct.getSugarSupplier3NodeRef(); // Supplier with HALAL claim (60%, TRUE)

			// 1. TEST RAW MATERIAL DOCUMENTS (CHOCOLATE NODE)
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
						assertNull("From date should be null", fromDate);
						assertNull("To date should be null", toDate);
					} else if (docName.contains("GMO Attestation")) {
						hasGmoDoc = true;
					} else if (docName.contains("Non-ionization Attestation")) {
						hasNonIonizationDoc = true;
					}
				}
			}

			assertTrue("Chocolate should have Label Copy document", hasLabelCopyDoc);
			logger.info("GMO document present: " + hasGmoDoc);
			logger.info("Non-ionization document present: " + hasNonIonizationDoc);

			// 2. TEST SUPPLIER CERTIFICATION DOCUMENTS FOR SUPPLIERS WITH CLAIMS
			// 2.1 Supplier 1 with EU_ORGANIC claim (100% applicable, 100% claimed, CERTIFIED)
			Map<NodeRef, NodeRef> supplier1DocumentMap = entityService.getDocumentsByType(supplier1NodeRef);
			List<NodeRef> supplier1Documents = new ArrayList<>(supplier1DocumentMap.values());
			assertFalse("Supplier 1 should have related documents", supplier1Documents.isEmpty());

			boolean hasOrganicDoc = false;
			for (NodeRef docRef : supplier1Documents) {
				String docName = (String) nodeService.getProperty(docRef, ContentModel.PROP_NAME);
				if ((docName != null) && docName.contains(CERT_ORGANIC)) {
					hasOrganicDoc = true;
					// Verify that document is mandatory (since isClaimed=TRUE)
					Boolean isMandatory = (Boolean) nodeService.getProperty(docRef, BeCPGModel.PROP_DOCUMENT_IS_MANDATORY);
					assertTrue("Organic document should be mandatory for Supplier 1", isMandatory);
				}
			}
			assertTrue("Supplier 1 should have Organic Certification document", hasOrganicDoc);

			// 2.2 Supplier 2 with KOSHER claim (75% applicable, 50% claimed, FALSE)
			Map<NodeRef, NodeRef> supplier2DocumentMap = entityService.getDocumentsByType(supplier2NodeRef);
			List<NodeRef> supplier2Documents = new ArrayList<>(supplier2DocumentMap.values());

			boolean hasKosherDoc = false;
			for (NodeRef docRef : supplier2Documents) {
				String docName = (String) nodeService.getProperty(docRef, ContentModel.PROP_NAME);
				if ((docName != null) && docName.contains(CERT_KOSHER)) {
					hasKosherDoc = true;
					// Verify that document is NOT mandatory (since isClaimed=FALSE)
					Boolean isMandatory = (Boolean) nodeService.getProperty(docRef, BeCPGModel.PROP_DOCUMENT_IS_MANDATORY);
					assertFalse("Kosher document should not be mandatory for Supplier 2", isMandatory);
				}
			}
			// Document exists but is not mandatory
			logger.info("Kosher document present for Supplier 2: " + hasKosherDoc);

			// 2.3 Supplier 3 with HALAL claim (60% applicable, 40% claimed, TRUE)
			Map<NodeRef, NodeRef> supplier3DocumentMap = entityService.getDocumentsByType(supplier3NodeRef);
			List<NodeRef> supplier3Documents = new ArrayList<>(supplier3DocumentMap.values());

			boolean hasHalalDoc = false;
			for (NodeRef docRef : supplier3Documents) {
				String docName = (String) nodeService.getProperty(docRef, ContentModel.PROP_NAME);
				if ((docName != null) && docName.contains(CERT_HALAL)) {
					hasHalalDoc = true;
				}
			}
			assertFalse("Supplier 3 should not have Halal Certification document", hasHalalDoc);

			return null;
		});

		// Test content update
		Map<NodeRef, NodeRef> productDocumentMap = inWriteTx(() -> {

			// 4. TEST DOCUMENT STATE SYNCHRONIZATION
			// Change the product state and verify that document states are synchronized

			Map<NodeRef, NodeRef> tmp = entityService.getDocumentsByType(testProduct.getProduct().getNodeRef());
			List<NodeRef> productDocuments = new ArrayList<>(tmp.values());
			assertFalse("Product should have related documents", productDocuments.isEmpty());

			// Update the content to trigger onContentUpdate policy
			for (NodeRef docRef : productDocuments) {
				String docName = (String) nodeService.getProperty(docRef, ContentModel.PROP_NAME);
				if ((docName != null) && docName.contains("QMS Survey analysis results.xlsx")) {
					ClassPathResource resource = new ClassPathResource("beCPG/signature/sample_1.pdf");
					ContentWriter contentWriter = contentService.getWriter(docRef, ContentModel.PROP_CONTENT, true);
					contentWriter.setEncoding("UTF-8");
					contentWriter.setMimetype(mimetypeService.guessMimetype("test_document_aspect.pdf", resource.getInputStream()));
					contentWriter.putContent(resource.getInputStream());
				}
			}
			return tmp;
		});

		inWriteTx(() -> {
			List<NodeRef> productDocuments = new ArrayList<>(productDocumentMap.values());
			testProduct.getProduct().setState(SystemState.Valid);
			formulationService.formulate(testProduct.getProduct()); // Re-formulate to synchronize document states

			for (NodeRef docRef : productDocuments) {
				String docState = (String) nodeService.getProperty(docRef, BeCPGModel.PROP_DOCUMENT_STATE);
				String docName = (String) nodeService.getProperty(docRef, ContentModel.PROP_NAME);
				if ((docName != null) && docName.contains("QMS Survey analysis results.xlsx")) {
					assertEquals("Document state should be synchronized with product state", SystemState.Valid.toString(), docState);
				} else {
					assertEquals("Document state should be Simulation", SystemState.Simulation.toString(), docState);
				}
			}

			// 5. TEST DOCUMENT FORMULATION WITH MANDATORY STATUS CHANGES
			// First, find the QMS Survey analysis results document
			NodeRef qmsDocument = null;
			for (NodeRef docRef : productDocuments) {
				String docName = (String) nodeService.getProperty(docRef, ContentModel.PROP_NAME);
				if ((docName != null) && docName.contains("QMS Survey analysis results.xlsx")) {
					qmsDocument = docRef;
					break;
				}
			}

			// Verify QMS document exists and is mandatory
			assertNotNull("QMS Survey analysis results.xlsx should exist", qmsDocument);
			Boolean isQmsMandatory = (Boolean) nodeService.getProperty(qmsDocument, BeCPGModel.PROP_DOCUMENT_IS_MANDATORY);
			assertTrue("QMS document should be mandatory", isQmsMandatory);

			// Store initial mandatory status
			boolean initialMandatoryStatus = (isQmsMandatory != null) && isQmsMandatory;

			// Get the survey responses from the product
			List<SurveyListDataItem> surveyResponses = testProduct.getProduct().getSurveyList();
			assertFalse("Product should have survey responses", surveyResponses.isEmpty());

			// Update the first survey response to change the mandatory status
			surveyResponses.get(0).setChoices(
					List.of(CharactTestHelper.getOrCreateSurveyQuestion(nodeService, StandardChocolateEclairTestProduct.ANSWER_PASTRY_PERFECT)));

			// Re-formulate to update document status
			formulationService.formulate(testProduct.getProduct());

			// Get the updated QMS document
			qmsDocument = null;
			Map<NodeRef, NodeRef> updatedDocumentMap = entityService.getDocumentsByType(testProduct.getProduct().getNodeRef());
			for (NodeRef docRef : updatedDocumentMap.values()) {
				String docName = (String) nodeService.getProperty(docRef, ContentModel.PROP_NAME);
				if ((docName != null) && docName.contains("QMS Survey analysis results.xlsx")) {
					qmsDocument = docRef;
					break;
				}
			}

			// Verify the document still exists
			assertNotNull("QMS document should still exist after survey update", qmsDocument);

			// Verify the mandatory status has changed
			Boolean updatedMandatoryStatus = (Boolean) nodeService.getProperty(qmsDocument, BeCPGModel.PROP_DOCUMENT_IS_MANDATORY);
			assertNotNull("Updated mandatory status should not be null", updatedMandatoryStatus);

			// Log the status change for verification
			logger.info(String.format("QMS Document mandatory status changed from %s to %s after survey update", initialMandatoryStatus,
					updatedMandatoryStatus));

			// Verify the status actually changed (this assumes the survey change should affect the mandatory status)
			assertNotEquals("Mandatory status should change after survey update", initialMandatoryStatus, updatedMandatoryStatus);

			return null;
		});
	}
}
