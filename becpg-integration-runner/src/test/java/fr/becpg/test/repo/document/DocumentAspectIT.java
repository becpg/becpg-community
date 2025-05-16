package fr.becpg.test.repo.document;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.GHSModel;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.sample.CharactTestHelper;
import fr.becpg.test.PLMBaseTestCase;

public class DocumentAspectIT extends PLMBaseTestCase {

	@Autowired
	FormulationService<ProductData> formulationService;
	
	NodeRef charact1;

	@Override
	public void setUp() throws Exception {
		super.setUp();

		inWriteTx(() -> {
//			
//			Map<QName, Serializable> properties = new HashMap<>();
//			properties.put(BeCPGModel.PROP_CHARACT_NAME, hCode);
//			properties.put(GHSModel.PROP_HAZARD_CODE, hCode);
//
//			charact1 =  CharactTestHelper.getOrCreateNode(nodeService, "/app:company_home/cm:System/cm:SecurityLists/bcpg:entityLists/cm:HazardStatements", hCode,
//					GHSModel.TYPE_HAZARD_STATEMENT, properties);
//			
		



			return "SUCCESS";
		});
	}

	@Test
	public void testDocumentAspect() throws IOException {

		Date date1 = new Date();

		NodeRef documentNodeRef = inWriteTx(
				() -> createNodeWithContent(getTestFolderNodeRef(), "test_document_aspect.pdf", "beCPG/signature/sample_1.pdf"));

		
		//associationService.update("", charact1)
		
		inReadTx(() -> {

			//Assert.assertTrue(date1.getTime() < nodeService.getProperty(documentNodeRef, CM_TO));

			return true;
		});

		//inWriteTx(() -> updateContent ...)
	}

	@Test
	public void testDocumentAspectFormulation() throws IOException {
		
		
		inWriteTx(() -> {
//			FinishedProductData testProduct = FinishedProductData.build().withName("TODO");
//
//			alfrescoRepository.create(getTestFolderNodeRef(), testProduct);
//
//			formulationService.formulate(testProduct);
			return false;
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
