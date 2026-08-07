package fr.becpg.repo.product.formulation.score;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ScorableEntity;
import fr.becpg.repo.score.LcaScoreEngine;
import fr.becpg.repo.score.ScoreContext;
import fr.becpg.repo.score.ScoreDefinitionService;
import fr.becpg.repo.score.ScoreResultWriter;
import fr.becpg.repo.score.ScoredEntity;
import fr.becpg.repo.score.data.ScoreDefinitionItem;

/**
 * Publishes every score aggregating the life cycle indicators of a product.
 *
 * <p>Like the threshold plugin it serves no single score: it walks the definitions
 * declaring the life cycle engine, so the PEF single score and the Ecobalyse environmental
 * cost are two rows of reference data rather than two classes.</p>
 *
 * @author matthieu
 */
@Service("lcaAggregatedScore")
public class LcaAggregatedScore implements ScoreCalculatingPlugin {

	private final ScoreDefinitionService scoreDefinitionService;

	private final LcaScoreEngine lcaScoreEngine;

	private final ScoreResultWriter scoreResultWriter;

	/**
	 * <p>Constructor for LcaAggregatedScore.</p>
	 *
	 * @param scoreDefinitionService a {@link fr.becpg.repo.score.ScoreDefinitionService} object
	 * @param lcaScoreEngine a {@link fr.becpg.repo.score.LcaScoreEngine} object
	 * @param scoreResultWriter a {@link fr.becpg.repo.score.ScoreResultWriter} object
	 */
	@Autowired
	public LcaAggregatedScore(ScoreDefinitionService scoreDefinitionService, LcaScoreEngine lcaScoreEngine,
			ScoreResultWriter scoreResultWriter) {
		this.scoreDefinitionService = scoreDefinitionService;
		this.lcaScoreEngine = lcaScoreEngine;
		this.scoreResultWriter = scoreResultWriter;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accept(ScorableEntity scorableEntity) {
		return (scorableEntity instanceof ProductData product) && (product.getLcaList() != null) && !product.getLcaList().isEmpty();
	}

	/** {@inheritDoc} */
	@Override
	public boolean formulateScore(ScorableEntity scorableEntity) {
		if (!(scorableEntity instanceof ScoredEntity scoredEntity)) {
			return true;
		}

		ProductData product = (ProductData) scorableEntity;

		for (ScoreDefinitionItem definition : lcaScoreEngine.filter(scoreDefinitionService.getEffectiveScoreDefinitions(new Date()))) {
			publish(scoredEntity, product, definition);
		}

		return true;
	}

	/**
	 * <p>publish.</p>
	 *
	 * @param scoredEntity a {@link fr.becpg.repo.score.ScoredEntity} object
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 */
	private void publish(ScoredEntity scoredEntity, ProductData product, ScoreDefinitionItem definition) {
		ScoreContext context = lcaScoreEngine.compute(product, definition);

		if (!context.getParts().isEmpty()) {
			scoreResultWriter.write(scoredEntity, context);
		}
	}

	/** {@inheritDoc} */
	@Override
	public String getCode() {
		return LcaScoreEngine.class.getSimpleName();
	}

	/**
	 * {@inheritDoc}
	 *
	 * The scores are published by the plugin itself, definition by definition.
	 */
	@Override
	public Optional<ScoreContext> getScoreContext(ScorableEntity scorableEntity) {
		return Optional.empty();
	}

}
