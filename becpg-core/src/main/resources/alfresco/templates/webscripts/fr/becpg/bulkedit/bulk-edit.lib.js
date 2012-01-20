<import resource="classpath:/alfresco/templates/webscripts/fr/becpg/bulkedit/evaluator.lib.js">
<import resource="classpath:/alfresco/templates/webscripts/fr/becpg/bulkedit/filters.lib.js">
<import resource="classpath:/alfresco/templates/webscripts/fr/becpg/bulkedit/parse-args.lib.js">


function contains(a, obj) {
    for (var i = 0; i < a.length; i++) {
        if (a[i] == obj) {
            return true;
        }
    }
    return false;
}

/**
 * Main entry point: Return bulk edit data list with properties being supplied
 * in POSTed arguments
 * 
 * @method getBulkData
 */
var BulkEdit =
{

 getBulkData : function()
{
   // Use helper function to get the arguments
   var parsedArgs = ParseArgs.getParsedArgs();
   if (parsedArgs === null)
   {
      return;
   }

   // Try to find a filter query based on the passed-in arguments
   var filter = parsedArgs.filter,
      allNodes = [], node,
      items = [];
	   
   var filterParams = Filters.getFilterParams(filter, parsedArgs),
	      query = filterParams.query;

    var nodeRef = parsedArgs.nodeRef;
	if(nodeRef==null || nodeRef.length ==0){
	   // Query the nodes - passing in default sort and result limit
 	   // parameters
 	   query = null;
	}

	   allNodes = bSearch.queryAdvSearch(
			       query,
			   	   filterParams.datatype, 
			   	   filterParams.params.term, 
			   	   filterParams.params.tag, 
			   	   filterParams.criteria,
				   filterParams.params.repo, 
				   filterParams.params.siteId,  
				   filterParams.params.containerId,
				   filterParams.sort, 
				   (filterParams.limitResults ? parseInt(filterParams.limitResults, 10) : 0));
	
	
		var item = null,
			itemTypes = [],
			itemType = "";
		
		for each (node in allNodes) {
		    try { 
		    	  item = Evaluator.run(node, parsedArgs.fields);
		    	  itemType = item.type;
		    	  if(!contains(itemTypes,itemType)){
		    		  itemTypes.push(itemType);
		    	  }
		          items.push(item);
		    }  catch(e) {
		    	
		    }
		}
	
	
	   return (
	   {
	      fields: parsedArgs.fields,
	      luceneQuery: filterParams.query,
	      paging:
	      {
	         totalRecords: items.length,
	         startIndex: 0
	      },
	      items: items,
	      itemTypes : itemTypes
	   });
	}

}


