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
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.web.scripts.remote.AbstractEntityWebScript;

/**
 * <p>AI-oriented entity search ({@code /becpg/remote/ai/search}).</p>
 * <p>
 * Reuses the standard search dispatch ({@link #findEntities}) — simple full-text search via {@code ?query=},
 * or an advanced criteria search via a POST JSON body — but returns a <b>compact</b> result
 * ({@code nodeRef}, {@code type}, {@code name}, {@code code}) instead of full entity serialization, to keep
 * AI token usage low. Search runs as the authenticated user, so tenant and permission filtering are inherited.
 * <p>
 * <b>Optional {@code fields} projection — performance-conscious by design.</b> When {@code ?fields=ns:p1,ns:p2}
 * is supplied, each hit also carries those properties under a {@code fields} object. To keep the cost bounded
 * and predictable we read the node's property map <b>once</b> ({@link org.alfresco.service.cmr.repository.NodeService#getProperties})
 * and return the requested <b>plain property values as-is</b>: we deliberately do <b>not</b> resolve
 * associations, characteristic names or datalists here (that is the expensive part of full serialization).
 * The number of projected fields is capped ({@link #MAX_PROJECTION_FIELDS}) and results are bounded by
 * {@code maxResults}. For richer (assoc-resolved) data the caller fetches the single entity on demand.
 *
 * @author matthieu
 */
public class AiSearchWebScript extends AbstractEntityWebScript {

	private static final int DEFAULT_MAX_RESULTS = 25;

	/** Hard cap on the number of projected fields, to keep the per-hit cost bounded. */
	private static final int MAX_PROJECTION_FIELDS = 20;

	/** {@inheritDoc} */
	@Override
	protected void executeInternal(WebScriptRequest req, WebScriptResponse resp) throws IOException {

		Integer maxResults = intParam(req, PARAM_MAX_RESULTS);
		if ((maxResults == null) || (maxResults <= 0) || (maxResults > maxResultsLimit())) {
			maxResults = DEFAULT_MAX_RESULTS;
		}

		Set<QName> projectedFields = parseFields(req.getParameter("fields"));

		PagingResults<NodeRef> results = findEntities(req, maxResults, true);

		QName codeQName = QName.createQName("bcpg:code", namespaceService);

		try {
			JSONArray items = new JSONArray();
			for (NodeRef nodeRef : results.getPage()) {
				// Single property read per node (properties are loaded/cached with the node) — no extra
				// per-field lookups, no association/datalist resolution.
				Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

				JSONObject item = new JSONObject();
				item.put("nodeRef", nodeRef.toString());
				item.put("type", nodeService.getType(nodeRef).toPrefixString(namespaceService));
				Serializable name = properties.get(ContentModel.PROP_NAME);
				if (name != null) {
					item.put("name", name.toString());
				}
				Serializable code = properties.get(codeQName);
				if (code != null) {
					item.put("code", code.toString());
				}

				if (!projectedFields.isEmpty()) {
					JSONObject fields = new JSONObject();
					for (QName field : projectedFields) {
						Serializable value = properties.get(field);
						if (value != null) {
							fields.put(field.toPrefixString(namespaceService), value.toString());
						}
					}
					if (fields.length() > 0) {
						item.put("fields", fields);
					}
				}

				items.put(item);
			}

			JSONObject out = new JSONObject();
			out.put("count", items.length());
			out.put("items", items);

			resp.setContentType("application/json");
			resp.setContentEncoding("UTF-8");
			resp.getWriter().write(out.toString());
			resp.setStatus(Status.STATUS_OK);
		} catch (JSONException e) {
			throw new WebScriptException("Cannot serialize AI search result: " + e.getMessage());
		}
	}

	/**
	 * Parses a comma-separated {@code fields} parameter into a bounded set of property QNames. Unknown
	 * prefixes / malformed tokens are skipped (best-effort), and the set is capped to keep the cost bounded.
	 */
	private Set<QName> parseFields(String fieldsParam) {
		Set<QName> fields = new LinkedHashSet<>();
		if ((fieldsParam == null) || fieldsParam.isBlank()) {
			return fields;
		}
		for (String token : fieldsParam.split(",")) {
			String trimmed = token.trim();
			if (trimmed.isEmpty()) {
				continue;
			}
			try {
				fields.add(QName.createQName(trimmed, namespaceService));
			} catch (Exception e) {
				// ignore unknown prefix / malformed field, keep the search resilient
			}
			if (fields.size() >= MAX_PROJECTION_FIELDS) {
				break;
			}
		}
		return fields;
	}

}
