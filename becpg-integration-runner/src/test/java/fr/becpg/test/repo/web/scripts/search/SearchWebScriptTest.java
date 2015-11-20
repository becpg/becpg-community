/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
public class SearchWebScriptTest extends fr.becpg.test.PLMBaseTestCase {

	/** The logger. */
	private static final Log logger = LogFactory.getLog(SearchWebScriptTest.class);

	@Test
	public void testSearch() throws IOException {


			String url = "/becpg/search?site=&term=MP*&tag=&maxResults=251&sort=&query=&repo=true&metadataFields=bcpg_legalName%2Cbcpg_productHierarchy1%2Cbcpg_productHierarchy2%2Cbcpg_productState%2Cbcpg_code%2Cbcpg_erpCode%2Cbcpg_eanCode%2Cbcpg_suppliers%2Cbcpg_clients";

			Response response = TestWebscriptExecuters.sendRequest(new GetRequest(url), 200, "admin");

			logger.debug("response: " + response.getContentAsString());


	}

}
