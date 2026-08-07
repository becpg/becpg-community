package fr.becpg.repo.product.formulation.score;

import java.util.Locale;
import java.util.Optional;

import org.alfresco.service.cmr.repository.MLText;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
