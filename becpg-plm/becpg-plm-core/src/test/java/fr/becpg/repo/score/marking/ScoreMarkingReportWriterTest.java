/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score.marking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.report.entity.EntityImageInfo;
import fr.becpg.repo.report.svg.SvgDimensions;
import fr.becpg.repo.score.data.RegulatoryScoreListDataItem;

/**
 * Unit tests of {@link ScoreMarkingReportWriter}: the XML a report binds to and the images it reads.
 *
 * @author matthieu
 */
public class ScoreMarkingReportWriterTest {

	private static final String SVG = "<svg xmlns=\"http://www.w3.org/2000/svg\"/>";

	private static final String SIZED_SVG = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"336pt\" height=\"115.5pt\" viewBox=\"0 0 336 115.5\"/>";

	private ScoreMarkingRenderer renderer;

	private FrontOfPackMarkingService frontOfPack;

	private ScoreMarkingReportWriter writer;

	private Element entityElt;

	private Set<EntityImageInfo> images;

	private double lineCount;

	@Before
	public void setUp() {
		renderer = mock(ScoreMarkingRenderer.class);
		frontOfPack = mock(FrontOfPackMarkingService.class);
		when(frontOfPack.render(any(), any())).thenReturn(Optional.empty());
		writer = new ScoreMarkingReportWriter(renderer, frontOfPack);
		entityElt = DocumentHelper.createElement("entity");
		images = new HashSet<>();
	}

	/** Lines compare by value, each one gets its own so that the stubs do not collide. */
	private RegulatoryScoreListDataItem newLine() {
		RegulatoryScoreListDataItem line = new RegulatoryScoreListDataItem();
		line.setValue(++lineCount);
		return line;
	}

	private RegulatoryScoreListDataItem lineMarkedAs(String code, String scoreClass) {
		RegulatoryScoreListDataItem line = newLine();
		when(renderer.render(line, Locale.ENGLISH)).thenReturn(Optional.of(new RenderedScoreMarking(code, scoreClass, SVG)));
		return line;
	}

	private RegulatoryScoreListDataItem lineWithoutMarking() {
		RegulatoryScoreListDataItem line = newLine();
		when(renderer.render(line, Locale.ENGLISH)).thenReturn(Optional.empty());
		return line;
	}

	private static ProductData productWith(RegulatoryScoreListDataItem... lines) {
		ProductData product = new FinishedProductData();
		product.setRegulatoryScoreList(new ArrayList<>(List.of(lines)));
		return product;
	}

	private List<Element> markings() {
		Element markingsElt = entityElt.element(ScoreMarkingReportWriter.TAG_SCORE_MARKINGS);
		return markingsElt != null ? markingsElt.elements(ScoreMarkingReportWriter.TAG_SCORE_MARKING) : new ArrayList<>();
	}

	@Test
	public void testMarkingIsNamedInTheXmlAndHandedOverAsAnSvgImage() {
		writer.write(productWith(lineMarkedAs("MTL", "High")), entityElt, images, Locale.ENGLISH);

		Element marking = markings().get(0);
		assertEquals("scoreMarking_MTL_1", marking.attributeValue(ScoreMarkingReportWriter.ATTR_IMAGE_ID));
		assertEquals("MTL", marking.attributeValue(ScoreMarkingReportWriter.ATTR_CODE));
		assertEquals("High", marking.attributeValue(ScoreMarkingReportWriter.ATTR_SCORE_CLASS));
		assertEquals("en", marking.attributeValue(ScoreMarkingReportWriter.ATTR_LOCALE));

		EntityImageInfo image = images.iterator().next();
		assertEquals("scoreMarking_MTL_1", image.getId());
		assertEquals("image/svg+xml", image.getMimeType());
		assertEquals(SVG, new String(image.getContent(), StandardCharsets.UTF_8));
	}

	@Test
	public void testScoreListedTwiceGetsTwoImageIds() {
		writer.write(productWith(lineMarkedAs("MTL", "High"), lineMarkedAs("MTL", "Low")), entityElt, images, Locale.ENGLISH);

		assertEquals("scoreMarking_MTL_2", markings().get(1).attributeValue(ScoreMarkingReportWriter.ATTR_IMAGE_ID));
		assertEquals(2, images.size());
	}

	@Test
	public void testScoreWithoutMarkingIsSkippedAndPositionsStayContiguous() {
		writer.write(productWith(lineWithoutMarking(), lineMarkedAs("MTL_PORTION", "Low")), entityElt, images, Locale.ENGLISH);

		assertEquals(1, markings().size());
		assertEquals("scoreMarking_MTL_PORTION_1", markings().get(0).attributeValue(ScoreMarkingReportWriter.ATTR_IMAGE_ID));
	}

	@Test
	public void testImageIdHoldsOnlyWhatTheReportEngineCanReach() {
		writer.write(productWith(lineMarkedAs("UK-MTL 2016", null)), entityElt, images, Locale.ENGLISH);

		assertEquals("scoreMarking_UK_MTL_2016_1", markings().get(0).attributeValue(ScoreMarkingReportWriter.ATTR_IMAGE_ID));
		assertEquals("UK-MTL 2016", markings().get(0).attributeValue(ScoreMarkingReportWriter.ATTR_CODE));
	}

	@Test
	public void testMissingClassIsLeftOut() {
		writer.write(productWith(lineMarkedAs("MTL", null)), entityElt, images, Locale.ENGLISH);

		assertNull(markings().get(0).attributeValue(ScoreMarkingReportWriter.ATTR_SCORE_CLASS));
	}

	@Test
	public void testEntityWithoutScoreListWritesNothing() {
		writer.write(new FinishedProductData(), entityElt, images, Locale.ENGLISH);

		assertTrue(markings().isEmpty());
		assertTrue(images.isEmpty());
	}

	@Test
	public void testMarkingFollowsTheLocaleOfTheReport() {
		RegulatoryScoreListDataItem line = newLine();
		when(renderer.render(any(RegulatoryScoreListDataItem.class), any(Locale.class))).thenReturn(Optional.empty());
		when(renderer.render(line, Locale.FRENCH)).thenReturn(Optional.of(new RenderedScoreMarking("MTL", "High", SVG)));

		writer.write(productWith(line), entityElt, images, Locale.FRENCH);

		assertEquals("fr", markings().get(0).attributeValue(ScoreMarkingReportWriter.ATTR_LOCALE));
	}

	@Test
	public void testFrontOfPackMarkingComesAfterTheScores() {
		when(frontOfPack.render(any(), any())).thenReturn(Optional.of(new RenderedScoreMarking(FrontOfPackMarkingBuilder.CODE, "High", SIZED_SVG)));

		writer.write(productWith(lineMarkedAs("MTL", "High")), entityElt, images, Locale.ENGLISH);

		assertEquals(2, markings().size());
		assertEquals("scoreMarking_MTL_FOP_2", markings().get(1).attributeValue(ScoreMarkingReportWriter.ATTR_IMAGE_ID));
		assertEquals(FrontOfPackMarkingBuilder.CODE, markings().get(1).attributeValue(ScoreMarkingReportWriter.ATTR_CODE));
	}

	@Test
	public void testMarkingStatesItsSizeForTheReportToLayItOut() {
		when(frontOfPack.render(any(), any())).thenReturn(Optional.of(new RenderedScoreMarking(FrontOfPackMarkingBuilder.CODE, "High", SIZED_SVG)));

		writer.write(new FinishedProductData(), entityElt, images, Locale.ENGLISH);

		assertEquals("336.0", markings().get(0).attributeValue(SvgDimensions.ATTR_WIDTH));
		assertEquals("115.5", markings().get(0).attributeValue(SvgDimensions.ATTR_HEIGHT));
	}

	@Test
	public void testMarkingWithoutSizeInPointsStatesNone() {
		writer.write(productWith(lineMarkedAs("MTL", "High")), entityElt, images, Locale.ENGLISH);

		assertNull(markings().get(0).attributeValue(SvgDimensions.ATTR_WIDTH));
	}

}
