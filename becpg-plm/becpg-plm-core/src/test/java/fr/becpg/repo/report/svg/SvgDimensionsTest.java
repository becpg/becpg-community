/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.report.svg;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Test;

/**
 * Unit tests of {@link SvgDimensions}.
 *
 * @author matthieu
 */
public class SvgDimensionsTest {

	@Test
	public void testSizeInPointsIsReadFromTheRootElement() {
		String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"144pt\" height=\"318.25pt\" viewBox=\"0 0 144 318.25\">"
				+ "<rect width=\"10pt\" height=\"20pt\"/></svg>";

		assertEquals(Optional.of(new SvgDimensions(144d, 318.25d)), SvgDimensions.of(svg));
	}

	@Test
	public void testRootElementSpreadOverSeveralLinesIsRead() {
		String svg = "\n<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"336pt\"\n\theight=\"78pt\"\n>";

		assertEquals(Optional.of(new SvgDimensions(336d, 78d)), SvgDimensions.of(svg));
	}

	@Test
	public void testSizeOfAChildElementIsNeverTaken() {
		assertEquals(Optional.empty(), SvgDimensions.of("<svg xmlns=\"http://www.w3.org/2000/svg\"><rect width=\"10pt\" height=\"20pt\"/></svg>"));
	}

	@Test
	public void testSizeInAnotherUnitIsNotRead() {
		assertEquals(Optional.empty(), SvgDimensions.of("<svg width=\"200px\" height=\"100px\"/>"));
		assertEquals(Optional.empty(), SvgDimensions.of("<svg width=\"100%\" height=\"100%\"/>"));
	}

	@Test
	public void testMissingOrForeignContentHasNoSize() {
		assertEquals(Optional.empty(), SvgDimensions.of(null));
		assertEquals(Optional.empty(), SvgDimensions.of("Fat 3 g"));
	}

	@Test
	public void testSizeIsWrittenOnTheImageElement() {
		Element image = DocumentHelper.createElement("nutritionFact");

		new SvgDimensions(144d, 318.25d).writeTo(image);

		assertEquals("144.0", image.attributeValue(SvgDimensions.ATTR_WIDTH));
		assertEquals("318.25", image.attributeValue(SvgDimensions.ATTR_HEIGHT));
	}

}
