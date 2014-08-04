/* beCPG : Get the export search templates */
//TODO Move that to ASYNC see project-list-toolbar.js
//Add search widget
function getExportSearchTpls(datatype) {
	var reportTpls = [];
	if (datatype != null && datatype.length > 0) {
		try {
			var uri = "/becpg/report/exportsearch/templates/" + datatype;
			var connector = remote.connect("alfresco");
			var result = connector.get(uri);
			if (result.status.code == status.STATUS_OK && result != "{}") {
				var tpls = eval('(' + result.response + ')');
				reportTpls = tpls.reportTpls;
			}
		} catch (e) {
		}
	}

	return reportTpls;
}
function customSearchMain() {
	// datatype
	var datatype = null;
	if (model.searchQuery !== null && model.searchQuery.length !== 0) {
		var formJson = jsonUtils.toObject(model.searchQuery);

		if (formJson.length !== 0) {
			datatype = formJson.datatype;
		}
	}

	model.exportSearchTpls = getExportSearchTpls(datatype);

	model.showWused = datatype != null;

	var metadatas = config.scoped["Search"]["metadata"].childrenMap["show"];
	var metadataFields = "";
	for (var i = 0, metadata; i < metadatas.size(); i++) {
		if (metadataFields.length > 0) {
			metadataFields += ",";
		}
		metadata = metadatas.get(i);
		metadataFields += metadata.attributes["id"].replace(":", "_");
	}

	for (var i = 0; i < model.widgets.length; i++) {
		if (model.widgets[i].id == "Search") {
			model.widgets[i].name = "beCPG.custom.Search";
			model.widgets[i].options.metadataFields = metadataFields;
		}
	}
}

function advSearch() {
	// fetch the request params required by the advanced search component
	// template
	var siteId = (page.url.templateArgs["site"] != null) ? page.url.templateArgs["site"] : "";

	// get the search forms from the config
	var formsElements = config.scoped["AdvancedSearch"]["advanced-search"].getChildren("forms");
	var searchForms = [];

	searchForms.push({
		id : "default",
		type : null,
		label : msg.get("search.form.types.label"),
		description : msg.get("search.form.types.description")
	});

	for (var x = 0, forms; x < formsElements.size(); x++) {
		forms = formsElements.get(x).childrenMap["form"];

		for (var i = 0, form, formId, label, desc; i < forms.size(); i++) {
			form = forms.get(i);

			// get optional attributes and resolve label/description text
			formId = form.attributes["id"];

			label = form.attributes["label"];
			if (label == null) {
				label = form.attributes["labelId"];
				if (label != null) {
					label = msg.get(label);
				}
			}

			desc = form.attributes["description"];
			if (desc == null) {
				desc = form.attributes["descriptionId"];
				if (desc != null) {
					desc = msg.get(desc);
				}
			}

			// create the model object to represent the form definition
			searchForms.push({
				id : formId ? formId : "search",
				type : form.value,
				label : label ? label : form.value,
				description : desc ? desc : ""
			});
		}
	}

	// Prepare the model
	var repoconfig = config.scoped['Search']['search'].getChildValue('repository-search');
	// config override can force repository search on/off
    model.searchRepo = ((page.url.args["r"] == "true") || repoconfig == "always") && repoconfig != "none";
 	model.siteId = siteId;
	model.searchForms = searchForms;

	// Widget instantiation metadata...
	var advancedSearch = {
		id : "AdvancedSearch",
		name : "Alfresco.AdvancedSearch",
		options : {
			siteId : model.siteId,
			savedQuery : (page.url.args.q != null) ? page.url.args.q : "",
			searchRepo : model.searchRepo,
			searchForms : model.searchForms
		}
	};
	model.widgets.push(advancedSearch);
}

advSearch();
customSearchMain();
