package fr.becpg.repo.ecm.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;

import fr.becpg.repo.ecm.data.dataList.WUsedListDataItem;

/**
 * Unit tests of the detection of where used lines whose list item no longer exists.
 */
public class MissingWUsedLinkFinderTest {

	private static final NodeRef RAW_MATERIAL = new NodeRef("workspace://SpacesStore/raw-material");

	private static final NodeRef SEMI_FINISHED = new NodeRef("workspace://SpacesStore/semi-finished");

	private static final NodeRef OTHER_PRODUCT = new NodeRef("workspace://SpacesStore/other-product");

	private static final NodeRef CURRENT_LINE = new NodeRef("workspace://SpacesStore/current-line");

	private static final NodeRef RECREATED_LINE = new NodeRef("workspace://SpacesStore/recreated-line");

	@Test
	public void lineStillInTheProductIsNotReported() {
		List<WUsedListDataItem> wUsedList = whereUsedOf(SEMI_FINISHED, CURRENT_LINE, true);

		assertTrue(MissingWUsedLinkFinder.findMissingLinks(SEMI_FINISHED, wUsedList, Set.of(CURRENT_LINE)).isEmpty());
	}

	@Test
	public void lineRecreatedSinceTheWhereUsedCalculationIsReported() {
		List<WUsedListDataItem> wUsedList = whereUsedOf(SEMI_FINISHED, CURRENT_LINE, true);

		List<WUsedListDataItem> missing = MissingWUsedLinkFinder.findMissingLinks(SEMI_FINISHED, wUsedList, Set.of(RECREATED_LINE));

		assertEquals(List.of(wUsedList.get(1)), missing);
	}

	@Test
	public void lineWhoseLinkWasRemovedIsReported() {
		List<WUsedListDataItem> wUsedList = whereUsedOf(SEMI_FINISHED, null, true);

		assertEquals(1, MissingWUsedLinkFinder.findMissingLinks(SEMI_FINISHED, wUsedList, Set.of(RECREATED_LINE)).size());
	}

	@Test
	public void lineNotSelectedForTheChangeIsIgnored() {
		List<WUsedListDataItem> wUsedList = whereUsedOf(SEMI_FINISHED, CURRENT_LINE, false);

		assertTrue(MissingWUsedLinkFinder.findMissingLinks(SEMI_FINISHED, wUsedList, Set.of(RECREATED_LINE)).isEmpty());
	}

	@Test
	public void lineOfAnotherProductIsIgnored() {
		List<WUsedListDataItem> wUsedList = whereUsedOf(OTHER_PRODUCT, CURRENT_LINE, true);

		assertTrue(MissingWUsedLinkFinder.findMissingLinks(SEMI_FINISHED, wUsedList, Set.of(RECREATED_LINE)).isEmpty());
	}

	@Test
	public void indirectUseIsIgnored() {
		List<WUsedListDataItem> wUsedList = whereUsedOf(OTHER_PRODUCT, CURRENT_LINE, true);
		wUsedList.add(line(wUsedList.get(1), SEMI_FINISHED, CURRENT_LINE, true));

		assertTrue(MissingWUsedLinkFinder.findMissingLinks(SEMI_FINISHED, List.of(wUsedList.get(2)), Set.of(RECREATED_LINE)).isEmpty());
	}

	private static List<WUsedListDataItem> whereUsedOf(NodeRef product, NodeRef link, boolean impacted) {
		WUsedListDataItem root = line(null, RAW_MATERIAL, null, impacted);
		return new ArrayList<>(List.of(root, line(root, product, link, impacted)));
	}

	private static WUsedListDataItem line(WUsedListDataItem parent, NodeRef source, NodeRef link, boolean impacted) {
		WUsedListDataItem item = new WUsedListDataItem();
		item.setParent(parent);
		item.setSourceItems(List.of(source));
		item.setLink(link);
		item.setIsWUsedImpacted(impacted);
		return item;
	}
}
