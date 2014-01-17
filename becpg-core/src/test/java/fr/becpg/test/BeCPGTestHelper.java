package fr.becpg.test;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BeCPGTestHelper {


	public static final String USER_ONE = "matthieuWF";
	public static final String USER_TWO = "philippeWF";
	

	
	
	private static Log logger = LogFactory.getLog(BeCPGTestHelper.class);
	
	/** The PAT h_ testfolder. */
	public static String PATH_TESTFOLDER = "TestFolder";
	
	
	public static NodeRef createTestFolder() {
		return createTestFolder( PATH_TESTFOLDER);
	}


	public static NodeRef createTestFolder( String folderName) {

		NodeRef folderNodeRef = RepoBaseTestCase.INSTANCE.nodeService.getChildByName(
				RepoBaseTestCase.INSTANCE.repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, folderName);
		
		if(folderNodeRef == null){
			folderNodeRef = RepoBaseTestCase.INSTANCE.fileFolderService.create(RepoBaseTestCase.INSTANCE.repositoryHelper.getCompanyHome(),
					folderName, ContentModel.TYPE_FOLDER).getNodeRef();
		}
		
		return folderNodeRef;
	}
	
	
	
	
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
