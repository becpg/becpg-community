package fr.becpg.repo.product.formulation.score;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ScorableEntity;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.score.ScoreContext;
import fr.becpg.repo.score.data.RegulatoryScoreListDataItem;
import fr.becpg.repo.score.ScorePart;
import fr.becpg.repo.score.ScoreScale;

/**
 * Computes the Planet-score of a product from the claims it carries.
 *
 * <p>The mark is not a weighted average: it states four themes side by side and sums them up
 * with the colour of its border. The green and orange levels require every theme to reach
 * their minimum, the red level is reached as soon as one theme falls to it, so the synthesis
 * is the worst of the themes.</p>
 *
 * <p>Farming, fair pay and origin are declared, one claim per level. The processing level
 * follows the Goum reading: the worst marker of the product, turned to level 3 when it holds
 * three markers of level 2, and to level 4 when it holds three of level 3.</p>
 *
 * @author matthieu
 */
@Service("planetScore")
public class PlanetScore implements ScoreCalculatingPlugin {

	/** Constant <code>SCORE_CODE="PLANETSCORE"</code> */
	public static final String SCORE_CODE = "PLANETSCORE";

	/** Constant <code>CLAIM_PREFIX="PS_"</code> */
	private static final String CLAIM_PREFIX = "PS_";

	/** Constant <code>FARMING="ELEVAGE"</code> */
	private static final String FARMING = "ELEVAGE";

	/** Constant <code>FAIR_PAY="REMUN"</code> */
	private static final String FAIR_PAY = "REMUN";

	/** Constant <code>ORIGIN="ORIGINE"</code> */
	private static final String ORIGIN = "ORIGINE";

	/** Constant <code>FAIR_TRADE_CLAIM="PS_EQUITABLE"</code> */
	private static final String FAIR_TRADE_CLAIM = "PS_EQUITABLE";

	/** Half the ingredients is the threshold the mark states for fair pay and for origin */
	private static final double FAIR_TRADE_THRESHOLD = 50d;

	/** The origin is claimed above four fifths of the ingredients */
	private static final double ORIGIN_THRESHOLD = 80d;

	/** Below a fifth from outside Europe the mark still shows a European origin */
	private static final double OUTSIDE_THRESHOLD = 20d;

	/** Constant <code>FRANCE="FR"</code> */
	private static final String FRANCE = "FR";

	private static final String LEVEL_A = "A";

	private static final String LEVEL_B = "B";

	private static final String LEVEL_C = "C";

	private static final String LEVEL_D = "D";

	private static final String EUROPE = "EU";

	private static final String WORLD = "MONDE";

	/** The environmental axes are graded on a five letter scale of their own */
	private static final String WORST_AXIS_CLASS = "E";

	/** Member states, the mark tells France, Europe and the rest of the world apart */
	private static final Set<String> EUROPEAN_UNION = Set.of("AT", "BE", "BG", "CY", "CZ", "DE", "DK", "EE", "ES", "FI",
			"FR", "GR", "HR", "HU", "IE", "IT", "LT", "LU", "LV", "MT", "NL", "PL", "PT", "RO", "SE", "SI", "SK");

	/** Constant <code>PROCESSING="UT"</code> */
	private static final String PROCESSING = "UT";

	/** The three environmental axes, computed from the life cycle indicators */
	private static final String[] ENVIRONMENT_AXES = { "PLANETSCORE_PESTICIDES", "PLANETSCORE_BIODIVERSITE",
			"PLANETSCORE_CLIMAT" };

	/** Constant <code>PERCENT="%"</code> */
	private static final String PERCENT = "%";

	/** Constant <code>LEVELS="ABCD"</code> */
	private static final String LEVELS = "ABCD";

	/** Three markers of a level are enough to fall to the next one */
	private static final int MARKERS_TO_DOWNGRADE = 3;

	private final NodeService nodeService;

	/**
	 * <p>Constructor for PlanetScore.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	@Autowired
	public PlanetScore(@Qualifier("nodeService") NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accept(ScorableEntity scorableEntity) {
		return (scorableEntity instanceof ProductData product) && !claimedCodes(product).isEmpty();
	}

	/** {@inheritDoc} */
	@Override
	public String getCode() {
		return SCORE_CODE;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Nothing to compute outside the breakdown: the themes are read from the claims.
	 */
	@Override
	public boolean formulateScore(ScorableEntity scorableEntity) {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public Optional<ScoreContext> getScoreContext(ScorableEntity scorableEntity) {
		if (!(scorableEntity instanceof ProductData product)) {
			return Optional.empty();
		}

		List<String> codes = claimedCodes(product);
		if (codes.isEmpty()) {
			return Optional.empty();
		}

		ScoreContext context = new ScoreContext();
		context.setCode(SCORE_CODE);
		context.setScale(ScoreScale.Letter.name());

		addEnvironment(context, product);
		addTheme(context, codes, FARMING);
		addFairPay(context, product);
		addOrigin(context, product);
		addProcessing(context, codes);

		context.setScoreClass(worstLevel(context));

		return Optional.of(context);
	}

	/**
	 * The three environmental axes are scores of their own, aggregated from the life cycle
	 * indicators: the mark reads their class rather than recomputing them.
	 *
	 * @param context a {@link fr.becpg.repo.score.ScoreContext} object
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 */
	private void addEnvironment(ScoreContext context, ProductData product) {
		for (String axis : ENVIRONMENT_AXES) {
			String reached = publishedClass(product, axis);
			if (reached != null) {
				context.getParts().add(new ScorePart(axis).withScoreClass(reached));
			}
		}
	}

	/**
	 * <p>Class of a score already published on the product.</p>
	 *
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param code the score code
	 * @return a {@link java.lang.String} object, null when the score is not published
	 */
	private String publishedClass(ProductData product, String code) {
		if (product.getRegulatoryScoreList() == null) {
			return null;
		}

		for (RegulatoryScoreListDataItem published : product.getRegulatoryScoreList()) {
			String details = published.getDetails();
			if ((details != null) && !details.isBlank() && code.equals(ScoreContext.parse(details).getCode())) {
				return published.getScoreClass();
			}
		}

		return null;
	}

	/**
	 * <p>Level declared for a theme, the worst one when several are claimed.</p>
	 *
	 * @param context a {@link fr.becpg.repo.score.ScoreContext} object
	 * @param codes the claimed codes
	 * @param theme the theme to read
	 */
	private void addTheme(ScoreContext context, List<String> codes, String theme) {
		String level = null;
		for (String code : codes) {
			String claimed = levelOf(code, theme);
			if ((claimed != null) && ((level == null) || (claimed.compareTo(level) > 0))) {
				level = claimed;
			}
		}

		if (level != null) {
			context.getParts().add(new ScorePart(theme).withScoreClass(level));
		}
	}

	/**
	 * Processing level of the product, read the way the Goum reference does: the worst marker
	 * it carries, downgraded when it holds three markers of the same level.
	 *
	 * @param context a {@link fr.becpg.repo.score.ScoreContext} object
	 * @param codes the claimed codes
	 */
	private void addProcessing(ScoreContext context, List<String> codes) {
		Map<Integer, Integer> markers = new HashMap<>();
		int worst = 0;

		for (String code : codes) {
			Integer level = processingLevel(code);
			if (level != null) {
				markers.merge(level, 1, Integer::sum);
				worst = Math.max(worst, level);
			}
		}

		if (worst == 0) {
			return;
		}

		if (markers.getOrDefault(3, 0) >= MARKERS_TO_DOWNGRADE) {
			worst = 4;
		} else if (markers.getOrDefault(2, 0) >= MARKERS_TO_DOWNGRADE) {
			worst = Math.max(worst, 3);
		}

		context.getParts().add(new ScorePart(PROCESSING).withScoreClass(String.valueOf(LEVELS.charAt(worst - 1))));
	}

	/**
	 * <p>The synthesis is the worst level of the themes.</p>
	 *
	 * @param context a {@link fr.becpg.repo.score.ScoreContext} object
	 * @return a {@link java.lang.String} object
	 */
	private String worstLevel(ScoreContext context) {
		String worst = null;

		for (ScorePart part : context.getParts()) {
			// an axis whose value fell outside its scale carries no level: it must not become
			// the verdict of the whole mark by comparing above every letter
			String level = levelOfAxis(part.getScoreClass());

			if ((level != null) && ((worst == null) || (level.compareTo(worst) > 0))) {
				worst = level;
			}
		}

		return worst;
	}

	/**
	 * <p>Level of the mark an axis stands at.</p>
	 *
	 * <p>The mark has four levels where the environmental axes are graded on a five letter
	 * scale of their own: an axis at E stands at the worst level of the mark. An axis whose
	 * value fell outside its scale carries no level and must not become the verdict of the
	 * whole mark by comparing above every letter.</p>
	 *
	 * @param scoreClass the class carried by an axis
	 * @return a {@link java.lang.String} object, null when the axis carries no level
	 */
	private String levelOfAxis(String scoreClass) {
		if ((scoreClass == null) || (scoreClass.length() != 1) || (LEVEL_A.compareTo(scoreClass) > 0)) {
			return null;
		}
		if (WORST_AXIS_CLASS.compareTo(scoreClass) < 0) {
			return null;
		}
		return LEVEL_D.compareTo(scoreClass) < 0 ? LEVEL_D : scoreClass;
	}

	/**
	 * <p>levelOf.</p>
	 *
	 * @param code the claim code
	 * @param theme the theme to read
	 * @return the level, null when the code does not belong to the theme
	 */
	private String levelOf(String code, String theme) {
		String prefix = CLAIM_PREFIX + theme + "_";
		if (!code.startsWith(prefix)) {
			return null;
		}

		String suffix = code.substring(prefix.length());
		return LEVELS.contains(suffix) ? suffix : originLevel(suffix);
	}

	/**
	 * The origin is not graded, it only tells the flag apart: France and Europe are the levels
	 * the mark asks for, anything else weighs like the lowest one.
	 *
	 * @param suffix the part of the code naming the origin
	 * @return a {@link java.lang.String} object
	 */
	private String originLevel(String suffix) {
		if ("FR".equals(suffix)) {
			return LEVEL_A;
		}
		if (EUROPE.equals(suffix)) {
			return LEVEL_B;
		}
		return WORLD.equals(suffix) ? LEVEL_C : null;
	}

	/**
	 * <p>processingLevel.</p>
	 *
	 * @param code the claim code
	 * @return the Goum level, null when the code is not a processing marker
	 */
	private Integer processingLevel(String code) {
		String prefix = CLAIM_PREFIX + PROCESSING + "_";
		if (!code.startsWith(prefix)) {
			return null;
		}

		try {
			int level = Integer.parseInt(code.substring(prefix.length()));
			return ((level >= 1) && (level <= LEVELS.length())) ? level : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * <p>Codes of the Planet-score claims the product actually claims.</p>
	 *
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 * @return a {@link java.util.List} object, never null
	 */
	private List<String> claimedCodes(ProductData product) {
		List<String> codes = new ArrayList<>();

		if (product.getLabelClaimList() == null) {
			return codes;
		}

		for (LabelClaimListDataItem item : product.getLabelClaimList()) {
			if (!Boolean.TRUE.equals(item.getIsClaimed()) || (item.getLabelClaim() == null)) {
				continue;
			}

			String code = code(item.getLabelClaim());
			if ((code != null) && code.startsWith(CLAIM_PREFIX)) {
				codes.add(code);
			}
		}

		return codes;
	}

	/**
	 * <p>code.</p>
	 *
	 * @param labelClaim a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link java.lang.String} object
	 */
	private String code(NodeRef labelClaim) {
		return (String) nodeService.getProperty(labelClaim, PLMModel.PROP_LABEL_CLAIM_CODE);
	}


	/**
	 * Fair pay is a share, not a level: the mark reads the part of the ingredients covered by a
	 * fair trade certification, and asks for more than half.
	 *
	 * @param context a {@link fr.becpg.repo.score.ScoreContext} object
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 */
	private void addFairPay(ScoreContext context, ProductData product) {
		Double claimed = fairTradeShare(product);

		if (claimed == null) {
			// unknown, the mark shows its grey level rather than a bad one
			context.getParts().add(new ScorePart(FAIR_PAY).withScoreClass(LEVEL_B));
			return;
		}

		context.getParts().add(new ScorePart(FAIR_PAY).withValue(claimed, PERCENT)
				.withScoreClass(claimed > FAIR_TRADE_THRESHOLD ? LEVEL_A : LEVEL_D));
	}

	/**
	 * <p>Share of the ingredients covered by a fair trade certification.</p>
	 *
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 * @return a {@link java.lang.Double} object, null when the claim is not carried
	 */
	private Double fairTradeShare(ProductData product) {
		if (product.getLabelClaimList() == null) {
			return null;
		}

		for (LabelClaimListDataItem item : product.getLabelClaimList()) {
			if ((item.getLabelClaim() != null) && FAIR_TRADE_CLAIM.equals(code(item.getLabelClaim()))) {
				return item.getPercentClaim();
			}
		}

		return null;
	}

	/**
	 * Origin is not declared either: it is read on the ingredients, weighted by their share of
	 * the recipe, and told apart between France, the union and the rest of the world.
	 *
	 * @param context a {@link fr.becpg.repo.score.ScoreContext} object
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 */
	private void addOrigin(ScoreContext context, ProductData product) {
		if ((product.getIngList() == null) || product.getIngList().isEmpty()) {
			return;
		}

		double known = 0d;
		double french = 0d;
		double european = 0d;

		for (IngListDataItem ing : product.getIngList()) {
			Double qty = ing.getQtyPerc();
			if ((qty == null) || (ing.getGeoOrigin() == null) || ing.getGeoOrigin().isEmpty()) {
				continue;
			}

			known += qty;
			String iso = isoCode(ing.getGeoOrigin().get(0));

			if (FRANCE.equals(iso)) {
				french += qty;
			}
			if ((iso != null) && EUROPEAN_UNION.contains(iso)) {
				european += qty;
			}
		}

		if (known == 0d) {
			return;
		}

		double frenchShare = (french * 100d) / known;
		double europeanShare = (european * 100d) / known;

		if (frenchShare > ORIGIN_THRESHOLD) {
			context.getParts().add(new ScorePart(ORIGIN).withValue(frenchShare, PERCENT).withScoreClass(LEVEL_A));
		} else if (europeanShare > ORIGIN_THRESHOLD) {
			context.getParts().add(new ScorePart(ORIGIN).withValue(europeanShare, PERCENT).withScoreClass(LEVEL_B));
		} else if ((100d - europeanShare) > OUTSIDE_THRESHOLD) {
			context.getParts().add(new ScorePart(ORIGIN).withValue(100d - europeanShare, PERCENT).withScoreClass(LEVEL_C));
		}
	}

	/**
	 * <p>isoCode.</p>
	 *
	 * @param geoOrigin a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link java.lang.String} object
	 */
	private String isoCode(NodeRef geoOrigin) {
		return (String) nodeService.getProperty(geoOrigin, PLMModel.PROP_GEO_ORIGIN_ISOCODE);
	}
}
