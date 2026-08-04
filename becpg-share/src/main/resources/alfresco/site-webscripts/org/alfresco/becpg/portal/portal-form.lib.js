/*******************************************************************************
 *  Copyright (C) 2010-2026 beCPG.
 *
 *  This file is part of beCPG
 *
 *  beCPG is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  beCPG is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License along with beCPG.
 *   If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

/**
 * Shared form-resolution helpers for the beCPG portal endpoints.
 *
 * WHY THIS FILE EXISTS
 * --------------------
 * Share owns the form configuration and its resolution cascade
 * (formId + entityType + siteId + list, then fallback to the default form); the
 * repository only describes the fields that Share asks about, through
 * POST /becpg/form. The reference implementation of that dance is
 *   .../modules/entity-datagrid/config/columns.get.js
 * which is the datagrid's own endpoint and must not be touched.
 *
 * The functions below are a faithful extraction of that logic so the portal
 * endpoint can reuse it verbatim instead of reimplementing the cascade — which
 * would guarantee divergence the first time a client overrides a form.
 *
 * DELIBERATE DIFFERENCE with columns.get.js: no AlfrescoUtil.getPreferences call.
 * Column preferences are a Share-internal, per-user notion with no meaning for an
 * external supplier, and depending on them would force this endpoint to run
 * inside an authenticated Share session.
 *
 * Additive: new file, nothing existing is modified.
 */

/**
 * Finds the form configuration for an item, applying the beCPG resolution
 * cascade. Extracted from columns.get.js::getFormConfig.
 *
 * CHOOSING THE LOOKUP KEY — this is the subtlety that makes `form` steps work.
 *
 * `config.scoped[x]` passes `x` straight to `ConfigService.getConfig(x)` as the
 * evaluation context (see ConfigModel.ScopedConfigMap in spring-webscripts). So the
 * key decides which evaluator can fire:
 *
 *   - a type QName ("bcpg:nutList") matches `evaluator="model-type"`;
 *   - a nodeRef string ("workspace://SpacesStore/…") matches `evaluator="node-type"`,
 *     because NodeMetadataBasedEvaluator.applies() only accepts a String matching
 *     `.+://.+/.+`, then resolves the node's type through /api/metadata.
 *
 * beCPG declares its datalist forms under BOTH evaluators (becpg-plm-form-config.xml:
 * 64 model-type blocks, 63 node-type ones) but its PRODUCT types only under
 * `node-type`. Passing the type QName therefore finds datalist forms and misses
 * product forms — which is exactly the symptom the portal saw.
 *
 * Hence `lookupKey`: the caller passes the nodeRef when it has one, and the type
 * QName otherwise.
 *
 * @param lookupKey the config evaluation key: a nodeRef (preferred) or a type QName
 * @param itemId the type QName the form belongs to, used for logging
 * @param formId the requested form id, or null for the default form
 * @param mode view | edit | create
 * @param prefixedSiteId "-<siteId>" or ""
 * @param prefixedEntityType "-<entityTypeLocalName>" or ""
 * @param list the datalist name, or null
 * @return the form configuration element, or null
 */
function portalGetFormConfig(lookupKey, itemId, formId, mode, prefixedSiteId, prefixedEntityType, list) {
	var formConfig = null;

	// `config.scoped[...]` yields `undefined` — not `null` — for an unknown condition,
	// so both must be tested: `undefined !== null` is true in JavaScript and a bare
	// null-check would let an unknown type through and then fail on `.forms`.
	var nodeConfig = config.scoped[lookupKey];

	if (nodeConfig !== null && nodeConfig !== undefined) {
		var formsConfig = nodeConfig.forms;

		if (formsConfig !== null && formsConfig !== undefined) {
			if (formId !== null && formId.length > 0) {

				var prefixedList = list ? "-" + list : null;

				// Most specific first, exactly like columns.get.js.
				if (prefixedEntityType != null && prefixedEntityType.length > 0 && prefixedSiteId != null && prefixedSiteId.length > 0
						&& prefixedList && formsConfig.getForm(formId + prefixedEntityType + prefixedSiteId + prefixedList) !== null) {
					formId = formId + prefixedEntityType + prefixedSiteId + prefixedList;
				} else if (prefixedSiteId != null && prefixedSiteId.length > 0 && prefixedList
						&& formsConfig.getForm(formId + prefixedSiteId + prefixedList) !== null) {
					formId = formId + prefixedSiteId + prefixedList;
				} else if (prefixedEntityType != null && prefixedEntityType.length > 0 && prefixedList
						&& formsConfig.getForm(formId + prefixedEntityType + prefixedList) !== null) {
					formId = formId + prefixedEntityType + prefixedList;
				} else if (prefixedList && formsConfig.getForm(formId + prefixedList) !== null) {
					formId = formId + prefixedList;
				} else if (prefixedEntityType != null && prefixedEntityType.length > 0 && prefixedSiteId != null && prefixedSiteId.length > 0
						&& formsConfig.getForm(formId + prefixedEntityType + prefixedSiteId) !== null) {
					formId = formId + prefixedEntityType + prefixedSiteId;
				} else if (prefixedSiteId != null && prefixedSiteId.length > 0 && formsConfig.getForm(formId + prefixedSiteId) !== null) {
					formId = formId + prefixedSiteId;
				} else if (prefixedEntityType != null && prefixedEntityType.length > 0
						&& formsConfig.getForm(formId + prefixedEntityType) !== null) {
					formId = formId + prefixedEntityType;
				}

				formConfig = formsConfig.getForm(formId);
			}

			if (formConfig === null) {
				formConfig = formsConfig.defaultForm;
			}
		}
	}

	return formConfig;
}

/**
 * Visible field names for a mode. Extracted from columns.get.js::getVisibleFields.
 *
 * @param mode view | edit | create
 * @param formConfig the form configuration, may be null
 * @return an array of field names, or null when the component should show everything
 */
function portalGetVisibleFields(mode, formConfig) {
	var visibleFields = null;

	if (formConfig !== null) {
		switch (mode) {
			case "view":
				visibleFields = formConfig.visibleViewFieldNames;
				break;
			case "edit":
				visibleFields = formConfig.visibleEditFieldNames;
				break;
			case "create":
				visibleFields = formConfig.visibleCreateFieldNames;
				break;
			default:
				visibleFields = formConfig.visibleViewFieldNames;
				break;
		}
	}

	return visibleFields;
}

/**
 * Builds the POST body for /becpg/form. Extracted from
 * columns.get.js::createPostBody, minus the user-preference lookup.
 *
 * Nested pseudo-fields (dataList_*, entity_*) are filtered out: the repository
 * does not know them, they are a Share rendering convention. They are returned
 * separately by portalResolveDefinition so the caller keeps the information.
 *
 * @return { body: Object, nested: String[] }
 */
function portalCreatePostBody(itemKind, itemId, visibleFields, formConfig, entityNodeRef, skipSecurityRules) {
	var postBody = {};
	var nested = [];

	postBody.itemKind = itemKind;
	postBody.itemId = ("" + itemId).replace(":/", "");
	if (entityNodeRef != null) {
		postBody.entityNodeRef = entityNodeRef;
	}
	postBody.formId = formConfig.id;
	if (skipSecurityRules === true) {
		postBody.skipSecurityRules = true;
	}

	if (visibleFields !== null) {
		var fields = [];
		var forced = [];
		for (var f = 0; f < visibleFields.length; f++) {
			var fieldId = visibleFields[f];
			if (("" + fieldId).indexOf("dataList_") === 0 || ("" + fieldId).indexOf("entity_") === 0) {
				nested.push("" + fieldId);
				continue;
			}
			fields.push(fieldId);
			if (formConfig.isFieldForced(fieldId)) {
				forced.push(fieldId);
			}
		}
		postBody.fields = fields;
		if (forced.length > 0) {
			postBody.force = forced;
		}
	}

	return { body: postBody, nested: nested };
}

/**
 * Reads the appearance (sets and tabs) out of the Share form configuration.
 *
 * The Alfresco FormConfigElement exposes sets through a bean property whose exact
 * shape has varied across versions, so every access is guarded: when the sets
 * cannot be read the caller still gets a usable definition, with the set ids
 * derived from the fields themselves.
 *
 * @param formConfig the form configuration
 * @return { sets: Object[], tabs: Object[] }
 */
function portalReadAppearance(formConfig) {
	var sets = [];
	var tabs = [];

	if (formConfig === null) {
		return { sets: sets, tabs: tabs };
	}

	try {
		var rawSets = formConfig.sets;
		if (rawSets != null) {
			for (var i = 0; i < rawSets.length; i++) {
				var set = rawSets[i];
				if (set == null) {
					continue;
				}
				var label = null;
				try {
					label = set.label != null ? set.label : (set.labelId != null ? msg.get(set.labelId) : null);
				} catch (eLabel) {
					label = null;
				}
				sets.push({
					id: "" + set.setId,
					label: label != null ? "" + label : "",
					appearance: set.appearance != null ? "" + set.appearance : "panel",
					parent: set.parent != null ? "" + set.parent : null,
					template: set.template != null ? "" + set.template : null
				});
			}
		}
	} catch (e) {
		// Sets are optional: the portal falls back to one set per distinct field set id.
		if (logger.isLoggingEnabled()) {
			logger.log("portalReadAppearance: sets unavailable (" + e + ")");
		}
	}

	return { sets: sets, tabs: tabs };
}

/**
 * Resolves a complete field definition for one wizard step.
 *
 * Share resolves WHICH fields (the cascade above) and HOW they look (appearance);
 * the repository describes the fields (data type, constraints, control, labels).
 *
 * @param itemType the type QName the step edits (bcpg:rawMaterial, bcpg:nutList…)
 * @param formId the requested form id (supplier, datagrid-supplier-nut…)
 * @param mode view | edit | create
 * @param list the datalist name without any @N suffix, or null
 * @param prefixedSiteId "-<siteId>" or ""
 * @param prefixedEntityType "-<entityTypeLocalName>" or ""
 * @param entityNodeRef the entity being edited, or null
 * @param skipSecurityRules whether the wizard asks to bypass the security rules
 * @param alfTicket the caller Alfresco ticket, forwarded to the repository
 * @param useNodeRefLookup true for an entity form (node-type lookup), false for a datalist
 * @return the definition object, or null when no configuration matches
 */
function portalResolveDefinition(itemType, formId, mode, list, prefixedSiteId, prefixedEntityType, entityNodeRef, skipSecurityRules, alfTicket, useNodeRefLookup) {
	if (itemType == null || ("" + itemType).length === 0) {
		return null;
	}

	// Lookup key selection — see portalGetFormConfig for the full reasoning.
	//
	// Only an ENTITY form (a `form` step) may be resolved through the nodeRef: the
	// nodeRef's type IS the item type in that case, so the `node-type` evaluator fires on
	// the right condition.
	//
	// A datalist step must NOT use it: the nodeRef it carries is the entity's (a raw
	// material), whereas the item type is the list item (bcpg:nutList). Passing the
	// nodeRef there would silently resolve the *entity's* form — a wrong definition, not
	// an empty one, which is far worse. Those steps rely on the `model-type` declarations,
	// which beCPG provides for every list type.
	var lookupKey = "" + itemType;
	if (useNodeRefLookup === true && entityNodeRef != null && ("" + entityNodeRef).indexOf("://") !== -1) {
		lookupKey = "" + entityNodeRef;
	}

	var formConfig = portalGetFormConfig(lookupKey, itemType, formId, mode, prefixedSiteId, prefixedEntityType, list);

	// The `node-type` lookup resolves the node — and walks its parent types — through the
	// repository, which the evaluator can only do with a *Share session*. This webscript is
	// called by the portal with an Alfresco ticket and no such session, so
	// `config.scoped[<nodeRef>]` yields nothing and the entity form came back
	// `no-form-config` on every product.
	//
	// Measured on dev.becpg.fr, `supplier-rawMaterial`: the eight datalist steps resolved 7
	// to 28 fields each — they look up by item type — while `step1` alone resolved none,
	// and it is the only step whose lookup key is the nodeRef.
	//
	// So fall back to the type, which is the lookup the datalist steps already use and the
	// one that reaches beCPG's product form: it is declared on `bcpg:entityV2`
	// (`becpg-form-config.xml`, `model-type`), which the type lookup finds by inheritance.
	// `portalGetFormConfig` then falls back to that config's `defaultForm` when the
	// requested `formId` ("supplier") is not declared — the same "no specific form, use the
	// default one" behaviour Share has. The nodeRef is still tried first, so an instance
	// where the Share session IS available keeps its per-node configuration.
	if ((formConfig === null || formConfig === undefined) && lookupKey !== ("" + itemType)) {
		lookupKey = "" + itemType;
		formConfig = portalGetFormConfig(lookupKey, itemType, formId, mode, prefixedSiteId, prefixedEntityType, list);
	}

	if (formConfig === null || formConfig === undefined) {
		// No form configuration matches. Returning a typed marker rather than null so the
		// portal can tell "not configured" apart from "not applicable" (a documents step).
		return {
			error: "no-form-config",
			itemType: "" + itemType,
			lookupKey: lookupKey,
			requestedFormId: formId != null ? "" + formId : null,
			list: list != null ? "" + list : null,
			fields: [], sets: [], tabs: [], nested: []
		};
	}

	var visibleFields = portalGetVisibleFields(mode === "create" ? "create" : (mode === "view" ? "view" : "edit"), formConfig);
	var post = portalCreatePostBody("type", itemType, visibleFields, formConfig, entityNodeRef, skipSecurityRules);

	// Endpoint selection — this is the crux of calling the repository from here.
	//
	// The "alfresco" endpoint authenticates with the *Share session* ticket
	// (authenticator-id alfresco-ticket) and its connector refuses the call outright,
	// with a 401, when there is no session. The portal has no Share session: it holds an
	// Alfresco ticket of its own.
	//
	// So when the caller forwards a ticket we go through "alfresco-noauth" — the
	// identity-less endpoint Share itself uses for anonymous access (see
	// templates/org/alfresco/quickshare.js) — and authenticate the repository call with
	// that ticket. Consequence, and it is the desired one: /becpg/form runs as the
	// SUPPLIER, so the field-level security rules applied are the supplier's own.
	//
	// With no ticket we keep the session-based endpoint, so this library still works when
	// called from inside an authenticated Share page.
	var formUrl = "/becpg/form";
	var endpoint = "alfresco";
	if (alfTicket != null && ("" + alfTicket).length > 0) {
		endpoint = "alfresco-noauth";
		formUrl = formUrl + "?alf_ticket=" + encodeURIComponent("" + alfTicket);
	}

	var connector = remote.connect(endpoint);
	var response = connector.post(formUrl, jsonUtils.toJSONString(post.body), "application/json");

	// `response.status` is a Status OBJECT, not an int (Surf ScriptRemoteConnector). Its
	// numeric value is `status.code`. Comparing the object to a number silently fails,
	// which is why the naive check reported every call as unauthenticated.
	var statusCode = -1;
	try {
		statusCode = parseInt(response.status.code, 10);
	} catch (eStatus) {
		statusCode = -1;
	}

	if (statusCode === 401 || statusCode === 403) {
		return {
			error: "unauthenticated",
			status: statusCode,
			endpoint: endpoint,
			fields: [], sets: [], tabs: [], nested: post.nested
		};
	}
	if (statusCode !== 200) {
		if (logger.isLoggingEnabled()) {
			logger.log("portalResolveDefinition: /becpg/form returned " + statusCode + " for " + itemType);
		}
		return {
			error: "form-service-error",
			status: statusCode,
			endpoint: endpoint,
			fields: [], sets: [], tabs: [], nested: post.nested
		};
	}

	var formModel = eval('(' + response.response + ')');
	var fields = formModel.fields != null ? formModel.fields : [];

	// Enrich with what only Share knows: the overridden label, the help text, the
	// read-only flag and the owning set.
	for (var i = 0; i < fields.length; i++) {
		var field = fields[i];
		var shareField = null;
		try {
			shareField = formConfig.fields[field.name];
		} catch (eField) {
			shareField = null;
		}
		if (shareField != null) {
			try {
				if (shareField.label != null) {
					field.label = "" + shareField.label;
				} else if (shareField.labelId != null) {
					field.label = "" + msg.get(shareField.labelId);
				}
			} catch (eLbl) { /* keep the repository label */ }
			try {
				if (shareField.getHelpText() != null) {
					field.help = "" + shareField.getHelpText();
				}
			} catch (eHelp) { /* no help configured */ }
			try {
				if (shareField.isReadOnly()) {
					field.readOnly = true;
				}
			} catch (eRo) { /* not configured */ }
			try {
				if (shareField.set != null) {
					field.set = "" + shareField.set;
				}
			} catch (eSet) { /* default set */ }
		}
		if (field.set == null) {
			field.set = "";
		}
	}

	var appearance = portalReadAppearance(formConfig);

	return {
		formId: "" + formConfig.id,
		fields: fields,
		sets: appearance.sets,
		tabs: appearance.tabs,
		nested: post.nested,
		override: formModel.override === true
	};
}
