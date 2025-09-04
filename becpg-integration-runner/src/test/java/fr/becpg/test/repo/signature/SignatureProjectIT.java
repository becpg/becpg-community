package fr.becpg.test.repo.signature;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import fr.becpg.artworks.signature.model.SignatureModel;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.DeliverableState;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.repo.signature.SignatureProjectService;
import fr.becpg.test.BeCPGTestHelper;
import fr.becpg.test.RepoBaseTestCase;

public class SignatureProjectIT extends RepoBaseTestCase {

	@Autowired
	private MimetypeService mimetypeService;
	
	@Autowired
	private SignatureProjectService signatureProjectService;
	
	@Autowired
	private LockService lockService;
	
	@Autowired
	private EntityReportService entityReportService;
	
	@Test
	public void testSingleDocumentSignature() throws IOException {
		
		NodeRef userOne = inWriteTx(() -> BeCPGTestHelper.createUser(BeCPGTestHelper.USER_ONE));
		
		NodeRef documentNodeRef = inWriteTx(() -> createNodeWithContent(getTestFolderNodeRef(), "sample_1.pdf", "beCPG/signature/sample_1.pdf"));
		
		NodeRef projectNodeRef = inWriteTx(() -> {
			ProjectData projectData = new ProjectData(null, "JUnit SignatureService Project test", null, null, new Date(), null, null, null, 2,
					ProjectState.InProgress, null, 0, null);
			
			projectData.setParentNodeRef(getTestFolderNodeRef());
			
			projectData = (ProjectData) alfrescoRepository.save(projectData);
			
			return projectData.getNodeRef();
		});
		
		inWriteTx(() -> nodeService.createAssociation(projectNodeRef, userOne, SignatureModel.ASSOC_RECIPIENTS));
		
		inWriteTx(() -> signatureProjectService.prepareSignatureProject(projectNodeRef, Arrays.asList(documentNodeRef)));
		
		inReadTx(() -> {
			
			assertTrue(lockService.isLocked(documentNodeRef));
			
			ProjectData project = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
			
			int numberOfDel = 0;
			int inProgress = 0;
			int planned = 0;
			
			for (DeliverableListDataItem del : project.getDeliverableList()) {
				
				if (documentNodeRef.equals(del.getContent())) {
					numberOfDel++;
					if (DeliverableState.InProgress.equals(del.getState())) {
						inProgress++;
					} else if (DeliverableState.Planned.equals(del.getState())) {
						planned++;
					}
				}
			}
			
			assertEquals(3, numberOfDel);
			assertEquals(1, inProgress);
			assertEquals(2, planned);
			
			return null;
		});
	
	}
	
	@Test
	public void testMultipleDocumentsSignature() throws IOException {
		
		NodeRef userOne = inWriteTx(() -> BeCPGTestHelper.createUser(BeCPGTestHelper.USER_ONE));
		NodeRef userTwo = inWriteTx(() -> BeCPGTestHelper.createUser(BeCPGTestHelper.USER_TWO));
		
		NodeRef document1NodeRef = inWriteTx(() -> createNodeWithContent(getTestFolderNodeRef(), "sample_1.pdf", "beCPG/signature/sample_1.pdf"));
		NodeRef document2NodeRef = inWriteTx(() -> createNodeWithContent(getTestFolderNodeRef(), "sample_2.pdf", "beCPG/signature/sample_2.pdf"));
		
		NodeRef projectNodeRef = inWriteTx(() -> {
			ProjectData projectData = new ProjectData(null, "JUnit SignatureService Project test", null, null, new Date(), null, null, null, 2,
					ProjectState.InProgress, null, 0, null);
			
			projectData.setParentNodeRef(getTestFolderNodeRef());
			
			projectData = (ProjectData) alfrescoRepository.save(projectData);
			
			return projectData.getNodeRef();
		});
		
		inWriteTx(() -> nodeService.createAssociation(projectNodeRef, userOne, SignatureModel.ASSOC_RECIPIENTS));
		inWriteTx(() -> nodeService.createAssociation(projectNodeRef, userTwo, SignatureModel.ASSOC_RECIPIENTS));
		
		inWriteTx(() -> signatureProjectService.prepareSignatureProject(projectNodeRef, Arrays.asList(document1NodeRef, document2NodeRef)));
		
		inReadTx(() -> {
			
			assertTrue(lockService.isLocked(document1NodeRef));
			
			ProjectData project = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
			
			int numberOfDel = 0;
			int inProgress = 0;
			int planned = 0;
			
			for (DeliverableListDataItem del : project.getDeliverableList()) {
				
				if (document1NodeRef.equals(del.getContent())) {
					numberOfDel++;
					if (DeliverableState.InProgress.equals(del.getState())) {
						inProgress++;
					} else if (DeliverableState.Planned.equals(del.getState())) {
						planned++;
					}
				}
			}
			
			assertEquals(4, numberOfDel);
			assertEquals(1, inProgress);
			assertEquals(3, planned);
			
			numberOfDel = 0;
			inProgress = 0;
			planned = 0;
			
			for (DeliverableListDataItem del : project.getDeliverableList()) {
				
				if (document2NodeRef.equals(del.getContent())) {
					numberOfDel++;
					if (DeliverableState.InProgress.equals(del.getState())) {
						inProgress++;
					} else if (DeliverableState.Planned.equals(del.getState())) {
						planned++;
					}
				}
			}
			
			assertEquals(4, numberOfDel);
			assertEquals(1, inProgress);
			assertEquals(3, planned);
			
			return null;
		});
	
	}
	
	@Test
	public void testReportSignature() throws IOException {
		
		NodeRef userOne = inWriteTx(() -> BeCPGTestHelper.createUser(BeCPGTestHelper.USER_ONE));
		
		NodeRef projectNodeRef = inWriteTx(() -> {
			ProjectData projectData = new ProjectData(null, "JUnit SignatureService Project test", null, null, new Date(), null, null, null, 2,
					ProjectState.InProgress, null, 0, null);
			
			projectData.setParentNodeRef(getTestFolderNodeRef());
			
			projectData = (ProjectData) alfrescoRepository.save(projectData);
			
			return projectData.getNodeRef();
		});
		
		inWriteTx(() -> nodeService.createAssociation(projectNodeRef, userOne, SignatureModel.ASSOC_RECIPIENTS));
		
		final NodeRef rawMaterialNodeRef = inWriteTx(() -> {
			
			RawMaterialData rawMaterial = new RawMaterialData();
			rawMaterial.setName("Junit SignatureService MP test");
			rawMaterial.setParentNodeRef(getTestFolderNodeRef());
			
			return alfrescoRepository.save(rawMaterial).getNodeRef();
			
		});
		
		NodeRef reportNodeRef = inWriteTx(() -> entityReportService.getOrRefreshReport(rawMaterialNodeRef, null));
		
		assertNotNull(reportNodeRef);
		
		inWriteTx(() -> signatureProjectService.prepareSignatureProject(projectNodeRef, Arrays.asList(reportNodeRef)));
		
		inReadTx(() -> {
			
			assertFalse(lockService.isLocked(reportNodeRef));
			
			String signedReportsName = TranslateHelper.getTranslatedPath("SignedReports");
			
			NodeRef signedReportsFolder = nodeService.getChildByName(rawMaterialNodeRef, ContentModel.ASSOC_CONTAINS, signedReportsName);
			
			assertNotNull(signedReportsFolder);
			
			String reportName = (String) nodeService.getProperty(reportNodeRef, ContentModel.PROP_NAME);

			int lastDotIndex = reportName.lastIndexOf(".");

			if (lastDotIndex != -1) {
				String nameWithoutExtension = reportName.substring(0, lastDotIndex);
				String extension = reportName.substring(lastDotIndex);
				String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).substring(0, 10);
				reportName = nameWithoutExtension + " - " + date + extension;
			}
			
			NodeRef signedReport = nodeService.getChildByName(signedReportsFolder, ContentModel.ASSOC_CONTAINS, reportName);
			
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
			
			assertEquals(3, numberOfDel);
			assertEquals(1, inProgress);
			assertEquals(2, planned);
			
			return null;
		});
		
	}
	
	private NodeRef createNodeWithContent(NodeRef parent, String name, String resourceLocation) throws IOException {
		NodeRef contentNode = nodeService.createNode(parent, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_CONTENT).getChildRef();
       
        nodeService.setProperty(contentNode, ContentModel.PROP_NAME, name);
        
        ClassPathResource resource = new ClassPathResource(resourceLocation);
        
		ContentWriter contentWriter = contentService.getWriter(contentNode, ContentModel.PROP_CONTENT, true);
		contentWriter.setEncoding("UTF-8");
		contentWriter.setMimetype(mimetypeService.guessMimetype(name, resource.getInputStream()));
		
		contentWriter.putContent(resource.getInputStream());
		
		return contentNode;
	}
	
}
