package fr.becpg.repo.product.formulation.score;

import java.io.Serializable;
import java.text.Normalizer;
import java.util.Optional;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ScorableEntity;
import fr.becpg.repo.product.data.productList.PackMaterialListDataItem;
import fr.becpg.repo.score.ScoreContext;
import fr.becpg.repo.score.ScoreDefinitionService;
import fr.becpg.repo.score.ScorePart;
import fr.becpg.repo.score.ScoreResultWriter;
import fr.becpg.repo.score.ScoredEntity;
import fr.becpg.repo.score.data.ScoreDefinitionItem;
import fr.becpg.repo.score.data.ScoreThresholdListDataItem;

/**
 * Computes the extended producer responsibility contribution of a product.
 *
 * <p>The scale is data: one threshold line per packaging material, holding its tariff per
 * kilo. The contribution is the weight of each material multiplied by its tariff, reduced by
 * the recycled content bonus when the material reaches the threshold the scheme sets.</p>
 *
 * <p>The breakdown states one part per material, so the published score reads like the
 * simulation sheet it replaces.</p>
 *
 * @author matthieu
 */
@Service("repEcoModulation")
public class RepEcoModulation implements ScoreCalculatingPlugin {

	/** Constant <code>SCORE_CODE="REPCITEO"</code> */
	public static final String SCORE_CODE = "REPCITEO";

	/** Half the material recycled earns the bonus */
	private static final double RECYCLED_THRESHOLD = 50d;

	/** The bonus cuts the contribution of the material by this share */
	private static final double RECYCLED_BONUS = 0.2d;

	/** Constant <code>EURO="EUR"</code> */
	private static final String EURO = "EUR";

	/** Weights are held in grams, the tariff is stated per kilo */
	private static final double GRAMS_PER_KILO = 1000d;

	private final ScoreDefinitionService scoreDefinitionService;

	private final ScoreResultWriter scoreResultWriter;

	private final NodeService nodeService;

	/**
	 * <p>Constructor for RepEcoModulation.</p>
	 *
	 * @param scoreDefinitionService a {@link fr.becpg.repo.score.ScoreDefinitionService} object
	 * @param scoreResultWriter a {@link fr.becpg.repo.score.ScoreResultWriter} object
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	@Autowired
	public RepEcoModulation(ScoreDefinitionService scoreDefinitionService, ScoreResultWriter scoreResultWriter,
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

		if (!context.getParts().isEmpty()) {
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
		ScoreContext context = new ScoreContext();

		context.setCode(SCORE_CODE);
		context.setVersion(definition.getVersion());
		context.setScale(definition.getScale());
		context.setUnit(EURO);

		double total = 0d;

		for (PackMaterialListDataItem line : product.getPackMaterialList()) {
			Optional<ScorePart> part = toPart(line, definition);
			if (part.isPresent()) {
				context.getParts().add(part.get());
				total += part.get().getContribution();
			}
		}

		context.setValue(total);

		return context;
	}

	/**
	 * <p>Contribution of one material: its weight times its tariff, less the bonus.</p>
	 *
	 * @param line a {@link fr.becpg.repo.product.data.productList.PackMaterialListDataItem} object
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 * @return a {@link java.util.Optional} object
	 */
	private Optional<ScorePart> toPart(PackMaterialListDataItem line, ScoreDefinitionItem definition) {
		if ((line.getPmlMaterial() == null) || (line.getPmlWeight() == null) || (line.getPmlWeight() == 0d)) {
			return Optional.empty();
		}

		String code = materialCode(line.getPmlMaterial());
		Optional<ScoreThresholdListDataItem> tariff = findTariff(definition, code);

		if (tariff.isEmpty() || (tariff.get().getPoints() == null)) {
			return Optional.empty();
		}

		double kilos = line.getPmlWeight() / GRAMS_PER_KILO;
		double contribution = kilos * tariff.get().getPoints() * (1d - bonus(line));

		return Optional.of(new ScorePart(code).withLabel(tariff.get().getResult()).withValue(line.getPmlWeight(), null)
				.withCoefficients(null, tariff.get().getPoints()).withContribution(contribution));
	}

	/**
	 * <p>Share of the contribution taken off by the recycled content bonus.</p>
	 *
	 * @param line a {@link fr.becpg.repo.product.data.productList.PackMaterialListDataItem} object
	 * @return a double
	 */
	private double bonus(PackMaterialListDataItem line) {
		Double recycled = line.getPmlRecycledPercentage();

		return ((recycled != null) && (recycled >= RECYCLED_THRESHOLD)) ? RECYCLED_BONUS : 0d;
	}

	/**
	 * <p>findTariff.</p>
	 *
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 * @param code the material code
	 * @return a {@link java.util.Optional} object
	 */
	private Optional<ScoreThresholdListDataItem> findTariff(ScoreDefinitionItem definition, String code) {
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
	 * <p>materialCode.</p>
	 *
	 * @param material a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link java.lang.String} object
	 */
	private String materialCode(NodeRef material) {
		String code = (String) nodeService.getProperty(material, BeCPGModel.PROP_CODE);

		return ((code != null) && !code.isBlank()) ? code : normalize(nodeService.getProperty(material, ContentModel.PROP_NAME));
	}

	/**
	 * <p>Materials are created by each repository, so their code is rarely the one of the
	 * scale: their name is then read as a code, "Carton ondulé" standing for CARTON_ONDULE.</p>
	 *
	 * @param name the name of the material
	 * @return a {@link java.lang.String} object
	 */
	private String normalize(Serializable name) {
		if (name == null) {
			return null;
		}

		String stripped = Normalizer.normalize(name.toString(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");

		return stripped.toUpperCase().replaceAll("[^A-Z0-9]+", "_");
	}

	/**
	 * {@inheritDoc}
	 *
	 * The score is published by the plugin itself, once its scale is read.
	 */
	@Override
	public Optional<ScoreContext> getScoreContext(ScorableEntity scorableEntity) {
		return Optional.empty();
	}

}
