/**
 * Search component GET method
 */

/* beCPG : Get the export search templates */
function getExportSearchTpls(datatype)
{
   var reportTpls = [];

   try
   {            
	   var uri = "/becpg/report/exportsearch/templates/" + datatype;
		var connector = remote.connect("alfresco");
		var result = connector.get(uri);
      if (result.status.code == status.STATUS_OK && result != "{}")
      {
         var tpls = eval('(' + result.response + ')');
			reportTpls = tpls.reportTpls;
      }
   }
   catch (e)
   {
   }

   return reportTpls;
}

function main()
{
   // fetch the request params required by the search component template
   var siteId = (page.url.templateArgs["site"] != null) ? page.url.templateArgs["site"] : "";
   var siteTitle = null;
   if (siteId.length != 0)
   {
      // look for request scoped cached site title
      siteTitle = context.properties["site-title"];
      if (siteTitle == null)
      {
         // Call the repository for the site profile
         var json = remote.call("/api/sites/" + siteId);
         if (json.status == 200)
         {
            // Create javascript objects from the repo response
            var obj = eval('(' + json + ')');
            if (obj)
            {
               siteTitle = (obj.title.length != 0) ? obj.title : obj.shortName;
            }
         }
      }
   }
   
   // get the search sorting fields from the config
   var sortables = config.scoped["Search"]["sorting"].childrenMap["sort"];
   var sortFields = [];
   for (var i = 0, sort, label; i < sortables.size(); i++)
   {
      sort = sortables.get(i);
      
      // resolve label text
      label = sort.attributes["label"];
      if (label == null)
      {
         label = sort.attributes["labelId"];
         if (label != null)
         {
            label = msg.get(label);
         }
      }
      
      // create the model object to represent the sort field definition
      sortFields.push(
      {
         type: sort.value,
         label: label ? label : sort.value
      });
   }
   
   var metadatas = config.scoped["Search"]["metadata"].childrenMap["show"];
   var metadataFields = "";
   for (var i = 0, metadata; i < metadatas.size(); i++)
   {
	   if(metadataFields.length>0){
		   metadataFields+=",";
	   }
	   metadata = metadatas.get(i);
	   metadataFields+=metadata.attributes["id"].replace(":","_");
   }
   
   // Prepare the model
   var repoconfig = config.scoped['Search']['search'].getChildValue('repository-search');
   model.siteId = siteId;
   model.siteTitle = (siteTitle != null ? siteTitle : "");
   model.sortFields = sortFields;
   model.metadataFields = metadataFields;
   model.searchTerm = (page.url.args["t"] != null) ? page.url.args["t"] : "";
   model.searchTag = (page.url.args["tag"] != null) ? page.url.args["tag"] : "";
   model.searchSort = (page.url.args["s"] != null) ? page.url.args["s"] : "";
   // config override can force repository search on/off
   model.searchRepo = ((page.url.args["r"] == "true") || repoconfig == "always") && repoconfig != "none";
   model.searchAllSites = (page.url.args["a"] == "true" || siteId.length == 0);
   
   // Advanced search forms based json query
   model.searchQuery = (page.url.args["q"] != null) ? page.url.args["q"] : "";

	// datatype
	var datatype;
	if (model.searchQuery !== null && model.searchQuery.length !== 0)
	{
     var formJson = jsonUtils.toObject(model.searchQuery);
           
     if(formJson.length !== 0)
     {
   	  datatype = formJson.datatype;
     }
	}	

	// Export Search Tpls
	model.exportSearchTpls = getExportSearchTpls(datatype);
}

main();