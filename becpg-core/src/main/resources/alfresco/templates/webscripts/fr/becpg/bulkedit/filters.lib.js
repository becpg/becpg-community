
var Filters = {
	/**
	 * Create filter parameters based on input parameters
	 * 
	 * @method getFilterParams
	 * @param filter
	 *            {string} Required filter
	 * @param parsedArgs
	 *            {object} Parsed arguments object literal
	 * @return {object} Object literal containing parameters to be used in
	 *         Lucene search
	 */
	getFilterParams : function Filter_getFilterParams(filter, parsedArgs) {

		var searchQuery = "";


		// Common types and aspects to filter from the UI
		searchQuery += " -TYPE:\"cm:systemfolder\""
				+ " -@cm\\:lockType:READ_ONLY_LOCK"
				+ " -ASPECT:\"bcpg:compositeVersion\" AND -ASPECT:\"eco:simulationEntityAspect\"";

		var nodeRef = parsedArgs.nodeRef;
		if (nodeRef != null && nodeRef.length > 0) {
			var node = search.findNode(nodeRef);
			if (node != null) {
				searchQuery += " +PATH:\"" + node.qnamePath + "//*\"";
			}
		}

		var params = parsedArgs.searchParams, formData = params.query, criteria = [], datatype = "";

		if (formData !== null && formData.length !== 0) {
			var formJson = jsonUtils.toObject(formData);

			for ( var p in formJson) {
				var propValue = formJson[p];
				if (propValue.length !== 0) {
					criteria[p] = propValue;
				}
			}
			if (formJson.length !== 0) {
				datatype = formJson.datatype;
			}

		}

		if (parsedArgs.itemType != null && parsedArgs.itemType.length > 0) {
			datatype = parsedArgs.itemType;
			searchQuery += " +TYPE:\"" + parsedArgs.itemType + "\"";
		}

		var filterParams = {
			query : searchQuery,
			limitResults : null,
			datatype : datatype,
			criteria : criteria,
			params : params,
			sort : [ {
				column : "@cm:name",
				ascending : true
			} ],
			language : "lucene",
			templates : null
		};

		// Max returned results specified?
		var argMax = args.max;
		if ((argMax !== null) && !isNaN(argMax)) {
			filterParams.limitResults = argMax;
		}

		return filterParams;
	}
};
