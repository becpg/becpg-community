package fr.becpg.repo.product.formulation.score;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ScorableEntity;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.score.ScoreContext;
import fr.becpg.repo.score.ScoreDefinitionService;
import fr.becpg.repo.score.ScorePart;
import fr.becpg.repo.score.ScoreResultWriter;
import fr.becpg.repo.score.ScoredEntity;
import fr.becpg.repo.score.data.ScoreDefinitionItem;

/**
 * Grades a product on the French animal welfare label.
 *
 * <p>The label audits the whole life of the animal, in four stages: the parent flock, the
 * rearing, the transport and the slaughter. Each stage is audited yearly by an independent
 * body and comes out at a level from A to E.</p>
 *
 * <p>The requirements are increasing — meeting a criterion at A means meeting it at B, C and
 * D — so a product is only worth its weakest stage. A stage that was never audited stands at
 * E, the level the scheme makes applicable to any product without audit information.</p>
 *
 * <p>The audit referential itself is confidential and stays outside beCPG: what the product
 * carries is the level each stage was awarded, declared as a claim.</p>
 *
 * @author matthieu
 */
@Service("animalWelfareLabel")
public class AnimalWelfareLabel implements ScoreCalculatingPlugin {

	/** Constant <code>SCORE_CODE="ANIMALWELFARE"</code> */
	public static final String SCORE_CODE = "ANIMALWELFARE";

	/** Claims declaring an audited level read as {@code AW_<stage>_<level>} */
	private static final String CLAIM_PREFIX = "AW_";

	private static final String CLAIM_SEPARATOR = "_";

	/** The level applicable to a product without audit information */
	private static final String DEFAULT_LEVEL = "E";

	/** The stages of the life of the animal the label audits, in the order it audits them */
	private static final List<String> STAGES = Arrays.asList("PARENTS", "ELEVAGE", "TRANSPORT", "ABATTAGE");

	private final ScoreDefinitionService scoreDefinitionService;

	private final ScoreResultWriter scoreResultWriter;

	private final NodeService nodeService;

	/**
	 * <p>Constructor for AnimalWelfareLabel.</p>
	 *
	 * @param scoreDefinitionService a {@link fr.becpg.repo.score.ScoreDefinitionService} object
	 * @param scoreResultWriter a {@link fr.becpg.repo.score.ScoreResultWriter} object
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	@Autowired
	public AnimalWelfareLabel(ScoreDefinitionService scoreDefinitionService, ScoreResultWriter scoreResultWriter,
			@Qualifier("nodeService") NodeService nodeService) {
		this.scoreDefinitionService = scoreDefinitionService;
		this.scoreResultWriter = scoreResultWriter;
		this.nodeService = nodeService;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accept(ScorableEntity scorableEntity) {
		return (scorableEntity instanceof ProductData product) && !auditedLevels(product).isEmpty();
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
		ScoreContext context = new ScoreContext();

		context.setCode(SCORE_CODE);
		context.setVersion(definition.getVersion());
		context.setScale(definition.getScale());

		Map<String, String> audited = auditedLevels(product);
		String worst = null;

		for (String stage : STAGES) {
			String level = audited.getOrDefault(stage, DEFAULT_LEVEL);

			context.getParts().add(new ScorePart(stage).withScoreClass(level));

			if ((worst == null) || (level.compareTo(worst) > 0)) {
				worst = level;
			}
		}

		context.setScoreClass(worst);

		return context;
	}

	/**
	 * <p>Level awarded to each audited stage, read from the claims of the product.</p>
	 *
	 * <p>A stage claimed at several levels keeps the most demanding one: the requirements
	 * being increasing, a stage audited at A also meets the levels below it.</p>
	 *
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 * @return a {@link java.util.Map} object, never null
	 */
	private Map<String, String> auditedLevels(ProductData product) {
		Map<String, String> levels = new LinkedHashMap<>();

		if (product.getLabelClaimList() == null) {
			return levels;
		}

		for (LabelClaimListDataItem item : product.getLabelClaimList()) {
			if (!Boolean.TRUE.equals(item.getIsClaimed())) {
				continue;
			}

			String code = code(item.getLabelClaim());
			if ((code == null) || !code.startsWith(CLAIM_PREFIX)) {
				continue;
			}

			addLevel(levels, code);
		}

		return levels;
	}

	/**
	 * <p>Records the level a claim awards its stage, keeping the most demanding one.</p>
	 *
	 * @param levels the levels gathered so far
	 * @param code the claim code, {@code AW_<stage>_<level>}
	 */
	private void addLevel(Map<String, String> levels, String code) {
		int separator = code.lastIndexOf(CLAIM_SEPARATOR);
		String stage = code.substring(CLAIM_PREFIX.length(), separator < 0 ? code.length() : separator);
		String level = separator < 0 ? "" : code.substring(separator + 1);

		if (!STAGES.contains(stage) || (level.length() != 1)) {
			return;
		}

		String known = levels.get(stage);
		if ((known == null) || (level.compareTo(known) < 0)) {
			levels.put(stage, level);
		}
	}

	/**
	 * <p>code.</p>
	 *
	 * @param labelClaim a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link java.lang.String} object
	 */
	private String code(NodeRef labelClaim) {
		return labelClaim != null ? (String) nodeService.getProperty(labelClaim, BeCPGModel.PROP_CODE) : null;
	}

	/**
	 * {@inheritDoc}
	 *
	 * The level is published by the plugin itself.
	 */
	@Override
	public Optional<ScoreContext> getScoreContext(ScorableEntity scorableEntity) {
		return Optional.empty();
	}

}
