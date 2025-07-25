/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.SecurityModel;
import fr.becpg.repo.autocomplete.AutoCompleteEntry;
import fr.becpg.repo.autocomplete.AutoCompletePage;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.security.SecurityService;
import fr.becpg.repo.security.autocomplete.SecurityAutoCompletePlugin;
import fr.becpg.repo.security.data.ACLGroupData;
import fr.becpg.repo.security.data.PermissionModel;
import fr.becpg.repo.security.data.dataList.ACLEntryDataItem;
import fr.becpg.repo.system.SystemConfigurationService;
import fr.becpg.test.BeCPGTestHelper;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class SecurityServiceIT extends AbstractFinishedProductTest {

	protected static final String USER_ONE = "matthieu_secu";

	protected static final String USER_TWO = "philippe_secu";

	protected static final String USER_THREE = "steven_secu";

	protected static final String RM_GLOBAL_ACL = "Rm Global Acl";
	protected static final String RM_LOCAL_ACL = "Rm Local Acl";

	private String grp1;
	private String grp2;
	private String grp3;

	private static final Log logger = LogFactory.getLog(SecurityServiceIT.class);

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	@Autowired
	private SecurityService securityService;

	@Autowired
	protected ProductService productService;

	@Autowired
	SecurityAutoCompletePlugin securityAutoCompletePlugin;

	@Autowired
	NamespaceService namespaceService;

	@Autowired
	private SystemConfigurationService systemConfigurationService;

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

	private NodeRef createDefaultReadOnlyACLGroup() {

		createUsers();
		ACLGroupData aclGroupData = new ACLGroupData();
		aclGroupData.setName("Test read only ACL");
		aclGroupData.setNodeType(SecurityModel.TYPE_ACL_ENTRY.toPrefixString(namespaceService));
		aclGroupData.setIsDefaultReadOnly(true);

		List<ACLEntryDataItem> acls = new ArrayList<>();

		List<NodeRef> group1s = new ArrayList<>();
		group1s.add(authorityService.getAuthorityNodeRef(grp1));

		acls.add(new ACLEntryDataItem("sec:propName", PermissionModel.READ_WRITE, group1s));
		acls.add(new ACLEntryDataItem("cm:description", PermissionModel.READ_WRITE, group1s));
		acls.add(new ACLEntryDataItem("cm:titled", PermissionModel.READ_READANDWRITE, group1s));

		aclGroupData.setAcls(acls);
		alfrescoRepository.create(getTestFolderNodeRef(), aclGroupData);

		return aclGroupData.getNodeRef();

	}

	private NodeRef createGlobalACLGroup() {
		createUsers();
		List<NodeRef> group1s = new ArrayList<>();
		group1s.add(authorityService.getAuthorityNodeRef(grp1));
		List<NodeRef> group2s = new ArrayList<>();
		group2s.add(authorityService.getAuthorityNodeRef(grp2));
		List<NodeRef> group3s = new ArrayList<>();
		group3s.add(authorityService.getAuthorityNodeRef(grp3));

		// CREATE RM ACL GROUP DATA
		ACLGroupData rmAclGroupData = new ACLGroupData();
		rmAclGroupData.setName("Test RM ACL");
		rmAclGroupData.setNodeType(PLMModel.TYPE_RAWMATERIAL.toPrefixString(namespaceService));
		rmAclGroupData.setIsLocalPermission(false);

		List<ACLEntryDataItem> rmAcls = new ArrayList<>();

		rmAcls.add(new ACLEntryDataItem("bcpg:erpCode", PermissionModel.READ_READANDWRITE, group3s));
		rmAcls.add(new ACLEntryDataItem("View-documents", PermissionModel.READ_WRITE, group1s));
		rmAcls.add(new ACLEntryDataItem("bcpg:nutList", PermissionModel.READ_ONLY, group2s));

		rmAclGroupData.setAcls(rmAcls);

		return alfrescoRepository.create(getTestFolderNodeRef(), rmAclGroupData).getNodeRef();

	}

	private NodeRef createLocalACLGroup() {
		createUsers();
		List<NodeRef> group1s = new ArrayList<>();
		group1s.add(authorityService.getAuthorityNodeRef(grp1));
		List<NodeRef> group2s = new ArrayList<>();
		group2s.add(authorityService.getAuthorityNodeRef(grp2));
		List<NodeRef> group3s = new ArrayList<>();
		group3s.add(authorityService.getAuthorityNodeRef(grp3));

		// CREATE LOCAL RM ACL GROUP DATA
		ACLGroupData localRMAclGroupData = new ACLGroupData();
		localRMAclGroupData.setName("Test Local ACL");
		localRMAclGroupData.setNodeType(PLMModel.TYPE_RAWMATERIAL.toPrefixString(namespaceService));
		localRMAclGroupData.setIsLocalPermission(true);

		List<ACLEntryDataItem> localRmAcls = new ArrayList<>();

		localRmAcls.add(new ACLEntryDataItem("bcpg:organoList", PermissionModel.READ_READANDWRITE, group3s));
		localRmAcls.add(new ACLEntryDataItem("bcpg:allergenList", PermissionModel.READ_WRITE, group1s));
		localRmAcls.add(new ACLEntryDataItem("View-documents", PermissionModel.READ_ONLY, group2s));

		localRMAclGroupData.setAcls(localRmAcls);
		return alfrescoRepository.create(getTestFolderNodeRef(), localRMAclGroupData).getNodeRef();

	}

	private Map<String, NodeRef> createTestProducts(NodeRef localAclGroupNodeRef) {

		return inWriteTx(() -> {
			// Create products
			Map<String, NodeRef> products = new HashMap<>();

			permissionService.setPermission(getTestFolderNodeRef(), USER_ONE, PermissionService.COORDINATOR, true);
			permissionService.setPermission(getTestFolderNodeRef(), USER_TWO, PermissionService.COORDINATOR, true);
			permissionService.setPermission(getTestFolderNodeRef(), USER_THREE, PermissionService.COORDINATOR, true);
			permissionService.setPermission(getTestFolderNodeRef(), "Test", PermissionService.COORDINATOR, true);

			RawMaterialData rmGlocalAcl = new RawMaterialData();
			rmGlocalAcl.setName(RM_GLOBAL_ACL);
			NodeRef rmGlobalNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rmGlocalAcl).getNodeRef();
			products.put(RM_GLOBAL_ACL, rmGlobalNodeRef);

			RawMaterialData rmLocalAcl = new RawMaterialData();
			rmLocalAcl.setName(RM_LOCAL_ACL);
			NodeRef rmLocalNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rmLocalAcl).getNodeRef();
			nodeService.createAssociation(rmLocalNodeRef, localAclGroupNodeRef, SecurityModel.ASSOC_SECURITY_REF);
			products.put(RM_LOCAL_ACL, rmLocalNodeRef);

			return products;
		});

	}

	@Test
	public void testComputeAccessMode() {

		final NodeRef aclGroupNodeRef = inWriteTx(() -> {

			NodeRef ret = createACLGroup();

			securityService.refreshAcls();

			return ret;

		});

		inWriteTx(() -> {

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

			return null;

		});

		inWriteTx(() -> {

			nodeService.deleteNode(aclGroupNodeRef);

			return null;
		});

	}

	@Test
	public void testLocalComputeAccessMode() {

		final NodeRef globalAclGroupNodeRef = inWriteTx(this::createGlobalACLGroup);

		final NodeRef localAclGroupNodeRef = inWriteTx(this::createLocalACLGroup);

		final Map<String, NodeRef> products = inWriteTx(() -> {
			Map<String, NodeRef> ret = createTestProducts(localAclGroupNodeRef);
			securityService.refreshAcls();

			return ret;

		});

		inWriteTx(() -> {

			NodeRef rmGlobalNodeRef = products.get(RM_GLOBAL_ACL);
			NodeRef rmLocalNodeRef = products.get(RM_LOCAL_ACL);

			productService.formulate(rmGlobalNodeRef);
			productService.formulate(rmLocalNodeRef);

			if ((rmGlobalNodeRef != null) && (rmLocalNodeRef != null)) {

				authenticationComponent.setCurrentUser(USER_ONE);

				assertEquals(securityService.computeAccessMode(rmGlobalNodeRef, PLMModel.TYPE_RAWMATERIAL, PLMModel.TYPE_NUTLIST),
						SecurityService.READ_ACCESS);

				assertEquals(securityService.computeAccessMode(rmGlobalNodeRef, PLMModel.TYPE_RAWMATERIAL, BeCPGModel.PROP_ERP_CODE),
						SecurityService.NONE_ACCESS);

				assertEquals(securityService.computeAccessMode(rmLocalNodeRef, PLMModel.TYPE_RAWMATERIAL, PLMModel.TYPE_ORGANOLIST),
						SecurityService.NONE_ACCESS);

				assertEquals(securityService.computeAccessMode(rmLocalNodeRef, PLMModel.TYPE_RAWMATERIAL, PLMModel.TYPE_ALLERGENLIST),
						SecurityService.WRITE_ACCESS);

				assertEquals(securityService.computeAccessMode(rmLocalNodeRef, PLMModel.TYPE_RAWMATERIAL, PLMModel.TYPE_NUTLIST),
						SecurityService.WRITE_ACCESS);

				assertEquals(securityService.computeAccessMode(rmLocalNodeRef, PLMModel.TYPE_RAWMATERIAL, BeCPGModel.PROP_ERP_CODE),
						SecurityService.WRITE_ACCESS);

				authenticationComponent.setCurrentUser(USER_TWO);

				assertEquals(securityService.computeAccessMode(rmGlobalNodeRef, PLMModel.TYPE_RAWMATERIAL, PLMModel.TYPE_NUTLIST),
						SecurityService.NONE_ACCESS);

				assertEquals(securityService.computeAccessMode(rmGlobalNodeRef, PLMModel.TYPE_RAWMATERIAL, BeCPGModel.PROP_ERP_CODE),
						SecurityService.WRITE_ACCESS);

				assertEquals(securityService.computeAccessMode(rmLocalNodeRef, PLMModel.TYPE_RAWMATERIAL, PLMModel.TYPE_ORGANOLIST),
						SecurityService.WRITE_ACCESS);

				assertEquals(securityService.computeAccessMode(rmLocalNodeRef, PLMModel.TYPE_RAWMATERIAL, PLMModel.TYPE_ALLERGENLIST),
						SecurityService.READ_ACCESS);

				assertEquals(securityService.computeAccessMode(rmLocalNodeRef, PLMModel.TYPE_RAWMATERIAL, PLMModel.TYPE_NUTLIST),
						SecurityService.WRITE_ACCESS);

				assertEquals(securityService.computeAccessMode(rmLocalNodeRef, PLMModel.TYPE_RAWMATERIAL, BeCPGModel.PROP_ERP_CODE),
						SecurityService.WRITE_ACCESS);

				// Check permissions

				authenticationComponent.setSystemUserAsCurrentUser();
				// Global rm datalists
				NodeRef globalRmListContainerNodeRef = entityListDAO.getListContainer(rmGlobalNodeRef);
				List<NodeRef> globalRmDatalists = entityListDAO.getExistingListsNodeRef(globalRmListContainerNodeRef);
				for (NodeRef globalRmDatalist : globalRmDatalists) {
					String globalDataListQName = (String) nodeService.getProperty(globalRmDatalist, DataListModel.PROP_DATALISTITEMTYPE);
					if (globalDataListQName.equals("bcpg:nutList")) {
						for (AccessPermission permission : permissionService.getAllSetPermissions(globalRmDatalist)) {
							if (permission.getAuthority().equals(grp2)) {
								logger.info("Global nutList permission: " + permission);
								assertEquals(permission.getPermission(), PermissionService.CONSUMER);
							}
						}
					}
				}
				// Global rm folder
				List<PermissionModel> globalFolderPerms = securityService
						.getPermissionContext(rmGlobalNodeRef, nodeService.getType(rmGlobalNodeRef), "View-documents").getPermissions();
				if (globalFolderPerms != null) {
					List<ChildAssociationRef> folders = nodeService.getChildAssocs(rmGlobalNodeRef, ContentModel.ASSOC_CONTAINS,
							RegexQNamePattern.MATCH_ALL);
					for (ChildAssociationRef folder : folders) {
						for (AccessPermission permission : permissionService.getAllSetPermissions(folder.getChildRef())) {
							if (permission.getAuthority().equals(grp1)) {
								logger.info("Global folder permission: " + permission);
								assertEquals(permission.getPermission(), PermissionService.CONTRIBUTOR);
							}
						}
					}
				}
				// Local rm datalists
				NodeRef localRmListContainerNodeRef = entityListDAO.getListContainer(rmLocalNodeRef);
				List<NodeRef> localRmDatalists = entityListDAO.getExistingListsNodeRef(localRmListContainerNodeRef);
				for (NodeRef localRmDatalist : localRmDatalists) {
					String localDataListQName = (String) nodeService.getProperty(localRmDatalist, DataListModel.PROP_DATALISTITEMTYPE);
					if (localDataListQName.equals("bcpg:organoList")) {
						for (AccessPermission permission : permissionService.getAllSetPermissions(localRmDatalist)) {
							if (permission.getAuthority().equals(grp3)) {
								logger.info("Local organoList permission: " + permission);
								assertEquals(permission.getPermission(), PermissionService.CONTRIBUTOR);
							}
						}
					} else if (localDataListQName.equals("bcpg:allergenList")) {
						for (AccessPermission permission : permissionService.getAllSetPermissions(localRmDatalist)) {
							if (permission.getAuthority().equals(grp1)) {
								logger.info("Local allergenList permission: " + permission);
								assertEquals(permission.getPermission(), PermissionService.CONTRIBUTOR);
							}
						}
					}
				}
				//Local folder permission
				List<PermissionModel> localDocPerms = securityService
						.getPermissionContext(rmLocalNodeRef, nodeService.getType(rmLocalNodeRef), "View-documents").getPermissions();
				if (localDocPerms != null) {
					List<ChildAssociationRef> folders = nodeService.getChildAssocs(rmLocalNodeRef, ContentModel.ASSOC_CONTAINS,
							RegexQNamePattern.MATCH_ALL);
					for (ChildAssociationRef folder : folders) {
						for (AccessPermission permission : permissionService.getAllSetPermissions(folder.getChildRef())) {
							if (permission.getAuthority().equals(grp2)) {
								logger.info("Local folder permission: " + permission);
								assertEquals(permission.getPermission(), PermissionService.CONSUMER);
							}
						}
					}
				}
			}

			return null;

		});

		inWriteTx(() -> {

			nodeService.deleteNode(globalAclGroupNodeRef);

			return null;
		});

	}

	@Test
	public void testConstainst() {
		AutoCompletePage types = securityAutoCompletePlugin.suggest("aclType", "*", 1, 25, null);
		assertNotNull(types);
		assertTrue(types.getFullListSize() > 0);

		if (logger.isDebugEnabled()) {
			for (AutoCompleteEntry type : types.getResults()) {
				logger.debug("Type : " + type);
			}
		}

	}

	@Test
	public void testDefaultReadOnly() {

		final NodeRef aclGroupNodeRef = inWriteTx(() -> {
			NodeRef ret = createDefaultReadOnlyACLGroup();
			securityService.refreshAcls();
			return ret;

		});

		inReadTx(() -> {
			authenticationComponent.setCurrentUser(USER_TWO);
			assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "cm:name"), SecurityService.READ_ACCESS);
			assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "sec:propName"), SecurityService.READ_ACCESS);
			assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "sec:aclPermission"), SecurityService.READ_ACCESS);
			assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "cm:description"), SecurityService.READ_ACCESS);
			assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "cm:titled"), SecurityService.NONE_ACCESS);

			authenticationComponent.setCurrentUser(USER_ONE);
			assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "cm:name"), SecurityService.READ_ACCESS);
			assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "sec:propName"), SecurityService.WRITE_ACCESS);
			assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "sec:aclPermission"), SecurityService.READ_ACCESS);
			assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "cm:description"), SecurityService.WRITE_ACCESS);
			assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "cm:titled"), SecurityService.WRITE_ACCESS);

			authenticationComponent.setCurrentUser(USER_THREE);
			assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "cm:name"), SecurityService.READ_ACCESS);
			assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "sec:propName"), SecurityService.READ_ACCESS);
			assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "sec:aclPermission"), SecurityService.READ_ACCESS);
			assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "cm:description"), SecurityService.READ_ACCESS);
			assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "cm:titled"), SecurityService.NONE_ACCESS);

			authenticationComponent.setCurrentUser("admin");
			assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "cm:name"), SecurityService.WRITE_ACCESS);
			assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "sec:propName"), SecurityService.WRITE_ACCESS);
			assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "sec:aclPermission"), SecurityService.WRITE_ACCESS);
			assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "cm:description"), SecurityService.WRITE_ACCESS);
			assertEquals(securityService.computeAccessMode(null, SecurityModel.TYPE_ACL_ENTRY, "cm:titled"), SecurityService.WRITE_ACCESS);
			return null;
		});

		inWriteTx(() -> {
			nodeService.deleteNode(aclGroupNodeRef);
			return null;
		});
	}

	@Test
	public void testDataListPermissions() {

		inWriteTx(() -> {
			systemConfigurationService.updateConfValue("beCPG.formulation.security.enforceACL", "true");
			return null;
		});

		initParts();

		NodeRef securityRuleNodeRef = inWriteTx(() -> {
			NodeRef ret = createDataListSecurityRule();
			securityService.refreshAcls();
			return ret;
		});

		try {

			NodeRef productNodeRef = inWriteTx(() -> {
				FinishedProductData productData = new FinishedProductData();
				productData.setParentNodeRef(getTestFolderNodeRef());
				productData.setName("FP testDataListPermissions");
				List<NutListDataItem> nutList = new LinkedList<>();
				nutList.add(NutListDataItem.build().withValue(1d).withUnit(null).withMini(null).withMaxi(null).withGroup(null).withNut(nut1));
				productData.setNutList(nutList);
				return alfrescoRepository.save(productData).getNodeRef();
			});

			// grp1 write and grp2 read
			inWriteTx(() -> {
				productService.formulate(productNodeRef);
				FinishedProductData productData = (FinishedProductData) alfrescoRepository.findOne(productNodeRef);
				NodeRef nutListNodeRef = productData.getNutList().get(0).getParentNodeRef();
				assertFalse(permissionService.getInheritParentPermissions(nutListNodeRef));
				Set<AccessPermission> permissions = permissionService.getAllSetPermissions(nutListNodeRef);
				checkPermissions(productNodeRef, permissions, new ArrayList<>(List.of(grp2)), new ArrayList<>(List.of(grp1)));
				return null;
			});

			// grp2 read
			inWriteTx(() -> {
				ACLGroupData securityRule = (ACLGroupData) alfrescoRepository.findOne(securityRuleNodeRef);
				for (ACLEntryDataItem permissionList : securityRule.getAcls()) {
					if (permissionList.getAclPermission().equals(PermissionModel.READ_WRITE)) {
						permissionList.setPropName("cm:description");
					}
				}
				alfrescoRepository.save(securityRule);
				securityService.refreshAcls();
				productService.formulate(productNodeRef);
				FinishedProductData productData = (FinishedProductData) alfrescoRepository.findOne(productNodeRef);
				NodeRef nutListNodeRef = productData.getNutList().get(0).getParentNodeRef();
				assertFalse(permissionService.getInheritParentPermissions(nutListNodeRef));
				Set<AccessPermission> permissions = permissionService.getAllSetPermissions(nutListNodeRef);
				checkPermissions(productNodeRef, permissions, new ArrayList<>(List.of(grp2)), new ArrayList<>(List.of()));
				return null;
			});

			// grp1 write
			inWriteTx(() -> {
				ACLGroupData securityRule = (ACLGroupData) alfrescoRepository.findOne(securityRuleNodeRef);
				for (ACLEntryDataItem permissionList : securityRule.getAcls()) {
					if (permissionList.getAclPermission().equals(PermissionModel.READ_WRITE)) {
						permissionList.setPropName("bcpg:nutList");
					} else if (permissionList.getAclPermission().equals(PermissionModel.READ_ONLY)) {
						permissionList.setPropName("cm:description");
					}
				}
				alfrescoRepository.save(securityRule);
				securityService.refreshAcls();
				productService.formulate(productNodeRef);
				FinishedProductData productData = (FinishedProductData) alfrescoRepository.findOne(productNodeRef);
				NodeRef nutListNodeRef = productData.getNutList().get(0).getParentNodeRef();
				assertFalse(permissionService.getInheritParentPermissions(nutListNodeRef));
				Set<AccessPermission> permissions = permissionService.getAllSetPermissions(nutListNodeRef);
				checkPermissions(productNodeRef, permissions, new ArrayList<>(List.of()), new ArrayList<>(List.of(grp1)));
				return null;
			});

			// grp1 write and grp2 read
			inWriteTx(() -> {
				ACLGroupData securityRule = (ACLGroupData) alfrescoRepository.findOne(securityRuleNodeRef);
				for (ACLEntryDataItem permissionList : securityRule.getAcls()) {
					if (permissionList.getAclPermission().equals(PermissionModel.READ_ONLY)
							|| permissionList.getAclPermission().equals(PermissionModel.READ_WRITE)) {
						permissionList.setPropName("bcpg:nutList");
					}
				}
				alfrescoRepository.save(securityRule);
				securityService.refreshAcls();
				productService.formulate(productNodeRef);
				FinishedProductData productData = (FinishedProductData) alfrescoRepository.findOne(productNodeRef);
				NodeRef nutListNodeRef = productData.getNutList().get(0).getParentNodeRef();
				assertFalse(permissionService.getInheritParentPermissions(nutListNodeRef));
				Set<AccessPermission> permissions = permissionService.getAllSetPermissions(nutListNodeRef);
				checkPermissions(productNodeRef, permissions, new ArrayList<>(List.of(grp2)), new ArrayList<>(List.of(grp1)));
				return null;
			});

			// nothing
			inWriteTx(() -> {
				ACLGroupData securityRule = (ACLGroupData) alfrescoRepository.findOne(securityRuleNodeRef);
				for (ACLEntryDataItem permissionList : securityRule.getAcls()) {
					permissionList.setPropName("cm:description");
				}
				alfrescoRepository.save(securityRule);
				securityService.refreshAcls();
				productService.formulate(productNodeRef);
				FinishedProductData productData = (FinishedProductData) alfrescoRepository.findOne(productNodeRef);
				NodeRef nutListNodeRef = productData.getNutList().get(0).getParentNodeRef();
				assertTrue(permissionService.getInheritParentPermissions(nutListNodeRef));
				Set<AccessPermission> permissions = permissionService.getAllSetPermissions(nutListNodeRef);
				checkPermissions(productNodeRef, permissions, new ArrayList<>(List.of()), new ArrayList<>(List.of()));
				return null;
			});

		} finally {
			inWriteTx(() -> {
				nodeService.deleteNode(securityRuleNodeRef);
				systemConfigurationService.resetConfValue("beCPG.formulation.security.enforceACL");
				return null;
			});
		}

	}

	@Test
	public void testDocumentViewPermissions() {

		initParts();

		inWriteTx(() -> {
			NodeRef ret = createDocumentViewSecurityRule();
			securityService.refreshAcls();
			return ret;
		});

		ProductData entityTpl = inWriteTx(() -> {
			FinishedProductData templateFinishedProduct = new FinishedProductData();
			templateFinishedProduct.setName("FP TPL");
			templateFinishedProduct.getAspects().add(BeCPGModel.ASPECT_ENTITY_TPL);
			return (ProductData) alfrescoRepository.create(getTestFolderNodeRef(), templateFinishedProduct);
		});

		NodeRef productNodeRef = inWriteTx(() -> {
			FinishedProductData productData = new FinishedProductData();
			productData.setParentNodeRef(getTestFolderNodeRef());
			productData.setName("FP testDocumentViewPermissions");
			productData.setEntityTpl(entityTpl);
			return alfrescoRepository.save(productData).getNodeRef();
		});

		// grp1 write and grp2 read
		Set<AccessPermission> beforeTemplatePermissions = inWriteTx(() -> {
			productService.formulate(productNodeRef);
			NodeRef folderNodeRef = nodeService.getChildByName(productNodeRef, ContentModel.ASSOC_CONTAINS, "Brief");
			assertFalse(permissionService.getInheritParentPermissions(folderNodeRef));
			return permissionService.getAllSetPermissions(folderNodeRef);
		});

		inWriteTx(() -> {
			NodeRef folderNodeRef = nodeService.getChildByName(entityTpl.getNodeRef(), ContentModel.ASSOC_CONTAINS, "Brief");
			permissionService.setInheritParentPermissions(folderNodeRef, false);
			permissionService.setPermission(folderNodeRef, "TEST_AUTHORITY", PermissionService.READ, true);
			return null;
		});

		inWriteTx(() -> {
			productService.formulate(productNodeRef);
			NodeRef folderNodeRef = nodeService.getChildByName(productNodeRef, ContentModel.ASSOC_CONTAINS, "Brief");
			assertFalse(permissionService.getInheritParentPermissions(folderNodeRef));
			Set<AccessPermission> permissions = permissionService.getAllSetPermissions(folderNodeRef);
			assertEquals(1, permissions.size());
			assertEquals("TEST_AUTHORITY", permissions.iterator().next().getAuthority());
			assertEquals(PermissionService.READ, permissions.iterator().next().getPermission());
			return null;
		});

		inWriteTx(() -> {
			NodeRef tplFolderNodeRef = nodeService.getChildByName(entityTpl.getNodeRef(), ContentModel.ASSOC_CONTAINS, "Brief");
			permissionService.setInheritParentPermissions(tplFolderNodeRef, true);
			permissionService.clearPermission(tplFolderNodeRef, "TEST_AUTHORITY");
			productService.formulate(productNodeRef);
			NodeRef folderNodeRef = nodeService.getChildByName(productNodeRef, ContentModel.ASSOC_CONTAINS, "Brief");
			assertFalse(permissionService.getInheritParentPermissions(folderNodeRef));
			Set<AccessPermission> permissions = permissionService.getAllSetPermissions(folderNodeRef);
			assertEquals(beforeTemplatePermissions, permissions);
			return null;
		});

	}

	private void checkPermissions(NodeRef productNodeRef, Set<AccessPermission> permissions, List<String> allowedRead, List<String> allowedWrite) {

		int readChecks = 0;
		int writeChecks = 0;

		for (AccessPermission permission : permissions) {
			if (AccessStatus.ALLOWED.equals(permission.getAccessStatus())) {
				if ((PermissionService.READ.equals(permission.getPermission()) || PermissionService.CONSUMER.equals(permission.getPermission()))
						&& (grp1.equals(permission.getAuthority()) || grp2.equals(permission.getAuthority()))) {
					assertTrue(allowedRead.contains(permission.getAuthority()));
					readChecks++;
				}
				if ((PermissionService.WRITE.equals(permission.getPermission()) || PermissionService.CONTRIBUTOR.equals(permission.getPermission()))
						&& (grp1.equals(permission.getAuthority()) || grp2.equals(permission.getAuthority()))) {
					assertTrue(allowedWrite.contains(permission.getAuthority()));
					writeChecks++;
				}
			}
		}

		assertEquals(allowedRead.size(), readChecks);
		assertEquals(allowedWrite.size(), writeChecks);

		checkPermissionsAreSameAfterReformulation(productNodeRef, permissions);
	}

	private NodeRef createDataListSecurityRule() {
		createUsers();
		List<NodeRef> group1s = new ArrayList<>();
		group1s.add(authorityService.getAuthorityNodeRef(grp1));
		List<NodeRef> group2s = new ArrayList<>();
		group2s.add(authorityService.getAuthorityNodeRef(grp2));

		ACLGroupData rmAclGroupData = new ACLGroupData();
		rmAclGroupData.setName("Test Datalist ACL");
		rmAclGroupData.setNodeType(PLMModel.TYPE_FINISHEDPRODUCT.toPrefixString(namespaceService));
		rmAclGroupData.setIsLocalPermission(false);

		List<ACLEntryDataItem> permissionList = new ArrayList<>();

		permissionList.add(new ACLEntryDataItem("bcpg:nutList", PermissionModel.READ_WRITE, group1s));
		permissionList.add(new ACLEntryDataItem("bcpg:nutList", PermissionModel.READ_ONLY, group2s));

		rmAclGroupData.setAcls(permissionList);

		return alfrescoRepository.create(getTestFolderNodeRef(), rmAclGroupData).getNodeRef();

	}

	private NodeRef createDocumentViewSecurityRule() {
		createUsers();
		List<NodeRef> group1s = new ArrayList<>();
		group1s.add(authorityService.getAuthorityNodeRef(grp1));
		List<NodeRef> group2s = new ArrayList<>();
		group2s.add(authorityService.getAuthorityNodeRef(grp2));

		ACLGroupData rmAclGroupData = new ACLGroupData();
		rmAclGroupData.setName("Test Document View ACL");
		rmAclGroupData.setNodeType(PLMModel.TYPE_FINISHEDPRODUCT.toPrefixString(namespaceService));
		rmAclGroupData.setIsLocalPermission(false);

		List<ACLEntryDataItem> permissionList = new ArrayList<>();

		permissionList.add(new ACLEntryDataItem("View-documents", PermissionModel.READ_WRITE, group1s));
		permissionList.add(new ACLEntryDataItem("View-documents", PermissionModel.READ_ONLY, group2s));

		rmAclGroupData.setAcls(permissionList);

		return alfrescoRepository.create(getTestFolderNodeRef(), rmAclGroupData).getNodeRef();

	}

	private void checkPermissionsAreSameAfterReformulation(NodeRef productNodeRef, Set<AccessPermission> expectedPermissions) {
		productService.formulate(productNodeRef);
		FinishedProductData productData = (FinishedProductData) alfrescoRepository.findOne(productNodeRef);
		NodeRef nutListNodeRef = productData.getNutList().get(0).getParentNodeRef();
		Set<AccessPermission> permissions = permissionService.getAllSetPermissions(nutListNodeRef);
		assertEquals(expectedPermissions, permissions);
	}

	@Test
	public void testDatalistEnforceACL() {

		inWriteTx(() -> {
			systemConfigurationService.updateConfValue("beCPG.formulation.security.enforceACL", "true");
			return null;
		});

		initParts();

		NodeRef securityRuleNodeRef = inWriteTx(this::createDataListSecurityRuleEnforceACL);

		try {

			inWriteTx(() -> {
				permissionService.setInheritParentPermissions(getTestFolderNodeRef(), false);
				permissionService.setPermission(getTestFolderNodeRef(), grp1, PermissionService.READ, true);
				permissionService.setPermission(getTestFolderNodeRef(), grp2, PermissionService.READ, true);
				return null;
			});

			NodeRef productNodeRef = inWriteTx(() -> {
				FinishedProductData productData = new FinishedProductData();
				productData.setParentNodeRef(getTestFolderNodeRef());
				productData.setName("FP testDataListPermissions");
				List<NutListDataItem> nutList = new LinkedList<>();
				nutList.add(NutListDataItem.build().withValue(1d).withUnit(null).withMini(null).withMaxi(null).withGroup(null).withNut(nut1));
				productData.setNutList(nutList);
				return alfrescoRepository.save(productData).getNodeRef();
			});

			// grp1 write and grp2 read
			inWriteTx(() -> {
				productService.formulate(productNodeRef);
				FinishedProductData productData = (FinishedProductData) alfrescoRepository.findOne(productNodeRef);
				NodeRef nutListNodeRef = productData.getNutList().get(0).getParentNodeRef();
				assertFalse(permissionService.getInheritParentPermissions(nutListNodeRef));
				Set<AccessPermission> permissions = permissionService.getAllSetPermissions(nutListNodeRef);
				checkPermissions(productNodeRef, permissions, new ArrayList<>(List.of(grp2)), new ArrayList<>(List.of(grp1)));
				return null;
			});

		} finally {
			inWriteTx(() -> {
				nodeService.deleteNode(securityRuleNodeRef);
				systemConfigurationService.resetConfValue("beCPG.formulation.security.enforceACL");
				return null;
			});
		}

	}

	private NodeRef createDataListSecurityRuleEnforceACL() {
		createUsers();
		List<NodeRef> group1s = new ArrayList<>();
		group1s.add(authorityService.getAuthorityNodeRef(grp1));
		List<NodeRef> group2s = new ArrayList<>();
		group2s.add(authorityService.getAuthorityNodeRef(grp2));

		ACLGroupData rmAclGroupData = new ACLGroupData();
		rmAclGroupData.setName("Test Datalist ACL");
		rmAclGroupData.setNodeType(PLMModel.TYPE_FINISHEDPRODUCT.toPrefixString(namespaceService));
		rmAclGroupData.setIsLocalPermission(false);

		List<ACLEntryDataItem> permissionList = new ArrayList<>();

		permissionList.add(new ACLEntryDataItem("bcpg:nutList", PermissionModel.READ_WRITE, group1s, true));
		permissionList.add(new ACLEntryDataItem("bcpg:nutList", PermissionModel.READ_WRITE, group2s, false));

		rmAclGroupData.setAcls(permissionList);

		return alfrescoRepository.create(getTestFolderNodeRef(), rmAclGroupData).getNodeRef();
	}
}
