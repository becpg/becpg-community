/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.LCAListDataItem;
import fr.becpg.repo.project.formulation.ScoreRangeConverter;
import fr.becpg.repo.score.data.ScoreDefCoeffListDataItem;
import fr.becpg.repo.score.data.ScoreDefinitionItem;

/**
 * Aggregates the life cycle indicators of a product into a single score.
 *
 * <p>Each indicator is divided by its normalization factor then weighted, which is the
 * shape shared by the PEF single score and by the Ecobalyse environmental cost: only the
 * factors differ, and they are data. The factors come from the coefficients of the
 * definition, falling back on the ones carried by the indicator itself so a repository
 * that never declared coefficients keeps its historical single score.</p>
 *
 * @author matthieu
 */
@Service("lcaScoreEngine")
public class LcaScoreEngine {

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(LcaScoreEngine.class);

	private final ScoreDefinitionService scoreDefinitionService;

	private final NodeService nodeService;

	/**
	 * <p>Constructor for LcaScoreEngine.</p>
	 *
	 * @param scoreDefinitionService a {@link fr.becpg.repo.score.ScoreDefinitionService} object
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	@Autowired
	public LcaScoreEngine(ScoreDefinitionService scoreDefinitionService, @Qualifier("nodeService") NodeService nodeService) {
		this.scoreDefinitionService = scoreDefinitionService;
		this.nodeService = nodeService;
	}

	/**
	 * <p>Computes the aggregated score of a product for one definition.</p>
	 *
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 * @return a {@link fr.becpg.repo.score.ScoreContext} object, its parts empty when nothing could be aggregated
	 */
	public ScoreContext compute(ProductData product, ScoreDefinitionItem definition) {
		ScoreContext context = newContext(definition);

		if ((product.getLcaList() == null) || product.getLcaList().isEmpty()) {
			return context;
		}

		// a definition declaring its own coefficients states which indicators it aggregates;
		// only the historical single score falls back on the ones carried by the indicators
		boolean declared = !scoreDefinitionService.getCoefficients(definition).isEmpty();

		Set<String> seen = new HashSet<>();
		double total = 0d;

		for (LCAListDataItem line : product.getLcaList()) {
			if ((line.getLca() == null) || (declared && scoreDefinitionService.findCoefficient(definition, line.getLca()).isEmpty())) {
				continue;
			}

			// the referential may hold several indicators sharing a code, they must not count twice
			if (!seen.add(code(line.getLca()))) {
				continue;
			}

			Optional<ScorePart> part = toPart(line, definition);
			if (part.isPresent()) {
				context.getParts().add(part.get());
				total += part.get().getContribution();
			}
		}

		if (context.getParts().isEmpty()) {
			return context;
		}

		Double value = scale(total, definition);
		context.setValue(value);

		if ((definition.getRange() != null) && !definition.getRange().isBlank()) {
			context.setScoreClass(new ScoreRangeConverter(definition.getRange()).getScoreLetter(value));
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Aggregated " + context.getParts().size() + " indicators into " + context.getValue() + " for " + definition.getCode());
		}

		return context;
	}

	/**
	 * <p>newContext.</p>
	 *
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 * @return a {@link fr.becpg.repo.score.ScoreContext} object
	 */
	private ScoreContext newContext(ScoreDefinitionItem definition) {
		ScoreContext context = new ScoreContext();

		context.setCode(definition.getCode());
		context.setVersion(definition.getVersion());
		context.setScale(definition.getScale());
		context.setUnit(definition.getUnit());

		return context;
	}

	/**
	 * <p>Turns one indicator of the product into a weighted contribution.</p>
	 *
	 * @param line a {@link fr.becpg.repo.product.data.productList.LCAListDataItem} object
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 * @return a {@link java.util.Optional} object
	 */
	private Optional<ScorePart> toPart(LCAListDataItem line, ScoreDefinitionItem definition) {
		if ((line.getLca() == null) || (line.getValue() == null)) {
			return Optional.empty();
		}

		Double normalization = normalization(line.getLca(), definition);
		Double weight = weight(line.getLca(), definition);

		if ((normalization == null) || (normalization == 0d) || (weight == null)) {
			return Optional.empty();
		}

		double contribution = (line.getValue() / normalization) * weight;

		return Optional.of(new ScorePart(code(line.getLca())).withValue(line.getValue(), line.getUnit())
				.withCoefficients(normalization, weight).withContribution(contribution));
	}

	/**
	 * <p>Normalization factor of an indicator, from the definition then from the indicator.</p>
	 *
	 * @param lcaNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 * @return a {@link java.lang.Double} object
	 */
	private Double normalization(NodeRef lcaNodeRef, ScoreDefinitionItem definition) {
		Optional<ScoreDefCoeffListDataItem> coefficient = scoreDefinitionService.findCoefficient(definition, lcaNodeRef);

		if (coefficient.isPresent() && (coefficient.get().getNormalization() != null)) {
			return coefficient.get().getNormalization();
		}
		return (Double) nodeService.getProperty(lcaNodeRef, PLMModel.PROP_LCA_NORMALIZATION);
	}

	/**
	 * <p>Weight of an indicator, from the definition then from the indicator.</p>
	 *
	 * @param lcaNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 * @return a {@link java.lang.Double} object
	 */
	private Double weight(NodeRef lcaNodeRef, ScoreDefinitionItem definition) {
		Optional<ScoreDefCoeffListDataItem> coefficient = scoreDefinitionService.findCoefficient(definition, lcaNodeRef);

		if (coefficient.isPresent() && (coefficient.get().getPonderation() != null)) {
			return coefficient.get().getPonderation();
		}
		return (Double) nodeService.getProperty(lcaNodeRef, PLMModel.PROP_LCA_PONDERATION);
	}

	/**
	 * <p>code.</p>
	 *
	 * @param lcaNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link java.lang.String} object
	 */
	private String code(NodeRef lcaNodeRef) {
		String lcaCode = (String) nodeService.getProperty(lcaNodeRef, PLMModel.PROP_LCA_CODE);
		return lcaCode != null ? lcaCode : lcaNodeRef.getId();
	}

	/**
	 * <p>Applies the scale factor of the definition, the PEF publishing its single score
	 * in millipoints where Ecobalyse publishes points.</p>
	 *
	 * @param total the weighted sum
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 * @return a {@link java.lang.Double} object
	 */
	private Double scale(double total, ScoreDefinitionItem definition) {
		Double factor = definition.getScaleFactor();
		return (factor == null) || (factor == 0d) ? total : total * factor;
	}

	/**
	 * <p>Definitions aggregating life cycle indicators.</p>
	 *
	 * @param definitions the definitions to filter
	 * @return a {@link java.util.List} object
	 */
	public List<ScoreDefinitionItem> filter(List<ScoreDefinitionItem> definitions) {
		return definitions.stream().filter(definition -> ScoreEngine.Lca.equals(definition.getScoreEngine())).toList();
	}

}
