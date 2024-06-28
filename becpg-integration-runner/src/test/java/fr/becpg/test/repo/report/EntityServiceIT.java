package fr.becpg.test.repo.report;

import java.util.Iterator;

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

			NodeRef tempFPNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					ContentModel.ASSOC_CONTAINS, PLMModel.TYPE_FINISHEDPRODUCT).getChildRef();

			assertNotNull(entityService.getEntityDefaultIcon(tempRMNodeRef, "thumb"));
			assertNotNull(entityService.getEntityDefaultIcon(tempFPNodeRef, "thumb"));

			assertNull(entityService.getEntityDefaultIcon(tempRMNodeRef, "16"));
			assertNull(entityService.getEntityDefaultIcon(tempFPNodeRef, "16"));

			return null;
		});

		inReadTx(() -> {
			assertNotNull(entityService.getEntityIcons());
			return null;
		});
	}

	@Test
	public void testExtractImage() {

		assertTrue(extractImage("beCPG/birt/productImage2.jpg"));
		assertTrue(extractImage("beCPG/birt/productImage.jpg"));

	}

	private boolean extractImage(final String path) {

		return inWriteTx(() -> {

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

		});

	}

	@Test
	public void testExecute() throws Exception {
		Response response = TestWebscriptExecuters.sendRequest(new GetRequest("/becpg/entity/icons.css"), 200, "admin");
		assertNotNull(response);
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
