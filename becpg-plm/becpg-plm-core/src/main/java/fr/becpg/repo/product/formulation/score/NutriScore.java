package fr.becpg.repo.product.formulation.score;

import java.util.Locale;

import org.alfresco.service.cmr.repository.MLText;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ScorableEntity;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.helper.NutrientRegulatoryHelper;
import fr.becpg.repo.repository.model.BeCPGDataObject;

@Service("nutriScore")
public class NutriScore implements ScoreCalculatingPlugin {

	private static final Log logger = LogFactory.getLog(NutriScore.class);

	@Override
	public boolean accept(ScorableEntity productData) {
		return (productData instanceof ProductData)
				&& ((BeCPGDataObject) productData).getAspects().contains(PLMModel.ASPECT_NUTRIENT_PROFILING_SCORE);
	}

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
			productData.getReqCtrlList().add(ReqCtrlListDataItem.forbidden().withMessage(errorMsg)
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
