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
package fr.becpg.repo.entity.remote.extractor;

import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.json.JSONObject;

/**
 * The model class that declares a field of the remote JSON schema: a type or an aspect, with its
 * prefixed name.
 *
 * @param kind {@link #KIND_TYPE} or {@link #KIND_ASPECT}
 * @param name the prefixed name of the declaring class, e.g. <code>bcpg:productGeoOriginAspect</code>
 */
public record DefinedBy(String kind, String name) {

    /** Kind of a field declared by a type. */
    public static final String KIND_TYPE = "type";

    /** Kind of a field declared by an aspect. */
    public static final String KIND_ASPECT = "aspect";

    private static final String JSON_KIND = "kind";

    private static final String JSON_NAME = "name";

    /**
     * Describes the class that declares a field.
     *
     * @param declaringClass the container class of a property, or the source class of an association
     * @param prefixResolver the resolver used to prefix the class name
     * @return the declaring class description
     */
    public static DefinedBy of(ClassDefinition declaringClass, NamespacePrefixResolver prefixResolver) {
        String kind = declaringClass.isAspect() ? KIND_ASPECT : KIND_TYPE;
        return new DefinedBy(kind, declaringClass.getName().toPrefixString(prefixResolver));
    }

    /**
     * Converts the description to its JSON schema form.
     *
     * @return a JSON object holding <code>kind</code> and <code>name</code>
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put(JSON_KIND, kind);
        json.put(JSON_NAME, name);
        return json;
    }
}
