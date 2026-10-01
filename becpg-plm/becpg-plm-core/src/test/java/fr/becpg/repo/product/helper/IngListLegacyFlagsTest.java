package fr.becpg.repo.product.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.namespace.QName;
import org.junit.Test;

import fr.becpg.model.PLMModel;

@SuppressWarnings("deprecation")
public class IngListLegacyFlagsTest {

    @Test
    public void tickingAFlagFillsItsBoolean() {
        Map<QName, Serializable> before = lineWith(List.of(), false, false, false, false);
        Map<QName, Serializable> after = lineWith(List.of("GMO"), false, false, false, false);

        Map<QName, Serializable> changes = IngListLegacyFlags.synchronize(before, after);

        assertEquals(Map.of(PLMModel.PROP_INGLIST_IS_GMO, Boolean.TRUE), changes);
    }

    @Test
    public void tickingABooleanFillsItsFlag() {
        Map<QName, Serializable> before = lineWith(List.of("NANO"), false, false, false, false);
        Map<QName, Serializable> after = lineWith(List.of("NANO"), false, false, false, true);

        Map<QName, Serializable> changes = IngListLegacyFlags.synchronize(before, after);

        assertEquals(Map.of(PLMModel.PROP_INGLIST_FLAGS, new ArrayList<>(List.of("SUPPORT", "NANO"))), changes);
    }

    @Test
    public void untickingABooleanRemovesItsFlagOnly() {
        Map<QName, Serializable> before = lineWith(List.of("GMO", "IMPURITY"), true, false, false, false);
        Map<QName, Serializable> after = lineWith(List.of("GMO", "IMPURITY"), false, false, false, false);

        Map<QName, Serializable> changes = IngListLegacyFlags.synchronize(before, after);

        assertEquals(Map.of(PLMModel.PROP_INGLIST_FLAGS, new ArrayList<>(List.of("IMPURITY"))), changes);
    }

    @Test
    public void theFlagsWinWhenBothSidesChanged() {
        Map<QName, Serializable> before = lineWith(List.of(), false, false, false, false);
        Map<QName, Serializable> after = lineWith(List.of("IONIZED"), true, false, false, false);

        Map<QName, Serializable> changes = IngListLegacyFlags.synchronize(before, after);

        assertEquals(Boolean.FALSE, changes.get(PLMModel.PROP_INGLIST_IS_GMO));
        assertEquals(Boolean.TRUE, changes.get(PLMModel.PROP_INGLIST_IS_IONIZED));
        assertFalse(changes.containsKey(PLMModel.PROP_INGLIST_FLAGS));
    }

    @Test
    public void nothingIsWrittenWhenBothSidesAgree() {
        Map<QName, Serializable> line = lineWith(List.of("PROCESSING_AID", "NANO"), false, false, true, false);

        assertTrue(IngListLegacyFlags.synchronize(line, line).isEmpty());
    }

    @Test
    public void aLineWrittenBeforeTheFlagsGetsItsFlagsFromTheBooleans() {
        Map<QName, Serializable> line = new HashMap<>(lineWith(List.of(), true, false, false, true));
        line.remove(PLMModel.PROP_INGLIST_FLAGS);

        Map<QName, Serializable> changes = IngListLegacyFlags.synchronize(line, line);

        assertEquals(Map.of(PLMModel.PROP_INGLIST_FLAGS, new ArrayList<>(List.of("SUPPORT", "GMO"))), changes);
    }

    @Test
    public void aLineWhoseBooleansWereRemovedGetsThemBackFromItsFlags() {
        Map<QName, Serializable> line = new HashMap<>();
        line.put(PLMModel.PROP_INGLIST_FLAGS, new ArrayList<>(List.of("SUPPORT")));

        Map<QName, Serializable> changes = IngListLegacyFlags.synchronize(line, line);

        assertEquals(Boolean.TRUE, changes.get(PLMModel.PROP_INGLIST_IS_SUPPORT));
        assertEquals(Boolean.FALSE, changes.get(PLMModel.PROP_INGLIST_IS_GMO));
        assertEquals(4, changes.size());
    }

    @Test
    public void anImportCreatingALineWithBooleansOnlyFillsTheFlags() {
        Map<QName, Serializable> created = new HashMap<>();
        created.put(PLMModel.PROP_INGLIST_IS_PROCESSING_AID, true);

        Map<QName, Serializable> changes = IngListLegacyFlags.synchronize(Collections.emptyMap(), created);

        assertEquals(new ArrayList<>(List.of("PROCESSING_AID")), changes.get(PLMModel.PROP_INGLIST_FLAGS));
        assertEquals(Boolean.FALSE, changes.get(PLMModel.PROP_INGLIST_IS_GMO));
    }

    @Test
    public void aFormSavedWithNoBoxTickedMeansNoFlag() {
        Map<QName, Serializable> before = lineWith(List.of("GMO"), true, false, false, false);
        Map<QName, Serializable> after = lineWith(Arrays.asList(""), true, false, false, false);

        Map<QName, Serializable> changes = IngListLegacyFlags.synchronize(before, after);

        assertEquals(Map.of(PLMModel.PROP_INGLIST_IS_GMO, Boolean.FALSE), changes);
    }

    @Test
    public void toLegacyValuesRebuildsEveryBoolean() {
        Map<QName, Boolean> legacyValues = IngListLegacyFlags.toLegacyValues(Arrays.asList("PROCESSING_AID", "NANO"));

        assertEquals(4, legacyValues.size());
        assertTrue(legacyValues.get(PLMModel.PROP_INGLIST_IS_PROCESSING_AID));
        assertFalse(legacyValues.get(PLMModel.PROP_INGLIST_IS_SUPPORT));
    }

    private static Map<QName, Serializable> lineWith(List<String> flags, boolean gmo, boolean ionized, boolean processingAid, boolean support) {
        Map<QName, Serializable> line = new HashMap<>();
        line.put(PLMModel.PROP_INGLIST_FLAGS, new ArrayList<>(flags));
        line.put(PLMModel.PROP_INGLIST_IS_GMO, gmo);
        line.put(PLMModel.PROP_INGLIST_IS_IONIZED, ionized);
        line.put(PLMModel.PROP_INGLIST_IS_PROCESSING_AID, processingAid);
        line.put(PLMModel.PROP_INGLIST_IS_SUPPORT, support);
        return line;
    }
}
