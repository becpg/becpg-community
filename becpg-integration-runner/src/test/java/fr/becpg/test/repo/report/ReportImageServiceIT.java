package fr.becpg.test.repo.report;

import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import fr.becpg.repo.entity.EntityService;
import fr.becpg.test.RepoBaseTestCase;

public class ReportImageServiceIT extends RepoBaseTestCase {

	private Log logger = LogFactory.getLog(ReportImageServiceIT.class);

	@Autowired
	EntityService entityService;

	@Test
	public void testExtractImage() {

		assertTrue(extractImage("beCPG/birt/productImage2.jpg"));
		assertTrue(extractImage("beCPG/birt/productImage.jpg"));

	}

	private boolean extractImage(final String path) {

		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			NodeRef tempImgNodeRef = nodeService
					.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_CONTENT)
					.getChildRef();

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
		//	byte[] image2Byte = twelveMonkeyExtractor(tempImgNodeRef);

		
			return (image1Byte != null); //&& (image2Byte == null);

		}, false, true);

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
