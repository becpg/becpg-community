package fr.becpg.test.repo.regulatory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.activity.EntityActivityService;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.json.JsonHelper;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.ing.IngItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.IngRegulatoryListDataItem;
import fr.becpg.repo.product.data.productList.RegulatoryListDataItem;
import fr.becpg.repo.regulatory.*;
import fr.becpg.repo.regulatory.becpg.regulatory.BecpgRegulatoryAuthenticationService;
import fr.becpg.repo.regulatory.becpg.regulatory.BecpgRegulatoryClient;
import fr.becpg.repo.regulatory.becpg.regulatory.BecpgRegulatoryService;
import fr.becpg.repo.regulatory.becpg.regulatory.RegulatoryComplianceViewService;
import fr.becpg.repo.regulatory.becpg.regulatory.ProductDataEntityJsonService;
import fr.becpg.repo.regulatory.decernis.RegulatoryContext;
import fr.becpg.repo.sample.CosmeticRequirementTestProduct;
import fr.becpg.repo.sample.CosmeticRequirementTestProduct.CosmeticScenario;
import fr.becpg.repo.sample.IfraTestProduct;
import fr.becpg.repo.sample.IfraTestProduct.IfraScenario;
import fr.becpg.repo.sample.StandardBodyMilkTestProduct;
import fr.becpg.repo.sample.StandardSoapTestProduct;
import fr.becpg.repo.system.SystemConfigurationService;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;
import fr.becpg.util.MutexFactory;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static fr.becpg.repo.sample.CosmeticRequirementTestProduct.ACETYLATED_VETIVER;
import static fr.becpg.repo.sample.CosmeticRequirementTestProduct.BENZYL_SALICYLATE;
import static fr.becpg.repo.sample.CosmeticRequirementTestProduct.CI_44045;
import static fr.becpg.repo.sample.CosmeticRequirementTestProduct.HEXYL_SALICYLATE;
import static fr.becpg.repo.sample.CosmeticRequirementTestProduct.LEGAL_STATUS_OTC_DRUG;
import static fr.becpg.repo.sample.CosmeticRequirementTestProduct.PRESERVATIVE_BLEND;
import static fr.becpg.repo.sample.CosmeticRequirementTestProduct.PROPYLPARABEN;
import static fr.becpg.repo.sample.CosmeticRequirementTestProduct.SAFROLE;
import static fr.becpg.repo.sample.CosmeticRequirementTestProduct.SALICYLIC_ACID;
import static fr.becpg.repo.sample.CosmeticRequirementTestProduct.SODIUM_HYDROXIDE;
import static fr.becpg.repo.sample.CosmeticRequirementTestProduct.UNKNOWN_INGREDIENT_TYPE;
import static fr.becpg.repo.sample.CosmeticRequirementTestProduct.UNKNOWN_USAGE_CODE;
import static fr.becpg.repo.sample.CosmeticRequirementTestProduct.WATER;
import static fr.becpg.repo.sample.CosmeticRequirementTestProduct.ZINC_GLUCONATE;
import static fr.becpg.repo.sample.IfraTestProduct.*;

public class BecpgRegulatoryServiceIT extends AbstractFinishedProductTest {

    private static final String LIVE_REGULATORY_REQUIRED = "Requires access to running becpg-regulatory instance, to run manually";

    private static final String REGULATORY_ENABLED = "beCPG.regulatory.enabled";

    private static final String REGULATORY_CHAIN_ID = "regulatory";

    private static final String USAGE_CODE_SEPARATOR = " - ";

    private static final String MAXIMUM_FORMAT = "maximum: %s%%";

    private static final String NOT_ENFORCED_FORMAT = "IFRA standard %s is not enforced by this service";

    private static final String PROHIBITED_MESSAGE = "Ingredient usage is prohibited under this jurisdiction.";

    private static final String CAS_CONFLICT_MESSAGE = "CAS conflict.";

    private static final String REGULATORY_ITEM_SEPARATOR = " ;; ";

    private static final String RESTRICTED_LEVEL = "RESTRICTED";

    private static final String RESTRICTION_MAXIMUM_FORMAT = "max: %s,";

    private static final String RESTRICTION_FORMAT = "%s :: " + RESTRICTION_MAXIMUM_FORMAT;

    private static final String PERFUMES_LABEL = "Perfumes";

    private static final String HYDROALCOHOLIC_PERFUMES_LABEL = "Hydroalcoholic perfumes";

    private static final String UNIDENTIFIED_MESSAGE = "Ingredient could not be identified by name, regulatory code, CAS number, or EC number.";

    private static final String NOT_AUTHORIZED_MESSAGE = "Ingredient is not explicitly authorized for usage";

    private static final String NOT_LISTED_MESSAGE = "Ingredient not identified; it should be explicitly listed";

    private static final String TYPE_NOT_SPECIFIED_MESSAGE = "Ingredient type not specified.";

    private static final String TYPE_NOT_FOUND_FORMAT = "Ingredient type %s not found.";

    private static final String USAGE_NOT_FOUND_FORMAT = "Regulatory use %s not found.";

    private static final String PH_MAXIMUM_RESTRICTION = "pHMax: 11.0";

    private static final String LEGAL_STATUS_NOT_CARRIED_FORMAT = "No regulation source carries the legal status %s.";

    @Autowired
    private NodeService nodeService;

    @Autowired
    private SystemConfigurationService systemConfigurationService;

    @Autowired
    private FormulationService<FormulatedEntity> formulationService;

    @Autowired
    private EntityActivityService entityActivityService;

    @Autowired
    private MutexFactory mutexFactory;

    @Autowired
    RemoteEntityService remoteEntityService;

    @Autowired
    ProductDataEntityJsonService productDataEntityJsonService;

    private BecpgRegulatoryService regulatoryService;

    @Autowired
    private BecpgRegulatoryAuthenticationService becpgRegulatoryAuthenticationService;

    @Autowired
    private BecpgRegulatoryClient becpgRegulatoryClient;

    @Autowired
    private RegulatoryComplianceViewService regulatoryComplianceViewService;

    private MockWebServer mockWebServer;

    private String mockServerUrl;

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    @Override
    public void setUp() throws Exception {

        regulatoryService = new BecpgRegulatoryService(
                nodeService,
                alfrescoRepository,
                formulationService,
                batchQueueService,
                policyBehaviourFilter,
                entityActivityService,
                mutexFactory,
                systemConfigurationService,
                productDataEntityJsonService,
                becpgRegulatoryAuthenticationService,
                becpgRegulatoryClient
        );

        mockWebServer = new MockWebServer();
        mockWebServer.start();
        mockServerUrl = "http://" + mockWebServer.getHostName() + ":" + mockWebServer.getPort();

        super.setUp();
        initParts();
    }

    private NodeRef createTestFinishedProduct() {
        return inWriteTx(() -> {
            StandardSoapTestProduct soap = new StandardBodyMilkTestProduct.Builder().withAlfrescoRepository(alfrescoRepository)
                    .withNodeService(nodeService)
                    .withDestFolder(getTestFolderNodeRef())
                    .build();
            ProductData testProduct = soap.createTestProduct();
            return testProduct.getNodeRef();
        });
    }

    private NodeRef getCountry(String name, Serializable code) {
        return inWriteTx(() -> {
            Map<QName, Serializable> properties = new HashMap<>();
            properties.put(ContentModel.PROP_NAME, name);
            properties.put(BeCPGModel.PROP_CHARACT_NAME, name);
            properties.put(PLMModel.PROP_REGULATORY_CODE, code);
            properties.put(PLMModel.PROP_GEO_ORIGIN_ISOCODE, code);
            return nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
                    PLMModel.TYPE_GEO_ORIGIN, properties).getChildRef();
        });
    }

    private NodeRef getUsage(String name, String code) {
        return inWriteTx(() -> {
            Map<QName, Serializable> properties = new HashMap<>();
            properties.put(BeCPGModel.PROP_CHARACT_NAME, name);
            properties.put(PLMModel.PROP_REGULATORY_CODE, code);
            properties.put(PLMModel.PROP_REGULATORY_MODULE, "COSMETICS");
            return nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
                    PLMModel.TYPE_REGULATORY_USAGE, properties).getChildRef();
        });
    }

    private NodeRef createFinishedProduct(final String finishedProductName) {
        return inWriteTx(() -> {
            FinishedProductData finishedProduct = new FinishedProductData();
            finishedProduct.setName(finishedProductName);
            return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
        });
    }

    /**
     * Reads a JSON mock response from the classpath.
     *
     * @param resourcePath the classpath resource path
     * @return the normalized JSON response
     */
    private String readJsonResource(String resourcePath) {
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            return JsonHelper.read(resource.getContentAsString(StandardCharsets.UTF_8)).toString();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read resource: " + resourcePath, e);
        }
    }

    /**
     * Impossible to mock, as mapping is done based on IDs that are dynamically generated on product creation
     * becpg-integration-runner/src/test/resources/beCPG/regulatory/becpg/response.json contains response example for illustrative purposes
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void becpgRegulatoryRemoteTest() {
        NodeRef finishedProductNodeRef = createTestFinishedProduct();

        inWriteTx(() -> {
            systemConfigurationService.updateConfValue("beCPG.regulatory.enabled", "true");
            return null;
        });

        inWriteTx(() -> {
            ProductData product = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
            return regulatoryService.doCheck(false, new ComplianceResult(), product);
        });

        inWriteTx(() -> {
            ProductData product = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
            RegulatoryListDataItem regulatoryElement = product.getRegulatoryList().getFirst();
            assertEquals(RegulatoryResult.PERMITTED, regulatoryElement.getRegulatoryResult());

            // set CHROMIUM qty to a tiny number - bcpg-regulatory should still invalidate product: EU Annex II
            // prohibits "Chromium; chromic acid and its salts", and CHROMIUM is a colorant that is not listed
            IngListDataItem chromiumListItem = product.getIngList().stream().filter(ing ->
                    alfrescoRepository.findOne(ing.getIng()).getName().equals("CHROMIUM")
            ).findFirst().orElse(null);
            assertNotNull(chromiumListItem);
            chromiumListItem.setQtyPerc(0.00075);
            return alfrescoRepository.save(chromiumListItem);
        });

        inWriteTx(() -> {
            ProductData product = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
            return regulatoryService.doCheck(false, new ComplianceResult(), product);
        });

        inWriteTx(() -> {
            ProductData product = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
            RegulatoryListDataItem regulatoryElement = product.getRegulatoryList().getFirst();
            assertEquals(RegulatoryResult.PROHIBITED, regulatoryElement.getRegulatoryResult());
            assertEquals(1, regulatoryElement.getLimitingIngredients().size());
            IngListDataItem offenderListItem = (IngListDataItem) alfrescoRepository.findOne(regulatoryElement.getLimitingIngredients().getFirst());
            IngItem offender = (IngItem) alfrescoRepository.findOne(offenderListItem.getIng());
            assertEquals("CHROMIUM", offender.getCharactName());
            return null;
        });
    }

    /**
     * Category 4 refuses the two ingredients above their limit, and lets through the ingredient sitting exactly at
     * its limit as well as the ingredients with no IFRA line.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testIfraFineFragranceRefusesIngredientsAboveTheirLimit() {
        NodeRef productNodeRef = createCheckedIfraProduct(IfraScenario.FINE_FRAGRANCE);

        inReadTx(() -> {
            RegulatoryRequirements requirements = new RegulatoryRequirements(productNodeRef);
            requirements.assertExceeds(CINNAMIC_ALDEHYDE, "0.25");
            requirements.assertExceeds(AHMI, "2");
            requirements.assertNotForbidden(CITRAL, AMYL_CINNAMIC_ALDEHYDE, BENZYL_SALICYLATE, ALCOHOL_DENAT, AQUA, PARFUM);
            requirements.assertLimitingIngredients(CINNAMIC_ALDEHYDE, AHMI);
            return null;
        });
    }

    /**
     * One regulatory line declared for categories 5A and 5D: the same recipe passes in 5A and is refused in 5D,
     * each result carrying the usage it belongs to.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testIfraBodyLotionLimitsFollowTheCategory() {
        NodeRef productNodeRef = createCheckedIfraProduct(IfraScenario.BODY_LOTION_TWO_CATEGORIES);

        inReadTx(() -> {
            RegulatoryRequirements requirements = new RegulatoryRequirements(productNodeRef);
            requirements.forUsage(USAGE_CODE_CATEGORY_5A).assertNotForbidden(CINNAMIC_ALDEHYDE, CITRAL, AMYL_CINNAMIC_ALDEHYDE,
                    BENZYL_SALICYLATE, AQUA, GLYCERIN, CELLULOSE_GUM);
            RegulatoryRequirements babyCream = requirements.forUsage(USAGE_CODE_CATEGORY_5D);
            babyCream.assertExceeds(CINNAMIC_ALDEHYDE, "0.014");
            babyCream.assertExceeds(CITRAL, "0.051");
            babyCream.assertNotForbidden(AMYL_CINNAMIC_ALDEHYDE, BENZYL_SALICYLATE, AQUA, GLYCERIN, CELLULOSE_GUM);
            return null;
        });
    }

    /**
     * Category 9 refuses the two ingredients above their limit; AHMI, restricted in most categories, has no
     * requirement in this one and is let through at 3 %.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testIfraShampooHasNoAhmiRequirement() {
        NodeRef productNodeRef = createCheckedIfraProduct(IfraScenario.RINSE_OFF_SHAMPOO);

        inReadTx(() -> {
            RegulatoryRequirements requirements = new RegulatoryRequirements(productNodeRef);
            requirements.assertExceeds(CITRAL, "1.2");
            requirements.assertExceeds(AMYL_CINNAMIC_ALDEHYDE, "1.5");
            requirements.assertNotForbidden(AHMI, CINNAMIC_ALDEHYDE, BENZYL_SALICYLATE, AQUA, GLYCERIN, CELLULOSE_GUM);
            return null;
        });
    }

    /**
     * Category 12 refuses the three enforced prohibitions, reports the two prohibitions the service does not enforce
     * as not evaluated, naming their standard, and lets the listed ingredient through.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testIfraScentedCandleReportsEnforcedAndNotEnforcedProhibitions() {
        NodeRef productNodeRef = createCheckedIfraProduct(IfraScenario.SCENTED_CANDLE);

        inReadTx(() -> {
            RegulatoryRequirements requirements = new RegulatoryRequirements(productNodeRef);
            requirements.assertProhibited(ALLYL_ISOTHIOCYANATE, MINTLACTONE, ACETYL_DIMETHYLFURAN);
            requirements.assertNotEnforced(CYCLAMEN_ALCOHOL, "125");
            requirements.assertNotEnforced(ISOPHORONE, "154");
            requirements.assertNotForbidden(CYCLAMEN_ALCOHOL, ISOPHORONE, LIMONENE, MICROCRYSTALLINE_WAX, BEESWAX);
            return null;
        });
    }

    /**
     * Category 4 refuses as prohibited each ingredient IFRA prohibits on every category, each of them limiting the
     * market, and lets the unregulated solvent through.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testIfraCategory4RefusesProhibitedIngredients() {
        NodeRef productNodeRef = createCheckedIfraProduct(IfraScenario.PROHIBITED_IN_CATEGORY_4);

        inReadTx(() -> {
            RegulatoryRequirements requirements = new RegulatoryRequirements(productNodeRef);
            requirements.assertProhibited(QUINOLINE, MUSK_XYLENE, FURFURYL_ALCOHOL, MASSOIA_LACTONE, BOLDO_OIL);
            requirements.assertNotForbidden(ALCOHOL_DENAT);
            requirements.assertLimitingIngredients(QUINOLINE, MUSK_XYLENE, FURFURYL_ALCOHOL, MASSOIA_LACTONE, BOLDO_OIL);
            return null;
        });
    }

    /**
     * The recipe lines that reach the IFRA row are refused above their category 2 limit, and the two lines that cannot
     * be resolved are reported as such. The two hexyl base lines are identified by their regulatory code, which wins
     * over the CAS number of benzyl salicylate on the second one: both get the hexyl cinnamic aldehyde limit of 0.53 %,
     * never the benzyl salicylate limit of 0.39 %. The citral line that carries the CAS number of cinnamic aldehyde
     * reaches both records and gets both limits. The line whose CAS and EC numbers point to two records is not
     * evaluated, so it is reported but not refused.
     * <p>
     * By the standard, the amyl cinnamal lines exceed 0.53 % and the benzyl salicylate line exceeds 0.39 %. They are
     * left unasserted, as the service does not refuse them yet: the name narrows the CAS match of the amyl cinnamal line
     * down to a record with no IFRA line, the IFRA record of benzyl salicylate carries no EC number and drops out when
     * the CAS and EC matches are crossed, and the IFRA records are not linked to the INCI records the names reach.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testIfraIdentificationMatrixMatchesMissesAndFailures() {
        NodeRef productNodeRef = createCheckedIfraProduct(IfraScenario.IDENTIFICATION_MATRIX);

        inReadTx(() -> {
            RegulatoryRequirements requirements = new RegulatoryRequirements(productNodeRef);
            requirements.assertExceeds(ACCORD_17, "2");
            requirements.assertExceeds(CINNAMIC_ALDEHYDE, "0.014");
            requirements.assertExceeds(ALLYL_PHENOXYACETATE, "0.016");
            requirements.assertExceeds(PROPYLIDENEPHTHALIDE, "0.022");
            requirements.assertExceedsOnLine(requirements.line(CITRAL, CAS_GERANIAL), CITRAL, "0.032");
            NodeRef mismatchingCasLine = requirements.line(CITRAL, CAS_CINNAMIC_ALDEHYDE);
            requirements.assertExceedsOnLine(mismatchingCasLine, CITRAL + " with the CAS of " + CINNAMIC_ALDEHYDE, "0.032");
            requirements.assertExceedsOnLine(mismatchingCasLine, CITRAL + " with the CAS of " + CINNAMIC_ALDEHYDE, "0.014");
            requirements.assertExceedsOnLine(requirements.line(HEXYL_BASE, null), HEXYL_BASE + " without CAS", "0.53");
            NodeRef conflictingCasLine = requirements.line(HEXYL_BASE, CAS_BENZYL_SALICYLATE);
            requirements.assertExceedsOnLine(conflictingCasLine, HEXYL_BASE + " with the CAS of " + BENZYL_SALICYLATE, "0.53");
            requirements.assertDoesNotExceedOnLine(conflictingCasLine, HEXYL_BASE + " with the CAS of " + BENZYL_SALICYLATE, "0.39");
            requirements.assertNotForbidden(ALCOHOL_DENAT, AQUA, SENSATE_ACCORD);
            requirements.assertReported(SENSATE_ACCORD, CAS_CONFLICT_MESSAGE);
            requirements.assertReported(HOUSE_WOODY_ACCORD, UNIDENTIFIED_MESSAGE);
            return null;
        });
    }

    /**
     * Toothpaste from 3 years: the clause from 6 months to 1 year never applies, the clause above 1 year replaces the
     * clause on every cosmetic, and 0.9 % stays within 1 %.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testZincToothpasteFromThreeYearsIsCompliant() {
        assertCosmeticRequirements(CosmeticScenario.ZINC_TOOTHPASTE_FROM_3_YEARS,
                requirements -> requirements.assertCompliant(ZINC_GLUCONATE, WATER));
    }

    /**
     * Toothpaste from 6 months: the clause from 6 months to 1 year applies, and 0.9 % is above 0.72 %.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testZincToothpasteFromSixMonthsIsRefused() {
        assertCosmeticRequirements(CosmeticScenario.ZINC_TOOTHPASTE_FROM_6_MONTHS,
                requirements -> requirements.assertRefusedAbove(ZINC_GLUCONATE, "0.72"));
    }

    /**
     * Toothpaste from birth: no clause covers birth, so the youngest clause above it, from 6 months to 1 year,
     * applies, and 0.9 % is above 0.72 %.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testZincToothpasteFromBirthIsRefused() {
        assertCosmeticRequirements(CosmeticScenario.ZINC_TOOTHPASTE_FROM_BIRTH,
                requirements -> requirements.assertRefusedAbove(ZINC_GLUCONATE, "0.72"));
    }

    /**
     * Toothpaste with no target population: checked for every age, the runs from birth and from 6 months fail.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testZincToothpasteWithNoTargetPopulationIsRefused() {
        assertCosmeticRequirements(CosmeticScenario.ZINC_TOOTHPASTE_NO_TARGET_POPULATION,
                requirements -> requirements.assertRefusedAbove(ZINC_GLUCONATE, "0.72"));
    }

    /**
     * Mouth wash for adults: the mouth wash clause replaces the clause on every cosmetic, and 0.5 % is above 0.1 %.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testZincMouthWashForAdultsIsRefused() {
        assertCosmeticRequirements(CosmeticScenario.ZINC_MOUTH_WASH_ADULT,
                requirements -> requirements.assertRefusedAbove(ZINC_GLUCONATE, "0.1"));
    }

    /**
     * Mouth wash from 3 years: the mouth wash clause starts at 6 years, but its lower bound does not exclude younger
     * users, so it still applies.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testZincMouthWashFromThreeYearsIsRefused() {
        assertCosmeticRequirements(CosmeticScenario.ZINC_MOUTH_WASH_FROM_3_YEARS,
                requirements -> requirements.assertRefusedAbove(ZINC_GLUCONATE, "0.1"));
    }

    /**
     * Mouth wash for adults at 0.08 %: within the mouth wash limit of 0.1 %.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testZincMouthWashForAdultsBelowItsLimitIsCompliant() {
        assertCosmeticRequirements(CosmeticScenario.ZINC_MOUTH_WASH_ADULT_LOW_DOSE,
                requirements -> requirements.assertCompliant(ZINC_GLUCONATE, WATER));
    }

    /**
     * Perfume for adults: the clause for children below 3 does not match an adult product, the hydroalcoholic perfume
     * clause at 2 % replaces the leave-on clause of 0.3 %, and 1 % stays within it.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testHexylSalicylatePerfumeForAdultsIsCompliant() {
        assertCosmeticRequirements(CosmeticScenario.HEXYL_SALICYLATE_PERFUME_ADULT, requirements -> {
            requirements.assertCompliant(HEXYL_SALICYLATE, WATER);
            requirements.assertRestrictedBy(HEXYL_SALICYLATE, HYDROALCOHOLIC_PERFUMES_LABEL, 2);
            requirements.assertNotRestrictedTo(HEXYL_SALICYLATE, 0.1);
            requirements.assertNotRestrictedTo(HEXYL_SALICYLATE, 0.3);
        });
    }

    /**
     * Perfume with no target population: a product with no age is checked for every age, so the fragrance clause for
     * children below 3, at 0.1 % on the parent type Perfumes, applies and refuses 1 %. The adult run applies the
     * hydroalcoholic perfume clause at 2 %.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testHexylSalicylatePerfumeWithNoTargetPopulationIsRestricted() {
        assertCosmeticRequirements(CosmeticScenario.HEXYL_SALICYLATE_PERFUME_NO_TARGET_POPULATION, requirements -> {
            requirements.assertRefusedAbove(HEXYL_SALICYLATE, "0.1");
            requirements.assertRestrictedBy(HEXYL_SALICYLATE, PERFUMES_LABEL, 0.1);
            requirements.assertRestrictedBy(HEXYL_SALICYLATE, HYDROALCOHOLIC_PERFUMES_LABEL, 2);
        });
    }

    /**
     * Perfume from 3 years: the clause for children below 3 ends where the product starts, and the perfume clause
     * allows 1 %.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testHexylSalicylatePerfumeFromThreeYearsIsCompliant() {
        assertCosmeticRequirements(CosmeticScenario.HEXYL_SALICYLATE_PERFUME_FROM_3_YEARS,
                requirements -> requirements.assertCompliant(HEXYL_SALICYLATE, WATER));
    }

    /**
     * Perfume from 6 months at 0.08 %: within the clause for children below 3 as well as the perfume clause.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testHexylSalicylatePerfumeFromSixMonthsBelowEveryLimitIsCompliant() {
        assertCosmeticRequirements(CosmeticScenario.HEXYL_SALICYLATE_PERFUME_FROM_6_MONTHS_LOW_DOSE,
                requirements -> requirements.assertCompliant(HEXYL_SALICYLATE, WATER));
    }

    /**
     * Leave-on face mask: the leave-on clause applies, and 0.15 % is above 0.1 %.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testVetiverLeaveOnFaceMaskIsRefused() {
        assertCosmeticRequirements(CosmeticScenario.VETIVER_FACE_MASK_LEAVE_ON,
                requirements -> requirements.assertRefusedAbove(ACETYLATED_VETIVER, "0.1"));
    }

    /**
     * Rinse-off face mask: the rinse-off clause allows 0.2 %, the leave-on clause does not reach it.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testVetiverRinseOffFaceMaskIsCompliant() {
        assertCosmeticRequirements(CosmeticScenario.VETIVER_FACE_MASK_RINSE_OFF,
                requirements -> requirements.assertCompliant(ACETYLATED_VETIVER, WATER));
    }

    /**
     * Brief contact face mask: brief contact is matched strictly, so neither the leave-on nor the rinse-off clause
     * reaches it.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testVetiverBriefContactFaceMaskIsCompliant() {
        assertCosmeticRequirements(CosmeticScenario.VETIVER_FACE_MASK_BRIEF_CONTACT,
                requirements -> requirements.assertCompliant(ACETYLATED_VETIVER, WATER));
    }

    /**
     * Face mask with no use mode: checked for every use mode, the leave-on run fails.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testVetiverFaceMaskWithNoUseModeIsRefused() {
        assertCosmeticRequirements(CosmeticScenario.VETIVER_FACE_MASK_NO_USE_MODE,
                requirements -> requirements.assertRefusedAbove(ACETYLATED_VETIVER, "0.1"));
    }

    /**
     * Brief contact wipe for the nappy area from birth: the ban is for leave-on products only, and 0.1 % stays within
     * 0.14 %.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testPropylparabenWipeFromBirthIsCompliant() {
        assertCosmeticRequirements(CosmeticScenario.PROPYLPARABEN_WIPE_FROM_BIRTH,
                requirements -> requirements.assertCompliant(PROPYLPARABEN, WATER));
    }

    /**
     * Brief contact wipe for the nappy area from birth at 0.2 %: above the preservative limit of 0.14 %.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testPropylparabenWipeAboveItsLimitIsRefused() {
        assertCosmeticRequirements(CosmeticScenario.PROPYLPARABEN_WIPE_FROM_BIRTH_HIGH_DOSE,
                requirements -> requirements.assertRefusedAbove(PROPYLPARABEN, "0.14"));
    }

    /**
     * Wipe for the nappy area from birth with no use mode: checked for every use mode, the leave-on run hits the ban.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testPropylparabenWipeWithNoUseModeIsRefused() {
        assertCosmeticRequirements(CosmeticScenario.PROPYLPARABEN_WIPE_NO_USE_MODE,
                requirements -> requirements.assertRefusedAsProhibited(PROPYLPARABEN));
    }

    /**
     * Leave-on nappy cream from birth: the ban for children under 3, on the closer age span, replaces the limit of
     * 0.14 %.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testPropylparabenNappyCreamFromBirthIsRefused() {
        assertCosmeticRequirements(CosmeticScenario.PROPYLPARABEN_NAPPY_CREAM_FROM_BIRTH, requirements -> {
            requirements.assertRefusedAsProhibited(PROPYLPARABEN);
            requirements.assertNotRestrictedTo(PROPYLPARABEN, 0.14);
        });
    }

    /**
     * Leave-on nappy cream from 3 years: the ban ends, exclusively, at 36 months, where the product starts, and 0.1 %
     * stays within 0.14 %.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testPropylparabenNappyCreamFromThreeYearsIsCompliant() {
        assertCosmeticRequirements(CosmeticScenario.PROPYLPARABEN_NAPPY_CREAM_FROM_3_YEARS,
                requirements -> requirements.assertCompliant(PROPYLPARABEN, WATER));
    }

    /**
     * Leave-on body lotion from birth: the ban is for the nappy area only, and 0.1 % stays within 0.14 %.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testPropylparabenBodyLotionFromBirthIsCompliant() {
        assertCosmeticRequirements(CosmeticScenario.PROPYLPARABEN_BODY_LOTION_FROM_BIRTH,
                requirements -> requirements.assertCompliant(PROPYLPARABEN, WATER));
    }

    /**
     * Leave-on intimate cream for adults: the ban is for children under 3 only, and 0.1 % stays within 0.14 %.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testPropylparabenIntimateCreamForAdultsIsCompliant() {
        assertCosmeticRequirements(CosmeticScenario.PROPYLPARABEN_INTIMATE_CREAM_ADULT,
                requirements -> requirements.assertCompliant(PROPYLPARABEN, WATER));
    }

    /**
     * Face cream: the leave-on clause allows 0.1 %, and 0.08 % stays within it.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testVetiverFaceCreamIsCompliant() {
        assertCosmeticRequirements(CosmeticScenario.VETIVER_FACE_CREAM, requirements -> requirements.assertCompliant(ACETYLATED_VETIVER, WATER));
    }

    /**
     * Foundation: the make-up clause applies, the leave-on clause excluding make-up, and 0.08 % is above 0.05 %.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testVetiverFoundationIsRefused() {
        assertCosmeticRequirements(CosmeticScenario.VETIVER_FOUNDATION,
                requirements -> requirements.assertRefusedAbove(ACETYLATED_VETIVER, "0.05"));
    }

    /**
     * Deodorant: the deodorant clause applies, and 0.08 % is above 0.05 %.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testVetiverDeodorantIsRefused() {
        assertCosmeticRequirements(CosmeticScenario.VETIVER_DEODORANT,
                requirements -> requirements.assertRefusedAbove(ACETYLATED_VETIVER, "0.05"));
    }

    /**
     * Perfume: the fragrance clause allows 0.9 %, and 0.5 % stays within it.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testVetiverPerfumeIsCompliant() {
        assertCosmeticRequirements(CosmeticScenario.VETIVER_PERFUME, requirements -> requirements.assertCompliant(ACETYLATED_VETIVER, WATER));
    }

    /**
     * Perfume with no use mode: in the rinse-off run, the fragrance clause, on the closer product type, replaces the
     * rinse-off clause on every cosmetic. Compare {@link #testVetiverFaceMaskWithNoUseModeIsRefused()}.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testVetiverPerfumeWithNoUseModeIsCompliant() {
        assertCosmeticRequirements(CosmeticScenario.VETIVER_PERFUME_NO_USE_MODE,
                requirements -> requirements.assertCompliant(ACETYLATED_VETIVER, WATER));
    }

    /**
     * Leave-in hair spray for adults at 0.4 %: within the 2 % of the first line and the 0.5 % of the preservative line.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testSalicylicAcidSprayForAdultsIsCompliant() {
        assertCosmeticRequirements(CosmeticScenario.SALICYLIC_ACID_SPRAY_ADULT,
                requirements -> requirements.assertCompliant(SALICYLIC_ACID, WATER));
    }

    /**
     * Leave-in hair spray for adults at 1 %: the first line alone would allow 2 %, but precedence never crosses lines,
     * so the 0.5 % of the preservative line still applies.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testSalicylicAcidSprayAboveThePreservativeLimitIsRefused() {
        assertCosmeticRequirements(CosmeticScenario.SALICYLIC_ACID_SPRAY_ADULT_HIGH_DOSE,
                requirements -> requirements.assertRefusedAbove(SALICYLIC_ACID, "0.5"));
    }

    /**
     * Leave-in hair spray from 6 months: on the Annex III line, the ban for children under 3 on every cosmetic product
     * survives the 2 % on hair and scalp, the age span deciding; on the Annex V line, the ban for children under 3 on
     * hair conditioners replaces the 0.5 %, the product type being closer.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testSalicylicAcidSprayFromSixMonthsIsRefused() {
        assertCosmeticRequirements(CosmeticScenario.SALICYLIC_ACID_SPRAY_FROM_6_MONTHS,
                requirements -> requirements.assertRefusedAsProhibited(SALICYLIC_ACID));
    }

    /**
     * Leave-in hair spray with no target population: checked for every age, the runs from birth and from 6 months hit
     * the ban of the preservative line.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testSalicylicAcidSprayWithNoTargetPopulationIsRefused() {
        assertCosmeticRequirements(CosmeticScenario.SALICYLIC_ACID_SPRAY_NO_TARGET_POPULATION,
                requirements -> requirements.assertRefusedAsProhibited(SALICYLIC_ACID));
    }

    /**
     * Leave-in hair spray from 3 years: both bans for children under 3 end where the product starts, and 0.4 % stays
     * within 0.5 %.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testSalicylicAcidSprayFromThreeYearsIsCompliant() {
        assertCosmeticRequirements(CosmeticScenario.SALICYLIC_ACID_SPRAY_FROM_3_YEARS,
                requirements -> requirements.assertCompliant(SALICYLIC_ACID, WATER));
    }

    /**
     * Hair relaxer for professional use: the professional limit of 4.5 % applies, and 3 % stays within it.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testSodiumHydroxideRelaxerForProfessionalUseIsCompliant() {
        assertCosmeticRequirements(CosmeticScenario.SODIUM_HYDROXIDE_RELAXER_PROFESSIONAL,
                requirements -> requirements.assertCompliant(SODIUM_HYDROXIDE, WATER));
    }

    /**
     * Hair relaxer for general use: the general limit of 2 % applies, and 3 % is above it.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testSodiumHydroxideRelaxerForGeneralUseIsRefused() {
        assertCosmeticRequirements(CosmeticScenario.SODIUM_HYDROXIDE_RELAXER_GENERAL_USE,
                requirements -> requirements.assertRefusedAbove(SODIUM_HYDROXIDE, "2"));
    }

    /**
     * Hair relaxer with no professional use: checked for both, the general use run fails.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testSodiumHydroxideRelaxerWithNoProfessionalUseIsRefused() {
        assertCosmeticRequirements(CosmeticScenario.SODIUM_HYDROXIDE_RELAXER_NO_PROFESSIONAL_USE,
                requirements -> requirements.assertRefusedAbove(SODIUM_HYDROXIDE, "2"));
    }

    /**
     * Hair relaxer for general use, typed as pH adjuster: the pH clause only states a pH, so it never replaces a
     * limit, and the general use limit of 2 % still refuses 3 %.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testSodiumHydroxideRelaxerForGeneralUseTypedAsPhAdjusterIsRefused() {
        assertCosmeticRequirements(CosmeticScenario.SODIUM_HYDROXIDE_RELAXER_GENERAL_USE_PH_ADJUSTER,
                requirements -> requirements.assertRefusedAbove(SODIUM_HYDROXIDE, "2"));
    }

    /**
     * Aerosol deodorant at 0.7 %: the spray clause allows 0.91 %, and the leave-on, powder or inhalable clause does not
     * reach it.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testBenzylSalicylateAerosolDeodorantIsCompliant() {
        assertCosmeticRequirements(CosmeticScenario.BENZYL_SALICYLATE_AEROSOL_DEODORANT,
                requirements -> requirements.assertCompliant(BENZYL_SALICYLATE, WATER));
    }

    /**
     * Aerosol deodorant at 1 %: above the spray clause of 0.91 %.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testBenzylSalicylateAerosolDeodorantAboveItsLimitIsRefused() {
        assertCosmeticRequirements(CosmeticScenario.BENZYL_SALICYLATE_AEROSOL_DEODORANT_HIGH_DOSE,
                requirements -> requirements.assertRefusedAbove(BENZYL_SALICYLATE, "0.91"));
    }

    /**
     * Roll-on deodorant at 0.7 % with no exposure route: every route is checked. By the standard, the leave-on non-spray
     * clause of 0.5 % refuses a roll-on whatever its route, but the data stores it with the powder or inhalable route,
     * so only the powder and inhalable runs hit it.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testBenzylSalicylateRollOnWithNoExposureRouteIsRefused() {
        assertCosmeticRequirements(CosmeticScenario.BENZYL_SALICYLATE_ROLL_ON,
                requirements -> requirements.assertRefusedAbove(BENZYL_SALICYLATE, "0.5"));
    }

    /**
     * Roll-on deodorant at 0.4 % with no exposure route: every route run passes, the powder and inhalable runs
     * included.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testBenzylSalicylateRollOnBelowEveryLimitIsCompliant() {
        assertCosmeticRequirements(CosmeticScenario.BENZYL_SALICYLATE_ROLL_ON_LOW_DOSE,
                requirements -> requirements.assertCompliant(BENZYL_SALICYLATE, WATER));
    }

    /**
     * Perfume from 6 months at 1 %: the fragrance clause for children below 3, at 0.1 % on the parent type Perfumes,
     * applies and refuses it.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testHexylSalicylatePerfumeFromSixMonthsIsRefused() {
        assertCosmeticRequirements(CosmeticScenario.HEXYL_SALICYLATE_PERFUME_FROM_6_MONTHS, requirements -> {
            requirements.assertRefusedAbove(HEXYL_SALICYLATE, "0.1");
            requirements.assertRestrictedBy(HEXYL_SALICYLATE, PERFUMES_LABEL, 0.1);
        });
    }

    /**
     * Body lotion declared with no mucous contact: the ban on products in contact with mucous membranes does not
     * apply.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testCi44045BodyLotionWithNoMucousContactIsCompliant() {
        assertCosmeticRequirements(CosmeticScenario.CI_44045_BODY_LOTION_NO_MUCOUS_CONTACT,
                requirements -> requirements.assertCompliant(CI_44045, WATER));
    }

    /**
     * Body lotion with no mucous contact declared: checked with and without, the run with mucous contact hits the
     * ban.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testCi44045BodyLotionWithNoDeclaredMucousContactIsRefused() {
        assertCosmeticRequirements(CosmeticScenario.CI_44045_BODY_LOTION_NO_DECLARED_MUCOUS_CONTACT,
                requirements -> requirements.assertRefusedAsProhibited(CI_44045));
    }

    /**
     * Body lotion in contact with mucous membranes: the ban applies.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testCi44045BodyLotionInContactWithMucousMembranesIsRefused() {
        assertCosmeticRequirements(CosmeticScenario.CI_44045_BODY_LOTION_MUCOUS_CONTACT,
                requirements -> requirements.assertRefusedAsProhibited(CI_44045));
    }

    /**
     * Toothpaste for adults at 80 ppm: the oral hygiene clause of 50 ppm replaces the clause on every cosmetic.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testSafroleToothpasteForAdultsIsRefused() {
        assertCosmeticRequirements(CosmeticScenario.SAFROLE_TOOTHPASTE_ADULT,
                requirements -> requirements.assertRefusedAbove(SAFROLE, "0.005"));
    }

    /**
     * Toothpaste with no target population at 80 ppm: checked for every age, the runs below 18 years hit the ban on
     * toothpaste for children.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testSafroleToothpasteWithNoTargetPopulationIsProhibited() {
        assertCosmeticRequirements(CosmeticScenario.SAFROLE_TOOTHPASTE_NO_TARGET_POPULATION,
                requirements -> requirements.assertRefusedAsProhibited(SAFROLE));
    }

    /**
     * Body lotion for adults: the clause on every cosmetic applies, and the dose stays within its 100 ppm.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testSafroleBodyLotionForAdultsIsCompliant() {
        assertCosmeticRequirements(CosmeticScenario.SAFROLE_BODY_LOTION_ADULT,
                requirements -> requirements.assertCompliant(SAFROLE, WATER));
    }

    /**
     * Body lotion typed as pH adjuster: the pH clause is the only one that applies, it is recorded on the regulatory
     * item, and the check does not compare pH values.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testSodiumHydroxideBodyLotionTypedAsPhAdjusterRecordsThePhLimit() {
        assertCosmeticRequirements(CosmeticScenario.SODIUM_HYDROXIDE_BODY_LOTION_PH_ADJUSTER, requirements -> {
            requirements.assertCompliant(SODIUM_HYDROXIDE, WATER);
            requirements.assertRestrictionMentions(SODIUM_HYDROXIDE, PH_MAXIMUM_RESTRICTION);
        });
    }

    /**
     * Body lotion with no ingredient type: the clause on pH adjusters is not dropped for want of a type, it is recorded
     * on the regulatory item, and the missing type is reported.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testUntypedSodiumHydroxideBodyLotionRecordsThePhLimit() {
        assertCosmeticRequirements(CosmeticScenario.SODIUM_HYDROXIDE_BODY_LOTION_UNTYPED, requirements -> {
            requirements.assertCompliant(SODIUM_HYDROXIDE, WATER);
            requirements.assertTolerated(SODIUM_HYDROXIDE, TYPE_NOT_SPECIFIED_MESSAGE);
            requirements.assertRestrictionMentions(SODIUM_HYDROXIDE, PH_MAXIMUM_RESTRICTION);
        });
    }

    /**
     * Leave-on cream from birth for the body and the nappy area: a clause applies when it shares one zone with the
     * product, so the ban on the nappy area refuses it.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testPropylparabenCreamForBodyAndNappyAreaIsRefused() {
        assertCosmeticRequirements(CosmeticScenario.PROPYLPARABEN_BODY_AND_NAPPY_CREAM_FROM_BIRTH,
                requirements -> requirements.assertRefusedAsProhibited(PROPYLPARABEN));
    }

    /**
     * Leave-on cream from birth with no application zone: checked for every zone, the nappy area run hits the ban.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testPropylparabenCreamWithNoApplicationZoneIsRefused() {
        assertCosmeticRequirements(CosmeticScenario.PROPYLPARABEN_NAPPY_CREAM_NO_APPLICATION_ZONE,
                requirements -> requirements.assertRefusedAsProhibited(PROPYLPARABEN));
    }

    /**
     * Leave-on nappy cream from birth at 0 %: an ingredient that is not there is not refused, even when prohibited.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testPropylparabenNappyCreamAtZeroDoseIsCompliant() {
        assertCosmeticRequirements(CosmeticScenario.PROPYLPARABEN_NAPPY_CREAM_FROM_BIRTH_ZERO_DOSE,
                requirements -> requirements.assertCompliant(PROPYLPARABEN, WATER));
    }

    /**
     * Body lotion with benzyl salicylate typed as preservative: it is not listed among the authorized preservatives,
     * so the preservative whitelist refuses it.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testBenzylSalicylateTypedAsPreservativeIsRefused() {
        assertCosmeticRequirements(CosmeticScenario.BENZYL_SALICYLATE_BODY_LOTION_PRESERVATIVE, requirements -> {
            requirements.assertForbiddenWith(BENZYL_SALICYLATE, NOT_AUTHORIZED_MESSAGE);
            requirements.assertLimitingIngredients(BENZYL_SALICYLATE);
        });
    }

    /**
     * Body lotion with an unidentified trade name typed as preservative: the line is reported as unidentified, and the
     * preservative whitelist refuses it.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testUnidentifiedPreservativeIsRefused() {
        assertCosmeticRequirements(CosmeticScenario.UNIDENTIFIED_PRESERVATIVE_BODY_LOTION, requirements -> {
            requirements.assertTolerated(PRESERVATIVE_BLEND, UNIDENTIFIED_MESSAGE);
            requirements.assertForbiddenWith(PRESERVATIVE_BLEND, NOT_LISTED_MESSAGE);
        });
    }

    /**
     * Body lotion with an unidentified trade name typed as perfuming: the line is reported as unidentified, and no
     * whitelist applies to it.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testUnidentifiedPerfumingIngredientIsOnlyReported() {
        assertCosmeticRequirements(CosmeticScenario.UNIDENTIFIED_PERFUMING_BODY_LOTION, requirements -> {
            requirements.assertTolerated(PRESERVATIVE_BLEND, UNIDENTIFIED_MESSAGE);
            requirements.assertCompliant(PRESERVATIVE_BLEND, WATER);
        });
    }

    /**
     * Cleansing wipe with no ingredient type: the missing type is reported, and the preservative limit of 0.14 % still
     * refuses 0.2 %.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testPropylparabenWipeWithNoIngredientTypeIsRefused() {
        assertCosmeticRequirements(CosmeticScenario.PROPYLPARABEN_WIPE_UNTYPED_HIGH_DOSE, requirements -> {
            requirements.assertTolerated(PROPYLPARABEN, TYPE_NOT_SPECIFIED_MESSAGE);
            requirements.assertRefusedAbove(PROPYLPARABEN, "0.14");
        });
    }

    /**
     * Cleansing wipe with an ingredient type the service does not know: the unknown type is reported, and the
     * preservative limit of 0.14 % still refuses 0.2 %.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testPropylparabenWipeWithUnknownIngredientTypeIsRefused() {
        assertCosmeticRequirements(CosmeticScenario.PROPYLPARABEN_WIPE_UNKNOWN_TYPE_HIGH_DOSE, requirements -> {
            requirements.assertTolerated(PROPYLPARABEN, String.format(TYPE_NOT_FOUND_FORMAT, UNKNOWN_INGREDIENT_TYPE));
            requirements.assertRefusedAbove(PROPYLPARABEN, "0.14");
        });
    }

    /**
     * Toothpaste declared for a product type the service does not know: the product is refused for that usage. The
     * refusal carries the bare usage code rather than "country - usage", so it does not reach the market, and the
     * market verdict is left unasserted until that contract is settled.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testZincToothpasteWithUnknownUsageIsRefused() {
        assertCosmeticRequirements(CosmeticScenario.ZINC_TOOTHPASTE_UNKNOWN_USAGE,
                requirements -> requirements.assertProductRequirement(String.format(USAGE_NOT_FOUND_FORMAT, UNKNOWN_USAGE_CODE)));
    }

    /**
     * Toothpaste sold as an over-the-counter drug: no regulation source of the service carries that legal status, so
     * the product is refused on its market.
     */
    @Ignore(LIVE_REGULATORY_REQUIRED)
    @Test
    public void testZincToothpasteUnderUnsupportedLegalStatusIsRefused() {
        assertCosmeticRequirements(CosmeticScenario.ZINC_TOOTHPASTE_OTC_DRUG,
                requirements -> requirements.assertProductRefused(String.format(LEGAL_STATUS_NOT_CARRIED_FORMAT, LEGAL_STATUS_OTC_DRUG)));
    }

    /**
     * Creates the product of an IFRA scenario and checks it against the running becpg-regulatory.
     *
     * @param scenario the IFRA situation to create
     * @return the checked product
     */
    private NodeRef createCheckedIfraProduct(IfraScenario scenario) {
        return checkWithLiveRegulatory(inWriteTx(() -> new IfraTestProduct.Builder().withScenario(scenario)
                .withAlfrescoRepository(alfrescoRepository)
                .withNodeService(nodeService)
                .withDestFolder(getTestFolderNodeRef())
                .build()
                .createTestProduct()
                .getNodeRef()));
    }

    /**
     * Creates the product of a cosmetic scenario and checks it against the running becpg-regulatory.
     *
     * @param scenario the cosmetic situation to create
     * @return the checked product
     */
    private NodeRef createCheckedCosmeticProduct(CosmeticScenario scenario) {
        return checkWithLiveRegulatory(inWriteTx(() -> new CosmeticRequirementTestProduct.Builder().withScenario(scenario)
                .withAlfrescoRepository(alfrescoRepository)
                .withNodeService(nodeService)
                .withDestFolder(getTestFolderNodeRef())
                .build()
                .createTestProduct()
                .getNodeRef()));
    }

    /**
     * Creates the product of a cosmetic scenario, checks it against the running becpg-regulatory and runs the
     * assertions on the requirements the check left.
     *
     * @param scenario the cosmetic situation to create
     * @param assertions the assertions on the requirements of the checked product
     */
    private void assertCosmeticRequirements(CosmeticScenario scenario, Consumer<RegulatoryRequirements> assertions) {
        NodeRef productNodeRef = createCheckedCosmeticProduct(scenario);
        inReadTx(() -> {
            assertions.accept(new RegulatoryRequirements(productNodeRef));
            return null;
        });
    }

    /**
     * Checks a product against the running becpg-regulatory, the regulatory check being enabled for this check only.
     *
     * @param productNodeRef the product to check
     * @return the checked product
     */
    private NodeRef checkWithLiveRegulatory(NodeRef productNodeRef) {
        try {
            inWriteTx(() -> {
                systemConfigurationService.updateConfValue(REGULATORY_ENABLED, Boolean.TRUE.toString());
                return null;
            });
            inWriteTx(() -> regulatoryService.doCheck(false, new ComplianceResult(), (ProductData) alfrescoRepository.findOne(productNodeRef)));
        } finally {
            inWriteTx(() -> {
                systemConfigurationService.resetConfValue(REGULATORY_ENABLED);
                return null;
            });
        }
        return productNodeRef;
    }

    /**
     * The regulatory requirements a check left on a product, read per recipe line and, optionally, for one usage of
     * the product. A violation may be reported more than once, so the assertions look for at least one.
     */
    private final class RegulatoryRequirements {

        private final ProductData product;

        private final String usageCode;

        private RegulatoryRequirements(NodeRef productNodeRef) {
            this((ProductData) alfrescoRepository.findOne(productNodeRef), null);
        }

        private RegulatoryRequirements(ProductData product, String usageCode) {
            this.product = product;
            this.usageCode = usageCode;
        }

        /**
         * @param code the regulatory code of the usage
         * @return the requirements of this product reported for that usage only
         */
        private RegulatoryRequirements forUsage(String code) {
            return new RegulatoryRequirements(product, code);
        }

        private void assertExceeds(String ingredientName, String maximum) {
            assertExceedsOnLine(line(ingredientName), ingredientName, maximum);
        }

        /**
         * @param line the recipe line refused above its limit
         * @param label the name of the line in the failure message
         * @param maximum the limit, as the message writes it
         */
        private void assertExceedsOnLine(NodeRef line, String label, String maximum) {
            assertTrue(label + " should exceed its limit of " + maximum + " %" + usageSuffix(),
                    hasRequirement(line, RequirementType.Forbidden, String.format(MAXIMUM_FORMAT, maximum)));
        }

        /**
         * @param line the recipe line
         * @param label the name of the line in the failure message
         * @param maximum a limit, as the message writes it, that the line may not be refused above
         */
        private void assertDoesNotExceedOnLine(NodeRef line, String label, String maximum) {
            assertFalse(label + " should not be refused above " + maximum + " %" + usageSuffix(),
                    hasRequirement(line, RequirementType.Forbidden, String.format(MAXIMUM_FORMAT, maximum)));
        }

        /**
         * @param ingredientName the ingredient of the refused recipe line
         * @param message a part of the English message of the refusal
         */
        private void assertForbiddenWith(String ingredientName, String message) {
            assertTrue(ingredientName + " should be refused with: " + message + usageSuffix(),
                    hasRequirement(line(ingredientName), RequirementType.Forbidden, message));
        }

        /**
         * @param ingredientName the ingredient of the reported recipe line
         * @param message a part of the English message of the warning
         */
        private void assertTolerated(String ingredientName, String message) {
            assertTrue(ingredientName + " should be reported as tolerated with: " + message + usageSuffix(),
                    hasRequirement(line(ingredientName), RequirementType.Tolerated, message));
        }

        /**
         * Asserts the check refused the product itself, whatever its recipe lines, and that the market is refused.
         *
         * @param message a part of the English message of the refusal
         */
        private void assertProductRefused(String message) {
            assertProductRequirement(message);
            RegulatoryListDataItem market = product.getRegulatoryList().getFirst();
            assertEquals(product.getName() + " should be prohibited", RegulatoryResult.PROHIBITED, market.getRegulatoryResult());
        }

        /**
         * Asserts the check refused the product itself, whatever its recipe lines, without looking at the market.
         *
         * @param message a part of the English message of the refusal
         */
        private void assertProductRequirement(String message) {
            assertTrue(product.getName() + " should be refused with: " + message, hasProductRequirement(message));
        }

        /**
         * @param message a part of the English message
         * @return whether the regulatory check left a refusal with that message, on any source
         */
        private boolean hasProductRequirement(String message) {
            for (RequirementListDataItem requirement : product.getReqCtrlList()) {
                if (REGULATORY_CHAIN_ID.equals(requirement.getFormulationChainId())
                        && RequirementType.Forbidden.equals(requirement.getReqType()) && englishMessage(requirement).contains(message)) {
                    return true;
                }
            }
            return false;
        }

        private void assertProhibited(String... ingredientNames) {
            for (String ingredientName : ingredientNames) {
                assertTrue(ingredientName + " should be prohibited" + usageSuffix(),
                        hasRequirement(line(ingredientName), RequirementType.Forbidden, PROHIBITED_MESSAGE));
            }
        }

        private void assertNotEnforced(String ingredientName, String standard) {
            assertReported(ingredientName, String.format(NOT_ENFORCED_FORMAT, standard));
        }

        private void assertReported(String ingredientName, String message) {
            assertTrue(ingredientName + " should be reported with: " + message + usageSuffix(), hasRequirement(line(ingredientName), null, message));
        }

        private void assertNotForbidden(String... ingredientNames) {
            for (String ingredientName : ingredientNames) {
                assertNotForbiddenOnLine(line(ingredientName), ingredientName);
            }
        }

        private void assertNotForbiddenOnLine(NodeRef line, String label) {
            assertFalse(label + " should not be refused" + usageSuffix(), hasRequirement(line, RequirementType.Forbidden, ""));
        }

        /**
         * @param ingredientNames the ingredients of the recipe lines, none of which may be refused
         */
        private void assertCompliant(String... ingredientNames) {
            assertNotForbidden(ingredientNames);
            RegulatoryListDataItem market = product.getRegulatoryList().getFirst();
            assertEquals(product.getName() + " should be permitted", RegulatoryResult.PERMITTED, market.getRegulatoryResult());
        }

        /**
         * @param ingredientName the ingredient refused above its limit, which limits the market
         * @param maximum the limit, as the message writes it
         */
        private void assertRefusedAbove(String ingredientName, String maximum) {
            assertExceeds(ingredientName, maximum);
            assertLimitingIngredients(ingredientName);
        }

        /**
         * @param ingredientName the ingredient refused as prohibited, which limits the market
         */
        private void assertRefusedAsProhibited(String ingredientName) {
            assertProhibited(ingredientName);
            assertLimitingIngredients(ingredientName);
        }

        /**
         * Asserts the regulatory item the check left for the ingredient is restricted, and records the clause of a
         * product type with its maximum, which proves which clause was applied.
         *
         * @param ingredientName the ingredient of the regulatory item
         * @param productType the English label of the product type the clause sits on
         * @param maximum the maximum concentration of the clause, in %
         */
        private void assertRestrictedBy(String ingredientName, String productType, double maximum) {
            assertTrue(ingredientName + " should be restricted",
                    regulatoryItemText(ingredientName, IngRegulatoryListDataItem::getResultIndicator).contains(RESTRICTED_LEVEL));
            String restriction = String.format(RESTRICTION_FORMAT, productType, maximum);
            assertTrue(ingredientName + " should be restricted by: " + restriction,
                    regulatoryItemText(ingredientName, IngRegulatoryListDataItem::getRestrictionLevels).contains(restriction));
        }

        /**
         * @param ingredientName the ingredient of the regulatory item
         * @param maximum a maximum concentration, in %, that no clause applied to the ingredient may carry
         */
        private void assertNotRestrictedTo(String ingredientName, double maximum) {
            String restriction = String.format(RESTRICTION_MAXIMUM_FORMAT, maximum);
            assertFalse(ingredientName + " should not be restricted to " + maximum + " %",
                    regulatoryItemText(ingredientName, IngRegulatoryListDataItem::getRestrictionLevels).contains(restriction));
        }

        /**
         * @param ingredientName the ingredient of the regulatory item
         * @param restriction a part of the English restriction levels that a clause applied to the ingredient records
         */
        private void assertRestrictionMentions(String ingredientName, String restriction) {
            assertTrue(ingredientName + " should be restricted by: " + restriction,
                    regulatoryItemText(ingredientName, IngRegulatoryListDataItem::getRestrictionLevels).contains(restriction));
        }

        /**
         * @param ingredientName the ingredient of the regulatory items
         * @param field the text field to read on each item
         * @return the English values of that field over the product's regulatory items for the ingredient, joined
         */
        private String regulatoryItemText(String ingredientName, Function<IngRegulatoryListDataItem, MLText> field) {
            List<String> values = new ArrayList<>();
            for (IngRegulatoryListDataItem item : product.getIngRegulatoryList()) {
                MLText value = field.apply(item);
                if (value != null && ingredientName.equals(nodeService.getProperty(item.getIng(), BeCPGModel.PROP_CHARACT_NAME))) {
                    values.add(MLTextHelper.getClosestValue(value, Locale.ENGLISH));
                }
            }
            return String.join(REGULATORY_ITEM_SEPARATOR, values);
        }

        private void assertLimitingIngredients(String... ingredientNames) {
            RegulatoryListDataItem market = product.getRegulatoryList().getFirst();
            assertEquals(RegulatoryResult.PROHIBITED, market.getRegulatoryResult());
            for (String ingredientName : ingredientNames) {
                assertTrue(ingredientName + " should limit the market", market.getLimitingIngredients().contains(line(ingredientName)));
            }
        }

        private NodeRef line(String ingredientName) {
            return findLine(ingredient -> ingredientName.equals(nodeService.getProperty(ingredient, BeCPGModel.PROP_CHARACT_NAME)));
        }

        /**
         * @param ingredientName the ingredient name of the line
         * @param cas the CAS number of the line, null for a line with none
         * @return the recipe line naming that ingredient with that CAS number
         */
        private NodeRef line(String ingredientName, String cas) {
            return findLine(ingredient -> ingredientName.equals(nodeService.getProperty(ingredient, BeCPGModel.PROP_CHARACT_NAME))
                    && Objects.equals(cas, nodeService.getProperty(ingredient, PLMModel.PROP_CAS_NUMBER)));
        }

        private NodeRef findLine(Predicate<NodeRef> ingredientFilter) {
            for (IngListDataItem ingLine : product.getIngList()) {
                if (ingredientFilter.test(ingLine.getIng())) {
                    return ingLine.getNodeRef();
                }
            }
            throw new AssertionError("No recipe line for this ingredient in " + product.getName());
        }

        /**
         * @param line the recipe line the requirement is about
         * @param type the requirement type, null for any
         * @param message a part of the English message
         * @return whether the check left such a requirement on the line
         */
        private boolean hasRequirement(NodeRef line, RequirementType type, String message) {
            for (RequirementListDataItem requirement : product.getReqCtrlList()) {
                if (isAbout(requirement, line) && (type == null || type.equals(requirement.getReqType()))
                        && englishMessage(requirement).contains(message)) {
                    return true;
                }
            }
            return false;
        }

        private boolean isAbout(RequirementListDataItem requirement, NodeRef line) {
            return REGULATORY_CHAIN_ID.equals(requirement.getFormulationChainId()) && requirement.getSources().contains(line)
                    && (usageCode == null || (requirement.getRegulatoryCode() != null
                            && requirement.getRegulatoryCode().endsWith(USAGE_CODE_SEPARATOR + usageCode)));
        }

        private String englishMessage(RequirementListDataItem requirement) {
            String message = requirement.getReqMlMessage() != null ? MLTextHelper.getClosestValue(requirement.getReqMlMessage(), Locale.ENGLISH)
                    : requirement.getReqMessage();
            return message != null ? message : "";
        }

        private String usageSuffix() {
            return usageCode != null ? " in " + usageCode : "";
        }
    }

    @Test
    public void becpgRegulatoryNotAccessibleTest() {
        try {

            NodeRef finishedProductNodeRef = createTestFinishedProduct();

            inWriteTx(() -> {
                systemConfigurationService.updateConfValue("beCPG.regulatory.serverUrl", "http://does-not-exist.nowhere");
                systemConfigurationService.updateConfValue("beCPG.regulatory.enabled", "true");
                return null;
            });

            inWriteTx(() -> {
                ProductData product = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
                return regulatoryService.doCheck(false, new ComplianceResult(), product);
            });

            inWriteTx(() -> {
                ProductData product = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);

                int declaredCountriesAmount = product.getRegulatoryList().getFirst().getRegulatoryCountriesRef().size();
                int declaredUsagesAmount = product.getRegulatoryList().getFirst().getRegulatoryUsagesRef().size();
                int amountOfRegulatoryErrors = declaredCountriesAmount * declaredUsagesAmount;

                RegulatoryListDataItem regulatoryElement = product.getRegulatoryList().getFirst();
                assertEquals(RegulatoryResult.ERROR, regulatoryElement.getRegulatoryResult());

                List<RequirementListDataItem> regulatoryErrorsList = product.getReqCtrlList().stream().filter(reqCtrl ->
                        "regulatory".equals(reqCtrl.getFormulationChainId())
                ).toList();

                // list has errors for each country usage pair
                assertTrue(regulatoryErrorsList.size() >= amountOfRegulatoryErrors);
                int serviceUnreachableErrorsAmount = 0;
                for (RequirementListDataItem reqCtrl : regulatoryErrorsList) {
                    if (RequirementType.Forbidden.equals(reqCtrl.getReqType()) &&
                            RequirementDataType.Formulation.equals(reqCtrl.getReqDataType()) &&
                            reqCtrl.getReqMessage().contains("I/O error on POST request for \"http://does-not-exist.nowhere/v1/regulatory/check\"") &&
                            reqCtrl.getReqMessage().toLowerCase().contains("becpg")) {
                        serviceUnreachableErrorsAmount++;
                    }
                }
                assertEquals(serviceUnreachableErrorsAmount, amountOfRegulatoryErrors);
                return null;
            });
        } finally {
            inWriteTx(() -> {
                systemConfigurationService.resetConfValue("beCPG.regulatory.serverUrl");
                systemConfigurationService.resetConfValue("beCPG.regulatory.enabled");
                return null;
            });
        }
    }

    /**
     * The embedded compliance view relays the answer of becpg-regulatory as is, whatever the
     * regulatory mode of the product (Decernis by default), and writes nothing: neither the
     * ingredient regulatory list nor the becpg codes of the ingredients.
     */
    @Test
    public void testComplianceViewIsRelayedWithoutPersisting() throws Exception {
        inWriteTx(() -> {
            nodeService.setProperty(ing1, PLMModel.PROP_REGULATORY_CODE, "DECERNIS_42");
            return null;
        });
        NodeRef finishedProductNodeRef = createProductWithRegulatoryList("PF BecpgRegulatory testComplianceViewIsRelayedWithoutPersisting");
        String viewBody = "{\"summary\":{\"verdict\":\"COMPLIANT\"},\"markets\":[]}";

        try {
            inWriteTx(() -> {
                systemConfigurationService.updateConfValue("beCPG.regulatory.serverUrl", mockServerUrl);
                mockWebServer.enqueue(new MockResponse().setBody(viewBody));
                return null;
            });

            String view = inReadTx(() -> regulatoryComplianceViewService.fetchView(finishedProductNodeRef, true));

            JSONObject relayed = new JSONObject(view);
            assertEquals("COMPLIANT", relayed.getJSONObject("summary").getString("verdict"));
            assertTrue(relayed.has("recipeFetchMs"));
            RecordedRequest request = mockWebServer.takeRequest();
            assertEquals("/v1/regulatory/check/view?refresh=true", request.getPath());
            assertTrue(request.getBody().readUtf8().contains(ing1.getId()));
            inReadTx(() -> {
                ProductData product = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
                assertTrue(product.getIngRegulatoryList() == null || product.getIngRegulatoryList().isEmpty());
                assertEquals("DECERNIS_42", nodeService.getProperty(ing1, PLMModel.PROP_REGULATORY_CODE));
                return null;
            });
        } finally {
            inWriteTx(() -> {
                systemConfigurationService.resetConfValue("beCPG.regulatory.serverUrl");
                return null;
            });
        }
    }

    private NodeRef createProductWithRegulatoryList(String name) {
        NodeRef countryNodeRef = getCountry("Germany", "DE");
        NodeRef usageNodeRef = getUsage("Body soap", "COSMETIC_BODY_SOAP,DECERNIS_Body Soap");
        NodeRef finishedProductNodeRef = createFinishedProduct(name);
        inWriteTx(() -> {
            ProductData product = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
            product.getIngList().add(IngListDataItem.build().withQtyPerc(2d).withGeoOrigin(null).withBioOrigin(null).withIsGMO(null)
                    .withIsIonized(null).withIsProcessingAid(null).withIngredient(ing1).withIsManual(null));
            RegulatoryListDataItem item = new RegulatoryListDataItem();
            item.setRegulatoryCountriesRef(new ArrayList<>(List.of(countryNodeRef)));
            item.setRegulatoryUsagesRef(new ArrayList<>(List.of(usageNodeRef)));
            item.setRegulatoryState(SystemState.Simulation);
            product.getRegulatoryList().add(item);
            return alfrescoRepository.save(product);
        });
        return finishedProductNodeRef;
    }

    @Test
    public void testRequestsToBecpgRegulatoryDoesNotOverrideDecernisRegulatoryCode() {

        inWriteTx(() -> {
            nodeService.setProperty(ing1, PLMModel.PROP_REGULATORY_CODE, "42");
            nodeService.setProperty(ing2, PLMModel.PROP_REGULATORY_CODE, "BECPG_123,42");
            nodeService.setProperty(ing3, PLMModel.PROP_REGULATORY_CODE, "DECERNIS_42");
            nodeService.setProperty(ing4, PLMModel.PROP_REGULATORY_CODE, "BECPG_123,DECERNIS_42");
            nodeService.setProperty(ing5, PLMModel.PROP_REGULATORY_CODE, "DECERNIS_42");
            return null;
        });

        NodeRef countryNodeRef = getCountry("Germany", "DE");
        NodeRef usageNodeRef = getUsage("Body soap", "COSMETIC_BODY_SOAP,DECERNIS_Body Soap");
        NodeRef finishedProductNodeRef = createFinishedProduct("PF BecpgRegulatory testRequestsToBecpgRegulatoryDoesNotOverrideDecernisRegulatoryCode");

        // substitute placeholders with actual IDs
        inWriteTx(() -> {
            systemConfigurationService.updateConfValue("beCPG.regulatory.serverUrl", mockServerUrl);
            systemConfigurationService.updateConfValue("beCPG.regulatory.enabled", "true");
            String body = readJsonResource("beCPG/regulatory/becpg/response-codes.json");
            body = body.replaceAll("ing-1-id", ing1.getId());
            body = body.replaceAll("ing-2-id", ing2.getId());
            body = body.replaceAll("ing-3-id", ing3.getId());
            body = body.replaceAll("ing-4-id", ing4.getId());
            body = body.replaceAll("ing-5-id", ing5.getId());
            body = body.replaceAll("regulatory-country-uuid-here", countryNodeRef.getId());
            mockWebServer.enqueue(new MockResponse().setBody(body));
            return null;
        });

        try {
            inWriteTx(() -> {
                ProductData product = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);

                List<IngListDataItem> ingList = product.getIngList();

                for (NodeRef ing : List.of(ing1, ing2, ing3, ing4, ing5)) {
                    ingList.add(IngListDataItem.build().withQtyPerc(2d).withGeoOrigin(null).withBioOrigin(null).withIsGMO(null).withIsIonized(null).withIsProcessingAid(null).withIngredient(ing).withIsManual(null));
                }

                List<RegulatoryListDataItem> regulatoryList = product.getRegulatoryList();

                RegulatoryListDataItem item = new RegulatoryListDataItem();
                item.setRegulatoryCountriesRef(new ArrayList<>(List.of(countryNodeRef)));
                item.setRegulatoryUsagesRef(new ArrayList<>(List.of(usageNodeRef)));
                item.setRegulatoryState(SystemState.Simulation);

                regulatoryList.add(item);

                return alfrescoRepository.save(product);
            });

            inWriteTx(() -> {
                ProductData product = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
                ComplianceResult result = new ComplianceResult();
                regulatoryService.doCheck(false, result, product);
                // ingredient characts should get updated
                return null;
            });

            inWriteTx(() -> {
                IngItem updatedIng1 = (IngItem) alfrescoRepository.findOne(ing1);
                IngItem updatedIng2 = (IngItem) alfrescoRepository.findOne(ing2);
                IngItem updatedIng3 = (IngItem) alfrescoRepository.findOne(ing3);
                IngItem updatedIng4 = (IngItem) alfrescoRepository.findOne(ing4);
                IngItem updatedIng5 = (IngItem) alfrescoRepository.findOne(ing5);

                Set<String> ing1codes = Arrays.stream(updatedIng1.getRegulatoryCode().split(",")).collect(Collectors.toSet());
                Set<String> ing2codes = Arrays.stream(updatedIng2.getRegulatoryCode().split(",")).collect(Collectors.toSet());
                Set<String> ing3codes = Arrays.stream(updatedIng3.getRegulatoryCode().split(",")).collect(Collectors.toSet());
                Set<String> ing4codes = Arrays.stream(updatedIng4.getRegulatoryCode().split(",")).collect(Collectors.toSet());
                Set<String> ing5codes = Arrays.stream(updatedIng5.getRegulatoryCode().split(",")).collect(Collectors.toSet());

                assertEquals(Set.of("BECPG_123", "42"), ing1codes);
                assertEquals(Set.of("BECPG_123", "42"), ing2codes);
                assertEquals(Set.of("BECPG_123", "DECERNIS_42"), ing3codes);
                assertEquals(Set.of("BECPG_123", "DECERNIS_42"), ing4codes);
                assertEquals(Set.of("BECPG_123", "BECPG_456", "DECERNIS_42"), ing5codes);
                return null;
            });
        } finally {
            inWriteTx(() -> {
                systemConfigurationService.resetConfValue("beCPG.regulatory.serverUrl");
                systemConfigurationService.resetConfValue("beCPG.regulatory.enabled");
                return null;
            });
        }
    }

    @Test
    public void testRequestsToBecpgRegulatoryWithNullAndEmptyRegulatoryCodes() {
        inWriteTx(() -> {
            nodeService.removeProperty(ing1, PLMModel.PROP_REGULATORY_CODE);
            nodeService.setProperty(ing2, PLMModel.PROP_REGULATORY_CODE, "");
            nodeService.setProperty(ing3, PLMModel.PROP_REGULATORY_CODE, ",,DECERNIS_42,");
            return null;
        });

        NodeRef countryNodeRef = getCountry("Germany", "DE");
        NodeRef usageNodeRef = getUsage("Body soap", "COSMETIC_BODY_SOAP,DECERNIS_Body Soap");
        NodeRef finishedProductNodeRef = createFinishedProduct("PF BecpgRegulatory testRequestsToBecpgRegulatoryWithNullAndEmptyRegulatoryCodes");

        inWriteTx(() -> {
            systemConfigurationService.updateConfValue("beCPG.regulatory.serverUrl", mockServerUrl);
            systemConfigurationService.updateConfValue("beCPG.regulatory.enabled", "true");
            String body = readJsonResource("beCPG/regulatory/becpg/response-codes.json");
            body = body.replaceAll("ing-1-id", ing1.getId());
            body = body.replaceAll("ing-2-id", ing2.getId());
            body = body.replaceAll("ing-3-id", ing3.getId());
            body = body.replaceAll("ing-4-id", ing4.getId());
            body = body.replaceAll("ing-5-id", ing5.getId());
            body = body.replaceAll("regulatory-country-uuid-here", countryNodeRef.getId());
            mockWebServer.enqueue(new MockResponse().setBody(body));
            return null;
        });

        try {
            inWriteTx(() -> {
                ProductData product = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
                List<IngListDataItem> ingList = product.getIngList();
                for (NodeRef ing : List.of(ing1, ing2, ing3)) {
                    ingList.add(IngListDataItem.build().withQtyPerc(2d).withGeoOrigin(null).withBioOrigin(null).withIsGMO(null).withIsIonized(null).withIsProcessingAid(null).withIngredient(ing).withIsManual(null));
                }

                RegulatoryListDataItem item = new RegulatoryListDataItem();
                item.setRegulatoryCountriesRef(new ArrayList<>(List.of(countryNodeRef)));
                item.setRegulatoryUsagesRef(new ArrayList<>(List.of(usageNodeRef)));
                item.setRegulatoryState(SystemState.Simulation);
                product.getRegulatoryList().add(item);

                return alfrescoRepository.save(product);
            });

            inWriteTx(() -> {
                ProductData product = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
                ComplianceResult result = new ComplianceResult();
                regulatoryService.doCheck(false, result, product);
                return null;
            });

            inWriteTx(() -> {
                IngItem updatedIng1 = (IngItem) alfrescoRepository.findOne(ing1);
                IngItem updatedIng2 = (IngItem) alfrescoRepository.findOne(ing2);
                IngItem updatedIng3 = (IngItem) alfrescoRepository.findOne(ing3);

                assertNotNull(updatedIng1.getRegulatoryCode());
                assertEquals("BECPG_123", updatedIng1.getRegulatoryCode());

                assertNotNull(updatedIng2.getRegulatoryCode());
                assertEquals("BECPG_123", updatedIng2.getRegulatoryCode());

                Set<String> ing3codes = Arrays.stream(updatedIng3.getRegulatoryCode().split(",")).collect(Collectors.toSet());
                assertEquals(Set.of("BECPG_123", "DECERNIS_42"), ing3codes);
                assertFalse(ing3codes.contains(""));
                return null;
            });
        } finally {
            inWriteTx(() -> {
                systemConfigurationService.resetConfValue("beCPG.regulatory.serverUrl");
                systemConfigurationService.resetConfValue("beCPG.regulatory.enabled");
                return null;
            });
        }
    }

    @Test
    public void testEmptyIngRegulatoryListItemsAreFilteredAndContextPreserved() {
        inWriteTx(() -> {
            nodeService.setProperty(ing1, PLMModel.PROP_REGULATORY_CODE, "BECPG_123");
            nodeService.setProperty(ing2, PLMModel.PROP_REGULATORY_CODE, "BECPG_123");
            nodeService.setProperty(ing3, PLMModel.PROP_REGULATORY_CODE, "DECERNIS_42");
            nodeService.setProperty(ing4, PLMModel.PROP_REGULATORY_CODE, "DECERNIS_42");
            nodeService.setProperty(ing5, PLMModel.PROP_REGULATORY_CODE, "DECERNIS_42");
            return null;
        });

        NodeRef countryNodeRef = getCountry("Germany", "DE");
        NodeRef usageNodeRef = getUsage("Body soap", "COSMETIC_BODY_SOAP,DECERNIS_Body Soap");
        NodeRef finishedProductNodeRef = createFinishedProduct("PF BecpgRegulatory testEmptyIngRegulatoryListItemsAreFilteredAndContextPreserved");

        inWriteTx(() -> {
            systemConfigurationService.updateConfValue("beCPG.regulatory.serverUrl", mockServerUrl);
            systemConfigurationService.updateConfValue("beCPG.regulatory.enabled", "true");
            String body = readJsonResource("beCPG/regulatory/becpg/response-codes.json");
            body = body.replaceAll("ing-1-id", ing1.getId());
            body = body.replaceAll("ing-2-id", ing2.getId());
            body = body.replaceAll("ing-3-id", ing3.getId());
            body = body.replaceAll("ing-4-id", ing4.getId());
            body = body.replaceAll("ing-5-id", ing5.getId());
            body = body.replaceAll("regulatory-country-uuid-here", countryNodeRef.getId());
            mockWebServer.enqueue(new MockResponse().setBody(body));
            return null;
        });

        try {
            inWriteTx(() -> {
                ProductData product = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
                List<IngListDataItem> ingList = product.getIngList();
                for (NodeRef ing : List.of(ing1, ing2, ing3, ing4, ing5)) {
                    ingList.add(IngListDataItem.build().withQtyPerc(2d).withGeoOrigin(null).withBioOrigin(null).withIsGMO(null).withIsIonized(null).withIsProcessingAid(null).withIngredient(ing).withIsManual(null));
                }

                RegulatoryListDataItem item = new RegulatoryListDataItem();
                item.setRegulatoryCountriesRef(new ArrayList<>(List.of(countryNodeRef)));
                item.setRegulatoryUsagesRef(new ArrayList<>(List.of(usageNodeRef)));
                item.setRegulatoryState(SystemState.Simulation);
                product.getRegulatoryList().add(item);

                return alfrescoRepository.save(product);
            });

            inWriteTx(() -> {
                ProductData product = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
                ComplianceResult result = new ComplianceResult();

                IngRegulatoryListDataItem existingItem = new IngRegulatoryListDataItem();
                RegulatoryContext context = createContext(product);
                result.setContext(context);
                result.getContext().getIngRegulatoryListDataItems().add(existingItem);

                regulatoryService.doCheck(false, result, product);

                List<NodeRef> ingredientsWithRegulations = result.getContext().getIngRegulatoryListDataItems().stream()
                        .map(IngRegulatoryListDataItem::getIng)
                        .filter(Objects::nonNull)
                        .toList();

                // non-empty
                assertEquals(2, ingredientsWithRegulations.size());
                assertTrue(ingredientsWithRegulations.contains(ing1));
                assertTrue(ingredientsWithRegulations.contains(ing2));

                // only bearing code, filtered
                assertFalse(ingredientsWithRegulations.contains(ing3));
                assertFalse(ingredientsWithRegulations.contains(ing4));
                assertFalse(ingredientsWithRegulations.contains(ing5));
                return null;
            });
        } finally {
            inWriteTx(() -> {
                systemConfigurationService.resetConfValue("beCPG.regulatory.serverUrl");
                systemConfigurationService.resetConfValue("beCPG.regulatory.enabled");
                return null;
            });
        }
    }

    protected RegulatoryContext createContext(ProductData product) {
        RegulatoryContext context = new RegulatoryContext();
        if (product.getIngList() != null) {
            context.getIngList().addAll(product.getIngList().stream().filter(this::isIngItemValid).toList());
        }
        context.setProduct(product);
        return context;
    }

    protected boolean isIngItemValid(IngListDataItem ingListDataItem) {
        return !DeclarationType.Omit.equals(ingListDataItem.getDeclType());
    }
}
