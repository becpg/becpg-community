/*******************************************************************************
 *  Copyright (C) 2010-2014 beCPG. 
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
 * BulkEdit component GET method
 */

function main() {

	// get the bulk-edit types from the config
	var bulkEditables = config.scoped["bulk-edit"]["itemTypes"].childrenMap["itemType"];
	var itemTypes = [];
	for (var i = 0, itemType, label; i < bulkEditables.size(); i++) {
		itemType = bulkEditables.get(i);

		// resolve label text
		label = itemType.attributes["label"];
		if (label == null) {
			label = "type." + itemType.attributes["name"].replace(":", "_");
			if (label != null) {
				label = msg.get(label);
			}
		}

		// create the model object to represent the sort field definition
		itemTypes.push({
			name : itemType.attributes["name"],
			label : label ? label : itemType.attributes["name"],
			formId : itemType.attributes["formId"] ? itemType.attributes["formId"] : "bulk-edit",
			editSelectedFormId : itemType.attributes["editSelectedFormId"] ? itemType.attributes["editSelectedFormId"] : "create"
		});
	}

	model.itemTypes = itemTypes;

	model.nodeRef = (page.url.args["nodeRef"] !== null) ? page.url.args["nodeRef"] : null;
	model.entityNodeRefs =(page.url.args["nodeRefs"] != null) ? page.url.args["nodeRefs"] : null;

	// fetch the request params required by the search component template
	var siteId = (page.url.templateArgs["site"] != null) ? page.url.templateArgs["site"] : "";
	var siteTitle = null;
	if (siteId.length != 0) {
		// look for request scoped cached site title
		siteTitle = context.properties["site-title"];
		if (siteTitle == null) {
			// Call the repository for the site profile
			var json = remote.call("/api/sites/" + siteId);
			if (json.status == 200) {
				// Create javascript objects from the repo response
				var obj = eval('(' + json + ')');
				if (obj) {
					siteTitle = (obj.title.length != 0) ? obj.title : obj.shortName;
				}
			}
		}
	}

	// Prepare the model
	var repoconfig = config.scoped['Search']['search'].getChildValue('repository-search');
	model.siteId = siteId;
	model.siteTitle = (siteTitle != null ? siteTitle : "");
	model.searchTerm = (page.url.args["t"] != null) ? page.url.args["t"] : "";
	model.searchTag = (page.url.args["tag"] != null) ? page.url.args["tag"] : "";
	model.searchSort = (page.url.args["s"] != null) ? page.url.args["s"] : "";
	// config override can force repository search on/off
	model.searchRepo = ((page.url.args["r"] == "true") || repoconfig == "always") && repoconfig != "none";
	model.searchAllSites = (page.url.args["a"] == "true" || siteId.length == 0);

	// Advanced search forms based json query
	model.searchQuery = (page.url.args["q"] != null) ? page.url.args["q"] : "";

	var bulkEdit = {
		id : "BulkEdit",
		name : "beCPG.component.BulkEdit",
		options : {
			siteId : model.siteId,
			siteTitle : model.siteTitle,
			initialSearchTerm : model.searchTerm,
			initialSearchTag : model.searchTag,
			initialSearchAllSites : model.searchAllSites,
			initialSearchRepository : model.searchRepo,
			initialSort : model.searchSort,
			searchQuery : model.searchQuery,
			nodeRef : model.nodeRef ,
			entityNodeRefs : model.entityNodeRefs,
			usePagination : true
		}
	}

	model.widgets = [ bulkEdit ];

}

main();
