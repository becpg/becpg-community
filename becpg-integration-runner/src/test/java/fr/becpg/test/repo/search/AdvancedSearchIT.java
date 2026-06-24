/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.search;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.InvalidQNameException;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.PermissionService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.json.JsonHelper;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.test.BeCPGTestHelper;
import fr.becpg.repo.search.AdvSearchQueryFilter;
import fr.becpg.repo.search.AdvSearchService;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.search.ProductAdvSearchPlugin;
import fr.becpg.test.BeCPGPLMTestHelper;
import fr.becpg.test.PLMBaseTestCase;

/**
 *
 * @author valentin
 */
public class AdvancedSearchIT extends PLMBaseTestCase {

	protected static final Log logger = LogFactory.getLog(AdvancedSearchIT.class);

	@Autowired
	protected AdvSearchService advSearchService;

	@Autowired
	protected NamespaceService namespaceService;

	@Autowired
	protected ProductAdvSearchPlugin productAdvSearchPlugin;

	@Test
	public void testAdvancedSearch() {

		NodeRef raw10 = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			NodeRef result = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP search test 1");

			RawMaterialData rawMaterial = (RawMaterialData) alfrescoRepository.findOne(result);

			rawMaterial.getCostList().get(0).setValue(10.0);

			rawMaterial = (RawMaterialData) alfrescoRepository.save(rawMaterial);

			return result;

		}, false, true);

		NodeRef raw20 = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			NodeRef result = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP search test 2");

			RawMaterialData rawMaterial = (RawMaterialData) alfrescoRepository.findOne(result);

			rawMaterial.getCostList().get(0).setValue(20.0);

			rawMaterial = (RawMaterialData) alfrescoRepository.save(rawMaterial);

			return result;

		}, false, true);

		NodeRef raw30 = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			NodeRef result = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP search test 3");

			RawMaterialData rawMaterial = (RawMaterialData) alfrescoRepository.findOne(result);

			rawMaterial.getCostList().get(0).setValue(30.0);

			rawMaterial = (RawMaterialData) alfrescoRepository.save(rawMaterial);

			return result;

		}, false, true);

		NodeRef cost0NodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			NodeRef result = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP search test 4");

			RawMaterialData rawMaterial = (RawMaterialData) alfrescoRepository.findOne(result);

			return rawMaterial.getCostList().get(0).getCost();

		}, false, true);

		waitForSolr();

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			String query = "{\"assoc_bcpg_costListCost_added\":\"" + cost0NodeRef.toString()
					+ "\",\"prop_bcpg_costListValue-range\":\"9.1|11.2\",\"datatype\":\"bcpg:rawMaterial\"}";

			List<NodeRef> results = queryAdvancedSearch(query);
			AdvSearchQueryFilter queryFilter = buildQueryFilter(query);

			assertTrue(results.contains(raw10));
			assertTrue(queryFilter.hasIncludeIds());
			assertTrue(queryFilter.getIncludeIds().contains(raw10));

			assertFalse(results.contains(raw20));
			assertFalse(queryFilter.getIncludeIds().contains(raw20));

			assertFalse(results.contains(raw30));
			assertFalse(queryFilter.getIncludeIds().contains(raw30));

			query = "{\"assoc_bcpg_costListCost_added\":\"" + cost0NodeRef.toString()
					+ "\",\"prop_bcpg_costListValue-range\":\"19.1|21.2\",\"datatype\":\"bcpg:rawMaterial\"}";

			results = queryAdvancedSearch(query);
			queryFilter = buildQueryFilter(query);

			assertFalse(results.contains(raw10));
			assertFalse(queryFilter.getIncludeIds().contains(raw10));

			assertTrue(results.contains(raw20));
			assertTrue(queryFilter.getIncludeIds().contains(raw20));

			assertFalse(results.contains(raw30));
			assertFalse(queryFilter.getIncludeIds().contains(raw30));

			query = "{\"assoc_bcpg_costListCost_added\":\"" + cost0NodeRef.toString()
					+ "\",\"prop_bcpg_costListValue-range\":\"|21.2\",\"datatype\":\"bcpg:rawMaterial\"}";

			results = queryAdvancedSearch(query);

			assertTrue(results.contains(raw10));

			assertTrue(results.contains(raw20));

			assertFalse(results.contains(raw30));

			query = "{\"assoc_bcpg_costListCost_added\":\"" + cost0NodeRef.toString()
					+ "\",\"prop_bcpg_costListValue-range\":\"15|\",\"datatype\":\"bcpg:rawMaterial\"}";

			results = queryAdvancedSearch(query);

			assertFalse(results.contains(raw10));

			assertTrue(results.contains(raw20));

			assertTrue(results.contains(raw30));

			return true;

		}, false, true);

	}

	@Test
	public void testAdvancedSearchWithMultipleIds() {
		NodeRef raw1 = inWriteTx(() -> BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP search id test 1"));
		NodeRef raw2 = inWriteTx(() -> BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP search id test 2"));
		NodeRef raw3 = inWriteTx(() -> BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP search id test 3"));

		waitForSolr();

		inReadTx(() -> {
			BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery();
			queryBuilder.andIDs(Set.of(raw1, raw2));

			List<NodeRef> results = queryBuilder.ofType(QName.createQName("bcpg:rawMaterial", namespaceService)).inDBIfPossible().list();

			assertTrue(results.contains(raw1));
			assertTrue(results.contains(raw2));
			assertFalse(results.contains(raw3));

			return null;
		});
	}

	@Test
	public void testIdFilterForcedInDb() {
		NodeRef raw1 = inWriteTx(() -> BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP db id test 1"));
		NodeRef raw2 = inWriteTx(() -> BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP db id test 2"));
		NodeRef raw3 = inWriteTx(() -> BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP db id test 3"));

		inReadTx(() -> {
			QName type = QName.createQName("bcpg:rawMaterial", namespaceService);

			// andIDs forced in DB: the DB-AFTS engine does not support the ID field, the filter is applied as a post-filter
			List<NodeRef> included = BeCPGQueryBuilder.createQuery().ofType(type).andIDs(Set.of(raw1, raw2)).inDB().list();
			assertTrue(included.contains(raw1));
			assertTrue(included.contains(raw2));
			assertFalse(included.contains(raw3));

			// single andID forced in DB
			NodeRef single = BeCPGQueryBuilder.createQuery().ofType(type).andID(raw1).inDB().singleValue();
			assertEquals(raw1, single);

			// andNotID forced in DB excludes the node (uniqueness check pattern)
			List<NodeRef> excluded = BeCPGQueryBuilder.createQuery().ofType(type).parent(getTestFolderNodeRef()).andNotID(raw1)
					.inDB().list();
			assertFalse(excluded.contains(raw1));
			assertTrue(excluded.contains(raw2));
			assertTrue(excluded.contains(raw3));

			return null;
		});
	}

	private List<NodeRef> queryAdvancedSearch(String query) throws InvalidQNameException, NamespaceException, JSONException {
		QName datatype = null;
		Map<String, String> criteriaMap = null;

		JSONObject jsonObject = new JSONObject(query);
		criteriaMap = JsonHelper.extractCriteria(jsonObject);
		datatype = QName.createQName(jsonObject.getString("datatype"), namespaceService);

		BeCPGQueryBuilder queryBuilder = advSearchService.createSearchQuery(datatype, "", "", true, "", null);

		queryBuilder.andOperator();

		return advSearchService.queryAdvSearch(datatype, queryBuilder, criteriaMap, 251);

	}

	private AdvSearchQueryFilter buildQueryFilter(String query) throws JSONException {
		JSONObject jsonObject = new JSONObject(query);
		Map<String, String> criteriaMap = JsonHelper.extractCriteria(jsonObject);
		QName datatype = QName.createQName(jsonObject.getString("datatype"), namespaceService);

		return productAdvSearchPlugin.buildQueryFilter(datatype, criteriaMap, advSearchService.getSearchConfig());
	}

	@Test
	public void testLargeSearchWithPermissions() {
		final String userName = "search_test_user_large";
		inWriteTx(() -> {
			BeCPGTestHelper.createUser(userName);
			return null;
		});

		final int totalNodes = 10000;
		final int batchSize = 1000;
		final List<NodeRef> createdNodes = new ArrayList<>(totalNodes);

//		for (int i = 0; i < totalNodes; i += batchSize) {
//			final int start = i;
//			final int end = Math.min(i + batchSize, totalNodes);
//			List<NodeRef> batchResult = inWriteTx(() -> {
//				List<NodeRef> batch = new ArrayList<>();
//				try {
//					policyBehaviourFilter.disableBehaviour();
//					for (int k = start; k < end; k++) {
//						Map<QName, Serializable> properties = new HashMap<>();
//						properties.put(ContentModel.PROP_NAME, "AdvancedSearchLargeTest_" + k);
//						properties.put(ContentModel.PROP_TITLE, "LargeTestTitle");
//						NodeRef nodeRef = nodeService.createNode(
//								getTestFolderNodeRef(),
//								ContentModel.ASSOC_CONTAINS,
//								QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "node_" + k),
//								BeCPGModel.TYPE_LIST_VALUE,
//								properties).getChildRef();
//
//						permissionService.setInheritParentPermissions(nodeRef, false);
//						batch.add(nodeRef);
//					}
//				} finally {
//					policyBehaviourFilter.enableBehaviour();
//				}
//				return batch;
//			});
//			createdNodes.addAll(batchResult);
//		}
//
//		List<NodeRef> shuffledNodes = new ArrayList<>(createdNodes);
//		Collections.shuffle(shuffledNodes);
//		List<NodeRef> selectedNodes = shuffledNodes.subList(0, 1000);
//
//		inWriteTx(() -> {
//			for (NodeRef nodeRef : selectedNodes) {
//				permissionService.setPermission(nodeRef, userName, PermissionService.READ, true);
//			}
//			return null;
//		});

		List<NodeRef> queriedNodes = inReadTx(() -> {
			return AuthenticationUtil.runAs(() -> {
				BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery()
						.ofType(BeCPGModel.TYPE_LIST_VALUE)
						.andPropEquals(ContentModel.PROP_TITLE, "LargeTestTitle")
						.maxResults(10000)
						.inDB();
				return queryBuilder.list();
			}, userName);
		});

		assertEquals(1000, queriedNodes.size());
//		assertTrue(selectedNodes.containsAll(queriedNodes));
	}

}
