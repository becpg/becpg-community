/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.product.helper;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.repo.product.data.CompoListView;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * Unit tests of {@link AllocationHelper}: the raw material allocations of a composition.
 *
 * @author matthieu
 */
public class AllocationHelperTest {

	private static final NodeRef FINISHED = new NodeRef("workspace://SpacesStore/finished");

	private static final NodeRef SEMI_FINISHED = new NodeRef("workspace://SpacesStore/semi-finished");

	private static final NodeRef FLOUR = new NodeRef("workspace://SpacesStore/flour");

	private static final NodeRef SUGAR = new NodeRef("workspace://SpacesStore/sugar");

	private AlfrescoRepository<BeCPGDataObject> repository;

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() {
		repository = mock(AlfrescoRepository.class);
		register(new RawMaterialData(), FLOUR);
		register(new RawMaterialData(), SUGAR);
	}

	private <T extends ProductData> T register(T product, NodeRef nodeRef, CompoListDataItem... compo) {
		product.setNodeRef(nodeRef);
		product.setName(nodeRef.getId());
		product.setNetWeight(1d);
		CompoListView view = new CompoListView();
		view.setCompoList(new ArrayList<>(List.of(compo)));
		product.setCompoListView(view);
		when(repository.findOne(nodeRef)).thenReturn(product);
		return product;
	}

	private static CompoListDataItem line(NodeRef component, double qty) {
		return CompoListDataItem.build().withProduct(component).withQty(qty).withQtyUsed(qty).withUnit(ProductUnit.kg);
	}

	private Map<NodeRef, Double> allocationsOf(ProductData product) {
		return AllocationHelper.extractAllocations(product, new LinkedHashMap<>(), 1d, repository);
	}

	@Test
	public void testRawMaterialsOfASemiFinishedAreAllocated() {
		register(new SemiFinishedProductData(), SEMI_FINISHED, line(FLOUR, 0.4d), line(SUGAR, 0.6d));
		FinishedProductData finished = register(new FinishedProductData(), FINISHED, line(SEMI_FINISHED, 1d));

		Map<NodeRef, Double> allocations = allocationsOf(finished);

		assertEquals(0.4d, allocations.get(FLOUR), 1e-9);
		assertEquals(0.6d, allocations.get(SUGAR), 1e-9);
	}

	@Test
	public void testCyclicCompositionIsCutInsteadOfOverflowingTheStack() {
		register(new SemiFinishedProductData(), SEMI_FINISHED, line(FINISHED, 0.5d), line(FLOUR, 0.5d));
		FinishedProductData finished = register(new FinishedProductData(), FINISHED, line(SEMI_FINISHED, 1d));

		Map<NodeRef, Double> allocations = allocationsOf(finished);

		assertEquals("the raw material outside the cycle is still allocated", 0.5d, allocations.get(FLOUR), 1e-9);
		assertEquals(1, allocations.size());
	}

	@Test
	public void testProductContainingItselfIsCut() {
		FinishedProductData finished = register(new FinishedProductData(), FINISHED, line(FINISHED, 1d), line(SUGAR, 1d));

		assertEquals(1d, allocationsOf(finished).get(SUGAR), 1e-9);
	}

	@Test
	public void testComponentUsedInTwoBranchesIsCountedInBoth() {
		NodeRef otherSemiFinished = new NodeRef("workspace://SpacesStore/other-semi-finished");
		register(new SemiFinishedProductData(), SEMI_FINISHED, line(FLOUR, 1d));
		register(new SemiFinishedProductData(), otherSemiFinished, line(SEMI_FINISHED, 1d));
		FinishedProductData finished = register(new FinishedProductData(), FINISHED, line(SEMI_FINISHED, 0.3d), line(otherSemiFinished, 0.7d));

		assertEquals(1d, allocationsOf(finished).get(FLOUR), 1e-9);
	}

}
