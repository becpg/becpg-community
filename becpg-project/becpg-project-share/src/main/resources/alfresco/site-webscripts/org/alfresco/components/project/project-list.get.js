<import resource="classpath:alfresco/site-webscripts/org/alfresco/components/workflow/filter/filter.lib.js">
<import resource="classpath:alfresco/site-webscripts/org/alfresco/modules/entity-datagrid/include/actions.lib.js">
<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

function getFiltersMenu(viewId)
{
   var myConfig = new XML(config.script),
      filters = [];

   for each (var xmlFilter in myConfig..filtermenu)
   {
	   
	   var viewFilter = xmlFilter.@view.toString();
	   
	  if(viewFilter == "" || viewFilter.indexOf(viewId)> -1){
	      filters.push(
	      {
	         id: xmlFilter.@id.toString(),
	         data: xmlFilter.@data.toString()
	      });
	  }
   }
   return filters;
}



function main()
{
	
	var view = page.url.args.view ? page.url.args.view: "dataTable";
	
	var site = page.url.templateArgs.site;
	var prefs = "org.alfresco.share.project.list";

	if(site!=null && site.length>0){
	   prefs+="."+site;
	}

   var preferences = AlfrescoUtil.getPreferences(prefs);
  
   parseActions(view);
   
   var filterParameters =  getFilterParameters();
   model.filters = getFiltersMenu(view);
	

   //Widget instantiation metadata...
   var projectList = {
    id : "projectList", 
    name : "beCPG.component.ProjectList",
    initArgs: [ '"'+args.htmlid+'"' , '"'+view+'"' ],
    options : {
       siteId : (page.url.templateArgs.site != null) ? page.url.templateArgs.site : "",
       extraDataParams: page.url.templateArgs.site!=null ? "&repo=false&container=documentLibrary":"&repo=true",
       filterParameters : filterParameters,
       sortUrl :  page.url.context+"/proxy/alfresco/becpg/entity/datalists/sort/node",
       dataUrl : page.url.context+"/proxy/alfresco/becpg/entity/datalists/data/node",
       itemUrl : page.url.context+"/proxy/alfresco/becpg/entity/datalists/item/node/",
       simpleView : preferences.simpleView !=null ? preferences.simpleView : false,
       filter : preferences.filter
      }
   };
    
   model.widgets = [projectList];

   
}

main();
