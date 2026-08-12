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
package fr.becpg.test.repo.entity;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.query.ListBackedPagingResults;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.entity.remote.RemoteParams;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.SupplierData;
import fr.becpg.test.BeCPGPLMTestHelper;
import fr.becpg.test.PLMBaseTestCase;

/**
 * Integration test for <code>becpg/remote/entity/list</code>, which reads node metadata through the
 * internal <code>nodeService</code> rather than the public one.
 * <p>
 * Two things must hold for that shortcut to be a correct one: a row must be serialized exactly as
 * the single-entity endpoint serializes it — a property arriving as a raw multilingual value is
 * what a wrong choice of bean would look like — and an association target, reached by traversal and
 * filtered by nothing, must still be refused to a caller who may not read it.
 *
 * @author matthieu
 */
public class RemoteEntityListingIT extends PLMBaseTestCase {

	private static final String PRODUCT_NAME = "Listed product";
	private static final String PRODUCT_TITLE = "Titre du produit listé";
	private static final String SUPPLIER_NAME = "Supplier out of the caller's reach";
	private static final String ACCESS_DENIED_VALUE = "#AccessDenied";

	private static final String FIELD_TITLE = "cm:title";
	private static final String FIELD_SUPPLIERS = "bcpg:suppliers";
	private static final String KEY_ENTITIES = "entities";
	private static final String KEY_ATTRIBUTES = "attributes";
	private static final String KEY_ENTITY = "entity";
	private static final String KEY_NAME = "cm:name";

	@Autowired
	private RemoteEntityService remoteEntityService;

	@Autowired
	private AssociationService associationService;

	@Autowired
	@Qualifier("namespaceService")
	private NamespaceService remoteNamespaceService;

	@Test
	public void testListingRendersPropertiesAsTheSingleEntityEndpointDoes() {
		NodeRef productNodeRef = inWriteTx(() -> {
			NodeRef nodeRef = createProduct();
			nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, PRODUCT_TITLE);
			return nodeRef;
		});

		inReadTx(() -> {
			JSONObject listed = extractListedEntity(productNodeRef, FIELD_TITLE);
			JSONObject single = extractSingleEntity(productNodeRef, FIELD_TITLE);

			Assert.assertEquals(PRODUCT_NAME, listed.getString(KEY_NAME));
			Assert.assertEquals(PRODUCT_TITLE, listed.getJSONObject(KEY_ATTRIBUTES).getString(FIELD_TITLE));
			Assert.assertEquals(single.getJSONObject(KEY_ATTRIBUTES).getString(FIELD_TITLE),
					listed.getJSONObject(KEY_ATTRIBUTES).getString(FIELD_TITLE));
			return null;
		});
	}

	@Test
	public void testListingDeniesAnAssociationTargetTheCallerCannotRead() {
		NodeRef supplierNodeRef = inWriteTx(() -> {
			SupplierData supplier = new SupplierData();
			supplier.setName(SUPPLIER_NAME);
			return alfrescoRepository.create(getTestFolderNodeRef(), supplier).getNodeRef();
		});

		NodeRef productNodeRef = inWriteTx(() -> {
			NodeRef nodeRef = createProduct();

			BeCPGPLMTestHelper.createUser(BeCPGPLMTestHelper.USER_ONE);
			associationService.update(nodeRef, PLMModel.ASSOC_SUPPLIERS, List.of(supplierNodeRef));
			permissionService.setPermission(nodeRef, BeCPGPLMTestHelper.USER_ONE, PermissionService.READ, true);
			permissionService.setInheritParentPermissions(supplierNodeRef, false);
			return nodeRef;
		});

		AuthenticationUtil.runAs(() -> inReadTx(() -> {
			JSONObject listed = extractListedEntity(productNodeRef, FIELD_SUPPLIERS);
			JSONObject supplier = listed.getJSONObject(KEY_ATTRIBUTES).getJSONArray(FIELD_SUPPLIERS).getJSONObject(0);

			Assert.assertEquals(ACCESS_DENIED_VALUE, supplier.getString(KEY_NAME));
			Assert.assertFalse(supplier.toString().contains(SUPPLIER_NAME));
			return null;
		}), BeCPGPLMTestHelper.USER_ONE);
	}

	private NodeRef createProduct() {
		FinishedProductData product = new FinishedProductData();
		product.setName(PRODUCT_NAME);
		return alfrescoRepository.create(getTestFolderNodeRef(), product).getNodeRef();
	}

	private JSONObject extractListedEntity(NodeRef nodeRef, String field) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		remoteEntityService.listEntities(new ListBackedPagingResults<>(List.of(nodeRef)), out, buildParams(field));

		JSONObject root = new JSONObject(out.toString(StandardCharsets.UTF_8));
		return root.getJSONArray(KEY_ENTITIES).getJSONObject(0).getJSONObject(KEY_ENTITY);
	}

	private JSONObject extractSingleEntity(NodeRef nodeRef, String field) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		remoteEntityService.getEntity(nodeRef, out, buildParams(field));

		return new JSONObject(out.toString(StandardCharsets.UTF_8)).getJSONObject(KEY_ENTITY);
	}

	private RemoteParams buildParams(String field) {
		RemoteParams params = new RemoteParams(RemoteEntityFormat.json);
		params.setFilteredFields(Set.of(field), remoteNamespaceService);
		return params;
	}

}
