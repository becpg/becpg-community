/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.search;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.InvalidQNameException;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.repo.helper.JsonHelper;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.search.AdvSearchService;
import fr.becpg.repo.search.BeCPGQueryBuilder;
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

	@Test
	public void testAdvancedSearch() {

		NodeRef raw10 = inWriteTx(() -> {

			NodeRef result = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP search test 1");

			RawMaterialData rawMaterial = (RawMaterialData) alfrescoRepository.findOne(result);

			rawMaterial.getCostList().get(0).setValue(10.0);

			rawMaterial = (RawMaterialData) alfrescoRepository.save(rawMaterial);

			return result;

		});

		NodeRef raw20 = inWriteTx(() -> {

			NodeRef result = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP search test 2");

			RawMaterialData rawMaterial = (RawMaterialData) alfrescoRepository.findOne(result);

			rawMaterial.getCostList().get(0).setValue(20.0);

			rawMaterial = (RawMaterialData) alfrescoRepository.save(rawMaterial);

			return result;

		});

		NodeRef raw30 = inWriteTx(() -> {

			NodeRef result = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP search test 3");

			RawMaterialData rawMaterial = (RawMaterialData) alfrescoRepository.findOne(result);

			rawMaterial.getCostList().get(0).setValue(30.0);

			rawMaterial = (RawMaterialData) alfrescoRepository.save(rawMaterial);

			return result;

		});

		NodeRef cost0NodeRef = inWriteTx(() -> {

			NodeRef result = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP search test 4");

			RawMaterialData rawMaterial = (RawMaterialData) alfrescoRepository.findOne(result);

			return rawMaterial.getCostList().get(0).getCost();

		});

		waitForSolr();

		inWriteTx(() -> {

			String query = "{\"assoc_bcpg_costListCost_added\":\"" + cost0NodeRef.toString()
					+ "\",\"prop_bcpg_costListValue-range\":\"9.1|11.2\",\"datatype\":\"bcpg:rawMaterial\"}";

			List<NodeRef> results = queryAdvancedSearch(query);

			assertTrue(results.contains(raw10));

			assertFalse(results.contains(raw20));

			assertFalse(results.contains(raw30));

			query = "{\"assoc_bcpg_costListCost_added\":\"" + cost0NodeRef.toString()
					+ "\",\"prop_bcpg_costListValue-range\":\"19.1|21.2\",\"datatype\":\"bcpg:rawMaterial\"}";

			results = queryAdvancedSearch(query);

			assertFalse(results.contains(raw10));

			assertTrue(results.contains(raw20));

			assertFalse(results.contains(raw30));

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

		});

	}

	private List<NodeRef> queryAdvancedSearch(String query)
			throws InvalidQNameException, NamespaceException, JSONException {
		QName datatype = null;
		Map<String, String> criteriaMap = null;

		JSONObject jsonObject = new JSONObject(query);
		criteriaMap = JsonHelper.extractCriteria(jsonObject);
		datatype = QName.createQName(jsonObject.getString("datatype"), namespaceService);

		BeCPGQueryBuilder queryBuilder = advSearchService.createSearchQuery(datatype, "", "", true, "", null);

		queryBuilder.andOperator();

		return advSearchService.queryAdvSearch(datatype, queryBuilder, criteriaMap, 251);

	}

}
