/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.product.formulation;

import java.util.ArrayList;
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
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
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

    @Override
    public void setUp() throws Exception {
        super.setUp();
        initParts();
    }

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

    /**
     * Validates that the "Qté ap. rdmt (%)" column is present even when the root
     * product has no yield of its own, the yield being carried by a semi-finished
     * component (Fix #30123, last KO point).
     *
     * The detail decomposition must stay consistent with the ingList computed by
     * IngsCalculatingFormulationHandler: the sum of the per-component values of the
     * yield column must match the root ingList qtyPercWithYield.
     *
     * @throws Exception the exception
     */
    @Test
    public void testYieldColumnWithoutRootYield() throws Exception {

        final NodeRef semiFinishedNodeRef = inWriteTx(() -> {
            SemiFinishedProductData semiFinished = new SemiFinishedProductData();
            semiFinished.setName("SF rendement 90 - #30123");
            semiFinished.setLegalName("Legal SF rendement 90");
            semiFinished.setUnit(ProductUnit.kg);
            semiFinished.setQty(0.9d);
            semiFinished.setDensity(1d);
            List<CompoListDataItem> compoList = new ArrayList<>();
            compoList.add(CompoListDataItem.build().withQtyUsed(0.5d).withUnit(ProductUnit.kg)
                    .withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
            compoList.add(CompoListDataItem.build().withQtyUsed(0.5d).withUnit(ProductUnit.kg)
                    .withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial2NodeRef));
            semiFinished.getCompoListView().setCompoList(compoList);
            return alfrescoRepository.create(getTestFolderNodeRef(), semiFinished).getNodeRef();
        });

        final NodeRef finishedProductNodeRef = inWriteTx(() -> {
            productService.formulate(semiFinishedNodeRef);

            FinishedProductData finishedProduct = new FinishedProductData();
            finishedProduct.setName("PF sans rendement - #30123");
            finishedProduct.setLegalName("Legal PF sans rendement");
            finishedProduct.setUnit(ProductUnit.kg);
            List<CompoListDataItem> compoList = new ArrayList<>();
            compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg)
                    .withDeclarationType(DeclarationType.Declare).withProduct(semiFinishedNodeRef));
            compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg)
                    .withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial3NodeRef));
            finishedProduct.getCompoListView().setCompoList(compoList);
            return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
        });

        inWriteTx(() -> {
            productService.formulate(finishedProductNodeRef);

            FinishedProductData finishedProduct = (FinishedProductData) alfrescoRepository.findOne(finishedProductNodeRef);
            Assert.assertNull("Root product should have no yield", finishedProduct.getYield());

            Double ingListQtyPercWithYield = finishedProduct.getIngList().stream()
                    .filter(item -> ing1.equals(item.getIng())).findFirst()
                    .map(item -> item.getQtyPercWithYield()).orElse(null);
            Assert.assertNotNull("Root ingList should carry qtyPercWithYield", ingListQtyPercWithYield);
            Assert.assertEquals(16.2037d, ingListQtyPercWithYield, 0.001d);

            CharactDetails ingDetails = productService.formulateDetails(finishedProductNodeRef, PLMModel.TYPE_INGLIST, "ingList", null, 2);

            logger.info(CharactDetailsHelper.toJSONObject(ingDetails, nodeService, attributeExtractorService).toString(3));

            List<CharactDetailsValue> ing1Values = ingDetails.getData().get(ing1);
            Assert.assertNotNull("ing1 should be present in details", ing1Values);

            boolean semiFinishedLineChecked = false;
            boolean rawMaterialLineChecked = false;
            double levelZeroWithYieldSum = 0d;

            for (CharactDetailsValue detailsValue : ing1Values) {
                Double withYieldValue = getAdditionalValue(detailsValue, "bcpg:ingListQtyPercWithYield");
                Assert.assertNotNull("Yield column should be present at level " + detailsValue.getLevel(), withYieldValue);
                Assert.assertNull("Secondary yield column should be absent",
                        getAdditionalValue(detailsValue, "bcpg:ingListQtyPercWithSecondaryYield"));

                if (Integer.valueOf(0).equals(detailsValue.getLevel())) {
                    levelZeroWithYieldSum += withYieldValue;
                    if (semiFinishedNodeRef.equals(detailsValue.getKeyNodeRef())) {
                        Assert.assertEquals("Quantity (%) should stay before yield", 14.5833d, detailsValue.getValue(), 0.001d);
                        Assert.assertEquals("Qty with yield (%) should carry the SF yield", 16.2037d, withYieldValue, 0.001d);
                        semiFinishedLineChecked = true;
                    }
                } else if (rawMaterial1NodeRef.equals(detailsValue.getKeyNodeRef())) {
                    Assert.assertEquals(9.2593d, withYieldValue, 0.001d);
                    rawMaterialLineChecked = true;
                }
            }

            Assert.assertTrue("Semi-finished line should be checked at level 0", semiFinishedLineChecked);
            Assert.assertTrue("Raw material line should be checked at level 1", rawMaterialLineChecked);
            Assert.assertEquals("Level 0 yield column should sum up to the root ingList value", ingListQtyPercWithYield,
                    levelZeroWithYieldSum, 0.001d);

            return null;
        });
    }

    /**
     * Returns the value of an additional column of a detail line.
     *
     * @param detailsValue the detail line
     * @param columnKey the additional column key
     * @return the column value, or null when the column is absent
     */
    private Double getAdditionalValue(CharactDetailsValue detailsValue, String columnKey) {
        for (CharactDetailAdditionalValue additionalValue : detailsValue.getAdditionalValues()) {
            if ((additionalValue != null) && columnKey.equals(additionalValue.getColumnKey())) {
                return additionalValue.getValue();
            }
        }
        return null;
    }

}
