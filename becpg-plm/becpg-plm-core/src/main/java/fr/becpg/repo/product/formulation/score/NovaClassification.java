package fr.becpg.repo.product.formulation.score;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ScorableEntity;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.score.ScoreContext;
import fr.becpg.repo.score.ScoreDefinitionService;
import fr.becpg.repo.score.ScorePart;
import fr.becpg.repo.score.ScoreResultWriter;
import fr.becpg.repo.score.ScoredEntity;
import fr.becpg.repo.score.data.ScoreDefinitionItem;

/**
 * Classifies a product in one of the four NOVA groups of food processing.
 *
 * <p>The classification is read from the ingredients rather than declared: a marker of
 * industrial formulation puts the product in group 4, an added culinary ingredient in group
 * 3, and a composition made of nothing but culinary ingredients in group 2.</p>
 *
 * <p>The markers of group 4 are the additive functions that exist to make an industrial
 * formulation palatable — colours, flavour enhancers, sweeteners, emulsifiers, thickeners
 * and the like. The additives that only preserve or stabilise a food are not markers, which
 * is what separates a group 3 preserve from a group 4 formulation.</p>
 *
 * @author matthieu
 */
@Service("novaClassification")
public class NovaClassification implements ScoreCalculatingPlugin {

	/** Constant <code>SCORE_CODE="NOVA"</code> */
	public static final String SCORE_CODE = "NOVA";

	/** Additive functions that mark an industrial formulation */
	private static final Set<String> COSMETIC_ADDITIVES = new HashSet<>(Arrays.asList("colour", "colour additive", "color additive",
			"flavour enhancer", "flavor enhancer", "sweetener", "emulsifier", "thickener", "gelling agent", "bulking agent", "foaming agent",
			"anti-foaming agent", "antifoaming agent", "glazing agent", "humectant", "firming agent", "stabiliser", "stabilizer"));

	/** Ingredient types that are themselves culinary ingredients */
	private static final Set<String> CULINARY_INGREDIENTS = new HashSet<>(
			Arrays.asList("salt", "sugar", "oil", "fat", "vinegar", "starch", "honey"));

	private final ScoreDefinitionService scoreDefinitionService;

	private final ScoreResultWriter scoreResultWriter;

	private final NodeService nodeService;

	/**
	 * <p>Constructor for NovaClassification.</p>
	 *
	 * @param scoreDefinitionService a {@link fr.becpg.repo.score.ScoreDefinitionService} object
	 * @param scoreResultWriter a {@link fr.becpg.repo.score.ScoreResultWriter} object
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	@Autowired
	public NovaClassification(ScoreDefinitionService scoreDefinitionService, ScoreResultWriter scoreResultWriter,
			@Qualifier("nodeService") NodeService nodeService) {
		this.scoreDefinitionService = scoreDefinitionService;
		this.scoreResultWriter = scoreResultWriter;
		this.nodeService = nodeService;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accept(ScorableEntity scorableEntity) {
		return (scorableEntity instanceof ProductData product) && (product.getIngList() != null) && !product.getIngList().isEmpty();
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

		Map<NodeRef, String> codes = readIngredientTypeCodes(product);

		List<IngListDataItem> markers = markersOf(product, COSMETIC_ADDITIVES, codes);
		List<IngListDataItem> culinary = markersOf(product, CULINARY_INGREDIENTS, codes);

		int group = group(markers, culinary, product);

		context.setValue(group * 1d);
		context.setScoreClass(String.valueOf(group));

		for (IngListDataItem marker : markers) {
			String label = labelOf(marker);
			context.getParts().add(new ScorePart(label).withLabel(label).withContribution(1d));
		}

		return context;
	}

	/**
	 * <p>The NOVA group a product belongs to.</p>
	 *
	 * @param markers the ingredients marking an industrial formulation
	 * @param culinary the culinary ingredients found in the composition
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 * @return an int between 1 and 4
	 */
	private int group(List<IngListDataItem> markers, List<IngListDataItem> culinary, ProductData product) {
		if (!markers.isEmpty()) {
			return 4;
		}
		if (!culinary.isEmpty()) {
			return product.getIngList().size() == culinary.size() ? 2 : 3;
		}
		return 1;
	}

	/**
	 * <p>Ingredients of the product whose type belongs to a set of markers.</p>
	 *
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param markers the marker types, in lower case
	 * @param codes the code of every ingredient type of the product
	 * @return a {@link java.util.List} object, never null
	 */
	private List<IngListDataItem> markersOf(ProductData product, Set<String> markers, Map<NodeRef, String> codes) {
		return product.getIngList().stream().filter(ing -> matches(ing, markers, codes)).toList();
	}

	/**
	 * <p>Whether an ingredient carries one of the marker types.</p>
	 *
	 * @param ing a {@link fr.becpg.repo.product.data.productList.IngListDataItem} object
	 * @param markers the marker types, in lower case
	 * @param codes the code of every ingredient type of the product
	 * @return a boolean
	 */
	private boolean matches(IngListDataItem ing, Set<String> markers, Map<NodeRef, String> codes) {
		if (ing.getIngTypes() == null) {
			return false;
		}

		for (NodeRef ingType : ing.getIngTypes()) {
			String code = codes.get(ingType);
			if ((code != null) && markers.contains(code)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * <p>Codes of every ingredient type used by a product, read once.</p>
	 *
	 * <p>The same type is carried by many ingredients, and the classification tests each one
	 * against two sets of markers: reading the referential once spares as many repository
	 * reads as there are ingredients.</p>
	 *
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 * @return a {@link java.util.Map} object, never null
	 */
	private Map<NodeRef, String> readIngredientTypeCodes(ProductData product) {
		Map<NodeRef, String> codes = new HashMap<>();

		for (IngListDataItem ing : product.getIngList()) {
			if (ing.getIngTypes() == null) {
				continue;
			}
			for (NodeRef ingType : ing.getIngTypes()) {
				codes.computeIfAbsent(ingType, this::lowerCaseCode);
			}
		}

		return codes;
	}

	/**
	 * <p>lowerCaseCode.</p>
	 *
	 * @param ingType a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link java.lang.String} object
	 */
	private String lowerCaseCode(NodeRef ingType) {
		String code = (String) nodeService.getProperty(ingType, BeCPGModel.PROP_LV_CODE);
		return code != null ? code.toLowerCase() : null;
	}

	/**
	 * <p>labelOf.</p>
	 *
	 * @param ing a {@link fr.becpg.repo.product.data.productList.IngListDataItem} object
	 * @return a {@link java.lang.String} object
	 */
	private String labelOf(IngListDataItem ing) {
		Object name = ing.getIng() != null ? nodeService.getProperty(ing.getIng(), BeCPGModel.PROP_CHARACT_NAME) : null;
		return name != null ? name.toString() : "";
	}

	/**
	 * {@inheritDoc}
	 *
	 * The classification is published by the plugin itself.
	 */
	@Override
	public Optional<ScoreContext> getScoreContext(ScorableEntity scorableEntity) {
		return Optional.empty();
	}

}
