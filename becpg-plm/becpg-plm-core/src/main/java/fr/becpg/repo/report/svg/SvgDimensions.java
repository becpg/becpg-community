/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.report.svg;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Element;

/**
 * <p>Size in points an SVG image declares on its root element, handed over to the report so that
 * each image is laid out at its own size.</p>
 *
 * <p>A panel or a marking grows with what it states: a report giving every image the same box
 * would either stretch it or leave it cropped. The report reads these attributes in the
 * {@code onCreate} script of the image and sets its width and height from them.</p>
 *
 * @param width the width in points
 * @param height the height in points
 * @author matthieu
 */
public record SvgDimensions(double width, double height) {

	/** Constant <code>ATTR_WIDTH="width"</code> */
	public static final String ATTR_WIDTH = "width";

	/** Constant <code>ATTR_HEIGHT="height"</code> */
	public static final String ATTR_HEIGHT = "height";

	private static final Pattern ROOT_ELEMENT = Pattern.compile("<svg\\b[^>]*>", Pattern.DOTALL);

	private static final String POINT_ATTRIBUTE = "\\s%s=\"([0-9]+(?:\\.[0-9]+)?)pt\"";

	private static final Pattern WIDTH = Pattern.compile(String.format(POINT_ATTRIBUTE, ATTR_WIDTH));

	private static final Pattern HEIGHT = Pattern.compile(String.format(POINT_ATTRIBUTE, ATTR_HEIGHT));

	/**
	 * <p>Reads the size an SVG declares in points on its root element.</p>
	 *
	 * @param svg the SVG document
	 * @return the size, empty when the root element states none in points, a panel pasted by hand being the case
	 */
	public static Optional<SvgDimensions> of(String svg) {
		if (svg == null) {
			return Optional.empty();
		}
		Matcher root = ROOT_ELEMENT.matcher(svg);
		if (!root.find()) {
			return Optional.empty();
		}
		Optional<Double> width = points(WIDTH, root.group());
		Optional<Double> height = points(HEIGHT, root.group());
		return width.isPresent() && height.isPresent() ? Optional.of(new SvgDimensions(width.get(), height.get())) : Optional.empty();
	}

	private static Optional<Double> points(Pattern attribute, String rootElement) {
		Matcher matcher = attribute.matcher(rootElement);
		return matcher.find() ? Optional.of(Double.valueOf(matcher.group(1))) : Optional.empty();
	}

	/**
	 * <p>Writes the size on the element naming the image in the report data.</p>
	 *
	 * @param imageElt the element naming the image
	 */
	public void writeTo(Element imageElt) {
		imageElt.addAttribute(ATTR_WIDTH, String.valueOf(width));
		imageElt.addAttribute(ATTR_HEIGHT, String.valueOf(height));
	}

}
