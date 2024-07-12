<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

function getTypes()
{
   var myConfig = new XML(config.script),
      types = [];

   for each (var xmlType in myConfig..type)
   {
      types.push(
      {
         name: xmlType.@name.toString(),
         parameters: xmlType.@parameters.toString(),
         filter: xmlType.@filter.toString()
      });
   }
   return types;
}

/* Get filters */
function getFilters()
{
   var myConfig = new XML(config.script),
      filters = [];

   for each (var xmlFilter in myConfig..filter)
   {
      filters.push(
      {
         type: xmlFilter.@type.toString(),
         parameters: xmlFilter.@parameters.toString()
      });
   }
   return filters;
}

function main()
{

   
	
	var site = page.url.templateArgs.site;
	var prefsId = "org.alfresco.share.product.list."+(site!= null && site.length>0 ? site : "home");
	
	model.preferences = AlfrescoUtil.getPreferences(prefsId);
	
	model.selectedType = page.url.args.list ? page.url.args.list: (preferences.type!=null ? preferences.type: "finishedProduct");

	
	// Widget instantiation metadata...
	var productListToolbar = {
	   id : "ProductListToolbar", 
	   name : "beCPG.component.ProductListToolbar",
	   options : {
	      siteId : (page.url.templateArgs.site != null) ? page.url.templateArgs.site : "",
	      selectedType: model.selectedType,
	      selectedFilter: preferences.filter!=null ? preferences.filter: "all" , 
		  filters: getFilters(),
		  types: getTypes(),
	      prefsId : prefsId
	   }
	};
	
	model.filters = getFilters();
	model.types = getTypes();
	model.widgets = [productListToolbar];
}


main();
