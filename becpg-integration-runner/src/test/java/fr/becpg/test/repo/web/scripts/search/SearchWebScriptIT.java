/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.test.repo.web.scripts.search;

import java.io.IOException;

import org.junit.Test;

import fr.becpg.test.utils.TestWebscriptExecuters;
import fr.becpg.test.utils.TestWebscriptExecuters.GetRequest;
import fr.becpg.test.utils.TestWebscriptExecuters.Response;

// TODO: Auto-generated Javadoc
/**
 * The Class ExportSearchWebScriptTest.
 *
 * @author querephi
 */
public class SearchWebScriptIT extends fr.becpg.test.PLMBaseTestCase {



	@Test
	public void testSearch() throws IOException {

		String url = "/becpg/search?site=&term=MP*&tag=&maxResults=251&sort=&query=&repo=true&metadataFields=bcpg_legalName%2Cbcpg_productHierarchy1%2Cbcpg_productHierarchy2%2Cbcpg_productState%2Cbcpg_code%2Cbcpg_erpCode%2Cbcpg_eanCode%2Cbcpg_suppliers%2Cbcpg_clients";

		Response response = TestWebscriptExecuters.sendRequest(new GetRequest(url), 200, "admin");
		
		assertEquals(response.getStatus(),200);

	}

	@Test
	public void testSearchWithPaginationCache() throws IOException {

		String url = "/becpg/search?site=&term=MP*&tag=&maxResults=251&sort=&query=&repo=true&metadataFields=bcpg_legalName";

		Response response = TestWebscriptExecuters.sendRequest(new GetRequest(url), 200, "admin");
		assertEquals(response.getStatus(), 200);

		String responseString = response.getContentAsString();
		assertNotNull(responseString);

		org.json.JSONObject json = new org.json.JSONObject(responseString);
		assertTrue(json.has("queryExecutionId"));
		String queryExecutionId = json.getString("queryExecutionId");
		assertNotNull(queryExecutionId);
		assertFalse(queryExecutionId.isEmpty());

		// Second request passing queryExecutionId
		String urlPage2 = url + "&page=2&pageSize=50&queryExecutionId=" + queryExecutionId;
		Response responsePage2 = TestWebscriptExecuters.sendRequest(new GetRequest(urlPage2), 200, "admin");
		assertEquals(responsePage2.getStatus(), 200);

		String responsePage2String = responsePage2.getContentAsString();
		assertNotNull(responsePage2String);
		org.json.JSONObject jsonPage2 = new org.json.JSONObject(responsePage2String);
		assertEquals(jsonPage2.getString("queryExecutionId"), queryExecutionId);
	}

}
