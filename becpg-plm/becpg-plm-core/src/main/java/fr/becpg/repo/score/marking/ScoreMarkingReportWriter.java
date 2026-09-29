/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score.marking;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.report.entity.EntityImageInfo;
import fr.becpg.repo.report.svg.SvgDimensions;
import fr.becpg.repo.score.data.RegulatoryScoreListDataItem;

/**
 * <p>Hands the regulatory markings of the scores of an entity over to the report engine.</p>
 *
 * <p>Each marking is rendered from the score list itself, so it asks nothing of the labeling
 * rules of the customer, and streamed as an in-memory SVG image exactly like a nutrition facts
 * panel: the XML names it, the report reads its bytes from the BIRT appContext under its image
 * id and embeds it as vector graphics.</p>
 *
 * <pre>
 * &lt;scoreMarkings&gt;
 *   &lt;scoreMarking imageId="scoreMarking_MTL_1" code="MTL" scoreClass="High" locale="en" width="336.0" height="78.0"/&gt;
 *   &lt;scoreMarking imageId="scoreMarking_MTL_FOP_2" code="MTL_FOP" scoreClass="High" locale="en" width="336.0" height="115.0"/&gt;
 * &lt;/scoreMarkings&gt;
 * </pre>
 *
 * <p>Besides the marking of each score, the UK front of pack marking a technical sheet prints is
 * written under the code {@code MTL_FOP}, see {@link FrontOfPackMarkingService}.</p>
 *
 * @author matthieu
 */
@Service("scoreMarkingReportWriter")
public class ScoreMarkingReportWriter {

	/** Constant <code>TAG_SCORE_MARKINGS="scoreMarkings"</code> */
	public static final String TAG_SCORE_MARKINGS = "scoreMarkings";

	/** Constant <code>TAG_SCORE_MARKING="scoreMarking"</code> */
	public static final String TAG_SCORE_MARKING = "scoreMarking";

	/** Constant <code>ATTR_IMAGE_ID="imageId"</code> */
	public static final String ATTR_IMAGE_ID = "imageId";

	/** Constant <code>ATTR_CODE="code"</code> */
	public static final String ATTR_CODE = "code";

	/** Constant <code>ATTR_SCORE_CLASS="scoreClass"</code> */
	public static final String ATTR_SCORE_CLASS = "scoreClass";

	/** Constant <code>ATTR_LOCALE="locale"</code> */
	public static final String ATTR_LOCALE = "locale";

	private static final String IMAGE_ID_PREFIX = "scoreMarking_";

	private static final String IMAGE_ID_SEPARATOR = "_";

	/**
	 * An image id is a resource key of the report engine: anything but letters, digits and
	 * underscores makes the image unreachable, "The resource of this report item is not reachable".
	 */
	private static final String IMAGE_ID_FORBIDDEN_CHARACTERS = "[^A-Za-z0-9]+";

	private static final String SVG_MIME_TYPE = "image/svg+xml";

	private final ScoreMarkingRenderer scoreMarkingRenderer;

	private final FrontOfPackMarkingService frontOfPackMarkingService;

	/**
	 * <p>Constructor for ScoreMarkingReportWriter.</p>
	 *
	 * @param scoreMarkingRenderer the score marking renderer
	 * @param frontOfPackMarkingService the front of pack marking service
	 */
	@Autowired
	public ScoreMarkingReportWriter(ScoreMarkingRenderer scoreMarkingRenderer, FrontOfPackMarkingService frontOfPackMarkingService) {
		this.scoreMarkingRenderer = scoreMarkingRenderer;
		this.frontOfPackMarkingService = frontOfPackMarkingService;
	}

	/**
	 * <p>Writes the markings of the scores of a product under the entity element, and their images.</p>
	 *
	 * @param product the product, its score list may be null
	 * @param entityElt the element of the entity in the report data
	 * @param images the images handed over to the report engine
	 * @param locale the locale of the report
	 */
	public void write(ProductData product, Element entityElt, Set<EntityImageInfo> images, Locale locale) {
		Element markingsElt = entityElt.addElement(TAG_SCORE_MARKINGS);
		int position = 0;

		for (RegulatoryScoreListDataItem scoreLine : scoreLines(product)) {
			Optional<RenderedScoreMarking> marking = scoreMarkingRenderer.render(scoreLine, locale);
			if (marking.isPresent()) {
				addMarking(marking.get(), ++position, markingsElt, images, locale);
			}
		}

		Optional<RenderedScoreMarking> frontOfPack = frontOfPackMarkingService.render(product, locale);
		if (frontOfPack.isPresent()) {
			addMarking(frontOfPack.get(), ++position, markingsElt, images, locale);
		}
	}

	private static List<RegulatoryScoreListDataItem> scoreLines(ProductData product) {
		return product.getRegulatoryScoreList() != null ? product.getRegulatoryScoreList() : List.of();
	}

	private static void addMarking(RenderedScoreMarking marking, int position, Element markingsElt, Set<EntityImageInfo> images, Locale locale) {
		String imageId = imageId(marking.code(), position);
		addMarkingElement(markingsElt, marking, imageId, locale);
		images.add(new EntityImageInfo(imageId, marking.svg().getBytes(StandardCharsets.UTF_8), SVG_MIME_TYPE));
	}

	/**
	 * A score may be listed once per country or usage, hence the position in the image id: a
	 * report selects a marking by its code attribute, never by its image id.
	 */
	private static String imageId(String code, int position) {
		return IMAGE_ID_PREFIX + code.replaceAll(IMAGE_ID_FORBIDDEN_CHARACTERS, IMAGE_ID_SEPARATOR) + IMAGE_ID_SEPARATOR + position;
	}

	private static void addMarkingElement(Element markingsElt, RenderedScoreMarking marking, String imageId, Locale locale) {
		Element markingElt = markingsElt.addElement(TAG_SCORE_MARKING);
		markingElt.addAttribute(ATTR_IMAGE_ID, imageId);
		markingElt.addAttribute(ATTR_CODE, marking.code());
		markingElt.addAttribute(ATTR_SCORE_CLASS, marking.scoreClass());
		markingElt.addAttribute(ATTR_LOCALE, MLTextHelper.localeKey(locale));
		SvgDimensions.of(marking.svg()).ifPresent(dimensions -> dimensions.writeTo(markingElt));
	}

}
