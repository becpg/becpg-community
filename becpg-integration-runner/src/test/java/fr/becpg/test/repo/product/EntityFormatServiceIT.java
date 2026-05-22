/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.product;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel.EntityFormat;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityFormatService;
import fr.becpg.test.BeCPGPLMTestHelper;
import fr.becpg.test.PLMBaseTestCase;

public class EntityFormatServiceIT extends PLMBaseTestCase {

	@Autowired
	private EntityFormatService entityFormatService;

	@Autowired
	private fr.becpg.repo.entity.EntityListDAO entityListDAO;

	@Autowired
	private fr.becpg.repo.entity.remote.RemoteEntityService remoteEntityService;

	@Test
	public void testGetArchivedEntityRemoteJson() {
		NodeRef rawMaterialNoderef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			NodeRef result = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test archived remote");
			return result;
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			entityFormatService.convertToFormat(rawMaterialNoderef, EntityFormat.JSON);
			
			// Verify that the datalist is absent from the node itself in DB
			NodeRef listContainer = entityListDAO.getListContainer(rawMaterialNoderef);
			if (listContainer != null) {
				NodeRef listNodeRef = entityListDAO.getList(listContainer, PLMModel.TYPE_INGLIST);
				if (listNodeRef != null) {
					java.util.List<org.alfresco.service.cmr.repository.ChildAssociationRef> childAssocs = nodeService.getChildAssocs(listNodeRef);
					assertTrue(childAssocs == null || childAssocs.isEmpty());
				}
			}

			java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
			remoteEntityService.getEntity(rawMaterialNoderef, out, new fr.becpg.repo.entity.remote.RemoteParams(fr.becpg.repo.entity.remote.RemoteEntityFormat.json));
			
			String resultJson = out.toString();
			assertNotNull(resultJson);
			
			JSONObject root = new JSONObject(resultJson);
			assertNotNull(root);
			
			JSONObject entity = root.getJSONObject("entity");
			assertNotNull(entity);
			assertEquals("MP test archived remote", entity.getString("cm:name"));

			// Verify that lists are still present in the returned JSON
			JSONObject datalists = entity.getJSONObject("datalists");
			assertNotNull(datalists);
			JSONArray ingList = datalists.getJSONArray("bcpg:" + PLMModel.TYPE_INGLIST.getLocalName());
			assertNotNull(ingList);
			assertTrue(ingList.length() > 0);

			return true;
		}, false, true);
	}

	@Test
	public void convertToJsonTest() {

		NodeRef rawMaterialNoderef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			NodeRef result = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test report");
			return result;
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			entityFormatService.convertToFormat(rawMaterialNoderef, EntityFormat.JSON);
			String format = entityFormatService.getEntityFormat(rawMaterialNoderef);
			assertEquals(EntityFormat.JSON.toString(), format);

			String entityJson = entityFormatService.getEntityData(rawMaterialNoderef);
			assertNotNull(entityJson);

			JSONObject root = new JSONObject(entityJson);
			assertNotNull(root);

			JSONObject entity = (JSONObject) root.get("entity");
			assertNotNull(entity);

			JSONObject datalists = (JSONObject) entity.get("datalists");
			assertNotNull(datalists);

			JSONArray ingList = (JSONArray) datalists.get("bcpg:" + PLMModel.TYPE_INGLIST.getLocalName());
			assertNotNull(ingList);

			return true;
		}, false, true);
	}
}
