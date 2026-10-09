/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score.marking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.score.NutrientValueProvider;
import fr.becpg.repo.score.ScoreBasis;
import fr.becpg.repo.score.ScoreContext;
import fr.becpg.repo.score.ScoreDefinitionService;
import fr.becpg.repo.score.ScoreEngine;
import fr.becpg.repo.score.ThresholdScoreEngine;
import fr.becpg.repo.score.data.RegulatoryScoreListDataItem;
import fr.becpg.repo.score.data.ScoreDefinitionItem;

/**
 * Unit tests of {@link FrontOfPackMarkingService}: where the scores of the marking come from.
 *
 * @author matthieu
 */
public class FrontOfPackMarkingServiceTest {

	private static final String MTL_DETAILS = "{\"code\":\"MTL\",\"scale\":\"Traffic\",\"class\":\"High\",\"parts\":[{\"code\":\"FAT\",\"label\":\"High\",\"value\":20}]}";

	private ScoreDefinitionService scoreDefinitionService;

	private ThresholdScoreEngine thresholdScoreEngine;

	private ScoreMarkingRenderer renderer;

	private FrontOfPackMarkingService service;

	@Before
	public void setUp() {
		NutrientValueProvider nutrientValueProvider = mock(NutrientValueProvider.class);
		when(nutrientValueProvider.extractNutrients(any(), eq(ScoreBasis.PerServing))).thenReturn(Map.of("FAT", 6d));
		when(nutrientValueProvider.extractNutrients(any(), eq(ScoreBasis.Per100g))).thenReturn(Map.of("FAT", 20d));
		when(nutrientValueProvider.extractReferenceIntakes(any())).thenReturn(Map.of("FAT", 70d));

		scoreDefinitionService = mock(ScoreDefinitionService.class);
		when(scoreDefinitionService.findByCode(any(), any())).thenReturn(Optional.empty());
		thresholdScoreEngine = mock(ThresholdScoreEngine.class);
		renderer = mock(ScoreMarkingRenderer.class);
		when(renderer.renderMarking(any(), any())).thenReturn(Optional.of("<svg/>"));

		service = new FrontOfPackMarkingService(nutrientValueProvider, scoreDefinitionService, thresholdScoreEngine, renderer);
	}

	private static ProductData product(Double servingSize, String... details) {
		List<RegulatoryScoreListDataItem> lines = new ArrayList<>();
		for (String detail : details) {
			RegulatoryScoreListDataItem line = new RegulatoryScoreListDataItem();
			line.setDetails(detail);
			lines.add(line);
		}
		FinishedProductData product = new FinishedProductData();
		product.setServingSize(servingSize);
		product.setRegulatoryScoreList(lines);
		return product;
	}

	private ScoreMarking renderedMarking() {
		ArgumentCaptor<ScoreMarking> marking = ArgumentCaptor.forClass(ScoreMarking.class);
		verify(renderer).renderMarking(marking.capture(), any());
		return marking.getValue();
	}

	@Test
	public void testMarkingIsDrawnFromTheStoredScore() {
		RenderedScoreMarking marking = service.render(product(30d, MTL_DETAILS), Locale.ENGLISH).orElseThrow();

		assertEquals(FrontOfPackMarkingBuilder.CODE, marking.code());
		assertEquals("6g", renderedMarking().parts().get(0).amount());
		verify(thresholdScoreEngine, never()).compute(any(), any());
	}

	@Test
	public void testScoreMissingFromTheListIsComputedFromItsDefinition() {
		ScoreDefinitionItem definition = new ScoreDefinitionItem();
		definition.setEngine(ScoreEngine.Threshold.name());
		when(scoreDefinitionService.findByCode(FrontOfPackMarkingService.MTL_CODE, null)).thenReturn(Optional.of(definition));
		when(thresholdScoreEngine.compute(any(), eq(definition))).thenReturn(ScoreContext.parse(MTL_DETAILS));

		assertTrue(service.render(product(30d), Locale.ENGLISH).isPresent());
	}

	@Test
	public void testNoMarkingWithoutScoreNorDefinition() {
		assertEquals(Optional.empty(), service.render(product(30d), Locale.ENGLISH));
		verify(renderer, never()).renderMarking(any(), any());
	}

	@Test
	public void testDefinitionComputedByAnotherEngineIsNotUsed() {
		ScoreDefinitionItem definition = new ScoreDefinitionItem();
		definition.setEngine(ScoreEngine.Plugin.name());
		when(scoreDefinitionService.findByCode(FrontOfPackMarkingService.MTL_CODE, null)).thenReturn(Optional.of(definition));

		assertEquals(Optional.empty(), service.render(product(30d), Locale.ENGLISH));
	}

	@Test
	public void testNoMarkingWithoutServingSize() {
		assertEquals(Optional.empty(), service.render(product(null, MTL_DETAILS), Locale.ENGLISH));
	}

	@Test
	public void testUnreadableScoreLineIsSkipped() {
		assertTrue(service.render(product(30d, "{broken", MTL_DETAILS), Locale.ENGLISH).isPresent());
	}

	private static ProductData productWithNutrientUnits(String... units) {
		FinishedProductData product = new FinishedProductData();
		List<NutListDataItem> nutList = new ArrayList<>();
		for (String unit : units) {
			NutListDataItem nut = new NutListDataItem();
			nut.setUnit(unit);
			nutList.add(nut);
		}
		product.setNutList(nutList);
		return product;
	}

	@Test
	public void testNutrientsDeclaredPer100MillilitresAreStatedSo() {
		assertTrue(FrontOfPackMarkingService.hasNutrientsPerVolume(productWithNutrientUnits("kJ/100mL", "g/100mL")));
	}

	@Test
	public void testNutrientsDeclaredPer100GramsAreStatedSo() {
		assertFalse(FrontOfPackMarkingService.hasNutrientsPerVolume(productWithNutrientUnits("kJ/100g", "g/100g")));
		assertFalse(FrontOfPackMarkingService.hasNutrientsPerVolume(new FinishedProductData()));
	}

}
