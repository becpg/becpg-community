package fr.becpg.repo.web.scripts.entity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

import fr.becpg.test.BaseWebScriptTest;

/**
 * @author matthieu
 */
public class EntityDictionnaryWebScriptTest extends BaseWebScriptTest{

	private static Log logger = LogFactory.getLog(EntityDictionnaryWebScriptTest.class);
	
	

	@Test
	public void testEntityDictionnary() throws Exception {
		
			Response response = sendRequest(new GetRequest("/becpg/dictionnary/entity?itemType=bcpg:ing"), 200, "admin");
			String respString = response.getContentAsString();
			
			logger.info("content : " + respString);
			Assert.assertTrue(respString.contains("bcpg:ingListIng"));
			
			response = sendRequest(new GetRequest("/becpg/dictionnary/entity?assocName=assoc_bcpg_allergenListAllergen"), 200, "admin");
			respString = response.getContentAsString();
			
			logger.info("content : " + respString);
			Assert.assertTrue(respString.contains("bcpg:allergen"));
			

    }
    	
}
