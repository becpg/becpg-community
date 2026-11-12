/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.product.formulation.score;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.Pair;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.model.PLMModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.product.data.productList.PackMaterialListDataItem;
import fr.becpg.repo.product.formulation.ecoscore.EcoScoreContext;
import fr.becpg.repo.product.formulation.ecoscore.EcoScoreService;
import fr.becpg.repo.product.formulation.ecoscore.EcoScoreService.EnvironmentalFootprintValue;

/**
 * Unit tests of {@link FrenchEcoScore} against the Green-Score methodology of September 2024.
 *
 * @author matthieu
 */
public class FrenchEcoScoreTest {

	private static final String CATEGORY = "24036";

	private static final String FRANCE = "FR";

	/** A footprint this low gives a life cycle score of 100 */
	private static final double LOWEST_FOOTPRINT = 0.0001d;

	private static final double FRENCH_TRANSPORT_SCORE = 100d;

	private static final double FRENCH_POLICY_SCORE = 90d;

	private final NodeRef ingredient = new NodeRef("workspace://SpacesStore/water");

	private final NodeRef france = new NodeRef("workspace://SpacesStore/france");

	private final NodeRef organic = new NodeRef("workspace://SpacesStore/organic");

	private final NodeRef plastic = new NodeRef("workspace://SpacesStore/plastic");

	private NodeService nodeService;

	private FrenchEcoScore ecoScore;

	private FinishedProductData product;

	@Before
	public void setUp() {
		nodeService = mock(NodeService.class);
		EcoScoreService ecoScoreService = mock(EcoScoreService.class);
		when(ecoScoreService.getEnvironmentalFootprints())
				.thenReturn(Map.of(CATEGORY, new EnvironmentalFootprintValue(CATEGORY, CATEGORY, LOWEST_FOOTPRINT)));
		when(ecoScoreService.getCountryScores()).thenReturn(Map.of(FRANCE, new Pair<>(FRENCH_TRANSPORT_SCORE, FRENCH_POLICY_SCORE)));
		when(nodeService.getProperty(france, PLMModel.PROP_GEO_ORIGIN_ISOCODE)).thenReturn(FRANCE);
		when(nodeService.getProperty(organic, PLMModel.PROP_LABEL_CLAIM_CODE)).thenReturn("EU_ORGANIC");
		when(nodeService.hasAspect(ingredient, PLMModel.ASPECT_WATER)).thenReturn(true);

		ecoScore = new FrenchEcoScore(nodeService, ecoScoreService);
		product = new FinishedProductData();
		product.setEcoScoreCategory(CATEGORY);
	}

	@Test
	public void gradesOnTheSevenClassGrid() {
		assertEquals("A+", ecoScore.computeScoreClass(90));
		assertEquals("A", ecoScore.computeScoreClass(89));
		assertEquals("A", ecoScore.computeScoreClass(75));
		assertEquals("B", ecoScore.computeScoreClass(74));
		assertEquals("C", ecoScore.computeScoreClass(45));
		assertEquals("D", ecoScore.computeScoreClass(30));
		assertEquals("E", ecoScore.computeScoreClass(15));
		assertEquals("F", ecoScore.computeScoreClass(14));
		assertEquals("F", ecoScore.computeScoreClass(-3));
	}

	@Test
	public void aNonRecyclablePackagingNoLongerCapsTheScore() {
		withPackaging(100, true);

		ecoScore.formulateScore(product);

		// 100 for the life cycle, -5 for a product of unknown origin, no packaging malus
		assertEquals(Double.valueOf(95d), product.getEcoScore());
		assertEquals("A+", product.getEcoScoreClass());
	}

	@Test
	public void theCountryCountsWithoutLabel() {
		withFrenchIngredient();

		ecoScore.formulateScore(product);

		assertEquals(4, details().getPoliticalScore());
	}

	@Test
	public void theCountryNoLongerCountsOnceALabelScores() {
		withFrenchIngredient();
		LabelClaimListDataItem claim = new LabelClaimListDataItem();
		claim.setLabelClaim(organic);
		claim.setIsClaimed(true);
		product.setLabelClaimList(new ArrayList<>(List.of(claim)));

		ecoScore.formulateScore(product);

		assertEquals(15, details().getClaimBonus());
		assertEquals(0, details().getPoliticalScore());
	}

	@Test
	public void theOriginOfWaterCountsInTransport() {
		withFrenchIngredient();

		ecoScore.formulateScore(product);

		assertEquals(15, details().getTransportScore());
	}

	private EcoScoreContext details() {
		return EcoScoreContext.parse(product.getEcoScoreDetails());
	}

	private void withFrenchIngredient() {
		IngListDataItem ing = new IngListDataItem();
		ing.setIng(ingredient);
		ing.setQtyPerc(100d);
		ing.setGeoOrigin(new ArrayList<>(List.of(france)));
		product.setIngList(new ArrayList<>(List.of(ing)));
	}

	private void withPackaging(int materialScore, boolean notRecyclable) {
		when(nodeService.getProperty(plastic, PackModel.PROP_PM_ECOSCORE)).thenReturn(materialScore);
		when(nodeService.getProperty(plastic, PackModel.PROP_PM_ISNOTRECYCLABLE)).thenReturn(notRecyclable);
		product.setPackMaterialList(
				new ArrayList<>(List.of(PackMaterialListDataItem.build().withMaterial(plastic).withWeight(1d).withPkgLevel(PackagingLevel.Primary))));
	}
}
