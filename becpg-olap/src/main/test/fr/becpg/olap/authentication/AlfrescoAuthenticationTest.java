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
package fr.becpg.olap.authentication;

import java.io.IOException;
import java.util.Properties;

import junit.framework.TestCase;
import fr.becpg.olap.http.LoginCommand;
import fr.becpg.tools.InstanceManager;
import fr.becpg.tools.jdbc.JdbcConnectionManager;


public class AlfrescoAuthenticationTest extends TestCase {

	
	public void testLogin() throws IOException {

		Properties props = new Properties();
			props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("jdbc.properties"));
	

		 JdbcConnectionManager jdbcConnectionManager = new JdbcConnectionManager((String) props.get("jdbc.user"), (String) props.get("jdbc.password"),
				(String) props.get("jdbc.url"));
		 InstanceManager instanceManager = new InstanceManager();
		 instanceManager.setJdbcConnectionManager(jdbcConnectionManager);
		 
		 AlfrescoUserDetailsService alfrescoUserDetailsService = new AlfrescoUserDetailsService();
		 
		 alfrescoUserDetailsService.setInstanceManager(instanceManager);
		 
		 AlfrescoUserDetails user =  (AlfrescoUserDetails) alfrescoUserDetailsService.loadUserByUsername("default$admin@default");
		
		 LoginCommand loginCommand = new LoginCommand(user.getInstance().getInstanceUrl());
		 
		 assertNull(loginCommand.getAlfTicket("admin", "pwet"));
		 
		 assertNotNull(loginCommand.getAlfTicket("admin", "becpg"));
	}

}
