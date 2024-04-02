package fr.becpg.test.repo.report;

import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityIconService;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.test.RepoBaseTestCase;
import fr.becpg.test.utils.TestWebscriptExecuters;
import fr.becpg.test.utils.TestWebscriptExecuters.GetRequest;
import fr.becpg.test.utils.TestWebscriptExecuters.Response;

public class EntityServiceIT extends RepoBaseTestCase {

	private Log logger = LogFactory.getLog(EntityServiceIT.class);

	@Autowired
	EntityService entityService;
	
	@Autowired
	EntityIconService entityIconService;
	
	@Autowired
	FileFolderService fileFolderService;
	
	@Test
	public void testEntityIcon() {

		inWriteTx(() -> { 

			NodeRef tempRMNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					ContentModel.ASSOC_CONTAINS, PLMModel.TYPE_RAWMATERIAL).getChildRef();

			
			//getEntityDefaultIcon()			
			
			// Récuperation des entitées avec une résolution "thumb"
			assertNotNull(entityService.getEntityDefaultIcon(tempRMNodeRef, "thumb"));
			System.out.println("thumb => " + "http://localhost:8180/share/page/document-details?nodeRef=" + entityService.getEntityDefaultIcon(tempRMNodeRef, "thumb"));

			// Récuperation des entitées avec une résolution "32"
			assertNotNull(entityService.getEntityDefaultIcon(tempRMNodeRef, "32"));
			System.out.println("32 => " + "http://localhost:8180/share/page/document-details?nodeRef=" + entityService.getEntityDefaultIcon(tempRMNodeRef, "32"));
			
			// Récuperation des entitées qui n'existe pas
			assertNull(entityService.getEntityDefaultIcon(tempRMNodeRef, "16"));
			System.out.println("16 => " + entityService.getEntityDefaultIcon(tempRMNodeRef, "16"));
			
			
//			FileInfo folder = fileFolderService.getFileInfo(tempRMNodeRef);
//			ContentWriter writer = fileFolderService.getWriter(tempRMNodeRef);
//			
//			entityIconService.writeIconCSS(new FileOutputStream(tempRMNodeRef.toString()));
//			assertNotNull(writer);
//			entityIconService.writeIconCSS(writer.getContentOutputStream());
			
			return null;
		});

		inReadTx(() -> {

			//getEntityIcons()
			
			//tmp, pour me repérer
			for (Map.Entry<String, NodeRef> icon : entityService.getEntityIcons().entrySet()) {
				System.out.println(icon.getKey() + " => " + icon.getValue());
			}
			assertNotNull(entityService.getEntityIcons());
			System.out.println("getEntityIcons() null ? => " + entityService.getEntityIcons().isEmpty());

			return null;
		});
	}

	
	

	@Test
	public void testExtractImage() {

		assertTrue(extractImage("beCPG/birt/productImage2.jpg"));
		assertTrue(extractImage("beCPG/birt/productImage.jpg"));

	}

	private boolean extractImage(final String path) {

		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			NodeRef tempImgNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_CONTENT).getChildRef();

			ContentWriter writer = contentService.getWriter(tempImgNodeRef, ContentModel.PROP_CONTENT, true);
			if (writer != null) {

				ClassPathResource resource = new ClassPathResource(path);

				writer.setMimetype("image/jpeg");
				writer.putContent(resource.getInputStream());

				if (logger.isDebugEnabled()) {
					logger.debug("File successfully modified");
				}
			} else {
				logger.error("Cannot write node");
			}

			Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("JPEG");
			while (readers.hasNext()) {
				logger.info("reader: " + readers.next());
			}

			byte[] image1Byte = entityService.getImage(tempImgNodeRef);
			// byte[] image2Byte = twelveMonkeyExtractor(tempImgNodeRef);

			return (image1Byte != null); // && (image2Byte == null);

		}, false, true);

	}
		
	
//	@Before
//	public void setUp() throws Exception {
//		MockitoAnnotations.initMocks(this);
//		entityIconService = mock(EntityIconService.class);
//		webScript = new EntityIconWebScript();
//		webScript.setEntityIconService(entityIconService);
//	}
//	
//	
	@Test
	public void testExecute() throws Exception {

		//Exemple
		Response response = TestWebscriptExecuters.sendRequest(new GetRequest("/becpg/entity/icons.css"), 200, "admin");
		assertNotNull(response);
//		System.out.println("testExecute() => " + response.getContentAsString().replaceAll(";}", ";}\n\n\n")); // mise en page dans le terminal...
		System.out.println("testExecute() => " + response.getContentAsString());
		
//		
//		MockitoAnnotations.initMocks(this);
//		entityIconService = mock(EntityIconService.class);
//		webScript = new EntityIconWebScript();
//		webScript.setEntityIconService(entityIconService);
//		
//		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//		webScriptResponse = mock(WebScriptResponse.class);
//		when(webScriptResponse.getOutputStream()).thenReturn(outputStream);
//		
//		webScript.execute(null, webScriptResponse);
//		
//		String result = outputStream.toString();
//		System.out.println("testExecute() " + result);
//		assertNotNull(result);
	}
	

	
	
	// Assert twelve monkey is not used because to slow
//	private byte[] twelveMonkeyExtractor(NodeRef nodeRef) {
//		ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
//
//		if (reader != null) {
//
//			try (FastByteArrayOutputStream out = new FastByteArrayOutputStream()) {
//
//				BufferedImage image = ImageIO.read(reader.getContentInputStream());
//
//				if (image != null) {
//					ImageIO.write(image, guessImageFormat(reader.getMimetype()), out);
//					return out.toByteArrayUnsafe();
//				}
//
//			} catch (IOException e) {
//				logger.error("Failed to get the content for " + nodeRef, e);
//			}
//		}
//		return null;
//	}
//
//	private String guessImageFormat(String mimeType) {
//
//		switch (mimeType) {
//		case MimetypeMap.MIMETYPE_IMAGE_PNG:
//			return "png";
//		case MimetypeMap.MIMETYPE_IMAGE_TIFF:
//			return "tiff";
//		case MimetypeMap.MIMETYPE_IMAGE_GIF:
//			return "gif";
//		default:
//			return "jpg";
//		}
//
//	}

}
