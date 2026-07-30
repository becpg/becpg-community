/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.product;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel.EntityFormat;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityFormatService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.entity.remote.RemoteParams;
import fr.becpg.test.BeCPGPLMTestHelper;
import fr.becpg.test.PLMBaseTestCase;

/**
 * Integration tests for {@link EntityFormatService}.
 */
public class EntityFormatServiceIT extends PLMBaseTestCase {

	@Autowired
	private EntityFormatService entityFormatService;

	@Autowired
	private EntityListDAO entityListDAO;

	@Autowired
	private RemoteEntityService remoteEntityService;

	@Test
	public void testGetArchivedEntityRemoteJson() {
		NodeRef rawMaterialNodeRef = inWriteTx(() -> BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test archived remote"));

		inWriteTx(() -> {
			entityFormatService.convertToFormat(rawMaterialNodeRef, EntityFormat.JSON);

			NodeRef listContainer = entityListDAO.getListContainer(rawMaterialNodeRef);
			if (listContainer != null) {
				NodeRef listNodeRef = entityListDAO.getList(listContainer, PLMModel.TYPE_INGLIST);
				if (listNodeRef != null) {
					List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(listNodeRef);
					assertTrue(childAssocs == null || childAssocs.isEmpty());
				}
			}

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			remoteEntityService.getEntity(rawMaterialNodeRef, out, new RemoteParams(RemoteEntityFormat.json));

			String resultJson = out.toString();
			assertNotNull(resultJson);

			JSONObject root = new JSONObject(resultJson);
			JSONObject entity = root.getJSONObject("entity");
			assertEquals("MP test archived remote", entity.getString("cm:name"));

			JSONObject datalists = entity.getJSONObject("datalists");
			assertNotNull(datalists);
			JSONArray ingList = datalists.getJSONArray("bcpg:" + PLMModel.TYPE_INGLIST.getLocalName());
			assertNotNull(ingList);
			assertTrue(ingList.length() > 0);

			return null;
		});
	}

	@Test
	public void testGetArchivedEntityRemoteJsonWithFilters() {
		NodeRef rawMaterialNodeRef = inWriteTx(() -> BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test archived filters"));

		inWriteTx(() -> {
			entityFormatService.convertToFormat(rawMaterialNodeRef, EntityFormat.JSON);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			RemoteParams params = new RemoteParams(RemoteEntityFormat.json);

			Set<String> fields = new HashSet<>();
			fields.add("!cm:name");
			params.setFilteredFields(fields, serviceRegistry.getNamespaceService());

			Set<String> lists = new HashSet<>();
			lists.add("!" + PLMModel.TYPE_INGLIST.getLocalName());
			params.setFilteredLists(lists);

			remoteEntityService.getEntity(rawMaterialNodeRef, out, params);

			String resultJson = out.toString();
			JSONObject root = new JSONObject(resultJson);
			JSONObject entity = root.getJSONObject("entity");

			if (entity.has("attributes")) {
				JSONObject attributes = entity.getJSONObject("attributes");
				assertFalse("The name property should have been filtered out", attributes.has("cm:name"));
			}

			if (entity.has("datalists")) {
				JSONObject datalists = entity.getJSONObject("datalists");
				assertFalse("The ingredients list should have been filtered out", datalists.has("bcpg:" + PLMModel.TYPE_INGLIST.getLocalName()));
			}

			return null;
		});
	}

	@Test
	public void testGetArchivedEntityRemoteJsonWithPositiveFieldFilter() {
		NodeRef rawMaterialNodeRef = inWriteTx(() -> BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test archived positive fields"));

		inWriteTx(() -> {
			entityFormatService.convertToFormat(rawMaterialNodeRef, EntityFormat.JSON);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			RemoteParams params = new RemoteParams(RemoteEntityFormat.json);
			Set<String> fields = new HashSet<>();
			fields.add("cm:name");
			fields.add("bcpg:legalName");
			params.setFilteredFields(fields, serviceRegistry.getNamespaceService());

			remoteEntityService.getEntity(rawMaterialNodeRef, out, params);

			JSONObject root = new JSONObject(out.toString());
			JSONObject entity = root.getJSONObject("entity");

			assertEquals("MP test archived positive fields", entity.getString("cm:name"));
			if (entity.has("attributes")) {
				JSONObject attributes = entity.getJSONObject("attributes");
				assertFalse("Unrequested attribute bcpg:erpCode should be filtered out", attributes.has("bcpg:erpCode"));
			}

			return null;
		});
	}

	@Test
	public void testListArchivedEntityRemoteJsonNoDatalists() {
		NodeRef rawMaterialNodeRef = inWriteTx(() -> BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test archived list"));

		inWriteTx(() -> {
			entityFormatService.convertToFormat(rawMaterialNodeRef, EntityFormat.JSON);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			RemoteParams params = new RemoteParams(RemoteEntityFormat.json);

			PagingResults<NodeRef> pagingResults = new PagingResults<NodeRef>() {
				@Override
				public boolean hasMoreItems() {
					return false;
				}
				@Override
				public Pair<Integer, Integer> getTotalResultCount() {
					return new Pair<>(1, 1);
				}
				@Override
				public String getQueryExecutionId() {
					return null;
				}
				@Override
				public List<NodeRef> getPage() {
					return List.of(rawMaterialNodeRef);
				}
			};

			remoteEntityService.listEntities(pagingResults, out, params);

			String resultJson = out.toString();
			JSONObject root = new JSONObject(resultJson);
			JSONArray entities = root.getJSONArray("entities");
			assertEquals(1, entities.length());

			JSONObject entityObj = entities.getJSONObject(0).getJSONObject("entity");
			assertEquals("MP test archived list", entityObj.getString("cm:name"));
			assertFalse("Datalists should not be returned in entity lists", entityObj.has("datalists"));
			assertFalse("Attributes should not be returned in entity lists when no fields specified", entityObj.has("attributes"));

			return null;
		});
	}

	@Test
	public void testConvertToJson() {
		NodeRef rawMaterialNodeRef = inWriteTx(() -> BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test report"));

		inWriteTx(() -> {
			entityFormatService.convertToFormat(rawMaterialNodeRef, EntityFormat.JSON);
			String format = entityFormatService.getEntityFormat(rawMaterialNodeRef);
			assertEquals(EntityFormat.JSON.toString(), format);

			String entityJson = entityFormatService.getEntityData(rawMaterialNodeRef);
			assertNotNull(entityJson);

			JSONObject root = new JSONObject(entityJson);
			JSONObject entity = root.getJSONObject("entity");
			assertNotNull(entity);

			JSONObject datalists = entity.getJSONObject("datalists");
			assertNotNull(datalists);

			JSONArray ingList = datalists.getJSONArray("bcpg:" + PLMModel.TYPE_INGLIST.getLocalName());
			assertNotNull(ingList);

			return null;
		});
	}
}
