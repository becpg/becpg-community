/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score.marking;

import java.util.Locale;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.NutrientProfileCategory;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.score.NutrientValueProvider;
import fr.becpg.repo.score.ScoreBasis;
import fr.becpg.repo.score.ScoreContext;
import fr.becpg.repo.score.ScoreDefinitionService;
import fr.becpg.repo.score.ScoreEngine;
import fr.becpg.repo.score.ThresholdScoreEngine;
import fr.becpg.repo.score.data.RegulatoryScoreListDataItem;
import fr.becpg.repo.score.data.ScoreDefinitionItem;

/**
 * <p>Renders the UK front of pack marking a technical sheet prints, from the Multiple Traffic
 * Lights scores of the product.</p>
 *
 * <p>The score is read from the score list the formulation filled. A product formulated before
 * the scores existed has no such line: the score is then computed on the fly from its
 * definition, so that the sheet does not lose its marking silently until the product is
 * formulated again. Without a definition in the repository, there is no marking.</p>
 *
 * @author matthieu
 */
@Service("frontOfPackMarkingService")
public class FrontOfPackMarkingService {

	private static final Log logger = LogFactory.getLog(FrontOfPackMarkingService.class);

	/** Constant <code>MTL_CODE="MTL"</code> */
	public static final String MTL_CODE = "MTL";

	/** Constant <code>MTL_PORTION_CODE="MTL_PORTION"</code> */
	public static final String MTL_PORTION_CODE = "MTL_PORTION";

	private final NutrientValueProvider nutrientValueProvider;

	private final ScoreDefinitionService scoreDefinitionService;

	private final ThresholdScoreEngine thresholdScoreEngine;

	private final ScoreMarkingRenderer scoreMarkingRenderer;

	/**
	 * <p>Constructor for FrontOfPackMarkingService.</p>
	 *
	 * @param nutrientValueProvider the nutrient value provider
	 * @param scoreDefinitionService the score definition service
	 * @param thresholdScoreEngine the threshold score engine
	 * @param scoreMarkingRenderer the score marking renderer
	 */
	@Autowired
	public FrontOfPackMarkingService(NutrientValueProvider nutrientValueProvider, ScoreDefinitionService scoreDefinitionService,
			ThresholdScoreEngine thresholdScoreEngine, ScoreMarkingRenderer scoreMarkingRenderer) {
		this.nutrientValueProvider = nutrientValueProvider;
		this.scoreDefinitionService = scoreDefinitionService;
		this.thresholdScoreEngine = thresholdScoreEngine;
		this.scoreMarkingRenderer = scoreMarkingRenderer;
	}

	/**
	 * <p>Renders the front of pack marking of a product.</p>
	 *
	 * @param product the product
	 * @param locale the locale of the report
	 * @return the rendered marking, empty without a Multiple Traffic Lights score or a serving size
	 */
	public Optional<RenderedScoreMarking> render(ProductData product, Locale locale) {
		Optional<ScoreContext> perHundred = scoreOf(product, MTL_CODE);
		if (perHundred.isEmpty() || perHundred.get().getParts().isEmpty()) {
			return Optional.empty();
		}

		Optional<ScoreMarking> marking = new FrontOfPackMarkingBuilder(locale).build(facts(product, perHundred.get()));
		return marking.flatMap(m -> scoreMarkingRenderer.renderMarking(m, locale).map(svg -> new RenderedScoreMarking(m.code(), m.scoreClass(), svg)));
	}

	private FrontOfPackFacts facts(ProductData product, ScoreContext perHundred) {
		return new FrontOfPackFacts(perHundred, scoreOf(product, MTL_PORTION_CODE).orElse(null),
				nutrientValueProvider.extractNutrients(product, ScoreBasis.PerServing), nutrientValueProvider.extractNutrients(product, ScoreBasis.Per100g),
				nutrientValueProvider.extractReferenceIntakes(product), product.getServingSize(), isBeverage(product));
	}

	private static boolean isBeverage(ProductData product) {
		return NutrientProfileCategory.Beverages.name().equals(product.getNutrientProfileCategory());
	}

	/**
	 * The score the formulation stored first, the one computed on the fly from its definition
	 * otherwise.
	 */
	private Optional<ScoreContext> scoreOf(ProductData product, String code) {
		Optional<ScoreContext> stored = storedScore(product, code);
		return stored.isPresent() ? stored : computedScore(product, code);
	}

	private static Optional<ScoreContext> storedScore(ProductData product, String code) {
		if (product.getRegulatoryScoreList() == null) {
			return Optional.empty();
		}
		for (RegulatoryScoreListDataItem line : product.getRegulatoryScoreList()) {
			Optional<ScoreContext> score = parse(line.getDetails());
			if (score.isPresent() && code.equals(score.get().getCode())) {
				return score;
			}
		}
		return Optional.empty();
	}

	private Optional<ScoreContext> computedScore(ProductData product, String code) {
		Optional<ScoreDefinitionItem> definition = scoreDefinitionService.findByCode(code, null);
		if (definition.isEmpty() || !ScoreEngine.Threshold.equals(definition.get().getScoreEngine())) {
			return Optional.empty();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("No stored score " + code + " on " + product.getNodeRef() + ", computed for the front of pack marking");
		}
		return Optional.of(thresholdScoreEngine.compute(product, definition.get()));
	}

	private static Optional<ScoreContext> parse(String details) {
		if ((details == null) || details.isBlank()) {
			return Optional.empty();
		}
		try {
			return Optional.of(ScoreContext.parse(details));
		} catch (JSONException e) {
			logger.debug("Unreadable score detail skipped: " + e.getMessage());
			return Optional.empty();
		}
	}

}
