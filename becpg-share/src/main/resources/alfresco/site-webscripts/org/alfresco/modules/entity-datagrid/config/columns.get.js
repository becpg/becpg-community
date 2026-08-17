<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

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
 * Retrieves the value of the given named argument from the URL arguments
 * 
 * @method getArgument
 * @param argName
 *            The name of the argument to locate
 * @param defValue
 *            The default value to use if the argument could not be found
 * @return The value or null if not found
 */
function getArgument(argName, defValue) {
	var result = args[argName];

	// if we don't have a result and a default has been defined, return that
	// instead
	if (result === null && typeof defValue !== "undefined") {
		result = defValue;
	}

	return result;
}

var preferenceRoot = null;

/**
 * Reads the whole user preference tree, once per request.
 *
 * AlfrescoUtil.getPreferences() re-parses preferences.value on every single call - this web script
 * calls it twice per column - and blows up when no user is bound to the request context (the stock
 * "preferences" root object encodes a null user id). A missing preference must never fail the
 * column definitions, so any failure degrades to "no preference set".
 *
 * @method getPreferenceRoot
 * @return Object the preference tree, never null
 */
function getPreferenceRoot() {
	if (preferenceRoot === null) {
		preferenceRoot = {};
		try {
			var value = preferences.value;
			if (value) {
				preferenceRoot = jsonUtils.toObject(value);
			}
		} catch (e) {
			if (logger.isLoggingEnabled()) {
				logger.log("Unable to read the user preferences, falling back on an empty set: " + e);
			}
		}
	}

	return preferenceRoot;
}

/**
 * Resolves a dotted preference path, same contract as AlfrescoUtil.getPreferences.
 *
 * @method getPreference
 * @param filter
 *            The dotted preference path
 * @return The preference node or null when the path is not set
 */
function getPreference(filter) {
	var node = getPreferenceRoot(), parts = filter.split(".");

	for (var i = 0; i < parts.length; i++) {
		if ((node === null) || (typeof node !== "object") || !node[parts[i]]) {
			return null;
		}
		node = node[parts[i]];
	}

	return node;
}

/**
 * Parses the body of a repository response, without ever throwing.
 *
 * A connector error leaves the response body null and a proxy may answer HTML: eval() then raises a
 * SyntaxError that surfaces as a 500 to the browser, i.e. the "Could not read Data List Column
 * definitions" pop-up, instead of an empty column set.
 *
 * @method parseFormResponse
 * @param response
 *            The repository response
 * @return Object the parsed body or null when it is not JSON
 */
function parseFormResponse(response) {
	var body = null;

	try {
		// Response.toString() returns the raw body, which is null whenever the call failed
		body = response.response;
		if (!body || (body == "null")) {
			if (logger.isLoggingEnabled()) {
				logger.log("Empty form definition response");
			}
			return null;
		}

		return eval('(' + body + ')');
	} catch (e) {
		if (logger.isLoggingEnabled()) {
			logger.log("Unparseable form definition response: " + e + " - " + body);
		}
		return null;
	}
}

function getRequestHeader(headerName) {
	try {
		if (typeof request !== "undefined" && request !== null && typeof request.getHeader === "function") {
			return request.getHeader(headerName);
		}
	} catch (e) {
		// ignore
	}

	try {
		if (typeof headers !== "undefined" && headers !== null) {
			return headers[headerName] || headers[headerName.toLowerCase()];
		}
	} catch (e2) {
		// ignore
	}

	return null;
}

function extractWizardIdFromReferer(referer) {
	if (referer !== null) {
		var wizardIndex = referer.indexOf("/share/page/wizard");
		if (wizardIndex !== -1) {
			var wizardPart = referer.substring(wizardIndex);
			var idIndex = wizardPart.indexOf("id=");
			if (idIndex !== -1) {
				var start = idIndex + 3;
				var end = wizardPart.indexOf("&", start);
				if (end === -1) {
					end = wizardPart.length;
				}
				return wizardPart.substring(start, end);
			}
		}
	}
	return null;
}

function isWizardConfiguredForSkipSecurity(wizardId) {
	if (wizardId !== null) {
		var wizardsConfig = config.scoped["wizard"];
		if (wizardsConfig && wizardsConfig["wizards"] && wizardsConfig["wizards"].childrenMap && wizardsConfig["wizards"].childrenMap["wizard"]) {
			var wizards = wizardsConfig["wizards"].childrenMap["wizard"];
			for (var i = 0; i < wizards.size(); i++) {
				var wizard = wizards.get(i);
				if (wizard && wizard.attributes && wizard.attributes["id"] == wizardId) {
					return wizard.attributes["skipSecurityRules"] == "true";
				}
			}
		}
	}
	return false;
}

/**
 * Finds the configuration for the given item id, if there isn't any
 * configuration for the item null is returned.
 * 
 * @method getFormConfig
 * @param itemId
 *            The id of the item to retrieve for config for
 * @param formId
 *            The id of the specific form to lookup or null to get the default
 *            form
 * @return Object representing the configuration or null
 */
function getFormConfig(itemId, formId, mode, prefixedSiteId, prefixedEntityType, list) {
	var formConfig = null;
	
	// query for configuration for item
	var nodeConfig = config.scoped[itemId];

	if (nodeConfig !== null) {
		// get the forms configuration
		var formsConfig = nodeConfig.forms;

		if (formsConfig !== null) {
			if (formId !== null && formId.length > 0) {
				
				var prefixedList = list ? "-" + list : null;

				// look up the specific form
				if (prefixedEntityType!=null && prefixedEntityType.length > 0 && prefixedSiteId!=null && prefixedSiteId.length > 0 
				    && prefixedList &&  formsConfig.getForm(formId + prefixedEntityType + prefixedSiteId + prefixedList) !== null) {
					formId = formId + prefixedEntityType + prefixedSiteId + prefixedList;
			    } else if (prefixedSiteId!=null && prefixedSiteId.length > 0 && prefixedList && formsConfig.getForm(formId + prefixedSiteId + prefixedList) !== null) {
					formId = formId + prefixedSiteId + prefixedList;
				} else if(prefixedEntityType!=null && prefixedEntityType.length > 0 && prefixedList && formsConfig.getForm(formId + prefixedEntityType + prefixedList) !== null) {
					formId = formId + prefixedEntityType + prefixedList;
				} else if (prefixedList &&  formsConfig.getForm(formId + prefixedList) !== null) {
					formId = formId + prefixedList;
			    } else if (prefixedEntityType!=null && prefixedEntityType.length > 0 && prefixedSiteId!=null && prefixedSiteId.length > 0 
				    &&  formsConfig.getForm(formId + prefixedEntityType + prefixedSiteId) !== null) {
					formId = formId + prefixedEntityType + prefixedSiteId;
			    } else if (prefixedSiteId!=null && prefixedSiteId.length > 0 && formsConfig.getForm(formId + prefixedSiteId) !== null) {
					formId = formId + prefixedSiteId;
				} else if(prefixedEntityType!=null && prefixedEntityType.length > 0 && formsConfig.getForm(formId + prefixedEntityType ) !== null) {
					formId = formId + prefixedEntityType;
				}
				
				formConfig = formsConfig.getForm(formId);
			}

			if(formId == "export" && formConfig === null) { 
				formConfig = getFormConfig(itemId, "datagrid", mode, prefixedSiteId, prefixedEntityType, list);
			} else if(formId == "exportWUsed" && formConfig === null) { 
				formConfig = getFormConfig(itemId, "datagridWUsed", mode, prefixedSiteId, prefixedEntityType, list);
			}

			if (mode == "bulk-edit" && formConfig === null) {
				// look up the specific form
				formConfig = formsConfig.getForm("create");
			}
			

			// drop back to default form if formId config missing
			if (formConfig === null && formId != "taskList") {
				// look up the default form
				formConfig = formsConfig.defaultForm;
			}
		}
	}

	return formConfig;
}

/**
 * Returns the list of fields configured to be visible for the given mode. If
 * this method returns null or an empty list the component should attempt to
 * display ALL known data for the item, unless there are fields configured to be
 * hidden.
 * 
 * @method getVisibleFields
 * @param mode
 *            The mode the form is rendering, 'view', 'edit' or 'create'
 * @param formConfig
 *            The form configuration, maybe null
 * @return Array of field names or null
 */
function getVisibleFields(mode, formConfig) {
	var visibleFields = null;

	if (formConfig !== null) {
		// get visible fields for the current mode
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

	if (logger.isLoggingEnabled()) {
		var listOfVisibleFields = visibleFields;
		if (visibleFields !== null) {
			listOfVisibleFields = "[" + visibleFields.join(",") + "]";
		}
		logger.log("Fields configured to be visible for " + mode + " mode = " + listOfVisibleFields);
	}

	return visibleFields;
}

/**
 * Creates an Object to represent the body of the POST request to send to the
 * form service.
 * 
 * @method createPostBody
 * @param itemKind
 *            The kind of item
 * @param itemId
 *            The id of the item
 * @param visibleFields
 *            List of fields to get data for
 * @param formConfig
 *            The form configuration object
 * @return Object representing the POST body
 */
function createPostBody(itemKind, itemId, visibleFields, formConfig, mode, entityNodeRef, skipSecurityRules) {
	var postBody = {};

	postBody.itemKind = itemKind;
	postBody.itemId = itemId.replace(":/", "");
	if(entityNodeRef!=null){
		postBody.entityNodeRef = entityNodeRef;
	}
	postBody.formId = formConfig.id;
	if (skipSecurityRules === true) {
		postBody.skipSecurityRules = true;
	}

	if (visibleFields !== null) {
		// create list of fields to show and a list of
		// those fields to 'force'
		var postBodyFields = [];
		var postBodyForcedFields = [];
		var fieldId = null;
		for (var f = 0; f < visibleFields.length; f++) {
			fieldId = visibleFields[f];
			if (fieldId.indexOf("dataList_") < 0 && fieldId.indexOf("entity_") < 0) {

				postBodyFields.push(fieldId);
				if (formConfig.isFieldForced(fieldId) || mode == "datagrid-prefs") {
					postBodyForcedFields.push(fieldId);
				} else  {
					var preferences = getPreference("fr.becpg.formulation.dashlet.custom.datagrid-prefs" + "." + itemId.replace(":", "_") + "." + fieldId.replace(":", "_"));

					if(existInPref(preferences) && isChecked(preferences)){
						postBodyForcedFields.push(fieldId);
					}
					
				}

			}
		}

		postBody.fields = postBodyFields;
		if (postBodyForcedFields.length > 0) {
			postBody.force = postBodyForcedFields;
		}
	}

	return postBody;
}

/**
 * Main entrypoint for component webscript logic
 * 
 * @method main
 */
function main() {
	var itemType = getArgument("itemType"), list = getArgument("list"), formId = getArgument("formId")
	, mode = getArgument("mode"), noCache = getArgument("noCache"), siteId = getArgument("siteId")
	, entityType = getArgument("entityType"), entityNodeRef = getArgument("entityNodeRef");

	/*
	 * withControls=true adds the field's <control> to every column.
	 *
	 * Opt-in, and deliberately per call rather than per instance: without the
	 * argument this web script answers exactly what it answered before, byte for
	 * byte, so Share's own datagrid is untouched. It exists because the columns
	 * carry the field's type but not the control the form configuration declares
	 * for it, and a client that renders its own grid - the supplier portal - has
	 * no other way to learn that bcpg:ingListIngTypes is filled from
	 * becpg/autocomplete/targetassoc/associations/bcpg:ingTypeItem. The
	 * information already crosses the wire for a form; it did not for a column.
	 */
	var withControls = getArgument("withControls") == "true";

	var skipSecurityRules = false;
	var referer = getRequestHeader("Referer");
	if (referer !== null && referer.indexOf("/share/page/wizard") !== -1) {
		var wizardId = extractWizardIdFromReferer(referer);
		if (wizardId !== null && isWizardConfiguredForSkipSecurity(wizardId)) {
			skipSecurityRules = true;
		}
	}

	
	// This column set is per user: the column preferences and the security rules both filter it.
	// It must never be stored by a shared cache, and "no-cache" is the only thing the web script
	// framework can express - it has no "private" branch (WebScriptServletResponse.setCache), and
	// omitting "public" is not enough: a CDN or a corporate proxy stores a bare "max-age" response
	// all the same and serves it to another user, which was verified on dev on 2026-08-17 (an
	// answer built for an authenticated user came back on a request carrying no session at all).
	cache.neverCache = true;

	var prefixedSiteId = siteId ? "-" + siteId : "";
	
	var prefixedEntityType = "";
	
	if(entityType && entityType.includes(":")){
		prefixedEntityType = "-"+entityType.split(":")[1];
	}
	
	// pass form ui model to FTL
	model.columns = getColumns(itemType, list, formId, mode, prefixedSiteId, prefixedEntityType, entityNodeRef, null, skipSecurityRules, withControls);

}

function getColumns(itemType, list, formIdArgs, mode, prefixedSiteId, prefixedEntityType, entityNodeRef , nestedPrefKey, skipSecurityRules, withControls) {
	
	var columns = [], defaultColumns = [], ret = [];

	if (itemType != null && itemType.length > 0) {
		// get the config for the form
		// beCPG : WUsed

		var formId = mode == "bulk-edit" ? "bulk-edit" : "datagrid";

		if (formIdArgs == null || formIdArgs.length == 0) {
			if (list != null && list.indexOf("WUsed") == 0) {
				formId = "datagridWUsed";
			} else if (list == "sub-datagrid") {
				formId = "sub-datagrid";
			}
		} else {
			if(formIdArgs == "export" && list != null && list.indexOf("WUsed") == 0){
				formId = "exportWUsed";
			} else {
				formId = formIdArgs;
			}
		}

		var formConfig = getFormConfig(itemType, formId, mode, prefixedSiteId, prefixedEntityType, list);
			
		if (formConfig != null) {
			
		
			// get the configured visible fields
			var visibleFields = getVisibleFields(mode == "bulk-edit" ? "edit" : "view", formConfig);

			// build the JSON object to send to the server
			var postBody = createPostBody("type", itemType, visibleFields, formConfig, mode, entityNodeRef, skipSecurityRules);


			// make remote call to service
			var connector = remote.connect("alfresco");
		
			
			var json = connector.post("/becpg/form", jsonUtils.toJSONString(postBody), "application/json");

			if (logger.isLoggingEnabled()) {
				logger.log("json = " + json);
			}

			if (json.status == 401) {
				status.setCode(json.status, "Not authenticated");
				return;
			}

			var override = false;

			// if we got a successful response attempt to render the form
			if (json.status == 200) {
				// only a 200 carries a JSON body: parsing anything else (empty body on a connector
				// error, HTML error page from a proxy) throws and turns into a 500 for the client
				var formModel = parseFormResponse(json);
				if (formModel === null) {
					status.setCode(502, "Invalid form definition response");
					return;
				}
				columns = formModel.fields;
				override = formModel.override;
			} else {
				if (logger.isLoggingEnabled()) {
					logger.log("error = " + json.status + " " + json);
				}
				columns = [];
			}
			
           	// get default fields
			if(mode == "datagrid-prefs"){			
				postBody.force = [];
				var jsonDefaultFields = connector.post("/becpg/form", jsonUtils.toJSONString(postBody), "application/json");
				var defaultFieldsModel = jsonDefaultFields.status == 200 ? parseFormResponse(jsonDefaultFields) : null;
				if (defaultFieldsModel !== null) {
					defaultColumns = defaultFieldsModel.fields;
				}
			}

			
			if(override){
				var nestedFields= [];
				for (var i in visibleFields) {
					var fieldId = visibleFields[i];
					if (fieldId.indexOf("dataList_") == 0
					|| fieldId.indexOf("entity_") == 0
					) {
					  nestedFields.push(fieldId);
					}	
				}
				
				visibleFields = [];
				for (var j in columns) {
					visibleFields.push(columns[j].name);
				}
				
				 visibleFields.push.apply(visibleFields, nestedFields);
				
			}

			for (var i in visibleFields) {

				  var fieldId = visibleFields[i], name, column;
				  var prefKey =  itemType.replace(":", "_") + "."  + fieldId.replace(":", "_");
				   if(nestedPrefKey){
					    prefKey =nestedPrefKey+"_"+fieldId.replace(":", "_");
				   } 
				   
					var preferences = getPreference("fr.becpg.formulation.dashlet.custom.datagrid-prefs." +prefKey);

				if (fieldId.indexOf("dataList_") == 0) {

					name = fieldId.replace("dataList_", "");
					column = {
						type: "dataList",
						name: name,
						"dataType": "nested"
					};
					
				 if(!override){

						if (formConfig.fields[fieldId].label != null || formConfig.fields[fieldId].labelId != null) {
							column.label = formConfig.fields[fieldId].label != null ? formConfig.fields[fieldId].label
								: formConfig.fields[fieldId].labelId;
						}
	
						if (formConfig.fields[fieldId].getHelpText() != null) {
							column.help = formConfig.fields[fieldId].getHelpText();
						}
					}


					column.columns = getColumns(name + "", "sub-datagrid", null, mode, null, null, null, null, skipSecurityRules, withControls);

					ret.push(column);

				} else if (fieldId.indexOf("entity_") == 0) {
					var splitted = fieldId.replace("entity_", "").replace("_asColumn","").split("_");
					name = splitted[0];
					column = {
						type: "entity",
						name: name,
						"dataType": (fieldId.indexOf("_asColumn") > 0) ? "nested_column" : "nested"
					};

					if(!override){
						if (formConfig.fields[fieldId].label != null || formConfig.fields[fieldId].labelId != null) {
							column.label = formConfig.fields[fieldId].label != null ? formConfig.fields[fieldId].label
								: formConfig.fields[fieldId].labelId;
						}
	
						if (formConfig.fields[fieldId].getHelpText() != null) {
							column.help = formConfig.fields[fieldId].getHelpText();
						}
					}
					
					var subPrefKey =  itemType.replace(":", "_") + "."  + name.replace(":", "_");
					
					if (splitted[1].includes("@")) {
						var formSplitted = splitted[1].split("@");
						column.columns = getColumns(formSplitted[0] + "", "sub-datagrid", formSplitted[1] + "", mode, null, null, null, subPrefKey, skipSecurityRules, withControls);
					} else if (formIdArgs != null && formIdArgs.length > 0) {
						column.columns = getColumns(splitted[1] + "", "sub-datagrid", "sub-datagrid-" + formIdArgs, mode, null, null, null, subPrefKey, skipSecurityRules, withControls);
					} else {
						column.columns = getColumns(splitted[1] + "", "sub-datagrid", null, mode, null, null, null, subPrefKey, skipSecurityRules, withControls);
					}

					ret.push(column);

				} else {

					for (var j in columns) {
						if (columns[j].name == fieldId) {
							if(!override){
								
								if (formConfig.fields[fieldId].label != null || formConfig.fields[fieldId].labelId != null) {
									columns[j].label = formConfig.fields[fieldId].label != null ? formConfig.fields[fieldId].label
										: formConfig.fields[fieldId].labelId;
								} 
	
								if (formConfig.fields[fieldId].getHelpText() != null) {
									columns[j].help = formConfig.fields[fieldId].getHelpText();
								}
								
								
								columns[j].readOnly = formConfig.fields[fieldId].isReadOnly();

								if (withControls) {
									columns[j].control = getControl(formConfig.fields[fieldId], itemType);
								}

							}	
							
							if (mode == "datagrid-prefs") {

								if(existInPref(preferences)){
									columns[j].checked = isChecked(preferences);
								} else {
									if(isDefault(fieldId, defaultColumns) || formConfig.isFieldForced(fieldId)) {
										columns[j].checked = true;
									} else {
										columns[j].checked = false;
									}
									
								}
								
							} else {
								if (mode != "bulk-edit"  && existInPref(preferences) && !isChecked(preferences)) {
									columns[j].label = "datasource";
								}
							}

							ret.push(columns[j]);
						}
					}
				}

			}

		}
	}

	return ret;
}



/**
 * The <control> a field declares, as a plain object the FTL can serialise.
 *
 * The template names the widget - autocomplete.ftl, textfield.ftl - and the
 * parameters carry what makes it work, first of all the "ds" of an autocomplete.
 * The values arrive as configured, whitespace included: a <control-param> written
 * on its own line carries the indentation of the XML, and trimming it here is the
 * only place where the caller cannot get it wrong.
 *
 * The datagrid form is asked first, then the item's DEFAULT form. That order is
 * not a convenience: a datagrid form lists the columns to show and almost never
 * repeats the control, while the default form is where the picker is described -
 * bcpg:ingListIngTypes declares its "ds" there and nowhere else. Share resolves
 * the same way when it edits a row, so a client rendering its own grid sees what
 * Share's editor sees.
 *
 * @method getControl
 * @param field The form configuration field of the resolved form
 * @param itemType The item type, to fall back on its default form
 * @return Object {template, params} or null when no form declares a control
 */
function getControl(field, itemType) {
	var control = readControl(field);
	if (control == null) {
		control = readControl(getDefaultFormField(itemType, field != null ? field.id : null));
	}
	return control;
}

/**
 * @method readControl
 * @param field a form configuration field, or null
 * @return Object {template, params} or null when the field declares nothing
 */
function readControl(field) {
	var control = field != null ? field.control : null;
	if (control == null) {
		return null;
	}

	var params = {}, hasParam = false;
	// `getParams()` answers a ControlParam[], each carrying its own name and
	// value - not a Map, and not something for..in can walk under Rhino.
	var declared = control.params;
	if (declared != null) {
		for (var k = 0; k < declared.length; k++) {
			var param = declared[k];
			if (param != null && param.name != null) {
				params["" + param.name] = trimValue(param.value);
				hasParam = true;
			}
		}
	}

	var template = control.template != null ? "" + control.template : null;
	if (template == null && !hasParam) {
		return null;
	}

	return { template: template, params: params };
}

/**
 * A control-param value written on its own line in the XML carries the
 * indentation with it.
 *
 * @method trimValue
 * @param value
 * @return String
 */
function trimValue(value) {
	return value != null ? ("" + value).replace(/^\s+|\s+$/g, "") : "";
}

/**
 * The same field, as the item's default form describes it.
 *
 * @method getDefaultFormField
 * @param itemType prefixed type, e.g. bcpg:ingList
 * @param fieldId the field to look up
 * @return the field configuration, or null
 */
function getDefaultFormField(itemType, fieldId) {
	if (itemType == null || fieldId == null) {
		return null;
	}
	var nodeConfig = config.scoped[itemType];
	var formsConfig = nodeConfig !== null ? nodeConfig.forms : null;
	var defaultForm = formsConfig !== null ? formsConfig.defaultForm : null;
	return defaultForm !== null && defaultForm.fields != null ? defaultForm.fields[fieldId] : null;
}

function isChecked(preferences) {
	if (existInPref(preferences)) {
		return preferences.checked;
	}

	return false;
}

function existInPref(preferences) {
	if (typeof (preferences) !== "undefined" && preferences != null && typeof (preferences.checked) === "boolean") {
		return true;
	}

	return false;
}

function isDefault(fieldId, defaultColums) {
	for (var i in defaultColums) {
		if (defaultColums[i].name == fieldId) {
			return true;
		}
	}

	return false;
}



main();
