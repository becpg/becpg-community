<import resource="classpath:/alfresco/site-webscripts/org/alfresco/becpg/portal/portal-form.lib.js">
/*
 * NOTE: the <import> directive above must stay on the FIRST line of the file — it is
 * only substituted when it opens the script, exactly as in
 * modules/entity-datagrid/config/columns.get.js.
 */
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
 * Wizard structure and resolved field definitions, as JSON, for the React
 * supplier portal.
 *
 * The portal cannot read the Share configuration itself: it holds an Alfresco
 * ticket, not a Share session, and the effective configuration is the result of
 * beCPG's resolution cascade which only Share knows how to run. This endpoint
 * returns, in ONE call, the wizard steps AND each step's resolved field
 * definition — client overrides included.
 *
 * Consumed by becpg-supplier-portal, lib/adapters/wizardConfig.js.
 * Additive: new file, nothing existing is modified. The Share wizard page
 * (wizard-mgr) is untouched and keeps rendering exactly as before.
 */

function getArgument(argName, defValue) {
	var result = args[argName];
	if ((result === null || result === undefined) && typeof defValue !== "undefined") {
		result = defValue;
	}
	return result;
}

/**
 * Resolves a label from either the literal attribute or its i18n key, the same
 * way wizard-mgr.get.js does.
 */
function getI18N(element, itemName) {
	var label = element.attributes[itemName];
	if (label == null) {
		var labelId = element.attributes[itemName + "Id"];
		if (labelId != null) {
			label = msg.get(labelId);
			if (label == labelId) {
				label = "";
			}
		}
	}
	return label != null ? "" + label : "";
}

/**
 * Whether the caller carries an identity: an authenticated Share session, or the Alfresco ticket
 * the portal forwards. The ticket itself is validated downstream by the repository, which answers
 * 401 on the field definitions when it is not a valid one.
 */
function hasCallerIdentity(alfTicket) {
	if (alfTicket != null && ("" + alfTicket).length > 0) {
		return true;
	}
	if (typeof user === "undefined" || user == null || user.id == null) {
		return false;
	}
	var userId = "" + user.id;
	return userId.length > 0 && userId !== "guest";
}

/** A listId may carry an @N suffix identifying the list instance (surveyList@1). */
function stripListInstance(listId) {
	if (listId == null) {
		return null;
	}
	var value = "" + listId;
	var at = value.indexOf("@");
	return at === -1 ? value : value.substring(0, at);
}

function main() {
	var wizardId = getArgument("wizardId", null);
	var nodeRef = getArgument("nodeRef", null);
	var entityType = getArgument("entityType", null);
	var siteId = getArgument("siteId", null);
	var mode = getArgument("mode", "edit");
	// Forwarded by the portal so the repository call runs as the supplier (see
	// portal-form.lib.js::portalResolveDefinition).
	var alfTicket = getArgument("portalTicket", null);

	if (wizardId == null || ("" + wizardId).length === 0) {
		model.error = "wizardId is required";
		status.setCode(400, model.error);
		return;
	}

	// The descriptor cannot ask Surf to authenticate (see wizard.get.desc.xml), so the caller is
	// checked here: without a Share session nor a ticket, the wizard structure — its steps, its
	// form ids, its list ids — would be readable by anyone.
	if (!hasCallerIdentity(alfTicket)) {
		model.error = "authentication required";
		status.setCode(401, model.error);
		return;
	}

	var prefixedSiteId = siteId ? "-" + siteId : "";
	var prefixedEntityType = "";
	if (entityType && ("" + entityType).indexOf(":") !== -1) {
		prefixedEntityType = "-" + ("" + entityType).split(":")[1];
	}

	var wizardsConfig = config.scoped["wizard"];
	if (!wizardsConfig || !wizardsConfig["wizards"] || !wizardsConfig["wizards"].childrenMap
			|| !wizardsConfig["wizards"].childrenMap["wizard"]) {
		model.error = "no wizard configuration available";
		status.setCode(404, model.error);
		return;
	}

	var wizards = wizardsConfig["wizards"].childrenMap["wizard"];
	var wizard = null;
	for (var i = 0; i < wizards.size(); i++) {
		var candidate = wizards.get(i);
		if (candidate && candidate.attributes && candidate.attributes["id"] == wizardId) {
			wizard = candidate;
			break;
		}
	}

	if (wizard === null) {
		model.error = "unknown wizard: " + wizardId;
		status.setCode(404, model.error);
		return;
	}

	var skipSecurityRules = wizard.attributes["skipSecurityRules"] == "true";

	var descriptor = {
		id: "" + wizardId,
		draft: wizard.attributes["draft"] == "true",
		comments: wizard.attributes["comments"] == "true",
		allSteps: wizard.attributes["allSteps"] == "true",
		readOnly: wizard.attributes["read-only"] == "true",
		enforceTask: wizard.attributes["enforceTask"] == "true",
		skipSecurityRules: skipSecurityRules
	};

	var steps = [];
	var configuredSteps = wizard.childrenMap["step"];

	if (configuredSteps != null) {
		for (var j = 0; j < configuredSteps.size(); j++) {
			var step = configuredSteps.get(j);
			var type = step.attributes["type"] != null ? "" + step.attributes["type"] : "";
			var listId = step.attributes["listId"] != null ? "" + step.attributes["listId"] : null;
			var itemId = step.attributes["itemId"] != null ? "" + step.attributes["itemId"] : null;
			var formId = step.attributes["formId"] != null ? "" + step.attributes["formId"] : null;

			var entry = {
				id: "" + step.attributes["id"],
				type: type,
				label: getI18N(step, "label"),
				title: getI18N(step, "title"),
				formId: formId,
				itemId: itemId,
				// The @N suffix is KEPT here: it identifies the list instance the
				// portal must read and write (multiple questionnaires, spec §2ter.1).
				listId: listId,
				nextStepWebScript: step.attributes["nextStepWebScript"] != null ? "" + step.attributes["nextStepWebScript"] : null,
				nodeRefStepIndex: step.attributes["nodeRefStepIndex"] != null ? "" + step.attributes["nodeRefStepIndex"] : null,
				readOnly: step.attributes["read-only"] == "true",
				definition: null
			};

			// 'documents' steps have no form definition: they are a file deposit.
			if (type === "form" || type === "entityDataList" || type === "survey") {
				// For the config lookup the list must be given WITHOUT its instance suffix.
				var lookupList = type === "form" ? null : stripListInstance(listId);
				var stepMode = entry.readOnly || descriptor.readOnly ? "view" : mode;
				entry.definition = portalResolveDefinition(
					itemId,
					formId != null ? formId : (type === "form" ? "supplier" : "datagrid"),
					stepMode,
					lookupList,
					prefixedSiteId,
					prefixedEntityType,
					nodeRef,
					skipSecurityRules,
					alfTicket,
					// Only an entity form may be resolved through the nodeRef: for a
					// datalist the nodeRef is the entity's, not the list item's.
					type === "form");
			}

			steps.push(entry);
		}
	}

	// Configuration, not data: cacheable, but never shared between users.
	cache.maxAge = 300;
	cache.neverCache = false;
	cache.isPublic = false;
	cache.mustRevalidate = true;

	model.wizard = descriptor;
	model.steps = steps;
}

main();
