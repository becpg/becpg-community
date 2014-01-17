<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

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

function getTypes()
{
   var myConfig = new XML(config.script),
      types = [];

   for each (var xmlType in myConfig..type)
   {
      types.push(
      {
         name: xmlType.@name.toString(),
         parameters: xmlType.@parameters.toString()
      });
   }
   return types;
}

/* Max Items */
function getMaxItems()
{
   var myConfig = new XML(config.script),
      maxItems = myConfig["max-items"];

   if (maxItems)
   {
      maxItems = myConfig["max-items"].toString();
   }
   return parseInt(maxItems && maxItems.length > 0 ? maxItems : 25, 10);
}
var site = page.url.templateArgs.site;
var prefs = "org.alfresco.share.product.catalog.dashlet";

if(site!=null && site.length>0){
   prefs+="."+site;
}

model.preferences = AlfrescoUtil.getPreferences(prefs);
model.filters = getFilters();
model.types = getTypes();
model.maxItems = getMaxItems();
