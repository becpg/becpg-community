package fr.becpg.test.repo.report;

import java.util.Iterator;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.Assert;

import fr.becpg.repo.entity.EntityService;
import fr.becpg.test.RepoBaseTestCase;

public class ReportImageServiceTest extends RepoBaseTestCase {

	private Log logger = LogFactory.getLog(ReportImageServiceTest.class);

	@Resource
	EntityService entityService;

	@Test
	public void testExtractImage() {

		extractImage("beCPG/birt/productImage2.jpg");
		extractImage("beCPG/birt/productImage.jpg");
		

	}
	
	
	private void extractImage(final String path){
		
		Assert.notNull(transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<byte[]>() {
			public byte[] execute() throws Throwable {

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
				
				
				return entityService.getImage(tempImgNodeRef);
				

			}
		}, false, true));

		
	}

}
