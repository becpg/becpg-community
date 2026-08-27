package fr.becpg.repo.product.formulation.score;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ScorableEntity;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.productList.PackMaterialListDataItem;
import fr.becpg.repo.project.formulation.ScoreRangeConverter;
import fr.becpg.repo.score.ScoreContext;
import fr.becpg.repo.score.ScoreDefinitionService;
import fr.becpg.repo.score.ScorePart;
import fr.becpg.repo.score.ScoreResultWriter;
import fr.becpg.repo.score.ScoredEntity;
import fr.becpg.repo.score.data.ScoreDefinitionItem;
import fr.becpg.repo.score.data.ScoreThresholdListDataItem;

/**
 * Grades the recyclability of the packaging under the PPWR, regulation (EU) 2025/40.
 *
 * <p>Annex II table 3 grades a packaging unit by the share of its weight that is recyclable:
 * grade A from 95%, B from 80%, C from 70%, and anything below is not recyclable and cannot
 * be placed on the market from the first of January 2030.</p>
 *
 * <p>beCPG computes no recyclability rate of its own: the rate of a material is reference
 * data, one threshold line per material, holding what the declaration of conformity of the
 * supplier or a laboratory report states. The plugin only weighs those rates by the weight
 * each material takes in the packaging.</p>
 *
 * <p>A material is matched by its code, then by its eco-tax category. Reading the code alone
 * would only serve a repository holding the current generation of the material referential,
 * where the category names the same families and is filled far more widely.</p>
 *
 * <p>The regulation grades a packaging unit, not a product: the primary, secondary and
 * tertiary packaging are each assessed on their own. The score of the product is therefore
 * the worst of its levels, since that is the one blocking the placing on the market, and the
 * breakdown states one line per level so the failing unit is named.</p>
 *
 * @author matthieu
 */
@Service("ppwrRecyclability")
public class PpwrRecyclability implements ScoreCalculatingPlugin {

	/** Constant <code>SCORE_CODE="PPWR"</code> */
	public static final String SCORE_CODE = "PPWR";

	/** The parts are named after the packaging level they grade */
	private static final String PART_PREFIX = "PPWR_";

	/** Weight the materials of which no rate is known, always counted as not recyclable */
	private static final String UNKNOWN_PART = "PPWR_UNKNOWN";

	/** Constant <code>PERCENT="%"</code> */
	private static final String PERCENT = "%";

	/** Constant <code>GRAM="g"</code> */
	private static final String GRAM = "g";

	/** Constant <code>NOT_RECYCLABLE=0d</code> */
	private static final double NOT_RECYCLABLE = 0d;

	private final ScoreDefinitionService scoreDefinitionService;

	private final ScoreResultWriter scoreResultWriter;

	private final NodeService nodeService;

	/**
	 * <p>Constructor for PpwrRecyclability.</p>
	 *
	 * @param scoreDefinitionService a {@link fr.becpg.repo.score.ScoreDefinitionService} object
	 * @param scoreResultWriter a {@link fr.becpg.repo.score.ScoreResultWriter} object
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	@Autowired
	public PpwrRecyclability(ScoreDefinitionService scoreDefinitionService, ScoreResultWriter scoreResultWriter,
			@Qualifier("nodeService") NodeService nodeService) {
		this.scoreDefinitionService = scoreDefinitionService;
		this.scoreResultWriter = scoreResultWriter;
		this.nodeService = nodeService;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accept(ScorableEntity scorableEntity) {
		return (scorableEntity instanceof ProductData product) && (product.getPackMaterialList() != null)
				&& !product.getPackMaterialList().isEmpty();
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

		ScoreContext context = buildContext((ProductData) scorableEntity, definition.get());

		if (context.getValue() != null) {
			scoreResultWriter.write(scoredEntity, context);
		}

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
		ScoreContext context = newContext(definition);

		Map<PackagingLevel, PackagingUnit> units = weighUnits(product, definition);
		Double worst = null;

		for (Map.Entry<PackagingLevel, PackagingUnit> entry : units.entrySet()) {
			double share = entry.getValue().recyclableShare();

			context.getParts().add(toPart(entry.getKey(), entry.getValue(), definition));
			worst = (worst == null) ? share : Math.min(worst, share);
		}

		addUndocumentedPart(context, units.values());

		context.setValue(worst);

		return context;
	}

	/**
	 * States the weight the grade could not be given for, so a packaging graded down by a
	 * material nobody documented does not read as a packaging graded down by its design.
	 *
	 * @param context a {@link fr.becpg.repo.score.ScoreContext} object
	 * @param units a {@link java.util.Collection} object
	 */
	private void addUndocumentedPart(ScoreContext context, Collection<PackagingUnit> units) {
		double weight = 0d;

		for (PackagingUnit unit : units) {
			weight += unit.getUnknownWeight();
		}

		if (weight > 0d) {
			context.getParts().add(new ScorePart(UNKNOWN_PART).withValue(weight, GRAM).withCoefficients(null, NOT_RECYCLABLE)
					.withContribution(NOT_RECYCLABLE));
		}
	}

	/**
	 * <p>newContext.</p>
	 *
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 * @return a {@link fr.becpg.repo.score.ScoreContext} object
	 */
	private ScoreContext newContext(ScoreDefinitionItem definition) {
		ScoreContext context = new ScoreContext();

		context.setCode(SCORE_CODE);
		context.setVersion(definition.getVersion());
		context.setScale(definition.getScale());
		context.setRange(definition.getRange());
		context.setUnit(PERCENT);

		return context;
	}

	/**
	 * <p>Weight and recyclable weight of each packaging level, in the order of the levels.</p>
	 *
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 * @return a {@link java.util.Map} object, keyed by packaging level
	 */
	private Map<PackagingLevel, PackagingUnit> weighUnits(ProductData product, ScoreDefinitionItem definition) {
		Map<PackagingLevel, PackagingUnit> units = new EnumMap<>(PackagingLevel.class);

		for (PackMaterialListDataItem line : product.getPackMaterialList()) {
			if ((line.getPmlWeight() == null) || (line.getPmlWeight() <= 0d)) {
				continue;
			}

			// a line left without level is a primary packaging, which is what the historical
			// data holds: reading it as a level of its own would grade a unit nobody declared
			PackagingLevel level = (line.getPkgLevel() != null) ? line.getPkgLevel() : PackagingLevel.Primary;

			units.computeIfAbsent(level, key -> new PackagingUnit()).add(line.getPmlWeight(), rateOf(line, definition));
		}

		return units;
	}

	/**
	 * Recyclability rate of the material of a line.
	 *
	 * <p>A material the repository flags as not recyclable is read as such whatever the
	 * reference data states, the flag being the verdict of the client on its own market.</p>
	 *
	 * @param line a {@link fr.becpg.repo.product.data.productList.PackMaterialListDataItem} object
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 * @return a {@link java.lang.Double} object, null when no rate is known
	 */
	private Double rateOf(PackMaterialListDataItem line, ScoreDefinitionItem definition) {
		NodeRef material = line.getPmlMaterial();

		if (material == null) {
			return null;
		}

		if (isFlaggedNotRecyclable(material)) {
			return NOT_RECYCLABLE;
		}

		Optional<ScoreThresholdListDataItem> rate = findRate(definition, materialCode(material));

		if (rate.isEmpty()) {
			rate = findRate(definition, ecoTaxeCategory(material));
		}

		return rate.map(ScoreThresholdListDataItem::getPoints).orElse(null);
	}

	/**
	 * Code identifying a packaging material in the reference data of the score, read from the
	 * repository. A subclass may serve it from elsewhere.
	 *
	 * @param material a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link java.lang.String} object, null when the material carries no code
	 */
	protected String materialCode(NodeRef material) {
		String code = (String) nodeService.getProperty(material, BeCPGModel.PROP_LV_CODE);

		return ((code == null) || code.isBlank()) ? null : code.trim();
	}

	/**
	 * Eco-tax category of a material, the fallback key of the reference data.
	 *
	 * <p>Material codes come in generations, and a repository holds materials of its own that
	 * carry none. The category of the extended producer responsibility names the same
	 * families and is the one such repositories do fill, so it catches the materials the code
	 * misses.</p>
	 *
	 * @param material a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link java.lang.String} object, null when the material declares none
	 */
	protected String ecoTaxeCategory(NodeRef material) {
		String category = (String) nodeService.getProperty(material, PackModel.PROP_PACK_MATERIAL_ECOTAXE_CATEGORY);

		return ((category == null) || category.isBlank()) ? null : category.trim();
	}

	/**
	 * Whether the repository states the material is not recyclable, read from the material. A
	 * subclass may serve it from elsewhere.
	 *
	 * @param material a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a boolean
	 */
	protected boolean isFlaggedNotRecyclable(NodeRef material) {
		return Boolean.TRUE.equals(nodeService.getProperty(material, PackModel.PROP_PM_ISNOTRECYCLABLE));
	}

	/**
	 * <p>findRate.</p>
	 *
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 * @param code the code of the material
	 * @return a {@link java.util.Optional} object
	 */
	private Optional<ScoreThresholdListDataItem> findRate(ScoreDefinitionItem definition, String code) {
		if ((code == null) || (definition.getThresholdList() == null)) {
			return Optional.empty();
		}

		for (ScoreThresholdListDataItem threshold : definition.getThresholdList()) {
			if (code.equals(threshold.getNutCode())) {
				return Optional.of(threshold);
			}
		}

		return Optional.empty();
	}

	/**
	 * <p>Line of the breakdown stating the grade of one packaging level.</p>
	 *
	 * @param level a {@link fr.becpg.repo.product.data.constraints.PackagingLevel} object
	 * @param unit a {@link fr.becpg.repo.product.formulation.score.PpwrRecyclability.PackagingUnit} object
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 * @return a {@link fr.becpg.repo.score.ScorePart} object
	 */
	private ScorePart toPart(PackagingLevel level, PackagingUnit unit, ScoreDefinitionItem definition) {
		return new ScorePart(PART_PREFIX + level.name().toUpperCase()).withValue(unit.getWeight(), GRAM)
				.withCoefficients(null, unit.recyclableShare()).withContribution(unit.getRecyclableWeight())
				.withScoreClass(gradeOf(unit.recyclableShare(), definition));
	}

	/**
	 * <p>Grade of a share, read from the range of the definition.</p>
	 *
	 * @param share the recyclable share of a packaging unit
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 * @return a {@link java.lang.String} object, null when the definition declares no range
	 */
	private String gradeOf(double share, ScoreDefinitionItem definition) {
		String range = definition.getRange();

		if ((range == null) || range.isBlank()) {
			return null;
		}

		return new ScoreRangeConverter(range).getScoreLetter(share);
	}

	/**
	 * {@inheritDoc}
	 *
	 * The score is published by the plugin itself, once its reference data is read.
	 */
	@Override
	public Optional<ScoreContext> getScoreContext(ScorableEntity scorableEntity) {
		return Optional.empty();
	}

	/**
	 * One packaging unit being graded, that is one packaging level of the product.
	 *
	 * <p>A material of which no rate is known weighs in the unit and adds nothing to its
	 * recyclable weight: leaving it out of the total would grade the unit on the materials
	 * that happen to be documented, and read as compliant a packaging nobody assessed.</p>
	 */
	private static class PackagingUnit {

		private double weight = 0d;

		private double recyclableWeight = 0d;

		private double unknownWeight = 0d;

		void add(double lineWeight, Double rate) {
			weight += lineWeight;

			if (rate == null) {
				unknownWeight += lineWeight;
				return;
			}

			recyclableWeight += (lineWeight * rate) / 100d;
		}

		double getWeight() {
			return weight;
		}

		double getRecyclableWeight() {
			return recyclableWeight;
		}

		double getUnknownWeight() {
			return unknownWeight;
		}

		double recyclableShare() {
			return weight == 0d ? 0d : (recyclableWeight / weight) * 100d;
		}

	}

}
