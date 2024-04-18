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

	@Test
	public void convertToJsonTest() {

		NodeRef rawMaterialNoderef = inWriteTx(() -> {
			NodeRef result = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test report");
			return result;
		});

		inWriteTx(() -> {

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
		});
	}
}
