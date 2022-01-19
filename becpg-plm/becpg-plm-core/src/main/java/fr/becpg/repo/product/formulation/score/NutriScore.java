package fr.becpg.repo.product.formulation.score;

import java.util.ArrayList;
import java.util.Locale;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeService;
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
import fr.becpg.repo.product.data.productList.NutDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.helper.Nutrient5C2021Helper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.model.BeCPGDataObject;

@Service("nutriScore")
public class NutriScore implements ScoreCalculatingPlugin {

	private static final Log logger = LogFactory.getLog(NutriScore.class);

	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	@Override
	public boolean accept(ScorableEntity productData) {
		return (productData instanceof ProductData) &&  ((BeCPGDataObject) productData).getAspects().contains(PLMModel.ASPECT_NUTRIENT_PROFILING_SCORE);
	}

	@Override
	public boolean formulateScore(ScorableEntity scorableEntity) {
		ProductData productData  = (ProductData) scorableEntity;
		
		String nutrientProfileCategory = productData.getNutrientProfileCategory();
		
		if (nutrientProfileCategory != null && !nutrientProfileCategory.isBlank()) {
			
			try {
				
				NutriScoreContext nutriScoreContext = new NutriScoreContext();
				
				nutriScoreContext.setCategory(nutrientProfileCategory);

				for (NutListDataItem nutList : productData.getNutList()) {
					
					NutDataItem nut = (NutDataItem) alfrescoRepository.findOne(nutList.getNut());
					
					if ("ENER-KJO".equals(nut.getNutCode())) {
						nutriScoreContext.setEnergy(new NutriScoreFrame(nutList.value("EU")));
					} else if ("FASAT".equals(nut.getNutCode())) {
						nutriScoreContext.setSatFat(new NutriScoreFrame(nutList.value("EU")));
					} else if ("FAT".equals(nut.getNutCode())) {
						nutriScoreContext.setTotalFat(new NutriScoreFrame(nutList.value("EU")));
					} else if ("SUGAR".equals(nut.getNutCode())) {
						nutriScoreContext.setTotalSugar(new NutriScoreFrame(nutList.value("EU")));
					} else if ("NA".equals(nut.getNutCode())) {
						nutriScoreContext.setSodium(new NutriScoreFrame(nutList.value("EU") == null ? null : nutList.value("EU") * 1000));
					} else if ("PSACNS".equals(nut.getNutCode())) {
						nutriScoreContext.setNspFibre(new NutriScoreFrame(nutList.value("EU")));
					} else if ("FIBTG".equals(nut.getNutCode())) {
						nutriScoreContext.setAoacFibre(new NutriScoreFrame(nutList.value("EU")));
					} else if ("PRO-".equals(nut.getNutCode())) {
						nutriScoreContext.setProtein(new NutriScoreFrame(nutList.value("EU")));
					}
				}
				
				for (PhysicoChemListDataItem physico : productData.getPhysicoChemList()) {
					if ("FRUIT_VEGETABLE".equals(nodeService.getProperty(physico.getPhysicoChem(), PLMModel.PROP_PHYSICO_CHEM_CODE))) {
						nutriScoreContext.setPercFruitsAndVetgs(new NutriScoreFrame(physico.getValue()));
						break;
					}
				}
				
				productData.setNutrientScore((double) Nutrient5C2021Helper.build5CScore(nutriScoreContext));
				productData.setNutrientClass(Nutrient5C2021Helper.buildNutrientClass(nutriScoreContext));
				productData.setNutrientDetails(nutriScoreContext.buildNutrientDetails());
				
			} catch (Exception e) {
				MLText errorMsg = MLTextHelper.getI18NMessage("message.formulate.formula.incorrect.nutrientProfile", e.getLocalizedMessage());
				
				productData.setNutrientClass(MLTextHelper.getClosestValue(errorMsg, Locale.getDefault()));
				
				productData.getReqCtrlList().add(new ReqCtrlListDataItem(null, RequirementType.Forbidden, errorMsg, null, new ArrayList<>(),
						RequirementDataType.Formulation));
				
				if (logger.isDebugEnabled()) {
					logger.warn("Error in nutrient score formulation :" + productData.getNodeRef());
					logger.trace(e, e);
				}
				
				throw e;
			}
		} else {
			productData.setNutrientScore(null);
			productData.setNutrientClass(null);
			productData.setNutrientDetails(null);
		}
		
		return true;
	}
	
}
