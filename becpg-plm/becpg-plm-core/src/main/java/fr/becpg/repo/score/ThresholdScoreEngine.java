/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.project.formulation.ScoreRangeConverter;
import fr.becpg.repo.score.data.ScoreDefinitionItem;
import fr.becpg.repo.score.data.ScoreThresholdListDataItem;

/**
 * Computes the front of pack schemes that read a nutrient, compare it to a threshold and
 * publish a verdict.
 *
 * <p>One engine serves the traffic lights, the beverage grades, the warning labels and the
 * reference intake batteries: they only differ by their thresholds and by the way the per
 * nutrient verdicts are aggregated, both of which are data. A scheme needs Java only when
 * its algorithm is not a per nutrient comparison.</p>
 *
 * @author matthieu
 */
@Service("thresholdScoreEngine")
public class ThresholdScoreEngine {

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(ThresholdScoreEngine.class);

	/** Constant <code>CLASS_SEPARATOR=","</code> */
	private static final String CLASS_SEPARATOR = ",";

	/** Constant <code>PERCENT="%"</code> */
	private static final String PERCENT = "%";

	private final NutrientValueProvider nutrientValueProvider;

	/**
	 * <p>Constructor for ThresholdScoreEngine.</p>
	 *
	 * @param nutrientValueProvider a {@link fr.becpg.repo.score.NutrientValueProvider} object
	 */
	@Autowired
	public ThresholdScoreEngine(NutrientValueProvider nutrientValueProvider) {
		this.nutrientValueProvider = nutrientValueProvider;
	}

	/**
	 * <p>Computes the score of a product against the thresholds of a definition.</p>
	 *
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 * @return a {@link fr.becpg.repo.score.ScoreContext} object, its value being null when
	 *         no nutrient of the scheme is documented
	 */
	public ScoreContext compute(ProductData product, ScoreDefinitionItem definition) {
		ScoreContext context = newContext(definition);

		List<ScoreThresholdListDataItem> thresholds = definition.getThresholdList();
		if ((thresholds == null) || thresholds.isEmpty()) {
			return context;
		}

		Map<String, Double> nutrients = nutrientValueProvider.extractNutrients(product, definition.getScoreBasis());
		String category = product.getNutrientProfileCategory();

		for (Map.Entry<String, ScoreThresholdListDataItem> matched : matchThresholds(thresholds, nutrients, category).entrySet()) {
			context.getParts().add(toScorePart(matched.getKey(), matched.getValue(), nutrients.get(matched.getKey())));
		}

		aggregate(context, definition);

		return context;
	}

	/**
	 * <p>newContext.</p>
	 *
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 * @return a {@link fr.becpg.repo.score.ScoreContext} object
	 */
	private ScoreContext newContext(ScoreDefinitionItem definition) {
		ScoreContext context = new ScoreContext();
		context.setCode(definition.getCode());
		context.setVersion(definition.getVersion());
		context.setUnit(definition.getUnit());
		context.setScale(definition.getScoreScale().name());
		return context;
	}

	/**
	 * Keeps, for each nutrient of the scheme, the single threshold its value falls into.
	 *
	 * @param thresholds the thresholds of the definition
	 * @param nutrients the documented nutrients of the product
	 * @param category the nutrient profile category of the product
	 * @return the matched threshold indexed by nutrient code
	 */
	private Map<String, ScoreThresholdListDataItem> matchThresholds(List<ScoreThresholdListDataItem> thresholds, Map<String, Double> nutrients,
			String category) {
		Map<String, ScoreThresholdListDataItem> matched = new LinkedHashMap<>();

		for (ScoreThresholdListDataItem threshold : thresholds) {
			if (!threshold.appliesTo(category) || matched.containsKey(threshold.getNutCode())) {
				continue;
			}
			if (threshold.matches(threshold.comparedValue(nutrients.get(threshold.getNutCode()), nutrients))) {
				matched.put(threshold.getNutCode(), threshold);
			}
		}

		return matched;
	}

	/**
	 * <p>toScorePart.</p>
	 *
	 * @param nutCode the nutrient code
	 * @param threshold the threshold the value falls into
	 * @param value the value of the nutrient
	 * @return a {@link fr.becpg.repo.score.ScorePart} object
	 */
	private ScorePart toScorePart(String nutCode, ScoreThresholdListDataItem threshold, Double value) {
		ScorePart part = new ScorePart(nutCode).withLabel(threshold.getResult()).withValue(value, null)
				.withContribution(threshold.getPoints());

		if ((threshold.getReferenceIntake() != null) && (threshold.getReferenceIntake() != 0d) && (value != null)) {
			part.withValue((value / threshold.getReferenceIntake()) * 100d, PERCENT);
		}

		return part;
	}

	/**
	 * <p>aggregate.</p>
	 *
	 * @param context a {@link fr.becpg.repo.score.ScoreContext} object
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 */
	private void aggregate(ScoreContext context, ScoreDefinitionItem definition) {
		ScoreAggregation aggregation = definition.getScoreAggregation();

		if (ScoreAggregation.Worst.equals(aggregation)) {
			applyWorstClass(context, definition);
		} else if (ScoreAggregation.Count.equals(aggregation)) {
			applyCount(context);
		} else if (ScoreAggregation.Sum.equals(aggregation)) {
			applySum(context, definition);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Threshold score " + definition.getCode() + " = " + context.getValue() + " (" + context.getScoreClass() + ")");
		}
	}

	/**
	 * The worst verdict decides the class, which is how a beverage grade is read.
	 *
	 * @param context a {@link fr.becpg.repo.score.ScoreContext} object
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 */
	private void applyWorstClass(ScoreContext context, ScoreDefinitionItem definition) {
		List<String> order = classOrder(definition);
		int worst = -1;

		for (ScorePart part : context.getParts()) {
			worst = Math.max(worst, order.indexOf(part.getLabel()));
		}

		if (worst >= 0) {
			context.setScoreClass(order.get(worst));
			context.setValue(worst * 1d);
		}
	}

	/**
	 * The number of verdicts is the score, which is how warning labels are counted.
	 *
	 * @param context a {@link fr.becpg.repo.score.ScoreContext} object
	 */
	private void applyCount(ScoreContext context) {
		double count = 0d;
		for (ScorePart part : context.getParts()) {
			if ((part.getLabel() != null) && !part.getLabel().isBlank()) {
				count++;
			}
		}
		context.setValue(count);
		context.setScoreClass(String.valueOf((int) count));
	}

	/**
	 * <p>applySum.</p>
	 *
	 * @param context a {@link fr.becpg.repo.score.ScoreContext} object
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 */
	private void applySum(ScoreContext context, ScoreDefinitionItem definition) {
		Double total = null;

		for (ScorePart part : context.getParts()) {
			if (part.getContribution() != null) {
				total = (total == null ? 0d : total) + part.getContribution();
			}
		}

		context.setValue(total);

		if ((total != null) && (definition.getRange() != null) && !definition.getRange().isBlank()) {
			context.setScoreClass(new ScoreRangeConverter(definition.getRange()).getScoreLetter(total));
		}
	}

	/**
	 * <p>classOrder.</p>
	 *
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 * @return the classes from the best to the worst, never null
	 */
	private List<String> classOrder(ScoreDefinitionItem definition) {
		if ((definition.getClassOrder() == null) || definition.getClassOrder().isBlank()) {
			return new ArrayList<>();
		}
		return Arrays.stream(definition.getClassOrder().split(CLASS_SEPARATOR)).map(String::trim).toList();
	}

}
