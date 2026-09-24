package fr.becpg.test.repo.regulatory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.activity.EntityActivityService;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.formulation.FormulationService;
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
import fr.becpg.repo.sample.StandardBodyMilkTestProduct;
import fr.becpg.repo.sample.StandardSoapTestProduct;
import fr.becpg.repo.system.SystemConfigurationService;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;
import fr.becpg.util.MutexFactory;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

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
            RegulatoryListDataItem regulatoryElement = product.getRegulatoryList().getFirst();
            assertEquals(RegulatoryResult.PERMITTED, regulatoryElement.getRegulatoryResult());

            // set CITRUS PARADISI FRUIT EXTRACT to 1 - bcpg-regulatory should invalidate product,
            // as this ingredient is linked with forbidden "furucomarines"
            IngListDataItem arsenicListItem = product.getIngList().stream().filter(ing ->
                    alfrescoRepository.findOne(ing.getIng()).getName().equals("CITRUS PARADISI FRUIT EXTRACT")
            ).findFirst().orElse(null);
            assertNotNull(arsenicListItem);
            arsenicListItem.setQtyPerc(0.00075);
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
            assertEquals(1, regulatoryElement.getLimitingIngredients().size());
            IngListDataItem offenderListItem = (IngListDataItem) alfrescoRepository.findOne(regulatoryElement.getLimitingIngredients().getFirst());
            IngItem offender = (IngItem) alfrescoRepository.findOne(offenderListItem.getIng());
            assertEquals("CITRUS PARADISI FRUIT EXTRACT", offender.getCharactName());
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

            assertEquals(viewBody, view);
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
