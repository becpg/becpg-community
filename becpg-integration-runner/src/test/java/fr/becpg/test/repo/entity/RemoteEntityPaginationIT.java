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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.entity.remote.RemoteParams;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.test.PLMBaseTestCase;

/**
 * Integration test for the pagination published by <code>becpg/remote/entity/list</code>.
 * <p>
 * The transactional query engine stops reading one row past the requested page, so the number of
 * matches it reports is only a lower bound as soon as another page follows: on a page of 256 it
 * announces 257, on the next one 513. A caller paging through the result set must never be told
 * about an entity the endpoint cannot hand back, so a total is published only once it is known,
 * either because the whole result set was read or because the index counted it.
 *
 * @author matthieu
 */
public class RemoteEntityPaginationIT extends PLMBaseTestCase {

	private static final int PRODUCT_COUNT = 5;
	private static final int PAGE_SIZE = 2;

	private static final String PRODUCT_NAME_PREFIX = "Paged product ";

	private static final String KEY_PAGINATION = "pagination";
	private static final String PAGINATION_COUNT = "count";
	private static final String PAGINATION_TOTAL_ITEMS = "totalItems";
	private static final String PAGINATION_HAS_MORE_ITEMS = "hasMoreItems";

	@Autowired
	private RemoteEntityService remoteEntityService;

	@Test
	public void testIndexCountsWhatTheDatabaseStopsCounting() {
		createProducts();
		waitForSolr();

		inReadTx(() -> {
			Assert.assertEquals(PRODUCT_COUNT, queryProducts().indexedCount().intValue());
			return null;
		});
	}

	@Test
	public void testPagingNeverAnnouncesATotalItCannotHandBack() {
		createProducts();
		waitForSolr();

		inReadTx(() -> {
			Set<NodeRef> readBack = new HashSet<>();

			for (int page = 1; page <= PRODUCT_COUNT; page++) {
				PagingResults<NodeRef> results = queryProducts().maxResults(PAGE_SIZE).page(page).inDBIfPossible().pagingResults();
				readBack.addAll(results.getPage());

				Pair<Integer, Integer> totalResultCount = results.getTotalResultCount();
				if (totalResultCount.getSecond() != null) {
					Assert.assertEquals("an announced total is the number of entities the caller can read back", PRODUCT_COUNT,
							totalResultCount.getSecond().intValue());
				}

				if (!results.hasMoreItems()) {
					break;
				}
			}

			Assert.assertEquals(PRODUCT_COUNT, readBack.size());
			return null;
		});
	}

	@Test
	public void testPageCountAndTotalAreWrittenApart() {
		List<NodeRef> productNodeRefs = createProducts();

		inReadTx(() -> {
			JSONObject pagination = extractPagination(productNodeRefs.subList(0, PAGE_SIZE), true, PRODUCT_COUNT);

			Assert.assertEquals(PAGE_SIZE, pagination.getInt(PAGINATION_COUNT));
			Assert.assertEquals(PRODUCT_COUNT, pagination.getInt(PAGINATION_TOTAL_ITEMS));
			Assert.assertTrue(pagination.getBoolean(PAGINATION_HAS_MORE_ITEMS));
			return null;
		});
	}

	@Test
	public void testUnknownTotalIsLeftOutOfThePagination() {
		List<NodeRef> productNodeRefs = createProducts();

		inReadTx(() -> {
			JSONObject pagination = extractPagination(productNodeRefs.subList(0, PAGE_SIZE), true, null);

			Assert.assertEquals(PAGE_SIZE, pagination.getInt(PAGINATION_COUNT));
			Assert.assertFalse("an unknown total must not be published", pagination.has(PAGINATION_TOTAL_ITEMS));
			return null;
		});
	}

	private List<NodeRef> createProducts() {
		return inWriteTx(() -> {
			List<NodeRef> nodeRefs = new ArrayList<>();
			for (int i = 0; i < PRODUCT_COUNT; i++) {
				FinishedProductData product = new FinishedProductData();
				product.setName(PRODUCT_NAME_PREFIX + i);
				nodeRefs.add(alfrescoRepository.create(getTestFolderNodeRef(), product).getNodeRef());
			}
			return nodeRefs;
		});
	}

	private BeCPGQueryBuilder queryProducts() {
		return BeCPGQueryBuilder.createQuery().inParent(getTestFolderNodeRef()).ofType(PLMModel.TYPE_FINISHEDPRODUCT);
	}

	private JSONObject extractPagination(List<NodeRef> page, boolean hasMoreItems, Integer totalItems) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		remoteEntityService.listEntities(asPagingResults(page, hasMoreItems, totalItems), out, new RemoteParams(RemoteEntityFormat.json));

		return new JSONObject(out.toString(StandardCharsets.UTF_8)).getJSONObject(KEY_PAGINATION);
	}

	private PagingResults<NodeRef> asPagingResults(List<NodeRef> page, boolean hasMoreItems, Integer totalItems) {
		return new PagingResults<NodeRef>() {

			@Override
			public List<NodeRef> getPage() {
				return page;
			}

			@Override
			public boolean hasMoreItems() {
				return hasMoreItems;
			}

			@Override
			public Pair<Integer, Integer> getTotalResultCount() {
				return new Pair<>(page.size(), totalItems);
			}

			@Override
			public String getQueryExecutionId() {
				return null;
			}
		};
	}

}
