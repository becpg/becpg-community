package fr.becpg.repo.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SecurityModel;
import fr.becpg.repo.BeCPGDao;
import fr.becpg.repo.security.constraint.DynPropsConstraint;
import fr.becpg.repo.security.data.ACLGroupData;
import fr.becpg.repo.security.data.dataList.ACLEntryDataItem;
import fr.becpg.repo.security.data.dataList.ACLEntryDataItem.PermissionModel;
import fr.becpg.test.RepoBaseTestCase;

public class SecurityServiceTest extends RepoBaseTestCase {

	protected static final String USER_ONE = "matthieu_secu";

	protected static final String USER_TWO = "philippe_secu";

	private static String PATH_TESTFOLDER = "SecTestFolder";
	
	
	private String grp1;
	private String grp2;
	private String grp3;
	
	

	/** The logger. */
	private static Log logger = LogFactory.getLog(SecurityServiceTest.class);

	private BeCPGDao<ACLGroupData> aclGroupDao;
	private SecurityService securityService;

	private AuthorityService authorityService;

	private MutableAuthenticationDao authenticationDAO;

	private MutableAuthenticationService authenticationService;

	private PersonService personService;

	private DynPropsConstraint dynPropsConstraint;
	
	
	/** The repository helper. */
	private Repository repositoryHelper;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.util.BaseAlfrescoTestCase#setUp()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		logger.debug("SecurityServiceTest:setUp");

		
		aclGroupDao = (BeCPGDao<ACLGroupData>) ctx.getBean("aclGroupDao");
		securityService = (SecurityService) ctx.getBean("securityService");

		authenticationService = (MutableAuthenticationService) ctx
				.getBean("authenticationService");
		authenticationDAO = (MutableAuthenticationDao) ctx
				.getBean("authenticationDao");
		authorityService = (AuthorityService) ctx
				.getBean("authorityService");

		personService = (PersonService) ctx.getBean("PersonService");

		dynPropsConstraint = (DynPropsConstraint) ctx.getBean("dynPropsConstraint");
		
		authenticationComponent.setSystemUserAsCurrentUser();
		
		

	

	}
	
	private void createUsers(){
		
		/*
		 * Matthieu : GRP_1, GRP_2 Philippe : GRP_3 Admin
		 */
		
		grp1 = authorityService.createAuthority(
				AuthorityType.GROUP, "GRP_1");
		grp2 = authorityService.createAuthority(
				AuthorityType.GROUP, "GRP_2");
		grp3 = authorityService.createAuthority(
				AuthorityType.GROUP, "GRP_3");

		if (!authenticationDAO.userExists(USER_ONE)) {
			createUser(USER_ONE);
			
			authorityService.addAuthority(grp1, USER_ONE);

		
			authorityService.addAuthority(grp2, USER_ONE);
		}

		if (!authenticationDAO.userExists(USER_TWO)) {
			createUser(USER_TWO);
			
			authorityService.addAuthority(grp3, USER_TWO);
		}
	}
	

	private void createUser(String userName) {
		if (this.authenticationService.authenticationExists(userName) == false) {
			this.authenticationService.createAuthentication(userName,
					"PWD".toCharArray());

			PropertyMap ppOne = new PropertyMap(4);
			ppOne.put(ContentModel.PROP_USERNAME, userName);
			ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
			ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
			ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
			ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");

			this.personService.createPerson(ppOne);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.util.BaseAlfrescoTestCase#tearDown()
	 */
	@Override
	public void tearDown() throws Exception {
		try {
			authenticationComponent.clearCurrentSecurityContext();
		} catch (Throwable e) {
			e.printStackTrace();
			// Don't let this mask any previous exceptions
		}
		super.tearDown();

	}

	private void createACLGroup(NodeRef folderNodeRef) {

		createUsers();
		
		ACLGroupData aclGroupData = new ACLGroupData();
		aclGroupData.setName("Test ACL");
		aclGroupData.setNodeType(SecurityModel.TYPE_ACL_ENTRY.toString());
		
		
		
		aclGroupData.setNodeAspects(Arrays.asList(new String[]{BeCPGModel.ASPECT_CLIENTS.toString(),BeCPGModel.ASPECT_CODE.toString()}));
		
		
		List<String> groups = new ArrayList<String>();
		groups.add(grp3);
		List<ACLEntryDataItem> acls = new ArrayList<ACLEntryDataItem>();

		acls.add(new ACLEntryDataItem(null, "cm:name",
				PermissionModel.READ_ONLY, groups));

		groups = new ArrayList<String>();
		groups.add(grp1);

		acls.add(new ACLEntryDataItem(null, "sec:propName",
				PermissionModel.READ_WRITE, groups));

		acls.add(new ACLEntryDataItem(null, "sec:aclPermission",
				PermissionModel.READ_ONLY, groups));

		aclGroupData.setAcls(acls);
		aclGroupDao.create(folderNodeRef, aclGroupData);

	}

//	Failed tests:   testSuggestProduct(fr.becpg.repo.web.scripts.listvalue.AutoCompleteWebScriptTest): Status code 500 returned, but expected 200 for /becpg/autocomplete/product?q=ra (get)(..)
//			  testComputeAccessMode(fr.becpg.repo.security.SecurityServiceTest): expected:<1> but was:<2>
//
//			Tests in error: 
//			  testECOInMultiLeveCompo(fr.becpg.repo.ecm.ECOTest): 00230000 Exception in Transaction.
//			  testImportProducts(fr.becpg.repo.importer.ImportServiceTest): 00230001 Exception from transactional callback: fr.becpg.repo.importer.impl.ImportServiceImpl$2@194b24b
//
//	
//	
	public void testComputeAccessMode() {

		
		transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<NodeRef>() {
					@Override
					public NodeRef execute() throws Throwable {

						/*-- Create test folder --*/
						NodeRef folderNodeRef = nodeService.getChildByName(
								repositoryHelper.getCompanyHome(),
								ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);
						if (folderNodeRef != null) {
							fileFolderService.delete(folderNodeRef);
						}
						folderNodeRef = fileFolderService.create(
								repositoryHelper.getCompanyHome(),
								PATH_TESTFOLDER, ContentModel.TYPE_FOLDER)
								.getNodeRef();

						createACLGroup(folderNodeRef);

						securityService.computeAcls();

						return null;

					}
				}, false, true);

		authenticationComponent.setCurrentUser(USER_TWO);

		Assert.assertEquals(securityService.computeAccessMode(
				SecurityModel.TYPE_ACL_ENTRY, "cm:name"),
				SecurityService.READ_ACCESS);

		Assert.assertEquals(securityService.computeAccessMode(
				SecurityModel.TYPE_ACL_ENTRY, "sec:propName"),
				SecurityService.READ_ACCESS);

		Assert.assertEquals(securityService.computeAccessMode(
				SecurityModel.TYPE_ACL_ENTRY, "sec:aclPermission"),
				SecurityService.NONE_ACCESS);

		authenticationComponent.setCurrentUser(USER_ONE);

		Assert.assertEquals(securityService.computeAccessMode(
				SecurityModel.TYPE_ACL_ENTRY, "cm:name"),
				SecurityService.NONE_ACCESS);

		Assert.assertEquals(securityService.computeAccessMode(
				SecurityModel.TYPE_ACL_ENTRY, "sec:propName"),
				SecurityService.WRITE_ACCESS);

		Assert.assertEquals(securityService.computeAccessMode(
				SecurityModel.TYPE_ACL_ENTRY, "sec:aclPermission"),
				SecurityService.READ_ACCESS);

		authenticationComponent.setCurrentUser("admin");

		Assert.assertEquals(securityService.computeAccessMode(
				SecurityModel.TYPE_ACL_ENTRY, "cm:name"),
				SecurityService.WRITE_ACCESS);

		Assert.assertEquals(securityService.computeAccessMode(
				SecurityModel.TYPE_ACL_ENTRY, "sec:propName"),
				SecurityService.WRITE_ACCESS);

		Assert.assertEquals(securityService.computeAccessMode(
				SecurityModel.TYPE_ACL_ENTRY, "sec:aclPermission"),
				SecurityService.WRITE_ACCESS);

	}
	
	public void testConstainst(){
		dynPropsConstraint.setConstraintType(DynPropsConstraint.TYPE_NODE);
		List<String> types =  dynPropsConstraint.getAllowedValues();
		Assert.assertNotNull(types);
		Assert.assertTrue(types.size()>0);
		
//		for(String type : dynPropsConstraint.getAllowedValues()){
//			System.out.println("Type : "+type);
//		}
		
		dynPropsConstraint.setConstraintType(DynPropsConstraint.ASPECT_NODE);
		List<String> aspects =  dynPropsConstraint.getAllowedValues();
		Assert.assertNotNull(aspects);
		Assert.assertTrue(aspects.size()>0);
		
		
		
//		for(String aspect : dynPropsConstraint.getAllowedValues()){
//			System.out.println("aspect : "+aspect);
//		}
		
	}
	
}
