package fr.becpg.repo.product.formulation.score;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.NutrientProfileCategory;
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
 * Computes the UK nutrient profiling model of the Food Standards Agency, the one Ofcom
 * uses to decide whether a food may be advertised to children.
 *
 * <p>The model is a points system rather than a per nutrient comparison, and it carries a
 * conditional rule the threshold engine cannot express: a product scoring eleven A points
 * or more loses its protein points, unless it earns the full five points for fruit,
 * vegetables and nuts. It therefore needs its own plugin.</p>
 *
 * <p>Source: Nutrient Profiling Technical Guidance, Department of Health, January 2011.</p>
 *
 * @author matthieu
 */
@Service("fsaOfcomNutrientProfile")
public class FsaOfcomNutrientProfile implements ScoreCalculatingPlugin {

	/** Constant <code>SCORE_CODE="FSAOFCOM"</code> */
	public static final String SCORE_CODE = "FSAOFCOM";

	/** Constant <code>SCORE_VERSION="2004"</code> */
	public static final String SCORE_VERSION = "2004";

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
	/** Constant <code>FRUIT_VEGETABLE_CODE="FRUIT_VEGETABLE"</code> */
	private static final String FRUIT_VEGETABLE_CODE = "FRUIT_VEGETABLE";

	/** A food is high in fat, salt and sugar from four points */
	private static final int FOOD_HFSS_THRESHOLD = 4;

	/** A drink is high in fat, salt and sugar from one point */
	private static final int DRINK_HFSS_THRESHOLD = 1;

	/** Constant <code>HFSS="HFSS"</code> */
	private static final String HFSS = "HFSS";

	/** Constant <code>NON_HFSS="NonHFSS"</code> */
	private static final String NON_HFSS = "NonHFSS";
	/** Constant <code>NSP_FIBRE_CODE="PSACNS"</code> */
	private static final String NSP_FIBRE_CODE = "PSACNS";
	/** Constant <code>AOAC_FIBRE_CODE="FIBTG"</code> */
	private static final String AOAC_FIBRE_CODE = "FIBTG";
	/** Constant <code>PROTEIN_CODE="PRO-"</code> */
	private static final String PROTEIN_CODE = "PRO-";

	/** Constant <code>SALT_TO_SODIUM_MG=1000d / 2.5d</code> */
	private static final double SALT_TO_SODIUM_MG = 1000d / 2.5d;

	/** Lower bounds of the A points, one row per nutrient, points being the index */
	private static final double[] ENERGY_POINTS = { 335, 670, 1005, 1340, 1675, 2010, 2345, 2680, 3015, 3350 };
	private static final double[] SATFAT_POINTS = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
	private static final double[] SUGAR_POINTS = { 4.5, 9, 13.5, 18, 22.5, 27, 31, 36, 40, 45 };
	private static final double[] SODIUM_POINTS = { 90, 180, 270, 360, 450, 540, 630, 720, 810, 900 };

	/** Lower bounds of the C points */
	private static final double[] FRUIT_VEGETABLE_POINTS = { 40, 60, Double.MAX_VALUE, Double.MAX_VALUE, 80 };
	private static final double[] NSP_FIBRE_POINTS = { 0.7, 1.4, 2.1, 2.8, 3.5 };
	private static final double[] AOAC_FIBRE_POINTS = { 0.9, 1.9, 2.8, 3.7, 4.7 };
	private static final double[] PROTEIN_POINTS = { 1.6, 3.2, 4.8, 6.4, 8.0 };

	/** Constant <code>PROTEIN_CAP_A_POINTS=11</code> */
	private static final int PROTEIN_CAP_A_POINTS = 11;

	/** Constant <code>MAX_FRUIT_VEGETABLE_POINTS=5</code> */
	private static final int MAX_FRUIT_VEGETABLE_POINTS = 5;

	private final NutrientValueProvider nutrientValueProvider;

	private final ScoreDefinitionService scoreDefinitionService;

	private final ScoreResultWriter scoreResultWriter;

	/**
	 * <p>Constructor for FsaOfcomNutrientProfile.</p>
	 *
	 * @param nutrientValueProvider a {@link fr.becpg.repo.score.NutrientValueProvider} object
	 * @param scoreDefinitionService a {@link fr.becpg.repo.score.ScoreDefinitionService} object
	 * @param scoreResultWriter a {@link fr.becpg.repo.score.ScoreResultWriter} object
	 */
	@Autowired
	public FsaOfcomNutrientProfile(NutrientValueProvider nutrientValueProvider, ScoreDefinitionService scoreDefinitionService,
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

		ScoreContext context = new ScoreContext();
		context.setCode(SCORE_CODE);
		context.setVersion(SCORE_VERSION);
		context.setScale(ScoreScale.Numeric.name());
		context.setUnit(definition.getUnit());

		int aPoints = computeAPoints(nutrients, context);
		int fruitPoints = points(nutrients.get(FRUIT_VEGETABLE_CODE), FRUIT_VEGETABLE_POINTS);
		int fibrePoints = computeFibrePoints(nutrients);
		int proteinPoints = points(nutrients.get(PROTEIN_CODE), PROTEIN_POINTS);

		context.getParts().add(part(FRUIT_VEGETABLE_CODE, nutrients.get(FRUIT_VEGETABLE_CODE), -fruitPoints));
		context.getParts().add(part("FIBRE", fibreValue(nutrients), -fibrePoints));

		int countedProtein = countsProtein(aPoints, fruitPoints) ? proteinPoints : 0;
		context.getParts().add(part(PROTEIN_CODE, nutrients.get(PROTEIN_CODE), -countedProtein));

		double score = (aPoints - fruitPoints - fibrePoints - countedProtein) * 1d;
		context.setValue(score);
		context.setScoreClass(hfssVerdict(product, score));
		context.getSteps().add(new ScorePart("aPoints").withContribution(aPoints * 1d));
		context.getSteps().add(new ScorePart("cPoints").withContribution((fruitPoints + fibrePoints + countedProtein) * 1d));

		return context;
	}

	/**
	 * <p>computeAPoints.</p>
	 *
	 * @param nutrients the documented nutrients
	 * @param context the context collecting the breakdown
	 * @return the total A points
	 */
	private int computeAPoints(Map<String, Double> nutrients, ScoreContext context) {
		int energy = points(nutrients.get(ENERGY_CODE), ENERGY_POINTS);
		int satFat = points(nutrients.get(SATFAT_CODE), SATFAT_POINTS);
		int sugar = points(nutrients.get(SUGAR_CODE), SUGAR_POINTS);
		int sodium = points(sodium(nutrients), SODIUM_POINTS);

		context.getParts().add(part(ENERGY_CODE, nutrients.get(ENERGY_CODE), energy * 1d));
		context.getParts().add(part(SATFAT_CODE, nutrients.get(SATFAT_CODE), satFat * 1d));
		context.getParts().add(part(SUGAR_CODE, nutrients.get(SUGAR_CODE), sugar * 1d));
		context.getParts().add(part(SODIUM_CODE, sodium(nutrients), sodium * 1d));

		return energy + satFat + sugar + sodium;
	}

	/**
	 * The fibre points are earned on whichever of the two fibre methods the product
	 * documents, the guidance letting the manufacturer use either.
	 *
	 * @param nutrients the documented nutrients
	 * @return the fibre points
	 */
	private int computeFibrePoints(Map<String, Double> nutrients) {
		if (nutrients.get(AOAC_FIBRE_CODE) != null) {
			return points(nutrients.get(AOAC_FIBRE_CODE), AOAC_FIBRE_POINTS);
		}
		return points(nutrients.get(NSP_FIBRE_CODE), NSP_FIBRE_POINTS);
	}

	/**
	 * <p>fibreValue.</p>
	 *
	 * @param nutrients the documented nutrients
	 * @return the fibre value actually scored
	 */
	private Double fibreValue(Map<String, Double> nutrients) {
		return nutrients.get(AOAC_FIBRE_CODE) != null ? nutrients.get(AOAC_FIBRE_CODE) : nutrients.get(NSP_FIBRE_CODE);
	}

	/**
	 * Products usually declare salt rather than sodium, the model scoring sodium in
	 * milligrams.
	 *
	 * @param nutrients the documented nutrients
	 * @return the sodium content in milligrams per 100 g
	 */
	private Double sodium(Map<String, Double> nutrients) {
		if (nutrients.get(SODIUM_CODE) != null) {
			return nutrients.get(SODIUM_CODE);
		}
		return nutrients.get(SALT_CODE) != null ? nutrients.get(SALT_CODE) * SALT_TO_SODIUM_MG : null;
	}

	/**
	 * A product scoring eleven A points or more loses its protein points, unless it earns
	 * the full five points for fruit, vegetables and nuts.
	 *
	 * @param aPoints the total A points
	 * @param fruitPoints the fruit, vegetable and nut points
	 * @return a boolean
	 */
	private boolean countsProtein(int aPoints, int fruitPoints) {
		return (aPoints < PROTEIN_CAP_A_POINTS) || (fruitPoints >= MAX_FRUIT_VEGETABLE_POINTS);
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

	/**
	 * <p>part.</p>
	 *
	 * @param code the nutrient code
	 * @param value the nutrient value
	 * @param contribution what the nutrient adds to the score
	 * @return a {@link fr.becpg.repo.score.ScorePart} object
	 */
	private ScorePart part(String code, Double value, Double contribution) {
		return new ScorePart(code).withValue(value, null).withContribution(contribution);
	}

	/**
	 * <p>part.</p>
	 *
	 * @param code the nutrient code
	 * @param value the nutrient value
	 * @param contribution what the nutrient adds to the score
	 * @return a {@link fr.becpg.repo.score.ScorePart} object
	 */
	private ScorePart part(String code, Double value, int contribution) {
		return part(code, value, contribution * 1d);
	}

	/** {@inheritDoc} */
	@Override
	public Optional<ScoreContext> getScoreContext(ScorableEntity scorableEntity) {
		return Optional.empty();
	}


	/**
	 * HFSS verdict of the product. The regulation sets the bar at four points for a food and
	 * at one point for a drink, so the category decides which threshold applies.
	 *
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param score the nutrient profiling score
	 * @return a {@link java.lang.String} object
	 */
	private String hfssVerdict(ProductData product, double score) {
		boolean drink = NutrientProfileCategory.Beverages.toString().equals(product.getNutrientProfileCategory());

		return score >= (drink ? DRINK_HFSS_THRESHOLD : FOOD_HFSS_THRESHOLD) ? HFSS : NON_HFSS;
	}
}
