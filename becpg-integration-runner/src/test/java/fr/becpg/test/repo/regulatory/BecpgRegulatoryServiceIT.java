package fr.becpg.test.repo.regulatory;

import fr.becpg.repo.activity.EntityActivityService;
import fr.becpg.repo.authentication.BeCPGTicketService;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.IngRegulatoryListDataItem;
import fr.becpg.repo.product.data.productList.RegulatoryListDataItem;
import fr.becpg.repo.regulatory.ComplianceResult;
import fr.becpg.repo.regulatory.RegulatoryResult;
import fr.becpg.repo.regulatory.RequirementListDataItem;
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

    @Autowired
    private BeCPGTicketService beCPGTicketService;

    private BecpgRegulatoryService regulatoryService;


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
                beCPGTicketService
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
    public void becpgRegulatoryTest() {
        NodeRef finishedProductNodeRef = createTestFinishedProduct();

        inWriteTx(() -> {
            ProductData product = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
            return regulatoryService.doCheck(false, new ComplianceResult(), product);
        });

        inWriteTx(() -> {
            ProductData product = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);

            int amountOfListedIngredients = 45; // identifiable ingredients for this product
            int amountOfListedCountries = 5; // "France", "Germany", "Spain", "Italy", "European Union"
            int amountOfIdentifiedUsages = 1; // IFRA_LIQUID_SOAP

            int amountOfCountryToUsagePairsNotFound = 23; // non-EU countries : "Body Cream", "Body Soap", "Hand Soap"
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
}
