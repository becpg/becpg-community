/*
 *
 */
package fr.becpg.test.repo.web.scripts.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.test.BeCPGPLMTestHelper;
import fr.becpg.test.PLMBaseTestCase;
import fr.becpg.test.utils.TestWebscriptExecuters;
import fr.becpg.test.utils.TestWebscriptExecuters.GetRequest;
import fr.becpg.test.utils.TestWebscriptExecuters.PostRequest;
import fr.becpg.test.utils.TestWebscriptExecuters.Response;

/**
 * The Class VersionHistoryWebScriptTest.
 *
 * @author querephi
 */
public class EntityVersionWebScriptIT extends PLMBaseTestCase {

	@Autowired
	private EntityVersionService entityVersionService;

	private static final Log logger = LogFactory.getLog(EntityVersionWebScriptIT.class);

	@Test
	public void testGetVersionHistory() throws Exception {

		final NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper()
				.doInTransaction(() -> BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test report"), false, true);

		if (!nodeService.hasAspect(rawMaterialNodeRef, ContentModel.ASPECT_VERSIONABLE)) {
			transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
				logger.debug("Add versionnable aspect");
				Map<QName, Serializable> aspectProperties = new HashMap<>();
				aspectProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
				nodeService.addAspect(rawMaterialNodeRef, ContentModel.ASPECT_VERSIONABLE, aspectProperties);
				return rawMaterialNodeRef;
			}, false, true);

		}

		final NodeRef checkedOutNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			
			NodeRef destNodeRef = nodeService.getPrimaryParent(rawMaterialNodeRef).getParentRef();
			return entityVersionService.createBranch(rawMaterialNodeRef, destNodeRef);
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			NodeRef checkedInNodeRef = entityVersionService.mergeBranch(checkedOutNodeRef, rawMaterialNodeRef, VersionType.MAJOR, "This is a test version");

			NodeRef destNodeRef = nodeService.getPrimaryParent(checkedInNodeRef).getParentRef();
			NodeRef checkedOutNodeRef2 = entityVersionService.createBranch(checkedInNodeRef, destNodeRef);

			entityVersionService.mergeBranch(checkedOutNodeRef2, checkedInNodeRef, VersionType.MAJOR, "This is a test version");

			return null;

		}, false, true);

		// Call webscript on raw material to check out
		String url = "/api/version?nodeRef=" + rawMaterialNodeRef;
		logger.debug("url : " + url);

		Response response = TestWebscriptExecuters.sendRequest(new GetRequest(url), 200, "admin");
		logger.debug("version history: " + response.getContentAsString());

	}
	
	@Test
	public void testCreateAndMergeBranchProduct() throws Exception {

		final NodeRef rawMaterialNodeRef = createRawMaterial("Raw material test - testCreateAndMergeBranchProduct");

		String url = "/becpg/entity/simulation/create?entityNodeRef=" + rawMaterialNodeRef;
		String data = "{}";
		logger.debug("url : " + url);

		Response response = TestWebscriptExecuters.sendRequest(new PostRequest(url, data, "application/json"), 200, "admin");
		logger.debug("content checkout: " + response.getContentAsString());

		NodeRef workingCopyNodeRef = getWorkingCopy(response.getContentAsString());

		Assert.assertNotNull("working copy should exist", workingCopyNodeRef);
		
		assertTrue(nodeService.exists(workingCopyNodeRef));

		JSONObject object = new JSONObject();
		
		object.put("nodeRef", workingCopyNodeRef.toString());
		object.put("description", "");
		object.put("majorVersion", "false");
		object.put("version", "1.0");
		object.put("branchToNodeRef", rawMaterialNodeRef.toString());
		
		data = object.toString();
		
		url = "/becpg/entity/form-checkin";
		logger.debug("url : " + url);

		response = TestWebscriptExecuters.sendRequest(new PostRequest(url, data, "application/json"), 200, "admin");
		logger.debug("content checkin: " + response.getContentAsString());
		
		JSONObject json = new JSONObject(response.getContentAsString());
		
		NodeRef nodeRef = new NodeRef((String) ((JSONObject) ((JSONArray) json.get("results")).get(0)).get("nodeRef"));

		assertTrue(nodeService.exists(nodeRef));

	}
	
	private NodeRef createRawMaterial(final String name) {
		return transactionService.getRetryingTransactionHelper()
				.doInTransaction(() -> BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), name), false, true);
	}
	
	private NodeRef getWorkingCopy(String resp) throws JSONException {

		JSONObject json = new JSONObject(resp);

		return new NodeRef((String) json.get("persistedObject"));

	}

}
