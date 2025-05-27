package fr.becpg.test.repo.document;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.document.DocumentTypeItem;
import fr.becpg.repo.product.data.document.DocumentTypeItem.DocumentEffectivityType;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.test.PLMBaseTestCase;

public class DocumentAspectIT extends PLMBaseTestCase {

	@Autowired
	private AlfrescoRepository<DocumentTypeItem> documentTypeItemRepository;

	@Autowired
	private AssociationService associationService;

	@Test
	public void testDocumentAspect() throws IOException {

		NodeRef charact1 = inWriteTx(() -> documentTypeItemRepository.create(getTestFolderNodeRef(), DocumentTypeItem.builder().withCharactName("Test Document Type Auto")
				.withIsMandatory(true).withEffectivityType(DocumentEffectivityType.AUTO).withAutoExpirationDelay(30)).getNodeRef());

		// Create a document with content
		Date date1 = new Date();

		NodeRef documentNodeRef = inWriteTx(() -> {
			NodeRef docRef = createNodeWithContent(getTestFolderNodeRef(), "test_document_aspect.pdf", "beCPG/signature/sample_1.pdf");

			// Apply the document aspect
			nodeService.addAspect(docRef, BeCPGModel.ASPECT_DOCUMENT_ASPECT, null);

			// Associate with document type (AUTO effectivity)
			associationService.update(docRef, BeCPGModel.ASSOC_DOCUMENT_TYPE_REF, charact1);

			return docRef;
		});

		// Verify initial property values
		inReadTx(() -> {
			// Check if from date was set
			Date fromDate = (Date) nodeService.getProperty(documentNodeRef, BeCPGModel.PROP_CM_FROM);
			assertNotNull("From date should be set", fromDate);
			assertTrue("From date should be after or equal to date1", fromDate.getTime() >= date1.getTime());

			// Check if to date was set (auto expiration)
			Date toDate = (Date) nodeService.getProperty(documentNodeRef, BeCPGModel.PROP_CM_TO);
			assertNotNull("To date should be set for AUTO effectivity", toDate);

			// Check if the to date is 30 days after the from date
			Calendar expectedToDate = Calendar.getInstance();
			expectedToDate.setTime(fromDate);
			expectedToDate.add(Calendar.DAY_OF_YEAR, 30);

			// Allow for small time differences in milliseconds
			long timeDifference = Math.abs(expectedToDate.getTimeInMillis() - toDate.getTime());
			assertTrue("To date should be approximately 30 days after from date", timeDifference < 1000);

			// Check if document state is set to ToValidate
			String docState = (String) nodeService.getProperty(documentNodeRef, BeCPGModel.PROP_DOCUMENT_STATE);
			assertEquals("Document state should be set to ToValidate", SystemState.ToValidate.toString(), docState);

			return true;
		});

		// Test content update
		inWriteTx(() -> {
			// Update the content to trigger onContentUpdate policy
			ClassPathResource resource = new ClassPathResource("beCPG/signature/sample_1.pdf");
			ContentWriter contentWriter = contentService.getWriter(documentNodeRef, ContentModel.PROP_CONTENT, true);
			contentWriter.setEncoding("UTF-8");
			contentWriter.setMimetype(mimetypeService.guessMimetype("test_document_aspect.pdf", resource.getInputStream()));
			contentWriter.putContent(resource.getInputStream());
			return null;
		});

		// Verify property values after content update
		inReadTx(() -> {
			// After content update, the dates should be updated
			Date fromDate = (Date) nodeService.getProperty(documentNodeRef, BeCPGModel.PROP_CM_FROM);
			assertNotNull("From date should still be set after update", fromDate);

			Date toDate = (Date) nodeService.getProperty(documentNodeRef, BeCPGModel.PROP_CM_TO);
			assertNotNull("To date should still be set after update", toDate);

			// Document state should be reset to ToValidate
			String docState = (String) nodeService.getProperty(documentNodeRef, BeCPGModel.PROP_DOCUMENT_STATE);
			assertEquals("Document state should be ToValidate after content update", SystemState.ToValidate.toString(), docState);

			return true;
		});
	}

	@Test
	public void testDocumentAspectNoneEffectivity() throws IOException {
		// Create a document with content and associate with NONE effectivity type
		Date date1 = new Date();

		NodeRef charact2 = inWriteTx(() -> documentTypeItemRepository.create(getTestFolderNodeRef(), DocumentTypeItem.builder().withCharactName("Test Document Type None")
				.withIsMandatory(true).withEffectivityType(DocumentEffectivityType.NONE)).getNodeRef());

		NodeRef documentNodeRef = inWriteTx(() -> {
			NodeRef docRef = createNodeWithContent(getTestFolderNodeRef(), "test_document_aspect_none.pdf", "beCPG/signature/sample_1.pdf");

			// Apply the document aspect
			nodeService.addAspect(docRef, BeCPGModel.ASPECT_DOCUMENT_ASPECT, null);

			// Associate with document type (NONE effectivity)
			associationService.update(docRef, BeCPGModel.ASSOC_DOCUMENT_TYPE_REF, charact2);

			return docRef;
		});

		// Verify initial property values
		inReadTx(() -> {
			// Check if from date was set
			Date fromDate = (Date) nodeService.getProperty(documentNodeRef, BeCPGModel.PROP_CM_FROM);
			assertNotNull("From date should be set", fromDate);
			assertTrue("From date should be after or equal to date1", fromDate.getTime() >= date1.getTime());

			// Check if to date was NOT set for NONE effectivity
			Date toDate = (Date) nodeService.getProperty(documentNodeRef, BeCPGModel.PROP_CM_TO);
			assertNull("To date should NOT be set for NONE effectivity", toDate);

			// Check if document state is set to ToValidate
			String docState = (String) nodeService.getProperty(documentNodeRef, BeCPGModel.PROP_DOCUMENT_STATE);
			assertEquals("Document state should be set to ToValidate", SystemState.ToValidate.toString(), docState);

			return true;
		});
	}

	private NodeRef createNodeWithContent(NodeRef parent, String name, String resourceLocation) throws IOException {
		NodeRef contentNode = nodeService.createNode(parent, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_CONTENT)
				.getChildRef();

		nodeService.setProperty(contentNode, ContentModel.PROP_NAME, name);

		ClassPathResource resource = new ClassPathResource(resourceLocation);

		ContentWriter contentWriter = contentService.getWriter(contentNode, ContentModel.PROP_CONTENT, true);
		contentWriter.setEncoding("UTF-8");
		contentWriter.setMimetype(mimetypeService.guessMimetype(name, resource.getInputStream()));

		contentWriter.putContent(resource.getInputStream());

		return contentNode;
	}
}
