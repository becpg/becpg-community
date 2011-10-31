/**
 * BulkEdit component GET method
 */

function main()
{
	
   
   // get the bulk-edit types from the config
   var bulkEditables = config.scoped["bulk-edit"]["itemTypes"].childrenMap["itemType"];
   var itemTypes = [];
   for (var i = 0, itemType, label; i < bulkEditables.size(); i++)
   {
	   itemType = bulkEditables.get(i);
      
      // resolve label text
      label = itemType.attributes["label"];
      if (label == null)
      {
         label = itemType.attributes["labelId"];
         if (label != null)
         {
            label = msg.get(label);
         }
      }
      
      // create the model object to represent the sort field definition
      itemTypes.push(
      {
         name: itemType.attributes["name"],
         label: label ? label : itemType.attributes["name"]
      });
   }
   
   // Prepare the model
  
   // Advanced search forms based json query
   model.searchQuery = (page.url.args["q"] != null) ? page.url.args["q"] : "@cm\\\\:name:(test)";
   
   // the types
   model.itemTypes = itemTypes;
}



main();