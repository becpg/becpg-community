/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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
package fr.becpg.repo.web.scripts.entity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

import fr.becpg.test.PLMBaseWebScriptTest;

/**
 * @author matthieu
 */
public class EntityDictionnaryWebScriptTest extends PLMBaseWebScriptTest{

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
