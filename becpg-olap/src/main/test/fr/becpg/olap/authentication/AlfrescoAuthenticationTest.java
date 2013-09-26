package fr.becpg.olap.authentication;

import java.io.IOException;
import java.util.Properties;

import junit.framework.TestCase;
import fr.becpg.olap.InstanceManager;
import fr.becpg.olap.http.LoginCommand;
import fr.becpg.olap.jdbc.JdbcConnectionManager;


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
