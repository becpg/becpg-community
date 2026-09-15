package fr.becpg.repo.product.formulation.score;

import java.util.Objects;
import java.util.Optional;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ScorableEntity;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.score.ScoreContext;
import fr.becpg.repo.score.ScoreDefinitionService;
import fr.becpg.repo.score.ScorePart;
import fr.becpg.repo.score.ScoreResultWriter;
import fr.becpg.repo.score.ScoredEntity;
import fr.becpg.repo.score.data.RegulatoryScoreListDataItem;
import fr.becpg.repo.score.data.ScoreDefinitionItem;

/**
 * Rates a product out of a hundred by the method Yuka publishes.
 *
 * <p>The rating weighs three criteria the repository already holds: the nutritional quality,
 * read from the Nutri-Score, the additives, read from the NOVA classification, and the
 * organic character, read from a claim. Nothing has to be entered by hand.</p>
 *
 * @author matthieu
 */
@Service("yukaScore")
public class YukaScore implements ScoreCalculatingPlugin {

	/** Constant <code>SCORE_CODE="YUKA"</code> */
	public static final String SCORE_CODE = "YUKA";

	private static final double NUTRITION_WEIGHT = 0.6d;

	private static final double ADDITIVES_WEIGHT = 0.3d;

	private static final double ORGANIC_WEIGHT = 0.1d;

	private static final String NUTRITION = "NUTRITION";

	private static final String ADDITIVES = "ADDITIVES";

	private static final String ORGANIC = "ORGANIC";

	private static final String NOVA_UNPROCESSED = "1";

	private static final String NOVA_CULINARY = "2";

	private static final String NOVA_PROCESSED = "3";

	private static final String NOVA_ULTRA_PROCESSED = "4";

	private final ScoreDefinitionService scoreDefinitionService;

	private final ScoreResultWriter scoreResultWriter;

	private final NodeService nodeService;

	/**
	 * <p>Constructor for YukaScore.</p>
	 *
	 * @param scoreDefinitionService a {@link fr.becpg.repo.score.ScoreDefinitionService} object
	 * @param scoreResultWriter a {@link fr.becpg.repo.score.ScoreResultWriter} object
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	@Autowired
	public YukaScore(ScoreDefinitionService scoreDefinitionService, ScoreResultWriter scoreResultWriter,
			@Qualifier("nodeService") NodeService nodeService) {
		this.scoreDefinitionService = scoreDefinitionService;
		this.scoreResultWriter = scoreResultWriter;
		this.nodeService = nodeService;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accept(ScorableEntity scorableEntity) {
		return (scorableEntity instanceof ProductData) && (scorableEntity instanceof ScoredEntity scored) && (nutritionGrade(scored) != null)
				&& (gradeOf(scored, NovaClassification.SCORE_CODE) != null);
	}

	/** {@inheritDoc} */
	@Override
	public String getCode() {
		return SCORE_CODE;
	}

	/** {@inheritDoc} */
	@Override
	public boolean formulateScore(ScorableEntity scorableEntity) {
		if (!(scorableEntity instanceof ScoredEntity scoredEntity)) {
			return true;
		}

		Optional<ScoreDefinitionItem> definition = scoreDefinitionService.findByCode(SCORE_CODE, null);
		if (definition.isEmpty()) {
			return true;
		}

		scoreResultWriter.write(scoredEntity, buildContext((ProductData) scorableEntity, scoredEntity, definition.get()));

		return true;
	}

	/**
	 * <p>buildContext.</p>
	 *
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param scored a {@link fr.becpg.repo.score.ScoredEntity} object
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 * @return a {@link fr.becpg.repo.score.ScoreContext} object
	 */
	private ScoreContext buildContext(ProductData product, ScoredEntity scored, ScoreDefinitionItem definition) {
		ScoreContext context = new ScoreContext();

		context.setCode(SCORE_CODE);
		context.setVersion(definition.getVersion());
		context.setScale(definition.getScale());

		double nutrition = nutritionPoints(scored);
		double additives = additivePoints(scored);
		double organic = isOrganic(product) ? 100d : 0d;

		context.getParts().add(part(NUTRITION, nutrition, NUTRITION_WEIGHT));
		context.getParts().add(part(ADDITIVES, additives, ADDITIVES_WEIGHT));
		context.getParts().add(part(ORGANIC, organic, ORGANIC_WEIGHT));

		context.setValue((nutrition * NUTRITION_WEIGHT) + (additives * ADDITIVES_WEIGHT) + (organic * ORGANIC_WEIGHT));

		return context;
	}

	/**
	 * <p>part.</p>
	 *
	 * @param code the criterion code
	 * @param points its rating out of a hundred
	 * @param weight its weight in the rating
	 * @return a {@link fr.becpg.repo.score.ScorePart} object
	 */
	private ScorePart part(String code, double points, double weight) {
		return new ScorePart(code).withValue(points, null).withCoefficients(null, weight).withContribution(points * weight);
	}

	/**
	 * <p>Nutritional quality, the Nutri-Score letter read as a rating out of a hundred.</p>
	 *
	 * @param scored a {@link fr.becpg.repo.score.ScoredEntity} object
	 * @return a double
	 */
	private double nutritionPoints(ScoredEntity scored) {
		return switch (Objects.toString(nutritionGrade(scored), "")) {
			case "A" -> 100d;
			case "B" -> 75d;
			case "C" -> 50d;
			case "D" -> 25d;
			default -> 0d;
		};
	}

	/**
	 * <p>Additives, the NOVA group read as a rating out of a hundred: a product free of
	 * industrial markers scores full marks, an ultra-processed one scores nothing.</p>
	 *
	 * @param scored a {@link fr.becpg.repo.score.ScoredEntity} object
	 * @return a double
	 * @throws java.lang.IllegalStateException when NOVA was not published, an absent
	 *         classification being no reason to award the marks of a product free of additives
	 */
	private double additivePoints(ScoredEntity scored) {
		return switch (Objects.toString(gradeOf(scored, NovaClassification.SCORE_CODE), "")) {
			case NOVA_UNPROCESSED -> 100d;
			case NOVA_CULINARY -> 75d;
			case NOVA_PROCESSED -> 50d;
			case NOVA_ULTRA_PROCESSED -> 0d;
			default -> throw new IllegalStateException("The Yuka rating needs the NOVA classification of the product");
		};
	}

	/**
	 * <p>nutritionGrade.</p>
	 *
	 * @param scored a {@link fr.becpg.repo.score.ScoredEntity} object
	 * @return a {@link java.lang.String} object
	 */
	private String nutritionGrade(ScoredEntity scored) {
		return gradeOf(scored, NutriScoreContext.SCORE_CODE);
	}

	/**
	 * <p>Class published for a score code, read from the scores of the entity.</p>
	 *
	 * @param scored a {@link fr.becpg.repo.score.ScoredEntity} object
	 * @param code the score code
	 * @return a {@link java.lang.String} object, null when the score is not published
	 */
	private String gradeOf(ScoredEntity scored, String code) {
		if (scored.getRegulatoryScoreList() == null) {
			return null;
		}

		for (RegulatoryScoreListDataItem score : scored.getRegulatoryScoreList()) {
			String details = score.getDetails();

			if ((details != null) && !details.isBlank() && code.equals(ScoreContext.parse(details).getCode())) {
				return score.getScoreClass();
			}
		}

		return null;
	}

	/**
	 * <p>isOrganic.</p>
	 *
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 * @return a boolean
	 */
	private boolean isOrganic(ProductData product) {
		if (product.getLabelClaimList() == null) {
			return false;
		}

		for (LabelClaimListDataItem claim : product.getLabelClaimList()) {
			if (Boolean.TRUE.equals(claim.getIsClaimed()) && ORGANIC.equals(claimCode(claim.getLabelClaim()))) {
				return true;
			}
		}

		return false;
	}

	/**
	 * <p>claimCode.</p>
	 *
	 * @param claim a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link java.lang.String} object
	 */
	private String claimCode(NodeRef claim) {
		return claim != null ? (String) nodeService.getProperty(claim, PLMModel.PROP_LABEL_CLAIM_CODE) : null;
	}

	/**
	 * {@inheritDoc}
	 *
	 * The rating is published by the plugin itself.
	 */
	@Override
	public Optional<ScoreContext> getScoreContext(ScorableEntity scorableEntity) {
		return Optional.empty();
	}

}
