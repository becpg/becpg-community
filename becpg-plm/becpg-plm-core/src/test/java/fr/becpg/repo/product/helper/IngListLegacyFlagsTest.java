package fr.becpg.repo.product.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.namespace.QName;
import org.junit.Test;

import fr.becpg.model.PLMModel;

@SuppressWarnings("deprecation")
public class IngListLegacyFlagsTest {

    @Test
    public void detectsALegacyPropertyWhateverItsValue() {
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(PLMModel.PROP_INGLIST_IS_GMO, false);

        assertTrue(IngListLegacyFlags.containsLegacyProperty(properties));
    }

    @Test
    public void ignoresPropertiesWithoutLegacyBooleans() {
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(PLMModel.PROP_INGLIST_FLAGS, new ArrayList<>(Arrays.asList("NANO")));

        assertFalse(IngListLegacyFlags.containsLegacyProperty(properties));
    }

    @Test
    public void migrateTurnsTrueBooleansIntoFlags() {
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(PLMModel.PROP_INGLIST_IS_SUPPORT, true);
        properties.put(PLMModel.PROP_INGLIST_IS_GMO, true);
        properties.put(PLMModel.PROP_INGLIST_IS_IONIZED, false);

        Map<QName, Serializable> migrated = IngListLegacyFlags.migrate(properties);

        assertEquals(Arrays.asList("SUPPORT", "GMO"), migrated.get(PLMModel.PROP_INGLIST_FLAGS));
    }

    @Test
    public void migrateRemovesTheLegacyBooleans() {
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(PLMModel.PROP_INGLIST_IS_PROCESSING_AID, true);
        properties.put(ContentModel.PROP_NAME, "line");

        Map<QName, Serializable> migrated = IngListLegacyFlags.migrate(properties);

        assertFalse(IngListLegacyFlags.containsLegacyProperty(migrated));
        assertEquals("line", migrated.get(ContentModel.PROP_NAME));
    }

    @Test
    public void migrateKeepsTheFlagsNotCoveredByLegacyBooleans() {
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(PLMModel.PROP_INGLIST_FLAGS, new ArrayList<>(Arrays.asList("IMPURITY", "NANO")));
        properties.put(PLMModel.PROP_INGLIST_IS_GMO, true);

        Map<QName, Serializable> migrated = IngListLegacyFlags.migrate(properties);

        assertEquals(Arrays.asList("IMPURITY", "NANO", "GMO"), migrated.get(PLMModel.PROP_INGLIST_FLAGS));
    }

    @Test
    public void migrateRemovesAFlagSetToFalseByALegacyWriter() {
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(PLMModel.PROP_INGLIST_FLAGS, new ArrayList<>(Arrays.asList("GMO", "NANO")));
        properties.put(PLMModel.PROP_INGLIST_IS_GMO, false);

        Map<QName, Serializable> migrated = IngListLegacyFlags.migrate(properties);

        assertEquals(Arrays.asList("NANO"), migrated.get(PLMModel.PROP_INGLIST_FLAGS));
    }

    @Test
    public void migrateLeavesTheSourceMapUnchanged() {
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(PLMModel.PROP_INGLIST_IS_GMO, true);

        IngListLegacyFlags.migrate(properties);

        assertTrue(properties.containsKey(PLMModel.PROP_INGLIST_IS_GMO));
    }

    @Test
    public void toLegacyValuesRebuildsEveryBoolean() {
        Map<QName, Boolean> legacyValues = IngListLegacyFlags.toLegacyValues(Arrays.asList("PROCESSING_AID", "NANO"));

        assertEquals(4, legacyValues.size());
        assertTrue(legacyValues.get(PLMModel.PROP_INGLIST_IS_PROCESSING_AID));
        assertFalse(legacyValues.get(PLMModel.PROP_INGLIST_IS_SUPPORT));
    }

    @Test
    public void withLegacyValuesExposesTheBooleansForReports() {
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(PLMModel.PROP_INGLIST_FLAGS, new ArrayList<>(Arrays.asList("GMO")));

        Map<QName, Serializable> withLegacy = IngListLegacyFlags.withLegacyValues(properties);

        assertEquals(Boolean.TRUE, withLegacy.get(PLMModel.PROP_INGLIST_IS_GMO));
        assertEquals(Boolean.FALSE, withLegacy.get(PLMModel.PROP_INGLIST_IS_IONIZED));
    }

    @Test
    public void withLegacyValuesKeepsAValueStoredOnALineNotYetMigrated() {
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(PLMModel.PROP_INGLIST_IS_SUPPORT, true);

        Map<QName, Serializable> withLegacy = IngListLegacyFlags.withLegacyValues(properties);

        assertEquals(Boolean.TRUE, withLegacy.get(PLMModel.PROP_INGLIST_IS_SUPPORT));
    }

    @Test
    public void withLegacyValuesAcceptsASingleFlagValue() {
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(PLMModel.PROP_INGLIST_FLAGS, "IONIZED");

        Map<QName, Serializable> withLegacy = IngListLegacyFlags.withLegacyValues(properties);

        assertEquals(Boolean.TRUE, withLegacy.get(PLMModel.PROP_INGLIST_IS_IONIZED));
    }
}
