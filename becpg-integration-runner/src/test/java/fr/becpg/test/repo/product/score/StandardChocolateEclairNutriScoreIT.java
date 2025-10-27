package fr.becpg.test.repo.product.score;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.formulation.score.NutriScoreContext;
import fr.becpg.repo.product.helper.Nutrient5C2023Helper;
import fr.becpg.repo.sample.StandardChocolateEclairTestProduct;
import fr.becpg.repo.system.SystemConfigurationService;
import fr.becpg.test.PLMBaseTestCase;

public class StandardChocolateEclairNutriScoreIT extends PLMBaseTestCase {

    @Autowired
    private ProductService productService;

    @Autowired
    private SystemConfigurationService systemConfigurationService;

    @Test
    public void testNutriScoreComputationWithStandardChocolateEclair() {
        try {
            transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
                systemConfigurationService.updateConfValue("beCPG.formulation.score.nutriscore.regulatoryClass",
                        Nutrient5C2023Helper.class.getName());
                return null;
            }, false, true);

            FinishedProductData product = inWriteTx(() -> {
                StandardChocolateEclairTestProduct testProduct = new StandardChocolateEclairTestProduct.Builder()
                        .withAlfrescoRepository(alfrescoRepository)
                        .withNodeService(nodeService)
                        .withDestFolder(getTestFolderNodeRef())
                        .withCompo(true)
                        .withNuts(true)
                        .build();
                return testProduct.createTestProduct();
            });

            inWriteTx(() -> {
                FinishedProductData persistedProduct = (FinishedProductData) alfrescoRepository.findOne(product.getNodeRef());
                productService.formulate(persistedProduct);

                NutriScoreContext context = Nutrient5C2023Helper.buildNutriScoreContext(persistedProduct);
                Assert.assertNotNull("NutriScoreContext should not be null", context);

                int expectedScore = Nutrient5C2023Helper.compute5CScore(context);
                String expectedClass = Nutrient5C2023Helper.extractNutrientClass(context);

                Assert.assertNotNull("Nutrient score should have been computed", persistedProduct.getNutrientScore());
                Assert.assertEquals("Unexpected nutrient score",
                        (double) expectedScore,
                        persistedProduct.getNutrientScore(),
                        0.001d);
                Assert.assertEquals("Unexpected nutrient class", expectedClass, persistedProduct.getNutrientClass());

                return null;
            });
        } finally {
            transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
                systemConfigurationService.resetConfValue("beCPG.formulation.score.nutriscore.regulatoryClass");
                return null;
            }, false, true);
        }
    }
}
