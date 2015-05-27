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
package fr.becpg.test;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BeCPGTestHelper {


	public static final String USER_ONE = "matthieuWF";
	public static final String USER_TWO = "philippeWF";
	

	
	
	private static Log logger = LogFactory.getLog(BeCPGTestHelper.class);
	
	
	
	
	
	public static void createUsers() {

		/*
		 * Matthieu : user Philippe : validators
		 */


		// USER_ONE
		NodeRef userOne = RepoBaseTestCase.INSTANCE.personService.getPerson(USER_ONE);
		if (userOne != null) {
			RepoBaseTestCase.INSTANCE.personService.deletePerson(userOne);
		}

		if (!RepoBaseTestCase.INSTANCE.authenticationDAO.userExists(USER_ONE)) {
			createUser(USER_ONE);
		}

		// USER_TWO
		NodeRef userTwo = RepoBaseTestCase.INSTANCE.personService.getPerson(USER_TWO);
		if (userTwo != null) {
			RepoBaseTestCase.INSTANCE.personService.deletePerson(userTwo);
		}

		if (!RepoBaseTestCase.INSTANCE.authenticationDAO.userExists(USER_TWO)) {
			createUser(USER_TWO);

		}

		for (String s : RepoBaseTestCase.INSTANCE.authorityService.getAuthoritiesForUser(USER_ONE)) {
			logger.debug("user in group: " + s);
		}

	}
	
	public static NodeRef createGroup(String groupName, String user){
		
		Set<String> zones = new HashSet<String>();
		zones.add(AuthorityService.ZONE_APP_DEFAULT);
		zones.add(AuthorityService.ZONE_APP_SHARE);
		zones.add(AuthorityService.ZONE_AUTH_ALFRESCO);
		
		if (!RepoBaseTestCase.INSTANCE.authorityService.authorityExists(PermissionService.GROUP_PREFIX + groupName)) {
			logger.debug("create group: " + groupName);
			RepoBaseTestCase.INSTANCE.authorityService.createAuthority(AuthorityType.GROUP, groupName, groupName, zones);
			
			RepoBaseTestCase.INSTANCE.authorityService.addAuthority(PermissionService.GROUP_PREFIX + groupName, user);
		}
		return RepoBaseTestCase.INSTANCE.authorityService.getAuthorityNodeRef(PermissionService.GROUP_PREFIX + groupName);
	}
	

	public static NodeRef createUser(String userName) {
		if (RepoBaseTestCase.INSTANCE.authenticationService.authenticationExists(userName) == false) {
			RepoBaseTestCase.INSTANCE.authenticationService.createAuthentication(userName, "PWD".toCharArray());

			PropertyMap ppOne = new PropertyMap(4);
			ppOne.put(ContentModel.PROP_USERNAME, userName);
			ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
			ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
			ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
			ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");

			return RepoBaseTestCase.INSTANCE.personService.createPerson(ppOne);
		} else {
			return RepoBaseTestCase.INSTANCE.personService.getPerson(userName);
		}
	}

	


	
	
}
