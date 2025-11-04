/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.product.formulation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.product.data.CharactDetailAdditionalValue;
import fr.becpg.repo.product.data.CharactDetails;
import fr.becpg.repo.product.data.CharactDetailsValue;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.sample.StandardChocolateEclairTestProduct;
import fr.becpg.repo.web.scripts.product.CharactDetailsHelper;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

/**
 * Test for ingredient proportion columns in details view (Fix #30123)
 *
 * @author matthieu
 */
public class IngCharactDetailsFormulationIT extends AbstractFinishedProductTest {

    protected static final Log logger = LogFactory.getLog(IngCharactDetailsFormulationIT.class);

    @Autowired
    private AttributeExtractorService attributeExtractorService;

 
    /**
     * Validates that proportion percentages are displayed correctly at all levels
     * without being recalculated with qtyUsed.
     * 
     * The key requirement is that getQtyPerc() and getQtyPercWithYield() values
     * should be displayed as-is, since they already represent final proportions
     * at the finished product level.
     *
     * @throws Exception the exception
     */
    @Test
    public void testIngredientProportionMultiLevelWithYield() throws Exception {

        final NodeRef finishedProductNodeRef = inWriteTx(() -> {

            StandardChocolateEclairTestProduct testProduct = new StandardChocolateEclairTestProduct.Builder()
                    .withAlfrescoRepository(alfrescoRepository)
                    .withNodeService(nodeService)
                    .withDestFolder(getTestFolderNodeRef())
                    .withCompo(true)
                    .withLabeling(false)
                    .withIngredients(true)
                    .build();

            FinishedProductData finishedProduct = testProduct.createTestProduct();

            return finishedProduct.getNodeRef();
        });

        inWriteTx(() -> {

            productService.formulate(finishedProductNodeRef);

            FinishedProductData finishedProduct = (FinishedProductData) alfrescoRepository.findOne(finishedProductNodeRef);
            Assert.assertNotNull("Ingredient list should not be null", finishedProduct.getIngList());

            logger.info("Testing multi-level ingredient details with maxLevel=2 for sugar proportions");

            CharactDetails ingDetailsMultiLevel = productService.formulateDetails(finishedProductNodeRef, PLMModel.TYPE_INGLIST, "ingList", null, 2);

            Assert.assertNotNull("CharactDetails should not be null", ingDetailsMultiLevel);

            logger.info(CharactDetailsHelper.toJSONObject(ingDetailsMultiLevel, nodeService, attributeExtractorService).toString(3));

            // Expected values by level (we have 3 components with 2 levels each = 6 entries)
            // Level 0: [6.493506493506494, 7.272727272727273, 5.454545454545455]
            // Level 1: [41.32231404958677, 7.272727272727273, 5.454545454545455]
            Map<Integer, Double[]> expectedSugarByLevel = new HashMap<>();
            expectedSugarByLevel.put(0, new Double[]{6.49350649350649, 7.27272727272727, 5.45454545454546});
            expectedSugarByLevel.put(1, new Double[]{41.3223140495868, 7.27272727272727, 5.45454545454546});

            Map<String, Integer> foundSugarEntries = new HashMap<>();
            boolean sugarFound = false;
            int totalSugarCount = 0;

            for (Map.Entry<NodeRef, List<CharactDetailsValue>> entry : ingDetailsMultiLevel.getData().entrySet()) {
                String ingredientName = (String) nodeService.getProperty(entry.getKey(), BeCPGModel.PROP_CHARACT_NAME);

                if (!"Sucre".equals(ingredientName)) {
                    continue;
                }

                sugarFound = true;
                logger.info("Found sugar ingredient, checking levels...");

                for (CharactDetailsValue detailsValue : entry.getValue()) {
                    if (detailsValue == null) {
                        continue;
                    }
                    
                    Integer level = detailsValue.getLevel();
                    
                    // The main value is the qtyPerc (Sucre column)
                    Double qtyPercValue = detailsValue.getValue();
                    
                    logger.info("Sugar at level " + level + ": Qty%=" + (qtyPercValue != null ? qtyPercValue : "null"));

                    // Get the qtyPercWithYield from additional values
                    Double qtyPercWithYieldValue = null;
                    List<CharactDetailAdditionalValue> additionalValues = detailsValue.getAdditionalValues();
                    if (additionalValues != null) {
                        for (CharactDetailAdditionalValue additionalValue : additionalValues) {
                            if (additionalValue != null && "Qté ap. rdmt (%)".equals(additionalValue.getColumnName())) {
                                qtyPercWithYieldValue = additionalValue.getValue();
                                logger.info("  - Qté ap. rdmt (%): " + (qtyPercWithYieldValue != null ? qtyPercWithYieldValue : "null"));
                                break;
                            }
                        }
                    }
                    
                    // Validate values
                    if (qtyPercValue != null && level != null) {
                        Assert.assertTrue("Proportion value should be >= 0 for Sucre at level " + level, qtyPercValue >= 0);
                        
                        // Check if this value matches one of the expected values for this level
                        Double[] expectedValuesForLevel = expectedSugarByLevel.get(level);
                        if (expectedValuesForLevel != null) {
                            boolean found = false;
                            for (Double expectedValue : expectedValuesForLevel) {
                                if (Math.abs(qtyPercValue - expectedValue) < 0.0001) {
                                    found = true;
                                    logger.info("✓ Correct sugar proportion at L" + level + ": Qty%=" + qtyPercValue + "%, Qty with yield%=" + qtyPercWithYieldValue + "%");
                                    String key = "L" + level + "_" + totalSugarCount;
                                    foundSugarEntries.put(key, 1);
                                    totalSugarCount++;
                                    break;
                                }
                            }
                            Assert.assertTrue("Sugar proportion " + qtyPercValue + " at level " + level + " should match one of expected values", found);
                        }
                    }
                }
            }

            Assert.assertTrue("Sugar ingredient should be found in multi-level details", sugarFound);
            Assert.assertEquals("Should have found all 6 expected sugar entries (3 components × 2 levels)", 
                    6, totalSugarCount);

            logger.info("✓ All sugar proportion values validated correctly at all levels");

            return null;
        });
    }

}
