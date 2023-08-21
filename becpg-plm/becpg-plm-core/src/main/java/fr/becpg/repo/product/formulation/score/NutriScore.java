package fr.becpg.repo.product.formulation.score;

import java.util.ArrayList;
import java.util.Locale;

import org.alfresco.service.cmr.repository.MLText;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ScorableEntity;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.helper.NutriScoreRegulatoryHelper;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.system.SystemConfigurationService;

@Service("nutriScore")
public class NutriScore implements ScoreCalculatingPlugin {

	@Autowired
	private SystemConfigurationService systemConfigurationService;
	
	@Autowired
	private NutriScoreRegulatoryHelper[] regulatoryHelpers;
	
	private static final Log logger = LogFactory.getLog(NutriScore.class);
	
	@Override
	public boolean accept(ScorableEntity productData) {
		return (productData instanceof ProductData) &&  ((BeCPGDataObject) productData).getAspects().contains(PLMModel.ASPECT_NUTRIENT_PROFILING_SCORE);
	}

	@Override
	public boolean formulateScore(ScorableEntity scorableEntity) {
		ProductData productData  = (ProductData) scorableEntity;
		
		try {
			
			NutriScoreRegulatoryHelper regulatoryHelper = retrieveRegulatoryHelper();
			
			NutriScoreContext nutriScoreContext = regulatoryHelper.buildContext(productData);
			
			if (nutriScoreContext != null) {
				double computedScore = regulatoryHelper.computeScore(nutriScoreContext);
				productData.setNutrientScore(computedScore);
				
				String extractedClass = regulatoryHelper.extractClass(nutriScoreContext);
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
			productData.getReqCtrlList().add(new ReqCtrlListDataItem(null, RequirementType.Forbidden, errorMsg, null, new ArrayList<>(), RequirementDataType.Formulation));
			if (logger.isDebugEnabled()) {
				logger.warn("Error in nutrient score formulation :" + productData.getNodeRef());
				logger.trace(e, e);
			}
			throw e;
		}
		
		return true;
	}
	
	private NutriScoreRegulatoryHelper retrieveRegulatoryHelper() {
		String regulatoryClassName = systemConfigurationService.confValue("beCPG.score.nutriscore.regulatoryCass");
		for (NutriScoreRegulatoryHelper helper : regulatoryHelpers) {
			if (helper.getClass().getName().equals(regulatoryClassName)) {
				return helper;
			}
		}
		throw new IllegalStateException("Regulatory helper class unknown: " + regulatoryClassName);
	}
	
}
