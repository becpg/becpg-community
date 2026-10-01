/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.product.helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.namespace.QName;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.constraints.IngListFlag;

/**
 * Bridges the deprecated boolean properties of an ingredient line (bcpg:ingListIsGMO,
 * bcpg:ingListIsIonized, bcpg:ingListIsProcessingAid, bcpg:ingListIsSupport) and the
 * multi-valued bcpg:ingListFlags property that replaces them.
 *
 * <p>The migration patch and the ingredient line policy fold the legacy values into the flags, so
 * that imports and remote clients still writing the booleans keep working; report extraction
 * rebuilds the booleans from the flags, so that existing report templates keep working.</p>
 *
 * @author matthieu
 */
public final class IngListLegacyFlags {

    @SuppressWarnings("deprecation")
    private static final Map<QName, IngListFlag> LEGACY_PROPERTIES = createLegacyProperties();

    private IngListLegacyFlags() {
    }

    @SuppressWarnings("deprecation")
    private static Map<QName, IngListFlag> createLegacyProperties() {
        Map<QName, IngListFlag> legacyProperties = new LinkedHashMap<>();
        legacyProperties.put(PLMModel.PROP_INGLIST_IS_GMO, IngListFlag.GMO);
        legacyProperties.put(PLMModel.PROP_INGLIST_IS_IONIZED, IngListFlag.IONIZED);
        legacyProperties.put(PLMModel.PROP_INGLIST_IS_PROCESSING_AID, IngListFlag.PROCESSING_AID);
        legacyProperties.put(PLMModel.PROP_INGLIST_IS_SUPPORT, IngListFlag.SUPPORT);
        return Collections.unmodifiableMap(legacyProperties);
    }

    /**
     * Tells whether a property map still holds one of the deprecated boolean properties.
     *
     * @param properties the node properties
     * @return true when at least one legacy boolean property is present, whatever its value
     */
    public static boolean containsLegacyProperty(Map<QName, Serializable> properties) {
        for (QName legacyProperty : LEGACY_PROPERTIES.keySet()) {
            if (properties.containsKey(legacyProperty)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Folds the legacy boolean properties into bcpg:ingListFlags: a true value adds the flag, any other
     * value removes it, and the legacy property itself is dropped.
     *
     * @param properties the node properties, left unchanged
     * @return a copy of the properties without the legacy booleans and with the resulting flags
     */
    public static Map<QName, Serializable> migrate(Map<QName, Serializable> properties) {
        Map<QName, Serializable> migrated = new HashMap<>(properties);
        List<String> flags = new ArrayList<>(readFlags(properties));
        for (Map.Entry<QName, IngListFlag> legacy : LEGACY_PROPERTIES.entrySet()) {
            if (migrated.containsKey(legacy.getKey())) {
                Serializable value = migrated.remove(legacy.getKey());
                applyLegacyValue(flags, legacy.getValue(), Boolean.TRUE.equals(value));
            }
        }
        migrated.put(PLMModel.PROP_INGLIST_FLAGS, new ArrayList<>(IngListFlag.normalize(flags)));
        return migrated;
    }

    /**
     * Rebuilds the value each legacy boolean property would have for the given flags.
     *
     * @param flags the flag codes of the line, may be null
     * @return the legacy boolean value per legacy property, in a stable order
     */
    public static Map<QName, Boolean> toLegacyValues(Collection<String> flags) {
        Map<QName, Boolean> legacyValues = new LinkedHashMap<>();
        for (Map.Entry<QName, IngListFlag> legacy : LEGACY_PROPERTIES.entrySet()) {
            legacyValues.put(legacy.getKey(), (flags != null) && flags.contains(legacy.getValue().name()));
        }
        return legacyValues;
    }

    /**
     * Adds the legacy boolean properties rebuilt from bcpg:ingListFlags, so that report templates
     * written before the flags keep reading bcpg:ingListIsGMO and the other booleans. A legacy value
     * still stored on a line not yet migrated is kept as it is.
     *
     * @param properties the ingredient line properties, left unchanged
     * @return a copy of the properties with every legacy boolean property set
     */
    public static Map<QName, Serializable> withLegacyValues(Map<QName, Serializable> properties) {
        Map<QName, Serializable> withLegacy = new HashMap<>(properties);
        for (Map.Entry<QName, Boolean> legacy : toLegacyValues(readFlags(properties)).entrySet()) {
            withLegacy.putIfAbsent(legacy.getKey(), legacy.getValue());
        }
        return withLegacy;
    }

    @SuppressWarnings("unchecked")
    private static Collection<String> readFlags(Map<QName, Serializable> properties) {
        Serializable flags = properties.get(PLMModel.PROP_INGLIST_FLAGS);
        if (flags instanceof Collection<?>) {
            return (Collection<String>) flags;
        }
        if (flags instanceof String flag) {
            return List.of(flag);
        }
        return Collections.emptyList();
    }

    private static void applyLegacyValue(List<String> flags, IngListFlag flag, boolean enabled) {
        flags.remove(flag.name());
        if (enabled) {
            flags.add(flag.name());
        }
    }
}
