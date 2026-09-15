package fr.becpg.repo.product.formulation.score;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ScorableEntity;
import fr.becpg.repo.score.ScoreContext;
import fr.becpg.repo.score.ScoreDefinitionService;
import fr.becpg.repo.score.ScoreEngine;
import fr.becpg.repo.score.ScoreResultWriter;
import fr.becpg.repo.score.ScoredEntity;
import fr.becpg.repo.score.ThresholdScoreEngine;
import fr.becpg.repo.score.data.ScoreDefinitionItem;

/**
 * Computes every front of pack scheme driven by nutrient thresholds.
 *
 * <p>Unlike the other plugins this one serves no single score: it walks the definitions
 * declaring the threshold engine and computes each of them. Adding the traffic lights of
 * another country is therefore a row in a reference data file, not a class.</p>
 *
 * @author matthieu
 */
@Service("thresholdNutritionScore")
public class ThresholdNutritionScore implements ScoreCalculatingPlugin {

	private final ScoreDefinitionService scoreDefinitionService;

	private final ThresholdScoreEngine thresholdScoreEngine;

	private final ScoreResultWriter scoreResultWriter;

	/**
	 * <p>Constructor for ThresholdNutritionScore.</p>
	 *
	 * @param scoreDefinitionService a {@link fr.becpg.repo.score.ScoreDefinitionService} object
	 * @param thresholdScoreEngine a {@link fr.becpg.repo.score.ThresholdScoreEngine} object
	 * @param scoreResultWriter a {@link fr.becpg.repo.score.ScoreResultWriter} object
	 */
	@Autowired
	public ThresholdNutritionScore(ScoreDefinitionService scoreDefinitionService, ThresholdScoreEngine thresholdScoreEngine,
			ScoreResultWriter scoreResultWriter) {
		this.scoreDefinitionService = scoreDefinitionService;
		this.thresholdScoreEngine = thresholdScoreEngine;
		this.scoreResultWriter = scoreResultWriter;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accept(ScorableEntity scorableEntity) {
		return (scorableEntity instanceof ProductData product) && (product.getNutList() != null) && !product.getNutList().isEmpty();
	}

	/** {@inheritDoc} */
	@Override
	public boolean formulateScore(ScorableEntity scorableEntity) {
		if (!(scorableEntity instanceof ScoredEntity scoredEntity)) {
			return true;
		}

		ProductData product = (ProductData) scorableEntity;

		for (ScoreDefinitionItem definition : scoreDefinitionService.getEffectiveScoreDefinitions(new Date())) {
			if (isThresholdDriven(definition)) {
				publish(scoredEntity, product, definition);
			}
		}

		return true;
	}

	/**
	 * <p>isThresholdDriven.</p>
	 *
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 * @return a boolean
	 */
	private boolean isThresholdDriven(ScoreDefinitionItem definition) {
		return ScoreEngine.Threshold.equals(definition.getScoreEngine());
	}

	/**
	 * <p>publish.</p>
	 *
	 * @param scoredEntity a {@link fr.becpg.repo.score.ScoredEntity} object
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 */
	private void publish(ScoredEntity scoredEntity, ProductData product, ScoreDefinitionItem definition) {
		ScoreContext context = thresholdScoreEngine.compute(product, definition);

		if (!context.getParts().isEmpty()) {
			scoreResultWriter.write(scoredEntity, context);
		}
	}

	/** {@inheritDoc} */
	@Override
	public String getCode() {
		return ThresholdScoreEngine.class.getSimpleName();
	}

	/**
	 * {@inheritDoc}
	 *
	 * The scores are published by the plugin itself, definition by definition, so the
	 * orchestrator has nothing to publish on its behalf.
	 */
	@Override
	public Optional<ScoreContext> getScoreContext(ScorableEntity scorableEntity) {
		return Optional.empty();
	}

}
