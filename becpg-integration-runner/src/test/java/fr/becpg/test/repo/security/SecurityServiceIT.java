/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG.
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
package fr.becpg.test.repo.security;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.SecurityModel;
import fr.becpg.repo.listvalue.ListValueEntry;
import fr.becpg.repo.listvalue.ListValuePage;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.security.SecurityService;
import fr.becpg.repo.security.data.ACLGroupData;
import fr.becpg.repo.security.data.dataList.ACLEntryDataItem;
import fr.becpg.repo.security.data.dataList.ACLEntryDataItem.PermissionModel;
import fr.becpg.repo.security.listvalue.SecurityListValuePlugin;
import fr.becpg.test.BeCPGTestHelper;
import fr.becpg.test.RepoBaseTestCase;

public class SecurityServiceIT extends RepoBaseTestCase {

	protected static final String USER_ONE = "matthieu_secu";

	protected static final String USER_TWO = "philippe_secu";

	protected static final String USER_THREE = "steven_secu";

	private String grp1;
	private String grp2;
	private String grp3;

	
	private static final Log logger = LogFactory.getLog(SecurityServiceIT.class);

	@Autowired
	private AlfrescoRepository<ACLGroupData> alfrescoRepository;
	@Autowired
	private SecurityService securityService;

	@Autowired
	SecurityListValuePlugin securityListValuePlugin;

	@Autowired
	NamespaceService namespaceService;

	private void createUsers() {

		/*
		 * Matthieu : GRP_1, GRP_2 Philippe : GRP_3 Admin
		 */

		if (authorityService.authorityExists(PermissionService.GROUP_PREFIX + "GRP_1")) {
			grp1 = PermissionService.GROUP_PREFIX + "GRP_1";
		} else {
			grp1 = authorityService.createAuthority(AuthorityType.GROUP, "GRP_1");
		}
		if (authorityService.authorityExists(PermissionService.GROUP_PREFIX + "GRP_2")) {
			grp2 = PermissionService.GROUP_PREFIX + "GRP_2";
		} else {
			grp2 = authorityService.createAuthority(AuthorityType.GROUP, "GRP_2");
		}

		if (authorityService.authorityExists(PermissionService.GROUP_PREFIX + "GRP_3")) {
			grp3 = PermissionService.GROUP_PREFIX + "GRP_3";
		} else {
			grp3 = authorityService.createAuthority(AuthorityType.GROUP, "GRP_3");
		}

		if (!authenticationDAO.userExists(USER_ONE)) {
			BeCPGTestHelper.createUser(USER_ONE);

			authorityService.addAuthority(grp1, USER_ONE);

			authorityService.addAuthority(grp2, USER_ONE);
		}

		if (!authenticationDAO.userExists(USER_TWO)) {
			BeCPGTestHelper.createUser(USER_TWO);

			authorityService.addAuthority(grp3, USER_TWO);
		}

		if (!authenticationDAO.userExists(USER_THREE)) {
			BeCPGTestHelper.createUser(USER_THREE);

			authorityService.addAuthority(grp2, USER_THREE);
		}
	}

	private NodeRef createACLGroup() {

		createUsers();
		ACLGroupData aclGroupData = new ACLGroupData();
		aclGroupData.setName("Test ACL");
		aclGroupData.setNodeType(SecurityModel.TYPE_ACL_ENTRY.toPrefixString(namespaceService));

		// aclGroupData.setNodeAspects(Arrays.asList(new
		// String[]{BeCPGModel.ASPECT_CLIENTS.toString(),BeCPGModel.ASPECT_CODE.toString()}));

		List<NodeRef> group3s = new ArrayList<>();
		group3s.add(authorityService.getAuthorityNodeRef(grp3));
		List<ACLEntryDataItem> acls = new ArrayList<>();

		acls.add(new ACLEntryDataItem("cm:name", PermissionModel.READ_ONLY, group3s));

		List<NodeRef> group1s = new ArrayList<>();
		group1s = new ArrayList<>();
		group1s.add(authorityService.getAuthorityNodeRef(grp1));

		acls.add(new ACLEntryDataItem("sec:propName", PermissionModel.READ_WRITE, group1s));

		acls.add(new ACLEntryDataItem("sec:aclPermission", PermissionModel.READ_ONLY, group1s));

		acls.add(new ACLEntryDataItem("cm:titled", PermissionModel.READ_ONLY, group3s));
		acls.add(new ACLEntryDataItem("cm:titled", PermissionModel.READ_WRITE, group1s));

		acls.add(new ACLEntryDataItem("cm:description", PermissionModel.READ_WRITE, group1s));
		acls.add(new ACLEntryDataItem("cm:description", PermissionModel.READ_ONLY, group3s));

		aclGroupData.setAcls(acls);
		alfrescoRepository.create(getTestFolderNodeRef(), aclGroupData);

		return aclGroupData.getNodeRef();

	}

	@Test
	public void testComputeAccessMode() {

		final NodeRef aclGroupNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			NodeRef ret = createACLGroup();

			securityService.refreshAcls();

			return ret;

		}, false, true);

		authenticationComponent.setCurrentUser(USER_TWO);

		assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "cm:name"), SecurityService.READ_ACCESS);

		assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "sec:propName"), SecurityService.READ_ACCESS);

		assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "sec:aclPermission"), SecurityService.NONE_ACCESS);

		assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "cm:description"), SecurityService.READ_ACCESS);

		assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "cm:titled"), SecurityService.READ_ACCESS);

		authenticationComponent.setCurrentUser(USER_ONE);

		assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "cm:name"), SecurityService.NONE_ACCESS);

		assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "sec:propName"), SecurityService.WRITE_ACCESS);

		assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "sec:aclPermission"), SecurityService.READ_ACCESS);

		assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "cm:description"), SecurityService.WRITE_ACCESS);

		assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "cm:titled"), SecurityService.WRITE_ACCESS);

		authenticationComponent.setCurrentUser(USER_THREE);

		assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "cm:description"), SecurityService.NONE_ACCESS);

		assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "cm:titled"), SecurityService.NONE_ACCESS);

		authenticationComponent.setCurrentUser("admin");

		assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "cm:name"), SecurityService.WRITE_ACCESS);

		assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "sec:propName"), SecurityService.WRITE_ACCESS);

		assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "sec:aclPermission"), SecurityService.WRITE_ACCESS);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			nodeService.deleteNode(aclGroupNodeRef);

			return null;
		}, false, true);

	}

	@Test
	public void testConstainst() {
		ListValuePage types = securityListValuePlugin.suggest("aclType", "*", 1, 25, null);
		assertNotNull(types);
		assertTrue(types.getFullListSize() > 0);

		if (logger.isDebugEnabled()) {
			for (ListValueEntry type : types.getResults()) {
				logger.debug("Type : " + type);
			}
		}

	}

}
