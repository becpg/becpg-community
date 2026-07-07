package fr.becpg.test.repo.clp;

import java.util.ArrayList;
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
import fr.becpg.repo.product.data.productList.HazardClassificationListDataItem;
import fr.becpg.repo.product.formulation.clp.HazardClassificationFormulaContext;
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.repo.regulatory.RequirementType;
import fr.becpg.repo.sample.CharactTestHelper;
import fr.becpg.repo.sample.SampleProductBuilder;
import fr.becpg.repo.sample.StandardSoapTestProduct;
import fr.becpg.test.PLMBaseTestCase;

public class HazardClassificationFormulationIT extends PLMBaseTestCase {

	@Autowired
	FormulationService<ProductData> formulationService;

	@Override
	public void setUp() throws Exception {
		super.setUp();

		inWriteTx(() -> {
			// Arrays of hazard statements and pictograms
			String[] hazardStatements = { "H314", "H310", "H330", "H225", "H315", "H224", "H413", "H412", "H318", "H317", "H410", "H302", "H226",
					"H412", "H290", "H319", "EUH208", "H304", "H300", "H312" };

			String[] pictograms = { "GHS02", "GHS05", "GHS07", "GHS09", "GHS06", "GHS08" };

			// Create hazard statements
			for (String hazard : hazardStatements) {
				CharactTestHelper.getOrCreateH(nodeService, hazard);
			}

			// Create pictograms
			for (String picto : pictograms) {
				CharactTestHelper.getOrCreatePicto(nodeService, picto);
			}

			return "SUCCESS";
		});
	}

	@Test
	public void testFormulateHazardClassification() {
		ProductData soapTestProduct = inWriteTx(() -> new StandardSoapTestProduct.Builder().withAlfrescoRepository(alfrescoRepository)
				.withNodeService(nodeService).withDestFolder(getTestFolderNodeRef()).withSpecification(true).build().createTestProduct());

		inWriteTx(() -> {
			
			soapTestProduct.setHcList(new ArrayList<>());
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
			//expectedHazards.put("H302", false); // From Tea Tree Oil
			//expectedHazards.put("H412", false); // From Lavender Oil

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
			boolean hasPhysicoChemProperties = soapTestProduct.getPhysicoChemList().stream().anyMatch(p -> {
				String code = (String) nodeService.getProperty(p.getPhysicoChem(), PLMModel.PROP_PHYSICO_CHEM_CODE);
				return HazardClassificationFormulaContext.BOILING_POINT.equals(code) || HazardClassificationFormulaContext.FLASH_POINT.equals(code)
						|| HazardClassificationFormulaContext.HYDROCARBON_PERC.equals(code);
			});

			Assert.assertTrue("Physical-chemical properties should be present", hasPhysicoChemProperties);

			// Verify requirement controls
			List<RequirementListDataItem> reqControls = soapTestProduct.getReqCtrlList();
			Assert.assertNotNull("Requirement control list should not be null", reqControls);

			// Verify no forbidden requirements were generated
			boolean hasForbidden = reqControls.stream().anyMatch(req -> RequirementType.Forbidden.equals(req.getReqType()));
			Assert.assertTrue("Should have forbidden requirements", hasForbidden);
			// Track which requirements we've found
			int foundRequirements = 0;

			for (RequirementListDataItem requirement : reqControls) {

				// Verify each requirement
				switch (requirement.getReqMessage()) {
				case StandardSoapTestProduct.H226_FORBIDDEN, StandardSoapTestProduct.H290_DANGER_FORBIDDEN, 
				StandardSoapTestProduct.DANGER_FORBIDDEN, StandardSoapTestProduct.GHS07_FORBIDDEN:
					foundRequirements++;
					break;

				default:
					break;
				}
			}

			// Verify we found all expected requirements
			assertEquals("Should have found 3 specified requirements", 3, foundRequirements);

			return "SUCCESS";
		});

	}

	// Test physical hazard classifications
	@Test
	public void testFlammableLiquidClassification() {
		assertHasHazard(inWriteTx(() -> {
			ProductData product = createTestProduct((builder, p) -> {
				// Set physical properties for flammable liquid testing
				((StandardSoapTestProduct) builder).addPhysicoChemProperty(p, "Flash Point", HazardClassificationFormulaContext.FLASH_POINT, 21.0);
				((StandardSoapTestProduct) builder).addPhysicoChemProperty(p, "Boiling Point", HazardClassificationFormulaContext.BOILING_POINT,
						78.0);
			});

			return formulationService.formulate(product);

			// Should be classified as H225 (Flam. Liq. 2) since FP < 23 && BP > 35
		}), "H225", "GHS02", "Danger");
	}

	// Test health hazard classifications
	@Test
	public void testCorrosiveClassification() {
		assertHasHazard(inWriteTx(() -> {
			ProductData product = createTestProduct((builder, p) -> {
				// Set physical properties for flammable liquid testing
				((StandardSoapTestProduct) builder).addIngredient(p, "Corrosive A", 6.0, "H314", null, null, null); // >5% H314
			});
			return formulationService.formulate(product);
			// Should be classified as H314 (Skin Corr. 1) since H314 conc >= 5%
		}), "H314", "GHS05", "Danger");
	}

	@Test
	public void testSkinIrritantClassification() {
		assertHasHazard(inWriteTx(() -> {
			ProductData product = createTestProduct((builder, p) -> {
				((StandardSoapTestProduct) builder).addIngredient(p, "Irritant A", 3.0, "H314", null, null, null); // H314 1-5%
			});
			return formulationService.formulate(product);

			// Should be classified as H315 (Skin Irrit. 2) since 1% ≤ H314 < 5%
		}), "H315", "GHS07", "Warning");
	}

	@Test
	public void testSkinSensitizationClassification() {
		assertHasHazard(inWriteTx(() -> {
			ProductData product = createTestProduct((builder, p) -> {
				((StandardSoapTestProduct) builder).addIngredient(p, "Sensitizer A", 1.0, "H317", null, null, true); // Super sensitizing
			});
			return formulationService.formulate(product);

			// Should be classified as H317 since contains super sensitizing ingredient ≥ 0.01%

		}), "H317", "GHS07", "Warning");
	}

	// Test environmental hazard classifications
	@Test
	public void testAquaticToxicityClassification() {
		assertHasHazard(inWriteTx(() -> {
			ProductData product = createTestProduct((builder, p) -> {
				((StandardSoapTestProduct) builder).addIngredient(p, "Aquatic A", 30.0, "H410", null, 1.0, null); // With M-factor
			});
			return formulationService.formulate(product);

			// Should be classified as H410 since M*concentration ≥ 25%
		}), "H410", "GHS09", "Warning");
	}

	// Test acute toxicity classifications
	@Test
	public void testAcuteToxicityClassification() {

		assertHasHazard(inWriteTx(() -> {
			ProductData product = createTestProduct((builder, p) -> {
				((StandardSoapTestProduct) builder).addIngredient(p, "Toxic A", 50.0, "H302", 500.0, null, null); // Acute oral toxicity
			});
			// Should be classified as H302 based on ATE calculation

			return formulationService.formulate(product);
		}), "H302", "GHS07", "Warning");
	}

	@Test
	public void testSubstanceThresholdExcludesLowQuantities() {
		// 9.5% + 0.6% = 10.1% but the 0.6% substance is below the >=1% inclusion threshold: sum = 9.5 < 10
		assertNoHazard(inWriteTx(() -> {
			ProductData product = createTestProduct((builder, p) -> {
				((StandardSoapTestProduct) builder).addIngredient(p, "Eye Irritant Major", 9.5, "Eye Irrit. 2:H319", null, null, null);
				((StandardSoapTestProduct) builder).addIngredient(p, "Eye Irritant Trace", 0.6, "Eye Irrit. 2:H319", null, null, null);
			});
			return formulationService.formulate(product);
		}), "H319");

		// 9.5% + 1% = 10.5 >= 10, the 0.5% substance stays excluded from sum and detail
		ProductData product = inWriteTx(() -> {
			ProductData p = createTestProduct((builder, pr) -> {
				((StandardSoapTestProduct) builder).addIngredient(pr, "Eye Irritant Major Bis", 9.5, "Eye Irrit. 2:H319", null, null, null);
				((StandardSoapTestProduct) builder).addIngredient(pr, "Eye Irritant Minor Bis", 1.0, "Eye Irrit. 2:H319", null, null, null);
				((StandardSoapTestProduct) builder).addIngredient(pr, "Eye Irritant Trace Bis", 0.5, "Eye Irrit. 2:H319", null, null, null);
			});
			return formulationService.formulate(p);
		});

		assertHasHazard(product, "H319", "GHS07", "Warning");

		for (HazardClassificationListDataItem hc : product.getHcList()) {
			if ("H319".equals(nodeService.getProperty(hc.getHazardStatement(), GHSModel.PROP_HAZARD_CODE)) && (hc.getDetails() != null)) {
				Assert.assertTrue("Detail should contain included substance", hc.getDetails().contains("Eye Irritant Major Bis"));
				Assert.assertFalse("Detail should not contain excluded substance", hc.getDetails().contains("Eye Irritant Trace Bis"));
			}
		}
	}

	@Test
	public void testSubstanceBothH314AndH318CountedOnce() {
		// The substance carries both H314 and H318: it must be counted once (2.5%),
		// so H318 (>= 3) does not apply, H319 (1 <= x < 3) and H315 (1 <= H314 < 5) do
		ProductData product = inWriteTx(() -> {
			ProductData p = createTestProduct((builder, pr) -> {
				((StandardSoapTestProduct) builder).addIngredient(pr, "Corrosive Eye Dam Substance", 2.5,
						"Skin Corr. 1A:H314,Eye Dam. 1:H318", null, null, null);
			});
			return formulationService.formulate(p);
		});

		assertNoHazard(product, "H318");
		assertHasHazard(product, "H319", "GHS07", "Warning");
		assertHasHazard(product, "H315", "GHS07", "Warning");
	}

	@Test
	public void testAcuteToxicityExclusions() {
		// "Above ATE Bound" is excluded (oral toxicity > 2000) and "Toxic Trace" is below the
		// 1% inclusion threshold of the H302 rule: etaVo = 100 / (90/1900) ≈ 2111 > 2000
		ProductData product = inWriteTx(() -> {
			ProductData p = createTestProduct((builder, pr) -> {
				((StandardSoapTestProduct) builder).addIngredient(pr, "Moderate Oral Tox", 90.0, null, 1900.0, null, null);
				((StandardSoapTestProduct) builder).addIngredient(pr, "Above ATE Bound", 10.0, null, 2500.0, null, null);
				((StandardSoapTestProduct) builder).addIngredient(pr, "Toxic Trace", 0.5, null, 100.0, null, null);
				((StandardSoapTestProduct) builder).addIngredient(pr, "Eye Irritant Control", 15.0, "Eye Irrit. 2:H319", null, null, null);
			});
			return formulationService.formulate(p);
		});

		assertHasHazard(product, "H319", "GHS07", "Warning");
		assertNoHazard(product, "H300");
		assertNoHazard(product, "H301");
		assertNoHazard(product, "H302");
	}

	private ProductData createTestProduct(SampleProductBuilder.ProductBuilder builder) {
		StandardSoapTestProduct testProduct = new StandardSoapTestProduct.Builder().withAlfrescoRepository(alfrescoRepository)
				.withNodeService(nodeService).withDestFolder(getTestFolderNodeRef()).withCompo(false).withPhysico(false).build();

		ProductData product = testProduct.createTestProduct();

		if (builder != null) {
			builder.build(testProduct, product);
		}

		product.setHcList(new ArrayList<>());

		alfrescoRepository.save(product);

		return product;
	}

	private void assertHasHazard(ProductData product, String hazardCode, String pictogramCode, String signalWord) {
		boolean found = false;
		for (HazardClassificationListDataItem hc : product.getHcList()) {
			String currentHazardCode = (String) nodeService.getProperty(hc.getHazardStatement(), GHSModel.PROP_HAZARD_CODE);
			String currentPictogramCode = null;
			if (hc.getPictogram() != null) {
				currentPictogramCode = (String) nodeService.getProperty(hc.getPictogram(), GHSModel.PROP_PICTOGRAM_CODE);
			}

			if (hazardCode.equals(currentHazardCode) && pictogramCode.equals(currentPictogramCode) && signalWord.equals(hc.getSignalWord())) {
				found = true;
				break;
			}
		}
		Assert.assertTrue("Expected hazard " + hazardCode + " with pictogram " + pictogramCode + " and signal word " + signalWord + " not found",
				found);
	}

	private ProductData assertNoHazard(ProductData product, String hazardCode) {
		for (HazardClassificationListDataItem hc : product.getHcList()) {
			String currentHazardCode = (String) nodeService.getProperty(hc.getHazardStatement(), GHSModel.PROP_HAZARD_CODE);
			Assert.assertNotEquals("Unexpected hazard " + hazardCode + " found", hazardCode, currentHazardCode);
		}
		return product;
	}

}
