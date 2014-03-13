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
				+ " AND -@cm\\:lockType:READ_ONLY_LOCK"
				+ " AND -ASPECT:\"bcpg:compositeVersion\"";

		var nodeRef = parsedArgs.nodeRef;
		if (nodeRef != null && nodeRef.length > 0) {
			var node = search.findNode(nodeRef);
			if (node != null) {
				searchQuery += " AND +PATH:\"" + node.qnamePath + "//*\"";
			}
		}
		
		var params = parsedArgs.searchParams, formData = params.query, criteria = [], datatype = "";
		
		// sort field - expecting field to in one of the following formats:
	    //  - short QName form such as: cm:name
	    //  - pseudo cm:content field starting with "." such as: .size
	    //  - any other directly supported search field such as: TYPE
	    var sortColumns = [];
	    var sort = params.sort;
	    if (sort != null && sort.length != 0)
	    {
	       var asc = true;
	       var separator = sort.indexOf("|");
	       if (separator != -1)
	       {
	          sort = sort.substring(0, separator);
	          asc = (sort.substring(separator + 1) == "true");
	       }
	       var column;
	       if (sort.charAt(0) == '.')
	       {
	          // handle pseudo cm:content fields
	          column = "@{http://www.alfresco.org/model/content/1.0}content" + sort;
	       }
	       else if (sort.indexOf(":") != -1)
	       {
	          // handle attribute field sort
	          column = "@" + utils.longQName(sort);
	       }
	       else
	       {
	          // other sort types e.g. TYPE
	          column = sort;
	       }
	       sortColumns.push(
	       {
	          column: column,
	          ascending: asc
	       });
	    } else {
	    	sortColumns.push(
	    		       {
	    		          column: "@{http://www.alfresco.org/model/content/1.0}name",
	    		          ascending: true
	    		       });
	    }

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
			searchQuery += " AND +TYPE:\"" + parsedArgs.itemType + "\"";
		}

		var filterParams = {
			query : searchQuery,
			limitResults : null,
			datatype : datatype,
			criteria : criteria,
			params : params,
			sort : sortColumns
		};

		// Max returned results specified?
		var argMax = args.max;
		if ((argMax !== null) && !isNaN(argMax)) {
			filterParams.limitResults = argMax;
		}

		return filterParams;
	}
};
