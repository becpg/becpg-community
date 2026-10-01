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
import java.util.Objects;

import org.alfresco.service.namespace.QName;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.constraints.IngListFlag;

/**
 * Keeps the deprecated boolean properties of an ingredient line (bcpg:ingListIsGMO,
 * bcpg:ingListIsIonized, bcpg:ingListIsProcessingAid, bcpg:ingListIsSupport) in sync with the
 * multi-valued bcpg:ingListFlags property.
 *
 * <p>Both representations stay stored, so that forms, reports, connectors and exports written for
 * the booleans keep working. The flags that have no boolean counterpart (impurity, nano) live in
 * bcpg:ingListFlags only.</p>
 *
 * <p>Conflict rule: when the flags changed they win and rewrite the booleans; when only the booleans
 * changed they rewrite their four flags; when neither changed, the flags win if the property is
 * stored, the booleans otherwise (a line written before the flags existed).</p>
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
     * Computes the properties to write so that the flags and the legacy booleans agree again.
     *
     * @param before the properties before the update, empty for a creation
     * @param after the properties after the update
     * @return the properties to set, empty when both representations already agree
     */
    public static Map<QName, Serializable> synchronize(Map<QName, Serializable> before, Map<QName, Serializable> after) {
        List<String> flags = resolveFlags(before, after);
        Map<QName, Serializable> changes = new HashMap<>();
        if (!flags.equals(IngListFlag.normalize(readFlags(after)))) {
            changes.put(PLMModel.PROP_INGLIST_FLAGS, new ArrayList<>(flags));
        }
        for (Map.Entry<QName, Boolean> legacy : toLegacyValues(flags).entrySet()) {
            if (!Objects.equals(legacy.getValue(), after.get(legacy.getKey()))) {
                changes.put(legacy.getKey(), legacy.getValue());
            }
        }
        return changes;
    }

    /**
     * Rebuilds the value each legacy boolean property has for the given flags.
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

    private static List<String> resolveFlags(Map<QName, Serializable> before, Map<QName, Serializable> after) {
        List<String> flagsAfter = IngListFlag.normalize(readFlags(after));
        if (!flagsAfter.equals(IngListFlag.normalize(readFlags(before)))) {
            return flagsAfter;
        }
        if (legacyChanged(before, after) || !after.containsKey(PLMModel.PROP_INGLIST_FLAGS)) {
            return applyLegacyValues(flagsAfter, after);
        }
        return flagsAfter;
    }

    private static boolean legacyChanged(Map<QName, Serializable> before, Map<QName, Serializable> after) {
        for (QName legacyProperty : LEGACY_PROPERTIES.keySet()) {
            if (!Objects.equals(before.get(legacyProperty), after.get(legacyProperty))) {
                return true;
            }
        }
        return false;
    }

    private static List<String> applyLegacyValues(List<String> flags, Map<QName, Serializable> properties) {
        List<String> merged = new ArrayList<>(flags);
        for (Map.Entry<QName, IngListFlag> legacy : LEGACY_PROPERTIES.entrySet()) {
            if (properties.containsKey(legacy.getKey())) {
                merged.remove(legacy.getValue().name());
                if (Boolean.TRUE.equals(properties.get(legacy.getKey()))) {
                    merged.add(legacy.getValue().name());
                }
            }
        }
        return IngListFlag.normalize(merged);
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
}
