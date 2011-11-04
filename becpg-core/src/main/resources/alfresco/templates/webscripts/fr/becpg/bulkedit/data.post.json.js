<import resource="classpath:/alfresco/templates/webscripts/fr/becpg/bulkedit/evaluator.lib.js">
<import resource="classpath:/alfresco/templates/webscripts/fr/becpg/bulkedit/filters.lib.js">
<import resource="classpath:/alfresco/templates/webscripts/fr/becpg/bulkedit/parse-args.lib.js">

/**
 * Main entry point: Return bulk edit data list with properties being supplied in POSTed arguments
 *
 * @method getData
 */
function getData()
{
   // Use helper function to get the arguments
   var parsedArgs = ParseArgs.getParsedArgs();
   if (parsedArgs === null)
   {
      return;
   }

   var fields = null;
   // Extract fields (if given)
   if (json.has("fields"))
   {
      // Convert the JSONArray object into a native JavaScript array
      fields = [];
      var jsonFields = json.get("fields"),
         numFields = jsonFields.length();
      
      for (count = 0; count < numFields; count++)
      {
         fields.push(jsonFields.get(count).replaceFirst("_", ":"));
      }
   }

   // Try to find a filter query based on the passed-in arguments
   var filter = parsedArgs.filter,
      allNodes = [], node,
      items = [];

   var filterParams = Filters.getFilterParams(filter, parsedArgs),
      query = filterParams.query;

   // Query the nodes - passing in default sort and result limit parameters
   if (query !== "")
   {
      allNodes = search.query(
      {
         query: query,
         language: filterParams.language,
         page:
         {
            maxItems: (filterParams.limitResults ? parseInt(filterParams.limitResults, 10) : 0)
         },
         sort: filterParams.sort,
         templates: filterParams.templates,
         namespace: (filterParams.namespace ? filterParams.namespace : null)
      });
      ///TODO  add type here
      for each (node in allNodes)
      {
         try {
             items.push(Evaluator.run(node, fields));
         }  catch(e) {}
      }
   }

   return (
   {
      fields: fields,
      luceneQuery: query,
      paging:
      {
         totalRecords: items.length,
         startIndex: 0
      },
      items: items
   });
}

model.data = getData();