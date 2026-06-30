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
package fr.becpg.repo.web.scripts.remote.ai;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;

/**
 * <p>AI-shaped data dictionary ({@code /becpg/remote/ai/dictionary}).</p>
 * <p>
 * Returns a flat, AI-friendly view of the <b>per-tenant</b> content model so becpg-ai can resolve field/type
 * names from intent and validate them (anti-hallucination), including the tenant's <b>custom</b> fields. This
 * is the server-driven replacement for the static field/type embeddings baked into the becpg-ai image.
 * <ul>
 *   <li>{@code ?type=ns:type} → the type title plus its {@code fields} (name, title, dataType, mandatory) and
 *       {@code associations} (name, title, targetType).</li>
 *   <li>no parameter → the list of entity {@code types} (subtypes of {@code bcpg:entityV2}).</li>
 * </ul>
 * Cheap by design: everything comes from the compiled (in-memory, cached) data model — no Solr/DB queries.
 *
 * @author matthieu
 */
public class AiDictionaryWebScript extends AbstractWebScript {

	private DictionaryService dictionaryService;
	private EntityDictionaryService entityDictionaryService;
	private NamespaceService namespaceService;

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {
		String typeParam = req.getParameter("type");
		try {
			JSONObject out = new JSONObject();
			if ((typeParam == null) || typeParam.isBlank()) {
				out.put("types", buildTypes());
				out.put("fields", buildAllFields());
			} else {
				out.put("type", typeParam);
				ClassDefinition classDef = dictionaryService.getClass(QName.createQName(typeParam, namespaceService));
				if (classDef != null) {
					String title = classDef.getTitle(dictionaryService);
					if (title != null) {
						out.put("title", title);
					}
					out.put("fields", buildFields(classDef));
					out.put("associations", buildAssociations(classDef));
				}
			}

			resp.setContentType("application/json");
			resp.setContentEncoding("UTF-8");
			resp.getWriter().write(out.toString());
			resp.setStatus(Status.STATUS_OK);
		} catch (JSONException e) {
			throw new WebScriptException("Cannot serialize AI dictionary: " + e.getMessage());
		}
	}

	private JSONArray buildTypes() throws JSONException {
		JSONArray types = new JSONArray();
		for (QName type : entityDictionaryService.getSubTypes(BeCPGModel.TYPE_ENTITY_V2)) {
			JSONObject entry = new JSONObject();
			entry.put("name", type.toPrefixString(namespaceService));
			ClassDefinition classDef = dictionaryService.getClass(type);
			if (classDef != null) {
				String title = classDef.getTitle(dictionaryService);
				if (title != null) {
					entry.put("title", title);
				}
			}
			types.put(entry);
		}
		return types;
	}

	/**
	 * The full default field set of a type: its own properties plus the properties of its default aspects
	 * (mirrors how {@code JsonSchemaEntityVisitor} builds an entity schema). In-memory model lookups only.
	 */
	private Map<QName, PropertyDefinition> allProperties(ClassDefinition classDef) {
		Map<QName, PropertyDefinition> properties = new LinkedHashMap<>(classDef.getProperties());
		for (AspectDefinition aspect : classDef.getDefaultAspects()) {
			properties.putAll(aspect.getProperties());
		}
		return properties;
	}

	private Map<QName, AssociationDefinition> allAssociations(ClassDefinition classDef) {
		Map<QName, AssociationDefinition> associations = new LinkedHashMap<>(classDef.getAssociations());
		for (AspectDefinition aspect : classDef.getDefaultAspects()) {
			associations.putAll(aspect.getAssociations());
		}
		return associations;
	}

	/**
	 * Distinct union of properties (type + default aspects) across all entity types (subtypes of
	 * {@code bcpg:entityV2}), deduplicated by name — the lexical corpus for intent → field resolution.
	 * In-memory (compiled model), so cheap; bounded by the data model and cached client-side per tenant.
	 */
	private JSONArray buildAllFields() throws JSONException {
		Map<String, JSONObject> byName = new LinkedHashMap<>();
		for (QName type : entityDictionaryService.getSubTypes(BeCPGModel.TYPE_ENTITY_V2)) {
			ClassDefinition classDef = dictionaryService.getClass(type);
			if (classDef == null) {
				continue;
			}
			for (PropertyDefinition prop : allProperties(classDef).values()) {
				String name = prop.getName().toPrefixString(namespaceService);
				if (!byName.containsKey(name)) {
					byName.put(name, fieldEntry(prop, false));
				}
			}
		}
		return new JSONArray(byName.values());
	}

	private JSONArray buildFields(ClassDefinition classDef) throws JSONException {
		JSONArray fields = new JSONArray();
		for (PropertyDefinition prop : allProperties(classDef).values()) {
			fields.put(fieldEntry(prop, true));
		}
		return fields;
	}

	private JSONObject fieldEntry(PropertyDefinition prop, boolean withMandatory) throws JSONException {
		JSONObject entry = new JSONObject();
		entry.put("name", prop.getName().toPrefixString(namespaceService));
		String title = prop.getTitle(dictionaryService);
		if (title != null) {
			entry.put("title", title);
		}
		if (prop.getDataType() != null) {
			entry.put("dataType", prop.getDataType().getName().toPrefixString(namespaceService));
		}
		if (withMandatory && prop.isMandatory()) {
			entry.put("mandatory", true);
		}
		return entry;
	}

	private JSONArray buildAssociations(ClassDefinition classDef) throws JSONException {
		JSONArray associations = new JSONArray();
		for (AssociationDefinition assoc : allAssociations(classDef).values()) {
			JSONObject entry = new JSONObject();
			entry.put("name", assoc.getName().toPrefixString(namespaceService));
			String title = assoc.getTitle(dictionaryService);
			if (title != null) {
				entry.put("title", title);
			}
			if (assoc.getTargetClass() != null) {
				entry.put("targetType", assoc.getTargetClass().getName().toPrefixString(namespaceService));
			}
			associations.put(entry);
		}
		return associations;
	}

	/**
	 * <p>Setter for the field <code>dictionaryService</code>.</p>
	 *
	 * @param dictionaryService a {@link org.alfresco.service.cmr.dictionary.DictionaryService} object
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	/**
	 * <p>Setter for the field <code>entityDictionaryService</code>.</p>
	 *
	 * @param entityDictionaryService a {@link fr.becpg.repo.entity.EntityDictionaryService} object
	 */
	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}

	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

}
