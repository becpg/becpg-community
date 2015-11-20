/*
 *
 */
package fr.becpg.test.repo.web.scripts.remote;

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

import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.test.RepoBaseTestCase;
import fr.becpg.test.utils.TestWebscriptExecuters;
import fr.becpg.test.utils.TestWebscriptExecuters.PostRequest;
import fr.becpg.test.utils.TestWebscriptExecuters.PutRequest;
import fr.becpg.test.utils.TestWebscriptExecuters.Response;

/**
 * The Class RemoteEntityWebScriptTest.
 *
 * @author matthieu
 */
public class RemoteEntityWebScriptTest extends RepoBaseTestCase {

	private static final Log logger = LogFactory.getLog(RemoteEntityWebScriptTest.class);

	@Autowired
	private EntityService entityService;

	@Test
	public void testCRUDEntity() throws Exception {

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			NodeRef nodeRef = BeCPGQueryBuilder.createQuery().selectNodeByPath(repositoryHelper.getCompanyHome(),
					"/app:company_home/cm:Exchange/cm:Import/cm:ImportToDo/cm:Abricot_x002c__x0020_nectar_x002c__x0020_pasteurisé");
			if (nodeRef != null) {
				nodeService.deleteNode(nodeRef);
			}

			return null;
		} , false, true);

		// Call webscript on raw material
		String url = "/becpg/remote/entity";

		Resource res = new ClassPathResource("beCPG/remote/entity.xml");
		Resource data = new ClassPathResource("beCPG/remote/data.xml");

		Response response = TestWebscriptExecuters.sendRequest(new PutRequest(url, convertStreamToString(res.getInputStream()), "application/xml"),
				200, "admin");
		logger.info("Resp : " + response.getContentAsString());

		final NodeRef nodeRef = parseNodeRef(response.getContentAsString());

		logger.info("Name : " + nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));

		Assert.assertTrue(nodeService.exists(nodeRef));

		NodeRef imageNodeRef = entityService.getImageFolder(nodeRef);

		Assert.assertNotNull(imageNodeRef);
		Assert.assertEquals(0, fileFolderService.list(imageNodeRef).size());

		// create product
		final NodeRef sfNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> entityService.createDefaultImage(nodeRef),
				false, true);

		response = TestWebscriptExecuters.sendRequest(
				new PostRequest(url + "/data?nodeRef=" + sfNodeRef.toString(), convertStreamToString(data.getInputStream()), "application/xml"), 200,
				"admin");
		logger.info("Resp : " + response.getContentAsString());

		for (FileInfo file : fileFolderService.list(nodeRef)) {
			logger.info(file.getName());
			for (FileInfo file2 : fileFolderService.list(file.getNodeRef())) {
				logger.info("-- " + file2.getName());
			}
		}

		Assert.assertEquals(1, fileFolderService.list(imageNodeRef).size());

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			nodeService.deleteNode(nodeRef);
			return null;
		} , false, true);

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
