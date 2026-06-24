package fr.becpg.test.repo.regulatory;

import fr.becpg.repo.activity.EntityActivityService;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.IngRegulatoryListDataItem;
import fr.becpg.repo.product.data.productList.RegulatoryListDataItem;
import fr.becpg.repo.regulatory.*;
import fr.becpg.repo.regulatory.becpg.regulatory.BecpgRegulatoryAuthenticationService;
import fr.becpg.repo.regulatory.becpg.regulatory.BecpgRegulatoryService;
import fr.becpg.repo.regulatory.becpg.regulatory.ProductDataEntityJsonService;
import fr.becpg.repo.sample.StandardBodyMilkTestProduct;
import fr.becpg.repo.sample.StandardSoapTestProduct;
import fr.becpg.repo.system.SystemConfigurationService;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;
import fr.becpg.util.MutexFactory;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class BecpgRegulatoryServiceIT extends AbstractFinishedProductTest {

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

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
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
                remoteEntityService,
                becpgRegulatoryAuthenticationService
        );

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

    /**
     * Impossible to mock, as mapping is done based on IDs that are dynamically generated on product creation
     * becpg-integration-runner/src/test/resources/beCPG/regulatory/becpg/response.json contains response example for illustrative purposes
     */
    @Ignore("Requires access to running becpg-regulatory instance, to run manually")
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

            int amountOfListedIngredients = 45; // identifiable ingredients for this product
            int amountOfListedCountries = 5; // "France", "Germany", "Spain", "Italy", "European Union"
            int amountOfIdentifiedUsages = 2; // COSMETIC_BODY_LOTION, COSMETIC_BODY_CREAM

            // NOTE: this test is @Ignore (run manually against a live becpg-regulatory). Both usages now
            // resolve to product types, so the not-found count must be re-tuned against the live instance.
            int amountOfCountryToUsagePairsNotFound = 0;
            int amountOfIngredientsNotHandled = 45;

            int resolvedCombinations = amountOfListedIngredients * amountOfListedCountries * amountOfIdentifiedUsages;

            List<IngRegulatoryListDataItem> ingRegulatoryListDataItems = product.getIngRegulatoryList();
            assertEquals(resolvedCombinations, ingRegulatoryListDataItems.size());

            List<RequirementListDataItem> requirements = product.getReqCtrlList();
            assertEquals(resolvedCombinations + amountOfCountryToUsagePairsNotFound + amountOfIngredientsNotHandled, requirements.size());

            // product is valid. There are forbidden ingredients, but qty is 0
            RegulatoryListDataItem regulatoryElement = product.getRegulatoryList().getFirst();
            assertEquals(RegulatoryResult.PERMITTED, regulatoryElement.getRegulatoryResult());

            // set ARSENIC to 1 - bcpg-regulatory should invalidate product
            IngListDataItem arsenicListItem = product.getIngList().stream().filter(ing ->
                    alfrescoRepository.findOne(ing.getIng()).getName().equals("ARSENIC")
            ).findFirst().orElse(null);
            assertNotNull(arsenicListItem);
            arsenicListItem.setQtyPerc(1.0);
            return alfrescoRepository.save(arsenicListItem);
        });

        inWriteTx(() -> {
            ProductData product = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
            return regulatoryService.doCheck(false, new ComplianceResult(), product);
        });

        inWriteTx(() -> {
            ProductData product = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
            RegulatoryListDataItem regulatoryElement = product.getRegulatoryList().getFirst();
            assertEquals(RegulatoryResult.PROHIBITED, regulatoryElement.getRegulatoryResult());
            return null;
        });
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

                assertEquals(amountOfRegulatoryErrors, regulatoryErrorsList.size());
                for (RequirementListDataItem reqCtrl : regulatoryErrorsList) {
                    assertEquals(RequirementType.Forbidden, reqCtrl.getReqType());
                    assertEquals(RequirementDataType.Formulation, reqCtrl.getReqDataType());
                    assertTrue(reqCtrl.getReqMessage().contains("I/O error on POST request for \"http://does-not-exist.nowhere/v1/regulatory/check\""));
                    assertTrue(reqCtrl.getReqMessage().toLowerCase().contains("becpg"));
                }
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
}
