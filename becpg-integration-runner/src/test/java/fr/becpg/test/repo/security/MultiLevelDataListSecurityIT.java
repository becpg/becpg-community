/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.PLMModel;
import fr.becpg.model.SecurityModel;
import fr.becpg.model.SystemGroup;
import fr.becpg.repo.entity.datalist.DataListExtractor;
import fr.becpg.repo.entity.datalist.DataListExtractorFactory;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.impl.AbstractDataListExtractor;
import fr.becpg.repo.helper.impl.AttributeExtractorField;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.security.SecurityService;
import fr.becpg.repo.security.data.ACLGroupData;
import fr.becpg.repo.security.data.PermissionModel;
import fr.becpg.repo.security.data.dataList.ACLEntryDataItem;
import fr.becpg.test.BeCPGTestHelper;
import fr.becpg.test.PLMBaseTestCase;

/**
 * Reproduces the security defect reported on #36462: a sub product whose composition is protected by a security rule
 * stays editable once it is expanded in the multi-level composition of a product the user may write.
 *
 * @author matthieu
 */
public class MultiLevelDataListSecurityIT extends PLMBaseTestCase {

	private static final String USER_NAME = "matthieu_multilevel_secu";

	private static final String GROUP_NAME = "GRP_MULTILEVEL_SECU";

	private static final String EDIT_ACCESS = "edit";

	private static final int PAGE_SIZE = 100;

	@Autowired
	private DataListExtractorFactory dataListExtractorFactory;

	@Autowired
	private SecurityService securityService;

	private NodeRef protectedSubProductNodeRef;

	private NodeRef finishedProductNodeRef;

	@Test
	public void testProtectedSubProductRowsStayReadOnly() {

		createTestUser();

		NodeRef aclGroupNodeRef = inWriteTx(this::createReadOnlyCompoListACLGroup);

		inWriteTx(() -> {
			securityService.refreshAcls();
			return null;
		});

		createProducts(aclGroupNodeRef);

		waitForSolr();

		// The extractor stores the requested depth as a user preference, hence a write transaction
		List<Map<String, Object>> rows = AuthenticationUtil.runAs(() -> inWriteTx(this::extractMultiLevelCompoList), USER_NAME);

		Assert.assertEquals("The whole composition must be extracted", 2, rows.size());

		Assert.assertTrue("The rows of the product on screen must stay editable", hasEditAccess(rows.get(0)));
		Assert.assertFalse("The rows of a sub product protected by a security rule must be read only", hasEditAccess(rows.get(1)));
	}

	private void createTestUser() {
		inWriteTx(() -> {
			String groupName = authorityService.authorityExists(PermissionService.GROUP_PREFIX + GROUP_NAME)
					? PermissionService.GROUP_PREFIX + GROUP_NAME
					: authorityService.createAuthority(AuthorityType.GROUP, GROUP_NAME);

			if (!authenticationDAO.userExists(USER_NAME)) {
				BeCPGTestHelper.createUser(USER_NAME);
			}

			addToGroup(groupName);
			addToGroup(PermissionService.GROUP_PREFIX + SystemGroup.LicenseWriteNamed.toString());
			permissionService.setPermission(getTestFolderNodeRef(), USER_NAME, PermissionService.COORDINATOR, true);

			return null;
		});
	}

	private void addToGroup(String groupName) {
		if (!authorityService.getAuthoritiesForUser(USER_NAME).contains(groupName)) {
			authorityService.addAuthority(groupName, USER_NAME);
		}
	}

	/**
	 * Build a local ACL group making the composition read only for the test group, the way a validated product is
	 * protected on a customer instance.
	 *
	 * @return the ACL group node reference
	 */
	private NodeRef createReadOnlyCompoListACLGroup() {
		List<NodeRef> groups = new ArrayList<>();
		groups.add(authorityService.getAuthorityNodeRef(PermissionService.GROUP_PREFIX + GROUP_NAME));

		ACLGroupData aclGroupData = new ACLGroupData();
		aclGroupData.setName("Read only compoList ACL");
		aclGroupData.setNodeType(PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT.toPrefixString(namespaceService));
		aclGroupData.setIsLocalPermission(true);

		List<ACLEntryDataItem> acls = new ArrayList<>();
		acls.add(new ACLEntryDataItem(PLMModel.TYPE_COMPOLIST.toPrefixString(namespaceService), PermissionModel.READ_ONLY, groups));
		aclGroupData.setAcls(acls);

		return alfrescoRepository.create(getTestFolderNodeRef(), aclGroupData).getNodeRef();
	}

	private void createProducts(NodeRef aclGroupNodeRef) {
		inWriteTx(() -> {
			RawMaterialData rawMaterial = new RawMaterialData();
			rawMaterial.setName("Raw material of the protected sub product");
			NodeRef rawMaterialNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial).getNodeRef();

			LocalSemiFinishedProductData subProduct = new LocalSemiFinishedProductData();
			subProduct.setName("Protected sub product");
			subProduct.getCompoListView().setCompoList(Collections.singletonList(compoListItem(rawMaterialNodeRef, null)));
			protectedSubProductNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), subProduct).getNodeRef();

			nodeService.createAssociation(protectedSubProductNodeRef, aclGroupNodeRef, SecurityModel.ASSOC_SECURITY_REF);

			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Finished product on screen");
			List<CompoListDataItem> compoList = new LinkedList<>();
			compoList.add(compoListItem(protectedSubProductNodeRef, null));
			finishedProduct.getCompoListView().setCompoList(compoList);
			finishedProductNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

			return null;
		});
	}

	private CompoListDataItem compoListItem(NodeRef productNodeRef, CompoListDataItem parent) {
		return CompoListDataItem.build().withParent(parent).withQty(1d).withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
				.withDeclarationType(DeclarationType.Declare).withProduct(productNodeRef);
	}

	/**
	 * Extract the composition of the finished product, expanded down to the composition of its sub product.
	 *
	 * @return the extracted rows, the first one standing for the sub product and the second one for its raw material
	 */
	private List<Map<String, Object>> extractMultiLevelCompoList() {
		DataListFilter dataListFilter = new DataListFilter();
		dataListFilter.setDataListName(PLMModel.TYPE_COMPOLIST.getLocalName());
		dataListFilter.setDataType(PLMModel.TYPE_COMPOLIST);
		dataListFilter.setEntityNodeRefs(Collections.singletonList(finishedProductNodeRef));
		dataListFilter.updateMaxDepth(-1);
		dataListFilter.setHasWriteAccess(true);
		dataListFilter.getPagination().setMaxResults(-1);
		dataListFilter.getPagination().setPageSize(PAGE_SIZE);

		List<AttributeExtractorField> metadataFields = new LinkedList<>();
		metadataFields.add(new AttributeExtractorField(PLMModel.PROP_COMPOLIST_QTY.toPrefixString(namespaceService), null));

		DataListExtractor extractor = dataListExtractorFactory.getExtractor(dataListFilter);
		PaginatedExtractedItems extractedItems = extractor.extract(dataListFilter, metadataFields);

		return extractedItems.getPageItems();
	}

	private boolean hasEditAccess(Map<String, Object> row) {
		@SuppressWarnings("unchecked")
		Map<String, Map<String, Boolean>> permissions = (Map<String, Map<String, Boolean>>) row.get(AbstractDataListExtractor.PROP_PERMISSIONS);

		return Boolean.TRUE.equals(permissions.get(AbstractDataListExtractor.PROP_USERACCESS).get(EDIT_ACCESS));
	}

}
