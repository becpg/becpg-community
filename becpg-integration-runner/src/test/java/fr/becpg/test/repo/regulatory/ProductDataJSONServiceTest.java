package fr.becpg.test.repo.regulatory;

import com.google.common.collect.Lists;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.IngRegulatoryListDataItem;
import fr.becpg.repo.product.data.productList.RegulatoryListDataItem;
import fr.becpg.repo.regulatory.RequirementDataType;
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.repo.regulatory.RequirementType;
import fr.becpg.repo.regulatory.becpg.regulatory.ProductDataJSONService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class ProductDataJSONServiceTest {

    private static final String STORE_PREFIX        = "workspace://SpacesStore/";
    private static final String ING_ID              = "51fa6f16-3448-43ae-ba6f-16344833ae9f";
    private static final String ING_LIST_ELEMENT_ID = "58b9e321-c7f8-489e-b9e3-21c7f8e89ed3";
    private static final String COUNTRY_ID          = "40f8e29a-54d4-42cb-b8e2-9a54d462cb11";
    private static final String USAGE_ID            = "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d";

    // Extra ingredient NOT referenced by bcpg:irlIng in the JSON fixture
    private static final String UNHANDLED_ING_ID              = "9c1d2e3f-4a5b-6c7d-8e9f-0a1b2c3d4e5f";
    private static final String UNHANDLED_ING_LIST_ELEMENT_ID = "0f1e2d3c-4b5a-6978-8e7f-6d5c4b3a2918";

    // Extra country/usage NOT referenced by bcpg:reqCtrlList in the JSON fixture
    private static final String OTHER_COUNTRY_ID = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";
    private static final String OTHER_USAGE_ID    = "11111111-2222-3333-4444-555555555555";

    private static final NodeRef ING_NODE               = new NodeRef(STORE_PREFIX + ING_ID);
    private static final NodeRef ING_LIST_ELEMENT_NODE  = new NodeRef(STORE_PREFIX + ING_LIST_ELEMENT_ID);
    private static final NodeRef COUNTRY_NODE           = new NodeRef(STORE_PREFIX + COUNTRY_ID);
    private static final NodeRef USAGE_NODE             = new NodeRef(STORE_PREFIX + USAGE_ID);

    private static final NodeRef UNHANDLED_ING_NODE              = new NodeRef(STORE_PREFIX + UNHANDLED_ING_ID);
    private static final NodeRef UNHANDLED_ING_LIST_ELEMENT_NODE  = new NodeRef(STORE_PREFIX + UNHANDLED_ING_LIST_ELEMENT_ID);

    private static final NodeRef OTHER_COUNTRY_NODE = new NodeRef(STORE_PREFIX + OTHER_COUNTRY_ID);
    private static final NodeRef OTHER_USAGE_NODE   = new NodeRef(STORE_PREFIX + OTHER_USAGE_ID);

    private static final String COUNTRY_CODE            = "European Union";
    private static final String USAGE_CODE              = "IFRA_BAR_SOAP";

    private static final String OTHER_COUNTRY_CODE      = "United States";
    private static final String OTHER_USAGE_CODE        = "FDA_BAR_SOAP";

    private static final String RESSOURCE_PATH          = "beCPG/regulatory/becpg/response.json";
    private static final String FORMULATION_CHAIN_ID    = "regulatory";

    private static final MLText INGREDIENT_NOT_LISTED           = new MLText("Not listed ingredients");
    private static final MLText COUNTRY_USAGE_PAIR_NOT_FOUND    = new MLText("No requirements found for this Country-Usage pair");
    private static final MLText RESTRICTION_LEVELS              = new MLText("COSMETIC :: (a) Leave-on products (b) Rinse-off products Maximum concentration: (a) 3,0 % (b) 4,0 %");
    private static final MLText CITATION                        = new MLText("CE Regulation 1223/2009 - Annex III, Entry 257");
    private static final MLText RESULT_INDICATOR                = new MLText("COSMETIC: RESTRICTED: Simple business rule: RESTRICTED for leave-on products ;; Simple business rule: RESTRICTED for rinse-off products");

    @Mock
    private NodeService nodeService;

    private ProductDataJSONService service;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ProductDataJSONService(nodeService);
    }

    private ProductData referenceWithOneRegulatoryPair(String countryCode, String usageCode) {
        when(nodeService.getProperty(eq(COUNTRY_NODE), any(QName.class))).thenReturn(countryCode);
        when(nodeService.getProperty(eq(USAGE_NODE), any(QName.class))).thenReturn(usageCode);

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

    /**
     * One ingredient, one usage, one country
     * For this combination forund 2 regulations: for rinse-off and leave-on application. One is ok - second in violated.
     * Therefore - 2 reqCtrl elements - one is Tolerated another - Forbidden.
     * Both Notions are combined under the same IngRegulatory element.
     */
    @Test
    public void fillProductDataFromJson_parsesReqCtrlListScalarFields() throws IOException {
        ProductData ref = referenceWithOneRegulatoryPair(COUNTRY_CODE, USAGE_CODE);
        ref.getIngList().getFirst().setQtyPerc(3.5);

        try (InputStream is = new ClassPathResource(RESSOURCE_PATH).getInputStream()) {
            assertNotNull(is);
            JSONObject json = new JSONObject(new JSONTokener(is));

            ProductData result;
            try (MockedStatic<MLTextHelper> mlTextHelper = mockStatic(MLTextHelper.class)) {
                mlTextHelper.when(() -> MLTextHelper.getI18NMessage(ProductDataJSONService.MESSAGE_NOTLISTED_ING))
                        .thenReturn(INGREDIENT_NOT_LISTED);
                result = service.newProductDataFromJson(ref, json);

                // ReqCtrl elements
                assertNotNull(result.getReqCtrlList());
                assertEquals(2, result.getReqCtrlList().size());
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

                boolean hasToleratedElement = result.getReqCtrlList().stream()
                        .filter(i -> i.getReqType().equals(RequirementType.Tolerated))
                        .anyMatch(i -> i.getReqMaxQty().equals(4.0));
                assertTrue(hasToleratedElement);

                // ingRegulatory Element
                assertNotNull(result.getIngRegulatoryList());
                assertEquals(1, result.getIngRegulatoryList().size());

                IngRegulatoryListDataItem i = result.getIngRegulatoryList().getFirst();
                assertEquals(ING_ID, i.getIng().getId());
                assertEquals(1, i.getRegulatoryCountries().size());
                assertEquals(COUNTRY_ID, i.getRegulatoryCountries().getFirst().getId());

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
        ProductData ref = referenceWithOneRegulatoryPair(COUNTRY_CODE, USAGE_CODE);
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
                mlTextHelper.when(() -> MLTextHelper.getI18NMessage(ProductDataJSONService.MESSAGE_NOTLISTED_ING))
                        .thenReturn(INGREDIENT_NOT_LISTED);

                ProductData result = service.newProductDataFromJson(ref, json);

                assertNotNull(result.getIngRegulatoryList());
                assertEquals(1, result.getIngRegulatoryList().size());

                // the 2 explicit reqCtrl entries from the JSON + 1 alert for the unhandled ingredient
                assertEquals(3, result.getReqCtrlList().size());

                List<RequirementListDataItem> alerts = result.getReqCtrlList().stream()
                        .filter(i -> i.getReqMlMessage() != null && i.getReqMlMessage().equals(INGREDIENT_NOT_LISTED))
                        .toList();
                assertEquals(1, alerts.size());

                RequirementListDataItem alert = alerts.getFirst();
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
     * As all reqCtrl elements cover one COUNTRY - USAGE pair - the rest 3 remain uncovered and surfaced as Tolerated
     */
    @Test
    public void fillProductDataFromJson_emitsAlertForUnhandledCountryUsagePair() throws IOException {
        ProductData ref = referenceWithOneRegulatoryPair(COUNTRY_CODE, USAGE_CODE);
        ref.getIngList().getFirst().setQtyPerc(3.5);

        when(nodeService.getProperty(eq(OTHER_COUNTRY_NODE), any(QName.class))).thenReturn(OTHER_COUNTRY_CODE);
        when(nodeService.getProperty(eq(OTHER_USAGE_NODE), any(QName.class))).thenReturn(OTHER_USAGE_CODE);

        // second regulatory entry whose pair is not present in the JSON's bcpg:reqCtrlList
        RegulatoryListDataItem regElement = ref.getRegulatoryList().getFirst();
        regElement.getRegulatoryCountriesRef().add(OTHER_COUNTRY_NODE);
        regElement.getRegulatoryUsagesRef().add(OTHER_USAGE_NODE);

        try (InputStream is = new ClassPathResource(RESSOURCE_PATH).getInputStream()) {
            assertNotNull(is);
            JSONObject json = new JSONObject(new JSONTokener(is));

            try (MockedStatic<MLTextHelper> mlTextHelper = mockStatic(MLTextHelper.class)) {
                mlTextHelper.when(() -> MLTextHelper.getI18NMessage(ProductDataJSONService.MESSAGE_NOTLISTED_ING))
                        .thenReturn(INGREDIENT_NOT_LISTED);
                mlTextHelper.when(() -> MLTextHelper.getI18NMessage(ProductDataJSONService.MESSAGE_COUNTRY_USAGE_PAIR_NOT_FOUND))
                        .thenReturn(COUNTRY_USAGE_PAIR_NOT_FOUND);

                ProductData result = service.newProductDataFromJson(ref, json);

                // the 2 explicit reqCtrl entries from the JSON + 3 alerts for each uncovered pair
                assertEquals(5, result.getReqCtrlList().size());

                List<RequirementListDataItem> alerts = result.getReqCtrlList().stream()
                        .filter(i -> i.getReqMlMessage() != null && i.getReqMlMessage().equals(COUNTRY_USAGE_PAIR_NOT_FOUND))
                        .toList();
                assertEquals(3, alerts.size());

                List<String> expectedCodes = List.of(COUNTRY_CODE + " - " + OTHER_USAGE_CODE,
                        OTHER_COUNTRY_CODE + " - " + USAGE_CODE, OTHER_COUNTRY_CODE + " - " + OTHER_USAGE_CODE);

                List<String> actualCodes = alerts.stream()
                        .map(RequirementListDataItem::getRegulatoryCode)
                        .toList();
                assertTrue(actualCodes.containsAll(expectedCodes));

                for (RequirementListDataItem alert : alerts) {
                    assertEquals(RequirementType.Tolerated, alert.getReqType());
                    assertEquals(RequirementDataType.Specification, alert.getReqDataType());
                    assertEquals(FORMULATION_CHAIN_ID, alert.getFormulationChainId());
                    assertNull(alert.getCharact());
                    assertEquals(2, alert.getSources().size());
                }

                // spot-check one alert's sources resolve to the correct country/usage nodes
                RequirementListDataItem otherCoutryOtherUsageP = alerts.stream()
                        .filter(a -> a.getRegulatoryCode().equals(OTHER_COUNTRY_CODE + " - " + OTHER_USAGE_CODE))
                        .findFirst().orElseThrow();
                assertTrue(otherCoutryOtherUsageP.getSources().stream().anyMatch(n -> n.getId().equals(OTHER_COUNTRY_ID)));
                assertTrue(otherCoutryOtherUsageP.getSources().stream().anyMatch(n -> n.getId().equals(OTHER_USAGE_ID)));
            }
        }
    }
}