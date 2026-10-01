package fr.becpg.repo.product.data.constraints;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class IngListFlagTest {

    @Test
    public void normalizeOrdersKnownFlagsByDeclaration() {
        List<String> normalized = IngListFlag.normalize(Arrays.asList("GMO", "PROCESSING_AID", "NANO"));

        assertEquals(Arrays.asList("PROCESSING_AID", "NANO", "GMO"), normalized);
    }

    @Test
    public void normalizeDropsDuplicates() {
        List<String> normalized = IngListFlag.normalize(Arrays.asList("SUPPORT", "SUPPORT"));

        assertEquals(Arrays.asList("SUPPORT"), normalized);
    }

    @Test
    public void normalizeKeepsUnknownCodesAfterKnownFlags() {
        List<String> normalized = IngListFlag.normalize(Arrays.asList("CUSTOM", "IMPURITY", null));

        assertEquals(Arrays.asList("IMPURITY", "CUSTOM"), normalized);
    }

    @Test
    public void normalizeReturnsAnEmptyListForNull() {
        assertTrue(IngListFlag.normalize(null).isEmpty());
    }

    @Test
    public void normalizeDropsTheEmptyCodeOfAFormSavedWithNoBoxTicked() {
        List<String> normalized = IngListFlag.normalize(Arrays.asList("", " ", "NANO"));

        assertEquals(Arrays.asList("NANO"), normalized);
    }
}
