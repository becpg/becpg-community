/*
 * 
 */
package fr.becpg.repo.web.scripts.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PutRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * The Class RemoteEntityWebScriptTest.
 * 
 * @author matthieu
 */
public class RemoteEntityWebScriptTest extends fr.becpg.test.BaseWebScriptTest {

	private static Log logger = LogFactory.getLog(RemoteEntityWebScriptTest.class);

	@Test
	public void testCRUDEntity() throws Exception {

		// Call webscript on raw material
		String url = "/becpg/remote/entity";

		Resource res = new ClassPathResource("beCPG/remote/entity.xml");
		Resource data = new ClassPathResource("beCPG/remote/data.xml");

		Response response = sendRequest(new PutRequest(url, convertStreamToString(res.getInputStream()), "application/xml"), 200, "admin");
		logger.debug("Resp : " + response.getContentAsString());

		final NodeRef nodeRef = parseNodeRef(response.getContentAsString());

		Assert.assertTrue(nodeService.exists(nodeRef));

		NodeRef imageNodeRef = null;
		for (FileInfo fi : fileFolderService.list(testFolderNodeRef)) {
			logger.error("Create Image Folder : " + fi.getName() + " " + fi.getType());
			imageNodeRef = nodeService.getChildByName(fi.getNodeRef(), ContentModel.ASSOC_CONTAINS, "Images");
			if (imageNodeRef != null) {
				fileFolderService.delete(imageNodeRef);
			}
			imageNodeRef = fileFolderService.create(fi.getNodeRef(), "Images", ContentModel.TYPE_FOLDER).getNodeRef();
		}

		response = sendRequest(new PostRequest(url + "/data?nodeRef=" + nodeRef.toString(), convertStreamToString(data.getInputStream()), "application/xml"), 200, "admin");
		logger.debug("Resp : " + response.getContentAsString());

		Assert.assertEquals(1, fileFolderService.list(imageNodeRef).size());

	}

	private NodeRef parseNodeRef(String contentAsString) {
		return new NodeRef(contentAsString);
	}

	public void testFormulateEntity() throws Exception {

	}

	public String convertStreamToString(InputStream is) throws IOException {
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}

}
