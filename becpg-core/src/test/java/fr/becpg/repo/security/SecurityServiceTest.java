package fr.becpg.repo.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SecurityModel;
import fr.becpg.repo.BeCPGDao;
import fr.becpg.repo.security.constraint.DynPropsConstraint;
import fr.becpg.repo.security.data.ACLGroupData;
import fr.becpg.repo.security.data.dataList.ACLEntryDataItem;
import fr.becpg.repo.security.data.dataList.ACLEntryDataItem.PermissionModel;
import fr.becpg.test.BeCPGTestHelper;
import fr.becpg.test.RepoBaseTestCase;

public class SecurityServiceTest extends RepoBaseTestCase {

	protected static final String USER_ONE = "matthieu_secu";

	protected static final String USER_TWO = "philippe_secu";

	
	private String grp1;
	private String grp2;
	private String grp3;
	
	

	/** The logger. */
	private static Log logger = LogFactory.getLog(SecurityServiceTest.class);

	@Resource
	private BeCPGDao<ACLGroupData> aclGroupDao;
	@Resource
	private SecurityService securityService;

	@Resource
	private DynPropsConstraint dynPropsConstraint;
	
	
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
			BeCPGTestHelper.createUser(USER_ONE,repoBaseTestCase);
			
			authorityService.addAuthority(grp1, USER_ONE);

		
			authorityService.addAuthority(grp2, USER_ONE);
		}

		if (!authenticationDAO.userExists(USER_TWO)) {
			BeCPGTestHelper.createUser(USER_TWO,repoBaseTestCase);
			
			authorityService.addAuthority(grp3, USER_TWO);
		}
	}
	

	private void createACLGroup(NodeRef testFolderNodeRef) {

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
		aclGroupDao.create(testFolderNodeRef, aclGroupData);

	}

	@Test
	public void testComputeAccessMode() {

		
		transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<NodeRef>() {
					@Override
					public NodeRef execute() throws Throwable {
						
						createACLGroup(testFolderNodeRef);

						securityService.refreshAcls();

						return null;

					}
				}, false, true);

		authenticationComponent.setCurrentUser(USER_TWO);

		assertEquals(securityService.computeAccessMode(
				SecurityModel.TYPE_ACL_ENTRY, "cm:name"),
				SecurityService.READ_ACCESS);

		assertEquals(securityService.computeAccessMode(
				SecurityModel.TYPE_ACL_ENTRY, "sec:propName"),
				SecurityService.READ_ACCESS);

		assertEquals(securityService.computeAccessMode(
				SecurityModel.TYPE_ACL_ENTRY, "sec:aclPermission"),
				SecurityService.NONE_ACCESS);

		authenticationComponent.setCurrentUser(USER_ONE);

		assertEquals(securityService.computeAccessMode(
				SecurityModel.TYPE_ACL_ENTRY, "cm:name"),
				SecurityService.NONE_ACCESS);

		assertEquals(securityService.computeAccessMode(
				SecurityModel.TYPE_ACL_ENTRY, "sec:propName"),
				SecurityService.WRITE_ACCESS);

		assertEquals(securityService.computeAccessMode(
				SecurityModel.TYPE_ACL_ENTRY, "sec:aclPermission"),
				SecurityService.READ_ACCESS);

		authenticationComponent.setCurrentUser("admin");

		assertEquals(securityService.computeAccessMode(
				SecurityModel.TYPE_ACL_ENTRY, "cm:name"),
				SecurityService.WRITE_ACCESS);

		assertEquals(securityService.computeAccessMode(
				SecurityModel.TYPE_ACL_ENTRY, "sec:propName"),
				SecurityService.WRITE_ACCESS);

		assertEquals(securityService.computeAccessMode(
				SecurityModel.TYPE_ACL_ENTRY, "sec:aclPermission"),
				SecurityService.WRITE_ACCESS);

	}
	
	@Test
	public void testConstainst(){
		dynPropsConstraint.setConstraintType(DynPropsConstraint.TYPE_NODE);
		List<String> types =  dynPropsConstraint.getAllowedValues();
		assertNotNull(types);
		assertTrue(types.size()>0);
		
		if(logger.isDebugEnabled()){
			for(String type : dynPropsConstraint.getAllowedValues()){
				logger.debug("Type : "+type);
			}
		}
		
		dynPropsConstraint.setConstraintType(DynPropsConstraint.ASPECT_NODE);
		List<String> aspects =  dynPropsConstraint.getAllowedValues();
		assertNotNull(aspects);
		assertTrue(aspects.size()>0);
		
		if(logger.isDebugEnabled()){
			for(String aspect : dynPropsConstraint.getAllowedValues()){
				logger.debug("aspect : "+aspect);
			}
		}
		
	}
	
}
