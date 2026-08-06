/*
 *
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.ClientData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.SupplierData;
import fr.becpg.repo.product.data.productList.LCAListDataItem;
import fr.becpg.repo.regulatory.RequirementDataType;
import fr.becpg.repo.score.ScoreContext;
import fr.becpg.repo.score.ScoreDefinitionService;
import fr.becpg.repo.score.ScorePart;
import fr.becpg.repo.score.ScoreResultWriter;
import fr.becpg.repo.score.data.ScoreDefCoeffListDataItem;
import fr.becpg.repo.score.data.ScoreDefinitionItem;

/**
 * <p>LCACalculatingFormulationHandler class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class LCACalculatingFormulationHandler extends AbstractCostCalculatingFormulationHandler<LCAListDataItem> {

	/** Constant <code>MESSAGE_FORMULATE_LCA_LIST_ERROR="message.formulate.lcaList.error"</code> */
	private static final String MESSAGE_FORMULATE_LCA_LIST_ERROR = "message.formulate.lcaList.error";

	/** Constant <code>DEFAULT_SCALE_FACTOR=10d</code> */
	private static final double DEFAULT_SCALE_FACTOR = 10d;

	private ScoreDefinitionService scoreDefinitionService;

	private ScoreResultWriter scoreResultWriter;

	/**
	 * <p>Setter for the field <code>scoreDefinitionService</code>.</p>
	 *
	 * @param scoreDefinitionService a {@link fr.becpg.repo.score.ScoreDefinitionService} object
	 */
	public void setScoreDefinitionService(ScoreDefinitionService scoreDefinitionService) {
		this.scoreDefinitionService = scoreDefinitionService;
	}

	/**
	 * <p>Setter for the field <code>scoreResultWriter</code>.</p>
	 *
	 * @param scoreResultWriter a {@link fr.becpg.repo.score.ScoreResultWriter} object
	 */
	public void setScoreResultWriter(ScoreResultWriter scoreResultWriter) {
		this.scoreResultWriter = scoreResultWriter;
	}

	/** {@inheritDoc} */
	@Override
	protected void afterProcess(ProductData formulatedProduct) {
		if (!shouldCalculateScore(formulatedProduct)) {
			return;
		}

		Optional<ScoreDefinitionItem> definition = findScoreDefinition(formulatedProduct);

		ScoreContext context = buildScoreContext(formulatedProduct, definition);

		formulatedProduct.setLcaScore(context.getValue());

		publishScore(formulatedProduct, definition, context);
	}

	/**
	 * Score definition matching the method of the product, when the score framework holds
	 * one. Its coefficients then take precedence over the ones held by the LCA characts,
	 * which is what lets two aggregation methods coexist on the same indicators.
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @return a {@link java.util.Optional} object
	 */
	private Optional<ScoreDefinitionItem> findScoreDefinition(ProductData formulatedProduct) {
		if ((scoreDefinitionService == null) || (formulatedProduct.getLcaScoreMethod() == null)) {
			return Optional.empty();
		}
		return scoreDefinitionService.findByCode(formulatedProduct.getLcaScoreMethod(), null);
	}

	/**
	 * <p>buildScoreContext.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param definition a {@link java.util.Optional} object
	 * @return a {@link fr.becpg.repo.score.ScoreContext} object
	 */
	private ScoreContext buildScoreContext(ProductData formulatedProduct, Optional<ScoreDefinitionItem> definition) {
		ScoreContext context = new ScoreContext();

		definition.ifPresent(scoreDefinition -> {
			context.setCode(scoreDefinition.getCode());
			context.setVersion(scoreDefinition.getVersion());
			context.setUnit(scoreDefinition.getUnit());
			context.setScale(scoreDefinition.getScoreScale().name());
		});

		if (formulatedProduct.getLcaList() == null) {
			return context;
		}

		double scaleFactor = extractScaleFactor(definition);
		Double singleScore = null;

		for (LCAListDataItem lcaItem : formulatedProduct.getLcaList()) {
			Double contribution = computeContribution(lcaItem, definition, scaleFactor, context);
			if (contribution != null) {
				singleScore = (singleScore == null ? 0d : singleScore) + contribution;
			}
		}

		context.setValue(singleScore);
		context.computeShares();

		return context;
	}

	/**
	 * Computes what one indicator adds to the single score, and records it as a part of the
	 * breakdown.
	 *
	 * @param lcaItem a {@link fr.becpg.repo.product.data.productList.LCAListDataItem} object
	 * @param definition a {@link java.util.Optional} object
	 * @param scaleFactor the factor turning the weighted sum into the unit of the method
	 * @param context a {@link fr.becpg.repo.score.ScoreContext} object
	 * @return the contribution, null when the indicator misses a value or a coefficient
	 */
	private Double computeContribution(LCAListDataItem lcaItem, Optional<ScoreDefinitionItem> definition, double scaleFactor, ScoreContext context) {
		Double normalizationFactor = extractNormalization(lcaItem, definition);
		Double ponderationFactor = extractPonderation(lcaItem, definition);

		if ((lcaItem.getValue() == null) || (normalizationFactor == null) || (ponderationFactor == null) || (normalizationFactor == 0d)) {
			return null;
		}

		double contribution = ((lcaItem.getValue() / normalizationFactor) * ponderationFactor) * scaleFactor;

		String label = (String) nodeService.getProperty(lcaItem.getLca(), PLMModel.PROP_LCA_CODE);

		context.getParts().add(new ScorePart(lcaItem.getLca().getId()).withLabel(label).withValue(lcaItem.getValue(), lcaItem.getUnit())
				.withCoefficients(normalizationFactor, ponderationFactor).withContribution(contribution));

		return contribution;
	}

	/**
	 * <p>extractNormalization.</p>
	 *
	 * @param lcaItem a {@link fr.becpg.repo.product.data.productList.LCAListDataItem} object
	 * @param definition a {@link java.util.Optional} object
	 * @return a {@link java.lang.Double} object
	 */
	private Double extractNormalization(LCAListDataItem lcaItem, Optional<ScoreDefinitionItem> definition) {
		Optional<ScoreDefCoeffListDataItem> coefficient = findCoefficient(lcaItem, definition);
		if (coefficient.isPresent() && (coefficient.get().getNormalization() != null)) {
			return coefficient.get().getNormalization();
		}
		return (Double) nodeService.getProperty(lcaItem.getLca(), PLMModel.PROP_LCA_NORMALIZATION);
	}

	/**
	 * <p>extractPonderation.</p>
	 *
	 * @param lcaItem a {@link fr.becpg.repo.product.data.productList.LCAListDataItem} object
	 * @param definition a {@link java.util.Optional} object
	 * @return a {@link java.lang.Double} object
	 */
	private Double extractPonderation(LCAListDataItem lcaItem, Optional<ScoreDefinitionItem> definition) {
		Optional<ScoreDefCoeffListDataItem> coefficient = findCoefficient(lcaItem, definition);
		if (coefficient.isPresent() && (coefficient.get().getPonderation() != null)) {
			return coefficient.get().getPonderation();
		}
		return (Double) nodeService.getProperty(lcaItem.getLca(), PLMModel.PROP_LCA_PONDERATION);
	}

	/**
	 * <p>findCoefficient.</p>
	 *
	 * @param lcaItem a {@link fr.becpg.repo.product.data.productList.LCAListDataItem} object
	 * @param definition a {@link java.util.Optional} object
	 * @return a {@link java.util.Optional} object
	 */
	private Optional<ScoreDefCoeffListDataItem> findCoefficient(LCAListDataItem lcaItem, Optional<ScoreDefinitionItem> definition) {
		if (definition.isEmpty()) {
			return Optional.empty();
		}
		return scoreDefinitionService.findCoefficient(definition.get(), lcaItem.getLca());
	}

	/**
	 * The historical factor of ten stays the default, so a repository without score
	 * definition keeps the score it had.
	 *
	 * @param definition a {@link java.util.Optional} object
	 * @return a double
	 */
	private double extractScaleFactor(Optional<ScoreDefinitionItem> definition) {
		if (definition.isPresent() && (definition.get().getScaleFactor() != null)) {
			return definition.get().getScaleFactor();
		}
		return DEFAULT_SCALE_FACTOR;
	}

	/**
	 * <p>publishScore.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param definition a {@link java.util.Optional} object
	 * @param context a {@link fr.becpg.repo.score.ScoreContext} object
	 */
	private void publishScore(ProductData formulatedProduct, Optional<ScoreDefinitionItem> definition, ScoreContext context) {
		if (definition.isEmpty() || (scoreResultWriter == null) || (context.getValue() == null)) {
			return;
		}
		scoreResultWriter.write(formulatedProduct, context);
	}

	/**
	 * <p>shouldCalculateScore.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @return a boolean
	 */
	private boolean shouldCalculateScore(ProductData formulatedProduct) {
		return formulatedProduct.getLcaScoreMethod() == null || "Formulation".equals(formulatedProduct.getLcaScoreMethod());
	}

	/** {@inheritDoc} */
	@Override
	protected List<LCAListDataItem> getDataListVisited(ClientData client) {
		return client.getLcaList();
	}

	/** {@inheritDoc} */
	@Override
	protected List<LCAListDataItem> getDataListVisited(SupplierData supplier) {
		return supplier.getLcaList();
	}

	/** {@inheritDoc} */
	@Override
	protected Class<LCAListDataItem> getInstanceClass() {
		return LCAListDataItem.class;
	}
	
	/** {@inheritDoc} */
	@Override
	protected LCAListDataItem newSimpleListDataItem(NodeRef charactNodeRef) {
		LCAListDataItem lcaListDataItem = new LCAListDataItem();
		lcaListDataItem.setCharactNodeRef(charactNodeRef);
		return lcaListDataItem;
	}

	/** {@inheritDoc} */
	@Override
	protected boolean accept(ProductData formulatedProduct) {
		return !(formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL) || (formulatedProduct instanceof ProductSpecificationData)
				|| ((formulatedProduct.getCostList() == null) && !alfrescoRepository.hasDataList(formulatedProduct, PLMModel.TYPE_LCALIST)));

	}

	/** {@inheritDoc} */
	@Override
	protected List<LCAListDataItem> getDataListVisited(ProductData partProduct) {
		return partProduct.getLcaList();
	}

	/** {@inheritDoc} */
	@Override
	protected RequirementDataType getRequirementDataType() {
		return RequirementDataType.Lca;
	}

	/** {@inheritDoc} */
	@Override
	protected void setDataListVisited(ProductData formulatedProduct) {
		formulatedProduct.setLcaList(new ArrayList<>());
	}

	/** {@inheritDoc} */
	@Override
	protected QName getCostFormulaPropName() {
		return PLMModel.PROP_LCA_FORMULA;
	}

	/** {@inheritDoc} */
	@Override
	protected QName getCostFixedPropName() {
		return PLMModel.PROP_LCAFIXED;
	}

	/** {@inheritDoc} */
	@Override
	protected QName getCostUnitPropName() {
		return PLMModel.PROP_LCAUNIT;
	}
	/** {@inheritDoc} */
	@Override
	protected String getFormulationErrorMessage() {
		return MESSAGE_FORMULATE_LCA_LIST_ERROR;
	}
}
