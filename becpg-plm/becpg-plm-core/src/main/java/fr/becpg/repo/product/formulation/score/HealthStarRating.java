package fr.becpg.repo.product.formulation.score;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ScorableEntity;
import fr.becpg.repo.score.NutrientValueProvider;
import fr.becpg.repo.score.ScoreContext;
import fr.becpg.repo.score.ScoreDefinitionService;
import fr.becpg.repo.score.ScorePart;
import fr.becpg.repo.score.ScoreResultWriter;
import fr.becpg.repo.score.ScoreScale;
import fr.becpg.repo.score.ScoredEntity;
import fr.becpg.repo.score.data.ScoreDefinitionItem;

/**
 * Computes the Health Star Rating of Australia and New Zealand.
 *
 * <p>The scheme is neither a per nutrient comparison nor a plain points sum: the baseline
 * table, the eligibility to the modifying points and the conversion of the score into
 * stars all depend on the category of the product. It therefore needs its own plugin.</p>
 *
 * <p>Source: Health Star Rating System Implementation Guide, version 9, tables 1 to 7.</p>
 *
 * @author matthieu
 */
@Service("healthStarRating")
public class HealthStarRating implements ScoreCalculatingPlugin {

	/** Constant <code>SCORE_CODE="HSR"</code> */
	public static final String SCORE_CODE = "HSR";

	/** Constant <code>SCORE_VERSION="9"</code> */
	public static final String SCORE_VERSION = "9";

	/** Constant <code>ENERGY_CODE="ENER-KJO"</code> */
	private static final String ENERGY_CODE = "ENER-KJO";
	/** Constant <code>SATFAT_CODE="FASAT"</code> */
	private static final String SATFAT_CODE = "FASAT";
	/** Constant <code>SUGAR_CODE="SUGAR"</code> */
	private static final String SUGAR_CODE = "SUGAR";
	/** Constant <code>SODIUM_CODE="NA"</code> */
	private static final String SODIUM_CODE = "NA";
	/** Constant <code>SALT_CODE="NACL"</code> */
	private static final String SALT_CODE = "NACL";
	/** Constant <code>FIBRE_CODE="FIBTG"</code> */
	private static final String FIBRE_CODE = "FIBTG";
	/** Constant <code>PROTEIN_CODE="PRO-"</code> */
	private static final String PROTEIN_CODE = "PRO-";
	/** Constant <code>FVNL_CODE="FRUIT_VEGETABLE"</code> */
	private static final String FVNL_CODE = "FRUIT_VEGETABLE";

	/** Constant <code>SALT_TO_SODIUM_MG=1000d / 2.5d</code> */
	private static final double SALT_TO_SODIUM_MG = 1000d / 2.5d;

	/** Constant <code>PROTEIN_CAP_BASELINE=13</code> */
	private static final int PROTEIN_CAP_BASELINE = 13;

	/** Constant <code>PROTEIN_CAP_V_POINTS=5</code> */
	private static final int PROTEIN_CAP_V_POINTS = 5;

	private final NutrientValueProvider nutrientValueProvider;

	private final ScoreDefinitionService scoreDefinitionService;

	private final ScoreResultWriter scoreResultWriter;

	/**
	 * <p>Constructor for HealthStarRating.</p>
	 *
	 * @param nutrientValueProvider a {@link fr.becpg.repo.score.NutrientValueProvider} object
	 * @param scoreDefinitionService a {@link fr.becpg.repo.score.ScoreDefinitionService} object
	 * @param scoreResultWriter a {@link fr.becpg.repo.score.ScoreResultWriter} object
	 */
	@Autowired
	public HealthStarRating(NutrientValueProvider nutrientValueProvider, ScoreDefinitionService scoreDefinitionService,
			ScoreResultWriter scoreResultWriter) {
		this.nutrientValueProvider = nutrientValueProvider;
		this.scoreDefinitionService = scoreDefinitionService;
		this.scoreResultWriter = scoreResultWriter;
	}

	/** {@inheritDoc} */
	@Override
	public String getCode() {
		return SCORE_CODE;
	}

	/** {@inheritDoc} */
	@Override
	public String getVersion() {
		return SCORE_VERSION;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accept(ScorableEntity scorableEntity) {
		return (scorableEntity instanceof ProductData product) && (product.getNutList() != null) && !product.getNutList().isEmpty()
				&& scoreDefinitionService.findByCode(SCORE_CODE, SCORE_VERSION).isPresent();
	}

	/** {@inheritDoc} */
	@Override
	public boolean formulateScore(ScorableEntity scorableEntity) {
		if (!(scorableEntity instanceof ScoredEntity scoredEntity)) {
			return true;
		}

		Optional<ScoreDefinitionItem> definition = scoreDefinitionService.findByCode(SCORE_CODE, SCORE_VERSION);

		if (definition.isEmpty()) {
			return true;
		}

		scoreResultWriter.write(scoredEntity, buildContext((ProductData) scorableEntity, definition.get()));

		return true;
	}

	/**
	 * <p>buildContext.</p>
	 *
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 * @return a {@link fr.becpg.repo.score.ScoreContext} object
	 */
	private ScoreContext buildContext(ProductData product, ScoreDefinitionItem definition) {
		Map<String, Double> nutrients = nutrientValueProvider.extractNutrients(product);
		HsrCategory category = HsrCategory.of(product.getNutrientProfileCategory());

		ScoreContext context = new ScoreContext();
		context.setCode(SCORE_CODE);
		context.setVersion(SCORE_VERSION);
		context.setScale(ScoreScale.Stars.name());
		context.setUnit(definition.getUnit());

		int baseline = computeBaseline(nutrients, category, context);
		int vPoints = computeVPoints(nutrients, category);
		int pPoints = computePPoints(nutrients, category, baseline, vPoints);
		int fPoints = computeFPoints(nutrients, category);

		int score = baseline - vPoints - pPoints - fPoints;

		context.getParts().add(new ScorePart(FVNL_CODE).withValue(nutrients.get(FVNL_CODE), null).withContribution(-vPoints * 1d));
		context.getParts().add(new ScorePart(PROTEIN_CODE).withValue(nutrients.get(PROTEIN_CODE), null).withContribution(-pPoints * 1d));
		context.getParts().add(new ScorePart(FIBRE_CODE).withValue(nutrients.get(FIBRE_CODE), null).withContribution(-fPoints * 1d));

		context.getSteps().add(new ScorePart("baselinePoints").withContribution(baseline * 1d));
		context.getSteps().add(new ScorePart("modifyingPoints").withContribution((vPoints + pPoints + fPoints) * 1d));
		context.getSteps().add(new ScorePart("hsrScore").withContribution(score * 1d));

		context.setValue(category.stars(score));
		context.setScoreClass(String.valueOf(context.getValue()));

		return context;
	}

	/**
	 * <p>computeBaseline.</p>
	 *
	 * @param nutrients the documented nutrients
	 * @param category the HSR category of the product
	 * @param context the context collecting the breakdown
	 * @return the baseline points
	 */
	private int computeBaseline(Map<String, Double> nutrients, HsrCategory category, ScoreContext context) {
		int energy = points(nutrients.get(ENERGY_CODE), category.energyBounds());
		int sugar = points(nutrients.get(SUGAR_CODE), category.sugarBounds());

		context.getParts().add(new ScorePart(ENERGY_CODE).withValue(nutrients.get(ENERGY_CODE), null).withContribution(energy * 1d));
		context.getParts().add(new ScorePart(SUGAR_CODE).withValue(nutrients.get(SUGAR_CODE), null).withContribution(sugar * 1d));

		if (!category.hasFatAndSodiumBaseline()) {
			return energy + sugar;
		}

		int satFat = points(nutrients.get(SATFAT_CODE), category.satFatBounds());
		int sodium = points(sodium(nutrients), category.sodiumBounds());

		context.getParts().add(new ScorePart(SATFAT_CODE).withValue(nutrients.get(SATFAT_CODE), null).withContribution(satFat * 1d));
		context.getParts().add(new ScorePart(SODIUM_CODE).withValue(sodium(nutrients), null).withContribution(sodium * 1d));

		return energy + sugar + satFat + sodium;
	}

	/**
	 * <p>computeVPoints.</p>
	 *
	 * @param nutrients the documented nutrients
	 * @param category the HSR category of the product
	 * @return the fruit, vegetable, nut and legume points
	 */
	private int computeVPoints(Map<String, Double> nutrients, HsrCategory category) {
		return points(nutrients.get(FVNL_CODE), category.fvnlBounds());
	}

	/**
	 * Category 1 earns no protein point, and a product of the other categories reaching
	 * thirteen baseline points keeps them only when it earns five FVNL points.
	 *
	 * @param nutrients the documented nutrients
	 * @param category the HSR category of the product
	 * @param baseline the baseline points
	 * @param vPoints the FVNL points
	 * @return the protein points
	 */
	private int computePPoints(Map<String, Double> nutrients, HsrCategory category, int baseline, int vPoints) {
		if (!category.isEligibleToProtein()) {
			return 0;
		}
		if ((baseline >= PROTEIN_CAP_BASELINE) && (vPoints < PROTEIN_CAP_V_POINTS)) {
			return 0;
		}
		return points(nutrients.get(PROTEIN_CODE), HsrTables.PROTEIN_POINTS);
	}

	/**
	 * <p>computeFPoints.</p>
	 *
	 * @param nutrients the documented nutrients
	 * @param category the HSR category of the product
	 * @return the dietary fibre points
	 */
	private int computeFPoints(Map<String, Double> nutrients, HsrCategory category) {
		if (!category.isEligibleToFibre()) {
			return 0;
		}
		return points(nutrients.get(FIBRE_CODE), HsrTables.FIBRE_POINTS);
	}

	/**
	 * Products usually declare salt rather than sodium, the scheme scoring sodium in
	 * milligrams.
	 *
	 * @param nutrients the documented nutrients
	 * @return the sodium content in milligrams
	 */
	private Double sodium(Map<String, Double> nutrients) {
		if (nutrients.get(SODIUM_CODE) != null) {
			return nutrients.get(SODIUM_CODE);
		}
		return nutrients.get(SALT_CODE) != null ? nutrients.get(SALT_CODE) * SALT_TO_SODIUM_MG : null;
	}

	/**
	 * <p>Points earned by a value, the bounds being exclusive lower bounds.</p>
	 *
	 * @param value the nutrient value, null counting as zero point
	 * @param bounds the lower bound of each point
	 * @return the points earned
	 */
	private int points(Double value, double[] bounds) {
		if (value == null) {
			return 0;
		}

		int earned = 0;
		for (int i = 0; i < bounds.length; i++) {
			if (value > bounds[i]) {
				earned = i + 1;
			}
		}

		return earned;
	}

	/** {@inheritDoc} */
	@Override
	public Optional<ScoreContext> getScoreContext(ScorableEntity scorableEntity) {
		return Optional.empty();
	}

}
