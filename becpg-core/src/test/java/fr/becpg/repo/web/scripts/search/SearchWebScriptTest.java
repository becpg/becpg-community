package fr.becpg.repo.web.scripts.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

// TODO: Auto-generated Javadoc
/**
 * The Class ExportSearchWebScriptTest.
 * 
 * @author querephi
 */
public class SearchWebScriptTest extends fr.becpg.test.BaseWebScriptTest {

	/** The logger. */
	private static Log logger = LogFactory.getLog(SearchWebScriptTest.class);

	@Test
	public void testSearch() {

		try {

			String url = "/becpg/search?site=&term=MP*&tag=&maxResults=251&sort=&query=&repo=true&metadataFields=bcpg_legalName%2Cbcpg_productHierarchy1%2Cbcpg_productHierarchy2%2Cbcpg_productState%2Cbcpg_code%2Cbcpg_erpCode%2Cbcpg_eanCode%2Cbcpg_suppliers%2Cbcpg_clients";

			Response response = sendRequest(new GetRequest(url), 200, "admin");

			logger.debug("response: " + response.getContentAsString());
		} catch (Exception e) {
			logger.error("Failed to execute webscript", e);
		}

	}

}
