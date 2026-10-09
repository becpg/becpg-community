/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.product.formulation.score;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.score.ScoreContext;
import fr.becpg.repo.score.ScoreDefinitionService;
import fr.becpg.repo.score.ScoreEngine;
import fr.becpg.repo.score.ScoreResultWriter;
import fr.becpg.repo.score.data.RegulatoryScoreListDataItem;
import fr.becpg.repo.score.data.ScoreDefinitionItem;

/**
 * Unit tests of {@link ManualScore}: the breakdown of a score entered by hand.
 *
 * @author matthieu
 */
public class ManualScoreTest {

	private static final NodeRef ECOVADIS = new NodeRef("workspace://SpacesStore/ecovadis");

	private ScoreResultWriter writer;

	private ManualScore manualScore;

	private ScoreDefinitionItem definition;

	@Before
	public void setUp() {
		definition = new ScoreDefinitionItem();
		definition.setNodeRef(ECOVADIS);
		definition.setCode("ECOVADIS");
		definition.setVersion("2024");
		definition.setEngine(ScoreEngine.Manual.name());
		definition.setScale("Grade");

		ScoreDefinitionService definitions = mock(ScoreDefinitionService.class);
		when(definitions.getEffectiveScoreDefinitions(any())).thenReturn(List.of(definition));
		writer = mock(ScoreResultWriter.class);
		manualScore = new ManualScore(definitions, writer);
	}

	private static FinishedProductData productWith(RegulatoryScoreListDataItem line) {
		FinishedProductData product = new FinishedProductData();
		product.setRegulatoryScoreList(new ArrayList<>(List.of(line)));
		return product;
	}

	private static RegulatoryScoreListDataItem line(NodeRef scoreDef, String scoreClass) {
		RegulatoryScoreListDataItem line = new RegulatoryScoreListDataItem();
		line.setScoreDef(scoreDef);
		line.setScoreClass(scoreClass);
		line.setIsManual(true);
		return line;
	}

	@Test
	public void testEnteredScoreIsPublishedWithItsVerdict() {
		FinishedProductData product = productWith(line(ECOVADIS, "Gold"));

		manualScore.formulateScore(product);

		ArgumentCaptor<ScoreContext> context = ArgumentCaptor.forClass(ScoreContext.class);
		verify(writer).write(any(), context.capture());
		assertEquals("ECOVADIS", context.getValue().getCode());
		assertEquals("Grade", context.getValue().getScale());
		assertEquals("Gold", context.getValue().getScoreClass());
		assertEquals("2024", context.getValue().getVersion());
	}

	@Test
	public void testScoreNeverEnteredIsNotPublished() {
		manualScore.formulateScore(productWith(line(ECOVADIS, " ")));

		verify(writer, never()).write(any(), any());
	}

	@Test
	public void testLineOfAnotherScoreIsNotPublished() {
		manualScore.formulateScore(productWith(line(new NodeRef("workspace://SpacesStore/other"), "A")));

		verify(writer, never()).write(any(), any());
	}

	@Test
	public void testComputedScoreEnteredByHandWithoutBreakdownIsPublished() {
		definition.setEngine(ScoreEngine.Criteria.name());

		manualScore.formulateScore(productWith(line(ECOVADIS, "A")));

		verify(writer).write(any(), any());
	}

	@Test
	public void testComputedScoreWithItsBreakdownIsLeftToItsEngine() {
		definition.setEngine(ScoreEngine.Threshold.name());
		RegulatoryScoreListDataItem computed = line(ECOVADIS, "High");
		computed.setDetails("{\"code\":\"ECOVADIS\"}");

		manualScore.formulateScore(productWith(computed));

		verify(writer, never()).write(any(), any());
	}

	@Test
	public void testComputedScoreNotEnteredByHandIsLeftToItsEngine() {
		definition.setEngine(ScoreEngine.Threshold.name());
		RegulatoryScoreListDataItem computed = line(ECOVADIS, "High");
		computed.setIsManual(false);

		manualScore.formulateScore(productWith(computed));

		verify(writer, never()).write(any(), any());
	}

	@Test
	public void testProductWithoutScoreListIsNotAccepted() {
		assertFalse(manualScore.accept(new FinishedProductData()));
		assertTrue(manualScore.accept(productWith(line(ECOVADIS, "Gold"))));
	}

}
