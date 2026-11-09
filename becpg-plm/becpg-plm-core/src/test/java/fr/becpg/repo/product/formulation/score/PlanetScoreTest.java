/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.product.formulation.score;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.score.ScoreContext;
import fr.becpg.repo.score.ScorePart;

/**
 * Unit tests of {@link PlanetScore}: the fair pay axis and the synthesis it weighs on.
 *
 * @author matthieu
 */
public class PlanetScoreTest {

	private static final String FARMING_B = "PS_ELEVAGE_B";

	private static final String FAIR_TRADE = "PS_EQUITABLE";

	private static final String FAIR_PAY_AXIS = "REMUN";

	private NodeService nodeService;

	private PlanetScore planetScore;

	private FinishedProductData product;

	@Before
	public void setUp() {
		nodeService = mock(NodeService.class);
		planetScore = new PlanetScore(nodeService);
		product = new FinishedProductData();
		product.setLabelClaimList(new ArrayList<>());
		claim(FARMING_B, true, null);
	}

	@Test
	public void unknownFairPayIsLeftOut() {
		ScoreContext context = planetScore.getScoreContext(product).orElseThrow();

		assertTrue(fairPay(context).isEmpty());
	}

	@Test
	public void unknownFairPayDoesNotWeighOnTheSynthesis() {
		assertEquals("B", planetScore.getScoreContext(product).orElseThrow().getScoreClass());
	}

	@Test
	public void fairTradeAboveHalfIsTheBestLevel() {
		claim(FAIR_TRADE, true, 60d);

		assertEquals("A", fairPay(planetScore.getScoreContext(product).orElseThrow()).orElseThrow().getScoreClass());
	}

	@Test
	public void fairTradeBelowHalfIsTheWorstLevel() {
		claim(FAIR_TRADE, true, 30d);

		ScoreContext context = planetScore.getScoreContext(product).orElseThrow();

		assertEquals("D", fairPay(context).orElseThrow().getScoreClass());
		assertEquals("D", context.getScoreClass());
	}

	private static Optional<ScorePart> fairPay(ScoreContext context) {
		return context.getParts().stream().filter(part -> FAIR_PAY_AXIS.equals(part.getCode())).findFirst();
	}

	private void claim(String code, boolean claimed, Double percent) {
		NodeRef labelClaim = new NodeRef("workspace://SpacesStore/" + code);
		when(nodeService.getProperty(labelClaim, PLMModel.PROP_LABEL_CLAIM_CODE)).thenReturn(code);

		LabelClaimListDataItem item = new LabelClaimListDataItem();
		item.setLabelClaim(labelClaim);
		item.setIsClaimed(claimed);
		item.setPercentClaim(percent);

		List<LabelClaimListDataItem> claims = product.getLabelClaimList();
		claims.add(item);
	}
}
