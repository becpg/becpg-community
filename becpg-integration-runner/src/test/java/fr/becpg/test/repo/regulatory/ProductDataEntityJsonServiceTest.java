package fr.becpg.test.repo.regulatory;

import com.google.common.collect.Lists;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.IngRegulatoryListDataItem;
import fr.becpg.repo.product.data.productList.RegulatoryListDataItem;
import fr.becpg.repo.regulatory.RequirementDataType;
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.repo.regulatory.RequirementType;
import fr.becpg.repo.regulatory.becpg.regulatory.ProductDataEntityJsonService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class ProductDataEntityJsonServiceTest {

    private static final String STORE_PREFIX = "workspace://SpacesStore/";
    private static final String ING_ID = "1103b3df-f293-404a-83b3-dff293204aed";
    private static final String ING_LIST_ELEMENT_ID = "7ac4f839-a7d2-42cf-84f8-39a7d252cf38";
    private static final String COUNTRY_ID = "1e920086-ea25-424e-9200-86ea25524e5d";

    // Extra ingredient NOT referenced by bcpg:irlIng in the JSON fixture
    private static final String UNHANDLED_ING_ID = "9c1d2e3f-4a5b-6c7d-8e9f-0a1b2c3d4e5f";
    private static final String UNHANDLED_ING_LIST_ELEMENT_ID = "0f1e2d3c-4b5a-6978-8e7f-6d5c4b3a2918";

    // Extra country NOT referenced by bcpg:reqCtrlList in the JSON fixture
    private static final String OTHER_COUNTRY_ID = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";
    private static final String USAGE_ID = "2f930086-ea25-424e-9200-86ea25524e5e";
    private static final String USAGE_ID_2 = "3a930086-ea25-424e-9200-86ea25524e5f";

    private static final NodeRef ING_NODE = new NodeRef(STORE_PREFIX + ING_ID);
    private static final NodeRef ING_LIST_ELEMENT_NODE = new NodeRef(STORE_PREFIX + ING_LIST_ELEMENT_ID);
    private static final NodeRef COUNTRY_NODE = new NodeRef(STORE_PREFIX + COUNTRY_ID);

    private static final NodeRef UNHANDLED_ING_NODE = new NodeRef(STORE_PREFIX + UNHANDLED_ING_ID);
    private static final NodeRef UNHANDLED_ING_LIST_ELEMENT_NODE = new NodeRef(STORE_PREFIX + UNHANDLED_ING_LIST_ELEMENT_ID);

    private static final NodeRef OTHER_COUNTRY_NODE = new NodeRef(STORE_PREFIX + OTHER_COUNTRY_ID);
    private static final NodeRef USAGE_NODE = new NodeRef(STORE_PREFIX + USAGE_ID);
    private static final NodeRef USAGE_NODE_2 = new NodeRef(STORE_PREFIX + USAGE_ID_2);

    private static final String COUNTRY_CODE = "DE";
    private static final String USAGE_CODE = "COSMETIC_LEAVE_ON_PRODUCTS";
    private static final String USAGE_CODE_2 = "COSMETIC_RINSE_OFF_PRODUCTS";

    private static final String RESSOURCE_PATH = "beCPG/regulatory/becpg/response.json";
    private static final String FORMULATION_CHAIN_ID = "regulatory";

    private static final MLText INGREDIENT_NOT_LISTED = new MLText("Not listed ingredients");
    private static final MLText COUNTRY_USAGE_PAIR_NOT_FOUND = new MLText("No requirements found for this Country-Usage pair");
    private static final MLText RESTRICTION_LEVELS = new MLText("Leave-on products :: max: 3.0, unit: % ;; Rinse-off products :: max: 4.0, unit: %");
    private static final MLText CITATION = new MLText("Leave-on products :: (EU) 2013/483 - Annex III, Entry 257 ;; Rinse-off products :: (EU) 2013/483 - Annex III, Entry 257");
    private static final MLText RESULT_INDICATOR = new MLText("Leave-on products :: RESTRICTED ;; Rinse-off products :: RESTRICTED");

    @Mock
    private NodeService nodeService;

    private ProductDataEntityJsonService service;

    @Before
    public void setUp() {
        openMocks(this);
        service = new ProductDataEntityJsonService(nodeService);
    }

    private ProductData referenceWithOneRegulatoryPair() {
        RegulatoryListDataItem reg = new RegulatoryListDataItem();
        reg.setRegulatoryCountriesRef(Lists.newArrayList(COUNTRY_NODE));
        reg.setRegulatoryUsagesRef(Lists.newArrayList(USAGE_NODE));

        IngListDataItem ingListItem = new IngListDataItem();
        ingListItem.setIng(ING_NODE);
        ingListItem.setNodeRef(ING_LIST_ELEMENT_NODE);

        ProductData ref = new ProductData();
        ref.setIngList(Lists.newArrayList(ingListItem));
        ref.setRegulatoryList(Lists.newArrayList(reg));
        ref.setReqCtrlList(new ArrayList<>());
        return ref;
    }

    private void mockLocales(MockedStatic<MLTextHelper> mlTextHelper) {
        mlTextHelper.when(() -> MLTextHelper.parseLocale("fr")).thenReturn(Locale.FRENCH);
        mlTextHelper.when(() -> MLTextHelper.parseLocale("en")).thenReturn(Locale.ENGLISH);
        mlTextHelper.when(() -> MLTextHelper.parseLocale("it")).thenReturn(Locale.ITALIAN);
        mlTextHelper.when(() -> MLTextHelper.parseLocale("de")).thenReturn(Locale.GERMAN);
        mlTextHelper.when(() -> MLTextHelper.parseLocale("es")).thenReturn(Locale.of("es", "ES"));
        mlTextHelper.when(() -> MLTextHelper.parseLocale("pt")).thenReturn(Locale.of("pt", "PT"));
    }

    /**
     * One ingredient, one usage, one country
     * For this combination found regulations: for rinse-off and leave-on application. One is ok - second in violated.
     * Therefore - 1 reqCtrl Forbidden element.
     */
    @Test
    public void fillProductDataFromJson_parsesReqCtrlListScalarFields() throws IOException {
        ProductData ref = referenceWithOneRegulatoryPair();
        ref.getIngList().getFirst().setQtyPerc(3.5);

        try (InputStream is = new ClassPathResource(RESSOURCE_PATH).getInputStream()) {
            assertNotNull(is);
            JSONObject json = new JSONObject(new JSONTokener(is));

            ProductData result;
            try (MockedStatic<MLTextHelper> mlTextHelper = mockStatic(MLTextHelper.class)) {
                mlTextHelper.when(() -> MLTextHelper.getI18NMessage(ProductDataEntityJsonService.MESSAGE_NOTLISTED_ING))
                        .thenReturn(INGREDIENT_NOT_LISTED);
                mockLocales(mlTextHelper);
                result = service.newProductDataFromJson(json);

                // ReqCtrl elements
                assertNotNull(result.getReqCtrlList());
                assertEquals(1, result.getReqCtrlList().size());
                long amountOfCorrectItems = result.getReqCtrlList().stream()
                        .filter(i -> i.getRegulatoryCode().equals(COUNTRY_CODE + " - " + USAGE_CODE))
                        .filter(i -> i.getSources().size() == 1 && i.getSources().getFirst().getId().equals(ING_LIST_ELEMENT_ID))
                        .filter(i -> i.getCharact().getId().equals(ING_LIST_ELEMENT_ID))
                        .filter(i -> i.getReqDataType().equals(RequirementDataType.Specification))
                        .filter(i -> i.getFormulationChainId().equals(FORMULATION_CHAIN_ID))
                        .count();
                assertEquals(amountOfCorrectItems, result.getReqCtrlList().size());

                boolean hasForbiddingElement = result.getReqCtrlList().stream()
                        .filter(i -> i.getReqType().equals(RequirementType.Forbidden))
                        .anyMatch(i -> i.getReqMaxQty().equals(3.0));
                assertTrue(hasForbiddingElement);

                // Check multilingual parsed text existence and correctness
                RequirementListDataItem reqCtrl = result.getReqCtrlList().getFirst();
                assertNotNull(reqCtrl.getReqMlMessage());
                assertEquals("Ingredient exceeds allowed limit (actual: 3.5%, maximum: 3%).", reqCtrl.getReqMlMessage().getValue(Locale.ENGLISH));
                assertEquals("L’ingrédient dépasse la limite autorisée (réel: 3.5%, maximum: 3%).", reqCtrl.getReqMlMessage().getValue(Locale.FRENCH));
                assertEquals("L’ingrediente supera il limite consentito (effettivo: 3.5%, massimo: 3%).", reqCtrl.getReqMlMessage().getValue(Locale.ITALIAN));
                assertEquals("Inhaltsstoff überschreitet den zulässigen Grenzwert (tatsächlich: 3.5%, Maximum: 3%).", reqCtrl.getReqMlMessage().getValue(Locale.GERMAN));
                assertEquals("El ingrediente supera el límite permitido (real: 3.5%, máximo: 3%).", reqCtrl.getReqMlMessage().getValue(Locale.of("es", "ES")));
                assertEquals("O ingrediente excede o limite permitido (real: 3.5%, máximo: 3%).", reqCtrl.getReqMlMessage().getValue(Locale.of("pt", "PT")));

                // ingRegulatory Element
                assertNotNull(result.getIngRegulatoryList());
                assertEquals(1, result.getIngRegulatoryList().size());

                IngRegulatoryListDataItem i = result.getIngRegulatoryList().getFirst();
                assertEquals(ING_ID, i.getIng().getId());
                assertEquals(1, i.getRegulatoryCountries().size());
                assertEquals(COUNTRY_ID, i.getRegulatoryCountries().getFirst().getId());

                assertNotNull(i.getPrecautions());
                assertEquals("Leave-on products :: Maximum concentration of 3.0% in leave-on products. Shall not be used as a propellant in aerosols. ;; Rinse-off products :: Maximum concentration of 4.0% in rinse-off products. Shall not be used as a propellant in aerosols.", i.getPrecautions().getValue(Locale.ENGLISH));

                assertEquals(RESTRICTION_LEVELS, i.getRestrictionLevels());
                assertEquals(CITATION, i.getCitation());
                assertEquals(RESULT_INDICATOR, i.getResultIndicator());
            }
        }
    }

    /**
     * 2 ingredients, one usage, one country
     * As there is only 1 ingRegulatory Element : additional ReqCtrl is generated to alert that ing was not handled
     */
    @Test
    public void fillProductDataFromJson_emitsAlertForUnhandledIngredient() throws IOException {
        ProductData ref = referenceWithOneRegulatoryPair();
        ref.getIngList().getFirst().setQtyPerc(3.5);

        // second ingredient not present in the JSON's bcpg:ingRegulatoryList
        IngListDataItem unhandledIngListItem = new IngListDataItem();
        unhandledIngListItem.setIng(UNHANDLED_ING_NODE);
        unhandledIngListItem.setNodeRef(UNHANDLED_ING_LIST_ELEMENT_NODE);
        ref.getIngList().add(unhandledIngListItem);

        try (InputStream is = new ClassPathResource(RESSOURCE_PATH).getInputStream()) {
            assertNotNull(is);
            JSONObject json = new JSONObject(new JSONTokener(is));

            try (MockedStatic<MLTextHelper> mlTextHelper = mockStatic(MLTextHelper.class)) {
                mlTextHelper.when(() -> MLTextHelper.getI18NMessage(ProductDataEntityJsonService.MESSAGE_NOTLISTED_ING))
                        .thenReturn(INGREDIENT_NOT_LISTED);
                mockLocales(mlTextHelper);

                ProductData result = service.newProductDataFromJson(json);

                assertNotNull(result.getIngRegulatoryList());
                assertEquals(1, result.getIngRegulatoryList().size());

                // the explicit reqCtrl entries from the JSON
                assertEquals(1, result.getReqCtrlList().size());

                List<RequirementListDataItem> alertsForMissingIngRegulatory = service.createAlertsForNotCoveredIngredients(ref.getIngList(), result.getIngRegulatoryList()).toList();
                assertEquals(1, alertsForMissingIngRegulatory.size());

                RequirementListDataItem alert = alertsForMissingIngRegulatory.getFirst();
                assertEquals(INGREDIENT_NOT_LISTED, alert.getReqMlMessage());
                assertEquals(RequirementType.Tolerated, alert.getReqType());
                assertEquals(RequirementDataType.Specification, alert.getReqDataType());
                assertEquals(FORMULATION_CHAIN_ID, alert.getFormulationChainId());
                assertEquals(UNHANDLED_ING_ID, alert.getCharact().getId());
                assertEquals(1, alert.getSources().size());
                assertEquals(UNHANDLED_ING_ID, alert.getSources().getFirst().getId());
                assertNull(alert.getRegulatoryCode());
            }
        }
    }

    /**
     * 1 ingredient, 2 usages, 2 countries under the same regulatory element
     * As all reqCtrl elements cover one COUNTRY - USAGE pair - the missing countries remain uncovered and surfaced as Tolerated
     */
    @Test
    public void fillProductDataFromJson_emitsAlertForUnhandledCountry() throws IOException {
        ProductData ref = referenceWithOneRegulatoryPair();
        ref.getIngList().getFirst().setQtyPerc(3.5);

        // second regulatory entry whose country is not present in the JSON's bcpg:ingRegulatoryList
        RegulatoryListDataItem regElement = ref.getRegulatoryList().getFirst();
        regElement.getRegulatoryCountriesRef().add(OTHER_COUNTRY_NODE);

        when(nodeService.getProperty(OTHER_COUNTRY_NODE, PLMModel.PROP_REGULATORY_CODE)).thenReturn("FR");
        when(nodeService.getProperty(USAGE_NODE, PLMModel.PROP_REGULATORY_CODE)).thenReturn(USAGE_CODE);

        try (InputStream is = new ClassPathResource(RESSOURCE_PATH).getInputStream()) {
            assertNotNull(is);
            JSONObject json = new JSONObject(new JSONTokener(is));

            try (MockedStatic<MLTextHelper> mlTextHelper = mockStatic(MLTextHelper.class)) {
                mlTextHelper.when(() -> MLTextHelper.getI18NMessage(ProductDataEntityJsonService.MESSAGE_NOTLISTED_ING))
                        .thenReturn(INGREDIENT_NOT_LISTED);
                mlTextHelper.when(() -> MLTextHelper.getI18NMessage(ProductDataEntityJsonService.MESSAGE_COUNTRY_USAGE_PAIR_NOT_FOUND))
                        .thenReturn(COUNTRY_USAGE_PAIR_NOT_FOUND);
                mockLocales(mlTextHelper);

                ProductData result = service.newProductDataFromJson(json);

                // the explicit reqCtrl entries from the JSON
                assertEquals(1, result.getReqCtrlList().size());

                List<RequirementListDataItem> alertsForMissingsCountryUsage = service.createAlertsForNotCoveredCountries(ref.getRegulatoryList(), result.getIngRegulatoryList()).toList();
                assertEquals(1, alertsForMissingsCountryUsage.size());

                RequirementListDataItem alert = alertsForMissingsCountryUsage.getFirst();
                assertEquals(RequirementType.Tolerated, alert.getReqType());
                assertEquals(RequirementDataType.Specification, alert.getReqDataType());
                assertEquals(FORMULATION_CHAIN_ID, alert.getFormulationChainId());
                assertEquals(COUNTRY_USAGE_PAIR_NOT_FOUND, alert.getReqMlMessage());
                assertNull(alert.getCharact());
                assertEquals(List.of(OTHER_COUNTRY_NODE, USAGE_NODE), alert.getSources());
                assertEquals("FR - " + USAGE_CODE, alert.getRegulatoryCode());
            }
        }
    }

    @Test
    public void createAlertsForNotCoveredCountries_generatesMultipleAlertsForMultipleUsages() {
        RegulatoryListDataItem regElement = new RegulatoryListDataItem();
        regElement.setRegulatoryCountriesRef(Lists.newArrayList(OTHER_COUNTRY_NODE));
        regElement.setRegulatoryUsagesRef(Lists.newArrayList(USAGE_NODE, USAGE_NODE_2));

        when(nodeService.getProperty(OTHER_COUNTRY_NODE, PLMModel.PROP_REGULATORY_CODE)).thenReturn("FR");
        when(nodeService.getProperty(USAGE_NODE, PLMModel.PROP_REGULATORY_CODE)).thenReturn(USAGE_CODE);
        when(nodeService.getProperty(USAGE_NODE_2, PLMModel.PROP_REGULATORY_CODE)).thenReturn(USAGE_CODE_2);

        try (MockedStatic<MLTextHelper> mlTextHelper = mockStatic(MLTextHelper.class)) {
            mlTextHelper.when(() -> MLTextHelper.getI18NMessage(ProductDataEntityJsonService.MESSAGE_COUNTRY_USAGE_PAIR_NOT_FOUND))
                    .thenReturn(COUNTRY_USAGE_PAIR_NOT_FOUND);

            List<RequirementListDataItem> alerts = service.createAlertsForNotCoveredCountries(
                    List.of(regElement), List.of()
            ).toList();

            assertEquals(2, alerts.size());
            assertTrue(alerts.stream().anyMatch(a -> ("FR - " + USAGE_CODE).equals(a.getRegulatoryCode()) && a.getSources().contains(USAGE_NODE)));
            assertTrue(alerts.stream().anyMatch(a -> ("FR - " + USAGE_CODE_2).equals(a.getRegulatoryCode()) && a.getSources().contains(USAGE_NODE_2)));
        }
    }

    @Test
    public void createAlertsForNotCoveredCountries_returnsEmptyWhenAllCountriesCovered() {
        RegulatoryListDataItem regElement = new RegulatoryListDataItem();
        regElement.setRegulatoryCountriesRef(Lists.newArrayList(COUNTRY_NODE));
        regElement.setRegulatoryUsagesRef(Lists.newArrayList(USAGE_NODE));

        IngRegulatoryListDataItem coveredItem = new IngRegulatoryListDataItem();
        coveredItem.setRegulatoryCountries(Lists.newArrayList(COUNTRY_NODE));

        List<RequirementListDataItem> alerts = service.createAlertsForNotCoveredCountries(
                List.of(regElement), List.of(coveredItem)
        ).toList();

        assertTrue(alerts.isEmpty());
    }

    @Test
    public void extractIngIdToRegulatoryCodes_returnsMap() throws IOException {
        try (InputStream is = new ClassPathResource(RESSOURCE_PATH).getInputStream()) {
            assertNotNull(is);
            JSONObject json = new JSONObject(new JSONTokener(is));
            Map<String, String> map = service.extractIngIdToRegulatoryCodes(json);
            assertEquals(1, map.size());
            assertEquals("BECPG_5417", map.get("1103b3df-f293-404a-83b3-dff293204aed"));
        }
    }

    @Test
    public void extractIngIdToRegulatoryCodes_handlesMalformedJsonSafely() {
        JSONObject malformedJson = new JSONObject("""
                {
                  "datalists": {
                    "bcpg:ingRegulatoryList": [
                      {},
                      { "attributes": {} },
                      { "attributes": { "bcpg:irlIng": {} } },
                      { "attributes": { "bcpg:irlIng": { "id": "ing-no-attrs" } } },
                      { "attributes": { "bcpg:irlIng": { "id": "", "attributes": { "bcpg:regulatoryCode": "BECPG_1" } } } },
                      { "attributes": { "bcpg:irlIng": { "id": "ing-empty-code", "attributes": { "bcpg:regulatoryCode": "" } } } },
                      { "attributes": { "bcpg:irlIng": { "id": "ing-valid", "attributes": { "bcpg:regulatoryCode": "BECPG_999" } } } }
                    ]
                  }
                }
                """);

        Map<String, String> map = service.extractIngIdToRegulatoryCodes(malformedJson);
        assertEquals(1, map.size());
        assertEquals("BECPG_999", map.get("ing-valid"));
    }

    @Test
    public void extractIngIdToRegulatoryCodes_handlesConflictingCodesForSameIngredient() {
        JSONObject conflictingJson = new JSONObject("""
                {
                  "datalists": {
                    "bcpg:ingRegulatoryList": [
                      { "attributes": { "bcpg:irlIng": { "id": "ing-dup", "attributes": { "bcpg:regulatoryCode": "BECPG_100" } } } },
                      { "attributes": { "bcpg:irlIng": { "id": "ing-dup", "attributes": { "bcpg:regulatoryCode": "BECPG_200" } } } }
                    ]
                  }
                }
                """);

        Map<String, String> map = service.extractIngIdToRegulatoryCodes(conflictingJson);
        assertEquals(1, map.size());
        assertEquals("BECPG_100", map.get("ing-dup"));
    }

    @Test
    public void newProductDataFromJson_parsesMixedBaseAndLocalizedMLText() {
        JSONObject json = new JSONObject("""
                {
                  "datalists": {
                    "bcpg:ingRegulatoryList": [
                      {
                        "attributes": {
                          "bcpg:irlPrecautions": "Base precaution",
                          "bcpg:irlPrecautions_fr": "Précaution FR",
                          "bcpg:irlPrecautions_en": "Precaution EN"
                        }
                      }
                    ],
                    "bcpg:reqCtrlList": [
                      {
                        "attributes": {
                          "bcpg:rclReqMessage": "Base requirement message",
                          "bcpg:rclReqMessage_fr": "Message d'exigence FR"
                        }
                      }
                    ]
                  }
                }
                """);

        try (MockedStatic<MLTextHelper> mlTextHelper = mockStatic(MLTextHelper.class);
             MockedStatic<MLText> mlTextMock = mockStatic(MLText.class, Mockito.CALLS_REAL_METHODS)) {

            mlTextMock.when(MLText::getDefaultLocale).thenReturn(Locale.US);
            mockLocales(mlTextHelper);

            ProductData result = service.newProductDataFromJson(json);

            assertNotNull(result.getIngRegulatoryList());
            assertEquals(1, result.getIngRegulatoryList().size());
            MLText precautions = result.getIngRegulatoryList().getFirst().getPrecautions();
            assertNotNull(precautions);
            assertEquals("Base precaution", precautions.getValue(MLText.getDefaultLocale()));
            assertEquals("Précaution FR", precautions.getValue(Locale.FRENCH));
            assertEquals("Precaution EN", precautions.getValue(Locale.ENGLISH));

            assertNotNull(result.getReqCtrlList());
            assertEquals(1, result.getReqCtrlList().size());
            MLText reqMessage = result.getReqCtrlList().getFirst().getReqMlMessage();
            assertNotNull(reqMessage);
            assertEquals("Base requirement message", reqMessage.getValue(MLText.getDefaultLocale()));
            assertEquals("Message d'exigence FR", reqMessage.getValue(Locale.FRENCH));
        }
    }

    @Test
    public void newProductDataFromJson_parsesStandaloneBaseKeyMLText() {
        JSONObject json = new JSONObject("""
                {
                  "datalists": {
                    "bcpg:ingRegulatoryList": [
                      {
                        "attributes": {
                          "bcpg:irlPrecautions": "Only base precaution"
                        }
                      }
                    ]
                  }
                }
                """);
        try (MockedStatic<MLTextHelper> mlTextHelper = mockStatic(MLTextHelper.class);
             MockedStatic<MLText> mlTextMock = mockStatic(MLText.class, Mockito.CALLS_REAL_METHODS)) {
            mlTextMock.when(MLText::getDefaultLocale).thenReturn(Locale.US);
            mockLocales(mlTextHelper);

            ProductData result = service.newProductDataFromJson(json);

            assertNotNull(result.getIngRegulatoryList());
            assertEquals(1, result.getIngRegulatoryList().size());
            MLText precautions = result.getIngRegulatoryList().getFirst().getPrecautions();
            assertNotNull(precautions);
            assertEquals("Only base precaution", precautions.getValue(MLText.getDefaultLocale()));
            assertEquals(1, precautions.size());
        }
    }
}
