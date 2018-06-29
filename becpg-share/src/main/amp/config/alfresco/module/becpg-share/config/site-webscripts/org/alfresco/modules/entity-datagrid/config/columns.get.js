<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">
/*******************************************************************************
 *  Copyright (C) 2010-2018 beCPG. 
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
function getFormConfig(itemId, formId, mode) {
	var formConfig = null;

	// query for configuration for item
	var nodeConfig = config.scoped[itemId];

	if (nodeConfig !== null) {
		// get the forms configuration
		var formsConfig = nodeConfig.forms;

		if (formsConfig !== null) {
			if (formId !== null && formId.length > 0) {
				// look up the specific form
				if(formsConfig.getForm(formId + prefixedSiteId) !== null){
					formId += prefixedSiteId;
				}
				formConfig = formsConfig.getForm(formId);
			}

			if (mode == "bulk-edit" && formConfig === null) {
				// look up the specific form
				formConfig = formsConfig.getForm("create");
			}
			
			// drop back to default form if formId config missing
			if (formConfig === null) {
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
function createPostBody(itemKind, itemId, visibleFields, formConfig, mode) {
	var postBody = {};

	postBody.itemKind = itemKind;
	postBody.itemId = itemId.replace(":/", "");

	if (visibleFields !== null) {
		// create list of fields to show and a list of
		// those fields to 'force'
		var postBodyFields = [];
		var postBodyForcedFields = [];
		var fieldId = null;
		for (var f = 0; f < visibleFields.length; f++) {
			fieldId = visibleFields[f];
			if (fieldId.indexOf("dataList_") < 0 && fieldId.indexOf("entity_") < 0) {
				
				//delete field if it's unchecked
				if(isAllowedOrChecked(fieldId, formConfig, "fields") || mode == "datagrid-prefs"){
					postBodyFields.push(fieldId);	
				}
				//add not forced fields if they're checked 
				if(isAllowedOrChecked(fieldId, formConfig, "forcedFields") || mode == "datagrid-prefs"){
					postBodyForcedFields.push(fieldId);
				}
				
			}
		}

		postBody.fields = postBodyFields;
		if (postBodyForcedFields.length > 0) {
			postBody.force = postBodyForcedFields;
		}
	}

	if (logger.isLoggingEnabled()) {
		logger.log("postBody = " + jsonUtils.toJSONString(postBody));
	}

	return postBody;
}

/**
 * Main entrypoint for component webscript logic
 * 
 * @method main
 */
function main() {
	var itemType = getArgument("itemType"), list = getArgument("list"), formId = getArgument("formId"),mode = getArgument("mode"), clearCache = getArgument("clearCache"), siteId = getArgument("siteId");// beCPG
	
	cache.maxAge = 3600; // in seconds
	
	if(clearCache){
		cache.maxAge = 0;
	}
	
	prefixedSiteId = siteId ? "-" + siteId : "";
	
	prefs = "fr.becpg.formulation.dashlet.custom.datagrid-prefs"+"."+itemType.replace(":","_");
	
	// pass form ui model to FTL
	model.columns = getColumns(itemType, list, formId, mode);
	
}

function getColumns(itemType, list, formIdArgs, mode) {
	
	var columns = [], defaultColumns = [], ret = [];

	if (itemType != null && itemType.length > 0) {
		// get the config for the form
		// beCPG : WUsed
		
		var formId = mode == "bulk-edit" ? "bulk-edit" : "datagrid";
		
		if (formIdArgs == null) {
			if (list!=null && list.indexOf("WUsed") == 0) {
				formId = "datagridWUsed";
			} else if (list == "sub-datagrid") {
				formId = "sub-datagrid";
			}
		} else {
			formId = formIdArgs;
		}
		
		var formConfig = getFormConfig(itemType, formId, mode);
		
		// get the configured visible fields
		var visibleFields = getVisibleFields(mode == "bulk-edit" ? "edit" : "view", formConfig);
		
		// build the JSON object to send to the server
		var postBody = createPostBody("type", itemType, visibleFields, formConfig, mode);

		// make remote call to service
		var connector = remote.connect("alfresco");
		var json = connector.post("/api/formdefinitions", jsonUtils.toJSONString(postBody), "application/json");

		if (logger.isLoggingEnabled()) {
			logger.log("json = " + json);
		}

		if (json.status == 401) {
			status.setCode(json.status, "Not authenticated");
			return;
		}

		var formModel = eval('(' + json + ')');

		// if we got a successful response attempt to render the form
		if (json.status == 200) {
			columns = formModel.data.definition.fields;
		} else {
			if (logger.isLoggingEnabled()) {
				logger.log("error = " + formModel.message);
			}
			columns = [];
		}
		
		// get default fields
		if(mode == "datagrid-prefs"){			
			postBody.force = [];
			var jsonDefaultFields = connector.post("/api/formdefinitions", jsonUtils.toJSONString(postBody), "application/json");
			formModel = eval('(' + jsonDefaultFields + ')');
			defaultColumns = formModel.data.definition.fields;
		}

		for ( var i in visibleFields) {

			var fieldId = visibleFields[i];

			if (fieldId.indexOf("dataList_") == 0) {

				var name = fieldId.replace("dataList_", ""), column = {
					type : "dataList",
					name : name,
					label : (formConfig.fields[fieldId].labelId != null ? formConfig.fields[fieldId].labelId : formConfig.fields[fieldId].label),
					"dataType" : "nested"
				};
				column.columns = getColumns(name + "", "sub-datagrid");

				ret.push(column);

			} else if (fieldId.indexOf("entity_") == 0) {
				var splitted = fieldId.replace("entity_", "").split("_");
				var name = splitted[0], column = {
					type : "entity",
					name : name,
					label : (formConfig.fields[fieldId].labelId != null ? formConfig.fields[fieldId].labelId : formConfig.fields[fieldId].label),
					"dataType" : "nested"
				};
				if (formIdArgs != null) {
					column.columns = getColumns(splitted[1] + "", "sub-datagrid", "sub-datagrid-" + formIdArgs);
				} else {
					column.columns = getColumns(splitted[1] + "", "sub-datagrid");
				}

				ret.push(column);

			} else {

				for ( var j in columns) {
					if (columns[j].name == fieldId) {
						if (formConfig.fields[fieldId].label != null || formConfig.fields[fieldId].labelId != null) {
							columns[j].label = formConfig.fields[fieldId].labelId != null ? formConfig.fields[fieldId].labelId
									: formConfig.fields[fieldId].label;
						}

						columns[j].readOnly = formConfig.fields[fieldId].isReadOnly();
						
						if(mode == "datagrid-prefs"){
							columns[j].checked = isAllowedOrChecked(fieldId, formConfig, "popup", defaultColumns);
						}
						
						ret.push(columns[j]);
					}
				}
			}

		}

	}

	return ret;
}



function isAllowedOrChecked(fieldId, formConfig, mode, defaultColumns){
	
	var key = fieldId.replace(":","_");
	var prfs = prefs+"."+key;
	var preferences = AlfrescoUtil.getPreferences(prfs);
	
	// get checked or not 
	if(mode == "popup"){
		if( (isDefault(fieldId, defaultColumns) && !existInPref(preferences))
				||(!existInPref(preferences) && formConfig.isFieldForced(fieldId) ) ){			
			return true;
		}
		else {
			return isChecked(preferences);
		}
	}
	// get allowed or not
	if ( ((!existInPref(preferences) || isChecked(preferences)) && mode == "fields") 
		|| ((isChecked(preferences) || (formConfig.isFieldForced(fieldId) && !existInPref(preferences))) && mode == "forcedFields")){
		
		return true;
	}
	
	return false;
}

function isChecked(preferences){
	if(existInPref(preferences)){
		return  preferences.checked;
	}
	
	return false;
}

function existInPref(preferences){	
	if(typeof(preferences) !== "undefined" && preferences!=null && typeof(preferences.checked) === "boolean" ){
		return true;
	}
	
	return false;
}

function isDefault(fieldId, defaultColums){
	for ( var i in defaultColums) {
		if (defaultColums[i].name == fieldId){			
			return true;
		}
	}
	
	return false;
}

	

main();
