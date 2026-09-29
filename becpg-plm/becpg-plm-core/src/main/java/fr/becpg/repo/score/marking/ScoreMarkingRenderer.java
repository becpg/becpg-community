/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score.marking;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.repo.score.ScoreContext;
import fr.becpg.repo.score.ScoreDefinitionService;
import fr.becpg.repo.score.data.RegulatoryScoreListDataItem;
import fr.becpg.repo.score.data.ScoreDefinitionItem;
import fr.becpg.repo.template.BeCPGTemplateRenderService;
import fr.becpg.repo.template.TemplateRenderException;

/**
 * <p>Renders the regulatory marking of a computed score as SVG, on the server, so that the very
 * same drawing reaches the screen and the printed report.</p>
 *
 * <p>The template is looked up by score code first, then by scale: {@code score-mtl.ftlx} would
 * draw the Multiple Traffic Lights alone, {@code score-traffic.ftlx} draws every score graded on
 * traffic lights. A score whose scale has no template is left to the drawing of the browser.</p>
 *
 * @author matthieu
 */
@Service("scoreMarkingRenderer")
public class ScoreMarkingRenderer {

	private static final Log logger = LogFactory.getLog(ScoreMarkingRenderer.class);

	/** Constant <code>MODEL_SCORE="score"</code> */
	public static final String MODEL_SCORE = "score";

	private static final String TEMPLATE_PREFIX = "score-";

	private static final String TEMPLATE_SUFFIX = ".ftlx";

	private final BeCPGTemplateRenderService templateRenderService;

	private final ScoreDefinitionService scoreDefinitionService;

	/**
	 * <p>Constructor for ScoreMarkingRenderer.</p>
	 *
	 * @param templateRenderService the template render service
	 * @param scoreDefinitionService the score definition service, which a score entered by hand is drawn from
	 */
	@Autowired
	public ScoreMarkingRenderer(@Qualifier("beCPGTemplateRenderService") BeCPGTemplateRenderService templateRenderService,
			ScoreDefinitionService scoreDefinitionService) {
		this.templateRenderService = templateRenderService;
		this.scoreDefinitionService = scoreDefinitionService;
	}

	/**
	 * <p>Constructor for a renderer that draws only the scores carrying their breakdown.</p>
	 *
	 * @param templateRenderService the template render service
	 */
	public ScoreMarkingRenderer(BeCPGTemplateRenderService templateRenderService) {
		this(templateRenderService, null);
	}

	/**
	 * <p>Renders the marking of a line of the score list, from the detail the formulation stored.</p>
	 *
	 * @param scoreLine the line of {@code bcpg:regulatoryScoreList}
	 * @param locale the locale of the marking
	 * @return the rendered marking, empty when the line holds no readable detail or nothing to mark
	 */
	public Optional<RenderedScoreMarking> render(RegulatoryScoreListDataItem scoreLine, Locale locale) {
		Optional<ScoreContext> score = parseDetails(scoreLine.getDetails());
		if (score.isEmpty()) {
			score = verdictOf(scoreLine);
		}
		return score.flatMap(s -> renderLine(s, locale));
	}

	/**
	 * A score entered by hand, EcoVadis being the case, is never computed and so carries no
	 * breakdown: it is drawn from the verdict of the line and the definition it points to, the
	 * way Share draws it.
	 */
	private Optional<ScoreContext> verdictOf(RegulatoryScoreListDataItem scoreLine) {
		if ((scoreDefinitionService == null) || (scoreLine.getScoreDef() == null) || (isBlank(scoreLine.getScoreClass()) && (scoreLine.getValue() == null))) {
			return Optional.empty();
		}
		for (ScoreDefinitionItem definition : scoreDefinitionService.getScoreDefinitions()) {
			if (scoreLine.getScoreDef().equals(definition.getNodeRef())) {
				ScoreContext score = new ScoreContext();
				score.setCode(definition.getCode());
				score.setVersion(scoreLine.getVersion() != null ? scoreLine.getVersion() : definition.getVersion());
				score.setScale(definition.getScoreScale().name());
				score.setScoreClass(scoreLine.getScoreClass());
				score.setValue(scoreLine.getValue());
				return Optional.of(score);
			}
		}
		return Optional.empty();
	}

	private Optional<RenderedScoreMarking> renderLine(ScoreContext score, Locale locale) {
		String code = score.getCode() != null ? score.getCode() : score.getScale();
		return render(score, locale).map(svg -> new RenderedScoreMarking(code, score.getScoreClass(), svg));
	}

	/**
	 * <p>Renders the marking of the score of a given code, among the lines of a score list.</p>
	 *
	 * @param scoreLines the lines of {@code bcpg:regulatoryScoreList}, may be null
	 * @param code the score code, "MTL"
	 * @param locale the locale of the marking
	 * @return the rendered marking, empty when the list holds no such score or nothing draws it
	 */
	public Optional<RenderedScoreMarking> renderScore(List<RegulatoryScoreListDataItem> scoreLines, String code, Locale locale) {
		if ((scoreLines == null) || (code == null)) {
			return Optional.empty();
		}
		for (RegulatoryScoreListDataItem scoreLine : scoreLines) {
			Optional<ScoreContext> score = parseDetails(scoreLine.getDetails());
			if (score.isPresent() && code.equals(score.get().getCode())) {
				return render(score.get(), locale).map(svg -> new RenderedScoreMarking(code, score.get().getScoreClass(), svg));
			}
		}
		return Optional.empty();
	}

	/**
	 * <p>Renders the marking of a score from its serialized detail, as held by {@code bcpg:rslDetails}.</p>
	 *
	 * @param details the serialized score detail
	 * @param locale the locale of the marking
	 * @return the rendered marking, empty when the detail is unreadable or holds nothing to mark
	 */
	public Optional<RenderedScoreMarking> renderDetails(String details, Locale locale) {
		return parseDetails(details).flatMap(score -> renderLine(score, locale));
	}

	/**
	 * <p>Renders the marking of a computed score.</p>
	 *
	 * @param score the computed score, as parsed from {@code bcpg:rslDetails}
	 * @param locale the locale of the marking
	 * @return the SVG, empty when the score has neither parts nor class, or no template draws it
	 */
	public Optional<String> render(ScoreContext score, Locale locale) {
		if (score.getParts().isEmpty() && isBlank(score.getScoreClass())) {
			return Optional.empty();
		}
		return renderMarking(new ScoreMarkingBuilder(locale).build(score), locale);
	}

	/**
	 * <p>Renders a marking already laid out, such as the front of pack marking of a technical sheet.</p>
	 *
	 * @param marking the marking
	 * @param locale the locale of the marking
	 * @return the SVG, empty when no template draws the marking
	 */
	public Optional<String> renderMarking(ScoreMarking marking, Locale locale) {
		Optional<String> templateName = findTemplate(marking.code(), marking.scale(), locale);
		return templateName.isPresent() ? renderTemplate(templateName.get(), marking, locale) : Optional.empty();
	}

	/**
	 * A template is editable content: a broken one must cost the marking, never the report or
	 * the page it is drawn in, which both fall back on their own drawing.
	 */
	private Optional<String> renderTemplate(String templateName, ScoreMarking marking, Locale locale) {
		try {
			return Optional.of(templateRenderService.render(templateName, locale, Map.of(MODEL_SCORE, marking)).strip());
		} catch (TemplateRenderException e) {
			logger.error("Cannot render the marking of score " + marking.code() + " with " + templateName, e);
			return Optional.empty();
		}
	}

	private Optional<String> findTemplate(String code, String scale, Locale locale) {
		for (String templateName : candidateTemplates(code, scale)) {
			if (templateRenderService.exists(templateName, locale)) {
				return Optional.of(templateName);
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("No marking template for score " + code + ", scale " + scale);
		}
		return Optional.empty();
	}

	private static boolean isBlank(String value) {
		return (value == null) || value.isBlank();
	}

	private static Optional<ScoreContext> parseDetails(String details) {
		if ((details == null) || details.isBlank()) {
			return Optional.empty();
		}
		try {
			return Optional.of(ScoreContext.parse(details));
		} catch (JSONException e) {
			logger.warn("Unreadable score detail, no marking rendered: " + e.getMessage());
			return Optional.empty();
		}
	}

	/**
	 * <p>Templates able to draw a marking, the most specific first.</p>
	 *
	 * @param code the code of the marking
	 * @param scale the scale of the marking
	 * @return the names of the candidate templates
	 */
	static List<String> candidateTemplates(String code, String scale) {
		return Stream.of(code, scale)
				.filter(name -> (name != null) && !name.isBlank())
				.map(name -> TEMPLATE_PREFIX + name.toLowerCase(Locale.ROOT) + TEMPLATE_SUFFIX)
				.toList();
	}

}
