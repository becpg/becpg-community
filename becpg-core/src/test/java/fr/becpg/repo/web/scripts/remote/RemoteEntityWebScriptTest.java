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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PutRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

import fr.becpg.repo.entity.EntityService;

/**
 * The Class RemoteEntityWebScriptTest.
 * 
 * @author matthieu
 */
public class RemoteEntityWebScriptTest extends fr.becpg.test.BaseWebScriptTest {

	private static Log logger = LogFactory.getLog(RemoteEntityWebScriptTest.class);
	
	@Autowired
	private EntityService entityService;

	@Test
	public void testCRUDEntity() throws Exception {

		// Call webscript on raw material
		String url = "/becpg/remote/entity";

		Resource res = new ClassPathResource("beCPG/remote/entity.xml");
		Resource data = new ClassPathResource("beCPG/remote/data.xml");

		Response response = sendRequest(new PutRequest(url, convertStreamToString(res.getInputStream()), "application/xml"), 200, "admin");
		logger.info("Resp : " + response.getContentAsString());

		 NodeRef nodeRef = parseNodeRef(response.getContentAsString());
		
		logger.info("Name : " + nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
		logger.info("Path : " + nodeService.getPath(nodeRef).toPrefixString(serviceRegistry.getNamespaceService()));
		
		Assert.assertTrue(nodeService.exists(nodeRef));

		NodeRef imageNodeRef = entityService.getImageFolder(nodeRef);
		
		Assert.assertNotNull(imageNodeRef);
		Assert.assertEquals(0, fileFolderService.list(imageNodeRef).size());
		
		response = sendRequest(new PostRequest(url + "/data?nodeRef=" + nodeRef.toString(), convertStreamToString(data.getInputStream()), "application/xml"), 200, "admin");
		logger.info("Resp : " + response.getContentAsString());
		
		for(FileInfo file : fileFolderService.list(nodeRef)){
			logger.info(file.getName());
			for(FileInfo file2 : fileFolderService.list(file.getNodeRef())){
				logger.info("-- "+file2.getName());
			}
		}
		
		
		Assert.assertEquals(1, fileFolderService.list(imageNodeRef).size());

	}

	private NodeRef parseNodeRef(String contentAsString) {
		return new NodeRef(contentAsString);
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
