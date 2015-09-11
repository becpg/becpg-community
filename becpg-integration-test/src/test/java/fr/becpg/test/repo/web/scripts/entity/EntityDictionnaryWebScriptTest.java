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
package fr.becpg.test.repo.web.scripts.entity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;

import fr.becpg.test.PLMBaseTestCase;
import fr.becpg.test.utils.TestWebscriptExecuters;
import fr.becpg.test.utils.TestWebscriptExecuters.GetRequest;
import fr.becpg.test.utils.TestWebscriptExecuters.Response;

/**
 * @author matthieu
 */
public class EntityDictionnaryWebScriptTest extends PLMBaseTestCase{

	private static final Log logger = LogFactory.getLog(EntityDictionnaryWebScriptTest.class);
	
	

	@Test
	public void testEntityDictionnary() throws Exception {
		
			Response response = TestWebscriptExecuters.sendRequest(new GetRequest("/becpg/dictionnary/entity?itemType=bcpg:ing"), 200, "admin");
			String respString = response.getContentAsString();
			
			logger.info("content : " + respString);
			Assert.assertTrue(respString.contains("bcpg:ingListIng"));
			
			response = TestWebscriptExecuters.sendRequest(new GetRequest("/becpg/dictionnary/entity?assocName=assoc_bcpg_allergenListAllergen"), 200, "admin");
			respString = response.getContentAsString();
			
			logger.info("content : " + respString);
			Assert.assertTrue(respString.contains("bcpg:allergen"));
			

    }
    	
}
