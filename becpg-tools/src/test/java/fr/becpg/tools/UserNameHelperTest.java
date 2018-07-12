package fr.becpg.tools;


import org.apache.ws.commons.util.Base64;
import org.junit.Assert;
import org.junit.Test;

import fr.becpg.tools.helper.UserNameHelper;

public class UserNameHelperTest {

	@Test
	public void test() {
		
		String authToken =  getCurrentAuthToken("matthieu Laborie","45121",null);
		System.out.println(authToken);
		Assert.assertEquals("45121",UserNameHelper.extractTicket(authToken));
		System.out.println(UserNameHelper.extractUserName(authToken));
		Assert.assertEquals("matthieu Laborie",UserNameHelper.extractLogin(UserNameHelper.extractUserName(authToken)));
		
		
	}

	
	private String getCurrentAuthToken(String userName, String ticket, String instanceName) {
		String currentUserName = getCurrentOlapUserName(instanceName, userName);
		currentUserName += "#" + ticket;

		return Base64.encode(currentUserName.getBytes());
	}

	private String getCurrentOlapUserName(String instanceName, String userName) {
		String currentUserName = (instanceName != null ? instanceName : "default") + "$" + userName;
		if (!currentUserName.contains("@")) {
			currentUserName += "@default";
		}
		return currentUserName;
	}

	
}
