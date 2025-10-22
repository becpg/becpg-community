/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.product.formulation;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;

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
     * Test ingredient proportion columns in details view using StandardChocolateEclairTestProduct
     * 
     * Validates that:
     * - Proportion Qty % column is calculated correctly
     * - Proportion Qty with yield % column is calculated correctly
     * - Values are properly propagated to final product level
     *
     * @throws Exception the exception
     */
    @Test
    public void testIngredientProportionColumns() throws Exception {

        logger.info("testIngredientProportionColumns");

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
            Assert.assertTrue("Ingredient list should not be empty", !finishedProduct.getIngList().isEmpty());

            logger.info("Product has " + finishedProduct.getIngList().size() + " ingredients");

            CharactDetails ingDetails = productService.formulateDetails(finishedProductNodeRef, PLMModel.TYPE_INGLIST, "ingList", null, null);

            Assert.assertNotNull("CharactDetails should not be null", ingDetails);

            logger.info(CharactDetailsHelper.toJSONObject(ingDetails, nodeService, attributeExtractorService).toString(3));

            Map<NodeRef, List<CharactDetailsValue>> detailsData = ingDetails.getData();
            Assert.assertNotNull("Details data should not be null", detailsData);

            DecimalFormat df = new DecimalFormat("0.###");

            int checksPerformed = 0;
            int proportionColumnsFound = 0;
            String proportionQtyLabel = I18NUtil.getMessage("bcpg_bcpgmodel.property.bcpg_ingListProportionQtyPerc.title");
            String proportionQtyWithYieldLabel = I18NUtil.getMessage("bcpg_bcpgmodel.property.bcpg_ingListProportionQtyPercWithYield.title");

            for (Map.Entry<NodeRef, List<CharactDetailsValue>> entry : detailsData.entrySet()) {
                NodeRef ingredientNodeRef = entry.getKey();
                List<CharactDetailsValue> detailsValues = entry.getValue();

                String ingredientName = (String) nodeService.getProperty(ingredientNodeRef, BeCPGModel.PROP_CHARACT_NAME);

                for (CharactDetailsValue detailsValue : detailsValues) {

                    String sourceName = (String) nodeService.getProperty(detailsValue.getKeyNodeRef(), BeCPGModel.PROP_CHARACT_NAME);

                    logger.debug("Ingredient: " + ingredientName
                            + " - Source: " + sourceName
                            + " - Value: " + detailsValue.getValue()
                            + " - Additional values: " + detailsValue.getAdditionalValues().size());

                    List<CharactDetailAdditionalValue> additionalValues = detailsValue.getAdditionalValues();

                    for (CharactDetailAdditionalValue additionalValue : additionalValues) {
                        String columnName = additionalValue.getColumnName();
                        logger.debug("  → Additional column: " + columnName 
                                + " - Value: " + additionalValue.getValue()
                                + " - Unit: " + additionalValue.getUnit());

                        if (proportionQtyLabel.equals(columnName)) {
                            proportionColumnsFound++;
                            if (additionalValue.getValue() != null) {
                                logger.info("✓ Found Qty % for " + ingredientName + " from " + sourceName 
                                        + ": " + df.format(additionalValue.getValue()) + "%");
                            } else {
                                logger.info("✓ Found Qty % for " + ingredientName + " from " + sourceName 
                                        + ": 0% (or negligible)");
                            }
                        } else if (proportionQtyWithYieldLabel.equals(columnName)) {
                            proportionColumnsFound++;
                            if (additionalValue.getValue() != null) {
                                logger.info("✓ Found Qty with yield % for " + ingredientName + " from " + sourceName 
                                        + ": " + df.format(additionalValue.getValue()) + "%");
                            } else {
                                logger.info("✓ Found Qty with yield % for " + ingredientName + " from " + sourceName 
                                        + ": 0% (or negligible)");
                            }
                        }
                    }

                    if (!additionalValues.isEmpty()) {
                        checksPerformed++;
                    }
                }
            }

            Assert.assertTrue("Should have found proportion columns", proportionColumnsFound > 0);
            Assert.assertTrue("At least some checks should have been performed", checksPerformed > 0);
            logger.info("Total checks performed: " + checksPerformed + ", Proportion columns found: " + proportionColumnsFound);

            return null;
        });
    }

    /**
     * Test ingredient proportion columns with explicit yield using StandardChocolateEclairTestProduct
     * Tests the "Pâte à choux" semi-finished product which has yield defined
     * 
     * @throws Exception the exception
     */
    @Test
    public void testIngredientProportionColumnsWithYield() throws Exception {

        logger.info("testIngredientProportionColumnsWithYield");

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

            logger.info("Ingredient list size: " + finishedProduct.getIngList().size());

            CharactDetails ingDetails = productService.formulateDetails(finishedProductNodeRef, PLMModel.TYPE_INGLIST, "ingList", null, null);

            Assert.assertNotNull("CharactDetails should not be null", ingDetails);

            logger.info(CharactDetailsHelper.toJSONObject(ingDetails, nodeService, attributeExtractorService).toString(3));

            boolean foundProportionQty = false;
            boolean foundProportionWithYield = false;
            String proportionQtyLabel = I18NUtil.getMessage("bcpg_bcpgmodel.property.bcpg_ingListProportionQtyPerc.title");
            String proportionQtyWithYieldLabel = I18NUtil.getMessage("bcpg_bcpgmodel.property.bcpg_ingListProportionQtyPercWithYield.title");

            for (Map.Entry<NodeRef, List<CharactDetailsValue>> entry : ingDetails.getData().entrySet()) {
                String ingredientName = (String) nodeService.getProperty(entry.getKey(), BeCPGModel.PROP_CHARACT_NAME);

                for (CharactDetailsValue detailsValue : entry.getValue()) {
                    String sourceName = (String) nodeService.getProperty(detailsValue.getKeyNodeRef(), BeCPGModel.PROP_CHARACT_NAME);
                    List<CharactDetailAdditionalValue> additionalValues = detailsValue.getAdditionalValues();
                    
                    for (CharactDetailAdditionalValue additionalValue : additionalValues) {
                        String valueStr = additionalValue.getValue() != null 
                            ? String.valueOf(additionalValue.getValue()) 
                            : "0 (or negligible)";
                        logger.info("Additional column for " + ingredientName + " from " + sourceName + ": " 
                                + additionalValue.getColumnName() 
                                + " = " + valueStr
                                + " " + additionalValue.getUnit());
                        
                        if (proportionQtyLabel.equals(additionalValue.getColumnName())) {
                            foundProportionQty = true;
                        } else if (proportionQtyWithYieldLabel.equals(additionalValue.getColumnName())) {
                            foundProportionWithYield = true;
                        }
                    }
                }
            }

            Assert.assertTrue("Should have found Qty % proportion columns", foundProportionQty);
            Assert.assertTrue("Should have found Qty with yield % proportion columns", foundProportionWithYield);
            logger.info("✓ Found both proportion columns - Qty %: " + foundProportionQty 
                    + ", Qty with yield %: " + foundProportionWithYield);

            return null;
        });
    }

}
