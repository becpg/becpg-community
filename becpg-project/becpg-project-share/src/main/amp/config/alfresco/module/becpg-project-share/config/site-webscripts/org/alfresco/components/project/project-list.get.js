<import resource="classpath:alfresco/site-webscripts/org/alfresco/components/workflow/filter/filter.lib.js">
<import resource="classpath:alfresco/site-webscripts/org/alfresco/modules/entity-datagrid/include/actions.lib.js">

function main()
{
  
   parseActions();
   
   var filterParameters =  getFilterParameters();

   //Widget instantiation metadata...
   var projectList = {
    id : "projectList", 
    name : "beCPG.component.ProjectList",
    initArgs: [ '"'+args.htmlid+'"' , page.url.args.view ? '"'+page.url.args.view+'"': '"dataTable"' ],
    options : {
       siteId : (page.url.templateArgs.site != null) ? page.url.templateArgs.site : "",
       extraDataParams: page.url.templateArgs.site!=null ? "&repo=false&container=documentLibrary":"&repo=true",
       filterParameters : filterParameters,
       sortUrl :  page.url.context+"/proxy/alfresco/becpg/entity/datalists/sort/node",
       dataUrl : page.url.context+"/proxy/alfresco/becpg/entity/datalists/data/node",
       itemUrl : page.url.context+"/proxy/alfresco/becpg/entity/datalists/item/node/"
      }
   };
    
   model.widgets = [projectList];

   
}

main();
