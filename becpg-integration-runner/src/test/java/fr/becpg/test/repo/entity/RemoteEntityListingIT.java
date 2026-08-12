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
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.entity.remote.RemoteParams;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.test.PLMBaseTestCase;

/**
 * Integration test for <code>becpg/remote/entity/list</code>, which reads node metadata through the
 * internal <code>nodeService</code> rather than the public one.
 * <p>
 * That bean keeps the multilingual and nodeRef interceptors and drops only the security one, so a
 * row must come out of a listing serialized exactly as the single-entity endpoint serializes it.
 * A property arriving as a raw multilingual value instead of the text of the caller's locale is
 * what a wrong choice of bean would look like.
 *
 * @author matthieu
 */
public class RemoteEntityListingIT extends PLMBaseTestCase {

	private static final String PRODUCT_NAME = "Listed product";
	private static final String PRODUCT_TITLE = "Titre du produit listé";

	private static final String FIELD_TITLE = "cm:title";
	private static final String KEY_ENTITIES = "entities";
	private static final String KEY_ATTRIBUTES = "attributes";
	private static final String KEY_ENTITY = "entity";
	private static final String KEY_NAME = "cm:name";

	@Autowired
	private RemoteEntityService remoteEntityService;

	@Autowired
	@Qualifier("namespaceService")
	private NamespaceService remoteNamespaceService;

	@Test
	public void testListingRendersPropertiesAsTheSingleEntityEndpointDoes() {
		NodeRef productNodeRef = inWriteTx(() -> {
			FinishedProductData product = new FinishedProductData();
			product.setName(PRODUCT_NAME);
			NodeRef nodeRef = alfrescoRepository.create(getTestFolderNodeRef(), product).getNodeRef();
			nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, PRODUCT_TITLE);
			return nodeRef;
		});

		inReadTx(() -> {
			JSONObject listed = extractListedEntity(productNodeRef);
			JSONObject single = extractSingleEntity(productNodeRef);

			Assert.assertEquals(PRODUCT_NAME, listed.getString(KEY_NAME));
			Assert.assertEquals(PRODUCT_TITLE, listed.getJSONObject(KEY_ATTRIBUTES).getString(FIELD_TITLE));
			Assert.assertEquals(single.getJSONObject(KEY_ATTRIBUTES).getString(FIELD_TITLE),
					listed.getJSONObject(KEY_ATTRIBUTES).getString(FIELD_TITLE));
			return null;
		});
	}

	private JSONObject extractListedEntity(NodeRef nodeRef) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		remoteEntityService.listEntities(new ListBackedPagingResults<>(List.of(nodeRef)), out, buildParams());

		JSONObject root = new JSONObject(out.toString(StandardCharsets.UTF_8));
		return root.getJSONArray(KEY_ENTITIES).getJSONObject(0).getJSONObject(KEY_ENTITY);
	}

	private JSONObject extractSingleEntity(NodeRef nodeRef) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		remoteEntityService.getEntity(nodeRef, out, buildParams());

		return new JSONObject(out.toString(StandardCharsets.UTF_8)).getJSONObject(KEY_ENTITY);
	}

	private RemoteParams buildParams() {
		RemoteParams params = new RemoteParams(RemoteEntityFormat.json);
		params.setFilteredFields(Set.of(FIELD_TITLE), remoteNamespaceService);
		return params;
	}

}
