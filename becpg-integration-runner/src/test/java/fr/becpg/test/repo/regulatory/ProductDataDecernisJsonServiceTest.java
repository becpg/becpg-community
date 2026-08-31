package fr.becpg.test.repo.regulatory;

import fr.becpg.repo.product.data.ing.IngItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.regulatory.decernis.ProductDataDecernisJsonService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ProductDataDecernisJsonServiceTest {

    private static final NodeRef ING_NODE = new NodeRef("workspace://SpacesStore/1103b3df-f293-404a-83b3-dff293204aed");
    private IngListDataItem ingListDataItem;
    private IngItem ingItem;

    @Before
    public void setUp() {
        ingListDataItem = new IngListDataItem();
        ingListDataItem.setIng(ING_NODE);
        ingItem = new IngItem();
    }

    @Test
    public void extractRid_returnsDecernisIdWhenValidPrefix() {
        ingItem.setRegulatoryCode("DECERNIS_42");
        String rid = ProductDataDecernisJsonService.extractRid(ingListDataItem, ingItem);
        assertEquals("42", rid);
    }

    @Test
    public void extractRid_returnsDecernisIdWhenMixedCodes() {
        ingItem.setRegulatoryCode("BECPG_123,DECERNIS_42");
        String rid = ProductDataDecernisJsonService.extractRid(ingListDataItem, ingItem);
        assertEquals("42", rid);
    }

    @Test
    public void extractRid_filtersEmptyTokensWithLeadingTrailingConsecutiveCommas() {
        ingItem.setRegulatoryCode(",,DECERNIS_42,,,");
        String rid = ProductDataDecernisJsonService.extractRid(ingListDataItem, ingItem);
        assertEquals("42", rid);
    }

    @Test
    public void extractRid_returnsCodeWhenNoDecernisPrefix() {
        ingItem.setRegulatoryCode("42");
        String rid = ProductDataDecernisJsonService.extractRid(ingListDataItem, ingItem);
        assertEquals("42", rid);
    }

    @Test
    public void extractRid_returnsNullWhenPrefixWithoutValue() {
        ingItem.setRegulatoryCode("DECERNIS_");
        String rid = ProductDataDecernisJsonService.extractRid(ingListDataItem, ingItem);
        assertNull(rid);
    }

    @Test
    public void extractRid_returnsNullWhenDuplicatePrefixes() {
        ingItem.setRegulatoryCode("DECERNIS_DECERNIS_42");
        String rid = ProductDataDecernisJsonService.extractRid(ingListDataItem, ingItem);
        assertNull(rid);
    }

    @Test
    public void extractRid_returnsNullWhenRegulatoryCodeIsNull() {
        ingItem.setRegulatoryCode(null);
        String rid = ProductDataDecernisJsonService.extractRid(ingListDataItem, ingItem);
        assertNull(rid);
    }

    @Test
    public void extractRid_returnsNullWhenRegulatoryCodeIsEmptyOrOnlyCommas() {
        ingItem.setRegulatoryCode(",,,");
        String rid = ProductDataDecernisJsonService.extractRid(ingListDataItem, ingItem);
        assertNull(rid);
    }

    @Test
    public void extractRid_returnsFirstWhenMultipleDecernisCodes() {
        ingItem.setRegulatoryCode("DECERNIS_42,DECERNIS_99");
        String rid = ProductDataDecernisJsonService.extractRid(ingListDataItem, ingItem);
        assertEquals("42", rid);
    }
}
