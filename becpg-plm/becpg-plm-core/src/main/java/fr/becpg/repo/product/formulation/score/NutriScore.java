package fr.becpg.repo.product.formulation.score;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.alfresco.service.cmr.repository.MLText;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ScorableEntity;
import fr.becpg.repo.product.helper.NutrientRegulatoryHelper;
import fr.becpg.repo.regulatory.RequirementDataType;
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.score.ScoreContext;
import fr.becpg.repo.score.ScoreResultWriter;
import fr.becpg.repo.score.ScoredEntity;

/**
 * <p>NutriScore class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("nutriScore")
public class NutriScore implements ScoreCalculatingPlugin {

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(NutriScore.class);

	private final ScoreResultWriter scoreResultWriter;

	/**
	 * <p>Constructor for NutriScore.</p>
	 *
	 * @param scoreResultWriter a {@link fr.becpg.repo.score.ScoreResultWriter} object
	 */
	@Autowired
	public NutriScore(ScoreResultWriter scoreResultWriter) {
		this.scoreResultWriter = scoreResultWriter;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accept(ScorableEntity productData) {
		return (productData instanceof ProductData)
				&& ((BeCPGDataObject) productData).getAspects().contains(PLMModel.ASPECT_NUTRIENT_PROFILING_SCORE);
	}

	/** {@inheritDoc} */
	@Override
	public String getCode() {
		return NutriScoreContext.SCORE_CODE;
	}

	/**
	 * {@inheritDoc}
	 *
	 * The version is carried by the product through {@code bcpg:nutrientProfileVersion},
	 * so this plugin serves every version of the code.
	 */
	@Override
	public String getVersion() {
		return ANY_VERSION;
	}

	/** {@inheritDoc} */
	@Override
	public Optional<ScoreContext> getScoreContext(ScorableEntity scorableEntity) {
		ProductData productData = (ProductData) scorableEntity;
		String details = productData.getNutrientDetails();
		if ((details == null) || details.isBlank()) {
			return Optional.empty();
		}

		ScoreContext context = NutriScoreContext.parse(details).toScoreContext();
		// the persisted breakdown does not carry the version, without it the score would be
		// published against whichever definition of the code comes first
		context.setVersion(NutrientRegulatoryHelper.resolveVersion(productData));

		return Optional.of(context);
	}

	/**
	 * {@inheritDoc}
	 *
	 * The version carried by {@code bcpg:nutrientProfileVersion} feeds the historical
	 * properties, the nutrient list and the technical sheet. Every other version the product
	 * lists in its score list is computed on top, so 2017 and 2023 can be compared side by side.
	 */
	@Override
	public List<ScoreContext> getScoreContexts(ScorableEntity scorableEntity) {
		List<ScoreContext> contexts = new ArrayList<>();
		getScoreContext(scorableEntity).ifPresent(contexts::add);

		if (!(scorableEntity instanceof ScoredEntity scoredEntity) || contexts.isEmpty()) {
			return contexts;
		}

		ProductData productData = (ProductData) scorableEntity;
		String appliedVersion = contexts.get(0).getVersion();

		for (String version : scoreResultWriter.listedVersions(scoredEntity, getCode())) {
			if (!version.equals(appliedVersion)) {
				computeOtherVersion(productData, version).ifPresent(contexts::add);
			}
		}

		return contexts;
	}

	/**
	 * Computes a version other than the one applied to the product.
	 *
	 * <p>The missing characteristics are already reported by the version applied: the
	 * requirements raised again while building this one are dropped.</p>
	 *
	 * @param productData a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param version the version to compute
	 * @return a {@link java.util.Optional} object
	 */
	private Optional<ScoreContext> computeOtherVersion(ProductData productData, String version) {
		List<RequirementListDataItem> requirements = productData.getReqCtrlList();
		int requirementCount = requirements != null ? requirements.size() : 0;

		try {
			return NutrientRegulatoryHelper.computeContext(productData, version).map(nutriScoreContext -> {
				ScoreContext context = nutriScoreContext.toScoreContext();
				context.setVersion(version);
				return context;
			});
		} catch (RuntimeException e) {
			logger.warn("Cannot compute the Nutri-Score " + version + " of " + productData.getNodeRef() + ": " + e.getMessage());
			if (logger.isDebugEnabled()) {
				logger.debug(e, e);
			}
			return Optional.empty();
		} finally {
			while ((requirements != null) && (requirements.size() > requirementCount)) {
				requirements.remove(requirements.size() - 1);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean formulateScore(ScorableEntity scorableEntity) {
		ProductData productData = (ProductData) scorableEntity;

		try {

			NutriScoreContext nutriScoreContext = NutrientRegulatoryHelper.buildContext(productData);

			if (nutriScoreContext != null) {
				double computedScore = NutrientRegulatoryHelper.computeScore(nutriScoreContext);
				productData.setNutrientScore(computedScore);

				String extractedClass = NutrientRegulatoryHelper.extractClass(nutriScoreContext);
				productData.setNutrientClass(extractedClass);

				productData.setNutrientDetails(nutriScoreContext.toJSON().toString());
			} else {
				productData.setNutrientScore(null);
				productData.setNutrientClass(null);
				productData.setNutrientDetails(null);
			}
		} catch (Exception e) {
			MLText errorMsg = MLTextHelper.getI18NMessage("message.formulate.formula.incorrect.nutrientProfile", e.getLocalizedMessage());
			productData.setNutrientClass(MLTextHelper.getClosestValue(errorMsg, Locale.getDefault()));
			productData.getReqCtrlList().add(RequirementListDataItem.forbidden().withMessage(errorMsg)
					.ofDataType(RequirementDataType.Formulation));
			if (logger.isDebugEnabled()) {
				logger.warn("Error in nutrient score formulation :" + productData.getNodeRef());
				logger.trace(e, e);
			}
			throw e;
		}

		return true;
	}

}
