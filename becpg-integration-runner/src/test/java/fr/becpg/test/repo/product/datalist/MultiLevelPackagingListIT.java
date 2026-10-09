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
package fr.becpg.test.repo.product.datalist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.datalist.DataListExtractor;
import fr.becpg.repo.entity.datalist.DataListExtractorFactory;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.helper.impl.AttributeExtractorField;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

/**
 * Reproduces the display defect reported on #31701: in the multi-level packaging list, the
 * composition rows injected through the secondary pivot showed the kg-normalized quantity
 * (0.3) next to their own unit (g), reading as "0.3 g" instead of "300 g".
 */
public class MultiLevelPackagingListIT extends AbstractFinishedProductTest {

	private static final String PACKAGING_LIST = "packagingList";

	private static final String COMPOLIST_ITEM_TYPE = "bcpg:compoList";

	private static final String QTY_FIELD = "prop_bcpg_packagingListQty";

	private static final String UNIT_FIELD = "prop_bcpg_packagingListUnit";

	private static final int PAGE_SIZE = 100;

	@Autowired
	private DataListExtractorFactory dataListExtractorFactory;

	private NodeRef semiFinishedNodeRef;

	private NodeRef finishedProductNodeRef;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		initParts();
		semiFinishedNodeRef = createSemiFinishedWithPackaging();
		finishedProductNodeRef = createFinishedProductUsing(semiFinishedNodeRef);
	}

	/**
	 * Creates a semi finished product carrying its own secondary packaging, so that it shows up
	 * as a parent row in the packaging list of the products using it.
	 */
	private NodeRef createSemiFinishedWithPackaging() {
		return inWriteTx(() -> {
			SemiFinishedProductData semiFinished = new SemiFinishedProductData();
			semiFinished.setName("SF with its own caisse");
			semiFinished.setUnit(ProductUnit.kg);
			semiFinished.setQty(1d);
			semiFinished.setDensity(1d);

			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial5NodeRef));
			semiFinished.getCompoListView().setCompoList(compoList);

			List<PackagingListDataItem> packagingList = new ArrayList<>();
			packagingList.add(PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.P).withPkgLevel(PackagingLevel.Secondary)
					.withIsMaster(true).withProduct(packagingMaterial4NodeRef));
			semiFinished.getPackagingListView().setPackagingList(packagingList);

			return alfrescoRepository.create(getTestFolderNodeRef(), semiFinished).getNodeRef();
		});
	}

	/**
	 * Creates a finished product using 300 g of the given semi finished product.
	 */
	private NodeRef createFinishedProductUsing(NodeRef subProductNodeRef) {
		return inWriteTx(() -> {
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("FP using 300 g of SF");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(1d);
			finishedProduct.setDensity(1d);

			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withQtyUsed(300d).withUnit(ProductUnit.g).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(subProductNodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);

			List<PackagingListDataItem> packagingList = new ArrayList<>();
			packagingList.add(PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.P).withPkgLevel(PackagingLevel.Primary)
					.withIsMaster(true).withProduct(packagingMaterial1NodeRef));
			finishedProduct.getPackagingListView().setPackagingList(packagingList);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		});
	}

	@Test
	public void testCompositionRowKeepsTheQuantityOfItsOwnUnit() {

		inWriteTx(() -> {
			productService.formulate(finishedProductNodeRef);
			return null;
		});

		// The extractor stores the requested depth as a user preference, hence a write transaction
		Map<String, Object> compositionRow = inWriteTx(this::extractCompositionRow);

		assertNotNull("The composition row must be injected in the multi-level packaging list", compositionRow);

		@SuppressWarnings("unchecked")
		Map<String, Object> qty = (Map<String, Object>) compositionRow.get(QTY_FIELD);
		@SuppressWarnings("unchecked")
		Map<String, Object> unit = (Map<String, Object>) compositionRow.get(UNIT_FIELD);

		assertEquals("The quantity must be expressed in the unit displayed next to it", 300d,
				((Number) qty.get("value")).doubleValue(), 0.0001d);
		assertEquals("The unit must remain the one of the composition row", ProductUnit.g.toString(), String.valueOf(unit.get("value")));
	}

	/**
	 * Extracts the multi-level packaging list of the finished product and returns the data of the
	 * row standing for the semi finished product, that is the only composition row of the list.
	 */
	private Map<String, Object> extractCompositionRow() {
		DataListFilter dataListFilter = new DataListFilter();
		dataListFilter.setDataListName(PACKAGING_LIST);
		dataListFilter.setDataType(PLMModel.TYPE_PACKAGINGLIST);
		dataListFilter.setEntityNodeRefs(Collections.singletonList(finishedProductNodeRef));
		dataListFilter.updateMaxDepth(-1);
		dataListFilter.setHasWriteAccess(true);
		dataListFilter.getPagination().setMaxResults(-1);
		dataListFilter.getPagination().setPageSize(PAGE_SIZE);

		List<AttributeExtractorField> metadataFields = new LinkedList<>();
		metadataFields.add(new AttributeExtractorField(PLMModel.PROP_PACKAGINGLIST_QTY.toPrefixString(namespaceService), null));
		metadataFields.add(new AttributeExtractorField(PLMModel.PROP_PACKAGINGLIST_UNIT.toPrefixString(namespaceService), null));

		DataListExtractor extractor = dataListExtractorFactory.getExtractor(dataListFilter);
		PaginatedExtractedItems extractedItems = extractor.extract(dataListFilter, metadataFields);

		for (Map<String, Object> item : extractedItems.getPageItems()) {
			if (COMPOLIST_ITEM_TYPE.equals(item.get("itemType"))) {
				@SuppressWarnings("unchecked")
				Map<String, Object> itemData = (Map<String, Object>) item.get("itemData");
				return itemData;
			}
		}

		return null;
	}

}
