package fr.becpg.repo.product.formulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;

import fr.becpg.repo.product.data.productList.SpecCompatibilityDataItem;
import fr.becpg.repo.regulatory.RequirementType;

/**
 * Unit tests of the update of the non-compatible products list of a product specification.
 */
public class SpecCompatibilityListUpdaterTest {

	private static final NodeRef RETESTED_PRODUCT = new NodeRef("workspace://SpacesStore/retested");

	private static final NodeRef SKIPPED_PRODUCT = new NodeRef("workspace://SpacesStore/skipped");

	private static final NodeRef OUT_OF_SCOPE_PRODUCT = new NodeRef("workspace://SpacesStore/outOfScope");

	private static final String PREVIOUS_DETAILS = "Cadmium above 0.30";

	private static final String NEW_DETAILS = "Cadmium above 0.15";

	@Test
	public void newRowOfRetestedProductSurvivesTheEndOfTheBatch() {
		List<SpecCompatibilityDataItem> specCompatibilityList = new ArrayList<>();

		SpecCompatibilityListUpdater.replaceProductRows(specCompatibilityList, RETESTED_PRODUCT, List.of(row(RETESTED_PRODUCT, NEW_DETAILS)));
		SpecCompatibilityListUpdater.removeObsoleteRows(specCompatibilityList, Set.of(), Set.of(RETESTED_PRODUCT));

		assertEquals(1, specCompatibilityList.size());
		assertEquals(NEW_DETAILS, specCompatibilityList.get(0).getReqDetails());
	}

	@Test
	public void retestedProductStillNonCompliantIsListedOnce() {
		List<SpecCompatibilityDataItem> specCompatibilityList = new ArrayList<>(List.of(row(RETESTED_PRODUCT, PREVIOUS_DETAILS)));

		SpecCompatibilityListUpdater.replaceProductRows(specCompatibilityList, RETESTED_PRODUCT, List.of(row(RETESTED_PRODUCT, NEW_DETAILS)));

		assertEquals(1, specCompatibilityList.size());
		assertEquals(NEW_DETAILS, specCompatibilityList.get(0).getReqDetails());
	}

	@Test
	public void retestedProductNowCompliantLeavesTheList() {
		List<SpecCompatibilityDataItem> specCompatibilityList = new ArrayList<>(List.of(row(RETESTED_PRODUCT, PREVIOUS_DETAILS)));

		SpecCompatibilityListUpdater.replaceProductRows(specCompatibilityList, RETESTED_PRODUCT, List.of());
		SpecCompatibilityListUpdater.removeObsoleteRows(specCompatibilityList, Set.of(), Set.of());

		assertTrue(specCompatibilityList.isEmpty());
	}

	@Test
	public void skippedProductKeepsItsPreviousRow() {
		List<SpecCompatibilityDataItem> specCompatibilityList = new ArrayList<>(List.of(row(SKIPPED_PRODUCT, PREVIOUS_DETAILS)));

		SpecCompatibilityListUpdater.removeObsoleteRows(specCompatibilityList, Set.of(SKIPPED_PRODUCT), Set.of());

		assertEquals(1, specCompatibilityList.size());
	}

	@Test
	public void productThatLeftTheScopeLeavesTheList() {
		List<SpecCompatibilityDataItem> specCompatibilityList = new ArrayList<>(List.of(row(OUT_OF_SCOPE_PRODUCT, PREVIOUS_DETAILS)));

		SpecCompatibilityListUpdater.removeObsoleteRows(specCompatibilityList, Set.of(SKIPPED_PRODUCT), Set.of(RETESTED_PRODUCT));

		assertTrue(specCompatibilityList.isEmpty());
	}

	private static SpecCompatibilityDataItem row(NodeRef productNodeRef, String reqDetails) {
		return new SpecCompatibilityDataItem(RequirementType.Forbidden, reqDetails, productNodeRef);
	}
}
