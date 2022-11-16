package fr.becpg.repo.product.formulation.score;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.NutrientProfileCategory;
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

	public static final String ENERGY_CODE = "ENER-KJO";
	public static final String SATFAT_CODE = "FASAT";
	public static final String FAT_CODE = "FAT";
	public static final String SUGAR_CODE = "SUGAR";
	public static final String SODIUM_CODE = "NA";
	public static final String NSP_CODE = "PSACNS";
	public static final String AOAC_CODE = "FIBTG";
	public static final String PROTEIN_CODE = "PRO-";
	
	public static final String FRUIT_VEGETABLE_CODE = "FRUIT_VEGETABLE";
	
	private static final String[] NUTRIENT_CODE_LIST = { ENERGY_CODE, SATFAT_CODE, FAT_CODE, SUGAR_CODE, SODIUM_CODE, NSP_CODE, AOAC_CODE, PROTEIN_CODE };
	private static final String[] PHYSICO_CODE_LIST = { FRUIT_VEGETABLE_CODE };
	
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
		
		NutriScoreContext nutriScoreContext = buildNutriScoreContext(productData);
		
		if (nutriScoreContext != null) {
			try {
				productData.setNutrientScore((double) Nutrient5C2021Helper.build5CScore(nutriScoreContext));
				productData.setNutrientClass(Nutrient5C2021Helper.buildNutrientClass(nutriScoreContext));
				productData.setNutrientDetails(nutriScoreContext.toJSON().toString());
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
	
	private NutriScoreContext buildNutriScoreContext(ProductData productData) {
		
		String nutrientProfileCategory = productData.getNutrientProfileCategory();
		
		if (nutrientProfileCategory != null && !nutrientProfileCategory.isBlank() && !NutrientProfileCategory.NonApplicable.equals(NutrientProfileCategory.valueOf(nutrientProfileCategory))) {
			NutriScoreContext nutriScoreContext = new NutriScoreContext();
			
			nutriScoreContext.setWater(productData.getAspects().contains(PLMModel.ASPECT_WATER));
			
			nutriScoreContext.setCategory(nutrientProfileCategory);

			Map<String, NodeRef> missingCharacts = new HashMap<>();
			
			for (String nutrientCode : NUTRIENT_CODE_LIST) {
				checkAndFillNutrient(productData, nutriScoreContext, nutrientCode, missingCharacts);
			}
			
			for (String physicoCode : PHYSICO_CODE_LIST) {
				checkAndFillPhysico(productData, nutriScoreContext, physicoCode, missingCharacts);
			}
			
			// check if both fiber missing, otherwise remove them
			if (!missingCharacts.containsKey(NSP_CODE) || !missingCharacts.containsKey(AOAC_CODE)) {
				if (missingCharacts.containsKey(NSP_CODE)) {
					missingCharacts.remove(NSP_CODE);
				}
				if (missingCharacts.containsKey(AOAC_CODE)) {
					missingCharacts.remove(AOAC_CODE);
				}
			}
			
			if (!missingCharacts.isEmpty()) {
				productData.getReqCtrlList().add(new ReqCtrlListDataItem(null, RequirementType.Forbidden, MLTextHelper.getI18NMessage("nutriscore.message.missingCharacts"), null, new ArrayList<>(missingCharacts.values()), RequirementDataType.Formulation));
			}
			
			return nutriScoreContext;
		}
		
		return null;
	}

	private void checkAndFillPhysico(ProductData productData, NutriScoreContext nutriScoreContext, String physicoCode, Map<String, NodeRef> missingCharacts) {
		PhysicoChemListDataItem physicoListItem = findPhysico(productData, physicoCode, missingCharacts);
		
		if (physicoListItem != null) {
			Double value = physicoListItem.getValue();
			
			if (value == null) {
				missingCharacts.put(physicoCode, physicoListItem.getPhysicoChem());
				return;
			}
			
			JSONObject nutrientPart = new JSONObject();
			
			nutrientPart.put(NutriScoreContext.VALUE, value);
			
			nutriScoreContext.getParts().put(physicoCode, nutrientPart);
		}
	}
	
	private void checkAndFillNutrient(ProductData productData, NutriScoreContext nutriScoreContext, String nutrientCode, Map<String, NodeRef> missingCharacts) {
		NutListDataItem nutListItem = findNutrient(productData, nutrientCode, missingCharacts);
		
		if (nutListItem != null) {
			Double value = nutListItem.value("EU");
			
			if (value == null) {
				missingCharacts.put(nutrientCode, nutListItem.getNut());
				return;
			}
			
			JSONObject nutrientPart = new JSONObject();
			
			nutrientPart.put(NutriScoreContext.VALUE, value);
			
			// specific case of Sodium
			if (SODIUM_CODE.equals(nutrientCode)) {
				nutrientPart.put(NutriScoreContext.VALUE, value * 1000);
			}
			
			nutriScoreContext.getParts().put(nutrientCode, nutrientPart);
		}
	}
	
	private PhysicoChemListDataItem findPhysico(ProductData productData, String physicoCode, Map<String, NodeRef> missingCharacts) {
		for (PhysicoChemListDataItem physico : productData.getPhysicoChemList()) {
			
			if (physicoCode.equals(nodeService.getProperty(physico.getPhysicoChem(), PLMModel.PROP_PHYSICO_CHEM_CODE))) {
				
				for (ReqCtrlListDataItem reqCtrl : productData.getReqCtrlList()) {
					if (RequirementType.Forbidden.equals(reqCtrl.getReqType()) && RequirementDataType.Physicochem.equals(reqCtrl.getReqDataType()) && physico.getPhysicoChem().equals(reqCtrl.getCharact())) {
						missingCharacts.put(physicoCode, reqCtrl.getCharact());
						break;
					}
				}
				
				return physico;
			}
		}
		
		return null;
	}
	
	private NutListDataItem findNutrient(ProductData productData, String nutrientCode, Map<String, NodeRef> missingCharacts) {
		for (NutListDataItem nutList : productData.getNutList()) {
			
			NutDataItem nut = (NutDataItem) alfrescoRepository.findOne(nutList.getNut());
			
			if (nutrientCode.equals(nut.getNutCode())) {
				
				for (ReqCtrlListDataItem reqCtrl : productData.getReqCtrlList()) {
					if (RequirementType.Forbidden.equals(reqCtrl.getReqType()) && RequirementDataType.Nutrient.equals(reqCtrl.getReqDataType()) && nutList.getNut().equals(reqCtrl.getCharact())) {
						missingCharacts.put(nutrientCode, reqCtrl.getCharact());
						break;
					}
				}
				
				return nutList;
			}
		}
		
		return null;
	}
	
}
