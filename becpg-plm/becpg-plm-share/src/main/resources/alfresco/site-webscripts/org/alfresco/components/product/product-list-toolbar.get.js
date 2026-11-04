<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">
<import resource="classpath:alfresco/site-webscripts/org/alfresco/components/product/product-list.lib.js">

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
	var prefsId = getProductListPrefsId(site);
	
	model.preferences = AlfrescoUtil.getPreferences(prefsId);
	
	model.selectedType = getProductListType(model.preferences);
	model.selectedFilter = page.url.args.filter ? page.url.args.filter : (model.preferences.filter!=null ? model.preferences.filter: "all");
	model.showThumbnails = model.preferences.showThumbnails !== null ? (model.preferences.showThumbnails) : false;

	
	// Widget instantiation metadata...
	var productListToolbar = {
	   id : "ProductListToolbar", 
	   name : "beCPG.component.ProductListToolbar",
	   options : {
	      siteId : (site != null) ? site : "",
	      selectedType: model.selectedType,
	      selectedFilter: model.selectedFilter,
		  showThumbnails: model.showThumbnails,
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
