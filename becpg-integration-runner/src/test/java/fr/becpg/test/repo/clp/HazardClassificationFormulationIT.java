package fr.becpg.test.repo.clp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.GHSModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.HazardClassificationListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.formulation.cpl.HazardClassificationFormulaContext;
import fr.becpg.test.PLMBaseTestCase;
import fr.becpg.test.repo.StandardSoapTestProduct;

public class HazardClassificationFormulationIT extends PLMBaseTestCase{
	
	@Autowired
	FormulationService<ProductData> formulationService;

	@Test
	public void testFormulateHazardClassification() {
		ProductData soapTestProduct = inWriteTx(() -> {
			
			return new StandardSoapTestProduct.Builder()
				    .withAlfrescoRepository(alfrescoRepository)
				    .withNodeService(nodeService)
				    .withDestFolder(getTestFolderNodeRef())
				    .build().createTestProduct();
		});
		
		inWriteTx(() -> {
	        // Perform formulation
            formulationService.formulate(soapTestProduct);
            
            // Verify hazard classifications
            Assert.assertNotNull("Hazard classification list should not be null", soapTestProduct.getHcList());
            Assert.assertFalse("Hazard classification list should not be empty", soapTestProduct.getHcList().isEmpty());
            
            // Verify specific hazard statements from the soap ingredients are present
            Map<String, Boolean> expectedHazards = new HashMap<>();
            expectedHazards.put("H314", false); // From Sodium Hydroxide
            expectedHazards.put("H290", false); // From Sodium Hydroxide
            expectedHazards.put("H319", false); // From Sodium Carbonate and Sodium Chloride
            expectedHazards.put("H315", false); // From Oleic Acid
            expectedHazards.put("H317", false); // From Linoleic Acid and Essential Oils
            expectedHazards.put("H226", false); // From Eucalyptus and Tea Tree Oil
            expectedHazards.put("H302", false); // From Tea Tree Oil
            expectedHazards.put("H412", false); // From Lavender Oil
            
            for (HazardClassificationListDataItem hc : soapTestProduct.getHcList()) {
                String hazardCode = (String) nodeService.getProperty(hc.getHazardStatement(), GHSModel.PROP_HAZARD_CODE);
                if (expectedHazards.containsKey(hazardCode)) {
                    expectedHazards.put(hazardCode, true);
                }
            }
            
            // Verify all expected hazards were found
            for (Map.Entry<String, Boolean> entry : expectedHazards.entrySet()) {
                Assert.assertTrue("Hazard statement " + entry.getKey() + " should be present", entry.getValue());
            }
            
            // Verify physical properties were considered
            boolean hasPhysicoChemProperties = soapTestProduct.getPhysicoChemList().stream()
                .anyMatch(p -> {
                    String code = (String) nodeService.getProperty(p.getPhysicoChem(), PLMModel.PROP_PHYSICO_CHEM_CODE);
                    return HazardClassificationFormulaContext.BOILING_POINT.equals(code) ||
                           HazardClassificationFormulaContext.FLASH_POINT.equals(code) ||
                           HazardClassificationFormulaContext.HYDROCARBON_PERC.equals(code);
                });
            
            Assert.assertTrue("Physical-chemical properties should be present", hasPhysicoChemProperties);
            
            // Verify requirement controls
            List<ReqCtrlListDataItem> reqControls = soapTestProduct.getReqCtrlList();
            Assert.assertNotNull("Requirement control list should not be null", reqControls);
            
            // Verify no forbidden requirements were generated
            boolean hasForbidden = reqControls.stream()
                .anyMatch(req -> RequirementType.Forbidden.equals(req.getReqType()));
            Assert.assertFalse("Should not have any forbidden requirements", hasForbidden);
            
            return "SUCCESS";
        });
    

	}
	
}
